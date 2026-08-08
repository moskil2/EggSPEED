package com.bafspeed.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bafspeed.app.UiState
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.ui.components.ExpandableParamTile
import com.bafspeed.app.ui.components.MicroLabel
import com.bafspeed.app.ui.components.PreviewBanner
import com.bafspeed.app.ui.components.StepBtn
import com.bafspeed.app.ui.components.TokenCard
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens

private val WhiteBorder = Color(0x59FFFFFF)

@Composable
fun BatteryScreen(
    state: UiState,
    onCellCountChange: (Int) -> Unit,
    onCapacityAhChange: (Double) -> Unit,
    onCapacityWhChange: (Double) -> Unit,
) {
    val basic = state.basicOrDefault

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
                "Odcięcie niskiego napięcia wysyłane jest do sterownika dopiero po Twoim potwierdzeniu. Liczba cel i " +
                    "pojemność to dane pomocnicze aplikacji - nie są zapisywane w sterowniku, ale są potrzebne, żeby " +
                    "EggSPEED mógł poprawnie wyliczyć zasięg roweru na ekranie Kokpit.",
                "Low voltage cutoff is sent to the controller only after your confirmation. Cell count and " +
                    "capacity are app-side helper data - they aren't saved in the controller, but are needed so " +
                    "EggSPEED can correctly estimate the bike's range on the Cockpit screen.",
            ),
            borderWidth = 2.dp,
        )

        // Napięcie nominalne z GEN (tylko odczyt)
        TokenCard(borderColor = WhiteBorder) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(tr("Nominalne napięcie", "Nominal voltage"), fontFamily = Manrope, fontSize = 14.sp, color = Tokens.TextPrimary, modifier = Modifier.weight(1f))
                Text(
                    state.general?.let { "${it.nominalVoltage} V" } ?: "-",
                    fontFamily = Sora, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Tokens.TextPrimary,
                )
            }
        }

        MicroLabel(tr("Twoja bateria", "Your battery"))

        // Tylko podgląd - edycja przeniesiona do zakładki Bafang Basic (jedno źródło prawdy,
        // żeby ta sama wartość nie była edytowalna w dwóch miejscach naraz).
        TokenCard(borderColor = WhiteBorder) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    tr("Odcięcie niskiego napięcia (LBP)", "Low voltage cutoff (LBP)"),
                    fontFamily = Manrope, fontSize = 14.sp, color = Tokens.TextPrimary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${basic.lowBatteryProtection} V",
                    fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Tokens.TextPrimary,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                tr("Edytowalne w zakładce Bafang Basic.", "Editable in the Bafang Basic tab."),
                fontFamily = Manrope, fontSize = 11.sp, color = Tokens.TextSecondary,
            )
        }

        ExpandableParamTile(
            label = tr("Liczba cel", "Cell count"),
            valueLabel = "${state.cellCount}S",
            description = tr(
                "Liczba ogniw połączonych szeregowo w Twoim pakiecie (np. 13S = 13 ogniw). Razem z " +
                    "napięciem ogniwa (ok. 3,7V) wyznacza napięcie nominalne pakietu (tu: ${state.nominalPackVoltage}V) - " +
                    "EggSPEED używa go do przeliczania prądu na moc i do szacowania zasięgu. To dane pomocnicze aplikacji, " +
                    "nie są wysyłane do sterownika.",
                "The number of cells connected in series in your pack (e.g. 13S = 13 cells). Together with " +
                    "the cell voltage (approx. 3.7V) it determines the pack's nominal voltage (here: ${state.nominalPackVoltage}V) - " +
                    "EggSPEED uses it to convert current to power and to estimate range. This is app-side helper data, " +
                    "not sent to the controller.",
            ),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                StepBtn("-", true) { onCellCountChange(state.cellCount - 1) }
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .background(Tokens.BlueFaint16, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text("${state.nominalPackVoltage}V", fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Tokens.Blue)
                }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onCellCountChange(state.cellCount + 1) }
            }
        }

        ExpandableParamTile(
            label = tr("Pojemność", "Capacity"),
            valueLabel = "${String.format("%.1f", state.capacityAh)} Ah",
            description = tr(
                "Pojemność Twojego pakietu w Ah (amperogodzinach) i Wh (watogodzinach, Ah × napięcie " +
                    "nominalne) - wpisz wartość z etykiety baterii lub karty produktu. EggSPEED używa jej razem z " +
                    "bieżącym zużyciem prądu, żeby szacować zasięg na ekranie Kokpit. To dane pomocnicze aplikacji, " +
                    "nie są wysyłane do sterownika.",
                "Your pack's capacity in Ah (amp-hours) and Wh (watt-hours, Ah × nominal " +
                    "voltage) - enter the value from the battery label or product page. EggSPEED uses it together " +
                    "with the current power draw to estimate range on the Cockpit screen. This is app-side helper " +
                    "data, not sent to the controller.",
            ),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CapacityField(
                    value = String.format("%.1f", state.capacityAh),
                    suffix = "Ah",
                    onValueChange = { text -> text.replace(',', '.').toDoubleOrNull()?.let(onCapacityAhChange) },
                    modifier = Modifier.weight(1f),
                )
                CapacityField(
                    value = String.format("%.0f", state.capacityWh),
                    suffix = "Wh",
                    onValueChange = { text -> text.replace(',', '.').toDoubleOrNull()?.let(onCapacityWhChange) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun CapacityField(value: String, suffix: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    // Bufor lokalny odłączony od zewnętrznej wartości podczas edycji - bez tego kasowanie/wpisywanie
    // niepełnej liczby ("12." albo puste pole) było natychmiast nadpisywane sformatowaną wartością
    // z ViewModel (bo toDoubleOrNull() na niepełnym tekście zwraca null i nic się nie zmieniało).
    var isFocused by remember { mutableStateOf(false) }
    var localText by remember { mutableStateOf(value) }
    LaunchedEffect(value, isFocused) {
        if (!isFocused) localText = value
    }

    Row(
        modifier = modifier
            .background(Tokens.Elevated, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = localText,
            onValueChange = { text ->
                localText = text
                onValueChange(text)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = TextStyle(color = Tokens.TextPrimary, fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 16.sp),
            cursorBrush = SolidColor(Tokens.Blue),
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { isFocused = it.isFocused },
        )
        Text(suffix, fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextSecondary)
    }
}
