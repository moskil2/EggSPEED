package com.bafspeed.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.bafspeed.app.R

/**
 * Ustawienie "Wysoki kontrast" (Ustawienia, po ODOMETER) - dostarczane z poziomu MainActivity
 * na podstawie UiState.highContrast. Podbija [Tokens.TextSecondary]/[Tokens.TextTertiary] (dziś
 * wyblakłe szarości) do niemal pełnej bieli w całej apce, żeby poprawić czytelność w pełnym słońcu.
 */
val LocalHighContrast = compositionLocalOf { false }

/** Design tokens dla ciemnego motywu EggSPEED. */
object Tokens {
    val Bg = Color(0xFF020203)            // tło strony
    val Card = Color(0xFF0D1013)          // powierzchnia karty
    val Elevated = Color(0xFF181D22)      // powierzchnia podniesiona / input / track
    val Border = Color(0x0FFFFFFF)        // rgba(255,255,255,0.06)
    val TextPrimary = Color(0xFFF2F3F5)

    /** ~0.6 alfa normalnie, niemal pełna biel (1.0) przy włączonym Wysokim kontraście. */
    val TextSecondary: Color
        @Composable get() = if (LocalHighContrast.current) Color(0xFFF2F3F5) else Color(0x99F2F3F5)

    /** ~0.4 alfa normalnie, ~0.9 przy Wysokim kontraście (odrobinę niżej niż TextSecondary, żeby zostawić ślad hierarchii/stanu disabled). */
    val TextTertiary: Color
        @Composable get() = if (LocalHighContrast.current) Color(0xE6F2F3F5) else Color(0x66F2F3F5)

    val TextBright80 = Color(0xCCF2F3F5)  // ~0.8 - etykiety wymagające wyższego kontrastu niż TextSecondary (np. w słońcu)
    val Blue = Color(0xFF4C8DFF)          // electric blue
    val Emerald = Color(0xFF34D399)
    val Amber = Color(0xFFF5A524)
    val Red = Color(0xFFF0453A)
    val Purple = Color(0xFF9B6BFF)
    val OnAccent = Color(0xFF04101F)      // tekst na wypełnieniu blue/amber
    val BlueFaint16 = Color(0x294C8DFF)
    val BlueFaint28 = Color(0x474C8DFF)
}

@OptIn(ExperimentalTextApi::class)
private fun variableFont(resId: Int, weight: FontWeight) = Font(
    resId = resId,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

/** Sora - wartości liczbowe, nagłówki. */
val Sora = FontFamily(
    variableFont(R.font.sora, FontWeight.Medium),
    variableFont(R.font.sora, FontWeight.SemiBold),
    variableFont(R.font.sora, FontWeight.Bold),
    variableFont(R.font.sora, FontWeight.ExtraBold),
)

/** Manrope - etykiety, tekst UI. */
val Manrope = FontFamily(
    variableFont(R.font.manrope, FontWeight.Normal),
    variableFont(R.font.manrope, FontWeight.Medium),
    variableFont(R.font.manrope, FontWeight.SemiBold),
    variableFont(R.font.manrope, FontWeight.Bold),
)
