package com.bafspeed.app.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

/** Języki dostępne w aplikacji - flaga (emoji) + nazwa własna, pokazywane w wyborze języka. */
enum class AppLanguage(val flag: String, val displayName: String) {
    PL("🇵🇱", "Polski"),
    EN("🇬🇧", "English"),
    DE("🇩🇪", "Deutsch"),
    FR("🇫🇷", "Français"),
    ES("🇪🇸", "Español"),
    PT("🇵🇹", "Português"),
    IT("🇮🇹", "Italiano"),
    NL("🇳🇱", "Nederlands"),
    SV("🇸🇪", "Svenska"),
    CS("🇨🇿", "Čeština"),
    SK("🇸🇰", "Slovenčina"),
}

/** Bieżący język UI - dostarczany z poziomu MainActivity na podstawie UiState.language. */
val LocalAppLanguage = compositionLocalOf { AppLanguage.EN }

/**
 * Tłumaczenie inline w miejscu użycia - zamiast osobnego pliku z setkami kluczy, każdy widoczny
 * dla użytkownika napis dostaje od razu swój odpowiednik w drugim języku, prosto przy literale
 * który zastępuje. Wersja bez CompositionLocal - do użycia poza Compose (np. w AppViewModel).
 *
 * [de]/[fr]/[es]/[pt]/[it]/[nl]/[sv]/[cs]/[sk] są opcjonalne i domyślnie spadają na [en] - pozwala
 * to dodawać języki stopniowo, ekran po ekranie, bez ryzyka pustego/zepsutego tekstu w miejscach
 * jeszcze nieprzetłumaczonych.
 */
fun tr(
    lang: AppLanguage,
    pl: String,
    en: String,
    de: String? = null,
    fr: String? = null,
    es: String? = null,
    pt: String? = null,
    it: String? = null,
    nl: String? = null,
    sv: String? = null,
    cs: String? = null,
    sk: String? = null,
): String = when (lang) {
    AppLanguage.PL -> pl
    AppLanguage.EN -> en
    AppLanguage.DE -> de ?: en
    AppLanguage.FR -> fr ?: en
    AppLanguage.ES -> es ?: en
    AppLanguage.PT -> pt ?: en
    AppLanguage.IT -> it ?: en
    AppLanguage.NL -> nl ?: en
    AppLanguage.SV -> sv ?: en
    AppLanguage.CS -> cs ?: en
    AppLanguage.SK -> sk ?: en
}

/** Wersja @Composable - czyta bieżący język z [LocalAppLanguage] automatycznie. */
@Composable
fun tr(
    pl: String,
    en: String,
    de: String? = null,
    fr: String? = null,
    es: String? = null,
    pt: String? = null,
    it: String? = null,
    nl: String? = null,
    sv: String? = null,
    cs: String? = null,
    sk: String? = null,
): String = tr(LocalAppLanguage.current, pl, en, de, fr, es, pt, it, nl, sv, cs, sk)
