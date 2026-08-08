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
import com.bafspeed.app.UiState
import com.bafspeed.app.i18n.AppLanguage
import com.bafspeed.app.i18n.LocalAppLanguage
import com.bafspeed.app.i18n.tr
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Tokens.Bg)
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = 16.dp)),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (state.general == null) {
            PreviewBanner(tr(
                "Brak połączenia - poniżej pokazane są wartości domyślne aplikacji, niekoniecznie zgodne z tym, co faktycznie ma zapisane Twój sterownik. Połącz się na ekranie Połączenie, żeby zobaczyć realne dane.",
                "No connection - the values below are the app's defaults, not necessarily what your controller actually has saved. Connect on the Connect screen to see the real data.",
            ))
        } else {
            PreviewBanner(tr(
                "Wyłącznie podgląd do odczytu - to spis aktualnie ustawionych parametrów. Edycja odbywa się na właściwych zakładkach (Bafang Basic/Pedal (PAS)/Throttle, Poziomy wspomagania).",
                "Read-only preview only - this is a list of currently set parameters. Editing happens in the actual tabs (Bafang Basic/Pedal (PAS)/Throttle, Assist levels).",
            ))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Tokens.Card, RoundedCornerShape(15.dp))
                .border(1.dp, WhiteBorder, RoundedCornerShape(15.dp))
                .clickable {
                    clipboard.setText(AnnotatedString(buildDiagnosticsText(state, lang)))
                    Toast.makeText(context, tr(lang, "Skopiowano do schowka", "Copied to clipboard"), Toast.LENGTH_SHORT).show()
                }
                .padding(vertical = 15.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(tr("Kopiuj wszystko", "Copy all"), fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Tokens.Blue)
        }

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

        if (state.general != null) {
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
