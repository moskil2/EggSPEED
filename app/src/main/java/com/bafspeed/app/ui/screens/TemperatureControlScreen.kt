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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bafspeed.app.UiState
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.ui.components.ExpandableParamTile
import com.bafspeed.app.ui.components.FlankedSlider
import com.bafspeed.app.ui.components.MicroLabel
import com.bafspeed.app.ui.components.TokenCard
import com.bafspeed.app.ui.components.ToggleRow
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Tokens

/**
 * Zakładka sterująca kafelkiem Tc (temp. sterownika) na Kokpicie - tylko bbs-fw (patrz MainActivity.kt
 * BBS_FW_ONLY_SCREENS). Tm/rejestr 0x21 zawsze zwraca 0 na bbs-fw (potwierdzone w źródłach,
 * PROTOKOL_BBSFW.md sekcja 5) - firmware temperatureSensorMode (zakładka System) reguluje TYLKO
 * własną reakcję termiczną firmware, nie to, które rejestry telemetryczne są zasilane, więc
 * apka celowo pokazuje tylko jeden, realnie działający odczyt zamiast dwóch.
 */
@Composable
fun TemperatureControlScreen(
    state: UiState,
    onShowChange: (Boolean) -> Unit,
    onWarningChange: (Int) -> Unit,
    onAlarmChange: (Int) -> Unit,
    onAlarmSoundChange: (Boolean) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Tokens.Bg)
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = 16.dp)),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        MicroLabel(tr("Wyświetlanie na Kokpicie", "Cockpit display", de = "Cockpit-Anzeige", fr = "Affichage Cockpit", es = "Visualización en Cockpit", pt = "Visualização no Cockpit", it = "Visualizzazione Cockpit", nl = "Cockpit-weergave", sv = "Cockpit-visning", cs = "Zobrazení Cockpitu", sk = "Zobrazenie Cockpitu", da = "Cockpit-visning", ru = "Отображение на Кокпите"))
        TokenCard(borderColor = Tokens.WhiteBorder) {
            ToggleRow(
                tr("Temp. sterownika (Tc)", "Controller temp. (Tc)", de = "Steuergerätetemp. (Tc)", fr = "Temp. contrôleur (Tc)", es = "Temp. del controlador (Tc)", pt = "Temp. do controlador (Tc)", it = "Temp. controller (Tc)", nl = "Controllertemp. (Tc)", sv = "Styrenhetens temp. (Tc)", cs = "Teplota řadiče (Tc)", sk = "Teplota radiča (Tc)", da = "Controllertemp. (Tc)", ru = "Темп. контроллера (Tc)"),
                state.showTempOnCockpit, onShowChange, accent = Tokens.Amber,
                description = tr(
                    "Pokazuje kafelek Tc po lewej stronie Kokpitu, mniej więcej na wysokości odczytu mocy.",
                    "Shows the Tc tile on the left side of the Cockpit, roughly at the power reading's height.",
                    de = "Zeigt die Tc-Kachel auf der linken Seite des Cockpits, ungefähr auf Höhe der Leistungsanzeige.",
                    fr = "Affiche la tuile Tc sur le côté gauche du Cockpit, à peu près à la hauteur de l'indicateur de puissance.",
                    es = "Muestra el mosaico Tc en el lado izquierdo del Cockpit, aproximadamente a la altura de la lectura de potencia.",
                    pt = "Mostra o bloco Tc no lado esquerdo do Cockpit, aproximadamente à altura da leitura de potência.",
                    it = "Mostra il riquadro Tc sul lato sinistro del Cockpit, all'incirca all'altezza della lettura di potenza.",
                    nl = "Toont de Tc-tegel aan de linkerkant van de Cockpit, ongeveer op de hoogte van de vermogensaflezing.",
                    sv = "Visar Tc-rutan på vänster sida av Cockpit, ungefär i höjd med effektavläsningen.",
                    cs = "Zobrazuje dlaždici Tc na levé straně Cockpitu, přibližně ve výšce odečtu výkonu.",
                    sk = "Zobrazuje dlaždicu Tc na ľavej strane Cockpitu, približne vo výške odčítania výkonu.", da = "Viser Tc-feltet i venstre side af Cockpit, cirka i samme højde som effektaflæsningen.", ru = "Показывает плитку Tc в левой части Кокпита, примерно на уровне показания мощности.",
                ),
            )
        }

        MicroLabel(tr("Reakcja na przekroczenie", "Reaction on threshold exceeded", de = "Reaktion bei Grenzwertüberschreitung", fr = "Réaction au dépassement de seuil", es = "Reacción al superar el umbral", pt = "Reação ao ultrapassar o limite", it = "Reazione al superamento della soglia", nl = "Reactie bij overschrijding drempel", sv = "Reaktion vid överskriden gräns", cs = "Reakce na překročení prahu", sk = "Reakcia na prekročenie prahu", da = "Reaktion ved overskredet tærskel", ru = "Реакция при превышении порога"))
        ExpandableParamTile(
            label = "Warning",
            valueLabel = "${state.tempWarningC}°C",
            description = tr(
                "Niższy próg - po przekroczeniu kafelek Tc podświetla się na pomarańczowo (bez migania).",
                "Lower threshold - when exceeded, the Tc tile highlights orange (no blinking).",
                de = "Niedrigerer Schwellenwert - bei Überschreitung leuchtet die Tc-Kachel orange auf (ohne Blinken).",
                fr = "Seuil inférieur - en cas de dépassement, la tuile Tc s'allume en orange (sans clignoter).",
                es = "Umbral inferior - al superarlo, el mosaico Tc se resalta en naranja (sin parpadear).",
                pt = "Limite inferior - ao ser ultrapassado, o bloco Tc realça-se a laranja (sem piscar).",
                it = "Soglia inferiore - al superamento, il riquadro Tc si illumina in arancione (senza lampeggiare).",
                nl = "Lagere drempel - bij overschrijding licht de Tc-tegel oranje op (zonder te knipperen).",
                sv = "Lägre tröskel - vid överskridning lyser Tc-rutan orange (utan att blinka).",
                cs = "Nižší práh - při překročení se dlaždice Tc rozsvítí oranžově (bez blikání).",
                sk = "Nižší prah - pri prekročení sa dlaždica Tc rozsvieti oranžovo (bez blikania).", da = "Nedre tærskel - ved overskridelse lyser Tc-feltet orange (uden at blinke).", ru = "Нижний порог - при превышении плитка Tc подсвечивается оранжевым (без мигания).",
            ),
        ) {
            FlankedSlider(value = state.tempWarningC, range = 30..150, accent = Tokens.Amber, onValueChange = onWarningChange)
        }
        ExpandableParamTile(
            label = "Alarm",
            valueLabel = "${state.tempAlarmC}°C",
            description = tr(
                "Wyższy próg - po przekroczeniu kafelek Tc miga na czerwono i (jeśli włączone poniżej) gra " +
                    "jednorazowy dźwięk. Dźwięk włącza się ponownie dopiero po spadku temperatury z powrotem " +
                    "poniżej tego progu.",
                "Higher threshold - when exceeded, the Tc tile blinks red and (if enabled below) plays a " +
                    "one-time sound. The sound re-arms only after the temperature drops back below this threshold.",
                de = "Höherer Schwellenwert - bei Überschreitung blinkt die Tc-Kachel rot und spielt (falls unten " +
                    "aktiviert) einmalig einen Ton ab. Der Ton wird erst wieder scharf, nachdem die Temperatur unter " +
                    "diesen Schwellenwert zurückgefallen ist.",
                fr = "Seuil supérieur - en cas de dépassement, la tuile Tc clignote en rouge et (si activé ci-dessous) " +
                    "joue un son unique. Le son ne se réarme qu'après que la température soit repassée sous ce seuil.",
                es = "Umbral superior - al superarlo, el mosaico Tc parpadea en rojo y (si está activado más abajo) " +
                    "reproduce un sonido único. El sonido vuelve a activarse solo después de que la temperatura baje de " +
                    "nuevo por debajo de este umbral.",
                pt = "Limite superior - ao ser ultrapassado, o bloco Tc pisca a vermelho e (se ativado abaixo) " +
                    "reproduz um som único. O som só volta a armar depois de a temperatura descer novamente abaixo " +
                    "deste limite.",
                it = "Soglia superiore - al superamento, il riquadro Tc lampeggia in rosso e (se abilitato sotto) " +
                    "riproduce un suono una tantum. Il suono si riattiva solo dopo che la temperatura scende di nuovo " +
                    "sotto questa soglia.",
                nl = "Hogere drempel - bij overschrijding knippert de Tc-tegel rood en speelt (indien hieronder " +
                    "ingeschakeld) eenmalig een geluid af. Het geluid wordt pas weer geactiveerd nadat de temperatuur " +
                    "weer onder deze drempel is gezakt.",
                sv = "Högre tröskel - vid överskridning blinkar Tc-rutan rött och spelar (om aktiverat nedan) " +
                    "ett engångsljud. Ljudet återaktiveras först när temperaturen sjunker under " +
                    "denna tröskel igen.",
                cs = "Vyšší práh - při překročení dlaždice Tc bliká červeně a (pokud je zapnuto níže) přehraje " +
                    "jednorázový zvuk. Zvuk se znovu aktivuje až po poklesu teploty zpět " +
                    "pod tento práh.",
                sk = "Vyšší prah - pri prekročení dlaždica Tc bliká na červeno a (ak je zapnuté nižšie) prehrá " +
                    "jednorazový zvuk. Zvuk sa znova aktivuje až po poklese teploty späť " +
                    "pod tento prah.",
                da = "Højere tærskel - ved overskridelse blinker Tc-feltet rødt og afspiller (hvis aktiveret " +
                    "nedenfor) en engangslyd. Lyden genaktiveres først, når temperaturen falder tilbage " +
                    "under denne tærskel.",
                ru = "Верхний порог - при превышении плитка Tc мигает красным и (если включено ниже) " +
                    "воспроизводит однократный звук. Звук снова активируется только после того, как " +
                    "температура опустится обратно ниже этого порога.",
            ),
        ) {
            FlankedSlider(value = state.tempAlarmC, range = 30..150, accent = Tokens.Red, onValueChange = onAlarmChange)
        }
        TokenCard(borderColor = Tokens.WhiteBorder) {
            ToggleRow(
                tr("Dźwięk przy Alarm", "Sound on Alarm", de = "Ton bei Alarm", fr = "Son à l'Alarm", es = "Sonido en Alarm", pt = "Som no Alarm", it = "Suono su Alarm", nl = "Geluid bij Alarm", sv = "Ljud vid Alarm", cs = "Zvuk při Alarm", sk = "Zvuk pri Alarm", da = "Lyd ved Alarm", ru = "Звук при Alarm"),
                state.tempAlarmSoundEnabled, onAlarmSoundChange, accent = Tokens.Red,
                description = tr(
                    "Jednorazowy sygnał dźwiękowy przy przekroczeniu progu Alarm.",
                    "A one-time beep when the Alarm threshold is exceeded.",
                    de = "Ein einmaliger Signalton bei Überschreitung des Alarm-Schwellenwerts.",
                    fr = "Un bip unique lorsque le seuil Alarm est dépassé.",
                    es = "Un pitido único cuando se supera el umbral de Alarm.",
                    pt = "Um sinal sonoro único quando o limite Alarm é ultrapassado.",
                    it = "Un segnale acustico una tantum quando viene superata la soglia Alarm.",
                    nl = "Eén geluidssignaal wanneer de Alarm-drempel wordt overschreden.",
                    sv = "En engångssignal när Alarm-tröskeln överskrids.",
                    cs = "Jednorázový zvukový signál při překročení prahu Alarm.",
                    sk = "Jednorazový zvukový signál pri prekročení prahu Alarm.", da = "Et enkelt bip, når Alarm-tærsklen overskrides.", ru = "Однократный звуковой сигнал при превышении порога Alarm.",
                ),
            )
        }

        MicroLabel(tr("Czujniki w firmware", "Firmware sensors", de = "Sensoren in der Firmware", fr = "Capteurs du firmware", es = "Sensores del firmware", pt = "Sensores no firmware", it = "Sensori nel firmware", nl = "Sensoren in firmware", sv = "Sensorer i firmware", cs = "Senzory ve firmwaru", sk = "Senzory vo firmvéri", da = "Sensorer i firmware", ru = "Датчики в прошивке"))
        TokenCard(borderColor = Tokens.WhiteBorder) {
            val mode = state.bbsFwConfigOrDefault.temperatureSensorMode
            val modeLabel = listOf(
                tr("Wyłączony", "Disabled", de = "Deaktiviert", fr = "Désactivé", es = "Desactivado", pt = "Desativado", it = "Disattivato", nl = "Uitgeschakeld", sv = "Avstängd", cs = "Vypnuto", sk = "Vypnuté", da = "Deaktiveret", ru = "Отключено"),
                tr("Sterownika", "Controller", de = "Steuergerät", fr = "Contrôleur", es = "Controlador", pt = "Controlador", it = "Controller", nl = "Controller", sv = "Styrenhet", cs = "Řadič", sk = "Radič", da = "Controller", ru = "Контроллер"),
                tr("Silnika", "Motor", de = "Motor", fr = "Moteur", es = "Motor", pt = "Motor", it = "Motore", nl = "Motor", sv = "Motor", cs = "Motor", sk = "Motor", da = "Motor", ru = "Мотор"),
                "All",
            ).getOrElse(mode) { "?" }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    tr("Temperature Sensor (zakładka System)", "Temperature Sensor (System tab)", de = "Temperature Sensor (Tab System)", fr = "Temperature Sensor (onglet Système)", es = "Temperature Sensor (pestaña Sistema)", pt = "Temperature Sensor (separador System)", it = "Temperature Sensor (scheda System)", nl = "Temperature Sensor (tabblad System)", sv = "Temperature Sensor (fliken System)", cs = "Temperature Sensor (karta System)", sk = "Temperature Sensor (karta System)", da = "Temperature Sensor (fanen System)", ru = "Temperature Sensor (вкладка System)"),
                    fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextPrimary, modifier = Modifier.weight(1f),
                )
                Text(modeLabel, fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Tokens.Emerald)
            }
            HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
            Text(
                tr(
                    "To ustawienie steruje WYŁĄCZNIE tym, jak firmware samo reaguje na przegrzanie (np. ogranicza " +
                        "moc) - nie decyduje o tym, co widać tutaj na Kokpicie.",
                    "This setting controls ONLY how the firmware itself reacts to overheating (e.g. limits power) " +
                        "- it does not decide what's shown here on the Cockpit.",
                    de = "Diese Einstellung steuert AUSSCHLIESSLICH, wie die Firmware selbst auf Überhitzung reagiert " +
                        "(z. B. Leistung begrenzt) - sie entscheidet nicht darüber, was hier im Cockpit angezeigt wird.",
                    fr = "Ce réglage contrôle UNIQUEMENT la façon dont le firmware lui-même réagit à la surchauffe " +
                        "(par ex. limite la puissance) - il ne décide pas de ce qui est affiché ici sur le Cockpit.",
                    es = "Este ajuste controla ÚNICAMENTE cómo reacciona el propio firmware al sobrecalentamiento (por " +
                        "ejemplo, limitando la potencia) - no decide lo que se muestra aquí en el Cockpit.",
                    pt = "Esta definição controla APENAS a forma como o próprio firmware reage ao sobreaquecimento " +
                        "(por ex. limita a potência) - não determina o que é mostrado aqui no Cockpit.",
                    it = "Questa impostazione controlla SOLO il modo in cui il firmware stesso reagisce al " +
                        "surriscaldamento (ad es. limita la potenza) - non determina ciò che viene mostrato qui nel Cockpit.",
                    nl = "Deze instelling regelt UITSLUITEND hoe de firmware zelf reageert op oververhitting (bijv. " +
                        "vermogen beperken) - het bepaalt niet wat hier op de Cockpit wordt getoond.",
                    sv = "Denna inställning styr ENDAST hur firmware själv reagerar på överhettning (t.ex. " +
                        "begränsar effekten) - den avgör inte vad som visas här på Cockpit.",
                    cs = "Toto nastavení řídí VÝHRADNĚ to, jak firmware sám reaguje na přehřátí (např. " +
                        "omezuje výkon) - neurčuje, co se zobrazuje zde v Cockpitu.",
                    sk = "Toto nastavenie riadi VÝHRADNE to, ako firmvér sám reaguje na prehriatie (napr. " +
                        "obmedzuje výkon) - neurčuje, čo sa zobrazuje tu v Cockpite.",
                    da = "Denne indstilling styrer UDELUKKENDE, hvordan firmwaren selv reagerer på overophedning " +
                        "(f.eks. begrænser effekten) - den afgør ikke, hvad der vises her på Cockpit.",
                    ru = "Эта настройка управляет ИСКЛЮЧИТЕЛЬНО тем, как сама прошивка реагирует на перегрев " +
                        "(например, ограничивает мощность) - она не определяет, что отображается здесь в Кокпите.",
                ),
                fontFamily = Manrope, fontSize = 11.sp, color = Tokens.TextSecondary,
            )
        }
    }
}
