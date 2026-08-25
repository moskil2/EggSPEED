package com.bafspeed.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bafspeed.app.MonitoringChart
import com.bafspeed.app.MonitoringSample
import com.bafspeed.app.MonitoringState
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.ui.components.MicroLabel
import com.bafspeed.app.ui.components.PreviewBanner
import com.bafspeed.app.ui.components.TokenCard
import com.bafspeed.app.ui.components.TokenSwitch
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens
import kotlin.math.roundToInt

@Composable
fun MonitoringScreen(
    monitoring: MonitoringState,
    onMasterEnabledChange: (Boolean) -> Unit,
    onChartEnabledChange: (MonitoringChart, Boolean) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Tokens.Bg)
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = 16.dp)),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PreviewBanner(
            tr(
                "Próbkowanie co 0,5s, historia do 10 minut wstecz. Przeciągnij po wykresie, żeby odczytać " +
                    "wartość w wybranym momencie.",
                "Sampling every 0.5s, up to 10 minutes of history. Drag across a chart to read the value " +
                    "at a given moment.",
            ),
            borderWidth = 2.dp,
        )

        TokenCard(contentPadding = 12.dp, contentPaddingVertical = 6.dp) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(tr("Monitoring", "Monitoring"), fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Tokens.TextPrimary)
                    Text(
                        tr("Włącza próbkowanie i rysowanie wykresów poniżej", "Enables sampling and drawing the charts below"),
                        fontFamily = Manrope, fontSize = 12.sp, color = Tokens.TextSecondary,
                    )
                }
                TokenSwitch(checked = monitoring.masterEnabled, onCheckedChange = onMasterEnabledChange, scale = 0.7f)
            }
        }

        MicroLabel(tr("Wykresy", "Charts"))

        // Definicje serii wspólne dla kafelka zbiorczego i osobnych kafelków poniżej - jedno
        // źródło tytułu/jednostki/koloru, żeby oba miejsca się nie rozjechały.
        val allSeries = listOf(
            SeriesSpec(MonitoringChart.POWER, tr("Moc", "Power"), "W", Tokens.Purple, MonitoringSample::powerW),
            SeriesSpec(MonitoringChart.CURRENT, tr("Prąd", "Current"), "A", Tokens.Red, MonitoringSample::currentA),
            SeriesSpec(MonitoringChart.VOLTAGE, tr("Napięcie", "Voltage"), "V", Tokens.Blue, MonitoringSample::voltageV),
            SeriesSpec(MonitoringChart.SPEED, tr("Prędkość", "Speed"), "km/h", Tokens.Emerald, MonitoringSample::speedKmh),
        )

        CombinedMonitoringChartCard(series = allSeries, monitoring = monitoring)

        allSeries.forEach { s ->
            MonitoringChartCard(
                title = s.title,
                unit = s.unit,
                color = s.color,
                chart = s.chart,
                valueSelector = s.valueSelector,
                monitoring = monitoring,
                onChartEnabledChange = onChartEnabledChange,
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

/** Opis jednej serii pomiarowej - wspólny dla kafelka zbiorczego i osobnych kafelków wykresów. */
private data class SeriesSpec(
    val chart: MonitoringChart,
    val title: String,
    val unit: String,
    val color: Color,
    val valueSelector: (MonitoringSample) -> Double,
)

@Composable
private fun MonitoringChartCard(
    title: String,
    unit: String,
    color: Color,
    chart: MonitoringChart,
    valueSelector: (MonitoringSample) -> Double,
    monitoring: MonitoringState,
    onChartEnabledChange: (MonitoringChart, Boolean) -> Unit,
) {
    val chartEnabled = chart in monitoring.enabledCharts
    val active = monitoring.masterEnabled && chartEnabled

    TokenCard(contentPadding = 12.dp, contentPaddingVertical = 6.dp) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                title.uppercase(), fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 11.sp,
                letterSpacing = 1.sp, color = Tokens.TextPrimary, modifier = Modifier.weight(1f),
            )
            TokenSwitch(checked = chartEnabled, onCheckedChange = { onChartEnabledChange(chart, it) }, enabled = monitoring.masterEnabled, scale = 0.7f)
        }
        Spacer(Modifier.height(6.dp))
        if (active && monitoring.samples.isNotEmpty()) {
            LineChart(samples = monitoring.samples, unit = unit, color = color, valueSelector = valueSelector)
        } else {
            Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                Text(
                    when {
                        !monitoring.masterEnabled -> tr("Monitoring wyłączony", "Monitoring is off")
                        !chartEnabled -> tr("Wykres wyłączony", "Chart is off")
                        else -> tr("Brak danych - czekam na próbki", "No data yet - waiting for samples")
                    },
                    fontFamily = Manrope, fontSize = 12.sp, color = Tokens.TextTertiary,
                )
            }
        }
    }
}

/**
 * Kafelek zbiorczy - wszystkie serie na raz na jednym wykresie, każda znormalizowana do
 * własnego zakresu min-max (0-100% wysokości), bo Moc (W), Prąd (A), Napięcie (V) i Prędkość
 * (km/h) mają zupełnie różne skale - bez normalizacji Moc zdominowałaby wykres. Bez nazwy
 * kafelka - poniżej i tak wymienione są wszystkie 4 parametry, więc nagłówek byłby zbędny.
 * Bez własnego przełącznika całości - kafelek żyje razem z globalnym Monitoringiem. Przełączniki
 * poszczególnych serii są CELOWO niezależne od stanu osobnych kafelków wykresów poniżej
 * (monitoring.enabledCharts) - to tylko lokalny, wizualny wybór "co pokazać na tym jednym
 * wykresie", nie wpływa na kafelki pojedynczych parametrów.
 */
@Composable
private fun CombinedMonitoringChartCard(
    series: List<SeriesSpec>,
    monitoring: MonitoringState,
) {
    var visibleCharts by remember { mutableStateOf(series.map { it.chart }.toSet()) }

    TokenCard(contentPadding = 12.dp, contentPaddingVertical = 6.dp) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            series.forEach { s ->
                val checked = s.chart in visibleCharts
                SeriesChip(
                    label = s.title,
                    color = s.color,
                    checked = checked,
                    enabled = monitoring.masterEnabled,
                    onClick = { visibleCharts = if (checked) visibleCharts - s.chart else visibleCharts + s.chart },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        val activeSeries = series.filter { monitoring.masterEnabled && it.chart in visibleCharts }
        if (activeSeries.isNotEmpty() && monitoring.samples.isNotEmpty()) {
            MultiLineChart(samples = monitoring.samples, series = activeSeries)
        } else {
            Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                Text(
                    when {
                        !monitoring.masterEnabled -> tr("Monitoring wyłączony", "Monitoring is off")
                        activeSeries.isEmpty() -> tr("Wszystkie parametry wyłączone", "All parameters are off")
                        else -> tr("Brak danych - czekam na próbki", "No data yet - waiting for samples")
                    },
                    fontFamily = Manrope, fontSize = 12.sp, color = Tokens.TextTertiary,
                )
            }
        }
    }
}

/** Klikalny "chip" włączający/wyłączający jedną serię w kafelku zbiorczym. */
@Composable
private fun SeriesChip(label: String, color: Color, checked: Boolean, enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .background(if (checked) color.copy(alpha = 0.16f) else Tokens.Elevated, RoundedCornerShape(8.dp))
            .let { if (enabled) it.clickable { onClick() } else it }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).background(if (checked) color else Tokens.TextTertiary, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(
            label,
            fontFamily = Manrope, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
            color = if (checked) Tokens.TextPrimary else Tokens.TextTertiary,
            maxLines = 1,
        )
    }
}

/** Wykres wielu znormalizowanych serii na raz, z odczytem rzeczywistej wartości każdej z nich pod kursorem. */
@Composable
private fun MultiLineChart(samples: List<MonitoringSample>, series: List<SeriesSpec>) {
    var cursorFraction by remember { mutableFloatStateOf(1f) }
    val index = (cursorFraction * (samples.size - 1)).roundToInt().coerceIn(0, samples.size - 1)
    val cursorSample = samples[index]
    // Kolor odczytany tutaj (kontekst @Composable) - wnętrze Canvas to DrawScope, nie @Composable,
    // więc nie może odpytać Tokens.TextSecondary bezpośrednio (zależy od LocalHighContrast).
    val cursorLineColor = Tokens.TextSecondary

    Canvas(
        Modifier
            .fillMaxWidth()
            .height(120.dp)
            .pointerInput(samples.size) {
                detectDragGestures { change, _ ->
                    cursorFraction = (change.position.x / size.width).coerceIn(0f, 1f)
                }
            },
    ) {
        val w = size.width
        val h = size.height
        val stepX = if (samples.size > 1) w / (samples.size - 1) else 0f

        series.forEach { s ->
            val values = samples.map(s.valueSelector)
            val minV = values.min()
            val maxV = values.max()
            val range = maxV - minV
            val path = Path()
            values.forEachIndexed { i, v ->
                val x = i * stepX
                // Seria praktycznie stała (np. napięcie w krótkim oknie) - brak realnego zakresu do
                // znormalizowania, rysujemy płasko na ŚRODKU wysokości, zamiast dzielić przez sztuczny
                // fallback zakresu (co zawsze dawało linię przyklejoną do samego dołu wykresu).
                val y = if (range < 1e-6) h / 2f else h - ((v - minV) / range * h).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, color = s.color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
        }

        val cx = cursorFraction * w
        drawLine(
            color = cursorLineColor,
            start = Offset(cx, 0f),
            end = Offset(cx, h),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
        )
    }

    Spacer(Modifier.height(6.dp))
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        series.forEach { s ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).background(s.color, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text(
                    "${s.title}: ${String.format("%.1f", s.valueSelector(cursorSample))} ${s.unit}",
                    fontFamily = Sora, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = Tokens.TextSecondary,
                )
            }
        }
    }
}

/** Wiek próbki pod kursorem jako "teraz" albo "-M:SS" (np. "-1:35") zamiast samych sekund. */
@Composable
private fun formatSampleAge(ageS: Double): String {
    if (ageS < 1.0) return tr("teraz", "now")
    val totalS = ageS.roundToInt()
    val m = totalS / 60
    val s = totalS % 60
    return "-$m:${s.toString().padStart(2, '0')}"
}

/** Prosty wykres liniowy na Canvas (bez zewnętrznej biblioteki) z przesuwanym kursorem odczytu. */
@Composable
private fun LineChart(
    samples: List<MonitoringSample>,
    unit: String,
    color: Color,
    valueSelector: (MonitoringSample) -> Double,
) {
    // Frakcja szerokości wykresu (0..1) - domyślnie 1 (prawa krawędź = najnowsza próbka), więc
    // dopóki użytkownik nie przeciągnął kursora, odczyt "śledzi" na żywo najnowszą wartość.
    var cursorFraction by remember { mutableFloatStateOf(1f) }

    val values = samples.map(valueSelector)
    val index = (cursorFraction * (values.size - 1)).roundToInt().coerceIn(0, values.size - 1)
    val cursorSample = samples[index]
    val ageS = (samples.last().tMs - cursorSample.tMs) / 1000.0
    // Jw. - Canvas to DrawScope, nie @Composable, kolor trzeba odczytać przed wejściem do niego.
    val cursorLineColor = Tokens.TextSecondary

    Canvas(
        Modifier
            .fillMaxWidth()
            .height(120.dp)
            .pointerInput(samples.size) {
                detectDragGestures { change, _ ->
                    cursorFraction = (change.position.x / size.width).coerceIn(0f, 1f)
                }
            },
    ) {
        val w = size.width
        val h = size.height
        val minV = values.min()
        val maxV = values.max()
        val range = maxV - minV
        val stepX = if (values.size > 1) w / (values.size - 1) else 0f

        val path = Path()
        values.forEachIndexed { i, v ->
            val x = i * stepX
            // Jw. (patrz MultiLineChart) - seria praktycznie stała rysowana płasko na środku,
            // zamiast przyklejona do dołu przez sztuczny fallback zakresu.
            val y = if (range < 1e-6) h / 2f else h - ((v - minV) / range * h).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

        val cx = cursorFraction * w
        drawLine(
            color = cursorLineColor,
            start = Offset(cx, 0f),
            end = Offset(cx, h),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
        )
    }

    Spacer(Modifier.height(4.dp))
    Text(
        "${String.format("%.1f", valueSelector(cursorSample))} $unit  ·  ${formatSampleAge(ageS)}",
        fontFamily = Sora, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = Tokens.TextSecondary,
    )
}
