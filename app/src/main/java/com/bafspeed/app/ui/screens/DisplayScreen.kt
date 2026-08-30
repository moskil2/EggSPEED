package com.bafspeed.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.bafspeed.app.UiState
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.ui.components.MicroLabel
import com.bafspeed.app.ui.components.SegmentedControl
import com.bafspeed.app.ui.components.ToggleRow
import com.bafspeed.app.ui.components.TokenCard
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Tokens

/** Zakladka "Screen" (menu, pod Ustawieniami) - Motyw (ciemny/jasny), Wysoki kontrast i Kokpit na ekranie blokady/AOD. */
@Composable
fun DisplayScreen(
    state: UiState,
    onHighContrastChange: (Boolean) -> Unit,
    onLightModeChange: (Boolean) -> Unit,
    onAodEnabledChange: (Boolean) -> Unit,
    onAodAssistControlsChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    Column(
        Modifier
            .fillMaxSize()
            .background(Tokens.Bg)
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = 16.dp)),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        TokenCard(borderColor = Tokens.WhiteBorder) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(tr("Motyw", "Theme"), fontFamily = Manrope, fontSize = 14.sp, color = Tokens.TextPrimary, modifier = Modifier.weight(1f))
                SegmentedControl(
                    options = listOf(tr("Ciemny", "Dark"), tr("Jasny", "Light")),
                    selectedIndex = if (state.lightMode) 1 else 0,
                    onSelect = { onLightModeChange(it == 1) },
                    modifier = Modifier.width(180.dp),
                )
            }
            HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
            ToggleRow(
                label = tr("Wysoki kontrast", "High contrast"),
                checked = state.highContrast,
                onCheckedChange = onHighContrastChange,
                accent = Tokens.Blue,
                description = tr(
                    "Rozjaśnia wyblakłe szare napisy w menu i na Kokpicie do niemal pełnej bieli - przydatne przy jeździe w pełnym słońcu. Działa w obu motywach (ciemnym i jasnym).",
                    "Brightens faded gray text in menus and on the Cockpit to near-full white - useful when riding in bright sunlight. Works in both themes (dark and light).",
                ),
            )
        }

        MicroLabel(tr("Ekran blokady / AOD", "Lock screen / AOD"))
        TokenCard(borderColor = Tokens.WhiteBorder) {
            ToggleRow(
                label = tr("Pokaż Kokpit na ekranie blokady/AOD", "Show Cockpit on lock screen/AOD"),
                checked = state.aodEnabled,
                onCheckedChange = { enabled ->
                    if (enabled &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    onAodEnabledChange(enabled)
                },
                accent = Tokens.Blue,
                description = tr(
                    "Podczas jazdy pokazuje prędkość/moc/wspomaganie na ekranie blokady i AOD, żeby telefon mógł faktycznie usnąć zamiast trzymać ekran cały czas włączony - realna oszczędność baterii, w odróżnieniu od zwykłego jasnego ekranu. Działa jako \"teraz odtwarzane\" (udajemy odtwarzacz muzyki) - to jedyny publiczny mechanizm Androida, który daje taki efekt, więc wygląda jak muzyka, nie jak dedykowany kokpit. Wymaga zgody na powiadomienia - bez niej nic się nie pokaże. Nie każdy telefon pokazuje odtwarzane media na AOD.",
                    "While riding, shows speed/power/assist on the lock screen and AOD so the phone can actually sleep instead of keeping the screen lit the whole time - a real battery saving, unlike a plain bright screen. It works as \"now playing\" (pretending to be a music player) - the only public Android mechanism that achieves this, so it looks like music, not a dedicated cockpit. Requires notification permission - without it, nothing will show. Not every phone displays now-playing media on its AOD.",
                ),
            )
            // Widoczne zawsze (niezależnie od stanu przełącznika wyżej), nie tylko gdy AOD włączone -
            // na wyraźne życzenie użytkownika, żeby opcja była widoczna/konfigurowalna z wyprzedzeniem.
            HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
            ToggleRow(
                label = tr("Sterowanie +/- na ekranie blokady", "+/- controls on lock screen"),
                checked = state.aodAssistControlsEnabled,
                onCheckedChange = onAodAssistControlsChange,
                accent = Tokens.Blue,
                description = tr(
                    "Dodaje przyciski poprzedni/następny (jako +/-) do zmiany wspomagania bezpośrednio z ekranu blokady. Uwaga: telefon zgłasza się wtedy jako aktywnie odtwarzający media, co może kolidować z prawdziwą muzyką (słuchawki Bluetooth, przyciski multimedialne).",
                    "Adds previous/next buttons (as +/-) to change assist level directly from the lock screen. Note: the phone then reports itself as actively playing media, which can conflict with real music (Bluetooth headphones, media buttons).",
                ),
            )
        }
    }
}
