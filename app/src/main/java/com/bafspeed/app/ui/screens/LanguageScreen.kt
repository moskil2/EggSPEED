package com.bafspeed.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bafspeed.app.i18n.AppLanguage
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.ui.components.MicroLabel
import com.bafspeed.app.ui.components.TokenCard
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens

/** Zakładka "Language" - wybór języka UI (flaga + nazwa własna), dostępna też jako skrót-flaga na ekranie Połączenie. */
@Composable
fun LanguageScreen(current: AppLanguage, onSelect: (AppLanguage) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Tokens.Bg)
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = 16.dp)),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        MicroLabel(tr("Język aplikacji", "App language", de = "App-Sprache", fr = "Langue de l'application", es = "Idioma de la app", pt = "Idioma da app", it = "Lingua dell'app", nl = "App-taal", sv = "Appspråk", cs = "Jazyk aplikace", sk = "Jazyk aplikácie"))
        AppLanguage.entries.forEach { lang ->
            val selected = lang == current
            TokenCard(
                modifier = Modifier.clickable { onSelect(lang) },
                contentPadding = 10.dp,
                borderColor = if (selected) Tokens.Blue else Tokens.WhiteBorder,
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(lang.flag, fontSize = 26.sp)
                    Spacer(Modifier.size(14.dp))
                    Text(
                        lang.displayName,
                        fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                        color = Tokens.TextPrimary, modifier = Modifier.weight(1f),
                    )
                    if (selected) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Tokens.Blue, RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(
                                tr("Aktywny", "Active", de = "Aktiv", fr = "Actif", es = "Activo", pt = "Ativo", it = "Attivo", nl = "Actief", sv = "Aktiv", cs = "Aktivní", sk = "Aktívny"),
                                fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Tokens.OnAccent,
                            )
                        }
                    }
                }
            }
        }
    }
}
