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
    onLargeCockpitDigitsChange: (Boolean) -> Unit,
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
                Text(tr("Motyw", "Theme", de = "Design", fr = "Thème", es = "Tema", pt = "Tema", it = "Tema", nl = "Thema", sv = "Tema", cs = "Motiv", sk = "Motív", da = "Tema", ru = "Тема"), fontFamily = Manrope, fontSize = 14.sp, color = Tokens.TextPrimary, modifier = Modifier.weight(1f))
                SegmentedControl(
                    options = listOf(
                        tr("Ciemny", "Dark", de = "Dunkel", fr = "Sombre", es = "Oscuro", pt = "Escuro", it = "Scuro", nl = "Donker", sv = "Mörkt", cs = "Tmavý", sk = "Tmavý", da = "Mørk", ru = "Тёмный"),
                        tr("Jasny", "Light", de = "Hell", fr = "Clair", es = "Claro", pt = "Claro", it = "Chiaro", nl = "Licht", sv = "Ljust", cs = "Světlý", sk = "Svetlý", da = "Lys", ru = "Светлый"),
                    ),
                    selectedIndex = if (state.lightMode) 1 else 0,
                    onSelect = { onLightModeChange(it == 1) },
                    modifier = Modifier.width(180.dp),
                )
            }
            HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
            ToggleRow(
                label = tr("Wysoki kontrast", "High contrast", de = "Hoher Kontrast", fr = "Contraste élevé", es = "Alto contraste", pt = "Alto contraste", it = "Alto contrasto", nl = "Hoog contrast", sv = "Hög kontrast", cs = "Vysoký kontrast", sk = "Vysoký kontrast", da = "Høj kontrast", ru = "Высокая контрастность"),
                checked = state.highContrast,
                onCheckedChange = onHighContrastChange,
                accent = Tokens.Blue,
                description = tr(
                    "Rozjaśnia wyblakłe szare napisy w menu i na Kokpicie do niemal pełnej bieli - przydatne przy jeździe w pełnym słońcu. Działa w obu motywach (ciemnym i jasnym).",
                    "Brightens faded gray text in menus and on the Cockpit to near-full white - useful when riding in bright sunlight. Works in both themes (dark and light).",
                    de = "Hellt verblasste graue Texte in Menüs und im Cockpit auf fast reines Weiß auf - nützlich bei Fahrten in praller Sonne. Funktioniert in beiden Designs (dunkel und hell).",
                    fr = "Éclaircit les textes gris fades des menus et du Cockpit en blanc quasi pur - utile en roulant en plein soleil. Fonctionne dans les deux thèmes (sombre et clair).",
                    es = "Aclara los textos grises apagados de los menús y del Cockpit a un blanco casi total - útil al circular con sol intenso. Funciona en ambos temas (oscuro y claro).",
                    pt = "Clareia o texto cinzento desbotado nos menus e no Cockpit para um branco quase total - útil ao conduzir com sol forte. Funciona em ambos os temas (escuro e claro).",
                    it = "Schiarisce il testo grigio sbiadito nei menu e nel Cockpit fino a un bianco quasi pieno - utile quando si guida sotto il sole intenso. Funziona in entrambi i temi (scuro e chiaro).",
                    nl = "Maakt vervaagde grijze tekst in menu's en op de Cockpit bijna volledig wit - handig bij fel zonlicht. Werkt in beide thema's (donker en licht).",
                    sv = "Ljusar upp blekt grå text i menyer och på Cockpit till nästan helvit - användbart vid körning i starkt solljus. Fungerar i båda temana (mörkt och ljust).",
                    cs = "Zesvětlí vybledlý šedý text v nabídkách a v Cockpitu na téměř plnou bílou - užitečné při jízdě na plném slunci. Funguje v obou motivech (tmavý i světlý).",
                    sk = "Zosvetlí vyblednutý sivý text v ponukách a v Cockpite na takmer plnú bielu - užitočné pri jazde na plnom slnku. Funguje v oboch motívoch (tmavý aj svetlý).",
                    da = "Gør falmet grå tekst i menuer og på Cockpit næsten helt hvid - nyttigt ved kørsel i stærkt sollys. Virker i begge temaer (mørkt og lyst).",
                    ru = "Делает выцветший серый текст в меню и на Кокпите почти полностью белым - полезно при езде в яркое солнце. Работает в обеих темах (тёмной и светлой).",
                ),
            )
            HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
            ToggleRow(
                label = tr("Powiększone cyfry na Kokpicie", "Large Cockpit digits", de = "Größere Zahlen im Cockpit", fr = "Chiffres agrandis (Cockpit)", es = "Cifras grandes en el Cockpit", pt = "Números grandes no Cockpit", it = "Numeri grandi nel Cockpit", nl = "Grote cijfers in Cockpit", sv = "Stora siffror i Cockpit", cs = "Velká čísla v Cockpitu", sk = "Veľké čísla v Cockpite", da = "Store tal i Cockpit", ru = "Крупные цифры в Кокпите"),
                checked = state.largeCockpitDigits,
                onCheckedChange = onLargeCockpitDigitsChange,
                accent = Tokens.Blue,
                description = tr(
                    "Powiększa WYŁĄCZNIE wartości liczbowe na Kokpicie (prędkość, moc, kafelki Distance/Trip/Current/itd., poziomy wspomagania, przyciski -/+, Light/Brake) - etykiety, jednostki i pozycje kafelków zostają bez zmian.",
                    "Enlarges ONLY the numeric values on the Cockpit (speed, power, Distance/Trip/Current/etc. tiles, assist levels, -/+ buttons, Light/Brake) - labels, units, and tile positions stay unchanged.",
                    de = "Vergrößert AUSSCHLIESSLICH die Zahlenwerte im Cockpit (Geschwindigkeit, Leistung, Kacheln Distance/Trip/Current/usw., Unterstützungsstufen, -/+ Tasten, Light/Brake) - Beschriftungen, Einheiten und Kachelpositionen bleiben unverändert.",
                    fr = "Agrandit UNIQUEMENT les valeurs numériques du Cockpit (vitesse, puissance, tuiles Distance/Trip/Current/etc., niveaux d'assistance, boutons -/+, Light/Brake) - libellés, unités et positions des tuiles restent inchangés.",
                    es = "Agranda ÚNICAMENTE los valores numéricos del Cockpit (velocidad, potencia, casillas Distance/Trip/Current/etc., niveles de asistencia, botones -/+, Light/Brake) - etiquetas, unidades y posiciones de las casillas no cambian.",
                    pt = "Aumenta APENAS os valores numéricos no Cockpit (velocidade, potência, blocos Distance/Trip/Current/etc., níveis de assistência, botões -/+, Light/Brake) - etiquetas, unidades e posições dos blocos permanecem inalteradas.",
                    it = "Ingrandisce SOLO i valori numerici nel Cockpit (velocità, potenza, riquadri Distance/Trip/Current/ecc., livelli di assistenza, pulsanti -/+, Light/Brake) - etichette, unità e posizioni dei riquadri restano invariate.",
                    nl = "Vergroot ALLEEN de numerieke waarden op de Cockpit (snelheid, vermogen, tegels Distance/Trip/Current/enz., ondersteuningsniveaus, -/+ knoppen, Light/Brake) - labels, eenheden en tegelposities blijven ongewijzigd.",
                    sv = "Förstorar ENDAST de numeriska värdena i Cockpit (hastighet, effekt, rutorna Distance/Trip/Current/etc., assistansnivåer, -/+ knappar, Light/Brake) - etiketter, enheter och ruteplaceringar förblir oförändrade.",
                    cs = "Zvětšuje POUZE číselné hodnoty v Cockpitu (rychlost, výkon, dlaždice Distance/Trip/Current/atd., úrovně asistence, tlačítka -/+, Light/Brake) - popisky, jednotky a pozice dlaždic zůstávají beze změny.",
                    sk = "Zväčšuje IBA číselné hodnoty v Cockpite (rýchlosť, výkon, dlaždice Distance/Trip/Current/atď., úrovne asistencie, tlačidlá -/+, Light/Brake) - popisky, jednotky a pozície dlaždíc zostávajú bez zmeny.",
                    da = "Forstørrer KUN de numeriske værdier i Cockpit (hastighed, effekt, felterne Distance/Trip/Current/osv., assistanceniveauer, -/+ knapper, Light/Brake) - etiketter, enheder og feltpositioner forbliver uændrede.",
                    ru = "Увеличивает ТОЛЬКО числовые значения в Кокпите (скорость, мощность, плитки Distance/Trip/Current/и т.д., уровни ассистирования, кнопки -/+, Light/Brake) - подписи, единицы измерения и положение плиток остаются без изменений.",
                ),
            )
        }

        MicroLabel(tr("Ekran blokady / AOD", "Lock screen / AOD", de = "Sperrbildschirm / AOD", fr = "Écran verrouillé / AOD", es = "Pantalla de bloqueo / AOD", pt = "Ecrã de bloqueio / AOD", it = "Schermata di blocco / AOD", nl = "Vergrendelscherm / AOD", sv = "Låsskärm / AOD", cs = "Zamykací obrazovka / AOD", sk = "Zamykacia obrazovka / AOD", da = "Låseskærm / AOD", ru = "Экран блокировки / AOD"))
        TokenCard(borderColor = Tokens.WhiteBorder) {
            ToggleRow(
                label = tr("Pokaż Kokpit na ekranie blokady/AOD", "Show Cockpit on lock screen/AOD", de = "Cockpit auf Sperrbildschirm/AOD zeigen", fr = "Afficher le Cockpit sur écran verrouillé/AOD", es = "Mostrar Cockpit en pantalla de bloqueo/AOD", pt = "Mostrar Cockpit no ecrã de bloqueio/AOD", it = "Mostra Cockpit nella schermata di blocco/AOD", nl = "Cockpit tonen op vergrendelscherm/AOD", sv = "Visa Cockpit på låsskärm/AOD", cs = "Zobrazit Cockpit na zamykací obrazovce/AOD", sk = "Zobraziť Cockpit na zamykacej obrazovke/AOD", da = "Vis Cockpit på låseskærm/AOD", ru = "Показывать Кокпит на экране блокировки/AOD"),
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
                    de = "Zeigt während der Fahrt Geschwindigkeit/Leistung/Unterstützung auf dem Sperrbildschirm und AOD, damit das Telefon wirklich einschlafen kann, statt den Bildschirm ständig eingeschaltet zu lassen - echte Akku-Ersparnis, anders als ein einfach heller Bildschirm. Funktioniert als \"Wird gerade abgespielt\" (täuscht einen Musikplayer vor) - der einzige öffentliche Android-Mechanismus mit diesem Effekt, daher sieht es wie Musik aus, nicht wie ein eigenes Cockpit. Benötigt die Benachrichtigungsberechtigung - ohne sie wird nichts angezeigt. Nicht jedes Telefon zeigt wiedergegebene Medien auf dem AOD an.",
                    fr = "Pendant la conduite, affiche vitesse/puissance/assistance sur l'écran verrouillé et l'AOD pour que le téléphone puisse vraiment s'endormir au lieu de garder l'écran allumé en permanence - une vraie économie de batterie, contrairement à un écran simplement allumé. Fonctionne comme \"lecture en cours\" (imite un lecteur de musique) - le seul mécanisme public d'Android permettant cet effet, donc ça ressemble à de la musique, pas à un cockpit dédié. Nécessite l'autorisation de notifications - sans elle, rien ne s'affiche. Tous les téléphones n'affichent pas les médias en cours sur leur AOD.",
                    es = "Mientras conduces, muestra velocidad/potencia/asistencia en la pantalla de bloqueo y el AOD para que el teléfono pueda dormir de verdad en vez de mantener la pantalla encendida todo el rato - un ahorro real de batería, a diferencia de una pantalla simplemente encendida. Funciona como \"reproduciendo ahora\" (simula un reproductor de música) - el único mecanismo público de Android que logra este efecto, así que parece música, no un cockpit dedicado. Requiere permiso de notificaciones - sin él, no se mostrará nada. No todos los teléfonos muestran la reproducción en curso en su AOD.",
                    pt = "Durante a condução, mostra velocidade/potência/assistência no ecrã de bloqueio e no AOD para que o telemóvel possa realmente adormecer em vez de manter o ecrã sempre aceso - uma poupança real de bateria, ao contrário de um ecrã simplesmente ligado. Funciona como \"a reproduzir agora\" (finge ser um leitor de música) - o único mecanismo público do Android que consegue este efeito, por isso parece música, não um cockpit dedicado. Requer permissão de notificações - sem ela, nada será mostrado. Nem todos os telemóveis mostram a reprodução em curso no AOD.",
                    it = "Durante la guida, mostra velocità/potenza/assistenza sulla schermata di blocco e sull'AOD, in modo che il telefono possa effettivamente entrare in standby invece di tenere lo schermo sempre acceso - un risparmio reale di batteria, a differenza di uno schermo semplicemente acceso. Funziona come \"in riproduzione\" (fingendo di essere un lettore musicale) - l'unico meccanismo pubblico di Android che ottiene questo effetto, quindi sembra musica, non un cockpit dedicato. Richiede l'autorizzazione alle notifiche - senza di essa non verrà mostrato nulla. Non tutti i telefoni mostrano i contenuti in riproduzione sull'AOD.",
                    nl = "Toont tijdens het rijden snelheid/vermogen/ondersteuning op het vergrendelscherm en de AOD, zodat de telefoon echt kan slapen in plaats van het scherm de hele tijd aan te houden - een echte batterijbesparing, in tegenstelling tot een gewoon verlicht scherm. Het werkt als \"nu afspelend\" (doet zich voor als muziekspeler) - het enige openbare Android-mechanisme dat dit effect bereikt, dus het ziet eruit als muziek, niet als een speciale cockpit. Vereist meldingstoestemming - zonder deze wordt niets getoond. Niet elke telefoon toont nu-afspelend media op de AOD.",
                    sv = "Visar under körningen hastighet/effekt/assistans på låsskärmen och AOD så att telefonen faktiskt kan somna istället för att hålla skärmen tänd hela tiden - en verklig batteribesparing, till skillnad från en vanlig upplyst skärm. Fungerar som \"spelas nu\" (låtsas vara en musikspelare) - den enda offentliga Android-mekanismen som uppnår detta, så det ser ut som musik, inte en dedikerad cockpit. Kräver aviseringsbehörighet - utan den visas ingenting. Inte alla telefoner visar nu-spelande media på sin AOD.",
                    cs = "Během jízdy zobrazuje rychlost/výkon/asistenci na zamykací obrazovce a AOD, aby telefon mohl skutečně usnout místo neustálého svícení obrazovky - reálná úspora baterie, na rozdíl od obyčejné svítící obrazovky. Funguje jako \"právě se přehrává\" (předstírá hudební přehrávač) - jediný veřejný mechanismus Androidu, který tohoto efektu dosahuje, takže to vypadá jako hudba, ne jako vyhrazený kokpit. Vyžaduje oprávnění k oznámením - bez něj se nic nezobrazí. Ne každý telefon zobrazuje právě přehrávaná média na AOD.",
                    sk = "Počas jazdy zobrazuje rýchlosť/výkon/asistenciu na zamykacej obrazovke a AOD, aby telefón mohol skutočne zaspať namiesto neustáleho svietenia obrazovky - reálna úspora batérie, na rozdiel od obyčajnej svietiacej obrazovky. Funguje ako \"práve sa prehráva\" (predstiera hudobný prehrávač) - jediný verejný mechanizmus Androidu, ktorý dosahuje tento efekt, takže to vyzerá ako hudba, nie ako vyhradený kokpit. Vyžaduje povolenie na oznámenia - bez neho sa nič nezobrazí. Nie každý telefón zobrazuje práve prehrávané médiá na AOD.",
                    da = "Under kørslen vises hastighed/effekt/assistance på låseskærmen og AOD, så telefonen faktisk kan gå i dvale i stedet for at holde skærmen tændt hele tiden - en reel batteribesparelse, i modsætning til en almindelig oplyst skærm. Det fungerer som \"afspiller nu\" (udgiver sig for at være en musikafspiller) - den eneste offentlige Android-mekanisme, der opnår denne effekt, så det ligner musik, ikke et dedikeret cockpit. Kræver notifikationstilladelse - uden den vises intet. Ikke alle telefoner viser afspillede medier på deres AOD.",
                    ru = "Во время езды показывает скорость/мощность/ассистирование на экране блокировки и AOD, чтобы телефон мог по-настоящему уснуть, а не держать экран включённым всё время - реальная экономия батареи, в отличие от обычного яркого экрана. Работает как «сейчас играет» (притворяясь музыкальным плеером) - единственный публичный механизм Android, позволяющий добиться такого эффекта, поэтому это выглядит как музыка, а не как отдельный кокпит. Требует разрешения на уведомления - без него ничего не отобразится. Не каждый телефон показывает воспроизводимые медиа на своём AOD.",
                ),
            )
            // Widoczne zawsze (niezależnie od stanu przełącznika wyżej), nie tylko gdy AOD włączone -
            // na wyraźne życzenie użytkownika, żeby opcja była widoczna/konfigurowalna z wyprzedzeniem.
            HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
            ToggleRow(
                label = tr("Sterowanie +/- na ekranie blokady", "+/- controls on lock screen", de = "+/- Steuerung auf dem Sperrbildschirm", fr = "Contrôles +/- sur écran verrouillé", es = "Controles +/- en pantalla de bloqueo", pt = "Controlos +/- no ecrã de bloqueio", it = "Controlli +/- sulla schermata di blocco", nl = "+/- bediening op vergrendelscherm", sv = "+/- reglage på låsskärmen", cs = "Ovládání +/- na zamykací obrazovce", sk = "Ovládanie +/- na zamykacej obrazovke", da = "+/- betjening på låseskærmen", ru = "Управление +/- на экране блокировки"),
                checked = state.aodAssistControlsEnabled,
                onCheckedChange = onAodAssistControlsChange,
                accent = Tokens.Blue,
                description = tr(
                    "Dodaje przyciski poprzedni/następny (jako +/-) do zmiany wspomagania bezpośrednio z ekranu blokady. Uwaga: telefon zgłasza się wtedy jako aktywnie odtwarzający media, co może kolidować z prawdziwą muzyką (słuchawki Bluetooth, przyciski multimedialne).",
                    "Adds previous/next buttons (as +/-) to change assist level directly from the lock screen. Note: the phone then reports itself as actively playing media, which can conflict with real music (Bluetooth headphones, media buttons).",
                    de = "Fügt Zurück/Weiter-Tasten (als +/-) hinzu, um die Unterstützung direkt vom Sperrbildschirm aus zu ändern. Hinweis: Das Telefon meldet sich dann als aktiv Medien abspielend, was mit echter Musik kollidieren kann (Bluetooth-Kopfhörer, Medientasten).",
                    fr = "Ajoute des boutons précédent/suivant (comme +/-) pour changer l'assistance directement depuis l'écran verrouillé. Remarque : le téléphone se signale alors comme lisant activement des médias, ce qui peut entrer en conflit avec de la vraie musique (écouteurs Bluetooth, boutons multimédias).",
                    es = "Añade botones anterior/siguiente (como +/-) para cambiar la asistencia directamente desde la pantalla de bloqueo. Nota: el teléfono se anuncia entonces como reproduciendo medios activamente, lo que puede entrar en conflicto con música real (auriculares Bluetooth, botones multimedia).",
                    pt = "Adiciona botões anterior/seguinte (como +/-) para mudar o nível de assistência diretamente no ecrã de bloqueio. Nota: o telemóvel passa a reportar-se como estando a reproduzir media ativamente, o que pode entrar em conflito com música real (auscultadores Bluetooth, botões multimédia).",
                    it = "Aggiunge pulsanti precedente/successivo (come +/-) per cambiare il livello di assistenza direttamente dalla schermata di blocco. Nota: il telefono si segnala quindi come se stesse riproducendo attivamente contenuti multimediali, il che può entrare in conflitto con musica reale (cuffie Bluetooth, pulsanti multimediali).",
                    nl = "Voegt vorige/volgende knoppen toe (als +/-) om het ondersteuningsniveau rechtstreeks vanaf het vergrendelscherm te wijzigen. Let op: de telefoon meldt zich dan als actief mediaspeler, wat kan botsen met echte muziek (Bluetooth-koptelefoon, mediaknoppen).",
                    sv = "Lägger till föregående/nästa-knappar (som +/-) för att ändra assistansnivå direkt från låsskärmen. Obs: telefonen anmäler sig då som aktivt spelande media, vilket kan krocka med riktig musik (Bluetooth-hörlurar, medieknappar).",
                    cs = "Přidává tlačítka předchozí/další (jako +/-) pro změnu úrovně asistence přímo ze zamykací obrazovky. Poznámka: telefon se pak hlásí jako aktivně přehrávající médium, což může kolidovat se skutečnou hudbou (Bluetooth sluchátka, mediální tlačítka).",
                    sk = "Pridáva tlačidlá predchádzajúce/ďalšie (ako +/-) na zmenu úrovne asistencie priamo zo zamykacej obrazovky. Poznámka: telefón sa potom hlási ako aktívne prehrávajúce médium, čo môže kolidovať so skutočnou hudbou (Bluetooth slúchadlá, mediálne tlačidlá).",
                    da = "Tilføjer forrige/næste-knapper (som +/-) til at ændre assistanceniveau direkte fra låseskærmen. Bemærk: telefonen melder sig derefter som aktivt afspillende medier, hvilket kan give konflikt med rigtig musik (Bluetooth-hovedtelefoner, medieknapper).",
                    ru = "Добавляет кнопки предыдущий/следующий (как +/-) для изменения уровня ассистирования прямо с экрана блокировки. Примечание: телефон при этом сообщает, что активно воспроизводит медиа, что может конфликтовать с реальной музыкой (Bluetooth-наушники, медиакнопки).",
                ),
            )
        }
    }
}
