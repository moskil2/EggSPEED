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
    onToggleTestMode: () -> Unit,
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
        TokenCard(borderColor = Tokens.WhiteBorder) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(tr("Tryb testowy", "Test mode", de = "Testmodus", fr = "Mode test", es = "Modo de prueba", pt = "Modo de teste", it = "Modalità test", nl = "Testmodus", sv = "Testläge", cs = "Testovací režim", sk = "Testovací režim"), fontFamily = Manrope, fontSize = 14.sp, color = Tokens.TextPrimary)
                    Text(
                        tr(
                            "Wymusza skrajne wartości na Kokpicie - test układu ekranu",
                            "Forces extreme values on the Cockpit - screen layout test",
                            de = "Erzwingt Extremwerte im Cockpit - Test des Bildschirmlayouts",
                            fr = "Force des valeurs extrêmes sur le Cockpit - test de la mise en page de l'écran",
                            es = "Fuerza valores extremos en el Cockpit - prueba del diseño de pantalla",
                            pt = "Força valores extremos no Cockpit - teste de layout do ecrã",
                            it = "Forza valori estremi nel Cockpit - test del layout dello schermo",
                            nl = "Forceert extreme waarden op de Cockpit - test van het schermlayout",
                            sv = "Tvingar fram extremvärden på Cockpit - test av skärmlayout",
                            cs = "Vynucuje extrémní hodnoty v Cockpitu - test rozvržení obrazovky",
                            sk = "Vynucuje extrémne hodnoty v Cockpite - test rozloženia obrazovky",
                        ),
                        fontFamily = Manrope, fontSize = 11.sp, color = Tokens.TextSecondary,
                    )
                }
                Box(
                    modifier = Modifier
                        .background(if (state.testMode) Tokens.Blue else Tokens.Elevated, RoundedCornerShape(10.dp))
                        .clickable { onToggleTestMode() }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "TEST", fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                        color = if (state.testMode) Tokens.OnAccent else Tokens.TextPrimary,
                    )
                }
            }
        }

        CollapsibleInfoBanner(
            title = if (!connected) {
                tr("Połącz się, żeby zeskanować rejestry", "Connect to scan registers", de = "Verbinden, um Register zu scannen", fr = "Connectez-vous pour scanner les registres", es = "Conéctate para escanear los registros", pt = "Liga-te para escanear os registos", it = "Connettiti per scansionare i registri", nl = "Verbind om registers te scannen", sv = "Anslut för att skanna register", cs = "Připoj se pro skenování registrů", sk = "Pripoj sa na skenovanie registrov")
            } else {
                tr("O pełnym skanie rejestrów", "About the full register scan", de = "Über den vollständigen Registerscan", fr = "À propos du scan complet des registres", es = "Sobre el escaneo completo de registros", pt = "Sobre a verificação completa dos registos", it = "Informazioni sulla scansione completa dei registri", nl = "Over de volledige registerscan", sv = "Om den fullständiga registerskanningen", cs = "O úplném skenování registrů", sk = "O úplnom skenovaní registrov")
            },
            text = buildString {
                if (!connected) {
                    append(
                        tr(
                            "Połącz się na ekranie Połączenie, żeby zeskanować rejestry. ",
                            "Connect on the Connect screen to scan registers. ",
                            de = "Verbinde dich auf dem Bildschirm Verbindung, um Register zu scannen. ",
                            fr = "Connectez-vous sur l'écran Connexion pour scanner les registres. ",
                            es = "Conéctate en la pantalla Conexión para escanear los registros. ",
                            pt = "Liga-te no ecrã Ligação para escanear os registos. ",
                            it = "Connettiti nella schermata Connessione per scansionare i registri. ",
                            nl = "Verbind op het scherm Verbinding om registers te scannen. ",
                            sv = "Anslut på skärmen Anslutning för att skanna register. ",
                            cs = "Připoj se na obrazovce Připojení pro skenování registrů. ",
                            sk = "Pripoj sa na obrazovke Pripojenie na skenovanie registrov. ",
                        ),
                    )
                }
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
                        de = "Der vollständige Scan sendet Leseanfragen (Präfix 0x11, niemals 0x16 Schreiben) " +
                            "für ALLE Codes 0x00-0xFF und zeichnet diejenigen auf, die überhaupt geantwortet " +
                            "haben - auch außerhalb bekannter Register. Jeder Durchlauf fügt einen neuen Eintrag " +
                            "zur Historie unten hinzu, sodass du mehrere Scans hintereinander vergleichen " +
                            "kannst. Dauert etwa 30-50 Sekunden.",
                        fr = "Le scan complet envoie des requêtes de lecture (préfixe 0x11, jamais 0x16 " +
                            "d'écriture) pour TOUS les codes 0x00-0xFF et enregistre ceux qui ont répondu, même " +
                            "en dehors des registres connus. Chaque exécution ajoute une nouvelle entrée à " +
                            "l'historique ci-dessous, ce qui permet de comparer plusieurs scans d'affilée. " +
                            "Prend environ 30-50 secondes.",
                        es = "El escaneo completo envía solicitudes de lectura (prefijo 0x11, nunca 0x16 de " +
                            "escritura) para TODOS los códigos 0x00-0xFF y registra los que respondieron, " +
                            "incluso fuera de los registros conocidos. Cada ejecución añade una nueva entrada " +
                            "al historial de abajo, para que puedas comparar varios escaneos seguidos. Tarda " +
                            "unos 30-50 segundos.",
                        pt = "A verificação completa envia pedidos de leitura (prefixo 0x11, nunca 0x16 de " +
                            "escrita) para TODOS os códigos 0x00-0xFF e regista os que responderam - mesmo fora " +
                            "dos registos conhecidos. Cada execução adiciona uma nova entrada ao histórico " +
                            "abaixo, para que possas comparar várias verificações seguidas. Demora cerca de " +
                            "30-50 segundos.",
                        it = "La scansione completa invia richieste di lettura (prefisso 0x11, mai 0x16 di " +
                            "scrittura) per TUTTI i codici 0x00-0xFF e registra quelli che hanno risposto - " +
                            "anche al di fuori dei registri noti. Ogni esecuzione aggiunge una nuova voce alla " +
                            "cronologia sottostante, così puoi confrontare più scansioni consecutive. Richiede " +
                            "circa 30-50 secondi.",
                        nl = "De volledige scan verstuurt leesverzoeken (prefix 0x11, nooit 0x16 schrijven) " +
                            "voor ALLE codes 0x00-0xFF en registreert degene die reageerden - zelfs buiten " +
                            "bekende registers. Elke run voegt een nieuw item toe aan de geschiedenis " +
                            "hieronder, zodat je meerdere scans achter elkaar kunt vergelijken. Duurt ongeveer " +
                            "30-50 seconden.",
                        sv = "Den fullständiga skanningen skickar läsförfrågningar (prefix 0x11, aldrig 0x16 " +
                            "skrivning) för ALLA koder 0x00-0xFF och registrerar de som svarade - även utanför " +
                            "kända register. Varje körning lägger till en ny post i historiken " +
                            "nedan, så du kan jämföra flera skanningar i rad. Tar ca " +
                            "30-50 sekunder.",
                        cs = "Úplné skenování odesílá požadavky na čtení (prefix 0x11, nikdy 0x16 zápis) " +
                            "pro VŠECHNY kódy 0x00-0xFF a zaznamenává ty, které vůbec odpověděly - i mimo " +
                            "známé registry. Každé spuštění přidá nový záznam do historie " +
                            "níže, takže můžeš porovnat několik skenů za sebou. Trvá přibližně " +
                            "30-50 sekund.",
                        sk = "Úplné skenovanie odosiela požiadavky na čítanie (prefix 0x11, nikdy 0x16 zápis) " +
                            "pre VŠETKY kódy 0x00-0xFF a zaznamenáva tie, ktoré vôbec odpovedali - aj mimo " +
                            "známych registrov. Každé spustenie pridá nový záznam do histórie " +
                            "nižšie, takže môžeš porovnať niekoľko skenov za sebou. Trvá približne " +
                            "30-50 sekúnd.",
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
                    !connected -> tr("Skanuj wszystkie rejestry (wymaga połączenia)", "Scan all registers (requires connection)", de = "Alle Register scannen (Verbindung erforderlich)", fr = "Scanner tous les registres (connexion requise)", es = "Escanear todos los registros (requiere conexión)", pt = "Escanear todos os registos (requer ligação)", it = "Scansiona tutti i registri (richiede connessione)", nl = "Alle registers scannen (verbinding vereist)", sv = "Skanna alla register (kräver anslutning)", cs = "Skenovat všechny registry (vyžaduje připojení)", sk = "Skenovať všetky registre (vyžaduje pripojenie)")
                    scanning -> tr("Skanowanie… $scanProgress/256", "Scanning… $scanProgress/256", de = "Scanne… $scanProgress/256", fr = "Analyse… $scanProgress/256", es = "Escaneando… $scanProgress/256", pt = "A escanear… $scanProgress/256", it = "Scansione… $scanProgress/256", nl = "Scannen… $scanProgress/256", sv = "Skannar… $scanProgress/256", cs = "Skenuji… $scanProgress/256", sk = "Skenujem… $scanProgress/256")
                    else -> tr("Skanuj wszystkie rejestry (0x00-0xFF)", "Scan all registers (0x00-0xFF)", de = "Alle Register scannen (0x00-0xFF)", fr = "Scanner tous les registres (0x00-0xFF)", es = "Escanear todos los registros (0x00-0xFF)", pt = "Escanear todos os registos (0x00-0xFF)", it = "Scansiona tutti i registri (0x00-0xFF)", nl = "Alle registers scannen (0x00-0xFF)", sv = "Skanna alla register (0x00-0xFF)", cs = "Skenovat všechny registry (0x00-0xFF)", sk = "Skenovať všetky registre (0x00-0xFF)")
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
                        Toast.makeText(
                            context,
                            tr(lang, "Skopiowano do schowka", "Copied to clipboard", de = "In die Zwischenablage kopiert", fr = "Copié dans le presse-papiers", es = "Copiado al portapapeles", pt = "Copiado para a área de transferência", it = "Copiato negli appunti", nl = "Gekopieerd naar klembord", sv = "Kopierat till urklipp", cs = "Zkopírováno do schránky", sk = "Skopírované do schránky"),
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(tr("Kopiuj historię skanów", "Copy scan history", de = "Scanverlauf kopieren", fr = "Copier l'historique des scans", es = "Copiar historial de escaneos", pt = "Copiar histórico de verificações", it = "Copia cronologia scansioni", nl = "Scangeschiedenis kopiëren", sv = "Kopiera skanningshistorik", cs = "Kopírovat historii skenování", sk = "Kopírovať históriu skenovania"), fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Tokens.Blue)
            }
        }

        if (scanResults.isNotEmpty()) {
            MicroLabel(
                tr(
                    "Wyniki ostatniego skanu - rejestry, które odpowiedziały (${scanResults.size})",
                    "Latest scan results - registers that responded (${scanResults.size})",
                    de = "Ergebnisse des letzten Scans - Register, die geantwortet haben (${scanResults.size})",
                    fr = "Résultats du dernier scan - registres ayant répondu (${scanResults.size})",
                    es = "Resultados del último escaneo - registros que respondieron (${scanResults.size})",
                    pt = "Resultados da última verificação - registos que responderam (${scanResults.size})",
                    it = "Risultati dell'ultima scansione - registri che hanno risposto (${scanResults.size})",
                    nl = "Resultaten van laatste scan - registers die reageerden (${scanResults.size})",
                    sv = "Resultat från senaste skanningen - register som svarade (${scanResults.size})",
                    cs = "Výsledky posledního skenu - registry, které odpověděly (${scanResults.size})",
                    sk = "Výsledky posledného skenu - registre, ktoré odpovedali (${scanResults.size})",
                ),
            )
            TokenCard(borderColor = Tokens.WhiteBorder) {
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
                de = "Frühere Scans (der neueste oben ist „Ergebnisse des letzten Scans“) - vergleiche, welche Werte sich zwischen den Scans ändern.",
                fr = "Scans précédents (le plus récent ci-dessus est « Résultats du dernier scan ») - comparez les valeurs qui changent entre les scans.",
                es = "Escaneos anteriores (el más reciente arriba es «Resultados del último escaneo») - compara qué valores cambian entre escaneos.",
                pt = "Verificações anteriores (a mais recente acima é «Resultados da última verificação») - compara quais valores mudam entre verificações.",
                it = "Scansioni precedenti (la più recente sopra è «Risultati dell'ultima scansione») - confronta quali valori cambiano tra le scansioni.",
                nl = "Eerdere scans (de nieuwste hierboven is «Resultaten van laatste scan») - vergelijk welke waarden veranderen tussen scans.",
                sv = "Tidigare skanningar (den senaste ovan är «Resultat från senaste skanningen») - jämför vilka värden som ändras mellan skanningar.",
                cs = "Předchozí skeny (nejnovější nahoře je «Výsledky posledního skenu») - porovnej, které hodnoty se mezi skeny mění.",
                sk = "Predchádzajúce skeny (najnovší hore je «Výsledky posledného skenu») - porovnaj, ktoré hodnoty sa medzi skenmi menia.",
            ))
            fullScanHistory.dropLast(1).reversed().forEach { snap ->
                MicroLabel(
                    tr(
                        "Skan ${snap.index} (${snap.results.size} odpowiedzi)",
                        "Scan ${snap.index} (${snap.results.size} responses)",
                        de = "Scan ${snap.index} (${snap.results.size} Antworten)",
                        fr = "Scan ${snap.index} (${snap.results.size} réponses)",
                        es = "Escaneo ${snap.index} (${snap.results.size} respuestas)",
                        pt = "Verificação ${snap.index} (${snap.results.size} respostas)",
                        it = "Scansione ${snap.index} (${snap.results.size} risposte)",
                        nl = "Scan ${snap.index} (${snap.results.size} reacties)",
                        sv = "Skanning ${snap.index} (${snap.results.size} svar)",
                        cs = "Sken ${snap.index} (${snap.results.size} odpovědí)",
                        sk = "Sken ${snap.index} (${snap.results.size} odpovedí)",
                    ),
                )
                TokenCard(borderColor = Tokens.WhiteBorder) {
                    if (snap.results.isEmpty()) {
                        DiagRow(tr("brak odpowiedzi", "no response", de = "keine Antwort", fr = "aucune réponse", es = "sin respuesta", pt = "sem resposta", it = "nessuna risposta", nl = "geen reactie", sv = "inget svar", cs = "žádná odpověď", sk = "žiadna odpoveď"), "-", last = true)
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
 * Legenda TYLKO pewnych, nazwanych rejestrów - żadnych eksperymentalnych/niepewnych kandydatów
 * (te są tylko w wynikach pełnego skanu, do własnej interpretacji). Dwie grupy: 9 rejestrów
 * wyświetlacza znanych z oficjalnego źródła bbs-fw (`extcom.c`, OPCODE_BAFANG_DISPLAY_READ_*) -
 * odpowiadają niezależnie od wybranego firmware, bo to warstwa zgodności z fabrycznym
 * wyświetlaczem - oraz 4 bloki protokołu Bafang Configuration Tool (GEN/BAS/PAS/THR), które
 * bbs-fw świadomie NIE implementuje w tej warstwie (potwierdzone w `extcom.c` - stałe
 * OPCODE_BAFANG_TOOL_* są zdefiniowane, ale nigdzie nieużywane w żadnym switch/case). Własny stan
 * zwinięcia w jednej karcie (bez zagnieżdżonej karty w środku) - w przeciwieństwie do
 * ExpandableParamTile, którego content() renderuje się zawsze niezależnie od stanu rozwinięcia,
 * więc nie nadaje się do faktycznie zwijanej listy.
 */
@Composable
private fun RegisterLegend() {
    var expanded by remember { mutableStateOf(false) }
    TokenCard(
        modifier = Modifier.clickable { expanded = !expanded },
        contentPadding = 14.dp,
        borderColor = Tokens.WhiteBorder,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                tr("LEGENDA ZNANYCH REJESTRÓW", "KNOWN REGISTER LEGEND", de = "LEGENDE BEKANNTER REGISTER", fr = "LÉGENDE DES REGISTRES CONNUS", es = "LEYENDA DE REGISTROS CONOCIDOS", pt = "LEGENDA DE REGISTOS CONHECIDOS", it = "LEGENDA REGISTRI NOTI", nl = "LEGENDA BEKENDE REGISTERS", sv = "FÖRKLARING TILL KÄNDA REGISTER", cs = "LEGENDA ZNÁMÝCH REGISTRŮ", sk = "LEGENDA ZNÁMYCH REGISTROV"), fontFamily = Manrope, fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp, letterSpacing = 1.sp, color = Tokens.TextPrimary, modifier = Modifier.weight(1f),
            )
            Text(if (expanded) "▲" else "▼", fontFamily = Manrope, fontSize = 16.sp, color = Tokens.Emerald)
        }
        if (expanded) {
            Spacer(Modifier.height(10.dp))
            Text(
                tr(
                    "Rejestry wyświetlacza znane z oficjalnego źródła bbs-fw (extcom.c) - odpowiadają NIEZALEŻNIE od wybranego firmware, bo bbs-fw naśladuje fabryczny protokół wyświetlacza dla zgodności wstecznej:",
                    "Display registers known from bbs-fw's official source (extcom.c) - respond REGARDLESS of the selected firmware, since bbs-fw mimics the factory display protocol for backward compatibility:",
                    de = "Anzeige-Register, bekannt aus der offiziellen bbs-fw-Quelle (extcom.c) - antworten " +
                        "UNABHÄNGIG von der gewählten Firmware, da bbs-fw das werkseitige Anzeigeprotokoll zur " +
                        "Abwärtskompatibilität nachahmt:",
                    fr = "Registres d'affichage connus depuis la source officielle de bbs-fw (extcom.c) - " +
                        "répondent INDÉPENDAMMENT du firmware sélectionné, car bbs-fw imite le protocole " +
                        "d'affichage d'usine pour la rétrocompatibilité :",
                    es = "Registros de pantalla conocidos de la fuente oficial de bbs-fw (extcom.c) - " +
                        "responden INDEPENDIENTEMENTE del firmware seleccionado, ya que bbs-fw imita el " +
                        "protocolo de pantalla de fábrica por compatibilidad hacia atrás:",
                    pt = "Registos de visor conhecidos da fonte oficial do bbs-fw (extcom.c) - respondem " +
                        "INDEPENDENTEMENTE do firmware selecionado, uma vez que o bbs-fw imita o protocolo de " +
                        "visor de fábrica para compatibilidade com versões anteriores:",
                    it = "Registri del display noti dalla fonte ufficiale di bbs-fw (extcom.c) - rispondono " +
                        "INDIPENDENTEMENTE dal firmware selezionato, poiché bbs-fw imita il protocollo del " +
                        "display di fabbrica per la retrocompatibilità:",
                    nl = "Displayregisters bekend uit de officiële bbs-fw-bron (extcom.c) - reageren ONGEACHT " +
                        "de geselecteerde firmware, omdat bbs-fw het fabrieksdisplayprotocol nabootst voor " +
                        "achterwaartse compatibiliteit:",
                    sv = "Displayregister kända från bbs-fw:s officiella källa (extcom.c) - svarar OAVSETT " +
                        "vilken firmware som valts, eftersom bbs-fw efterliknar fabrikens displayprotokoll för " +
                        "bakåtkompatibilitet:",
                    cs = "Registry displeje známé z oficiálního zdroje bbs-fw (extcom.c) - odpovídají BEZ OHLEDU " +
                        "na zvolený firmware, protože bbs-fw napodobuje tovární protokol displeje kvůli " +
                        "zpětné kompatibilitě:",
                    sk = "Registre displeja známe z oficiálneho zdroja bbs-fw (extcom.c) - odpovedajú BEZ OHĽADU " +
                        "na zvolený firmvér, pretože bbs-fw napodobňuje továrenský protokol displeja kvôli " +
                        "spätnej kompatibilite:",
                ),
                fontFamily = Manrope, fontSize = 11.sp, color = Tokens.TextSecondary,
            )
            Spacer(Modifier.height(8.dp))
            DiagRow("0x08", tr("Status", "Status", de = "Status", fr = "Statut", es = "Estado", pt = "Estado", it = "Stato", nl = "Status", sv = "Status", cs = "Stav", sk = "Stav"))
            DiagRow("0x0A", tr("Prąd - używany w Kokpicie", "Current - used in the Cockpit", de = "Strom - im Cockpit verwendet", fr = "Courant - utilisé dans le Cockpit", es = "Corriente - usada en el Cockpit", pt = "Corrente - usada no Cockpit", it = "Corrente - usata nel Cockpit", nl = "Stroom - gebruikt in de Cockpit", sv = "Ström - används i Cockpit", cs = "Proud - používaný v Cockpitu", sk = "Prúd - používaný v Cockpite"))
            DiagRow("0x11", tr("Bateria [%] - używana w Kokpicie", "Battery [%] - used in the Cockpit", de = "Batterie [%] - im Cockpit verwendet", fr = "Batterie [%] - utilisée dans le Cockpit", es = "Batería [%] - usada en el Cockpit", pt = "Bateria [%] - usada no Cockpit", it = "Batteria [%] - usata nel Cockpit", nl = "Batterij [%] - gebruikt in de Cockpit", sv = "Batteri [%] - används i Cockpit", cs = "Baterie [%] - používaná v Cockpitu", sk = "Batéria [%] - používaná v Cockpite"))
            DiagRow("0x20", tr("Prędkość [rpm] - używana w Kokpicie", "Speed [rpm] - used in the Cockpit", de = "Geschwindigkeit [U/min] - im Cockpit verwendet", fr = "Vitesse [tr/min] - utilisée dans le Cockpit", es = "Velocidad [rpm] - usada en el Cockpit", pt = "Velocidade [rpm] - usada no Cockpit", it = "Velocità [rpm] - usata nel Cockpit", nl = "Snelheid [rpm] - gebruikt in de Cockpit", sv = "Hastighet [rpm] - används i Cockpit", cs = "Rychlost [rpm] - používaná v Cockpitu", sk = "Rýchlosť [rpm] - používaná v Cockpite"))
            DiagRow("0x21", tr("Nieznany - nawet autor bbs-fw go tak nazwał (Unknown1)", "Unknown - even the bbs-fw author named it that (Unknown1)", de = "Unbekannt - selbst der bbs-fw-Autor hat es so genannt (Unknown1)", fr = "Inconnu - même l'auteur de bbs-fw l'a appelé ainsi (Unknown1)", es = "Desconocido - incluso el autor de bbs-fw lo llamó así (Unknown1)", pt = "Desconhecido - até o autor do bbs-fw lhe chamou isso (Unknown1)", it = "Sconosciuto - persino l'autore di bbs-fw lo ha chiamato così (Unknown1)", nl = "Onbekend - zelfs de bbs-fw-auteur noemde het zo (Unknown1)", sv = "Okänd - även bbs-fw-författaren kallade det så (Unknown1)", cs = "Neznámý - i autor bbs-fw ho tak pojmenoval (Unknown1)", sk = "Neznámy - aj autor bbs-fw ho tak nazval (Unknown1)"))
            DiagRow("0x22", tr("Zasięg", "Range", de = "Reichweite", fr = "Autonomie", es = "Autonomía", pt = "Autonomia", it = "Autonomia", nl = "Bereik", sv = "Räckvidd", cs = "Dojezd", sk = "Dojazd"))
            DiagRow("0x24", tr("Kalorie", "Calories", de = "Kalorien", fr = "Calories", es = "Calorías", pt = "Calorias", it = "Calorie", nl = "Calorieën", sv = "Kalorier", cs = "Kalorie", sk = "Kalórie"))
            DiagRow("0x25", tr("Nieznany - nawet autor bbs-fw go tak nazwał (Unknown3)", "Unknown - even the bbs-fw author named it that (Unknown3)", de = "Unbekannt - selbst der bbs-fw-Autor hat es so genannt (Unknown3)", fr = "Inconnu - même l'auteur de bbs-fw l'a appelé ainsi (Unknown3)", es = "Desconocido - incluso el autor de bbs-fw lo llamó así (Unknown3)", pt = "Desconhecido - até o autor do bbs-fw lhe chamou isso (Unknown3)", it = "Sconosciuto - persino l'autore di bbs-fw lo ha chiamato così (Unknown3)", nl = "Onbekend - zelfs de bbs-fw-auteur noemde het zo (Unknown3)", sv = "Okänd - även bbs-fw-författaren kallade det så (Unknown3)", cs = "Neznámý - i autor bbs-fw ho tak pojmenoval (Unknown3)", sk = "Neznámy - aj autor bbs-fw ho tak nazval (Unknown3)"))
            DiagRow("0x31", tr("W ruchu (moving)", "Moving", de = "In Bewegung (moving)", fr = "En mouvement (moving)", es = "En movimiento (moving)", pt = "Em movimento (moving)", it = "In movimento (moving)", nl = "In beweging (moving)", sv = "I rörelse (moving)", cs = "V pohybu (moving)", sk = "V pohybe (moving)"), last = true)
            Spacer(Modifier.height(14.dp))
            Text(
                tr(
                    "Rejestry protokołu konfiguracji Bafang Configuration Tool (osobny format):",
                    "Bafang Configuration Tool protocol registers (separate format):",
                    de = "Protokollregister des Bafang Configuration Tool (separates Format):",
                    fr = "Registres du protocole Bafang Configuration Tool (format distinct) :",
                    es = "Registros del protocolo Bafang Configuration Tool (formato separado):",
                    pt = "Registos do protocolo Bafang Configuration Tool (formato separado):",
                    it = "Registri del protocollo Bafang Configuration Tool (formato separato):",
                    nl = "Registers van het Bafang Configuration Tool-protocol (apart formaat):",
                    sv = "Register för Bafang Configuration Tool-protokollet (separat format):",
                    cs = "Registry protokolu Bafang Configuration Tool (samostatný formát):",
                    sk = "Registre protokolu Bafang Configuration Tool (samostatný formát):",
                ),
                fontFamily = Manrope, fontSize = 11.sp, color = Tokens.TextSecondary,
            )
            Spacer(Modifier.height(8.dp))
            DiagRow("0x51", tr("GEN - blok informacji ogólnych (producent/model/HW/FW/napięcie/prąd)", "GEN - general info block (manufacturer/model/HW/FW/voltage/current)", de = "GEN - Block mit allgemeinen Informationen (Hersteller/Modell/HW/FW/Spannung/Strom)", fr = "GEN - bloc d'informations générales (fabricant/modèle/HW/FW/tension/courant)", es = "GEN - bloque de información general (fabricante/modelo/HW/FW/voltaje/corriente)", pt = "GEN - bloco de informação geral (fabricante/modelo/HW/FW/tensão/corrente)", it = "GEN - blocco informazioni generali (produttore/modello/HW/FW/tensione/corrente)", nl = "GEN - blok algemene informatie (fabrikant/model/HW/FW/spanning/stroom)", sv = "GEN - block med allmän information (tillverkare/modell/HW/FW/spänning/ström)", cs = "GEN - blok obecných informací (výrobce/model/HW/FW/napětí/proud)", sk = "GEN - blok všeobecných informácií (výrobca/model/HW/FW/napätie/prúd)"))
            DiagRow("0x52", tr("BAS - blok ustawień Basic", "BAS - Basic settings block", de = "BAS - Block der Basic-Einstellungen", fr = "BAS - bloc des réglages Basic", es = "BAS - bloque de ajustes Basic", pt = "BAS - bloco de definições Basic", it = "BAS - blocco impostazioni Basic", nl = "BAS - blok Basic-instellingen", sv = "BAS - block med Basic-inställningar", cs = "BAS - blok nastavení Basic", sk = "BAS - blok nastavenia Basic"))
            DiagRow("0x53", tr("PAS - blok ustawień Pedal Assist", "PAS - Pedal Assist settings block", de = "PAS - Block der Pedal-Assist-Einstellungen", fr = "PAS - bloc des réglages Pedal Assist", es = "PAS - bloque de ajustes Pedal Assist", pt = "PAS - bloco de definições Pedal Assist", it = "PAS - blocco impostazioni Pedal Assist", nl = "PAS - blok Pedal Assist-instellingen", sv = "PAS - block med Pedal Assist-inställningar", cs = "PAS - blok nastavení Pedal Assist", sk = "PAS - blok nastavenia Pedal Assist"))
            DiagRow("0x54", tr("THR - blok ustawień Throttle", "THR - Throttle settings block", de = "THR - Block der Throttle-Einstellungen", fr = "THR - bloc des réglages Throttle", es = "THR - bloque de ajustes Throttle", pt = "THR - bloco de definições Throttle", it = "THR - blocco impostazioni Throttle", nl = "THR - blok Throttle-instellingen", sv = "THR - block med Throttle-inställningar", cs = "THR - blok nastavení Throttle", sk = "THR - blok nastavenia Throttle"), last = true)
            Spacer(Modifier.height(10.dp))
            Text(
                tr(
                    "Na bbs-fw pełny skan zwykle odpowie tylko na 9 rejestrów wyświetlacza powyżej (0x08-0x31) - bbs-fw " +
                        "NIE implementuje odczytu 0x51-0x54 w warstwie zgodności z wyświetlaczem (to inny, osobny " +
                        "protokół, obsługiwany na zakładkach bbs-fw System/Assist Levels).",
                    "On bbs-fw the full scan will usually only get responses from the 9 display registers above " +
                        "(0x08-0x31) - bbs-fw does NOT implement reading 0x51-0x54 in its display-compatibility layer " +
                        "(that's a separate protocol, handled on the bbs-fw System/Assist Levels tabs).",
                    de = "Bei bbs-fw erhält der vollständige Scan normalerweise nur Antworten von den 9 oben " +
                        "genannten Anzeige-Registern (0x08-0x31) - bbs-fw implementiert das Lesen von 0x51-0x54 " +
                        "in seiner Anzeige-Kompatibilitätsschicht NICHT (das ist ein separates Protokoll, das in " +
                        "den bbs-fw-Tabs System/Assist Levels behandelt wird).",
                    fr = "Sur bbs-fw, le scan complet ne recevra généralement des réponses que des 9 registres " +
                        "d'affichage ci-dessus (0x08-0x31) - bbs-fw n'implémente PAS la lecture de 0x51-0x54 " +
                        "dans sa couche de compatibilité d'affichage (il s'agit d'un protocole distinct, géré " +
                        "dans les onglets bbs-fw System/Assist Levels).",
                    es = "En bbs-fw, el escaneo completo normalmente solo obtendrá respuestas de los 9 " +
                        "registros de pantalla anteriores (0x08-0x31) - bbs-fw NO implementa la lectura de " +
                        "0x51-0x54 en su capa de compatibilidad de pantalla (es un protocolo separado, " +
                        "gestionado en las pestañas bbs-fw System/Assist Levels).",
                    pt = "No bbs-fw, a verificação completa normalmente só obterá respostas dos 9 registos de " +
                        "visor acima (0x08-0x31) - o bbs-fw NÃO implementa a leitura de 0x51-0x54 na sua camada " +
                        "de compatibilidade com o visor (esse é um protocolo separado, tratado nos separadores " +
                        "bbs-fw System/Assist Levels).",
                    it = "Su bbs-fw, la scansione completa di solito otterrà risposte solo dai 9 registri del " +
                        "display sopra (0x08-0x31) - bbs-fw NON implementa la lettura di 0x51-0x54 nel suo " +
                        "livello di compatibilità con il display (si tratta di un protocollo separato, gestito " +
                        "nelle schede bbs-fw System/Assist Levels).",
                    nl = "Bij bbs-fw krijgt de volledige scan meestal alleen reacties van de 9 bovenstaande " +
                        "displayregisters (0x08-0x31) - bbs-fw implementeert het lezen van 0x51-0x54 NIET in " +
                        "zijn displaycompatibiliteitslaag (dat is een apart protocol, afgehandeld op de bbs-fw " +
                        "System/Assist Levels-tabbladen).",
                    sv = "På bbs-fw kommer den fullständiga skanningen vanligtvis bara få svar från de 9 " +
                        "displayregistren ovan (0x08-0x31) - bbs-fw implementerar INTE läsning av 0x51-0x54 i " +
                        "sitt displaykompatibilitetslager (det är ett separat protokoll, hanterat på bbs-fw " +
                        "System/Assist Levels-flikarna).",
                    cs = "Na bbs-fw úplný sken obvykle dostane odpovědi jen z 9 výše uvedených " +
                        "registrů displeje (0x08-0x31) - bbs-fw NEIMPLEMENTUJE čtení 0x51-0x54 ve své " +
                        "vrstvě kompatibility s displejem (jedná se o samostatný protokol, obsluhovaný na kartách " +
                        "bbs-fw System/Assist Levels).",
                    sk = "Na bbs-fw úplný sken zvyčajne dostane odpovede iba z 9 vyššie uvedených " +
                        "registrov displeja (0x08-0x31) - bbs-fw NEIMPLEMENTUJE čítanie 0x51-0x54 vo svojej " +
                        "vrstve kompatibility s displejom (ide o samostatný protokol, obsluhovaný na kartách " +
                        "bbs-fw System/Assist Levels).",
                ),
                fontFamily = Manrope, fontSize = 11.sp, color = Tokens.TextTertiary,
            )
        }
    }
}

private fun buildScanHistoryText(history: List<ScanSnapshot>, lang: AppLanguage): String = buildString {
    appendLine(tr(lang, "EggSPEED - Diagnostyka (historia skanow 0x00-0xFF)", "EggSPEED - Diagnostics (scan history 0x00-0xFF)", de = "EggSPEED - Diagnose (Scanverlauf 0x00-0xFF)", fr = "EggSPEED - Diagnostic (historique des scans 0x00-0xFF)", es = "EggSPEED - Diagnóstico (historial de escaneos 0x00-0xFF)", pt = "EggSPEED - Diagnóstico (histórico de verificações 0x00-0xFF)", it = "EggSPEED - Diagnostica (cronologia scansioni 0x00-0xFF)", nl = "EggSPEED - Diagnostiek (scangeschiedenis 0x00-0xFF)", sv = "EggSPEED - Diagnostik (skanningshistorik 0x00-0xFF)", cs = "EggSPEED - Diagnostika (historie skenu 0x00-0xFF)", sk = "EggSPEED - Diagnostika (história skenov 0x00-0xFF)"))
    appendLine()
    history.forEach { snap ->
        appendLine("[${tr(lang, "Skan", "Scan", de = "Scan", fr = "Scan", es = "Escaneo", pt = "Verificação", it = "Scansione", nl = "Scan", sv = "Skanning", cs = "Sken", sk = "Sken")} ${snap.index} - ${snap.results.size} ${tr(lang, "odpowiedzi", "responses", de = "Antworten", fr = "réponses", es = "respuestas", pt = "respostas", it = "risposte", nl = "reacties", sv = "svar", cs = "odpovědí", sk = "odpovedí")}]")
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
