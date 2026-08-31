package com.bafspeed.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bafspeed.app.ConnectionStatus
import com.bafspeed.app.FirmwareType
import com.bafspeed.app.UiState
import com.bafspeed.app.protocol.Telemetry
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.ui.components.CollapsibleMicroLabel
import com.bafspeed.app.ui.components.ExpandableParamTile
import com.bafspeed.app.ui.components.MicroLabel
import com.bafspeed.app.ui.components.PlainSlider
import com.bafspeed.app.ui.components.PreviewBanner
import com.bafspeed.app.ui.components.StepBtn
import com.bafspeed.app.ui.components.TokenCard
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens

@Composable
fun CalibrationScreen(
    state: UiState,
    telemetry: Telemetry,
    onFactorChange: (Double) -> Unit,
    onVoltageOffsetChange: (Double) -> Unit,
    onSpeedFactorChange: (Double) -> Unit,
    onStartDisplay: () -> Unit,
    onStopDisplay: () -> Unit,
) {
    // Podgląd napięcia poniżej potrzebuje żywej telemetrii - bez tego, wejście tu bez
    // wcześniejszego pobytu na Kokpicie zostawiało telemetry na domyślnych 0,0 (patrz DashboardScreen).
    DisposableEffect(Unit) {
        onStartDisplay()
        onDispose { onStopDisplay() }
    }
    val connected = state.connection == ConnectionStatus.CONNECTED
    val factor = state.currentCalibrationFactor
    val declaredLimitA = state.basicOrDefault.currentLimit.toDouble()
    val voltageOffsetV = state.voltageCalibrationOffsetV
    val nominalVoltageV = state.nominalPackVoltage.toDouble()
    val speedFactor = state.speedCalibrationFactor
    val unitLabel = state.units.label
    // Wartość referencyjna na sztywno 30 (nie 30km/h przeliczane na mph) - sama liczba jest stała,
    // zmienia się tylko etykieta jednostki pod aktualne ustawienie (patrz SettingsScreen/SpeedUnit).
    val referenceSpeed = 30.0

    Column(
        Modifier
            .fillMaxSize()
            .background(Tokens.Bg)
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = 16.dp)),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        PreviewBanner(
            tr(
                "Dla sterowników z shunt modem odczyt prądu bywa zaniżony/zawyżony. Współczynnik X " +
                    "przemnaża WYŁĄCZNIE wartość wyświetlaną w aplikacji (prąd i moc) - nic nie jest " +
                    "zapisywane ani zmieniane w sterowniku. 1,00× = brak kalibracji.",
                "For controllers with a shunt mod, the current reading is sometimes under/overstated. The X " +
                    "factor multiplies ONLY the value shown in the app (current and power) - nothing is " +
                    "saved or changed in the controller. 1.00× = no calibration.",
                de = "Bei Steuergeräten mit Shunt-Mod ist der Stromwert manchmal zu niedrig/zu hoch. Der Faktor X " +
                    "multipliziert AUSSCHLIESSLICH den in der App angezeigten Wert (Strom und Leistung) - im Steuergerät " +
                    "wird nichts gespeichert oder geändert. 1,00× = keine Kalibrierung.",
                fr = "Sur les contrôleurs avec un shunt mod, la lecture du courant est parfois sous/surestimée. Le facteur X " +
                    "multiplie UNIQUEMENT la valeur affichée dans l'application (courant et puissance) - rien n'est " +
                    "enregistré ni modifié dans le contrôleur. 1,00× = pas de calibration.",
                es = "En controladores con shunt mod, la lectura de corriente a veces es baja/alta. El factor X " +
                    "multiplica ÚNICAMENTE el valor mostrado en la app (corriente y potencia) - no se guarda ni cambia " +
                    "nada en el controlador. 1,00× = sin calibración.",
                pt = "Em controladores com shunt mod, a leitura de corrente por vezes é subestimada/sobrestimada. O fator X " +
                    "multiplica APENAS o valor mostrado na app (corrente e potência) - nada é guardado nem alterado no " +
                    "controlador. 1,00× = sem calibração.",
                it = "Nei controller con shunt mod, la lettura della corrente a volte è sottostimata/sovrastimata. Il fattore X " +
                    "moltiplica SOLO il valore mostrato nell'app (corrente e potenza) - non viene salvato né modificato nulla " +
                    "nel controller. 1,00× = nessuna calibrazione.",
                nl = "Bij controllers met een shunt mod is de stroommeting soms te laag/te hoog. De factor X vermenigvuldigt " +
                    "ALLEEN de waarde die in de app wordt getoond (stroom en vermogen) - er wordt niets opgeslagen of gewijzigd " +
                    "in de controller. 1,00× = geen kalibratie.",
                sv = "På styrenheter med shunt mod är strömavläsningen ibland för låg/hög. Faktor X multiplicerar " +
                    "ENDAST värdet som visas i appen (ström och effekt) - inget sparas eller ändras " +
                    "i styrenheten. 1,00× = ingen kalibrering.",
                cs = "U řadičů se shunt modem bývá odečet proudu podhodnocený/nadhodnocený. Faktor X " +
                    "násobí VÝHRADNĚ hodnotu zobrazenou v aplikaci (proud a výkon) - v řadiči se " +
                    "nic neukládá ani nemění. 1,00× = žádná kalibrace.",
                sk = "Pri radičoch so shunt modom býva odčítanie prúdu podhodnotené/nadhodnotené. Faktor X " +
                    "násobí VÝHRADNE hodnotu zobrazenú v aplikácii (prúd a výkon) - v radiči sa " +
                    "nič neukladá ani nemení. 1,00× = žiadna kalibrácia.",
            ),
            collapsible = true,
        )

        ExpandableParamTile(
            label = tr("Współczynnik kalibracji prądu", "Current calibration factor", de = "Stromkalibrierungsfaktor", fr = "Facteur de calibration du courant", es = "Factor de calibración de corriente", pt = "Fator de calibração de corrente", it = "Fattore di calibrazione corrente", nl = "Kalibratiefactor stroom", sv = "Kalibreringsfaktor för ström", cs = "Kalibrační faktor proudu", sk = "Kalibračný faktor prúdu"),
            valueLabel = String.format("%.2f×", factor),
            description = tr(
                "Współczynnik mnoży surowy odczyt prądu z kontrolera przed wyświetleniem go w Kokpicie " +
                    "(prąd i moc). Przydaje się, gdy sterownik ma shunt mod i pokazuje zaniżoną/zawyżoną wartość - " +
                    "nic w tym sterowniku się nie zmienia.",
                "The factor multiplies the raw current reading from the controller before it's shown in the " +
                    "Cockpit (current and power). Useful when the controller has a shunt mod and reports an " +
                    "under/overstated value - nothing changes in the controller itself.",
                de = "Der Faktor multipliziert den rohen Stromwert des Steuergeräts, bevor er im Cockpit " +
                    "(Strom und Leistung) angezeigt wird. Nützlich, wenn das Steuergerät einen Shunt-Mod hat und einen " +
                    "zu niedrigen/hohen Wert meldet - im Steuergerät selbst ändert sich nichts.",
                fr = "Le facteur multiplie la lecture brute du courant du contrôleur avant qu'elle ne soit affichée dans le " +
                    "Cockpit (courant et puissance). Utile lorsque le contrôleur a un shunt mod et signale une valeur " +
                    "sous/surestimée - rien ne change dans le contrôleur lui-même.",
                es = "El factor multiplica la lectura bruta de corriente del controlador antes de mostrarla en el " +
                    "Cockpit (corriente y potencia). Útil cuando el controlador tiene un shunt mod y reporta un valor " +
                    "bajo/alto - no cambia nada en el propio controlador.",
                pt = "O fator multiplica a leitura bruta de corrente do controlador antes de ser mostrada no Cockpit " +
                    "(corrente e potência). Útil quando o controlador tem shunt mod e reporta um valor subestimado/sobrestimado " +
                    "- nada muda no próprio controlador.",
                it = "Il fattore moltiplica la lettura grezza della corrente dal controller prima che venga mostrata nel " +
                    "Cockpit (corrente e potenza). Utile quando il controller ha uno shunt mod e riporta un valore " +
                    "sottostimato/sovrastimato - non cambia nulla nel controller stesso.",
                nl = "De factor vermenigvuldigt de ruwe stroommeting van de controller voordat deze in de Cockpit wordt " +
                    "getoond (stroom en vermogen). Handig wanneer de controller een shunt mod heeft en een te lage/hoge " +
                    "waarde meldt - er verandert niets in de controller zelf.",
                sv = "Faktorn multiplicerar den råa strömavläsningen från styrenheten innan den visas i " +
                    "Cockpit (ström och effekt). Användbart när styrenheten har en shunt mod och rapporterar en " +
                    "för låg/hög värde - inget ändras i själva styrenheten.",
                cs = "Faktor násobí surový odečet proudu z řadiče, než se zobrazí v " +
                    "Cockpitu (proud a výkon). Užitečné, když má řadič shunt mod a hlásí " +
                    "podhodnocenou/nadhodnocenou hodnotu - v samotném řadiči se nic nemění.",
                sk = "Faktor násobí surový odčítaný prúd z radiča, kým sa zobrazí v " +
                    "Cockpite (prúd a výkon). Užitočné, keď má radič shunt mod a hlási " +
                    "podhodnotenú/nadhodnotenú hodnotu - v samotnom radiči sa nič nemení.",
            ),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                StepBtn("-", true) { onFactorChange((factor - 0.01).coerceIn(0.01, 3.0)) }
                Spacer(Modifier.width(10.dp))
                Box(Modifier.weight(1f)) {
                    PlainSlider(
                        value = factor.toFloat(),
                        range = 0.01f..3.00f,
                        accent = Tokens.Amber,
                        onValueChange = { onFactorChange(it.toDouble()) },
                    )
                }
                Spacer(Modifier.width(10.dp))
                StepBtn("+", true) { onFactorChange((factor + 0.01).coerceIn(0.01, 3.0)) }
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Tokens.Elevated, RoundedCornerShape(12.dp))
                    .clickable { onFactorChange(1.0) }
                    .padding(vertical = 10.dp),
            ) {
                Text(
                    tr(
                        "Resetuj do 1,00× (brak kalibracji)", "Reset to 1.00× (no calibration)",
                        de = "Auf 1,00× zurücksetzen (keine Kalibrierung)",
                        fr = "Réinitialiser à 1,00× (pas de calibration)",
                        es = "Restablecer a 1,00× (sin calibración)",
                        pt = "Repor para 1,00× (sem calibração)",
                        it = "Ripristina a 1,00× (nessuna calibrazione)",
                        nl = "Terugzetten naar 1,00× (geen kalibratie)",
                        sv = "Återställ till 1,00× (ingen kalibrering)",
                        cs = "Obnovit na 1,00× (žádná kalibrace)",
                        sk = "Obnoviť na 1,00× (žiadna kalibrácia)",
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Tokens.TextPrimary,
                )
            }
        }

        CollapsibleMicroLabel(tr("Podgląd prądu", "Current preview", de = "Stromvorschau", fr = "Aperçu du courant", es = "Vista previa de corriente", pt = "Pré-visualização de corrente", it = "Anteprima corrente", nl = "Voorbeeld stroom", sv = "Förhandsvisning av ström", cs = "Náhled proudu", sk = "Náhľad prúdu")) {
            TokenCard(borderColor = Tokens.WhiteBorder, contentPaddingVertical = 8.dp) {
                InfoRow(tr("Limit prądu (zadeklarowany)", "Current limit (declared)", de = "Stromlimit (deklariert)", fr = "Limite de courant (déclarée)", es = "Límite de corriente (declarado)", pt = "Limite de corrente (declarado)", it = "Limite di corrente (dichiarato)", nl = "Stroomlimiet (opgegeven)", sv = "Strömgräns (angiven)", cs = "Omezení proudu (uvedené)", sk = "Obmedzenie prúdu (uvedené)"), String.format("%.1f A", declaredLimitA))
                HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                InfoRow(tr("Po kalibracji", "After calibration", de = "Nach Kalibrierung", fr = "Après calibration", es = "Tras la calibración", pt = "Após calibração", it = "Dopo la calibrazione", nl = "Na kalibratie", sv = "Efter kalibrering", cs = "Po kalibraci", sk = "Po kalibrácii"), String.format("%.1f A", declaredLimitA * factor))
                HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                // Maksymalne napiecie (cellCount x 4,2V - "Gorny limit naladowania" w zakladce
                // Bateria), nie nominalne - moc szczytowa jest liczona przy pelnym naladowaniu.
                InfoRow(tr("Moc po kalibracji", "Power after calibration", de = "Leistung nach Kalibrierung", fr = "Puissance après calibration", es = "Potencia tras la calibración", pt = "Potência após calibração", it = "Potenza dopo la calibrazione", nl = "Vermogen na kalibratie", sv = "Effekt efter kalibrering", cs = "Výkon po kalibraci", sk = "Výkon po kalibrácii"), String.format("%.0f W", state.cellCount * 4.2 * declaredLimitA * factor))
            }
        }

        ExpandableParamTile(
            label = tr("Korekta napięcia", "Voltage correction", de = "Spannungskorrektur", fr = "Correction de tension", es = "Corrección de voltaje", pt = "Correção de tensão", it = "Correzione tensione", nl = "Spanningscorrectie", sv = "Spänningskorrigering", cs = "Korekce napětí", sk = "Korekcia napätia"),
            valueLabel = "${if (voltageOffsetV > 0) "+" else ""}${String.format("%.1f", voltageOffsetV)} V",
            description = if (state.firmwareType == FirmwareType.BBS_FW) {
                tr(
                    "Napięcie w Kokpicie to realny pomiar z ADC sterownika (bbs-fw udostępnia go pod rejestrem " +
                        "0x24, w przeciwieństwie do fabrycznego firmware, gdzie ten rejestr jest martwy) - jeśli " +
                        "mimo to odbiega od pomiaru multimetrem (np. dryf ADC), skoryguj różnicę tutaj. Korekta " +
                        "doliczana jest do każdego kolejnego odczytu napięcia (i przez to też do mocy).",
                    "Voltage in the Cockpit is a real ADC measurement from the controller (bbs-fw exposes it via " +
                        "register 0x24, unlike the factory firmware where that register is dead) - if it still " +
                        "deviates from a multimeter reading (e.g. ADC drift), correct the difference here. The " +
                        "correction is added to every subsequent voltage reading (and therefore to power too).",
                    de = "Die Spannung im Cockpit ist eine reale ADC-Messung des Steuergeräts (bbs-fw stellt sie über " +
                        "Register 0x24 bereit, anders als beim werkseitigen Firmware, wo dieses Register tot ist) - wenn sie " +
                        "trotzdem von einer Multimeter-Messung abweicht (z. B. ADC-Drift), korrigiere den Unterschied hier. Die " +
                        "Korrektur wird zu jedem weiteren Spannungswert addiert (und damit auch zur Leistung).",
                    fr = "La tension dans le Cockpit est une mesure ADC réelle du contrôleur (bbs-fw l'expose via le " +
                        "registre 0x24, contrairement au firmware d'origine où ce registre est mort) - si elle diffère " +
                        "quand même d'une mesure au multimètre (dérive ADC par ex.), corrigez la différence ici. La " +
                        "correction est ajoutée à chaque lecture de tension suivante (et donc aussi à la puissance).",
                    es = "El voltaje en el Cockpit es una medición ADC real del controlador (bbs-fw la expone mediante el " +
                        "registro 0x24, a diferencia del firmware de fábrica donde ese registro está muerto) - si aun así " +
                        "difiere de una medición con multímetro (p. ej. deriva del ADC), corrige la diferencia aquí. La " +
                        "corrección se suma a cada lectura de voltaje posterior (y por tanto también a la potencia).",
                    pt = "A tensão no Cockpit é uma medição real do ADC do controlador (o bbs-fw expõe-a através do registo " +
                        "0x24, ao contrário do firmware de fábrica, onde este registo está morto) - se ainda assim divergir " +
                        "de uma medição com multímetro (por ex. desvio do ADC), corrige a diferença aqui. A correção é " +
                        "somada a cada leitura de tensão seguinte (e por isso também à potência).",
                    it = "La tensione nel Cockpit è una misurazione ADC reale del controller (bbs-fw la espone tramite il " +
                        "registro 0x24, a differenza del firmware di fabbrica dove tale registro è inattivo) - se differisce " +
                        "comunque da una misurazione con multimetro (ad es. deriva dell'ADC), correggi la differenza qui. La " +
                        "correzione viene aggiunta a ogni successiva lettura di tensione (e quindi anche alla potenza).",
                    nl = "De spanning in de Cockpit is een echte ADC-meting van de controller (bbs-fw stelt deze beschikbaar " +
                        "via register 0x24, in tegenstelling tot de fabrieksfirmware waar dit register inactief is) - als " +
                        "deze toch afwijkt van een multimetermeting (bijv. ADC-drift), corrigeer het verschil hier. De " +
                        "correctie wordt toegepast op elke volgende spanningsmeting (en dus ook op het vermogen).",
                    sv = "Spänningen i Cockpit är en verklig ADC-mätning från styrenheten (bbs-fw exponerar den " +
                        "via register 0x24, till skillnad från fabriksfirmware där registret är dött) - om " +
                        "den ändå avviker från en multimetermätning (t.ex. ADC-drift), korrigera skillnaden här. " +
                        "Korrigeringen läggs till varje efterföljande spänningsavläsning (och därmed även effekten).",
                    cs = "Napětí v Cockpitu je reálné ADC měření z řadiče (bbs-fw ho zpřístupňuje přes " +
                        "registr 0x24, na rozdíl od továrního firmwaru, kde je tento registr mrtvý) - pokud " +
                        "přesto neodpovídá měření multimetrem (např. drift ADC), oprav rozdíl zde. " +
                        "Korekce se přičítá ke každému dalšímu odečtu napětí (a tím i k výkonu).",
                    sk = "Napätie v Cockpite je reálne ADC meranie z radiča (bbs-fw ho sprístupňuje cez " +
                        "register 0x24, na rozdiel od továrenského firmvéru, kde je tento register mŕtvy) - ak " +
                        "napriek tomu nezodpovedá meraniu multimetrom (napr. drift ADC), oprav rozdiel tu. " +
                        "Korekcia sa pripočíta ku každému ďalšiemu odčítaniu napätia (a tým aj k výkonu).",
                )
            } else {
                tr(
                    "Napięcie w Kokpicie jest estymowane z % baterii (rejestr 0x24 martwy na fabrycznym " +
                        "firmware) - jeśli odbiega od realnego pomiaru (multimetr), skoryguj różnicę tutaj. Korekta " +
                        "doliczana jest do każdego kolejnego odczytu napięcia (i przez to też do mocy).",
                    "Voltage in the Cockpit is estimated from the battery % (register 0x24 is dead on the factory " +
                        "firmware) - if it deviates from a real measurement (multimeter), correct the difference here. " +
                        "The correction is added to every subsequent voltage reading (and therefore to power too).",
                    de = "Die Spannung im Cockpit wird aus dem Batterie-% geschätzt (Register 0x24 ist beim werkseitigen " +
                        "Firmware tot) - wenn sie von einer realen Messung (Multimeter) abweicht, korrigiere den Unterschied hier. " +
                        "Die Korrektur wird zu jedem weiteren Spannungswert addiert (und damit auch zur Leistung).",
                    fr = "La tension dans le Cockpit est estimée à partir du % de batterie (le registre 0x24 est mort sur le " +
                        "firmware d'origine) - si elle diffère d'une mesure réelle (multimètre), corrigez la différence ici. " +
                        "La correction est ajoutée à chaque lecture de tension suivante (et donc aussi à la puissance).",
                    es = "El voltaje en el Cockpit se estima a partir del % de batería (el registro 0x24 está muerto en el " +
                        "firmware de fábrica) - si difiere de una medición real (multímetro), corrige la diferencia aquí. " +
                        "La corrección se suma a cada lectura de voltaje posterior (y por tanto también a la potencia).",
                    pt = "A tensão no Cockpit é estimada a partir da % da bateria (o registo 0x24 está morto no firmware " +
                        "de fábrica) - se divergir de uma medição real (multímetro), corrige a diferença aqui. A correção " +
                        "é somada a cada leitura de tensão seguinte (e por isso também à potência).",
                    it = "La tensione nel Cockpit è stimata dalla % della batteria (il registro 0x24 è inattivo nel " +
                        "firmware di fabbrica) - se differisce da una misurazione reale (multimetro), correggi la " +
                        "differenza qui. La correzione viene aggiunta a ogni successiva lettura di tensione (e quindi anche alla potenza).",
                    nl = "De spanning in de Cockpit wordt geschat op basis van het batterij-% (register 0x24 is inactief " +
                        "op de fabrieksfirmware) - als deze afwijkt van een echte meting (multimeter), corrigeer het " +
                        "verschil hier. De correctie wordt toegepast op elke volgende spanningsmeting (en dus ook op het vermogen).",
                    sv = "Spänningen i Cockpit uppskattas utifrån batteri-% (register 0x24 är dött på " +
                        "fabriksfirmware) - om den avviker från en verklig mätning (multimeter), korrigera " +
                        "skillnaden här. Korrigeringen läggs till varje efterföljande spänningsavläsning (och därmed även effekten).",
                    cs = "Napětí v Cockpitu je odhadováno z % baterie (registr 0x24 je v továrním " +
                        "firmwaru mrtvý) - pokud se liší od reálného měření (multimetr), oprav " +
                        "rozdíl zde. Korekce se přičítá ke každému dalšímu odečtu napětí (a tím i k výkonu).",
                    sk = "Napätie v Cockpite je odhadované z % batérie (register 0x24 je v továrenskom " +
                        "firmvéri mŕtvy) - ak sa líši od reálneho merania (multimeter), oprav " +
                        "rozdiel tu. Korekcia sa pripočíta ku každému ďalšiemu odčítaniu napätia (a tým aj k výkonu).",
                )
            },
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                StepBtn("-", true) { onVoltageOffsetChange((voltageOffsetV - 0.1).coerceIn(-5.0, 5.0)) }
                Spacer(Modifier.width(10.dp))
                Box(Modifier.weight(1f)) {
                    PlainSlider(
                        value = voltageOffsetV.toFloat(),
                        range = -5.0f..5.0f,
                        accent = Tokens.Blue,
                        onValueChange = { onVoltageOffsetChange(it.toDouble()) },
                    )
                }
                Spacer(Modifier.width(10.dp))
                StepBtn("+", true) { onVoltageOffsetChange((voltageOffsetV + 0.1).coerceIn(-5.0, 5.0)) }
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Tokens.Elevated, RoundedCornerShape(12.dp))
                    .clickable { onVoltageOffsetChange(0.0) }
                    .padding(vertical = 10.dp),
            ) {
                Text(
                    tr(
                        "Resetuj do 0,0 V (brak korekty)", "Reset to 0.0 V (no correction)",
                        de = "Auf 0,0 V zurücksetzen (keine Korrektur)",
                        fr = "Réinitialiser à 0,0 V (pas de correction)",
                        es = "Restablecer a 0,0 V (sin corrección)",
                        pt = "Repor para 0,0 V (sem correção)",
                        it = "Ripristina a 0,0 V (nessuna correzione)",
                        nl = "Terugzetten naar 0,0 V (geen correctie)",
                        sv = "Återställ till 0,0 V (ingen korrigering)",
                        cs = "Obnovit na 0,0 V (žádná korekce)",
                        sk = "Obnoviť na 0,0 V (žiadna korekcia)",
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Tokens.TextPrimary,
                )
            }
        }

        CollapsibleMicroLabel(tr("Podgląd napięcia", "Voltage preview", de = "Spannungsvorschau", fr = "Aperçu de la tension", es = "Vista previa de voltaje", pt = "Pré-visualização de tensão", it = "Anteprima tensione", nl = "Voorbeeld spanning", sv = "Förhandsvisning av spänning", cs = "Náhled napětí", sk = "Náhľad napätia")) {
            TokenCard(borderColor = Tokens.WhiteBorder, contentPaddingVertical = 8.dp) {
                // Offline: pokazujemy ostatnie znane napięcie sprzed rozłączenia (state.lastKnownVoltageV),
                // a jeśli go nigdy nie było (0,0 - apka jeszcze się nie łączyła) - estymatę z napięcia
                // nominalnego pakietu (np. 52V dla 14S), żeby podgląd korekty miał sens nawet offline.
                val readV = when {
                    connected -> telemetry.estimatedVoltageV
                    state.lastKnownVoltageV > 0.0 -> state.lastKnownVoltageV
                    else -> nominalVoltageV
                }
                InfoRow(tr("Napięcie odczytane", "Voltage read", de = "Gemessene Spannung", fr = "Tension lue", es = "Voltaje leído", pt = "Tensão lida", it = "Tensione letta", nl = "Gemeten spanning", sv = "Uppmätt spänning", cs = "Naměřené napětí", sk = "Namerané napätie"), String.format("%.1f V", readV))
                HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                InfoRow(tr("Napięcie po korekcie", "Voltage after correction", de = "Spannung nach Korrektur", fr = "Tension après correction", es = "Voltaje tras la corrección", pt = "Tensão após correção", it = "Tensione dopo la correzione", nl = "Spanning na correctie", sv = "Spänning efter korrigering", cs = "Napětí po korekci", sk = "Napätie po korekcii"), String.format("%.1f V", readV + voltageOffsetV))
            }
        }

        ExpandableParamTile(
            label = tr("Kalibracja prędkości", "Speed calibration", de = "Geschwindigkeitskalibrierung", fr = "Calibration de la vitesse", es = "Calibración de velocidad", pt = "Calibração de velocidade", it = "Calibrazione velocità", nl = "Snelheidskalibratie", sv = "Hastighetskalibrering", cs = "Kalibrace rychlosti", sk = "Kalibrácia rýchlosti"),
            valueLabel = String.format("%.2f×", speedFactor),
            description = tr(
                "Współczynnik mnoży surowy odczyt prędkości z kontrolera przed wyświetleniem jej w Kokpicie " +
                    "i na wykresach w Monitoringu. Przydaje się, gdy prędkość jest zaniżona/zawyżona (np. błędny " +
                    "obwód koła albo czujnik prędkości) - nic w tym sterowniku się nie zmienia.",
                "The factor multiplies the raw speed reading from the controller before it's shown in the " +
                    "Cockpit and on the Monitoring charts. Useful when the speed is under/overstated (e.g. wrong " +
                    "wheel circumference or speed sensor) - nothing changes in the controller itself.",
                de = "Der Faktor multipliziert den rohen Geschwindigkeitswert des Steuergeräts, bevor er im Cockpit " +
                    "und in den Monitoring-Diagrammen angezeigt wird. Nützlich, wenn die Geschwindigkeit zu niedrig/hoch " +
                    "angezeigt wird (z. B. falscher Radumfang oder Geschwindigkeitssensor) - im Steuergerät ändert sich nichts.",
                fr = "Le facteur multiplie la lecture brute de vitesse du contrôleur avant qu'elle ne soit affichée dans le " +
                    "Cockpit et sur les graphiques du Monitoring. Utile lorsque la vitesse est sous/surestimée (mauvaise " +
                    "circonférence de roue ou capteur de vitesse par ex.) - rien ne change dans le contrôleur lui-même.",
                es = "El factor multiplica la lectura bruta de velocidad del controlador antes de mostrarla en el " +
                    "Cockpit y en los gráficos de Monitoring. Útil cuando la velocidad es baja/alta (p. ej. circunferencia " +
                    "de rueda o sensor de velocidad incorrectos) - no cambia nada en el propio controlador.",
                pt = "O fator multiplica a leitura bruta de velocidade do controlador antes de ser mostrada no Cockpit " +
                    "e nos gráficos de Monitoring. Útil quando a velocidade é subestimada/sobrestimada (por ex. circunferência " +
                    "de roda ou sensor de velocidade errados) - nada muda no próprio controlador.",
                it = "Il fattore moltiplica la lettura grezza della velocità dal controller prima che venga mostrata nel " +
                    "Cockpit e nei grafici di Monitoring. Utile quando la velocità è sottostimata/sovrastimata (ad es. " +
                    "circonferenza ruota o sensore di velocità errati) - non cambia nulla nel controller stesso.",
                nl = "De factor vermenigvuldigt de ruwe snelheidsmeting van de controller voordat deze in de Cockpit en " +
                    "op de Monitoring-grafieken wordt getoond. Handig wanneer de snelheid te laag/hoog is (bijv. verkeerde " +
                    "wielomtrek of snelheidssensor) - er verandert niets in de controller zelf.",
                sv = "Faktorn multiplicerar den råa hastighetsavläsningen från styrenheten innan den visas i " +
                    "Cockpit och i Monitoring-diagrammen. Användbart när hastigheten är för låg/hög (t.ex. fel " +
                    "hjulomkrets eller hastighetssensor) - inget ändras i själva styrenheten.",
                cs = "Faktor násobí surový odečet rychlosti z řadiče, než se zobrazí v " +
                    "Cockpitu a v grafech Monitoringu. Užitečné, když je rychlost podhodnocená/nadhodnocená (např. " +
                    "špatný obvod kola nebo snímač rychlosti) - v samotném řadiči se nic nemění.",
                sk = "Faktor násobí surovú odčítanú rýchlosť z radiča, kým sa zobrazí v " +
                    "Cockpite a v grafoch Monitoringu. Užitočné, keď je rýchlosť podhodnotená/nadhodnotená (napr. " +
                    "nesprávny obvod kolesa alebo snímač rýchlosti) - v samotnom radiči sa nič nemení.",
            ),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                StepBtn("-", true) { onSpeedFactorChange((speedFactor - 0.01).coerceIn(0.50, 2.0)) }
                Spacer(Modifier.width(10.dp))
                Box(Modifier.weight(1f)) {
                    PlainSlider(
                        value = speedFactor.toFloat(),
                        range = 0.50f..2.00f,
                        accent = Tokens.Amber,
                        onValueChange = { onSpeedFactorChange(it.toDouble()) },
                    )
                }
                Spacer(Modifier.width(10.dp))
                StepBtn("+", true) { onSpeedFactorChange((speedFactor + 0.01).coerceIn(0.50, 2.0)) }
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Tokens.Elevated, RoundedCornerShape(12.dp))
                    .clickable { onSpeedFactorChange(1.0) }
                    .padding(vertical = 10.dp),
            ) {
                Text(
                    tr(
                        "Resetuj do 1,00× (brak kalibracji)", "Reset to 1.00× (no calibration)",
                        de = "Auf 1,00× zurücksetzen (keine Kalibrierung)",
                        fr = "Réinitialiser à 1,00× (pas de calibration)",
                        es = "Restablecer a 1,00× (sin calibración)",
                        pt = "Repor para 1,00× (sem calibração)",
                        it = "Ripristina a 1,00× (nessuna calibrazione)",
                        nl = "Terugzetten naar 1,00× (geen kalibratie)",
                        sv = "Återställ till 1,00× (ingen kalibrering)",
                        cs = "Obnovit na 1,00× (žádná kalibrace)",
                        sk = "Obnoviť na 1,00× (žiadna kalibrácia)",
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Tokens.TextPrimary,
                )
            }
        }

        CollapsibleMicroLabel(tr("Podgląd prędkości", "Speed preview", de = "Geschwindigkeitsvorschau", fr = "Aperçu de la vitesse", es = "Vista previa de velocidad", pt = "Pré-visualização de velocidade", it = "Anteprima velocità", nl = "Voorbeeld snelheid", sv = "Förhandsvisning av hastighet", cs = "Náhled rychlosti", sk = "Náhľad rýchlosti")) {
            TokenCard(borderColor = Tokens.WhiteBorder, contentPaddingVertical = 8.dp) {
                InfoRow(tr("Prędkość przed kalibracją", "Speed before calibration", de = "Geschwindigkeit vor Kalibrierung", fr = "Vitesse avant calibration", es = "Velocidad antes de calibrar", pt = "Velocidade antes da calibração", it = "Velocità prima della calibrazione", nl = "Snelheid vóór kalibratie", sv = "Hastighet före kalibrering", cs = "Rychlost před kalibrací", sk = "Rýchlosť pred kalibráciou"), String.format("%.1f %s", referenceSpeed, unitLabel))
                HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                InfoRow(tr("Prędkość po kalibracji", "Speed after calibration", de = "Geschwindigkeit nach Kalibrierung", fr = "Vitesse après calibration", es = "Velocidad tras calibrar", pt = "Velocidade após calibração", it = "Velocità dopo la calibrazione", nl = "Snelheid na kalibratie", sv = "Hastighet efter kalibrering", cs = "Rychlost po kalibraci", sk = "Rýchlosť po kalibrácii"), String.format("%.1f %s", referenceSpeed * speedFactor, unitLabel))
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    // Padding zaciesniety (bylo 9dp, potem 5dp) - kafelki PODGLAD maja byc kompaktowe, bez zmiany
    // rozmiaru czcionki (patrz TokenCard.contentPaddingVertical przy wywolaniach tych kafelkow).
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontFamily = Manrope, fontSize = 14.sp, color = Tokens.TextPrimary, modifier = Modifier.weight(1f))
        Text(value, fontFamily = Sora, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Tokens.TextPrimary)
    }
}
