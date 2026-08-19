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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bafspeed.app.FirmwareType
import com.bafspeed.app.UiState
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.ui.components.ExpandableParamTile
import com.bafspeed.app.ui.components.MicroLabel
import com.bafspeed.app.ui.components.PreviewBanner
import com.bafspeed.app.ui.components.StepBtn
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens

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
                "Liczba cel i pojemność to dane pomocnicze aplikacji - nie są zapisywane w sterowniku, ale są potrzebne, żeby " +
                    "EggSPEED mógł poprawnie wyliczyć zasięg roweru na ekranie Kokpit.",
                "Cell count and capacity are app-side helper data - they aren't saved in the controller, but are needed so " +
                    "EggSPEED can correctly estimate the bike's range on the Cockpit screen.",
            ),
            borderWidth = 2.dp,
        )

        val isBbsFw = state.firmwareType == FirmwareType.BBS_FW
        val bbsFwCfg = state.bbsFwConfigOrDefault

        MicroLabel(tr("Twoja bateria", "Your battery"))

        // Trzy progi napięcia baterii scalone w jeden kafelek - to same dane informacyjne (odczyt
        // lub wyliczone z liczby cel), użytkownik nic tu nie ustawia, więc osobne karty tylko
        // rozpraszały. Rosnąco: dolny limit rozładowania (LBP, realny odczyt - edycja w Bafang
        // Basic / bbs-fw General, jedno źródło prawdy), napięcie nominalne (z liczby cel, tak samo
        // jak w kafelku "Liczba cel" niżej) i górny limit naładowania (cellCount x 4,2V). Nominalne
        // i górny limit celowo liczone tą samą metodą (z cellCount), żeby nie mieszać realnego
        // odczytu z wyliczoną estymatą.
        val lbpV = if (isBbsFw) bbsFwCfg.lowCutOffV else basic.lowBatteryProtection
        ExpandableParamTile(
            label = tr("Napięcie baterii", "Battery voltage"),
            // Puste - kafelek jest rozwijalnym zestawieniem trzech progów, więc nagłówek nie
            // powiela żadnego z nich osobną wartością (dawniej powtarzał tu napięcie nominalne).
            valueLabel = "",
            description = "",
            descriptionContent = {
                BatteryVoltageDescription(isBbsFw = isBbsFw, cellCount = state.cellCount)
            },
        ) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                VoltageReadout(tr("Dolne napięcie odcięcia (LBP)", "Low voltage cutoff (LBP)"), "$lbpV V")
                VoltageReadout(tr("Napięcie nominalne", "Nominal voltage"), "${state.nominalPackVoltage} V")
                VoltageReadout(tr("Górny limit naładowania", "Upper charge limit"), "${String.format("%.1f", state.cellCount * 4.2)} V")
            }
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

/**
 * Opis kafelka "Napięcie baterii" - wstęp plus trzy osobne akapity (po jednym na próg napięcia),
 * każdy zaczynający się nazwą progu na zielono (ten sam odcień co trójkącik rozwijania), reszta
 * zdania białym tekstem - zamiast jednego zbitego akapitu jak wcześniej.
 */
@Composable
private fun BatteryVoltageDescription(isBbsFw: Boolean, cellCount: Int) {
    val nameStyle = SpanStyle(color = Tokens.Emerald, fontWeight = FontWeight.Bold)
    val bodyStyle = SpanStyle(color = Tokens.TextPrimary)

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            tr(
                "Trzy informacyjne progi napięcia - żadnego z nich nie ustawiasz tutaj.",
                "Three informational voltage thresholds - none of them is set here.",
            ),
            fontFamily = Manrope, fontSize = 11.sp, lineHeight = 15.sp, color = Tokens.TextSecondary,
        )
        Text(
            buildAnnotatedString {
                withStyle(nameStyle) { append(tr("Dolne napięcie odcięcia (LBP)", "Low voltage cutoff (LBP)")) }
                withStyle(bodyStyle) {
                    append(
                        tr(
                            " - realny odczyt ze sterownika: próg, przy którym odcina zasilanie, żeby chronić " +
                                "ogniwa przed głębokim rozładowaniem. Edytowalne w zakładce " +
                                (if (isBbsFw) "bbs-fw - Ustawienia podstawowe." else "Bafang Basic."),
                            " - a real reading from the controller: the point at which it cuts power to protect " +
                                "the cells from deep discharge. Editable in the " +
                                (if (isBbsFw) "bbs-fw General tab." else "Bafang Basic tab."),
                        ),
                    )
                }
            },
            fontFamily = Manrope, fontSize = 11.sp, lineHeight = 15.sp,
        )
        Text(
            buildAnnotatedString {
                withStyle(nameStyle) { append(tr("Napięcie nominalne", "Nominal voltage")) }
                withStyle(bodyStyle) {
                    append(
                        tr(
                            " - wartość wyliczona z liczby cel (${cellCount}S x ok. 3,7V/ogniwo) - ta sama, co w " +
                                "kafelku \"Liczba cel\" niżej.",
                            " - calculated from cell count (${cellCount}S x approx. 3.7V/cell) - the same value " +
                                "shown in the \"Cell count\" tile below.",
                        ),
                    )
                }
            },
            fontFamily = Manrope, fontSize = 11.sp, lineHeight = 15.sp,
        )
        Text(
            buildAnnotatedString {
                withStyle(nameStyle) { append(tr("Górny limit naładowania", "Upper charge limit")) }
                withStyle(bodyStyle) {
                    append(
                        tr(
                            " - wartość wyliczona ze specyfikacji ogniw Li-ion (4,2V x liczba cel). Nominalne i " +
                                "górny limit to orientacyjne estymaty - rzeczywiste napięcie pod obciążeniem " +
                                "zmienia się w zależności od poziomu naładowania.",
                            " - calculated from Li-ion cell spec (4.2V x cell count). Nominal and upper limit are " +
                                "approximate estimates - the real voltage under load varies with charge level.",
                        ),
                    )
                }
            },
            fontFamily = Manrope, fontSize = 11.sp, lineHeight = 15.sp,
        )
    }
}

/** Jeden z trzech wierszy w kafelku "Napięcie baterii" - etykieta po lewej, wartość po prawej, jak w oryginalnych osobnych kafelkach. */
@Composable
private fun VoltageReadout(label: String, valueText: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Tokens.Elevated, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label.uppercase(), fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 11.sp,
            letterSpacing = 0.5.sp, color = Tokens.TextSecondary, modifier = Modifier.weight(1f),
        )
        Text(valueText, fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Tokens.TextPrimary)
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
