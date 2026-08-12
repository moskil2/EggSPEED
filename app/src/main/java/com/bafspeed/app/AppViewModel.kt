package com.bafspeed.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bafspeed.app.protocol.BafangCommands
import com.bafspeed.app.protocol.BafangDecoder
import com.bafspeed.app.protocol.BafangValidation
import com.bafspeed.app.protocol.BafangWriter
import com.bafspeed.app.protocol.BasicSettings
import com.bafspeed.app.protocol.BbsFwAssistBaseType
import com.bafspeed.app.protocol.BbsFwAssistFlags
import com.bafspeed.app.protocol.BbsFwAssistLevel
import com.bafspeed.app.protocol.BbsFwAssistPasVariant
import com.bafspeed.app.protocol.BbsFwCommands
import com.bafspeed.app.protocol.BbsFwConfig
import com.bafspeed.app.protocol.BbsFwController
import com.bafspeed.app.protocol.BbsFwFrameParser
import com.bafspeed.app.protocol.BbsFwVersionInfo
import com.bafspeed.app.protocol.BbsFwWriteResponseParser
import com.bafspeed.app.protocol.BbsFwWriter
import com.bafspeed.app.protocol.ConfigFrameParser
import com.bafspeed.app.protocol.DisplayStateMachine
import com.bafspeed.app.protocol.EnergyAnalyzer
import com.bafspeed.app.protocol.EnergyAnalyzerSnapshot
import com.bafspeed.app.protocol.EnergyAnalyzerStore
import com.bafspeed.app.protocol.GeneralInfo
import com.bafspeed.app.protocol.PedalAssistSettings
import com.bafspeed.app.protocol.ScanResult
import com.bafspeed.app.protocol.ScanSnapshot
import com.bafspeed.app.protocol.Telemetry
import com.bafspeed.app.protocol.ThrottleSettings
import com.bafspeed.app.protocol.WHEEL_CIRCUMFERENCE_M
import com.bafspeed.app.protocol.WHEEL_SIZE_LABELS
import com.bafspeed.app.protocol.WriteResponseParser
import com.bafspeed.app.i18n.AppLanguage
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.profile.BbsFwProfileIo
import com.bafspeed.app.profile.ProfileData
import com.bafspeed.app.profile.ProfileIo
import com.bafspeed.app.serial.UsbSerialManager
import java.io.File
import kotlin.math.roundToInt
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

enum class ConnectionStatus { DISCONNECTED, SEARCHING, CONNECTING, CONNECTED, ERROR }

/**
 * Firmware sterownika po drugiej stronie kabla - decyduje, którego protokołu konfiguracji
 * używamy (patrz [BafangCommands] vs [BbsFwCommands]). bbs-fw (github.com/danielnilsson9/bbs-fw)
 * jawnie odrzuca ramki fabrycznego Bafang Config Tool (0x11+0x51..0x54) i ma własny protokół na
 * tym samym porcie 1200 baud - stąd osobna, równoległa ścieżka odczytu/zapisu w całej apce,
 * przełączana tym ustawieniem (zakładka Ustawienia). Tryb wyświetlacza (Kokpit/Diagnostyka)
 * NIE zależy od tego przełącznika - bbs-fw reimplementuje te same rejestry co OEM.
 */
enum class FirmwareType { OEM_BAFANG, BBS_FW }

/**
 * Stan automatycznego ponownego łączenia po zerwaniu połączenia podczas aktywnego Kokpitu
 * (patrz [AppViewModel.onSerialError]). RETRYING - w trakcie prób (maks. [AppViewModel.MAX_RECONNECT_ATTEMPTS]),
 * FAILED - wszystkie próby wyczerpane, IDLE - nic się nie dzieje (normalny stan).
 */
enum class AutoReconnectState { IDLE, RETRYING, FAILED }

enum class SpeedUnit(val label: String, val distanceLabel: String) {
    KMH("km/h", "km"), MPH("mph", "mi");
    fun fromKmh(kmh: Double): Double = if (this == MPH) kmh * 0.621371 else kmh
    fun toKmh(value: Double): Double = if (this == MPH) value / 0.621371 else value
}

/** Znane prefiksy modeli silników Bafang. */
private val KNOWN_MODEL_PREFIXES = listOf("BBS", "SZZ", "SW06") // BBS01/02/HD i warianty hub

/** Maks. liczba prób automatycznego ponownego łączenia po zerwaniu podczas aktywnego Kokpitu. */
private const val MAX_RECONNECT_ATTEMPTS = 3
/** Odczekanie przed PIERWSZĄ próbą auto-reconnect po zerwaniu połączenia. */
private const val RECONNECT_FIRST_DELAY_MS = 3000L
/** Odczekanie przed KAŻDĄ KOLEJNĄ próbą (2., 3.) po nieudanej poprzedniej. */
private const val RECONNECT_RETRY_DELAY_MS = 5000L

/** Odstęp między ponowieniami identyfikacji bbs-fw - jak oficjalny BBSFWTool.exe (Thread.Sleep(200)). */
private const val BBS_FW_IDENTIFY_RETRY_INTERVAL_MS = 200L
/** Maks. czas ponawiania identyfikacji bbs-fw zanim zgłosimy brak odpowiedzi (oficjalne narzędzie czeka 120s). */
private const val BBS_FW_IDENTIFY_TIMEOUT_MS = 30_000L

/** Podgląd ramki do zapisu - pokazywany użytkownikowi przed wysyłką (tryb dry-run). */
data class FramePreview(val blockName: String, val hex: String)

/** Stan przepływu zapisu do sterownika (Milestone 2). */
sealed interface WriteFlow {
    data object Idle : WriteFlow
    data class Confirming(val changes: List<String>, val frames: List<FramePreview>) : WriteFlow
    data class InProgress(val step: String) : WriteFlow
    data class Done(val success: Boolean, val message: String) : WriteFlow
}

data class UiState(
    val connection: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    /** Patrz [AutoReconnectState] - steruje ikoną "Łączę ponownie…"/"Połączenie nieudane" obok OFFLINE w Kokpicie. */
    val autoReconnectState: AutoReconnectState = AutoReconnectState.IDLE,
    val statusMessage: String = "",
    val deviceLabel: String? = null,
    val general: GeneralInfo? = null,
    // Konfiguracja robocza (podgląd/edycja lokalna). Odczyt ze sterownika nadpisuje te pola.
    val basic: BasicSettings? = null,
    val pedalAssist: PedalAssistSettings? = null,
    val throttle: ThrottleSettings? = null,
    val displayMode: Boolean = false,
    val assistLevel: Int = 0,
    val lightOn: Boolean = false,
    /** Tryb jazdy Normal/Sport (przycisk w Kokpicie) - ulotny jak assistLevel/lightOn, patrz BafangCommands.setOperationMode. */
    val sportMode: Boolean = false,
    /** Dystans trasy - trwały (SharedPreferences), zapisywany przy stopDisplayMode()/resetTrip(). Reset ręczny przyciskiem w Kokpicie. */
    val tripKm: Double = 0.0,
    /** Dystans przejechany W RUCHU od resetu prędkości średniej - licznik NIEZALEŻNY od Trip. */
    val avgSpeedDistanceKm: Double = 0.0,
    /** Czas W RUCHU [h] od resetu prędkości średniej - jw., dzielnik do avgSpeedKmh. */
    val avgSpeedMovingTimeH: Double = 0.0,
    val units: SpeedUnit = SpeedUnit.KMH,
    /** Język UI - trwały (SharedPreferences), domyślnie angielski. */
    val language: AppLanguage = AppLanguage.EN,
    /** Tryb testowy (przycisk TEST w Ustawieniach) - wymusza skrajne wartości na Kokpicie, żeby
     * sprawdzić czy mieszczą się na ekranie. Ulotny (nietrwały), tylko na czas sesji. */
    val testMode: Boolean = false,
    // ODO & Alarms, cellCount/capacityAh, currentCalibrationFactor, tripKm - jedyne pola UiState trwałe
    // między restartami/rozłączeniami (SharedPreferences, patrz AppViewModel.prefs). Reszta stanu jest
    // ulotna i wraca do wartości domyślnych.
    /** Przebieg przepisany ręcznie przez użytkownika (np. migracja z innego wyświetlacza) [km]. */
    val odoOffsetKm: Double = 0.0,
    /** Prognozowany zasięg [km] - patrz [com.bafspeed.app.protocol.EnergyAnalyzer]. Ulotne (przeliczane co tick z EnergyAnalyzer, który sam persystuje swój stan). */
    val predictedRangeKm: Double = 0.0,
    /** Średnie zużycie całej trasy od resetu [Wh/km]. */
    val tripAvgWhPerKm: Double = 0.0,
    /** Aktualne (krótkoterminowe, wygładzone) zużycie [Wh/km]. */
    val currentAvgWhPerKm: Double = 0.0,
    // Metadane baterii - liczba cel i pojemność, tylko po stronie aplikacji (do estymacji zasięgu),
    // nie wysyłane do sterownika. Trwałe (SharedPreferences, patrz AppViewModel.prefs) jak ODO/Alarms.
    val cellCount: Int = 13,
    val capacityAh: Double = 17.5,
    /** Współczynnik kalibracji odczytu prądu (np. sterowniki z shunt modem). 1.0 = brak kalibracji. */
    val currentCalibrationFactor: Double = 1.0,
    /** Ręczna korekta odczytu napięcia [V] (-5..+5, krok 0,1) - napięcie estymowane z % baterii bywa lekko przesunięte. */
    val voltageCalibrationOffsetV: Double = 0.0,
    /** Ostatni znany % baterii przed rozłączeniem - trwały (SharedPreferences), żeby nie znikał po rozłączeniu/restarcie apki. */
    val lastKnownBatteryPct: Int = 0,
    /** Ostatnie znane napięcie [V] przed rozłączeniem - jw. */
    val lastKnownVoltageV: Double = 0.0,
    // Ślad, że konfiguracja robocza różni się od odczytanej ze sterownika (edycja lub wczytany profil)
    val configDirty: Boolean = false,
    val profiles: List<String> = emptyList(),
    // Ostatnie wartości faktycznie odczytane ze sterownika - punkt odniesienia do porównań
    // przy zapisie (co się zmieniło) i do weryfikacji po zapisie (read-after-write).
    val lastReadBasic: BasicSettings? = null,
    val lastReadPedalAssist: PedalAssistSettings? = null,
    val lastReadThrottle: ThrottleSettings? = null,
    val writeFlow: WriteFlow = WriteFlow.Idle,
    /** Firmware sterownika - trwały (SharedPreferences), patrz [FirmwareType]. Zmiana wymaga rozłączenia. */
    val firmwareType: FirmwareType = FirmwareType.OEM_BAFANG,
    /** Wersja bbs-fw + typ sterownika (tylko odczyt) - przeżywa rozłączenie jak [general]. */
    val bbsFwVersion: BbsFwVersionInfo? = null,
    val bbsFwConfig: BbsFwConfig? = null,
    val lastReadBbsFwConfig: BbsFwConfig? = null,
) {
    val basicOrDefault: BasicSettings get() = basic ?: BasicSettings.DEFAULT
    val pasOrDefault: PedalAssistSettings get() = pedalAssist ?: PedalAssistSettings.DEFAULT
    val thrOrDefault: ThrottleSettings get() = throttle ?: ThrottleSettings.DEFAULT
    val bbsFwConfigOrDefault: BbsFwConfig get() = bbsFwConfig ?: BbsFwConfig.DEFAULT

    /** Napięcie nominalne pakietu wyliczone z liczby cel (3,7V/cela) - np. 14S → 52V. */
    val nominalPackVoltage: Int get() = (cellCount * 3.7).roundToInt()
    val capacityWh: Double get() = capacityAh * nominalPackVoltage

    /** Całkowity przebieg: ręcznie wpisany offset + dystans naliczony od uruchomienia Kokpitu. */
    val totalOdoKm: Double get() = odoOffsetKm + tripKm

    /** Średnia prędkość [km/h] od resetu, liczona TYLKO z czasu i dystansu w ruchu (postoje nie zaniżają średniej). */
    val avgSpeedKmh: Double get() = if (avgSpeedMovingTimeH > 0.0) avgSpeedDistanceKm / avgSpeedMovingTimeH else 0.0
}

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val serial = UsbSerialManager(app)
    private val configParser = ConfigFrameParser()
    private val writeResponseParser = WriteResponseParser()
    private val bbsFwFrameParser = BbsFwFrameParser()
    private val bbsFwWriteResponseParser = BbsFwWriteResponseParser()

    // Jedyny trwały zapis w apce (poza plikami .ini profili) - ODO, metadane baterii i stan
    // EnergyAnalyzer, celowo osobno od reszty stanu, który ma zostać ulotny (patrz UiState.odoOffsetKm i inne).
    private val prefs = app.getSharedPreferences("eggspeed_prefs", android.content.Context.MODE_PRIVATE)

    /** Adapter SharedPreferences dla [EnergyAnalyzer] - klasa algorytmu zostaje czystym Kotlinem/testowalna. */
    private class PrefsEnergyStore(private val prefs: android.content.SharedPreferences) : EnergyAnalyzerStore {
        override fun load(): EnergyAnalyzerSnapshot? {
            if (!prefs.contains("energy_historical_avg_wh_km")) return null
            return EnergyAnalyzerSnapshot(
                historicalAvgWhPerKm = prefs.getFloat("energy_historical_avg_wh_km", 12f).toDouble(),
                historicalKm = prefs.getFloat("energy_historical_km", 0f).toDouble(),
                tripDistanceKm = prefs.getFloat("energy_trip_distance_km", 0f).toDouble(),
                tripEnergyWh = prefs.getFloat("energy_trip_energy_wh", 0f).toDouble(),
                shortTermAvgWhPerKm = prefs.getFloat("energy_short_term_wh_km", 12f).toDouble(),
                mediumTermAvgWhPerKm = prefs.getFloat("energy_medium_term_wh_km", 12f).toDouble(),
                lastBatteryPct = prefs.getInt("energy_last_battery_pct", -1),
            )
        }

        override fun save(snapshot: EnergyAnalyzerSnapshot) {
            prefs.edit()
                .putFloat("energy_historical_avg_wh_km", snapshot.historicalAvgWhPerKm.toFloat())
                .putFloat("energy_historical_km", snapshot.historicalKm.toFloat())
                .putFloat("energy_trip_distance_km", snapshot.tripDistanceKm.toFloat())
                .putFloat("energy_trip_energy_wh", snapshot.tripEnergyWh.toFloat())
                .putFloat("energy_short_term_wh_km", snapshot.shortTermAvgWhPerKm.toFloat())
                .putFloat("energy_medium_term_wh_km", snapshot.mediumTermAvgWhPerKm.toFloat())
                .putInt("energy_last_battery_pct", snapshot.lastBatteryPct)
                .apply()
        }
    }

    private val energyAnalyzer = EnergyAnalyzer(PrefsEnergyStore(prefs))

    /**
     * Ostatnio odczytane dane sterownika (GEN/BAS/PAS/THR) przeżywają zamknięcie aplikacji -
     * użytkownik ma wgląd np. do wersji firmware nawet bez podłączonego sterownika. Ręczny
     * wzorzec zapisu (bez biblioteki JSON) - listy jako stringi rozdzielone przecinkami.
     */
    private fun saveGeneral(gen: GeneralInfo) {
        prefs.edit()
            .putString("gen_manufacturer", gen.manufacturer)
            .putString("gen_model", gen.model)
            .putString("gen_hw_version", gen.hardwareVersion)
            .putString("gen_fw_version", gen.firmwareVersion)
            .putString("gen_nominal_voltage", gen.nominalVoltage)
            .putInt("gen_max_current_a", gen.maxCurrentA)
            .apply()
    }

    private fun loadGeneral(): GeneralInfo? {
        val manufacturer = prefs.getString("gen_manufacturer", null) ?: return null
        return GeneralInfo(
            manufacturer = manufacturer,
            model = prefs.getString("gen_model", "") ?: "",
            hardwareVersion = prefs.getString("gen_hw_version", "") ?: "",
            firmwareVersion = prefs.getString("gen_fw_version", "") ?: "",
            nominalVoltage = prefs.getString("gen_nominal_voltage", "") ?: "",
            maxCurrentA = prefs.getInt("gen_max_current_a", 0),
        )
    }

    private fun saveBasic(bas: BasicSettings) {
        prefs.edit()
            .putInt("bas_lbp", bas.lowBatteryProtection)
            .putInt("bas_lc", bas.currentLimit)
            .putString("bas_alc", bas.assistCurrentPct.joinToString(","))
            .putString("bas_albp", bas.assistSpeedPct.joinToString(","))
            .putInt("bas_wd", bas.wheelDiameterCode)
            .putInt("bas_smm", bas.speedMeterModel)
            .putInt("bas_sms", bas.speedMeterSignals)
            .apply()
    }

    private fun loadBasic(): BasicSettings? {
        val alc = prefs.getString("bas_alc", null) ?: return null
        return BasicSettings(
            lowBatteryProtection = prefs.getInt("bas_lbp", BasicSettings.DEFAULT.lowBatteryProtection),
            currentLimit = prefs.getInt("bas_lc", BasicSettings.DEFAULT.currentLimit),
            assistCurrentPct = alc.split(",").map { it.toInt() },
            assistSpeedPct = (prefs.getString("bas_albp", null) ?: "").split(",").map { it.toInt() },
            wheelDiameterCode = prefs.getInt("bas_wd", BasicSettings.DEFAULT.wheelDiameterCode),
            speedMeterModel = prefs.getInt("bas_smm", BasicSettings.DEFAULT.speedMeterModel),
            speedMeterSignals = prefs.getInt("bas_sms", BasicSettings.DEFAULT.speedMeterSignals),
        )
    }

    private fun savePas(pas: PedalAssistSettings) {
        prefs.edit()
            .putInt("pas_pt", pas.pedalType)
            .putInt("pas_da", pas.designatedAssist)
            .putInt("pas_sl", pas.speedLimit)
            .putInt("pas_sc", pas.startCurrentPct)
            .putInt("pas_ssm", pas.slowStartMode)
            .putInt("pas_sdn", pas.startDegree)
            .putInt("pas_wm", pas.workMode)
            .putInt("pas_ts", pas.timeOfStop)
            .putInt("pas_cd", pas.currentDecay)
            .putInt("pas_sd", pas.stopDecay)
            .putInt("pas_kc", pas.keepCurrentPct)
            .putBoolean("pas_present", true)
            .apply()
    }

    private fun loadPas(): PedalAssistSettings? {
        if (!prefs.getBoolean("pas_present", false)) return null
        return PedalAssistSettings(
            pedalType = prefs.getInt("pas_pt", PedalAssistSettings.DEFAULT.pedalType),
            designatedAssist = prefs.getInt("pas_da", PedalAssistSettings.DEFAULT.designatedAssist),
            speedLimit = prefs.getInt("pas_sl", PedalAssistSettings.DEFAULT.speedLimit),
            startCurrentPct = prefs.getInt("pas_sc", PedalAssistSettings.DEFAULT.startCurrentPct),
            slowStartMode = prefs.getInt("pas_ssm", PedalAssistSettings.DEFAULT.slowStartMode),
            startDegree = prefs.getInt("pas_sdn", PedalAssistSettings.DEFAULT.startDegree),
            workMode = prefs.getInt("pas_wm", PedalAssistSettings.DEFAULT.workMode),
            timeOfStop = prefs.getInt("pas_ts", PedalAssistSettings.DEFAULT.timeOfStop),
            currentDecay = prefs.getInt("pas_cd", PedalAssistSettings.DEFAULT.currentDecay),
            stopDecay = prefs.getInt("pas_sd", PedalAssistSettings.DEFAULT.stopDecay),
            keepCurrentPct = prefs.getInt("pas_kc", PedalAssistSettings.DEFAULT.keepCurrentPct),
        )
    }

    private fun saveBbsFwVersion(v: BbsFwVersionInfo) {
        prefs.edit()
            .putInt("bbsfw_major", v.major)
            .putInt("bbsfw_minor", v.minor)
            .putInt("bbsfw_patch", v.patch)
            .putInt("bbsfw_config_version", v.configVersion)
            .putInt("bbsfw_ctrl_type", v.ctrlType)
            .apply()
    }

    private fun loadBbsFwVersion(): BbsFwVersionInfo? {
        if (!prefs.contains("bbsfw_config_version")) return null
        return BbsFwVersionInfo(
            major = prefs.getInt("bbsfw_major", 0),
            minor = prefs.getInt("bbsfw_minor", 0),
            patch = prefs.getInt("bbsfw_patch", 0),
            configVersion = prefs.getInt("bbsfw_config_version", 0),
            ctrlType = prefs.getInt("bbsfw_ctrl_type", 0),
        )
    }

    /** Cała struktura config_t zapisana jako jeden string bajtów - prostsze niż pole-po-polu (34+120 pól). */
    private fun saveBbsFwConfig(c: BbsFwConfig) {
        prefs.edit().putString("bbsfw_config_raw", c.serialize().joinToString(",")).apply()
    }

    private fun loadBbsFwConfig(): BbsFwConfig? {
        val raw = prefs.getString("bbsfw_config_raw", null) ?: return null
        return runCatching { BbsFwConfig.deserialize(raw.split(",").map { it.toInt() }) }.getOrNull()
    }

    private fun saveThr(thr: ThrottleSettings) {
        prefs.edit()
            .putInt("thr_sv", thr.startVoltage)
            .putInt("thr_ev", thr.endVoltage)
            .putInt("thr_mode", thr.mode)
            .putInt("thr_da", thr.designatedAssist)
            .putInt("thr_sl", thr.speedLimit)
            .putInt("thr_sc", thr.startCurrentPct)
            .putBoolean("thr_present", true)
            .apply()
    }

    private fun loadThr(): ThrottleSettings? {
        if (!prefs.getBoolean("thr_present", false)) return null
        return ThrottleSettings(
            startVoltage = prefs.getInt("thr_sv", ThrottleSettings.DEFAULT.startVoltage),
            endVoltage = prefs.getInt("thr_ev", ThrottleSettings.DEFAULT.endVoltage),
            mode = prefs.getInt("thr_mode", ThrottleSettings.DEFAULT.mode),
            designatedAssist = prefs.getInt("thr_da", ThrottleSettings.DEFAULT.designatedAssist),
            speedLimit = prefs.getInt("thr_sl", ThrottleSettings.DEFAULT.speedLimit),
            startCurrentPct = prefs.getInt("thr_sc", ThrottleSettings.DEFAULT.startCurrentPct),
        )
    }

    private val _state = MutableStateFlow(
        UiState(
            language = runCatching { AppLanguage.valueOf(prefs.getString("language", AppLanguage.EN.name)!!) }.getOrDefault(AppLanguage.EN),
            odoOffsetKm = prefs.getFloat("odo_offset_km", 0f).toDouble(),
            cellCount = prefs.getInt("cell_count", 13),
            capacityAh = prefs.getFloat("capacity_ah", 17.5f).toDouble(),
            currentCalibrationFactor = prefs.getFloat("current_calibration_factor", 1.0f).toDouble(),
            voltageCalibrationOffsetV = prefs.getFloat("voltage_calibration_offset_v", 0f).toDouble(),
            lastKnownBatteryPct = prefs.getInt("last_known_battery_pct", 0),
            lastKnownVoltageV = prefs.getFloat("last_known_voltage_v", 0f).toDouble(),
            tripKm = prefs.getFloat("trip_km", 0f).toDouble(),
            avgSpeedDistanceKm = prefs.getFloat("avg_speed_distance_km", 0f).toDouble(),
            avgSpeedMovingTimeH = prefs.getFloat("avg_speed_moving_time_h", 0f).toDouble(),
            general = loadGeneral(),
            basic = loadBasic(),
            lastReadBasic = loadBasic(),
            pedalAssist = loadPas(),
            lastReadPedalAssist = loadPas(),
            throttle = loadThr(),
            lastReadThrottle = loadThr(),
            firmwareType = runCatching { FirmwareType.valueOf(prefs.getString("firmware_type", FirmwareType.OEM_BAFANG.name)!!) }.getOrDefault(FirmwareType.OEM_BAFANG),
            bbsFwVersion = loadBbsFwVersion(),
            bbsFwConfig = loadBbsFwConfig(),
            lastReadBbsFwConfig = loadBbsFwConfig(),
        ),
    )
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val display = DisplayStateMachine(viewModelScope) { bytes ->
        withContext(Dispatchers.IO) { runCatching { serial.write(bytes) } }
    }
    val telemetry: StateFlow<Telemetry> = display.telemetry
    val scanResults: StateFlow<List<ScanResult>> = display.scanResults
    val scanProgress: StateFlow<Int> = display.scanProgress
    val scanning: StateFlow<Boolean> = display.scanning
    val scanHistory: StateFlow<List<ScanSnapshot>> = display.scanHistory
    val fullScanHistory: StateFlow<List<ScanSnapshot>> = display.fullScanHistory

    private var tripJob: Job? = null
    private var configTimeoutJob: Job? = null
    /** Pętla ponawiająca identyfikację bbs-fw (READ_FW_VERSION) - patrz [startBbsFwIdentifyRetry]. */
    private var identifyRetryJob: Job? = null
    /** Licznik prób w trwającej sekwencji auto-reconnect (patrz [AutoReconnectState], [handleConnectFailure]). */
    private var reconnectAttempt = 0

    // Routing przychodzących bajtów podczas zapisu/weryfikacji (wyłącza się nawzajem
    // z displayMode i ze zwykłym trybem odczytu konfiguracji - patrz onSerialData).
    private var writeAckMode = false
    private var pendingAck: CompletableDeferred<WriteResponseParser.Result.Ack>? = null
    private var verifyMode = false
    private var pendingVerifyFrame: CompletableDeferred<ConfigFrameParser.Result.Frame>? = null
    /** Odpowiedniki pendingAck/pendingVerifyFrame dla ścieżki bbs-fw - patrz [FirmwareType.BBS_FW]. */
    private var pendingBbsFwAck: CompletableDeferred<BbsFwWriteResponseParser.Result.Ack>? = null
    private var pendingBbsFwVerifyFrame: CompletableDeferred<BbsFwFrameParser.Result.ConfigFrame>? = null

    /**
     * Połączenie: uprawnienia → otwarcie portu → odczyt GEN → BAS/PAS/THR (tylko odczyt).
     * @param isAutoRetry true gdy to kolejna próba w sekwencji auto-reconnect (patrz [onSerialError],
     * [handleConnectFailure]) - wtedy NIE resetujemy licznika prób ani [AutoReconnectState].
     */
    fun connect(isAutoRetry: Boolean = false) {
        viewModelScope.launch {
            if (!isAutoRetry) {
                reconnectAttempt = 0
                _state.value = _state.value.copy(autoReconnectState = AutoReconnectState.IDLE)
            }
            val lang = _state.value.language
            _state.value = _state.value.copy(connection = ConnectionStatus.SEARCHING, statusMessage = tr(lang, "Szukam mostka USB…", "Searching for USB bridge…"))
            val granted = serial.requestPermission()
            if (!granted) {
                handleConnectFailure(tr(lang, "Brak zgody na dostęp USB lub brak kabla", "No permission for USB access or no cable"))
                return@launch
            }
            try {
                withContext(Dispatchers.IO) {
                    // Oficjalny BBSFWTool.exe nigdy nie ustawia DTR/RTS (zostają false) - u nas
                    // wymuszanie ich na true dla bbs-fw przeszkadzało w nawiązaniu połączenia.
                    serial.open(
                        onData = ::onSerialData,
                        onError = { onSerialError(it) },
                        assertDtrRts = _state.value.firmwareType == FirmwareType.OEM_BAFANG,
                    )
                }
            } catch (e: Exception) {
                handleConnectFailure(e.message ?: tr(lang, "Błąd otwarcia portu", "Error opening port"))
                return@launch
            }
            _state.value = _state.value.copy(
                connection = ConnectionStatus.CONNECTING,
                deviceLabel = serial.deviceLabel,
                statusMessage = tr(lang, "Port otwarty (1200 baud) - identyfikuję sterownik…", "Port open (1200 baud) - identifying controller…"),
            )
            configParser.reset()
            bbsFwFrameParser.reset()
            when (_state.value.firmwareType) {
                FirmwareType.OEM_BAFANG -> sendConfigRead(BafangCommands.READ_GEN)
                FirmwareType.BBS_FW -> startBbsFwIdentifyRetry()
            }
        }
    }

    /**
     * Wywoływane w każdym miejscu, gdzie próba połączenia się nie powiodła (brak uprawnień,
     * wyjątek przy otwarciu portu, brak odpowiedzi sterownika - timeout). Jeśli jesteśmy w trakcie
     * sekwencji auto-reconnect ([AutoReconnectState.RETRYING]) i zostały jeszcze próby, czeka
     * [RECONNECT_RETRY_DELAY_MS] i próbuje ponownie (bez pokazywania pośredniego błędu) - port USB
     * potrzebuje chwili, żeby się w pełni zwolnić po poprzedniej nieudanej próbie, więc próba "na
     * styk" (bez odczekania) zwykle też się nie udaje. Inaczej ustawia stan błędu na stałe.
     */
    private suspend fun handleConnectFailure(message: String) {
        val wasRetrying = _state.value.autoReconnectState == AutoReconnectState.RETRYING
        if (wasRetrying && reconnectAttempt < MAX_RECONNECT_ATTEMPTS) {
            runCatching { serial.close() }
            _state.value = _state.value.copy(connection = ConnectionStatus.DISCONNECTED, autoReconnectState = AutoReconnectState.IDLE)
            scheduleReconnectAttempt(RECONNECT_RETRY_DELAY_MS)
        } else {
            _state.value = _state.value.copy(
                connection = ConnectionStatus.ERROR,
                autoReconnectState = if (wasRetrying) AutoReconnectState.FAILED else _state.value.autoReconnectState,
                statusMessage = if (wasRetrying) {
                    tr(_state.value.language, "Połączenie nieudane po $MAX_RECONNECT_ATTEMPTS próbach", "Connection failed after $MAX_RECONNECT_ATTEMPTS attempts")
                } else message,
            )
        }
    }

    /**
     * Czeka [delayMs], po czym oznacza kolejną próbę w sekwencji auto-reconnect (patrz
     * [AutoReconnectState.RETRYING], pokazywane w Kokpicie jako niebieski napis "Connecting")
     * i faktycznie próbuje połączyć. Wspólna dla pierwszej próby ([onSerialError]) i kolejnych
     * ([handleConnectFailure]) - różni je tylko długość odczekania.
     */
    private suspend fun scheduleReconnectAttempt(delayMs: Long) {
        delay(delayMs)
        reconnectAttempt++
        _state.value = _state.value.copy(
            connection = ConnectionStatus.SEARCHING,
            statusMessage = tr(
                _state.value.language,
                "Łączę ponownie ($reconnectAttempt/$MAX_RECONNECT_ATTEMPTS)…",
                "Reconnecting ($reconnectAttempt/$MAX_RECONNECT_ATTEMPTS)…",
            ),
            autoReconnectState = AutoReconnectState.RETRYING,
        )
        connect(isAutoRetry = true)
    }

    /**
     * Zapisuje ostatni znany % baterii i napięcie do SharedPreferences - przeżywa rozłączenie
     * i restart aplikacji, żeby użytkownik nie widział "0" tam, gdzie ostatnio było np. 68%.
     * Zapisuje tylko gdy mamy realny odczyt (batteryPct > 0) - nie chcemy nadpisać dobrej,
     * ostatnio znanej wartości zerem z jeszcze nie w pełni zainicjowanej telemetrii.
     */
    private fun saveLastKnownTelemetry() {
        val t = telemetry.value
        if (t.batteryPct <= 0) return
        _state.value = _state.value.copy(lastKnownBatteryPct = t.batteryPct, lastKnownVoltageV = t.voltageV)
        prefs.edit()
            .putInt("last_known_battery_pct", t.batteryPct)
            .putFloat("last_known_voltage_v", t.voltageV.toFloat())
            .apply()
    }

    /**
     * Rozłączenie: zeruje TYLKO stan sesji (poziom wspomagania, światło, tryb wyświetlacza itd.).
     * Dane sterownika (general/basic/pedalAssist/throttle) celowo ZOSTAJĄ widoczne (nieaktualne,
     * ale ostatnio znane) - użytkownik ma do nich wgląd nawet bez połączenia, patrz saveGeneral() itp.
     * Kolejne połączenie i tak odświeży je świeżym odczytem.
     */
    fun disconnect() {
        val s = _state.value
        viewModelScope.launch {
            // Bezpieczenstwo: zanim faktycznie zamkniemy port, jesli Kokpit byl aktywny
            // (silnik mogl miec ustawione wspomaganie/swiatlo/tryb Sport), wysylamy te same ulotne
            // komendy co fabryczny wyswietlacz - wspomaganie na 0 zawsze, swiatlo OFF i tryb Normal
            // tylko jesli byly wlaczone - zeby rower nie zostal z wlaczonym silnikiem/swiatlem/Sportem.
            if (s.displayMode && s.connection == ConnectionStatus.CONNECTED) {
                runCatching {
                    withContext(Dispatchers.IO) {
                        serial.write(BafangCommands.setAssistLevel(0))
                        delay(100)
                        if (s.lightOn) {
                            serial.write(BafangCommands.LIGHT_OFF)
                            delay(100)
                        }
                        if (s.sportMode) {
                            serial.write(BafangCommands.setOperationMode(false))
                            delay(100)
                        }
                    }
                }
            }
            saveLastKnownTelemetry()
            stopDisplayMode()
            serial.close()
            configTimeoutJob?.cancel()
            identifyRetryJob?.cancel()
            val cur = _state.value
            _state.value = UiState(
                language = cur.language,
                odoOffsetKm = cur.odoOffsetKm,
                cellCount = cur.cellCount,
                capacityAh = cur.capacityAh,
                currentCalibrationFactor = cur.currentCalibrationFactor,
                voltageCalibrationOffsetV = cur.voltageCalibrationOffsetV,
                tripKm = cur.tripKm,
                avgSpeedDistanceKm = cur.avgSpeedDistanceKm,
                avgSpeedMovingTimeH = cur.avgSpeedMovingTimeH,
                general = cur.general,
                basic = cur.basic,
                lastReadBasic = cur.lastReadBasic,
                pedalAssist = cur.pedalAssist,
                lastReadPedalAssist = cur.lastReadPedalAssist,
                throttle = cur.throttle,
                lastReadThrottle = cur.lastReadThrottle,
                configDirty = cur.configDirty,
                lastKnownBatteryPct = cur.lastKnownBatteryPct,
                lastKnownVoltageV = cur.lastKnownVoltageV,
                firmwareType = cur.firmwareType,
                bbsFwVersion = cur.bbsFwVersion,
                bbsFwConfig = cur.bbsFwConfig,
                lastReadBbsFwConfig = cur.lastReadBbsFwConfig,
            )
        }
    }

    /**
     * Zmiana firmware sterownika (OEM Bafang / bbs-fw) - protokoły odczytu/zapisu konfiguracji
     * są całkowicie różne (patrz [FirmwareType]), więc zmiana w trakcie połączenia rozłącza
     * najpierw, żeby nie zostawić parserów w niespójnym stanie z ramkami "starego" protokołu.
     */
    fun setFirmwareType(type: FirmwareType) {
        if (_state.value.firmwareType == type) return
        if (_state.value.connection != ConnectionStatus.DISCONNECTED) disconnect()
        _state.value = _state.value.copy(firmwareType = type)
        prefs.edit().putString("firmware_type", type.name).apply()
    }

    // --- ODO & Alarms (jedyne trwałe ustawienia w apce - zapisywane w SharedPreferences) ---
    fun setOdoOffsetKm(km: Double) {
        val clamped = km.coerceIn(0.0, 999_999.0)
        _state.value = _state.value.copy(odoOffsetKm = clamped)
        prefs.edit().putFloat("odo_offset_km", clamped.toFloat()).apply()
    }

    /**
     * Zeruje licznik trasy (Trip) w Kokpicie - nie wpływa na ręcznie wpisany offset ODO.
     * Celowo NIE resetuje [EnergyAnalyzer] - jego własna "trasa" (używana do prognozy zasięgu
     * i średniego zużycia) resetuje się wyłącznie przy wykryciu pełnego naładowania (patrz brief),
     * niezależnie od tego licznika kilometrów.
     */
    fun resetTrip() {
        _state.value = _state.value.copy(tripKm = 0.0)
        prefs.edit().putFloat("trip_km", 0f).apply()
    }

    /** Zeruje licznik prędkości średniej (Kokpit) - niezależny od resetu TRIP. */
    fun resetAvgSpeed() {
        _state.value = _state.value.copy(avgSpeedDistanceKm = 0.0, avgSpeedMovingTimeH = 0.0)
        prefs.edit().putFloat("avg_speed_distance_km", 0f).putFloat("avg_speed_moving_time_h", 0f).apply()
    }

    /** Ponowny odczyt wszystkich bloków konfiguracji (tylko odczyt). */
    fun readAllConfig() {
        if (_state.value.connection != ConnectionStatus.CONNECTED) return
        if (_state.value.displayMode) return
        viewModelScope.launch {
            when (_state.value.firmwareType) {
                FirmwareType.OEM_BAFANG -> {
                    configParser.reset()
                    sendConfigRead(BafangCommands.READ_BAS)
                }
                FirmwareType.BBS_FW -> {
                    bbsFwFrameParser.reset()
                    sendConfigRead(BbsFwCommands.READ_CONFIG)
                }
            }
        }
    }

    /** Eksperymentalny pełny skan rejestrów 0x00-0xFF (zakładka Diagnostyka) - patrz DisplayStateMachine.runFullScan(). */
    fun startFullDiagnosticScan() {
        if (_state.value.connection != ConnectionStatus.CONNECTED) return
        viewModelScope.launch { display.runFullScan() }
    }

    /** Szybka migawka 13 kandydatów (przycisk w Kokpicie) - patrz DisplayStateMachine.takeCandidateSnapshot(). */
    fun takeRegisterSnapshot() {
        if (_state.value.connection != ConnectionStatus.CONNECTED) return
        viewModelScope.launch { display.takeCandidateSnapshot() }
    }

    fun startDisplayMode() {
        if (_state.value.connection != ConnectionStatus.CONNECTED) return
        _state.value = _state.value.copy(displayMode = true)
        display.assistLevel = _state.value.assistLevel
        display.lightOn = _state.value.lightOn
        display.sportMode = _state.value.sportMode
        display.currentCalibrationFactor = _state.value.currentCalibrationFactor
        display.voltageCalibrationOffsetV = _state.value.voltageCalibrationOffsetV
        display.lowBatteryProtectionV = _state.value.basicOrDefault.lowBatteryProtection
        display.cellCount = _state.value.cellCount
        display.useRealVoltage = _state.value.firmwareType == FirmwareType.BBS_FW
        display.start()
        startTripIntegration()
    }

    fun stopDisplayMode() {
        display.stop()
        tripJob?.cancel()
        _state.value = _state.value.copy(displayMode = false)
        prefs.edit()
            .putFloat("trip_km", _state.value.tripKm.toFloat())
            .putFloat("avg_speed_distance_km", _state.value.avgSpeedDistanceKm.toFloat())
            .putFloat("avg_speed_moving_time_h", _state.value.avgSpeedMovingTimeH.toFloat())
            .apply()
        energyAnalyzer.saveState()
    }

    fun setAssistLevel(level: Int) {
        val l = level.coerceIn(0, 9)
        display.assistLevel = l
        _state.value = _state.value.copy(assistLevel = l)
    }

    fun setLight(on: Boolean) {
        display.lightOn = on
        _state.value = _state.value.copy(lightOn = on)
    }

    fun setSportMode(on: Boolean) {
        display.sportMode = on
        _state.value = _state.value.copy(sportMode = on)
    }

    fun setUnits(units: SpeedUnit) {
        _state.value = _state.value.copy(units = units)
    }

    fun setLanguage(language: AppLanguage) {
        _state.value = _state.value.copy(language = language)
        prefs.edit().putString("language", language.name).apply()
    }

    fun toggleTestMode() {
        _state.value = _state.value.copy(testMode = !_state.value.testMode)
    }

    // --- Edycja lokalna konfiguracji (M1: WYŁĄCZNIE podgląd, nic nie leci do sterownika) ---

    private fun editBasic(transform: (BasicSettings) -> BasicSettings) {
        val cur = _state.value.basicOrDefault
        _state.value = _state.value.copy(basic = transform(cur), configDirty = true)
    }

    private fun editPas(transform: (PedalAssistSettings) -> PedalAssistSettings) {
        val cur = _state.value.pasOrDefault
        _state.value = _state.value.copy(pedalAssist = transform(cur), configDirty = true)
    }

    private fun editThr(transform: (ThrottleSettings) -> ThrottleSettings) {
        val cur = _state.value.thrOrDefault
        _state.value = _state.value.copy(throttle = transform(cur), configDirty = true)
    }

    fun setAssistLevelCurrent(level: Int, pct: Int) = editBasic { b ->
        b.copy(assistCurrentPct = b.assistCurrentPct.toMutableList().also { it[level] = pct.coerceIn(0, 100) })
    }

    fun setAssistLevelSpeed(level: Int, pct: Int) = editBasic { b ->
        b.copy(assistSpeedPct = b.assistSpeedPct.toMutableList().also { it[level] = pct.coerceIn(0, 100) })
    }

    fun setWheelSize(code: Int) = editBasic { it.copy(wheelDiameterCode = code.coerceIn(0, WHEEL_CIRCUMFERENCE_M.size - 1)) }
    fun setCurrentLimit(a: Int) = editBasic { it.copy(currentLimit = a.coerceIn(1, 100)) }
    fun setLowBatteryProtection(v: Int) {
        editBasic { it.copy(lowBatteryProtection = v.coerceIn(0, 100)) }
        display.lowBatteryProtectionV = _state.value.basicOrDefault.lowBatteryProtection
    }
    fun setSpeedMeterModel(v: Int) = editBasic { it.copy(speedMeterModel = v.coerceIn(0, 2)) }
    fun setSpeedMeterSignals(v: Int) = editBasic { it.copy(speedMeterSignals = v.coerceIn(0, 63)) }
    fun setPasSpeedLimit(v: Int) = editPas { it.copy(speedLimit = v.coerceIn(0, 26)) }
    fun setThrottleMode(mode: Int) = editThr { it.copy(mode = mode) }
    fun setThrottleDesignatedAssist(level: Int) = editThr { it.copy(designatedAssist = level.coerceIn(0, 10)) }
    fun setCellCount(n: Int) {
        val clamped = n.coerceIn(1, 30)
        _state.value = _state.value.copy(cellCount = clamped)
        prefs.edit().putInt("cell_count", clamped).putBoolean("cell_count_locked", true).apply()
        display.cellCount = clamped
    }

    /**
     * Auto-detekcja liczby cel z bloku GEN - działa TYLKO dopóki użytkownik nie ustawił liczby
     * cel ręcznie (patrz [setCellCount], które trwale ustawia "cell_count_locked"). Uruchamiana
     * przy każdym udanym odczycie GEN, ale efektywnie jest jednorazowa - po pierwszej próbie
     * (niezależnie od wyniku) blokujemy się na stałe, żeby nie nadpisywać późniejszej ręcznej zmiany.
     *
     * Bafang (rodzina BBS03/BBSHD) potrafi zgłosić w GEN napięcie 48V nawet dla wariantu 28A,
     * który w rzeczywistości jest pakietem 52V (14S) - stąd jawne wyjątki zamiast ślepego
     * przeliczenia zgłoszonego napięcia.
     */
    private fun maybeAutoDetectCellCount(gen: GeneralInfo) {
        if (prefs.getBoolean("cell_count_locked", false)) return
        val detected = when {
            gen.maxCurrentA == 28 && gen.nominalVoltage == "48" -> 14 // realnie 52V, Bafang zglasza blednie 48V
            gen.maxCurrentA == 30 && gen.nominalVoltage == "48" -> 13 // 48V - zgodne ze zgloszeniem
            else -> gen.nominalVoltage.toIntOrNull()?.let { v -> (v / 3.7).roundToInt() }
        }
        if (detected != null) {
            _state.value = _state.value.copy(cellCount = detected.coerceIn(1, 30))
            prefs.edit().putInt("cell_count", detected.coerceIn(1, 30)).apply()
            display.cellCount = detected.coerceIn(1, 30)
        }
        prefs.edit().putBoolean("cell_count_locked", true).apply()
    }

    // --- Pełny zestaw pól Pedal Assist (zakładka "Pedal") ---
    fun setPasPedalType(v: Int) = editPas { it.copy(pedalType = v.coerceIn(0, 3)) }
    fun setPasDesignatedAssist(v: Int) = editPas { it.copy(designatedAssist = v.coerceIn(0, 10)) }
    fun setPasStartCurrent(v: Int) = editPas { it.copy(startCurrentPct = v.coerceIn(0, 100)) }
    fun setPasSlowStartMode(v: Int) = editPas { it.copy(slowStartMode = v.coerceIn(0, 9)) }
    fun setPasStartDegree(v: Int) = editPas { it.copy(startDegree = v.coerceIn(0, 100)) }
    fun setPasWorkMode(v: Int) = editPas { it.copy(workMode = v.coerceIn(0, 9)) }
    fun setPasTimeOfStop(v: Int) = editPas { it.copy(timeOfStop = v.coerceIn(0, 250)) }
    fun setPasCurrentDecay(v: Int) = editPas { it.copy(currentDecay = v.coerceIn(0, 100)) }
    fun setPasStopDecay(v: Int) = editPas { it.copy(stopDecay = v.coerceIn(0, 250)) }
    fun setPasKeepCurrent(v: Int) = editPas { it.copy(keepCurrentPct = v.coerceIn(0, 100)) }

    // --- Pełny zestaw pól Throttle Handle (zakładka "Throttle") ---
    fun setThrStartVoltage(v: Int) = editThr { it.copy(startVoltage = v.coerceIn(0, 50)) }
    fun setThrEndVoltage(v: Int) = editThr { it.copy(endVoltage = v.coerceIn(0, 50)) }
    fun setThrSpeedLimit(v: Int) = editThr { it.copy(speedLimit = v.coerceIn(0, 26)) }
    fun setThrStartCurrent(v: Int) = editThr { it.copy(startCurrentPct = v.coerceIn(0, 100)) }

    // --- Edycja lokalna konfiguracji bbs-fw (config_t) - ten sam wzorzec co editBasic/editPas/editThr ---

    private fun editBbsFwConfig(transform: (BbsFwConfig) -> BbsFwConfig) {
        val cur = _state.value.bbsFwConfigOrDefault
        _state.value = _state.value.copy(bbsFwConfig = transform(cur), configDirty = true)
    }

    // Zakresy poniżej odpowiadają Configuration.Validate() w oficjalnej apce Windows autora - patrz
    // komentarz w BbsFwValidation.kt. Klamrowanie tutaj to tylko wygoda UI (natychmiastowa reakcja
    // suwaka/stepera) - ostateczna, wiążąca sanityzacja i tak następuje w BbsFwWriter.build().
    fun setBbsFwUseFreedomUnits(v: Boolean) = editBbsFwConfig { it.copy(useFreedomUnits = v) }
    fun setBbsFwMaxCurrentAmps(v: Int) {
        val limit = BbsFwController.maxCurrentAmps(_state.value.bbsFwVersion?.ctrlType ?: 0)
        editBbsFwConfig { it.copy(maxCurrentAmps = v.coerceIn(5, limit)) }
    }
    fun setBbsFwCurrentRampAmpsS(v: Int) = editBbsFwConfig { it.copy(currentRampAmpsS = v.coerceIn(1, 255)) }
    fun setBbsFwMaxBatteryVoltageX100(v: Int) = editBbsFwConfig { it.copy(maxBatteryX100v = v.coerceIn(100, 10000)) }
    fun setBbsFwLowCutOffV(v: Int) = editBbsFwConfig { it.copy(lowCutOffV = v.coerceIn(1, 100)) }
    fun setBbsFwMaxSpeedKph(v: Int) = editBbsFwConfig { it.copy(maxSpeedKph = v.coerceIn(0, 180)) }
    fun setBbsFwUseSpeedSensor(v: Boolean) = editBbsFwConfig { it.copy(useSpeedSensor = v) }
    fun setBbsFwUseShiftSensor(v: Boolean) = editBbsFwConfig { it.copy(useShiftSensor = v) }
    fun setBbsFwUsePushWalk(v: Boolean) = editBbsFwConfig { it.copy(usePushWalk = v) }
    /** Patrz [com.bafspeed.app.protocol.BbsFwTemperatureSensor] - NIE bool, 4-wartościowy wybór. */
    fun setBbsFwTemperatureSensorMode(v: Int) = editBbsFwConfig { it.copy(temperatureSensorMode = v.coerceIn(0, 3)) }
    fun setBbsFwLightsMode(v: Int) = editBbsFwConfig { it.copy(lightsMode = v.coerceIn(0, 3)) }
    fun setBbsFwWheelSizeInchX10(v: Int) = editBbsFwConfig { it.copy(wheelSizeInchX10 = v.coerceIn(100, 400)) }
    fun setBbsFwSpeedSensorSignals(v: Int) = editBbsFwConfig { it.copy(speedSensorSignals = v.coerceIn(1, 10)) }
    fun setBbsFwPasStartDelayPulses(v: Int) = editBbsFwConfig { it.copy(pasStartDelayPulses = v.coerceIn(0, 24)) }
    fun setBbsFwPasStopDelayX100s(v: Int) = editBbsFwConfig { it.copy(pasStopDelayX100s = v.coerceIn(5, 100)) }
    fun setBbsFwPasKeepCurrentPercent(v: Int) = editBbsFwConfig { it.copy(pasKeepCurrentPercent = v.coerceIn(10, 100)) }
    fun setBbsFwPasKeepCurrentCadenceRpm(v: Int) = editBbsFwConfig { it.copy(pasKeepCurrentCadenceRpm = v.coerceIn(0, 255)) }
    fun setBbsFwThrottleStartVoltageMv(v: Int) = editBbsFwConfig { it.copy(throttleStartVoltageMv = v.coerceIn(200, 2500)) }
    fun setBbsFwThrottleEndVoltageMv(v: Int) = editBbsFwConfig { it.copy(throttleEndVoltageMv = v.coerceIn(2500, 5000)) }
    fun setBbsFwThrottleStartPercent(v: Int) = editBbsFwConfig { it.copy(throttleStartPercent = v.coerceIn(0, 100)) }
    fun setBbsFwThrottleGlobalSpdLimOpt(v: Int) = editBbsFwConfig { it.copy(throttleGlobalSpdLimOpt = v.coerceIn(0, 2)) }
    fun setBbsFwThrottleGlobalSpdLimPercent(v: Int) = editBbsFwConfig { it.copy(throttleGlobalSpdLimPercent = v.coerceIn(0, 100)) }
    fun setBbsFwShiftInterruptDurationMs(v: Int) = editBbsFwConfig { it.copy(shiftInterruptDurationMs = v.coerceIn(50, 2000)) }
    fun setBbsFwShiftInterruptCurrentThresholdPercent(v: Int) = editBbsFwConfig { it.copy(shiftInterruptCurrentThresholdPercent = v.coerceIn(0, 100)) }
    fun setBbsFwWalkModeDataDisplay(v: Int) = editBbsFwConfig { it.copy(walkModeDataDisplay = v.coerceIn(0, 3)) }
    fun setBbsFwAssistModeSelect(v: Int) = editBbsFwConfig { it.copy(assistModeSelect = v.coerceIn(0, 13)) }
    fun setBbsFwAssistStartupLevel(v: Int) = editBbsFwConfig { it.copy(assistStartupLevel = v.coerceIn(0, 9)) }

    private fun setBbsFwAssistLevel(profile: Int, level: Int, transform: (BbsFwAssistLevel) -> BbsFwAssistLevel) = editBbsFwConfig { cfg ->
        cfg.withAssistLevel(profile, level, transform(cfg.assistLevel(profile, level)))
    }
    fun setBbsFwAssistFlag(profile: Int, level: Int, flag: Int, enabled: Boolean) = setBbsFwAssistLevel(profile, level) { it.withFlag(flag, enabled) }
    fun setBbsFwAssistTargetCurrent(profile: Int, level: Int, pct: Int) = setBbsFwAssistLevel(profile, level) { it.copy(targetCurrentPercent = pct.coerceIn(0, 100)) }
    fun setBbsFwAssistMaxThrottleCurrent(profile: Int, level: Int, pct: Int) = setBbsFwAssistLevel(profile, level) { it.copy(maxThrottleCurrentPercent = pct.coerceIn(0, 100)) }
    fun setBbsFwAssistMaxCadence(profile: Int, level: Int, pct: Int) = setBbsFwAssistLevel(profile, level) { it.copy(maxCadencePercent = pct.coerceIn(0, 100)) }
    fun setBbsFwAssistMaxSpeed(profile: Int, level: Int, pct: Int) = setBbsFwAssistLevel(profile, level) { it.copy(maxSpeedPercent = pct.coerceIn(0, 100)) }
    fun setBbsFwAssistTorqueFactor(profile: Int, level: Int, x10: Int) = setBbsFwAssistLevel(profile, level) { it.copy(torqueAmplificationFactorX10 = x10.coerceIn(0, 250)) }

    /**
     * "Type" poziomu (Motor Disabled/PAS/Throttle/Cruise) - logika 1:1 z `SelectedType` setter
     * w oficjalnej apce autora (`AssistLevelViewModel.cs`, `ApplyBaseTypeFlag` + efekty uboczne
     * per-typ), łącznie z zerowaniem pól, które przestają mieć znaczenie po zmianie typu.
     */
    fun setBbsFwAssistBaseType(profile: Int, level: Int, baseType: Int) = setBbsFwAssistLevel(profile, level) { lvl ->
        val baseBits = BbsFwAssistFlags.PAS or BbsFwAssistFlags.THROTTLE or BbsFwAssistFlags.CRUISE
        var flags = lvl.flags and baseBits.inv()
        flags = when (baseType) {
            BbsFwAssistBaseType.PAS -> flags or BbsFwAssistFlags.PAS
            BbsFwAssistBaseType.THROTTLE -> flags or BbsFwAssistFlags.THROTTLE
            BbsFwAssistBaseType.CRUISE -> flags or BbsFwAssistFlags.CRUISE
            else -> flags
        }
        when (baseType) {
            BbsFwAssistBaseType.DISABLED -> lvl.copy(
                flags = flags and (BbsFwAssistFlags.PAS_TORQUE or BbsFwAssistFlags.PAS_VARIABLE or BbsFwAssistFlags.OVERRIDE_CADENCE or BbsFwAssistFlags.OVERRIDE_SPEED).inv(),
                targetCurrentPercent = 0, maxThrottleCurrentPercent = 0, maxSpeedPercent = 0, torqueAmplificationFactorX10 = 0,
            )
            BbsFwAssistBaseType.THROTTLE -> lvl.copy(
                flags = flags and (BbsFwAssistFlags.PAS_TORQUE or BbsFwAssistFlags.PAS_VARIABLE or BbsFwAssistFlags.OVERRIDE_CADENCE or BbsFwAssistFlags.OVERRIDE_SPEED).inv(),
                targetCurrentPercent = 0, torqueAmplificationFactorX10 = 0,
            )
            BbsFwAssistBaseType.PAS -> lvl.copy(flags = flags, maxThrottleCurrentPercent = 0)
            else -> lvl.copy(flags = flags) // Cruise - bez dodatkowych efektow ubocznych (jak w oryginale)
        }
    }

    /**
     * "Variant" poziomu PAS (Cadence/Torque/Variable) - logika 1:1 z `SelectedPasVariant` setter
     * w oficjalnej apce autora: Variable wyłącza i zeruje wszystko związane z manetką (bo w tym
     * wariancie to manetka reguluje moc PAS, patrz apply_pas_cadence w app.c), Cadence zeruje
     * współczynnik momentu (nieużywany poza Torque), Torque zostawia go bez zmian.
     */
    fun setBbsFwAssistPasVariant(profile: Int, level: Int, variant: Int) = setBbsFwAssistLevel(profile, level) { lvl ->
        val variantBits = BbsFwAssistFlags.PAS_TORQUE or BbsFwAssistFlags.PAS_VARIABLE
        var flags = lvl.flags and variantBits.inv()
        flags = when (variant) {
            BbsFwAssistPasVariant.TORQUE -> flags or BbsFwAssistFlags.PAS_TORQUE
            BbsFwAssistPasVariant.VARIABLE -> flags or BbsFwAssistFlags.PAS_VARIABLE
            else -> flags
        }
        when (variant) {
            BbsFwAssistPasVariant.VARIABLE -> lvl.copy(
                flags = flags and (BbsFwAssistFlags.THROTTLE or BbsFwAssistFlags.OVERRIDE_CADENCE or BbsFwAssistFlags.OVERRIDE_SPEED).inv(),
                torqueAmplificationFactorX10 = 0, maxThrottleCurrentPercent = 0,
            )
            BbsFwAssistPasVariant.CADENCE -> lvl.copy(flags = flags, torqueAmplificationFactorX10 = 0)
            else -> lvl.copy(flags = flags) // Torque - wspolczynnik momentu zostaje bez zmian
        }
    }

    /** Ustawia pojemność wprost w Ah. */
    fun setCapacityAh(ah: Double) {
        val clamped = ah.coerceIn(0.0, 999.0)
        _state.value = _state.value.copy(capacityAh = clamped)
        prefs.edit().putFloat("capacity_ah", clamped.toFloat()).apply()
    }

    /** Ustawia pojemność przez Wh - przelicza na Ah wg bieżącego napięcia nominalnego. */
    fun setCapacityWh(wh: Double) {
        val v = _state.value.nominalPackVoltage
        val ah = (if (v > 0) wh / v else 0.0).coerceIn(0.0, 999.0)
        _state.value = _state.value.copy(capacityAh = ah)
        prefs.edit().putFloat("capacity_ah", ah.toFloat()).apply()
    }

    /** Współczynnik kalibracji odczytu prądu (0,01–3,00×) - kalibruje WYŁĄCZNIE wyświetlaną wartość. */
    fun setCurrentCalibrationFactor(x: Double) {
        val clamped = x.coerceIn(0.01, 3.0)
        display.currentCalibrationFactor = clamped
        _state.value = _state.value.copy(currentCalibrationFactor = clamped)
        prefs.edit().putFloat("current_calibration_factor", clamped.toFloat()).apply()
    }

    /** Ręczna korekta odczytu napięcia (-5,0..+5,0 V) - kalibruje WYŁĄCZNIE wyświetlaną wartość. */
    fun setVoltageCalibrationOffsetV(v: Double) {
        val clamped = v.coerceIn(-5.0, 5.0)
        display.voltageCalibrationOffsetV = clamped
        _state.value = _state.value.copy(voltageCalibrationOffsetV = clamped)
        prefs.edit().putFloat("voltage_calibration_offset_v", clamped.toFloat()).apply()
    }

    // --- Zapis do sterownika (Milestone 2) ---
    //
    // Gwarancje bezpieczeństwa:
    // 1. Zapisywane są WYŁĄCZNIE bloki, które faktycznie różnią się od ostatniego odczytu
    //    (lastRead*) - jeśli nic się nie zmieniło, nic nie jest wysyłane.
    // 2. Każda wartość przechodzi przez BafangValidation.sanitize() TUŻ PRZED zbudowaniem
    //    ramki (BafangWriter) - użytkownik w dialogu potwierdzenia widzi dokładnie to,
    //    co zostanie wysłane, nigdy wartość sprzed przycięcia.
    // 3. Zapis wymaga jawnego potwierdzenia (Confirming → confirmSaveToController()).
    // 4. Zapis jest zablokowany, gdy aktywny jest tryb wyświetlacza (współdzieli port UART).
    // 5. Po zapisie następuje odczyt tego samego bloku i porównanie z zamierzoną wartością
    //    (read-after-write) - wynik jest jawnie pokazany użytkownikowi.

    private fun currentDirtyBlocks(s: UiState): Triple<Boolean, Boolean, Boolean> {
        val bas = s.basic != null && s.lastReadBasic != null && s.basic != s.lastReadBasic
        val pas = s.pedalAssist != null && s.lastReadPedalAssist != null && s.pedalAssist != s.lastReadPedalAssist
        val thr = s.throttle != null && s.lastReadThrottle != null && s.throttle != s.lastReadThrottle
        return Triple(bas, pas, thr)
    }

    fun requestSaveToController() {
        val s = _state.value
        if (s.connection != ConnectionStatus.CONNECTED) return
        if (s.displayMode) {
            _state.value = s.copy(statusMessage = tr(s.language, "Zatrzymaj kokpit (żywe dane) przed zapisem ustawień", "Stop the cockpit (live data) before writing settings"))
            return
        }
        if (s.firmwareType == FirmwareType.BBS_FW) {
            requestSaveBbsFw(s)
            return
        }
        val (basChanged, pasChanged, thrChanged) = currentDirtyBlocks(s)
        val changes = mutableListOf<String>()
        val previews = mutableListOf<FramePreview>()

        if (basChanged) {
            changes += diffBasic(s.language, s.lastReadBasic!!, s.basic!!)
            val (_, frame) = BafangWriter.buildBasic(s.basic, s.general?.maxCurrentA)
            previews += FramePreview("Basic", BafangWriter.toHex(frame))
        }
        if (pasChanged) {
            changes += diffPas(s.language, s.lastReadPedalAssist!!, s.pedalAssist!!)
            val (_, frame) = BafangWriter.buildPedalAssist(s.pedalAssist)
            previews += FramePreview("Pedal Assist", BafangWriter.toHex(frame))
        }
        if (thrChanged) {
            changes += diffThr(s.language, s.lastReadThrottle!!, s.throttle!!)
            val (_, frame) = BafangWriter.buildThrottle(s.throttle)
            previews += FramePreview("Throttle", BafangWriter.toHex(frame))
        }

        if (changes.isEmpty()) {
            _state.value = s.copy(statusMessage = tr(s.language, "Brak zmian do zapisania", "No changes to save"), configDirty = false)
            return
        }
        _state.value = s.copy(writeFlow = WriteFlow.Confirming(changes, previews))
    }

    fun cancelSaveToController() {
        _state.value = _state.value.copy(writeFlow = WriteFlow.Idle)
    }

    fun acknowledgeWriteResult() {
        _state.value = _state.value.copy(writeFlow = WriteFlow.Idle)
    }

    fun confirmSaveToController() {
        val s = _state.value
        if (s.writeFlow !is WriteFlow.Confirming) return
        if (s.firmwareType == FirmwareType.BBS_FW) {
            confirmSaveBbsFw()
            return
        }
        val (basChanged, pasChanged, thrChanged) = currentDirtyBlocks(s)

        viewModelScope.launch {
            if (basChanged) {
                val (_, frame) = BafangWriter.buildBasic(s.basic!!, s.general?.maxCurrentA)
                if (!writeBlockAndAwaitAck("Basic", frame)) return@launch
            }
            if (pasChanged) {
                val (_, frame) = BafangWriter.buildPedalAssist(s.pedalAssist!!)
                if (!writeBlockAndAwaitAck("Pedal Assist", frame)) return@launch
            }
            if (thrChanged) {
                val (_, frame) = BafangWriter.buildThrottle(s.throttle!!)
                if (!writeBlockAndAwaitAck("Throttle", frame)) return@launch
            }

            val lang = _state.value.language
            _state.value = _state.value.copy(writeFlow = WriteFlow.InProgress(tr(lang, "Weryfikuję zapis (odczyt zwrotny)…", "Verifying write (read-back)…")))
            val verifyOk = verifyAfterWrite(basChanged, pasChanged, thrChanged)
            val cur = _state.value
            val stillDirty = cur.basic != cur.lastReadBasic || cur.pedalAssist != cur.lastReadPedalAssist || cur.throttle != cur.lastReadThrottle
            _state.value = cur.copy(
                writeFlow = WriteFlow.Done(
                    verifyOk,
                    if (verifyOk) {
                        tr(
                            lang,
                            "Zapisano i zweryfikowano pomyślnie - sterownik odczytany ponownie zgadza się z wysłanymi wartościami.",
                            "Saved and verified successfully - the controller read back again matches the values sent.",
                        )
                    } else {
                        tr(
                            lang,
                            "Zapis wysłany, ale odczyt zwrotny NIE zgadza się z oczekiwanymi wartościami. Sprawdź ustawienia ręcznie przed jazdą.",
                            "Write sent, but the read-back does NOT match the expected values. Check the settings manually before riding.",
                        )
                    },
                ),
                configDirty = stillDirty,
            )
        }
    }

    private suspend fun writeBlockAndAwaitAck(blockName: String, frame: ByteArray): Boolean {
        val lang = _state.value.language
        _state.value = _state.value.copy(writeFlow = WriteFlow.InProgress(tr(lang, "Wysyłam blok: $blockName", "Sending block: $blockName")))
        writeResponseParser.reset()
        writeAckMode = true
        val deferred = CompletableDeferred<WriteResponseParser.Result.Ack>()
        pendingAck = deferred
        withContext(Dispatchers.IO) { runCatching { serial.write(frame) } }
        val ack = withTimeoutOrNull(3000) { deferred.await() }
        writeAckMode = false
        pendingAck = null

        if (ack == null) {
            _state.value = _state.value.copy(
                writeFlow = WriteFlow.Done(
                    false,
                    tr(lang, "Brak odpowiedzi sterownika dla bloku „$blockName” - sprawdź kabel i spróbuj ponownie.", "No response from the controller for block “$blockName” - check the cable and try again."),
                ),
            )
            return false
        }
        if (!ack.ok) {
            _state.value = _state.value.copy(
                writeFlow = WriteFlow.Done(false, tr(lang, "Sterownik odrzucił zapis bloku „$blockName”: ${ack.message}", "The controller rejected the write for block “$blockName”: ${ack.message}")),
            )
            return false
        }
        return true
    }

    private suspend fun readBlockForVerify(cmd: ByteArray): ConfigFrameParser.Result.Frame? {
        configParser.reset()
        verifyMode = true
        val deferred = CompletableDeferred<ConfigFrameParser.Result.Frame>()
        pendingVerifyFrame = deferred
        delay(300)
        withContext(Dispatchers.IO) { runCatching { serial.write(cmd) } }
        val result = withTimeoutOrNull(3000) { deferred.await() }
        verifyMode = false
        pendingVerifyFrame = null
        return result
    }

    private suspend fun verifyAfterWrite(basChanged: Boolean, pasChanged: Boolean, thrChanged: Boolean): Boolean {
        val target = _state.value
        var allOk = true
        if (basChanged) {
            val frame = readBlockForVerify(BafangCommands.READ_BAS)
            if (frame == null) allOk = false else {
                val decoded = BafangDecoder.decodeBas(frame.data)
                _state.value = _state.value.copy(basic = decoded, lastReadBasic = decoded)
                if (decoded != BafangValidation.sanitize(target.basic!!)) allOk = false
            }
        }
        if (pasChanged) {
            val frame = readBlockForVerify(BafangCommands.READ_PAS)
            if (frame == null) allOk = false else {
                val decoded = BafangDecoder.decodePas(frame.data)
                _state.value = _state.value.copy(pedalAssist = decoded, lastReadPedalAssist = decoded)
                if (decoded != BafangValidation.sanitize(target.pedalAssist!!)) allOk = false
            }
        }
        if (thrChanged) {
            val frame = readBlockForVerify(BafangCommands.READ_THR)
            if (frame == null) allOk = false else {
                val decoded = BafangDecoder.decodeThr(frame.data)
                _state.value = _state.value.copy(throttle = decoded, lastReadThrottle = decoded)
                if (decoded != BafangValidation.sanitize(target.throttle!!)) allOk = false
            }
        }
        return allOk
    }

    // --- Zapis do sterownika bbs-fw - odpowiednik sekcji OEM powyżej, ale JEDEN blok configu
    // zamiast trzech (BAS/PAS/THR), i ACK bez kodów błędów per pole (patrz BbsFwWriteResponseParser).

    private fun requestSaveBbsFw(s: UiState) {
        val old = s.lastReadBbsFwConfig
        val new = s.bbsFwConfig
        if (old == null || new == null || old == new) {
            _state.value = s.copy(statusMessage = tr(s.language, "Brak zmian do zapisania", "No changes to save"), configDirty = false)
            return
        }
        val changes = diffBbsFw(s.language, old, new)
        val (_, frame) = BbsFwWriter.build(new, s.bbsFwVersion?.ctrlType)
        _state.value = s.copy(writeFlow = WriteFlow.Confirming(changes, listOf(FramePreview("bbs-fw Config", BbsFwWriter.toHex(frame)))))
    }

    private suspend fun writeBbsFwConfigAndAwaitAck(frame: ByteArray): Boolean {
        val lang = _state.value.language
        _state.value = _state.value.copy(writeFlow = WriteFlow.InProgress(tr(lang, "Wysyłam konfigurację do sterownika (bbs-fw)…", "Sending configuration to the controller (bbs-fw)…")))
        bbsFwWriteResponseParser.reset()
        writeAckMode = true
        val deferred = CompletableDeferred<BbsFwWriteResponseParser.Result.Ack>()
        pendingBbsFwAck = deferred
        withContext(Dispatchers.IO) { runCatching { serial.write(frame) } }
        val ack = withTimeoutOrNull(3000) { deferred.await() }
        writeAckMode = false
        pendingBbsFwAck = null

        if (ack == null) {
            _state.value = _state.value.copy(
                writeFlow = WriteFlow.Done(false, tr(lang, "Brak odpowiedzi sterownika (bbs-fw) - sprawdź kabel i spróbuj ponownie.", "No response from the controller (bbs-fw) - check the cable and try again.")),
            )
            return false
        }
        if (!ack.ok) {
            _state.value = _state.value.copy(
                writeFlow = WriteFlow.Done(
                    false,
                    tr(
                        lang,
                        "Sterownik bbs-fw odrzucił zapis (niezgodna wersja/długość konfiguracji albo błąd zapisu do pamięci - firmware nie zwraca szczegółów).",
                        "The bbs-fw controller rejected the write (config version/length mismatch, or a flash write error - the firmware doesn't report details).",
                    ),
                ),
            )
            return false
        }
        return true
    }

    private suspend fun readBbsFwConfigForVerify(): BbsFwFrameParser.Result.ConfigFrame? {
        bbsFwFrameParser.reset()
        verifyMode = true
        val deferred = CompletableDeferred<BbsFwFrameParser.Result.ConfigFrame>()
        pendingBbsFwVerifyFrame = deferred
        delay(300)
        withContext(Dispatchers.IO) { runCatching { serial.write(BbsFwCommands.READ_CONFIG) } }
        val result = withTimeoutOrNull(3000) { deferred.await() }
        verifyMode = false
        pendingBbsFwVerifyFrame = null
        return result
    }

    private fun confirmSaveBbsFw() {
        val target = _state.value.bbsFwConfig ?: return
        viewModelScope.launch {
            val (sanitized, frame) = BbsFwWriter.build(target, _state.value.bbsFwVersion?.ctrlType)
            if (!writeBbsFwConfigAndAwaitAck(frame)) return@launch

            val lang = _state.value.language
            _state.value = _state.value.copy(writeFlow = WriteFlow.InProgress(tr(lang, "Weryfikuję zapis (odczyt zwrotny)…", "Verifying write (read-back)…")))
            val readBack = readBbsFwConfigForVerify()
            var verifyOk = false
            if (readBack != null && readBack.version == BbsFwCommands.CONFIG_VERSION && readBack.data.size == BbsFwConfig.BYTE_SIZE) {
                val decoded = BbsFwConfig.deserialize(readBack.data)
                _state.value = _state.value.copy(bbsFwConfig = decoded, lastReadBbsFwConfig = decoded)
                saveBbsFwConfig(decoded)
                verifyOk = decoded == sanitized
            }

            val cur = _state.value
            _state.value = cur.copy(
                writeFlow = WriteFlow.Done(
                    verifyOk,
                    if (verifyOk) {
                        tr(
                            lang,
                            "Zapisano i zweryfikowano pomyślnie - sterownik odczytany ponownie zgadza się z wysłanymi wartościami.",
                            "Saved and verified successfully - the controller read back again matches the values sent.",
                        )
                    } else {
                        tr(
                            lang,
                            "Zapis wysłany, ale odczyt zwrotny NIE zgadza się z oczekiwanymi wartościami (albo się nie powiódł). Sprawdź ustawienia ręcznie przed jazdą.",
                            "Write sent, but the read-back does NOT match the expected values (or failed). Check the settings manually before riding.",
                        )
                    },
                ),
                configDirty = cur.bbsFwConfig != cur.lastReadBbsFwConfig,
            )
        }
    }

    private fun diffBbsFw(lang: AppLanguage, old: BbsFwConfig, new: BbsFwConfig): List<String> {
        val out = mutableListOf<String>()
        if (old.maxCurrentAmps != new.maxCurrentAmps) out += "${tr(lang, "Limit prądu", "Current limit")}: ${old.maxCurrentAmps}A → ${new.maxCurrentAmps}A"
        if (old.currentRampAmpsS != new.currentRampAmpsS) out += "${tr(lang, "Narastanie prądu", "Current ramp")}: ${old.currentRampAmpsS}A/s → ${new.currentRampAmpsS}A/s"
        if (old.maxBatteryX100v != new.maxBatteryX100v) out += "${tr(lang, "Napięcie maksymalne", "Max voltage")}: ${old.maxBatteryX100v / 100.0}V → ${new.maxBatteryX100v / 100.0}V"
        if (old.lowCutOffV != new.lowCutOffV) out += "${tr(lang, "Odcięcie niskiego napięcia", "Low voltage cutoff")}: ${old.lowCutOffV}V → ${new.lowCutOffV}V"
        if (old.maxSpeedKph != new.maxSpeedKph) out += "${tr(lang, "Limit prędkości", "Speed limit")}: ${old.maxSpeedKph}km/h → ${new.maxSpeedKph}km/h"
        if (old.wheelSizeInchX10 != new.wheelSizeInchX10) out += "${tr(lang, "Rozmiar koła", "Wheel size")}: ${old.wheelSizeInchX10 / 10.0}\" → ${new.wheelSizeInchX10 / 10.0}\""
        if (old.lightsMode != new.lightsMode) out += "${tr(lang, "Tryb świateł", "Lights mode")}: ${old.lightsMode} → ${new.lightsMode}"
        if (old.throttleStartVoltageMv != new.throttleStartVoltageMv) out += "${tr(lang, "Napięcie startowe manetki", "Throttle start voltage")}: ${old.throttleStartVoltageMv}mV → ${new.throttleStartVoltageMv}mV"
        if (old.throttleEndVoltageMv != new.throttleEndVoltageMv) out += "${tr(lang, "Napięcie końcowe manetki", "Throttle end voltage")}: ${old.throttleEndVoltageMv}mV → ${new.throttleEndVoltageMv}mV"
        if (old.assistModeSelect != new.assistModeSelect) out += "${tr(lang, "Tryb wyboru wspomagania", "Assist mode select")}: ${old.assistModeSelect} → ${new.assistModeSelect}"
        val changedLevels = (0..1).sumOf { p -> (0..9).count { l -> old.assistLevels[p][l] != new.assistLevels[p][l] } }
        if (changedLevels > 0) out += "${tr(lang, "Poziomy wspomagania - zmienionych pól", "Assist levels - changed entries")}: $changedLevels"
        if (out.isEmpty()) out += tr(lang, "Inne parametry bbs-fw (bez szczegółowego podglądu w tym oknie)", "Other bbs-fw parameters (no detailed preview in this dialog)")
        return out
    }

    private fun diffBasic(lang: AppLanguage, old: BasicSettings, new: BasicSettings): List<String> {
        val out = mutableListOf<String>()
        if (old.lowBatteryProtection != new.lowBatteryProtection) out += "${tr(lang, "Ochrona niskiego napięcia", "Low battery protection")}: ${old.lowBatteryProtection}V → ${new.lowBatteryProtection}V"
        if (old.currentLimit != new.currentLimit) out += "${tr(lang, "Limit prądu", "Current limit")}: ${old.currentLimit}A → ${new.currentLimit}A"
        for (i in 0..9) {
            if (old.assistCurrentPct[i] != new.assistCurrentPct[i]) out += "${tr(lang, "Poziom", "Level")} $i · ${tr(lang, "limit prądu", "current limit")}: ${old.assistCurrentPct[i]}% → ${new.assistCurrentPct[i]}%"
            if (old.assistSpeedPct[i] != new.assistSpeedPct[i]) out += "${tr(lang, "Poziom", "Level")} $i · ${tr(lang, "limit prędkości", "speed limit")}: ${old.assistSpeedPct[i]}% → ${new.assistSpeedPct[i]}%"
        }
        if (old.wheelDiameterCode != new.wheelDiameterCode) {
            out += "${tr(lang, "Rozmiar koła", "Wheel size")}: ${WHEEL_SIZE_LABELS.getOrNull(old.wheelDiameterCode)} → ${WHEEL_SIZE_LABELS.getOrNull(new.wheelDiameterCode)}"
        }
        if (old.speedMeterModel != new.speedMeterModel) out += "${tr(lang, "Tryb czujnika prędkości", "Speed meter type")}: ${old.speedMeterModel} → ${new.speedMeterModel}"
        if (old.speedMeterSignals != new.speedMeterSignals) out += "${tr(lang, "Sygnały czujnika prędkości", "Speed meter signals")}: ${old.speedMeterSignals} → ${new.speedMeterSignals}"
        return out
    }

    private fun diffPas(lang: AppLanguage, old: PedalAssistSettings, new: PedalAssistSettings): List<String> {
        val out = mutableListOf<String>()
        if (old.speedLimit != new.speedLimit) out += "${tr(lang, "Limit prędkości (PAS)", "Speed limit (PAS)")}: ${old.speedLimit} → ${new.speedLimit}"
        if (old.designatedAssist != new.designatedAssist) out += "${tr(lang, "Wskazany poziom wspomagania", "Designated assist level")}: ${old.designatedAssist} → ${new.designatedAssist}"
        if (old.pedalType != new.pedalType) out += "${tr(lang, "Typ czujnika pedałowania", "Pedal sensor type")}: ${old.pedalType} → ${new.pedalType}"
        if (old.startCurrentPct != new.startCurrentPct) out += "${tr(lang, "Prąd startowy", "Start current")}: ${old.startCurrentPct}% → ${new.startCurrentPct}%"
        if (old.slowStartMode != new.slowStartMode) out += "${tr(lang, "Tryb wolnego startu", "Slow-start mode")}: ${old.slowStartMode} → ${new.slowStartMode}"
        if (old.startDegree != new.startDegree) out += "${tr(lang, "Stopień startu", "Start degree")}: ${old.startDegree} → ${new.startDegree}"
        if (old.workMode != new.workMode) out += "${tr(lang, "Tryb pracy", "Work mode")}: ${old.workMode} → ${new.workMode}"
        if (old.timeOfStop != new.timeOfStop) out += "${tr(lang, "Opóźnienie zatrzymania", "Stop delay")}: ${old.timeOfStop} → ${new.timeOfStop}"
        if (old.currentDecay != new.currentDecay) out += "${tr(lang, "Zanik prądu", "Current decay")}: ${old.currentDecay} → ${new.currentDecay}"
        if (old.stopDecay != new.stopDecay) out += "${tr(lang, "Zanik zatrzymania", "Stop decay")}: ${old.stopDecay} → ${new.stopDecay}"
        if (old.keepCurrentPct != new.keepCurrentPct) out += "${tr(lang, "Podtrzymanie prądu", "Keep current")}: ${old.keepCurrentPct}% → ${new.keepCurrentPct}%"
        return out
    }

    private fun diffThr(lang: AppLanguage, old: ThrottleSettings, new: ThrottleSettings): List<String> {
        val out = mutableListOf<String>()
        if (old.mode != new.mode) {
            val modeLabel = { m: Int -> tr(lang, if (m == 0) "prędkość" else "prąd", if (m == 0) "speed" else "current") }
            out += "${tr(lang, "Tryb manetki", "Throttle mode")}: ${modeLabel(old.mode)} → ${modeLabel(new.mode)}"
        }
        if (old.designatedAssist != new.designatedAssist) out += "${tr(lang, "Wskazany poziom (manetka)", "Designated level (throttle)")}: ${old.designatedAssist} → ${new.designatedAssist}"
        if (old.speedLimit != new.speedLimit) out += "${tr(lang, "Limit prędkości (manetka)", "Speed limit (throttle)")}: ${old.speedLimit} → ${new.speedLimit}"
        if (old.startVoltage != new.startVoltage) out += "${tr(lang, "Napięcie startowe", "Start voltage")}: ${old.startVoltage} → ${new.startVoltage}"
        if (old.endVoltage != new.endVoltage) out += "${tr(lang, "Napięcie końcowe", "End voltage")}: ${old.endVoltage} → ${new.endVoltage}"
        if (old.startCurrentPct != new.startCurrentPct) out += "${tr(lang, "Prąd startowy", "Start current")}: ${old.startCurrentPct}% → ${new.startCurrentPct}%"
        return out
    }

    // --- Profile (zapis/odczyt .ini) ---

    private val profilesDir: File by lazy {
        File(getApplication<Application>().filesDir, "profiles").apply { mkdirs() }
    }

    fun refreshProfiles() {
        val names = profilesDir.listFiles { f -> f.extension.equals("ini", true) }
            ?.map { it.nameWithoutExtension }?.sorted() ?: emptyList()
        _state.value = _state.value.copy(profiles = names)
    }

    /** Buduje treść profilu z bieżącej konfiguracji roboczej - format zależny od wybranego firmware. */
    fun exportIni(): String = when (_state.value.firmwareType) {
        FirmwareType.BBS_FW -> BbsFwProfileIo.serialize(_state.value.bbsFwConfigOrDefault)
        FirmwareType.OEM_BAFANG -> ProfileIo.serialize(
            ProfileData(
                general = _state.value.general,
                basic = _state.value.basicOrDefault,
                pedalAssist = _state.value.pasOrDefault,
                throttle = _state.value.thrOrDefault,
            )
        )
    }

    /**
     * Wczytuje profil do konfiguracji roboczej (podgląd). NIE wysyła do sterownika.
     * Profile OEM i bbs-fw NIE są wymienne (różne struktury/rejestry) - plik bbs-fw ma na
     * początku znacznik [BbsFwProfileIo.FIRMWARE_MARKER], plik OEM (albo dowolny plik .ini
     * z Bafang Configuration Tool - profile mają być z nim wymienne) go nie ma. Próba wczytania
     * profilu niezgodnego z aktualnie wybranym firmware jest jawnie odrzucana - inaczej dane
     * trafiłyby w złe pola albo (dla configu bbs-fw wczytanego jako OEM) zostałyby po cichu
     * zignorowane.
     */
    fun importIni(text: String): Result<Unit> = runCatching {
        val lang = _state.value.language
        val currentFw = _state.value.firmwareType
        val isBbsFwProfile = text.lineSequence().any { it.trim() == BbsFwProfileIo.FIRMWARE_MARKER }
        if (isBbsFwProfile && currentFw != FirmwareType.BBS_FW) {
            error(tr(lang, "Ten profil zapisano dla firmware bbs-fw (Daniel Nilsson) - przełącz firmware w Ustawieniach, żeby go wczytać.", "This profile was saved for bbs-fw (Daniel Nilsson) firmware - switch firmware in Settings to load it."))
        }
        if (!isBbsFwProfile && currentFw == FirmwareType.BBS_FW) {
            error(tr(lang, "Ten profil zapisano dla fabrycznego firmware Bafang (OEM) - przełącz firmware w Ustawieniach, żeby go wczytać.", "This profile was saved for factory Bafang (OEM) firmware - switch firmware in Settings to load it."))
        }
        when (currentFw) {
            FirmwareType.BBS_FW -> {
                val cfg = BbsFwProfileIo.parse(text)
                _state.value = _state.value.copy(
                    bbsFwConfig = cfg,
                    configDirty = true,
                    statusMessage = tr(lang, "Wczytano profil bbs-fw (podgląd) - nie wysłano do sterownika", "bbs-fw profile loaded (preview) - not sent to the controller"),
                )
            }
            FirmwareType.OEM_BAFANG -> {
                val data = ProfileIo.parse(text)
                data.basic.wheelDiameterCode.let { code ->
                    if (code in WHEEL_CIRCUMFERENCE_M.indices) display.wheelCircumferenceM = WHEEL_CIRCUMFERENCE_M[code]
                }
                display.lowBatteryProtectionV = data.basic.lowBatteryProtection
                _state.value = _state.value.copy(
                    basic = data.basic,
                    pedalAssist = data.pedalAssist,
                    throttle = data.throttle,
                    configDirty = true,
                    statusMessage = tr(lang, "Wczytano profil (podgląd) - nie wysłano do sterownika", "Profile loaded (preview) - not sent to the controller"),
                )
            }
        }
    }

    fun saveProfileInternal(name: String) {
        val safe = name.trim().ifBlank { "profil" }.replace(Regex("[^A-Za-z0-9 _-]"), "_")
        File(profilesDir, "$safe.ini").writeText(exportIni())
        refreshProfiles()
    }

    fun loadProfileInternal(name: String): Result<Unit> = runCatching {
        val f = File(profilesDir, "$name.ini")
        importIni(f.readText()).getOrThrow()
    }

    fun deleteProfileInternal(name: String) {
        File(profilesDir, "$name.ini").delete()
        refreshProfiles()
    }

    // --- wewnętrzne ---

    private suspend fun sendConfigRead(cmd: ByteArray) {
        configTimeoutJob?.cancel()
        configTimeoutJob = viewModelScope.launch {
            delay(3000)
            if (_state.value.connection == ConnectionStatus.CONNECTING) {
                handleConnectFailure(
                    tr(
                        _state.value.language,
                        "Sterownik nie odpowiada - sprawdź kabel i zasilanie (bateria włączona?)",
                        "Controller not responding - check the cable and power (is the battery on?)",
                    ),
                )
            }
        }
        delay(500) // oryginalna apka odczekiwała 500 ms przed każdą komendą konfiguracji
        withContext(Dispatchers.IO) { runCatching { serial.write(cmd) } }
    }

    /**
     * Identyfikacja bbs-fw (READ_FW_VERSION) - w przeciwieństwie do [sendConfigRead] (pojedyncza
     * próba + 3s timeout) ponawia wysyłkę co [BBS_FW_IDENTIFY_RETRY_INTERVAL_MS] aż do
     * [BBS_FW_IDENTIFY_TIMEOUT_MS], dokładnie tak jak oficjalne narzędzie Daniela Nilssona
     * (BBSFWTool.exe, `SetupConnection`: wysyłka + Thread.Sleep(200), w pętli do timeoutu) -
     * kontroler na tym firmware bywa wolny w wybudzaniu się na porcie programującym i pojedyncza
     * próba z krótkim timeoutem regularnie kończyła się niepowodzeniem.
     */
    private fun startBbsFwIdentifyRetry() {
        identifyRetryJob?.cancel()
        configTimeoutJob?.cancel()
        identifyRetryJob = viewModelScope.launch {
            delay(500) // oryginalna apka odczekiwała 500 ms przed każdą komendą konfiguracji
            val deadline = System.currentTimeMillis() + BBS_FW_IDENTIFY_TIMEOUT_MS
            while (_state.value.connection == ConnectionStatus.CONNECTING && System.currentTimeMillis() < deadline) {
                withContext(Dispatchers.IO) { runCatching { serial.write(BbsFwCommands.READ_FW_VERSION) } }
                delay(BBS_FW_IDENTIFY_RETRY_INTERVAL_MS)
            }
            if (_state.value.connection == ConnectionStatus.CONNECTING) {
                handleConnectFailure(
                    tr(
                        _state.value.language,
                        "Sterownik nie odpowiada - sprawdź kabel i zasilanie (bateria włączona?)",
                        "Controller not responding - check the cable and power (is the battery on?)",
                    ),
                )
            }
        }
    }

    private fun onSerialData(bytes: ByteArray) {
        val firmwareType = _state.value.firmwareType
        if (writeAckMode) {
            when (firmwareType) {
                FirmwareType.OEM_BAFANG -> when (val r = writeResponseParser.process(bytes)) {
                    is WriteResponseParser.Result.Ack -> pendingAck?.complete(r)
                    WriteResponseParser.Result.Processing -> Unit
                }
                FirmwareType.BBS_FW -> when (val r = bbsFwWriteResponseParser.process(bytes)) {
                    is BbsFwWriteResponseParser.Result.Ack -> pendingBbsFwAck?.complete(r)
                    BbsFwWriteResponseParser.Result.Processing -> Unit
                }
            }
            return
        }
        if (verifyMode) {
            when (firmwareType) {
                FirmwareType.OEM_BAFANG -> when (val r = configParser.process(bytes)) {
                    is ConfigFrameParser.Result.Frame -> pendingVerifyFrame?.complete(r)
                    ConfigFrameParser.Result.Processing -> Unit
                }
                FirmwareType.BBS_FW -> when (val r = bbsFwFrameParser.process(bytes)) {
                    is BbsFwFrameParser.Result.ConfigFrame -> pendingBbsFwVerifyFrame?.complete(r)
                    else -> Unit
                }
            }
            return
        }
        // Bez displayMode ANI display.scanning tu, bajty trafialyby do configParser (GEN/BAS/PAS/THR)
        // i byly ciche gubione - dokladnie to psulo skan rejestrow w zakladce Diagnostyka po tym,
        // jak przestalismy uruchamiac tryb wyswietlacza na tym ekranie.
        if (_state.value.displayMode || display.scanning.value) {
            display.onDataReceived(bytes)
            return
        }
        when (firmwareType) {
            FirmwareType.OEM_BAFANG -> when (val result = configParser.process(bytes)) {
                is ConfigFrameParser.Result.Frame -> viewModelScope.launch { handleFrame(result) }
                ConfigFrameParser.Result.Processing -> Unit
            }
            FirmwareType.BBS_FW -> when (val result = bbsFwFrameParser.process(bytes)) {
                is BbsFwFrameParser.Result.VersionFrame -> viewModelScope.launch { handleBbsFwVersionFrame(result) }
                is BbsFwFrameParser.Result.ConfigFrame -> viewModelScope.launch { handleBbsFwConfigFrame(result) }
                BbsFwFrameParser.Result.Processing -> Unit
            }
        }
    }

    private suspend fun handleBbsFwVersionFrame(frame: BbsFwFrameParser.Result.VersionFrame) {
        configTimeoutJob?.cancel()
        identifyRetryJob?.cancel()
        val lang = _state.value.language
        _state.value = _state.value.copy(
            bbsFwVersion = frame.info,
            statusMessage = tr(
                lang,
                "bbs-fw ${frame.info.versionLabel} (config v${frame.info.configVersion}, typ sterownika ${frame.info.ctrlType}) - czytam konfigurację…",
                "bbs-fw ${frame.info.versionLabel} (config v${frame.info.configVersion}, controller type ${frame.info.ctrlType}) - reading config…",
            ),
        )
        saveBbsFwVersion(frame.info)
        sendConfigRead(BbsFwCommands.READ_CONFIG)
    }

    private suspend fun handleBbsFwConfigFrame(frame: BbsFwFrameParser.Result.ConfigFrame) {
        configTimeoutJob?.cancel()
        val lang = _state.value.language
        if (frame.version != BbsFwCommands.CONFIG_VERSION || frame.data.size != BbsFwConfig.BYTE_SIZE) {
            _state.value = _state.value.copy(
                connection = ConnectionStatus.ERROR,
                statusMessage = tr(
                    lang,
                    "Niewspierana wersja konfiguracji bbs-fw (sterownik zgłasza v${frame.version}, ${frame.data.size} B; ta wersja apki zna v${BbsFwCommands.CONFIG_VERSION}, ${BbsFwConfig.BYTE_SIZE} B) - sprawdź wersję firmware.",
                    "Unsupported bbs-fw config version (controller reports v${frame.version}, ${frame.data.size} B; this app version knows v${BbsFwCommands.CONFIG_VERSION}, ${BbsFwConfig.BYTE_SIZE} B) - check the firmware version.",
                ),
            )
            return
        }
        val cfg = BbsFwConfig.deserialize(frame.data)
        _state.value = _state.value.copy(
            bbsFwConfig = cfg,
            lastReadBbsFwConfig = cfg,
            connection = ConnectionStatus.CONNECTED,
            configDirty = false,
            statusMessage = tr(lang, "Połączono (bbs-fw) - odczytano konfigurację", "Connected (bbs-fw) - config read"),
            autoReconnectState = AutoReconnectState.IDLE,
        )
        reconnectAttempt = 0
        saveBbsFwConfig(cfg)
    }

    private suspend fun handleFrame(frame: ConfigFrameParser.Result.Frame) {
        configTimeoutJob?.cancel()
        when (frame.blockAddress) {
            BafangCommands.ADDR_GEN -> {
                val gen = BafangDecoder.decodeGen(frame.data)
                val known = KNOWN_MODEL_PREFIXES.any { gen.model.startsWith(it) || gen.manufacturer.isNotBlank() }
                val lang = _state.value.language
                _state.value = _state.value.copy(
                    general = gen,
                    statusMessage = if (known) {
                        tr(lang, "Rozpoznano: ${gen.manufacturer} ${gen.model}", "Recognized: ${gen.manufacturer} ${gen.model}")
                    } else {
                        tr(lang, "Nieznany sterownik: ${gen.manufacturer} ${gen.model}", "Unknown controller: ${gen.manufacturer} ${gen.model}")
                    },
                )
                saveGeneral(gen)
                maybeAutoDetectCellCount(gen)
                sendConfigRead(BafangCommands.READ_BAS)
            }
            BafangCommands.ADDR_BAS -> {
                val bas = BafangDecoder.decodeBas(frame.data)
                bas.wheelDiameterCode.let { code ->
                    if (code in WHEEL_CIRCUMFERENCE_M.indices) display.wheelCircumferenceM = WHEEL_CIRCUMFERENCE_M[code]
                }
                display.lowBatteryProtectionV = bas.lowBatteryProtection
                _state.value = _state.value.copy(basic = bas, lastReadBasic = bas)
                saveBasic(bas)
                sendConfigRead(BafangCommands.READ_PAS)
            }
            BafangCommands.ADDR_PAS -> {
                val pas = BafangDecoder.decodePas(frame.data)
                _state.value = _state.value.copy(pedalAssist = pas, lastReadPedalAssist = pas)
                savePas(pas)
                sendConfigRead(BafangCommands.READ_THR)
            }
            BafangCommands.ADDR_THR -> {
                val thr = BafangDecoder.decodeThr(frame.data)
                _state.value = _state.value.copy(
                    throttle = thr,
                    lastReadThrottle = thr,
                    connection = ConnectionStatus.CONNECTED,
                    configDirty = false,
                    statusMessage = tr(_state.value.language, "Połączono - odczytano pełną konfigurację", "Connected - full configuration read"),
                    autoReconnectState = AutoReconnectState.IDLE,
                )
                reconnectAttempt = 0
                saveThr(thr)
            }
        }
    }

    private fun onSerialError(e: Exception) {
        // displayMode==true oznacza, że użytkownik był aktywnie na Kokpicie/Diagnostyce w momencie
        // zerwania połączenia (obie te zakładki napędzają pętlę wyświetlacza) - w takim wypadku
        // próbujemy połączyć się ponownie automatycznie (do MAX_RECONNECT_ATTEMPTS razy, patrz
        // handleConnectFailure), zamiast zostawiać go na ekranie błędu.
        val wasActive = _state.value.displayMode
        viewModelScope.launch {
            saveLastKnownTelemetry()
            stopDisplayMode()
            serial.close()
            configTimeoutJob?.cancel()
            identifyRetryJob?.cancel()
            val lang = _state.value.language
            if (wasActive) {
                reconnectAttempt = 0
                _state.value = _state.value.copy(connection = ConnectionStatus.DISCONNECTED, statusMessage = tr(lang, "Połączenie przerwane", "Connection interrupted"))
                scheduleReconnectAttempt(RECONNECT_FIRST_DELAY_MS)
            } else {
                _state.value = _state.value.copy(
                    connection = ConnectionStatus.ERROR,
                    statusMessage = tr(lang, "Połączenie przerwane: ${e.message ?: "błąd USB"}", "Connection interrupted: ${e.message ?: "USB error"}"),
                )
            }
        }
    }

    /**
     * Trip: całkowanie prędkości po czasie (licznik po stronie aplikacji) - i w tym samym takcie
     * karmienie [EnergyAnalyzer] próbką (nawet przy postoju - dystans=0, ale pobór prądu na postoju
     * ma się doliczyć do średniego zużycia trasy).
     */
    private fun startTripIntegration() {
        tripJob?.cancel()
        tripJob = viewModelScope.launch {
            var lastTs = System.currentTimeMillis()
            while (true) {
                delay(1000)
                val now = System.currentTimeMillis()
                val dtH = (now - lastTs) / 3_600_000.0
                lastTs = now
                val kmh = telemetry.value.speedKmh
                val distanceDeltaKm = if (kmh > 0.5) kmh * dtH else 0.0
                if (distanceDeltaKm > 0.0) {
                    _state.value = _state.value.copy(
                        tripKm = _state.value.tripKm + distanceDeltaKm,
                        // Predkosc srednia liczona TYLKO z czasu/dystansu w ruchu - postoje (kmh<=0.5)
                        // celowo nie dolicza sie do avgSpeedMovingTimeH, zeby nie zanizac sredniej.
                        avgSpeedDistanceKm = _state.value.avgSpeedDistanceKm + distanceDeltaKm,
                        avgSpeedMovingTimeH = _state.value.avgSpeedMovingTimeH + dtH,
                    )
                }
                val estimate = energyAnalyzer.addSample(
                    timestampMs = now,
                    distanceDeltaKm = distanceDeltaKm,
                    batteryPct = telemetry.value.batteryPct,
                    voltageV = telemetry.value.voltageV,
                    currentA = telemetry.value.currentA,
                    capacityWh = _state.value.capacityWh,
                )
                _state.value = _state.value.copy(
                    predictedRangeKm = estimate.predictedRangeKm,
                    tripAvgWhPerKm = estimate.tripAvgWhPerKm,
                    currentAvgWhPerKm = estimate.currentAvgWhPerKm,
                )
            }
        }
    }

    override fun onCleared() {
        serial.close()
        energyAnalyzer.saveState()
    }
}
