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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bafspeed.app.UiState
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.ui.components.MicroLabel
import com.bafspeed.app.ui.components.PreviewBanner
import com.bafspeed.app.ui.components.ToggleRow
import com.bafspeed.app.ui.components.TokenCard
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens

private val WhiteBorder = Color(0x59FFFFFF)

/**
 * Zakładka "Serwis" - domyślnie otwarta (pusty PIN = brak ochrony). Użytkownik może w środku
 * ustawić własny PIN, od tego momentu wejście wymaga go podania. Steruje funkcją PROTECT
 * (kafelek na Kokpicie, patrz DashboardScreen) - włączenie/wyłączenie jej istnienia oraz
 * jedyny sposób odblokowania aktywnej ochrony (poza tym apka nie daje żadnej drogi wyjścia
 * z PROTECT z poziomu Kokpitu - to celowe, patrz komentarz przy activateProtect w AppViewModel).
 */
@Composable
fun ServiceScreen(
    state: UiState,
    onProtectFeatureEnabledChange: (Boolean) -> Unit,
    onDeactivateProtect: () -> Unit,
    onSetServicePin: (String) -> Unit,
) {
    var unlocked by remember(state.servicePin) { mutableStateOf(state.servicePin.isEmpty()) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Tokens.Bg)
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = 16.dp)),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (!unlocked) {
            PinGate(onUnlock = { unlocked = true }, correctPin = state.servicePin)
        } else {
            PreviewBanner(
                tr(
                    "PROTECT to funkcja bezpieczeństwa, która pozwala Ci szybko i niepostrzeżenie zablokować rower, kiedy tego potrzebujesz. Po naciśnięciu przycisku PROTECT na Kokpicie rower przechodzi w tryb 0, ale ekran w dalszym ciągu pozoruje możliwość zmiany poziomu wspomagania. Odblokowanie roweru wymaga wejścia do zakładki Serwis i wciśnięcia przycisku odblokowania. Ikona SAFE na Kokpicie sygnalizuje aktywną blokadę roweru.",
                    "PROTECT is a safety feature that lets you lock the bike down quickly and inconspicuously whenever you need to. Pressing the PROTECT button on the Cockpit switches the bike to mode 0, while the screen keeps pretending you can still change the assist level. Unlocking the bike requires opening the Service tab and pressing the unlock button. The SAFE icon on the Cockpit signals that the bike's lock is active.",
                ),
            )

            MicroLabel(tr("PROTECT", "PROTECT"))
            TokenCard(borderColor = WhiteBorder) {
                ToggleRow(
                    label = tr("Funkcja PROTECT", "PROTECT feature"),
                    checked = state.protectFeatureEnabled,
                    onCheckedChange = onProtectFeatureEnabledChange,
                    accent = Tokens.Blue,
                    description = tr(
                        "Gdy włączona, na Kokpicie pojawia się dodatkowy przycisk, który pozwala użyć funkcji PROTECT w razie potrzeby.",
                        "When on, an extra button appears on the Cockpit that lets you use the PROTECT feature whenever needed.",
                    ),
                )
            }

            Spacer(Modifier.height(2.dp))
            TokenCard(borderColor = if (state.protectActive) Color(0x6634C759) else Color(0x66FF3B30)) {
                Text(
                    if (state.protectActive) tr("PROTECT jest WŁĄCZONY", "PROTECT is ON") else tr("PROTECT jest WYŁĄCZONY", "PROTECT is OFF"),
                    fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                    color = if (state.protectActive) Tokens.Emerald else Tokens.Red,
                )
                if (state.protectActive) {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Tokens.Emerald, RoundedCornerShape(12.dp))
                            .clickable { onDeactivateProtect() }
                            .padding(vertical = 12.dp),
                    ) {
                        Text(
                            tr("Odblokuj (wyłącz PROTECT)", "Unlock (turn PROTECT off)"),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Tokens.OnAccent,
                        )
                    }
                }
            }

            MicroLabel(tr("PIN serwisu", "Service PIN"))
            TokenCard(borderColor = WhiteBorder) {
                Text(
                    tr(
                        "Ustawiony PIN blokuje dostęp do tej zakładki - wejście do Serwisu wymaga wtedy jego podania.",
                        "A set PIN locks access to this tab - entering Service then requires it.",
                    ),
                    fontFamily = Manrope, fontSize = 11.sp, color = Tokens.TextSecondary,
                )
                Spacer(Modifier.height(10.dp))
                PinField(initial = state.servicePin, onSave = onSetServicePin)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun PinGate(onUnlock: () -> Unit, correctPin: String) {
    var entered by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Spacer(Modifier.height(40.dp))
    Text(
        tr("Wpisz PIN, żeby wejść do Serwisu", "Enter the PIN to access Service"),
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        fontFamily = Manrope, fontSize = 14.sp, color = Tokens.TextPrimary,
    )
    Spacer(Modifier.height(16.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Tokens.Elevated, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        BasicTextField(
            value = entered,
            onValueChange = { if (it.length <= 8) { entered = it; error = false } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            textStyle = TextStyle(color = Tokens.TextPrimary, fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 20.sp, textAlign = TextAlign.Center),
            cursorBrush = SolidColor(Tokens.Blue),
            modifier = Modifier.fillMaxWidth(),
        )
    }
    if (error) {
        Spacer(Modifier.height(8.dp))
        Text(
            tr("Zły PIN", "Wrong PIN"),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontFamily = Manrope, fontSize = 12.sp, color = Tokens.Red,
        )
    }
    Spacer(Modifier.height(16.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Tokens.Blue, RoundedCornerShape(12.dp))
            .clickable { if (entered == correctPin) onUnlock() else error = true }
            .padding(vertical = 12.dp),
    ) {
        Text(
            tr("Zatwierdź", "Confirm"),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Tokens.OnAccent,
        )
    }
}

@Composable
private fun PinField(initial: String, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf(initial) }
    var saved by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Tokens.Elevated, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        BasicTextField(
            value = text,
            onValueChange = { if (it.length <= 8) { text = it; saved = false } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            textStyle = TextStyle(color = Tokens.TextPrimary, fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 16.sp),
            cursorBrush = SolidColor(Tokens.Blue),
            modifier = Modifier.weight(1f),
        )
    }
    Spacer(Modifier.height(10.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (saved) Tokens.Emerald else Tokens.Elevated, RoundedCornerShape(12.dp))
            .clickable(enabled = text.isNotBlank()) { onSave(text); saved = true }
            .padding(vertical = 10.dp),
    ) {
        Text(
            if (saved) tr("Zapisano", "Saved") else tr("Zapisz PIN", "Save PIN"),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 13.sp,
            color = if (saved) Tokens.OnAccent else Tokens.TextPrimary,
        )
    }
}
