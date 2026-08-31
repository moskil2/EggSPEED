package com.bafspeed.app.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bafspeed.app.AutoReconnectState
import com.bafspeed.app.FirmwareType
import com.bafspeed.app.SpeedUnit
import com.bafspeed.app.UiState
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.protocol.Telemetry
import com.bafspeed.app.ui.theme.LocalLightMode
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens
import kotlin.math.roundToInt

/** Kokpit: wysoki kontrast do czytelności w pełnym słońcu - mocniejsze obramowania niż reszta apki. Motyw jasny odwraca jasność 1:1, zachowując ten sam poziom kontrastu. */
private val HighContrastBorder: Color
    @Composable get() = if (LocalLightMode.current) Color(0x40000000) else Color(0x40FFFFFF) // ~25% czarny/biały, znacznie mocniejszy niż Tokens.Border (6%)
private val HighContrastText: Color
    @Composable get() = if (LocalLightMode.current) Color(0xFF0A0B0C) else Color(0xFFFFFFFF)
private val TileBg: Color
    @Composable get() = if (LocalLightMode.current) Color(0xFFE9EAEC) else Color(0xFF121418)

/** Tło całego Kokpitu - w motywie ciemnym czysta czerń (mocniejsza niż Tokens.Bg, celowo dla OLED/nocnej jazdy), w jasnym to samo tło co reszta apki. */
private val CockpitBg: Color
    @Composable get() = if (LocalLightMode.current) Tokens.Bg else Color(0xFF000000)

/** Odstęp między rzędem Światło/Hamulec a rzędem Sport (na wyraźne życzenie: oba rzędy razem
 * nie wyższe niż pojedynczy przycisk −/+, czyli 2×COMPACT_TILE_HEIGHT + COMPACT_TILE_GAP ≤ 70.dp). */
private val COMPACT_TILE_GAP = 4.dp
private val COMPACT_TILE_HEIGHT = 33.dp

/**
 * Rozszerza dostępną szerokość o [extra] na każdą stronę ponad ograniczenie z rodzica (np. padding
 * Column) i wyśrodkowuje wynik w oryginalnie przydzielonym miejscu - efekt "full bleed" bez użycia
 * ujemnego paddingu (Compose rzuca wyjątkiem dla ujemnych wartości w Modifier.padding).
 */
private fun Modifier.horizontalBleed(extra: Dp): Modifier = layout { measurable, constraints ->
    val extraPx = extra.roundToPx()
    val widened = constraints.copy(
        minWidth = (constraints.minWidth + extraPx * 2).coerceAtLeast(0),
        maxWidth = (constraints.maxWidth + extraPx * 2).coerceAtLeast(0),
    )
    val placeable = measurable.measure(widened)
    layout(placeable.width - extraPx * 2, placeable.height) {
        placeable.place(-extraPx, 0)
    }
}

@Composable
fun DashboardScreen(
    state: UiState,
    telemetry: Telemetry,
    onStartDisplay: () -> Unit,
    onStopDisplay: () -> Unit,
    onAssistChange: (Int) -> Unit,
    onLightToggle: () -> Unit,
    onSportModeToggle: () -> Unit,
    onGoToConnect: () -> Unit,
    onResetTrip: () -> Unit,
    onResetAvgSpeed: () -> Unit,
    onActivateProtect: () -> Unit,
) {
    // Tryb wyświetlacza aktywny tylko gdy ekran widoczny (i połączony - start jest guardowany)
    DisposableEffect(Unit) {
        onStartDisplay()
        onDispose { onStopDisplay() }
    }
    val unit = state.units
    val connected = state.connection == com.bafspeed.app.ConnectionStatus.CONNECTED

    var showResetTripConfirm by remember { mutableStateOf(false) }
    var showResetAvgSpeedConfirm by remember { mutableStateOf(false) }
    // Kafelka z krótkim wyjaśnieniem "jak to jest liczone" po kliknięciu kafelki Pręd. śr. /
    // Śr. zużycie / Chwil. zuż. / Zasięg - Pair(tytuł, opis), null = dialog ukryty.
    var infoDialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    val avgSpeedLabel = tr("Pręd. śr.", "Avg. speed", de = "Ø-Tempo", fr = "Vit. moy.", es = "Vel. med.", pt = "Vel. méd.", it = "Vel. med.", nl = "Gem. snelh.", sv = "Medelhast.", cs = "Prům. rychl.", sk = "Priem. rýchl.")
    val avgSpeedInfo = tr(
        "Dystans podzielony przez czas W RUCHU od ostatniego resetu - postoje (np. na światłach) nie obniżają średniej.",
        "Distance divided by time spent moving since the last reset - stops (e.g. at traffic lights) don't lower the average.",
        de = "Distanz geteilt durch die Zeit IN BEWEGUNG seit dem letzten Reset - Stopps (z. B. an Ampeln) senken den Durchschnitt nicht.",
        fr = "Distance divisée par le temps EN MOUVEMENT depuis la dernière réinitialisation - les arrêts (ex. aux feux) ne réduisent pas la moyenne.",
        es = "Distancia dividida entre el tiempo EN MOVIMIENTO desde el último reinicio - las paradas (p. ej. en semáforos) no reducen la media.",
        pt = "Distância dividida pelo tempo EM MOVIMENTO desde o último reinício - as paradas (por ex. em semáforos) não reduzem a média.",
        it = "Distanza divisa per il tempo IN MOVIMENTO dall'ultimo reset - le soste (ad es. ai semafori) non abbassano la media.",
        nl = "Afstand gedeeld door de tijd IN BEWEGING sinds de laatste reset - stops (bijv. bij verkeerslichten) verlagen het gemiddelde niet.",
        sv = "Sträcka delat med tiden I RÖRELSE sedan senaste återställningen - stopp (t.ex. vid trafikljus) sänker inte medelvärdet.",
        cs = "Vzdálenost dělená časem STRÁVENÝM V POHYBU od posledního resetu - zastávky (např. na semaforu) nesnižují průměr.",
        sk = "Vzdialenosť delená časom STRÁVENÝM V POHYBE od posledného resetu - zastávky (napr. na semafore) nesnižujú priemer.",
    )
    val avgUsageLabel = tr("Śr. zużycie", "Avg. usage", de = "Ø Verbr.", fr = "Conso. moy.", es = "Cons. media", pt = "Cons. médio", it = "Cons. medio", nl = "Gem. verbr.", sv = "Medelförbr.", cs = "Prům. spotř.", sk = "Priem. spotr.")
    val avgUsageInfo = tr(
        "Cała energia zużyta od ostatniego resetu TRIP, podzielona przez przejechany dystans - czysta średnia dla całej trasy.",
        "Total energy used since the last TRIP reset, divided by the distance traveled - a plain average for the whole trip.",
        de = "Gesamte Energie seit dem letzten TRIP-Reset, geteilt durch die zurückgelegte Strecke - ein reiner Durchschnitt für die gesamte Fahrt.",
        fr = "Toute l'énergie consommée depuis la dernière réinitialisation du TRIP, divisée par la distance parcourue - une moyenne simple pour tout le trajet.",
        es = "Toda la energía consumida desde el último reinicio de TRIP, dividida entre la distancia recorrida - una media simple de todo el trayecto.",
        pt = "Toda a energia consumida desde o último reinício do TRIP, dividida pela distância percorrida - uma média simples de todo o percurso.",
        it = "Tutta l'energia consumata dall'ultimo reset del TRIP, divisa per la distanza percorsa - una media semplice per l'intero percorso.",
        nl = "Alle energie verbruikt sinds de laatste TRIP-reset, gedeeld door de afgelegde afstand - een eenvoudig gemiddelde voor de hele rit.",
        sv = "All energi som förbrukats sedan senaste TRIP-återställningen, delat med tillryggalagd sträcka - ett enkelt medelvärde för hela resan.",
        cs = "Veškerá energie spotřebovaná od posledního resetu TRIP, dělená ujetou vzdáleností - jednoduchý průměr za celou jízdu.",
        sk = "Všetka energia spotrebovaná od posledného resetu TRIP, delená prejdenou vzdialenosťou - jednoduchý priemer za celú jazdu.",
    )
    val actUsageLabel = tr("Chwil. zuż.", "Act. usage", de = "Akt. Verbr.", fr = "Cons. act.", es = "Cons. act.", pt = "Cons. atual", it = "Cons. att.", nl = "Act. verbr.", sv = "Akt. förbr.", cs = "Akt. spotř.", sk = "Akt. spotr.")
    val actUsageInfo = tr(
        "Krótkoterminowa średnia krocząca (ostatnie ok. 300 m) - szybko reaguje na Twój aktualny styl jazdy, w odróżnieniu od średniej z całej trasy.",
        "Short-term rolling average (roughly the last 300 m) - reacts quickly to your current riding style, unlike the whole-trip average.",
        de = "Kurzfristiger gleitender Durchschnitt (ca. die letzten 300 m) - reagiert schnell auf deinen aktuellen Fahrstil, im Gegensatz zum Durchschnitt der gesamten Fahrt.",
        fr = "Moyenne glissante à court terme (environ les 300 derniers m) - réagit rapidement à votre style de conduite actuel, contrairement à la moyenne de tout le trajet.",
        es = "Media móvil a corto plazo (aprox. los últimos 300 m) - reacciona rápido a tu estilo de conducción actual, a diferencia de la media de todo el trayecto.",
        pt = "Média móvel de curto prazo (aproximadamente os últimos 300 m) - reage rapidamente ao seu estilo de condução atual, ao contrário da média de todo o percurso.",
        it = "Media mobile a breve termine (circa gli ultimi 300 m) - reagisce rapidamente al tuo stile di guida attuale, a differenza della media dell'intero percorso.",
        nl = "Kortetermijn voortschrijdend gemiddelde (ongeveer de laatste 300 m) - reageert snel op je huidige rijstijl, in tegenstelling tot het gemiddelde van de hele rit.",
        sv = "Kortsiktigt glidande medelvärde (ungefär de senaste 300 m) - reagerar snabbt på din nuvarande körstil, till skillnad från medelvärdet för hela resan.",
        cs = "Krátkodobý klouzavý průměr (přibližně posledních 300 m) - rychle reaguje na tvůj aktuální styl jízdy, na rozdíl od průměru za celou jízdu.",
        sk = "Krátkodobý kĺzavý priemer (približne posledných 300 m) - rýchlo reaguje na tvoj aktuálny štýl jazdy, na rozdiel od priemeru za celú jazdu.",
    )
    val rangeLabel = tr("Zasięg ~", "Range ~", de = "Reichw. ~", fr = "Autonomie ~", es = "Autonomía ~", pt = "Autonomia ~", it = "Autonomia ~", nl = "Bereik ~", sv = "Räckvidd ~", cs = "Dojezd ~", sk = "Dojazd ~")
    val rangeInfo = tr(
        "Pozostała energia baterii (bateria % × pojemność) podzielona przez zużycie Wh/km. To zużycie to mieszanka Twojej długoterminowej historii jazdy i stylu z ostatnich kilku km - na starcie trasy liczy się głównie historia, po ok. 4 km coraz bardziej bieżący styl jazdy.",
        "Remaining battery energy (battery % × capacity) divided by Wh/km usage. That usage blends your long-term riding history with your style from the last few km - mostly history at the start of a trip, shifting toward your current style after about 4 km.",
        de = "Verbleibende Batterieenergie (Batterie-% × Kapazität) geteilt durch den Wh/km-Verbrauch. Dieser Verbrauch mischt deine langfristige Fahrhistorie mit deinem Stil der letzten paar km - am Anfang der Fahrt zählt vor allem die Historie, nach ca. 4 km zunehmend dein aktueller Fahrstil.",
        fr = "Énergie restante de la batterie (% batterie × capacité) divisée par la consommation Wh/km. Cette consommation mélange votre historique de conduite à long terme avec votre style des derniers km - surtout l'historique en début de trajet, puis de plus en plus votre style actuel après environ 4 km.",
        es = "Energía restante de la batería (% batería × capacidad) dividida entre el consumo Wh/km. Ese consumo combina tu historial de conducción a largo plazo con tu estilo de los últimos km - sobre todo el historial al inicio del trayecto, y cada vez más tu estilo actual a partir de unos 4 km.",
        pt = "Energia restante da bateria (% da bateria × capacidade) dividida pelo consumo em Wh/km. Esse consumo combina seu histórico de condução de longo prazo com seu estilo dos últimos km - principalmente o histórico no início do percurso, mudando cada vez mais para o seu estilo atual após cerca de 4 km.",
        it = "Energia residua della batteria (% batteria × capacità) divisa per il consumo Wh/km. Questo consumo combina la tua cronologia di guida a lungo termine con il tuo stile degli ultimi km - soprattutto la cronologia all'inizio del percorso, spostandosi sempre più verso il tuo stile attuale dopo circa 4 km.",
        nl = "Resterende batterij-energie (batterij % × capaciteit) gedeeld door het Wh/km-verbruik. Dat verbruik combineert je langetermijn rijgeschiedenis met je stijl van de laatste paar km - vooral geschiedenis aan het begin van de rit, steeds meer verschuivend naar je huidige stijl na ongeveer 4 km.",
        sv = "Återstående batterienergi (batteri % × kapacitet) delat med Wh/km-förbrukningen. Den förbrukningen blandar din långsiktiga körhistorik med din stil de senaste km - mest historik i början av resan, alltmer din nuvarande stil efter ca 4 km.",
        cs = "Zbývající energie baterie (% baterie × kapacita) dělená spotřebou Wh/km. Tato spotřeba kombinuje tvou dlouhodobou historii jízdy s tvým stylem z posledních pár km - hlavně historie na začátku jízdy, po cca 4 km stále více tvůj aktuální styl.",
        sk = "Zostávajúca energia batérie (% batérie × kapacita) delená spotrebou Wh/km. Táto spotreba kombinuje tvoju dlhodobú históriu jazdy s tvojím štýlom z posledných pár km - hlavne história na začiatku jazdy, po cca 4 km čoraz viac tvoj aktuálny štýl.",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CockpitBg)
            .verticalScroll(rememberScrollState())
            .padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 10.dp),
    ) {
        // Status row - ONLINE/OFFLINE jako klikalny przycisk → zakładka Połączenie (symetrycznie
        // do kafelka PROTECT/SAFE, bez osobnej kropki koloru - kolor niesie już obramowanie+tekst).
        // PROTECT/SAFE (antynapadowy, patrz ServiceScreen) dosunięty do prawej krawędzi tego samego
        // wiersza - stąd fillMaxWidth() + Spacer(weight(1f)), czego reszta wiersza wcześniej nie miała.
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            if (connected) {
                Text(
                    state.general?.model?.trim() ?: "",
                    fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = HighContrastText,
                )
                Spacer(Modifier.size(8.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .border(1.dp, if (connected) Tokens.Emerald else Tokens.Red, RoundedCornerShape(8.dp))
                    .clickable { onGoToConnect() }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    if (connected) "ONLINE" else "OFFLINE",
                    fontFamily = Manrope, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, letterSpacing = 1.sp,
                    color = if (connected) Tokens.Emerald else Tokens.Red,
                )
            }
            if (state.autoReconnectState != AutoReconnectState.IDLE) {
                Spacer(Modifier.size(8.dp))
                ReconnectBadge(state.autoReconnectState)
            }
            if (state.protectFeatureEnabled) {
                Spacer(Modifier.weight(1f))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .border(1.dp, if (state.protectActive) Tokens.Emerald else Tokens.Red, RoundedCornerShape(8.dp))
                        .let { if (!state.protectActive) it.clickable { onActivateProtect() } else it }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        if (state.protectActive) "SAFE" else "PROTECT",
                        fontFamily = Manrope, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, letterSpacing = 1.sp,
                        color = if (state.protectActive) Tokens.Emerald else Tokens.Red,
                    )
                }
            }
        }

        Spacer(Modifier.height(2.dp))

        // Tryb testowy (przycisk TEST w Ustawieniach) - wymusza skrajne wartosci na duzym
        // wyswietlaczu predkosci/mocy i na kafelkach ponizej, zeby sprawdzic czy dluzsze/wieksze
        // liczby nadal miesza sie w layoucie. Predkosc ograniczona do 199,9 - to twardy limit
        // samego DigitalSpeed (skalowany pod maks. 3 cyfry), nie ma sensu podawac wiecej.
        val t = state.testMode

        // Prędkość - czysto cyfrowa, bez pierścienia, jednostka po prawej stronie odczytu.
        // Adnotacja GPS (Ustawienia) nakłada się w prawym górnym rogu tego samego bloku (Box, nie
        // osobny wiersz) - tak jak TempTile nakłada się na blok mocy niżej - żeby NIE przesuwać
        // głównego odczytu prędkości w dół.
        Box(Modifier.fillMaxWidth()) {
            DigitalSpeed(speedKmh = if (t) unit.toKmh(199.9) else telemetry.speedKmh, unit = unit, largeDigits = state.largeCockpitDigits, modifier = Modifier.fillMaxWidth())
            if (state.gpsSpeedEnabled) {
                Text(
                    // Format zgodny z DigitalSpeed (kropka dziesietna, nie przecinek - ten sam wzorzec
                    // co reszta odczytow liczbowych w apce, patrz String.format("%.1f", ...) wyzej).
                    // Szerokosc pola stala na 4 znaki (spacja zamiast zera wiodacego, nie "%04.1f") -
                    // "0.0"/"9.9" bez zera z przodu, a tekst nie "skacze" w prawo/lewo przy przejsciu
                    // przez prog 10 (10.0 zajmuje dokladnie te same 4 znaki co " 9.9").
                    String.format("GPS %4.1f%s", unit.fromKmh(state.gpsSpeedKmh), unit.label.uppercase()),
                    fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = HighContrastText,
                    textAlign = TextAlign.End,
                    modifier = Modifier.align(Alignment.TopEnd),
                )
            }
        }

        // Moc - pod prędkością, mniejsza czcionka. Kafelek Tc (zakładka "Temperature control", tylko
        // bbs-fw - Tm/rejestr 0x21 zawsze zwraca 0 na bbs-fw, patrz PROTOKOL_BBSFW.md sekcja 5)
        // nakłada się po lewej stronie tego bloku, na wysokości odczytu mocy - test mode wymusza
        // 100°C, tak samo jak wymusza skrajne wartości predkosci/mocy powyzej.
        val tempControllerC = if (t) 100 else telemetry.tempControllerC
        Box(Modifier.fillMaxWidth().horizontalBleed(6.dp)) {
            DigitalPower(powerW = if (t) 3000.0 else telemetry.powerW, largeDigits = state.largeCockpitDigits, modifier = Modifier.fillMaxWidth())
            if (state.showTempOnCockpit && state.firmwareType == FirmwareType.BBS_FW) {
                TempTile(
                    valueC = tempControllerC,
                    warning = tempControllerC >= state.tempWarningC,
                    alarm = tempControllerC >= state.tempAlarmC,
                    modifier = Modifier.align(Alignment.CenterStart),
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        // Stat grid: Dystans, Trip (z resetem), Prąd, Napięcie, Zasięg, Temp. sterownika, Temp. silnika
        // Poszerzone wzgledem reszty ekranu - "bleed" o 6dp na kazda strone (poza domyslny padding
        // Column=8dp, wiec zostaje 2dp marginesu do fizycznej krawedzi ekranu - przy wiekszym bleedzie
        // (probowane 10dp) ramka kafelka wychodzila poza ekran i skrajne brzegi sie nie wyswietlaly)
        // i wezszy odstep miedzy kartami (6dp -> 3dp), tak by karty zyskaly wiecej miejsca - etykiety
        // w StatCard maja maxLines=1, wiec brakujace miejsce psowaloby uklad (2. linia rozciagala kafelke).
        val statRowModifier = Modifier.fillMaxWidth().horizontalBleed(6.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = statRowModifier) {
            StatCard(tr("Dystans", "Distance", de = "Distanz", fr = "Distance", es = "Distancia", pt = "Distância", it = "Distanza", nl = "Afstand", sv = "Distans", cs = "Vzdálenost", sk = "Vzdialenosť"), if (t) "10000.0" else String.format("%.1f", unit.fromKmh(state.totalOdoKm)), unit.distanceLabel, Modifier.weight(1f), largeDigits = state.largeCockpitDigits)
            StatCard(
                "Trip", if (t) "1000.0" else String.format("%.1f", unit.fromKmh(state.tripKm)), unit.distanceLabel, Modifier.weight(1f),
                largeDigits = state.largeCockpitDigits,
                onReset = { showResetTripConfirm = true },
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = statRowModifier) {
            StatCard(tr("Prąd", "Current", de = "Strom", fr = "Courant", es = "Corriente", pt = "Corrente", it = "Corrente", nl = "Stroom", sv = "Ström", cs = "Proud", sk = "Prúd"), if (t) "80.0" else String.format("%.1f", telemetry.currentA), "A", Modifier.weight(1f), largeDigits = state.largeCockpitDigits)
            StatCard(
                tr("Napięcie", "Voltage", de = "Spannung", fr = "Tension", es = "Voltaje", pt = "Tensão", it = "Tensione", nl = "Spanning", sv = "Spänning", cs = "Napětí", sk = "Napätie"),
                // Bez polaczenia pokazujemy ostatnio znane napiecie (UiState.lastKnownVoltageV)
                // zamiast zerowac kafelke do 0,0.
                if (t) "80.0" else String.format("%.1f", if (connected) telemetry.voltageV else state.lastKnownVoltageV),
                "V", Modifier.weight(1f),
                largeDigits = state.largeCockpitDigits,
            )
            StatCard(
                avgSpeedLabel, if (t) "80.8" else String.format("%.1f", unit.fromKmh(state.avgSpeedKmh)), unit.label, Modifier.weight(1f),
                largeDigits = state.largeCockpitDigits,
                onReset = { showResetAvgSpeedConfirm = true },
                onInfoClick = { infoDialog = avgSpeedLabel to avgSpeedInfo },
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = statRowModifier) {
            StatCard(
                avgUsageLabel, if (t) "800.0" else String.format("%.1f", state.tripAvgWhPerKm), "Wh/km", Modifier.weight(1f),
                largeDigits = state.largeCockpitDigits,
                onInfoClick = { infoDialog = avgUsageLabel to avgUsageInfo },
            )
            StatCard(
                actUsageLabel, if (t) "800.0" else String.format("%.1f", state.currentAvgWhPerKm), "Wh/km", Modifier.weight(1f),
                largeDigits = state.largeCockpitDigits,
                onInfoClick = { infoDialog = actUsageLabel to actUsageInfo },
            )
            StatCard(
                rangeLabel, if (t) "800" else unit.fromKmh(state.predictedRangeKm).roundToInt().toString(), unit.distanceLabel, Modifier.weight(1f),
                largeDigits = state.largeCockpitDigits,
                onInfoClick = { infoDialog = rangeLabel to rangeInfo },
            )
        }

        if (showResetTripConfirm) {
            AlertDialog(
                containerColor = Tokens.Card,
                onDismissRequest = { showResetTripConfirm = false },
                title = { Text(tr("Zresetować TRIP?", "Reset TRIP?", de = "TRIP zurücksetzen?", fr = "Réinitialiser le TRIP ?", es = "¿Reiniciar el TRIP?", pt = "Reiniciar o TRIP?", it = "Reimpostare il TRIP?", nl = "TRIP resetten?", sv = "Återställa TRIP?", cs = "Resetovat TRIP?", sk = "Resetovať TRIP?"), fontFamily = Sora, fontWeight = FontWeight.Bold, color = Tokens.TextPrimary) },
                text = {
                    Text(
                        tr(
                            "Czy na pewno chcesz zresetować TRIP?", "Are you sure you want to reset TRIP?",
                            de = "Möchtest du den TRIP wirklich zurücksetzen?",
                            fr = "Voulez-vous vraiment réinitialiser le TRIP ?",
                            es = "¿Seguro que quieres reiniciar el TRIP?",
                            pt = "Tem certeza de que deseja reiniciar o TRIP?",
                            it = "Sei sicuro di voler reimpostare il TRIP?",
                            nl = "Weet je zeker dat je TRIP wilt resetten?",
                            sv = "Är du säker på att du vill återställa TRIP?",
                            cs = "Opravdu chceš resetovat TRIP?",
                            sk = "Naozaj chceš resetovať TRIP?",
                        ),
                        fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextSecondary,
                    )
                },
                confirmButton = {
                    TextButton(onClick = { onResetTrip(); showResetTripConfirm = false }) {
                        Text(tr("Tak", "Yes", de = "Ja", fr = "Oui", es = "Sí", pt = "Sim", it = "Sì", nl = "Ja", sv = "Ja", cs = "Ano", sk = "Áno"), color = Tokens.Blue, fontFamily = Manrope, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetTripConfirm = false }) {
                        Text(tr("Nie", "No", de = "Nein", fr = "Non", es = "No", pt = "Não", it = "No", nl = "Nee", sv = "Nej", cs = "Ne", sk = "Nie"), color = Tokens.TextTertiary, fontFamily = Manrope)
                    }
                },
            )
        }

        if (showResetAvgSpeedConfirm) {
            AlertDialog(
                containerColor = Tokens.Card,
                onDismissRequest = { showResetAvgSpeedConfirm = false },
                title = { Text(tr("Zresetować prędkość średnią?", "Reset average speed?", de = "Durchschnittsgeschwindigkeit zurücksetzen?", fr = "Réinitialiser la vitesse moyenne ?", es = "¿Reiniciar la velocidad media?", pt = "Reiniciar a velocidade média?", it = "Reimpostare la velocità media?", nl = "Gemiddelde snelheid resetten?", sv = "Återställa medelhastigheten?", cs = "Resetovat průměrnou rychlost?", sk = "Resetovať priemernú rýchlosť?"), fontFamily = Sora, fontWeight = FontWeight.Bold, color = Tokens.TextPrimary) },
                text = {
                    Text(
                        tr(
                            "Czy na pewno chcesz zresetować prędkość średnią?", "Are you sure you want to reset the average speed?",
                            de = "Möchtest du die Durchschnittsgeschwindigkeit wirklich zurücksetzen?",
                            fr = "Voulez-vous vraiment réinitialiser la vitesse moyenne ?",
                            es = "¿Seguro que quieres reiniciar la velocidad media?",
                            pt = "Tem certeza de que deseja reiniciar a velocidade média?",
                            it = "Sei sicuro di voler reimpostare la velocità media?",
                            nl = "Weet je zeker dat je de gemiddelde snelheid wilt resetten?",
                            sv = "Är du säker på att du vill återställa medelhastigheten?",
                            cs = "Opravdu chceš resetovat průměrnou rychlost?",
                            sk = "Naozaj chceš resetovať priemernú rýchlosť?",
                        ),
                        fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextSecondary,
                    )
                },
                confirmButton = {
                    TextButton(onClick = { onResetAvgSpeed(); showResetAvgSpeedConfirm = false }) {
                        Text(tr("Tak", "Yes", de = "Ja", fr = "Oui", es = "Sí", pt = "Sim", it = "Sì", nl = "Ja", sv = "Ja", cs = "Ano", sk = "Áno"), color = Tokens.Blue, fontFamily = Manrope, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetAvgSpeedConfirm = false }) {
                        Text(tr("Nie", "No", de = "Nein", fr = "Non", es = "No", pt = "Não", it = "No", nl = "Nee", sv = "Nej", cs = "Ne", sk = "Nie"), color = Tokens.TextTertiary, fontFamily = Manrope)
                    }
                },
            )
        }

        infoDialog?.let { (title, text) ->
            AlertDialog(
                modifier = Modifier.border(1.dp, Color(0x59FFFFFF), RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                containerColor = Tokens.Card,
                onDismissRequest = { infoDialog = null },
                title = { Text(title, fontFamily = Sora, fontWeight = FontWeight.Bold, color = Tokens.TextPrimary) },
                text = { Text(text, fontFamily = Manrope, fontSize = 13.sp, lineHeight = 18.sp, color = Tokens.TextSecondary) },
                confirmButton = {
                    TextButton(onClick = { infoDialog = null }) {
                        Text(tr("OK", "OK", de = "OK", fr = "OK", es = "OK", pt = "OK", it = "OK", nl = "OK", sv = "OK", cs = "OK", sk = "OK"), color = Tokens.Blue, fontFamily = Manrope, fontWeight = FontWeight.Bold)
                    }
                },
            )
        }

        Spacer(Modifier.height(16.dp))

        // Poziomy wspomagania - 10 klikalnych, wysokich kafelków (bezpośredni wybór 0-9)
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
            repeat(10) { i ->
                val selected = i == state.assistLevel
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .background(if (selected) Tokens.Blue else TileBg, RoundedCornerShape(12.dp))
                        .border(1.dp, if (selected) Tokens.Blue else HighContrastBorder, RoundedCornerShape(12.dp))
                        .clickable { onAssistChange(i) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        i.toString(),
                        fontFamily = Sora, fontWeight = FontWeight.Bold,
                        fontSize = if (state.largeCockpitDigits) {
                            if (selected) 40.sp else 22.sp
                        } else {
                            if (selected) 34.sp else 18.sp
                        },
                        color = HighContrastText,
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))

        // − / [Światło+Hamulec (+ Sport tylko na bbs-fw)] / +
        // Przycisk Sport pokazujemy WYŁĄCZNIE na bbs-fw - na OEM nie ma potwierdzonego wsparcia
        // (patrz PROTOKOL_BBSFW.md), więc znika razem z ograniczeniem wysokości, które było
        // potrzebne tylko po to, żeby zmieścić 3 elementy w miejscu jednego przycisku −/+.
        val showSportMode = state.firmwareType == FirmwareType.BBS_FW
        val toggleTileHeight = if (showSportMode) COMPACT_TILE_HEIGHT else 56.dp
        val toggleTileFontSize = when {
            state.largeCockpitDigits && showSportMode -> 15.sp
            state.largeCockpitDigits -> 17.sp
            showSportMode -> 13.sp
            else -> 15.sp
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SquareButton("−", largeDigits = state.largeCockpitDigits) { onAssistChange(state.assistLevel - 1) }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(COMPACT_TILE_GAP)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ToggleTile(
                        label = tr("Światło", "Light", de = "Licht", fr = "Phare", es = "Luz", pt = "Luz", it = "Luce", nl = "Licht", sv = "Ljus", cs = "Světlo", sk = "Svetlo"),
                        active = state.lightOn,
                        activeColor = Tokens.Amber,
                        activeTextColor = Tokens.OnAccent,
                        modifier = Modifier.weight(1f),
                        height = toggleTileHeight,
                        fontSize = toggleTileFontSize,
                        onClick = onLightToggle,
                    )
                    ToggleTile(
                        label = tr("Hamulec", "Brake", de = "Bremse", fr = "Frein", es = "Freno", pt = "Freio", it = "Freno", nl = "Rem", sv = "Broms", cs = "Brzda", sk = "Brzda"),
                        active = telemetry.brakeActive,
                        activeColor = Tokens.Red,
                        activeTextColor = Color.White,
                        modifier = Modifier.weight(1f),
                        height = toggleTileHeight,
                        fontSize = toggleTileFontSize,
                    )
                }
                // Tryb jazdy Normal/Sport - ulotna komenda 0x16 0x0C, ta sama rodzina co Światło
                // (patrz BafangCommands.setOperationMode) - potwierdzona tylko na bbs-fw.
                if (showSportMode) {
                    ToggleTile(
                        label = tr("Tryb: Sport", "Mode: Sport", de = "Sport-Modus", fr = "Mode : Sport", es = "Modo: Sport", pt = "Modo: Sport", it = "Modalità: Sport", nl = "Modus: Sport", sv = "Läge: Sport", cs = "Režim: Sport", sk = "Režim: Sport"),
                        active = state.sportMode,
                        activeColor = Tokens.Red,
                        activeTextColor = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        height = COMPACT_TILE_HEIGHT,
                        fontSize = toggleTileFontSize,
                        onClick = onSportModeToggle,
                    )
                }
            }
            SquareButton("+", largeDigits = state.largeCockpitDigits) { onAssistChange(state.assistLevel + 1) }
        }
        // assistModeSelect 2-12 (LIGHTS / PASx_LIGHT) przejmuje na bbs-fw przycisk Światła do
        // przełączania Normal/Sport (patrz app_set_lights() w app.c) - nasz przycisk Światło wysyła
        // swój stan w KAŻDYM cyklu odpytywania, więc przy tym ustawieniu może po cichu nadpisywać
        // powyższy przełącznik Sport (ostatni wysłany w cyklu wygrywa). Ostrzegamy zamiast cichej niespójności.
        if (state.firmwareType == FirmwareType.BBS_FW && state.bbsFwConfigOrDefault.assistModeSelect in 2..12) {
            Spacer(Modifier.height(4.dp))
            Text(
                tr(
                    "Uwaga: \"Tryb wyboru wspomagania\" (zakładka bbs-fw System) jest ustawiony na sterowanie Sport/Normal " +
                        "przyciskiem Światła - przycisk Światło może nie świecić prawdziwym światłem i może nadpisywać przycisk Sport co cykl.",
                    "Warning: \"Assist Mode Select\" (bbs-fw System tab) is set to control Sport/Normal via the Lights " +
                        "button - the Lights button may not light a real light and may override the Sport button every cycle.",
                    de = "Achtung: \"Modusauswahl\" (bbs-fw System-Tab) ist so eingestellt, dass sie Sport/Normal über die " +
                        "Licht-Taste steuert - die Licht-Taste leuchtet dann möglicherweise kein echtes Licht und kann die Sport-Taste bei jedem Zyklus überschreiben.",
                    fr = "Attention : \"Sélection du mode d'assistance\" (onglet System bbs-fw) est réglé pour contrôler Sport/Normal " +
                        "via le bouton Lumière - le bouton Lumière peut ne pas allumer une vraie lumière et peut écraser le bouton Sport à chaque cycle.",
                    es = "Atención: \"Selección de modo de asistencia\" (pestaña System de bbs-fw) está configurado para controlar Sport/Normal " +
                        "mediante el botón de Luz - el botón de Luz puede no encender una luz real y puede sobrescribir el botón Sport en cada ciclo.",
                    pt = "Aviso: \"Assist Mode Select\" (aba System do bbs-fw) está configurado para controlar Sport/Normal " +
                        "pelo botão de Luz - o botão de Luz pode não acender uma luz real e pode sobrescrever o botão Sport a cada ciclo.",
                    it = "Attenzione: \"Assist Mode Select\" (scheda System di bbs-fw) è impostato per controllare Sport/Normal " +
                        "tramite il pulsante Luce - il pulsante Luce potrebbe non accendere una luce reale e potrebbe sovrascrivere il pulsante Sport a ogni ciclo.",
                    nl = "Let op: \"Assist Mode Select\" (bbs-fw System-tab) is ingesteld om Sport/Normal via de Lichtknop te bedienen " +
                        "- de Lichtknop laat mogelijk geen echt licht branden en kan de Sport-knop elke cyclus overschrijven.",
                    sv = "Varning: \"Assist Mode Select\" (bbs-fw System-fliken) är inställt att styra Sport/Normal via " +
                        "Ljusknappen - Ljusknappen kanske inte tänder ett riktigt ljus och kan skriva över Sport-knappen varje cykel.",
                    cs = "Pozor: \"Assist Mode Select\" (karta System v bbs-fw) je nastaveno na ovládání Sport/Normal " +
                        "tlačítkem světel - tlačítko světel nemusí rozsvítit skutečné světlo a může přepsat tlačítko Sport při každém cyklu.",
                    sk = "Pozor: \"Assist Mode Select\" (karta System v bbs-fw) je nastavené na ovládanie Sport/Normal " +
                        "tlačidlom svetiel - tlačidlo svetiel nemusí rozsvietiť skutočné svetlo a môže prepísať tlačidlo Sport pri každom cykle.",
                ),
                fontFamily = Manrope, fontSize = 10.sp, color = Tokens.Amber, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(6.dp))
    }
}

/** Prędkość jako duża, cyfrowa wartość - skalowana pod maks. 199.9 (3 cyfry części całkowitej). Bez pierścienia/gauge'a. */
@Composable
private fun DigitalSpeed(speedKmh: Double, unit: SpeedUnit, largeDigits: Boolean, modifier: Modifier = Modifier) {
    val animatedSpeed by animateFloatAsState(
        targetValue = unit.fromKmh(speedKmh).toFloat(),
        animationSpec = tween(300),
        label = "speed",
    )
    // Zakotwiczone do prawej krawędzi - przy zmianie liczby cyfr (9,9 → 10,0) prawa
    // krawędź (i jednostka) zostaje w miejscu, liczba rośnie tylko w lewo (nie "ucieka" po ekranie).
    // Cyfra po przecinku pomniejszona do 60% - dzięki Arrangement.End całość (część całkowita +
    // mniejsza część dziesiętna) i tak zostaje dosunięta do jednostki KM/H, bez dodatkowego kodu.
    val formattedSpeed = String.format("%.1f", animatedSpeed.coerceIn(0f, 199.9f))
    val decimalSeparatorIndex = formattedSpeed.indexOfFirst { !it.isDigit() }
    val integerPart = formattedSpeed.substring(0, decimalSeparatorIndex)
    val decimalPart = formattedSpeed.substring(decimalSeparatorIndex)
    Row(modifier = modifier, horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.Bottom) {
        // alignByBaseline (nie Alignment.Bottom) - przy dwóch różnych rozmiarach czcionki (104sp/62sp)
        // wyrównanie "do dołu" boxa daje różne wizualne podstawy cyfr (inny descent/line height),
        // baseline gwarantuje, że obie cyfry faktycznie stoją na tej samej linii.
        Text(
            integerPart,
            fontFamily = Sora, fontWeight = FontWeight.ExtraBold, fontSize = 104.sp,
            letterSpacing = (-2).sp, color = HighContrastText,
            style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"),
            modifier = Modifier.alignByBaseline(),
        )
        Text(
            decimalPart,
            fontFamily = Sora, fontWeight = FontWeight.ExtraBold, fontSize = if (largeDigits) 80.sp else 62.sp,
            letterSpacing = (-2).sp, color = HighContrastText,
            style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"),
            modifier = Modifier.alignByBaseline(),
        )
        Spacer(Modifier.size(8.dp))
        Text(
            unit.label.uppercase(),
            fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 15.sp,
            letterSpacing = 3.sp, color = Tokens.TextSecondary,
            modifier = Modifier.padding(bottom = 18.dp, end = 4.dp),
        )
    }
}

/**
 * Moc - pod prędkością, mniejsza, bez miejsc po przecinku, skalowana pod maks. 9999 W.
 *
 * Zakotwiczona tak, by SAMA LICZBA (bez jednostki "W") była zawsze idealnie na środku ekranu,
 * niezależnie od liczby cyfr - zwykłe centrowanie całego bloku "liczba + W" przesuwa liczbę
 * w lewo od środka o połowę szerokości jednostki. Technika: mierzymy stałą szerokość końcówki
 * "spacer + W" (nie zmienia się wraz z wartością mocy) i przesuwamy cały blok o połowę tej
 * szerokości w prawo względem zwykłego centrowania - to dokładnie kompensuje przesunięcie,
 * więc liczba wypada na środku przy KAŻDEJ wartości (w tym oczywiście przy 1000 W).
 */
@Composable
private fun DigitalPower(powerW: Double, largeDigits: Boolean, modifier: Modifier = Modifier) {
    val animatedPower by animateFloatAsState(
        targetValue = powerW.toFloat().coerceIn(0f, 9999f),
        animationSpec = tween(300),
        label = "power",
    )
    var trailingWidthPx by remember { mutableStateOf(0) }
    // Dodatkowe przesuniecie w prawo o szerokosc dwoch cyfr (na zadanie) - ponad dokladne
    // centrowanie liczone z trailingWidthPx. Mierzone raz z widmowego "00" (tabularne cyfry,
    // wiec kazda para cyfr ma te sama szerokosc).
    var twoDigitsWidthPx by remember { mutableStateOf(0) }
    val density = androidx.compose.ui.platform.LocalDensity.current

    val powerFontSize = if (largeDigits) 56.sp else 44.sp
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            "00",
            fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = powerFontSize, letterSpacing = (-1).sp,
            style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"),
            modifier = Modifier
                .alpha(0f)
                .onGloballyPositioned { twoDigitsWidthPx = it.size.width },
        )
        // Na zadanie: przesuniecie o dodatkowe 1,5 szerokosci znaku w lewo wzgledem powyzszego
        // dokladnego centrowania - stad odejmujemy 1,5 * oneCharWidthPx (polowa twoDigitsWidthPx)
        // od dotychczasowego przesuniecia w prawo o cale dwa znaki.
        val oneCharWidthPx = twoDigitsWidthPx / 2f
        Row(
            modifier = Modifier.offset(x = with(density) { (trailingWidthPx / 2 + twoDigitsWidthPx - 1.5f * oneCharWidthPx).toDp() }),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                animatedPower.roundToInt().toString(),
                fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = powerFontSize,
                letterSpacing = (-1).sp, color = HighContrastText,
                style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"),
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.onGloballyPositioned { trailingWidthPx = it.size.width },
            ) {
                Spacer(Modifier.size(6.dp))
                Text(
                    "W",
                    fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    color = Tokens.TextSecondary, modifier = Modifier.padding(bottom = 8.dp),
                )
            }
        }
    }
}

/**
 * Kwadratowy kafelek temperatury sterownika (Tc) - kafelka Kokpitu (zakładka "Temperature control").
 * Trzy stany, rosnąca dotkliwość: normalny (szare tło) → [warning] podświetla na pomarańczowo
 * (stałe tło, bez migania) → [alarm] miga na czerwono (nieskończona animacja alpha) - progi
 * ustawiane w tamtej zakładce jako "Warning"/"Alarm".
 */
@Composable
private fun TempTile(valueC: Int, warning: Boolean, alarm: Boolean, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "tempBlink")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(500), repeatMode = RepeatMode.Reverse),
        label = "tempBlinkAlpha",
    )
    val bg = when {
        alarm -> Tokens.Red.copy(alpha = blinkAlpha)
        warning -> Tokens.Amber
        else -> TileBg
    }
    val border = when {
        alarm -> Tokens.Red
        warning -> Tokens.Amber
        else -> HighContrastBorder
    }
    Box(
        modifier = modifier
            .size(56.dp)
            .background(bg, RoundedCornerShape(12.dp))
            .border(1.dp, border, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Tc", fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = HighContrastText.copy(alpha = 0.75f))
            Text("$valueC°", fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = HighContrastText)
        }
    }
}

/**
 * Jednostka złożona (np. "Wh/km") jako klasyczny ułamek dziesiętny: licznik nad kreską,
 * mianownik pod nią - zamiast "Wh/km" w jednej linii, które przy dużej wartości liczbowej
 * nie mieściło się w kafelce StatCard. Czcionka linii ułamka dobrana tak, by całość (2 linie
 * tekstu + kreska) nie przekraczała wysokości cyfry wartości (fontSize 22.sp) - kafelka nie
 * rozciąga się w pionie. Szerokość Column ograniczona do treści (IntrinsicSize.Min), żeby
 * kreska miała dokładnie szerokość szerszego z dwóch słów.
 */
@Composable
private fun FractionUnit(numerator: String, denominator: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.width(IntrinsicSize.Min),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(numerator, fontFamily = Manrope, fontWeight = FontWeight.Medium, fontSize = 8.sp, lineHeight = 8.sp, color = color)
        Box(
            Modifier
                .padding(vertical = 0.5.dp)
                .fillMaxWidth()
                .height(1.dp)
                .background(color),
        )
        Text(denominator, fontFamily = Manrope, fontWeight = FontWeight.Medium, fontSize = 8.sp, lineHeight = 8.sp, color = color)
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    largeDigits: Boolean = false,
    onReset: (() -> Unit)? = null,
    onInfoClick: (() -> Unit)? = null,
) {
    val bg = if (highlighted) Tokens.Red else TileBg
    val border = if (highlighted) Tokens.Red else HighContrastBorder
    val textColor = if (highlighted) Color.White else HighContrastText
    val labelColor = if (highlighted) Color.White.copy(alpha = 0.85f) else Tokens.TextSecondary

    // Ikona resetu (jeśli jest) leży POZA paddingiem karty (align na całym Box, nie na wnętrzu
    // po padding(12dp)) - dzięki temu siedzi bliżej faktycznego rogu kafelka, nie jest wciśnięta
    // przez wewnętrzny odstęp treści. Klik na całą kafelkę (onInfoClick) otwiera dialog z
    // wyjaśnieniem, jak dana wartość jest liczona - nie koliduje z ikoną resetu, bo to osobny,
    // "wyższy" element z własnym clickable (Compose oddaje mu dotknięcie w pierwszej kolejności).
    Box(
        modifier = modifier
            .border(1.dp, border, RoundedCornerShape(16.dp))
            .background(bg, RoundedCornerShape(16.dp))
            .let { if (onInfoClick != null) it.clickable { onInfoClick() } else it },
    ) {
        // Padding/odstep pomniejszone gdy largeDigits, zeby skompensowac wyzsza linie wartosci
        // (22sp->28sp) i utrzymac identyczna calkowita wysokosc kafelka - patrz duzy komentarz
        // w DisplayScreen.kt przy przelaczniku "Powiekszone cyfry na Kokpicie".
        val cardPadding = if (largeDigits) 9.dp else 12.dp
        val labelValueGap = if (largeDigits) 2.dp else 6.dp
        Column(Modifier.fillMaxWidth().padding(cardPadding), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                // Gdy jest ikona resetu (TopEnd, poza tym Column) - rezerwujemy dla niej miejsce
                // z prawej, zeby wysrodkowana etykieta na nia nie nachodzila w waskich (1/3) kafelkach.
                modifier = Modifier.fillMaxWidth().padding(end = if (onReset != null) 18.dp else 0.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    label.uppercase(),
                    fontFamily = Manrope, fontWeight = FontWeight.Medium, fontSize = 10.sp,
                    letterSpacing = 0.6.sp, color = labelColor, textAlign = TextAlign.Center,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                if (onInfoClick != null) {
                    Spacer(Modifier.width(4.dp))
                    // Ten sam glif/kolor co trójkąt rozwijania w polach programowania
                    // (ExpandableParamTile) - sygnalizuje "dotknij, żeby zobaczyć więcej",
                    // spójnie z resztą apki (dawniej było "ⓘ").
                    Text("▼", fontFamily = Manrope, fontSize = 9.sp, color = Tokens.Emerald)
                }
            }
            Spacer(Modifier.height(labelValueGap))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = if (largeDigits) 28.sp else 22.sp, color = textColor)
                if (unit.isNotEmpty()) {
                    Spacer(Modifier.size(4.dp))
                    if (unit.contains("/")) {
                        // Wh/km jako klasyczny ułamek dziesiętny (licznik/kreska/mianownik) zamiast
                        // "Wh/km" w jednej linii - ta forma jest węższa, więc mieści się obok dużej
                        // wartości bez rozciągania kafelki w pionie (patrz FractionUnit).
                        val (numerator, denominator) = unit.split("/", limit = 2)
                        FractionUnit(numerator, denominator, labelColor, modifier = Modifier.padding(bottom = 1.dp))
                    } else {
                        Text(unit, fontFamily = Manrope, fontSize = 12.sp, color = labelColor, modifier = Modifier.padding(bottom = 3.dp))
                    }
                }
            }
        }
        if (onReset != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(20.dp)
                    .background(Tokens.Blue, CircleShape)
                    .clickable { onReset() },
                contentAlignment = Alignment.Center,
            ) {
                Text("↺", fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
            }
        }
    }
}

/** Kwadratowy przycisk z zaokrąglonymi rogami - wysoki kontrast. 84dp = 120% poprzednich 70dp
 * (łatwiejsze trafienie w czasie jazdy) - zwęża proporcjonalnie sąsiednie ToggleTile (Światło/Hamulec,
 * weight(1f)) w tym samym Row, bez poszerzania całego wiersza (ich wysokość zostaje bez zmian). */
@Composable
private fun SquareButton(label: String, largeDigits: Boolean = false, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(84.dp)
            .background(TileBg, RoundedCornerShape(14.dp))
            .border(1.5.dp, HighContrastBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = if (largeDigits) 36.sp else 31.sp, color = HighContrastText)
    }
}

/** Przycisk/wskaźnik stanu (Światło, Hamulec, Sport) - podświetla się kolorem, gdy aktywny. onClick=null → tylko wskaźnik. */
@Composable
private fun ToggleTile(
    label: String,
    active: Boolean,
    activeColor: Color,
    activeTextColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    onClick: (() -> Unit)? = null,
) {
    val bg = if (active) activeColor else TileBg
    val border = if (active) activeColor else HighContrastBorder
    val textColor = if (active) activeTextColor else HighContrastText
    Box(
        modifier = modifier
            .height(height)
            .background(bg, RoundedCornerShape(14.dp))
            .border(1.5.dp, border, RoundedCornerShape(14.dp))
            .let { if (onClick != null) it.clickable { onClick() } else it },
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = fontSize, color = textColor, maxLines = 1)
    }
}

/**
 * Ikona obok OFFLINE gdy trwa auto-reconnect po zerwaniu połączenia na Kokpicie (patrz
 * AppViewModel.onSerialError/handleConnectFailure): wirujący pierścień podczas prób,
 * czerwony wykrzyknik gdy wszystkie MAX_RECONNECT_ATTEMPTS próby zawiodły.
 */
@Composable
private fun ReconnectBadge(state: AutoReconnectState) {
    when (state) {
        AutoReconnectState.RETRYING -> {
            Text(tr("Łączę ponownie…", "Connecting…", de = "Verbinde…", fr = "Connexion…", es = "Conectando…", pt = "Conectando…", it = "Connessione…", nl = "Verbinden…", sv = "Ansluter igen…", cs = "Připojuji znovu…", sk = "Pripájam znova…"), fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Tokens.Blue)
        }
        AutoReconnectState.FAILED -> {
            Box(Modifier.size(16.dp).background(Tokens.Red, CircleShape), contentAlignment = Alignment.Center) {
                Text("!", fontFamily = Sora, fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color.White)
            }
        }
        AutoReconnectState.IDLE -> Unit
    }
}

/** Wskaźnik baterii - używany w górnym pasku (przy hamburgerze). scale=1.5 → 150% wielkości bazowej. */
@Composable
fun BatteryPill(pct: Int, scale: Float = 1f, modifier: Modifier = Modifier) {
    val color = when {
        pct >= 40 -> Tokens.Emerald
        pct >= 18 -> Tokens.Amber
        else -> Tokens.Red
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        // Mniejszy margines poziomy niz kiedys (byl 12*scale) - zeby wskaznik mial wiecej
        // miejsca do rozrostu (100% = 3 cyfry) zanim dotknie wysrodkowanego logo w TopBar.
        modifier = modifier
            .padding(horizontal = (4 * scale).dp, vertical = (6 * scale).dp),
    ) {
        // glif baterii: obrys + wypełnienie proporcjonalne
        Box(
            Modifier
                .size(width = (22 * scale).dp, height = (11 * scale).dp)
                .border(1.dp, HighContrastText, RoundedCornerShape((3 * scale).dp))
                .padding((2 * scale).dp),
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (pct / 100f).coerceIn(0.05f, 1f))
                    .background(color, RoundedCornerShape(1.dp)),
            )
        }
        Spacer(Modifier.size((7 * scale).dp))
        Text("$pct%", fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = (13 * scale).sp, color = HighContrastText)
    }
}
