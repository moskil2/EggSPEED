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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bafspeed.app.UiState
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.protocol.SPEED_METER_TYPE_LABELS
import com.bafspeed.app.protocol.WHEEL_SIZE_LABELS
import com.bafspeed.app.ui.components.ExpandableParamTile
import com.bafspeed.app.ui.components.FlankedSlider
import com.bafspeed.app.ui.components.ReadWriteButtons
import com.bafspeed.app.ui.components.StepBtn
import com.bafspeed.app.ui.theme.Tokens

/**
 * Zakladka "General" - kolejnosc i etykiety 1:1 jak w Bafang Configuration Tool (Basic):
 * Low Battery Protection, Current Limit, Speed Meter Type, Speed Meter Signals, Wheel Diameter.
 * Zbudowana na tym samym wzorze co Pedal/Throttle: kazde pole to osobna, rozwijalna karta
 * z opisem z Help.pdf. Wszystkie nastawy sterowane przyciskami -/+ (tylko aktualna wartosc
 * widoczna), poza Low Battery Protection i Current Limit, ktore zostaja suwakami (krok 1)
 * oflankowanymi przyciskami -/+.
 */
@Composable
fun GeneralScreen(
    state: UiState,
    onLowBatteryProtectionChange: (Int) -> Unit,
    onCurrentLimitChange: (Int) -> Unit,
    onSpeedMeterTypeChange: (Int) -> Unit,
    onSpeedMeterSignalsChange: (Int) -> Unit,
    onWheelChange: (Int) -> Unit,
    onRead: () -> Unit,
    onWrite: () -> Unit,
    readWriteEnabled: Boolean,
) {
    val basic = state.basicOrDefault
    // Prąd maksymalny zgłoszony przez podłączony sterownik (blok GEN) - różne silniki
    // BBS01/BBS02/BBSHD mają różne wartości nominalne. Bez połączenia: bezpieczny fallback 40A.
    val maxCurrentA = state.general?.maxCurrentA?.takeIf { it > 0 } ?: 40

    Column(
        Modifier
            .fillMaxSize()
            .background(Tokens.Bg)
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = 16.dp)),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ReadWriteButtons(onRead = onRead, onWrite = onWrite, enabled = readWriteEnabled)

        ExpandableParamTile(
            label = tr("Ochrona niskiego napięcia", "Low Battery Protection"),
            valueLabel = "${basic.lowBatteryProtection} V",
            description = tr(
                "Napięcie, przy którym sterownik zatrzyma silnik, żeby chronić baterię przed nadmiernym " +
                    "rozładowaniem. Powinno być prawidłowo ustawione przez producenta - normalnie nie musisz tego " +
                    "zmieniać. Dla pakietów 13S domyślną wartością jest 41V.",
                "This is the voltage at which the controller will stop the motor to protect your battery " +
                    "from over-discharge. It should be set correctly by the manufacturer - you normally don't need to " +
                    "change it. For 13S battery packs, 41V is the default.",
            ),
        ) {
            FlankedSlider(
                value = basic.lowBatteryProtection,
                range = 20..100,
                accent = Tokens.Blue,
                onValueChange = onLowBatteryProtectionChange,
            )
        }

        ExpandableParamTile(
            label = tr("Limit prądu [A]", "Current Limit [A]"),
            valueLabel = "${basic.currentLimit} A",
            description = tr(
                "Maksymalny prąd, jaki może płynąć przez silnik. Najwyższa możliwa wartość jest zdefiniowana " +
                    "przez Twój sterownik - nie możesz ustawić wyżej. " + if (state.general?.maxCurrentA != null && state.general.maxCurrentA > 0) {
                        "W tej chwili to maksimum to ${state.general.maxCurrentA} A, zgłoszone przez podłączony sterownik."
                    } else {
                        "Połącz się ze sterownikiem, żeby odczytać jego realne maksimum."
                    },
                "The maximum current allowed to flow through the motor. The highest possible value is " +
                    "defined by your controller - you cannot set it any higher than that. " + if (state.general?.maxCurrentA != null && state.general.maxCurrentA > 0) {
                        "Right now that maximum is ${state.general.maxCurrentA} A, reported by your connected controller."
                    } else {
                        "Connect to your controller to read its real maximum."
                    },
            ),
        ) {
            FlankedSlider(
                value = basic.currentLimit,
                range = 1..maxCurrentA,
                accent = Tokens.Amber,
                onValueChange = onCurrentLimitChange,
            )
        }

        ExpandableParamTile(
            label = tr("Typ czujnika prędkości", "Speed Meter Type"),
            valueLabel = SPEED_METER_TYPE_LABELS.getOrElse(basic.speedMeterModel) { "?" },
            description = tr(
                "Wybiera czujnik prędkości używany na Twoim rowerze. Dla zestawów BBS to External. Ten parametr " +
                    "jest ustawiany przez producenta - jeśli Twój zestaw nie jest niestandardowy, nie musisz go zmieniać.",
                "Selects the speed meter used on your bicycle. For BBS kits it is External. This parameter " +
                    "is set by the manufacturer - if your setup isn't custom, you don't need to change it.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onSpeedMeterTypeChange(basic.speedMeterModel - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onSpeedMeterTypeChange(basic.speedMeterModel + 1) }
            }
        }

        ExpandableParamTile(
            label = tr("Sygnały czujnika prędkości", "Speed Meter Signals"),
            valueLabel = basic.speedMeterSignals.toString(),
            description = tr(
                "Ustala, ile sygnałów na obrót generuje Twój czujnik prędkości. Zewnętrzny czujnik z magnesem " +
                    "generuje jeden sygnał na obrót koła. Ustawiane przez producenta - jeśli Twój zestaw nie jest " +
                    "niestandardowy, nie musisz go zmieniać.",
                "Sets how many signals per revolution your speed sensor generates. An external sensor " +
                    "with a magnet generates one signal per wheel revolution. Set by the manufacturer - if your setup " +
                    "isn't custom, you don't need to change it.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onSpeedMeterSignalsChange(basic.speedMeterSignals - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onSpeedMeterSignalsChange(basic.speedMeterSignals + 1) }
            }
        }

        ExpandableParamTile(
            label = tr("Średnica koła [cale]", "Wheel Diameter [Inch]"),
            valueLabel = WHEEL_SIZE_LABELS.getOrElse(basic.wheelDiameterCode) { "?" },
            description = tr(
                "Średnica koła powinna odpowiadać realnemu rozmiarowi koła napędzanego (rower może mieć dwa koła " +
                    "różnych rozmiarów). Ustawienie mniejszej niż w rzeczywistości zwiększy wyświetlaną prędkość, ale " +
                    "może też doprowadzić do uszkodzenia silnika.",
                "The wheel diameter should match the real size of your drive wheel (a bicycle can have " +
                    "two different sized wheels). Setting it smaller than reality will increase the displayed speed but " +
                    "can also lead to motor damage.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onWheelChange(basic.wheelDiameterCode - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onWheelChange(basic.wheelDiameterCode + 1) }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
