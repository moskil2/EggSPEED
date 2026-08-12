package com.bafspeed.app

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
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
import com.bafspeed.app.ui.screens.GeneralScreen
import com.bafspeed.app.ui.screens.LanguageScreen
import com.bafspeed.app.ui.screens.ParametersScreen
import com.bafspeed.app.ui.screens.PedalScreen
import com.bafspeed.app.ui.screens.ProfilesScreen
import com.bafspeed.app.ui.screens.ThrottleScreen
import com.bafspeed.app.ui.screens.SettingsScreen
import com.bafspeed.app.ui.components.EggSpeedWordmark
import com.bafspeed.app.ui.components.WriteFlowDialogs
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
    SETTINGS,
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
private val BBS_FW_ONLY_SCREENS = setOf(Screen.BBSFW_INFO, Screen.BBSFW_SYSTEM, Screen.BBSFW_ASSIST)

/** Tytuł zakładki - zależny od języka (patrz [LocalAppLanguage]). */
@Composable
private fun Screen.title(): String = when (this) {
    Screen.CONNECT -> tr("Połączenie", "Connect")
    Screen.DASHBOARD -> tr("Kokpit", "Cockpit")
    Screen.BAFANG_TYPE -> tr("Bafang - Typ silnika", "Bafang Motor Type")
    Screen.MOTOR -> tr("Bafang - Ustawienia podstawowe", "Bafang Basic")
    Screen.PEDAL -> tr("Bafang - Pedałowanie (PAS)", "Bafang Pedal (PAS)")
    Screen.THROTTLE -> tr("Bafang - Manetka", "Bafang Throttle")
    Screen.ASSIST -> tr("Poziomy wspomagania", "Assist levels")
    Screen.BBSFW_INFO -> "BBS-FW Version"
    Screen.BBSFW_SYSTEM -> "System"
    Screen.BBSFW_ASSIST -> "Assist Levels"
    Screen.BATTERY -> tr("Bateria", "Battery")
    Screen.SETTINGS -> tr("Ustawienia", "Settings")
    Screen.CALIBRATION -> tr("Kalibracja", "Calibration")
    Screen.DIAGNOSTICS -> tr("Wszystko (podgląd)", "All in View")
    Screen.PROFILES -> tr("Profile", "Profiles")
    Screen.REGISTER_DIAGNOSTICS -> tr("Diagnostyka", "Diagnostics")
    Screen.LANGUAGE -> tr("Język", "Language")
    Screen.ABOUT -> tr("Menu", "Menu")
}

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.refreshProfiles()
        setContent { App(viewModel) }
    }
}

@Composable
private fun App(vm: AppViewModel) {
    val state by vm.state.collectAsState()
    val telemetry by vm.telemetry.collectAsState()
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
            }.onSuccess { Toast.makeText(context, tr(lang, "Zapisano profil", "Profile saved"), Toast.LENGTH_SHORT).show() }
                .onFailure { Toast.makeText(context, tr(lang, "Błąd zapisu: ${it.message}", "Save error: ${it.message}"), Toast.LENGTH_LONG).show() }
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
                Toast.makeText(context, tr(lang, "Nie udało się odczytać pliku", "Failed to read the file"), Toast.LENGTH_LONG).show()
            } else {
                vm.importIni(text)
                    .onSuccess { Toast.makeText(context, tr(lang, "Wczytano profil (podgląd)", "Profile loaded (preview)"), Toast.LENGTH_SHORT).show() }
                    .onFailure { Toast.makeText(context, tr(lang, "Błędny plik: ${it.message}", "Invalid file: ${it.message}"), Toast.LENGTH_LONG).show() }
            }
        }
    }

    fun go(s: Screen) {
        screen = s
        scope.launch { drawerState.close() }
    }

    // Gdy zmieni się firmware (zakładka Ustawienia), zakładka aktualnie otwarta może zniknąć z menu
    // (patrz OEM_ONLY_SCREENS/BBS_FW_ONLY_SCREENS) - w takim wypadku przenosimy na odpowiednik "Typ/Wersja".
    LaunchedEffect(state.firmwareType) {
        val hidden = if (state.firmwareType == FirmwareType.BBS_FW) OEM_ONLY_SCREENS else BBS_FW_ONLY_SCREENS
        if (screen in hidden) {
            screen = if (state.firmwareType == FirmwareType.BBS_FW) Screen.BBSFW_INFO else Screen.BAFANG_TYPE
        }
    }

    CompositionLocalProvider(LocalAppLanguage provides state.language) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = Tokens.Card) {
                DrawerContent(
                    current = screen,
                    firmwareType = state.firmwareType,
                    onNavigate = { go(it) },
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
                title = screen.title(),
                onMenu = { scope.launch { drawerState.open() } },
                trailing = when {
                    screen == Screen.DASHBOARD -> {
                        // Bez polaczenia pokazujemy ostatnio znany % (patrz UiState.lastKnownBatteryPct),
                        // zamiast zerowac wskaznik do 0.
                        val pct = if (state.connection == ConnectionStatus.CONNECTED) telemetry.batteryPct else state.lastKnownBatteryPct
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
                    )
                    Screen.ASSIST -> AssistLevelsScreen(
                        state = state,
                        onCurrentChange = vm::setAssistLevelCurrent,
                        onSpeedChange = vm::setAssistLevelSpeed,
                        onRead = vm::readAllConfig,
                        onWrite = vm::requestSaveToController,
                        readWriteEnabled = readWriteEnabled,
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
                    )
                    Screen.BATTERY -> BatteryScreen(
                        state = state,
                        onCellCountChange = vm::setCellCount,
                        onCapacityAhChange = vm::setCapacityAh,
                        onCapacityWhChange = vm::setCapacityWh,
                    )
                    Screen.CALIBRATION -> CalibrationScreen(
                        state = state,
                        telemetry = telemetry,
                        onFactorChange = vm::setCurrentCalibrationFactor,
                        onVoltageOffsetChange = vm::setVoltageCalibrationOffsetV,
                    )
                    Screen.REGISTER_DIAGNOSTICS -> DiagnosticsScreen(
                        state = state,
                        scanResults = scanResults,
                        scanProgress = scanProgress,
                        scanning = scanning,
                        fullScanHistory = fullScanHistory,
                        onStartScan = vm::startFullDiagnosticScan,
                    )
                    Screen.DIAGNOSTICS -> ParametersScreen(state = state, onRefresh = vm::readAllConfig)
                    Screen.PROFILES -> ProfilesScreen(
                        state = state,
                        onSaveNew = vm::saveProfileInternal,
                        onLoad = { name ->
                            vm.loadProfileInternal(name)
                                .onSuccess { go(if (state.firmwareType == FirmwareType.BBS_FW) Screen.BBSFW_SYSTEM else Screen.MOTOR) }
                                .onFailure { Toast.makeText(context, tr(state.language, "Błąd wczytywania profilu: ${it.message}", "Error loading profile: ${it.message}"), Toast.LENGTH_LONG).show() }
                        },
                        onDelete = vm::deleteProfileInternal,
                        onImportFile = { importLauncher.launch(arrayOf("*/*")) },
                        onExportFile = { exportLauncher.launch("eggspeed-profil.ini") },
                    )
                    Screen.SETTINGS -> SettingsScreen(
                        state = state,
                        onUnitsChange = vm::setUnits,
                        onOdoOffsetChange = vm::setOdoOffsetKm,
                        onToggleTestMode = vm::toggleTestMode,
                        onFirmwareTypeChange = vm::setFirmwareType,
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
            EggSpeedWordmark(fontSize = 18.sp, letterSpacing = 3.sp, modifier = Modifier.align(Alignment.Center))
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
    Canvas(Modifier.size(22.dp)) {
        val w = size.width
        val stroke = 2.dp.toPx()
        listOf(0.2f, 0.5f, 0.8f).forEach { fy ->
            drawLine(
                color = Tokens.TextPrimary,
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
) {
    Column(
        Modifier
            .fillMaxHeight()
            .width(288.dp)
            .background(Tokens.Card)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(16.dp),
    ) {
        EggSpeedWordmark(fontSize = 15.sp, letterSpacing = 3.sp)
        Text(tr("konfigurator Bafang", "Bafang configurator"), fontFamily = Manrope, fontSize = 12.sp, color = Tokens.TextTertiary)
        Spacer(Modifier.height(18.dp))

        // "Menu" (About) na samej górze (ocena w Google Play, kontakt, itd. są w środku) - zwykły
        // wygląd jak reszta pozycji, tylko kolejność wyróżnia je jako pierwsze.
        DrawerItem(Screen.ABOUT.title(), selected = current == Screen.ABOUT) { onNavigate(Screen.ABOUT) }
        Spacer(Modifier.height(10.dp))

        // Ekrany OEM i bbs-fw są wzajemnie wykluczające się (różne protokoły konfiguracji, patrz
        // FirmwareType) - w menu widoczny jest tylko komplet pasujący do aktualnie wybranego firmware.
        val hidden = if (firmwareType == FirmwareType.BBS_FW) OEM_ONLY_SCREENS else BBS_FW_ONLY_SCREENS
        Screen.entries.filter { it !in hidden && it != Screen.ABOUT }.forEach { s ->
            // Pozycja "Language" dostaje flagę bieżącego języka jako prefiks - jedyna pozycja menu
            // z ikoną, zeby była łatwo rozpoznawalna wśród tekstowych etykiet.
            val icon = if (s == Screen.LANGUAGE) LocalAppLanguage.current.flag else null
            DrawerItem(s.title(), icon = icon, selected = s == current) { onNavigate(s) }
        }
    }
}

@Composable
private fun DrawerItem(label: String, icon: String? = null, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .background(if (selected) Tokens.Elevated else Tokens.Card, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
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
                fontSize = 15.sp,
                color = if (selected) Tokens.Blue else Tokens.TextSecondary,
            )
        }
    }
}

