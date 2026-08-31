package com.bafspeed.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bafspeed.app.UiState
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.protocol.BbsFwController
import com.bafspeed.app.ui.components.MicroLabel
import com.bafspeed.app.ui.components.PreviewBanner
import com.bafspeed.app.ui.components.TokenCard
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens

/**
 * Odpowiednik [BafangTypeScreen] dla bbs-fw - bbs-fw nie ma bloku GEN (producent/model), tylko
 * OPCODE_READ_FW_VERSION: wersja firmware, wersja formatu konfiguracji i numeryczny typ sterownika.
 * Wersja configu musi się zgadzać z tym, co zna ta apka (patrz [com.bafspeed.app.protocol.BbsFwCommands.CONFIG_VERSION]) -
 * inaczej odczyt/zapis pełnej konfiguracji jest zablokowany (patrz AppViewModel.handleBbsFwConfigFrame).
 */
@Composable
fun BbsFwInfoScreen(state: UiState) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Tokens.Bg)
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = 16.dp)),
    ) {
        state.bbsFwVersion?.let { v ->
            MicroLabel("bbs-fw")
            Spacer(Modifier.height(6.dp))
            TokenCard(borderColor = Tokens.WhiteBorder) {
                ParamRow(tr("Wersja firmware", "Firmware version", de = "Firmware-Version", fr = "Version du firmware", es = "Versión de firmware", pt = "Versão de firmware", it = "Versione firmware", nl = "Firmwareversie", sv = "Firmwareversion", cs = "Verze firmwaru", sk = "Verzia firmvéru"), v.versionLabel)
                ParamRow(tr("Wersja formatu konfiguracji", "Config format version", de = "Konfigurationsformat-Version", fr = "Version du format de configuration", es = "Versión del formato de configuración", pt = "Versão do formato de configuração", it = "Versione formato configurazione", nl = "Configuratieformaat-versie", sv = "Konfigurationsformatversion", cs = "Verze formátu konfigurace", sk = "Verzia formátu konfigurácie"), v.configVersion.toString())
                ParamRow(tr("Typ sterownika", "Controller type", de = "Steuergerätetyp", fr = "Type de contrôleur", es = "Tipo de controlador", pt = "Tipo de controlador", it = "Tipo di controller", nl = "Controllertype", sv = "Kontrollertyp", cs = "Typ řadiče", sk = "Typ radiča"), "${BbsFwController.name(v.ctrlType)} (${v.ctrlType})", last = true)
            }
            Spacer(Modifier.height(10.dp))
            PreviewBanner(
                tr(
                    "Ta apka zna format konfiguracji w wersji 5. Jeśli powyższa wersja formatu jest inna, odczyt/zapis " +
                        "pełnej konfiguracji jest zablokowany (bbs-fw zmienił układ danych między wersjami) - tryb wyświetlacza " +
                        "(Kokpit/Diagnostyka) nadal działa niezależnie od tego.",
                    "This app knows config format version 5. If the version above differs, reading/writing the full " +
                        "configuration is blocked (bbs-fw changed the data layout between versions) - display mode " +
                        "(Cockpit/Diagnostics) still works regardless.",
                    de = "Diese App kennt das Konfigurationsformat Version 5. Wenn die obige Version abweicht, ist das " +
                        "Lesen/Schreiben der vollständigen Konfiguration blockiert (bbs-fw hat das Datenlayout zwischen " +
                        "den Versionen geändert) - der Anzeigemodus (Cockpit/Diagnose) funktioniert davon unabhängig weiterhin.",
                    fr = "Cette application connaît le format de configuration version 5. Si la version ci-dessus " +
                        "diffère, la lecture/écriture de la configuration complète est bloquée (bbs-fw a modifié la " +
                        "disposition des données entre les versions) - le mode d'affichage (Cockpit/Diagnostic) continue " +
                        "de fonctionner malgré tout.",
                    es = "Esta app conoce el formato de configuración versión 5. Si la versión anterior es distinta, " +
                        "la lectura/escritura de la configuración completa queda bloqueada (bbs-fw cambió la disposición " +
                        "de los datos entre versiones) - el modo de visualización (Cockpit/Diagnóstico) sigue " +
                        "funcionando de todos modos.",
                    pt = "Esta app conhece o formato de configuração versão 5. Se a versão acima for diferente, a " +
                        "leitura/escrita da configuração completa fica bloqueada (o bbs-fw alterou a disposição dos dados " +
                        "entre versões) - o modo de visualização (Cockpit/Diagnóstico) continua a funcionar de qualquer forma.",
                    it = "Questa app conosce il formato di configurazione versione 5. Se la versione sopra è diversa, " +
                        "la lettura/scrittura della configurazione completa viene bloccata (bbs-fw ha modificato la " +
                        "disposizione dei dati tra le versioni) - la modalità display (Cockpit/Diagnostica) continua " +
                        "comunque a funzionare.",
                    nl = "Deze app kent configuratieformaat versie 5. Als de bovenstaande versie afwijkt, wordt het " +
                        "lezen/schrijven van de volledige configuratie geblokkeerd (bbs-fw heeft de gegevensindeling " +
                        "tussen versies gewijzigd) - de displaymodus (Cockpit/Diagnostiek) blijft hoe dan ook werken.",
                    sv = "Den här appen känner till konfigurationsformat version 5. Om versionen ovan skiljer sig " +
                        "blockeras läsning/skrivning av hela konfigurationen (bbs-fw ändrade dataformatet " +
                        "mellan versioner) - visningsläget (Cockpit/Diagnostik) fortsätter ändå att fungera.",
                    cs = "Tato aplikace zná formát konfigurace verze 5. Pokud se výše uvedená verze liší, čtení/zápis " +
                        "celé konfigurace je zablokováno (bbs-fw změnil rozložení dat mezi verzemi) - zobrazovací " +
                        "režim (Cockpit/Diagnostika) přesto funguje dál.",
                    sk = "Táto aplikácia pozná formát konfigurácie verzie 5. Ak sa vyššie uvedená verzia líši, " +
                        "čítanie/zápis celej konfigurácie je zablokované (bbs-fw zmenil usporiadanie dát medzi " +
                        "verziami) - zobrazovací režim (Cockpit/Diagnostika) napriek tomu funguje ďalej.",
                ),
            )
        } ?: PreviewBanner(
            tr(
                "Połącz się ze sterownikiem, żeby zobaczyć tutaj wersję bbs-fw i typ sterownika.",
                "Connect to your controller to see the bbs-fw version and controller type here.",
                de = "Verbinde dich mit deinem Steuergerät, um hier die bbs-fw-Version und den Steuergerätetyp zu sehen.",
                fr = "Connectez-vous à votre contrôleur pour voir ici la version bbs-fw et le type de contrôleur.",
                es = "Conéctate a tu controlador para ver aquí la versión de bbs-fw y el tipo de controlador.",
                pt = "Liga-te ao teu controlador para ver aqui a versão do bbs-fw e o tipo de controlador.",
                it = "Connettiti al tuo controller per vedere qui la versione bbs-fw e il tipo di controller.",
                nl = "Verbind met je controller om hier de bbs-fw-versie en het controllertype te zien.",
                sv = "Anslut till din styrenhet för att se bbs-fw-versionen och kontrollertypen här.",
                cs = "Připoj se k řadiči, abys zde viděl verzi bbs-fw a typ řadiče.",
                sk = "Pripoj sa k radiču, aby si tu videl verziu bbs-fw a typ radiča.",
            ),
        )
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
