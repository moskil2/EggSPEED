package com.bafspeed.app.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bafspeed.app.BuildConfig
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.ui.components.EggSpeedWordmark
import com.bafspeed.app.ui.components.ExpandableParamTile
import com.bafspeed.app.ui.components.TokenCard
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens

private val APP_VERSION = BuildConfig.VERSION_NAME
private val BUILD_STAMP = BuildConfig.BUILD_STAMP
private const val CONTACT_EMAIL = "tomasz.pieczara@gazeta.pl"
private const val WEBSITE = "spotrobotics.app"
private const val SUPPORT_FORM_URL = "https://spotrobotics.app/support/"
private const val GITHUB_DISPLAY = "github.com/moskil2/EggSPEED"
private const val GITHUB_URL = "https://$GITHUB_DISPLAY"

/**
 * Zakladka "Menu" - poza informacjami o aplikacji (dawne "About") zawiera tez akcje zwiazane
 * z Google Play (ocena, sprawdzenie aktualizacji). Ten sam wzorzec wizualny co Pedal/Throttle/General.
 */
@Composable
fun AboutScreen() {
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .background(Tokens.Bg)
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = 16.dp)),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        TokenCard(borderColor = Tokens.WhiteBorder) {
            EggSpeedWordmark(fontSize = 20.sp, letterSpacing = 0.sp)
            Spacer(Modifier.height(4.dp))
            Text(tr("Stworzone przez Tomasza Pieczarę", "Created by Tomasz Pieczara"), fontFamily = Manrope, fontSize = 14.sp, color = Tokens.TextPrimary)
        }

        TokenCard(borderColor = Tokens.WhiteBorder) {
            ActionRow(
                icon = "⭐",
                label = tr("Oceń aplikację w Google Play", "Rate the app on Google Play"),
                onClick = { openPlayStoreListing(context) },
            )
            HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
            ActionRow(
                icon = "⬆",
                label = tr("Sprawdź aktualizacje", "Check for updates"),
                onClick = { openPlayStoreListing(context) },
            )
        }

        TokenCard(borderColor = Tokens.WhiteBorder) {
            InfoRow(tr("Wersja", "Version"), APP_VERSION)
            HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
            InfoRow("Build", BUILD_STAMP)
        }

        TokenCard(borderColor = Tokens.WhiteBorder) {
            InfoRow(
                tr("Kontakt", "Contact"),
                CONTACT_EMAIL,
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$CONTACT_EMAIL"))
                    runCatching { context.startActivity(intent) }
                },
            )
            HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
            InfoRow(
                tr("Strona", "Website"),
                WEBSITE,
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://$WEBSITE"))
                    runCatching { context.startActivity(intent) }
                },
            )
            HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
            InfoRow(
                "GitHub",
                GITHUB_DISPLAY,
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL))
                    runCatching { context.startActivity(intent) }
                },
            )
        }

        ExpandableParamTile(
            label = tr("Polityka prywatności", "Privacy policy"),
            valueLabel = "",
            descriptionColor = Tokens.TextPrimary,
            description = tr(
                "EggSPEED nie zbiera, nie wysyła ani nie przechowuje żadnych danych osobowych poza " +
                    "Twoim telefonem. Komunikacja odbywa się wyłącznie lokalnie przez kabel USB między telefonem " +
                    "a sterownikiem Bafang - bez internetu, bez analityki, bez śledzenia. Profile konfiguracji " +
                    "zapisujesz wyłącznie Ty, jako pliki .ini na swoim urządzeniu.",
                "EggSPEED does not collect, send, or store any personal data beyond " +
                    "your phone. Communication happens purely locally over a USB cable between the phone " +
                    "and the Bafang controller - no internet, no analytics, no tracking. Configuration profiles " +
                    "are saved only by you, as .ini files on your own device.",
            ),
        ) {}

        ExpandableParamTile(
            label = tr("Warunki korzystania", "Terms of service"),
            valueLabel = "",
            descriptionColor = Tokens.Red,
            description = tr(
                "EggSPEED to program hobbystyczny, przeznaczony dla świadomych entuzjastów, którzy wiedzą jak " +
                    "programować swój sterownik Bafang - służy wyłącznie do odczytu konfiguracji oraz bezpiecznego " +
                    "sterowania (poziom wspomagania, światło) sterownikiem Bafang BBS01/BBS02/BBSHD przez kabel USB. " +
                    "Korzystając z aplikacji ponosisz pełną odpowiedzialność za sposób jej używania oraz za " +
                    "konfigurację i legalność użytkowania swojego roweru elektrycznego, w tym zgodność z lokalnymi " +
                    "przepisami. Aplikacja dostarczana jest \"tak jak jest\", bez żadnej gwarancji nieprzerwanego " +
                    "lub bezbłędnego działania. Twórca nie ponosi żadnej odpowiedzialności za szkody wynikłe z " +
                    "używania aplikacji - używasz jej na własną odpowiedzialność.",
                "EggSPEED is a hobby program, built for informed enthusiasts who know how to program their " +
                    "Bafang controller - it is intended solely for reading the configuration of, and safely " +
                    "controlling (assist level, light), a Bafang BBS01/BBS02/BBSHD controller over a USB cable. By " +
                    "using the app you are fully responsible for how you use it and for the configuration and " +
                    "legal use of your e-bike, including compliance with local regulations. The app is provided " +
                    "\"as is\", without any warranty of uninterrupted or error-free operation. The developer bears " +
                    "no responsibility whatsoever for damage resulting from using the app - you use it at your " +
                    "own risk.",
            ),
        ) {}

        ContactExpandableTile(
            label = tr("Kontakt / Zgłoś błąd / Propozycja funkcji", "Contact / Report bug / Feature request"),
            body = tr(
                "Znalazłeś błąd? Masz pomysł na nową funkcję? Chcesz się po prostu przywitać? Wypełnij krótki " +
                    "formularz - każda wiadomość jest czytana osobiście.",
                "Found a bug or have an idea for a new feature? Just want to say hi? Fill out a short form - " +
                    "every message is read personally.",
            ),
            linkLabel = tr("Otwórz formularz zgłoszeniowy →", "Open support form →"),
            onLinkClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(SUPPORT_FORM_URL))
                runCatching { context.startActivity(intent) }
            },
        )
        Spacer(Modifier.height(8.dp))
    }
}

/**
 * Wariant [ExpandableParamTile] dla sekcji Kontakt - link do formularza jest częścią rozwijanej
 * treści (widoczny dopiero po kliknięciu kafelki), a nie stałym elementem, żeby użytkownik
 * najpierw przeczytał, do czego formularz służy.
 */
@Composable
private fun ContactExpandableTile(label: String, body: String, linkLabel: String, onLinkClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    TokenCard(
        modifier = Modifier.clickable { expanded = !expanded },
        contentPadding = 6.dp,
        borderColor = Tokens.WhiteBorder,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                label.uppercase(),
                fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 11.sp,
                letterSpacing = 1.sp, color = Tokens.TextPrimary, modifier = Modifier.weight(1f),
            )
            Text(if (expanded) "▲" else "▼", fontFamily = Manrope, fontSize = 16.sp, color = Tokens.Emerald)
        }
        if (expanded) {
            Spacer(Modifier.height(6.dp))
            Text(body, fontFamily = Manrope, fontSize = 11.sp, lineHeight = 15.sp, color = Tokens.TextPrimary)
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLinkClick() },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(linkLabel, fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Tokens.Blue)
            }
        }
    }
}

/** Próbuje otworzyć wpis appki w Sklepie Play przez samą appkę Sklepu (`market://`), a jeśli jej brak - w przeglądarce. */
private fun openPlayStoreListing(context: Context) {
    val pkg = context.packageName
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")).apply {
                setPackage("com.android.vending")
            },
        )
    } catch (e: ActivityNotFoundException) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg")))
        }
    }
}

/** Klikalny wiersz akcji (ikona + etykieta + strzałka) - dla pozycji menu prowadzących poza appkę (np. do Sklepu Play). */
@Composable
private fun ActionRow(icon: String, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(icon, fontSize = 16.sp, modifier = Modifier.width(24.dp))
        Text(label, fontFamily = Manrope, fontSize = 14.sp, color = Tokens.TextPrimary, modifier = Modifier.weight(1f))
        Text("→", fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Tokens.Blue)
    }
}

@Composable
private fun InfoRow(label: String, value: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable { onClick() } else it }
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontFamily = Manrope, fontSize = 14.sp, color = Tokens.TextPrimary, modifier = Modifier.weight(1f))
        Text(
            value,
            fontFamily = Sora, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
            color = if (onClick != null) Tokens.Blue else Tokens.TextPrimary,
        )
    }
}
