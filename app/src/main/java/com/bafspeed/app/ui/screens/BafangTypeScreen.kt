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
                ParamRow(tr("Producent", "Manufacturer", de = "Hersteller", fr = "Fabricant", es = "Fabricante", pt = "Fabricante", it = "Produttore", nl = "Fabrikant", sv = "Tillverkare", cs = "Výrobce", sk = "Výrobca"), gen.manufacturer)
                ParamRow("Model", gen.model)
                ParamRow(tr("Wersja sprzętu", "Hardware version", de = "Hardware-Version", fr = "Version matérielle", es = "Versión de hardware", pt = "Versão de hardware", it = "Versione hardware", nl = "Hardwareversie", sv = "Hårdvaruversion", cs = "Verze hardwaru", sk = "Verzia hardvéru"), gen.hardwareVersion)
                ParamRow(tr("Wersja firmware", "Firmware version", de = "Firmware-Version", fr = "Version du firmware", es = "Versión de firmware", pt = "Versão de firmware", it = "Versione firmware", nl = "Firmwareversie", sv = "Firmwareversion", cs = "Verze firmwaru", sk = "Verzia firmwaru"), gen.firmwareVersion)
                ParamRow(tr("Napięcie nominalne", "Nominal voltage", de = "Nennspannung", fr = "Tension nominale", es = "Voltaje nominal", pt = "Tensão nominal", it = "Tensione nominale", nl = "Nominale spanning", sv = "Nominell spänning", cs = "Jmenovité napětí", sk = "Menovité napätie"), "${gen.nominalVoltage} V")
                ParamRow(tr("Prąd maksymalny", "Max current", de = "Maximalstrom", fr = "Courant maximal", es = "Corriente máxima", pt = "Corrente máxima", it = "Corrente massima", nl = "Maximale stroom", sv = "Max ström", cs = "Maximální proud", sk = "Maximálny prúd"), "${gen.maxCurrentA} A", last = true)
            }
        } ?: PreviewBanner(
            tr(
                "Połącz się ze sterownikiem, żeby zobaczyć tutaj jego tożsamość (producent, model, firmware).",
                "Connect to your controller to see its identity (manufacturer, model, firmware) here.",
                de = "Verbinde dich mit deinem Steuergerät, um hier seine Identität zu sehen (Hersteller, Modell, Firmware).",
                fr = "Connectez-vous à votre contrôleur pour voir ici son identité (fabricant, modèle, firmware).",
                es = "Conéctate a tu controlador para ver aquí su identidad (fabricante, modelo, firmware).",
                pt = "Liga-te ao teu controlador para ver aqui a sua identidade (fabricante, modelo, firmware).",
                it = "Connettiti al tuo controller per vedere qui la sua identità (produttore, modello, firmware).",
                nl = "Verbind met je controller om hier de identiteit ervan te zien (fabrikant, model, firmware).",
                sv = "Anslut till din styrenhet för att se dess identitet här (tillverkare, modell, firmware).",
                cs = "Připoj se ke své řídicí jednotce, abys zde viděl její identitu (výrobce, model, firmware).",
                sk = "Pripoj sa k svojej riadiacej jednotke, aby si tu videl jej identitu (výrobca, model, firmware).",
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
