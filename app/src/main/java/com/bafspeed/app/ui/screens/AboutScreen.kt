package com.bafspeed.app.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bafspeed.app.BuildConfig
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.ui.components.EggSpeedWordmark
import com.bafspeed.app.ui.components.ExpandableParamTile
import com.bafspeed.app.ui.components.TokenCard
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens

private val APP_VERSION = BuildConfig.VERSION_NAME
private val BUILD_STAMP = BuildConfig.BUILD_STAMP
private const val CONTACT_EMAIL = "tomasz.pieczara@gazeta.pl"
private const val WEBSITE = "spotrobotics.app"
private const val SUPPORT_FORM_URL = "https://spotrobotics.app/support/"
private const val GITHUB_DISPLAY = "github.com/moskil2/EggSPEED"
private const val GITHUB_URL = "https://$GITHUB_DISPLAY"

/**
 * Zakladka "Menu" - poza informacjami o aplikacji (dawne "About") zawiera tez akcje zwiazane
 * z Google Play (ocena, sprawdzenie aktualizacji). Ten sam wzorzec wizualny co Pedal/Throttle/General.
 */
@Composable
fun AboutScreen() {
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .background(Tokens.Bg)
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = 16.dp)),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        TokenCard(borderColor = Tokens.WhiteBorder) {
            EggSpeedWordmark(fontSize = 20.sp, letterSpacing = 0.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                tr("Stworzone przez Tomasza Pieczarę", "Created by Tomasz Pieczara", de = "Erstellt von Tomasz Pieczara", fr = "Créé par Tomasz Pieczara", es = "Creado por Tomasz Pieczara", pt = "Criado por Tomasz Pieczara", it = "Creato da Tomasz Pieczara", nl = "Gemaakt door Tomasz Pieczara", sv = "Skapad av Tomasz Pieczara", cs = "Vytvořil Tomasz Pieczara", sk = "Vytvoril Tomasz Pieczara", da = "Skabt af Tomasz Pieczara", ru = "Создано Томашем Печарой"),
                fontFamily = Manrope, fontSize = 14.sp, color = Tokens.TextPrimary,
            )
        }

        TokenCard(borderColor = Tokens.WhiteBorder) {
            ActionRow(
                icon = "⭐",
                label = tr("Oceń aplikację w Google Play", "Rate the app on Google Play", de = "App bei Google Play bewerten", fr = "Noter l'application sur Google Play", es = "Valorar la app en Google Play", pt = "Avaliar a app na Google Play", it = "Valuta l'app su Google Play", nl = "Beoordeel de app op Google Play", sv = "Betygsätt appen på Google Play", cs = "Ohodnotit aplikaci na Google Play", sk = "Ohodnotiť aplikáciu na Google Play", da = "Bedøm appen på Google Play", ru = "Оцените приложение в Google Play"),
                onClick = { openPlayStoreListing(context) },
            )
            HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
            ActionRow(
                icon = "⬆️",
                label = tr("Sprawdź aktualizacje", "Check for updates", de = "Nach Updates suchen", fr = "Vérifier les mises à jour", es = "Buscar actualizaciones", pt = "Verificar atualizações", it = "Controlla aggiornamenti", nl = "Controleren op updates", sv = "Sök efter uppdateringar", cs = "Zkontrolovat aktualizace", sk = "Skontrolovať aktualizácie", da = "Søg efter opdateringer", ru = "Проверить обновления"),
                onClick = { openPlayStoreListing(context) },
            )
        }

        TokenCard(borderColor = Tokens.WhiteBorder) {
            InfoRow(tr("Wersja", "Version", de = "Version", fr = "Version", es = "Versión", pt = "Versão", it = "Versione", nl = "Versie", sv = "Version", cs = "Verze", sk = "Verzia", da = "Version", ru = "Версия"), APP_VERSION)
            HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
            InfoRow("Build", BUILD_STAMP)
        }

        TokenCard(borderColor = Tokens.WhiteBorder) {
            InfoRow(
                tr("Kontakt", "Contact", de = "Kontakt", fr = "Contact", es = "Contacto", pt = "Contacto", it = "Contatto", nl = "Contact", sv = "Kontakt", cs = "Kontakt", sk = "Kontakt", da = "Kontakt", ru = "Контакт"),
                CONTACT_EMAIL,
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$CONTACT_EMAIL"))
                    runCatching { context.startActivity(intent) }
                },
            )
            HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
            InfoRow(
                tr("Strona", "Website", de = "Webseite", fr = "Site web", es = "Sitio web", pt = "Site", it = "Sito web", nl = "Website", sv = "Webbplats", cs = "Web", sk = "Web", da = "Websted", ru = "Веб-сайт"),
                WEBSITE,
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://$WEBSITE"))
                    runCatching { context.startActivity(intent) }
                },
            )
            HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
            InfoRow(
                "GitHub",
                GITHUB_DISPLAY,
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL))
                    runCatching { context.startActivity(intent) }
                },
            )
        }

        ExpandableParamTile(
            label = tr("Polityka prywatności", "Privacy policy", de = "Datenschutzrichtlinie", fr = "Politique de confidentialité", es = "Política de privacidad", pt = "Política de privacidade", it = "Informativa sulla privacy", nl = "Privacybeleid", sv = "Integritetspolicy", cs = "Zásady ochrany osobních údajů", sk = "Zásady ochrany osobných údajov", da = "Privatlivspolitik", ru = "Политика конфиденциальности"),
            valueLabel = "",
            descriptionColor = Tokens.TextPrimary,
            description = tr(
                "EggSPEED nie zbiera, nie wysyła ani nie przechowuje żadnych danych osobowych poza " +
                    "Twoim telefonem. Komunikacja odbywa się wyłącznie lokalnie przez kabel USB między telefonem " +
                    "a sterownikiem Bafang - bez internetu, bez analityki, bez śledzenia. Profile konfiguracji " +
                    "zapisujesz wyłącznie Ty, jako pliki .ini na swoim urządzeniu.",
                "EggSPEED does not collect, send, or store any personal data beyond " +
                    "your phone. Communication happens purely locally over a USB cable between the phone " +
                    "and the Bafang controller - no internet, no analytics, no tracking. Configuration profiles " +
                    "are saved only by you, as .ini files on your own device.",
                de = "EggSPEED sammelt, sendet oder speichert keinerlei personenbezogene Daten außerhalb " +
                    "deines Telefons. Die Kommunikation erfolgt ausschließlich lokal über ein USB-Kabel zwischen Telefon " +
                    "und Bafang-Steuergerät - kein Internet, keine Analyse, kein Tracking. Konfigurationsprofile " +
                    "speicherst nur du selbst, als .ini-Dateien auf deinem eigenen Gerät.",
                fr = "EggSPEED ne collecte, n'envoie ni ne stocke aucune donnée personnelle en dehors de " +
                    "votre téléphone. La communication se fait uniquement en local via un câble USB entre le téléphone " +
                    "et le contrôleur Bafang - sans internet, sans analytique, sans suivi. Les profils de configuration " +
                    "ne sont enregistrés que par vous, sous forme de fichiers .ini sur votre propre appareil.",
                es = "EggSPEED no recopila, envía ni almacena ningún dato personal fuera de " +
                    "tu teléfono. La comunicación ocurre únicamente de forma local mediante un cable USB entre el teléfono " +
                    "y el controlador Bafang - sin internet, sin analítica, sin seguimiento. Los perfiles de configuración " +
                    "los guardas únicamente tú, como archivos .ini en tu propio dispositivo.",
                pt = "O EggSPEED não recolhe, envia nem armazena quaisquer dados pessoais fora do teu telemóvel. A " +
                    "comunicação ocorre apenas localmente através de um cabo USB entre o telemóvel e o controlador Bafang - " +
                    "sem internet, sem análise, sem rastreamento. Os perfis de configuração são guardados apenas por ti, " +
                    "como ficheiros .ini no teu próprio dispositivo.",
                it = "EggSPEED non raccoglie, invia o memorizza alcun dato personale al di fuori del tuo telefono. La " +
                    "comunicazione avviene esclusivamente in locale tramite un cavo USB tra il telefono e il controller " +
                    "Bafang - senza internet, senza analisi, senza tracciamento. I profili di configurazione vengono " +
                    "salvati solo da te, come file .ini sul tuo dispositivo.",
                nl = "EggSPEED verzamelt, verzendt of bewaart geen persoonlijke gegevens buiten je telefoon. " +
                    "Communicatie verloopt uitsluitend lokaal via een USB-kabel tussen de telefoon en de Bafang-controller " +
                    "- geen internet, geen analytics, geen tracking. Configuratieprofielen worden alleen door jou " +
                    "opgeslagen, als .ini-bestanden op je eigen apparaat.",
                sv = "EggSPEED samlar inte in, skickar inte och lagrar inte några personuppgifter utanför " +
                    "din telefon. Kommunikationen sker uteslutande lokalt via en USB-kabel mellan telefonen " +
                    "och Bafang-styrenheten - ingen internetanslutning, ingen analys, ingen spårning. Konfigurationsprofiler " +
                    "sparas endast av dig, som .ini-filer på din egen enhet.",
                cs = "EggSPEED neshromažďuje, neodesílá ani neukládá žádné osobní údaje mimo " +
                    "tvůj telefon. Komunikace probíhá výhradně lokálně přes USB kabel mezi telefonem " +
                    "a řídicí jednotkou Bafang - žádný internet, žádná analytika, žádné sledování. Konfigurační profily " +
                    "ukládáš pouze ty, jako soubory .ini ve vlastním zařízení.",
                sk = "EggSPEED nezhromažďuje, neodosiela ani neukladá žiadne osobné údaje mimo " +
                    "tvojho telefónu. Komunikácia prebieha výlučne lokálne cez USB kábel medzi telefónom " +
                    "a riadiacou jednotkou Bafang - žiadny internet, žiadna analytika, žiadne sledovanie. Konfiguračné profily " +
                    "ukladáš iba ty, ako súbory .ini vo vlastnom zariadení.",
                da = "EggSPEED indsamler, sender eller gemmer ingen personoplysninger uden for " +
                    "din telefon. Kommunikationen foregår udelukkende lokalt via et USB-kabel mellem telefonen " +
                    "og Bafang-controlleren - ingen internetforbindelse, ingen analyse, ingen sporing. Konfigurationsprofiler " +
                    "gemmes kun af dig selv, som .ini-filer på din egen enhed.",
                ru = "EggSPEED не собирает, не отправляет и не хранит никаких персональных данных за пределами " +
                    "вашего телефона. Обмен данными происходит исключительно локально по USB-кабелю между телефоном " +
                    "и контроллером Bafang - без интернета, без аналитики, без отслеживания. Профили конфигурации " +
                    "сохраняете только вы сами, в виде файлов .ini на своём устройстве.",
            ),
        ) {}

        ExpandableParamTile(
            label = tr("Warunki korzystania", "Terms of service", de = "Nutzungsbedingungen", fr = "Conditions d'utilisation", es = "Términos de uso", pt = "Termos de utilização", it = "Termini di servizio", nl = "Gebruiksvoorwaarden", sv = "Användarvillkor", cs = "Podmínky používání", sk = "Podmienky používania", da = "Servicevilkår", ru = "Условия использования"),
            valueLabel = "",
            descriptionColor = Tokens.Red,
            description = tr(
                "EggSPEED to program hobbystyczny, przeznaczony dla świadomych entuzjastów, którzy wiedzą jak " +
                    "programować swój sterownik Bafang - służy wyłącznie do odczytu konfiguracji oraz bezpiecznego " +
                    "sterowania (poziom wspomagania, światło) sterownikiem Bafang BBS01/BBS02/BBSHD przez kabel USB. " +
                    "Korzystając z aplikacji ponosisz pełną odpowiedzialność za sposób jej używania oraz za " +
                    "konfigurację i legalność użytkowania swojego roweru elektrycznego, w tym zgodność z lokalnymi " +
                    "przepisami. Aplikacja dostarczana jest \"tak jak jest\", bez żadnej gwarancji nieprzerwanego " +
                    "lub bezbłędnego działania. Twórca nie ponosi żadnej odpowiedzialności za szkody wynikłe z " +
                    "używania aplikacji - używasz jej na własną odpowiedzialność.",
                "EggSPEED is a hobby program, built for informed enthusiasts who know how to program their " +
                    "Bafang controller - it is intended solely for reading the configuration of, and safely " +
                    "controlling (assist level, light), a Bafang BBS01/BBS02/BBSHD controller over a USB cable. By " +
                    "using the app you are fully responsible for how you use it and for the configuration and " +
                    "legal use of your e-bike, including compliance with local regulations. The app is provided " +
                    "\"as is\", without any warranty of uninterrupted or error-free operation. The developer bears " +
                    "no responsibility whatsoever for damage resulting from using the app - you use it at your " +
                    "own risk.",
                de = "EggSPEED ist ein Hobbyprogramm für informierte Enthusiasten, die wissen, wie man ihr " +
                    "Bafang-Steuergerät programmiert - es dient ausschließlich dem Auslesen der Konfiguration und der sicheren " +
                    "Steuerung (Unterstützungsstufe, Licht) eines Bafang BBS01/BBS02/BBSHD-Steuergeräts über ein USB-Kabel. " +
                    "Durch die Nutzung der App trägst du die volle Verantwortung für die Art der Nutzung sowie für die " +
                    "Konfiguration und die legale Nutzung deines E-Bikes, einschließlich der Einhaltung örtlicher " +
                    "Vorschriften. Die App wird \"wie besehen\" bereitgestellt, ohne jegliche Garantie für unterbrechungsfreien " +
                    "oder fehlerfreien Betrieb. Der Entwickler übernimmt keinerlei Haftung für Schäden, die aus der " +
                    "Nutzung der App entstehen - du nutzt sie auf eigenes Risiko.",
                fr = "EggSPEED est un programme amateur, conçu pour des passionnés avertis sachant programmer leur " +
                    "contrôleur Bafang - destiné uniquement à la lecture de la configuration et au contrôle sûr " +
                    "(niveau d'assistance, lumière) d'un contrôleur Bafang BBS01/BBS02/BBSHD via un câble USB. En " +
                    "utilisant l'application, vous assumez l'entière responsabilité de son usage ainsi que de la " +
                    "configuration et de la légalité d'utilisation de votre vélo électrique, y compris la conformité " +
                    "aux réglementations locales. L'application est fournie \"telle quelle\", sans aucune garantie de " +
                    "fonctionnement ininterrompu ou sans erreur. Le développeur décline toute responsabilité pour les " +
                    "dommages résultant de l'utilisation de l'application - vous l'utilisez à vos propres risques.",
                es = "EggSPEED es un programa de aficionado, pensado para entusiastas informados que saben cómo " +
                    "programar su controlador Bafang - sirve únicamente para leer la configuración y controlar de forma " +
                    "segura (nivel de asistencia, luz) un controlador Bafang BBS01/BBS02/BBSHD mediante un cable USB. Al " +
                    "usar la app asumes toda la responsabilidad sobre su uso y sobre la configuración y legalidad del uso " +
                    "de tu bicicleta eléctrica, incluido el cumplimiento de la normativa local. La app se ofrece " +
                    "\"tal cual\", sin garantía alguna de funcionamiento ininterrumpido o libre de errores. El desarrollador " +
                    "no asume responsabilidad alguna por daños derivados del uso de la app - la usas bajo tu propio riesgo.",
                pt = "O EggSPEED é um programa de hobby, criado para entusiastas informados que sabem programar o seu " +
                    "controlador Bafang - destina-se exclusivamente à leitura da configuração e ao controlo seguro (nível " +
                    "de assistência, luz) de um controlador Bafang BBS01/BBS02/BBSHD através de um cabo USB. Ao usar a " +
                    "aplicação, assumes total responsabilidade pela forma como a utilizas e pela configuração e " +
                    "legalidade da utilização da tua bicicleta elétrica, incluindo o cumprimento da regulamentação " +
                    "local. A aplicação é fornecida \"tal como está\", sem qualquer garantia de funcionamento " +
                    "ininterrupto ou isento de erros. O criador não assume qualquer responsabilidade por danos " +
                    "resultantes da utilização da aplicação - utilizas-a por tua conta e risco.",
                it = "EggSPEED è un programma amatoriale, pensato per appassionati informati che sanno come " +
                    "programmare il proprio controller Bafang - serve esclusivamente per leggere la configurazione e " +
                    "controllare in sicurezza (livello di assistenza, luce) un controller Bafang BBS01/BBS02/BBSHD " +
                    "tramite cavo USB. Utilizzando l'app assumi la piena responsabilità del modo in cui la usi e della " +
                    "configurazione e legalità dell'uso della tua e-bike, inclusa la conformità alle normative locali. " +
                    "L'app viene fornita \"così com'è\", senza alcuna garanzia di funzionamento ininterrotto o privo di " +
                    "errori. Lo sviluppatore non si assume alcuna responsabilità per danni derivanti dall'uso dell'app " +
                    "- la usi a tuo rischio.",
                nl = "EggSPEED is een hobbyprogramma, gemaakt voor goed geïnformeerde liefhebbers die weten hoe ze hun " +
                    "Bafang-controller moeten programmeren - het dient uitsluitend om de configuratie te lezen en veilig " +
                    "(ondersteuningsniveau, licht) een Bafang BBS01/BBS02/BBSHD-controller via een USB-kabel te bedienen. " +
                    "Door de app te gebruiken ben je volledig verantwoordelijk voor de manier waarop je deze gebruikt en " +
                    "voor de configuratie en wettelijke rijgeschiktheid van je e-bike, inclusief naleving van lokale " +
                    "regelgeving. De app wordt \"zoals ze is\" geleverd, zonder enige garantie op ononderbroken of " +
                    "foutloze werking. De ontwikkelaar aanvaardt geen enkele aansprakelijkheid voor schade als gevolg " +
                    "van het gebruik van de app - je gebruikt haar op eigen risico.",
                sv = "EggSPEED är ett hobbyprogram, byggt för medvetna entusiaster som vet hur man programmerar sin " +
                    "Bafang-styrenhet - det är avsett enbart för att läsa av konfigurationen av och säkert styra " +
                    "(stödnivå, ljus) en Bafang BBS01/BBS02/BBSHD-styrenhet via en USB-kabel. Genom att använda appen " +
                    "är du fullt ansvarig för hur du använder den samt för konfigurationen och den lagliga användningen " +
                    "av din elcykel, inklusive efterlevnad av lokala föreskrifter. Appen tillhandahålls \"i befintligt " +
                    "skick\", utan någon garanti för oavbruten eller felfri drift. Utvecklaren bär inget som helst " +
                    "ansvar för skador som uppstår vid användning av appen - du använder den på egen risk.",
                cs = "EggSPEED je hobby program určený pro informované nadšence, kteří vědí, jak naprogramovat svou " +
                    "řídicí jednotku Bafang - slouží výhradně ke čtení konfigurace a bezpečnému ovládání (úroveň " +
                    "asistence, světlo) řídicí jednotky Bafang BBS01/BBS02/BBSHD přes USB kabel. Používáním aplikace " +
                    "neseš plnou odpovědnost za způsob jejího použití a za konfiguraci a legální provoz svého " +
                    "elektrokola, včetně souladu s místními předpisy. Aplikace je poskytována \"tak, jak je\", bez " +
                    "jakékoli záruky nepřerušovaného nebo bezchybného provozu. Vývojář nenese žádnou odpovědnost za " +
                    "škody vzniklé používáním aplikace - používáš ji na vlastní riziko.",
                sk = "EggSPEED je hobby program určený pre informovaných nadšencov, ktorí vedia, ako naprogramovať " +
                    "svoju riadiacu jednotku Bafang - slúži výlučne na čítanie konfigurácie a bezpečné ovládanie " +
                    "(úroveň asistencie, svetlo) riadiacej jednotky Bafang BBS01/BBS02/BBSHD cez USB kábel. Používaním " +
                    "aplikácie nesieš plnú zodpovednosť za spôsob jej používania a za konfiguráciu a legálne používanie " +
                    "svojho elektrobicykla, vrátane súladu s miestnymi predpismi. Aplikácia sa poskytuje \"tak, ako je\", " +
                    "bez akejkoľvek záruky neprerušovanej alebo bezchybnej prevádzky. Vývojár nenesie žiadnu " +
                    "zodpovednosť za škody vzniknuté používaním aplikácie - používaš ju na vlastné riziko.",
                da = "EggSPEED er et hobbyprogram bygget til informerede entusiaster, der ved, hvordan man " +
                    "programmerer deres Bafang-controller - det er udelukkende beregnet til at læse konfigurationen af " +
                    "og sikkert styre (understøttelsesniveau, lys) en Bafang BBS01/BBS02/BBSHD-controller via et " +
                    "USB-kabel. Ved at bruge appen er du fuldt ansvarlig for, hvordan du bruger den, samt for " +
                    "konfigurationen og den lovlige brug af din el-cykel, herunder overholdelse af lokale regler. Appen " +
                    "leveres \"som den er\", uden nogen garanti for uafbrudt eller fejlfri drift. Udvikleren påtager sig " +
                    "intet ansvar for skader som følge af brug af appen - du bruger den på eget ansvar.",
                ru = "EggSPEED - это любительская программа, созданная для осведомлённых энтузиастов, умеющих " +
                    "программировать свой контроллер Bafang - она предназначена исключительно для чтения " +
                    "конфигурации и безопасного управления (уровень помощи, свет) контроллером Bafang " +
                    "BBS01/BBS02/BBSHD через USB-кабель. Используя приложение, вы несёте полную ответственность за " +
                    "то, как вы его используете, а также за конфигурацию и законность использования вашего " +
                    "электровелосипеда, включая соответствие местным нормам. Приложение предоставляется \"как есть\", " +
                    "без каких-либо гарантий бесперебойной или безошибочной работы. Разработчик не несёт никакой " +
                    "ответственности за ущерб, возникший в результате использования приложения - вы используете его " +
                    "на свой страх и риск.",
            ),
        ) {}

        ContactExpandableTile(
            label = tr("Kontakt / Zgłoś błąd / Propozycja funkcji", "Contact / Report bug / Feature request", de = "Kontakt / Fehler melden / Funktionswunsch", fr = "Contact / Signaler un bug / Proposer une fonctionnalité", es = "Contacto / Reportar error / Sugerir función", pt = "Contacto / Reportar erro / Sugerir função", it = "Contatto / Segnala bug / Richiedi funzione", nl = "Contact / Bug melden / Functieverzoek", sv = "Kontakt / Rapportera bugg / Funktionsförslag", cs = "Kontakt / Nahlásit chybu / Návrh funkce", sk = "Kontakt / Nahlásiť chybu / Návrh funkcie", da = "Kontakt / Rapportér fejl / Funktionsforslag", ru = "Контакты / Сообщить об ошибке / Предложить функцию"),
            body = tr(
                "Znalazłeś błąd? Masz pomysł na nową funkcję? Chcesz się po prostu przywitać? Wypełnij krótki " +
                    "formularz - każda wiadomość jest czytana osobiście.",
                "Found a bug or have an idea for a new feature? Just want to say hi? Fill out a short form - " +
                    "every message is read personally.",
                de = "Einen Fehler gefunden? Eine Idee für eine neue Funktion? Möchtest du einfach nur Hallo sagen? Fülle das " +
                    "kurze Formular aus - jede Nachricht wird persönlich gelesen.",
                fr = "Vous avez trouvé un bug ou une idée de nouvelle fonctionnalité ? Envie de dire bonjour ? Remplissez ce " +
                    "court formulaire - chaque message est lu personnellement.",
                es = "¿Encontraste un error o tienes una idea para una nueva función? ¿Solo quieres saludar? Rellena este " +
                    "breve formulario - cada mensaje se lee personalmente.",
                pt = "Encontraste um erro? Tens uma ideia para uma nova função? Só queres dizer olá? Preenche um pequeno " +
                    "formulário - cada mensagem é lida pessoalmente.",
                it = "Hai trovato un bug o hai un'idea per una nuova funzione? Vuoi solo salutare? Compila un breve " +
                    "modulo - ogni messaggio viene letto personalmente.",
                nl = "Een bug gevonden of een idee voor een nieuwe functie? Wil je gewoon even hallo zeggen? Vul een " +
                    "kort formulier in - elk bericht wordt persoonlijk gelezen.",
                sv = "Har du hittat en bugg eller har en idé till en ny funktion? Vill du bara säga hej? Fyll i ett " +
                    "kort formulär - varje meddelande läses personligen.",
                cs = "Našel jsi chybu nebo máš nápad na novou funkci? Chceš prostě jen pozdravit? Vyplň krátký " +
                    "formulář - každá zpráva je čtena osobně.",
                sk = "Našiel si chybu alebo máš nápad na novú funkciu? Chceš sa jednoducho pozdraviť? Vyplň krátky " +
                    "formulár - každá správa je čítaná osobne.",
                da = "Fundet en fejl eller har du en idé til en ny funktion? Vil du bare sige hej? Udfyld en kort " +
                    "formular - hver besked bliver læst personligt.",
                ru = "Нашли ошибку или есть идея новой функции? Просто хотите поздороваться? Заполните короткую " +
                    "форму - каждое сообщение читается лично.",
            ),
            linkLabel = tr(
                "Otwórz formularz zgłoszeniowy →", "Open support form →",
                de = "Formular öffnen →", fr = "Ouvrir le formulaire →", es = "Abrir el formulario →",
                pt = "Abrir formulário de contacto →", it = "Apri modulo di supporto →", nl = "Contactformulier openen →",
                sv = "Öppna supportformulär →", cs = "Otevřít formulář podpory →", sk = "Otvoriť formulár podpory →", da = "Åbn supportformular →", ru = "Открыть форму поддержки →",
            ),
            onLinkClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(SUPPORT_FORM_URL))
                runCatching { context.startActivity(intent) }
            },
        )
        Spacer(Modifier.height(8.dp))
    }
}

/**
 * Wariant [ExpandableParamTile] dla sekcji Kontakt - link do formularza jest częścią rozwijanej
 * treści (widoczny dopiero po kliknięciu kafelki), a nie stałym elementem, żeby użytkownik
 * najpierw przeczytał, do czego formularz służy.
 */
@Composable
private fun ContactExpandableTile(label: String, body: String, linkLabel: String, onLinkClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    TokenCard(
        modifier = Modifier.clickable { expanded = !expanded },
        contentPadding = 6.dp,
        borderColor = Tokens.WhiteBorder,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                label.uppercase(),
                fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 11.sp,
                letterSpacing = 1.sp, color = Tokens.TextPrimary, modifier = Modifier.weight(1f),
            )
            Text(if (expanded) "▲" else "▼", fontFamily = Manrope, fontSize = 16.sp, color = Tokens.Emerald)
        }
        if (expanded) {
            Spacer(Modifier.height(6.dp))
            Text(body, fontFamily = Manrope, fontSize = 11.sp, lineHeight = 15.sp, color = Tokens.TextPrimary)
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLinkClick() },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(linkLabel, fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Tokens.Blue)
            }
        }
    }
}

/** Próbuje otworzyć wpis appki w Sklepie Play przez samą appkę Sklepu (`market://`), a jeśli jej brak - w przeglądarce. */
private fun openPlayStoreListing(context: Context) {
    val pkg = context.packageName
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")).apply {
                setPackage("com.android.vending")
            },
        )
    } catch (e: ActivityNotFoundException) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg")))
        }
    }
}

/** Klikalny wiersz akcji (ikona + etykieta + strzałka) - dla pozycji menu prowadzących poza appkę (np. do Sklepu Play). */
@Composable
private fun ActionRow(icon: String, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(icon, fontSize = 16.sp, modifier = Modifier.width(24.dp))
        Text(label, fontFamily = Manrope, fontSize = 14.sp, color = Tokens.TextPrimary, modifier = Modifier.weight(1f))
        Text("→", fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Tokens.Blue)
    }
}

@Composable
private fun InfoRow(label: String, value: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable { onClick() } else it }
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontFamily = Manrope, fontSize = 14.sp, color = Tokens.TextPrimary, modifier = Modifier.weight(1f))
        Text(
            value,
            fontFamily = Sora, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
            color = if (onClick != null) Tokens.Blue else Tokens.TextPrimary,
        )
    }
}
