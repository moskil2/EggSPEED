package com.bafspeed.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bafspeed.app.UiState
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.protocol.THROTTLE_MODE_LABELS
import com.bafspeed.app.protocol.designatedAssistLabel
import com.bafspeed.app.protocol.speedLimitLabel
import com.bafspeed.app.ui.components.ExpandableParamTile
import com.bafspeed.app.ui.components.FlankedSlider
import com.bafspeed.app.ui.components.PreviewBanner
import com.bafspeed.app.ui.components.SegmentedControl
import com.bafspeed.app.ui.components.StepBtn
import com.bafspeed.app.ui.theme.Tokens

/**
 * Zakładka "Throttle" - odpowiednik Throttle Handle settings z Bafang Configuration Tool.
 * Pola: Start Voltage, End Voltage, Mode, Designated Assist Level, Speed Limit, Start
 * Current. Każde pole to osobna, rozwijalna karta z opisem z Help.pdf.
 */
@Composable
fun ThrottleScreen(
    state: UiState,
    onStartVoltage: (Int) -> Unit,
    onEndVoltage: (Int) -> Unit,
    onMode: (Int) -> Unit,
    onDesignatedAssist: (Int) -> Unit,
    onSpeedLimit: (Int) -> Unit,
    onStartCurrent: (Int) -> Unit,
) {
    val thr = state.thrOrDefault

    Column(
        Modifier
            .fillMaxSize()
            .background(Tokens.Bg)
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = 16.dp)),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        PreviewBanner(tr(
            "Zmiany są lokalne, dopóki nie potwierdzisz zapisu przyciskiem \"Masz niezapisane zmiany\" poniżej.",
            "Changes stay local until you confirm the write with the \"Unsaved changes\" button below.",
        ))

        ExpandableParamTile(
            label = tr("Napięcie startowe", "Start Voltage"),
            valueLabel = String.format("%.1f V", thr.startVoltage / 10.0),
            description = tr(
                "To jest napięcie wyjściowe manetki, przy którym silnik zacznie działać. " +
                    "Minimum, na które reaguje sterownik, to 1,1V, więc ten parametr jest zwykle ustawiony na 11 (11×100mV = 1,1V).",
                "This is the throttle handle output voltage at which the motor will start. " +
                    "The minimum at which the controller responds is 1.1V, so this parameter is usually set to 11 (11×100mV = 1.1V).",
            ),
        ) {
            FlankedSlider(
                value = thr.startVoltage,
                range = 0..50,
                accent = Tokens.Blue,
                onValueChange = onStartVoltage,
            )
        }

        ExpandableParamTile(
            label = tr("Napięcie końcowe", "End Voltage"),
            valueLabel = String.format("%.1f V", thr.endVoltage / 10.0),
            description = tr(
                "To jest napięcie wyjściowe manetki, przy którym silnik osiągnie maksymalną moc " +
                    "(ograniczoną innymi ustawieniami). Maksimum przyjmowane przez sterownik to 4,2V (42×100mV = 4,2V). " +
                    "Realne maksimum manetki może się różnić zależnie od modelu - ustawione za niskie dają prawie brak " +
                    "reakcji, ustawione na realne maksimum manetki dają najszerszy możliwy zakres kontroli.",
                "This is the throttle handle output voltage at which the motor will reach its maximum power " +
                    "(limited by other settings). The maximum accepted by the controller is 4.2V (42×100mV = 4.2V). The " +
                    "throttle's real maximum output can differ by model - set too low you get almost no response, set to " +
                    "the handle's real maximum you get the widest possible control range.",
            ),
        ) {
            FlankedSlider(
                value = thr.endVoltage,
                range = 0..50,
                accent = Tokens.Blue,
                onValueChange = onEndVoltage,
            )
        }

        ExpandableParamTile(
            label = tr("Tryb", "Mode"),
            valueLabel = THROTTLE_MODE_LABELS.getOrElse(thr.mode) { "?" },
            description = tr(
                "Tryb prędkości reaguje wolniej, ale daje precyzyjną kontrolę ściśle związaną z dokładną pozycją " +
                    "manetki. Tryb prądu reaguje szybko, ale bardziej na zasadzie włącz/wyłącz, mniej płynnie. " +
                    "Prędkość: sterownik używa prędkości jazdy do ustawienia mocy silnika na podstawie pozycji manetki - " +
                    "jest znaczne opóźnienie i reakcja jest często słaba. Prąd: manetka kontroluje prąd silnika " +
                    "bezpośrednio na podstawie swojej pozycji - ten tryb działa lepiej, podobnie do pedału gazu w samochodzie.",
                "Speed mode is slower to react but gives precise control tied closely to the throttle's " +
                    "exact position. Current mode reacts fast but feels more on/off, less gradual. " +
                    "Speed: the controller uses the moving speed to set motor power based on throttle position - there " +
                    "is significant delay and the response is often poor. Current: the handle controls motor current " +
                    "directly based on its position - this mode works better, similar to a car's accelerator.",
            ),
        ) {
            SegmentedControl(
                options = THROTTLE_MODE_LABELS,
                selectedIndex = thr.mode.coerceIn(0, 1),
                onSelect = onMode,
            )
        }

        ExpandableParamTile(
            label = tr("Wskazany poziom wspomagania", "Designated Assist Level"),
            valueLabel = designatedAssistLabel(thr.designatedAssist),
            description = tr(
                "\"By Display's Command\" używa poziomu wspomagania wybranego na Twoim LCD, więc maksymalna moc " +
                    "wyjściowa i prędkość zależą od tego poziomu i pozycji manetki - niski poziom PAS utrzymuje niski " +
                    "prąd i prędkość nawet przy pełnej manetce. Ustalony poziom (0-9) sprawia, że manetka zawsze " +
                    "używa maksymalnego prądu i prędkości tego poziomu, niezależnie od LCD. Uważaj z poziomem 9: nie " +
                    "wciskaj manetki do maksimum stojąc w miejscu, wysoki prąd może uszkodzić sterownik i silnik.",
                "\"By Display's Command\" uses the assist level selected on your LCD, so the maximum power " +
                    "output and speed depend on that level and the throttle position - a low PAS level keeps current and " +
                    "speed low even at full throttle. A fixed level (0-9) makes the throttle always use that level's " +
                    "maximum current and speed regardless of the LCD. Be careful with level 9: don't push the throttle to " +
                    "max while stopped, the high current can damage the controller and motor.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onDesignatedAssist(thr.designatedAssist - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onDesignatedAssist(thr.designatedAssist + 1) }
            }
        }

        ExpandableParamTile(
            label = tr("Limit prędkości", "Speed Limit"),
            valueLabel = speedLimitLabel(thr.speedLimit),
            description = tr(
                "Ogranicza maksymalną prędkość podczas używania manetki. To nadpisuje maksymalną prędkość " +
                    "ustalonego poziomu wspomagania, jeśli jest ustawiona wyżej.",
                "Limits the maximum speed when using the throttle handle. This overrides the designated " +
                    "assist level's maximum speed if that one is set higher.",
            ),
        ) {
            FlankedSlider(
                value = thr.speedLimit,
                range = 0..26,
                accent = Tokens.Emerald,
                onValueChange = onSpeedLimit,
            )
        }

        ExpandableParamTile(
            label = tr("Prąd startowy", "Start Current"),
            valueLabel = "${thr.startCurrentPct}%",
            description = tr(
                "Procent maksymalnego prądu podawanego do silnika, gdy manetka generuje minimalne przyjmowane " +
                    "napięcie. 10-20% zwykle działa dobrze - np. przy Current Limit 25A i Start Current 10% dostajesz " +
                    "płynny start na 2,5A. Zbyt wysoka wartość może uszkodzić wewnętrzne przekładnie i silnik.",
                "Percentage of maximum current applied to the motor when the throttle generates the minimum " +
                    "accepted voltage. 10-20% usually works well - e.g. with a 25A Current Limit and 10% Start Current you " +
                    "get a 2.5A smooth start. Too high a value can damage the internal gears and the motor.",
            ),
        ) {
            FlankedSlider(
                value = thr.startCurrentPct,
                range = 0..100,
                accent = Tokens.Amber,
                onValueChange = onStartCurrent,
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}
