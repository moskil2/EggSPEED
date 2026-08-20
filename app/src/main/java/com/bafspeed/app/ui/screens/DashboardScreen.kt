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
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens
import kotlin.math.roundToInt

/** Kokpit: wysoki kontrast do czytelności w pełnym słońcu - mocniejsze obramowania niż reszta apki. */
private val HighContrastBorder = Color(0x40FFFFFF) // ~25% biały, znacznie mocniejszy niż Tokens.Border (6%)
private val HighContrastText = Color(0xFFFFFFFF)
private val TileBg = Color(0xFF121418)

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

    val avgSpeedLabel = tr("Pręd. śr.", "Avg. speed")
    val avgSpeedInfo = tr(
        "Dystans podzielony przez czas W RUCHU od ostatniego resetu - postoje (np. na światłach) nie obniżają średniej.",
        "Distance divided by time spent moving since the last reset - stops (e.g. at traffic lights) don't lower the average.",
    )
    val avgUsageLabel = tr("Śr. zużycie", "Avg. usage")
    val avgUsageInfo = tr(
        "Cała energia zużyta od ostatniego resetu TRIP, podzielona przez przejechany dystans - czysta średnia dla całej trasy.",
        "Total energy used since the last TRIP reset, divided by the distance traveled - a plain average for the whole trip.",
    )
    val actUsageLabel = tr("Chwil. zuż.", "Act. usage")
    val actUsageInfo = tr(
        "Krótkoterminowa średnia krocząca (ostatnie ok. 300 m) - szybko reaguje na Twój aktualny styl jazdy, w odróżnieniu od średniej z całej trasy.",
        "Short-term rolling average (roughly the last 300 m) - reacts quickly to your current riding style, unlike the whole-trip average.",
    )
    val rangeLabel = tr("Zasięg ~", "Range ~")
    val rangeInfo = tr(
        "Pozostała energia baterii (bateria % × pojemność) podzielona przez zużycie Wh/km. To zużycie to mieszanka Twojej długoterminowej historii jazdy i stylu z ostatnich kilku km - na starcie trasy liczy się głównie historia, po ok. 4 km coraz bardziej bieżący styl jazdy.",
        "Remaining battery energy (battery % × capacity) divided by Wh/km usage. That usage blends your long-term riding history with your style from the last few km - mostly history at the start of a trip, shifting toward your current style after about 4 km.",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
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
        // liczby nadal miesza sie w layoucie. Predkosc ograniczona do 99,9 - to twardy limit
        // samego DigitalSpeed (skalowany pod maks. 2 cyfry), nie ma sensu podawac wiecej.
        val t = state.testMode

        // Prędkość - czysto cyfrowa, bez pierścienia, jednostka po prawej stronie odczytu
        DigitalSpeed(speedKmh = if (t) unit.toKmh(99.9) else telemetry.speedKmh, unit = unit, modifier = Modifier.fillMaxWidth())

        // Moc - pod prędkością, mniejsza czcionka. Kafelek Tc (zakładka "Temperature control", tylko
        // bbs-fw - Tm/rejestr 0x21 zawsze zwraca 0 na bbs-fw, patrz PROTOKOL_BBSFW.md sekcja 5)
        // nakłada się po lewej stronie tego bloku, na wysokości odczytu mocy - test mode wymusza
        // 100°C, tak samo jak wymusza skrajne wartości predkosci/mocy powyzej.
        val tempControllerC = if (t) 100 else telemetry.tempControllerC
        Box(Modifier.fillMaxWidth().horizontalBleed(6.dp)) {
            DigitalPower(powerW = if (t) 3000.0 else telemetry.powerW, modifier = Modifier.fillMaxWidth())
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
            StatCard(tr("Dystans", "Distance"), if (t) "10000.0" else String.format("%.1f", unit.fromKmh(state.totalOdoKm)), unit.distanceLabel, Modifier.weight(1f))
            StatCard(
                "Trip", if (t) "1000.0" else String.format("%.1f", unit.fromKmh(state.tripKm)), unit.distanceLabel, Modifier.weight(1f),
                onReset = { showResetTripConfirm = true },
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = statRowModifier) {
            StatCard(tr("Prąd", "Current"), if (t) "80.0" else String.format("%.1f", telemetry.currentA), "A", Modifier.weight(1f))
            StatCard(
                tr("Napięcie", "Voltage"),
                // Bez polaczenia pokazujemy ostatnio znane napiecie (UiState.lastKnownVoltageV)
                // zamiast zerowac kafelke do 0,0.
                if (t) "80.0" else String.format("%.1f", if (connected) telemetry.voltageV else state.lastKnownVoltageV),
                "V", Modifier.weight(1f),
            )
            StatCard(
                avgSpeedLabel, if (t) "80.8" else String.format("%.1f", unit.fromKmh(state.avgSpeedKmh)), unit.label, Modifier.weight(1f),
                onReset = { showResetAvgSpeedConfirm = true },
                onInfoClick = { infoDialog = avgSpeedLabel to avgSpeedInfo },
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = statRowModifier) {
            StatCard(
                avgUsageLabel, if (t) "800.0" else String.format("%.1f", state.tripAvgWhPerKm), "Wh/km", Modifier.weight(1f),
                onInfoClick = { infoDialog = avgUsageLabel to avgUsageInfo },
            )
            StatCard(
                actUsageLabel, if (t) "800.0" else String.format("%.1f", state.currentAvgWhPerKm), "Wh/km", Modifier.weight(1f),
                onInfoClick = { infoDialog = actUsageLabel to actUsageInfo },
            )
            StatCard(
                rangeLabel, if (t) "800" else unit.fromKmh(state.predictedRangeKm).roundToInt().toString(), unit.distanceLabel, Modifier.weight(1f),
                onInfoClick = { infoDialog = rangeLabel to rangeInfo },
            )
        }

        if (showResetTripConfirm) {
            AlertDialog(
                containerColor = Tokens.Card,
                onDismissRequest = { showResetTripConfirm = false },
                title = { Text(tr("Zresetować TRIP?", "Reset TRIP?"), fontFamily = Sora, fontWeight = FontWeight.Bold, color = Tokens.TextPrimary) },
                text = {
                    Text(
                        tr("Czy na pewno chcesz zresetować TRIP?", "Are you sure you want to reset TRIP?"),
                        fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextSecondary,
                    )
                },
                confirmButton = {
                    TextButton(onClick = { onResetTrip(); showResetTripConfirm = false }) {
                        Text(tr("Tak", "Yes"), color = Tokens.Blue, fontFamily = Manrope, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetTripConfirm = false }) {
                        Text(tr("Nie", "No"), color = Tokens.TextTertiary, fontFamily = Manrope)
                    }
                },
            )
        }

        if (showResetAvgSpeedConfirm) {
            AlertDialog(
                containerColor = Tokens.Card,
                onDismissRequest = { showResetAvgSpeedConfirm = false },
                title = { Text(tr("Zresetować prędkość średnią?", "Reset average speed?"), fontFamily = Sora, fontWeight = FontWeight.Bold, color = Tokens.TextPrimary) },
                text = {
                    Text(
                        tr("Czy na pewno chcesz zresetować prędkość średnią?", "Are you sure you want to reset the average speed?"),
                        fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextSecondary,
                    )
                },
                confirmButton = {
                    TextButton(onClick = { onResetAvgSpeed(); showResetAvgSpeedConfirm = false }) {
                        Text(tr("Tak", "Yes"), color = Tokens.Blue, fontFamily = Manrope, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetAvgSpeedConfirm = false }) {
                        Text(tr("Nie", "No"), color = Tokens.TextTertiary, fontFamily = Manrope)
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
                        Text(tr("OK", "OK"), color = Tokens.Blue, fontFamily = Manrope, fontWeight = FontWeight.Bold)
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
                        fontSize = if (selected) 34.sp else 18.sp,
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
        val toggleTileFontSize = if (showSportMode) 13.sp else 15.sp
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SquareButton("−") { onAssistChange(state.assistLevel - 1) }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(COMPACT_TILE_GAP)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ToggleTile(
                        label = tr("Światło", "Light"),
                        active = state.lightOn,
                        activeColor = Tokens.Amber,
                        activeTextColor = Tokens.OnAccent,
                        modifier = Modifier.weight(1f),
                        height = toggleTileHeight,
                        fontSize = toggleTileFontSize,
                        onClick = onLightToggle,
                    )
                    ToggleTile(
                        label = tr("Hamulec", "Brake"),
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
                        label = tr("Tryb: Sport", "Mode: Sport"),
                        active = state.sportMode,
                        activeColor = Tokens.Red,
                        activeTextColor = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        height = COMPACT_TILE_HEIGHT,
                        fontSize = 13.sp,
                        onClick = onSportModeToggle,
                    )
                }
            }
            SquareButton("+") { onAssistChange(state.assistLevel + 1) }
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
                ),
                fontFamily = Manrope, fontSize = 10.sp, color = Tokens.Amber, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(6.dp))
    }
}

/** Prędkość jako duża, cyfrowa wartość - skalowana pod maks. 99.9. Bez pierścienia/gauge'a. */
@Composable
private fun DigitalSpeed(speedKmh: Double, unit: SpeedUnit, modifier: Modifier = Modifier) {
    val animatedSpeed by animateFloatAsState(
        targetValue = unit.fromKmh(speedKmh).toFloat(),
        animationSpec = tween(300),
        label = "speed",
    )
    // Zakotwiczone do prawej krawędzi - przy zmianie liczby cyfr (9,9 → 10,0) prawa
    // krawędź (i jednostka) zostaje w miejscu, liczba rośnie tylko w lewo (nie "ucieka" po ekranie).
    // Cyfra po przecinku pomniejszona do 60% - dzięki Arrangement.End całość (część całkowita +
    // mniejsza część dziesiętna) i tak zostaje dosunięta do jednostki KM/H, bez dodatkowego kodu.
    val formattedSpeed = String.format("%.1f", animatedSpeed.coerceIn(0f, 99.9f))
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
            fontFamily = Sora, fontWeight = FontWeight.ExtraBold, fontSize = 62.sp,
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
private fun DigitalPower(powerW: Double, modifier: Modifier = Modifier) {
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

    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            "00",
            fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 44.sp, letterSpacing = (-1).sp,
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
                fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 44.sp,
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
        Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
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
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = textColor)
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

/** Kwadratowy przycisk z zaokrąglonymi rogami - wysoki kontrast. 70dp = 125% pierwotnych 56dp, zwęża
 * proporcjonalnie sąsiednie ToggleTile (Światło/Hamulec, weight(1f)) w tym samym Row. */
@Composable
private fun SquareButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(70.dp)
            .background(TileBg, RoundedCornerShape(14.dp))
            .border(1.5.dp, HighContrastBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = HighContrastText)
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
            Text(tr("Łączę ponownie…", "Connecting…"), fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Tokens.Blue)
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
