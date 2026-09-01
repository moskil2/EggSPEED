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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bafspeed.app.FirmwareType
import com.bafspeed.app.UiState
import com.bafspeed.app.i18n.AppLanguage
import com.bafspeed.app.i18n.LocalAppLanguage
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.protocol.BbsFwController
import com.bafspeed.app.protocol.WHEEL_SIZE_LABELS
import com.bafspeed.app.protocol.designatedAssistLabel
import com.bafspeed.app.protocol.speedLimitLabel
import com.bafspeed.app.ui.components.MicroLabel
import com.bafspeed.app.ui.components.PreviewBanner
import com.bafspeed.app.ui.components.TokenCard
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens

/**
 * Zakladka "All In" - zwiezly, wylacznie do odczytu spis wszystkich aktualnie
 * ustawionych parametrow z Bafang Basic / Bafang Pedal (PAS) / Bafang Throttle
 * i Poziomow wspomagania, z przyciskiem kopiowania calosci do schowka. Zero
 * edycji - edycja dzieje sie na wlasciwych zakladkach.
 */
@Composable
fun ParametersScreen(state: UiState, onRefresh: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val lang = LocalAppLanguage.current
    val isBbsFw = state.firmwareType == FirmwareType.BBS_FW
    val hasData = if (isBbsFw) state.bbsFwVersion != null else state.general != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Tokens.Bg)
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = 16.dp)),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (!hasData) {
            PreviewBanner(tr(
                "Brak połączenia - poniżej pokazane są wartości domyślne aplikacji, niekoniecznie zgodne z tym, co faktycznie ma zapisane Twój sterownik. Połącz się na ekranie Połączenie, żeby zobaczyć realne dane.",
                "No connection - the values below are the app's defaults, not necessarily what your controller actually has saved. Connect on the Connect screen to see the real data.",
                de = "Keine Verbindung - die unten gezeigten Werte sind die Standardwerte der App, nicht " +
                    "unbedingt das, was dein Steuergerät tatsächlich gespeichert hat. Verbinde dich auf dem " +
                    "Bildschirm Verbindung, um die echten Daten zu sehen.",
                fr = "Pas de connexion - les valeurs ci-dessous sont celles par défaut de l'application, pas " +
                    "nécessairement ce que votre contrôleur a réellement enregistré. Connectez-vous sur l'écran " +
                    "Connexion pour voir les données réelles.",
                es = "Sin conexión - los valores de abajo son los predeterminados de la app, no necesariamente " +
                    "lo que tu controlador tiene realmente guardado. Conéctate en la pantalla Conexión para ver " +
                    "los datos reales.",
                pt = "Sem ligação - os valores abaixo são os predefinidos da app, não necessariamente o que o " +
                    "teu controlador tem realmente guardado. Liga-te no ecrã Ligação para ver os dados reais.",
                it = "Nessuna connessione - i valori sottostanti sono quelli predefiniti dell'app, non " +
                    "necessariamente ciò che il tuo controller ha effettivamente salvato. Connettiti nella " +
                    "schermata Connessione per vedere i dati reali.",
                nl = "Geen verbinding - de onderstaande waarden zijn de standaardwaarden van de app, niet " +
                    "noodzakelijk wat je controller daadwerkelijk heeft opgeslagen. Verbind op het scherm " +
                    "Verbinding om de echte gegevens te zien.",
                sv = "Ingen anslutning - värdena nedan är appens standardvärden, inte nödvändigtvis " +
                    "vad din styrenhet faktiskt har sparat. Anslut på skärmen Anslutning för att se " +
                    "de verkliga uppgifterna.",
                cs = "Bez připojení - hodnoty níže jsou výchozí hodnoty aplikace, nemusí nutně " +
                    "odpovídat tomu, co má tvůj řadič skutečně uložené. Připoj se na obrazovce Připojení, " +
                    "abys viděl reálná data.",
                sk = "Bez pripojenia - hodnoty nižšie sú predvolené hodnoty aplikácie, nemusia nutne " +
                    "zodpovedať tomu, čo má tvoj radič skutočne uložené. Pripoj sa na obrazovke Pripojenie, " +
                    "aby si videl reálne dáta.",
                da = "Ingen forbindelse - værdierne nedenfor er appens standardværdier, ikke nødvendigvis " +
                    "det, som din controller faktisk har gemt. Opret forbindelse på skærmen Forbindelse, " +
                    "så du kan se de rigtige data.",
                ru = "Нет подключения - значения ниже являются настройками приложения по умолчанию, не " +
                    "обязательно совпадающими с тем, что реально сохранено в твоём контроллере. Подключись " +
                    "на экране Подключение, чтобы увидеть реальные данные.",
            ))
        } else {
            PreviewBanner(tr(
                "Wyłącznie podgląd do odczytu - to spis aktualnie ustawionych parametrów. Edycja odbywa się na właściwych zakładkach.",
                "Read-only preview only - this is a list of currently set parameters. Editing happens in the actual tabs.",
                de = "Nur ein schreibgeschützter Überblick - dies ist eine Liste der aktuell eingestellten " +
                    "Parameter. Die Bearbeitung erfolgt in den jeweiligen Tabs.",
                fr = "Aperçu en lecture seule uniquement - il s'agit d'une liste des paramètres actuellement " +
                    "définis. La modification se fait dans les onglets concernés.",
                es = "Solo vista previa de lectura - es una lista de los parámetros actualmente configurados. " +
                    "La edición se realiza en las pestañas correspondientes.",
                pt = "Apenas pré-visualização de leitura - esta é uma lista dos parâmetros atualmente " +
                    "definidos. A edição ocorre nas pestanas reais.",
                it = "Solo anteprima di sola lettura - questo è un elenco dei parametri attualmente impostati. " +
                    "La modifica avviene nelle schede effettive.",
                nl = "Alleen leesbare voorbeeldweergave - dit is een lijst van de momenteel ingestelde " +
                    "parameters. Bewerken gebeurt op de daadwerkelijke tabbladen.",
                sv = "Endast en skrivskyddad förhandsvisning - detta är en lista över de för närvarande inställda " +
                    "parametrarna. Redigering sker på de faktiska flikarna.",
                cs = "Pouze náhled pro čtení - jde o seznam aktuálně nastavených " +
                    "parametrů. Úpravy probíhají na skutečných kartách.",
                sk = "Iba náhľad na čítanie - ide o zoznam aktuálne nastavených " +
                    "parametrov. Úpravy prebiehajú na skutočných kartách.",
                da = "Kun skrivebeskyttet forhåndsvisning - dette er en liste over de aktuelt indstillede " +
                    "parametre. Redigering sker på de faktiske faneblade.",
                ru = "Только просмотр для чтения - это список текущих установленных " +
                    "параметров. Редактирование происходит на соответствующих вкладках.",
            ))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Tokens.Card, RoundedCornerShape(15.dp))
                .border(1.dp, Tokens.WhiteBorder, RoundedCornerShape(15.dp))
                .clickable {
                    val text = if (isBbsFw) buildBbsFwDiagnosticsText(state, lang) else buildDiagnosticsText(state, lang)
                    clipboard.setText(AnnotatedString(text))
                    Toast.makeText(
                        context,
                        tr(lang, "Skopiowano do schowka", "Copied to clipboard", de = "In die Zwischenablage kopiert", fr = "Copié dans le presse-papiers", es = "Copiado al portapapeles", pt = "Copiado para a área de transferência", it = "Copiato negli appunti", nl = "Gekopieerd naar klembord", sv = "Kopierat till urklipp", cs = "Zkopírováno do schránky", sk = "Skopírované do schránky", da = "Kopieret til udklipsholder", ru = "Скопировано в буфер обмена"),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
                .padding(vertical = 15.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(tr("Kopiuj wszystko", "Copy all", de = "Alles kopieren", fr = "Tout copier", es = "Copiar todo", pt = "Copiar tudo", it = "Copia tutto", nl = "Alles kopiëren", sv = "Kopiera allt", cs = "Kopírovat vše", sk = "Kopírovať všetko", da = "Kopiér alt", ru = "Копировать всё"), fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Tokens.Blue)
        }

        if (isBbsFw) {
            BbsFwParametersContent(state)
        } else {
            state.general?.let { gen ->
                Column {
                    MicroLabel("Bafang Motor Type")
                    Spacer(Modifier.height(6.dp))
                    TokenCard(borderColor = Tokens.WhiteBorder) {
                        ParamRow(tr("Producent", "Manufacturer", de = "Hersteller", fr = "Fabricant", es = "Fabricante", pt = "Fabricante", it = "Produttore", nl = "Fabrikant", sv = "Tillverkare", cs = "Výrobce", sk = "Výrobca", da = "Producent", ru = "Производитель"), gen.manufacturer)
                        ParamRow("Model", gen.model)
                        ParamRow(tr("Wersja sprzętu", "Hardware version", de = "Hardware-Version", fr = "Version matérielle", es = "Versión de hardware", pt = "Versão de hardware", it = "Versione hardware", nl = "Hardwareversie", sv = "Hårdvaruversion", cs = "Verze hardwaru", sk = "Verzia hardvéru", da = "Hardwareversion", ru = "Версия оборудования"), gen.hardwareVersion)
                        ParamRow("Firmware", gen.firmwareVersion)
                        ParamRow(tr("Napięcie nominalne", "Nominal voltage", de = "Nennspannung", fr = "Tension nominale", es = "Voltaje nominal", pt = "Tensão nominal", it = "Tensione nominale", nl = "Nominale spanning", sv = "Nominell spänning", cs = "Jmenovité napětí", sk = "Menovité napätie", da = "Nominel spænding", ru = "Номинальное напряжение"), "${gen.nominalVoltage} V")
                        ParamRow(tr("Prąd maksymalny", "Max current", de = "Maximalstrom", fr = "Courant maximal", es = "Corriente máxima", pt = "Corrente máxima", it = "Corrente massima", nl = "Maximale stroom", sv = "Maxström", cs = "Maximální proud", sk = "Maximálny prúd", da = "Maksimal strøm", ru = "Максимальный ток"), "${gen.maxCurrentA} A", last = true)
                    }
                }
            }

            val bas = state.basicOrDefault
            Column {
                MicroLabel("Bafang Basic")
                Spacer(Modifier.height(6.dp))
                TokenCard(borderColor = Tokens.WhiteBorder) {
                    ParamRow(tr("Ochrona baterii (LBP)", "Battery protection (LBP)", de = "Batterieschutz (LBP)", fr = "Protection batterie (LBP)", es = "Protección de batería (LBP)", pt = "Proteção de bateria (LBP)", it = "Protezione batteria (LBP)", nl = "Batterijbescherming (LBP)", sv = "Batteriskydd (LBP)", cs = "Ochrana baterie (LBP)", sk = "Ochrana batérie (LBP)", da = "Batteribeskyttelse (LBP)", ru = "Защита батареи (LBP)"), "${bas.lowBatteryProtection} V")
                    ParamRow(tr("Limit prądu (LC)", "Current limit (LC)", de = "Strombegrenzung (LC)", fr = "Limite de courant (LC)", es = "Límite de corriente (LC)", pt = "Limite de corrente (LC)", it = "Limite di corrente (LC)", nl = "Stroomlimiet (LC)", sv = "Strömgräns (LC)", cs = "Omezení proudu (LC)", sk = "Obmedzenie prúdu (LC)", da = "Strømgrænse (LC)", ru = "Ограничение тока (LC)"), "${bas.currentLimit} A")
                    ParamRow(tr("Koło", "Wheel", de = "Rad", fr = "Roue", es = "Rueda", pt = "Roda", it = "Ruota", nl = "Wiel", sv = "Hjul", cs = "Kolo", sk = "Koleso", da = "Hjul", ru = "Колесо"), WHEEL_SIZE_LABELS.getOrElse(bas.wheelDiameterCode) { "${tr(lang, "kod", "code", de = "Code", fr = "code", es = "código", pt = "código", it = "codice", nl = "code", sv = "kod", cs = "kód", sk = "kód", da = "kode", ru = "код")} ${bas.wheelDiameterCode}" })
                    ParamRow(tr("Czujnik prędkości (SMM)", "Speed meter (SMM)", de = "Geschwindigkeitssensor (SMM)", fr = "Capteur de vitesse (SMM)", es = "Sensor de velocidad (SMM)", pt = "Sensor de velocidade (SMM)", it = "Sensore di velocità (SMM)", nl = "Snelheidssensor (SMM)", sv = "Hastighetssensor (SMM)", cs = "Snímač rychlosti (SMM)", sk = "Snímač rýchlosti (SMM)", da = "Hastighedsmåler (SMM)", ru = "Датчик скорости (SMM)"), bas.speedMeterModel.toString())
                    ParamRow(tr("Sygnały czujnika (SMS)", "Meter signals (SMS)", de = "Sensorsignale (SMS)", fr = "Signaux du capteur (SMS)", es = "Señales del sensor (SMS)", pt = "Sinais do sensor (SMS)", it = "Segnali del sensore (SMS)", nl = "Sensorsignalen (SMS)", sv = "Sensorsignaler (SMS)", cs = "Signály snímače (SMS)", sk = "Signály snímača (SMS)", da = "Målersignaler (SMS)", ru = "Сигналы датчика (SMS)"), bas.speedMeterSignals.toString(), last = true)
                }
                Spacer(Modifier.height(6.dp))
                MicroLabel(tr("Poziomy wspomagania", "Assist levels", de = "Unterstützungsstufen", fr = "Niveaux d'assistance", es = "Niveles de asistencia", pt = "Níveis de assistência", it = "Livelli di assistenza", nl = "Ondersteuningsniveaus", sv = "Assistansnivåer", cs = "Úrovně asistence", sk = "Úrovne asistencie", da = "Understøttelsesniveauer", ru = "Уровни поддержки"))
                Spacer(Modifier.height(6.dp))
                TokenCard(borderColor = Tokens.WhiteBorder) {
                    bas.assistCurrentPct.forEachIndexed { i, alc ->
                        ParamRow(
                            tr("Poziom $i", "Level $i", de = "Stufe $i", fr = "Niveau $i", es = "Nivel $i", pt = "Nível $i", it = "Livello $i", nl = "Niveau $i", sv = "Nivå $i", cs = "Úroveň $i", sk = "Úroveň $i", da = "Niveau $i", ru = "Уровень $i"),
                            "$alc% / ${bas.assistSpeedPct.getOrElse(i) { 0 }}%",
                            last = i == 9,
                        )
                    }
                }
            }

            val pas = state.pasOrDefault
            Column {
                MicroLabel("Bafang Pedal (PAS)")
                Spacer(Modifier.height(6.dp))
                TokenCard(borderColor = Tokens.WhiteBorder) {
                    ParamRow(tr("Typ czujnika (PT)", "Sensor type (PT)", de = "Sensortyp (PT)", fr = "Type de capteur (PT)", es = "Tipo de sensor (PT)", pt = "Tipo de sensor (PT)", it = "Tipo di sensore (PT)", nl = "Sensortype (PT)", sv = "Sensortyp (PT)", cs = "Typ snímače (PT)", sk = "Typ snímača (PT)", da = "Sensortype (PT)", ru = "Тип датчика (PT)"), pas.pedalType.toString())
                    ParamRow("Designated Assist (DA)", designatedAssistLabel(pas.designatedAssist))
                    ParamRow(tr("Limit prędkości (SL)", "Speed limit (SL)", de = "Geschwindigkeitslimit (SL)", fr = "Limite de vitesse (SL)", es = "Límite de velocidad (SL)", pt = "Limite de velocidade (SL)", it = "Limite di velocità (SL)", nl = "Snelheidslimiet (SL)", sv = "Hastighetsgräns (SL)", cs = "Omezení rychlosti (SL)", sk = "Obmedzenie rýchlosti (SL)", da = "Hastighedsgrænse (SL)", ru = "Ограничение скорости (SL)"), speedLimitLabel(pas.speedLimit))
                    ParamRow(tr("Prąd startowy (SC)", "Start current (SC)", de = "Startstrom (SC)", fr = "Courant de démarrage (SC)", es = "Corriente de arranque (SC)", pt = "Corrente de arranque (SC)", it = "Corrente di avvio (SC)", nl = "Startstroom (SC)", sv = "Startström (SC)", cs = "Počáteční proud (SC)", sk = "Počiatočný prúd (SC)", da = "Startstrøm (SC)", ru = "Начальный ток (SC)"), "${pas.startCurrentPct}%")
                    ParamRow("Slow-start (SSM)", pas.slowStartMode.toString())
                    ParamRow("Start degree (SDN)", pas.startDegree.toString())
                    ParamRow(
                        tr("Tryb pracy (WM)", "Work mode (WM)", de = "Arbeitsmodus (WM)", fr = "Mode de fonctionnement (WM)", es = "Modo de trabajo (WM)", pt = "Modo de funcionamento (WM)", it = "Modalità di lavoro (WM)", nl = "Werkmodus (WM)", sv = "Arbetsläge (WM)", cs = "Pracovní režim (WM)", sk = "Pracovný režim (WM)", da = "Arbejdstilstand (WM)", ru = "Рабочий режим (WM)"),
                        if (pas.workMode == 0) {
                            tr("nieokreślony", "undetermined", de = "unbestimmt", fr = "indéterminé", es = "indeterminado", pt = "indeterminado", it = "indeterminato", nl = "onbepaald", sv = "obestämd", cs = "neurčeno", sk = "neurčené", da = "ubestemt", ru = "не определено")
                        } else {
                            pas.workMode.toString()
                        },
                    )
                    ParamRow("Time of stop (TS)", "${pas.timeOfStop} ×10 ms")
                    ParamRow("Current decay (CD)", pas.currentDecay.toString())
                    ParamRow("Stop decay (SD)", "${pas.stopDecay} ×10 ms")
                    ParamRow("Keep current (KC)", "${pas.keepCurrentPct}%", last = true)
                }
            }

            val thr = state.thrOrDefault
            Column {
                MicroLabel("Bafang Throttle")
                Spacer(Modifier.height(6.dp))
                TokenCard(borderColor = Tokens.WhiteBorder) {
                    ParamRow(tr("Napięcie start (SV)", "Start voltage (SV)", de = "Startspannung (SV)", fr = "Tension de démarrage (SV)", es = "Voltaje de arranque (SV)", pt = "Tensão de arranque (SV)", it = "Tensione di avvio (SV)", nl = "Startspanning (SV)", sv = "Startspänning (SV)", cs = "Počáteční napětí (SV)", sk = "Počiatočné napätie (SV)", da = "Startspænding (SV)", ru = "Начальное напряжение (SV)"), "${thr.startVoltage / 10.0} V")
                    ParamRow(tr("Napięcie końcowe (EV)", "End voltage (EV)", de = "Endspannung (EV)", fr = "Tension finale (EV)", es = "Voltaje final (EV)", pt = "Tensão final (EV)", it = "Tensione finale (EV)", nl = "Eindspanning (EV)", sv = "Slutspänning (EV)", cs = "Konečné napětí (EV)", sk = "Konečné napätie (EV)", da = "Slutspænding (EV)", ru = "Конечное напряжение (EV)"), "${thr.endVoltage / 10.0} V")
                    ParamRow(
                        tr("Tryb", "Mode", de = "Modus", fr = "Mode", es = "Modo", pt = "Modo", it = "Modalità", nl = "Modus", sv = "Läge", cs = "Režim", sk = "Režim", da = "Tilstand", ru = "Режим"),
                        tr(
                            if (thr.mode == 0) "prędkość" else "prąd",
                            if (thr.mode == 0) "speed" else "current",
                            de = if (thr.mode == 0) "Geschwindigkeit" else "Strom",
                            fr = if (thr.mode == 0) "vitesse" else "courant",
                            es = if (thr.mode == 0) "velocidad" else "corriente",
                            pt = if (thr.mode == 0) "velocidade" else "corrente",
                            it = if (thr.mode == 0) "velocità" else "corrente",
                            nl = if (thr.mode == 0) "snelheid" else "stroom",
                            sv = if (thr.mode == 0) "hastighet" else "ström",
                            cs = if (thr.mode == 0) "rychlost" else "proud",
                            sk = if (thr.mode == 0) "rýchlosť" else "prúd",
                            da = if (thr.mode == 0) "hastighed" else "strøm",
                            ru = if (thr.mode == 0) "скорость" else "ток",
                        ),
                    )
                    ParamRow("Designated Assist (DA)", designatedAssistLabel(thr.designatedAssist))
                    ParamRow(tr("Limit prędkości (SL)", "Speed limit (SL)", de = "Geschwindigkeitslimit (SL)", fr = "Limite de vitesse (SL)", es = "Límite de velocidad (SL)", pt = "Limite de velocidade (SL)", it = "Limite di velocità (SL)", nl = "Snelheidslimiet (SL)", sv = "Hastighetsgräns (SL)", cs = "Omezení rychlosti (SL)", sk = "Obmedzenie rýchlosti (SL)", da = "Hastighedsgrænse (SL)", ru = "Ограничение скорости (SL)"), speedLimitLabel(thr.speedLimit))
                    ParamRow(tr("Prąd startowy (SC)", "Start current (SC)", de = "Startstrom (SC)", fr = "Courant de démarrage (SC)", es = "Corriente de arranque (SC)", pt = "Corrente de arranque (SC)", it = "Corrente di avvio (SC)", nl = "Startstroom (SC)", sv = "Startström (SC)", cs = "Počáteční proud (SC)", sk = "Počiatočný prúd (SC)", da = "Startstrøm (SC)", ru = "Начальный ток (SC)"), "${thr.startCurrentPct}%", last = true)
                }
            }
        }

        if (hasData) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Tokens.Card, RoundedCornerShape(15.dp))
                    .border(1.dp, Tokens.WhiteBorder, RoundedCornerShape(15.dp))
                    .clickable { onRefresh() }
                    .padding(vertical = 15.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(tr("Odczytaj ponownie", "Read again", de = "Erneut lesen", fr = "Relire", es = "Leer de nuevo", pt = "Ler novamente", it = "Rileggi", nl = "Opnieuw lezen", sv = "Läs igen", cs = "Číst znovu", sk = "Čítať znova", da = "Læs igen", ru = "Прочитать снова"), fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Tokens.TextPrimary)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

/**
 * Odpowiednik OEM sekcji powyżej dla bbs-fw - te same kategorie i kolejność co ekran "System"
 * ([BbsFwSystemScreen]: Global/Throttle/Pedal Assist/Features/Speed Sensor/Shift Sensor/Miscellaneous),
 * plus wersja firmware/config i Poziomy wspomagania (2 profile × 10 poziomów).
 */
@Composable
private fun BbsFwParametersContent(state: UiState) {
    val cfg = state.bbsFwConfigOrDefault
    state.bbsFwVersion?.let { v ->
        Column {
            MicroLabel("bbs-fw")
            Spacer(Modifier.height(6.dp))
            TokenCard(borderColor = Tokens.WhiteBorder) {
                ParamRow(tr("Wersja firmware", "Firmware version", de = "Firmware-Version", fr = "Version du firmware", es = "Versión de firmware", pt = "Versão de firmware", it = "Versione firmware", nl = "Firmwareversie", sv = "Firmwareversion", cs = "Verze firmwaru", sk = "Verzia firmvéru", da = "Firmwareversion", ru = "Версия прошивки"), v.versionLabel)
                ParamRow(tr("Wersja formatu konfiguracji", "Config format version", de = "Konfigurationsformat-Version", fr = "Version du format de configuration", es = "Versión del formato de configuración", pt = "Versão do formato de configuração", it = "Versione formato configurazione", nl = "Configuratieformaat-versie", sv = "Konfigurationsformatversion", cs = "Verze formátu konfigurace", sk = "Verzia formátu konfigurácie", da = "Konfigurationsformatversion", ru = "Версия формата конфигурации"), v.configVersion.toString())
                ParamRow(tr("Typ sterownika", "Controller type", de = "Steuergerätetyp", fr = "Type de contrôleur", es = "Tipo de controlador", pt = "Tipo de controlador", it = "Tipo di controller", nl = "Controllertype", sv = "Kontrollertyp", cs = "Typ řadiče", sk = "Typ radiča", da = "Controllertype", ru = "Тип контроллера"), "${BbsFwController.name(v.ctrlType)} (${v.ctrlType})", last = true)
            }
        }
    }

    Column {
        MicroLabel(tr("Globalne", "Global", de = "Global", fr = "Global", es = "Global", pt = "Global", it = "Globale", nl = "Algemeen", sv = "Globalt", cs = "Globální", sk = "Globálne", da = "Globalt", ru = "Глобальные"))
        Spacer(Modifier.height(6.dp))
        TokenCard(borderColor = Tokens.WhiteBorder) {
            ParamRow(tr("Maks. prąd", "Max Current", de = "Max. Strom", fr = "Courant max", es = "Corriente máx.", pt = "Corrente máx.", it = "Corrente max", nl = "Max. stroom", sv = "Max ström", cs = "Max. proud", sk = "Max. prúd", da = "Maks. strøm", ru = "Макс. ток"), "${cfg.maxCurrentAmps} A")
            ParamRow(tr("Narastanie prądu", "Current Ramp", de = "Stromanstieg", fr = "Rampe de courant", es = "Rampa de corriente", pt = "Rampa de corrente", it = "Rampa di corrente", nl = "Stroomoploop", sv = "Strömramp", cs = "Náběh proudu", sk = "Nábeh prúdu", da = "Strømrampe", ru = "Нарастание тока"), "${cfg.currentRampAmpsS} A/s")
            ParamRow(tr("Maks. napięcie baterii", "Max Battery Voltage", de = "Max. Batteriespannung", fr = "Tension batterie max", es = "Voltaje máx. de batería", pt = "Voltagem máx. da bateria", it = "Tensione max batteria", nl = "Max. batterijspanning", sv = "Max batterispänning", cs = "Max. napětí baterie", sk = "Max. napätie batérie", da = "Maks. batterispænding", ru = "Макс. напряжение батареи"), "${cfg.maxBatteryX100v / 100.0} V")
            ParamRow(tr("Odcięcie niskiego napięcia", "Low Voltage Cutoff", de = "Unterspannungsabschaltung", fr = "Coupure basse tension", es = "Corte de bajo voltaje", pt = "Corte de baixa tensão", it = "Taglio bassa tensione", nl = "Onderspanningsafsluiting", sv = "Underspänningsavstängning", cs = "Vypnutí při nízkém napětí", sk = "Vypnutie pri nízkom napätí", da = "Underspændingsafbrydelse", ru = "Отключение при низком напряжении"), "${cfg.lowCutOffV} V")
            ParamRow(tr("Maks. prędkość", "Max Speed", de = "Max. Geschwindigkeit", fr = "Vitesse max", es = "Velocidad máx.", pt = "Velocidade máx.", it = "Velocità max", nl = "Max. snelheid", sv = "Max hastighet", cs = "Max. rychlost", sk = "Max. rýchlosť", da = "Maks. hastighed", ru = "Макс. скорость"), "${cfg.maxSpeedKph} km/h", last = true)
        }
    }

    Column {
        MicroLabel(tr("Manetka", "Throttle", de = "Gasgriff", fr = "Accélérateur", es = "Acelerador", pt = "Acelerador", it = "Acceleratore", nl = "Gasgreep", sv = "Gasreglage", cs = "Plynová páčka", sk = "Plynová páčka", da = "Gashåndtag", ru = "Газ"))
        Spacer(Modifier.height(6.dp))
        TokenCard(borderColor = Tokens.WhiteBorder) {
            ParamRow(tr("Napięcie startowe", "Start Voltage", de = "Startspannung", fr = "Tension de démarrage", es = "Voltaje de arranque", pt = "Tensão de arranque", it = "Tensione di avvio", nl = "Startspanning", sv = "Startspänning", cs = "Počáteční napětí", sk = "Počiatočné napätie", da = "Startspænding", ru = "Начальное напряжение"), "${cfg.throttleStartVoltageMv} mV")
            ParamRow(tr("Napięcie końcowe", "End Voltage", de = "Endspannung", fr = "Tension finale", es = "Voltaje final", pt = "Tensão final", it = "Tensione finale", nl = "Eindspanning", sv = "Slutspänning", cs = "Konečné napětí", sk = "Konečné napätie", da = "Slutspænding", ru = "Конечное напряжение"), "${cfg.throttleEndVoltageMv} mV")
            ParamRow(tr("Prąd startowy", "Start Current", de = "Startstrom", fr = "Courant de démarrage", es = "Corriente de arranque", pt = "Corrente de arranque", it = "Corrente di avvio", nl = "Startstroom", sv = "Startström", cs = "Počáteční proud", sk = "Počiatočný prúd", da = "Startstrøm", ru = "Начальный ток"), "${cfg.throttleStartPercent}%")
            ParamRow(tr("Opcje globalnego limitu prędkości", "Global Speed Limit Options", de = "Optionen für globales Geschwindigkeitslimit", fr = "Options de limite de vitesse globale", es = "Opciones de límite de velocidad global", pt = "Opções de limite de velocidade global", it = "Opzioni limite di velocità globale", nl = "Opties globale snelheidslimiet", sv = "Alternativ för global hastighetsgräns", cs = "Možnosti globálního omezení rychlosti", sk = "Možnosti globálneho obmedzenia rýchlosti", da = "Indstillinger for global hastighedsgrænse", ru = "Параметры глобального ограничения скорости"), THROTTLE_SPD_LIM_OPT_LABELS.getOrElse(cfg.throttleGlobalSpdLimOpt) { "?" })
            ParamRow(tr("Globalny limit prędkości", "Global Speed Limit", de = "Globales Geschwindigkeitslimit", fr = "Limite de vitesse globale", es = "Límite de velocidad global", pt = "Limite de velocidade global", it = "Limite di velocità globale", nl = "Globale snelheidslimiet", sv = "Global hastighetsgräns", cs = "Globální omezení rychlosti", sk = "Globálne obmedzenie rýchlosti", da = "Global hastighedsgrænse", ru = "Глобальное ограничение скорости"), "${cfg.throttleGlobalSpdLimPercent}%", last = true)
        }
    }

    Column {
        MicroLabel(tr("Wspomaganie pedałowania", "Pedal Assist", de = "Pedal Assist", fr = "Assistance au pédalage", es = "Asistencia de pedaleo", pt = "Assistência de pedalada", it = "Assistenza pedalata", nl = "Trapondersteuning", sv = "Pedalassistans", cs = "Asistence šlapání", sk = "Asistencia šliapania", da = "Pedalassistance", ru = "Педальная поддержка"))
        Spacer(Modifier.height(6.dp))
        TokenCard(borderColor = Tokens.WhiteBorder) {
            ParamRow(tr("Opóźnienie startu", "Start Delay", de = "Startverzögerung", fr = "Délai de démarrage", es = "Retardo de arranque", pt = "Atraso de arranque", it = "Ritardo di avvio", nl = "Startvertraging", sv = "Startfördröjning", cs = "Prodleva startu", sk = "Oneskorenie štartu", da = "Startforsinkelse", ru = "Задержка старта"), "${cfg.pasStartDelayPulses * 15}°")
            ParamRow(tr("Opóźnienie zatrzymania", "Stop Delay", de = "Stoppverzögerung", fr = "Délai d'arrêt", es = "Retardo de parada", pt = "Atraso de paragem", it = "Ritardo di arresto", nl = "Stopvertraging", sv = "Stoppfördröjning", cs = "Prodleva vypnutí", sk = "Oneskorenie vypnutia", da = "Stopforsinkelse", ru = "Задержка остановки"), "${cfg.pasStopDelayX100s * 10} ms")
            ParamRow(tr("Podtrzymanie prądu", "Keep Current", de = "Stromhaltung", fr = "Maintien du courant", es = "Mantenimiento de corriente", pt = "Manutenção de corrente", it = "Mantenimento corrente", nl = "Stroom vasthouden", sv = "Bibehållen ström", cs = "Udržení proudu", sk = "Udržanie prúdu", da = "Bibehold strøm", ru = "Удержание тока"), "${cfg.pasKeepCurrentPercent}%")
            ParamRow(tr("Kadencja podtrzymania prądu", "Keep Current Cadence", de = "Kadenz für Stromhaltung", fr = "Cadence de maintien du courant", es = "Cadencia de mantenimiento de corriente", pt = "Cadência de manutenção de corrente", it = "Cadenza di mantenimento corrente", nl = "Cadans voor stroom vasthouden", sv = "Kadens för bibehållen ström", cs = "Kadence pro udržení proudu", sk = "Kadencia pre udržanie prúdu", da = "Kadence for strømbibeholdelse", ru = "Каденс удержания тока"), "${cfg.pasKeepCurrentCadenceRpm} rpm", last = true)
        }
    }

    Column {
        MicroLabel(tr("Funkcje", "Features", de = "Funktionen", fr = "Fonctionnalités", es = "Funciones", pt = "Funcionalidades", it = "Funzionalità", nl = "Functies", sv = "Funktioner", cs = "Funkce", sk = "Funkcie", da = "Funktioner", ru = "Функции"))
        Spacer(Modifier.height(6.dp))
        TokenCard(borderColor = Tokens.WhiteBorder) {
            ParamRow(
                tr("Czujnik prędkości", "Speed Sensor", de = "Geschwindigkeitssensor", fr = "Capteur de vitesse", es = "Sensor de velocidad", pt = "Sensor de velocidade", it = "Sensore di velocità", nl = "Snelheidssensor", sv = "Hastighetssensor", cs = "Snímač rychlosti", sk = "Snímač rýchlosti", da = "Hastighedssensor", ru = "Датчик скорости"),
                tr(
                    if (cfg.useSpeedSensor) "Włączony" else "Wyłączony",
                    if (cfg.useSpeedSensor) "On" else "Off",
                    de = if (cfg.useSpeedSensor) "An" else "Aus",
                    fr = if (cfg.useSpeedSensor) "Activé" else "Désactivé",
                    es = if (cfg.useSpeedSensor) "Activado" else "Desactivado",
                    pt = if (cfg.useSpeedSensor) "Ativado" else "Desativado",
                    it = if (cfg.useSpeedSensor) "Attivato" else "Disattivato",
                    nl = if (cfg.useSpeedSensor) "Ingeschakeld" else "Uitgeschakeld",
                    sv = if (cfg.useSpeedSensor) "På" else "Av",
                    cs = if (cfg.useSpeedSensor) "Zapnuto" else "Vypnuto",
                    sk = if (cfg.useSpeedSensor) "Zapnuté" else "Vypnuté",
                    da = if (cfg.useSpeedSensor) "Til" else "Fra",
                    ru = if (cfg.useSpeedSensor) "Вкл" else "Выкл",
                ),
            )
            ParamRow(
                tr("Czujnik zmiany biegów", "Shift Sensor", de = "Schaltsensor", fr = "Capteur de dérailleur", es = "Sensor de cambio", pt = "Sensor de mudança", it = "Sensore cambio", nl = "Schakelsensor", sv = "Växelsensor", cs = "Snímač řazení", sk = "Snímač radenia", da = "Gearsensor", ru = "Датчик переключения передач"),
                tr(
                    if (cfg.useShiftSensor) "Włączony" else "Wyłączony",
                    if (cfg.useShiftSensor) "On" else "Off",
                    de = if (cfg.useShiftSensor) "An" else "Aus",
                    fr = if (cfg.useShiftSensor) "Activé" else "Désactivé",
                    es = if (cfg.useShiftSensor) "Activado" else "Desactivado",
                    pt = if (cfg.useShiftSensor) "Ativado" else "Desativado",
                    it = if (cfg.useShiftSensor) "Attivato" else "Disattivato",
                    nl = if (cfg.useShiftSensor) "Ingeschakeld" else "Uitgeschakeld",
                    sv = if (cfg.useShiftSensor) "På" else "Av",
                    cs = if (cfg.useShiftSensor) "Zapnuto" else "Vypnuto",
                    sk = if (cfg.useShiftSensor) "Zapnuté" else "Vypnuté",
                    da = if (cfg.useShiftSensor) "Til" else "Fra",
                    ru = if (cfg.useShiftSensor) "Вкл" else "Выкл",
                ),
            )
            ParamRow(
                tr("Tryb prowadzenia", "Walk Mode", de = "Schiebemodus", fr = "Mode marche", es = "Modo caminar", pt = "Modo caminhar", it = "Modalità camminata", nl = "Loopmodus", sv = "Gångläge", cs = "Režim chůze", sk = "Režim chôdze", da = "Gåtilstand", ru = "Режим ходьбы"),
                tr(
                    if (cfg.usePushWalk) "Włączony" else "Wyłączony",
                    if (cfg.usePushWalk) "On" else "Off",
                    de = if (cfg.usePushWalk) "An" else "Aus",
                    fr = if (cfg.usePushWalk) "Activé" else "Désactivé",
                    es = if (cfg.usePushWalk) "Activado" else "Desactivado",
                    pt = if (cfg.usePushWalk) "Ativado" else "Desativado",
                    it = if (cfg.usePushWalk) "Attivato" else "Disattivato",
                    nl = if (cfg.usePushWalk) "Ingeschakeld" else "Uitgeschakeld",
                    sv = if (cfg.usePushWalk) "På" else "Av",
                    cs = if (cfg.usePushWalk) "Zapnuto" else "Vypnuto",
                    sk = if (cfg.usePushWalk) "Zapnuté" else "Vypnuté",
                    da = if (cfg.usePushWalk) "Til" else "Fra",
                    ru = if (cfg.usePushWalk) "Вкл" else "Выкл",
                ),
            )
            ParamRow(tr("Czujnik temperatury", "Temperature Sensor", de = "Temperatursensor", fr = "Capteur de température", es = "Sensor de temperatura", pt = "Sensor de temperatura", it = "Sensore di temperatura", nl = "Temperatuursensor", sv = "Temperatursensor", cs = "Snímač teploty", sk = "Snímač teploty", da = "Temperatursensor", ru = "Датчик температуры"), TEMP_SENSOR_LABELS.getOrElse(cfg.temperatureSensorMode) { "?" })
            ParamRow(tr("Tryb świateł", "Lights Mode", de = "Lichtmodus", fr = "Mode des feux", es = "Modo de luces", pt = "Modo de luzes", it = "Modalità luci", nl = "Lichtmodus", sv = "Ljusläge", cs = "Režim světel", sk = "Režim svetiel", da = "Lystilstand", ru = "Режим освещения"), LIGHTS_MODE_LABELS.getOrElse(cfg.lightsMode) { "?" }, last = true)
        }
    }

    Column {
        MicroLabel(tr("Czujnik prędkości", "Speed Sensor", de = "Geschwindigkeitssensor", fr = "Capteur de vitesse", es = "Sensor de velocidad", pt = "Sensor de velocidade", it = "Sensore di velocità", nl = "Snelheidssensor", sv = "Hastighetssensor", cs = "Snímač rychlosti", sk = "Snímač rýchlosti", da = "Hastighedssensor", ru = "Датчик скорости"))
        Spacer(Modifier.height(6.dp))
        TokenCard(borderColor = Tokens.WhiteBorder) {
            ParamRow(tr("Rozmiar koła", "Wheel Size", de = "Radgröße", fr = "Taille de roue", es = "Tamaño de rueda", pt = "Tamanho da roda", it = "Dimensione ruota", nl = "Wielgrootte", sv = "Hjulstorlek", cs = "Velikost kola", sk = "Veľkosť kolesa", da = "Hjulstørrelse", ru = "Размер колеса"), "${cfg.wheelSizeInchX10 / 10.0}\"")
            ParamRow(tr("Sygnały (na obrót)", "Signals (per rotation)", de = "Signale (pro Umdrehung)", fr = "Signaux (par tour)", es = "Señales (por vuelta)", pt = "Sinais (por rotação)", it = "Segnali (per rotazione)", nl = "Signalen (per omwenteling)", sv = "Signaler (per varv)", cs = "Signály (na otáčku)", sk = "Signály (na otáčku)", da = "Signaler (pr. omdrejning)", ru = "Сигналы (за оборот)"), cfg.speedSensorSignals.toString(), last = true)
        }
    }

    Column {
        MicroLabel(tr("Czujnik zmiany biegów", "Shift Sensor", de = "Schaltsensor", fr = "Capteur de dérailleur", es = "Sensor de cambio", pt = "Sensor de mudança", it = "Sensore cambio", nl = "Schakelsensor", sv = "Växelsensor", cs = "Snímač řazení", sk = "Snímač radenia", da = "Gearsensor", ru = "Датчик переключения передач"))
        Spacer(Modifier.height(6.dp))
        TokenCard(borderColor = Tokens.WhiteBorder) {
            ParamRow(tr("Czas przerwania przy zmianie biegu", "Shift Interrupt Duration", de = "Dauer der Schaltunterbrechung", fr = "Durée de coupure au changement", es = "Duración de interrupción al cambiar", pt = "Duração da interrupção na mudança", it = "Durata interruzione cambio", nl = "Duur schakelonderbreking", sv = "Varaktighet växlingsavbrott", cs = "Doba přerušení při řazení", sk = "Doba prerušenia pri radení", da = "Varighed af gearafbrydelse", ru = "Длительность прерывания переключения"), "${cfg.shiftInterruptDurationMs} ms")
            ParamRow(tr("Próg prądu przy zmianie biegu", "Shift Current Threshold", de = "Stromschwelle beim Schalten", fr = "Seuil de courant au changement", es = "Umbral de corriente al cambiar", pt = "Limiar de corrente na mudança", it = "Soglia di corrente al cambio", nl = "Stroomdrempel bij schakelen", sv = "Strömtröskel vid växling", cs = "Práh proudu při řazení", sk = "Prah prúdu pri radení", da = "Strømtærskel ved gearskift", ru = "Порог тока при переключении"), "${cfg.shiftInterruptCurrentThresholdPercent}%", last = true)
        }
    }

    Column {
        MicroLabel(tr("Różne", "Miscellaneous", de = "Sonstiges", fr = "Divers", es = "Varios", pt = "Diversos", it = "Varie", nl = "Diversen", sv = "Övrigt", cs = "Různé", sk = "Rôzne", da = "Diverse", ru = "Разное"))
        Spacer(Modifier.height(6.dp))
        TokenCard(borderColor = Tokens.WhiteBorder) {
            ParamRow(tr("Dane na wyświetlaczu w trybie prowadzenia", "Walk Mode Data Display", de = "Datenanzeige im Schiebemodus", fr = "Affichage des données en mode marche", es = "Visualización de datos en modo caminar", pt = "Exibição de dados no modo caminhar", it = "Visualizzazione dati in modalità camminata", nl = "Gegevensweergave in loopmodus", sv = "Datavisning i gångläge", cs = "Zobrazení dat v režimu chůze", sk = "Zobrazenie dát v režime chôdze", da = "Datavisning i gåtilstand", ru = "Отображение данных в режиме ходьбы"), WALK_MODE_DATA_LABELS.getOrElse(cfg.walkModeDataDisplay) { "?" })
            ParamRow(tr("Tryb wyboru wspomagania", "Assist Mode Select", de = "Auswahl Unterstützungsmodus", fr = "Sélection du mode d'assistance", es = "Selección del modo de asistencia", pt = "Seleção do modo de assistência", it = "Selezione modalità di assistenza", nl = "Ondersteuningsmodus selectie", sv = "Val av assistansläge", cs = "Výběr režimu asistence", sk = "Výber režimu asistencie", da = "Valg af assistancetilstand", ru = "Выбор режима поддержки"), cfg.assistModeSelect.toString())
            ParamRow(tr("Poziom wspomagania przy starcie", "Assist Startup Level", de = "Unterstützungsstufe beim Start", fr = "Niveau d'assistance au démarrage", es = "Nivel de asistencia al arrancar", pt = "Nível de assistência ao arrancar", it = "Livello di assistenza all'avvio", nl = "Ondersteuningsniveau bij opstarten", sv = "Assistansnivå vid start", cs = "Úroveň asistence při startu", sk = "Úroveň asistencie pri štarte", da = "Assistanceniveau ved start", ru = "Уровень поддержки при старте"), cfg.assistStartupLevel.toString())
            ParamRow(
                tr("Jednostki imperialne (mph)", "Freedom units (mph)", de = "Imperiale Einheiten (mph)", fr = "Unités impériales (mph)", es = "Unidades imperiales (mph)", pt = "Unidades imperiais (mph)", it = "Unità imperiali (mph)", nl = "Imperiale eenheden (mph)", sv = "Imperialistiska enheter (mph)", cs = "Imperiální jednotky (mph)", sk = "Imperiálne jednotky (mph)", da = "Imperialske enheder (mph)", ru = "Имперские единицы (mph)"),
                tr(
                    if (cfg.useFreedomUnits) "Włączone" else "Wyłączone",
                    if (cfg.useFreedomUnits) "On" else "Off",
                    de = if (cfg.useFreedomUnits) "An" else "Aus",
                    fr = if (cfg.useFreedomUnits) "Activé" else "Désactivé",
                    es = if (cfg.useFreedomUnits) "Activado" else "Desactivado",
                    pt = if (cfg.useFreedomUnits) "Ativado" else "Desativado",
                    it = if (cfg.useFreedomUnits) "Attivato" else "Disattivato",
                    nl = if (cfg.useFreedomUnits) "Ingeschakeld" else "Uitgeschakeld",
                    sv = if (cfg.useFreedomUnits) "På" else "Av",
                    cs = if (cfg.useFreedomUnits) "Zapnuto" else "Vypnuto",
                    sk = if (cfg.useFreedomUnits) "Zapnuté" else "Vypnuté",
                    da = if (cfg.useFreedomUnits) "Til" else "Fra",
                    ru = if (cfg.useFreedomUnits) "Вкл" else "Выкл",
                ),
                last = true,
            )
        }
    }

    listOf(0, 1).forEach { profile ->
        Column {
            MicroLabel(
                if (profile == 0) {
                    tr("Poziomy wspomagania - Profil 1 (Standard)", "Assist Levels - Profile 1 (Standard)", de = "Unterstützungsstufen - Profil 1 (Standard)", fr = "Niveaux d'assistance - Profil 1 (Standard)", es = "Niveles de asistencia - Perfil 1 (Standard)", pt = "Níveis de assistência - Perfil 1 (Standard)", it = "Livelli di assistenza - Profilo 1 (Standard)", nl = "Ondersteuningsniveaus - Profiel 1 (Standard)", sv = "Assistansnivåer - Profil 1 (Standard)", cs = "Úrovně asistence - Profil 1 (Standard)", sk = "Úrovne asistencie - Profil 1 (Standard)", da = "Assistanceniveauer - Profil 1 (Standard)", ru = "Уровни поддержки - Профиль 1 (Standard)")
                } else {
                    tr("Poziomy wspomagania - Profil 2 (Sport)", "Assist Levels - Profile 2 (Sport)", de = "Unterstützungsstufen - Profil 2 (Sport)", fr = "Niveaux d'assistance - Profil 2 (Sport)", es = "Niveles de asistencia - Perfil 2 (Sport)", pt = "Níveis de assistência - Perfil 2 (Sport)", it = "Livelli di assistenza - Profilo 2 (Sport)", nl = "Ondersteuningsniveaus - Profiel 2 (Sport)", sv = "Assistansnivåer - Profil 2 (Sport)", cs = "Úrovně asistence - Profil 2 (Sport)", sk = "Úrovne asistencie - Profil 2 (Sport)", da = "Assistanceniveauer - Profil 2 (Sport)", ru = "Уровни поддержки - Профиль 2 (Sport)")
                },
            )
            Spacer(Modifier.height(6.dp))
            TokenCard(borderColor = Tokens.WhiteBorder) {
                for (level in 0..9) {
                    val al = cfg.assistLevel(profile, level)
                    ParamRow(
                        tr("Poziom $level", "Level $level", de = "Stufe $level", fr = "Niveau $level", es = "Nivel $level", pt = "Nível $level", it = "Livello $level", nl = "Niveau $level", sv = "Nivå $level", cs = "Úroveň $level", sk = "Úroveň $level", da = "Niveau $level", ru = "Уровень $level"),
                        "${ASSIST_TYPE_LABELS.getOrElse(al.baseType()) { "?" }} · ${al.targetCurrentPercent}% / ${al.maxSpeedPercent}%",
                        last = level == 9,
                    )
                }
            }
        }
    }
}

private val THROTTLE_SPD_LIM_OPT_LABELS = listOf("Disabled", "Enabled", "Standard Levels")
private val TEMP_SENSOR_LABELS = listOf("Disabled", "Controller", "Motor", "All")
private val LIGHTS_MODE_LABELS = listOf("Default", "Disabled", "Always On", "Brake Light")
private val WALK_MODE_DATA_LABELS = listOf("Speed", "Temperature", "Requested Power", "Battery Level")
private val ASSIST_TYPE_LABELS = listOf("Disabled", "PAS", "Throttle", "Cruise")

private fun buildDiagnosticsText(state: UiState, lang: AppLanguage): String = buildString {
    appendLine(tr(lang, "EggSPEED - Wszystko (podgląd) - spis parametrow", "EggSPEED - All in View - parameter list", de = "EggSPEED - Alles (Übersicht) - Parameterliste", fr = "EggSPEED - Tout (aperçu) - liste des paramètres", es = "EggSPEED - Todo (vista previa) - lista de parámetros", pt = "EggSPEED - Tudo (vista geral) - lista de parâmetros", it = "EggSPEED - Tutto (panoramica) - elenco parametri", nl = "EggSPEED - Alles (overzicht) - parameterlijst", sv = "EggSPEED - Allt (översikt) - parameterlista", cs = "EggSPEED - Vše (přehled) - seznam parametrů", sk = "EggSPEED - Všetko (prehľad) - zoznam parametrov", da = "EggSPEED - Alt (oversigt) - parameterliste", ru = "EggSPEED - Всё (обзор) - список параметров"))
    appendLine()
    state.general?.let { gen ->
        appendLine("[Bafang Motor Type]")
        appendLine("${tr(lang, "Producent", "Manufacturer", de = "Hersteller", fr = "Fabricant", es = "Fabricante", pt = "Fabricante", it = "Produttore", nl = "Fabrikant", sv = "Tillverkare", cs = "Výrobce", sk = "Výrobca", da = "Producent", ru = "Производитель")}: ${gen.manufacturer}")
        appendLine("Model: ${gen.model}")
        appendLine("${tr(lang, "Wersja sprzetu", "Hardware version", de = "Hardware-Version", fr = "Version matérielle", es = "Versión de hardware", pt = "Versão de hardware", it = "Versione hardware", nl = "Hardwareversie", sv = "Hårdvaruversion", cs = "Verze hardwaru", sk = "Verzia hardvéru", da = "Hardwareversion", ru = "Версия оборудования")}: ${gen.hardwareVersion}")
        appendLine("Firmware: ${gen.firmwareVersion}")
        appendLine("${tr(lang, "Napiecie nominalne", "Nominal voltage", de = "Nennspannung", fr = "Tension nominale", es = "Voltaje nominal", pt = "Tensão nominal", it = "Tensione nominale", nl = "Nominale spanning", sv = "Nominell spänning", cs = "Jmenovité napětí", sk = "Menovité napätie", da = "Nominel spænding", ru = "Номинальное напряжение")}: ${gen.nominalVoltage} V")
        appendLine("${tr(lang, "Prad maksymalny", "Max current", de = "Maximalstrom", fr = "Courant maximal", es = "Corriente máxima", pt = "Corrente máxima", it = "Corrente massima", nl = "Maximale stroom", sv = "Maxström", cs = "Maximální proud", sk = "Maximálny prúd", da = "Maksimal strøm", ru = "Максимальный ток")}: ${gen.maxCurrentA} A")
        appendLine()
    }
    val bas = state.basicOrDefault
    appendLine("[Bafang Basic]")
    appendLine("${tr(lang, "Ochrona baterii (LBP)", "Battery protection (LBP)", de = "Batterieschutz (LBP)", fr = "Protection batterie (LBP)", es = "Protección de batería (LBP)", pt = "Proteção de bateria (LBP)", it = "Protezione batteria (LBP)", nl = "Batterijbescherming (LBP)", sv = "Batteriskydd (LBP)", cs = "Ochrana baterie (LBP)", sk = "Ochrana batérie (LBP)", da = "Batteribeskyttelse (LBP)", ru = "Защита батареи (LBP)")}: ${bas.lowBatteryProtection} V")
    appendLine("${tr(lang, "Limit pradu (LC)", "Current limit (LC)", de = "Strombegrenzung (LC)", fr = "Limite de courant (LC)", es = "Límite de corriente (LC)", pt = "Limite de corrente (LC)", it = "Limite di corrente (LC)", nl = "Stroomlimiet (LC)", sv = "Strömgräns (LC)", cs = "Omezení proudu (LC)", sk = "Obmedzenie prúdu (LC)", da = "Strømgrænse (LC)", ru = "Ограничение тока (LC)")}: ${bas.currentLimit} A")
    appendLine("${tr(lang, "Kolo", "Wheel", de = "Rad", fr = "Roue", es = "Rueda", pt = "Roda", it = "Ruota", nl = "Wiel", sv = "Hjul", cs = "Kolo", sk = "Koleso", da = "Hjul", ru = "Колесо")}: ${WHEEL_SIZE_LABELS.getOrElse(bas.wheelDiameterCode) { "${tr(lang, "kod", "code", de = "Code", fr = "code", es = "código", pt = "código", it = "codice", nl = "code", sv = "kod", cs = "kód", sk = "kód", da = "kode", ru = "код")} ${bas.wheelDiameterCode}" }}")
    appendLine("${tr(lang, "Czujnik predkosci (SMM)", "Speed meter (SMM)", de = "Geschwindigkeitssensor (SMM)", fr = "Capteur de vitesse (SMM)", es = "Sensor de velocidad (SMM)", pt = "Sensor de velocidade (SMM)", it = "Sensore di velocità (SMM)", nl = "Snelheidssensor (SMM)", sv = "Hastighetssensor (SMM)", cs = "Snímač rychlosti (SMM)", sk = "Snímač rýchlosti (SMM)", da = "Hastighedsmåler (SMM)", ru = "Датчик скорости (SMM)")}: ${bas.speedMeterModel}")
    appendLine("${tr(lang, "Sygnaly czujnika (SMS)", "Meter signals (SMS)", de = "Sensorsignale (SMS)", fr = "Signaux du capteur (SMS)", es = "Señales del sensor (SMS)", pt = "Sinais do sensor (SMS)", it = "Segnali del sensore (SMS)", nl = "Sensorsignalen (SMS)", sv = "Sensorsignaler (SMS)", cs = "Signály snímače (SMS)", sk = "Signály snímača (SMS)", da = "Målersignaler (SMS)", ru = "Сигналы датчика (SMS)")}: ${bas.speedMeterSignals}")
    appendLine()
    appendLine("[${tr(lang, "Poziomy wspomagania", "Assist levels", de = "Unterstützungsstufen", fr = "Niveaux d'assistance", es = "Niveles de asistencia", pt = "Níveis de assistência", it = "Livelli di assistenza", nl = "Ondersteuningsniveaus", sv = "Assistansnivåer", cs = "Úrovně asistence", sk = "Úrovne asistencie", da = "Understøttelsesniveauer", ru = "Уровни поддержки")}]")
    bas.assistCurrentPct.forEachIndexed { i, alc ->
        appendLine("${tr(lang, "Poziom", "Level", de = "Stufe", fr = "Niveau", es = "Nivel", pt = "Nível", it = "Livello", nl = "Niveau", sv = "Nivå", cs = "Úroveň", sk = "Úroveň", da = "Niveau", ru = "Уровень")} $i: ${alc}% / ${bas.assistSpeedPct.getOrElse(i) { 0 }}%")
    }
    appendLine()
    val pas = state.pasOrDefault
    appendLine("[Bafang Pedal (PAS)]")
    appendLine("${tr(lang, "Typ czujnika (PT)", "Sensor type (PT)", de = "Sensortyp (PT)", fr = "Type de capteur (PT)", es = "Tipo de sensor (PT)", pt = "Tipo de sensor (PT)", it = "Tipo di sensore (PT)", nl = "Sensortype (PT)", sv = "Sensortyp (PT)", cs = "Typ snímače (PT)", sk = "Typ snímača (PT)", da = "Sensortype (PT)", ru = "Тип датчика (PT)")}: ${pas.pedalType}")
    appendLine("Designated Assist (DA): ${designatedAssistLabel(pas.designatedAssist)}")
    appendLine("${tr(lang, "Limit predkosci (SL)", "Speed limit (SL)", de = "Geschwindigkeitslimit (SL)", fr = "Limite de vitesse (SL)", es = "Límite de velocidad (SL)", pt = "Limite de velocidade (SL)", it = "Limite di velocità (SL)", nl = "Snelheidslimiet (SL)", sv = "Hastighetsgräns (SL)", cs = "Omezení rychlosti (SL)", sk = "Obmedzenie rýchlosti (SL)", da = "Hastighedsgrænse (SL)", ru = "Ограничение скорости (SL)")}: ${speedLimitLabel(pas.speedLimit)}")
    appendLine("${tr(lang, "Prad startowy (SC)", "Start current (SC)", de = "Startstrom (SC)", fr = "Courant de démarrage (SC)", es = "Corriente de arranque (SC)", pt = "Corrente de arranque (SC)", it = "Corrente di avvio (SC)", nl = "Startstroom (SC)", sv = "Startström (SC)", cs = "Počáteční proud (SC)", sk = "Počiatočný prúd (SC)", da = "Startstrøm (SC)", ru = "Начальный ток (SC)")}: ${pas.startCurrentPct}%")
    appendLine("Slow-start (SSM): ${pas.slowStartMode}")
    appendLine("Start degree (SDN): ${pas.startDegree}")
    appendLine(
        "${tr(lang, "Tryb pracy (WM)", "Work mode (WM)", de = "Arbeitsmodus (WM)", fr = "Mode de fonctionnement (WM)", es = "Modo de trabajo (WM)", pt = "Modo de funcionamento (WM)", it = "Modalità di lavoro (WM)", nl = "Werkmodus (WM)", sv = "Arbetsläge (WM)", cs = "Pracovní režim (WM)", sk = "Pracovný režim (WM)", da = "Arbejdstilstand (WM)", ru = "Рабочий режим (WM)")}: " +
            (if (pas.workMode == 0) tr(lang, "nieokreslony", "undetermined", de = "unbestimmt", fr = "indéterminé", es = "indeterminado", pt = "indeterminado", it = "indeterminato", nl = "onbepaald", sv = "obestämd", cs = "neurčeno", sk = "neurčené", da = "ubestemt", ru = "не определено") else pas.workMode.toString()),
    )
    appendLine("Time of stop (TS): ${pas.timeOfStop} x10 ms")
    appendLine("Current decay (CD): ${pas.currentDecay}")
    appendLine("Stop decay (SD): ${pas.stopDecay} x10 ms")
    appendLine("Keep current (KC): ${pas.keepCurrentPct}%")
    appendLine()
    val thr = state.thrOrDefault
    appendLine("[Bafang Throttle]")
    appendLine("${tr(lang, "Napiecie start (SV)", "Start voltage (SV)", de = "Startspannung (SV)", fr = "Tension de démarrage (SV)", es = "Voltaje de arranque (SV)", pt = "Tensão de arranque (SV)", it = "Tensione di avvio (SV)", nl = "Startspanning (SV)", sv = "Startspänning (SV)", cs = "Počáteční napětí (SV)", sk = "Počiatočné napätie (SV)", da = "Startspænding (SV)", ru = "Начальное напряжение (SV)")}: ${thr.startVoltage / 10.0} V")
    appendLine("${tr(lang, "Napiecie koncowe (EV)", "End voltage (EV)", de = "Endspannung (EV)", fr = "Tension finale (EV)", es = "Voltaje final (EV)", pt = "Tensão final (EV)", it = "Tensione finale (EV)", nl = "Eindspanning (EV)", sv = "Slutspänning (EV)", cs = "Konečné napětí (EV)", sk = "Konečné napätie (EV)", da = "Slutspænding (EV)", ru = "Конечное напряжение (EV)")}: ${thr.endVoltage / 10.0} V")
    appendLine(
        "${tr(lang, "Tryb", "Mode", de = "Modus", fr = "Mode", es = "Modo", pt = "Modo", it = "Modalità", nl = "Modus", sv = "Läge", cs = "Režim", sk = "Režim", da = "Tilstand", ru = "Режим")}: " +
            tr(
                lang,
                if (thr.mode == 0) "predkosc" else "prad",
                if (thr.mode == 0) "speed" else "current",
                de = if (thr.mode == 0) "Geschwindigkeit" else "Strom",
                fr = if (thr.mode == 0) "vitesse" else "courant",
                es = if (thr.mode == 0) "velocidad" else "corriente",
                pt = if (thr.mode == 0) "velocidade" else "corrente",
                it = if (thr.mode == 0) "velocità" else "corrente",
                nl = if (thr.mode == 0) "snelheid" else "stroom",
                sv = if (thr.mode == 0) "hastighet" else "ström",
                cs = if (thr.mode == 0) "rychlost" else "proud",
                sk = if (thr.mode == 0) "rýchlosť" else "prúd",
                da = if (thr.mode == 0) "hastighed" else "strøm",
                ru = if (thr.mode == 0) "скорость" else "ток",
            ),
    )
    appendLine("Designated Assist (DA): ${designatedAssistLabel(thr.designatedAssist)}")
    appendLine("${tr(lang, "Limit predkosci (SL)", "Speed limit (SL)", de = "Geschwindigkeitslimit (SL)", fr = "Limite de vitesse (SL)", es = "Límite de velocidad (SL)", pt = "Limite de velocidade (SL)", it = "Limite di velocità (SL)", nl = "Snelheidslimiet (SL)", sv = "Hastighetsgräns (SL)", cs = "Omezení rychlosti (SL)", sk = "Obmedzenie rýchlosti (SL)", da = "Hastighedsgrænse (SL)", ru = "Ограничение скорости (SL)")}: ${speedLimitLabel(thr.speedLimit)}")
    appendLine("${tr(lang, "Prad startowy (SC)", "Start current (SC)", de = "Startstrom (SC)", fr = "Courant de démarrage (SC)", es = "Corriente de arranque (SC)", pt = "Corrente de arranque (SC)", it = "Corrente di avvio (SC)", nl = "Startstroom (SC)", sv = "Startström (SC)", cs = "Počáteční proud (SC)", sk = "Počiatočný prúd (SC)", da = "Startstrøm (SC)", ru = "Начальный ток (SC)")}: ${thr.startCurrentPct}%")
}

private fun buildBbsFwDiagnosticsText(state: UiState, lang: AppLanguage): String = buildString {
    appendLine(tr(lang, "EggSPEED - Wszystko (podglad) - spis parametrow bbs-fw", "EggSPEED - All in View - bbs-fw parameter list", de = "EggSPEED - Alles (Übersicht) - bbs-fw Parameterliste", fr = "EggSPEED - Tout (aperçu) - liste des paramètres bbs-fw", es = "EggSPEED - Todo (vista previa) - lista de parámetros bbs-fw", pt = "EggSPEED - Tudo (vista geral) - lista de parâmetros bbs-fw", it = "EggSPEED - Tutto (panoramica) - elenco parametri bbs-fw", nl = "EggSPEED - Alles (overzicht) - bbs-fw parameterlijst", sv = "EggSPEED - Allt (översikt) - bbs-fw-parameterlista", cs = "EggSPEED - Vše (přehled) - seznam parametrů bbs-fw", sk = "EggSPEED - Všetko (prehľad) - zoznam parametrov bbs-fw", da = "EggSPEED - Alt (oversigt) - bbs-fw parameterliste", ru = "EggSPEED - Всё (обзор) - список параметров bbs-fw"))
    appendLine()
    state.bbsFwVersion?.let { v ->
        appendLine("[bbs-fw]")
        appendLine("${tr(lang, "Wersja firmware", "Firmware version", de = "Firmware-Version", fr = "Version du firmware", es = "Versión de firmware", pt = "Versão de firmware", it = "Versione firmware", nl = "Firmwareversie", sv = "Firmwareversion", cs = "Verze firmwaru", sk = "Verzia firmvéru", da = "Firmwareversion", ru = "Версия прошивки")}: ${v.versionLabel}")
        appendLine("${tr(lang, "Wersja formatu konfiguracji", "Config format version", de = "Konfigurationsformat-Version", fr = "Version du format de configuration", es = "Versión del formato de configuración", pt = "Versão do formato de configuração", it = "Versione formato configurazione", nl = "Configuratieformaat-versie", sv = "Konfigurationsformatversion", cs = "Verze formátu konfigurace", sk = "Verzia formátu konfigurácie", da = "Konfigurationsformatversion", ru = "Версия формата конфигурации")}: ${v.configVersion}")
        appendLine("${tr(lang, "Typ sterownika", "Controller type", de = "Steuergerätetyp", fr = "Type de contrôleur", es = "Tipo de controlador", pt = "Tipo de controlador", it = "Tipo di controller", nl = "Controllertype", sv = "Kontrollertyp", cs = "Typ řadiče", sk = "Typ radiča", da = "Controllertype", ru = "Тип контроллера")}: ${BbsFwController.name(v.ctrlType)} (${v.ctrlType})")
        appendLine()
    }
    val cfg = state.bbsFwConfigOrDefault
    appendLine("[${tr(lang, "Globalne", "Global", de = "Global", fr = "Global", es = "Global", pt = "Global", it = "Globale", nl = "Algemeen", sv = "Globalt", cs = "Globální", sk = "Globálne", da = "Globalt", ru = "Глобальные")}]")
    appendLine("${tr(lang, "Maks. prąd", "Max Current", de = "Max. Strom", fr = "Courant max", es = "Corriente máx.", pt = "Corrente máx.", it = "Corrente max", nl = "Max. stroom", sv = "Max ström", cs = "Max. proud", sk = "Max. prúd", da = "Maks. strøm", ru = "Макс. ток")}: ${cfg.maxCurrentAmps} A")
    appendLine("${tr(lang, "Narastanie prądu", "Current Ramp", de = "Stromanstieg", fr = "Rampe de courant", es = "Rampa de corriente", pt = "Rampa de corrente", it = "Rampa di corrente", nl = "Stroomoploop", sv = "Strömramp", cs = "Náběh proudu", sk = "Nábeh prúdu", da = "Strømrampe", ru = "Нарастание тока")}: ${cfg.currentRampAmpsS} A/s")
    appendLine("${tr(lang, "Maks. napięcie baterii", "Max Battery Voltage", de = "Max. Batteriespannung", fr = "Tension batterie max", es = "Voltaje máx. de batería", pt = "Voltagem máx. da bateria", it = "Tensione max batteria", nl = "Max. batterijspanning", sv = "Max batterispänning", cs = "Max. napětí baterie", sk = "Max. napätie batérie", da = "Maks. batterispænding", ru = "Макс. напряжение батареи")}: ${cfg.maxBatteryX100v / 100.0} V")
    appendLine("${tr(lang, "Odcięcie niskiego napięcia", "Low Voltage Cutoff", de = "Unterspannungsabschaltung", fr = "Coupure basse tension", es = "Corte de bajo voltaje", pt = "Corte de baixa tensão", it = "Taglio bassa tensione", nl = "Onderspanningsafsluiting", sv = "Underspänningsavstängning", cs = "Vypnutí při nízkém napětí", sk = "Vypnutie pri nízkom napätí", da = "Underspændingsafbrydelse", ru = "Отключение при низком напряжении")}: ${cfg.lowCutOffV} V")
    appendLine("${tr(lang, "Maks. prędkość", "Max Speed", de = "Max. Geschwindigkeit", fr = "Vitesse max", es = "Velocidad máx.", pt = "Velocidade máx.", it = "Velocità max", nl = "Max. snelheid", sv = "Max hastighet", cs = "Max. rychlost", sk = "Max. rýchlosť", da = "Maks. hastighed", ru = "Макс. скорость")}: ${cfg.maxSpeedKph} km/h")
    appendLine()
    appendLine("[${tr(lang, "Manetka", "Throttle", de = "Gasgriff", fr = "Accélérateur", es = "Acelerador", pt = "Acelerador", it = "Acceleratore", nl = "Gasgreep", sv = "Gasreglage", cs = "Plynová páčka", sk = "Plynová páčka", da = "Gashåndtag", ru = "Газ")}]")
    appendLine("${tr(lang, "Napięcie startowe", "Start Voltage", de = "Startspannung", fr = "Tension de démarrage", es = "Voltaje de arranque", pt = "Tensão de arranque", it = "Tensione di avvio", nl = "Startspanning", sv = "Startspänning", cs = "Počáteční napětí", sk = "Počiatočné napätie", da = "Startspænding", ru = "Начальное напряжение")}: ${cfg.throttleStartVoltageMv} mV")
    appendLine("${tr(lang, "Napięcie końcowe", "End Voltage", de = "Endspannung", fr = "Tension finale", es = "Voltaje final", pt = "Tensão final", it = "Tensione finale", nl = "Eindspanning", sv = "Slutspänning", cs = "Konečné napětí", sk = "Konečné napätie", da = "Slutspænding", ru = "Конечное напряжение")}: ${cfg.throttleEndVoltageMv} mV")
    appendLine("${tr(lang, "Prąd startowy", "Start Current", de = "Startstrom", fr = "Courant de démarrage", es = "Corriente de arranque", pt = "Corrente de arranque", it = "Corrente di avvio", nl = "Startstroom", sv = "Startström", cs = "Počáteční proud", sk = "Počiatočný prúd", da = "Startstrøm", ru = "Начальный ток")}: ${cfg.throttleStartPercent}%")
    appendLine("${tr(lang, "Opcje globalnego limitu prędkości", "Global Speed Limit Options", de = "Optionen für globales Geschwindigkeitslimit", fr = "Options de limite de vitesse globale", es = "Opciones de límite de velocidad global", pt = "Opções de limite de velocidade global", it = "Opzioni limite di velocità globale", nl = "Opties globale snelheidslimiet", sv = "Alternativ för global hastighetsgräns", cs = "Možnosti globálního omezení rychlosti", sk = "Možnosti globálneho obmedzenia rýchlosti", da = "Indstillinger for global hastighedsgrænse", ru = "Параметры глобального ограничения скорости")}: ${THROTTLE_SPD_LIM_OPT_LABELS.getOrElse(cfg.throttleGlobalSpdLimOpt) { "?" }}")
    appendLine("${tr(lang, "Globalny limit prędkości", "Global Speed Limit", de = "Globales Geschwindigkeitslimit", fr = "Limite de vitesse globale", es = "Límite de velocidad global", pt = "Limite de velocidade global", it = "Limite di velocità globale", nl = "Globale snelheidslimiet", sv = "Global hastighetsgräns", cs = "Globální omezení rychlosti", sk = "Globálne obmedzenie rýchlosti", da = "Global hastighedsgrænse", ru = "Глобальное ограничение скорости")}: ${cfg.throttleGlobalSpdLimPercent}%")
    appendLine()
    appendLine("[${tr(lang, "Wspomaganie pedałowania", "Pedal Assist", de = "Pedal Assist", fr = "Assistance au pédalage", es = "Asistencia de pedaleo", pt = "Assistência de pedalada", it = "Assistenza pedalata", nl = "Trapondersteuning", sv = "Pedalassistans", cs = "Asistence šlapání", sk = "Asistencia šliapania", da = "Pedalassistance", ru = "Педальная поддержка")}]")
    appendLine("${tr(lang, "Opóźnienie startu", "Start Delay", de = "Startverzögerung", fr = "Délai de démarrage", es = "Retardo de arranque", pt = "Atraso de arranque", it = "Ritardo di avvio", nl = "Startvertraging", sv = "Startfördröjning", cs = "Prodleva startu", sk = "Oneskorenie štartu", da = "Startforsinkelse", ru = "Задержка старта")}: ${cfg.pasStartDelayPulses * 15} deg")
    appendLine("${tr(lang, "Opóźnienie zatrzymania", "Stop Delay", de = "Stoppverzögerung", fr = "Délai d'arrêt", es = "Retardo de parada", pt = "Atraso de paragem", it = "Ritardo di arresto", nl = "Stopvertraging", sv = "Stoppfördröjning", cs = "Prodleva vypnutí", sk = "Oneskorenie vypnutia", da = "Stopforsinkelse", ru = "Задержка остановки")}: ${cfg.pasStopDelayX100s * 10} ms")
    appendLine("${tr(lang, "Podtrzymanie prądu", "Keep Current", de = "Stromhaltung", fr = "Maintien du courant", es = "Mantenimiento de corriente", pt = "Manutenção de corrente", it = "Mantenimento corrente", nl = "Stroom vasthouden", sv = "Bibehållen ström", cs = "Udržení proudu", sk = "Udržanie prúdu", da = "Bibehold strøm", ru = "Удержание тока")}: ${cfg.pasKeepCurrentPercent}%")
    appendLine("${tr(lang, "Kadencja podtrzymania prądu", "Keep Current Cadence", de = "Kadenz für Stromhaltung", fr = "Cadence de maintien du courant", es = "Cadencia de mantenimiento de corriente", pt = "Cadência de manutenção de corrente", it = "Cadenza di mantenimento corrente", nl = "Cadans voor stroom vasthouden", sv = "Kadens för bibehållen ström", cs = "Kadence pro udržení proudu", sk = "Kadencia pre udržanie prúdu", da = "Kadence for strømbibeholdelse", ru = "Каденс удержания тока")}: ${cfg.pasKeepCurrentCadenceRpm} rpm")
    appendLine()
    appendLine("[${tr(lang, "Funkcje", "Features", de = "Funktionen", fr = "Fonctionnalités", es = "Funciones", pt = "Funcionalidades", it = "Funzionalità", nl = "Functies", sv = "Funktioner", cs = "Funkce", sk = "Funkcie", da = "Funktioner", ru = "Функции")}]")
    appendLine("${tr(lang, "Czujnik prędkości", "Speed Sensor", de = "Geschwindigkeitssensor", fr = "Capteur de vitesse", es = "Sensor de velocidad", pt = "Sensor de velocidade", it = "Sensore di velocità", nl = "Snelheidssensor", sv = "Hastighetssensor", cs = "Snímač rychlosti", sk = "Snímač rýchlosti", da = "Hastighedssensor", ru = "Датчик скорости")}: ${if (cfg.useSpeedSensor) "On" else "Off"}")
    appendLine("${tr(lang, "Czujnik zmiany biegów", "Shift Sensor", de = "Schaltsensor", fr = "Capteur de dérailleur", es = "Sensor de cambio", pt = "Sensor de mudança", it = "Sensore cambio", nl = "Schakelsensor", sv = "Växelsensor", cs = "Snímač řazení", sk = "Snímač radenia", da = "Gearsensor", ru = "Датчик переключения передач")}: ${if (cfg.useShiftSensor) "On" else "Off"}")
    appendLine("${tr(lang, "Tryb prowadzenia", "Walk Mode", de = "Schiebemodus", fr = "Mode marche", es = "Modo caminar", pt = "Modo caminhar", it = "Modalità camminata", nl = "Loopmodus", sv = "Gångläge", cs = "Režim chůze", sk = "Režim chôdze", da = "Gåtilstand", ru = "Режим ходьбы")}: ${if (cfg.usePushWalk) "On" else "Off"}")
    appendLine("${tr(lang, "Czujnik temperatury", "Temperature Sensor", de = "Temperatursensor", fr = "Capteur de température", es = "Sensor de temperatura", pt = "Sensor de temperatura", it = "Sensore di temperatura", nl = "Temperatuursensor", sv = "Temperatursensor", cs = "Snímač teploty", sk = "Snímač teploty", da = "Temperatursensor", ru = "Датчик температуры")}: ${TEMP_SENSOR_LABELS.getOrElse(cfg.temperatureSensorMode) { "?" }}")
    appendLine("${tr(lang, "Tryb świateł", "Lights Mode", de = "Lichtmodus", fr = "Mode des feux", es = "Modo de luces", pt = "Modo de luzes", it = "Modalità luci", nl = "Lichtmodus", sv = "Ljusläge", cs = "Režim světel", sk = "Režim svetiel", da = "Lystilstand", ru = "Режим освещения")}: ${LIGHTS_MODE_LABELS.getOrElse(cfg.lightsMode) { "?" }}")
    appendLine()
    appendLine("[${tr(lang, "Czujnik prędkości", "Speed Sensor", de = "Geschwindigkeitssensor", fr = "Capteur de vitesse", es = "Sensor de velocidad", pt = "Sensor de velocidade", it = "Sensore di velocità", nl = "Snelheidssensor", sv = "Hastighetssensor", cs = "Snímač rychlosti", sk = "Snímač rýchlosti", da = "Hastighedssensor", ru = "Датчик скорости")}]")
    appendLine("${tr(lang, "Rozmiar koła", "Wheel Size", de = "Radgröße", fr = "Taille de roue", es = "Tamaño de rueda", pt = "Tamanho da roda", it = "Dimensione ruota", nl = "Wielgrootte", sv = "Hjulstorlek", cs = "Velikost kola", sk = "Veľkosť kolesa", da = "Hjulstørrelse", ru = "Размер колеса")}: ${cfg.wheelSizeInchX10 / 10.0}\"")
    appendLine("${tr(lang, "Sygnały (na obrót)", "Signals (per rotation)", de = "Signale (pro Umdrehung)", fr = "Signaux (par tour)", es = "Señales (por vuelta)", pt = "Sinais (por rotação)", it = "Segnali (per rotazione)", nl = "Signalen (per omwenteling)", sv = "Signaler (per varv)", cs = "Signály (na otáčku)", sk = "Signály (na otáčku)", da = "Signaler (pr. omdrejning)", ru = "Сигналы (за оборот)")}: ${cfg.speedSensorSignals}")
    appendLine()
    appendLine("[${tr(lang, "Czujnik zmiany biegów", "Shift Sensor", de = "Schaltsensor", fr = "Capteur de dérailleur", es = "Sensor de cambio", pt = "Sensor de mudança", it = "Sensore cambio", nl = "Schakelsensor", sv = "Växelsensor", cs = "Snímač řazení", sk = "Snímač radenia", da = "Gearsensor", ru = "Датчик переключения передач")}]")
    appendLine("${tr(lang, "Czas przerwania przy zmianie biegu", "Shift Interrupt Duration", de = "Dauer der Schaltunterbrechung", fr = "Durée de coupure au changement", es = "Duración de interrupción al cambiar", pt = "Duração da interrupção na mudança", it = "Durata interruzione cambio", nl = "Duur schakelonderbreking", sv = "Varaktighet växlingsavbrott", cs = "Doba přerušení při řazení", sk = "Doba prerušenia pri radení", da = "Varighed af gearafbrydelse", ru = "Длительность прерывания переключения")}: ${cfg.shiftInterruptDurationMs} ms")
    appendLine("${tr(lang, "Próg prądu przy zmianie biegu", "Shift Current Threshold", de = "Stromschwelle beim Schalten", fr = "Seuil de courant au changement", es = "Umbral de corriente al cambiar", pt = "Limiar de corrente na mudança", it = "Soglia di corrente al cambio", nl = "Stroomdrempel bij schakelen", sv = "Strömtröskel vid växling", cs = "Práh proudu při řazení", sk = "Prah prúdu pri radení", da = "Strømtærskel ved gearskift", ru = "Порог тока при переключении")}: ${cfg.shiftInterruptCurrentThresholdPercent}%")
    appendLine()
    appendLine("[${tr(lang, "Różne", "Miscellaneous", de = "Sonstiges", fr = "Divers", es = "Varios", pt = "Diversos", it = "Varie", nl = "Diversen", sv = "Övrigt", cs = "Různé", sk = "Rôzne", da = "Diverse", ru = "Разное")}]")
    appendLine("${tr(lang, "Dane na wyświetlaczu w trybie prowadzenia", "Walk Mode Data Display", de = "Datenanzeige im Schiebemodus", fr = "Affichage des données en mode marche", es = "Visualización de datos en modo caminar", pt = "Exibição de dados no modo caminhar", it = "Visualizzazione dati in modalità camminata", nl = "Gegevensweergave in loopmodus", sv = "Datavisning i gångläge", cs = "Zobrazení dat v režimu chůze", sk = "Zobrazenie dát v režime chôdze", da = "Datavisning i gåtilstand", ru = "Отображение данных в режиме ходьбы")}: ${WALK_MODE_DATA_LABELS.getOrElse(cfg.walkModeDataDisplay) { "?" }}")
    appendLine("${tr(lang, "Tryb wyboru wspomagania", "Assist Mode Select", de = "Auswahl Unterstützungsmodus", fr = "Sélection du mode d'assistance", es = "Selección del modo de asistencia", pt = "Seleção do modo de assistência", it = "Selezione modalità di assistenza", nl = "Ondersteuningsmodus selectie", sv = "Val av assistansläge", cs = "Výběr režimu asistence", sk = "Výber režimu asistencie", da = "Valg af assistancetilstand", ru = "Выбор режима поддержки")}: ${cfg.assistModeSelect}")
    appendLine("${tr(lang, "Poziom wspomagania przy starcie", "Assist Startup Level", de = "Unterstützungsstufe beim Start", fr = "Niveau d'assistance au démarrage", es = "Nivel de asistencia al arrancar", pt = "Nível de assistência ao arrancar", it = "Livello di assistenza all'avvio", nl = "Ondersteuningsniveau bij opstarten", sv = "Assistansnivå vid start", cs = "Úroveň asistence při startu", sk = "Úroveň asistencie pri štarte", da = "Assistanceniveau ved start", ru = "Уровень поддержки при старте")}: ${cfg.assistStartupLevel}")
    appendLine("${tr(lang, "Jednostki imperialne (mph)", "Freedom units (mph)", de = "Imperiale Einheiten (mph)", fr = "Unités impériales (mph)", es = "Unidades imperiales (mph)", pt = "Unidades imperiais (mph)", it = "Unità imperiali (mph)", nl = "Imperiale eenheden (mph)", sv = "Imperialistiska enheter (mph)", cs = "Imperiální jednotky (mph)", sk = "Imperiálne jednotky (mph)", da = "Imperialske enheder (mph)", ru = "Имперские единицы (mph)")}: ${if (cfg.useFreedomUnits) "On" else "Off"}")
    appendLine()
    listOf(0, 1).forEach { profile ->
        val profileLabel = if (profile == 0) {
            tr(lang, "Poziomy wspomagania - Profil 1 (Standard)", "Assist Levels - Profile 1 (Standard)", de = "Unterstützungsstufen - Profil 1 (Standard)", fr = "Niveaux d'assistance - Profil 1 (Standard)", es = "Niveles de asistencia - Perfil 1 (Standard)", pt = "Níveis de assistência - Perfil 1 (Standard)", it = "Livelli di assistenza - Profilo 1 (Standard)", nl = "Ondersteuningsniveaus - Profiel 1 (Standard)", sv = "Assistansnivåer - Profil 1 (Standard)", cs = "Úrovně asistence - Profil 1 (Standard)", sk = "Úrovne asistencie - Profil 1 (Standard)", da = "Assistanceniveauer - Profil 1 (Standard)", ru = "Уровни поддержки - Профиль 1 (Standard)")
        } else {
            tr(lang, "Poziomy wspomagania - Profil 2 (Sport)", "Assist Levels - Profile 2 (Sport)", de = "Unterstützungsstufen - Profil 2 (Sport)", fr = "Niveaux d'assistance - Profil 2 (Sport)", es = "Niveles de asistencia - Perfil 2 (Sport)", pt = "Níveis de assistência - Perfil 2 (Sport)", it = "Livelli di assistenza - Profilo 2 (Sport)", nl = "Ondersteuningsniveaus - Profiel 2 (Sport)", sv = "Assistansnivåer - Profil 2 (Sport)", cs = "Úrovně asistence - Profil 2 (Sport)", sk = "Úrovne asistencie - Profil 2 (Sport)", da = "Assistanceniveauer - Profil 2 (Sport)", ru = "Уровни поддержки - Профиль 2 (Sport)")
        }
        appendLine("[$profileLabel]")
        for (level in 0..9) {
            val al = cfg.assistLevel(profile, level)
            appendLine(
                "${tr(lang, "Poziom", "Level", de = "Stufe", fr = "Niveau", es = "Nivel", pt = "Nível", it = "Livello", nl = "Niveau", sv = "Nivå", cs = "Úroveň", sk = "Úroveň", da = "Niveau", ru = "Уровень")} $level: ${ASSIST_TYPE_LABELS.getOrElse(al.baseType()) { "?" }}, " +
                    "Target Current ${al.targetCurrentPercent}%, Max Speed ${al.maxSpeedPercent}%, " +
                    "Max Throttle Current ${al.maxThrottleCurrentPercent}%, Max Cadence ${al.maxCadencePercent}%, " +
                    "Torque Factor ${al.torqueAmplificationFactorX10 / 10.0}x",
            )
        }
        appendLine()
    }
}

@Composable
private fun ParamRow(label: String, value: String, last: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextPrimary, modifier = Modifier.weight(1f))
        Text(value, fontFamily = Sora, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Tokens.TextPrimary)
    }
    if (!last) HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
}
