package com.bafspeed.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.bafspeed.app.protocol.SPEED_METER_TYPE_LABELS
import com.bafspeed.app.protocol.WHEEL_SIZE_LABELS
import com.bafspeed.app.ui.components.ExpandableParamTile
import com.bafspeed.app.ui.components.FlankedSlider
import com.bafspeed.app.ui.components.ReadWriteButtons
import com.bafspeed.app.ui.components.StepBtn
import com.bafspeed.app.ui.components.TelemetryPausedNotice
import com.bafspeed.app.ui.theme.Tokens

/**
 * Zakladka "General" - kolejnosc i etykiety 1:1 jak w Bafang Configuration Tool (Basic):
 * Low Battery Protection, Current Limit, Speed Meter Type, Speed Meter Signals, Wheel Diameter.
 * Zbudowana na tym samym wzorze co Pedal/Throttle: kazde pole to osobna, rozwijalna karta
 * z opisem z Help.pdf. Wszystkie nastawy sterowane przyciskami -/+ (tylko aktualna wartosc
 * widoczna), poza Low Battery Protection i Current Limit, ktore zostaja suwakami (krok 1)
 * oflankowanymi przyciskami -/+.
 */
@Composable
fun GeneralScreen(
    state: UiState,
    onLowBatteryProtectionChange: (Int) -> Unit,
    onCurrentLimitChange: (Int) -> Unit,
    onSpeedMeterTypeChange: (Int) -> Unit,
    onSpeedMeterSignalsChange: (Int) -> Unit,
    onWheelChange: (Int) -> Unit,
    onRead: () -> Unit,
    onWrite: () -> Unit,
    readWriteEnabled: Boolean,
    monitoringActive: Boolean,
) {
    val basic = state.basicOrDefault
    // Prąd maksymalny zgłoszony przez podłączony sterownik (blok GEN) - różne silniki
    // BBS01/BBS02/BBSHD mają różne wartości nominalne. Bez połączenia: bezpieczny fallback 40A.
    val maxCurrentA = state.general?.maxCurrentA?.takeIf { it > 0 } ?: 40

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
            label = tr("Ochrona niskiego napięcia", "Low Battery Protection", de = "Unterspannungsschutz", fr = "Protection contre la décharge", es = "Protección de bajo voltaje", pt = "Proteção de baixa tensão", it = "Protezione bassa tensione", nl = "Onderspanningsbeveiliging", sv = "Underspänningsskydd", cs = "Ochrana proti podpětí", sk = "Ochrana proti podpätiu", da = "Underspændingsbeskyttelse", ru = "Защита от глубокого разряда"),
            valueLabel = "${basic.lowBatteryProtection} V",
            description = tr(
                "Napięcie, przy którym sterownik zatrzyma silnik, żeby chronić baterię przed nadmiernym " +
                    "rozładowaniem. Powinno być prawidłowo ustawione przez producenta - normalnie nie musisz tego " +
                    "zmieniać. Dla pakietów 13S domyślną wartością jest 41V.",
                "This is the voltage at which the controller will stop the motor to protect your battery " +
                    "from over-discharge. It should be set correctly by the manufacturer - you normally don't need to " +
                    "change it. For 13S battery packs, 41V is the default.",
                de = "Dies ist die Spannung, bei der das Steuergerät den Motor stoppt, um die Batterie vor Tiefentladung " +
                    "zu schützen. Sie sollte vom Hersteller korrekt eingestellt sein - normalerweise musst du sie nicht " +
                    "ändern. Für 13S-Akkupacks ist 41V der Standardwert.",
                fr = "C'est la tension à laquelle le contrôleur arrêtera le moteur pour protéger la batterie contre une " +
                    "décharge excessive. Elle doit être correctement réglée par le fabricant - vous n'avez normalement pas " +
                    "besoin de la modifier. Pour les packs 13S, la valeur par défaut est 41V.",
                es = "Este es el voltaje al que el controlador detendrá el motor para proteger la batería de una descarga " +
                    "excesiva. Debería estar correctamente ajustado por el fabricante - normalmente no necesitas cambiarlo. " +
                    "Para paquetes de 13S, el valor predeterminado es 41V.",
                pt = "Esta é a tensão à qual o controlador irá parar o motor para proteger a bateria de uma descarga " +
                    "excessiva. Deve estar corretamente definida pelo fabricante - normalmente não precisas de a alterar. " +
                    "Para packs de 13S, o valor predefinido é 41V.",
                it = "Questa è la tensione alla quale il controller fermerà il motore per proteggere la batteria da una " +
                    "scarica eccessiva. Dovrebbe essere impostata correttamente dal produttore - normalmente non è " +
                    "necessario modificarla. Per i pacchi 13S, il valore predefinito è 41V.",
                nl = "Dit is de spanning waarbij de controller de motor stopt om de batterij te beschermen tegen te " +
                    "diepe ontlading. Deze zou correct moeten zijn ingesteld door de fabrikant - normaal gesproken hoef " +
                    "je dit niet te wijzigen. Voor 13S-accupacks is 41V de standaardwaarde.",
                sv = "Detta är spänningen vid vilken styrenheten stoppar motorn för att skydda batteriet mot djup " +
                    "urladdning. Den bör vara korrekt inställd av tillverkaren - normalt behöver du inte ändra den. " +
                    "För 13S-batteripaket är 41V standardvärdet.",
                cs = "Toto je napětí, při kterém řídicí jednotka zastaví motor, aby ochránila baterii před nadměrným " +
                    "vybitím. Mělo by být správně nastaveno výrobcem - běžně jej není třeba měnit. U 13S bateriových " +
                    "sad je výchozí hodnota 41V.",
                sk = "Toto je napätie, pri ktorom riadiaca jednotka zastaví motor, aby ochránila batériu pred nadmerným " +
                    "vybitím. Malo by byť správne nastavené výrobcom - bežne ho nie je potrebné meniť. Pri 13S " +
                    "batériových paketoch je predvolená hodnota 41V.",
                da = "Dette er spændingen, hvorved controlleren stopper motoren for at beskytte batteriet mod " +
                    "overafladning. Den bør være korrekt indstillet af producenten - normalt behøver du ikke ændre " +
                    "den. For 13S-batteripakker er 41V standardværdien.",
                ru = "Это напряжение, при котором контроллер остановит мотор, чтобы защитить батарею от чрезмерного " +
                    "разряда. Оно должно быть правильно установлено производителем - обычно менять его не нужно. " +
                    "Для батарей 13S значение по умолчанию - 41В.",
            ),
        ) {
            FlankedSlider(
                value = basic.lowBatteryProtection,
                range = 20..100,
                accent = Tokens.Blue,
                onValueChange = onLowBatteryProtectionChange,
            )
        }

        ExpandableParamTile(
            label = tr("Limit prądu [A]", "Current Limit [A]", de = "Strombegrenzung [A]", fr = "Limite de courant [A]", es = "Límite de corriente [A]", pt = "Limite de corrente [A]", it = "Limite di corrente [A]", nl = "Stroomlimiet [A]", sv = "Strömgräns [A]", cs = "Omezení proudu [A]", sk = "Obmedzenie prúdu [A]", da = "Strømgrænse [A]", ru = "Ограничение тока [A]"),
            valueLabel = "${basic.currentLimit} A",
            description = tr(
                "Maksymalny prąd, jaki może płynąć przez silnik. Najwyższa możliwa wartość jest zdefiniowana " +
                    "przez Twój sterownik - nie możesz ustawić wyżej. " + if (state.general?.maxCurrentA != null && state.general.maxCurrentA > 0) {
                        "W tej chwili to maksimum to ${state.general.maxCurrentA} A, zgłoszone przez podłączony sterownik."
                    } else {
                        "Połącz się ze sterownikiem, żeby odczytać jego realne maksimum."
                    },
                "The maximum current allowed to flow through the motor. The highest possible value is " +
                    "defined by your controller - you cannot set it any higher than that. " + if (state.general?.maxCurrentA != null && state.general.maxCurrentA > 0) {
                        "Right now that maximum is ${state.general.maxCurrentA} A, reported by your connected controller."
                    } else {
                        "Connect to your controller to read its real maximum."
                    },
                de = "Der maximale Strom, der durch den Motor fließen darf. Der höchstmögliche Wert wird von deinem " +
                    "Steuergerät festgelegt - du kannst ihn nicht höher einstellen. " + if (state.general?.maxCurrentA != null && state.general.maxCurrentA > 0) {
                        "Aktuell liegt dieses Maximum bei ${state.general.maxCurrentA} A, gemeldet vom angeschlossenen Steuergerät."
                    } else {
                        "Verbinde dich mit dem Steuergerät, um sein reales Maximum auszulesen."
                    },
                fr = "Le courant maximal autorisé à traverser le moteur. La valeur la plus élevée possible est définie " +
                    "par votre contrôleur - vous ne pouvez pas la régler plus haut. " + if (state.general?.maxCurrentA != null && state.general.maxCurrentA > 0) {
                        "Actuellement, ce maximum est de ${state.general.maxCurrentA} A, signalé par le contrôleur connecté."
                    } else {
                        "Connectez-vous au contrôleur pour lire son maximum réel."
                    },
                es = "La corriente máxima que puede circular por el motor. El valor más alto posible está definido por " +
                    "tu controlador - no puedes ajustarlo por encima de eso. " + if (state.general?.maxCurrentA != null && state.general.maxCurrentA > 0) {
                        "En este momento ese máximo es de ${state.general.maxCurrentA} A, indicado por el controlador conectado."
                    } else {
                        "Conéctate al controlador para leer su máximo real."
                    },
                pt = "A corrente máxima que pode fluir através do motor. O valor mais alto possível é definido pelo " +
                    "teu controlador - não podes definir um valor superior a esse. " + if (state.general?.maxCurrentA != null && state.general.maxCurrentA > 0) {
                        "Neste momento esse máximo é de ${state.general.maxCurrentA} A, reportado pelo controlador ligado."
                    } else {
                        "Liga-te ao controlador para ler o seu máximo real."
                    },
                it = "La corrente massima che può fluire attraverso il motore. Il valore più alto possibile è definito " +
                    "dal tuo controller - non puoi impostarlo più in alto. " + if (state.general?.maxCurrentA != null && state.general.maxCurrentA > 0) {
                        "Al momento questo massimo è di ${state.general.maxCurrentA} A, riportato dal controller collegato."
                    } else {
                        "Connettiti al controller per leggere il suo massimo reale."
                    },
                nl = "De maximale stroom die door de motor mag vloeien. De hoogst mogelijke waarde wordt bepaald door " +
                    "je controller - je kunt deze niet hoger instellen. " + if (state.general?.maxCurrentA != null && state.general.maxCurrentA > 0) {
                        "Op dit moment is dat maximum ${state.general.maxCurrentA} A, gerapporteerd door de aangesloten controller."
                    } else {
                        "Verbind met je controller om het werkelijke maximum uit te lezen."
                    },
                sv = "Den maximala strömmen som får flöda genom motorn. Det högsta möjliga värdet definieras av din " +
                    "styrenhet - du kan inte ställa in ett högre värde. " + if (state.general?.maxCurrentA != null && state.general.maxCurrentA > 0) {
                        "Just nu är det maximumet ${state.general.maxCurrentA} A, rapporterat av den anslutna styrenheten."
                    } else {
                        "Anslut till din styrenhet för att läsa av dess verkliga maximum."
                    },
                cs = "Maximální proud, který smí protékat motorem. Nejvyšší možnou hodnotu definuje řídicí jednotka - " +
                    "výše ji nastavit nelze. " + if (state.general?.maxCurrentA != null && state.general.maxCurrentA > 0) {
                        "Právě teď je toto maximum ${state.general.maxCurrentA} A, nahlášené připojenou řídicí jednotkou."
                    } else {
                        "Pro zjištění skutečného maxima je nutné připojit se k řídicí jednotce."
                    },
                sk = "Maximálny prúd, ktorý smie pretekať motorom. Najvyššiu možnú hodnotu určuje riadiaca jednotka - " +
                    "vyššie ju nastaviť nemožno. " + if (state.general?.maxCurrentA != null && state.general.maxCurrentA > 0) {
                        "Práve teraz je toto maximum ${state.general.maxCurrentA} A, nahlásené pripojenou riadiacou jednotkou."
                    } else {
                        "Na zistenie skutočného maxima je potrebné pripojiť sa k riadiacej jednotke."
                    },
                da = "Den maksimale strøm, der må løbe gennem motoren. Den højst mulige værdi er defineret af din " +
                    "controller - du kan ikke indstille den højere end det. " + if (state.general?.maxCurrentA != null && state.general.maxCurrentA > 0) {
                        "Lige nu er dette maksimum ${state.general.maxCurrentA} A, rapporteret af den tilsluttede controller."
                    } else {
                        "Opret forbindelse til din controller for at aflæse dens reelle maksimum."
                    },
                ru = "Максимальный ток, который может протекать через мотор. Наибольшее возможное значение " +
                    "определяется вашим контроллером - выше него установить нельзя. " + if (state.general?.maxCurrentA != null && state.general.maxCurrentA > 0) {
                        "Сейчас этот максимум составляет ${state.general.maxCurrentA} А, по данным подключённого контроллера."
                    } else {
                        "Подключитесь к контроллеру, чтобы узнать его реальный максимум."
                    },
            ),
        ) {
            FlankedSlider(
                value = basic.currentLimit,
                range = 1..maxCurrentA,
                accent = Tokens.Amber,
                onValueChange = onCurrentLimitChange,
            )
        }

        ExpandableParamTile(
            label = tr("Typ czujnika prędkości", "Speed Meter Type", de = "Geschwindigkeitssensor-Typ", fr = "Type de capteur de vitesse", es = "Tipo de sensor de velocidad", pt = "Tipo de sensor de velocidade", it = "Tipo di sensore di velocità", nl = "Type snelheidssensor", sv = "Typ av hastighetssensor", cs = "Typ snímače rychlosti", sk = "Typ snímača rýchlosti", da = "Hastighedssensor-type", ru = "Тип датчика скорости"),
            valueLabel = SPEED_METER_TYPE_LABELS.getOrElse(basic.speedMeterModel) { "?" },
            description = tr(
                "Wybiera czujnik prędkości używany na Twoim rowerze. Dla zestawów BBS to External. Ten parametr " +
                    "jest ustawiany przez producenta - jeśli Twój zestaw nie jest niestandardowy, nie musisz go zmieniać.",
                "Selects the speed meter used on your bicycle. For BBS kits it is External. This parameter " +
                    "is set by the manufacturer - if your setup isn't custom, you don't need to change it.",
                de = "Wählt den an deinem Fahrrad verwendeten Geschwindigkeitssensor. Bei BBS-Kits ist das External. " +
                    "Dieser Parameter wird vom Hersteller eingestellt - wenn dein Aufbau nicht individuell angepasst ist, " +
                    "musst du ihn nicht ändern.",
                fr = "Sélectionne le capteur de vitesse utilisé sur votre vélo. Pour les kits BBS, c'est External. Ce " +
                    "paramètre est défini par le fabricant - si votre configuration n'est pas personnalisée, vous n'avez " +
                    "pas besoin de le modifier.",
                es = "Selecciona el sensor de velocidad usado en tu bicicleta. Para los kits BBS es External. Este " +
                    "parámetro lo establece el fabricante - si tu configuración no es personalizada, no necesitas cambiarlo.",
                pt = "Seleciona o sensor de velocidade usado na tua bicicleta. Para kits BBS é External. Este " +
                    "parâmetro é definido pelo fabricante - se o teu conjunto não for personalizado, não precisas de o alterar.",
                it = "Seleziona il sensore di velocità utilizzato sulla tua bicicletta. Per i kit BBS è External. " +
                    "Questo parametro viene impostato dal produttore - se il tuo setup non è personalizzato, non è necessario modificarlo.",
                nl = "Selecteert de snelheidssensor die op je fiets wordt gebruikt. Voor BBS-kits is dit External. " +
                    "Deze parameter wordt door de fabrikant ingesteld - als je opstelling niet aangepast is, hoef je dit niet te wijzigen.",
                sv = "Väljer vilken hastighetssensor som används på din cykel. För BBS-kit är det External. Den här " +
                    "parametern ställs in av tillverkaren - om din uppsättning inte är anpassad behöver du inte ändra den.",
                cs = "Určuje, jaký snímač rychlosti se používá na kole. U sad BBS je to External. Tento parametr " +
                    "nastavuje výrobce - pokud sestava není upravená, není třeba jej měnit.",
                sk = "Určuje, aký snímač rýchlosti sa používa na bicykli. Pri súpravách BBS je to External. Tento " +
                    "parameter nastavuje výrobca - ak zostava nie je upravená, nie je potrebné ho meniť.",
                da = "Vælger den hastighedssensor, der bruges på din cykel. For BBS-kits er det External. Denne " +
                    "parameter indstilles af producenten - hvis din opsætning ikke er brugerdefineret, behøver du " +
                    "ikke at ændre den.",
                ru = "Выбирает датчик скорости, используемый на вашем велосипеде. Для комплектов BBS это External. " +
                    "Этот параметр устанавливается производителем - если ваша сборка не нестандартная, менять его " +
                    "не нужно.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onSpeedMeterTypeChange(basic.speedMeterModel - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onSpeedMeterTypeChange(basic.speedMeterModel + 1) }
            }
        }

        ExpandableParamTile(
            label = tr("Sygnały czujnika prędkości", "Speed Meter Signals", de = "Geschwindigkeitssensor-Signale", fr = "Signaux du capteur de vitesse", es = "Señales del sensor de velocidad", pt = "Sinais do sensor de velocidade", it = "Segnali del sensore di velocità", nl = "Snelheidssensor-signalen", sv = "Hastighetssensor-signaler", cs = "Signály snímače rychlosti", sk = "Signály snímača rýchlosti", da = "Hastighedssensor-signaler", ru = "Сигналы датчика скорости"),
            valueLabel = basic.speedMeterSignals.toString(),
            description = tr(
                "Ustala, ile sygnałów na obrót generuje Twój czujnik prędkości. Zewnętrzny czujnik z magnesem " +
                    "generuje jeden sygnał na obrót koła. Ustawiane przez producenta - jeśli Twój zestaw nie jest " +
                    "niestandardowy, nie musisz go zmieniać.",
                "Sets how many signals per revolution your speed sensor generates. An external sensor " +
                    "with a magnet generates one signal per wheel revolution. Set by the manufacturer - if your setup " +
                    "isn't custom, you don't need to change it.",
                de = "Legt fest, wie viele Signale pro Umdrehung dein Geschwindigkeitssensor erzeugt. Ein externer " +
                    "Sensor mit Magnet erzeugt ein Signal pro Radumdrehung. Wird vom Hersteller eingestellt - wenn dein " +
                    "Aufbau nicht individuell angepasst ist, musst du das nicht ändern.",
                fr = "Définit le nombre de signaux par tour générés par votre capteur de vitesse. Un capteur externe " +
                    "à aimant génère un signal par tour de roue. Réglé par le fabricant - si votre configuration n'est " +
                    "pas personnalisée, vous n'avez pas besoin de le modifier.",
                es = "Establece cuántas señales por vuelta genera tu sensor de velocidad. Un sensor externo con imán " +
                    "genera una señal por vuelta de rueda. Lo ajusta el fabricante - si tu configuración no es " +
                    "personalizada, no necesitas cambiarlo.",
                pt = "Define quantos sinais por rotação o teu sensor de velocidade gera. Um sensor externo com íman " +
                    "gera um sinal por rotação da roda. Definido pelo fabricante - se o teu conjunto não for " +
                    "personalizado, não precisas de o alterar.",
                it = "Imposta quanti segnali per rotazione genera il tuo sensore di velocità. Un sensore esterno con " +
                    "magnete genera un segnale per rotazione della ruota. Impostato dal produttore - se il tuo setup " +
                    "non è personalizzato, non è necessario modificarlo.",
                nl = "Bepaalt hoeveel signalen per omwenteling je snelheidssensor genereert. Een externe sensor met " +
                    "magneet genereert één signaal per wielomwenteling. Ingesteld door de fabrikant - als je opstelling " +
                    "niet aangepast is, hoef je dit niet te wijzigen.",
                sv = "Anger hur många signaler per varv din hastighetssensor genererar. En extern sensor med magnet " +
                    "genererar en signal per hjulvarv. Ställs in av tillverkaren - om din uppsättning inte är anpassad " +
                    "behöver du inte ändra den.",
                cs = "Určuje, kolik signálů na otáčku generuje snímač rychlosti. Externí snímač s magnetem generuje " +
                    "jeden signál na otáčku kola. Nastavuje výrobce - pokud sestava není upravená, není třeba to měnit.",
                sk = "Určuje, koľko signálov na otáčku generuje snímač rýchlosti. Externý snímač s magnetom generuje " +
                    "jeden signál na otáčku kolesa. Nastavuje výrobca - ak zostava nie je upravená, nie je potrebné to meniť.",
                da = "Angiver hvor mange signaler pr. omdrejning din hastighedssensor genererer. En ekstern sensor " +
                    "med magnet genererer ét signal pr. hjulomdrejning. Indstillet af producenten - hvis din " +
                    "opsætning ikke er brugerdefineret, behøver du ikke at ændre det.",
                ru = "Задаёт, сколько сигналов на оборот генерирует ваш датчик скорости. Внешний датчик с магнитом " +
                    "генерирует один сигнал на оборот колеса. Устанавливается производителем - если ваша сборка " +
                    "не нестандартная, менять это не нужно.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onSpeedMeterSignalsChange(basic.speedMeterSignals - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onSpeedMeterSignalsChange(basic.speedMeterSignals + 1) }
            }
        }

        ExpandableParamTile(
            label = tr("Średnica koła [cale]", "Wheel Diameter [Inch]", de = "Raddurchmesser [Zoll]", fr = "Diamètre de roue [pouces]", es = "Diámetro de rueda [pulgadas]", pt = "Diâmetro da roda [polegadas]", it = "Diametro ruota [pollici]", nl = "Wieldiameter [inch]", sv = "Hjuldiameter [tum]", cs = "Průměr kola [palce]", sk = "Priemer kolesa [palce]", da = "Hjuldiameter [tommer]", ru = "Диаметр колеса [дюймы]"),
            valueLabel = WHEEL_SIZE_LABELS.getOrElse(basic.wheelDiameterCode) { "?" },
            description = tr(
                "Średnica koła powinna odpowiadać realnemu rozmiarowi koła napędzanego (rower może mieć dwa koła " +
                    "różnych rozmiarów). Ustawienie mniejszej niż w rzeczywistości zwiększy wyświetlaną prędkość, ale " +
                    "może też doprowadzić do uszkodzenia silnika.",
                "The wheel diameter should match the real size of your drive wheel (a bicycle can have " +
                    "two different sized wheels). Setting it smaller than reality will increase the displayed speed but " +
                    "can also lead to motor damage.",
                de = "Der Raddurchmesser sollte der tatsächlichen Größe deines Antriebsrads entsprechen (ein Fahrrad " +
                    "kann zwei unterschiedlich große Räder haben). Wird er kleiner als in Wirklichkeit eingestellt, " +
                    "erhöht das die angezeigte Geschwindigkeit, kann aber auch zu Motorschäden führen.",
                fr = "Le diamètre de roue doit correspondre à la taille réelle de votre roue motrice (un vélo peut " +
                    "avoir deux roues de tailles différentes). Le régler plus petit que la réalité augmentera la vitesse " +
                    "affichée, mais peut aussi endommager le moteur.",
                es = "El diámetro de rueda debe coincidir con el tamaño real de tu rueda motriz (una bicicleta puede " +
                    "tener dos ruedas de tamaños diferentes). Ajustarlo más pequeño de lo real aumentará la velocidad " +
                    "mostrada, pero también puede dañar el motor.",
                pt = "O diâmetro da roda deve corresponder ao tamanho real da tua roda motriz (uma bicicleta pode " +
                    "ter duas rodas de tamanhos diferentes). Defini-lo mais pequeno do que a realidade aumentará a " +
                    "velocidade apresentada, mas também pode causar danos no motor.",
                it = "Il diametro della ruota dovrebbe corrispondere alla dimensione reale della tua ruota motrice " +
                    "(una bicicletta può avere due ruote di dimensioni diverse). Impostarlo più piccolo della realtà " +
                    "aumenterà la velocità visualizzata, ma può anche causare danni al motore.",
                nl = "De wieldiameter moet overeenkomen met de werkelijke grootte van je aangedreven wiel (een fiets " +
                    "kan twee wielen van verschillende grootte hebben). Deze kleiner instellen dan in werkelijkheid " +
                    "verhoogt de weergegeven snelheid, maar kan ook motorschade veroorzaken.",
                sv = "Hjuldiametern bör motsvara den verkliga storleken på ditt drivhjul (en cykel kan ha två hjul av " +
                    "olika storlek). Om den ställs in mindre än verkligheten ökar den visade hastigheten, men det kan " +
                    "också leda till motorskador.",
                cs = "Průměr kola by měl odpovídat skutečné velikosti hnaného kola (jízdní kolo může mít dvě kola " +
                    "různých velikostí). Nastavení menší hodnoty, než je skutečnost, zvýší zobrazovanou rychlost, ale " +
                    "může také způsobit poškození motoru.",
                sk = "Priemer kolesa by mal zodpovedať skutočnej veľkosti hnaného kolesa (bicykel môže mať dve kolesá " +
                    "rôznych veľkostí). Nastavenie menšej hodnoty, ako je skutočnosť, zvýši zobrazovanú rýchlosť, ale " +
                    "môže tiež spôsobiť poškodenie motora.",
                da = "Hjuldiameteren bør svare til den reelle størrelse på dit drivhjul (en cykel kan have to " +
                    "forskellige hjulstørrelser). Hvis den indstilles mindre end i virkeligheden, øges den viste " +
                    "hastighed, men det kan også føre til motorskader.",
                ru = "Диаметр колеса должен соответствовать реальному размеру ведущего колеса (у велосипеда могут " +
                    "быть два колеса разного размера). Установка меньшего значения, чем в реальности, увеличит " +
                    "отображаемую скорость, но может также привести к повреждению мотора.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onWheelChange(basic.wheelDiameterCode - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onWheelChange(basic.wheelDiameterCode + 1) }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
