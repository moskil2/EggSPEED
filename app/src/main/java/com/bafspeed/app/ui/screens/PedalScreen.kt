package com.bafspeed.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bafspeed.app.UiState
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.protocol.PEDAL_SENSOR_TYPE_LABELS
import com.bafspeed.app.protocol.designatedAssistLabel
import com.bafspeed.app.protocol.speedLimitLabel
import com.bafspeed.app.ui.components.ExpandableParamTile
import com.bafspeed.app.ui.components.FlankedSlider
import com.bafspeed.app.ui.components.ReadWriteButtons
import com.bafspeed.app.ui.components.StepBtn
import com.bafspeed.app.ui.theme.Tokens

/**
 * Zakladka "Pedal" - kolejnosc i etykiety 1:1 jak w Bafang Configuration Tool
 * (Pedal Assist): Pedal Sensor Type, Designated Assist Level, Speed Limit, Start Current,
 * Slow-start Mode, Start Degree, Work Mode, Stop Delay, Current Decay, Stop Decay, Keep Current.
 * Kazde pole to osobna, rozwijalna karta z opisem z Help.pdf. Wszystkie nastawy sterowane
 * przyciskami -/+ (tylko aktualna wartosc widoczna), poza Start Current i Keep Current,
 * ktore zostaja suwakami (krok 1%) oflankowanymi przyciskami -/+.
 */
@Composable
fun PedalScreen(
    state: UiState,
    onPedalType: (Int) -> Unit,
    onDesignatedAssist: (Int) -> Unit,
    onSpeedLimit: (Int) -> Unit,
    onStartCurrent: (Int) -> Unit,
    onSlowStartMode: (Int) -> Unit,
    onStartDegree: (Int) -> Unit,
    onWorkMode: (Int) -> Unit,
    onTimeOfStop: (Int) -> Unit,
    onCurrentDecay: (Int) -> Unit,
    onStopDecay: (Int) -> Unit,
    onKeepCurrent: (Int) -> Unit,
    onRead: () -> Unit,
    onWrite: () -> Unit,
    readWriteEnabled: Boolean,
) {
    val pas = state.pasOrDefault

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
            label = tr("Typ czujnika pedałowania", "Pedal Sensor Type"),
            valueLabel = PEDAL_SENSOR_TYPE_LABELS.getOrElse(pas.pedalType) { "?" },
            description = tr(
                "Ten parametr wybiera typ czujnika obrotu pedałów. Jest ustawiany przez producenta i nie " +
                    "powinien być zmieniany.",
                "This parameter selects the pedal rotation sensor type. It is set by the manufacturer and " +
                    "should not be changed.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onPedalType(pas.pedalType - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onPedalType(pas.pedalType + 1) }
            }
        }

        ExpandableParamTile(
            label = tr("Wskazany poziom wspomagania", "Designated Assist Level"),
            valueLabel = designatedAssistLabel(pas.designatedAssist),
            description = tr(
                "Masz dwa tryby działania. \"By Display's Command\" oznacza, że poziom wspomagania (ten z " +
                    "zakładki General) będzie wybierany z Twojego wyświetlacza LCD. Druga opcja to wybór konkretnego " +
                    "poziomu wspomagania (0-9), który będzie stały i nie da się go zmienić z LCD.",
                "You have two types of operation. \"By Display's Command\" means the assist level (the " +
                    "one from the General tab) will be selected from your LCD. The second option is to choose a specific " +
                    "assist level (0-9) which will be fixed and you will not be able to change it from the LCD.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onDesignatedAssist(pas.designatedAssist - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onDesignatedAssist(pas.designatedAssist + 1) }
            }
        }

        ExpandableParamTile(
            label = tr("Limit prędkości", "Speed Limit"),
            valueLabel = speedLimitLabel(pas.speedLimit),
            description = tr(
                "To jest maksymalna prędkość, przy której silnik będzie zapewniał dodatkowe przyspieszenie. Po " +
                    "jej osiągnięciu tylko utrzyma tę prędkość, nie przyspieszając dalej. \"By Display's Command\" " +
                    "pozwala ustawić prędkość z LCD. To ustawienie dotyczy wszystkich poziomów wspomagania z zakładki General.",
                "This is the maximum speed at which the motor will provide additional acceleration. Once " +
                    "reached it will only keep that speed, not accelerate further. \"By Display's Command\" lets you set " +
                    "the speed from your LCD. This setting applies to all assist levels seen in the General tab.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onSpeedLimit(pas.speedLimit - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onSpeedLimit(pas.speedLimit + 1) }
            }
        }

        ExpandableParamTile(
            label = tr("Prąd startowy [%]", "Start Current [%]"),
            valueLabel = "${pas.startCurrentPct}%",
            description = tr(
                "To jest prąd startowy, gdy zaczynasz obracać pedałami. Ustaw go na co najmniej 10%, żeby rower " +
                    "zaczął się ruszać bez blokowania silnika. Zbyt wysoka wartość powoduje bardzo szybkie " +
                    "przyspieszenie na starcie, co może uszkodzić wewnętrzne przekładnie i silnik. Zalecany zakres: 10-30%.",
                "This is the startup current when you start rotating the pedals. Set it to at least 10% " +
                    "so the bicycle starts moving without stalling the motor. Too high a value causes very fast " +
                    "acceleration at start, which can damage the internal gears and the motor. Recommended range: 10-30%.",
            ),
        ) {
            FlankedSlider(
                value = pas.startCurrentPct,
                range = 0..100,
                accent = Tokens.Amber,
                onValueChange = onStartCurrent,
            )
        }

        ExpandableParamTile(
            label = tr("Tryb wolnego startu (1-8)", "Slow-start Mode (1-8)"),
            valueLabel = (pas.slowStartMode + 1).toString(),
            description = tr(
                "Kontroluje, jak szybko osiągany jest prąd startowy. Wartość ok. 4 zwykle działa dobrze dla " +
                    "normalnej jazdy. Niższe wartości przyspieszają start, co może być przydatne w terenie, ale " +
                    "grozi przeciążeniem sterownika i silnika.",
                "Controls how quickly the start current is reached. A value around 4 usually works well " +
                    "for normal cycling. Lower values make acceleration faster, which can be useful off-road but risks " +
                    "overloading the controller and motor.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onSlowStartMode(pas.slowStartMode - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onSlowStartMode(pas.slowStartMode + 1) }
            }
        }

        ExpandableParamTile(
            label = tr("Stopień startu (nr sygnału)", "Start Degree (Signal No.)"),
            valueLabel = pas.startDegree.toString(),
            description = tr(
                "Ustala, ile impulsów z czujnika pedałowania jest potrzebnych, zanim silnik się uruchomi. Pełny " +
                    "obrót pedałów w zestawach BBS generuje 24 impulsy. 0 lub 1 nie zadziała. Wartość ok. 4 działa " +
                    "dobrze: nie za nerwowo, nie za duży wymagany obrót.",
                "Sets how many pulses from the pedal sensor are needed before the motor starts. A full " +
                    "pedal revolution on BBS kits generates 24 pulses. 0 or 1 will not work. A value around 4 works " +
                    "well: not too twitchy, not too much rotation required.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onStartDegree(pas.startDegree - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onStartDegree(pas.startDegree + 1) }
            }
        }

        ExpandableParamTile(
            label = tr("Tryb pracy", "Work Mode"),
            valueLabel = if (pas.workMode == 0) tr("Nieokreślony", "Undetermined") else pas.workMode.toString(),
            description = tr(
                "Dokładne przeznaczenie tego parametru nie jest dobrze udokumentowane - przypuszczalnie kontroluje " +
                    "moc w zależności od prędkości obrotu pedałów. Domyślna wartość producenta działa dobrze, więc " +
                    "normalnie nie musisz jej zmieniać.",
                "This parameter's exact purpose is not well documented - it is supposed to control power " +
                    "according to pedal rotation speed. The manufacturer's default value works fine, so you normally " +
                    "don't need to change it.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onWorkMode(pas.workMode - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onWorkMode(pas.workMode + 1) }
            }
        }

        ExpandableParamTile(
            label = tr("Opóźnienie zatrzymania", "Stop Delay"),
            valueLabel = "${pas.timeOfStop * 10} ms",
            description = tr(
                "Opóźnienie po zatrzymaniu pedałowania, przed zatrzymaniem silnika. Sterownik przyjmuje to tylko " +
                    "w krokach po 10ms, więc każde dotknięcie -/+ przesuwa wartość o 10ms. 250ms działa dobrze.",
                "The delay after you stop pedaling before the motor stops. The controller only accepts " +
                    "this in steps of 10ms, so each -/+ tap moves it by 10ms. 250ms works well.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onTimeOfStop(pas.timeOfStop - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onTimeOfStop(pas.timeOfStop + 1) }
            }
        }

        ExpandableParamTile(
            label = tr("Zanik prądu (1-8)", "Current Decay (1-8)"),
            valueLabel = pas.currentDecay.toString(),
            description = tr(
                "Ustala, jak szybko spada prąd, gdy pedałujesz szybciej i osiągasz maksymalną prędkość na wybranym " +
                    "poziomie wspomagania. Niższa wartość oznacza, że prąd zaczyna spadać przy niższej prędkości.",
                "Sets how fast the current drops when you are pedaling faster and reaching the maximum " +
                    "speed at the selected assist level. A lower value means the current starts dropping at a lower " +
                    "speed.",
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onCurrentDecay(pas.currentDecay - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onCurrentDecay(pas.currentDecay + 1) }
            }
        }

        ExpandableParamTile(
            label = tr("Zanik zatrzymania", "Stop Decay"),
            valueLabel = "${pas.stopDecay * 10} ms",
            description = tr("Czas potrzebny silnikowi na zatrzymanie się. Kroki po 10ms na dotknięcie -/+.", "The amount of time it takes the motor to stop. Steps of 10ms per -/+ tap."),
        ) {
            Row(Modifier.fillMaxWidth()) {
                StepBtn("-", true) { onStopDecay(pas.stopDecay - 1) }
                Spacer(Modifier.weight(1f))
                StepBtn("+", true) { onStopDecay(pas.stopDecay + 1) }
            }
        }

        ExpandableParamTile(
            label = tr("Podtrzymanie prądu [%]", "Keep Current [%]"),
            valueLabel = "${pas.keepCurrentPct}%",
            description = tr(
                "Procent maksymalnego prądu dla wybranego poziomu wspomagania, który dalej płynie przez silnik po " +
                    "osiągnięciu maksymalnej prędkości i dalszym pedałowaniu. Np. przy Current Limit 25A, PAS5 przy " +
                    "50% prądu daje maks. 12,5A dla tego poziomu; z Keep Current na 50%, prąd jest utrzymywany na " +
                    "6,25A po osiągnięciu maks. prędkości - płynne przejście zamiast nagłego spadku.",
                "Percentage of the maximum current for the selected assist level that keeps flowing " +
                    "through the motor once you reach the maximum speed and keep pedaling. E.g. with a 25A Current " +
                    "Limit, PAS5 at 50% current gives 12.5A max for that level; with Keep Current at 50%, the current " +
                    "is held at 6.25A once max speed is reached - a smooth transition instead of a sudden drop.",
            ),
        ) {
            FlankedSlider(
                value = pas.keepCurrentPct,
                range = 0..100,
                accent = Tokens.Amber,
                onValueChange = onKeepCurrent,
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}
