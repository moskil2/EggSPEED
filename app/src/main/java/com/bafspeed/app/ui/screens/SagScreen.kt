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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bafspeed.app.ConnectionStatus
import com.bafspeed.app.SagCalibrationPhase
import com.bafspeed.app.UiState
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.ui.components.ExpandableParamTile
import com.bafspeed.app.ui.components.MicroLabel
import com.bafspeed.app.ui.components.TokenCard
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens
import java.util.concurrent.TimeUnit

/**
 * Zakladka "SAG" - dwie wartosci: SAG orientacyjny (liczony pasywnie w tle podczas zwyklej jazdy, patrz
 * AppViewModel.sampleSagPassive) i wynik Pomiaru SAG (obciazenie kontrolowane, wiec dokladniejszy i
 * porownywalny miedzy pomiarami). Zadna z nich nie jest pomiarem rezystancji w sensie inzynierskim -
 * obie to pochodna prostego spadku napiecia (SAG / prad) wzgledem napiecia spoczynkowego.
 */
@Composable
fun SagScreen(state: UiState, onStart: () -> Unit, onCancel: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Tokens.Bg)
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = 16.dp)),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ExpandableParamTile(
            label = "SAG",
            valueLabel = "",
            descriptionColor = Tokens.TextPrimary,
            description = tr(
                "SAG to spadek napięcia baterii pod obciążeniem względem napięcia spoczynkowego. To NIE jest " +
                    "pomiar rezystancji w sensie inżynierskim - to prosta pochodna (SAG / prąd), licząca się tylko " +
                    "z napięcia i prądu, przydatna do porównań między pomiarami i do wykrywania pogarszania się pakietu w czasie.",
                "SAG is the battery voltage drop under load relative to its resting voltage. This is NOT an " +
                    "engineering-grade resistance measurement - it's a simple derived value (SAG / current), based only " +
                    "on voltage and current, useful for comparing measurements and spotting pack degradation over time.",
            ),
        ) {}

        MicroLabel(tr("Jakość baterii wg SAG", "Battery quality by SAG"))
        TokenCard(borderColor = Tokens.WhiteBorder) {
            SagLegendRow(tr("≤ 2,0 V", "≤ 2.0 V"), tr("Doskonała", "Excellent"), Tokens.Emerald)
            SagLegendRow(tr("2,0 - 4,0 V", "2.0 - 4.0 V"), tr("Wystarczająca do Twoich zastosowań", "Sufficient for your use"), Tokens.Emerald)
            SagLegendRow(tr("4,0 - 6,0 V", "4.0 - 6.0 V"), tr("Na pograniczu - warto obserwować", "Borderline - worth watching"), Tokens.Amber)
            SagLegendRow(tr("> 6,0 V", "> 6.0 V"), tr("Zbyt niska jakość / uszkodzona", "Too low quality / damaged"), Tokens.Red, last = true)
        }

        ExpandableParamTile(
            label = tr("SAG orientacyjny", "Estimated SAG"),
            valueLabel = "",
            descriptionColor = Tokens.TextTertiary,
            description = tr(
                "Liczony tylko wtedy, gdy chwilowo używasz co najmniej 60% mocy maksymalnej silnika, i to " +
                    "nieprzerwanie przez min. 5 sekund (krótsze szarpnięcia prądem są odrzucane jako niewiarygodne). " +
                    "Wartość orientacyjna, adekwatna do TWOJEGO stylu jazdy - liczona z takich próbek i wygładzana, " +
                    "więc reaguje powoli. Przy niskim obciążeniu silnika (spokojna jazda, rzadkie przekraczanie 60%) " +
                    "wynik będzie wyglądał lepiej (niższy SAG), niż realnie pokazałby pełny Pomiar SAG poniżej.",
                "Only calculated while you're momentarily using at least 60% of the motor's max power, and only if " +
                    "held continuously for at least 5 seconds (shorter current spikes are discarded as unreliable). " +
                    "An approximate value, matched to YOUR riding style - calculated from such samples and smoothed, " +
                    "so it reacts slowly. Under low motor load (gentle riding, rarely crossing 60%) the result will " +
                    "look better (lower SAG) than a full SAG Measurement below would actually show.",
            ),
        ) {
            val sagV = state.everydaySagAtMaxCurrentV
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        tr("Projekcja przy max prądzie", "Projected at max current"),
                        fontFamily = Manrope, fontSize = 11.sp, color = Tokens.TextSecondary,
                    )
                    Box(Modifier.height(40.dp), contentAlignment = Alignment.CenterStart) {
                        Text(
                            if (sagV != null) "${String.format("%.1f", sagV)} V" else "-- V",
                            fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 26.sp,
                            color = if (sagV != null) Tokens.TextPrimary else Tokens.TextTertiary,
                        )
                    }
                }
                if (sagV != null) SagBadge(sagV)
                Spacer(Modifier.size(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${String.format("%.0f", state.maxCurrentACalibratedOrDefault)} A",
                        fontFamily = Manrope, fontSize = 12.sp, color = Tokens.TextTertiary,
                    )
                    if (!state.maxCurrentAIsKnown) {
                        Text(
                            tr("nieznany, połącz się", "unknown, connect first"),
                            fontFamily = Manrope, fontSize = 9.sp, color = Tokens.TextTertiary,
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        MicroLabel(tr("Pomiar SAG", "SAG Measurement"))
        TokenCard(borderColor = Tokens.WhiteBorder) {
            Text(
                tr(
                    "Dużo dokładniejszy niż SAG orientacyjny, bo wymusza maksymalne obciążenie baterii w kontrolowanych " +
                        "warunkach - niezależny od tego, jak akurat jeździsz.",
                    "Much more accurate than Estimated SAG, because it forces maximum battery load under controlled " +
                        "conditions - independent of however you happen to be riding.",
                ),
                fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp, color = Tokens.TextPrimary,
            )
            Spacer(Modifier.height(8.dp))

            var instructionsExpanded by remember { mutableStateOf(false) }
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { instructionsExpanded = !instructionsExpanded },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    tr("Jak wykonać pomiar", "How to run the measurement"),
                    fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Tokens.TextSecondary,
                    modifier = Modifier.weight(1f),
                )
                Text(if (instructionsExpanded) "▲" else "▼", fontFamily = Manrope, fontSize = 14.sp, color = Tokens.Emerald)
            }
            if (instructionsExpanded) {
                Spacer(Modifier.height(6.dp))
                Text(
                    tr(
                        "Wykonuj przy naładowaniu baterii 50-100%. Najlepiej pod lekką górkę, ewentualnie na płaskim. " +
                            "Ogranicz pedałowanie do minimum - ustaw najmniejszą (najcięższą) zębatkę z tyłu, żeby zmaksymalizować " +
                            "obciążenie silnika. Użyj manetki (jeśli jest) lub jedź tak, żeby uzyskać maksymalną moc wspomagania - " +
                            "staraj się obciążyć rower jak najmocniej przez cały czas trwania testu.",
                        "Do this with the battery at 50-100%. Ideally on a slight uphill, flat ground as a fallback. " +
                            "Minimize pedaling - use the smallest (heaviest) rear cog to maximize motor load. Use the throttle " +
                            "(if you have one) or ride so you get maximum assist power - try to load the bike as hard as " +
                            "possible for the whole test.",
                    ),
                    fontFamily = Manrope, fontSize = 12.sp, lineHeight = 17.sp, color = Tokens.TextSecondary,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                tr(
                    "Przebieg: 2 min odczekania bez jazdy (stabilizacja napięcia) → 30s pełnego obciążenia → 2 min odczekania bez jazdy (apka liczy SAG).",
                    "Sequence: 2 min waiting without riding (voltage stabilizes) → 30s full load → 2 min waiting without riding (app calculates SAG).",
                ),
                fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 17.sp, color = Tokens.Amber,
            )
            Spacer(Modifier.height(10.dp))

            when (state.sagCalibrationPhase) {
                SagCalibrationPhase.IDLE -> {
                    MeasureStartButton(
                        enabled = state.connection == ConnectionStatus.CONNECTED,
                        onClick = onStart,
                    )
                    if (state.connection != ConnectionStatus.CONNECTED) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            tr("Połącz się ze sterownikiem, żeby wykonać pomiar.", "Connect to the controller to run the measurement."),
                            fontFamily = Manrope, fontSize = 11.sp, color = Tokens.TextTertiary,
                        )
                    }
                }
                else -> MeasureProgress(phase = state.sagCalibrationPhase, remainingS = state.sagCalibrationRemainingS, onCancel = onCancel)
            }
        }

        MicroLabel(tr("Ostatni wynik pomiaru", "Last measurement result"))
        TokenCard(borderColor = Tokens.WhiteBorder) {
            if (state.sagCalibrationResultV == null) {
                Text(
                    tr("Brak pomiaru - wykonaj pierwszy Pomiar SAG powyżej.", "No measurement yet - run your first SAG Measurement above."),
                    fontFamily = Manrope, fontSize = 12.sp, color = Tokens.TextTertiary,
                )
            } else {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    ResultRow(tr("SAG pod obciążeniem", "SAG under load"), "${String.format("%.2f", state.sagCalibrationResultV)} V", modifier = Modifier.weight(1f))
                    SagBadge(state.sagCalibrationResultV)
                }
                state.sagCalibrationResultResistanceOhm?.let {
                    ResultRow(tr("Efektywna rezystancja", "Effective resistance"), "${String.format("%.0f", it * 1000)} mΩ")
                }
                state.sagCalibrationResultCurrentA?.let {
                    ResultRow(tr("Prąd testu", "Test current"), "${String.format("%.1f", it)} A")
                }
                state.sagCalibrationResultSocPct?.let {
                    ResultRow(tr("Naładowanie na starcie", "Charge at start"), "$it %")
                }
                state.sagCalibrationResultTimestampMs?.let {
                    ResultRow(tr("Kiedy", "When"), formatAgo(it))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SagLegendRow(range: String, label: String, color: Color, last: Boolean = false) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(3.dp)),
        )
        Spacer(Modifier.size(8.dp))
        Text(range, fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Tokens.TextPrimary, modifier = Modifier.width(84.dp))
        Text(label, fontFamily = Manrope, fontSize = 12.sp, lineHeight = 15.sp, color = Tokens.TextSecondary, modifier = Modifier.weight(1f))
    }
    if (!last) {
        HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
    }
}

/** Kolorowa plakietka jakości baterii wg SAG - te same progi co [SagLegendRow] powyżej. */
@Composable
private fun SagBadge(sagV: Double) {
    val color = when {
        sagV <= 4.0 -> Tokens.Emerald
        sagV <= 6.0 -> Tokens.Amber
        else -> Tokens.Red
    }
    Box(
        Modifier
            .background(color.copy(alpha = 0.16f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            when {
                sagV <= 2.0 -> tr("Doskonała", "Excellent")
                sagV <= 4.0 -> tr("Wystarczająca", "Sufficient")
                sagV <= 6.0 -> tr("Na pograniczu", "Borderline")
                else -> tr("Zbyt niska", "Too low")
            },
            fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = color,
        )
    }
}

@Composable
private fun MeasureStartButton(enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (enabled) Tokens.Blue else Tokens.Elevated, RoundedCornerShape(14.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            tr("Rozpocznij pomiar", "Start measurement"),
            fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 15.sp,
            color = if (enabled) Tokens.OnAccent else Tokens.TextTertiary,
        )
    }
}

@Composable
private fun MeasureProgress(phase: SagCalibrationPhase, remainingS: Int, onCancel: () -> Unit) {
    val label = when (phase) {
        SagCalibrationPhase.PRE_WAIT -> tr("Odczekaj - nie jedź, napięcie się stabilizuje", "Wait - don't ride, voltage is stabilizing")
        SagCalibrationPhase.LOADING -> tr("TERAZ! Obciąż rower maksymalnie", "NOW! Load the bike as hard as possible")
        SagCalibrationPhase.POST_WAIT -> tr("Zatrzymaj rower i odczekaj - trwa liczenie", "Stop the bike and wait - calculating")
        SagCalibrationPhase.IDLE -> ""
    }
    val accent = if (phase == SagCalibrationPhase.LOADING) Tokens.Amber else Tokens.Blue

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            String.format("%d:%02d", remainingS / 60, remainingS % 60),
            fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 40.sp, color = accent,
        )
        Spacer(Modifier.height(6.dp))
        Text(label, fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Tokens.TextPrimary)
        Spacer(Modifier.height(10.dp))
        Text(
            tr("Anuluj", "Cancel"),
            fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Tokens.Red,
            modifier = Modifier
                .clickable { onCancel() }
                .padding(6.dp),
        )
    }
}

@Composable
private fun ResultRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextSecondary, modifier = Modifier.weight(1f))
        Text(value, fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Tokens.TextPrimary)
    }
}

@Composable
private fun formatAgo(timestampMs: Long): String {
    val diffMs = System.currentTimeMillis() - timestampMs
    val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
    return when {
        hours < 1 -> tr("mniej niż godzinę temu", "less than an hour ago")
        hours < 24 -> tr("$hours godz. temu", "$hours h ago")
        else -> tr("${hours / 24} dni temu", "${hours / 24} days ago")
    }
}
