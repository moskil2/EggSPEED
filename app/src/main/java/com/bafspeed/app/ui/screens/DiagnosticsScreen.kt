package com.bafspeed.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bafspeed.app.ConnectionStatus
import com.bafspeed.app.UiState
import com.bafspeed.app.i18n.AppLanguage
import com.bafspeed.app.i18n.LocalAppLanguage
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.protocol.ScanResult
import com.bafspeed.app.protocol.ScanSnapshot
import com.bafspeed.app.ui.components.MicroLabel
import com.bafspeed.app.ui.components.PreviewBanner
import com.bafspeed.app.ui.components.TokenCard
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens

private val WhiteBorder = Color(0x59FFFFFF)

/**
 * Zakladka "Diagnostyka" - pelny skan rejestrow odczytu 0x00-0xFF, z historia kolejnych skanow
 * (kazde uruchomienie dopisuje nowy wpis, nie nadpisuje poprzedniego - patrz
 * DisplayStateMachine.fullScanHistory) i eksportem do schowka. Zbudowana na wzorze pelnej listy
 * opcode'ow z github.com/danielnilsson9/bbs-fw (plik extcom.c) - bbs-fw jest INNYM firmware i nie
 * gwarantuje tego samego przypisania znaczen do rejestrow na fabrycznym (zamknietym) firmware
 * Bafang, stad podejscie eksperymentalne.
 */
@Composable
fun DiagnosticsScreen(
    state: UiState,
    scanResults: List<ScanResult>,
    scanProgress: Int,
    scanning: Boolean,
    fullScanHistory: List<ScanSnapshot>,
    onStartScan: () -> Unit,
) {
    val connected = state.connection == ConnectionStatus.CONNECTED
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val lang = LocalAppLanguage.current

    Column(
        Modifier
            .fillMaxSize()
            .background(Tokens.Bg)
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = 16.dp)),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        CollapsibleInfoBanner(
            title = if (!connected) tr("Połącz się, żeby zeskanować rejestry", "Connect to scan registers") else tr("O pełnym skanie rejestrów", "About the full register scan"),
            text = buildString {
                if (!connected) append(tr("Połącz się na ekranie Połączenie, żeby zeskanować rejestry. ", "Connect on the Connect screen to scan registers. "))
                append(
                    tr(
                        "Pełny skan wysyła żądania odczytu (prefiks 0x11, nigdy 0x16 zapisu) dla WSZYSTKICH " +
                            "kodów 0x00-0xFF i zapisuje te, które w ogóle odpowiedziały - także spoza znanych " +
                            "rejestrów. Każde uruchomienie dopisuje nowy wpis do historii poniżej, więc możesz " +
                            "porównać kilka skanów z rzędu. Zajmuje ok. 30-50 sekund.",
                        "The full scan sends read requests (prefix 0x11, never 0x16 write) for ALL " +
                            "codes 0x00-0xFF and records those that responded at all - even outside known " +
                            "registers. Each run adds a new entry to the history below, so you can " +
                            "compare several scans in a row. Takes about 30-50 seconds.",
                    ),
                )
            },
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Tokens.Card, RoundedCornerShape(15.dp))
                .let { if (connected && !scanning) it.clickable { onStartScan() } else it }
                .padding(vertical = 15.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                when {
                    !connected -> tr("Skanuj wszystkie rejestry (wymaga połączenia)", "Scan all registers (requires connection)")
                    scanning -> tr("Skanowanie… $scanProgress/256", "Scanning… $scanProgress/256")
                    else -> tr("Skanuj wszystkie rejestry (0x00-0xFF)", "Scan all registers (0x00-0xFF)")
                },
                fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                color = if (connected && !scanning) Tokens.Blue else Tokens.TextTertiary,
            )
        }

        if (fullScanHistory.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Tokens.Elevated, RoundedCornerShape(12.dp))
                    .clickable {
                        clipboard.setText(AnnotatedString(buildScanHistoryText(fullScanHistory, lang)))
                        Toast.makeText(context, tr(lang, "Skopiowano do schowka", "Copied to clipboard"), Toast.LENGTH_SHORT).show()
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(tr("Kopiuj historię skanów", "Copy scan history"), fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Tokens.Blue)
            }
        }

        if (scanResults.isNotEmpty()) {
            MicroLabel(tr("Wyniki ostatniego skanu - rejestry, które odpowiedziały (${scanResults.size})", "Latest scan results - registers that responded (${scanResults.size})"))
            TokenCard(borderColor = WhiteBorder) {
                scanResults.forEachIndexed { i, r ->
                    DiagRow(
                        "0x${r.opcode.toString(16).padStart(2, '0').uppercase()}" +
                            if (r.neededChecksum) " (3B)" else " (2B)",
                        r.bytes.joinToString(", "),
                        last = i == scanResults.lastIndex,
                    )
                }
            }
        }

        if (fullScanHistory.size > 1) {
            Spacer(Modifier.height(4.dp))
            PreviewBanner(tr(
                "Poprzednie skany (najnowszy najniższy powyżej to \"Wyniki ostatniego skanu\") - porównaj które wartości się zmieniają między skanami.",
                "Previous scans (the newest one above is \"Latest scan results\") - compare which values change between scans.",
            ))
            fullScanHistory.dropLast(1).reversed().forEach { snap ->
                MicroLabel(tr("Skan ${snap.index} (${snap.results.size} odpowiedzi)", "Scan ${snap.index} (${snap.results.size} responses)"))
                TokenCard(borderColor = WhiteBorder) {
                    if (snap.results.isEmpty()) {
                        DiagRow(tr("brak odpowiedzi", "no response"), "-", last = true)
                    } else {
                        snap.results.forEachIndexed { i, r ->
                            DiagRow(
                                "0x${r.opcode.toString(16).padStart(2, '0').uppercase()}" +
                                    if (r.neededChecksum) " (3B)" else " (2B)",
                                r.bytes.joinToString(", "),
                                last = i == snap.results.lastIndex,
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        RegisterLegend()
        Spacer(Modifier.height(8.dp))
    }
}

/**
 * Baner informacyjny (wizualnie jak PreviewBanner) z opcją zwinięcia - do dłuższych opisów,
 * które nie muszą zajmować miejsca na stałe. Nie używa [com.bafspeed.app.ui.components.PreviewBanner]
 * bezpośrednio, bo ten komponent nie ma własnego stanu rozwinięcia.
 */
@Composable
private fun CollapsibleInfoBanner(title: String, text: String) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0x1AF5A524), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0x33F5A524), RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded }
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(Tokens.Amber, RoundedCornerShape(4.dp)))
            Spacer(Modifier.size(10.dp))
            Text(title, fontFamily = Manrope, fontSize = 12.sp, color = Tokens.TextPrimary, modifier = Modifier.weight(1f))
            Text(if (expanded) "▲" else "▼", fontFamily = Manrope, fontSize = 12.sp, color = Tokens.TextTertiary)
        }
        if (expanded) {
            Spacer(Modifier.height(8.dp))
            Text(text, fontFamily = Manrope, fontSize = 12.sp, lineHeight = 16.sp, color = Tokens.TextPrimary)
        }
    }
}

/**
 * Legenda TYLKO pewnych, faktycznie używanych gdzie indziej w apce rejestrów - żadnych
 * eksperymentalnych/niepewnych kandydatów (te są tylko w wynikach pełnego skanu, do
 * własnej interpretacji). Własny stan zwinięcia w jednej karcie (bez zagnieżdżonej karty
 * w środku) - w przeciwieństwie do ExpandableParamTile, którego content() renderuje się
 * zawsze niezależnie od stanu rozwinięcia, więc nie nadaje się do faktycznie zwijanej listy.
 */
@Composable
private fun RegisterLegend() {
    var expanded by remember { mutableStateOf(false) }
    TokenCard(
        modifier = Modifier.clickable { expanded = !expanded },
        contentPadding = 14.dp,
        borderColor = WhiteBorder,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                tr("LEGENDA ZNANYCH REJESTRÓW", "KNOWN REGISTER LEGEND"), fontFamily = Manrope, fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp, letterSpacing = 1.sp, color = Tokens.TextPrimary, modifier = Modifier.weight(1f),
            )
            Text(if (expanded) "▲" else "▼", fontFamily = Manrope, fontSize = 16.sp, color = Tokens.Emerald)
        }
        if (expanded) {
            Spacer(Modifier.height(10.dp))
            DiagRow("0x0A", tr("Prąd - używany w Kokpicie", "Current - used in the Cockpit"))
            DiagRow("0x11", tr("Bateria [%] - używana w Kokpicie", "Battery [%] - used in the Cockpit"))
            DiagRow("0x20", tr("Prędkość [rpm] - używana w Kokpicie", "Speed [rpm] - used in the Cockpit"))
            DiagRow("0x51", tr("GEN - blok informacji ogólnych (producent/model/HW/FW/napięcie/prąd)", "GEN - general info block (manufacturer/model/HW/FW/voltage/current)"))
            DiagRow("0x52", tr("BAS - blok ustawień Basic", "BAS - Basic settings block"))
            DiagRow("0x53", tr("PAS - blok ustawień Pedal Assist", "PAS - Pedal Assist settings block"))
            DiagRow("0x54", tr("THR - blok ustawień Throttle", "THR - Throttle settings block"), last = true)
        }
    }
}

private fun buildScanHistoryText(history: List<ScanSnapshot>, lang: AppLanguage): String = buildString {
    appendLine(tr(lang, "EggSPEED - Diagnostyka (historia skanow 0x00-0xFF)", "EggSPEED - Diagnostics (scan history 0x00-0xFF)"))
    appendLine()
    history.forEach { snap ->
        appendLine("[${tr(lang, "Skan", "Scan")} ${snap.index} - ${snap.results.size} ${tr(lang, "odpowiedzi", "responses")}]")
        snap.results.forEach { r ->
            val opcodeHex = "0x${r.opcode.toString(16).padStart(2, '0').uppercase()}"
            val checksum = if (r.neededChecksum) " (3B)" else " (2B)"
            appendLine("$opcodeHex$checksum: ${r.bytes.joinToString(", ")}")
        }
        appendLine()
    }
}

@Composable
private fun DiagRow(label: String, value: String, last: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextPrimary, modifier = Modifier.weight(1f))
        Text(value, fontFamily = Sora, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Tokens.TextPrimary)
    }
    if (!last) HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
}
