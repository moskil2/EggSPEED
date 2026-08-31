package com.bafspeed.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bafspeed.app.UiState
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.protocol.THROTTLE_MODE_LABELS
import com.bafspeed.app.protocol.designatedAssistLabel
import com.bafspeed.app.protocol.speedLimitLabel
import com.bafspeed.app.ui.components.ExpandableParamTile
import com.bafspeed.app.ui.components.FlankedSlider
import com.bafspeed.app.ui.components.ReadWriteButtons
import com.bafspeed.app.ui.components.SegmentedControl
import com.bafspeed.app.ui.components.StepBtn
import com.bafspeed.app.ui.components.TelemetryPausedNotice
import com.bafspeed.app.ui.theme.Tokens

/**
 * Zakładka "Throttle" - odpowiednik Throttle Handle settings z Bafang Configuration Tool.
 * Pola: Start Voltage, End Voltage, Mode, Designated Assist Level, Speed Limit, Start
 * Current. Każde pole to osobna, rozwijalna karta z opisem z Help.pdf.
 */
@Composable
fun ThrottleScreen(
    state: UiState,
    onStartVoltage: (Int) -> Unit,
    onEndVoltage: (Int) -> Unit,
    onMode: (Int) -> Unit,
    onDesignatedAssist: (Int) -> Unit,
    onSpeedLimit: (Int) -> Unit,
    onStartCurrent: (Int) -> Unit,
    onRead: () -> Unit,
    onWrite: () -> Unit,
    readWriteEnabled: Boolean,
    monitoringActive: Boolean,
) {
    val thr = state.thrOrDefault

    Column(
        Modifier
            .fillMaxSize()
            .background(Tokens.Bg)
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = 16.dp)),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ReadWriteButtons(onRead = onRead, onWrite = onWrite, enabled = readWriteEnabled)
        TelemetryPausedNotice(monitoringActive = monitoringActive, aodActive = state.aodEnabled)

        ExpandableParamTile(
            label = tr("Napięcie startowe", "Start Voltage", de = "Startspannung", fr = "Tension de démarrage", es = "Voltaje de arranque", pt = "Tensão de arranque", it = "Tensione di avvio", nl = "Startspanning", sv = "Startspänning", cs = "Počáteční napětí", sk = "Počiatočné napätie"),
            valueLabel = String.format("%.1f V", thr.startVoltage / 10.0),
            description = tr(
                "To jest napięcie wyjściowe manetki, przy którym silnik zacznie działać. " +
                    "Minimum, na które reaguje sterownik, to 1,1V, więc ten parametr jest zwykle ustawiony na 11 (11×100mV = 1,1V).",
                "This is the throttle handle output voltage at which the motor will start. " +
                    "The minimum at which the controller responds is 1.1V, so this parameter is usually set to 11 (11×100mV = 1.1V).",
                de = "Dies ist die Ausgangsspannung des Gasgriffs, bei der der Motor startet. Das Minimum, auf " +
                    "das das Steuergerät reagiert, beträgt 1,1V, daher wird dieser Parameter normalerweise auf 11 " +
                    "gesetzt (11×100mV = 1,1V).",
                fr = "Il s'agit de la tension de sortie de l'accélérateur à laquelle le moteur démarre. Le " +
                    "minimum auquel le contrôleur répond est de 1,1V, donc ce paramètre est généralement réglé " +
                    "sur 11 (11×100mV = 1,1V).",
                es = "Este es el voltaje de salida del acelerador al que el motor arrancará. El mínimo al que " +
                    "responde el controlador es 1,1V, por lo que este parámetro suele estar en 11 (11×100mV = 1,1V).",
                pt = "Esta é a tensão de saída do acelerador na qual o motor começará a funcionar. O mínimo ao " +
                    "qual o controlador responde é 1,1V, por isso este parâmetro normalmente está definido em 11 " +
                    "(11×100mV = 1,1V).",
                it = "Questa è la tensione di uscita dell'acceleratore alla quale il motore inizierà a " +
                    "funzionare. Il minimo a cui risponde il controller è 1,1V, quindi questo parametro è " +
                    "solitamente impostato su 11 (11×100mV = 1,1V).",
                nl = "Dit is de uitgangsspanning van de gasgreep waarbij de motor start. Het minimum waarop de " +
                    "controller reageert is 1,1V, dus deze parameter staat meestal op 11 (11×100mV = 1,1V).",
                sv = "Detta är gasreglagets utspänning vid vilken motorn startar. Minimum som " +
                    "styrenheten reagerar på är 1,1V, så denna parameter är vanligtvis satt till 11 (11×100mV = 1,1V).",
                cs = "Toto je výstupní napětí plynové páčky, při kterém motor spustí. Minimum, na které " +
                    "řadič reaguje, je 1,1V, takže tento parametr je obvykle nastaven na 11 (11×100mV = 1,1V).",
                sk = "Toto je výstupné napätie plynovej páčky, pri ktorom motor spustí. Minimum, na ktoré " +
                    "radič reaguje, je 1,1V, takže tento parameter je zvyčajne nastavený na 11 (11×100mV = 1,1V).",
            ),
        ) {
            FlankedSlider(
                value = thr.startVoltage,
                range = 0..50,
                accent = Tokens.Blue,
                onValueChange = onStartVoltage,
            )
        }

        ExpandableParamTile(
            label = tr("Napięcie końcowe", "End Voltage", de = "Endspannung", fr = "Tension finale", es = "Voltaje final", pt = "Tensão final", it = "Tensione finale", nl = "Eindspanning", sv = "Slutspänning", cs = "Konečné napětí", sk = "Konečné napätie"),
            valueLabel = String.format("%.1f V", thr.endVoltage / 10.0),
            description = tr(
                "To jest napięcie wyjściowe manetki, przy którym silnik osiągnie maksymalną moc " +
                    "(ograniczoną innymi ustawieniami). Maksimum przyjmowane przez sterownik to 4,2V (42×100mV = 4,2V). " +
                    "Realne maksimum manetki może się różnić zależnie od modelu - ustawione za niskie dają prawie brak " +
                    "reakcji, ustawione na realne maksimum manetki dają najszerszy możliwy zakres kontroli.",
                "This is the throttle handle output voltage at which the motor will reach its maximum power " +
                    "(limited by other settings). The maximum accepted by the controller is 4.2V (42×100mV = 4.2V). The " +
                    "throttle's real maximum output can differ by model - set too low you get almost no response, set to " +
                    "the handle's real maximum you get the widest possible control range.",
                de = "Dies ist die Ausgangsspannung des Gasgriffs, bei der der Motor seine maximale Leistung " +
                    "erreicht (begrenzt durch andere Einstellungen). Das vom Steuergerät akzeptierte Maximum " +
                    "beträgt 4,2V (42×100mV = 4,2V). Die reale maximale Ausgabe des Gasgriffs kann je nach Modell " +
                    "abweichen - zu niedrig eingestellt erhältst du fast keine Reaktion, auf das reale Maximum des " +
                    "Griffs eingestellt erhältst du den größtmöglichen Kontrollbereich.",
                fr = "Il s'agit de la tension de sortie de l'accélérateur à laquelle le moteur atteindra sa " +
                    "puissance maximale (limitée par d'autres réglages). Le maximum accepté par le contrôleur est " +
                    "de 4,2V (42×100mV = 4,2V). La sortie maximale réelle de l'accélérateur peut varier selon le " +
                    "modèle - réglée trop bas, vous obtenez presque aucune réaction ; réglée au maximum réel de la " +
                    "poignée, vous obtenez la plage de contrôle la plus large possible.",
                es = "Este es el voltaje de salida del acelerador al que el motor alcanzará su potencia máxima " +
                    "(limitada por otros ajustes). El máximo aceptado por el controlador es 4,2V (42×100mV = " +
                    "4,2V). La salida máxima real del acelerador puede variar según el modelo - ajustado " +
                    "demasiado bajo obtienes casi ninguna respuesta, ajustado al máximo real del puño obtienes el " +
                    "rango de control más amplio posible.",
                pt = "Esta é a tensão de saída do acelerador na qual o motor atingirá a sua potência máxima " +
                    "(limitada por outras definições). O máximo aceite pelo controlador é 4,2V (42×100mV = " +
                    "4,2V). A saída máxima real do acelerador pode variar consoante o modelo - se estiver " +
                    "demasiado baixa obténs quase nenhuma resposta, se estiver no máximo real do punho obténs a " +
                    "maior gama de controlo possível.",
                it = "Questa è la tensione di uscita dell'acceleratore alla quale il motore raggiungerà la sua " +
                    "potenza massima (limitata da altre impostazioni). Il massimo accettato dal controller è " +
                    "4,2V (42×100mV = 4,2V). L'uscita massima reale dell'acceleratore può variare a seconda del " +
                    "modello - impostata troppo bassa si ottiene quasi nessuna risposta, impostata al massimo " +
                    "reale della manopola si ottiene la gamma di controllo più ampia possibile.",
                nl = "Dit is de uitgangsspanning van de gasgreep waarbij de motor zijn maximale vermogen bereikt " +
                    "(begrensd door andere instellingen). Het maximum dat de controller accepteert is 4,2V " +
                    "(42×100mV = 4,2V). De werkelijke maximale uitgang van de gasgreep kan per model verschillen " +
                    "- te laag ingesteld krijg je bijna geen reactie, ingesteld op het werkelijke maximum van de " +
                    "greep krijg je het breedst mogelijke regelbereik.",
                sv = "Detta är gasreglagets utspänning vid vilken motorn når sin maximala effekt " +
                    "(begränsad av andra inställningar). Maximum som accepteras av styrenheten är 4,2V " +
                    "(42×100mV = 4,2V). Gasreglagets verkliga maximala utsignal kan skilja sig åt beroende på modell " +
                    "- inställt för lågt får du nästan ingen respons, inställt på reglagets verkliga maximum får du " +
                    "det bredast möjliga kontrollintervallet.",
                cs = "Toto je výstupní napětí plynové páčky, při kterém motor dosáhne svého maximálního výkonu " +
                    "(omezeného jinými nastaveními). Maximum přijímané řadičem je 4,2V " +
                    "(42×100mV = 4,2V). Skutečné maximální napětí páčky se může lišit podle modelu " +
                    "- nastavené příliš nízko dostaneš téměř žádnou reakci, nastavené na skutečné maximum " +
                    "páčky dostaneš nejširší možný rozsah ovládání.",
                sk = "Toto je výstupné napätie plynovej páčky, pri ktorom motor dosiahne svoj maximálny výkon " +
                    "(obmedzený inými nastaveniami). Maximum prijímané radičom je 4,2V " +
                    "(42×100mV = 4,2V). Skutočné maximálne napätie páčky sa môže líšiť podľa modelu " +
                    "- nastavené príliš nízko dostaneš takmer žiadnu reakciu, nastavené na skutočné maximum " +
                    "páčky dostaneš najširší možný rozsah ovládania.",
            ),
        ) {
            FlankedSlider(
                value = thr.endVoltage,
                range = 0..50,
                accent = Tokens.Blue,
                onValueChange = onEndVoltage,
            )
        }

        ExpandableParamTile(
            label = tr("Tryb", "Mode", de = "Modus", fr = "Mode", es = "Modo", pt = "Modo", it = "Modalità", nl = "Modus", sv = "Läge", cs = "Režim", sk = "Režim"),
            valueLabel = THROTTLE_MODE_LABELS.getOrElse(thr.mode) { "?" },
            description = tr(
                "Tryb prędkości reaguje wolniej, ale daje precyzyjną kontrolę ściśle związaną z dokładną pozycją " +
                    "manetki. Tryb prądu reaguje szybko, ale bardziej na zasadzie włącz/wyłącz, mniej płynnie. " +
                    "Prędkość: sterownik używa prędkości jazdy do ustawienia mocy silnika na podstawie pozycji manetki - " +
                    "jest znaczne opóźnienie i reakcja jest często słaba. Prąd: manetka kontroluje prąd silnika " +
                    "bezpośrednio na podstawie swojej pozycji - ten tryb działa lepiej, podobnie do pedału gazu w samochodzie.",
                "Speed mode is slower to react but gives precise control tied closely to the throttle's " +
                    "exact position. Current mode reacts fast but feels more on/off, less gradual. " +
                    "Speed: the controller uses the moving speed to set motor power based on throttle position - there " +
                    "is significant delay and the response is often poor. Current: the handle controls motor current " +
                    "directly based on its position - this mode works better, similar to a car's accelerator.",
                de = "Der Geschwindigkeitsmodus reagiert langsamer, bietet aber präzise Kontrolle, eng an die " +
                    "genaue Position des Gasgriffs gekoppelt. Der Strommodus reagiert schnell, fühlt sich aber " +
                    "eher wie Ein/Aus an, weniger stufenlos. Geschwindigkeit: Das Steuergerät nutzt die " +
                    "Fahrgeschwindigkeit, um die Motorleistung anhand der Gasgriffposition einzustellen - es gibt " +
                    "eine erhebliche Verzögerung, und die Reaktion ist oft schwach. Strom: Der Griff steuert den " +
                    "Motorstrom direkt basierend auf seiner Position - dieser Modus funktioniert besser, ähnlich " +
                    "wie das Gaspedal eines Autos.",
                fr = "Le mode Vitesse réagit plus lentement mais offre un contrôle précis étroitement lié à la " +
                    "position exacte de l'accélérateur. Le mode Courant réagit rapidement mais donne une " +
                    "sensation plus tout-ou-rien, moins progressive. Vitesse : le contrôleur utilise la vitesse de " +
                    "déplacement pour régler la puissance du moteur en fonction de la position de l'accélérateur " +
                    "- il y a un délai important et la réponse est souvent faible. Courant : la poignée contrôle " +
                    "directement le courant du moteur en fonction de sa position - ce mode fonctionne mieux, " +
                    "comme l'accélérateur d'une voiture.",
                es = "El modo Velocidad reacciona más lentamente, pero ofrece un control preciso estrechamente " +
                    "ligado a la posición exacta del acelerador. El modo Corriente reacciona rápido, pero se " +
                    "siente más de encendido/apagado, menos gradual. Velocidad: el controlador usa la velocidad " +
                    "de desplazamiento para ajustar la potencia del motor según la posición del acelerador - hay " +
                    "un retraso significativo y la respuesta suele ser pobre. Corriente: el puño controla la " +
                    "corriente del motor directamente según su posición - este modo funciona mejor, similar al " +
                    "acelerador de un coche.",
                pt = "O modo Velocidade reage mais lentamente, mas dá um controlo preciso ligado de perto à " +
                    "posição exata do acelerador. O modo Corrente reage rapidamente, mas dá uma sensação mais de " +
                    "ligado/desligado, menos gradual. Velocidade: o controlador usa a velocidade de deslocação " +
                    "para definir a potência do motor com base na posição do acelerador - há um atraso " +
                    "significativo e a resposta é frequentemente fraca. Corrente: o punho controla a corrente do " +
                    "motor diretamente com base na sua posição - este modo funciona melhor, semelhante ao " +
                    "acelerador de um carro.",
                it = "La modalità Velocità reagisce più lentamente ma offre un controllo preciso strettamente " +
                    "legato alla posizione esatta dell'acceleratore. La modalità Corrente reagisce rapidamente " +
                    "ma dà una sensazione più on/off, meno graduale. Velocità: il controller usa la velocità di " +
                    "movimento per impostare la potenza del motore in base alla posizione dell'acceleratore - " +
                    "c'è un ritardo significativo e la risposta è spesso scarsa. Corrente: la manopola controlla " +
                    "la corrente del motore direttamente in base alla sua posizione - questa modalità funziona " +
                    "meglio, simile all'acceleratore di un'auto.",
                nl = "Snelheidsmodus reageert trager, maar biedt nauwkeurige controle nauw gekoppeld aan de " +
                    "exacte positie van de gasgreep. Stroommodus reageert snel, maar voelt meer aan/uit, minder " +
                    "geleidelijk. Snelheid: de controller gebruikt de rijsnelheid om het motorvermogen in te " +
                    "stellen op basis van de gasgreeppositie - er is aanzienlijke vertraging en de respons is " +
                    "vaak zwak. Stroom: de greep regelt de motorstroom rechtstreeks op basis van zijn positie - " +
                    "deze modus werkt beter, vergelijkbaar met het gaspedaal van een auto.",
                sv = "Hastighetsläget reagerar långsammare men ger precis kontroll nära kopplad till gasreglagets " +
                    "exakta position. Strömläget reagerar snabbt men känns mer på/av, mindre gradvis. " +
                    "Hastighet: styrenheten använder körhastigheten för att ställa in motoreffekten baserat på " +
                    "gasreglagets position - det finns en betydande fördröjning och responsen är ofta dålig. " +
                    "Ström: reglaget styr motorströmmen direkt baserat på sin position - detta läge fungerar " +
                    "bättre, liknande en bils gaspedal.",
                cs = "Režim rychlosti reaguje pomaleji, ale poskytuje přesné ovládání úzce spojené s " +
                    "přesnou pozicí plynové páčky. Režim proudu reaguje rychle, ale působí spíš zapnuto/vypnuto, " +
                    "méně plynule. Rychlost: řadič používá rychlost jízdy k nastavení výkonu motoru na základě " +
                    "pozice plynové páčky - je zde výrazné zpoždění a reakce je často slabá. Proud: páčka " +
                    "ovládá proud motoru přímo na základě své pozice - tento režim funguje lépe, podobně " +
                    "jako plynový pedál v autě.",
                sk = "Režim rýchlosti reaguje pomalšie, ale poskytuje presné ovládanie úzko spojené s " +
                    "presnou pozíciou plynovej páčky. Režim prúdu reaguje rýchlo, ale pôsobí skôr zapnuté/vypnuté, " +
                    "menej plynulo. Rýchlosť: radič používa rýchlosť jazdy na nastavenie výkonu motora na základe " +
                    "pozície plynovej páčky - je tu výrazné oneskorenie a reakcia je často slabá. Prúd: páčka " +
                    "ovláda prúd motora priamo na základe svojej pozície - tento režim funguje lepšie, podobne " +
                    "ako plynový pedál v aute.",
            ),
        ) {
            SegmentedControl(
                options = THROTTLE_MODE_LABELS,
                selectedIndex = thr.mode.coerceIn(0, 1),
                onSelect = onMode,
            )
        }

        ExpandableParamTile(
            label = tr("Wskazany poziom wspomagania", "Designated Assist Level", de = "Festgelegte Unterstützungsstufe", fr = "Niveau d'assistance désigné", es = "Nivel de asistencia designado", pt = "Nível de assistência designado", it = "Livello di assistenza designato", nl = "Aangewezen ondersteuningsniveau", sv = "Angiven assistansnivå", cs = "Určená úroveň asistence", sk = "Určená úroveň asistencie"),
            valueLabel = designatedAssistLabel(thr.designatedAssist),
            description = tr(
                "\"By Display's Command\" używa poziomu wspomagania wybranego na Twoim LCD, więc maksymalna moc " +
                    "wyjściowa i prędkość zależą od tego poziomu i pozycji manetki - niski poziom PAS utrzymuje niski " +
                    "prąd i prędkość nawet przy pełnej manetce. Ustalony poziom (0-9) sprawia, że manetka zawsze " +
                    "używa maksymalnego prądu i prędkości tego poziomu, niezależnie od LCD. Uważaj z poziomem 9: nie " +
                    "wciskaj manetki do maksimum stojąc w miejscu, wysoki prąd może uszkodzić sterownik i silnik.",
                "\"By Display's Command\" uses the assist level selected on your LCD, so the maximum power " +
                    "output and speed depend on that level and the throttle position - a low PAS level keeps current and " +
                    "speed low even at full throttle. A fixed level (0-9) makes the throttle always use that level's " +
                    "maximum current and speed regardless of the LCD. Be careful with level 9: don't push the throttle to " +
                    "max while stopped, the high current can damage the controller and motor.",
                de = "„By Display's Command“ verwendet die auf deinem LCD ausgewählte Unterstützungsstufe, " +
                    "sodass die maximale Ausgangsleistung und Geschwindigkeit von dieser Stufe und der " +
                    "Gasgriffposition abhängen - eine niedrige PAS-Stufe hält Strom und Geschwindigkeit selbst bei " +
                    "Vollgas niedrig. Eine feste Stufe (0-9) sorgt dafür, dass der Gasgriff immer den maximalen " +
                    "Strom und die maximale Geschwindigkeit dieser Stufe nutzt, unabhängig vom LCD. Sei " +
                    "vorsichtig mit Stufe 9: Drücke den Gasgriff im Stillstand nicht auf Maximum, der hohe Strom " +
                    "kann Steuergerät und Motor beschädigen.",
                fr = "« By Display's Command » utilise le niveau d'assistance sélectionné sur votre LCD, donc " +
                    "la puissance de sortie maximale et la vitesse dépendent de ce niveau et de la position de " +
                    "l'accélérateur - un niveau PAS bas maintient un courant et une vitesse faibles même à pleine " +
                    "accélération. Un niveau fixe (0-9) fait que l'accélérateur utilise toujours le courant et la " +
                    "vitesse maximum de ce niveau, indépendamment du LCD. Soyez prudent avec le niveau 9 : ne " +
                    "poussez pas l'accélérateur au maximum à l'arrêt, le courant élevé peut endommager le " +
                    "contrôleur et le moteur.",
                es = "«By Display's Command» usa el nivel de asistencia seleccionado en tu LCD, por lo que la " +
                    "potencia máxima de salida y la velocidad dependen de ese nivel y de la posición del " +
                    "acelerador - un nivel PAS bajo mantiene la corriente y la velocidad bajas incluso con el " +
                    "acelerador a fondo. Un nivel fijo (0-9) hace que el acelerador use siempre la corriente y " +
                    "velocidad máximas de ese nivel, independientemente del LCD. Ten cuidado con el nivel 9: no " +
                    "lleves el acelerador al máximo estando parado, la corriente alta puede dañar el controlador " +
                    "y el motor.",
                pt = "«By Display's Command» usa o nível de assistência selecionado no teu LCD, por isso a " +
                    "potência máxima de saída e a velocidade dependem desse nível e da posição do acelerador - " +
                    "um nível PAS baixo mantém a corrente e a velocidade baixas mesmo com o acelerador todo " +
                    "aberto. Um nível fixo (0-9) faz com que o acelerador use sempre a corrente e a velocidade " +
                    "máximas desse nível, independentemente do LCD. Tem cuidado com o nível 9: não empurres o " +
                    "acelerador ao máximo enquanto estiveres parado, a corrente alta pode danificar o " +
                    "controlador e o motor.",
                it = "«By Display's Command» usa il livello di assistenza selezionato sul tuo LCD, quindi la " +
                    "potenza massima di uscita e la velocità dipendono da quel livello e dalla posizione " +
                    "dell'acceleratore - un livello PAS basso mantiene bassa la corrente e la velocità anche con " +
                    "l'acceleratore a tutta apertura. Un livello fisso (0-9) fa sì che l'acceleratore utilizzi " +
                    "sempre la corrente e la velocità massime di quel livello, indipendentemente dall'LCD. Fai " +
                    "attenzione con il livello 9: non spingere l'acceleratore al massimo da fermo, la corrente " +
                    "elevata può danneggiare il controller e il motore.",
                nl = "«By Display's Command» gebruikt het op je LCD geselecteerde ondersteuningsniveau, dus het " +
                    "maximale uitgangsvermogen en de snelheid hangen af van dat niveau en de gasgreeppositie - " +
                    "een laag PAS-niveau houdt de stroom en snelheid laag, zelfs bij volledig gas. Een vast " +
                    "niveau (0-9) zorgt ervoor dat de gasgreep altijd de maximale stroom en snelheid van dat " +
                    "niveau gebruikt, ongeacht het LCD. Wees voorzichtig met niveau 9: duw de gasgreep niet tot " +
                    "het maximum terwijl je stilstaat, de hoge stroom kan de controller en motor beschadigen.",
                sv = "«By Display's Command» använder assistansnivån vald på din LCD, så den maximala " +
                    "uteffekten och hastigheten beror på den nivån och gasreglagets position - " +
                    "en låg PAS-nivå håller ström och hastighet låga även vid full gas. En fast " +
                    "nivå (0-9) gör att gasreglaget alltid använder den nivåns maximala ström och hastighet, " +
                    "oavsett LCD. Var försiktig med nivå 9: tryck inte gasreglaget till max medan du står stilla, " +
                    "den höga strömmen kan skada styrenheten och motorn.",
                cs = "„By Display's Command“ používá úroveň asistence vybranou na tvém LCD, takže maximální " +
                    "výstupní výkon a rychlost závisí na této úrovni a pozici plynové páčky - " +
                    "nízká úroveň PAS udržuje proud a rychlost nízké i při plném plynu. Pevná " +
                    "úroveň (0-9) zajistí, že plynová páčka vždy používá maximální proud a rychlost této " +
                    "úrovně, bez ohledu na LCD. Buď opatrný s úrovní 9: netlač plynovou páčku na maximum " +
                    "při stání, vysoký proud může poškodit řadič a motor.",
                sk = "„By Display's Command“ používa úroveň asistencie vybranú na tvojom LCD, takže maximálny " +
                    "výstupný výkon a rýchlosť závisia od tejto úrovne a pozície plynovej páčky - " +
                    "nízka úroveň PAS udržuje prúd a rýchlosť nízke aj pri plnom plyne. Pevná " +
                    "úroveň (0-9) zabezpečí, že plynová páčka vždy používa maximálny prúd a rýchlosť tejto " +
                    "úrovne, bez ohľadu na LCD. Buď opatrný s úrovňou 9: netlač plynovú páčku na maximum " +
                    "pri státí, vysoký prúd môže poškodiť radič a motor.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onDesignatedAssist(thr.designatedAssist - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onDesignatedAssist(thr.designatedAssist + 1) }
            }
        }

        ExpandableParamTile(
            label = tr("Limit prędkości", "Speed Limit", de = "Geschwindigkeitslimit", fr = "Limite de vitesse", es = "Límite de velocidad", pt = "Limite de velocidade", it = "Limite di velocità", nl = "Snelheidslimiet", sv = "Hastighetsgräns", cs = "Omezení rychlosti", sk = "Obmedzenie rýchlosti"),
            valueLabel = speedLimitLabel(thr.speedLimit),
            description = tr(
                "Ogranicza maksymalną prędkość podczas używania manetki. To nadpisuje maksymalną prędkość " +
                    "ustalonego poziomu wspomagania, jeśli jest ustawiona wyżej.",
                "Limits the maximum speed when using the throttle handle. This overrides the designated " +
                    "assist level's maximum speed if that one is set higher.",
                de = "Begrenzt die maximale Geschwindigkeit bei Verwendung des Gasgriffs. Dies überschreibt die " +
                    "maximale Geschwindigkeit der festgelegten Unterstützungsstufe, falls diese höher eingestellt ist.",
                fr = "Limite la vitesse maximale lors de l'utilisation de l'accélérateur. Cela remplace la " +
                    "vitesse maximale du niveau d'assistance désigné si celle-ci est réglée plus haut.",
                es = "Limita la velocidad máxima al usar el acelerador. Esto anula la velocidad máxima del " +
                    "nivel de asistencia designado si este está ajustado más alto.",
                pt = "Limita a velocidade máxima ao usar o acelerador. Isto substitui a velocidade máxima do " +
                    "nível de assistência designado, se estiver definida mais alta.",
                it = "Limita la velocità massima quando si usa l'acceleratore. Questo sovrascrive la velocità " +
                    "massima del livello di assistenza designato, se impostata più alta.",
                nl = "Beperkt de maximale snelheid bij gebruik van de gasgreep. Dit overschrijft de maximale " +
                    "snelheid van het aangewezen ondersteuningsniveau, als die hoger is ingesteld.",
                sv = "Begränsar maxhastigheten vid användning av gasreglaget. Detta åsidosätter den angivna " +
                    "assistansnivåns maxhastighet om den är inställd högre.",
                cs = "Omezuje maximální rychlost při použití plynové páčky. Toto přepisuje maximální " +
                    "rychlost určené úrovně asistence, pokud je nastavena vyšší.",
                sk = "Obmedzuje maximálnu rýchlosť pri použití plynovej páčky. Toto prepisuje maximálnu " +
                    "rýchlosť určenej úrovne asistencie, ak je nastavená vyššie.",
            ),
        ) {
            FlankedSlider(
                value = thr.speedLimit,
                range = 0..26,
                accent = Tokens.Emerald,
                onValueChange = onSpeedLimit,
            )
        }

        ExpandableParamTile(
            label = tr("Prąd startowy", "Start Current", de = "Startstrom", fr = "Courant de démarrage", es = "Corriente de arranque", pt = "Corrente de arranque", it = "Corrente di avvio", nl = "Startstroom", sv = "Startström", cs = "Počáteční proud", sk = "Počiatočný prúd"),
            valueLabel = "${thr.startCurrentPct}%",
            description = tr(
                "Procent maksymalnego prądu podawanego do silnika, gdy manetka generuje minimalne przyjmowane " +
                    "napięcie. 10-20% zwykle działa dobrze - np. przy Current Limit 25A i Start Current 10% dostajesz " +
                    "płynny start na 2,5A. Zbyt wysoka wartość może uszkodzić wewnętrzne przekładnie i silnik.",
                "Percentage of maximum current applied to the motor when the throttle generates the minimum " +
                    "accepted voltage. 10-20% usually works well - e.g. with a 25A Current Limit and 10% Start Current you " +
                    "get a 2.5A smooth start. Too high a value can damage the internal gears and the motor.",
                de = "Prozentsatz des maximalen Stroms, der an den Motor angelegt wird, wenn der Gasgriff die " +
                    "minimal akzeptierte Spannung erzeugt. 10-20% funktionieren normalerweise gut - z. B. bei " +
                    "einem Current Limit von 25A und Start Current von 10% erhältst du einen sanften Start mit " +
                    "2,5A. Ein zu hoher Wert kann die internen Getriebe und den Motor beschädigen.",
                fr = "Pourcentage du courant maximal appliqué au moteur lorsque l'accélérateur génère la " +
                    "tension minimale acceptée. 10-20% fonctionne généralement bien - par ex. avec un Current " +
                    "Limit de 25A et un Start Current de 10%, vous obtenez un démarrage en douceur à 2,5A. Une " +
                    "valeur trop élevée peut endommager les engrenages internes et le moteur.",
                es = "Porcentaje de la corriente máxima aplicada al motor cuando el acelerador genera el " +
                    "voltaje mínimo aceptado. 10-20% suele funcionar bien - p. ej., con un Current Limit de 25A y " +
                    "un Start Current del 10% obtienes un arranque suave a 2,5A. Un valor demasiado alto puede " +
                    "dañar los engranajes internos y el motor.",
                pt = "Percentagem da corrente máxima aplicada ao motor quando o acelerador gera a tensão mínima " +
                    "aceite. 10-20% costuma funcionar bem - por ex., com um Current Limit de 25A e um Start " +
                    "Current de 10% obténs um arranque suave a 2,5A. Um valor demasiado alto pode danificar as " +
                    "engrenagens internas e o motor.",
                it = "Percentuale della corrente massima applicata al motore quando l'acceleratore genera la " +
                    "tensione minima accettata. 10-20% di solito funziona bene - ad es. con un Current Limit di " +
                    "25A e uno Start Current del 10% ottieni un avvio fluido a 2,5A. Un valore troppo alto può " +
                    "danneggiare gli ingranaggi interni e il motore.",
                nl = "Percentage van de maximale stroom die naar de motor wordt gestuurd wanneer de gasgreep de " +
                    "minimaal geaccepteerde spanning genereert. 10-20% werkt meestal goed - bijv. met een " +
                    "Current Limit van 25A en Start Current van 10% krijg je een soepele start op 2,5A. Een te " +
                    "hoge waarde kan de interne tandwielen en de motor beschadigen.",
                sv = "Procentandel av maxströmmen som appliceras på motorn när gasreglaget genererar den " +
                    "minsta accepterade spänningen. 10-20% fungerar oftast bra - t.ex. med en " +
                    "Current Limit på 25A och Start Current på 10% får du en mjuk start på 2,5A. För " +
                    "högt värde kan skada de interna växlarna och motorn.",
                cs = "Procento maximálního proudu aplikovaného na motor, když plynová páčka generuje " +
                    "minimální přijímané napětí. 10-20% obvykle funguje dobře - např. s " +
                    "Current Limit 25A a Start Current 10% dostaneš plynulý start na 2,5A. Příliš " +
                    "vysoká hodnota může poškodit vnitřní převody a motor.",
                sk = "Percento maximálneho prúdu aplikovaného na motor, keď plynová páčka generuje " +
                    "minimálne prijímané napätie. 10-20% zvyčajne funguje dobre - napr. s " +
                    "Current Limit 25A a Start Current 10% dostaneš plynulý štart na 2,5A. Príliš " +
                    "vysoká hodnota môže poškodiť vnútorné prevody a motor.",
            ),
        ) {
            FlankedSlider(
                value = thr.startCurrentPct,
                range = 0..100,
                accent = Tokens.Amber,
                onValueChange = onStartCurrent,
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}
