package com.bafspeed.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bafspeed.app.ui.theme.LocalLightMode
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens

/** Suwak z etykietą i wartością po prawej - styl briefu. */
@Composable
fun LabeledSlider(
    label: String,
    valueLabel: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    accent: Color,
    onValueChange: (Float) -> Unit,
    steps: Int = 0,
    enabled: Boolean = true,
) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                label.uppercase(),
                fontFamily = Manrope, fontWeight = FontWeight.Medium, fontSize = 11.sp,
                letterSpacing = 1.sp, color = Tokens.TextTertiary, modifier = Modifier.weight(1f),
            )
            Text(valueLabel, fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Tokens.TextPrimary)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            steps = steps,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = if (enabled) accent else Tokens.TextTertiary,
                activeTrackColor = if (enabled) accent else Tokens.Elevated,
                inactiveTrackColor = Tokens.Elevated,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent,
            ),
        )
    }
}

/** Goły suwak bez własnej etykiety/wartości - do użycia wewnątrz [ExpandableParamTile]. */
@Composable
fun PlainSlider(
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    accent: Color,
    onValueChange: (Float) -> Unit,
    steps: Int = 0,
    enabled: Boolean = true,
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = range,
        steps = steps,
        enabled = enabled,
        colors = SliderDefaults.colors(
            thumbColor = if (enabled) accent else Tokens.TextTertiary,
            activeTrackColor = if (enabled) accent else Tokens.Elevated,
            inactiveTrackColor = Tokens.Elevated,
            // Bez tego, przy gęstym steps (np. co 1%) Material3 rysuje domyślne fioletowe
            // znaczniki kroków, które zlewają się w widoczny pasek na torze suwaka.
            activeTickColor = Color.Transparent,
            inactiveTickColor = Color.Transparent,
        ),
    )
}

/**
 * Nagłówek sekcji (MicroLabel) klikalny w całości, ze strzałką - zwija/rozwija [content] pod spodem.
 * Domyślnie zwinięte. Do prostych bloków bez własnej wartości/opisu (np. kafelki PODGLĄD w Kalibracji) -
 * dla parametrów z wartością/opisem i kontrolką patrz [ExpandableParamTile].
 */
@Composable
fun CollapsibleMicroLabel(label: String, content: @Composable () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().clickable { expanded = !expanded },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MicroLabel(label, modifier = Modifier.weight(1f))
        Text(if (expanded) "▲" else "▼", fontFamily = Manrope, fontSize = 12.sp, color = Tokens.Emerald)
    }
    if (expanded) {
        Spacer(Modifier.height(6.dp))
        content()
    }
}

/**
 * Karta pojedynczego parametru: nagłówek (etykieta + wartość + strzałka) klikalny w całości -
 * rozwija/zwija opis funkcji (mała pomarańczowa czcionka). Kontrolka w [content] pozostaje
 * w pełni interaktywna, bo sama przechwytuje swoje gesty (Compose hit-testuje najgłębszy
 * uchwyt jako pierwszy), więc np. przeciąganie suwaka nie koliduje z klikiem rozwijającym kartę.
 */
@Composable
fun ExpandableParamTile(
    label: String,
    valueLabel: String,
    description: String,
    descriptionColor: Color = Tokens.Amber,
    // Nadpisuje domyślny jednokolorowy Text(description) bogatszą treścią (np. wieloakapitowy
    // opis z fragmentami w innym kolorze) - opcjonalne, żeby nie ruszać pozostałych wywołań.
    descriptionContent: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    // Długie wartości (np. "By Display's Command") dostają mniejszą czcionkę, żeby zmieścić
    // się w jednej linii obok etykiety zamiast zawijać/rozpychać nagłówek karty.
    val valueFontSize = when {
        valueLabel.length >= 20 -> 11.sp
        valueLabel.length >= 16 -> 13.sp
        else -> 15.sp
    }
    TokenCard(
        modifier = Modifier.clickable { expanded = !expanded },
        contentPadding = 6.dp,
        borderColor = Tokens.WhiteBorder,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                label.uppercase(),
                fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 11.sp,
                letterSpacing = 1.sp, color = Tokens.TextPrimary, modifier = Modifier.weight(1f),
            )
            Text(
                valueLabel,
                fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = valueFontSize,
                color = Tokens.TextPrimary, maxLines = 1,
            )
            Spacer(Modifier.size(8.dp))
            Text(if (expanded) "▲" else "▼", fontFamily = Manrope, fontSize = 16.sp, color = Tokens.Emerald)
        }
        if (expanded) {
            Spacer(Modifier.height(6.dp))
            if (descriptionContent != null) {
                descriptionContent()
            } else {
                Text(
                    description,
                    fontFamily = Manrope, fontSize = 11.sp, lineHeight = 15.sp, color = descriptionColor,
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        content()
    }
}

/**
 * Suwak oflankowany przyciskami -/+ (krok 1 jednostka) - zawęża wizualnie sam tor suwaka
 * i pozwala na precyzyjną korektę o pojedynczy % bez przeciągania.
 */
@Composable
fun FlankedSlider(value: Int, range: IntRange, accent: Color, onValueChange: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        StepBtn("-", true) { onValueChange((value - 1).coerceIn(range)) }
        Spacer(Modifier.size(10.dp))
        Box(Modifier.weight(1f)) {
            PlainSlider(
                value = value.toFloat(),
                range = range.first.toFloat()..range.last.toFloat(),
                accent = accent,
                steps = (range.last - range.first - 1).coerceAtLeast(0),
                onValueChange = { onValueChange(it.toInt()) },
            )
        }
        Spacer(Modifier.size(10.dp))
        StepBtn("+", true) { onValueChange((value + 1).coerceIn(range)) }
    }
}

/**
 * Pole do ręcznego wpisania liczby całkowitej - bufor lokalny odłączony od zewnętrznej
 * wartości podczas edycji (synchronizacja tylko gdy pole nie ma fokusu), żeby dało się
 * swobodnie kasować/wpisywać bez natychmiastowego nadpisania sformatowaną wartością.
 */
@Composable
fun IntEntryField(value: Int, onValueChange: (Int) -> Unit, range: IntRange, modifier: Modifier = Modifier) {
    var isFocused by remember { mutableStateOf(false) }
    var localText by remember { mutableStateOf(value.toString()) }
    LaunchedEffect(value, isFocused) {
        if (!isFocused) localText = value.toString()
    }
    BasicTextField(
        value = localText,
        onValueChange = { text ->
            localText = text
            text.toIntOrNull()?.let { onValueChange(it.coerceIn(range)) }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textStyle = TextStyle(
            color = Tokens.TextPrimary, fontFamily = Sora, fontWeight = FontWeight.Bold,
            fontSize = 15.sp, textAlign = TextAlign.End,
        ),
        cursorBrush = SolidColor(Tokens.Blue),
        modifier = modifier
            .background(Tokens.Elevated, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .onFocusChanged { isFocused = it.isFocused },
    )
}

@Composable
fun TokenSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accent: Color = Tokens.Blue,
    enabled: Boolean = true,
    // Skaluje przełącznik przez lokalną gęstość zamiast Modifier.scale - dzięki temu naprawdę
    // zajmuje mniej miejsca w layoucie (łącznie z niewidocznym minimalnym touch targetem M3),
    // a nie tylko wygląda mniejszy przy tym samym, "pustym" obszarze dookoła.
    scale: Float = 1f,
) {
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density * scale, density.fontScale)) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accent,
                uncheckedThumbColor = Tokens.TextTertiary,
                uncheckedTrackColor = Tokens.Elevated,
                uncheckedBorderColor = Tokens.Border,
            ),
        )
    }
}

/** Wiersz przełącznika: etykieta (+opcjonalny opis) po lewej, switch po prawej. */
@Composable
fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accent: Color = Tokens.Blue,
    description: String? = null,
    enabled: Boolean = true,
) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (enabled) Tokens.TextPrimary else Tokens.TextSecondary)
            if (description != null) {
                Spacer(Modifier.height(2.dp))
                Text(description, fontFamily = Manrope, fontSize = 12.sp, color = Tokens.TextSecondary)
            }
        }
        TokenSwitch(checked, onCheckedChange, accent, enabled)
    }
}

/** Segmentowany przełącznik (2-3 opcje). */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(Tokens.Card, RoundedCornerShape(14.dp))
            .border(1.dp, Tokens.Border, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEachIndexed { i, opt ->
            val selected = i == selectedIndex
            Box(
                Modifier
                    .weight(1f)
                    .background(if (selected) Tokens.Blue else Color.Transparent, RoundedCornerShape(11.dp))
                    .clickable { onSelect(i) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    opt,
                    fontFamily = Manrope,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp,
                    color = if (selected) Tokens.OnAccent else Tokens.TextTertiary,
                )
            }
        }
    }
}

/** Wiersz stepera: etykieta, − wartość + */
@Composable
fun StepperRow(label: String, value: String, onMinus: () -> Unit, onPlus: () -> Unit, enabled: Boolean = true) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Tokens.TextPrimary, modifier = Modifier.weight(1f))
        StepBtn("−", enabled, onMinus)
        Box(Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
            Text(value, fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Tokens.TextPrimary)
        }
        StepBtn("+", enabled, onPlus)
    }
}

/**
 * Wariant stepera dla długich etykiet/wartości (np. angielskie "Designated Assist Level" +
 * "By Display's Command") - etykieta na osobnej linii nad kontrolką, żeby nie łamać się
 * w jednym wierszu jak [StepperRow].
 */
@Composable
fun LabeledStepperRow(label: String, value: String, onMinus: () -> Unit, onPlus: () -> Unit, enabled: Boolean = true) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            label.uppercase(),
            fontFamily = Manrope, fontWeight = FontWeight.Medium, fontSize = 11.sp,
            letterSpacing = 1.sp, color = Tokens.TextTertiary,
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            StepBtn("−", enabled, onMinus)
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(value, fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Tokens.TextPrimary)
            }
            StepBtn("+", enabled, onPlus)
        }
    }
}

@Composable
fun StepBtn(label: String, enabled: Boolean, onClick: () -> Unit) {
    val borderColor = if (enabled) Tokens.WhiteBorder else (if (LocalLightMode.current) Color(0x24000000) else Color(0x24FFFFFF))
    Box(
        Modifier
            .size(38.dp)
            .background(Tokens.Elevated, RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if (enabled) Tokens.TextPrimary else Tokens.TextTertiary)
    }
}

/**
 * Banner ostrzegawczy - używany na ekranach edycji (M1: podgląd, brak zapisu). Gdy [collapsible] -
 * domyślnie zwinięty do jednej linii (z wielokropkiem) i zielonym trójkątem na końcu; klik rozwija
 * pełny tekst. Domyślnie [collapsible]=false, więc wszystkie dotychczasowe wywołania bez zmian.
 */
@Composable
fun PreviewBanner(
    text: String,
    borderWidth: Dp = 1.dp,
    textColor: Color = Tokens.TextPrimary,
    dotColor: Color = Tokens.Amber,
    showDot: Boolean = true,
    contentPadding: Dp = 12.dp,
    collapsible: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    val showFull = !collapsible || expanded
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0x1AF5A524), RoundedCornerShape(12.dp))
            .border(borderWidth, Color(0x33F5A524), RoundedCornerShape(12.dp))
            .then(if (collapsible) Modifier.clickable { expanded = !expanded } else Modifier)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showDot) {
            Box(Modifier.size(8.dp).background(dotColor, RoundedCornerShape(4.dp)))
            Spacer(Modifier.size(10.dp))
        }
        Text(
            text,
            fontFamily = Manrope, fontSize = 12.sp, color = textColor,
            maxLines = if (showFull) Int.MAX_VALUE else 1,
            overflow = if (showFull) TextOverflow.Clip else TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (collapsible) {
            Spacer(Modifier.size(8.dp))
            Text(if (expanded) "▲" else "▼", fontFamily = Manrope, fontSize = 14.sp, color = Tokens.Emerald)
        }
    }
}

/** Rozwijana lista wyboru: etykieta, aktualnie wybrana opcja, klik rozwija listę pod spodem. */
@Composable
fun DropdownRow(label: String, options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, fontFamily = Manrope, fontSize = 14.sp, color = Tokens.TextSecondary, modifier = Modifier.weight(1f))
            Text(
                options.getOrElse(selectedIndex) { "?" },
                fontFamily = Sora, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Tokens.TextPrimary,
            )
            Text(if (expanded) "  ▲" else "  ▼", fontFamily = Manrope, fontSize = 12.sp, color = Tokens.TextTertiary)
        }
        if (expanded) {
            Spacer(Modifier.height(6.dp))
            options.forEachIndexed { i, option ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(if (i == selectedIndex) Tokens.Elevated else Tokens.Card, RoundedCornerShape(8.dp))
                        .clickable { onSelect(i); expanded = false }
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                ) {
                    Text(option, fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextPrimary)
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

/**
 * Goła lista opcji do wyboru, bez własnej etykiety/wartości/strzałki - do użycia wewnątrz
 * [ExpandableParamTile], którego nagłówek już pokazuje etykietę i aktualną wartość. Zawsze
 * w pełni rozwinięta (nie ma własnego stanu collapsed/expanded), więc nie duplikuje się
 * z klikiem rozwijającym opis na karcie nadrzędnej.
 */
@Composable
fun PlainOptionList(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        options.forEachIndexed { i, option ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(if (i == selectedIndex) Tokens.Elevated else Tokens.Card, RoundedCornerShape(8.dp))
                    .clickable { onSelect(i) }
                    .padding(vertical = 10.dp, horizontal = 12.dp),
            ) {
                Text(option, fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextPrimary)
            }
            if (i != options.lastIndex) Spacer(Modifier.height(4.dp))
        }
    }
}

/** Nagłówek ekranu - tytuł Sora 22 + opcjonalny podtytuł. */
@Composable
fun ScreenHeader(title: String, subtitle: String? = null) {
    Column {
        Text(title, fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Tokens.TextPrimary)
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(subtitle, fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextTertiary)
        }
    }
}
