package com.bafspeed.app

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import kotlin.system.exitProcess
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bafspeed.app.i18n.LocalAppLanguage
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.ui.screens.AboutScreen
import com.bafspeed.app.ui.screens.AssistLevelsScreen
import com.bafspeed.app.ui.screens.BafangTypeScreen
import com.bafspeed.app.ui.screens.BatteryPill
import com.bafspeed.app.ui.screens.BatteryScreen
import com.bafspeed.app.ui.screens.BbsFwAssistLevelsScreen
import com.bafspeed.app.ui.screens.BbsFwInfoScreen
import com.bafspeed.app.ui.screens.BbsFwSystemScreen
import com.bafspeed.app.ui.screens.CalibrationScreen
import com.bafspeed.app.ui.screens.ConnectScreen
import com.bafspeed.app.ui.screens.DashboardScreen
import com.bafspeed.app.ui.screens.DiagnosticsScreen
import com.bafspeed.app.ui.screens.DisplayScreen
import com.bafspeed.app.ui.screens.GeneralScreen
import com.bafspeed.app.ui.screens.LanguageScreen
import com.bafspeed.app.ui.screens.MonitoringScreen
import com.bafspeed.app.ui.screens.ParametersScreen
import com.bafspeed.app.ui.screens.PedalScreen
import com.bafspeed.app.ui.screens.ProfilesScreen
import com.bafspeed.app.ui.screens.SagScreen
import com.bafspeed.app.ui.screens.ThrottleScreen
import com.bafspeed.app.ui.screens.SettingsScreen
import com.bafspeed.app.ui.screens.ServiceScreen
import com.bafspeed.app.ui.screens.TemperatureControlScreen
import com.bafspeed.app.ui.components.EggSpeedWordmark
import com.bafspeed.app.ui.components.WriteFlowDialogs
import com.bafspeed.app.ui.theme.LocalHighContrast
import com.bafspeed.app.ui.theme.LocalLightMode
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens
import kotlinx.coroutines.launch

private enum class Screen {
    CONNECT,
    DASHBOARD,
    BAFANG_TYPE,
    MOTOR,
    PEDAL,
    THROTTLE,
    ASSIST,
    BBSFW_INFO,
    BBSFW_SYSTEM,
    BBSFW_ASSIST,
    BATTERY,
    SAG,
    MONITORING,
    TEMPERATURE_CONTROL,
    SETTINGS,
    DISPLAY,
    SERVICE,
    CALIBRATION,
    DIAGNOSTICS,
    PROFILES,
    REGISTER_DIAGNOSTICS,
    LANGUAGE,
    ABOUT,
}

/** Ekrany specyficzne dla firmware OEM Bafang - ukrywane w menu, gdy wybrano bbs-fw (patrz [FirmwareType]). */
private val OEM_ONLY_SCREENS = setOf(Screen.BAFANG_TYPE, Screen.MOTOR, Screen.PEDAL, Screen.THROTTLE, Screen.ASSIST)

/** Ekrany specyficzne dla bbs-fw - protokół konfiguracji jest inny, więc mają osobny komplet zakładek. */
private val BBS_FW_ONLY_SCREENS = setOf(Screen.BBSFW_INFO, Screen.BBSFW_SYSTEM, Screen.BBSFW_ASSIST, Screen.TEMPERATURE_CONTROL)

/** Ekrany z odczytem/zapisem configu (przyciski Odczytaj/Zapisz) - patrz AppViewModel.setConfigScreenOpen. */
private val CONFIG_SCREENS = setOf(Screen.ASSIST, Screen.MOTOR, Screen.PEDAL, Screen.THROTTLE, Screen.BBSFW_SYSTEM, Screen.BBSFW_ASSIST)

/**
 * Tytuł zakładki - zależny od języka (patrz [LocalAppLanguage]). [Screen.DIAGNOSTICS] dodatkowo
 * zależy od aktywnego firmware - to jedyna pozycja menu wspólna dla obu firmware, więc dostaje
 * dopisek "Bafang"/"BBS-FW" (patrz [DrawerContent], gdzie jest wstawiana zaraz po zakładce
 * poziomów wspomagania właściwej dla danego firmware).
 */
@Composable
private fun Screen.title(firmwareType: FirmwareType): String = when (this) {
    Screen.CONNECT -> tr("Połączenie", "Connect", de = "Verbindung", fr = "Connexion", es = "Conexión", pt = "Ligação", it = "Connessione", nl = "Verbinding", sv = "Anslutning", cs = "Připojení", sk = "Pripojenie", da = "Forbindelse", ru = "Подключение")
    Screen.DASHBOARD -> tr("Kokpit", "Cockpit", de = "Cockpit", fr = "Cockpit", es = "Cockpit", pt = "Cockpit", it = "Cockpit", nl = "Cockpit", sv = "Cockpit", cs = "Kokpit", sk = "Kokpit", da = "Cockpit", ru = "Кокпит")
    Screen.BAFANG_TYPE -> tr("Bafang - Typ silnika", "Bafang Motor Type", de = "Bafang - Motortyp", fr = "Bafang - Type de moteur", es = "Bafang - Tipo de motor", pt = "Bafang - Tipo de motor", it = "Bafang - Tipo di motore", nl = "Bafang - Motortype", sv = "Bafang - Motortyp", cs = "Bafang - Typ motoru", sk = "Bafang - Typ motora", da = "Bafang - Motortype", ru = "Bafang - Тип двигателя")
    Screen.MOTOR -> tr("Bafang - Ustawienia podstawowe", "Bafang Basic", de = "Bafang - Grundeinstellungen", fr = "Bafang - Réglages de base", es = "Bafang - Ajustes básicos", pt = "Bafang - Definições básicas", it = "Bafang - Impostazioni di base", nl = "Bafang - Basisinstellingen", sv = "Bafang - Grundinställningar", cs = "Bafang - Základní nastavení", sk = "Bafang - Základné nastavenia", da = "Bafang - Grundindstillinger", ru = "Bafang - Основные настройки")
    Screen.PEDAL -> tr("Bafang - Pedałowanie (PAS)", "Bafang Pedal (PAS)", de = "Bafang - Pedalunterstützung (PAS)", fr = "Bafang - Pédalage (PAS)", es = "Bafang - Pedaleo (PAS)", pt = "Bafang - Pedalada (PAS)", it = "Bafang - Pedalata (PAS)", nl = "Bafang - Trappen (PAS)", sv = "Bafang - Trampning (PAS)", cs = "Bafang - Šlapání (PAS)", sk = "Bafang - Šliapanie (PAS)", da = "Bafang - Pedalering (PAS)", ru = "Bafang - Педалирование (PAS)")
    Screen.THROTTLE -> tr("Bafang - Manetka", "Bafang Throttle", de = "Bafang - Gasgriff", fr = "Bafang - Accélérateur", es = "Bafang - Acelerador", pt = "Bafang - Acelerador", it = "Bafang - Acceleratore", nl = "Bafang - Gasgreep", sv = "Bafang - Gasreglage", cs = "Bafang - Plynová páčka", sk = "Bafang - Plynová páčka", da = "Bafang - Gashåndtag", ru = "Bafang - Ручка газа")
    Screen.ASSIST -> tr("Bafang - Poziomy wspomagania", "Bafang Assist levels", de = "Bafang - Unterstützungsstufen", fr = "Bafang - Niveaux d'assistance", es = "Bafang - Niveles de asistencia", pt = "Bafang - Níveis de assistência", it = "Bafang - Livelli di assistenza", nl = "Bafang - Ondersteuningsniveaus", sv = "Bafang - Assistansnivåer", cs = "Bafang - Úrovně asistence", sk = "Bafang - Úrovne asistencie", da = "Bafang - Assistanceniveauer", ru = "Bafang - Уровни ассистента")
    Screen.BBSFW_INFO -> "BBS-FW Version"
    Screen.BBSFW_SYSTEM -> "BBS-FW System"
    Screen.BBSFW_ASSIST -> "BBS-FW Assist Levels"
    Screen.BATTERY -> tr("Bateria", "Battery", de = "Batterie", fr = "Batterie", es = "Batería", pt = "Bateria", it = "Batteria", nl = "Batterij", sv = "Batteri", cs = "Baterie", sk = "Batéria", da = "Batteri", ru = "Батарея")
    Screen.SAG -> tr("Pomiar SAG baterii", "Battery SAG Measurement", de = "SAG-Messung der Batterie", fr = "Mesure SAG de la batterie", es = "Medición SAG de la batería", pt = "Medição SAG da bateria", it = "Misurazione SAG della batteria", nl = "SAG-meting van batterij", sv = "SAG-mätning av batteri", cs = "Měření SAG baterie", sk = "Meranie SAG batérie", da = "SAG-måling af batteri", ru = "Измерение SAG батареи")
    Screen.MONITORING -> tr("Monitoring", "Monitoring", de = "Monitoring", fr = "Monitoring", es = "Monitoring", pt = "Monitoring", it = "Monitoring", nl = "Monitoring", sv = "Monitoring", cs = "Monitoring", sk = "Monitoring", da = "Monitoring", ru = "Monitoring")
    Screen.TEMPERATURE_CONTROL -> tr("Kontrola temperatury", "Temperature control", de = "Temperaturkontrolle", fr = "Contrôle de température", es = "Control de temperatura", pt = "Controlo de temperatura", it = "Controllo temperatura", nl = "Temperatuurregeling", sv = "Temperaturkontroll", cs = "Kontrola teploty", sk = "Kontrola teploty", da = "Temperaturkontrol", ru = "Контроль температуры")
    Screen.SETTINGS -> tr("Ustawienia", "Settings", de = "Einstellungen", fr = "Réglages", es = "Ajustes", pt = "Definições", it = "Impostazioni", nl = "Instellingen", sv = "Inställningar", cs = "Nastavení", sk = "Nastavenia", da = "Indstillinger", ru = "Настройки")
    Screen.DISPLAY -> tr("Ekran", "Screen", de = "Bildschirm", fr = "Écran", es = "Pantalla", pt = "Ecrã", it = "Schermo", nl = "Scherm", sv = "Skärm", cs = "Obrazovka", sk = "Obrazovka", da = "Skærm", ru = "Экран")
    Screen.SERVICE -> "PROTECT"
    Screen.CALIBRATION -> tr("Kalibracja", "Calibration", de = "Kalibrierung", fr = "Étalonnage", es = "Calibración", pt = "Calibração", it = "Calibrazione", nl = "Kalibratie", sv = "Kalibrering", cs = "Kalibrace", sk = "Kalibrácia", da = "Kalibrering", ru = "Калибровка")
    Screen.DIAGNOSTICS -> if (firmwareType == FirmwareType.BBS_FW) {
        tr("BBS-FW - Widok wszystkiego", "BBS-FW All in View", de = "BBS-FW - Alles im Blick", fr = "BBS-FW - Tout en un coup d'œil", es = "BBS-FW - Todo en una vista", pt = "BBS-FW - Tudo numa vista", it = "BBS-FW - Tutto in un colpo d'occhio", nl = "BBS-FW - Alles in beeld", sv = "BBS-FW - Allt i vy", cs = "BBS-FW - Vše v jednom pohledu", sk = "BBS-FW - Všetko v jednom pohľade", da = "BBS-FW - Alt på ét blik", ru = "BBS-FW - Всё в одном взгляде")
    } else {
        tr("Bafang - Widok wszystkiego", "Bafang All in View", de = "Bafang - Alles im Blick", fr = "Bafang - Tout en un coup d'œil", es = "Bafang - Todo en una vista", pt = "Bafang - Tudo numa vista", it = "Bafang - Tutto in un colpo d'occhio", nl = "Bafang - Alles in beeld", sv = "Bafang - Allt i vy", cs = "Bafang - Vše v jednom pohledu", sk = "Bafang - Všetko v jednom pohľade", da = "Bafang - Alt på ét blik", ru = "Bafang - Всё в одном взгляде")
    }
    Screen.PROFILES -> tr("Profile", "Profiles", de = "Profile", fr = "Profils", es = "Perfiles", pt = "Perfis", it = "Profili", nl = "Profielen", sv = "Profiler", cs = "Profily", sk = "Profily", da = "Profiler", ru = "Профили")
    Screen.REGISTER_DIAGNOSTICS -> tr("Diagnostyka", "Diagnostics", de = "Diagnose", fr = "Diagnostic", es = "Diagnóstico", pt = "Diagnóstico", it = "Diagnostica", nl = "Diagnostiek", sv = "Diagnostik", cs = "Diagnostika", sk = "Diagnostika", da = "Diagnostik", ru = "Диагностика")
    Screen.LANGUAGE -> tr("Język", "Language", de = "Sprache", fr = "Langue", es = "Idioma", pt = "Idioma", it = "Lingua", nl = "Taal", sv = "Språk", cs = "Jazyk", sk = "Jazyk", da = "Sprog", ru = "Язык")
    Screen.ABOUT -> tr("Menu", "Menu", de = "Menü", fr = "Menu", es = "Menú", pt = "Menu", it = "Menu", nl = "Menu", sv = "Meny", cs = "Menu", sk = "Menu", da = "Menu", ru = "Меню")
}

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.refreshProfiles()
        setContent { App(viewModel) }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAppResumed()
    }
}

@Composable
private fun App(vm: AppViewModel) {
    val state by vm.state.collectAsState()
    val telemetry by vm.telemetry.collectAsState()
    val monitoring by vm.monitoring.collectAsState()
    val scanResults by vm.scanResults.collectAsState()
    val scanProgress by vm.scanProgress.collectAsState()
    val scanning by vm.scanning.collectAsState()
    val fullScanHistory by vm.fullScanHistory.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    var screen by remember { mutableStateOf(Screen.CONNECT) }

    // SAF: zapis profilu do dowolnej lokalizacji
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri: Uri? ->
        if (uri != null) {
            val lang = state.language
            runCatching {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(vm.exportIni()) }
            }.onSuccess { Toast.makeText(context, tr(lang, "Zapisano profil", "Profile saved", de = "Profil gespeichert", fr = "Profil enregistré", es = "Perfil guardado", pt = "Perfil guardado", it = "Profilo salvato", nl = "Profiel opgeslagen", sv = "Profil sparad", cs = "Profil uložen", sk = "Profil uložený", da = "Profil gemt", ru = "Профиль сохранён"), Toast.LENGTH_SHORT).show() }
                .onFailure { Toast.makeText(context, tr(lang, "Błąd zapisu: ${it.message}", "Save error: ${it.message}", de = "Fehler beim Speichern: ${it.message}", fr = "Erreur d'enregistrement : ${it.message}", es = "Error al guardar: ${it.message}", pt = "Erro ao guardar: ${it.message}", it = "Errore di salvataggio: ${it.message}", nl = "Opslagfout: ${it.message}", sv = "Sparfel: ${it.message}", cs = "Chyba při ukládání: ${it.message}", sk = "Chyba pri ukladaní: ${it.message}", da = "Fejl ved gemning: ${it.message}", ru = "Ошибка сохранения: ${it.message}"), Toast.LENGTH_LONG).show() }
        }
    }

    // GPS Speed (Ustawienia) - uprawnienie proszone dopiero przy wlaczeniu opcji, nie przy starcie
    // apki. Odmowa zostawia przelacznik wylaczonym (patrz onGpsSpeedChange nizej).
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted: Boolean ->
        vm.setGpsSpeedEnabled(granted)
        if (!granted) {
            Toast.makeText(context, tr(state.language, "Brak zgody na dostęp do lokalizacji", "Location permission denied", de = "Standortzugriff verweigert", fr = "Autorisation de localisation refusée", es = "Permiso de ubicación denegado", pt = "Permissão de localização negada", it = "Permesso di posizione negato", nl = "Locatietoestemming geweigerd", sv = "Platsbehörighet nekad", cs = "Přístup k poloze odepřen", sk = "Prístup k polohe zamietnutý", da = "Placeringstilladelse nægtet", ru = "Доступ к местоположению запрещён"), Toast.LENGTH_LONG).show()
        }
    }
    val onGpsSpeedChange: (Boolean) -> Unit = { enabled ->
        if (!enabled) {
            vm.setGpsSpeedEnabled(false)
        } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            vm.setGpsSpeedEnabled(true)
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // SAF: wczytanie profilu z pliku
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri != null) {
            val lang = state.language
            val text = runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }.getOrNull()
            if (text == null) {
                Toast.makeText(context, tr(lang, "Nie udało się odczytać pliku", "Failed to read the file", de = "Datei konnte nicht gelesen werden", fr = "Échec de la lecture du fichier", es = "No se pudo leer el archivo", pt = "Não foi possível ler o ficheiro", it = "Impossibile leggere il file", nl = "Kan bestand niet lezen", sv = "Det gick inte att läsa filen", cs = "Soubor se nepodařilo přečíst", sk = "Súbor sa nepodarilo prečítať", da = "Filen kunne ikke læses", ru = "Не удалось прочитать файл"), Toast.LENGTH_LONG).show()
            } else {
                vm.importIni(text)
                    .onSuccess { Toast.makeText(context, tr(lang, "Wczytano profil (podgląd)", "Profile loaded (preview)", de = "Profil geladen (Vorschau)", fr = "Profil chargé (aperçu)", es = "Perfil cargado (vista previa)", pt = "Perfil carregado (pré-visualização)", it = "Profilo caricato (anteprima)", nl = "Profiel geladen (voorbeeld)", sv = "Profil laddad (förhandsvisning)", cs = "Profil načten (náhled)", sk = "Profil načítaný (náhľad)", da = "Profil indlæst (forhåndsvisning)", ru = "Профиль загружен (предпросмотр)"), Toast.LENGTH_SHORT).show() }
                    .onFailure { Toast.makeText(context, tr(lang, "Błędny plik: ${it.message}", "Invalid file: ${it.message}", de = "Ungültige Datei: ${it.message}", fr = "Fichier invalide : ${it.message}", es = "Archivo inválido: ${it.message}", pt = "Ficheiro inválido: ${it.message}", it = "File non valido: ${it.message}", nl = "Ongeldig bestand: ${it.message}", sv = "Ogiltig fil: ${it.message}", cs = "Neplatný soubor: ${it.message}", sk = "Neplatný súbor: ${it.message}", da = "Ugyldig fil: ${it.message}", ru = "Недопустимый файл: ${it.message}"), Toast.LENGTH_LONG).show() }
            }
        }
    }

    fun go(s: Screen) {
        screen = s
        scope.launch { drawerState.close() }
    }

    // Monitoring/AOD (gdy działają bez otwartego Kokpitu) wstrzymują się na czas edycji configu -
    // patrz AppViewModel.setConfigScreenOpen/syncDisplayPolling.
    LaunchedEffect(screen) {
        vm.setConfigScreenOpen(screen in CONFIG_SCREENS)
    }

    // Gdy zmieni się firmware (zakładka Ustawienia), zakładka aktualnie otwarta może zniknąć z menu
    // (patrz OEM_ONLY_SCREENS/BBS_FW_ONLY_SCREENS) - w takim wypadku przenosimy na odpowiednik "Typ/Wersja".
    LaunchedEffect(state.firmwareType) {
        val hidden = if (state.firmwareType == FirmwareType.BBS_FW) OEM_ONLY_SCREENS else BBS_FW_ONLY_SCREENS
        if (screen in hidden) {
            screen = if (state.firmwareType == FirmwareType.BBS_FW) Screen.BBSFW_INFO else Screen.BAFANG_TYPE
        }
    }

    CompositionLocalProvider(
        LocalAppLanguage provides state.language,
        LocalHighContrast provides state.highContrast,
        LocalLightMode provides state.lightMode,
    ) {
    // Pasek statusu i nawigacyjny Androida sa ustawiane w themes.xml (statyczne, tylko ciemne) -
    // tutaj nadpisujemy je w locie kolorem Tokens.Bg, zeby realnie podazaly za Trybem jasnym/ciemnym.
    val view = LocalView.current
    val systemBarColor = Tokens.Bg
    val lightSystemBars = state.lightMode
    SideEffect {
        val window = (context as ComponentActivity).window
        window.statusBarColor = systemBarColor.toArgb()
        window.navigationBarColor = systemBarColor.toArgb()
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = lightSystemBars
            isAppearanceLightNavigationBars = lightSystemBars
        }
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = Tokens.Card) {
                DrawerContent(
                    current = screen,
                    firmwareType = state.firmwareType,
                    onNavigate = { go(it) },
                    onClose = {
                        vm.disconnect()
                        (context as? Activity)?.finishAffinity()
                        exitProcess(0)
                    },
                )
            }
        },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(Tokens.Bg)
                .statusBarsPadding(),
        ) {
            TopBar(
                title = screen.title(state.firmwareType),
                onMenu = { scope.launch { drawerState.open() } },
                trailing = when {
                    screen == Screen.DASHBOARD -> {
                        // Bez polaczenia pokazujemy ostatnio znany % (patrz UiState.lastKnownBatteryPct),
                        // zamiast zerowac wskaznik do 0.
                        val pct = if (state.testMode) 100 else if (state.connection == ConnectionStatus.CONNECTED) telemetry.batteryPct else state.lastKnownBatteryPct
                        { BatteryPill(pct = pct, scale = 1.5f) }
                    }
                    else -> null
                },
                showWordmark = screen == Screen.DASHBOARD,
            )
            Box(
                Modifier
                    .weight(1f)
                    .navigationBarsPadding(),
            ) {
                val readWriteEnabled = state.connection == ConnectionStatus.CONNECTED && !state.displayMode

                when (screen) {
                    Screen.CONNECT -> ConnectScreen(
                        state = state,
                        onConnect = vm::connect,
                        onDisconnect = vm::disconnect,
                        onGoToDashboard = { go(Screen.DASHBOARD) },
                    )
                    Screen.DASHBOARD -> DashboardScreen(
                        state = state,
                        telemetry = telemetry,
                        onStartDisplay = vm::startDisplayMode,
                        onStopDisplay = vm::stopDisplayMode,
                        onAssistChange = vm::setAssistLevel,
                        onLightToggle = { vm.setLight(!state.lightOn) },
                        onSportModeToggle = { vm.setSportMode(!state.sportMode) },
                        onGoToConnect = { go(Screen.CONNECT) },
                        onResetTrip = vm::resetTrip,
                        onResetAvgSpeed = vm::resetAvgSpeed,
                        onActivateProtect = vm::activateProtect,
                    )
                    Screen.ASSIST -> AssistLevelsScreen(
                        state = state,
                        onCurrentChange = vm::setAssistLevelCurrent,
                        onSpeedChange = vm::setAssistLevelSpeed,
                        onRead = vm::readAllConfig,
                        onWrite = vm::requestSaveToController,
                        readWriteEnabled = readWriteEnabled,
                        monitoringActive = monitoring.masterEnabled,
                    )
                    Screen.BAFANG_TYPE -> BafangTypeScreen(state = state)
                    Screen.MOTOR -> GeneralScreen(
                        state = state,
                        onLowBatteryProtectionChange = vm::setLowBatteryProtection,
                        onCurrentLimitChange = vm::setCurrentLimit,
                        onSpeedMeterTypeChange = vm::setSpeedMeterModel,
                        onSpeedMeterSignalsChange = vm::setSpeedMeterSignals,
                        onWheelChange = vm::setWheelSize,
                        onRead = vm::readAllConfig,
                        onWrite = vm::requestSaveToController,
                        readWriteEnabled = readWriteEnabled,
                        monitoringActive = monitoring.masterEnabled,
                    )
                    Screen.PEDAL -> PedalScreen(
                        state = state,
                        onPedalType = vm::setPasPedalType,
                        onDesignatedAssist = vm::setPasDesignatedAssist,
                        onSpeedLimit = vm::setPasSpeedLimit,
                        onStartCurrent = vm::setPasStartCurrent,
                        onSlowStartMode = vm::setPasSlowStartMode,
                        onStartDegree = vm::setPasStartDegree,
                        onWorkMode = vm::setPasWorkMode,
                        onTimeOfStop = vm::setPasTimeOfStop,
                        onCurrentDecay = vm::setPasCurrentDecay,
                        onStopDecay = vm::setPasStopDecay,
                        onKeepCurrent = vm::setPasKeepCurrent,
                        onRead = vm::readAllConfig,
                        onWrite = vm::requestSaveToController,
                        readWriteEnabled = readWriteEnabled,
                        monitoringActive = monitoring.masterEnabled,
                    )
                    Screen.THROTTLE -> ThrottleScreen(
                        state = state,
                        onStartVoltage = vm::setThrStartVoltage,
                        onEndVoltage = vm::setThrEndVoltage,
                        onMode = vm::setThrottleMode,
                        onDesignatedAssist = vm::setThrottleDesignatedAssist,
                        onSpeedLimit = vm::setThrSpeedLimit,
                        onStartCurrent = vm::setThrStartCurrent,
                        onRead = vm::readAllConfig,
                        onWrite = vm::requestSaveToController,
                        readWriteEnabled = readWriteEnabled,
                        monitoringActive = monitoring.masterEnabled,
                    )
                    Screen.BBSFW_INFO -> BbsFwInfoScreen(state = state)
                    Screen.BBSFW_SYSTEM -> BbsFwSystemScreen(
                        state = state,
                        onMaxCurrent = vm::setBbsFwMaxCurrentAmps,
                        onCurrentRamp = vm::setBbsFwCurrentRampAmpsS,
                        onMaxBatteryVoltageX100 = vm::setBbsFwMaxBatteryVoltageX100,
                        onLowCutOff = vm::setBbsFwLowCutOffV,
                        onMaxSpeed = vm::setBbsFwMaxSpeedKph,
                        onThrottleStartVoltageMv = vm::setBbsFwThrottleStartVoltageMv,
                        onThrottleEndVoltageMv = vm::setBbsFwThrottleEndVoltageMv,
                        onThrottleStartPercent = vm::setBbsFwThrottleStartPercent,
                        onThrottleGlobalSpdLimOpt = vm::setBbsFwThrottleGlobalSpdLimOpt,
                        onThrottleGlobalSpdLimPercent = vm::setBbsFwThrottleGlobalSpdLimPercent,
                        onPasStartDelayPulses = vm::setBbsFwPasStartDelayPulses,
                        onPasStopDelayX100s = vm::setBbsFwPasStopDelayX100s,
                        onPasKeepCurrentPercent = vm::setBbsFwPasKeepCurrentPercent,
                        onPasKeepCurrentCadenceRpm = vm::setBbsFwPasKeepCurrentCadenceRpm,
                        onUseSpeedSensor = vm::setBbsFwUseSpeedSensor,
                        onUseShiftSensor = vm::setBbsFwUseShiftSensor,
                        onUsePushWalk = vm::setBbsFwUsePushWalk,
                        onTemperatureSensorMode = vm::setBbsFwTemperatureSensorMode,
                        onLightsMode = vm::setBbsFwLightsMode,
                        onWheelSizeX10 = vm::setBbsFwWheelSizeInchX10,
                        onSpeedSensorSignals = vm::setBbsFwSpeedSensorSignals,
                        onShiftInterruptDurationMs = vm::setBbsFwShiftInterruptDurationMs,
                        onShiftInterruptCurrentThreshold = vm::setBbsFwShiftInterruptCurrentThresholdPercent,
                        onWalkModeDataDisplay = vm::setBbsFwWalkModeDataDisplay,
                        onUseFreedomUnits = vm::setBbsFwUseFreedomUnits,
                        onRead = vm::readAllConfig,
                        onWrite = vm::requestSaveToController,
                        readWriteEnabled = readWriteEnabled,
                        monitoringActive = monitoring.masterEnabled,
                    )
                    Screen.BBSFW_ASSIST -> BbsFwAssistLevelsScreen(
                        state = state,
                        onBaseType = vm::setBbsFwAssistBaseType,
                        onPasVariant = vm::setBbsFwAssistPasVariant,
                        onTargetCurrent = vm::setBbsFwAssistTargetCurrent,
                        onMaxThrottleCurrent = vm::setBbsFwAssistMaxThrottleCurrent,
                        onMaxCadence = vm::setBbsFwAssistMaxCadence,
                        onMaxSpeed = vm::setBbsFwAssistMaxSpeed,
                        onTorqueFactor = vm::setBbsFwAssistTorqueFactor,
                        onFlag = vm::setBbsFwAssistFlag,
                        onAssistModeSelect = vm::setBbsFwAssistModeSelect,
                        onAssistStartupLevel = vm::setBbsFwAssistStartupLevel,
                        onRead = vm::readAllConfig,
                        onWrite = vm::requestSaveToController,
                        readWriteEnabled = readWriteEnabled,
                        monitoringActive = monitoring.masterEnabled,
                    )
                    Screen.BATTERY -> BatteryScreen(
                        state = state,
                        onCellCountChange = vm::setCellCount,
                        onCapacityAhChange = vm::setCapacityAh,
                        onCapacityWhChange = vm::setCapacityWh,
                    )
                    Screen.SAG -> SagScreen(
                        state = state,
                        onStart = vm::startSagCalibration,
                        onCancel = vm::cancelSagCalibration,
                    )
                    Screen.MONITORING -> MonitoringScreen(
                        monitoring = monitoring,
                        onMasterEnabledChange = vm::setMonitoringEnabled,
                        onChartEnabledChange = vm::setMonitoringChartEnabled,
                    )
                    Screen.TEMPERATURE_CONTROL -> TemperatureControlScreen(
                        state = state,
                        onShowChange = vm::setShowTempOnCockpit,
                        onWarningChange = vm::setTempWarningC,
                        onAlarmChange = vm::setTempAlarmC,
                        onAlarmSoundChange = vm::setTempAlarmSoundEnabled,
                    )
                    Screen.CALIBRATION -> CalibrationScreen(
                        state = state,
                        telemetry = telemetry,
                        onFactorChange = vm::setCurrentCalibrationFactor,
                        onVoltageOffsetChange = vm::setVoltageCalibrationOffsetV,
                        onSpeedFactorChange = vm::setSpeedCalibrationFactor,
                        onStartDisplay = vm::startDisplayMode,
                        onStopDisplay = vm::stopDisplayMode,
                    )
                    Screen.REGISTER_DIAGNOSTICS -> DiagnosticsScreen(
                        state = state,
                        scanResults = scanResults,
                        scanProgress = scanProgress,
                        scanning = scanning,
                        fullScanHistory = fullScanHistory,
                        onStartScan = vm::startFullDiagnosticScan,
                        onToggleTestMode = vm::toggleTestMode,
                    )
                    Screen.DIAGNOSTICS -> ParametersScreen(state = state, onRefresh = vm::readAllConfig)
                    Screen.PROFILES -> ProfilesScreen(
                        state = state,
                        onSaveNew = vm::saveProfileInternal,
                        onLoad = { name ->
                            vm.loadProfileInternal(name)
                                .onSuccess { go(if (state.firmwareType == FirmwareType.BBS_FW) Screen.BBSFW_SYSTEM else Screen.MOTOR) }
                                .onFailure {
                                    Toast.makeText(
                                        context,
                                        tr(
                                            state.language,
                                            "Błąd wczytywania profilu: ${it.message}",
                                            "Error loading profile: ${it.message}",
                                            de = "Fehler beim Laden des Profils: ${it.message}",
                                            fr = "Erreur de chargement du profil : ${it.message}",
                                            es = "Error al cargar el perfil: ${it.message}",
                                            pt = "Erro ao carregar o perfil: ${it.message}",
                                            it = "Errore durante il caricamento del profilo: ${it.message}",
                                            nl = "Fout bij laden van profiel: ${it.message}",
                                            sv = "Fel vid inläsning av profil: ${it.message}",
                                            cs = "Chyba při načítání profilu: ${it.message}",
                                            sk = "Chyba pri načítaní profilu: ${it.message}",
                                            da = "Fejl ved indlæsning af profil: ${it.message}",
                                            ru = "Ошибка загрузки профиля: ${it.message}",
                                        ),
                                        Toast.LENGTH_LONG,
                                    ).show()
                                }
                        },
                        onDelete = vm::deleteProfileInternal,
                        onImportFile = { importLauncher.launch(arrayOf("*/*")) },
                        onExportFile = { exportLauncher.launch("eggspeed-profil.ini") },
                    )
                    Screen.SETTINGS -> SettingsScreen(
                        state = state,
                        onUnitsChange = vm::setUnits,
                        onOdoOffsetChange = vm::setOdoOffsetKm,
                        onFirmwareTypeChange = vm::setFirmwareType,
                        onFastCockpitRefreshChange = vm::setFastCockpitRefresh,
                        onGpsSpeedChange = onGpsSpeedChange,
                    )
                    Screen.DISPLAY -> DisplayScreen(
                        state = state,
                        onHighContrastChange = vm::setHighContrast,
                        onLightModeChange = vm::setLightMode,
                        onAodEnabledChange = vm::setAodEnabled,
                        onAodAssistControlsChange = vm::setAodAssistControlsEnabled,
                        onLargeCockpitDigitsChange = vm::setLargeCockpitDigits,
                    )
                    Screen.SERVICE -> ServiceScreen(
                        state = state,
                        onProtectFeatureEnabledChange = vm::setProtectFeatureEnabled,
                        onDeactivateProtect = vm::deactivateProtect,
                        onSetServicePin = vm::setServicePin,
                    )
                    Screen.LANGUAGE -> LanguageScreen(current = state.language, onSelect = vm::setLanguage)
                    Screen.ABOUT -> AboutScreen()
                }
            }
        }
    }

    WriteFlowDialogs(
        flow = state.writeFlow,
        onConfirm = vm::confirmSaveToController,
        onCancel = vm::cancelSaveToController,
        onAcknowledge = vm::acknowledgeWriteResult,
    )
    }
}

@Composable
private fun TopBar(
    title: String,
    onMenu: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
    showWordmark: Boolean = false,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(Tokens.Bg)
            .padding(top = 10.dp, bottom = 4.dp),
    ) {
        Row(
            Modifier.align(Alignment.CenterStart).padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clickable { onMenu() },
                contentAlignment = Alignment.Center,
            ) {
                HamburgerIcon()
            }
            if (!showWordmark) {
                Spacer(Modifier.width(6.dp))
                Text(title, fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Tokens.TextPrimary)
            }
        }
        if (showWordmark) {
            Box(
                Modifier
                    .align(Alignment.Center)
                    // Ramka tylko w jasnym motywie - w ciemnym na żądanie użytkownika usunięta (zbędna
                    // na ciemnym tle, tam wordmark sam się odcina bez obramowania).
                    .border(1.dp, if (LocalLightMode.current) Tokens.WhiteBorder else Color.Transparent, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                EggSpeedWordmark(fontSize = 18.sp, letterSpacing = 3.sp)
            }
        }
        if (trailing != null) {
            // Blisko prawej krawedzi ekranu (minimalny margines) - zeby wskaznik baterii mial
            // maksimum miejsca do rozrostu (100% = 3 cyfry) zanim dotknie wysrodkowanego logo.
            Box(Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)) { trailing() }
        }
    }
}

@Composable
private fun HamburgerIcon() {
    val lineColor = Tokens.TextPrimary
    Canvas(Modifier.size(22.dp)) {
        val w = size.width
        val stroke = 2.dp.toPx()
        listOf(0.2f, 0.5f, 0.8f).forEach { fy ->
            drawLine(
                color = lineColor,
                start = Offset(0f, size.height * fy),
                end = Offset(w, size.height * fy),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun DrawerContent(
    current: Screen,
    firmwareType: FirmwareType,
    onNavigate: (Screen) -> Unit,
    onClose: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxHeight()
            .width(320.dp)
            .background(Tokens.Card)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 16.dp),
    ) {
        EggSpeedWordmark(fontSize = 15.sp, letterSpacing = 3.sp)
        Text(tr("konfigurator Bafang", "Bafang configurator", de = "Bafang-Konfigurator", fr = "configurateur Bafang", es = "configurador Bafang", pt = "configurador Bafang", it = "configuratore Bafang", nl = "Bafang-configurator", sv = "Bafang-konfigurator", cs = "Konfigurátor Bafang", sk = "Konfigurátor Bafang", da = "Bafang-konfigurator", ru = "Конфигуратор Bafang"), fontFamily = Manrope, fontSize = 12.sp, color = Tokens.TextTertiary)
        Spacer(Modifier.height(18.dp))

        // "Menu" (About) na samej górze (ocena w Google Play, kontakt, itd. są w środku) - zwykły
        // wygląd jak reszta pozycji, tylko kolejność wyróżnia je jako pierwsze. CLOSE po prawej,
        // na tej samej wysokości - rozłącza i twardo zamyka apkę (nie tylko cofa do tła).
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) {
                DrawerItem(Screen.ABOUT.title(firmwareType), selected = current == Screen.ABOUT) { onNavigate(Screen.ABOUT) }
            }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .background(Tokens.Card, RoundedCornerShape(12.dp))
                    .border(1.dp, Tokens.Red, RoundedCornerShape(12.dp))
                    .clickable { onClose() }
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            ) {
                Text(
                    "CLOSE",
                    fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                    letterSpacing = 1.sp, color = Tokens.Red,
                )
            }
        }
        Spacer(Modifier.height(10.dp))

        // Ekrany OEM i bbs-fw są wzajemnie wykluczające się (różne protokoły konfiguracji, patrz
        // FirmwareType) - w menu widoczny jest tylko komplet pasujący do aktualnie wybranego firmware.
        val hidden = if (firmwareType == FirmwareType.BBS_FW) OEM_ONLY_SCREENS else BBS_FW_ONLY_SCREENS
        // "Widok wszystkiego" (DIAGNOSTICS) jest wspólny dla obu firmware, więc nie ma stałej
        // pozycji w enumie Screen - wyjęty z normalnej iteracji i wstawiany ręcznie zaraz po
        // zakładce poziomów wspomagania właściwej dla aktywnego firmware (ASSIST / BBSFW_ASSIST).
        val allInViewAnchor = if (firmwareType == FirmwareType.BBS_FW) Screen.BBSFW_ASSIST else Screen.ASSIST
        Screen.entries.filter { it !in hidden && it != Screen.ABOUT && it != Screen.DIAGNOSTICS }.forEach { s ->
            // Pozycja "Language" dostaje flagę bieżącego języka jako prefiks - jedyna pozycja menu
            // z ikoną, zeby była łatwo rozpoznawalna wśród tekstowych etykiet.
            val icon = if (s == Screen.LANGUAGE) LocalAppLanguage.current.flag else null
            DrawerItem(s.title(firmwareType), icon = icon, selected = s == current) { onNavigate(s) }
            if (s == allInViewAnchor) {
                DrawerItem(Screen.DIAGNOSTICS.title(firmwareType), selected = current == Screen.DIAGNOSTICS) { onNavigate(Screen.DIAGNOSTICS) }
            }
        }
    }
}

@Composable
private fun DrawerItem(label: String, icon: String? = null, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(if (selected) Tokens.Elevated else Tokens.Card, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 5.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Text(icon, fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
            }
            Text(
                label,
                fontFamily = Manrope,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 17.sp,
                maxLines = 1,
                color = if (selected) Tokens.Blue else Tokens.TextSecondary,
            )
        }
    }
}

