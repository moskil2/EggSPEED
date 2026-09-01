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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bafspeed.app.UiState
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.protocol.BbsFwController
import com.bafspeed.app.ui.components.ExpandableParamTile
import com.bafspeed.app.ui.components.FlankedSlider
import com.bafspeed.app.ui.components.MicroLabel
import com.bafspeed.app.ui.components.ReadWriteButtons
import com.bafspeed.app.ui.components.StepBtn
import com.bafspeed.app.ui.components.TelemetryPausedNotice
import com.bafspeed.app.ui.components.TokenCard
import com.bafspeed.app.ui.components.ToggleRow
import com.bafspeed.app.ui.theme.Tokens

/**
 * Odpowiednik zakładki "System" z oficjalnej apki Windows autora bbs-fw (src/tool/View/SystemView.xaml) -
 * te same nazwy sekcji i pól, w tej samej kolejności, żeby użytkownik znający oryginalne narzędzie
 * miał bezpośrednie odniesienie. Opisy pól oparte na treści wiki projektu
 * (github.com/danielnilsson9/bbs-fw/wiki/Configuration-Tool) i tooltipach z SystemView.xaml.
 * Zastępuje dawne osobne ekrany General/Pedal/Throttle - jego apka trzyma to wszystko w jednej
 * zakładce (Assist Levels ma tylko poziomy wspomagania i przełączniki trybu - patrz [BbsFwAssistLevelsScreen]).
 */
@Composable
fun BbsFwSystemScreen(
    state: UiState,
    onMaxCurrent: (Int) -> Unit,
    onCurrentRamp: (Int) -> Unit,
    onMaxBatteryVoltageX100: (Int) -> Unit,
    onLowCutOff: (Int) -> Unit,
    onMaxSpeed: (Int) -> Unit,
    onThrottleStartVoltageMv: (Int) -> Unit,
    onThrottleEndVoltageMv: (Int) -> Unit,
    onThrottleStartPercent: (Int) -> Unit,
    onThrottleGlobalSpdLimOpt: (Int) -> Unit,
    onThrottleGlobalSpdLimPercent: (Int) -> Unit,
    onPasStartDelayPulses: (Int) -> Unit,
    onPasStopDelayX100s: (Int) -> Unit,
    onPasKeepCurrentPercent: (Int) -> Unit,
    onPasKeepCurrentCadenceRpm: (Int) -> Unit,
    onUseSpeedSensor: (Boolean) -> Unit,
    onUseShiftSensor: (Boolean) -> Unit,
    onUsePushWalk: (Boolean) -> Unit,
    onTemperatureSensorMode: (Int) -> Unit,
    onLightsMode: (Int) -> Unit,
    onWheelSizeX10: (Int) -> Unit,
    onSpeedSensorSignals: (Int) -> Unit,
    onShiftInterruptDurationMs: (Int) -> Unit,
    onShiftInterruptCurrentThreshold: (Int) -> Unit,
    onWalkModeDataDisplay: (Int) -> Unit,
    onUseFreedomUnits: (Boolean) -> Unit,
    onRead: () -> Unit,
    onWrite: () -> Unit,
    readWriteEnabled: Boolean,
    monitoringActive: Boolean,
) {
    val cfg = state.bbsFwConfigOrDefault
    val maxCurrentLimit = BbsFwController.maxCurrentAmps(state.bbsFwVersion?.ctrlType ?: 0)

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

        // --- Global ---
        MicroLabel(tr("Globalne", "Global", de = "Global", fr = "Global", es = "Global", pt = "Global", it = "Globale", nl = "Algemeen", sv = "Globalt", cs = "Globální", sk = "Globálne", da = "Globalt", ru = "Общие"))
        ExpandableParamTile(
            label = tr("Maks. prąd (A)", "Max Current (A)", de = "Max. Strom (A)", fr = "Courant max (A)", es = "Corriente máx. (A)", pt = "Corrente máx. (A)", it = "Corrente max (A)", nl = "Max. stroom (A)", sv = "Max ström (A)", cs = "Max. proud (A)", sk = "Max. prúd (A)", da = "Maks. strøm (A)", ru = "Макс. ток (A)"),
            valueLabel = "${cfg.maxCurrentAmps} A",
            description = tr(
                "Maksymalny prąd pobierany z baterii (do ${maxCurrentLimit}A dla Twojego sterownika). Na BBS02 nie " +
                    "przekraczaj bezpiecznych wartości, żeby nie uszkodzić silnika.",
                "Maximum current to draw from the battery (up to ${maxCurrentLimit}A for your controller). On BBS02, " +
                    "don't exceed safe limits to avoid motor damage.",
                de = "Maximaler Strom, der aus der Batterie gezogen wird (bis zu ${maxCurrentLimit}A für dein " +
                    "Steuergerät). Überschreite bei BBS02 nicht die sicheren Grenzwerte, um Motorschäden zu vermeiden.",
                fr = "Courant maximal prélevé sur la batterie (jusqu'à ${maxCurrentLimit}A pour votre contrôleur). " +
                    "Sur BBS02, ne dépassez pas les limites sûres pour éviter d'endommager le moteur.",
                es = "Corriente máxima extraída de la batería (hasta ${maxCurrentLimit}A para tu controlador). En " +
                    "BBS02, no superes los límites seguros para evitar dañar el motor.",
                pt = "Corrente máxima extraída da bateria (até ${maxCurrentLimit}A para o teu controlador). No " +
                    "BBS02, não excedas os limites seguros para evitar danificar o motor.",
                it = "Corrente massima prelevata dalla batteria (fino a ${maxCurrentLimit}A per il tuo controller). " +
                    "Sul BBS02, non superare i limiti sicuri per evitare danni al motore.",
                nl = "Maximale stroom die uit de batterij wordt getrokken (tot ${maxCurrentLimit}A voor je " +
                    "controller). Overschrijd bij de BBS02 niet de veilige limieten om motorschade te voorkomen.",
                sv = "Maximal ström som dras från batteriet (upp till ${maxCurrentLimit}A för din " +
                    "styrenhet). På BBS02, överskrid inte säkra gränser för att undvika motorskador.",
                cs = "Maximální proud odebíraný z baterie (až ${maxCurrentLimit}A pro tvůj " +
                    "řadič). U BBS02 nepřekračuj bezpečné limity, aby nedošlo k poškození motoru.",
                sk = "Maximálny prúd odoberaný z batérie (až ${maxCurrentLimit}A pre tvoj " +
                    "radič). Pri BBS02 neprekračuj bezpečné limity, aby nedošlo k poškodeniu motora.",
                da = "Maksimal strøm, der trækkes fra batteriet (op til ${maxCurrentLimit}A for din " +
                    "controller). På BBS02 må du ikke overskride sikre grænser for at undgå motorskader.",
                ru = "Максимальный ток, потребляемый от батареи (до ${maxCurrentLimit}А для вашего " +
                    "контроллера). На BBS02 не превышайте безопасные пределы, чтобы избежать повреждения мотора.",
            ),
        ) {
            FlankedSlider(value = cfg.maxCurrentAmps, range = 5..maxCurrentLimit, accent = Tokens.Amber, onValueChange = onMaxCurrent)
        }
        ExpandableParamTile(
            label = tr("Narastanie prądu (A/s)", "Current Ramp (A/s)", de = "Stromanstieg (A/s)", fr = "Rampe de courant (A/s)", es = "Rampa de corriente (A/s)", pt = "Rampa de corrente (A/s)", it = "Rampa di corrente (A/s)", nl = "Stroomoploop (A/s)", sv = "Strömramp (A/s)", cs = "Náběh proudu (A/s)", sk = "Nábeh prúdu (A/s)", da = "Strømrampe (A/s)", ru = "Нарастание тока (А/с)"),
            valueLabel = "${cfg.currentRampAmpsS} A/s",
            description = tr(
                "Narastanie prądu w amperach na sekundę przy załączaniu PAS lub Cruise.",
                "Current ramp up in Amps per second when engaging PAS or Cruise.",
                de = "Stromanstieg in Ampere pro Sekunde beim Aktivieren von PAS oder Cruise.",
                fr = "Augmentation du courant en ampères par seconde lors de l'activation de PAS ou Cruise.",
                es = "Aumento de corriente en amperios por segundo al activar PAS o Cruise.",
                pt = "Aumento de corrente em amperes por segundo ao ativar PAS ou Cruise.",
                it = "Aumento della corrente in ampere al secondo quando si attiva PAS o Cruise.",
                nl = "Stroomtoename in ampère per seconde bij het activeren van PAS of Cruise.",
                sv = "Strömökning i ampere per sekund vid aktivering av PAS eller Cruise.",
                cs = "Náběh proudu v ampérech za sekundu při zapnutí PAS nebo Cruise.",
                sk = "Nábeh prúdu v ampéroch za sekundu pri zapnutí PAS alebo Cruise.", da = "Strømstigning i ampere per sekund ved aktivering af PAS eller Cruise.", ru = "Нарастание тока в амперах в секунду при включении PAS или Cruise.",
            ),
        ) {
            FlankedSlider(value = cfg.currentRampAmpsS, range = 1..255, accent = Tokens.Amber, onValueChange = onCurrentRamp)
        }
        ExpandableParamTile(
            label = tr("Maks. napięcie baterii (V)", "Max Battery Voltage (V)", de = "Max. Batteriespannung (V)", fr = "Tension batterie max (V)", es = "Voltaje máx. de batería (V)", pt = "Voltagem máx. da bateria (V)", it = "Tensione max batteria (V)", nl = "Max. batterijspanning (V)", sv = "Max batterispänning (V)", cs = "Max. napětí baterie (V)", sk = "Max. napätie batérie (V)", da = "Maks. batterispænding (V)", ru = "Макс. напряжение батареи (В)"),
            valueLabel = "${cfg.maxBatteryX100v / 100.0} V",
            description = tr(
                "Maksymalne napięcie Twojej baterii - używane do wyliczenia % naładowania (SOC).",
                "Maximum voltage of your battery, used for battery SOC(%) calculation.",
                de = "Maximale Spannung deiner Batterie - wird zur Berechnung des Ladezustands (SOC %) verwendet.",
                fr = "Tension maximale de votre batterie - utilisée pour calculer le % de charge (SOC).",
                es = "Voltaje máximo de tu batería - se usa para calcular el % de carga (SOC).",
                pt = "Tensão máxima da tua bateria - usada para calcular o SOC(%) da bateria.",
                it = "Tensione massima della tua batteria - usata per calcolare lo stato di carica SOC(%).",
                nl = "Maximale spanning van je batterij - gebruikt voor de berekening van het laadpercentage (SOC%).",
                sv = "Maximal spänning på ditt batteri - används för beräkning av laddningsnivå (SOC%).",
                cs = "Maximální napětí tvé baterie - používá se pro výpočet stavu nabití (SOC %).",
                sk = "Maximálne napätie tvojej batérie - používa sa na výpočet stavu nabitia (SOC %).", da = "Maksimal spænding på dit batteri - bruges til beregning af batteriets ladetilstand (SOC%).", ru = "Максимальное напряжение вашей батареи - используется для расчёта уровня заряда (SOC%).",
            ),
        ) {
            FlankedSlider(value = cfg.maxBatteryX100v / 100, range = 1..100, accent = Tokens.Blue, onValueChange = { onMaxBatteryVoltageX100(it * 100) })
        }
        ExpandableParamTile(
            label = tr("Odcięcie niskiego napięcia (V)", "Low Voltage Cutoff (V)", de = "Unterspannungsabschaltung (V)", fr = "Coupure basse tension (V)", es = "Corte de bajo voltaje (V)", pt = "Corte de baixa tensão (V)", it = "Taglio bassa tensione (V)", nl = "Onderspanningsafsluiting (V)", sv = "Underspänningsavstängning (V)", cs = "Vypnutí při nízkém napětí (V)", sk = "Vypnutie pri nízkom napätí (V)", da = "Underspændingsafbrydelse (V)", ru = "Отключение при низком напряжении (В)"),
            valueLabel = "${cfg.lowCutOffV} V",
            description = tr(
                "Próg niskiego napięcia, przy którym moc silnika jest odcinana, żeby chronić baterię.",
                "Low voltage detection for when to cut power to motor to protect battery.",
                de = "Unterspannungsschwelle, bei der die Motorleistung abgeschaltet wird, um die Batterie zu schützen.",
                fr = "Seuil de basse tension auquel la puissance du moteur est coupée pour protéger la batterie.",
                es = "Umbral de bajo voltaje al que se corta la potencia del motor para proteger la batería.",
                pt = "Deteção de baixa tensão para saber quando cortar a energia ao motor e proteger a bateria.",
                it = "Rilevamento di bassa tensione per determinare quando interrompere l'alimentazione al motore " +
                    "per proteggere la batteria.",
                nl = "Detectie van lage spanning om te bepalen wanneer het motorvermogen wordt afgesneden om de " +
                    "batterij te beschermen.",
                sv = "Underspänningsdetektion för när motorns effekt ska brytas för att skydda " +
                    "batteriet.",
                cs = "Detekce nízkého napětí pro určení, kdy odpojit výkon motoru na ochranu " +
                    "baterie.",
                sk = "Detekcia nízkeho napätia na určenie, kedy odpojiť výkon motora na ochranu " +
                    "batérie.",
                da = "Registrering af lav spænding for at afgøre, hvornår motorens effekt skal afbrydes for at " +
                    "beskytte batteriet.",
                ru = "Определение низкого напряжения для отключения питания мотора с целью защиты " +
                    "батареи.",
            ),
        ) {
            FlankedSlider(value = cfg.lowCutOffV, range = 1..100, accent = Tokens.Blue, onValueChange = onLowCutOff)
        }
        ExpandableParamTile(
            label = tr("Maks. prędkość (km/h)", "Max Speed (km/h)", de = "Max. Geschwindigkeit (km/h)", fr = "Vitesse max (km/h)", es = "Velocidad máx. (km/h)", pt = "Velocidade máx. (km/h)", it = "Velocità max (km/h)", nl = "Max. snelheid (km/u)", sv = "Max hastighet (km/h)", cs = "Max. rychlost (km/h)", sk = "Max. rýchlosť (km/h)", da = "Maks. hastighed (km/t)", ru = "Макс. скорость (км/ч)"),
            valueLabel = "${cfg.maxSpeedKph} km/h",
            description = tr(
                "Maksymalna prędkość (przy włączonym czujniku prędkości) w km/h.",
                "Maximum speed (if using speed sensor) in km/h.",
                de = "Maximale Geschwindigkeit (bei Verwendung eines Geschwindigkeitssensors) in km/h.",
                fr = "Vitesse maximale (avec capteur de vitesse) en km/h.",
                es = "Velocidad máxima (usando sensor de velocidad) en km/h.",
                pt = "Velocidade máxima (se estiveres a usar sensor de velocidade) em km/h.",
                it = "Velocità massima (se si utilizza il sensore di velocità) in km/h.",
                nl = "Maximale snelheid (bij gebruik van een snelheidssensor) in km/u.",
                sv = "Maximal hastighet (vid användning av hastighetssensor) i km/h.",
                cs = "Maximální rychlost (při použití snímače rychlosti) v km/h.",
                sk = "Maximálna rýchlosť (pri použití snímača rýchlosti) v km/h.", da = "Maksimal hastighed (ved brug af hastighedssensor) i km/t.", ru = "Максимальная скорость (при использовании датчика скорости) в км/ч.",
            ),
        ) {
            FlankedSlider(value = cfg.maxSpeedKph, range = 0..180, accent = Tokens.Emerald, onValueChange = onMaxSpeed)
        }

        // --- Throttle ---
        MicroLabel(tr("Manetka", "Throttle", de = "Gasgriff", fr = "Accélérateur", es = "Acelerador", pt = "Acelerador", it = "Acceleratore", nl = "Gasgreep", sv = "Gasreglage", cs = "Plynová páčka", sk = "Plynová páčka", da = "Gashåndtag", ru = "Газ"))
        ExpandableParamTile(
            label = tr("Napięcie startowe (mV)", "Start Voltage (mV)", de = "Startspannung (mV)", fr = "Tension de démarrage (mV)", es = "Voltaje de arranque (mV)", pt = "Tensão de arranque (mV)", it = "Tensione di avvio (mV)", nl = "Startspanning (mV)", sv = "Startspänning (mV)", cs = "Počáteční napětí (mV)", sk = "Počiatočné napätie (mV)", da = "Startspænding (mV)", ru = "Начальное напряжение (мВ)"),
            valueLabel = "${cfg.throttleStartVoltageMv} mV",
            description = tr(
                "Ustawienie niższe niż minimalne napięcie sygnału z manetki spowoduje błąd.",
                "Setting lower than the minimum voltage signal from the throttle will result in an error.",
                de = "Eine Einstellung unterhalb der minimalen Signalspannung des Gasgriffs führt zu einem Fehler.",
                fr = "Un réglage inférieur à la tension minimale du signal de l'accélérateur entraînera une erreur.",
                es = "Un ajuste por debajo del voltaje mínimo de señal del acelerador provocará un error.",
                pt = "Uma definição inferior à tensão mínima do sinal do acelerador resultará num erro.",
                it = "Un'impostazione inferiore alla tensione minima del segnale dell'acceleratore causerà un errore.",
                nl = "Een instelling lager dan de minimale signaalspanning van de gasgreep resulteert in een fout.",
                sv = "En inställning lägre än den minsta signalspänningen från gasreglaget resulterar i ett fel.",
                cs = "Nastavení nižší než minimální napětí signálu z plynové páčky způsobí chybu.",
                sk = "Nastavenie nižšie ako minimálne napätie signálu z plynovej páčky spôsobí chybu.", da = "En indstilling lavere end minimumsspændingssignalet fra gashåndtaget vil resultere i en fejl.", ru = "Значение ниже минимального сигнала напряжения от газа приведёт к ошибке.",
            ),
        ) {
            FlankedSlider(value = cfg.throttleStartVoltageMv, range = 200..2500, accent = Tokens.Amber, onValueChange = onThrottleStartVoltageMv)
        }
        ExpandableParamTile(
            label = tr("Napięcie końcowe (mV)", "End Voltage (mV)", de = "Endspannung (mV)", fr = "Tension finale (mV)", es = "Voltaje final (mV)", pt = "Tensão final (mV)", it = "Tensione finale (mV)", nl = "Eindspanning (mV)", sv = "Slutspänning (mV)", cs = "Konečné napětí (mV)", sk = "Konečné napätie (mV)", da = "Slutspænding (mV)", ru = "Конечное напряжение (мВ)"),
            valueLabel = "${cfg.throttleEndVoltageMv} mV",
            description = tr(
                "Ustawienie wyższe niż maksymalny sygnał z manetki uniemożliwi osiągnięcie pełnej mocy.",
                "Setting this higher than the maximum signal from the throttle will make it impossible to reach maximum power.",
                de = "Eine Einstellung oberhalb des maximalen Signals des Gasgriffs macht es unmöglich, die volle " +
                    "Leistung zu erreichen.",
                fr = "Un réglage supérieur au signal maximal de l'accélérateur rendra impossible d'atteindre la " +
                    "puissance maximale.",
                es = "Un ajuste por encima de la señal máxima del acelerador hará imposible alcanzar la potencia máxima.",
                pt = "Definir isto acima do sinal máximo do acelerador tornará impossível atingir a potência máxima.",
                it = "Impostare questo valore superiore al segnale massimo dell'acceleratore renderà impossibile " +
                    "raggiungere la potenza massima.",
                nl = "Als je dit hoger instelt dan het maximale signaal van de gasgreep, wordt het onmogelijk om " +
                    "het maximale vermogen te bereiken.",
                sv = "Om du ställer in detta högre än gasreglagets maximala signal blir det omöjligt att " +
                    "nå maximal effekt.",
                cs = "Nastavení vyšší než maximální signál plynové páčky znemožní dosažení " +
                    "maximálního výkonu.",
                sk = "Nastavenie vyššie ako maximálny signál plynovej páčky znemožní dosiahnutie " +
                    "maximálneho výkonu.",
                da = "Hvis dette indstilles højere end det maksimale signal fra gashåndtaget, bliver det umuligt " +
                    "at nå maksimal effekt.",
                ru = "Установка значения выше максимального сигнала газа сделает невозможным достижение " +
                    "максимальной мощности.",
            ),
        ) {
            FlankedSlider(value = cfg.throttleEndVoltageMv, range = 2500..5000, accent = Tokens.Amber, onValueChange = onThrottleEndVoltageMv)
        }
        ExpandableParamTile(
            label = tr("Prąd startowy (%)", "Start Current (%)", de = "Startstrom (%)", fr = "Courant de démarrage (%)", es = "Corriente de arranque (%)", pt = "Corrente de arranque (%)", it = "Corrente di avvio (%)", nl = "Startstroom (%)", sv = "Startström (%)", cs = "Počáteční proud (%)", sk = "Počiatočný prúd (%)", da = "Startstrøm (%)", ru = "Начальный ток (%)"),
            valueLabel = "${cfg.throttleStartPercent}%",
            description = tr(
                "Minimalna moc przy najmniejszym wychyleniu manetki.",
                "Minimum power to apply for lowest throttle input.",
                de = "Minimale Leistung bei geringstem Gasgriffausschlag.",
                fr = "Puissance minimale à appliquer pour la plus faible sollicitation de l'accélérateur.",
                es = "Potencia mínima aplicada con la menor pulsación del acelerador.",
                pt = "Potência mínima a aplicar para a menor entrada do acelerador.",
                it = "Potenza minima da applicare per il minimo input dell'acceleratore.",
                nl = "Minimaal vermogen dat wordt toegepast bij de laagste gasgreepinvoer.",
                sv = "Minsta effekt som appliceras vid lägsta gasreglageinsats.",
                cs = "Minimální výkon uplatněný při nejnižším vstupu plynové páčky.",
                sk = "Minimálny výkon uplatnený pri najnižšom vstupe plynovej páčky.", da = "Minimal effekt der anvendes ved laveste gasindgang.", ru = "Минимальная мощность, применяемая при минимальном сигнале газа.",
            ),
        ) {
            FlankedSlider(value = cfg.throttleStartPercent, range = 0..100, accent = Tokens.Amber, onValueChange = onThrottleStartPercent)
        }
        ExpandableParamTile(
            label = tr("Opcje globalnego limitu prędkości", "Global Speed Limit Options", de = "Optionen für globales Geschwindigkeitslimit", fr = "Options de limite de vitesse globale", es = "Opciones de límite de velocidad global", pt = "Opções de limite de velocidade global", it = "Opzioni limite di velocità globale", nl = "Opties globale snelheidslimiet", sv = "Alternativ för global hastighetsgräns", cs = "Možnosti globálního omezení rychlosti", sk = "Možnosti globálneho obmedzenia rýchlosti", da = "Globale hastighedsgrænseindstillinger", ru = "Параметры глобального ограничения скорости"),
            valueLabel = listOf(
                tr("Wyłączony", "Disabled", de = "Deaktiviert", fr = "Désactivé", es = "Desactivado", pt = "Desativado", it = "Disattivato", nl = "Uitgeschakeld", sv = "Avstängd", cs = "Vypnuto", sk = "Vypnuté", da = "Deaktiveret", ru = "Отключено"),
                tr("Włączony", "Enabled", de = "Aktiviert", fr = "Activé", es = "Activado", pt = "Ativado", it = "Attivato", nl = "Ingeschakeld", sv = "Aktiverad", cs = "Zapnuto", sk = "Zapnuté", da = "Aktiveret", ru = "Включено"),
                tr("Standardowe poziomy", "Standard Levels", de = "Standard-Stufen", fr = "Niveaux Standard", es = "Niveles Standard", pt = "Níveis Standard", it = "Livelli Standard", nl = "Standaardniveaus", sv = "Standardnivåer", cs = "Standardní úrovně", sk = "Štandardné úrovne", da = "Standardniveauer", ru = "Стандартные уровни"),
            ).getOrElse(cfg.throttleGlobalSpdLimOpt) { "?" },
            description = tr(
                "Globalny limit prędkości dla manetki, przydatny tam, gdzie prawo ogranicza użycie manetki do " +
                    "określonej prędkości. Wyłączony = brak limitu. Włączony = dotyczy wszystkich poziomów. " +
                    "Standardowe poziomy = tylko profil Standard (nie Sport).",
                "Global speed limit for the throttle, useful where local law only permits throttle use up to a " +
                    "specific speed. Disabled = no limit. Enabled = applies to all assist levels. Standard Levels = " +
                    "only the Standard profile (not Sport).",
                de = "Globales Geschwindigkeitslimit für den Gasgriff, nützlich dort, wo lokale Gesetze die " +
                    "Nutzung des Gasgriffs auf eine bestimmte Geschwindigkeit beschränken. Deaktiviert = kein " +
                    "Limit. Aktiviert = gilt für alle Unterstützungsstufen. Standard-Stufen = nur das " +
                    "Standard-Profil (nicht Sport).",
                fr = "Limite de vitesse globale pour l'accélérateur, utile lorsque la loi locale ne permet " +
                    "l'usage de l'accélérateur que jusqu'à une vitesse précise. Désactivé = pas de limite. " +
                    "Activé = s'applique à tous les niveaux d'assistance. Niveaux Standard = uniquement le " +
                    "profil Standard (pas Sport).",
                es = "Límite de velocidad global para el acelerador, útil donde la ley local solo permite usar " +
                    "el acelerador hasta una velocidad concreta. Desactivado = sin límite. Activado = se aplica " +
                    "a todos los niveles de asistencia. Niveles Standard = solo el perfil Standard (no Sport).",
                pt = "Limite de velocidade global para o acelerador, útil onde a lei local só permite o uso do " +
                    "acelerador até uma velocidade específica. Desativado = sem limite. Ativado = aplica-se a " +
                    "todos os níveis de assistência. Níveis Standard = apenas o perfil Standard (não Sport).",
                it = "Limite di velocità globale per l'acceleratore, utile dove la legge locale consente l'uso " +
                    "dell'acceleratore solo fino a una velocità specifica. Disattivato = nessun limite. " +
                    "Attivato = si applica a tutti i livelli di assistenza. Livelli Standard = solo il profilo " +
                    "Standard (non Sport).",
                nl = "Globale snelheidslimiet voor de gasgreep, handig waar de lokale wetgeving het gebruik van " +
                    "de gasgreep alleen tot een bepaalde snelheid toestaat. Uitgeschakeld = geen limiet. " +
                    "Ingeschakeld = geldt voor alle ondersteuningsniveaus. Standaardniveaus = alleen het " +
                    "Standard-profiel (niet Sport).",
                sv = "Global hastighetsgräns för gasreglaget, användbar där lokal lag endast tillåter " +
                    "gasreglaget upp till en viss hastighet. Avstängd = ingen gräns. " +
                    "Aktiverad = gäller alla assistansnivåer. Standardnivåer = endast " +
                    "Standard-profilen (inte Sport).",
                cs = "Globální limit rychlosti pro plynovou páčku, užitečný tam, kde místní zákon povoluje " +
                    "použití plynové páčky pouze do určité rychlosti. Vypnuto = žádný limit. " +
                    "Zapnuto = platí pro všechny úrovně asistence. Standardní úrovně = pouze " +
                    "profil Standard (ne Sport).",
                sk = "Globálny limit rýchlosti pre plynovú páčku, užitočný tam, kde miestny zákon povoľuje " +
                    "použitie plynovej páčky iba do určitej rýchlosti. Vypnuté = žiadny limit. " +
                    "Zapnuté = platí pre všetky úrovne asistencie. Štandardné úrovne = iba " +
                    "profil Standard (nie Sport).",
                da = "Global hastighedsgrænse for gashåndtaget, nyttig hvor lokal lovgivning kun tillader brug " +
                    "af gashåndtaget op til en bestemt hastighed. Deaktiveret = ingen grænse. " +
                    "Aktiveret = gælder alle understøttelsesniveauer. Standardniveauer = kun " +
                    "Standard-profilen (ikke Sport).",
                ru = "Глобальное ограничение скорости для газа, полезно там, где местное законодательство " +
                    "разрешает использование газа только до определённой скорости. Отключено = без " +
                    "ограничения. Включено = применяется ко всем уровням помощи. Стандартные уровни = " +
                    "только профиль Standard (не Sport).",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onThrottleGlobalSpdLimOpt(cfg.throttleGlobalSpdLimOpt - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onThrottleGlobalSpdLimOpt(cfg.throttleGlobalSpdLimOpt + 1) }
            }
        }
        ExpandableParamTile(
            label = tr("Globalny limit prędkości (%)", "Global Speed Limit (%)", de = "Globales Geschwindigkeitslimit (%)", fr = "Limite de vitesse globale (%)", es = "Límite de velocidad global (%)", pt = "Limite de velocidade global (%)", it = "Limite di velocità globale (%)", nl = "Globale snelheidslimiet (%)", sv = "Global hastighetsgräns (%)", cs = "Globální omezení rychlosti (%)", sk = "Globálne obmedzenie rýchlosti (%)", da = "Global hastighedsgrænse (%)", ru = "Глобальное ограничение скорости (%)"),
            valueLabel = "${cfg.throttleGlobalSpdLimPercent}%",
            description = tr(
                "Limit prędkości w % skonfigurowanej prędkości maksymalnej, gdy powyższa opcja jest włączona.",
                "Set speed limit in % of configured max speed when global throttle speed limit is enabled.",
                de = "Geschwindigkeitslimit in % der konfigurierten Höchstgeschwindigkeit, wenn die obige Option " +
                    "aktiviert ist.",
                fr = "Limite de vitesse en % de la vitesse maximale configurée lorsque l'option ci-dessus est activée.",
                es = "Límite de velocidad en % de la velocidad máxima configurada cuando la opción anterior está activada.",
                pt = "Define o limite de velocidade em % da velocidade máxima configurada quando a opção acima " +
                    "está ativada.",
                it = "Imposta il limite di velocità in % della velocità massima configurata quando l'opzione " +
                    "sopra è attivata.",
                nl = "Stelt de snelheidslimiet in als % van de geconfigureerde maximale snelheid wanneer de " +
                    "bovenstaande optie is ingeschakeld.",
                sv = "Ställer in hastighetsgränsen i % av den konfigurerade maxhastigheten när alternativet " +
                    "ovan är aktiverat.",
                cs = "Nastaví limit rychlosti v % nakonfigurované maximální rychlosti, když je výše " +
                    "uvedená možnost zapnutá.",
                sk = "Nastaví limit rýchlosti v % nakonfigurovanej maximálnej rýchlosti, keď je vyššie " +
                    "uvedená možnosť zapnutá.",
                da = "Indstiller hastighedsgrænsen i % af den konfigurerede maksimale hastighed, når " +
                    "ovenstående globale hastighedsgrænse for gashåndtaget er aktiveret.",
                ru = "Устанавливает ограничение скорости в % от настроенной максимальной скорости, когда " +
                    "включена указанная выше опция глобального ограничения.",
            ),
        ) {
            FlankedSlider(value = cfg.throttleGlobalSpdLimPercent, range = 0..100, accent = Tokens.Amber, onValueChange = onThrottleGlobalSpdLimPercent)
        }

        // --- Pedal Assist ---
        MicroLabel(tr("Wspomaganie pedałowania", "Pedal Assist", de = "Pedal Assist", fr = "Assistance au pédalage", es = "Asistencia de pedaleo", pt = "Assistência de pedalada", it = "Assistenza pedalata", nl = "Trapondersteuning", sv = "Pedalassistans", cs = "Asistence šlapání", sk = "Asistencia šliapania", da = "Pedalassistance", ru = "Педальная поддержка"))
        ExpandableParamTile(
            label = tr("Opóźnienie startu (°)", "Start Delay (°)", de = "Startverzögerung (°)", fr = "Délai de démarrage (°)", es = "Retardo de arranque (°)", pt = "Atraso de arranque (°)", it = "Ritardo di avvio (°)", nl = "Startvertraging (°)", sv = "Startfördröjning (°)", cs = "Prodleva startu (°)", sk = "Oneskorenie štartu (°)", da = "Startforsinkelse (°)", ru = "Задержка старта (°)"),
            valueLabel = "${cfg.pasStartDelayPulses * 15}°",
            description = tr(
                "Opóźnienie startu w stopniach obrotu korby, po którym załącza się PAS (24 impulsy = 360° = pełny obrót).",
                "Start delay in degrees for when PAS shall engage (24 pulses = 360° = one full crank revolution).",
                de = "Startverzögerung in Grad der Kurbeldrehung, nach der PAS aktiviert wird (24 Impulse = 360° = " +
                    "eine volle Kurbelumdrehung).",
                fr = "Délai de démarrage en degrés de rotation de la manivelle avant l'activation de PAS (24 " +
                    "impulsions = 360° = un tour complet de manivelle).",
                es = "Retardo de arranque en grados de rotación de la biela antes de activar PAS (24 pulsos = " +
                    "360° = una vuelta completa de biela).",
                pt = "Atraso de arranque em graus de rotação da manivela após o qual o PAS é ativado (24 pulsos " +
                    "= 360° = uma rotação completa da manivela).",
                it = "Ritardo di avvio in gradi di rotazione della pedivella dopo il quale il PAS si attiva (24 " +
                    "impulsi = 360° = una rotazione completa della pedivella).",
                nl = "Startvertraging in graden crankrotatie waarna PAS wordt geactiveerd (24 pulsen = 360° = " +
                    "één volledige crankomwenteling).",
                sv = "Startfördröjning i grader vevrotation innan PAS aktiveras (24 pulser = 360° = " +
                    "ett helt vevvarv).",
                cs = "Prodleva startu ve stupních otáčení kliky, po které se PAS zapne (24 impulzů = 360° = " +
                    "jedna plná otáčka kliky).",
                sk = "Oneskorenie štartu v stupňoch otáčania kľuky, po ktorom sa PAS zapne (24 impulzov = 360° = " +
                    "jedna plná otáčka kľuky).",
                da = "Startforsinkelse i grader af krankrotation, hvorefter PAS aktiveres (24 impulser = 360° = " +
                    "én hel krankomdrejning).",
                ru = "Задержка старта в градусах вращения шатуна, после которой включается PAS (24 импульса = " +
                    "360° = один полный оборот шатуна).",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onPasStartDelayPulses(cfg.pasStartDelayPulses - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onPasStartDelayPulses(cfg.pasStartDelayPulses + 1) }
            }
        }
        ExpandableParamTile(
            label = tr("Opóźnienie zatrzymania (ms)", "Stop Delay (ms)", de = "Stoppverzögerung (ms)", fr = "Délai d'arrêt (ms)", es = "Retardo de parada (ms)", pt = "Atraso de paragem (ms)", it = "Ritardo di arresto (ms)", nl = "Stopvertraging (ms)", sv = "Stoppfördröjning (ms)", cs = "Prodleva vypnutí (ms)", sk = "Oneskorenie vypnutia (ms)", da = "Stopforsinkelse (ms)", ru = "Задержка остановки (мс)"),
            valueLabel = "${cfg.pasStopDelayX100s * 10} ms",
            description = tr(
                "Opóźnienie w milisekundach, po którym PAS się wyłącza po zatrzymaniu pedałowania.",
                "Stop delay in milliseconds for when PAS shall disengage.",
                de = "Verzögerung in Millisekunden, nach der sich PAS deaktiviert, sobald du aufhörst zu treten.",
                fr = "Délai en millisecondes avant la désactivation de PAS après l'arrêt du pédalage.",
                es = "Retardo en milisegundos antes de que PAS se desactive tras dejar de pedalear.",
                pt = "Atraso de paragem em milissegundos após o qual o PAS se desativa quando paras de pedalar.",
                it = "Ritardo di arresto in millisecondi dopo il quale il PAS si disattiva quando smetti di pedalare.",
                nl = "Stopvertraging in milliseconden waarna PAS wordt gedeactiveerd zodra je stopt met trappen.",
                sv = "Stoppfördröjning i millisekunder tills PAS kopplas ur när du slutar trampa.",
                cs = "Prodleva vypnutí v milisekundách, po které se PAS vypne po zastavení šlapání.",
                sk = "Oneskorenie vypnutia v milisekundách, po ktorom sa PAS vypne po zastavení šliapania.", da = "Stopforsinkelse i millisekunder, hvorefter PAS deaktiveres.", ru = "Задержка отключения в миллисекундах, после которой PAS выключается.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onPasStopDelayX100s(cfg.pasStopDelayX100s - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onPasStopDelayX100s(cfg.pasStopDelayX100s + 1) }
            }
        }
        ExpandableParamTile(
            label = tr("Podtrzymanie prądu (%)", "Keep Current (%)", de = "Stromhaltung (%)", fr = "Maintien du courant (%)", es = "Mantenimiento de corriente (%)", pt = "Manutenção de corrente (%)", it = "Mantenimento corrente (%)", nl = "Stroom vasthouden (%)", sv = "Bibehållen ström (%)", cs = "Udržení proudu (%)", sk = "Udržanie prúdu (%)", da = "Bevar strøm (%)", ru = "Удержание тока (%)"),
            valueLabel = "${cfg.pasKeepCurrentPercent}%",
            description = tr(
                "Podtrzymuje ten procent zadanego prądu poziomu, gdy osiągnięto docelową kadencję. Dotyczy tylko " +
                    "trybów opartych o kadencję (nie Torque/Variable).",
                "Keep this motor current in percent of assist level target current when target cadence of assist " +
                    "level has been reached. Applies only to cadence-based modes.",
                de = "Hält diesen Prozentsatz des Zielstroms der Unterstützungsstufe, sobald die Zielkadenz " +
                    "erreicht ist. Gilt nur für kadenzbasierte Modi (nicht Torque/Variable).",
                fr = "Maintient ce pourcentage du courant cible du niveau d'assistance une fois la cadence " +
                    "cible atteinte. S'applique uniquement aux modes basés sur la cadence (pas Torque/Variable).",
                es = "Mantiene este porcentaje de la corriente objetivo del nivel de asistencia al alcanzar la " +
                    "cadencia objetivo. Se aplica solo a los modos basados en cadencia (no Torque/Variable).",
                pt = "Mantém esta percentagem da corrente-alvo do nível de assistência quando a cadência-alvo " +
                    "do nível de assistência é atingida. Aplica-se apenas a modos baseados em cadência (não " +
                    "Torque/Variable).",
                it = "Mantiene questa percentuale della corrente target del livello di assistenza quando viene " +
                    "raggiunta la cadenza target del livello di assistenza. Si applica solo alle modalità " +
                    "basate sulla cadenza (non Torque/Variable).",
                nl = "Behoudt dit percentage van de doelstroom van het ondersteuningsniveau zodra de " +
                    "doelcadans van het ondersteuningsniveau is bereikt. Geldt alleen voor cadansgebaseerde " +
                    "modi (niet Torque/Variable).",
                sv = "Bibehåller denna procent av assistansnivåns målström när assistansnivåns " +
                    "målkadens har uppnåtts. Gäller endast kadensbaserade " +
                    "lägen (inte Torque/Variable).",
                cs = "Udržuje toto procento cílového proudu úrovně asistence po dosažení " +
                    "cílové kadence úrovně asistence. Platí pouze pro režimy založené na " +
                    "kadenci (ne Torque/Variable).",
                sk = "Udržiava toto percento cieľového prúdu úrovne asistencie po dosiahnutí " +
                    "cieľovej kadencie úrovne asistencie. Platí iba pre režimy založené na " +
                    "kadencii (nie Torque/Variable).",
                da = "Bevarer denne procentdel af assistanceniveauets målstrøm, når assistanceniveauets " +
                    "målkadence er nået. Gælder kun for kadencebaserede tilstande " +
                    "(ikke Torque/Variable).",
                ru = "Удерживает этот процент от целевого тока уровня помощи по достижении " +
                    "целевого каденса уровня помощи. Применяется только к режимам на основе " +
                    "каденса (не Torque/Variable).",
            ),
        ) {
            FlankedSlider(value = cfg.pasKeepCurrentPercent, range = 10..100, accent = Tokens.Amber, onValueChange = onPasKeepCurrentPercent)
        }
        ExpandableParamTile(
            label = tr("Kadencja podtrzymania prądu (rpm)", "Keep Current Cadence (rpm)", de = "Kadenz für Stromhaltung (U/min)", fr = "Cadence de maintien du courant (rpm)", es = "Cadencia de mantenimiento de corriente (rpm)", pt = "Cadência de manutenção de corrente (rpm)", it = "Cadenza di mantenimento corrente (rpm)", nl = "Cadans voor stroom vasthouden (rpm)", sv = "Kadens för bibehållen ström (rpm)", cs = "Kadence pro udržení proudu (rpm)", sk = "Kadencia pre udržanie prúdu (rpm)", da = "Kadence for strømfastholdelse (rpm)", ru = "Каденс удержания тока (об/мин)"),
            valueLabel = "${cfg.pasKeepCurrentCadenceRpm} rpm",
            description = tr(
                "Dolny próg kadencji, od którego zaczyna działać rampa \"Keep Current %\" powyżej.",
                "Lower cadence limit for when 'Keep Current %' ramp starts.",
                de = "Untere Kadenzschwelle, ab der die obige „Keep Current %“-Rampe zu wirken beginnt.",
                fr = "Seuil de cadence inférieur à partir duquel la rampe « Keep Current % » ci-dessus commence.",
                es = "Umbral inferior de cadencia a partir del cual empieza a actuar la rampa «Keep Current %» anterior.",
                pt = "Limite inferior de cadência a partir do qual a rampa «Keep Current %» acima começa a atuar.",
                it = "Limite inferiore di cadenza da cui inizia ad agire la rampa «Keep Current %» sopra.",
                nl = "Onderste cadanslimiet waarbij de «Keep Current %»-rampe hierboven begint te werken.",
                sv = "Nedre kadensgräns där rampen «Keep Current %» ovan börjar verka.",
                cs = "Dolní limit kadence, od kterého začíná působit rampa «Keep Current %» výše.",
                sk = "Dolný limit kadencie, od ktorého začína pôsobiť rampa «Keep Current %» vyššie.", da = "Nedre kadencegrænse for, hvornår «Keep Current %»-rampen ovenfor starter.", ru = "Нижний порог каденса, с которого начинает действовать рампа «Keep Current %» выше.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onPasKeepCurrentCadenceRpm(cfg.pasKeepCurrentCadenceRpm - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onPasKeepCurrentCadenceRpm(cfg.pasKeepCurrentCadenceRpm + 1) }
            }
        }

        // --- Features ---
        MicroLabel(tr("Funkcje", "Features", de = "Funktionen", fr = "Fonctionnalités", es = "Funciones", pt = "Funcionalidades", it = "Funzionalità", nl = "Functies", sv = "Funktioner", cs = "Funkce", sk = "Funkcie", da = "Funktioner", ru = "Функции"))
        TokenCard(borderColor = Tokens.WhiteBorder) {
            ToggleRow(
                tr("Czujnik prędkości", "Speed Sensor", de = "Geschwindigkeitssensor", fr = "Capteur de vitesse", es = "Sensor de velocidad", pt = "Sensor de velocidade", it = "Sensore di velocità", nl = "Snelheidssensor", sv = "Hastighetssensor", cs = "Snímač rychlosti", sk = "Snímač rýchlosti", da = "Hastighedssensor", ru = "Датчик скорости"), cfg.useSpeedSensor, onUseSpeedSensor, accent = Tokens.Blue,
                description = tr(
                    "Jeśli czujnik prędkości ulegnie awarii, silnik nadal będzie działał.",
                    "If your speed sensor malfunctions your motor will still work.",
                    de = "Falls dein Geschwindigkeitssensor ausfällt, funktioniert der Motor trotzdem weiter.",
                    fr = "Si votre capteur de vitesse tombe en panne, le moteur continuera de fonctionner.",
                    es = "Si tu sensor de velocidad falla, el motor seguirá funcionando.",
                    pt = "Se o teu sensor de velocidade avariar, o motor continuará a funcionar.",
                    it = "Se il tuo sensore di velocità si guasta, il motore continuerà a funzionare.",
                    nl = "Als je snelheidssensor defect raakt, blijft de motor werken.",
                    sv = "Om din hastighetssensor slutar fungera kommer motorn ändå att fungera.",
                    cs = "Pokud tvůj snímač rychlosti selže, motor bude přesto fungovat.",
                    sk = "Ak tvoj snímač rýchlosti zlyhá, motor bude napriek tomu fungovať.", da = "Hvis din hastighedssensor svigter, vil din motor stadig fungere.", ru = "Если датчик скорости выйдет из строя, мотор всё равно будет работать.",
                ),
            )
            HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
            ToggleRow(tr("Czujnik zmiany biegów", "Shift Sensor", de = "Schaltsensor", fr = "Capteur de dérailleur", es = "Sensor de cambio", pt = "Sensor de mudança", it = "Sensore cambio", nl = "Schakelsensor", sv = "Växelsensor", cs = "Snímač řazení", sk = "Snímač radenia", da = "Gearskiftesensor", ru = "Датчик переключения передач"), cfg.useShiftSensor, onUseShiftSensor, accent = Tokens.Blue)
            HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
            ToggleRow(
                tr("Tryb prowadzenia", "Walk Mode", de = "Schiebemodus", fr = "Mode marche", es = "Modo caminar", pt = "Modo caminhar", it = "Modalità camminata", nl = "Loopmodus", sv = "Gångläge", cs = "Režim chůze", sk = "Režim chôdze", da = "Gåtilstand", ru = "Режим ходьбы"), cfg.usePushWalk, onUsePushWalk, accent = Tokens.Blue,
                description = tr(
                    "Gdy wyłączony w konfiguracji, ale aktywowany komendą z wyświetlacza - kontynuuje z poprzednio wybranym poziomem wspomagania.",
                    "When walk mode is disabled in configuration but activated by command from the display the previously selected assist level will continue to be used.",
                    de = "Wenn der Schiebemodus in der Konfiguration deaktiviert, aber per Befehl vom Display " +
                        "aktiviert wird, wird weiterhin die zuletzt gewählte Unterstützungsstufe verwendet.",
                    fr = "Lorsque le mode marche est désactivé dans la configuration mais activé par une commande " +
                        "de l'écran, le niveau d'assistance précédemment sélectionné continue d'être utilisé.",
                    es = "Cuando el modo caminar está desactivado en la configuración pero se activa por comando " +
                        "desde la pantalla, se seguirá usando el nivel de asistencia seleccionado anteriormente.",
                    pt = "Quando o modo caminhar está desativado na configuração mas é ativado por comando do " +
                        "visor, continua a ser usado o nível de assistência selecionado anteriormente.",
                    it = "Quando la modalità camminata è disattivata nella configurazione ma viene attivata da " +
                        "un comando del display, continuerà a essere utilizzato il livello di assistenza " +
                        "selezionato in precedenza.",
                    nl = "Wanneer de loopmodus in de configuratie is uitgeschakeld maar via een commando van " +
                        "het display wordt geactiveerd, blijft het eerder geselecteerde ondersteuningsniveau in gebruik.",
                    sv = "När gångläget är inaktiverat i konfigurationen men aktiveras via ett kommando från " +
                        "displayen fortsätter den tidigare valda assistansnivån att användas.",
                    cs = "Když je režim chůze v konfiguraci vypnutý, ale je aktivován příkazem z " +
                        "displeje, i nadále se používá dříve zvolená úroveň asistence.",
                    sk = "Keď je režim chôdze v konfigurácii vypnutý, ale je aktivovaný príkazom z " +
                        "displeja, naďalej sa používa predtým zvolená úroveň asistencie.",
                    da = "Når gåtilstand er deaktiveret i konfigurationen, men aktiveres via en kommando fra " +
                        "displayet, fortsætter det tidligere valgte assistanceniveau med at blive brugt.",
                    ru = "Если режим ходьбы отключён в конфигурации, но активирован командой с дисплея, " +
                        "продолжает использоваться ранее выбранный уровень помощи.",
                ),
            )
        }
        ExpandableParamTile(
            label = tr("Czujnik temperatury", "Temperature Sensor", de = "Temperatursensor", fr = "Capteur de température", es = "Sensor de temperatura", pt = "Sensor de temperatura", it = "Sensore di temperatura", nl = "Temperatuursensor", sv = "Temperatursensor", cs = "Snímač teploty", sk = "Snímač teploty", da = "Temperatursensor", ru = "Датчик температуры"),
            valueLabel = listOf(
                tr("Wyłączony", "Disabled", de = "Deaktiviert", fr = "Désactivé", es = "Desactivado", pt = "Desativado", it = "Disattivato", nl = "Uitgeschakeld", sv = "Avstängd", cs = "Vypnuto", sk = "Vypnuté", da = "Deaktiveret", ru = "Отключено"),
                tr("Sterownika", "Controller", de = "Steuergerät", fr = "Contrôleur", es = "Controlador", pt = "Controlador", it = "Controller", nl = "Controller", sv = "Styrenhet", cs = "Řadič", sk = "Radič", da = "Controller", ru = "Контроллер"),
                tr("Silnika", "Motor", de = "Motor", fr = "Moteur", es = "Motor", pt = "Motor", it = "Motore", nl = "Motor", sv = "Motor", cs = "Motor", sk = "Motor", da = "Motor", ru = "Мотор"),
                "All",
            ).getOrElse(cfg.temperatureSensorMode) { "?" },
            description = tr(
                "Który czujnik temperatury jest używany do ograniczania mocy przy przegrzaniu. BBSHD ma dwa " +
                    "czujniki, BBS02 tylko jeden. Normalnie zostaw \"All\" - przydatne tylko gdy jeden z czujników jest uszkodzony.",
                "Select which temperature sensors to use for thermal limiting. The BBSHD has two temperature " +
                    "sensors, BBS02 only has a single. Normally leave as \"All\" - useful only if one sensor is broken.",
                de = "Welcher Temperatursensor zur thermischen Begrenzung verwendet wird. Der BBSHD hat zwei " +
                    "Sensoren, der BBS02 nur einen. Normalerweise auf „All“ belassen - nur nützlich, wenn einer " +
                    "der Sensoren defekt ist.",
                fr = "Quel capteur de température est utilisé pour la limitation thermique. Le BBSHD a deux " +
                    "capteurs, le BBS02 un seul. Laissez normalement sur « All » - utile uniquement si un " +
                    "capteur est défectueux.",
                es = "Qué sensor de temperatura se usa para la limitación térmica. El BBSHD tiene dos sensores, " +
                    "el BBS02 solo uno. Normalmente deja «All» - útil solo si uno de los sensores está averiado.",
                pt = "Seleciona quais sensores de temperatura usar para a limitação térmica. O BBSHD tem dois " +
                    "sensores de temperatura, o BBS02 tem apenas um. Normalmente deixa em «All» - útil apenas " +
                    "se um sensor estiver avariado.",
                it = "Seleziona quali sensori di temperatura utilizzare per la limitazione termica. Il BBSHD " +
                    "ha due sensori di temperatura, il BBS02 ne ha solo uno. Normalmente lascia su «All» - " +
                    "utile solo se un sensore è guasto.",
                nl = "Selecteert welke temperatuursensoren worden gebruikt voor thermische begrenzing. De " +
                    "BBSHD heeft twee temperatuursensoren, de BBS02 heeft er maar één. Laat normaal op «All» " +
                    "staan - alleen nuttig als één sensor defect is.",
                sv = "Väljer vilka temperatursensorer som används för termisk begränsning. " +
                    "BBSHD har två temperatursensorer, BBS02 har bara en. Lämna normalt på «All» " +
                    "- endast användbart om en sensor är trasig.",
                cs = "Vybírá, které teplotní senzory se používají pro tepelné omezení. " +
                    "BBSHD má dva teplotní senzory, BBS02 pouze jeden. Běžně ponechej na «All» " +
                    "- užitečné pouze pokud je jeden senzor poškozený.",
                sk = "Vyberá, ktoré teplotné senzory sa používajú na tepelné obmedzenie. " +
                    "BBSHD má dva teplotné senzory, BBS02 iba jeden. Bežne ponechaj na «All» " +
                    "- užitočné iba ak je jeden senzor poškodený.",
                da = "Vælger hvilke temperatursensorer der bruges til termisk begrænsning. " +
                    "BBSHD har to temperatursensorer, BBS02 har kun én. Lad normalt stå på «All» " +
                    "- kun nyttigt hvis en sensor er defekt.",
                ru = "Выбирает, какие датчики температуры используются для теплового ограничения. " +
                    "BBSHD имеет два датчика температуры, BBS02 - только один. Обычно оставляйте «All» " +
                    "- полезно только если один из датчиков неисправен.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onTemperatureSensorMode(cfg.temperatureSensorMode - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onTemperatureSensorMode(cfg.temperatureSensorMode + 1) }
            }
        }
        ExpandableParamTile(
            label = tr("Tryb świateł", "Lights Mode", de = "Lichtmodus", fr = "Mode des feux", es = "Modo de luces", pt = "Modo de luzes", it = "Modalità luci", nl = "Lichtmodus", sv = "Ljusläge", cs = "Režim světel", sk = "Režim svetiel", da = "Lystilstand", ru = "Режим освещения"),
            valueLabel = listOf(
                tr("Domyślny (z wyświetlacza)", "Default (display-controlled)", de = "Standard (vom Display gesteuert)", fr = "Par défaut (contrôlé par l'écran)", es = "Predeterminado (controlado por pantalla)", pt = "Predefinido (controlado pelo visor)", it = "Predefinito (controllato dal display)", nl = "Standaard (displaygestuurd)", sv = "Standard (styrs av display)", cs = "Výchozí (řízeno displejem)", sk = "Predvolené (riadené displejom)", da = "Standard (styret af display)", ru = "По умолчанию (управляется дисплеем)"),
                tr("Wyłączone", "Disabled", de = "Deaktiviert", fr = "Désactivé", es = "Desactivado", pt = "Desativado", it = "Disattivato", nl = "Uitgeschakeld", sv = "Avstängd", cs = "Vypnuto", sk = "Vypnuté", da = "Deaktiveret", ru = "Отключено"),
                tr("Zawsze włączone", "Always On", de = "Immer an", fr = "Toujours allumé", es = "Siempre encendido", pt = "Sempre ligado", it = "Sempre acceso", nl = "Altijd aan", sv = "Alltid på", cs = "Vždy zapnuto", sk = "Vždy zapnuté", da = "Altid tændt", ru = "Всегда включено"),
                tr("Światło stopu", "Brake Light", de = "Bremslicht", fr = "Feu stop", es = "Luz de freno", pt = "Luz de travagem", it = "Luce di stop", nl = "Remlicht", sv = "Bromsljus", cs = "Brzdové světlo", sk = "Brzdové svetlo", da = "Bremselys", ru = "Стоп-сигнал"),
            ).getOrElse(cfg.lightsMode) { "?" },
            description = tr(
                "Sterowanie wyjściem świateł zewnętrznych.",
                "Options for controlling external lights output.",
                de = "Optionen zur Steuerung des externen Lichtausgangs.",
                fr = "Options de contrôle de la sortie des feux externes.",
                es = "Opciones para controlar la salida de luces externas.",
                pt = "Opções para controlar a saída de luzes externas.",
                it = "Opzioni per controllare l'uscita delle luci esterne.",
                nl = "Opties voor het bedienen van de externe lichtuitgang.",
                sv = "Alternativ för att styra den externa ljusutgången.",
                cs = "Možnosti ovládání výstupu externích světel.",
                sk = "Možnosti ovládania výstupu externých svetiel.", da = "Indstillinger til styring af det eksterne lysudgang.", ru = "Параметры управления выходом внешнего освещения.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onLightsMode(cfg.lightsMode - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onLightsMode(cfg.lightsMode + 1) }
            }
        }
        // --- Speed Sensor ---
        MicroLabel(tr("Czujnik prędkości", "Speed Sensor", de = "Geschwindigkeitssensor", fr = "Capteur de vitesse", es = "Sensor de velocidad", pt = "Sensor de velocidade", it = "Sensore di velocità", nl = "Snelheidssensor", sv = "Hastighetssensor", cs = "Snímač rychlosti", sk = "Snímač rýchlosti", da = "Hastighedssensor", ru = "Датчик скорости"))
        ExpandableParamTile(
            label = tr("Rozmiar koła (cale)", "Wheel Size (inch)", de = "Radgröße (Zoll)", fr = "Taille de roue (pouces)", es = "Tamaño de rueda (pulgadas)", pt = "Tamanho da roda (polegadas)", it = "Dimensione ruota (pollici)", nl = "Wielgrootte (inch)", sv = "Hjulstorlek (tum)", cs = "Velikost kola (palce)", sk = "Veľkosť kolesa (palce)", da = "Hjulstørrelse (tommer)", ru = "Размер колеса (дюймы)"),
            valueLabel = "${cfg.wheelSizeInchX10 / 10.0}\"",
            description = tr(
                "Rozmiar koła (w calach) używany do przeliczeń prędkości.",
                "Wheel size (in inch) to use for speed calculations.",
                de = "Radgröße (in Zoll), die für Geschwindigkeitsberechnungen verwendet wird.",
                fr = "Taille de roue (en pouces) utilisée pour les calculs de vitesse.",
                es = "Tamaño de rueda (en pulgadas) usado para los cálculos de velocidad.",
                pt = "Tamanho da roda (em polegadas) a usar para os cálculos de velocidade.",
                it = "Dimensione della ruota (in pollici) da usare per i calcoli della velocità.",
                nl = "Wielgrootte (in inch) te gebruiken voor snelheidsberekeningen.",
                sv = "Hjulstorlek (i tum) att använda för hastighetsberäkningar.",
                cs = "Velikost kola (v palcích) použitá pro výpočty rychlosti.",
                sk = "Veľkosť kolesa (v palcoch) použitá na výpočty rýchlosti.", da = "Hjulstørrelse (i tommer), der bruges til hastighedsberegninger.", ru = "Размер колеса (в дюймах), используемый для расчёта скорости.",
            ),
        ) {
            FlankedSlider(value = cfg.wheelSizeInchX10, range = 100..400, accent = Tokens.Emerald, onValueChange = onWheelSizeX10)
        }
        ExpandableParamTile(
            label = tr("Sygnały (na obrót)", "Signals (per rotation)", de = "Signale (pro Umdrehung)", fr = "Signaux (par tour)", es = "Señales (por vuelta)", pt = "Sinais (por rotação)", it = "Segnali (per rotazione)", nl = "Signalen (per omwenteling)", sv = "Signaler (per varv)", cs = "Signály (na otáčku)", sk = "Signály (na otáčku)", da = "Signaler (pr. omdrejning)", ru = "Сигналы (на оборот)"),
            valueLabel = cfg.speedSensorSignals.toString(),
            description = tr(
                "Liczba sygnałów czujnika prędkości na jeden obrót koła.",
                "Number of speed sensor signals per wheel rotation.",
                de = "Anzahl der Geschwindigkeitssensor-Signale pro Radumdrehung.",
                fr = "Nombre de signaux du capteur de vitesse par tour de roue.",
                es = "Número de señales del sensor de velocidad por vuelta de rueda.",
                pt = "Número de sinais do sensor de velocidade por rotação da roda.",
                it = "Numero di segnali del sensore di velocità per rotazione della ruota.",
                nl = "Aantal snelheidssensor-signalen per wielomwenteling.",
                sv = "Antal hastighetssensorsignaler per hjulvarv.",
                cs = "Počet signálů snímače rychlosti na jednu otáčku kola.",
                sk = "Počet signálov snímača rýchlosti na jednu otáčku kolesa.", da = "Antal hastighedssensorsignaler pr. hjulomdrejning.", ru = "Количество сигналов датчика скорости на один оборот колеса.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onSpeedSensorSignals(cfg.speedSensorSignals - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onSpeedSensorSignals(cfg.speedSensorSignals + 1) }
            }
        }

        // --- Shift Sensor ---
        MicroLabel(tr("Czujnik zmiany biegów", "Shift Sensor", de = "Schaltsensor", fr = "Capteur de dérailleur", es = "Sensor de cambio", pt = "Sensor de mudança", it = "Sensore cambio", nl = "Schakelsensor", sv = "Växelsensor", cs = "Snímač řazení", sk = "Snímač radenia", da = "Gearskiftesensor", ru = "Датчик переключения передач"))
        ExpandableParamTile(
            label = tr("Czas przerwania przy zmianie biegu (ms)", "Shift Interrupt Duration (ms)", de = "Dauer der Schaltunterbrechung (ms)", fr = "Durée de coupure au changement (ms)", es = "Duración de interrupción al cambiar (ms)", pt = "Duração da interrupção na mudança (ms)", it = "Durata interruzione cambio (ms)", nl = "Duur schakelonderbreking (ms)", sv = "Varaktighet växlingsavbrott (ms)", cs = "Doba přerušení při řazení (ms)", sk = "Doba prerušenia pri radení (ms)", da = "Varighed af skifteafbrydelse (ms)", ru = "Длительность прерывания при переключении (мс)"),
            valueLabel = "${cfg.shiftInterruptDurationMs} ms",
            description = tr(
                "Czas trwania przerwania mocy po wykryciu zmiany biegu.",
                "Duration in milliseconds of the power interrupt that should occur.",
                de = "Dauer der Leistungsunterbrechung in Millisekunden, die bei einem Gangwechsel erfolgen soll.",
                fr = "Durée en millisecondes de la coupure de puissance qui doit se produire.",
                es = "Duración en milisegundos de la interrupción de potencia que debe producirse.",
                pt = "Duração em milissegundos da interrupção de potência que deve ocorrer.",
                it = "Durata in millisecondi dell'interruzione di potenza che deve verificarsi.",
                nl = "Duur in milliseconden van de vermogensonderbreking die moet plaatsvinden.",
                sv = "Varaktighet i millisekunder för effektavbrottet som ska ske.",
                cs = "Doba trvání přerušení výkonu v milisekundách, ke kterému má dojít.",
                sk = "Doba trvania prerušenia výkonu v milisekundách, ku ktorému má dôjsť.", da = "Varighed i millisekunder af den effektafbrydelse, der skal ske.", ru = "Длительность в миллисекундах прерывания мощности, которое должно произойти.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onShiftInterruptDurationMs(cfg.shiftInterruptDurationMs - 10) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onShiftInterruptDurationMs(cfg.shiftInterruptDurationMs + 10) }
            }
        }
        ExpandableParamTile(
            label = tr("Próg prądu przy zmianie biegu (%)", "Shift Current Threshold (%)", de = "Stromschwelle beim Schalten (%)", fr = "Seuil de courant au changement (%)", es = "Umbral de corriente al cambiar (%)", pt = "Limiar de corrente na mudança (%)", it = "Soglia di corrente al cambio (%)", nl = "Stroomdrempel bij schakelen (%)", sv = "Strömtröskel vid växling (%)", cs = "Práh proudu při řazení (%)", sk = "Prah prúdu pri radení (%)", da = "Strømtærskel ved gearskift (%)", ru = "Порог тока при переключении (%)"),
            valueLabel = "${cfg.shiftInterruptCurrentThresholdPercent}%",
            description = tr(
                "Maksymalny prąd silnika podczas zmiany biegu, w % Max Current (A) z sekcji Global.",
                "Maximum motor current during shifting expressed as a percentage of Max Current.",
                de = "Maximaler Motorstrom während des Schaltvorgangs, in % des Max Current (A) aus dem " +
                    "Abschnitt Global.",
                fr = "Courant moteur maximal pendant le changement de vitesse, en % du Max Current (A) de la " +
                    "section Global.",
                es = "Corriente máxima del motor durante el cambio de marcha, en % del Max Current (A) de la " +
                    "sección Global.",
                pt = "Corrente máxima do motor durante a mudança de velocidade, expressa em % do Max Current " +
                    "(A) da secção Global.",
                it = "Corrente massima del motore durante il cambio marcia, espressa in % del Max Current (A) " +
                    "della sezione Global.",
                nl = "Maximale motorstroom tijdens schakelen, uitgedrukt als percentage van Max Current (A) " +
                    "uit de sectie Global.",
                sv = "Maximal motorström under växling, uttryckt som procent av Max Current (A) " +
                    "från avsnittet Global.",
                cs = "Maximální proud motoru během řazení, vyjádřený jako procento Max Current (A) " +
                    "ze sekce Global.",
                sk = "Maximálny prúd motora počas radenia, vyjadrený ako percento Max Current (A) " +
                    "zo sekcie Global.",
                da = "Maksimal motorstrøm under gearskift, udtrykt som en procentdel af Max Current (A) " +
                    "fra afsnittet Global.",
                ru = "Максимальный ток мотора во время переключения передач, выраженный в процентах от " +
                    "Max Current (A) из раздела Global.",
            ),
        ) {
            FlankedSlider(value = cfg.shiftInterruptCurrentThresholdPercent, range = 0..100, accent = Tokens.Amber, onValueChange = onShiftInterruptCurrentThreshold)
        }

        // --- Miscellaneous ---
        MicroLabel(tr("Różne", "Miscellaneous", de = "Sonstiges", fr = "Divers", es = "Varios", pt = "Diversos", it = "Varie", nl = "Diversen", sv = "Övrigt", cs = "Různé", sk = "Rôzne", da = "Diverse", ru = "Разное"))
        ExpandableParamTile(
            label = tr("Dane na wyświetlaczu w trybie prowadzenia", "Walk Mode Data Display", de = "Datenanzeige im Schiebemodus", fr = "Affichage des données en mode marche", es = "Visualización de datos en modo caminar", pt = "Exibição de dados no modo caminhar", it = "Visualizzazione dati in modalità camminata", nl = "Gegevensweergave in loopmodus", sv = "Datavisning i gångläge", cs = "Zobrazení dat v režimu chůze", sk = "Zobrazenie dát v režime chôdze", da = "Datavisning i gåtilstand", ru = "Отображение данных в режиме ходьбы"),
            valueLabel = listOf(
                "Speed",
                tr("Temperatura (°C)", "Temperature (C)", de = "Temperatur (°C)", fr = "Température (°C)", es = "Temperatura (°C)", pt = "Temperatura (°C)", it = "Temperatura (°C)", nl = "Temperatuur (°C)", sv = "Temperatur (°C)", cs = "Teplota (°C)", sk = "Teplota (°C)", da = "Temperatur (°C)", ru = "Температура (°C)"),
                tr("Żądana moc (%)", "Requested Power (%)", de = "Angeforderte Leistung (%)", fr = "Puissance demandée (%)", es = "Potencia solicitada (%)", pt = "Potência solicitada (%)", it = "Potenza richiesta (%)", nl = "Gevraagd vermogen (%)", sv = "Begärd effekt (%)", cs = "Požadovaný výkon (%)", sk = "Požadovaný výkon (%)", da = "Ønsket effekt (%)", ru = "Запрошенная мощность (%)"),
                tr("Poziom baterii (%)", "Battery Level (%)", de = "Batteriestand (%)", fr = "Niveau de batterie (%)", es = "Nivel de batería (%)", pt = "Nível de bateria (%)", it = "Livello batteria (%)", nl = "Batterijniveau (%)", sv = "Batterinivå (%)", cs = "Úroveň baterie (%)", sk = "Úroveň batérie (%)", da = "Batteriniveau (%)", ru = "Уровень заряда батареи (%)"),
            ).getOrElse(cfg.walkModeDataDisplay) { "?" },
            description = tr(
                "Nadpisuje pole prędkości na wyświetlaczu inną daną, gdy aktywny jest tryb prowadzenia roweru.",
                "Override speed field display with Temperature, Requested Power, Battery Level or Speed while walk mode is active.",
                de = "Ersetzt das Geschwindigkeitsfeld auf dem Display durch Temperatur, angeforderte Leistung, " +
                    "Batteriestand oder Geschwindigkeit, solange der Schiebemodus aktiv ist.",
                fr = "Remplace le champ de vitesse sur l'écran par la température, la puissance demandée, le " +
                    "niveau de batterie ou la vitesse tant que le mode marche est actif.",
                es = "Sustituye el campo de velocidad en la pantalla por temperatura, potencia solicitada, " +
                    "nivel de batería o velocidad mientras el modo caminar está activo.",
                pt = "Substitui o campo de velocidade no visor por Temperatura, Potência Solicitada, Nível de " +
                    "Bateria ou Velocidade enquanto o modo caminhar está ativo.",
                it = "Sostituisce il campo velocità sul display con Temperatura, Potenza Richiesta, Livello " +
                    "Batteria o Velocità mentre la modalità camminata è attiva.",
                nl = "Vervangt het snelheidsveld op het display door Temperatuur, Gevraagd Vermogen, " +
                    "Batterijniveau of Snelheid terwijl de loopmodus actief is.",
                sv = "Ersätter hastighetsfältet på displayen med Temperatur, Begärd Effekt, " +
                    "Batterinivå eller Hastighet medan gångläget är aktivt.",
                cs = "Nahrazuje pole rychlosti na displeji hodnotou Teplota, Požadovaný výkon, " +
                    "Úroveň baterie nebo Rychlost, dokud je aktivní režim chůze.",
                sk = "Nahrádza pole rýchlosti na displeji hodnotou Teplota, Požadovaný výkon, " +
                    "Úroveň batérie alebo Rýchlosť, kým je aktívny režim chôdze.",
                da = "Erstatter hastighedsfeltet på displayet med Temperatur, Ønsket Effekt, " +
                    "Batteriniveau eller Hastighed, mens gåtilstand er aktiv.",
                ru = "Заменяет поле скорости на дисплее значением Температура, Запрошенная мощность, " +
                    "Уровень заряда батареи или Скорость, пока активен режим ходьбы.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onWalkModeDataDisplay(cfg.walkModeDataDisplay - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onWalkModeDataDisplay(cfg.walkModeDataDisplay + 1) }
            }
        }
        TokenCard(borderColor = Tokens.WhiteBorder) {
            ToggleRow(
                tr("Jednostki imperialne (mph)", "Freedom units (mph)", de = "Imperiale Einheiten (mph)", fr = "Unités impériales (mph)", es = "Unidades imperiales (mph)", pt = "Unidades imperiais (mph)", it = "Unità imperiali (mph)", nl = "Imperiale eenheden (mph)", sv = "Imperialistiska enheter (mph)", cs = "Imperiální jednotky (mph)", sk = "Imperiálne jednotky (mph)", da = "Imperiale enheder (mph)", ru = "Имперские единицы (mph)"),
                cfg.useFreedomUnits, onUseFreedomUnits, accent = Tokens.Blue,
                description = tr(
                    "Nie ma odpowiednika w zakładce System jego apki (tam to opcja globalna programu, Menu → Options) - " +
                        "dotyczy WYŁĄCZNIE wartości wysyłanych do wyświetlacza przez bbs-fw, nie jednostek w tej apce (patrz Ustawienia).",
                    "Has no equivalent in his app's System tab (there it's a global app option, Menu → Options) - " +
                        "affects ONLY the values bbs-fw sends to the display, not this app's own units (see Settings).",
                    de = "Hat keine Entsprechung im System-Tab der Originalanwendung (dort ist es eine globale " +
                        "Programmoption, Menu → Options) - betrifft AUSSCHLIESSLICH die von bbs-fw an das Display " +
                        "gesendeten Werte, nicht die eigenen Einheiten dieser App (siehe Einstellungen).",
                    fr = "N'a pas d'équivalent dans l'onglet System de l'application originale (là, c'est une " +
                        "option globale du programme, Menu → Options) - concerne UNIQUEMENT les valeurs envoyées " +
                        "à l'écran par bbs-fw, pas les unités propres à cette application (voir Réglages).",
                    es = "No tiene equivalente en la pestaña System de la app original (allí es una opción " +
                        "global del programa, Menu → Options) - afecta ÚNICAMENTE a los valores que bbs-fw envía " +
                        "a la pantalla, no a las unidades propias de esta app (ver Ajustes).",
                    pt = "Não tem equivalente no separador System da app original (lá é uma opção global do " +
                        "programa, Menu → Options) - afeta APENAS os valores que o bbs-fw envia para o visor, " +
                        "não as unidades desta app (ver Definições).",
                    it = "Non ha un equivalente nella scheda System dell'app originale (lì è un'opzione " +
                        "globale del programma, Menu → Options) - influisce SOLO sui valori che bbs-fw invia al " +
                        "display, non sulle unità di questa app (vedi Impostazioni).",
                    nl = "Heeft geen equivalent op het System-tabblad van de originele app (daar is het een " +
                        "globale programma-optie, Menu → Options) - heeft ALLEEN invloed op de waarden die " +
                        "bbs-fw naar het display stuurt, niet op de eenheden van deze app (zie Instellingen).",
                    sv = "Har ingen motsvarighet på fliken System i originalappen (där är det ett " +
                        "globalt programalternativ, Menu → Options) - påverkar ENDAST de värden som " +
                        "bbs-fw skickar till displayen, inte den här appens egna enheter (se Inställningar).",
                    cs = "Nemá ekvivalent na kartě System v originální aplikaci (tam je to globální " +
                        "možnost programu, Menu → Options) - ovlivňuje POUZE hodnoty, které bbs-fw " +
                        "posílá na displej, nikoli jednotky této aplikace (viz Nastavení).",
                    sk = "Nemá ekvivalent na karte System v originálnej aplikácii (tam je to globálna " +
                        "možnosť programu, Menu → Options) - ovplyvňuje IBA hodnoty, ktoré bbs-fw " +
                        "posiela na displej, nie jednotky tejto aplikácie (pozri Nastavenia).",
                    da = "Har ingen tilsvarende funktion i den originale apps System-fane (der er det en global " +
                        "programindstilling, Menu → Options) - påvirker KUN de værdier, som bbs-fw sender til " +
                        "displayet, ikke denne apps egne enheder (se Indstillinger).",
                    ru = "Не имеет аналога на вкладке System оригинального приложения (там это глобальная " +
                        "настройка программы, Menu → Options) - влияет ТОЛЬКО на значения, которые bbs-fw " +
                        "отправляет на дисплей, а не на единицы измерения этого приложения (см. Настройки).",
                ),
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

