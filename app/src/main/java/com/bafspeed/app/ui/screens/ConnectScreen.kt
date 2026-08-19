package com.bafspeed.app.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bafspeed.app.ConnectionStatus
import com.bafspeed.app.FirmwareType
import com.bafspeed.app.R
import com.bafspeed.app.UiState
import com.bafspeed.app.i18n.tr
import com.bafspeed.app.ui.components.EggSpeedWordmark
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens

@Composable
fun ConnectScreen(
    state: UiState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onGoToDashboard: () -> Unit,
) {
    val connected = state.connection == ConnectionStatus.CONNECTED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Tokens.Bg)
            .padding(horizontal = 22.dp, vertical = 16.dp),
    ) {
        // Wordmark - podniesione wyżej (bez dużego odstępu przed okręgiem)
        EggSpeedWordmark(fontSize = 18.sp, letterSpacing = 3.9.sp)
        Text(
            tr("dla Bafang BBS01 / BBS02 / BBSHD", "for Bafang BBS01 / BBS02 / BBSHD"),
            fontFamily = Manrope,
            fontSize = 13.sp,
            color = Tokens.TextTertiary,
        )

        Spacer(Modifier.height(36.dp))

        // Logo w zaokrąglonym kwadracie, z pomarańczową ramką i szeroką "mgłą" w tle -
        // czerwoną gdy brak połączenia, zieloną gdy połączono. Mgła ma kilka stopni
        // przezroczystości: intensywna blisko logo, słabnąca im dalej od niego.
        val glowColor = when (state.connection) {
            ConnectionStatus.CONNECTED -> Tokens.Emerald
            else -> Tokens.Red
        }
        val logoShape = RoundedCornerShape(56.dp)
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(340.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(340.dp)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f to glowColor.copy(alpha = 0.5f),
                            0.25f to glowColor.copy(alpha = 0.28f),
                            0.55f to glowColor.copy(alpha = 0.12f),
                            1.0f to glowColor.copy(alpha = 0f),
                        ),
                    ),
                )
            }
            Box(
                modifier = Modifier
                    // Ramka 5x grubsza niz pierwotnie (bylo 3dp) - w pelni kryjaca (bez gradientu
                    // przezroczystosci) i narysowana NA ZEWNATRZ ikony (Box wiekszy o 2x15dp,
                    // Image wciety padding'iem o 15dp) - zeby nie nachodzila na sama ikone.
                    .size(216.dp + 30.dp)
                    .border(BorderStroke(15.dp, glowColor), logoShape)
                    .padding(15.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_rounded),
                    contentDescription = "EggSPEED",
                    modifier = Modifier
                        .size(216.dp)
                        .clip(logoShape),
                )
            }
            if (state.connection == ConnectionStatus.SEARCHING || state.connection == ConnectionStatus.CONNECTING) {
                Spinner(glowColor)
            }
        }

        Spacer(Modifier.height(26.dp))

        Text(
            text = when (state.connection) {
                ConnectionStatus.DISCONNECTED -> tr("Niepołączono", "Not connected")
                ConnectionStatus.SEARCHING -> tr("Szukam sterownika…", "Searching for controller…")
                ConnectionStatus.CONNECTING -> tr("Identyfikuję sterownik…", "Identifying controller…")
                ConnectionStatus.CONNECTED -> tr("Połączono", "Connected")
                ConnectionStatus.ERROR -> tr("Błąd połączenia", "Connection error")
            },
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontFamily = Sora,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            color = Tokens.TextPrimary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = state.statusMessage.ifBlank { tr("Podłącz kabel programujący Bafang między telefonem a kontrolerem", "Connect the Bafang programming cable between your phone and the controller") },
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontFamily = Manrope,
            fontSize = 13.sp,
            color = Tokens.TextTertiary,
        )

        Spacer(Modifier.weight(1f))

        // Szybkie przejście do Kokpitu po połączeniu - bez szukania w menu
        if (connected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Tokens.Card, RoundedCornerShape(16.dp))
                    .border(1.dp, Tokens.Blue, RoundedCornerShape(16.dp))
                    .clickable { onGoToDashboard() }
                    .padding(vertical = 15.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(tr("Przejdź do Kokpitu →", "Go to Cockpit →"), fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Tokens.Blue)
            }
            Spacer(Modifier.height(10.dp))
        }

        // Aktualnie wybrany firmware (ustawiany w zakładce Ustawienia) - aktywny wariant na
        // zielono, drugi wyszarzony, żeby było widać na pierwszy rzut oka co apka skonfiguruje.
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                "OEM Bafang",
                fontFamily = Manrope, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                color = if (state.firmwareType == FirmwareType.OEM_BAFANG) Tokens.Emerald else Tokens.TextTertiary,
            )
            Text("   •   ", fontFamily = Manrope, fontSize = 11.sp, color = Tokens.TextTertiary)
            Text(
                "BBS-FW",
                fontFamily = Manrope, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                color = if (state.firmwareType == FirmwareType.BBS_FW) Tokens.Emerald else Tokens.TextTertiary,
            )
        }
        Spacer(Modifier.height(10.dp))

        // CTA - podniesiony wyżej od dolnej krawędzi (dodatkowy odstęp pod spodem o wysokość
        // ok. jednego przycisku) i z większym napisem niż reszta przycisków na ekranie.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (connected) Tokens.Elevated else Tokens.Blue, RoundedCornerShape(16.dp))
                .clickable { if (connected) onDisconnect() else onConnect() }
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                if (connected) tr("Rozłącz", "Disconnect") else tr("Połącz", "Connect"),
                fontFamily = Sora,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = if (connected) Tokens.TextPrimary else Tokens.OnAccent,
            )
        }
        Spacer(Modifier.height(64.dp))
    }
}

@Composable
private fun Spinner(color: Color) {
    val transition = rememberInfiniteTransition(label = "spinner")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart),
        label = "angle",
    )
    Canvas(Modifier.size(204.dp).rotate(angle)) {
        drawArc(
            color = color,
            startAngle = 45f,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(width = 3.dp.toPx()),
        )
    }
}
