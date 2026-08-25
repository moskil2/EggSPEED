package com.bafspeed.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bafspeed.app.UiState
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.protocol.BbsFwController
import com.bafspeed.app.ui.components.ExpandableParamTile
import com.bafspeed.app.ui.components.FlankedSlider
import com.bafspeed.app.ui.components.MicroLabel
import com.bafspeed.app.ui.components.ReadWriteButtons
import com.bafspeed.app.ui.components.StepBtn
import com.bafspeed.app.ui.components.TelemetryPausedNotice
import com.bafspeed.app.ui.components.TokenCard
import com.bafspeed.app.ui.components.ToggleRow
import com.bafspeed.app.ui.theme.Tokens

/**
 * Odpowiednik zakładki "System" z oficjalnej apki Windows autora bbs-fw (src/tool/View/SystemView.xaml) -
 * te same nazwy sekcji i pól, w tej samej kolejności, żeby użytkownik znający oryginalne narzędzie
 * miał bezpośrednie odniesienie. Opisy pól oparte na treści wiki projektu
 * (github.com/danielnilsson9/bbs-fw/wiki/Configuration-Tool) i tooltipach z SystemView.xaml.
 * Zastępuje dawne osobne ekrany General/Pedal/Throttle - jego apka trzyma to wszystko w jednej
 * zakładce (Assist Levels ma tylko poziomy wspomagania i przełączniki trybu - patrz [BbsFwAssistLevelsScreen]).
 */
@Composable
fun BbsFwSystemScreen(
    state: UiState,
    onMaxCurrent: (Int) -> Unit,
    onCurrentRamp: (Int) -> Unit,
    onMaxBatteryVoltageX100: (Int) -> Unit,
    onLowCutOff: (Int) -> Unit,
    onMaxSpeed: (Int) -> Unit,
    onThrottleStartVoltageMv: (Int) -> Unit,
    onThrottleEndVoltageMv: (Int) -> Unit,
    onThrottleStartPercent: (Int) -> Unit,
    onThrottleGlobalSpdLimOpt: (Int) -> Unit,
    onThrottleGlobalSpdLimPercent: (Int) -> Unit,
    onPasStartDelayPulses: (Int) -> Unit,
    onPasStopDelayX100s: (Int) -> Unit,
    onPasKeepCurrentPercent: (Int) -> Unit,
    onPasKeepCurrentCadenceRpm: (Int) -> Unit,
    onUseSpeedSensor: (Boolean) -> Unit,
    onUseShiftSensor: (Boolean) -> Unit,
    onUsePushWalk: (Boolean) -> Unit,
    onTemperatureSensorMode: (Int) -> Unit,
    onLightsMode: (Int) -> Unit,
    onWheelSizeX10: (Int) -> Unit,
    onSpeedSensorSignals: (Int) -> Unit,
    onShiftInterruptDurationMs: (Int) -> Unit,
    onShiftInterruptCurrentThreshold: (Int) -> Unit,
    onWalkModeDataDisplay: (Int) -> Unit,
    onUseFreedomUnits: (Boolean) -> Unit,
    onRead: () -> Unit,
    onWrite: () -> Unit,
    readWriteEnabled: Boolean,
    monitoringActive: Boolean,
) {
    val cfg = state.bbsFwConfigOrDefault
    val maxCurrentLimit = BbsFwController.maxCurrentAmps(state.bbsFwVersion?.ctrlType ?: 0)

    Column(
        Modifier
            .fillMaxSize()
            .background(Tokens.Bg)
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = 16.dp)),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ReadWriteButtons(onRead = onRead, onWrite = onWrite, enabled = readWriteEnabled)
        TelemetryPausedNotice(monitoringActive = monitoringActive, aodActive = state.aodEnabled)

        // --- Global ---
        MicroLabel("Global")
        ExpandableParamTile(
            label = "Max Current (A)",
            valueLabel = "${cfg.maxCurrentAmps} A",
            description = tr(
                "Maksymalny prąd pobierany z baterii (do ${maxCurrentLimit}A dla Twojego sterownika). Na BBS02 nie " +
                    "przekraczaj bezpiecznych wartości, żeby nie uszkodzić silnika.",
                "Maximum current to draw from the battery (up to ${maxCurrentLimit}A for your controller). On BBS02, " +
                    "don't exceed safe limits to avoid motor damage.",
            ),
        ) {
            FlankedSlider(value = cfg.maxCurrentAmps, range = 5..maxCurrentLimit, accent = Tokens.Amber, onValueChange = onMaxCurrent)
        }
        ExpandableParamTile(
            label = "Current Ramp (A/s)",
            valueLabel = "${cfg.currentRampAmpsS} A/s",
            description = tr("Narastanie prądu w amperach na sekundę przy załączaniu PAS lub Cruise.", "Current ramp up in Amps per second when engaging PAS or Cruise."),
        ) {
            FlankedSlider(value = cfg.currentRampAmpsS, range = 1..255, accent = Tokens.Amber, onValueChange = onCurrentRamp)
        }
        ExpandableParamTile(
            label = "Max Battery Voltage (V)",
            valueLabel = "${cfg.maxBatteryX100v / 100.0} V",
            description = tr("Maksymalne napięcie Twojej baterii - używane do wyliczenia % naładowania (SOC).", "Maximum voltage of your battery, used for battery SOC(%) calculation."),
        ) {
            FlankedSlider(value = cfg.maxBatteryX100v / 100, range = 1..100, accent = Tokens.Blue, onValueChange = { onMaxBatteryVoltageX100(it * 100) })
        }
        ExpandableParamTile(
            label = "Low Voltage Cutoff (V)",
            valueLabel = "${cfg.lowCutOffV} V",
            description = tr("Próg niskiego napięcia, przy którym moc silnika jest odcinana, żeby chronić baterię.", "Low voltage detection for when to cut power to motor to protect battery."),
        ) {
            FlankedSlider(value = cfg.lowCutOffV, range = 1..100, accent = Tokens.Blue, onValueChange = onLowCutOff)
        }
        ExpandableParamTile(
            label = "Max Speed (km/h)",
            valueLabel = "${cfg.maxSpeedKph} km/h",
            description = tr("Maksymalna prędkość (przy włączonym czujniku prędkości) w km/h.", "Maximum speed (if using speed sensor) in km/h."),
        ) {
            FlankedSlider(value = cfg.maxSpeedKph, range = 0..180, accent = Tokens.Emerald, onValueChange = onMaxSpeed)
        }

        // --- Throttle ---
        MicroLabel("Throttle")
        ExpandableParamTile(
            label = "Start Voltage (mV)",
            valueLabel = "${cfg.throttleStartVoltageMv} mV",
            description = tr(
                "Ustawienie niższe niż minimalne napięcie sygnału z manetki spowoduje błąd.",
                "Setting lower than the minimum voltage signal from the throttle will result in an error.",
            ),
        ) {
            FlankedSlider(value = cfg.throttleStartVoltageMv, range = 200..2500, accent = Tokens.Amber, onValueChange = onThrottleStartVoltageMv)
        }
        ExpandableParamTile(
            label = "End Voltage (mV)",
            valueLabel = "${cfg.throttleEndVoltageMv} mV",
            description = tr(
                "Ustawienie wyższe niż maksymalny sygnał z manetki uniemożliwi osiągnięcie pełnej mocy.",
                "Setting this higher than the maximum signal from the throttle will make it impossible to reach maximum power.",
            ),
        ) {
            FlankedSlider(value = cfg.throttleEndVoltageMv, range = 2500..5000, accent = Tokens.Amber, onValueChange = onThrottleEndVoltageMv)
        }
        ExpandableParamTile(
            label = "Start Current (%)",
            valueLabel = "${cfg.throttleStartPercent}%",
            description = tr("Minimalna moc przy najmniejszym wychyleniu manetki.", "Minimum power to apply for lowest throttle input."),
        ) {
            FlankedSlider(value = cfg.throttleStartPercent, range = 0..100, accent = Tokens.Amber, onValueChange = onThrottleStartPercent)
        }
        ExpandableParamTile(
            label = "Global Speed Limit Options",
            valueLabel = listOf(
                tr("Wyłączony", "Disabled"), tr("Włączony", "Enabled"), tr("Standardowe poziomy", "Standard Levels"),
            ).getOrElse(cfg.throttleGlobalSpdLimOpt) { "?" },
            description = tr(
                "Globalny limit prędkości dla manetki, przydatny tam, gdzie prawo ogranicza użycie manetki do " +
                    "określonej prędkości. Wyłączony = brak limitu. Włączony = dotyczy wszystkich poziomów. " +
                    "Standardowe poziomy = tylko profil Standard (nie Sport).",
                "Global speed limit for the throttle, useful where local law only permits throttle use up to a " +
                    "specific speed. Disabled = no limit. Enabled = applies to all assist levels. Standard Levels = " +
                    "only the Standard profile (not Sport).",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onThrottleGlobalSpdLimOpt(cfg.throttleGlobalSpdLimOpt - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onThrottleGlobalSpdLimOpt(cfg.throttleGlobalSpdLimOpt + 1) }
            }
        }
        ExpandableParamTile(
            label = "Global Speed Limit (%)",
            valueLabel = "${cfg.throttleGlobalSpdLimPercent}%",
            description = tr("Limit prędkości w % skonfigurowanej prędkości maksymalnej, gdy powyższa opcja jest włączona.", "Set speed limit in % of configured max speed when global throttle speed limit is enabled."),
        ) {
            FlankedSlider(value = cfg.throttleGlobalSpdLimPercent, range = 0..100, accent = Tokens.Amber, onValueChange = onThrottleGlobalSpdLimPercent)
        }

        // --- Pedal Assist ---
        MicroLabel("Pedal Assist")
        ExpandableParamTile(
            label = "Start Delay (°)",
            valueLabel = "${cfg.pasStartDelayPulses * 15}°",
            description = tr(
                "Opóźnienie startu w stopniach obrotu korby, po którym załącza się PAS (24 impulsy = 360° = pełny obrót).",
                "Start delay in degrees for when PAS shall engage (24 pulses = 360° = one full crank revolution).",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onPasStartDelayPulses(cfg.pasStartDelayPulses - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onPasStartDelayPulses(cfg.pasStartDelayPulses + 1) }
            }
        }
        ExpandableParamTile(
            label = "Stop Delay (ms)",
            valueLabel = "${cfg.pasStopDelayX100s * 10} ms",
            description = tr("Opóźnienie w milisekundach, po którym PAS się wyłącza po zatrzymaniu pedałowania.", "Stop delay in milliseconds for when PAS shall disengage."),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onPasStopDelayX100s(cfg.pasStopDelayX100s - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onPasStopDelayX100s(cfg.pasStopDelayX100s + 1) }
            }
        }
        ExpandableParamTile(
            label = "Keep Current (%)",
            valueLabel = "${cfg.pasKeepCurrentPercent}%",
            description = tr(
                "Podtrzymuje ten procent zadanego prądu poziomu, gdy osiągnięto docelową kadencję. Dotyczy tylko " +
                    "trybów opartych o kadencję (nie Torque/Variable).",
                "Keep this motor current in percent of assist level target current when target cadence of assist " +
                    "level has been reached. Applies only to cadence-based modes.",
            ),
        ) {
            FlankedSlider(value = cfg.pasKeepCurrentPercent, range = 10..100, accent = Tokens.Amber, onValueChange = onPasKeepCurrentPercent)
        }
        ExpandableParamTile(
            label = "Keep Current Cadence (rpm)",
            valueLabel = "${cfg.pasKeepCurrentCadenceRpm} rpm",
            description = tr("Dolny próg kadencji, od którego zaczyna działać rampa \"Keep Current %\" powyżej.", "Lower cadence limit for when 'Keep Current %' ramp starts."),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onPasKeepCurrentCadenceRpm(cfg.pasKeepCurrentCadenceRpm - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onPasKeepCurrentCadenceRpm(cfg.pasKeepCurrentCadenceRpm + 1) }
            }
        }

        // --- Features ---
        MicroLabel("Features")
        TokenCard(borderColor = Tokens.WhiteBorder) {
            ToggleRow(
                "Speed Sensor", cfg.useSpeedSensor, onUseSpeedSensor, accent = Tokens.Blue,
                description = tr("Jeśli czujnik prędkości ulegnie awarii, silnik nadal będzie działał.", "If your speed sensor malfunctions your motor will still work."),
            )
            HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
            ToggleRow("Shift Sensor", cfg.useShiftSensor, onUseShiftSensor, accent = Tokens.Blue)
            HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
            ToggleRow(
                "Walk Mode", cfg.usePushWalk, onUsePushWalk, accent = Tokens.Blue,
                description = tr(
                    "Gdy wyłączony w konfiguracji, ale aktywowany komendą z wyświetlacza - kontynuuje z poprzednio wybranym poziomem wspomagania.",
                    "When walk mode is disabled in configuration but activated by command from the display the previously selected assist level will continue to be used.",
                ),
            )
        }
        ExpandableParamTile(
            label = "Temperature Sensor",
            valueLabel = listOf(
                tr("Wyłączony", "Disabled"), tr("Sterownika", "Controller"), tr("Silnika", "Motor"), "All",
            ).getOrElse(cfg.temperatureSensorMode) { "?" },
            description = tr(
                "Który czujnik temperatury jest używany do ograniczania mocy przy przegrzaniu. BBSHD ma dwa " +
                    "czujniki, BBS02 tylko jeden. Normalnie zostaw \"All\" - przydatne tylko gdy jeden z czujników jest uszkodzony.",
                "Select which temperature sensors to use for thermal limiting. The BBSHD has two temperature " +
                    "sensors, BBS02 only has a single. Normally leave as \"All\" - useful only if one sensor is broken.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onTemperatureSensorMode(cfg.temperatureSensorMode - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onTemperatureSensorMode(cfg.temperatureSensorMode + 1) }
            }
        }
        ExpandableParamTile(
            label = "Lights Mode",
            valueLabel = listOf(
                tr("Domyślny (z wyświetlacza)", "Default (display-controlled)"), tr("Wyłączone", "Disabled"),
                tr("Zawsze włączone", "Always On"), tr("Światło stopu", "Brake Light"),
            ).getOrElse(cfg.lightsMode) { "?" },
            description = tr("Sterowanie wyjściem świateł zewnętrznych.", "Options for controlling external lights output."),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onLightsMode(cfg.lightsMode - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onLightsMode(cfg.lightsMode + 1) }
            }
        }
        // --- Speed Sensor ---
        MicroLabel("Speed Sensor")
        ExpandableParamTile(
            label = "Wheel Size (inch)",
            valueLabel = "${cfg.wheelSizeInchX10 / 10.0}\"",
            description = tr("Rozmiar koła (w calach) używany do przeliczeń prędkości.", "Wheel size (in inch) to use for speed calculations."),
        ) {
            FlankedSlider(value = cfg.wheelSizeInchX10, range = 100..400, accent = Tokens.Emerald, onValueChange = onWheelSizeX10)
        }
        ExpandableParamTile(
            label = "Signals (per rotation)",
            valueLabel = cfg.speedSensorSignals.toString(),
            description = tr("Liczba sygnałów czujnika prędkości na jeden obrót koła.", "Number of speed sensor signals per wheel rotation."),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onSpeedSensorSignals(cfg.speedSensorSignals - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onSpeedSensorSignals(cfg.speedSensorSignals + 1) }
            }
        }

        // --- Shift Sensor ---
        MicroLabel("Shift Sensor")
        ExpandableParamTile(
            label = "Shift Interrupt Duration (ms)",
            valueLabel = "${cfg.shiftInterruptDurationMs} ms",
            description = tr("Czas trwania przerwania mocy po wykryciu zmiany biegu.", "Duration in milliseconds of the power interrupt that should occur."),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onShiftInterruptDurationMs(cfg.shiftInterruptDurationMs - 10) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onShiftInterruptDurationMs(cfg.shiftInterruptDurationMs + 10) }
            }
        }
        ExpandableParamTile(
            label = "Shift Current Threshold (%)",
            valueLabel = "${cfg.shiftInterruptCurrentThresholdPercent}%",
            description = tr("Maksymalny prąd silnika podczas zmiany biegu, w % Max Current (A) z sekcji Global.", "Maximum motor current during shifting expressed as a percentage of Max Current."),
        ) {
            FlankedSlider(value = cfg.shiftInterruptCurrentThresholdPercent, range = 0..100, accent = Tokens.Amber, onValueChange = onShiftInterruptCurrentThreshold)
        }

        // --- Miscellaneous ---
        MicroLabel("Miscellaneous")
        ExpandableParamTile(
            label = "Walk Mode Data Display",
            valueLabel = listOf(
                "Speed", tr("Temperatura (°C)", "Temperature (C)"),
                tr("Żądana moc (%)", "Requested Power (%)"), tr("Poziom baterii (%)", "Battery Level (%)"),
            ).getOrElse(cfg.walkModeDataDisplay) { "?" },
            description = tr("Nadpisuje pole prędkości na wyświetlaczu inną daną, gdy aktywny jest tryb prowadzenia roweru.", "Override speed field display with Temperature, Requested Power, Battery Level or Speed while walk mode is active."),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onWalkModeDataDisplay(cfg.walkModeDataDisplay - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onWalkModeDataDisplay(cfg.walkModeDataDisplay + 1) }
            }
        }
        TokenCard(borderColor = Tokens.WhiteBorder) {
            ToggleRow(
                tr("Jednostki imperialne (mph)", "Freedom units (mph)"),
                cfg.useFreedomUnits, onUseFreedomUnits, accent = Tokens.Blue,
                description = tr(
                    "Nie ma odpowiednika w zakładce System jego apki (tam to opcja globalna programu, Menu → Options) - " +
                        "dotyczy WYŁĄCZNIE wartości wysyłanych do wyświetlacza przez bbs-fw, nie jednostek w tej apce (patrz Ustawienia).",
                    "Has no equivalent in his app's System tab (there it's a global app option, Menu → Options) - " +
                        "affects ONLY the values bbs-fw sends to the display, not this app's own units (see Settings).",
                ),
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

