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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bafspeed.app.FirmwareType
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

@Composable
fun SettingsScreen(
    state: UiState,
    onUnitsChange: (SpeedUnit) -> Unit,
    onOdoOffsetChange: (Double) -> Unit,
    onFirmwareTypeChange: (FirmwareType) -> Unit,
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
        MicroLabel(tr("Firmware sterownika", "Controller firmware"))
        TokenCard(borderColor = Tokens.WhiteBorder) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(tr("Firmware", "Firmware"), fontFamily = Manrope, fontSize = 14.sp, color = Tokens.TextPrimary, modifier = Modifier.weight(1f))
                SegmentedControl(
                    options = listOf(tr("OEM Bafang", "OEM Bafang"), "BBS-FW"),
                    selectedIndex = if (state.firmwareType == FirmwareType.BBS_FW) 1 else 0,
                    onSelect = { onFirmwareTypeChange(if (it == 1) FirmwareType.BBS_FW else FirmwareType.OEM_BAFANG) },
                    modifier = Modifier.width(180.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            FirmwareDescriptionParagraph(
                label = "OEM Bafang",
                text = tr(
                    "fabryczny, zamknięty firmware sterowników Bafang BBS01/BBS02/BBSHD. Komunikuje się protokołem Bafang Configuration Tool, którego apka używa domyślnie.",
                    "the factory, closed-source firmware that Bafang BBS01/BBS02/BBSHD controllers ship with by default. Speaks the Bafang Configuration Tool protocol, which the app uses by default.",
                ),
            )
            Spacer(Modifier.height(8.dp))
            FirmwareDescriptionParagraph(
                label = "BBS-FW",
                text = tr(
                    "(github.com/danielnilsson9/bbs-fw) - otwarte, alternatywne firmware, które można samodzielnie wgrać na te same sterowniki w miejsce fabrycznego. Ma WŁASNY, INNY protokół konfiguracji, więc przełącznik zmienia, jakich ramek apka używa do rozmowy ze sterownikiem.",
                    "(github.com/danielnilsson9/bbs-fw) - open-source, alternative firmware you can flash yourself onto the same controllers in place of the factory one. Has its OWN, DIFFERENT configuration protocol, so this switch changes which frames the app uses to talk to the controller.",
                ),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                tr(
                    "Wybierz opcję zgodną z tym, co faktycznie masz wgrane na sterowniku. Zmiana wymaga ponownego połączenia.",
                    "Pick whichever matches what's actually flashed on your controller. Changing it requires reconnecting.",
                ),
                fontFamily = Manrope, fontSize = 11.sp, color = Tokens.TextSecondary,
            )
        }

        MicroLabel(tr("Aplikacja", "Application"))
        TokenCard(borderColor = Tokens.WhiteBorder) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(tr("Jednostki", "Units"), fontFamily = Manrope, fontSize = 14.sp, color = Tokens.TextPrimary, modifier = Modifier.weight(1f))
                SegmentedControl(
                    options = listOf("km/h", "mph"),
                    selectedIndex = if (state.units == SpeedUnit.MPH) 1 else 0,
                    onSelect = { onUnitsChange(if (it == 1) SpeedUnit.MPH else SpeedUnit.KMH) },
                    modifier = Modifier.width(180.dp),
                )
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

        MicroLabel(tr("Połączenie", "Connection"))
        TokenCard(borderColor = Tokens.WhiteBorder, modifier = Modifier.alpha(0.55f)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(tr("Typ połączenia", "Connection type"), fontFamily = Manrope, fontSize = 14.sp, color = Tokens.TextPrimary)
                    Text(
                        tr("Bluetooth - wkrótce", "Bluetooth - coming soon"),
                        fontFamily = Manrope, fontSize = 11.sp, color = Tokens.TextSecondary,
                    )
                }
                SegmentedControl(
                    options = listOf("USB", "Bluetooth"),
                    selectedIndex = 0,
                    onSelect = {},
                    modifier = Modifier.width(180.dp),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

/** Akapit opisu firmware - etykieta ("OEM Bafang"/"BBS-FW") na zielono, reszta zdania normalnym kolorem. */
@Composable
private fun FirmwareDescriptionParagraph(label: String, text: String) {
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(color = Tokens.Emerald, fontWeight = FontWeight.Bold)) { append(label) }
            append(" $text")
        },
        fontFamily = Manrope, fontSize = 11.sp, color = Tokens.TextSecondary,
    )
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
