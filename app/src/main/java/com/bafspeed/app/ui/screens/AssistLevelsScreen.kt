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
import com.bafspeed.app.ui.components.ExpandableParamTile
import com.bafspeed.app.ui.components.FlankedSlider
import com.bafspeed.app.ui.components.MicroLabel
import com.bafspeed.app.ui.components.ReadWriteButtons
import com.bafspeed.app.ui.components.TokenCard
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens

/**
 * Zakladka "Poziomy wspomagania" - wybor poziomu PAS 0-9 (kafelki o rownej szerokosci,
 * wszystkie miesza sie na ekranie na raz) + limit predkosci/pradu dla wybranego poziomu
 * (suwaki -/+ krok 1%, ten sam wzorzec co Pedal/Throttle/General).
 */
@Composable
fun AssistLevelsScreen(
    state: UiState,
    onCurrentChange: (level: Int, pct: Int) -> Unit,
    onSpeedChange: (level: Int, pct: Int) -> Unit,
    onRead: () -> Unit,
    onWrite: () -> Unit,
    readWriteEnabled: Boolean,
) {
    var selected by remember { mutableIntStateOf(0) }
    val basic = state.basicOrDefault

    Column(
        Modifier
            .fillMaxSize()
            .background(Tokens.Bg)
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = 16.dp)),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ReadWriteButtons(onRead = onRead, onWrite = onWrite, enabled = readWriteEnabled)

        // Selektor poziomow 0-9 - kazdy kafelek ma rowna szerokosc (weight), wiec wszystkie
        // 10 miesci sie na jednym ekranie bez przewijania w bok, niezaleznie od szerokosci telefonu.
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
                    Text(
                        lvl.toString(),
                        fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                        color = Tokens.OnAccent,
                    )
                }
            }
        }

        ExpandableParamTile(
            label = tr("Poziom $selected - limit prędkości", "Level $selected - speed limit"),
            valueLabel = "${basic.assistSpeedPct[selected]}%",
            description = tr(
                "Procent maksymalnej prędkości (ustawionej w Bafang Pedal (PAS) / Bafang Throttle) " +
                    "dostępny na tym poziomie wspomagania. Np. jeśli limit prędkości to 40km/h, a tu ustawisz 50%, " +
                    "na tym poziomie osiągniesz maksymalnie 20km/h.",
                "Percentage of the maximum speed (set in Bafang Pedal (PAS) / Bafang Throttle) " +
                    "available at this assist level. E.g. if the speed limit is 40km/h and you set 50% here, " +
                    "at this level you'll reach a maximum of 20km/h.",
            ),
        ) {
            FlankedSlider(
                value = basic.assistSpeedPct[selected],
                range = 0..100,
                accent = Tokens.Emerald,
                onValueChange = { onSpeedChange(selected, it) },
            )
        }

        ExpandableParamTile(
            label = tr("Poziom $selected - limit prądu", "Level $selected - current limit"),
            valueLabel = "${basic.assistCurrentPct[selected]}%",
            description = tr(
                "Procent maksymalnego prądu (Current Limit z Bafang Basic) dostępny na tym poziomie " +
                    "wspomagania. Np. jeśli Current Limit to 24A, a tu ustawisz 50%, na tym poziomie silnik dostanie " +
                    "maksymalnie 12A.",
                "Percentage of the maximum current (Current Limit from Bafang Basic) available at this " +
                    "assist level. E.g. if Current Limit is 24A and you set 50% here, at this level the motor will " +
                    "get a maximum of 12A.",
            ),
        ) {
            FlankedSlider(
                value = basic.assistCurrentPct[selected],
                range = 0..100,
                accent = Tokens.Amber,
                onValueChange = { onCurrentChange(selected, it) },
            )
        }

        // Wykres mocy - wszystkie poziomy na raz (tylko podglad, bez edycji)
        TokenCard(borderColor = Color(0x59FFFFFF)) {
            MicroLabel(tr("Rozkład mocy - wszystkie poziomy", "Power distribution - all levels"))
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                basic.assistCurrentPct.forEachIndexed { i, pct ->
                    val color = if (i == selected) Tokens.Amber else Tokens.Blue
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height((pct.coerceIn(2, 100) * 0.9f).dp)
                                .background(color, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(i.toString(), fontFamily = Manrope, fontSize = 10.sp, color = Tokens.TextSecondary)
                    }
                }
            }
        }

        // Wykres predkosci - wszystkie poziomy na raz (tylko podglad, bez edycji) - identyczny
        // uklad co wykres mocy powyzej, ale dla assistSpeedPct.
        TokenCard(borderColor = Color(0x59FFFFFF)) {
            MicroLabel(tr("Rozkład prędkości - wszystkie poziomy", "Speed distribution - all levels"))
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                basic.assistSpeedPct.forEachIndexed { i, pct ->
                    val color = if (i == selected) Tokens.Amber else Tokens.Blue
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height((pct.coerceIn(2, 100) * 0.9f).dp)
                                .background(color, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(i.toString(), fontFamily = Manrope, fontSize = 10.sp, color = Tokens.TextSecondary)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
