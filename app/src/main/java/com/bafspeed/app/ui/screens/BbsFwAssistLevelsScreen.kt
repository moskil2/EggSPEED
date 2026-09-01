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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bafspeed.app.UiState
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.protocol.BbsFwAssistBaseType
import com.bafspeed.app.protocol.BbsFwAssistFlags
import com.bafspeed.app.protocol.BbsFwAssistPasVariant
import com.bafspeed.app.ui.components.FlankedSlider
import com.bafspeed.app.ui.components.PreviewBanner
import com.bafspeed.app.ui.components.ReadWriteButtons
import com.bafspeed.app.ui.components.SegmentedControl
import com.bafspeed.app.ui.components.StepBtn
import com.bafspeed.app.ui.components.TelemetryPausedNotice
import com.bafspeed.app.ui.components.TokenCard
import com.bafspeed.app.ui.components.ToggleRow
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens

/** Kolejność WYŚWIETLANIA w oficjalnej apce autora (ConfigurationViewModel.AssistModeSelectOptions) - NIE pokrywa się z kolejnością numeryczną enuma (BrakesOnBoot=13 pokazywane jako 4. pozycja). */
private val ASSIST_MODE_SELECT_DISPLAY_ORDER = listOf(0, 1, 2, 13, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)

private fun stepInOrder(order: List<Int>, current: Int, delta: Int): Int {
    val idx = order.indexOf(current).let { if (it < 0) 0 else it }
    return order[(idx + delta).coerceIn(0, order.lastIndex)]
}

/**
 * Odpowiednik zakładki "Assist Levels" z oficjalnej apki Windows autora bbs-fw
 * (src/tool/View/AssistLevelsView.xaml + AssistLevelPasView/ThrottleView/CruiseView.xaml,
 * ViewModel: AssistLevelViewModel.cs) - te same nazwy pól i ta sama logika Type/Variant → flagi
 * (patrz [com.bafspeed.app.AppViewModel.setBbsFwAssistBaseType]/[com.bafspeed.app.AppViewModel.setBbsFwAssistPasVariant]).
 */
@Composable
fun BbsFwAssistLevelsScreen(
    state: UiState,
    onBaseType: (profile: Int, level: Int, type: Int) -> Unit,
    onPasVariant: (profile: Int, level: Int, variant: Int) -> Unit,
    onTargetCurrent: (profile: Int, level: Int, pct: Int) -> Unit,
    onMaxThrottleCurrent: (profile: Int, level: Int, pct: Int) -> Unit,
    onMaxCadence: (profile: Int, level: Int, pct: Int) -> Unit,
    onMaxSpeed: (profile: Int, level: Int, pct: Int) -> Unit,
    onTorqueFactor: (profile: Int, level: Int, x10: Int) -> Unit,
    onFlag: (profile: Int, level: Int, flag: Int, enabled: Boolean) -> Unit,
    onAssistModeSelect: (Int) -> Unit,
    onAssistStartupLevel: (Int) -> Unit,
    onRead: () -> Unit,
    onWrite: () -> Unit,
    readWriteEnabled: Boolean,
    monitoringActive: Boolean,
) {
    var profile by remember { mutableIntStateOf(0) }
    var selected by remember { mutableIntStateOf(0) }
    val cfg = state.bbsFwConfigOrDefault
    val level = cfg.assistLevel(profile, selected)
    val baseType = level.baseType()
    val pasVariant = level.pasVariant()

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

        // --- Operation Mode Page ---
        Text(tr("Strona trybu pracy", "Operation Mode Page", de = "Betriebsmodus-Seite", fr = "Page de mode de fonctionnement", es = "Página de modo de funcionamiento", pt = "Página de modo de funcionamento", it = "Pagina modalità di funzionamento", nl = "Werkmoduspagina", sv = "Sida för driftläge", cs = "Stránka provozního režimu", sk = "Stránka prevádzkového režimu", da = "Driftstilstand-side", ru = "Страница режима работы"), fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Tokens.TextPrimary)
        SegmentedControl(
            options = listOf("Standard", "Sport"),
            selectedIndex = profile,
            onSelect = { profile = it },
        )

        // --- Poziomy 0-9 ---
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            (0..9).forEach { lvl ->
                val sel = lvl == selected
                Box(
                    Modifier
                        .weight(1f)
                        .background(if (sel) Tokens.Amber else Tokens.Blue, RoundedCornerShape(10.dp))
                        .clickable { selected = lvl }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(lvl.toString(), fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Tokens.OnAccent)
                }
            }
        }

        // --- Level N: Type ---
        TokenCard(borderColor = Tokens.WhiteBorder) {
            Text("${tr("Poziom", "Level", de = "Stufe", fr = "Niveau", es = "Nivel", pt = "Nível", it = "Livello", nl = "Niveau", sv = "Nivå", cs = "Úroveň", sk = "Úroveň", da = "Niveau", ru = "Уровень")} $selected", fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Tokens.TextPrimary)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(tr("Typ:", "Type:", de = "Typ:", fr = "Type :", es = "Tipo:", pt = "Tipo:", it = "Tipo:", nl = "Type:", sv = "Typ:", cs = "Typ:", sk = "Typ:", da = "Type:", ru = "Тип:"), fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextSecondary, modifier = Modifier.weight(1f))
                StepBtn("-", true) { onBaseType(profile, selected, (baseType - 1).coerceIn(0, 3)) }
                Spacer(Modifier.padding(4.dp))
                Text(baseTypeLabel(baseType), fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Tokens.TextPrimary)
                Spacer(Modifier.padding(4.dp))
                StepBtn("+", true) { onBaseType(profile, selected, (baseType + 1).coerceIn(0, 3)) }
            }
        }

        when (baseType) {
            BbsFwAssistBaseType.PAS -> {
                TokenCard(borderColor = Tokens.WhiteBorder) {
                    LabeledStepRow(tr("Wariant:", "Variant:", de = "Variante:", fr = "Variante :", es = "Variante:", pt = "Variante:", it = "Variante:", nl = "Variant:", sv = "Variant:", cs = "Varianta:", sk = "Variant:", da = "Variant:", ru = "Вариант:"), pasVariantLabel(pasVariant), { onPasVariant(profile, selected, (pasVariant - 1).coerceIn(0, 2)) }, { onPasVariant(profile, selected, (pasVariant + 1).coerceIn(0, 2)) })

                    if (pasVariant == BbsFwAssistPasVariant.TORQUE) {
                        HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                        LabeledStepRow(
                            tr("Wzmocnienie momentu:", "Torque Amplification:", de = "Drehmomentverstärkung:", fr = "Amplification du couple :", es = "Amplificación de par:", pt = "Amplificação de torque:", it = "Amplificazione della coppia:", nl = "Koppelversterking:", sv = "Momentförstärkning:", cs = "Zesílení momentu:", sk = "Zosilnenie momentu:", da = "Momentforstærkning:", ru = "Усиление момента:"), "${level.torqueAmplificationFactorX10 / 10.0}×",
                            { onTorqueFactor(profile, selected, level.torqueAmplificationFactorX10 - 1) },
                            { onTorqueFactor(profile, selected, level.torqueAmplificationFactorX10 + 1) },
                        )
                    }

                    HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                    LabeledSliderRow(tr("Maks. prąd (%)", "Max Current (%)", de = "Max. Strom (%)", fr = "Courant max (%)", es = "Corriente máx. (%)", pt = "Corrente máx. (%)", it = "Corrente max (%)", nl = "Max. stroom (%)", sv = "Max ström (%)", cs = "Max. proud (%)", sk = "Max. prúd (%)", da = "Maks. strøm (%)", ru = "Макс. ток (%)"), level.targetCurrentPercent, Tokens.Amber) { onTargetCurrent(profile, selected, it) }
                    HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                    LabeledSliderRow(tr("Maks. kadencja (%)", "Max Cadence (%)", de = "Max. Kadenz (%)", fr = "Cadence max (%)", es = "Cadencia máx. (%)", pt = "Cadência máx. (%)", it = "Cadenza max (%)", nl = "Max. cadans (%)", sv = "Max kadens (%)", cs = "Max. kadence (%)", sk = "Max. kadencia (%)", da = "Maks. kadence (%)", ru = "Макс. каденс (%)"), level.maxCadencePercent, Tokens.Emerald) { onMaxCadence(profile, selected, it) }
                    HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                    LabeledSliderRow(tr("Maks. prędkość (%)", "Max Speed (%)", de = "Max. Geschwindigkeit (%)", fr = "Vitesse max (%)", es = "Velocidad máx. (%)", pt = "Velocidade máx. (%)", it = "Velocità max (%)", nl = "Max. snelheid (%)", sv = "Max hastighet (%)", cs = "Max. rychlost (%)", sk = "Max. rýchlosť (%)", da = "Maks. hastighed (%)", ru = "Макс. скорость (%)"), level.maxSpeedPercent, Tokens.Emerald) { onMaxSpeed(profile, selected, it) }

                    if (pasVariant != BbsFwAssistPasVariant.VARIABLE) {
                        val throttleEnabled = level.hasFlag(BbsFwAssistFlags.THROTTLE)
                        HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                        ToggleRow(tr("Włącz manetkę:", "Enable Throttle:", de = "Gasgriff aktivieren:", fr = "Activer l'accélérateur :", es = "Activar acelerador:", pt = "Ativar acelerador:", it = "Abilita acceleratore:", nl = "Gasgreep inschakelen:", sv = "Aktivera gasreglage:", cs = "Povolit plynovou páčku:", sk = "Povoliť plynovú páčku:", da = "Aktivér gashåndtag:", ru = "Включить газ:"), throttleEnabled, { onFlag(profile, selected, BbsFwAssistFlags.THROTTLE, it) })
                        HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                        Column(Modifier.padding(vertical = 4.dp)) {
                            Text(tr("Nadpisania manetki:", "Throttle Overrides:", de = "Gasgriff-Überschreibungen:", fr = "Substitutions de l'accélérateur :", es = "Anulaciones del acelerador:", pt = "Substituições do acelerador:", it = "Sovrascritture acceleratore:", nl = "Gasgreep-overschrijvingen:", sv = "Åsidosättningar för gasreglage:", cs = "Přepsání plynové páčky:", sk = "Prepísania plynovej páčky:", da = "Gashåndtag-tilsidesættelser:", ru = "Переопределения газа:"), fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextSecondary)
                            Spacer(Modifier.height(6.dp))
                            Row {
                                Box(Modifier.weight(1f)) {
                                    ToggleRow(
                                        tr("Kadencja", "Cadence", de = "Kadenz", fr = "Cadence", es = "Cadencia", pt = "Cadência", it = "Cadenza", nl = "Cadans", sv = "Kadens", cs = "Kadence", sk = "Kadencia", da = "Kadence", ru = "Каденс"), level.hasFlag(BbsFwAssistFlags.OVERRIDE_CADENCE),
                                        { onFlag(profile, selected, BbsFwAssistFlags.OVERRIDE_CADENCE, it) },
                                        enabled = throttleEnabled,
                                    )
                                }
                                Box(Modifier.weight(1f)) {
                                    ToggleRow(
                                        tr("Prędkość", "Speed", de = "Geschwindigkeit", fr = "Vitesse", es = "Velocidad", pt = "Velocidade", it = "Velocità", nl = "Snelheid", sv = "Hastighet", cs = "Rychlost", sk = "Rýchlosť", da = "Hastighed", ru = "Скорость"), level.hasFlag(BbsFwAssistFlags.OVERRIDE_SPEED),
                                        { onFlag(profile, selected, BbsFwAssistFlags.OVERRIDE_SPEED, it) },
                                        enabled = throttleEnabled,
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                        LabeledSliderRow(tr("Maks. prąd manetki (%)", "Max Throttle Current (%)", de = "Max. Gasgriff-Strom (%)", fr = "Courant max accélérateur (%)", es = "Corriente máx. acelerador (%)", pt = "Corrente máx. acelerador (%)", it = "Corrente max acceleratore (%)", nl = "Max. gasgreepstroom (%)", sv = "Max gasreglageström (%)", cs = "Max. proud plynové páčky (%)", sk = "Max. prúd plynovej páčky (%)", da = "Maks. gashåndtagsstrøm (%)", ru = "Макс. ток газа (%)"), level.maxThrottleCurrentPercent, Tokens.Amber, enabled = throttleEnabled) { onMaxThrottleCurrent(profile, selected, it) }
                    }
                }
            }
            BbsFwAssistBaseType.THROTTLE -> {
                TokenCard(borderColor = Tokens.WhiteBorder) {
                    LabeledSliderRow(tr("Maks. prąd (%)", "Max Current (%)", de = "Max. Strom (%)", fr = "Courant max (%)", es = "Corriente máx. (%)", pt = "Corrente máx. (%)", it = "Corrente max (%)", nl = "Max. stroom (%)", sv = "Max ström (%)", cs = "Max. proud (%)", sk = "Max. prúd (%)", da = "Maks. strøm (%)", ru = "Макс. ток (%)"), level.maxThrottleCurrentPercent, Tokens.Amber) { onMaxThrottleCurrent(profile, selected, it) }
                    HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                    LabeledSliderRow(tr("Maks. kadencja (%)", "Max Cadence (%)", de = "Max. Kadenz (%)", fr = "Cadence max (%)", es = "Cadencia máx. (%)", pt = "Cadência máx. (%)", it = "Cadenza max (%)", nl = "Max. cadans (%)", sv = "Max kadens (%)", cs = "Max. kadence (%)", sk = "Max. kadencia (%)", da = "Maks. kadence (%)", ru = "Макс. каденс (%)"), level.maxCadencePercent, Tokens.Emerald) { onMaxCadence(profile, selected, it) }
                    HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                    LabeledSliderRow(tr("Maks. prędkość (%)", "Max Speed (%)", de = "Max. Geschwindigkeit (%)", fr = "Vitesse max (%)", es = "Velocidad máx. (%)", pt = "Velocidade máx. (%)", it = "Velocità max (%)", nl = "Max. snelheid (%)", sv = "Max hastighet (%)", cs = "Max. rychlost (%)", sk = "Max. rýchlosť (%)", da = "Maks. hastighed (%)", ru = "Макс. скорость (%)"), level.maxSpeedPercent, Tokens.Emerald) { onMaxSpeed(profile, selected, it) }
                }
            }
            BbsFwAssistBaseType.CRUISE -> {
                PreviewBanner(tr(
                    "Uwaga: tempomat (Cruise) - załączany pedałowaniem + manetką, wyłączany pedałowaniem wstecz, dotknięciem manetki albo hamulcem. Używaj ostrożnie.",
                    "Warning: Cruise mode - engaged by pedaling + throttle, disengaged by backpedaling, touching the throttle, or braking. Use with caution!",
                    de = "Achtung: Tempomat (Cruise) - wird durch Treten + Gasgriff aktiviert, durch Rückwärtstreten, Berühren des Gasgriffs oder Bremsen deaktiviert. Mit Vorsicht verwenden!",
                    fr = "Attention : régulateur de vitesse (Cruise) - activé en pédalant + accélérateur, désactivé en pédalant en arrière, en touchant l'accélérateur ou en freinant. À utiliser avec prudence !",
                    es = "Atención: control de crucero (Cruise) - se activa pedaleando + acelerador, se desactiva pedaleando hacia atrás, tocando el acelerador o frenando. ¡Úsalo con precaución!",
                    pt = "Atenção: piloto automático (Cruise) - ativado ao pedalar + acelerador, desativado ao pedalar para trás, tocando no acelerador ou travando. Usa com cuidado!",
                    it = "Attenzione: cruise control (Cruise) - attivato pedalando + acceleratore, disattivato pedalando all'indietro, toccando l'acceleratore o frenando. Usa con cautela!",
                    nl = "Let op: cruise control (Cruise) - geactiveerd door te trappen + gasgreep, gedeactiveerd door achteruit te trappen, de gasgreep aan te raken of te remmen. Gebruik met voorzichtigheid!",
                    sv = "Varning: farthållare (Cruise) - aktiveras genom att trampa + gasreglage, kopplas ur genom att trampa bakåt, röra gasreglaget eller bromsa. Använd med försiktighet!",
                    cs = "Pozor: tempomat (Cruise) - zapíná se šlapáním + plynovou páčkou, vypíná se zpětným šlapáním, dotykem plynové páčky nebo brzděním. Používej opatrně!",
                    sk = "Pozor: tempomat (Cruise) - zapína sa šliapaním + plynovou páčkou, vypína sa spätným šliapaním, dotykom plynovej páčky alebo brzdením. Používaj opatrne!",
                    da = "Advarsel: fartpilot (Cruise) - aktiveres ved trædning + gashåndtag, deaktiveres ved baglæns trædning, berøring af gashåndtaget eller bremsning. Brug med forsigtighed!",
                    ru = "Внимание: круиз-контроль (Cruise) - включается педалированием + газом, выключается обратным педалированием, касанием газа или торможением. Используйте с осторожностью!",
                ))
                TokenCard(borderColor = Tokens.WhiteBorder) {
                    LabeledSliderRow(tr("Maks. prąd (%)", "Max Current (%)", de = "Max. Strom (%)", fr = "Courant max (%)", es = "Corriente máx. (%)", pt = "Corrente máx. (%)", it = "Corrente max (%)", nl = "Max. stroom (%)", sv = "Max ström (%)", cs = "Max. proud (%)", sk = "Max. prúd (%)", da = "Maks. strøm (%)", ru = "Макс. ток (%)"), level.targetCurrentPercent, Tokens.Amber) { onTargetCurrent(profile, selected, it) }
                    HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                    LabeledSliderRow(tr("Maks. kadencja (%)", "Max Cadence (%)", de = "Max. Kadenz (%)", fr = "Cadence max (%)", es = "Cadencia máx. (%)", pt = "Cadência máx. (%)", it = "Cadenza max (%)", nl = "Max. cadans (%)", sv = "Max kadens (%)", cs = "Max. kadence (%)", sk = "Max. kadencia (%)", da = "Maks. kadence (%)", ru = "Макс. каденс (%)"), level.maxCadencePercent, Tokens.Emerald) { onMaxCadence(profile, selected, it) }
                    HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                    LabeledSliderRow(tr("Maks. prędkość (%)", "Max Speed (%)", de = "Max. Geschwindigkeit (%)", fr = "Vitesse max (%)", es = "Velocidad máx. (%)", pt = "Velocidade máx. (%)", it = "Velocità max (%)", nl = "Max. snelheid (%)", sv = "Max hastighet (%)", cs = "Max. rychlost (%)", sk = "Max. rýchlosť (%)", da = "Maks. hastighed (%)", ru = "Макс. скорость (%)"), level.maxSpeedPercent, Tokens.Emerald) { onMaxSpeed(profile, selected, it) }
                }
            }
            else -> {
                TokenCard(borderColor = Tokens.WhiteBorder) {
                    Text(
                        tr(
                            "Silnik wyłączony na tym poziomie - manetka i pedałowanie nie dają wspomagania.",
                            "Motor disabled at this level - throttle and pedaling give no assist.",
                            de = "Motor auf dieser Stufe deaktiviert - Gasgriff und Treten geben keine Unterstützung.",
                            fr = "Moteur désactivé à ce niveau - l'accélérateur et le pédalage ne donnent aucune assistance.",
                            es = "Motor desactivado en este nivel - el acelerador y pedalear no dan asistencia.",
                            pt = "Motor desativado neste nível - o acelerador e o pedalar não dão assistência.",
                            it = "Motore disattivato a questo livello - acceleratore e pedalata non forniscono assistenza.",
                            nl = "Motor uitgeschakeld op dit niveau - gasgreep en trappen geven geen ondersteuning.",
                            sv = "Motorn avstängd på denna nivå - gasreglage och trampning ger ingen assistans.",
                            cs = "Motor na této úrovni vypnutý - plynová páčka a šlapání neposkytují asistenci.",
                            sk = "Motor na tejto úrovni vypnutý - plynová páčka a šliapanie neposkytujú asistenciu.",
                            da = "Motoren er deaktiveret på dette niveau - gashåndtag og trædning giver ingen assistance.",
                            ru = "Двигатель отключён на этом уровне - газ и педалирование не дают помощи.",
                        ),
                        fontFamily = Manrope, fontSize = 12.sp, color = Tokens.TextSecondary,
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        HorizontalDivider(color = Tokens.Border, thickness = 2.dp)
        Spacer(Modifier.height(4.dp))

        // --- Operation Mode Toggle / Startup Assist Level (globalne, nie per-profil) ---
        TokenCard(borderColor = Tokens.WhiteBorder) {
            LabeledStepRow(
                tr("Przełącznik trybu pracy:", "Operation Mode Toggle:", de = "Betriebsmodus-Schalter:", fr = "Commutateur de mode de fonctionnement :", es = "Interruptor de modo de funcionamiento:", pt = "Interruptor de modo de funcionamento:", it = "Interruttore modalità di funzionamento:", nl = "Werkmodusschakelaar:", sv = "Omkopplare för driftläge:", cs = "Přepínač provozního režimu:", sk = "Prepínač prevádzkového režimu:", da = "Driftstilstand-kontakt:", ru = "Переключатель режима работы:"), assistModeSelectLabel(cfg.assistModeSelect),
                { onAssistModeSelect(stepInOrder(ASSIST_MODE_SELECT_DISPLAY_ORDER, cfg.assistModeSelect, -1)) },
                { onAssistModeSelect(stepInOrder(ASSIST_MODE_SELECT_DISPLAY_ORDER, cfg.assistModeSelect, 1)) },
            )
            HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
            LabeledStepRow(
                tr("Poziom wspomagania przy starcie:", "Startup Assist Level:", de = "Unterstützungsstufe beim Start:", fr = "Niveau d'assistance au démarrage :", es = "Nivel de asistencia al arrancar:", pt = "Nível de assistência ao arrancar:", it = "Livello di assistenza all'avvio:", nl = "Ondersteuningsniveau bij opstarten:", sv = "Assistansnivå vid start:", cs = "Úroveň asistence při startu:", sk = "Úroveň asistencie pri štarte:", da = "Assistanceniveau ved start:", ru = "Уровень ассистента при старте:"), cfg.assistStartupLevel.toString(),
                { onAssistStartupLevel(cfg.assistStartupLevel - 1) },
                { onAssistStartupLevel(cfg.assistStartupLevel + 1) },
            )
        }
        Text(
            tr(
                "Operation Mode Toggle: jak fizycznie przełącza się Standard/Sport - \"Przycisk Sport\" to dedykowany przycisk (u nas: przełącznik \"Sport\" w Kokpicie), \"Przycisk Światła\" przejmuje przycisk światła (patrz ostrzeżenie w Kokpicie). Startup Assist Level: poziom wspomagania profilu Standard, w którym startuje sterownik bez podłączonego wyświetlacza.",
                "Operation Mode Toggle: how Standard/Sport is physically switched - \"Sport Button\" is a dedicated button (in this app: the \"Sport\" switch in the Cockpit), \"Lights Button\" repurposes the lights button (see the Cockpit warning). Startup Assist Level: the Standard-profile assist level the controller starts in with no display connected.",
                de = "Operation Mode Toggle: wie physisch zwischen Standard/Sport umgeschaltet wird - „Sport Button“ ist " +
                    "eine eigene Taste (in dieser App: der „Sport“-Schalter im Cockpit), „Lights Button“ nutzt die " +
                    "Lichttaste um (siehe Warnhinweis im Cockpit). Startup Assist Level: die Unterstützungsstufe des " +
                    "Standard-Profils, mit der das Steuergerät ohne angeschlossenes Display startet.",
                fr = "Operation Mode Toggle : comment on bascule physiquement entre Standard/Sport - « Sport Button » " +
                    "est un bouton dédié (dans cette appli : l'interrupteur « Sport » du Cockpit), « Lights Button » " +
                    "réutilise le bouton des feux (voir l'avertissement dans le Cockpit). Startup Assist Level : le " +
                    "niveau d'assistance du profil Standard avec lequel le contrôleur démarre sans écran connecté.",
                es = "Operation Mode Toggle: cómo se cambia físicamente entre Standard/Sport - «Sport Button» es un " +
                    "botón dedicado (en esta app: el interruptor «Sport» del Cockpit), «Lights Button» reutiliza el " +
                    "botón de luces (ver la advertencia en el Cockpit). Startup Assist Level: el nivel de asistencia " +
                    "del perfil Standard con el que arranca el controlador sin pantalla conectada.",
                pt = "Operation Mode Toggle: como se alterna fisicamente entre Standard/Sport - «Sport Button» é um " +
                    "botão dedicado (nesta app: o interruptor «Sport» no Cockpit), «Lights Button» reutiliza o botão " +
                    "das luzes (ver o aviso no Cockpit). Startup Assist Level: o nível de assistência do perfil " +
                    "Standard com o qual o controlador arranca sem ecrã ligado.",
                it = "Operation Mode Toggle: come si passa fisicamente tra Standard/Sport - «Sport Button» è un " +
                    "pulsante dedicato (in questa app: l'interruttore «Sport» nel Cockpit), «Lights Button» riutilizza " +
                    "il pulsante delle luci (vedi l'avviso nel Cockpit). Startup Assist Level: il livello di assistenza " +
                    "del profilo Standard con cui il controller si avvia senza display collegato.",
                nl = "Operation Mode Toggle: hoe er fysiek wordt geschakeld tussen Standard/Sport - «Sport Button» is " +
                    "een speciale knop (in deze app: de «Sport»-schakelaar in de Cockpit), «Lights Button» hergebruikt " +
                    "de lichtknop (zie de waarschuwing in de Cockpit). Startup Assist Level: het ondersteuningsniveau " +
                    "van het Standard-profiel waarmee de controller start zonder aangesloten display.",
                sv = "Operation Mode Toggle: hur man fysiskt växlar mellan Standard/Sport - «Sport Button» är en " +
                    "dedikerad knapp (i denna app: «Sport»-omkopplaren i Cockpit), «Lights Button» återanvänder " +
                    "ljusknappen (se varningen i Cockpit). Startup Assist Level: assistansnivån för Standard-profilen " +
                    "som styrenheten startar med utan ansluten display.",
                cs = "Operation Mode Toggle: jak se fyzicky přepíná mezi Standard/Sport - „Sport Button“ je " +
                    "vyhrazené tlačítko (v této aplikaci: přepínač „Sport“ v Cockpitu), „Lights Button“ využívá " +
                    "tlačítko světel (viz varování v Cockpitu). Startup Assist Level: úroveň asistence profilu " +
                    "Standard, se kterou řídicí jednotka startuje bez připojeného displeje.",
                sk = "Operation Mode Toggle: ako sa fyzicky prepína medzi Standard/Sport - „Sport Button“ je " +
                    "vyhradené tlačidlo (v tejto aplikácii: prepínač „Sport“ v Cockpite), „Lights Button“ využíva " +
                    "tlačidlo svetiel (pozri varovanie v Cockpite). Startup Assist Level: úroveň asistencie profilu " +
                    "Standard, s ktorou riadiaca jednotka štartuje bez pripojeného displeja.",
                da = "Operation Mode Toggle: hvordan der fysisk skiftes mellem Standard/Sport - „Sport Button“ " +
                    "er en dedikeret knap (i denne app: „Sport“-kontakten i Cockpit), „Lights Button“ genbruger " +
                    "lysknappen (se advarslen i Cockpit). Startup Assist Level: assistanceniveauet for Standard-" +
                    "profilen, som controlleren starter i uden tilsluttet display.",
                ru = "Operation Mode Toggle: как физически переключается Standard/Sport - «Sport Button» - " +
                    "это отдельная кнопка (в этом приложении: переключатель «Sport» в Кокпите), «Lights Button» " +
                    "использует кнопку света (см. предупреждение в Кокпите). Startup Assist Level: уровень " +
                    "ассистента профиля Standard, с которым контроллер запускается без подключённого дисплея.",
            ),
            fontFamily = Manrope, fontSize = 11.sp, color = Tokens.TextSecondary,
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun LabeledSliderRow(label: String, value: Int, accent: Color, enabled: Boolean = true, onValueChange: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontFamily = Manrope, fontSize = 13.sp, color = if (enabled) Tokens.TextSecondary else Tokens.TextTertiary, modifier = Modifier.weight(1f))
            Text("$value%", fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (enabled) Tokens.TextPrimary else Tokens.TextTertiary)
        }
        Spacer(Modifier.height(4.dp))
        if (enabled) {
            FlankedSlider(value = value, range = 0..100, accent = accent, onValueChange = onValueChange)
        }
    }
}

@Composable
private fun LabeledStepRow(label: String, value: String, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextSecondary, modifier = Modifier.weight(1f))
        StepBtn("-", true, onMinus)
        Box(Modifier.padding(horizontal = 10.dp), contentAlignment = Alignment.Center) {
            Text(value, fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Tokens.TextPrimary)
        }
        StepBtn("+", true, onPlus)
    }
}

@Composable
private fun baseTypeLabel(type: Int): String = when (type) {
    BbsFwAssistBaseType.DISABLED -> "Motor Disabled"
    BbsFwAssistBaseType.PAS -> "PAS"
    BbsFwAssistBaseType.THROTTLE -> "Throttle"
    BbsFwAssistBaseType.CRUISE -> "Cruise"
    else -> "?"
}

@Composable
private fun pasVariantLabel(v: Int): String = when (v) {
    BbsFwAssistPasVariant.CADENCE -> "Cadence"
    BbsFwAssistPasVariant.TORQUE -> "Torque"
    BbsFwAssistPasVariant.VARIABLE -> "Variable"
    else -> "?"
}

@Composable
private fun assistModeSelectLabel(value: Int): String = when (value) {
    0 -> "Off"
    1 -> "Sport Button"
    2 -> "Lights Button"
    13 -> "Brakes @ Power On"
    in 3..12 -> "PAS ${value - 3} + Lights Button"
    else -> "?"
}
