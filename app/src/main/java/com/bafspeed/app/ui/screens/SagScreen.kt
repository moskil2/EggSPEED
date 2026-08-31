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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bafspeed.app.ConnectionStatus
import com.bafspeed.app.SagCalibrationPhase
import com.bafspeed.app.UiState
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.ui.components.ExpandableParamTile
import com.bafspeed.app.ui.components.MicroLabel
import com.bafspeed.app.ui.components.TokenCard
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens
import java.util.concurrent.TimeUnit

/**
 * Zakladka "SAG" - dwie wartosci: SAG orientacyjny (liczony pasywnie w tle podczas zwyklej jazdy, patrz
 * AppViewModel.sampleSagPassive) i wynik Pomiaru SAG (obciazenie kontrolowane, wiec dokladniejszy i
 * porownywalny miedzy pomiarami). Zadna z nich nie jest pomiarem rezystancji w sensie inzynierskim -
 * obie to pochodna prostego spadku napiecia (SAG / prad) wzgledem napiecia spoczynkowego.
 */
@Composable
fun SagScreen(state: UiState, onStart: () -> Unit, onCancel: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Tokens.Bg)
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = 16.dp)),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ExpandableParamTile(
            label = "SAG",
            valueLabel = "",
            descriptionColor = Tokens.TextPrimary,
            description = tr(
                "SAG to spadek napięcia baterii pod obciążeniem względem napięcia spoczynkowego. To NIE jest " +
                    "pomiar rezystancji w sensie inżynierskim - to prosta pochodna (SAG / prąd), licząca się tylko " +
                    "z napięcia i prądu, przydatna do porównań między pomiarami i do wykrywania pogarszania się pakietu w czasie.",
                "SAG is the battery voltage drop under load relative to its resting voltage. This is NOT an " +
                    "engineering-grade resistance measurement - it's a simple derived value (SAG / current), based only " +
                    "on voltage and current, useful for comparing measurements and spotting pack degradation over time.",
                de = "SAG ist der Spannungsabfall der Batterie unter Last im Vergleich zur Ruhespannung. Dies ist " +
                    "KEINE ingenieurmäßige Widerstandsmessung - es ist ein einfacher abgeleiteter Wert (SAG / " +
                    "Strom), der nur auf Spannung und Strom basiert, nützlich zum Vergleichen von Messungen und " +
                    "zum Erkennen der Alterung des Akkupacks über die Zeit.",
                fr = "Le SAG est la chute de tension de la batterie en charge par rapport à sa tension au repos. " +
                    "Ce n'est PAS une mesure de résistance de qualité technique - c'est une valeur dérivée simple " +
                    "(SAG / courant), basée uniquement sur la tension et le courant, utile pour comparer des " +
                    "mesures et détecter la dégradation du pack au fil du temps.",
                es = "El SAG es la caída de voltaje de la batería bajo carga respecto a su voltaje en reposo. " +
                    "Esto NO es una medición de resistencia de precisión técnica - es un valor derivado simple " +
                    "(SAG / corriente), basado solo en voltaje y corriente, útil para comparar mediciones y " +
                    "detectar la degradación del paquete con el tiempo.",
                pt = "SAG é a queda de tensão da bateria sob carga em relação à sua tensão em repouso. Isto NÃO " +
                    "é uma medição de resistência de nível técnico - é um valor derivado simples (SAG / " +
                    "corrente), baseado apenas em tensão e corrente, útil para comparar medições e detetar a " +
                    "degradação do pack ao longo do tempo.",
                it = "Il SAG è il calo di tensione della batteria sotto carico rispetto alla sua tensione a " +
                    "riposo. Questa NON è una misurazione di resistenza di livello ingegneristico - è un " +
                    "valore derivato semplice (SAG / corrente), basato solo su tensione e corrente, utile per " +
                    "confrontare le misurazioni e individuare il degrado del pacco nel tempo.",
                nl = "SAG is de spanningsval van de batterij onder belasting ten opzichte van de rustspanning. " +
                    "Dit is GEEN technische weerstandsmeting - het is een eenvoudige afgeleide waarde (SAG / " +
                    "stroom), uitsluitend gebaseerd op spanning en stroom, handig om metingen te vergelijken " +
                    "en degradatie van het pack in de loop van de tijd op te sporen.",
                sv = "SAG är batteriets spänningsfall under belastning i förhållande till vilospänningen. " +
                    "Detta är INGEN professionell resistansmätning - det är ett enkelt härlett värde (SAG / " +
                    "ström), baserat enbart på spänning och ström, användbart för att jämföra mätningar " +
                    "och upptäcka att batteripaketet försämras med tiden.",
                cs = "SAG je pokles napětí baterie pod zátěží vůči klidovému napětí. Toto NENÍ přesné " +
                    "inženýrské měření odporu - jde o jednoduchou odvozenou hodnotu (SAG / proud), " +
                    "založenou pouze na napětí a proudu, užitečnou pro porovnávání měření a odhalování " +
                    "degradace baterie v čase.",
                sk = "SAG je pokles napätia batérie pod záťažou voči pokojovému napätiu. Toto NIE JE " +
                    "presné inžinierske meranie odporu - ide o jednoduchú odvodenú hodnotu (SAG / prúd), " +
                    "založenú iba na napätí a prúde, užitočnú na porovnávanie meraní a odhaľovanie " +
                    "degradácie batérie v čase.",
            ),
        ) {}

        MicroLabel(tr("Jakość baterii wg SAG", "Battery quality by SAG", de = "Batteriequalität nach SAG", fr = "Qualité de la batterie selon le SAG", es = "Calidad de la batería según el SAG", pt = "Qualidade da bateria por SAG", it = "Qualità della batteria in base al SAG", nl = "Batterijkwaliteit volgens SAG", sv = "Batterikvalitet enligt SAG", cs = "Kvalita baterie podle SAG", sk = "Kvalita batérie podľa SAG"))
        TokenCard(borderColor = Tokens.WhiteBorder) {
            SagLegendRow(
                tr("≤ 2,0 V", "≤ 2.0 V", de = "≤ 2,0 V", fr = "≤ 2,0 V", es = "≤ 2,0 V", pt = "≤ 2,0 V", it = "≤ 2,0 V", nl = "≤ 2,0 V", sv = "≤ 2,0 V", cs = "≤ 2,0 V", sk = "≤ 2,0 V"),
                tr("Doskonała", "Excellent", de = "Ausgezeichnet", fr = "Excellente", es = "Excelente", pt = "Excelente", it = "Eccellente", nl = "Uitstekend", sv = "Utmärkt", cs = "Vynikající", sk = "Vynikajúca"),
                Tokens.Emerald,
            )
            SagLegendRow(
                tr("2,0 - 4,0 V", "2.0 - 4.0 V", de = "2,0 - 4,0 V", fr = "2,0 - 4,0 V", es = "2,0 - 4,0 V", pt = "2,0 - 4,0 V", it = "2,0 - 4,0 V", nl = "2,0 - 4,0 V", sv = "2,0 - 4,0 V", cs = "2,0 - 4,0 V", sk = "2,0 - 4,0 V"),
                tr("Wystarczająca do Twoich zastosowań", "Sufficient for your use", de = "Ausreichend für deine Anwendung", fr = "Suffisante pour votre usage", es = "Suficiente para tu uso", pt = "Suficiente para o teu uso", it = "Sufficiente per il tuo utilizzo", nl = "Voldoende voor jouw gebruik", sv = "Tillräcklig för din användning", cs = "Dostatečná pro vaše použití", sk = "Dostatočná pre vaše použitie"),
                Tokens.Emerald,
            )
            SagLegendRow(
                tr("4,0 - 6,0 V", "4.0 - 6.0 V", de = "4,0 - 6,0 V", fr = "4,0 - 6,0 V", es = "4,0 - 6,0 V", pt = "4,0 - 6,0 V", it = "4,0 - 6,0 V", nl = "4,0 - 6,0 V", sv = "4,0 - 6,0 V", cs = "4,0 - 6,0 V", sk = "4,0 - 6,0 V"),
                tr("Na pograniczu - warto obserwować", "Borderline - worth watching", de = "Grenzwertig - beobachten lohnt sich", fr = "Limite - à surveiller", es = "Límite - conviene vigilarlo", pt = "No limite - vale a pena monitorizar", it = "Al limite - vale la pena monitorare", nl = "Grensgeval - de moeite waard om te volgen", sv = "Gränsfall - värt att bevaka", cs = "Na hranici - stojí za sledování", sk = "Na hranici - oplatí sa sledovať"),
                Tokens.Amber,
            )
            SagLegendRow(
                tr("> 6,0 V", "> 6.0 V", de = "> 6,0 V", fr = "> 6,0 V", es = "> 6,0 V", pt = "> 6,0 V", it = "> 6,0 V", nl = "> 6,0 V", sv = "> 6,0 V", cs = "> 6,0 V", sk = "> 6,0 V"),
                tr("Zbyt niska jakość / uszkodzona", "Too low quality / damaged", de = "Zu geringe Qualität / beschädigt", fr = "Qualité trop faible / endommagée", es = "Calidad demasiado baja / dañada", pt = "Qualidade demasiado baixa / danificada", it = "Qualità troppo bassa / danneggiata", nl = "Kwaliteit te laag / beschadigd", sv = "För låg kvalitet / skadad", cs = "Příliš nízká kvalita / poškozeno", sk = "Príliš nízka kvalita / poškodené"),
                Tokens.Red,
                last = true,
            )
        }

        ExpandableParamTile(
            label = tr("SAG orientacyjny", "Estimated SAG", de = "Geschätzter SAG", fr = "SAG estimé", es = "SAG estimado", pt = "SAG estimado", it = "SAG stimato", nl = "Geschat SAG", sv = "Uppskattad SAG", cs = "Odhadovaný SAG", sk = "Odhadovaný SAG"),
            valueLabel = "",
            descriptionColor = Tokens.TextTertiary,
            description = tr(
                "Liczony tylko wtedy, gdy chwilowo używasz co najmniej 60% mocy maksymalnej silnika, i to " +
                    "nieprzerwanie przez min. 5 sekund (krótsze szarpnięcia prądem są odrzucane jako niewiarygodne). " +
                    "Wartość orientacyjna, adekwatna do TWOJEGO stylu jazdy - liczona z takich próbek i wygładzana, " +
                    "więc reaguje powoli. Przy niskim obciążeniu silnika (spokojna jazda, rzadkie przekraczanie 60%) " +
                    "wynik będzie wyglądał lepiej (niższy SAG), niż realnie pokazałby pełny Pomiar SAG poniżej.",
                "Only calculated while you're momentarily using at least 60% of the motor's max power, and only if " +
                    "held continuously for at least 5 seconds (shorter current spikes are discarded as unreliable). " +
                    "An approximate value, matched to YOUR riding style - calculated from such samples and smoothed, " +
                    "so it reacts slowly. Under low motor load (gentle riding, rarely crossing 60%) the result will " +
                    "look better (lower SAG) than a full SAG Measurement below would actually show.",
                de = "Wird nur berechnet, während du kurzzeitig mindestens 60% der maximalen Motorleistung " +
                    "nutzt, und zwar ununterbrochen für mind. 5 Sekunden (kürzere Stromspitzen werden als " +
                    "unzuverlässig verworfen). Ein Näherungswert, passend zu DEINEM Fahrstil - berechnet aus " +
                    "solchen Proben und geglättet, reagiert also langsam. Bei geringer Motorlast (ruhiges Fahren, " +
                    "selten über 60%) sieht das Ergebnis besser aus (niedrigerer SAG), als eine vollständige " +
                    "SAG-Messung unten tatsächlich zeigen würde.",
                fr = "Calculé uniquement lorsque vous utilisez momentanément au moins 60% de la puissance " +
                    "maximale du moteur, et seulement si cela est maintenu en continu pendant au moins 5 secondes " +
                    "(les pics de courant plus courts sont écartés comme non fiables). Une valeur approximative, " +
                    "adaptée à VOTRE style de conduite - calculée à partir de ces échantillons et lissée, elle " +
                    "réagit donc lentement. Sous faible charge moteur (conduite douce, dépassant rarement 60%), " +
                    "le résultat paraîtra meilleur (SAG plus bas) qu'une Mesure SAG complète ci-dessous ne le " +
                    "montrerait réellement.",
                es = "Solo se calcula mientras usas momentáneamente al menos el 60% de la potencia máxima del " +
                    "motor, y únicamente si se mantiene de forma continua durante al menos 5 segundos (los picos " +
                    "de corriente más breves se descartan por poco fiables). Un valor aproximado, ajustado a TU " +
                    "estilo de conducción - calculado a partir de esas muestras y suavizado, por lo que reacciona " +
                    "lentamente. Con baja carga del motor (conducción suave, superando raramente el 60%), el " +
                    "resultado parecerá mejor (SAG más bajo) de lo que realmente mostraría una Medición SAG " +
                    "completa más abajo.",
                pt = "Calculado apenas enquanto usas momentaneamente pelo menos 60% da potência máxima do " +
                    "motor, e apenas se mantido continuamente durante pelo menos 5 segundos (picos de corrente " +
                    "mais curtos são descartados por não serem fiáveis). Um valor aproximado, ajustado ao TEU " +
                    "estilo de condução - calculado a partir dessas amostras e suavizado, por isso reage " +
                    "lentamente. Com carga baixa do motor (condução suave, raramente ultrapassando 60%), o " +
                    "resultado parecerá melhor (SAG mais baixo) do que uma Medição SAG completa abaixo " +
                    "realmente mostraria.",
                it = "Calcolato solo mentre utilizzi momentaneamente almeno il 60% della potenza massima del " +
                    "motore, e solo se mantenuto continuativamente per almeno 5 secondi (picchi di corrente più " +
                    "brevi vengono scartati come inaffidabili). Un valore approssimativo, adattato al TUO stile " +
                    "di guida - calcolato da tali campioni e livellato, quindi reagisce lentamente. Con carico " +
                    "motore basso (guida tranquilla, che supera raramente il 60%), il risultato sembrerà " +
                    "migliore (SAG più basso) di quanto mostrerebbe realmente una Misurazione SAG completa qui sotto.",
                nl = "Wordt alleen berekend terwijl je momenteel minstens 60% van het maximale motorvermogen " +
                    "gebruikt, en alleen als dit continu minstens 5 seconden wordt volgehouden (kortere " +
                    "stroompieken worden als onbetrouwbaar genegeerd). Een benaderende waarde, afgestemd op " +
                    "JOUW rijstijl - berekend uit dergelijke samples en afgevlakt, dus reageert traag. Bij lage " +
                    "motorbelasting (rustig rijden, zelden boven 60%) zal het resultaat er beter uitzien " +
                    "(lagere SAG) dan een volledige SAG-meting hieronder daadwerkelijk zou tonen.",
                sv = "Beräknas endast medan du tillfälligt använder minst 60% av motorns maxeffekt, och " +
                    "endast om detta hålls i minst 5 sekunder utan avbrott (kortare strömtoppar avvisas " +
                    "som opålitliga). Ett ungefärligt värde, anpassat till DIN körstil - beräknat från " +
                    "sådana prover och utjämnat, så det reagerar långsamt. Vid låg motorbelastning (lugn " +
                    "körning, sällan över 60%) kommer resultatet att se bättre ut (lägre SAG) än vad en " +
                    "fullständig SAG-mätning nedan faktiskt skulle visa.",
                cs = "Počítá se pouze v okamžiku, kdy krátkodobě využíváte alespoň 60% maximálního výkonu " +
                    "motoru, a to jen pokud je to udrženo nepřetržitě po dobu min. 5 sekund (kratší proudové " +
                    "špičky jsou zahozeny jako nespolehlivé). Přibližná hodnota, odpovídající VAŠEMU stylu " +
                    "jízdy - počítaná z takových vzorků a vyhlazená, proto reaguje pomalu. Při nízkém zatížení " +
                    "motoru (klidná jízda, zřídka nad 60%) bude výsledek vypadat lépe (nižší SAG), než by " +
                    "skutečně ukázalo úplné Měření SAG níže.",
                sk = "Počíta sa iba počas krátkodobého využívania aspoň 60% maximálneho výkonu motora, a to " +
                    "len ak je to udržané nepretržite aspoň 5 sekúnd (kratšie prúdové špičky sú zahodené ako " +
                    "nespoľahlivé). Približná hodnota, zodpovedajúca VÁŠMU štýlu jazdy - počítaná z takýchto " +
                    "vzoriek a vyhladená, preto reaguje pomaly. Pri nízkom zaťažení motora (pokojná jazda, " +
                    "zriedka nad 60%) bude výsledok vyzerať lepšie (nižší SAG), než by v skutočnosti ukázalo " +
                    "úplné Meranie SAG nižšie.",
            ),
        ) {
            val sagV = state.everydaySagAtMaxCurrentV
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        tr("Projekcja przy max prądzie", "Projected at max current", de = "Projektion bei Maximalstrom", fr = "Projection au courant max", es = "Proyección a corriente máxima", pt = "Projeção à corrente máxima", it = "Proiezione alla corrente massima", nl = "Projectie bij max. stroom", sv = "Projektion vid maxström", cs = "Projekce při max. proudu", sk = "Projekcia pri max. prúde"),
                        fontFamily = Manrope, fontSize = 11.sp, color = Tokens.TextSecondary,
                    )
                    Box(Modifier.height(40.dp), contentAlignment = Alignment.CenterStart) {
                        Text(
                            if (sagV != null) "${String.format("%.1f", sagV)} V" else "-- V",
                            fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 26.sp,
                            color = if (sagV != null) Tokens.TextPrimary else Tokens.TextTertiary,
                        )
                    }
                }
                if (sagV != null) SagBadge(sagV)
                Spacer(Modifier.size(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${String.format("%.0f", state.maxCurrentACalibratedOrDefault)} A",
                        fontFamily = Manrope, fontSize = 12.sp, color = Tokens.TextTertiary,
                    )
                    if (!state.maxCurrentAIsKnown) {
                        Text(
                            tr("nieznany, połącz się", "unknown, connect first", de = "unbekannt, erst verbinden", fr = "inconnu, connectez-vous d'abord", es = "desconocido, conéctate primero", pt = "desconhecido, liga-te primeiro", it = "sconosciuto, connettiti prima", nl = "onbekend, verbind eerst", sv = "okänd, anslut först", cs = "neznámý, nejprve se připojte", sk = "neznámy, najprv sa pripojte"),
                            fontFamily = Manrope, fontSize = 9.sp, color = Tokens.TextTertiary,
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        MicroLabel(tr("Pomiar SAG", "SAG Measurement", de = "SAG-Messung", fr = "Mesure SAG", es = "Medición SAG", pt = "Medição SAG", it = "Misurazione SAG", nl = "SAG-meting", sv = "SAG-mätning", cs = "Měření SAG", sk = "Meranie SAG"))
        TokenCard(borderColor = Tokens.WhiteBorder) {
            Text(
                tr(
                    "Dużo dokładniejszy niż SAG orientacyjny, bo wymusza maksymalne obciążenie baterii w kontrolowanych " +
                        "warunkach - niezależny od tego, jak akurat jeździsz.",
                    "Much more accurate than Estimated SAG, because it forces maximum battery load under controlled " +
                        "conditions - independent of however you happen to be riding.",
                    de = "Deutlich genauer als der geschätzte SAG, da eine maximale Batterielast unter " +
                        "kontrollierten Bedingungen erzwungen wird - unabhängig davon, wie du gerade fährst.",
                    fr = "Bien plus précise que le SAG estimé, car elle force une charge maximale de la batterie " +
                        "dans des conditions contrôlées - indépendamment de votre façon de conduire à ce moment-là.",
                    es = "Mucho más precisa que el SAG estimado, porque fuerza la carga máxima de la batería en " +
                        "condiciones controladas - independientemente de cómo estés conduciendo en ese momento.",
                    pt = "Muito mais preciso do que o SAG estimado, porque força a carga máxima da bateria em " +
                        "condições controladas - independentemente de como estás a conduzir naquele momento.",
                    it = "Molto più preciso del SAG stimato, perché forza il carico massimo della batteria in " +
                        "condizioni controllate - indipendentemente da come stai guidando in quel momento.",
                    nl = "Veel nauwkeuriger dan geschat SAG, omdat het maximale batterijbelasting afdwingt " +
                        "onder gecontroleerde omstandigheden - onafhankelijk van hoe je toevallig rijdt.",
                    sv = "Mycket mer exakt än uppskattad SAG, eftersom den tvingar fram maximal " +
                        "batteribelastning under kontrollerade förhållanden - oberoende av hur du " +
                        "råkar köra just då.",
                    cs = "Mnohem přesnější než odhadovaný SAG, protože vynucuje maximální zatížení " +
                        "baterie za kontrolovaných podmínek - nezávisle na tom, jak právě jedete.",
                    sk = "Oveľa presnejšie než odhadovaný SAG, pretože vynucuje maximálne zaťaženie " +
                        "batérie za kontrolovaných podmienok - nezávisle od toho, ako práve idete.",
                ),
                fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp, color = Tokens.TextPrimary,
            )
            Spacer(Modifier.height(8.dp))

            var instructionsExpanded by remember { mutableStateOf(false) }
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { instructionsExpanded = !instructionsExpanded },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    tr("Jak wykonać pomiar", "How to run the measurement", de = "So führst du die Messung durch", fr = "Comment effectuer la mesure", es = "Cómo realizar la medición", pt = "Como realizar a medição", it = "Come eseguire la misurazione", nl = "Hoe de meting uit te voeren", sv = "Så genomför du mätningen", cs = "Jak provést měření", sk = "Ako vykonať meranie"),
                    fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Tokens.TextSecondary,
                    modifier = Modifier.weight(1f),
                )
                Text(if (instructionsExpanded) "▲" else "▼", fontFamily = Manrope, fontSize = 14.sp, color = Tokens.Emerald)
            }
            if (instructionsExpanded) {
                Spacer(Modifier.height(6.dp))
                Text(
                    tr(
                        "Wykonuj przy naładowaniu baterii 50-100%. Najlepiej pod lekką górkę, ewentualnie na płaskim. " +
                            "Ogranicz pedałowanie do minimum - ustaw najmniejszą (najcięższą) zębatkę z tyłu, żeby zmaksymalizować " +
                            "obciążenie silnika. Użyj manetki (jeśli jest) lub jedź tak, żeby uzyskać maksymalną moc wspomagania - " +
                            "staraj się obciążyć rower jak najmocniej przez cały czas trwania testu.",
                        "Do this with the battery at 50-100%. Ideally on a slight uphill, flat ground as a fallback. " +
                            "Minimize pedaling - use the smallest (heaviest) rear cog to maximize motor load. Use the throttle " +
                            "(if you have one) or ride so you get maximum assist power - try to load the bike as hard as " +
                            "possible for the whole test.",
                        de = "Führe dies bei einem Akkustand von 50-100% durch. Idealerweise an einer leichten " +
                            "Steigung, alternativ auf ebenem Gelände. Minimiere das Pedalieren - nutze den " +
                            "kleinsten (schwersten) hinteren Gang, um die Motorlast zu maximieren. Verwende den " +
                            "Gasgriff (falls vorhanden) oder fahre so, dass du maximale Unterstützungsleistung " +
                            "erhältst - versuche, das Fahrrad während des gesamten Tests so stark wie möglich zu belasten.",
                        fr = "Effectuez ceci avec la batterie à 50-100%. Idéalement sur une légère montée, à " +
                            "défaut sur terrain plat. Minimisez le pédalage - utilisez le plus petit (le plus " +
                            "lourd) pignon arrière pour maximiser la charge du moteur. Utilisez l'accélérateur " +
                            "(si vous en avez un) ou roulez de façon à obtenir la puissance d'assistance " +
                            "maximale - essayez de charger le vélo autant que possible pendant tout le test.",
                        es = "Hazlo con la batería al 50-100%. Idealmente en una ligera subida, o en terreno " +
                            "llano como alternativa. Minimiza el pedaleo - usa el piñón trasero más pequeño (más " +
                            "pesado) para maximizar la carga del motor. Usa el acelerador (si tienes) o conduce " +
                            "de forma que obtengas la máxima potencia de asistencia - intenta cargar la " +
                            "bicicleta lo más posible durante toda la prueba.",
                        pt = "Faz isto com a bateria a 50-100%. Idealmente numa ligeira subida, ou em terreno " +
                            "plano como alternativa. Minimiza o pedalar - usa a coroa traseira mais pequena " +
                            "(mais pesada) para maximizar a carga do motor. Usa o acelerador (se tiveres) ou " +
                            "conduz de forma a obter a potência máxima de assistência - tenta carregar a " +
                            "bicicleta o mais possível durante todo o teste.",
                        it = "Esegui questo con la batteria al 50-100%. Idealmente su una leggera salita, o su " +
                            "terreno pianeggiante come alternativa. Riduci al minimo la pedalata - usa il " +
                            "pignone posteriore più piccolo (più pesante) per massimizzare il carico del " +
                            "motore. Usa l'acceleratore (se presente) o guida in modo da ottenere la massima " +
                            "potenza di assistenza - cerca di caricare la bici il più possibile per tutta la " +
                            "durata del test.",
                        nl = "Doe dit met de batterij op 50-100%. Idealiter op een lichte helling, of op vlak " +
                            "terrein als alternatief. Minimaliseer het trappen - gebruik het kleinste " +
                            "(zwaarste) achtertandwiel om de motorbelasting te maximaliseren. Gebruik de " +
                            "gasgreep (indien aanwezig) of rij zo dat je maximaal ondersteuningsvermogen krijgt " +
                            "- probeer de fiets tijdens de hele test zo zwaar mogelijk te belasten.",
                        sv = "Gör detta med batteriet på 50-100%. Helst i en lätt uppförsbacke, annars på " +
                            "plan mark. Minimera trampandet - använd den minsta (tyngsta) bakre kedjekransen " +
                            "för att maximera motorbelastningen. Använd gasreglaget (om du har ett) eller kör " +
                            "så att du får maximal assistanseffekt - försök belasta cykeln så hårt som möjligt " +
                            "under hela testet.",
                        cs = "Proveďte to při nabití baterie 50-100%. Ideálně na mírném stoupání, případně na " +
                            "rovném terénu. Minimalizujte šlapání - použijte nejmenší (nejtěžší) zadní " +
                            "pastorek pro maximalizaci zatížení motoru. Použijte plynovou páčku (pokud ji " +
                            "máte) nebo jeďte tak, abyste dosáhli maximálního asistenčního výkonu - snažte se " +
                            "kolo zatížit co nejvíce po celou dobu testu.",
                        sk = "Urobte to pri nabití batérie 50-100%. Ideálne na miernom stúpaní, prípadne na " +
                            "rovnom teréne. Minimalizujte šliapanie - použite najmenší (najťažší) zadný " +
                            "pastorok na maximalizáciu zaťaženia motora. Použite plynovú páčku (ak ju máte) " +
                            "alebo choďte tak, aby ste dosiahli maximálny asistenčný výkon - snažte sa bicykel " +
                            "zaťažiť čo najviac počas celého testu.",
                    ),
                    fontFamily = Manrope, fontSize = 12.sp, lineHeight = 17.sp, color = Tokens.TextSecondary,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                tr(
                    "Przebieg: 2 min odczekania bez jazdy (stabilizacja napięcia) → 30s pełnego obciążenia → 2 min odczekania bez jazdy (apka liczy SAG).",
                    "Sequence: 2 min waiting without riding (voltage stabilizes) → 30s full load → 2 min waiting without riding (app calculates SAG).",
                    de = "Ablauf: 2 Min. warten ohne zu fahren (Spannung stabilisiert sich) → 30 Sek. Volllast → " +
                        "2 Min. warten ohne zu fahren (App berechnet SAG).",
                    fr = "Déroulement : 2 min d'attente sans rouler (la tension se stabilise) → 30s de charge " +
                        "complète → 2 min d'attente sans rouler (l'appli calcule le SAG).",
                    es = "Secuencia: 2 min de espera sin conducir (el voltaje se estabiliza) → 30s de carga " +
                        "máxima → 2 min de espera sin conducir (la app calcula el SAG).",
                    pt = "Sequência: 2 min de espera sem andar (a tensão estabiliza) → 30s de carga máxima → " +
                        "2 min de espera sem andar (a app calcula o SAG).",
                    it = "Sequenza: 2 min di attesa senza pedalare (la tensione si stabilizza) → 30s di carico " +
                        "massimo → 2 min di attesa senza pedalare (l'app calcola il SAG).",
                    nl = "Verloop: 2 min wachten zonder te rijden (spanning stabiliseert) → 30s volle " +
                        "belasting → 2 min wachten zonder te rijden (app berekent SAG).",
                    sv = "Förlopp: 2 min väntan utan att köra (spänningen stabiliseras) → 30s full " +
                        "belastning → 2 min väntan utan att köra (appen beräknar SAG).",
                    cs = "Postup: 2 min čekání bez jízdy (napětí se stabilizuje) → 30s plné zátěže → " +
                        "2 min čekání bez jízdy (aplikace vypočítá SAG).",
                    sk = "Postup: 2 min čakania bez jazdy (napätie sa stabilizuje) → 30s plnej záťaže → " +
                        "2 min čakania bez jazdy (aplikácia vypočíta SAG).",
                ),
                fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 17.sp, color = Tokens.Amber,
            )
            Spacer(Modifier.height(10.dp))

            when (state.sagCalibrationPhase) {
                SagCalibrationPhase.IDLE -> {
                    MeasureStartButton(
                        enabled = state.connection == ConnectionStatus.CONNECTED,
                        onClick = onStart,
                    )
                    if (state.connection != ConnectionStatus.CONNECTED) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            tr(
                                "Połącz się ze sterownikiem, żeby wykonać pomiar.",
                                "Connect to the controller to run the measurement.",
                                de = "Verbinde dich mit dem Steuergerät, um die Messung durchzuführen.",
                                fr = "Connectez-vous au contrôleur pour effectuer la mesure.",
                                es = "Conéctate al controlador para realizar la medición.",
                                pt = "Liga-te ao controlador para realizar a medição.",
                                it = "Connettiti al controller per eseguire la misurazione.",
                                nl = "Verbind met de controller om de meting uit te voeren.",
                                sv = "Anslut till kontrollern för att genomföra mätningen.",
                                cs = "Připojte se k řídicí jednotce pro provedení měření.",
                                sk = "Pripojte sa k riadiacej jednotke na vykonanie merania.",
                            ),
                            fontFamily = Manrope, fontSize = 11.sp, color = Tokens.TextTertiary,
                        )
                    }
                }
                else -> MeasureProgress(phase = state.sagCalibrationPhase, remainingS = state.sagCalibrationRemainingS, onCancel = onCancel)
            }
        }

        MicroLabel(tr("Ostatni wynik pomiaru", "Last measurement result", de = "Letztes Messergebnis", fr = "Dernier résultat de mesure", es = "Último resultado de medición", pt = "Último resultado de medição", it = "Ultimo risultato di misurazione", nl = "Laatste meetresultaat", sv = "Senaste mätresultat", cs = "Poslední výsledek měření", sk = "Posledný výsledok merania"))
        TokenCard(borderColor = Tokens.WhiteBorder) {
            if (state.sagCalibrationResultV == null) {
                Text(
                    tr(
                        "Brak pomiaru - wykonaj pierwszy Pomiar SAG powyżej.",
                        "No measurement yet - run your first SAG Measurement above.",
                        de = "Noch keine Messung - führe oben deine erste SAG-Messung durch.",
                        fr = "Aucune mesure encore - effectuez votre première Mesure SAG ci-dessus.",
                        es = "Aún no hay medición - realiza tu primera Medición SAG arriba.",
                        pt = "Ainda sem medição - realiza a tua primeira Medição SAG acima.",
                        it = "Nessuna misurazione ancora - esegui la tua prima Misurazione SAG sopra.",
                        nl = "Nog geen meting - voer hierboven je eerste SAG-meting uit.",
                        sv = "Ingen mätning ännu - genomför din första SAG-mätning ovan.",
                        cs = "Zatím žádné měření - proveďte výše první Měření SAG.",
                        sk = "Zatiaľ žiadne meranie - vykonajte vyššie prvé Meranie SAG.",
                    ),
                    fontFamily = Manrope, fontSize = 12.sp, color = Tokens.TextTertiary,
                )
            } else {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    ResultRow(tr("SAG pod obciążeniem", "SAG under load", de = "SAG unter Last", fr = "SAG en charge", es = "SAG bajo carga", pt = "SAG sob carga", it = "SAG sotto carico", nl = "SAG onder belasting", sv = "SAG under belastning", cs = "SAG pod zátěží", sk = "SAG pod záťažou"), "${String.format("%.2f", state.sagCalibrationResultV)} V", modifier = Modifier.weight(1f))
                    SagBadge(state.sagCalibrationResultV)
                }
                state.sagCalibrationResultResistanceOhm?.let {
                    ResultRow(tr("Efektywna rezystancja", "Effective resistance", de = "Effektiver Widerstand", fr = "Résistance effective", es = "Resistencia efectiva", pt = "Resistência efetiva", it = "Resistenza effettiva", nl = "Effectieve weerstand", sv = "Effektivt motstånd", cs = "Efektivní odpor", sk = "Efektívny odpor"), "${String.format("%.0f", it * 1000)} mΩ")
                }
                state.sagCalibrationResultCurrentA?.let {
                    ResultRow(tr("Prąd testu", "Test current", de = "Teststrom", fr = "Courant de test", es = "Corriente de prueba", pt = "Corrente de teste", it = "Corrente di prova", nl = "Teststroom", sv = "Testström", cs = "Testovací proud", sk = "Testovací prúd"), "${String.format("%.1f", it)} A")
                }
                state.sagCalibrationResultSocPct?.let {
                    ResultRow(tr("Naładowanie na starcie", "Charge at start", de = "Ladezustand zu Beginn", fr = "Charge au départ", es = "Carga al inicio", pt = "Carga no início", it = "Carica all'inizio", nl = "Lading bij start", sv = "Laddning vid start", cs = "Nabití na začátku", sk = "Nabitie na začiatku"), "$it %")
                }
                state.sagCalibrationResultTimestampMs?.let {
                    ResultRow(tr("Kiedy", "When", de = "Wann", fr = "Quand", es = "Cuándo", pt = "Quando", it = "Quando", nl = "Wanneer", sv = "När", cs = "Kdy", sk = "Kedy"), formatAgo(it))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SagLegendRow(range: String, label: String, color: Color, last: Boolean = false) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(3.dp)),
        )
        Spacer(Modifier.size(8.dp))
        Text(range, fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Tokens.TextPrimary, modifier = Modifier.width(84.dp))
        Text(label, fontFamily = Manrope, fontSize = 12.sp, lineHeight = 15.sp, color = Tokens.TextSecondary, modifier = Modifier.weight(1f))
    }
    if (!last) {
        HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
    }
}

/** Kolorowa plakietka jakości baterii wg SAG - te same progi co [SagLegendRow] powyżej. */
@Composable
private fun SagBadge(sagV: Double) {
    val color = when {
        sagV <= 4.0 -> Tokens.Emerald
        sagV <= 6.0 -> Tokens.Amber
        else -> Tokens.Red
    }
    Box(
        Modifier
            .background(color.copy(alpha = 0.16f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            when {
                sagV <= 2.0 -> tr("Doskonała", "Excellent", de = "Ausgezeichnet", fr = "Excellente", es = "Excelente", pt = "Excelente", it = "Eccellente", nl = "Uitstekend", sv = "Utmärkt", cs = "Vynikající", sk = "Vynikajúca")
                sagV <= 4.0 -> tr("Wystarczająca", "Sufficient", de = "Ausreichend", fr = "Suffisante", es = "Suficiente", pt = "Suficiente", it = "Sufficiente", nl = "Voldoende", sv = "Tillräcklig", cs = "Dostatečná", sk = "Dostatočná")
                sagV <= 6.0 -> tr("Na pograniczu", "Borderline", de = "Grenzwertig", fr = "Limite", es = "Límite", pt = "No limite", it = "Al limite", nl = "Grensgeval", sv = "Gränsfall", cs = "Na hranici", sk = "Na hranici")
                else -> tr("Zbyt niska", "Too low", de = "Zu niedrig", fr = "Trop faible", es = "Demasiado baja", pt = "Demasiado baixa", it = "Troppo bassa", nl = "Te laag", sv = "För låg", cs = "Příliš nízká", sk = "Príliš nízka")
            },
            fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = color,
        )
    }
}

@Composable
private fun MeasureStartButton(enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (enabled) Tokens.Blue else Tokens.Elevated, RoundedCornerShape(14.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            tr("Rozpocznij pomiar", "Start measurement", de = "Messung starten", fr = "Démarrer la mesure", es = "Iniciar medición", pt = "Iniciar medição", it = "Avvia misurazione", nl = "Meting starten", sv = "Starta mätning", cs = "Zahájit měření", sk = "Spustiť meranie"),
            fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 15.sp,
            color = if (enabled) Tokens.OnAccent else Tokens.TextTertiary,
        )
    }
}

@Composable
private fun MeasureProgress(phase: SagCalibrationPhase, remainingS: Int, onCancel: () -> Unit) {
    val label = when (phase) {
        SagCalibrationPhase.PRE_WAIT -> tr("Odczekaj - nie jedź, napięcie się stabilizuje", "Wait - don't ride, voltage is stabilizing", de = "Warten - nicht fahren, Spannung stabilisiert sich", fr = "Attendez - ne roulez pas, la tension se stabilise", es = "Espera - no conduzcas, el voltaje se está estabilizando", pt = "Espera - não andes, a tensão está a estabilizar", it = "Attendi - non pedalare, la tensione si sta stabilizzando", nl = "Wacht - niet rijden, spanning stabiliseert", sv = "Vänta - kör inte, spänningen stabiliseras", cs = "Počkejte - nejeďte, napětí se stabilizuje", sk = "Počkajte - nejazdite, napätie sa stabilizuje")
        SagCalibrationPhase.LOADING -> tr("TERAZ! Obciąż rower maksymalnie", "NOW! Load the bike as hard as possible", de = "JETZT! Belaste das Fahrrad maximal", fr = "MAINTENANT ! Chargez le vélo au maximum", es = "¡AHORA! Carga la bicicleta al máximo", pt = "AGORA! Carrega a bicicleta ao máximo", it = "ORA! Carica la bici il più possibile", nl = "NU! Belast de fiets zo hard mogelijk", sv = "NU! Belasta cykeln maximalt", cs = "TEĎ! Zatěžte kolo maximálně", sk = "TERAZ! Zaťažte bicykel maximálne")
        SagCalibrationPhase.POST_WAIT -> tr("Zatrzymaj rower i odczekaj - trwa liczenie", "Stop the bike and wait - calculating", de = "Fahrrad anhalten und warten - Berechnung läuft", fr = "Arrêtez le vélo et attendez - calcul en cours", es = "Detén la bicicleta y espera - calculando", pt = "Para a bicicleta e espera - a calcular", it = "Ferma la bici e attendi - calcolo in corso", nl = "Stop de fiets en wacht - berekenen", sv = "Stanna cykeln och vänta - beräknar", cs = "Zastavte kolo a počkejte - probíhá výpočet", sk = "Zastavte bicykel a počkajte - prebieha výpočet")
        SagCalibrationPhase.IDLE -> ""
    }
    val accent = if (phase == SagCalibrationPhase.LOADING) Tokens.Amber else Tokens.Blue

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            String.format("%d:%02d", remainingS / 60, remainingS % 60),
            fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 40.sp, color = accent,
        )
        Spacer(Modifier.height(6.dp))
        Text(label, fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Tokens.TextPrimary)
        Spacer(Modifier.height(10.dp))
        Text(
            tr("Anuluj", "Cancel", de = "Abbrechen", fr = "Annuler", es = "Cancelar", pt = "Cancelar", it = "Annulla", nl = "Annuleren", sv = "Avbryt", cs = "Zrušit", sk = "Zrušiť"),
            fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Tokens.Red,
            modifier = Modifier
                .clickable { onCancel() }
                .padding(6.dp),
        )
    }
}

@Composable
private fun ResultRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextSecondary, modifier = Modifier.weight(1f))
        Text(value, fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Tokens.TextPrimary)
    }
}

@Composable
private fun formatAgo(timestampMs: Long): String {
    val diffMs = System.currentTimeMillis() - timestampMs
    val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
    return when {
        hours < 1 -> tr("mniej niż godzinę temu", "less than an hour ago", de = "vor weniger als einer Stunde", fr = "il y a moins d'une heure", es = "hace menos de una hora", pt = "há menos de uma hora", it = "meno di un'ora fa", nl = "minder dan een uur geleden", sv = "mindre än en timme sedan", cs = "před méně než hodinou", sk = "pred menej ako hodinou")
        hours < 24 -> tr("$hours godz. temu", "$hours h ago", de = "vor $hours Std.", fr = "il y a $hours h", es = "hace $hours h", pt = "há $hours h", it = "$hours h fa", nl = "$hours u geleden", sv = "för $hours h sedan", cs = "před $hours h", sk = "pred $hours h")
        else -> tr("${hours / 24} dni temu", "${hours / 24} days ago", de = "vor ${hours / 24} Tagen", fr = "il y a ${hours / 24} jours", es = "hace ${hours / 24} días", pt = "há ${hours / 24} dias", it = "${hours / 24} giorni fa", nl = "${hours / 24} dagen geleden", sv = "för ${hours / 24} dagar sedan", cs = "před ${hours / 24} dny", sk = "pred ${hours / 24} dňami")
    }
}
