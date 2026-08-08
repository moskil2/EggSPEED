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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bafspeed.app.ConnectionStatus
import com.bafspeed.app.UiState
import com.bafspeed.app.protocol.Telemetry
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.ui.components.ExpandableParamTile
import com.bafspeed.app.ui.components.MicroLabel
import com.bafspeed.app.ui.components.PlainSlider
import com.bafspeed.app.ui.components.PreviewBanner
import com.bafspeed.app.ui.components.StepBtn
import com.bafspeed.app.ui.components.TokenCard
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens

private val WhiteBorder = Color(0x59FFFFFF)

@Composable
fun CalibrationScreen(
    state: UiState,
    telemetry: Telemetry,
    onFactorChange: (Double) -> Unit,
    onVoltageOffsetChange: (Double) -> Unit,
) {
    val connected = state.connection == ConnectionStatus.CONNECTED
    val factor = state.currentCalibrationFactor
    val declaredLimitA = state.basicOrDefault.currentLimit.toDouble()
    val voltageOffsetV = state.voltageCalibrationOffsetV
    val nominalVoltageV = state.nominalPackVoltage.toDouble()

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

        MicroLabel(tr("Podgląd", "Preview"))
        TokenCard(borderColor = WhiteBorder) {
            InfoRow(tr("Limit prądu (zadeklarowany)", "Current limit (declared)"), String.format("%.1f A", declaredLimitA))
            HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
            InfoRow(tr("Po kalibracji", "After calibration"), String.format("%.1f A", declaredLimitA * factor))
            HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
            InfoRow(tr("Moc po kalibracji", "Power after calibration"), String.format("%.0f W", nominalVoltageV * declaredLimitA * factor))
        }

        ExpandableParamTile(
            label = tr("Korekta napięcia", "Voltage correction"),
            valueLabel = "${if (voltageOffsetV > 0) "+" else ""}${String.format("%.1f", voltageOffsetV)} V",
            description = tr(
                "Napięcie w Kokpicie jest estymowane z % baterii (rejestr 0x24 martwy na fabrycznym " +
                    "firmware) - jeśli odbiega od realnego pomiaru (multimetr), skoryguj różnicę tutaj. Korekta " +
                    "doliczana jest do każdego kolejnego odczytu napięcia (i przez to też do mocy).",
                "Voltage in the Cockpit is estimated from the battery % (register 0x24 is dead on the factory " +
                    "firmware) - if it deviates from a real measurement (multimeter), correct the difference here. " +
                    "The correction is added to every subsequent voltage reading (and therefore to power too).",
            ),
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

        MicroLabel(tr("Podgląd", "Preview"))
        TokenCard(borderColor = WhiteBorder) {
            InfoRow(tr("Napięcie odczytane", "Voltage read"), if (connected) String.format("%.1f V", telemetry.estimatedVoltageV) else "-")
            HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
            InfoRow(tr("Napięcie po korekcie", "Voltage after correction"), if (connected) String.format("%.1f V", telemetry.voltageV) else "-")
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    // Padding zaciesniety (bylo 9dp) - kafelki PODGLAD maja byc kompaktowe.
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontFamily = Manrope, fontSize = 14.sp, color = Tokens.TextPrimary, modifier = Modifier.weight(1f))
        Text(value, fontFamily = Sora, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Tokens.TextPrimary)
    }
}
