package com.bafspeed.app.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

/** Języki dostępne w aplikacji - flaga (emoji) + nazwa własna, pokazywane w wyborze języka. */
enum class AppLanguage(val flag: String, val displayName: String) {
    PL("🇵🇱", "Polski"),
    EN("🇬🇧", "English"),
}

/** Bieżący język UI - dostarczany z poziomu MainActivity na podstawie UiState.language. */
val LocalAppLanguage = compositionLocalOf { AppLanguage.EN }

/**
 * Tłumaczenie inline w miejscu użycia - zamiast osobnego pliku z setkami kluczy, każdy widoczny
 * dla użytkownika napis dostaje od razu swój odpowiednik w drugim języku, prosto przy literale
 * który zastępuje. Wersja bez CompositionLocal - do użycia poza Compose (np. w AppViewModel).
 */
fun tr(lang: AppLanguage, pl: String, en: String): String = if (lang == AppLanguage.EN) en else pl

/** Wersja @Composable - czyta bieżący język z [LocalAppLanguage] automatycznie. */
@Composable
fun tr(pl: String, en: String): String = tr(LocalAppLanguage.current, pl, en)
