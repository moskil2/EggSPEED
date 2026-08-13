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
import androidx.compose.ui.graphics.Color
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

private val WhiteBorderTemp = Color(0x59FFFFFF)

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
        MicroLabel(tr("Wyświetlanie na Kokpicie", "Cockpit display"))
        TokenCard(borderColor = WhiteBorderTemp) {
            ToggleRow(
                tr("Temp. sterownika (Tc)", "Controller temp. (Tc)"),
                state.showTempOnCockpit, onShowChange, accent = Tokens.Amber,
                description = tr(
                    "Pokazuje kafelek Tc po lewej stronie Kokpitu, mniej więcej na wysokości odczytu mocy.",
                    "Shows the Tc tile on the left side of the Cockpit, roughly at the power reading's height.",
                ),
            )
        }

        MicroLabel(tr("Reakcja na przekroczenie", "Reaction on threshold exceeded"))
        ExpandableParamTile(
            label = "Warning",
            valueLabel = "${state.tempWarningC}°C",
            description = tr(
                "Niższy próg - po przekroczeniu kafelek Tc podświetla się na pomarańczowo (bez migania).",
                "Lower threshold - when exceeded, the Tc tile highlights orange (no blinking).",
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
            ),
        ) {
            FlankedSlider(value = state.tempAlarmC, range = 30..150, accent = Tokens.Red, onValueChange = onAlarmChange)
        }
        TokenCard(borderColor = WhiteBorderTemp) {
            ToggleRow(
                tr("Dźwięk przy Alarm", "Sound on Alarm"),
                state.tempAlarmSoundEnabled, onAlarmSoundChange, accent = Tokens.Red,
                description = tr(
                    "Jednorazowy sygnał dźwiękowy przy przekroczeniu progu Alarm.",
                    "A one-time beep when the Alarm threshold is exceeded.",
                ),
            )
        }

        MicroLabel(tr("Czujniki w firmware", "Firmware sensors"))
        TokenCard(borderColor = WhiteBorderTemp) {
            val mode = state.bbsFwConfigOrDefault.temperatureSensorMode
            val modeLabel = listOf(
                tr("Wyłączony", "Disabled"), tr("Sterownika", "Controller"), tr("Silnika", "Motor"), "All",
            ).getOrElse(mode) { "?" }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    tr("Temperature Sensor (zakładka System)", "Temperature Sensor (System tab)"),
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
                ),
                fontFamily = Manrope, fontSize = 11.sp, color = Tokens.TextSecondary,
            )
        }
    }
}
