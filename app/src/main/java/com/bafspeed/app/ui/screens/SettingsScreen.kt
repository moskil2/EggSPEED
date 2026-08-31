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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bafspeed.app.FirmwareType
import com.bafspeed.app.SpeedUnit
import com.bafspeed.app.UiState
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.ui.components.ExpandableParamTile
import com.bafspeed.app.ui.components.MicroLabel
import com.bafspeed.app.ui.components.SegmentedControl
import com.bafspeed.app.ui.components.ToggleRow
import com.bafspeed.app.ui.components.TokenCard
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens

@Composable
fun SettingsScreen(
    state: UiState,
    onUnitsChange: (SpeedUnit) -> Unit,
    onOdoOffsetChange: (Double) -> Unit,
    onFirmwareTypeChange: (FirmwareType) -> Unit,
    onFastCockpitRefreshChange: (Boolean) -> Unit,
    onGpsSpeedChange: (Boolean) -> Unit,
) {
    val unit = state.units

    Column(
        Modifier
            .fillMaxSize()
            .background(Tokens.Bg)
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = 16.dp)),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        MicroLabel(tr("Firmware sterownika", "Controller firmware", de = "Steuergerät-Firmware", fr = "Firmware du contrôleur", es = "Firmware del controlador", pt = "Firmware do controlador", it = "Firmware del controller", nl = "Controller-firmware", sv = "Styrenhetens firmware", cs = "Firmware řadiče", sk = "Firmware radiča"))
        var firmwareInfoExpanded by remember { mutableStateOf(false) }
        TokenCard(borderColor = Tokens.WhiteBorder) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(tr("Firmware", "Firmware", de = "Firmware", fr = "Firmware", es = "Firmware", pt = "Firmware", it = "Firmware", nl = "Firmware", sv = "Firmware", cs = "Firmware", sk = "Firmvér"), fontFamily = Manrope, fontSize = 14.sp, color = Tokens.TextPrimary, modifier = Modifier.weight(1f))
                SegmentedControl(
                    options = listOf(tr("OEM Bafang", "OEM Bafang", de = "OEM Bafang", fr = "OEM Bafang", es = "OEM Bafang", pt = "OEM Bafang", it = "OEM Bafang", nl = "OEM Bafang", sv = "OEM Bafang", cs = "OEM Bafang", sk = "OEM Bafang"), "BBS-FW"),
                    selectedIndex = if (state.firmwareType == FirmwareType.BBS_FW) 1 else 0,
                    onSelect = { onFirmwareTypeChange(if (it == 1) FirmwareType.BBS_FW else FirmwareType.OEM_BAFANG) },
                    modifier = Modifier.width(180.dp),
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier.fillMaxWidth().clickable { firmwareInfoExpanded = !firmwareInfoExpanded },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    tr("Co to znaczy?", "What does this mean?", de = "Was bedeutet das?", fr = "Qu'est-ce que cela signifie ?", es = "¿Qué significa esto?", pt = "O que significa isto?", it = "Cosa significa?", nl = "Wat betekent dit?", sv = "Vad betyder detta?", cs = "Co to znamená?", sk = "Čo to znamená?"),
                    fontFamily = Manrope, fontSize = 11.sp, color = Tokens.TextSecondary, modifier = Modifier.weight(1f),
                )
                Text(if (firmwareInfoExpanded) "▲" else "▼", fontFamily = Manrope, fontSize = 14.sp, color = Tokens.Emerald)
            }
            if (firmwareInfoExpanded) {
                Spacer(Modifier.height(8.dp))
                FirmwareDescriptionParagraph(
                    label = "OEM Bafang",
                    text = tr(
                        "fabryczny, zamknięty firmware sterowników Bafang BBS01/BBS02/BBSHD. Komunikuje się protokołem Bafang Configuration Tool, którego apka używa domyślnie.",
                        "the factory, closed-source firmware that Bafang BBS01/BBS02/BBSHD controllers ship with by default. Speaks the Bafang Configuration Tool protocol, which the app uses by default.",
                        de = "das werkseitige, closed-source Firmware, mit dem Bafang BBS01/BBS02/BBSHD-Steuergeräte standardmäßig ausgeliefert werden. Spricht das Bafang Configuration Tool-Protokoll, das die App standardmäßig verwendet.",
                        fr = "le firmware d'origine, propriétaire, livré par défaut avec les contrôleurs Bafang BBS01/BBS02/BBSHD. Parle le protocole Bafang Configuration Tool, utilisé par défaut par l'application.",
                        es = "el firmware de fábrica, de código cerrado, con el que vienen los controladores Bafang BBS01/BBS02/BBSHD por defecto. Habla el protocolo Bafang Configuration Tool, que la app usa por defecto.",
                        pt = "o firmware de fábrica, de código fechado, com que os controladores Bafang BBS01/BBS02/BBSHD vêm equipados por defeito. Fala o protocolo Bafang Configuration Tool, que a app usa por defeito.",
                        it = "il firmware di fabbrica, a codice chiuso, con cui i controller Bafang BBS01/BBS02/BBSHD vengono forniti per impostazione predefinita. Parla il protocollo Bafang Configuration Tool, usato di default dall'app.",
                        nl = "de fabrieksfirmware met gesloten broncode waarmee Bafang BBS01/BBS02/BBSHD-controllers standaard worden geleverd. Spreekt het Bafang Configuration Tool-protocol, dat de app standaard gebruikt.",
                        sv = "den slutna fabriksfirmware som Bafang BBS01/BBS02/BBSHD-styrenheter levereras med som standard. Talar Bafang Configuration Tool-protokollet, som appen använder som standard.",
                        cs = "tovární firmware s uzavřeným zdrojovým kódem, se kterým se řadiče Bafang BBS01/BBS02/BBSHD standardně dodávají. Komunikuje protokolem Bafang Configuration Tool, který aplikace používá ve výchozím nastavení.",
                        sk = "továrenský firmvér s uzavretým zdrojovým kódom, s ktorým sa radiče Bafang BBS01/BBS02/BBSHD štandardne dodávajú. Komunikuje protokolom Bafang Configuration Tool, ktorý aplikácia používa predvolene.",
                    ),
                )
                Spacer(Modifier.height(8.dp))
                FirmwareDescriptionParagraph(
                    label = "BBS-FW",
                    text = tr(
                        "(github.com/danielnilsson9/bbs-fw) - otwarte, alternatywne firmware, które można samodzielnie wgrać na te same sterowniki w miejsce fabrycznego. Ma WŁASNY, INNY protokół konfiguracji, więc przełącznik zmienia, jakich ramek apka używa do rozmowy ze sterownikiem.",
                        "(github.com/danielnilsson9/bbs-fw) - open-source, alternative firmware you can flash yourself onto the same controllers in place of the factory one. Has its OWN, DIFFERENT configuration protocol, so this switch changes which frames the app uses to talk to the controller.",
                        de = "(github.com/danielnilsson9/bbs-fw) - offenes, alternatives Firmware, das du selbst auf dieselben Steuergeräte statt des werkseitigen aufspielen kannst. Hat ein EIGENES, ANDERES Konfigurationsprotokoll, daher ändert dieser Schalter, welche Frames die App zur Kommunikation mit dem Steuergerät verwendet.",
                        fr = "(github.com/danielnilsson9/bbs-fw) - firmware alternatif open-source que vous pouvez installer vous-même sur les mêmes contrôleurs à la place de celui d'origine. Possède son PROPRE protocole de configuration, DIFFÉRENT, donc ce commutateur change les trames utilisées par l'application pour communiquer avec le contrôleur.",
                        es = "(github.com/danielnilsson9/bbs-fw) - firmware alternativo de código abierto que puedes instalar tú mismo en los mismos controladores en lugar del original. Tiene su PROPIO protocolo de configuración, DISTINTO, así que este interruptor cambia qué tramas usa la app para hablar con el controlador.",
                        pt = "(github.com/danielnilsson9/bbs-fw) - firmware alternativo de código aberto que podes instalar tu mesmo nos mesmos controladores em vez do de fábrica. Tem o SEU PRÓPRIO protocolo de configuração, DIFERENTE, por isso este interruptor muda que tramas a app usa para comunicar com o controlador.",
                        it = "(github.com/danielnilsson9/bbs-fw) - firmware alternativo open-source che puoi installare tu stesso sugli stessi controller al posto di quello di fabbrica. Ha un protocollo di configurazione PROPRIO e DIVERSO, quindi questo interruttore cambia quali frame l'app usa per comunicare con il controller.",
                        nl = "(github.com/danielnilsson9/bbs-fw) - open-source, alternatieve firmware die je zelf op dezelfde controllers kunt flashen in plaats van de fabrieksversie. Heeft een EIGEN, ANDER configuratieprotocol, dus deze schakelaar bepaalt welke frames de app gebruikt om met de controller te communiceren.",
                        sv = "(github.com/danielnilsson9/bbs-fw) - öppen källkod, alternativ firmware som du själv kan flasha på samma styrenheter i stället för fabriksversionen. Har sitt EGET, ANNAT konfigurationsprotokoll, så denna reglage ändrar vilka ramar appen använder för att prata med styrenheten.",
                        cs = "(github.com/danielnilsson9/bbs-fw) - open-source, alternativní firmware, který si můžeš sám nahrát do stejných řadičů namísto továrního. Má VLASTNÍ, JINÝ konfigurační protokol, takže tento přepínač mění, jaké rámce aplikace používá ke komunikaci s řadičem.",
                        sk = "(github.com/danielnilsson9/bbs-fw) - open-source, alternatívny firmvér, ktorý si môžeš sám nahrať do rovnakých radičov namiesto továrenského. Má VLASTNÝ, INÝ konfiguračný protokol, takže tento prepínač mení, aké rámce aplikácia používa na komunikáciu s radičom.",
                    ),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    tr(
                        "Wybierz opcję zgodną z tym, co faktycznie masz wgrane na sterowniku. Zmiana wymaga ponownego połączenia.",
                        "Pick whichever matches what's actually flashed on your controller. Changing it requires reconnecting.",
                        de = "Wähle die Option, die tatsächlich auf deinem Steuergerät aufgespielt ist. Die Änderung erfordert eine erneute Verbindung.",
                        fr = "Choisissez l'option correspondant à ce qui est réellement installé sur votre contrôleur. Le changement nécessite une reconnexion.",
                        es = "Elige la opción que coincida con lo que realmente tienes instalado en tu controlador. El cambio requiere reconectar.",
                        pt = "Escolhe a opção correspondente ao que está realmente instalado no teu controlador. A alteração requer nova ligação.",
                        it = "Scegli l'opzione corrispondente a ciò che è effettivamente installato sul tuo controller. La modifica richiede una riconnessione.",
                        nl = "Kies de optie die overeenkomt met wat er daadwerkelijk op je controller is geflasht. Wijzigen vereist opnieuw verbinden.",
                        sv = "Välj alternativet som matchar det som faktiskt är flashat på din styrenhet. Ändring kräver återanslutning.",
                        cs = "Vyber možnost, která odpovídá tomu, co je skutečně nahráno v tvém řadiči. Změna vyžaduje opětovné připojení.",
                        sk = "Vyber možnosť, ktorá zodpovedá tomu, čo je skutočne nahrané v tvojom radiči. Zmena vyžaduje opätovné pripojenie.",
                    ),
                    fontFamily = Manrope, fontSize = 11.sp, color = Tokens.TextSecondary,
                )
            }
        }

        ExpandableParamTile(
            label = tr("Szybkie odświeżanie Kokpitu", "Fast Cockpit refresh", de = "Schnelle Cockpit-Aktualisierung", fr = "Rafraîchissement rapide (Cockpit)", es = "Actualización rápida (Cockpit)", pt = "Atualização rápida do Cockpit", it = "Aggiornamento rapido Cockpit", nl = "Snelle Cockpit-verversing", sv = "Snabb uppdatering av Cockpit", cs = "Rychlé obnovování Cockpitu", sk = "Rýchle obnovovanie Cockpitu"),
            valueLabel = if (state.fastCockpitRefresh) {
                tr("Włączone", "On", de = "An", fr = "Activé", es = "Activado", pt = "Ativado", it = "Attivo", nl = "Aan", sv = "På", cs = "Zapnuto", sk = "Zapnuté")
            } else {
                tr("Wyłączone", "Off", de = "Aus", fr = "Désactivé", es = "Desactivado", pt = "Desativado", it = "Disattivo", nl = "Uit", sv = "Av", cs = "Vypnuto", sk = "Vypnuté")
            },
            description = tr(
                "Skraca odstępy między odczytami prędkości/prądu w pętli telemetrii, żeby wartości w Kokpicie " +
                    "zmieniały się płynniej zamiast skokowo. Może nie działać poprawnie na niektórych kontrolerach " +
                    "(np. brak/utrata odczytów) - jeśli po włączeniu Kokpit zacznie się zacinać lub pokazywać zera, wyłącz tę opcję.",
                "Shortens the gaps between speed/current reads in the telemetry loop, so Cockpit values change " +
                    "smoothly instead of in visible steps. May not work correctly on some controllers (e.g. missing/dropped " +
                    "readings) - if the Cockpit starts stuttering or showing zeros after enabling this, turn it back off.",
                de = "Verkürzt die Abstände zwischen Geschwindigkeits-/Stromabfragen in der Telemetrieschleife, damit sich die " +
                    "Cockpit-Werte flüssig statt sprunghaft ändern. Funktioniert bei manchen Steuergeräten evtl. nicht korrekt " +
                    "(z. B. fehlende/verlorene Messwerte) - wenn das Cockpit danach ruckelt oder Nullen zeigt, schalte die Option wieder aus.",
                fr = "Raccourcit les intervalles entre les lectures de vitesse/courant dans la boucle de télémétrie, pour que les " +
                    "valeurs du Cockpit changent en douceur plutôt que par à-coups. Peut ne pas fonctionner correctement sur certains " +
                    "contrôleurs (lectures manquantes/perdues) - si le Cockpit se met à ramer ou affiche des zéros, désactivez cette option.",
                es = "Acorta los intervalos entre lecturas de velocidad/corriente en el bucle de telemetría, para que los valores " +
                    "del Cockpit cambien con suavidad en vez de a saltos. Puede no funcionar bien en algunos controladores " +
                    "(lecturas perdidas/faltantes) - si el Cockpit empieza a fallar o muestra ceros, desactiva esta opción.",
                pt = "Reduz os intervalos entre as leituras de velocidade/corrente no ciclo de telemetria, para que os valores " +
                    "do Cockpit mudem de forma suave em vez de aos saltos. Pode não funcionar corretamente em alguns controladores " +
                    "(por ex. leituras em falta/perdidas) - se o Cockpit começar a engasgar ou mostrar zeros depois de ativar isto, desativa a opção novamente.",
                it = "Riduce gli intervalli tra le letture di velocità/corrente nel ciclo di telemetria, in modo che i valori " +
                    "del Cockpit cambino in modo fluido anziché a scatti. Potrebbe non funzionare correttamente su alcuni " +
                    "controller (ad es. letture mancanti/perse) - se il Cockpit inizia a bloccarsi o a mostrare zeri dopo aver attivato questa opzione, disattivala di nuovo.",
                nl = "Verkort de intervallen tussen snelheids-/stroommetingen in de telemetrielus, zodat de Cockpit-waarden " +
                    "vloeiend veranderen in plaats van in zichtbare stappen. Werkt mogelijk niet goed op sommige controllers " +
                    "(bijv. ontbrekende/verloren metingen) - als de Cockpit hierna gaat haperen of nullen toont, zet deze optie dan weer uit.",
                sv = "Förkortar intervallen mellan hastighets-/strömavläsningar i telemetrislingan, så att Cockpit-värdena " +
                    "ändras jämnt istället för i synliga steg. Kanske inte fungerar korrekt på vissa styrenheter " +
                    "(t.ex. saknade/tappade avläsningar) - om Cockpit börjar hacka eller visa nollor efter aktivering, stäng av den igen.",
                cs = "Zkracuje intervaly mezi odečty rychlosti/proudu ve smyčce telemetrie, aby se hodnoty v Cockpitu " +
                    "měnily plynule místo skokově. Nemusí správně fungovat na některých řadičích " +
                    "(např. chybějící/ztracené odečty) - pokud po zapnutí Cockpit začne sekat nebo zobrazovat nuly, tuto možnost znovu vypni.",
                sk = "Skracuje intervaly medzi odčítaniami rýchlosti/prúdu v slučke telemetrie, aby sa hodnoty v Cockpite " +
                    "menili plynulo namiesto skokovo. Nemusí správne fungovať na niektorých radičoch " +
                    "(napr. chýbajúce/stratené odčítania) - ak po zapnutí Cockpit začne sekať alebo zobrazovať nuly, túto možnosť znova vypni.",
            ),
        ) {
            ToggleRow(
                label = tr("Szybkie odświeżanie", "Fast refresh", de = "Schnelle Aktualisierung", fr = "Rafraîchissement rapide", es = "Actualización rápida", pt = "Atualização rápida", it = "Aggiornamento rapido", nl = "Snelle verversing", sv = "Snabb uppdatering", cs = "Rychlé obnovování", sk = "Rýchle obnovovanie"),
                checked = state.fastCockpitRefresh,
                onCheckedChange = onFastCockpitRefreshChange,
            )
        }

        ExpandableParamTile(
            label = "GPS Speed",
            valueLabel = if (state.gpsSpeedEnabled) {
                tr("Włączone", "On", de = "An", fr = "Activé", es = "Activado", pt = "Ativado", it = "Attivo", nl = "Aan", sv = "På", cs = "Zapnuto", sk = "Zapnuté")
            } else {
                tr("Wyłączone", "Off", de = "Aus", fr = "Désactivé", es = "Desactivado", pt = "Desativado", it = "Disattivo", nl = "Uit", sv = "Av", cs = "Vypnuto", sk = "Vypnuté")
            },
            description = tr(
                "Dodaje na Kokpicie małą adnotację z prędkością odczytaną bezpośrednio z GPS telefonu, obok " +
                    "głównego odczytu prędkości ze sterownika - przydaje się do porównania obu wartości. Wymaga " +
                    "zgody na dostęp do lokalizacji (poprosimy o nią dopiero po włączeniu tej opcji).",
                "Adds a small annotation on the Cockpit with the speed read directly from the phone's GPS, " +
                    "next to the main speed reading from the controller - useful for comparing the two. Requires " +
                    "location permission (we'll ask for it only once you enable this option).",
                de = "Fügt im Cockpit eine kleine Anmerkung mit der direkt vom Telefon-GPS gelesenen Geschwindigkeit hinzu, neben " +
                    "der Hauptanzeige vom Steuergerät - nützlich zum Vergleich beider Werte. Benötigt die Standortberechtigung " +
                    "(wir fragen erst danach, wenn du diese Option aktivierst).",
                fr = "Ajoute sur le Cockpit une petite annotation avec la vitesse lue directement depuis le GPS du téléphone, à côté " +
                    "de la lecture principale du contrôleur - utile pour comparer les deux. Nécessite l'autorisation de localisation " +
                    "(nous ne la demanderons qu'une fois cette option activée).",
                es = "Añade en el Cockpit una pequeña anotación con la velocidad leída directamente del GPS del teléfono, junto " +
                    "a la lectura principal del controlador - útil para comparar ambos valores. Requiere permiso de ubicación " +
                    "(lo pediremos solo al activar esta opción).",
                pt = "Adiciona uma pequena anotação no Cockpit com a velocidade lida diretamente do GPS do telemóvel, junto " +
                    "à leitura principal de velocidade do controlador - útil para comparar os dois valores. Requer permissão de localização " +
                    "(só a pediremos depois de ativares esta opção).",
                it = "Aggiunge una piccola annotazione nel Cockpit con la velocità letta direttamente dal GPS del telefono, accanto " +
                    "alla lettura principale della velocità dal controller - utile per confrontare i due valori. Richiede l'autorizzazione alla posizione " +
                    "(la chiederemo solo dopo aver attivato questa opzione).",
                nl = "Voegt een kleine annotatie toe op de Cockpit met de snelheid rechtstreeks van de gps van de telefoon, naast " +
                    "de hoofdaflezing van de controller - handig om beide waarden te vergelijken. Vereist locatietoestemming " +
                    "(we vragen hierom pas nadat je deze optie inschakelt).",
                sv = "Lägger till en liten anteckning på Cockpit med hastigheten avläst direkt från telefonens GPS, bredvid " +
                    "huvudavläsningen från styrenheten - användbart för att jämföra de två. Kräver platsbehörighet " +
                    "(vi frågar först när du aktiverar detta alternativ).",
                cs = "Přidá do Cockpitu malou poznámku s rychlostí čtenou přímo z GPS telefonu, vedle " +
                    "hlavního odečtu z řadiče - užitečné pro porovnání obou hodnot. Vyžaduje oprávnění k poloze " +
                    "(požádáme o něj až po zapnutí této možnosti).",
                sk = "Pridá do Cockpitu malú poznámku s rýchlosťou čítanou priamo z GPS telefónu, vedľa " +
                    "hlavného odčítania z radiča - užitočné na porovnanie oboch hodnôt. Vyžaduje povolenie na polohu " +
                    "(požiadame oň až po zapnutí tejto možnosti).",
            ),
        ) {
            ToggleRow(
                label = "GPS Speed",
                checked = state.gpsSpeedEnabled,
                onCheckedChange = onGpsSpeedChange,
            )
        }

        MicroLabel(tr("Aplikacja", "Application", de = "Anwendung", fr = "Application", es = "Aplicación", pt = "Aplicação", it = "Applicazione", nl = "Applicatie", sv = "Applikation", cs = "Aplikace", sk = "Aplikácia"))
        TokenCard(borderColor = Tokens.WhiteBorder) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(tr("Jednostki", "Units", de = "Einheiten", fr = "Unités", es = "Unidades", pt = "Unidades", it = "Unità", nl = "Eenheden", sv = "Enheter", cs = "Jednotky", sk = "Jednotky"), fontFamily = Manrope, fontSize = 14.sp, color = Tokens.TextPrimary, modifier = Modifier.weight(1f))
                SegmentedControl(
                    options = listOf("km/h", "mph"),
                    selectedIndex = if (state.units == SpeedUnit.MPH) 1 else 0,
                    onSelect = { onUnitsChange(if (it == 1) SpeedUnit.MPH else SpeedUnit.KMH) },
                    modifier = Modifier.width(180.dp),
                )
            }
        }

        ExpandableParamTile(
            label = tr("Przebieg (ODO)", "Odometer (ODO)", de = "Kilometerstand (ODO)", fr = "Compteur (ODO)", es = "Odómetro (ODO)", pt = "Odómetro (ODO)", it = "Contachilometri (ODO)", nl = "Kilometerteller (ODO)", sv = "Trippmätare (ODO)", cs = "Tachometr (ODO)", sk = "Tachometer (ODO)"),
            valueLabel = "",
            description = tr(
                "Jeśli migrujesz z innego wyświetlacza i chcesz zachować dotychczasowy przebieg, " +
                    "wpisz go tutaj jako punkt startowy. EggSPEED doda do niego dystans naliczany na bieżąco w " +
                    "Kokpicie (obecnie ${String.format("%.1f", unit.fromKmh(state.tripKm))} ${unit.distanceLabel} od uruchomienia tej sesji Kokpitu). " +
                    "Wartość zapisuje się trwale na telefonie.",
                "If you're migrating from another display and want to keep your existing mileage, " +
                    "enter it here as a starting point. EggSPEED will add to it the distance tracked live in the " +
                    "Cockpit (currently ${String.format("%.1f", unit.fromKmh(state.tripKm))} ${unit.distanceLabel} since this Cockpit session started). " +
                    "The value is saved permanently on your phone.",
                de = "Wenn du von einem anderen Display migrierst und deinen bisherigen Kilometerstand behalten möchtest, " +
                    "gib ihn hier als Startwert ein. EggSPEED addiert dazu die im Cockpit laufend erfasste Distanz " +
                    "(aktuell ${String.format("%.1f", unit.fromKmh(state.tripKm))} ${unit.distanceLabel} seit Start dieser Cockpit-Sitzung). " +
                    "Der Wert wird dauerhaft auf dem Telefon gespeichert.",
                fr = "Si vous migrez depuis un autre affichage et souhaitez conserver votre kilométrage actuel, " +
                    "saisissez-le ici comme point de départ. EggSPEED y ajoutera la distance suivie en direct dans le " +
                    "Cockpit (actuellement ${String.format("%.1f", unit.fromKmh(state.tripKm))} ${unit.distanceLabel} depuis le début de cette session Cockpit). " +
                    "La valeur est enregistrée durablement sur votre téléphone.",
                es = "Si migras desde otra pantalla y quieres conservar tu kilometraje actual, " +
                    "introdúcelo aquí como punto de partida. EggSPEED le sumará la distancia registrada en vivo en el " +
                    "Cockpit (actualmente ${String.format("%.1f", unit.fromKmh(state.tripKm))} ${unit.distanceLabel} desde el inicio de esta sesión de Cockpit). " +
                    "El valor se guarda de forma permanente en tu teléfono.",
                pt = "Se estás a migrar de outro visor e queres manter a quilometragem existente, " +
                    "insere-a aqui como ponto de partida. O EggSPEED irá somar-lhe a distância registada ao vivo no " +
                    "Cockpit (atualmente ${String.format("%.1f", unit.fromKmh(state.tripKm))} ${unit.distanceLabel} desde o início desta sessão do Cockpit). " +
                    "O valor é guardado permanentemente no teu telemóvel.",
                it = "Se stai migrando da un altro display e vuoi mantenere il chilometraggio esistente, " +
                    "inseriscilo qui come punto di partenza. EggSPEED vi aggiungerà la distanza rilevata in tempo reale nel " +
                    "Cockpit (attualmente ${String.format("%.1f", unit.fromKmh(state.tripKm))} ${unit.distanceLabel} dall'inizio di questa sessione Cockpit). " +
                    "Il valore viene salvato permanentemente sul tuo telefono.",
                nl = "Als je migreert van een andere display en je bestaande kilometerstand wilt behouden, " +
                    "voer deze hier in als startpunt. EggSPEED telt hier de afstand bij op die live wordt bijgehouden in de " +
                    "Cockpit (momenteel ${String.format("%.1f", unit.fromKmh(state.tripKm))} ${unit.distanceLabel} sinds het begin van deze Cockpit-sessie). " +
                    "De waarde wordt permanent op je telefoon opgeslagen.",
                sv = "Om du migrerar från en annan display och vill behålla din befintliga körsträcka, " +
                    "ange den här som startpunkt. EggSPEED lägger till den sträcka som spåras live i " +
                    "Cockpit (för närvarande ${String.format("%.1f", unit.fromKmh(state.tripKm))} ${unit.distanceLabel} sedan denna Cockpit-session startade). " +
                    "Värdet sparas permanent på din telefon.",
                cs = "Pokud migruješ z jiného displeje a chceš si zachovat stávající nájezd, " +
                    "zadej ho zde jako výchozí bod. EggSPEED k němu přičte vzdálenost sledovanou v reálném čase v " +
                    "Cockpitu (aktuálně ${String.format("%.1f", unit.fromKmh(state.tripKm))} ${unit.distanceLabel} od začátku této relace Cockpitu). " +
                    "Hodnota se trvale uloží do telefonu.",
                sk = "Ak migruješ z iného displeja a chceš si zachovať existujúci nájazd, " +
                    "zadaj ho tu ako východiskový bod. EggSPEED k nemu pripočíta vzdialenosť sledovanú v reálnom čase v " +
                    "Cockpite (aktuálne ${String.format("%.1f", unit.fromKmh(state.tripKm))} ${unit.distanceLabel} od začiatku tejto relácie Cockpitu). " +
                    "Hodnota sa trvalo uloží do telefónu.",
            ),
        ) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${String.format("%.1f", unit.fromKmh(state.totalOdoKm))} ${unit.distanceLabel}",
                    fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Tokens.TextPrimary,
                )
                Spacer(Modifier.height(10.dp))
                OdoField(
                    value = unit.fromKmh(state.odoOffsetKm),
                    unitLabel = unit.distanceLabel,
                    onValueChange = { onOdoOffsetChange(unit.toKmh(it)) },
                )
            }
        }

        MicroLabel(tr("Połączenie", "Connection", de = "Verbindung", fr = "Connexion", es = "Conexión", pt = "Ligação", it = "Connessione", nl = "Verbinding", sv = "Anslutning", cs = "Připojení", sk = "Pripojenie"))
        TokenCard(borderColor = Tokens.WhiteBorder, modifier = Modifier.alpha(0.55f)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(tr("Typ połączenia", "Connection type", de = "Verbindungstyp", fr = "Type de connexion", es = "Tipo de conexión", pt = "Tipo de ligação", it = "Tipo di connessione", nl = "Verbindingstype", sv = "Anslutningstyp", cs = "Typ připojení", sk = "Typ pripojenia"), fontFamily = Manrope, fontSize = 14.sp, color = Tokens.TextPrimary)
                    Text(
                        tr("Bluetooth - wkrótce", "Bluetooth - coming soon", de = "Bluetooth - demnächst", fr = "Bluetooth - bientôt", es = "Bluetooth - próximamente", pt = "Bluetooth - brevemente", it = "Bluetooth - prossimamente", nl = "Bluetooth - binnenkort", sv = "Bluetooth - kommer snart", cs = "Bluetooth - již brzy", sk = "Bluetooth - už čoskoro"),
                        fontFamily = Manrope, fontSize = 11.sp, color = Tokens.TextSecondary,
                    )
                }
                SegmentedControl(
                    options = listOf("USB", "Bluetooth"),
                    selectedIndex = 0,
                    onSelect = {},
                    modifier = Modifier.width(180.dp),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

/** Akapit opisu firmware - etykieta ("OEM Bafang"/"BBS-FW") na zielono, reszta zdania normalnym kolorem. */
@Composable
private fun FirmwareDescriptionParagraph(label: String, text: String) {
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(color = Tokens.Emerald, fontWeight = FontWeight.Bold)) { append(label) }
            append(" $text")
        },
        fontFamily = Manrope, fontSize = 11.sp, color = Tokens.TextSecondary,
    )
}

@Composable
private fun OdoField(value: Double, unitLabel: String, onValueChange: (Double) -> Unit) {
    // Bufor lokalny odlaczony od zewnetrznej wartosci podczas edycji - ten sam wzorzec
    // co CapacityField w BatteryScreen.kt (bez tego kasowanie/wpisywanie niepelnej liczby
    // jest natychmiast nadpisywane sformatowana wartoscia z ViewModel).
    var isFocused by remember { mutableStateOf(false) }
    var localText by remember { mutableStateOf(String.format("%.1f", value)) }
    LaunchedEffect(value, isFocused) {
        if (!isFocused) localText = String.format("%.1f", value)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Tokens.Elevated, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = localText,
            onValueChange = { text ->
                localText = text
                text.replace(',', '.').toDoubleOrNull()?.let(onValueChange)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = TextStyle(color = Tokens.TextPrimary, fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 16.sp),
            cursorBrush = SolidColor(Tokens.Blue),
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { isFocused = it.isFocused },
        )
        Text(unitLabel, fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextSecondary)
    }
}
