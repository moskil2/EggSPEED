package com.bafspeed.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bafspeed.app.FirmwareType
import com.bafspeed.app.UiState
import com.bafspeed.app.i18n.AppLanguage
import com.bafspeed.app.i18n.LocalAppLanguage
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.protocol.BbsFwController
import com.bafspeed.app.protocol.WHEEL_SIZE_LABELS
import com.bafspeed.app.protocol.designatedAssistLabel
import com.bafspeed.app.protocol.speedLimitLabel
import com.bafspeed.app.ui.components.MicroLabel
import com.bafspeed.app.ui.components.PreviewBanner
import com.bafspeed.app.ui.components.TokenCard
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens

private val WhiteBorder = Color(0x59FFFFFF)

/**
 * Zakladka "All In" - zwiezly, wylacznie do odczytu spis wszystkich aktualnie
 * ustawionych parametrow z Bafang Basic / Bafang Pedal (PAS) / Bafang Throttle
 * i Poziomow wspomagania, z przyciskiem kopiowania calosci do schowka. Zero
 * edycji - edycja dzieje sie na wlasciwych zakladkach.
 */
@Composable
fun ParametersScreen(state: UiState, onRefresh: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val lang = LocalAppLanguage.current
    val isBbsFw = state.firmwareType == FirmwareType.BBS_FW
    val hasData = if (isBbsFw) state.bbsFwVersion != null else state.general != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Tokens.Bg)
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = 16.dp)),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (!hasData) {
            PreviewBanner(tr(
                "Brak połączenia - poniżej pokazane są wartości domyślne aplikacji, niekoniecznie zgodne z tym, co faktycznie ma zapisane Twój sterownik. Połącz się na ekranie Połączenie, żeby zobaczyć realne dane.",
                "No connection - the values below are the app's defaults, not necessarily what your controller actually has saved. Connect on the Connect screen to see the real data.",
            ))
        } else {
            PreviewBanner(tr(
                "Wyłącznie podgląd do odczytu - to spis aktualnie ustawionych parametrów. Edycja odbywa się na właściwych zakładkach.",
                "Read-only preview only - this is a list of currently set parameters. Editing happens in the actual tabs.",
            ))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Tokens.Card, RoundedCornerShape(15.dp))
                .border(1.dp, WhiteBorder, RoundedCornerShape(15.dp))
                .clickable {
                    val text = if (isBbsFw) buildBbsFwDiagnosticsText(state, lang) else buildDiagnosticsText(state, lang)
                    clipboard.setText(AnnotatedString(text))
                    Toast.makeText(context, tr(lang, "Skopiowano do schowka", "Copied to clipboard"), Toast.LENGTH_SHORT).show()
                }
                .padding(vertical = 15.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(tr("Kopiuj wszystko", "Copy all"), fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Tokens.Blue)
        }

        if (isBbsFw) {
            BbsFwParametersContent(state)
        } else {
            state.general?.let { gen ->
                Column {
                    MicroLabel("Bafang Motor Type")
                    Spacer(Modifier.height(6.dp))
                    TokenCard(borderColor = WhiteBorder) {
                        ParamRow(tr("Producent", "Manufacturer"), gen.manufacturer)
                        ParamRow("Model", gen.model)
                        ParamRow(tr("Wersja sprzętu", "Hardware version"), gen.hardwareVersion)
                        ParamRow("Firmware", gen.firmwareVersion)
                        ParamRow(tr("Napięcie nominalne", "Nominal voltage"), "${gen.nominalVoltage} V")
                        ParamRow(tr("Prąd maksymalny", "Max current"), "${gen.maxCurrentA} A", last = true)
                    }
                }
            }

            val bas = state.basicOrDefault
            Column {
                MicroLabel("Bafang Basic")
                Spacer(Modifier.height(6.dp))
                TokenCard(borderColor = WhiteBorder) {
                    ParamRow(tr("Ochrona baterii (LBP)", "Battery protection (LBP)"), "${bas.lowBatteryProtection} V")
                    ParamRow(tr("Limit prądu (LC)", "Current limit (LC)"), "${bas.currentLimit} A")
                    ParamRow(tr("Koło", "Wheel"), WHEEL_SIZE_LABELS.getOrElse(bas.wheelDiameterCode) { "${tr(lang, "kod", "code")} ${bas.wheelDiameterCode}" })
                    ParamRow(tr("Czujnik prędkości (SMM)", "Speed meter (SMM)"), bas.speedMeterModel.toString())
                    ParamRow(tr("Sygnały czujnika (SMS)", "Meter signals (SMS)"), bas.speedMeterSignals.toString(), last = true)
                }
                Spacer(Modifier.height(6.dp))
                MicroLabel(tr("Poziomy wspomagania", "Assist levels"))
                Spacer(Modifier.height(6.dp))
                TokenCard(borderColor = WhiteBorder) {
                    bas.assistCurrentPct.forEachIndexed { i, alc ->
                        ParamRow(
                            tr("Poziom $i", "Level $i"),
                            "$alc% / ${bas.assistSpeedPct.getOrElse(i) { 0 }}%",
                            last = i == 9,
                        )
                    }
                }
            }

            val pas = state.pasOrDefault
            Column {
                MicroLabel("Bafang Pedal (PAS)")
                Spacer(Modifier.height(6.dp))
                TokenCard(borderColor = WhiteBorder) {
                    ParamRow(tr("Typ czujnika (PT)", "Sensor type (PT)"), pas.pedalType.toString())
                    ParamRow("Designated Assist (DA)", designatedAssistLabel(pas.designatedAssist))
                    ParamRow(tr("Limit prędkości (SL)", "Speed limit (SL)"), speedLimitLabel(pas.speedLimit))
                    ParamRow(tr("Prąd startowy (SC)", "Start current (SC)"), "${pas.startCurrentPct}%")
                    ParamRow("Slow-start (SSM)", pas.slowStartMode.toString())
                    ParamRow("Start degree (SDN)", pas.startDegree.toString())
                    ParamRow(tr("Tryb pracy (WM)", "Work mode (WM)"), if (pas.workMode == 0) tr("nieokreślony", "undetermined") else pas.workMode.toString())
                    ParamRow("Time of stop (TS)", "${pas.timeOfStop} ×10 ms")
                    ParamRow("Current decay (CD)", pas.currentDecay.toString())
                    ParamRow("Stop decay (SD)", "${pas.stopDecay} ×10 ms")
                    ParamRow("Keep current (KC)", "${pas.keepCurrentPct}%", last = true)
                }
            }

            val thr = state.thrOrDefault
            Column {
                MicroLabel("Bafang Throttle")
                Spacer(Modifier.height(6.dp))
                TokenCard(borderColor = WhiteBorder) {
                    ParamRow(tr("Napięcie start (SV)", "Start voltage (SV)"), "${thr.startVoltage / 10.0} V")
                    ParamRow(tr("Napięcie końcowe (EV)", "End voltage (EV)"), "${thr.endVoltage / 10.0} V")
                    ParamRow(tr("Tryb", "Mode"), tr(if (thr.mode == 0) "prędkość" else "prąd", if (thr.mode == 0) "speed" else "current"))
                    ParamRow("Designated Assist (DA)", designatedAssistLabel(thr.designatedAssist))
                    ParamRow(tr("Limit prędkości (SL)", "Speed limit (SL)"), speedLimitLabel(thr.speedLimit))
                    ParamRow(tr("Prąd startowy (SC)", "Start current (SC)"), "${thr.startCurrentPct}%", last = true)
                }
            }
        }

        if (hasData) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Tokens.Card, RoundedCornerShape(15.dp))
                    .border(1.dp, WhiteBorder, RoundedCornerShape(15.dp))
                    .clickable { onRefresh() }
                    .padding(vertical = 15.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(tr("Odczytaj ponownie", "Read again"), fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Tokens.TextPrimary)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

/**
 * Odpowiednik OEM sekcji powyżej dla bbs-fw - te same kategorie i kolejność co ekran "System"
 * ([BbsFwSystemScreen]: Global/Throttle/Pedal Assist/Features/Speed Sensor/Shift Sensor/Miscellaneous),
 * plus wersja firmware/config i Poziomy wspomagania (2 profile × 10 poziomów).
 */
@Composable
private fun BbsFwParametersContent(state: UiState) {
    val cfg = state.bbsFwConfigOrDefault
    state.bbsFwVersion?.let { v ->
        Column {
            MicroLabel("bbs-fw")
            Spacer(Modifier.height(6.dp))
            TokenCard(borderColor = WhiteBorder) {
                ParamRow(tr("Wersja firmware", "Firmware version"), v.versionLabel)
                ParamRow(tr("Wersja formatu konfiguracji", "Config format version"), v.configVersion.toString())
                ParamRow(tr("Typ sterownika", "Controller type"), "${BbsFwController.name(v.ctrlType)} (${v.ctrlType})", last = true)
            }
        }
    }

    Column {
        MicroLabel("Global")
        Spacer(Modifier.height(6.dp))
        TokenCard(borderColor = WhiteBorder) {
            ParamRow("Max Current", "${cfg.maxCurrentAmps} A")
            ParamRow("Current Ramp", "${cfg.currentRampAmpsS} A/s")
            ParamRow("Max Battery Voltage", "${cfg.maxBatteryX100v / 100.0} V")
            ParamRow("Low Voltage Cutoff", "${cfg.lowCutOffV} V")
            ParamRow("Max Speed", "${cfg.maxSpeedKph} km/h", last = true)
        }
    }

    Column {
        MicroLabel("Throttle")
        Spacer(Modifier.height(6.dp))
        TokenCard(borderColor = WhiteBorder) {
            ParamRow("Start Voltage", "${cfg.throttleStartVoltageMv} mV")
            ParamRow("End Voltage", "${cfg.throttleEndVoltageMv} mV")
            ParamRow("Start Current", "${cfg.throttleStartPercent}%")
            ParamRow("Global Speed Limit Options", THROTTLE_SPD_LIM_OPT_LABELS.getOrElse(cfg.throttleGlobalSpdLimOpt) { "?" })
            ParamRow("Global Speed Limit", "${cfg.throttleGlobalSpdLimPercent}%", last = true)
        }
    }

    Column {
        MicroLabel("Pedal Assist")
        Spacer(Modifier.height(6.dp))
        TokenCard(borderColor = WhiteBorder) {
            ParamRow("Start Delay", "${cfg.pasStartDelayPulses * 15}°")
            ParamRow("Stop Delay", "${cfg.pasStopDelayX100s * 10} ms")
            ParamRow("Keep Current", "${cfg.pasKeepCurrentPercent}%")
            ParamRow("Keep Current Cadence", "${cfg.pasKeepCurrentCadenceRpm} rpm", last = true)
        }
    }

    Column {
        MicroLabel("Features")
        Spacer(Modifier.height(6.dp))
        TokenCard(borderColor = WhiteBorder) {
            ParamRow("Speed Sensor", tr(if (cfg.useSpeedSensor) "Włączony" else "Wyłączony", if (cfg.useSpeedSensor) "On" else "Off"))
            ParamRow("Shift Sensor", tr(if (cfg.useShiftSensor) "Włączony" else "Wyłączony", if (cfg.useShiftSensor) "On" else "Off"))
            ParamRow("Walk Mode", tr(if (cfg.usePushWalk) "Włączony" else "Wyłączony", if (cfg.usePushWalk) "On" else "Off"))
            ParamRow("Temperature Sensor", TEMP_SENSOR_LABELS.getOrElse(cfg.temperatureSensorMode) { "?" })
            ParamRow("Lights Mode", LIGHTS_MODE_LABELS.getOrElse(cfg.lightsMode) { "?" }, last = true)
        }
    }

    Column {
        MicroLabel("Speed Sensor")
        Spacer(Modifier.height(6.dp))
        TokenCard(borderColor = WhiteBorder) {
            ParamRow("Wheel Size", "${cfg.wheelSizeInchX10 / 10.0}\"")
            ParamRow("Signals (per rotation)", cfg.speedSensorSignals.toString(), last = true)
        }
    }

    Column {
        MicroLabel("Shift Sensor")
        Spacer(Modifier.height(6.dp))
        TokenCard(borderColor = WhiteBorder) {
            ParamRow("Shift Interrupt Duration", "${cfg.shiftInterruptDurationMs} ms")
            ParamRow("Shift Current Threshold", "${cfg.shiftInterruptCurrentThresholdPercent}%", last = true)
        }
    }

    Column {
        MicroLabel("Miscellaneous")
        Spacer(Modifier.height(6.dp))
        TokenCard(borderColor = WhiteBorder) {
            ParamRow("Walk Mode Data Display", WALK_MODE_DATA_LABELS.getOrElse(cfg.walkModeDataDisplay) { "?" })
            ParamRow("Assist Mode Select", cfg.assistModeSelect.toString())
            ParamRow("Assist Startup Level", cfg.assistStartupLevel.toString())
            ParamRow(tr("Jednostki imperialne (mph)", "Freedom units (mph)"), tr(if (cfg.useFreedomUnits) "Włączone" else "Wyłączone", if (cfg.useFreedomUnits) "On" else "Off"), last = true)
        }
    }

    listOf(0, 1).forEach { profile ->
        Column {
            MicroLabel(if (profile == 0) tr("Poziomy wspomagania - Profil 1 (Standard)", "Assist Levels - Profile 1 (Standard)") else tr("Poziomy wspomagania - Profil 2 (Sport)", "Assist Levels - Profile 2 (Sport)"))
            Spacer(Modifier.height(6.dp))
            TokenCard(borderColor = WhiteBorder) {
                for (level in 0..9) {
                    val al = cfg.assistLevel(profile, level)
                    ParamRow(
                        tr("Poziom $level", "Level $level"),
                        "${ASSIST_TYPE_LABELS.getOrElse(al.baseType()) { "?" }} · ${al.targetCurrentPercent}% / ${al.maxSpeedPercent}%",
                        last = level == 9,
                    )
                }
            }
        }
    }
}

private val THROTTLE_SPD_LIM_OPT_LABELS = listOf("Disabled", "Enabled", "Standard Levels")
private val TEMP_SENSOR_LABELS = listOf("Disabled", "Controller", "Motor", "All")
private val LIGHTS_MODE_LABELS = listOf("Default", "Disabled", "Always On", "Brake Light")
private val WALK_MODE_DATA_LABELS = listOf("Speed", "Temperature", "Requested Power", "Battery Level")
private val ASSIST_TYPE_LABELS = listOf("Disabled", "PAS", "Throttle", "Cruise")

private fun buildDiagnosticsText(state: UiState, lang: AppLanguage): String = buildString {
    appendLine(tr(lang, "EggSPEED - Wszystko (podgląd) - spis parametrow", "EggSPEED - All in View - parameter list"))
    appendLine()
    state.general?.let { gen ->
        appendLine("[Bafang Motor Type]")
        appendLine("${tr(lang, "Producent", "Manufacturer")}: ${gen.manufacturer}")
        appendLine("Model: ${gen.model}")
        appendLine("${tr(lang, "Wersja sprzetu", "Hardware version")}: ${gen.hardwareVersion}")
        appendLine("Firmware: ${gen.firmwareVersion}")
        appendLine("${tr(lang, "Napiecie nominalne", "Nominal voltage")}: ${gen.nominalVoltage} V")
        appendLine("${tr(lang, "Prad maksymalny", "Max current")}: ${gen.maxCurrentA} A")
        appendLine()
    }
    val bas = state.basicOrDefault
    appendLine("[Bafang Basic]")
    appendLine("${tr(lang, "Ochrona baterii (LBP)", "Battery protection (LBP)")}: ${bas.lowBatteryProtection} V")
    appendLine("${tr(lang, "Limit pradu (LC)", "Current limit (LC)")}: ${bas.currentLimit} A")
    appendLine("${tr(lang, "Kolo", "Wheel")}: ${WHEEL_SIZE_LABELS.getOrElse(bas.wheelDiameterCode) { "${tr(lang, "kod", "code")} ${bas.wheelDiameterCode}" }}")
    appendLine("${tr(lang, "Czujnik predkosci (SMM)", "Speed meter (SMM)")}: ${bas.speedMeterModel}")
    appendLine("${tr(lang, "Sygnaly czujnika (SMS)", "Meter signals (SMS)")}: ${bas.speedMeterSignals}")
    appendLine()
    appendLine("[${tr(lang, "Poziomy wspomagania", "Assist levels")}]")
    bas.assistCurrentPct.forEachIndexed { i, alc ->
        appendLine("${tr(lang, "Poziom", "Level")} $i: ${alc}% / ${bas.assistSpeedPct.getOrElse(i) { 0 }}%")
    }
    appendLine()
    val pas = state.pasOrDefault
    appendLine("[Bafang Pedal (PAS)]")
    appendLine("${tr(lang, "Typ czujnika (PT)", "Sensor type (PT)")}: ${pas.pedalType}")
    appendLine("Designated Assist (DA): ${designatedAssistLabel(pas.designatedAssist)}")
    appendLine("${tr(lang, "Limit predkosci (SL)", "Speed limit (SL)")}: ${speedLimitLabel(pas.speedLimit)}")
    appendLine("${tr(lang, "Prad startowy (SC)", "Start current (SC)")}: ${pas.startCurrentPct}%")
    appendLine("Slow-start (SSM): ${pas.slowStartMode}")
    appendLine("Start degree (SDN): ${pas.startDegree}")
    appendLine("${tr(lang, "Tryb pracy (WM)", "Work mode (WM)")}: ${if (pas.workMode == 0) tr(lang, "nieokreslony", "undetermined") else pas.workMode.toString()}")
    appendLine("Time of stop (TS): ${pas.timeOfStop} x10 ms")
    appendLine("Current decay (CD): ${pas.currentDecay}")
    appendLine("Stop decay (SD): ${pas.stopDecay} x10 ms")
    appendLine("Keep current (KC): ${pas.keepCurrentPct}%")
    appendLine()
    val thr = state.thrOrDefault
    appendLine("[Bafang Throttle]")
    appendLine("${tr(lang, "Napiecie start (SV)", "Start voltage (SV)")}: ${thr.startVoltage / 10.0} V")
    appendLine("${tr(lang, "Napiecie koncowe (EV)", "End voltage (EV)")}: ${thr.endVoltage / 10.0} V")
    appendLine("${tr(lang, "Tryb", "Mode")}: ${tr(lang, if (thr.mode == 0) "predkosc" else "prad", if (thr.mode == 0) "speed" else "current")}")
    appendLine("Designated Assist (DA): ${designatedAssistLabel(thr.designatedAssist)}")
    appendLine("${tr(lang, "Limit predkosci (SL)", "Speed limit (SL)")}: ${speedLimitLabel(thr.speedLimit)}")
    appendLine("${tr(lang, "Prad startowy (SC)", "Start current (SC)")}: ${thr.startCurrentPct}%")
}

private fun buildBbsFwDiagnosticsText(state: UiState, lang: AppLanguage): String = buildString {
    appendLine(tr(lang, "EggSPEED - Wszystko (podglad) - spis parametrow bbs-fw", "EggSPEED - All in View - bbs-fw parameter list"))
    appendLine()
    state.bbsFwVersion?.let { v ->
        appendLine("[bbs-fw]")
        appendLine("${tr(lang, "Wersja firmware", "Firmware version")}: ${v.versionLabel}")
        appendLine("${tr(lang, "Wersja formatu konfiguracji", "Config format version")}: ${v.configVersion}")
        appendLine("${tr(lang, "Typ sterownika", "Controller type")}: ${BbsFwController.name(v.ctrlType)} (${v.ctrlType})")
        appendLine()
    }
    val cfg = state.bbsFwConfigOrDefault
    appendLine("[Global]")
    appendLine("Max Current: ${cfg.maxCurrentAmps} A")
    appendLine("Current Ramp: ${cfg.currentRampAmpsS} A/s")
    appendLine("Max Battery Voltage: ${cfg.maxBatteryX100v / 100.0} V")
    appendLine("Low Voltage Cutoff: ${cfg.lowCutOffV} V")
    appendLine("Max Speed: ${cfg.maxSpeedKph} km/h")
    appendLine()
    appendLine("[Throttle]")
    appendLine("Start Voltage: ${cfg.throttleStartVoltageMv} mV")
    appendLine("End Voltage: ${cfg.throttleEndVoltageMv} mV")
    appendLine("Start Current: ${cfg.throttleStartPercent}%")
    appendLine("Global Speed Limit Options: ${THROTTLE_SPD_LIM_OPT_LABELS.getOrElse(cfg.throttleGlobalSpdLimOpt) { "?" }}")
    appendLine("Global Speed Limit: ${cfg.throttleGlobalSpdLimPercent}%")
    appendLine()
    appendLine("[Pedal Assist]")
    appendLine("Start Delay: ${cfg.pasStartDelayPulses * 15} deg")
    appendLine("Stop Delay: ${cfg.pasStopDelayX100s * 10} ms")
    appendLine("Keep Current: ${cfg.pasKeepCurrentPercent}%")
    appendLine("Keep Current Cadence: ${cfg.pasKeepCurrentCadenceRpm} rpm")
    appendLine()
    appendLine("[Features]")
    appendLine("Speed Sensor: ${if (cfg.useSpeedSensor) "On" else "Off"}")
    appendLine("Shift Sensor: ${if (cfg.useShiftSensor) "On" else "Off"}")
    appendLine("Walk Mode: ${if (cfg.usePushWalk) "On" else "Off"}")
    appendLine("Temperature Sensor: ${TEMP_SENSOR_LABELS.getOrElse(cfg.temperatureSensorMode) { "?" }}")
    appendLine("Lights Mode: ${LIGHTS_MODE_LABELS.getOrElse(cfg.lightsMode) { "?" }}")
    appendLine()
    appendLine("[Speed Sensor]")
    appendLine("Wheel Size: ${cfg.wheelSizeInchX10 / 10.0}\"")
    appendLine("Signals (per rotation): ${cfg.speedSensorSignals}")
    appendLine()
    appendLine("[Shift Sensor]")
    appendLine("Shift Interrupt Duration: ${cfg.shiftInterruptDurationMs} ms")
    appendLine("Shift Current Threshold: ${cfg.shiftInterruptCurrentThresholdPercent}%")
    appendLine()
    appendLine("[Miscellaneous]")
    appendLine("Walk Mode Data Display: ${WALK_MODE_DATA_LABELS.getOrElse(cfg.walkModeDataDisplay) { "?" }}")
    appendLine("Assist Mode Select: ${cfg.assistModeSelect}")
    appendLine("Assist Startup Level: ${cfg.assistStartupLevel}")
    appendLine("${tr(lang, "Jednostki imperialne (mph)", "Freedom units (mph)")}: ${if (cfg.useFreedomUnits) "On" else "Off"}")
    appendLine()
    listOf(0, 1).forEach { profile ->
        appendLine("[${if (profile == 0) tr(lang, "Poziomy wspomagania - Profil 1 (Standard)", "Assist Levels - Profile 1 (Standard)") else tr(lang, "Poziomy wspomagania - Profil 2 (Sport)", "Assist Levels - Profile 2 (Sport)")}]")
        for (level in 0..9) {
            val al = cfg.assistLevel(profile, level)
            appendLine(
                "${tr(lang, "Poziom", "Level")} $level: ${ASSIST_TYPE_LABELS.getOrElse(al.baseType()) { "?" }}, " +
                    "Target Current ${al.targetCurrentPercent}%, Max Speed ${al.maxSpeedPercent}%, " +
                    "Max Throttle Current ${al.maxThrottleCurrentPercent}%, Max Cadence ${al.maxCadencePercent}%, " +
                    "Torque Factor ${al.torqueAmplificationFactorX10 / 10.0}x",
            )
        }
        appendLine()
    }
}

@Composable
private fun ParamRow(label: String, value: String, last: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextPrimary, modifier = Modifier.weight(1f))
        Text(value, fontFamily = Sora, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Tokens.TextPrimary)
    }
    if (!last) HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
}
