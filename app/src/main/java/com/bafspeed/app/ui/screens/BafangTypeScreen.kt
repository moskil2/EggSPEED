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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bafspeed.app.UiState
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.ui.components.MicroLabel
import com.bafspeed.app.ui.components.PreviewBanner
import com.bafspeed.app.ui.components.TokenCard
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens

/**
 * Zakladka "Bafang Type" - tozsamosc podlaczonego sterownika (blok GEN, tylko odczyt):
 * producent, model, wersje HW/FW, napiecie nominalne, prad maksymalny. Wydzielona z gory
 * ekranu "Bafang Basic", zeby nie mieszac danych identyfikacyjnych z edytowalnymi nastawami.
 */
@Composable
fun BafangTypeScreen(state: UiState) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Tokens.Bg)
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = 16.dp)),
    ) {
        state.general?.let { gen ->
            MicroLabel("Bafang")
            Spacer(Modifier.height(6.dp))
            TokenCard(borderColor = Tokens.WhiteBorder) {
                ParamRow(tr("Producent", "Manufacturer"), gen.manufacturer)
                ParamRow("Model", gen.model)
                ParamRow(tr("Wersja sprzętu", "Hardware version"), gen.hardwareVersion)
                ParamRow(tr("Wersja firmware", "Firmware version"), gen.firmwareVersion)
                ParamRow(tr("Napięcie nominalne", "Nominal voltage"), "${gen.nominalVoltage} V")
                ParamRow(tr("Prąd maksymalny", "Max current"), "${gen.maxCurrentA} A", last = true)
            }
        } ?: PreviewBanner(tr("Połącz się ze sterownikiem, żeby zobaczyć tutaj jego tożsamość (producent, model, firmware).", "Connect to your controller to see its identity (manufacturer, model, firmware) here."))
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
