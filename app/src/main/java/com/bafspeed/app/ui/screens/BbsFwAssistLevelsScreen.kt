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

        // --- Operation Mode Page ---
        Text("Operation Mode Page", fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Tokens.TextPrimary)
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
            Text("Level $selected", fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Tokens.TextPrimary)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Type:", fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextSecondary, modifier = Modifier.weight(1f))
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
                    LabeledStepRow("Variant:", pasVariantLabel(pasVariant), { onPasVariant(profile, selected, (pasVariant - 1).coerceIn(0, 2)) }, { onPasVariant(profile, selected, (pasVariant + 1).coerceIn(0, 2)) })

                    if (pasVariant == BbsFwAssistPasVariant.TORQUE) {
                        HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                        LabeledStepRow(
                            "Torque Amplification:", "${level.torqueAmplificationFactorX10 / 10.0}×",
                            { onTorqueFactor(profile, selected, level.torqueAmplificationFactorX10 - 1) },
                            { onTorqueFactor(profile, selected, level.torqueAmplificationFactorX10 + 1) },
                        )
                    }

                    HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                    LabeledSliderRow("Max Current (%)", level.targetCurrentPercent, Tokens.Amber) { onTargetCurrent(profile, selected, it) }
                    HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                    LabeledSliderRow("Max Cadence (%)", level.maxCadencePercent, Tokens.Emerald) { onMaxCadence(profile, selected, it) }
                    HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                    LabeledSliderRow("Max Speed (%)", level.maxSpeedPercent, Tokens.Emerald) { onMaxSpeed(profile, selected, it) }

                    if (pasVariant != BbsFwAssistPasVariant.VARIABLE) {
                        val throttleEnabled = level.hasFlag(BbsFwAssistFlags.THROTTLE)
                        HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                        ToggleRow("Enable Throttle:", throttleEnabled, { onFlag(profile, selected, BbsFwAssistFlags.THROTTLE, it) })
                        HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                        Column(Modifier.padding(vertical = 4.dp)) {
                            Text("Throttle Overrides:", fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextSecondary)
                            Spacer(Modifier.height(6.dp))
                            Row {
                                Box(Modifier.weight(1f)) {
                                    ToggleRow(
                                        "Cadence", level.hasFlag(BbsFwAssistFlags.OVERRIDE_CADENCE),
                                        { onFlag(profile, selected, BbsFwAssistFlags.OVERRIDE_CADENCE, it) },
                                        enabled = throttleEnabled,
                                    )
                                }
                                Box(Modifier.weight(1f)) {
                                    ToggleRow(
                                        "Speed", level.hasFlag(BbsFwAssistFlags.OVERRIDE_SPEED),
                                        { onFlag(profile, selected, BbsFwAssistFlags.OVERRIDE_SPEED, it) },
                                        enabled = throttleEnabled,
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                        LabeledSliderRow("Max Throttle Current (%)", level.maxThrottleCurrentPercent, Tokens.Amber, enabled = throttleEnabled) { onMaxThrottleCurrent(profile, selected, it) }
                    }
                }
            }
            BbsFwAssistBaseType.THROTTLE -> {
                TokenCard(borderColor = Tokens.WhiteBorder) {
                    LabeledSliderRow("Max Current (%)", level.maxThrottleCurrentPercent, Tokens.Amber) { onMaxThrottleCurrent(profile, selected, it) }
                    HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                    LabeledSliderRow("Max Cadence (%)", level.maxCadencePercent, Tokens.Emerald) { onMaxCadence(profile, selected, it) }
                    HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                    LabeledSliderRow("Max Speed (%)", level.maxSpeedPercent, Tokens.Emerald) { onMaxSpeed(profile, selected, it) }
                }
            }
            BbsFwAssistBaseType.CRUISE -> {
                PreviewBanner(tr(
                    "Uwaga: tempomat (Cruise) - załączany pedałowaniem + manetką, wyłączany pedałowaniem wstecz, dotknięciem manetki albo hamulcem. Używaj ostrożnie.",
                    "Warning: Cruise mode - engaged by pedaling + throttle, disengaged by backpedaling, touching the throttle, or braking. Use with caution!",
                ))
                TokenCard(borderColor = Tokens.WhiteBorder) {
                    LabeledSliderRow("Max Current (%)", level.targetCurrentPercent, Tokens.Amber) { onTargetCurrent(profile, selected, it) }
                    HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                    LabeledSliderRow("Max Cadence (%)", level.maxCadencePercent, Tokens.Emerald) { onMaxCadence(profile, selected, it) }
                    HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
                    LabeledSliderRow("Max Speed (%)", level.maxSpeedPercent, Tokens.Emerald) { onMaxSpeed(profile, selected, it) }
                }
            }
            else -> {
                TokenCard(borderColor = Tokens.WhiteBorder) {
                    Text(
                        tr("Silnik wyłączony na tym poziomie - manetka i pedałowanie nie dają wspomagania.", "Motor disabled at this level - throttle and pedaling give no assist."),
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
                "Operation Mode Toggle:", assistModeSelectLabel(cfg.assistModeSelect),
                { onAssistModeSelect(stepInOrder(ASSIST_MODE_SELECT_DISPLAY_ORDER, cfg.assistModeSelect, -1)) },
                { onAssistModeSelect(stepInOrder(ASSIST_MODE_SELECT_DISPLAY_ORDER, cfg.assistModeSelect, 1)) },
            )
            HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
            LabeledStepRow(
                "Startup Assist Level:", cfg.assistStartupLevel.toString(),
                { onAssistStartupLevel(cfg.assistStartupLevel - 1) },
                { onAssistStartupLevel(cfg.assistStartupLevel + 1) },
            )
        }
        Text(
            tr(
                "Operation Mode Toggle: jak fizycznie przełącza się Standard/Sport - \"Przycisk Sport\" to dedykowany przycisk (u nas: przełącznik \"Sport\" w Kokpicie), \"Przycisk Światła\" przejmuje przycisk światła (patrz ostrzeżenie w Kokpicie). Startup Assist Level: poziom wspomagania profilu Standard, w którym startuje sterownik bez podłączonego wyświetlacza.",
                "Operation Mode Toggle: how Standard/Sport is physically switched - \"Sport Button\" is a dedicated button (in this app: the \"Sport\" switch in the Cockpit), \"Lights Button\" repurposes the lights button (see the Cockpit warning). Startup Assist Level: the Standard-profile assist level the controller starts in with no display connected.",
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
