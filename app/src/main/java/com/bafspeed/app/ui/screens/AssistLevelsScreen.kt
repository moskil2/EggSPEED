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
import com.bafspeed.app.ui.components.ExpandableParamTile
import com.bafspeed.app.ui.components.FlankedSlider
import com.bafspeed.app.ui.components.MicroLabel
import com.bafspeed.app.ui.components.ReadWriteButtons
import com.bafspeed.app.ui.components.TelemetryPausedNotice
import com.bafspeed.app.ui.components.TokenCard
import com.bafspeed.app.ui.theme.Manrope
import com.bafspeed.app.ui.theme.Sora
import com.bafspeed.app.ui.theme.Tokens

/**
 * Zakladka "Poziomy wspomagania" - wybor poziomu PAS 0-9 (kafelki o rownej szerokosci,
 * wszystkie miesza sie na ekranie na raz) + limit predkosci/pradu dla wybranego poziomu
 * (suwaki -/+ krok 1%, ten sam wzorzec co Pedal/Throttle/General).
 */
@Composable
fun AssistLevelsScreen(
    state: UiState,
    onCurrentChange: (level: Int, pct: Int) -> Unit,
    onSpeedChange: (level: Int, pct: Int) -> Unit,
    onRead: () -> Unit,
    onWrite: () -> Unit,
    readWriteEnabled: Boolean,
    monitoringActive: Boolean,
) {
    var selected by remember { mutableIntStateOf(0) }
    val basic = state.basicOrDefault

    Column(
        Modifier
            .fillMaxSize()
            .background(Tokens.Bg)
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = 16.dp)),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ReadWriteButtons(onRead = onRead, onWrite = onWrite, enabled = readWriteEnabled)
        TelemetryPausedNotice(monitoringActive = monitoringActive, aodActive = state.aodEnabled)

        // Selektor poziomow 0-9 - kazdy kafelek ma rowna szerokosc (weight), wiec wszystkie
        // 10 miesci sie na jednym ekranie bez przewijania w bok, niezaleznie od szerokosci telefonu.
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
                    Text(
                        lvl.toString(),
                        fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                        color = Tokens.OnAccent,
                    )
                }
            }
        }

        ExpandableParamTile(
            label = tr("Poziom $selected - limit prędkości", "Level $selected - speed limit", de = "Stufe $selected - Geschwindigkeitslimit", fr = "Niveau $selected - limite de vitesse", es = "Nivel $selected - límite de velocidad", pt = "Nível $selected - limite de velocidade", it = "Livello $selected - limite di velocità", nl = "Niveau $selected - snelheidslimiet", sv = "Nivå $selected - hastighetsgräns", cs = "Úroveň $selected - limit rychlosti", sk = "Úroveň $selected - limit rýchlosti", da = "Niveau $selected - hastighedsgrænse", ru = "Уровень $selected - ограничение скорости"),
            valueLabel = "${basic.assistSpeedPct[selected]}%",
            description = tr(
                "Procent maksymalnej prędkości (ustawionej w Bafang Pedal (PAS) / Bafang Throttle) " +
                    "dostępny na tym poziomie wspomagania. Np. jeśli limit prędkości to 40km/h, a tu ustawisz 50%, " +
                    "na tym poziomie osiągniesz maksymalnie 20km/h.",
                "Percentage of the maximum speed (set in Bafang Pedal (PAS) / Bafang Throttle) " +
                    "available at this assist level. E.g. if the speed limit is 40km/h and you set 50% here, " +
                    "at this level you'll reach a maximum of 20km/h.",
                de = "Prozentsatz der maximalen Geschwindigkeit (eingestellt in Bafang Pedal (PAS) / Bafang Throttle), " +
                    "der auf dieser Unterstützungsstufe verfügbar ist. Wenn z. B. das Geschwindigkeitslimit 40 km/h " +
                    "beträgt und du hier 50% einstellst, erreichst du auf dieser Stufe maximal 20 km/h.",
                fr = "Pourcentage de la vitesse maximale (définie dans Bafang Pedal (PAS) / Bafang Throttle) " +
                    "disponible à ce niveau d'assistance. Par ex. si la limite de vitesse est de 40 km/h et que vous " +
                    "réglez 50% ici, vous atteindrez au maximum 20 km/h à ce niveau.",
                es = "Porcentaje de la velocidad máxima (ajustada en Bafang Pedal (PAS) / Bafang Throttle) disponible " +
                    "en este nivel de asistencia. P. ej., si el límite de velocidad es de 40 km/h y aquí ajustas el " +
                    "50%, en este nivel alcanzarás como máximo 20 km/h.",
                pt = "Percentagem da velocidade máxima (definida em Bafang Pedal (PAS) / Bafang Throttle) disponível " +
                    "neste nível de assistência. Por ex., se o limite de velocidade for 40 km/h e aqui definires 50%, " +
                    "neste nível atingirás no máximo 20 km/h.",
                it = "Percentuale della velocità massima (impostata in Bafang Pedal (PAS) / Bafang Throttle) " +
                    "disponibile a questo livello di assistenza. Ad es. se il limite di velocità è 40 km/h e qui " +
                    "imposti il 50%, a questo livello raggiungerai un massimo di 20 km/h.",
                nl = "Percentage van de maximale snelheid (ingesteld in Bafang Pedal (PAS) / Bafang Throttle) " +
                    "beschikbaar op dit ondersteuningsniveau. Bijv. als de snelheidslimiet 40 km/u is en je stelt " +
                    "hier 50% in, bereik je op dit niveau maximaal 20 km/u.",
                sv = "Procentandel av maxhastigheten (inställd i Bafang Pedal (PAS) / Bafang Throttle) som är " +
                    "tillgänglig på denna stödnivå. T.ex. om hastighetsgränsen är 40 km/h och du ställer in 50% " +
                    "här, når du max 20 km/h på denna nivå.",
                cs = "Procento maximální rychlosti (nastavené v Bafang Pedal (PAS) / Bafang Throttle) dostupné " +
                    "na této úrovni asistence. Např. pokud je limit rychlosti 40 km/h a zde nastavíš 50%, na této " +
                    "úrovni dosáhneš maximálně 20 km/h.",
                sk = "Percento maximálnej rýchlosti (nastavenej v Bafang Pedal (PAS) / Bafang Throttle) dostupné " +
                    "na tejto úrovni asistencie. Napr. ak je limit rýchlosti 40 km/h a tu nastavíš 50%, na tejto " +
                    "úrovni dosiahneš maximálne 20 km/h.",
                da = "Procentdel af maksimalhastigheden (indstillet i Bafang Pedal (PAS) / Bafang Throttle) der er " +
                    "tilgængelig på dette understøttelsesniveau. F.eks. hvis hastighedsgrænsen er 40 km/t, og du " +
                    "indstiller 50% her, når du på dette niveau maksimalt 20 km/t.",
                ru = "Процент от максимальной скорости (заданной в Bafang Pedal (PAS) / Bafang Throttle), " +
                    "доступный на этом уровне помощи. Например, если ограничение скорости - 40 км/ч, а здесь вы " +
                    "установите 50%, на этом уровне вы достигнете максимум 20 км/ч.",
            ),
        ) {
            FlankedSlider(
                value = basic.assistSpeedPct[selected],
                range = 0..100,
                accent = Tokens.Emerald,
                onValueChange = { onSpeedChange(selected, it) },
            )
        }

        ExpandableParamTile(
            label = tr("Poziom $selected - limit prądu", "Level $selected - current limit", de = "Stufe $selected - Strombegrenzung", fr = "Niveau $selected - limite de courant", es = "Nivel $selected - límite de corriente", pt = "Nível $selected - limite de corrente", it = "Livello $selected - limite di corrente", nl = "Niveau $selected - stroomlimiet", sv = "Nivå $selected - strömgräns", cs = "Úroveň $selected - limit proudu", sk = "Úroveň $selected - limit prúdu", da = "Niveau $selected - strømgrænse", ru = "Уровень $selected - ограничение тока"),
            valueLabel = "${basic.assistCurrentPct[selected]}%",
            description = tr(
                "Procent maksymalnego prądu (Current Limit z Bafang Basic) dostępny na tym poziomie " +
                    "wspomagania. Np. jeśli Current Limit to 24A, a tu ustawisz 50%, na tym poziomie silnik dostanie " +
                    "maksymalnie 12A.",
                "Percentage of the maximum current (Current Limit from Bafang Basic) available at this " +
                    "assist level. E.g. if Current Limit is 24A and you set 50% here, at this level the motor will " +
                    "get a maximum of 12A.",
                de = "Prozentsatz des maximalen Stroms (Current Limit aus Bafang Basic), der auf dieser " +
                    "Unterstützungsstufe verfügbar ist. Wenn z. B. Current Limit 24A beträgt und du hier 50% " +
                    "einstellst, erhält der Motor auf dieser Stufe maximal 12A.",
                fr = "Pourcentage du courant maximal (Current Limit de Bafang Basic) disponible à ce niveau " +
                    "d'assistance. Par ex. si Current Limit est de 24A et que vous réglez 50% ici, le moteur recevra " +
                    "au maximum 12A à ce niveau.",
                es = "Porcentaje de la corriente máxima (Current Limit de Bafang Basic) disponible en este nivel de " +
                    "asistencia. P. ej., si Current Limit es 24A y aquí ajustas el 50%, en este nivel el motor " +
                    "recibirá como máximo 12A.",
                pt = "Percentagem da corrente máxima (Current Limit do Bafang Basic) disponível neste nível de " +
                    "assistência. Por ex., se Current Limit for 24A e aqui definires 50%, neste nível o motor receberá " +
                    "no máximo 12A.",
                it = "Percentuale della corrente massima (Current Limit da Bafang Basic) disponibile a questo " +
                    "livello di assistenza. Ad es. se Current Limit è 24A e qui imposti il 50%, a questo livello il " +
                    "motore riceverà un massimo di 12A.",
                nl = "Percentage van de maximale stroom (Current Limit uit Bafang Basic) beschikbaar op dit " +
                    "ondersteuningsniveau. Bijv. als Current Limit 24A is en je stelt hier 50% in, krijgt de motor op " +
                    "dit niveau maximaal 12A.",
                sv = "Procentandel av maxströmmen (Current Limit från Bafang Basic) som är tillgänglig på denna " +
                    "stödnivå. T.ex. om Current Limit är 24A och du ställer in 50% här, får motorn max 12A på " +
                    "denna nivå.",
                cs = "Procento maximálního proudu (Current Limit z Bafang Basic) dostupné na této úrovni " +
                    "asistence. Např. pokud je Current Limit 24A a zde nastavíš 50%, na této úrovni motor dostane " +
                    "maximálně 12A.",
                sk = "Percento maximálneho prúdu (Current Limit z Bafang Basic) dostupné na tejto úrovni " +
                    "asistencie. Napr. ak je Current Limit 24A a tu nastavíš 50%, na tejto úrovni motor dostane " +
                    "maximálne 12A.",
                da = "Procentdel af maksimalstrømmen (Current Limit fra Bafang Basic) der er tilgængelig på dette " +
                    "understøttelsesniveau. F.eks. hvis Current Limit er 24A, og du indstiller 50% her, får motoren " +
                    "maksimalt 12A på dette niveau.",
                ru = "Процент от максимального тока (Current Limit из Bafang Basic), доступный на этом уровне " +
                    "помощи. Например, если Current Limit равен 24А, а здесь вы установите 50%, на этом уровне " +
                    "мотор получит максимум 12А.",
            ),
        ) {
            FlankedSlider(
                value = basic.assistCurrentPct[selected],
                range = 0..100,
                accent = Tokens.Amber,
                onValueChange = { onCurrentChange(selected, it) },
            )
        }

        // Wykres mocy - wszystkie poziomy na raz (tylko podglad, bez edycji)
        TokenCard(borderColor = Color(0x59FFFFFF)) {
            MicroLabel(tr("Rozkład mocy - wszystkie poziomy", "Power distribution - all levels", de = "Leistungsverteilung - alle Stufen", fr = "Répartition de puissance - tous niveaux", es = "Distribución de potencia - todos los niveles", pt = "Distribuição de potência - todos os níveis", it = "Distribuzione di potenza - tutti i livelli", nl = "Vermogensverdeling - alle niveaus", sv = "Effektfördelning - alla nivåer", cs = "Rozložení výkonu - všechny úrovně", sk = "Rozloženie výkonu - všetky úrovne", da = "Effektfordeling - alle niveauer", ru = "Распределение мощности - все уровни"))
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                basic.assistCurrentPct.forEachIndexed { i, pct ->
                    val color = if (i == selected) Tokens.Amber else Tokens.Blue
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height((pct.coerceIn(2, 100) * 0.9f).dp)
                                .background(color, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(i.toString(), fontFamily = Manrope, fontSize = 10.sp, color = Tokens.TextSecondary)
                    }
                }
            }
        }

        // Wykres predkosci - wszystkie poziomy na raz (tylko podglad, bez edycji) - identyczny
        // uklad co wykres mocy powyzej, ale dla assistSpeedPct.
        TokenCard(borderColor = Color(0x59FFFFFF)) {
            MicroLabel(tr("Rozkład prędkości - wszystkie poziomy", "Speed distribution - all levels", de = "Geschwindigkeitsverteilung - alle Stufen", fr = "Répartition de vitesse - tous niveaux", es = "Distribución de velocidad - todos los niveles", pt = "Distribuição de velocidade - todos os níveis", it = "Distribuzione di velocità - tutti i livelli", nl = "Snelheidsverdeling - alle niveaus", sv = "Hastighetsfördelning - alla nivåer", cs = "Rozložení rychlosti - všechny úrovně", sk = "Rozloženie rýchlosti - všetky úrovne", da = "Hastighedsfordeling - alle niveauer", ru = "Распределение скорости - все уровни"))
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                basic.assistSpeedPct.forEachIndexed { i, pct ->
                    val color = if (i == selected) Tokens.Amber else Tokens.Blue
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height((pct.coerceIn(2, 100) * 0.9f).dp)
                                .background(color, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(i.toString(), fontFamily = Manrope, fontSize = 10.sp, color = Tokens.TextSecondary)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
