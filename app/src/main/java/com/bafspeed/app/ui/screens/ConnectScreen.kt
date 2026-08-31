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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextDecoration
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 16.dp),
    ) {
        // Wordmark - podniesione wyżej (bez dużego odstępu przed okręgiem)
        EggSpeedWordmark(fontSize = 18.sp, letterSpacing = 3.9.sp)
        Text(
            tr(
                "dla Bafang BBS01 / BBS02 / BBSHD", "for Bafang BBS01 / BBS02 / BBSHD",
                de = "für Bafang BBS01 / BBS02 / BBSHD", fr = "pour Bafang BBS01 / BBS02 / BBSHD", es = "para Bafang BBS01 / BBS02 / BBSHD",
                pt = "para Bafang BBS01 / BBS02 / BBSHD", it = "per Bafang BBS01 / BBS02 / BBSHD", nl = "voor Bafang BBS01 / BBS02 / BBSHD",
                sv = "för Bafang BBS01 / BBS02 / BBSHD", cs = "pro Bafang BBS01 / BBS02 / BBSHD", sk = "pre Bafang BBS01 / BBS02 / BBSHD",
            ),
            fontFamily = Manrope,
            fontSize = 13.sp,
            color = Tokens.TextTertiary,
        )

        Spacer(Modifier.height(18.dp))

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
                    // Tlo na calym Boxie (pod ramka i obrazkiem) w tym samym ksztalcie co ramka -
                    // promien logoShape jest absolutny (56dp), wiec przy wcieciu obrazka o padding(15dp)
                    // jego wlasny clip (ten sam promien, mniejszy Box) nie pokrywa sie idealnie z
                    // wewnetrzna krawedzia ramki - w rogach zostaja waskie szczeliny ("polksiezyce"),
                    // ktore bez tego tla pokazuja to, co jest pod spodem (biale tlo strony w trybie jasnym).
                    .background(Color(0xFF020203), logoShape)
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
            if (state.connection == ConnectionStatus.SEARCHING ||
                state.connection == ConnectionStatus.CONNECTING ||
                state.connection == ConnectionStatus.CONNECTED
            ) {
                Spinner(glowColor)
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = when (state.connection) {
                ConnectionStatus.DISCONNECTED -> tr("Niepołączono", "Not connected", de = "Nicht verbunden", fr = "Non connecté", es = "No conectado", pt = "Não conectado", it = "Non connesso", nl = "Niet verbonden", sv = "Ej ansluten", cs = "Nepřipojeno", sk = "Nepripojené")
                ConnectionStatus.SEARCHING -> tr("Szukam sterownika…", "Searching for controller…", de = "Suche Steuergerät…", fr = "Recherche du contrôleur…", es = "Buscando controlador…", pt = "A procurar controlador…", it = "Ricerca controller…", nl = "Controller zoeken…", sv = "Söker styrenhet…", cs = "Hledám řadič…", sk = "Hľadám radič…")
                ConnectionStatus.CONNECTING -> tr("Identyfikuję sterownik…", "Identifying controller…", de = "Identifiziere Steuergerät…", fr = "Identification du contrôleur…", es = "Identificando controlador…", pt = "A identificar controlador…", it = "Identificazione controller…", nl = "Controller identificeren…", sv = "Identifierar styrenhet…", cs = "Identifikuji řadič…", sk = "Identifikujem radič…")
                ConnectionStatus.CONNECTED -> tr("Połączono", "Connected", de = "Verbunden", fr = "Connecté", es = "Conectado", pt = "Conectado", it = "Connesso", nl = "Verbonden", sv = "Ansluten", cs = "Připojeno", sk = "Pripojené")
                ConnectionStatus.ERROR -> tr("Błąd połączenia", "Connection error", de = "Verbindungsfehler", fr = "Erreur de connexion", es = "Error de conexión", pt = "Erro de ligação", it = "Errore di connessione", nl = "Verbindingsfout", sv = "Anslutningsfel", cs = "Chyba připojení", sk = "Chyba pripojenia")
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
            // tr() tutaj (nie w AppViewModel) - tlumaczy na zywo z aktualnego jezyka przy kazdym
            // renderze, zamiast trzymac juz-przetlumaczony string "zapieczony" na moment zdarzenia
            // (co zostawialo stary komunikat w poprzednim jezyku po zmianie jezyka w Ustawieniach).
            text = if (state.statusMessagePl.isBlank() && state.statusMessageEn.isBlank()) {
                tr(
                    "Podłącz kabel programujący Bafang między telefonem a kontrolerem", "Connect the Bafang programming cable between your phone and the controller",
                    de = "Verbinde das Bafang-Programmierkabel zwischen Telefon und Steuergerät",
                    fr = "Connectez le câble de programmation Bafang entre votre téléphone et le contrôleur",
                    es = "Conecta el cable de programación Bafang entre tu teléfono y el controlador",
                    pt = "Liga o cabo de programação Bafang entre o telemóvel e o controlador",
                    it = "Collega il cavo di programmazione Bafang tra il telefono e il controller",
                    nl = "Sluit de Bafang-programmeerkabel aan tussen je telefoon en de controller",
                    sv = "Anslut Bafang-programmeringskabeln mellan din telefon och styrenheten",
                    cs = "Připoj programovací kabel Bafang mezi telefon a řadič",
                    sk = "Pripoj programovací kábel Bafang medzi telefón a radič",
                )
            } else {
                tr(state.statusMessagePl, state.statusMessageEn)
            },
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontFamily = Manrope,
            fontSize = 13.sp,
            color = Tokens.TextTertiary,
        )

        Spacer(Modifier.height(14.dp))

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
                Text(
                    tr("Przejdź do Kokpitu →", "Go to Cockpit →", de = "Zum Cockpit →", fr = "Aller au Cockpit →", es = "Ir al Cockpit →", pt = "Ir para o Cockpit →", it = "Vai al Cockpit →", nl = "Ga naar Cockpit →", sv = "Gå till Cockpit →", cs = "Přejít do Cockpitu →", sk = "Prejsť do Cockpitu →"),
                    fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Tokens.Blue,
                )
            }
            Spacer(Modifier.height(10.dp))
        }

        // Aktualnie wybrany firmware (ustawiany w zakładce Ustawienia) - aktywny wariant na
        // zielono, drugi wyszarzony, żeby było widać na pierwszy rzut oka co apka skonfiguruje.
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                "OEM Bafang",
                fontFamily = Manrope, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = if (state.firmwareType == FirmwareType.OEM_BAFANG) Tokens.Emerald else Tokens.TextTertiary,
                textDecoration = if (state.firmwareType == FirmwareType.OEM_BAFANG) null else TextDecoration.LineThrough,
            )
            Text("   •   ", fontFamily = Manrope, fontSize = 11.sp, color = Tokens.TextTertiary)
            Text(
                "BBS-FW",
                fontFamily = Manrope, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = if (state.firmwareType == FirmwareType.BBS_FW) Tokens.Emerald else Tokens.TextTertiary,
                textDecoration = if (state.firmwareType == FirmwareType.BBS_FW) null else TextDecoration.LineThrough,
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
                if (connected) {
                    tr("Rozłącz", "Disconnect", de = "Trennen", fr = "Déconnecter", es = "Desconectar", pt = "Desligar", it = "Disconnetti", nl = "Verbreken", sv = "Koppla från", cs = "Odpojit", sk = "Odpojiť")
                } else {
                    tr("Połącz", "Connect", de = "Verbinden", fr = "Connecter", es = "Conectar", pt = "Ligar", it = "Connetti", nl = "Verbinden", sv = "Anslut", cs = "Připojit", sk = "Pripojiť")
                },
                fontFamily = Sora,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = if (connected) Tokens.Red else Tokens.OnAccent,
            )
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun Spinner(color: Color) {
    val transition = rememberInfiniteTransition(label = "spinner")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
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
