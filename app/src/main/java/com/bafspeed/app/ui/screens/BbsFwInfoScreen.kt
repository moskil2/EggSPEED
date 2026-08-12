package com.bafspeed.app.ui.screens

import androidx.compose.foundation.background
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
import com.bafspeed.app.protocol.BbsFwController
import com.bafspeed.app.ui.components.MicroLabel
import com.bafspeed.app.ui.components.PreviewBanner
import com.bafspeed.app.ui.components.TokenCard
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens

private val WhiteBorder = Color(0x59FFFFFF)

/**
 * Odpowiednik [BafangTypeScreen] dla bbs-fw - bbs-fw nie ma bloku GEN (producent/model), tylko
 * OPCODE_READ_FW_VERSION: wersja firmware, wersja formatu konfiguracji i numeryczny typ sterownika.
 * Wersja configu musi się zgadzać z tym, co zna ta apka (patrz [com.bafspeed.app.protocol.BbsFwCommands.CONFIG_VERSION]) -
 * inaczej odczyt/zapis pełnej konfiguracji jest zablokowany (patrz AppViewModel.handleBbsFwConfigFrame).
 */
@Composable
fun BbsFwInfoScreen(state: UiState) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Tokens.Bg)
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = 16.dp)),
    ) {
        state.bbsFwVersion?.let { v ->
            MicroLabel("bbs-fw")
            Spacer(Modifier.height(6.dp))
            TokenCard(borderColor = WhiteBorder) {
                ParamRow(tr("Wersja firmware", "Firmware version"), v.versionLabel)
                ParamRow(tr("Wersja formatu konfiguracji", "Config format version"), v.configVersion.toString())
                ParamRow(tr("Typ sterownika", "Controller type"), "${BbsFwController.name(v.ctrlType)} (${v.ctrlType})", last = true)
            }
            Spacer(Modifier.height(10.dp))
            PreviewBanner(
                tr(
                    "Ta apka zna format konfiguracji w wersji 5. Jeśli powyższa wersja formatu jest inna, odczyt/zapis " +
                        "pełnej konfiguracji jest zablokowany (bbs-fw zmienił układ danych między wersjami) - tryb wyświetlacza " +
                        "(Kokpit/Diagnostyka) nadal działa niezależnie od tego.",
                    "This app knows config format version 5. If the version above differs, reading/writing the full " +
                        "configuration is blocked (bbs-fw changed the data layout between versions) - display mode " +
                        "(Cockpit/Diagnostics) still works regardless.",
                ),
            )
        } ?: PreviewBanner(
            tr(
                "Połącz się ze sterownikiem, żeby zobaczyć tutaj wersję bbs-fw i typ sterownika.",
                "Connect to your controller to see the bbs-fw version and controller type here.",
            ),
        )
    }
}

@Composable
private fun ParamRow(label: String, value: String, last: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontFamily = Manrope, fontSize = 13.sp, color = Tokens.TextPrimary, modifier = Modifier.weight(1f))
        Text(value, fontFamily = Sora, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Tokens.TextPrimary)
    }
    if (!last) HorizontalDivider(color = Tokens.Border, thickness = 1.dp)
}
