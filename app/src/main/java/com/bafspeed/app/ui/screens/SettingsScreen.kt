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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bafspeed.app.SpeedUnit
import com.bafspeed.app.UiState
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.ui.components.ExpandableParamTile
import com.bafspeed.app.ui.components.MicroLabel
import com.bafspeed.app.ui.components.SegmentedControl
import com.bafspeed.app.ui.components.TokenCard
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens

private val WhiteBorder = Color(0x59FFFFFF)

@Composable
fun SettingsScreen(
    state: UiState,
    onUnitsChange: (SpeedUnit) -> Unit,
    onOdoOffsetChange: (Double) -> Unit,
    onToggleTestMode: () -> Unit,
) {
    val unit = state.units

    Column(
        Modifier
            .fillMaxSize()
            .background(Tokens.Bg)
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = 16.dp)),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        MicroLabel(tr("Aplikacja", "Application"))
        TokenCard(borderColor = WhiteBorder) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(tr("Jednostki", "Units"), fontFamily = Manrope, fontSize = 14.sp, color = Tokens.TextPrimary, modifier = Modifier.weight(1f))
                SegmentedControl(
                    options = listOf("km/h", "mph"),
                    selectedIndex = if (state.units == SpeedUnit.MPH) 1 else 0,
                    onSelect = { onUnitsChange(if (it == 1) SpeedUnit.MPH else SpeedUnit.KMH) },
                    modifier = Modifier.width(180.dp),
                )
            }
            HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
            InfoRow(tr("Typ połączenia", "Connection type"), state.deviceLabel?.let { "USB · $it" } ?: tr("USB (kabel)", "USB (cable)"))
            HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(tr("Tryb testowy", "Test mode"), fontFamily = Manrope, fontSize = 14.sp, color = Tokens.TextPrimary)
                    Text(
                        tr("Wymusza skrajne wartości na Kokpicie - test układu ekranu", "Forces extreme values on the Cockpit - screen layout test"),
                        fontFamily = Manrope, fontSize = 11.sp, color = Tokens.TextSecondary,
                    )
                }
                Box(
                    modifier = Modifier
                        .background(if (state.testMode) Tokens.Blue else Tokens.Elevated, RoundedCornerShape(10.dp))
                        .clickable { onToggleTestMode() }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "TEST", fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                        color = if (state.testMode) Tokens.OnAccent else Tokens.TextPrimary,
                    )
                }
            }
        }

        ExpandableParamTile(
            label = tr("Przebieg (ODO)", "Odometer (ODO)"),
            valueLabel = "",
            description = tr(
                "Jeśli migrujesz z innego wyświetlacza i chcesz zachować dotychczasowy przebieg, " +
                    "wpisz go tutaj jako punkt startowy. EggSPEED doda do niego dystans naliczany na bieżąco w " +
                    "Kokpicie (obecnie ${String.format("%.1f", unit.fromKmh(state.tripKm))} ${unit.distanceLabel} od uruchomienia tej sesji Kokpitu). " +
                    "Wartość zapisuje się trwale na telefonie.",
                "If you're migrating from another display and want to keep your existing mileage, " +
                    "enter it here as a starting point. EggSPEED will add to it the distance tracked live in the " +
                    "Cockpit (currently ${String.format("%.1f", unit.fromKmh(state.tripKm))} ${unit.distanceLabel} since this Cockpit session started). " +
                    "The value is saved permanently on your phone.",
            ),
        ) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${String.format("%.1f", unit.fromKmh(state.totalOdoKm))} ${unit.distanceLabel}",
                    fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Tokens.TextPrimary,
                )
                Spacer(Modifier.height(10.dp))
                OdoField(
                    value = unit.fromKmh(state.odoOffsetKm),
                    unitLabel = unit.distanceLabel,
                    onValueChange = { onOdoOffsetChange(unit.toKmh(it)) },
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            tr("Zapis parametrów wymaga jawnego potwierdzenia i weryfikacji po zapisie", "Saving parameters requires explicit confirmation and verification after writing"),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontFamily = Manrope, fontSize = 11.sp, color = Tokens.TextSecondary,
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontFamily = Manrope, fontSize = 14.sp, color = Tokens.TextPrimary, modifier = Modifier.weight(1f))
        Text(value, fontFamily = Sora, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Tokens.TextPrimary)
    }
}

@Composable
private fun OdoField(value: Double, unitLabel: String, onValueChange: (Double) -> Unit) {
    // Bufor lokalny odlaczony od zewnetrznej wartosci podczas edycji - ten sam wzorzec
    // co CapacityField w BatteryScreen.kt (bez tego kasowanie/wpisywanie niepelnej liczby
    // jest natychmiast nadpisywane sformatowana wartoscia z ViewModel).
    var isFocused by remember { mutableStateOf(false) }
    var localText by remember { mutableStateOf(String.format("%.1f", value)) }
    LaunchedEffect(value, isFocused) {
        if (!isFocused) localText = String.format("%.1f", value)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Tokens.Elevated, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = localText,
            onValueChange = { text ->
                localText = text
                text.replace(',', '.').toDoubleOrNull()?.let(onValueChange)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = TextStyle(color = Tokens.TextPrimary, fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 16.sp),
            cursorBrush = SolidColor(Tokens.Blue),
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { isFocused = it.isFocused },
        )
        Text(unitLabel, fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextSecondary)
    }
}
