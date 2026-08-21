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
 * Ustawienie "Wysoki kontrast" (Ustawienia, obok Trybu jasnego) - dostarczane z poziomu MainActivity
 * na podstawie UiState.highContrast. Podbija teksty drugorzędne (dziś wyblakłe szarości/czernie) do
 * niemal pełnego bieli/czerni w całej apce, żeby poprawić czytelność w pełnym słońcu. Działa niezależnie
 * od [LocalLightMode] - dostępny zarówno w trybie ciemnym, jak i jasnym.
 */
val LocalHighContrast = compositionLocalOf { false }

/**
 * Ustawienie "Tryb jasny" (Ustawienia, obok Wysokiego kontrastu) - dostarczane z poziomu MainActivity
 * na podstawie UiState.lightMode. Przełącza [Tokens] na odwróconą, jasną paletę. Domyślnie false
 * (tryb ciemny, historyczny wygląd apki, bez zmian).
 */
val LocalLightMode = compositionLocalOf { false }

/**
 * Design tokens dla EggSPEED. Domyślnie (LocalLightMode = false) dokładnie ten sam ciemny motyw co
 * dotychczas. Kolory zależne od motywu/kontrastu są `@Composable get()` i czytają [LocalLightMode]/
 * [LocalHighContrast]; kolory akcentów (Blue/Emerald/Amber/...) są wspólne dla obu trybów.
 */
object Tokens {
    val Bg: Color
        @Composable get() = if (LocalLightMode.current) Color(0xFFF7F7F8) else Color(0xFF020203)

    val Card: Color
        @Composable get() = if (LocalLightMode.current) Color(0xFFFFFFFF) else Color(0xFF0D1013)

    val Elevated: Color
        @Composable get() = if (LocalLightMode.current) Color(0xFFE9EAEC) else Color(0xFF181D22)

    val Border: Color
        @Composable get() = if (LocalLightMode.current) Color(0x0F000000) else Color(0x0FFFFFFF)

    /** Odpowiednik dawnego stałego, 35%-owego "WhiteBorder" powtarzanego dawniej osobno w każdym pliku ekranu. */
    val WhiteBorder: Color
        @Composable get() = if (LocalLightMode.current) Color(0x59000000) else Color(0x59FFFFFF)

    val TextPrimary: Color
        @Composable get() = if (LocalLightMode.current) Color(0xFF0A0B0C) else Color(0xFFF2F3F5)

    /**
     * ~0.6 alfa normalnie, niemal pełny kontrast (1.0) przy włączonym Wysokim kontraście - w motywie
     * ciemnym. W motywie jasnym ZAWSZE maksymalny kontrast, niezależnie od Wysokiego kontrastu - słabo
     * czytelne szarości na białym tle w pełnym słońcu są dużo gorsze niż te same szarości na czarnym.
     */
    val TextSecondary: Color
        @Composable get() = when {
            LocalLightMode.current -> Color(0xFF0A0B0C)
            LocalHighContrast.current -> Color(0xFFF2F3F5)
            else -> Color(0x99F2F3F5)
        }

    /** Jak [TextSecondary], ale odrobinę niżej niż TextSecondary, żeby zostawić ślad hierarchii/stanu disabled. */
    val TextTertiary: Color
        @Composable get() = when {
            LocalLightMode.current -> Color(0xE60A0B0C)
            LocalHighContrast.current -> Color(0xE6F2F3F5)
            else -> Color(0x66F2F3F5)
        }

    val TextBright80: Color
        @Composable get() = if (LocalLightMode.current) Color(0xCC0A0B0C) else Color(0xCCF2F3F5)  // ~0.8 - etykiety wymagające wyższego kontrastu niż TextSecondary (np. w słońcu)

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
