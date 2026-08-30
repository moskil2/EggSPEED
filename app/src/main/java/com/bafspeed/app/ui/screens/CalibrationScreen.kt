package com.bafspeed.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bafspeed.app.ConnectionStatus
import com.bafspeed.app.FirmwareType
import com.bafspeed.app.UiState
import com.bafspeed.app.protocol.Telemetry
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.ui.components.CollapsibleMicroLabel
import com.bafspeed.app.ui.components.ExpandableParamTile
import com.bafspeed.app.ui.components.MicroLabel
import com.bafspeed.app.ui.components.PlainSlider
import com.bafspeed.app.ui.components.PreviewBanner
import com.bafspeed.app.ui.components.StepBtn
import com.bafspeed.app.ui.components.TokenCard
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens

@Composable
fun CalibrationScreen(
    state: UiState,
    telemetry: Telemetry,
    onFactorChange: (Double) -> Unit,
    onVoltageOffsetChange: (Double) -> Unit,
    onSpeedFactorChange: (Double) -> Unit,
    onStartDisplay: () -> Unit,
    onStopDisplay: () -> Unit,
) {
    // Podgląd napięcia poniżej potrzebuje żywej telemetrii - bez tego, wejście tu bez
    // wcześniejszego pobytu na Kokpicie zostawiało telemetry na domyślnych 0,0 (patrz DashboardScreen).
    DisposableEffect(Unit) {
        onStartDisplay()
        onDispose { onStopDisplay() }
    }
    val connected = state.connection == ConnectionStatus.CONNECTED
    val factor = state.currentCalibrationFactor
    val declaredLimitA = state.basicOrDefault.currentLimit.toDouble()
    val voltageOffsetV = state.voltageCalibrationOffsetV
    val nominalVoltageV = state.nominalPackVoltage.toDouble()
    val speedFactor = state.speedCalibrationFactor
    val unitLabel = state.units.label
    // Wartość referencyjna na sztywno 30 (nie 30km/h przeliczane na mph) - sama liczba jest stała,
    // zmienia się tylko etykieta jednostki pod aktualne ustawienie (patrz SettingsScreen/SpeedUnit).
    val referenceSpeed = 30.0

    Column(
        Modifier
            .fillMaxSize()
            .background(Tokens.Bg)
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = 16.dp)),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        PreviewBanner(
            tr(
                "Dla sterowników z shunt modem odczyt prądu bywa zaniżony/zawyżony. Współczynnik X " +
                    "przemnaża WYŁĄCZNIE wartość wyświetlaną w aplikacji (prąd i moc) - nic nie jest " +
                    "zapisywane ani zmieniane w sterowniku. 1,00× = brak kalibracji.",
                "For controllers with a shunt mod, the current reading is sometimes under/overstated. The X " +
                    "factor multiplies ONLY the value shown in the app (current and power) - nothing is " +
                    "saved or changed in the controller. 1.00× = no calibration.",
            ),
            collapsible = true,
        )

        ExpandableParamTile(
            label = tr("Współczynnik kalibracji prądu", "Current calibration factor"),
            valueLabel = String.format("%.2f×", factor),
            description = tr(
                "Współczynnik mnoży surowy odczyt prądu z kontrolera przed wyświetleniem go w Kokpicie " +
                    "(prąd i moc). Przydaje się, gdy sterownik ma shunt mod i pokazuje zaniżoną/zawyżoną wartość - " +
                    "nic w tym sterowniku się nie zmienia.",
                "The factor multiplies the raw current reading from the controller before it's shown in the " +
                    "Cockpit (current and power). Useful when the controller has a shunt mod and reports an " +
                    "under/overstated value - nothing changes in the controller itself.",
            ),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                StepBtn("-", true) { onFactorChange((factor - 0.01).coerceIn(0.01, 3.0)) }
                Spacer(Modifier.width(10.dp))
                Box(Modifier.weight(1f)) {
                    PlainSlider(
                        value = factor.toFloat(),
                        range = 0.01f..3.00f,
                        accent = Tokens.Amber,
                        onValueChange = { onFactorChange(it.toDouble()) },
                    )
                }
                Spacer(Modifier.width(10.dp))
                StepBtn("+", true) { onFactorChange((factor + 0.01).coerceIn(0.01, 3.0)) }
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Tokens.Elevated, RoundedCornerShape(12.dp))
                    .clickable { onFactorChange(1.0) }
                    .padding(vertical = 10.dp),
            ) {
                Text(
                    tr("Resetuj do 1,00× (brak kalibracji)", "Reset to 1.00× (no calibration)"),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Tokens.TextPrimary,
                )
            }
        }

        CollapsibleMicroLabel(tr("Podgląd prądu", "Current preview")) {
            TokenCard(borderColor = Tokens.WhiteBorder, contentPaddingVertical = 8.dp) {
                InfoRow(tr("Limit prądu (zadeklarowany)", "Current limit (declared)"), String.format("%.1f A", declaredLimitA))
                HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                InfoRow(tr("Po kalibracji", "After calibration"), String.format("%.1f A", declaredLimitA * factor))
                HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                // Maksymalne napiecie (cellCount x 4,2V - "Gorny limit naladowania" w zakladce
                // Bateria), nie nominalne - moc szczytowa jest liczona przy pelnym naladowaniu.
                InfoRow(tr("Moc po kalibracji", "Power after calibration"), String.format("%.0f W", state.cellCount * 4.2 * declaredLimitA * factor))
            }
        }

        ExpandableParamTile(
            label = tr("Korekta napięcia", "Voltage correction"),
            valueLabel = "${if (voltageOffsetV > 0) "+" else ""}${String.format("%.1f", voltageOffsetV)} V",
            description = if (state.firmwareType == FirmwareType.BBS_FW) {
                tr(
                    "Napięcie w Kokpicie to realny pomiar z ADC sterownika (bbs-fw udostępnia go pod rejestrem " +
                        "0x24, w przeciwieństwie do fabrycznego firmware, gdzie ten rejestr jest martwy) - jeśli " +
                        "mimo to odbiega od pomiaru multimetrem (np. dryf ADC), skoryguj różnicę tutaj. Korekta " +
                        "doliczana jest do każdego kolejnego odczytu napięcia (i przez to też do mocy).",
                    "Voltage in the Cockpit is a real ADC measurement from the controller (bbs-fw exposes it via " +
                        "register 0x24, unlike the factory firmware where that register is dead) - if it still " +
                        "deviates from a multimeter reading (e.g. ADC drift), correct the difference here. The " +
                        "correction is added to every subsequent voltage reading (and therefore to power too).",
                )
            } else {
                tr(
                    "Napięcie w Kokpicie jest estymowane z % baterii (rejestr 0x24 martwy na fabrycznym " +
                        "firmware) - jeśli odbiega od realnego pomiaru (multimetr), skoryguj różnicę tutaj. Korekta " +
                        "doliczana jest do każdego kolejnego odczytu napięcia (i przez to też do mocy).",
                    "Voltage in the Cockpit is estimated from the battery % (register 0x24 is dead on the factory " +
                        "firmware) - if it deviates from a real measurement (multimeter), correct the difference here. " +
                        "The correction is added to every subsequent voltage reading (and therefore to power too).",
                )
            },
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                StepBtn("-", true) { onVoltageOffsetChange((voltageOffsetV - 0.1).coerceIn(-5.0, 5.0)) }
                Spacer(Modifier.width(10.dp))
                Box(Modifier.weight(1f)) {
                    PlainSlider(
                        value = voltageOffsetV.toFloat(),
                        range = -5.0f..5.0f,
                        accent = Tokens.Blue,
                        onValueChange = { onVoltageOffsetChange(it.toDouble()) },
                    )
                }
                Spacer(Modifier.width(10.dp))
                StepBtn("+", true) { onVoltageOffsetChange((voltageOffsetV + 0.1).coerceIn(-5.0, 5.0)) }
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Tokens.Elevated, RoundedCornerShape(12.dp))
                    .clickable { onVoltageOffsetChange(0.0) }
                    .padding(vertical = 10.dp),
            ) {
                Text(
                    tr("Resetuj do 0,0 V (brak korekty)", "Reset to 0.0 V (no correction)"),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Tokens.TextPrimary,
                )
            }
        }

        CollapsibleMicroLabel(tr("Podgląd napięcia", "Voltage preview")) {
            TokenCard(borderColor = Tokens.WhiteBorder, contentPaddingVertical = 8.dp) {
                // Offline: pokazujemy ostatnie znane napięcie sprzed rozłączenia (state.lastKnownVoltageV),
                // a jeśli go nigdy nie było (0,0 - apka jeszcze się nie łączyła) - estymatę z napięcia
                // nominalnego pakietu (np. 52V dla 14S), żeby podgląd korekty miał sens nawet offline.
                val readV = when {
                    connected -> telemetry.estimatedVoltageV
                    state.lastKnownVoltageV > 0.0 -> state.lastKnownVoltageV
                    else -> nominalVoltageV
                }
                InfoRow(tr("Napięcie odczytane", "Voltage read"), String.format("%.1f V", readV))
                HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                InfoRow(tr("Napięcie po korekcie", "Voltage after correction"), String.format("%.1f V", readV + voltageOffsetV))
            }
        }

        ExpandableParamTile(
            label = tr("Kalibracja prędkości", "Speed calibration"),
            valueLabel = String.format("%.2f×", speedFactor),
            description = tr(
                "Współczynnik mnoży surowy odczyt prędkości z kontrolera przed wyświetleniem jej w Kokpicie " +
                    "i na wykresach w Monitoringu. Przydaje się, gdy prędkość jest zaniżona/zawyżona (np. błędny " +
                    "obwód koła albo czujnik prędkości) - nic w tym sterowniku się nie zmienia.",
                "The factor multiplies the raw speed reading from the controller before it's shown in the " +
                    "Cockpit and on the Monitoring charts. Useful when the speed is under/overstated (e.g. wrong " +
                    "wheel circumference or speed sensor) - nothing changes in the controller itself.",
            ),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                StepBtn("-", true) { onSpeedFactorChange((speedFactor - 0.01).coerceIn(0.50, 2.0)) }
                Spacer(Modifier.width(10.dp))
                Box(Modifier.weight(1f)) {
                    PlainSlider(
                        value = speedFactor.toFloat(),
                        range = 0.50f..2.00f,
                        accent = Tokens.Amber,
                        onValueChange = { onSpeedFactorChange(it.toDouble()) },
                    )
                }
                Spacer(Modifier.width(10.dp))
                StepBtn("+", true) { onSpeedFactorChange((speedFactor + 0.01).coerceIn(0.50, 2.0)) }
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Tokens.Elevated, RoundedCornerShape(12.dp))
                    .clickable { onSpeedFactorChange(1.0) }
                    .padding(vertical = 10.dp),
            ) {
                Text(
                    tr("Resetuj do 1,00× (brak kalibracji)", "Reset to 1.00× (no calibration)"),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Tokens.TextPrimary,
                )
            }
        }

        CollapsibleMicroLabel(tr("Podgląd prędkości", "Speed preview")) {
            TokenCard(borderColor = Tokens.WhiteBorder, contentPaddingVertical = 8.dp) {
                InfoRow(tr("Prędkość przed kalibracją", "Speed before calibration"), String.format("%.1f %s", referenceSpeed, unitLabel))
                HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                InfoRow(tr("Prędkość po kalibracji", "Speed after calibration"), String.format("%.1f %s", referenceSpeed * speedFactor, unitLabel))
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    // Padding zaciesniety (bylo 9dp, potem 5dp) - kafelki PODGLAD maja byc kompaktowe, bez zmiany
    // rozmiaru czcionki (patrz TokenCard.contentPaddingVertical przy wywolaniach tych kafelkow).
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontFamily = Manrope, fontSize = 14.sp, color = Tokens.TextPrimary, modifier = Modifier.weight(1f))
        Text(value, fontFamily = Sora, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Tokens.TextPrimary)
    }
}
