package com.bafspeed.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens

/** Karta w stylu briefu: #0d1013, radius 18, border 6% white (domyślnie, nadpisywalny). */
@Composable
fun TokenCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 18.dp,
    borderColor: Color = Tokens.Border,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .background(Tokens.Card, RoundedCornerShape(18.dp))
            .padding(contentPadding),
        content = content,
    )
}

/** Wordmark apki - "Egg" niebieski + "SPEED" pomarańczowy, jeden spójny zapis "EggSPEED" wszędzie w apce. */
@Composable
fun EggSpeedWordmark(
    fontSize: TextUnit = 15.sp,
    letterSpacing: TextUnit = 3.sp,
    fontFamily: FontFamily = Sora,
    fontWeight: FontWeight = FontWeight.ExtraBold,
    modifier: Modifier = Modifier,
) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = Tokens.Blue)) { append("Egg") }
            withStyle(SpanStyle(color = Tokens.Amber)) { append("SPEED") }
        },
        modifier = modifier,
        fontFamily = fontFamily,
        fontWeight = fontWeight,
        fontSize = fontSize,
        letterSpacing = letterSpacing,
    )
}

/** Mikro-etykieta sekcji: uppercase 11sp, letter-spacing. */
@Composable
fun MicroLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 1.2.sp,
        color = Tokens.TextPrimary,
    )
}
