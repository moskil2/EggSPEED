package com.bafspeed.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bafspeed.app.UiState
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.protocol.PEDAL_SENSOR_TYPE_LABELS
import com.bafspeed.app.protocol.designatedAssistLabel
import com.bafspeed.app.protocol.speedLimitLabel
import com.bafspeed.app.ui.components.ExpandableParamTile
import com.bafspeed.app.ui.components.FlankedSlider
import com.bafspeed.app.ui.components.ReadWriteButtons
import com.bafspeed.app.ui.components.StepBtn
import com.bafspeed.app.ui.components.TelemetryPausedNotice
import com.bafspeed.app.ui.theme.Tokens

/**
 * Zakladka "Pedal" - kolejnosc i etykiety 1:1 jak w Bafang Configuration Tool
 * (Pedal Assist): Pedal Sensor Type, Designated Assist Level, Speed Limit, Start Current,
 * Slow-start Mode, Start Degree, Work Mode, Stop Delay, Current Decay, Stop Decay, Keep Current.
 * Kazde pole to osobna, rozwijalna karta z opisem z Help.pdf. Wszystkie nastawy sterowane
 * przyciskami -/+ (tylko aktualna wartosc widoczna), poza Start Current i Keep Current,
 * ktore zostaja suwakami (krok 1%) oflankowanymi przyciskami -/+.
 */
@Composable
fun PedalScreen(
    state: UiState,
    onPedalType: (Int) -> Unit,
    onDesignatedAssist: (Int) -> Unit,
    onSpeedLimit: (Int) -> Unit,
    onStartCurrent: (Int) -> Unit,
    onSlowStartMode: (Int) -> Unit,
    onStartDegree: (Int) -> Unit,
    onWorkMode: (Int) -> Unit,
    onTimeOfStop: (Int) -> Unit,
    onCurrentDecay: (Int) -> Unit,
    onStopDecay: (Int) -> Unit,
    onKeepCurrent: (Int) -> Unit,
    onRead: () -> Unit,
    onWrite: () -> Unit,
    readWriteEnabled: Boolean,
    monitoringActive: Boolean,
) {
    val pas = state.pasOrDefault

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
            label = tr("Typ czujnika pedałowania", "Pedal Sensor Type", de = "Pedalsensor-Typ", fr = "Type de capteur de pédalage", es = "Tipo de sensor de pedaleo", pt = "Tipo de sensor de pedalada", it = "Tipo di sensore di pedalata", nl = "Type trapsensor", sv = "Typ av trampsensor", cs = "Typ snímače šlapání", sk = "Typ snímača šliapania", da = "Pedalsensortype", ru = "Тип датчика педалирования"),
            valueLabel = PEDAL_SENSOR_TYPE_LABELS.getOrElse(pas.pedalType) { "?" },
            description = tr(
                "Ten parametr wybiera typ czujnika obrotu pedałów. Jest ustawiany przez producenta i nie " +
                    "powinien być zmieniany.",
                "This parameter selects the pedal rotation sensor type. It is set by the manufacturer and " +
                    "should not be changed.",
                de = "Dieser Parameter wählt den Typ des Pedalumdrehungssensors. Er wird vom Hersteller " +
                    "eingestellt und sollte nicht geändert werden.",
                fr = "Ce paramètre sélectionne le type de capteur de rotation des pédales. Il est réglé par le " +
                    "fabricant et ne doit pas être modifié.",
                es = "Este parámetro selecciona el tipo de sensor de rotación de pedales. Lo establece el " +
                    "fabricante y no debería modificarse.",
                pt = "Este parâmetro seleciona o tipo de sensor de rotação dos pedais. É definido pelo fabricante " +
                    "e não deve ser alterado.",
                it = "Questo parametro seleziona il tipo di sensore di rotazione dei pedali. Viene impostato dal " +
                    "produttore e non dovrebbe essere modificato.",
                nl = "Deze parameter selecteert het type traprotatiesensor. Wordt door de fabrikant ingesteld en " +
                    "zou niet gewijzigd moeten worden.",
                sv = "Denna parameter väljer typen av trapprotationssensor. Ställs in av tillverkaren och " +
                    "bör inte ändras.",
                cs = "Tento parametr vybírá typ snímače otáčení pedálů. Nastavuje ho výrobce a " +
                    "neměl by se měnit.",
                sk = "Tento parameter vyberá typ snímača otáčania pedálov. Nastavuje ho výrobca a " +
                    "nemal by sa meniť.",
                da = "Denne parameter vælger typen af pedalrotationssensor. Den indstilles af producenten og " +
                    "bør ikke ændres.",
                ru = "Этот параметр выбирает тип датчика вращения педалей. Он устанавливается производителем и " +
                    "не должен изменяться.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onPedalType(pas.pedalType - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onPedalType(pas.pedalType + 1) }
            }
        }

        ExpandableParamTile(
            label = tr("Wskazany poziom wspomagania", "Designated Assist Level", de = "Festgelegte Unterstützungsstufe", fr = "Niveau d'assistance désigné", es = "Nivel de asistencia designado", pt = "Nível de assistência designado", it = "Livello di assistenza designato", nl = "Aangewezen ondersteuningsniveau", sv = "Angiven assistansnivå", cs = "Určená úroveň asistence", sk = "Určená úroveň asistencie", da = "Angivet assistanceniveau", ru = "Заданный уровень помощи"),
            valueLabel = designatedAssistLabel(pas.designatedAssist),
            description = tr(
                "Masz dwa tryby działania. \"By Display's Command\" oznacza, że poziom wspomagania (ten z " +
                    "zakładki General) będzie wybierany z Twojego wyświetlacza LCD. Druga opcja to wybór konkretnego " +
                    "poziomu wspomagania (0-9), który będzie stały i nie da się go zmienić z LCD.",
                "You have two types of operation. \"By Display's Command\" means the assist level (the " +
                    "one from the General tab) will be selected from your LCD. The second option is to choose a specific " +
                    "assist level (0-9) which will be fixed and you will not be able to change it from the LCD.",
                de = "Es gibt zwei Betriebsarten. „By Display's Command“ bedeutet, dass die Unterstützungsstufe " +
                    "(die aus dem Tab General) von deinem LCD-Display ausgewählt wird. Die zweite Option ist die " +
                    "Wahl einer bestimmten Unterstützungsstufe (0-9), die fest eingestellt bleibt und sich nicht vom " +
                    "LCD aus ändern lässt.",
                fr = "Il existe deux modes de fonctionnement. « By Display's Command » signifie que le niveau " +
                    "d'assistance (celui de l'onglet General) sera sélectionné depuis votre écran LCD. La seconde " +
                    "option consiste à choisir un niveau d'assistance précis (0-9), qui restera fixe et ne pourra " +
                    "pas être modifié depuis le LCD.",
                es = "Tienes dos modos de funcionamiento. «By Display's Command» significa que el nivel de " +
                    "asistencia (el de la pestaña General) se seleccionará desde tu pantalla LCD. La segunda opción " +
                    "es elegir un nivel de asistencia concreto (0-9), que quedará fijo y no podrá cambiarse desde el LCD.",
                pt = "Tens dois modos de funcionamento. «By Display's Command» significa que o nível de " +
                    "assistência (o da aba General) será selecionado pelo teu LCD. A segunda opção é escolher um " +
                    "nível de assistência específico (0-9), que ficará fixo e não poderá ser alterado pelo LCD.",
                it = "Hai due modalità di funzionamento. «By Display's Command» significa che il livello di " +
                    "assistenza (quello della scheda General) verrà selezionato dal tuo LCD. La seconda opzione è " +
                    "scegliere un livello di assistenza specifico (0-9), che rimarrà fisso e non potrà essere " +
                    "modificato dall'LCD.",
                nl = "Je hebt twee bedrijfsmodi. «By Display's Command» betekent dat het ondersteuningsniveau " +
                    "(dat van het tabblad General) via je LCD wordt geselecteerd. De tweede optie is een specifiek " +
                    "ondersteuningsniveau (0-9) te kiezen, dat vast blijft en niet via het LCD kan worden gewijzigd.",
                sv = "Du har två driftlägen. «By Display's Command» betyder att assistansnivån " +
                    "(den från fliken General) väljs från din LCD. Det andra alternativet är att välja en specifik " +
                    "assistansnivå (0-9) som blir fast och inte kan ändras från LCD:n.",
                cs = "Máš dva režimy provozu. „By Display's Command“ znamená, že úroveň asistence " +
                    "(ta z karty General) bude vybírána z tvého LCD. Druhou možností je zvolit konkrétní " +
                    "úroveň asistence (0-9), která zůstane pevná a nepůjde ji změnit z LCD.",
                sk = "Máš dva režimy prevádzky. „By Display's Command“ znamená, že úroveň asistencie " +
                    "(tá z karty General) bude vyberaná z tvojho LCD. Druhou možnosťou je zvoliť konkrétnu " +
                    "úroveň asistencie (0-9), ktorá zostane pevná a nepôjde ju zmeniť z LCD.",
                da = "Der er to driftstyper. „By Display's Command“ betyder, at assistanceniveauet " +
                    "(det fra fanen General) vælges fra dit LCD-display. Den anden mulighed er at vælge et " +
                    "bestemt assistanceniveau (0-9), som er fast, og du vil ikke kunne ændre det fra LCD'et.",
                ru = "Есть два режима работы. «By Display's Command» означает, что уровень поддержки " +
                    "(тот же, что на вкладке General) будет выбираться с вашего LCD. Второй вариант - " +
                    "выбрать конкретный уровень поддержки (0-9), который будет фиксированным, и изменить его " +
                    "с LCD будет нельзя.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onDesignatedAssist(pas.designatedAssist - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onDesignatedAssist(pas.designatedAssist + 1) }
            }
        }

        ExpandableParamTile(
            label = tr("Limit prędkości", "Speed Limit", de = "Geschwindigkeitslimit", fr = "Limite de vitesse", es = "Límite de velocidad", pt = "Limite de velocidade", it = "Limite di velocità", nl = "Snelheidslimiet", sv = "Hastighetsgräns", cs = "Omezení rychlosti", sk = "Obmedzenie rýchlosti", da = "Hastighedsgrænse", ru = "Ограничение скорости"),
            valueLabel = speedLimitLabel(pas.speedLimit),
            description = tr(
                "To jest maksymalna prędkość, przy której silnik będzie zapewniał dodatkowe przyspieszenie. Po " +
                    "jej osiągnięciu tylko utrzyma tę prędkość, nie przyspieszając dalej. \"By Display's Command\" " +
                    "pozwala ustawić prędkość z LCD. To ustawienie dotyczy wszystkich poziomów wspomagania z zakładki General.",
                "This is the maximum speed at which the motor will provide additional acceleration. Once " +
                    "reached it will only keep that speed, not accelerate further. \"By Display's Command\" lets you set " +
                    "the speed from your LCD. This setting applies to all assist levels seen in the General tab.",
                de = "Dies ist die maximale Geschwindigkeit, bei der der Motor zusätzliche Beschleunigung " +
                    "liefert. Nach Erreichen hält er diese Geschwindigkeit nur, ohne weiter zu beschleunigen. „By " +
                    "Display's Command“ erlaubt es, die Geschwindigkeit vom LCD aus einzustellen. Diese Einstellung " +
                    "gilt für alle Unterstützungsstufen im Tab General.",
                fr = "Il s'agit de la vitesse maximale à laquelle le moteur fournira une accélération " +
                    "supplémentaire. Une fois atteinte, il maintiendra seulement cette vitesse, sans accélérer " +
                    "davantage. « By Display's Command » permet de régler la vitesse depuis votre LCD. Ce réglage " +
                    "s'applique à tous les niveaux d'assistance de l'onglet General.",
                es = "Esta es la velocidad máxima a la que el motor proporcionará aceleración adicional. Al " +
                    "alcanzarla, solo mantendrá esa velocidad sin acelerar más. «By Display's Command» permite " +
                    "ajustar la velocidad desde tu LCD. Este ajuste se aplica a todos los niveles de asistencia de " +
                    "la pestaña General.",
                pt = "Esta é a velocidade máxima na qual o motor fornecerá aceleração adicional. Ao atingi-la, " +
                    "apenas manterá essa velocidade, sem acelerar mais. «By Display's Command» permite definir a " +
                    "velocidade a partir do teu LCD. Esta definição aplica-se a todos os níveis de assistência da " +
                    "aba General.",
                it = "Questa è la velocità massima alla quale il motore fornirà un'accelerazione aggiuntiva. Una " +
                    "volta raggiunta, manterrà solo quella velocità, senza accelerare ulteriormente. «By Display's " +
                    "Command» consente di impostare la velocità dal tuo LCD. Questa impostazione si applica a tutti " +
                    "i livelli di assistenza della scheda General.",
                nl = "Dit is de maximale snelheid waarbij de motor extra versnelling levert. Eenmaal bereikt, " +
                    "handhaaft hij alleen die snelheid, zonder verder te versnellen. Met «By Display's Command» " +
                    "kun je de snelheid via je LCD instellen. Deze instelling geldt voor alle ondersteuningsniveaus " +
                    "op het tabblad General.",
                sv = "Detta är den maximala hastigheten vid vilken motorn ger extra acceleration. När den " +
                    "nås bibehåller den bara den hastigheten, utan att accelerera mer. «By Display's Command» " +
                    "låter dig ställa in hastigheten från din LCD. Denna inställning gäller alla assistansnivåer " +
                    "på fliken General.",
                cs = "Toto je maximální rychlost, při které motor poskytuje další zrychlení. Po jejím " +
                    "dosažení už jen udržuje tuto rychlost, bez dalšího zrychlování. „By Display's Command“ " +
                    "umožňuje nastavit rychlost z tvého LCD. Toto nastavení platí pro všechny úrovně asistence " +
                    "na kartě General.",
                sk = "Toto je maximálna rýchlosť, pri ktorej motor poskytuje ďalšie zrýchlenie. Po jej " +
                    "dosiahnutí už len udržiava túto rýchlosť, bez ďalšieho zrýchľovania. „By Display's Command“ " +
                    "umožňuje nastaviť rýchlosť z tvojho LCD. Toto nastavenie platí pre všetky úrovne asistencie " +
                    "na karte General.",
                da = "Dette er den maksimale hastighed, hvor motoren yder ekstra acceleration. Når den er " +
                    "nået, vil den kun holde den hastighed og ikke accelerere yderligere. „By Display's Command“ " +
                    "lader dig indstille hastigheden fra dit LCD. Denne indstilling gælder for alle " +
                    "assistanceniveauer på fanen General.",
                ru = "Это максимальная скорость, при которой мотор обеспечивает дополнительное ускорение. По " +
                    "её достижении он будет только поддерживать эту скорость, не ускоряясь дальше. «By Display's " +
                    "Command» позволяет задать скорость с вашего LCD. Эта настройка применяется ко всем " +
                    "уровням поддержки на вкладке General.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onSpeedLimit(pas.speedLimit - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onSpeedLimit(pas.speedLimit + 1) }
            }
        }

        ExpandableParamTile(
            label = tr("Prąd startowy [%]", "Start Current [%]", de = "Startstrom [%]", fr = "Courant de démarrage [%]", es = "Corriente de arranque [%]", pt = "Corrente de arranque [%]", it = "Corrente di avvio [%]", nl = "Startstroom [%]", sv = "Startström [%]", cs = "Počáteční proud [%]", sk = "Počiatočný prúd [%]", da = "Startstrøm [%]", ru = "Пусковой ток [%]"),
            valueLabel = "${pas.startCurrentPct}%",
            description = tr(
                "To jest prąd startowy, gdy zaczynasz obracać pedałami. Ustaw go na co najmniej 10%, żeby rower " +
                    "zaczął się ruszać bez blokowania silnika. Zbyt wysoka wartość powoduje bardzo szybkie " +
                    "przyspieszenie na starcie, co może uszkodzić wewnętrzne przekładnie i silnik. Zalecany zakres: 10-30%.",
                "This is the startup current when you start rotating the pedals. Set it to at least 10% " +
                    "so the bicycle starts moving without stalling the motor. Too high a value causes very fast " +
                    "acceleration at start, which can damage the internal gears and the motor. Recommended range: 10-30%.",
                de = "Dies ist der Startstrom, wenn du beginnst, die Pedale zu drehen. Stelle ihn auf mindestens " +
                    "10%, damit sich das Fahrrad in Bewegung setzt, ohne den Motor zu blockieren. Ein zu hoher Wert " +
                    "verursacht eine sehr schnelle Beschleunigung beim Start, was die internen Getriebe und den " +
                    "Motor beschädigen kann. Empfohlener Bereich: 10-30%.",
                fr = "Il s'agit du courant de démarrage lorsque vous commencez à pédaler. Réglez-le à au moins " +
                    "10% pour que le vélo commence à avancer sans caler le moteur. Une valeur trop élevée provoque " +
                    "une accélération très rapide au démarrage, ce qui peut endommager les engrenages internes et " +
                    "le moteur. Plage recommandée : 10-30%.",
                es = "Esta es la corriente de arranque cuando empiezas a pedalear. Ajústala al menos al 10% para " +
                    "que la bicicleta comience a moverse sin calar el motor. Un valor demasiado alto provoca una " +
                    "aceleración muy rápida al arrancar, lo que puede dañar los engranajes internos y el motor. " +
                    "Rango recomendado: 10-30%.",
                pt = "Esta é a corrente de arranque quando começas a pedalar. Define-a em pelo menos 10% para " +
                    "que a bicicleta comece a mover-se sem bloquear o motor. Um valor demasiado alto causa uma " +
                    "aceleração muito rápida no arranque, o que pode danificar as engrenagens internas e o motor. " +
                    "Intervalo recomendado: 10-30%.",
                it = "Questa è la corrente di avvio quando inizi a pedalare. Impostala almeno al 10% affinché la " +
                    "bicicletta inizi a muoversi senza bloccare il motore. Un valore troppo alto provoca " +
                    "un'accelerazione molto rapida all'avvio, che può danneggiare gli ingranaggi interni e il " +
                    "motore. Intervallo consigliato: 10-30%.",
                nl = "Dit is de startstroom wanneer je begint met trappen. Stel deze in op minstens 10%, zodat " +
                    "de fiets in beweging komt zonder de motor te blokkeren. Een te hoge waarde veroorzaakt een " +
                    "zeer snelle versnelling bij het starten, wat de interne tandwielen en de motor kan " +
                    "beschadigen. Aanbevolen bereik: 10-30%.",
                sv = "Detta är startströmmen när du börjar trampa. Ställ in den till minst 10% så " +
                    "cykeln börjar röra sig utan att stanna motorn. Ett för högt värde orsakar mycket snabb " +
                    "acceleration vid start, vilket kan skada de interna växlarna och motorn. " +
                    "Rekommenderat intervall: 10-30%.",
                cs = "Toto je počáteční proud, když začneš šlapat. Nastav ho na alespoň 10%, aby se " +
                    "kolo rozjelo, aniž by se motor zablokoval. Příliš vysoká hodnota způsobuje velmi rychlé " +
                    "zrychlení při startu, což může poškodit vnitřní převody a motor. Doporučený rozsah: 10-30%.",
                sk = "Toto je počiatočný prúd, keď začneš šliapať. Nastav ho na aspoň 10%, aby sa " +
                    "bicykel rozbehol bez zablokovania motora. Príliš vysoká hodnota spôsobuje veľmi rýchle " +
                    "zrýchlenie pri štarte, čo môže poškodiť vnútorné prevody a motor. Odporúčaný rozsah: 10-30%.",
                da = "Dette er startstrømmen, når du begynder at træde i pedalerne. Indstil den til mindst " +
                    "10%, så cyklen begynder at bevæge sig uden at blokere motoren. En for høj værdi forårsager " +
                    "meget hurtig acceleration ved start, hvilket kan beskadige de interne gear og motoren. " +
                    "Anbefalet område: 10-30%.",
                ru = "Это пусковой ток, когда вы начинаете крутить педали. Установите его не менее 10%, " +
                    "чтобы велосипед начал двигаться без остановки мотора. Слишком высокое значение вызывает " +
                    "очень быстрое ускорение при старте, что может повредить внутренние передачи и мотор. " +
                    "Рекомендуемый диапазон: 10-30%.",
            ),
        ) {
            FlankedSlider(
                value = pas.startCurrentPct,
                range = 0..100,
                accent = Tokens.Amber,
                onValueChange = onStartCurrent,
            )
        }

        ExpandableParamTile(
            label = tr("Tryb wolnego startu (1-8)", "Slow-start Mode (1-8)", de = "Sanftanlauf-Modus (1-8)", fr = "Mode démarrage progressif (1-8)", es = "Modo de arranque suave (1-8)", pt = "Modo de arranque suave (1-8)", it = "Modalità avvio lento (1-8)", nl = "Zachte-startmodus (1-8)", sv = "Läge för mjukstart (1-8)", cs = "Režim pomalého startu (1-8)", sk = "Režim pomalého štartu (1-8)", da = "Blød start-tilstand (1-8)", ru = "Режим плавного старта (1-8)"),
            valueLabel = (pas.slowStartMode + 1).toString(),
            description = tr(
                "Kontroluje, jak szybko osiągany jest prąd startowy. Wartość ok. 4 zwykle działa dobrze dla " +
                    "normalnej jazdy. Niższe wartości przyspieszają start, co może być przydatne w terenie, ale " +
                    "grozi przeciążeniem sterownika i silnika.",
                "Controls how quickly the start current is reached. A value around 4 usually works well " +
                    "for normal cycling. Lower values make acceleration faster, which can be useful off-road but risks " +
                    "overloading the controller and motor.",
                de = "Steuert, wie schnell der Startstrom erreicht wird. Ein Wert um 4 funktioniert " +
                    "normalerweise gut für normales Fahren. Niedrigere Werte beschleunigen den Start, was im " +
                    "Gelände nützlich sein kann, aber das Risiko einer Überlastung von Steuergerät und Motor birgt.",
                fr = "Contrôle la vitesse à laquelle le courant de démarrage est atteint. Une valeur autour de " +
                    "4 fonctionne généralement bien pour une conduite normale. Des valeurs plus basses accélèrent " +
                    "le démarrage, ce qui peut être utile en tout-terrain, mais risque de surcharger le contrôleur " +
                    "et le moteur.",
                es = "Controla la rapidez con la que se alcanza la corriente de arranque. Un valor cercano a 4 " +
                    "suele funcionar bien para uso normal. Valores más bajos aceleran el arranque, lo que puede ser " +
                    "útil fuera de carretera, pero arriesga sobrecargar el controlador y el motor.",
                pt = "Controla a rapidez com que a corrente de arranque é atingida. Um valor à volta de 4 " +
                    "costuma funcionar bem para condução normal. Valores mais baixos tornam a aceleração mais " +
                    "rápida, o que pode ser útil em todo-o-terreno, mas arrisca sobrecarregar o controlador e o motor.",
                it = "Controlla la rapidità con cui viene raggiunta la corrente di avvio. Un valore intorno a 4 " +
                    "di solito funziona bene per la guida normale. Valori più bassi rendono l'accelerazione più " +
                    "rapida, il che può essere utile fuoristrada, ma rischia di sovraccaricare il controller e il motore.",
                nl = "Bepaalt hoe snel de startstroom wordt bereikt. Een waarde rond de 4 werkt meestal goed " +
                    "voor normaal rijden. Lagere waarden versnellen de start, wat handig kan zijn off-road, maar " +
                    "riskeert overbelasting van controller en motor.",
                sv = "Styr hur snabbt startströmmen nås. Ett värde runt 4 fungerar oftast bra " +
                    "för normal cykling. Lägre värden gör starten snabbare, vilket kan vara användbart terräng, men " +
                    "riskerar överbelastning av styrenheten och motorn.",
                cs = "Určuje, jak rychle je dosaženo počátečního proudu. Hodnota kolem 4 obvykle funguje dobře " +
                    "pro běžnou jízdu. Nižší hodnoty zrychlují start, což může být užitečné v terénu, ale " +
                    "hrozí přetížením řadiče a motoru.",
                sk = "Určuje, ako rýchlo sa dosiahne počiatočný prúd. Hodnota okolo 4 zvyčajne funguje dobre " +
                    "pre bežnú jazdu. Nižšie hodnoty zrýchľujú štart, čo môže byť užitočné v teréne, ale " +
                    "hrozí preťažením radiča a motora.",
                da = "Styrer, hvor hurtigt startstrømmen nås. En værdi omkring 4 fungerer normalt godt til " +
                    "almindelig cykling. Lavere værdier gør accelerationen hurtigere, hvilket kan være nyttigt " +
                    "uden for vej, men risikerer at overbelaste styreenheden og motoren.",
                ru = "Определяет, как быстро достигается пусковой ток. Значение около 4 обычно хорошо " +
                    "подходит для обычной езды. Более низкие значения делают ускорение быстрее, что может " +
                    "быть полезно вне дорог, но рискует перегрузить контроллер и мотор.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onSlowStartMode(pas.slowStartMode - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onSlowStartMode(pas.slowStartMode + 1) }
            }
        }

        ExpandableParamTile(
            label = tr("Stopień startu (nr sygnału)", "Start Degree (Signal No.)", de = "Startgrad (Signal-Nr.)", fr = "Degré de démarrage (n° signal)", es = "Grado de arranque (n.º señal)", pt = "Grau de arranque (n.º sinal)", it = "Grado di avvio (n. segnale)", nl = "Startgraad (signaalnr.)", sv = "Startgrad (signalnr.)", cs = "Stupeň startu (č. signálu)", sk = "Stupeň štartu (č. signálu)", da = "Startgrad (signalnr.)", ru = "Степень старта (номер сигнала)"),
            valueLabel = pas.startDegree.toString(),
            description = tr(
                "Ustala, ile impulsów z czujnika pedałowania jest potrzebnych, zanim silnik się uruchomi. Pełny " +
                    "obrót pedałów w zestawach BBS generuje 24 impulsy. 0 lub 1 nie zadziała. Wartość ok. 4 działa " +
                    "dobrze: nie za nerwowo, nie za duży wymagany obrót.",
                "Sets how many pulses from the pedal sensor are needed before the motor starts. A full " +
                    "pedal revolution on BBS kits generates 24 pulses. 0 or 1 will not work. A value around 4 works " +
                    "well: not too twitchy, not too much rotation required.",
                de = "Legt fest, wie viele Impulse vom Pedalsensor benötigt werden, bevor der Motor startet. " +
                    "Eine volle Pedalumdrehung bei BBS-Kits erzeugt 24 Impulse. 0 oder 1 funktioniert nicht. Ein " +
                    "Wert um 4 funktioniert gut: nicht zu nervös, nicht zu viel Drehung erforderlich.",
                fr = "Définit le nombre d'impulsions du capteur de pédalage nécessaires avant que le moteur ne " +
                    "démarre. Un tour complet de pédale sur les kits BBS génère 24 impulsions. 0 ou 1 ne " +
                    "fonctionnera pas. Une valeur autour de 4 fonctionne bien : ni trop nerveux, ni trop de " +
                    "rotation nécessaire.",
                es = "Establece cuántos pulsos del sensor de pedaleo se necesitan antes de que arranque el " +
                    "motor. Una vuelta completa de pedal en los kits BBS genera 24 pulsos. 0 o 1 no funcionará. Un " +
                    "valor cercano a 4 funciona bien: ni demasiado nervioso, ni requiere demasiada rotación.",
                pt = "Define quantos pulsos do sensor de pedalada são necessários antes de o motor arrancar. " +
                    "Uma rotação completa dos pedais nos kits BBS gera 24 pulsos. 0 ou 1 não funcionará. Um valor " +
                    "à volta de 4 funciona bem: nem demasiado nervoso, nem exige demasiada rotação.",
                it = "Imposta quanti impulsi dal sensore di pedalata sono necessari prima che il motore si " +
                    "avvii. Una rotazione completa dei pedali sui kit BBS genera 24 impulsi. 0 o 1 non " +
                    "funzioneranno. Un valore intorno a 4 funziona bene: non troppo nervoso, non richiede troppa rotazione.",
                nl = "Bepaalt hoeveel pulsen van de trapsensor nodig zijn voordat de motor start. Een volledige " +
                    "traprotatie op BBS-kits genereert 24 pulsen. 0 of 1 werkt niet. Een waarde rond de 4 werkt " +
                    "goed: niet te nerveus, niet te veel rotatie vereist.",
                sv = "Anger hur många pulser från trampsensorn som behövs innan motorn startar. Ett fullt " +
                    "trampvarv på BBS-kit genererar 24 pulser. 0 eller 1 fungerar inte. Ett värde runt 4 fungerar " +
                    "bra: inte för nervöst, inte för mycket rotation krävs.",
                cs = "Určuje, kolik impulzů ze snímače šlapání je potřeba, než se motor spustí. Plná " +
                    "otáčka pedálů u sad BBS generuje 24 impulzů. 0 nebo 1 nebude fungovat. Hodnota kolem 4 funguje " +
                    "dobře: ne příliš nervózní, ne příliš velká vyžadovaná rotace.",
                sk = "Určuje, koľko impulzov zo snímača šliapania je potrebných, kým sa motor spustí. Plná " +
                    "otáčka pedálov pri sadách BBS generuje 24 impulzov. 0 alebo 1 nebude fungovať. Hodnota okolo 4 funguje " +
                    "dobre: nie príliš nervózna, nie príliš veľká vyžadovaná rotácia.",
                da = "Angiver, hvor mange impulser fra pedalsensoren der kræves, før motoren starter. En fuld " +
                    "pedalomdrejning på BBS-kits genererer 24 impulser. 0 eller 1 vil ikke virke. En værdi omkring " +
                    "4 fungerer godt: ikke for nervøs, ikke for meget rotation krævet.",
                ru = "Задаёт, сколько импульсов от датчика педалей требуется до запуска мотора. Полный оборот " +
                    "педалей в комплектах BBS создаёт 24 импульса. 0 или 1 не будут работать. Значение около 4 " +
                    "работает хорошо: не слишком резко, не требует слишком большого вращения.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onStartDegree(pas.startDegree - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onStartDegree(pas.startDegree + 1) }
            }
        }

        ExpandableParamTile(
            label = tr("Tryb pracy", "Work Mode", de = "Arbeitsmodus", fr = "Mode de fonctionnement", es = "Modo de trabajo", pt = "Modo de funcionamento", it = "Modalità di lavoro", nl = "Werkmodus", sv = "Arbetsläge", cs = "Pracovní režim", sk = "Pracovný režim", da = "Arbejdstilstand", ru = "Рабочий режим"),
            valueLabel = if (pas.workMode == 0) {
                tr("Nieokreślony", "Undetermined", de = "Unbestimmt", fr = "Indéterminé", es = "Indeterminado", pt = "Indeterminado", it = "Indeterminato", nl = "Onbepaald", sv = "Obestämd", cs = "Neurčeno", sk = "Neurčené", da = "Ubestemt", ru = "Не определено")
            } else {
                pas.workMode.toString()
            },
            description = tr(
                "Dokładne przeznaczenie tego parametru nie jest dobrze udokumentowane - przypuszczalnie kontroluje " +
                    "moc w zależności od prędkości obrotu pedałów. Domyślna wartość producenta działa dobrze, więc " +
                    "normalnie nie musisz jej zmieniać.",
                "This parameter's exact purpose is not well documented - it is supposed to control power " +
                    "according to pedal rotation speed. The manufacturer's default value works fine, so you normally " +
                    "don't need to change it.",
                de = "Der genaue Zweck dieses Parameters ist nicht gut dokumentiert - er soll vermutlich die " +
                    "Leistung in Abhängigkeit von der Pedaldrehzahl steuern. Der Standardwert des Herstellers " +
                    "funktioniert gut, sodass du ihn normalerweise nicht ändern musst.",
                fr = "L'objectif exact de ce paramètre n'est pas bien documenté - il est censé contrôler la " +
                    "puissance en fonction de la vitesse de rotation des pédales. La valeur par défaut du " +
                    "fabricant fonctionne bien, donc vous n'avez normalement pas besoin de la modifier.",
                es = "El propósito exacto de este parámetro no está bien documentado - se supone que controla " +
                    "la potencia según la velocidad de rotación de los pedales. El valor predeterminado del " +
                    "fabricante funciona bien, así que normalmente no necesitas cambiarlo.",
                pt = "O propósito exato deste parâmetro não está bem documentado - presume-se que controle a " +
                    "potência de acordo com a velocidade de rotação dos pedais. O valor predefinido do fabricante " +
                    "funciona bem, por isso normalmente não precisas de o alterar.",
                it = "Lo scopo esatto di questo parametro non è ben documentato - dovrebbe controllare la " +
                    "potenza in base alla velocità di rotazione dei pedali. Il valore predefinito del produttore " +
                    "funziona bene, quindi normalmente non è necessario modificarlo.",
                nl = "Het exacte doel van deze parameter is niet goed gedocumenteerd - vermoedelijk regelt hij " +
                    "het vermogen op basis van de traprotatiesnelheid. De standaardwaarde van de fabrikant werkt " +
                    "goed, dus meestal hoef je deze niet te wijzigen.",
                sv = "Denna parameters exakta syfte är inte väl dokumenterat - den ska troligen styra " +
                    "effekten baserat på trampningens rotationshastighet. Tillverkarens standardvärde fungerar " +
                    "bra, så du behöver normalt inte ändra det.",
                cs = "Přesný účel tohoto parametru není dobře zdokumentován - pravděpodobně řídí " +
                    "výkon podle rychlosti otáčení pedálů. Výchozí hodnota výrobce funguje " +
                    "dobře, takže ji obvykle nepotřebuješ měnit.",
                sk = "Presný účel tohto parametra nie je dobre zdokumentovaný - pravdepodobne riadi " +
                    "výkon podľa rýchlosti otáčania pedálov. Predvolená hodnota výrobcu funguje " +
                    "dobre, takže ju zvyčajne nepotrebuješ meniť.",
                da = "Denne parameters nøjagtige formål er ikke godt dokumenteret - den skal formentlig styre " +
                    "effekten ud fra pedalrotationshastigheden. Producentens standardværdi fungerer fint, så du " +
                    "behøver normalt ikke ændre den.",
                ru = "Точное назначение этого параметра плохо задокументировано - предположительно он " +
                    "управляет мощностью в зависимости от скорости вращения педалей. Значение по умолчанию от " +
                    "производителя работает хорошо, поэтому обычно менять его не нужно.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onWorkMode(pas.workMode - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onWorkMode(pas.workMode + 1) }
            }
        }

        ExpandableParamTile(
            label = tr("Opóźnienie zatrzymania", "Stop Delay", de = "Stoppverzögerung", fr = "Délai d'arrêt", es = "Retardo de parada", pt = "Atraso de paragem", it = "Ritardo di arresto", nl = "Stopvertraging", sv = "Stoppfördröjning", cs = "Zpoždění zastavení", sk = "Oneskorenie zastavenia", da = "Stopforsinkelse", ru = "Задержка остановки"),
            valueLabel = "${pas.timeOfStop * 10} ms",
            description = tr(
                "Opóźnienie po zatrzymaniu pedałowania, przed zatrzymaniem silnika. Sterownik przyjmuje to tylko " +
                    "w krokach po 10ms, więc każde dotknięcie -/+ przesuwa wartość o 10ms. 250ms działa dobrze.",
                "The delay after you stop pedaling before the motor stops. The controller only accepts " +
                    "this in steps of 10ms, so each -/+ tap moves it by 10ms. 250ms works well.",
                de = "Die Verzögerung, nachdem du aufhörst zu treten, bevor der Motor stoppt. Das Steuergerät " +
                    "akzeptiert dies nur in Schritten von 10ms, sodass jedes -/+ Tippen den Wert um 10ms " +
                    "verschiebt. 250ms funktioniert gut.",
                fr = "Le délai après l'arrêt du pédalage avant que le moteur ne s'arrête. Le contrôleur " +
                    "n'accepte cela que par pas de 10ms, donc chaque appui sur -/+ déplace la valeur de 10ms. " +
                    "250ms fonctionne bien.",
                es = "El retardo tras dejar de pedalear antes de que el motor se detenga. El controlador solo " +
                    "acepta esto en pasos de 10ms, por lo que cada toque en -/+ mueve el valor en 10ms. 250ms " +
                    "funciona bien.",
                pt = "O atraso depois de parares de pedalar, antes de o motor parar. O controlador só aceita " +
                    "isto em passos de 10ms, por isso cada toque em -/+ move o valor em 10ms. 250ms funciona bem.",
                it = "Il ritardo dopo aver smesso di pedalare, prima che il motore si fermi. Il controller lo " +
                    "accetta solo in incrementi di 10ms, quindi ogni tocco su -/+ sposta il valore di 10ms. 250ms " +
                    "funziona bene.",
                nl = "De vertraging nadat je stopt met trappen, voordat de motor stopt. De controller " +
                    "accepteert dit alleen in stappen van 10ms, dus elke -/+ tik verschuift de waarde met 10ms. " +
                    "250ms werkt goed.",
                sv = "Fördröjningen efter att du slutar trampa, innan motorn stannar. Styrenheten " +
                    "accepterar detta endast i steg om 10ms, så varje -/+ tryck flyttar värdet 10ms. " +
                    "250ms fungerar bra.",
                cs = "Zpoždění po zastavení šlapání, než se motor zastaví. Řadič " +
                    "to přijímá pouze v krocích po 10ms, takže každé ťuknutí -/+ posune hodnotu o 10ms. " +
                    "250ms funguje dobře.",
                sk = "Oneskorenie po zastavení šliapania, kým sa motor zastaví. Radič " +
                    "to prijíma iba v krokoch po 10ms, takže každé ťuknutie -/+ posunie hodnotu o 10ms. " +
                    "250ms funguje dobre.",
                da = "Forsinkelsen efter du stopper med at træde, før motoren stopper. Styreenheden " +
                    "accepterer kun dette i trin på 10ms, så hvert -/+ tryk flytter værdien med 10ms. " +
                    "250ms fungerer godt.",
                ru = "Задержка после того, как вы перестаёте крутить педали, перед остановкой мотора. " +
                    "Контроллер принимает это только шагами по 10ms, поэтому каждое нажатие -/+ сдвигает " +
                    "значение на 10ms. 250ms работает хорошо.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onTimeOfStop(pas.timeOfStop - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onTimeOfStop(pas.timeOfStop + 1) }
            }
        }

        ExpandableParamTile(
            label = tr("Zanik prądu (1-8)", "Current Decay (1-8)", de = "Stromabfall (1-8)", fr = "Décroissance du courant (1-8)", es = "Caída de corriente (1-8)", pt = "Decaimento de corrente (1-8)", it = "Decadimento corrente (1-8)", nl = "Stroomafname (1-8)", sv = "Strömavklingning (1-8)", cs = "Pokles proudu (1-8)", sk = "Pokles prúdu (1-8)", da = "Strømaftagning (1-8)", ru = "Спад тока (1-8)"),
            valueLabel = pas.currentDecay.toString(),
            description = tr(
                "Ustala, jak szybko spada prąd, gdy pedałujesz szybciej i osiągasz maksymalną prędkość na wybranym " +
                    "poziomie wspomagania. Niższa wartość oznacza, że prąd zaczyna spadać przy niższej prędkości.",
                "Sets how fast the current drops when you are pedaling faster and reaching the maximum " +
                    "speed at the selected assist level. A lower value means the current starts dropping at a lower " +
                    "speed.",
                de = "Legt fest, wie schnell der Strom abfällt, wenn du schneller trittst und die maximale " +
                    "Geschwindigkeit der gewählten Unterstützungsstufe erreichst. Ein niedrigerer Wert bedeutet, " +
                    "dass der Strom bereits bei einer niedrigeren Geschwindigkeit abzufallen beginnt.",
                fr = "Définit la vitesse à laquelle le courant diminue lorsque vous pédalez plus vite et " +
                    "atteignez la vitesse maximale du niveau d'assistance sélectionné. Une valeur plus basse " +
                    "signifie que le courant commence à diminuer à une vitesse plus faible.",
                es = "Establece la rapidez con la que cae la corriente cuando pedaleas más rápido y alcanzas la " +
                    "velocidad máxima del nivel de asistencia seleccionado. Un valor más bajo significa que la " +
                    "corriente empieza a caer a una velocidad menor.",
                pt = "Define a rapidez com que a corrente diminui quando pedalas mais depressa e atinges a " +
                    "velocidade máxima do nível de assistência selecionado. Um valor mais baixo significa que a " +
                    "corrente começa a diminuir a uma velocidade mais baixa.",
                it = "Imposta la rapidità con cui la corrente diminuisce quando pedali più velocemente e " +
                    "raggiungi la velocità massima al livello di assistenza selezionato. Un valore più basso " +
                    "significa che la corrente inizia a diminuire a una velocità inferiore.",
                nl = "Bepaalt hoe snel de stroom afneemt wanneer je sneller trapt en de maximale snelheid van " +
                    "het geselecteerde ondersteuningsniveau bereikt. Een lagere waarde betekent dat de stroom bij " +
                    "een lagere snelheid begint af te nemen.",
                sv = "Styr hur snabbt strömmen minskar när du trampar snabbare och når den maximala " +
                    "hastigheten för den valda assistansnivån. Ett lägre värde innebär att strömmen börjar " +
                    "minska vid en lägre hastighet.",
                cs = "Určuje, jak rychle proud klesá, když šlapeš rychleji a dosahuješ maximální " +
                    "rychlosti zvolené úrovně asistence. Nižší hodnota znamená, že proud " +
                    "začíná klesat při nižší rychlosti.",
                sk = "Určuje, ako rýchlo prúd klesá, keď šliapeš rýchlejšie a dosahuješ maximálnu " +
                    "rýchlosť zvolenej úrovne asistencie. Nižšia hodnota znamená, že prúd " +
                    "začína klesať pri nižšej rýchlosti.",
                da = "Indstiller, hvor hurtigt strømmen falder, når du træder hurtigere og når den " +
                    "maksimale hastighed for det valgte assistanceniveau. En lavere værdi betyder, at " +
                    "strømmen begynder at falde ved en lavere hastighed.",
                ru = "Задаёт, как быстро падает ток, когда вы крутите педали быстрее и достигаете " +
                    "максимальной скорости на выбранном уровне поддержки. Более низкое значение означает, " +
                    "что ток начинает падать при более низкой скорости.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onCurrentDecay(pas.currentDecay - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onCurrentDecay(pas.currentDecay + 1) }
            }
        }

        ExpandableParamTile(
            label = tr("Zanik zatrzymania", "Stop Decay", de = "Stoppabfall", fr = "Décroissance à l'arrêt", es = "Caída al detenerse", pt = "Decaimento de paragem", it = "Decadimento arresto", nl = "Stopafname", sv = "Stoppavklingning", cs = "Pokles při zastavení", sk = "Pokles pri zastavení", da = "Stopaftagning", ru = "Спад при остановке"),
            valueLabel = "${pas.stopDecay * 10} ms",
            description = tr(
                "Czas potrzebny silnikowi na zatrzymanie się. Kroki po 10ms na dotknięcie -/+.",
                "The amount of time it takes the motor to stop. Steps of 10ms per -/+ tap.",
                de = "Die Zeit, die der Motor zum Stoppen benötigt. Schritte von 10ms pro -/+ Tippen.",
                fr = "Le temps nécessaire au moteur pour s'arrêter. Pas de 10ms par appui sur -/+.",
                es = "El tiempo que tarda el motor en detenerse. Pasos de 10ms por toque en -/+.",
                pt = "O tempo que o motor demora a parar. Passos de 10ms por toque em -/+.",
                it = "Il tempo necessario al motore per fermarsi. Incrementi di 10ms per tocco su -/+.",
                nl = "De tijd die de motor nodig heeft om te stoppen. Stappen van 10ms per -/+ tik.",
                sv = "Tiden det tar för motorn att stanna. Steg om 10ms per -/+ tryck.",
                cs = "Doba, kterou motor potřebuje k zastavení. Kroky po 10ms na ťuknutí -/+.",
                sk = "Doba, ktorú motor potrebuje na zastavenie. Kroky po 10ms na ťuknutie -/+.",
                da = "Den tid det tager motoren at stoppe. Trin på 10ms pr. -/+ tryk.",
                ru = "Время, необходимое мотору для остановки. Шаг 10ms на каждое нажатие -/+.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onStopDecay(pas.stopDecay - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onStopDecay(pas.stopDecay + 1) }
            }
        }

        ExpandableParamTile(
            label = tr("Podtrzymanie prądu [%]", "Keep Current [%]", de = "Stromhaltung [%]", fr = "Maintien du courant [%]", es = "Mantenimiento de corriente [%]", pt = "Manutenção de corrente [%]", it = "Mantenimento corrente [%]", nl = "Stroom vasthouden [%]", sv = "Bibehållen ström [%]", cs = "Udržení proudu [%]", sk = "Udržanie prúdu [%]", da = "Fasthold strøm [%]", ru = "Удержание тока [%]"),
            valueLabel = "${pas.keepCurrentPct}%",
            description = tr(
                "Procent maksymalnego prądu dla wybranego poziomu wspomagania, który dalej płynie przez silnik po " +
                    "osiągnięciu maksymalnej prędkości i dalszym pedałowaniu. Np. przy Current Limit 25A, PAS5 przy " +
                    "50% prądu daje maks. 12,5A dla tego poziomu; z Keep Current na 50%, prąd jest utrzymywany na " +
                    "6,25A po osiągnięciu maks. prędkości - płynne przejście zamiast nagłego spadku.",
                "Percentage of the maximum current for the selected assist level that keeps flowing " +
                    "through the motor once you reach the maximum speed and keep pedaling. E.g. with a 25A Current " +
                    "Limit, PAS5 at 50% current gives 12.5A max for that level; with Keep Current at 50%, the current " +
                    "is held at 6.25A once max speed is reached - a smooth transition instead of a sudden drop.",
                de = "Prozentsatz des maximalen Stroms für die gewählte Unterstützungsstufe, der weiterhin " +
                    "durch den Motor fließt, sobald du die maximale Geschwindigkeit erreichst und weiter trittst. " +
                    "Z. B. bei einem Current Limit von 25A ergibt PAS5 bei 50% Strom maximal 12,5A für diese " +
                    "Stufe; mit Keep Current auf 50% wird der Strom bei Erreichen der Höchstgeschwindigkeit bei " +
                    "6,25A gehalten - ein sanfter Übergang statt eines plötzlichen Abfalls.",
                fr = "Pourcentage du courant maximal pour le niveau d'assistance sélectionné qui continue de " +
                    "circuler dans le moteur une fois la vitesse maximale atteinte et en continuant à pédaler. " +
                    "Par ex. avec un Current Limit de 25A, PAS5 à 50% de courant donne 12,5A max pour ce niveau ; " +
                    "avec Keep Current à 50%, le courant est maintenu à 6,25A une fois la vitesse max atteinte - " +
                    "une transition douce au lieu d'une chute brutale.",
                es = "Porcentaje de la corriente máxima para el nivel de asistencia seleccionado que sigue " +
                    "circulando por el motor una vez alcanzada la velocidad máxima y sigues pedaleando. P. ej., " +
                    "con un Current Limit de 25A, PAS5 al 50% de corriente da 12,5A máx. para ese nivel; con Keep " +
                    "Current al 50%, la corriente se mantiene en 6,25A al alcanzar la velocidad máxima - una " +
                    "transición suave en lugar de una caída brusca.",
                pt = "Percentagem da corrente máxima para o nível de assistência selecionado que continua a " +
                    "fluir através do motor depois de atingires a velocidade máxima e continuares a pedalar. Por " +
                    "ex., com um Current Limit de 25A, PAS5 a 50% de corrente dá um máximo de 12,5A para esse " +
                    "nível; com Keep Current a 50%, a corrente é mantida em 6,25A ao atingir a velocidade máxima - " +
                    "uma transição suave em vez de uma queda brusca.",
                it = "Percentuale della corrente massima per il livello di assistenza selezionato che continua " +
                    "a fluire nel motore una volta raggiunta la velocità massima e continuando a pedalare. Ad es. " +
                    "con un Current Limit di 25A, PAS5 al 50% di corrente dà un massimo di 12,5A per quel livello; " +
                    "con Keep Current al 50%, la corrente viene mantenuta a 6,25A una volta raggiunta la velocità " +
                    "massima - una transizione fluida invece di un calo brusco.",
                nl = "Percentage van de maximale stroom voor het geselecteerde ondersteuningsniveau dat door de " +
                    "motor blijft vloeien zodra je de maximale snelheid bereikt en blijft trappen. Bijv. met een " +
                    "Current Limit van 25A geeft PAS5 bij 50% stroom maximaal 12,5A voor dat niveau; met Keep " +
                    "Current op 50% wordt de stroom bij het bereiken van de maximale snelheid vastgehouden op " +
                    "6,25A - een soepele overgang in plaats van een plotselinge daling.",
                sv = "Procentandel av maxströmmen för den valda assistansnivån som fortsätter flöda genom " +
                    "motorn när du når maxhastigheten och fortsätter trampa. T.ex. med en " +
                    "Current Limit på 25A ger PAS5 vid 50% ström max 12,5A för den nivån; med Keep " +
                    "Current på 50% hålls strömmen vid 6,25A när maxhastigheten nås - " +
                    "en mjuk övergång istället för ett plötsligt fall.",
                cs = "Procento maximálního proudu pro zvolenou úroveň asistence, který dál protéká " +
                    "motorem po dosažení maximální rychlosti a dalším šlapání. Např. při " +
                    "Current Limit 25A dává PAS5 při 50% proudu max. 12,5A pro tuto úroveň; s Keep " +
                    "Current na 50% je proud po dosažení maximální rychlosti udržován na " +
                    "6,25A - plynulý přechod místo náhlého poklesu.",
                sk = "Percento maximálneho prúdu pre zvolenú úroveň asistencie, ktorý ďalej preteká " +
                    "motorom po dosiahnutí maximálnej rýchlosti a ďalšom šliapaní. Napr. pri " +
                    "Current Limit 25A dáva PAS5 pri 50% prúdu max. 12,5A pre túto úroveň; s Keep " +
                    "Current na 50% je prúd po dosiahnutí maximálnej rýchlosti udržiavaný na " +
                    "6,25A - plynulý prechod namiesto náhleho poklesu.",
                da = "Procentdel af den maksimale strøm for det valgte assistanceniveau, som fortsætter med " +
                    "at flyde gennem motoren, når du når den maksimale hastighed og fortsætter med at træde. " +
                    "F.eks. med en Current Limit på 25A giver PAS5 ved 50% strøm maks. 12,5A for det niveau; " +
                    "med Keep Current på 50% holdes strømmen på 6,25A, når maks. hastighed er nået - en jævn " +
                    "overgang i stedet for et pludseligt fald.",
                ru = "Процент от максимального тока для выбранного уровня поддержки, который продолжает " +
                    "течь через мотор после достижения максимальной скорости при продолжении педалирования. " +
                    "Например, при Current Limit 25A, PAS5 при 50% тока даёт макс. 12,5A для этого уровня; при " +
                    "Keep Current на 50% ток удерживается на уровне 6,25A по достижении макс. скорости - " +
                    "плавный переход вместо резкого падения.",
            ),
        ) {
            FlankedSlider(
                value = pas.keepCurrentPct,
                range = 0..100,
                accent = Tokens.Amber,
                onValueChange = onKeepCurrent,
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}
