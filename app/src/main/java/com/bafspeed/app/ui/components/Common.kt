package com.bafspeed.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens

/** Karta w stylu briefu: #0d1013, radius 18, border 6% white (domyślnie, nadpisywalny). */
@Composable
fun TokenCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 18.dp,
    // Pozwala zwęzić ramkę tylko w pionie (np. Monitoring), bez ruszania paddingu poziomego -
    // domyślnie równy contentPadding, więc dotychczasowe wywołania z jednym parametrem się nie zmieniają.
    contentPaddingVertical: Dp = contentPadding,
    borderColor: Color = Tokens.Border,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .background(Tokens.Card, RoundedCornerShape(18.dp))
            .padding(horizontal = contentPadding, vertical = contentPaddingVertical),
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

/** Stałe przyciski Read (zielony)/Write (czerwony) - zastępują dawny pływający pasek "Masz niezapisane zmiany". */
@Composable
fun ReadWriteButtons(onRead: () -> Unit, onWrite: () -> Unit, enabled: Boolean, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier.weight(1f)
                .background(if (enabled) Tokens.Emerald else Tokens.Elevated, RoundedCornerShape(14.dp))
                .let { if (enabled) it.clickable { onRead() } else it }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) { Text(tr("Odczytaj", "Read", de = "Lesen", fr = "Lire", es = "Leer", pt = "Ler", it = "Leggi", nl = "Lezen", sv = "Läs", cs = "Číst", sk = "Čítať", da = "Læs", ru = "Читать"), fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (enabled) Tokens.OnAccent else Tokens.TextTertiary) }
        Box(
            modifier = Modifier.weight(1f)
                .background(if (enabled) Tokens.Red else Tokens.Elevated, RoundedCornerShape(14.dp))
                .let { if (enabled) it.clickable { onWrite() } else it }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) { Text(tr("Zapisz", "Write", de = "Schreiben", fr = "Écrire", es = "Escribir", pt = "Escrever", it = "Scrivi", nl = "Schrijven", sv = "Skriv", cs = "Zapsat", sk = "Zapísať", da = "Skriv", ru = "Записать"), fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (enabled) Color.White else Tokens.TextTertiary) }
    }
}

/**
 * Krótka informacja pod przyciskami Odczytaj/Zapisz - Monitoring i/lub AOD (ekran blokady), gdy
 * były włączone, wstrzymują się na czas edycji configu (współdzielona magistrala), żeby nie
 * kolidować z odczytem/zapisem - patrz AppViewModel.setConfigScreenOpen/syncDisplayPolling. Osobna
 * linijka na każdą aktywną funkcję, bo obie mogą być włączone naraz niezależnie od siebie.
 */
@Composable
fun TelemetryPausedNotice(monitoringActive: Boolean, aodActive: Boolean, modifier: Modifier = Modifier) {
    if (!monitoringActive && !aodActive) return
    Column(modifier.fillMaxWidth().padding(top = 6.dp)) {
        if (monitoringActive) {
            Text(
                tr("Monitoring wstrzymany na czas edycji", "Monitoring paused while editing", de = "Monitoring während der Bearbeitung pausiert", fr = "Monitoring en pause pendant l'édition", es = "Monitoreo en pausa durante la edición", pt = "Monitoring pausado durante a edição", it = "Monitoring in pausa durante la modifica", nl = "Monitoring gepauzeerd tijdens bewerken", sv = "Monitoring pausad under redigering", cs = "Monitoring pozastaven během úprav", sk = "Monitoring pozastavený počas úprav", da = "Monitorering sat på pause under redigering", ru = "Мониторинг приостановлен во время редактирования"),
                fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Tokens.Red,
            )
        }
        if (aodActive) {
            Text(
                tr("Ekran blokady (AOD) wstrzymany na czas edycji", "Lock screen display (AOD) paused while editing", de = "Sperrbildschirm-Anzeige (AOD) während der Bearbeitung pausiert", fr = "Affichage écran verrouillé (AOD) en pause pendant l'édition", es = "Pantalla de bloqueo (AOD) en pausa durante la edición", pt = "Ecrã de bloqueio (AOD) pausado durante a edição", it = "Schermata di blocco (AOD) in pausa durante la modifica", nl = "Vergrendelscherm (AOD) gepauzeerd tijdens bewerken", sv = "Låsskärmsvisning (AOD) pausad under redigering", cs = "Zobrazení na zamykací obrazovce (AOD) pozastaveno během úprav", sk = "Zobrazenie na zamykacej obrazovke (AOD) pozastavené počas úprav", da = "Låseskærmsvisning (AOD) sat på pause under redigering", ru = "Экран блокировки (AOD) приостановлен во время редактирования"),
                fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Tokens.Red,
            )
        }
    }
}

/** Mikro-etykieta sekcji: uppercase 11sp, letter-spacing. */
@Composable
fun MicroLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 1.2.sp,
        color = Tokens.TextPrimary,
    )
}
