package com.bafspeed.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bafspeed.app.FirmwareType
import com.bafspeed.app.UiState
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.ui.components.ExpandableParamTile
import com.bafspeed.app.ui.components.MicroLabel
import com.bafspeed.app.ui.components.PreviewBanner
import com.bafspeed.app.ui.components.StepBtn
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens

@Composable
fun BatteryScreen(
    state: UiState,
    onCellCountChange: (Int) -> Unit,
    onCapacityAhChange: (Double) -> Unit,
    onCapacityWhChange: (Double) -> Unit,
) {
    val basic = state.basicOrDefault

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
                "Liczba cel i pojemność to dane pomocnicze aplikacji - nie są zapisywane w sterowniku, ale są potrzebne, żeby " +
                    "EggSPEED mógł poprawnie wyliczyć zasięg roweru na ekranie Kokpit.",
                "Cell count and capacity are app-side helper data - they aren't saved in the controller, but are needed so " +
                    "EggSPEED can correctly estimate the bike's range on the Cockpit screen.",
                de = "Zellenzahl und Kapazität sind Hilfsdaten der App - sie werden nicht im Steuergerät gespeichert, werden aber " +
                    "benötigt, damit EggSPEED die Reichweite des Rads im Cockpit-Bildschirm korrekt berechnen kann.",
                fr = "Le nombre de cellules et la capacité sont des données d'aide côté application - elles ne sont pas enregistrées " +
                    "dans le contrôleur, mais sont nécessaires pour qu'EggSPEED puisse estimer correctement l'autonomie du vélo sur le Cockpit.",
                es = "El número de celdas y la capacidad son datos auxiliares de la app - no se guardan en el controlador, pero son " +
                    "necesarios para que EggSPEED pueda estimar correctamente la autonomía de la bici en la pantalla del Cockpit.",
                pt = "O número de células e a capacidade são dados auxiliares da app - não são guardados no controlador, mas são " +
                    "necessários para que o EggSPEED possa estimar corretamente a autonomia da bicicleta no ecrã do Cockpit.",
                it = "Il numero di celle e la capacità sono dati ausiliari dell'app - non vengono salvati nel controller, ma sono " +
                    "necessari affinché EggSPEED possa stimare correttamente l'autonomia della bici nella schermata Cockpit.",
                nl = "Celaantal en capaciteit zijn hulpgegevens van de app - ze worden niet in de controller opgeslagen, maar zijn " +
                    "nodig zodat EggSPEED de actieradius van de fiets correct kan berekenen op het Cockpit-scherm.",
                sv = "Antal celler och kapacitet är hjälpdata på appsidan - de sparas inte i styrenheten, men behövs för " +
                    "att EggSPEED korrekt ska kunna beräkna cykelns räckvidd på skärmen Cockpit.",
                cs = "Počet článků a kapacita jsou pomocné údaje aplikace - neukládají se do řídicí jednotky, ale jsou " +
                    "potřeba, aby EggSPEED mohl správně vypočítat dojezd kola na obrazovce Cockpit.",
                sk = "Počet článkov a kapacita sú pomocné údaje aplikácie - neukladajú sa do riadiacej jednotky, ale sú " +
                    "potrebné na to, aby EggSPEED mohol správne vypočítať dojazd bicykla na obrazovke Cockpit.",
            ),
            borderWidth = 2.dp,
        )

        val isBbsFw = state.firmwareType == FirmwareType.BBS_FW
        val bbsFwCfg = state.bbsFwConfigOrDefault

        MicroLabel(tr("Twoja bateria", "Your battery", de = "Deine Batterie", fr = "Votre batterie", es = "Tu batería", pt = "A tua bateria", it = "La tua batteria", nl = "Jouw batterij", sv = "Ditt batteri", cs = "Tvoje baterie", sk = "Tvoja batéria"))

        // Trzy progi napięcia baterii scalone w jeden kafelek - to same dane informacyjne (odczyt
        // lub wyliczone z liczby cel), użytkownik nic tu nie ustawia, więc osobne karty tylko
        // rozpraszały. Rosnąco: dolny limit rozładowania (LBP, realny odczyt - edycja w Bafang
        // Basic / bbs-fw General, jedno źródło prawdy), napięcie nominalne (z liczby cel, tak samo
        // jak w kafelku "Liczba cel" niżej) i górny limit naładowania (cellCount x 4,2V). Nominalne
        // i górny limit celowo liczone tą samą metodą (z cellCount), żeby nie mieszać realnego
        // odczytu z wyliczoną estymatą.
        val lbpV = if (isBbsFw) bbsFwCfg.lowCutOffV else basic.lowBatteryProtection
        ExpandableParamTile(
            label = tr("Napięcie baterii", "Battery voltage", de = "Batteriespannung", fr = "Tension de la batterie", es = "Voltaje de la batería", pt = "Voltagem da bateria", it = "Tensione della batteria", nl = "Batterijspanning", sv = "Batterispänning", cs = "Napětí baterie", sk = "Napätie batérie"),
            // Puste - kafelek jest rozwijalnym zestawieniem trzech progów, więc nagłówek nie
            // powiela żadnego z nich osobną wartością (dawniej powtarzał tu napięcie nominalne).
            valueLabel = "",
            description = "",
            descriptionContent = {
                BatteryVoltageDescription(isBbsFw = isBbsFw, cellCount = state.cellCount)
            },
        ) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                VoltageReadout(tr("Dolne napięcie odcięcia (LBP)", "Low voltage cutoff (LBP)", de = "Untere Abschaltspannung (LBP)", fr = "Coupure basse tension (LBP)", es = "Corte de baja tensión (LBP)", pt = "Corte de baixa tensão (LBP)", it = "Soglia di taglio bassa tensione (LBP)", nl = "Onderste afsluitspanning (LBP)", sv = "Nedre avstängningsspänning (LBP)", cs = "Dolní vypínací napětí (LBP)", sk = "Dolné vypínacie napätie (LBP)"), "$lbpV V")
                VoltageReadout(tr("Napięcie nominalne", "Nominal voltage", de = "Nennspannung", fr = "Tension nominale", es = "Voltaje nominal", pt = "Tensão nominal", it = "Tensione nominale", nl = "Nominale spanning", sv = "Nominell spänning", cs = "Jmenovité napětí", sk = "Menovité napätie"), "${state.nominalPackVoltage} V")
                VoltageReadout(tr("Górny limit naładowania", "Upper charge limit", de = "Obere Ladegrenze", fr = "Limite de charge haute", es = "Límite superior de carga", pt = "Limite superior de carga", it = "Limite di carica superiore", nl = "Bovengrens laadspanning", sv = "Övre laddningsgräns", cs = "Horní limit nabíjení", sk = "Horný limit nabíjania"), "${String.format("%.1f", state.cellCount * 4.2)} V")
            }
        }

        ExpandableParamTile(
            label = tr("Liczba cel", "Cell count", de = "Zellenzahl", fr = "Nombre de cellules", es = "Número de celdas", pt = "Número de células", it = "Numero di celle", nl = "Aantal cellen", sv = "Antal celler", cs = "Počet článků", sk = "Počet článkov"),
            valueLabel = "${state.cellCount}S",
            description = tr(
                "Liczba ogniw połączonych szeregowo w Twoim pakiecie (np. 13S = 13 ogniw). Razem z " +
                    "napięciem ogniwa (ok. 3,7V) wyznacza napięcie nominalne pakietu (tu: ${state.nominalPackVoltage}V) - " +
                    "EggSPEED używa go do przeliczania prądu na moc i do szacowania zasięgu. To dane pomocnicze aplikacji, " +
                    "nie są wysyłane do sterownika.",
                "The number of cells connected in series in your pack (e.g. 13S = 13 cells). Together with " +
                    "the cell voltage (approx. 3.7V) it determines the pack's nominal voltage (here: ${state.nominalPackVoltage}V) - " +
                    "EggSPEED uses it to convert current to power and to estimate range. This is app-side helper data, " +
                    "not sent to the controller.",
                de = "Die Anzahl der in Reihe geschalteten Zellen in deinem Akku (z. B. 13S = 13 Zellen). Zusammen mit " +
                    "der Zellenspannung (ca. 3,7V) ergibt sich die Nennspannung des Akkus (hier: ${state.nominalPackVoltage}V) - " +
                    "EggSPEED nutzt sie, um Strom in Leistung umzurechnen und die Reichweite zu schätzen. Das ist eine Hilfsangabe der App, " +
                    "sie wird nicht an das Steuergerät gesendet.",
                fr = "Le nombre de cellules connectées en série dans votre batterie (ex. 13S = 13 cellules). Avec " +
                    "la tension de cellule (env. 3,7V), elle détermine la tension nominale de la batterie (ici : ${state.nominalPackVoltage}V) - " +
                    "EggSPEED l'utilise pour convertir le courant en puissance et estimer l'autonomie. C'est une donnée d'aide côté application, " +
                    "elle n'est pas envoyée au contrôleur.",
                es = "El número de celdas conectadas en serie en tu batería (ej. 13S = 13 celdas). Junto con " +
                    "el voltaje de celda (aprox. 3,7V) determina el voltaje nominal de la batería (aquí: ${state.nominalPackVoltage}V) - " +
                    "EggSPEED lo usa para convertir corriente en potencia y estimar la autonomía. Es un dato auxiliar de la app, " +
                    "no se envía al controlador.",
                pt = "O número de células ligadas em série no teu pack (por ex. 13S = 13 células). Junto com " +
                    "a tensão da célula (aprox. 3,7V) determina a tensão nominal do pack (aqui: ${state.nominalPackVoltage}V) - " +
                    "o EggSPEED usa-a para converter corrente em potência e estimar a autonomia. São dados auxiliares da app, " +
                    "não são enviados ao controlador.",
                it = "Il numero di celle collegate in serie nel tuo pacco (ad es. 13S = 13 celle). Insieme " +
                    "alla tensione di cella (circa 3,7V) determina la tensione nominale del pacco (qui: ${state.nominalPackVoltage}V) - " +
                    "EggSPEED la usa per convertire la corrente in potenza e stimare l'autonomia. Sono dati ausiliari dell'app, " +
                    "non vengono inviati al controller.",
                nl = "Het aantal in serie geschakelde cellen in je pack (bijv. 13S = 13 cellen). Samen met " +
                    "de celspanning (ca. 3,7V) bepaalt dit de nominale spanning van het pack (hier: ${state.nominalPackVoltage}V) - " +
                    "EggSPEED gebruikt dit om stroom om te rekenen naar vermogen en om de actieradius te schatten. Dit zijn hulpgegevens " +
                    "van de app, ze worden niet naar de controller verzonden.",
                sv = "Antalet celler i serie i ditt batteripaket (t.ex. 13S = 13 celler). Tillsammans med " +
                    "cellspänningen (ca 3,7V) bestäms paketets nominella spänning (här: ${state.nominalPackVoltage}V) - " +
                    "EggSPEED använder detta för att räkna om ström till effekt och för att uppskatta räckvidden. Detta är hjälpdata " +
                    "på appsidan, det skickas inte till styrenheten.",
                cs = "Počet článků zapojených sériově v tvém packu (např. 13S = 13 článků). Spolu " +
                    "s napětím článku (cca 3,7V) určuje jmenovité napětí packu (zde: ${state.nominalPackVoltage}V) - " +
                    "EggSPEED to používá k přepočtu proudu na výkon a k odhadu dojezdu. Jsou to pomocné údaje aplikace, " +
                    "neodesílají se do řídicí jednotky.",
                sk = "Počet článkov zapojených sériovo v tvojom packu (napr. 13S = 13 článkov). Spolu " +
                    "s napätím článku (cca 3,7V) určuje menovité napätie packu (tu: ${state.nominalPackVoltage}V) - " +
                    "EggSPEED to používa na prepočet prúdu na výkon a na odhad dojazdu. Sú to pomocné údaje aplikácie, " +
                    "neposielajú sa do riadiacej jednotky.",
            ),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                StepBtn("-", true) { onCellCountChange(state.cellCount - 1) }
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .background(Tokens.BlueFaint16, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text("${state.nominalPackVoltage}V", fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Tokens.Blue)
                }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onCellCountChange(state.cellCount + 1) }
            }
        }

        ExpandableParamTile(
            label = tr("Pojemność", "Capacity", de = "Kapazität", fr = "Capacité", es = "Capacidad", pt = "Capacidade", it = "Capacità", nl = "Capaciteit", sv = "Kapacitet", cs = "Kapacita", sk = "Kapacita"),
            valueLabel = "${String.format("%.1f", state.capacityAh)} Ah",
            description = tr(
                "Pojemność Twojego pakietu w Ah (amperogodzinach) i Wh (watogodzinach, Ah × napięcie " +
                    "nominalne) - wpisz wartość z etykiety baterii lub karty produktu. EggSPEED używa jej razem z " +
                    "bieżącym zużyciem prądu, żeby szacować zasięg na ekranie Kokpit. To dane pomocnicze aplikacji, " +
                    "nie są wysyłane do sterownika.",
                "Your pack's capacity in Ah (amp-hours) and Wh (watt-hours, Ah × nominal " +
                    "voltage) - enter the value from the battery label or product page. EggSPEED uses it together " +
                    "with the current power draw to estimate range on the Cockpit screen. This is app-side helper " +
                    "data, not sent to the controller.",
                de = "Die Kapazität deines Akkus in Ah (Amperestunden) und Wh (Wattstunden, Ah × Nenn" +
                    "spannung) - gib den Wert vom Akku-Etikett oder von der Produktseite ein. EggSPEED nutzt ihn zusammen " +
                    "mit der aktuellen Leistungsaufnahme, um die Reichweite im Cockpit zu schätzen. Das ist eine Hilfsangabe " +
                    "der App, sie wird nicht an das Steuergerät gesendet.",
                fr = "La capacité de votre batterie en Ah (ampères-heures) et Wh (watts-heures, Ah × tension " +
                    "nominale) - entrez la valeur indiquée sur l'étiquette de la batterie ou la fiche produit. EggSPEED l'utilise " +
                    "avec la puissance consommée actuelle pour estimer l'autonomie sur le Cockpit. C'est une donnée d'aide " +
                    "côté application, elle n'est pas envoyée au contrôleur.",
                es = "La capacidad de tu batería en Ah (amperios-hora) y Wh (vatios-hora, Ah × voltaje " +
                    "nominal) - introduce el valor de la etiqueta de la batería o la ficha del producto. EggSPEED lo usa junto " +
                    "con el consumo de potencia actual para estimar la autonomía en el Cockpit. Es un dato auxiliar de la app, " +
                    "no se envía al controlador.",
                pt = "A capacidade do teu pack em Ah (amperes-hora) e Wh (watts-hora, Ah × tensão " +
                    "nominal) - insere o valor da etiqueta da bateria ou da página do produto. O EggSPEED usa-a junto com " +
                    "o consumo de potência atual para estimar a autonomia no ecrã do Cockpit. São dados auxiliares da app, " +
                    "não são enviados ao controlador.",
                it = "La capacità del tuo pacco in Ah (amperora) e Wh (wattora, Ah × tensione " +
                    "nominale) - inserisci il valore riportato sull'etichetta della batteria o sulla pagina del prodotto. EggSPEED la usa " +
                    "insieme al consumo di potenza attuale per stimare l'autonomia nella schermata Cockpit. Sono dati ausiliari dell'app, " +
                    "non vengono inviati al controller.",
                nl = "De capaciteit van je pack in Ah (ampère-uur) en Wh (wattuur, Ah × nominale " +
                    "spanning) - voer de waarde van het batterij-etiket of de productpagina in. EggSPEED gebruikt dit samen " +
                    "met het huidige vermogensverbruik om de actieradius op het Cockpit-scherm te schatten. Dit zijn hulpgegevens " +
                    "van de app, ze worden niet naar de controller verzonden.",
                sv = "Ditt batteripakets kapacitet i Ah (amperetimmar) och Wh (wattimmar, Ah × nominell " +
                    "spänning) - ange värdet från batterietiketten eller produktsidan. EggSPEED använder detta tillsammans " +
                    "med den aktuella effektförbrukningen för att uppskatta räckvidden på skärmen Cockpit. Detta är hjälpdata " +
                    "på appsidan, det skickas inte till styrenheten.",
                cs = "Kapacita tvého packu v Ah (ampérhodinách) a Wh (watthodinách, Ah × jmenovité " +
                    "napětí) - zadej hodnotu z etikety baterie nebo stránky produktu. EggSPEED ji používá spolu " +
                    "s aktuálním odběrem výkonu k odhadu dojezdu na obrazovce Cockpit. Jsou to pomocné údaje aplikace, " +
                    "neodesílají se do řídicí jednotky.",
                sk = "Kapacita tvojho packu v Ah (ampérhodinách) a Wh (watthodinách, Ah × menovité " +
                    "napätie) - zadaj hodnotu z etikety batérie alebo stránky produktu. EggSPEED ju používa spolu " +
                    "s aktuálnym odberom výkonu na odhad dojazdu na obrazovke Cockpit. Sú to pomocné údaje aplikácie, " +
                    "neposielajú sa do riadiacej jednotky.",
            ),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CapacityField(
                    value = String.format("%.1f", state.capacityAh),
                    suffix = "Ah",
                    onValueChange = { text -> text.replace(',', '.').toDoubleOrNull()?.let(onCapacityAhChange) },
                    modifier = Modifier.weight(1f),
                )
                CapacityField(
                    value = String.format("%.0f", state.capacityWh),
                    suffix = "Wh",
                    onValueChange = { text -> text.replace(',', '.').toDoubleOrNull()?.let(onCapacityWhChange) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

/**
 * Opis kafelka "Napięcie baterii" - wstęp plus trzy osobne akapity (po jednym na próg napięcia),
 * każdy zaczynający się nazwą progu na zielono (ten sam odcień co trójkącik rozwijania), reszta
 * zdania białym tekstem - zamiast jednego zbitego akapitu jak wcześniej.
 */
@Composable
private fun BatteryVoltageDescription(isBbsFw: Boolean, cellCount: Int) {
    val nameStyle = SpanStyle(color = Tokens.Emerald, fontWeight = FontWeight.Bold)
    val bodyStyle = SpanStyle(color = Tokens.TextPrimary)

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            tr(
                "Trzy informacyjne progi napięcia - żadnego z nich nie ustawiasz tutaj.",
                "Three informational voltage thresholds - none of them is set here.",
                de = "Drei informative Spannungsschwellen - keine davon stellst du hier ein.",
                fr = "Trois seuils de tension informatifs - aucun d'eux ne se règle ici.",
                es = "Tres umbrales de voltaje informativos - ninguno se configura aquí.",
                pt = "Três limiares de tensão informativos - nenhum deles é definido aqui.",
                it = "Tre soglie di tensione informative - nessuna di esse viene impostata qui.",
                nl = "Drie informatieve spanningsdrempels - geen ervan wordt hier ingesteld.",
                sv = "Tre informativa spänningströsklar - ingen av dem ställs in här.",
                cs = "Tři informativní napěťové prahy - žádný z nich se zde nenastavuje.",
                sk = "Tri informatívne napäťové prahy - žiadny z nich sa tu nenastavuje.",
            ),
            fontFamily = Manrope, fontSize = 11.sp, lineHeight = 15.sp, color = Tokens.TextSecondary,
        )
        Text(
            buildAnnotatedString {
                withStyle(nameStyle) {
                    append(tr("Dolne napięcie odcięcia (LBP)", "Low voltage cutoff (LBP)", de = "Untere Abschaltspannung (LBP)", fr = "Coupure basse tension (LBP)", es = "Corte de baja tensión (LBP)", pt = "Corte de baixa tensão (LBP)", it = "Soglia di taglio bassa tensione (LBP)", nl = "Onderste afsluitspanning (LBP)", sv = "Nedre avstängningsspänning (LBP)", cs = "Dolní vypínací napětí (LBP)", sk = "Dolné vypínacie napätie (LBP)"))
                }
                withStyle(bodyStyle) {
                    append(
                        tr(
                            " - realny odczyt ze sterownika: próg, przy którym odcina zasilanie, żeby chronić " +
                                "ogniwa przed głębokim rozładowaniem. Edytowalne w zakładce " +
                                (if (isBbsFw) "bbs-fw - Ustawienia podstawowe." else "Bafang Basic."),
                            " - a real reading from the controller: the point at which it cuts power to protect " +
                                "the cells from deep discharge. Editable in the " +
                                (if (isBbsFw) "bbs-fw General tab." else "Bafang Basic tab."),
                            de = " - ein realer Messwert des Steuergeräts: der Schwellenwert, bei dem die Stromversorgung " +
                                "gekappt wird, um die Zellen vor Tiefentladung zu schützen. Editierbar im Tab " +
                                (if (isBbsFw) "bbs-fw General." else "Bafang Basic."),
                            fr = " - une lecture réelle du contrôleur : le seuil auquel il coupe l'alimentation pour protéger " +
                                "les cellules d'une décharge profonde. Modifiable dans l'onglet " +
                                (if (isBbsFw) "bbs-fw General." else "Bafang Basic."),
                            es = " - una lectura real del controlador: el umbral en el que corta la alimentación para proteger " +
                                "las celdas de una descarga profunda. Editable en la pestaña " +
                                (if (isBbsFw) "bbs-fw General." else "Bafang Basic."),
                            pt = " - uma leitura real do controlador: o ponto em que corta a alimentação para proteger " +
                                "as células de uma descarga profunda. Editável no separador " +
                                (if (isBbsFw) "bbs-fw General." else "Bafang Basic."),
                            it = " - una lettura reale dal controller: il punto in cui interrompe l'alimentazione per proteggere " +
                                "le celle da una scarica profonda. Modificabile nella scheda " +
                                (if (isBbsFw) "bbs-fw General." else "Bafang Basic."),
                            nl = " - een echte meting van de controller: het punt waarop de stroom wordt afgesneden om de cellen " +
                                "tegen diepe ontlading te beschermen. Aanpasbaar op het tabblad " +
                                (if (isBbsFw) "bbs-fw General." else "Bafang Basic."),
                            sv = " - en verklig avläsning från styrenheten: nivån där den bryter strömmen för att skydda " +
                                "cellerna från djup urladdning. Redigerbar på fliken " +
                                (if (isBbsFw) "bbs-fw General." else "Bafang Basic."),
                            cs = " - reálná hodnota z řídicí jednotky: práh, při kterém odpojí napájení, aby chránila " +
                                "články před hlubokým vybitím. Upravitelné na kartě " +
                                (if (isBbsFw) "bbs-fw General." else "Bafang Basic."),
                            sk = " - reálna hodnota z riadiacej jednotky: prah, pri ktorom odpojí napájanie, aby chránila " +
                                "články pred hlbokým vybitím. Upraviteľné na karte " +
                                (if (isBbsFw) "bbs-fw General." else "Bafang Basic."),
                        ),
                    )
                }
            },
            fontFamily = Manrope, fontSize = 11.sp, lineHeight = 15.sp,
        )
        Text(
            buildAnnotatedString {
                withStyle(nameStyle) { append(tr("Napięcie nominalne", "Nominal voltage", de = "Nennspannung", fr = "Tension nominale", es = "Voltaje nominal", pt = "Tensão nominal", it = "Tensione nominale", nl = "Nominale spanning", sv = "Nominell spänning", cs = "Jmenovité napětí", sk = "Menovité napätie")) }
                withStyle(bodyStyle) {
                    append(
                        tr(
                            " - wartość wyliczona z liczby cel (${cellCount}S x ok. 3,7V/ogniwo) - ta sama, co w " +
                                "kafelku \"Liczba cel\" niżej.",
                            " - calculated from cell count (${cellCount}S x approx. 3.7V/cell) - the same value " +
                                "shown in the \"Cell count\" tile below.",
                            de = " - berechnet aus der Zellenzahl (${cellCount}S x ca. 3,7V/Zelle) - derselbe Wert " +
                                "wie in der Kachel \"Zellenzahl\" unten.",
                            fr = " - calculée à partir du nombre de cellules (${cellCount}S x env. 3,7V/cellule) - la même valeur " +
                                "que dans la tuile \"Nombre de cellules\" ci-dessous.",
                            es = " - calculado a partir del número de celdas (${cellCount}S x aprox. 3,7V/celda) - el mismo valor " +
                                "que en la casilla \"Número de celdas\" de abajo.",
                            pt = " - valor calculado a partir do número de células (${cellCount}S x aprox. 3,7V/célula) - o mesmo " +
                                "valor mostrado no bloco \"Número de células\" abaixo.",
                            it = " - valore calcolato dal numero di celle (${cellCount}S x circa 3,7V/cella) - lo stesso valore " +
                                "mostrato nel riquadro \"Numero di celle\" sotto.",
                            nl = " - berekend op basis van het aantal cellen (${cellCount}S x ca. 3,7V/cel) - dezelfde waarde " +
                                "als in de tegel \"Aantal cellen\" hieronder.",
                            sv = " - beräknat från antal celler (${cellCount}S x ca 3,7V/cell) - samma värde " +
                                "som visas i rutan \"Antal celler\" nedan.",
                            cs = " - vypočteno z počtu článků (${cellCount}S x cca 3,7V/článek) - stejná hodnota " +
                                "jako v dlaždici \"Počet článků\" níže.",
                            sk = " - vypočítané z počtu článkov (${cellCount}S x cca 3,7V/článok) - rovnaká hodnota " +
                                "ako v dlaždici \"Počet článkov\" nižšie.",
                        ),
                    )
                }
            },
            fontFamily = Manrope, fontSize = 11.sp, lineHeight = 15.sp,
        )
        Text(
            buildAnnotatedString {
                withStyle(nameStyle) { append(tr("Górny limit naładowania", "Upper charge limit", de = "Obere Ladegrenze", fr = "Limite de charge haute", es = "Límite superior de carga", pt = "Limite superior de carga", it = "Limite di carica superiore", nl = "Bovengrens laadspanning", sv = "Övre laddningsgräns", cs = "Horní limit nabíjení", sk = "Horný limit nabíjania")) }
                withStyle(bodyStyle) {
                    append(
                        tr(
                            " - wartość wyliczona ze specyfikacji ogniw Li-ion (4,2V x liczba cel). Nominalne i " +
                                "górny limit to orientacyjne estymaty - rzeczywiste napięcie pod obciążeniem " +
                                "zmienia się w zależności od poziomu naładowania.",
                            " - calculated from Li-ion cell spec (4.2V x cell count). Nominal and upper limit are " +
                                "approximate estimates - the real voltage under load varies with charge level.",
                            de = " - berechnet aus der Li-Ion-Zellenspezifikation (4,2V x Zellenzahl). Nennwert und " +
                                "obere Grenze sind grobe Schätzungen - die reale Spannung unter Last variiert " +
                                "je nach Ladezustand.",
                            fr = " - calculée à partir des spécifications des cellules Li-ion (4,2V x nombre de cellules). La valeur " +
                                "nominale et la limite haute sont des estimations approximatives - la tension réelle en charge " +
                                "varie selon le niveau de charge.",
                            es = " - calculado según la especificación de las celdas Li-ion (4,2V x número de celdas). El valor " +
                                "nominal y el límite superior son estimaciones aproximadas - el voltaje real bajo carga " +
                                "varía según el nivel de carga.",
                            pt = " - valor calculado a partir da especificação das células Li-ion (4,2V x número de células). O " +
                                "valor nominal e o limite superior são estimativas aproximadas - a tensão real sob carga " +
                                "varia consoante o nível de carga.",
                            it = " - valore calcolato dalle specifiche delle celle Li-ion (4,2V x numero di celle). Il valore " +
                                "nominale e il limite superiore sono stime approssimative - la tensione reale sotto carico " +
                                "varia in base al livello di carica.",
                            nl = " - berekend op basis van de Li-ion celspecificatie (4,2V x aantal cellen). Nominale waarde en " +
                                "bovengrens zijn ruwe schattingen - de werkelijke spanning onder belasting varieert met het laadniveau.",
                            sv = " - beräknat från Li-ion-cellernas specifikation (4,2V x antal celler). Nominellt värde och " +
                                "övre gräns är ungefärliga uppskattningar - den verkliga spänningen under belastning varierar med laddningsnivån.",
                            cs = " - vypočteno ze specifikace Li-ion článků (4,2V x počet článků). Jmenovitá hodnota a " +
                                "horní limit jsou přibližné odhady - skutečné napětí pod zátěží se mění podle úrovně nabití.",
                            sk = " - vypočítané zo špecifikácie Li-ion článkov (4,2V x počet článkov). Menovitá hodnota a " +
                                "horný limit sú približné odhady - skutočné napätie pod záťažou sa mení podľa úrovne nabitia.",
                        ),
                    )
                }
            },
            fontFamily = Manrope, fontSize = 11.sp, lineHeight = 15.sp,
        )
    }
}

/** Jeden z trzech wierszy w kafelku "Napięcie baterii" - etykieta po lewej, wartość po prawej, jak w oryginalnych osobnych kafelkach. */
@Composable
private fun VoltageReadout(label: String, valueText: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Tokens.Elevated, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label.uppercase(), fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 11.sp,
            letterSpacing = 0.5.sp, color = Tokens.TextBright80, modifier = Modifier.weight(1f),
        )
        Text(valueText, fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Tokens.TextPrimary)
    }
}

@Composable
private fun CapacityField(value: String, suffix: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    // Bufor lokalny odłączony od zewnętrznej wartości podczas edycji - bez tego kasowanie/wpisywanie
    // niepełnej liczby ("12." albo puste pole) było natychmiast nadpisywane sformatowaną wartością
    // z ViewModel (bo toDoubleOrNull() na niepełnym tekście zwraca null i nic się nie zmieniało).
    var isFocused by remember { mutableStateOf(false) }
    var localText by remember { mutableStateOf(value) }
    LaunchedEffect(value, isFocused) {
        if (!isFocused) localText = value
    }

    Row(
        modifier = modifier
            .background(Tokens.Elevated, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = localText,
            onValueChange = { text ->
                localText = text
                onValueChange(text)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = TextStyle(color = Tokens.TextPrimary, fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 16.sp),
            cursorBrush = SolidColor(Tokens.Blue),
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { isFocused = it.isFocused },
        )
        Text(suffix, fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextSecondary)
    }
}
