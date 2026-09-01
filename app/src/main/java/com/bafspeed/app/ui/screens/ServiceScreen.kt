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

/**
 * Zakładka "PROTECT" - domyślnie otwarta (pusty PIN = brak ochrony). Użytkownik może w środku
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
                    "PROTECT to funkcja bezpieczeństwa, która pozwala Ci szybko i niepostrzeżenie zablokować rower, kiedy tego potrzebujesz. Po naciśnięciu przycisku PROTECT na Kokpicie rower przechodzi w tryb 0, ale ekran w dalszym ciągu pozoruje możliwość zmiany poziomu wspomagania. Odblokowanie roweru wymaga wejścia do zakładki PROTECT i wciśnięcia przycisku odblokowania. Ikona SAFE na Kokpicie sygnalizuje aktywną blokadę roweru.",
                    "PROTECT is a safety feature that lets you lock the bike down quickly and inconspicuously whenever you need to. Pressing the PROTECT button on the Cockpit switches the bike to mode 0, while the screen keeps pretending you can still change the assist level. Unlocking the bike requires opening the PROTECT tab and pressing the unlock button. The SAFE icon on the Cockpit signals that the bike's lock is active.",
                    de = "PROTECT ist eine Sicherheitsfunktion, mit der du das Fahrrad bei Bedarf schnell und unauffällig sperren kannst. Durch Drücken der PROTECT-Schaltfläche im Cockpit wechselt das Fahrrad in Modus 0, während der Bildschirm weiterhin vortäuscht, dass die Unterstützungsstufe geändert werden kann. Zum Entsperren muss der PROTECT-Tab geöffnet und die Entsperren-Schaltfläche gedrückt werden. Das SAFE-Symbol im Cockpit zeigt an, dass die Sperre aktiv ist.",
                    fr = "PROTECT est une fonction de sécurité qui vous permet de verrouiller le vélo rapidement et discrètement en cas de besoin. Appuyer sur le bouton PROTECT du Cockpit fait passer le vélo en mode 0, tandis que l'écran continue de simuler la possibilité de changer le niveau d'assistance. Déverrouiller le vélo nécessite d'ouvrir l'onglet PROTECT et d'appuyer sur le bouton de déverrouillage. L'icône SAFE sur le Cockpit signale que le verrouillage du vélo est actif.",
                    es = "PROTECT es una función de seguridad que te permite bloquear la bicicleta de forma rápida y discreta cuando lo necesites. Al pulsar el botón PROTECT en el Cockpit, la bicicleta pasa al modo 0, mientras la pantalla sigue simulando que puedes cambiar el nivel de asistencia. Para desbloquear la bicicleta hay que abrir la pestaña PROTECT y pulsar el botón de desbloqueo. El icono SAFE en el Cockpit indica que el bloqueo de la bicicleta está activo.",
                    pt = "PROTECT é uma função de segurança que te permite bloquear a bicicleta de forma rápida e discreta sempre que precisares. Ao premir o botão PROTECT no Cockpit, a bicicleta passa para o modo 0, enquanto o ecrã continua a simular que ainda podes alterar o nível de assistência. Desbloquear a bicicleta requer abrir o separador PROTECT e premir o botão de desbloqueio. O ícone SAFE no Cockpit indica que o bloqueio da bicicleta está ativo.",
                    it = "PROTECT è una funzione di sicurezza che ti permette di bloccare la bici rapidamente e in modo discreto ogni volta che ne hai bisogno. Premendo il pulsante PROTECT sul Cockpit, la bici passa alla modalità 0, mentre lo schermo continua a fingere che tu possa ancora cambiare il livello di assistenza. Per sbloccare la bici bisogna aprire la scheda PROTECT e premere il pulsante di sblocco. L'icona SAFE sul Cockpit indica che il blocco della bici è attivo.",
                    nl = "PROTECT is een veiligheidsfunctie waarmee je de fiets snel en onopvallend kunt vergrendelen wanneer nodig. Door op de PROTECT-knop op de Cockpit te drukken, schakelt de fiets naar modus 0, terwijl het scherm blijft doen alsof je nog steeds het ondersteuningsniveau kunt wijzigen. Om de fiets te ontgrendelen moet je het PROTECT-tabblad openen en op de ontgrendelknop drukken. Het SAFE-pictogram op de Cockpit geeft aan dat de vergrendeling van de fiets actief is.",
                    sv = "PROTECT är en säkerhetsfunktion som gör att du snabbt och diskret kan låsa cykeln när du behöver det. Att trycka på PROTECT-knappen på Cockpit växlar cykeln till läge 0, medan skärmen fortsätter låtsas att du fortfarande kan ändra assistansnivån. Att låsa upp cykeln kräver att du öppnar fliken PROTECT och trycker på upplåsningsknappen. SAFE-ikonen på Cockpit visar att cykelns lås är aktivt.",
                    cs = "PROTECT je bezpečnostní funkce, která ti umožní kolo rychle a nenápadně zablokovat, kdykoli to potřebuješ. Stisknutím tlačítka PROTECT v Cockpitu se kolo přepne do režimu 0, zatímco obrazovka nadále předstírá, že stále můžeš měnit úroveň asistence. Odemknutí kola vyžaduje otevření karty PROTECT a stisknutí tlačítka odemknutí. Ikona SAFE v Cockpitu signalizuje, že je zablokování kola aktivní.",
                    sk = "PROTECT je bezpečnostná funkcia, ktorá ti umožní bicykel rýchlo a nenápadne zablokovať, kedykoľvek to potrebuješ. Stlačením tlačidla PROTECT v Cockpite sa bicykel prepne do režimu 0, pričom obrazovka naďalej predstiera, že stále môžeš meniť úroveň asistencie. Odomknutie bicykla vyžaduje otvorenie karty PROTECT a stlačenie tlačidla odomknutia. Ikona SAFE v Cockpite signalizuje, že je zablokovanie bicykla aktívne.", da = "PROTECT er en sikkerhedsfunktion, der lader dig låse cyklen hurtigt og diskret, når som helst du har brug for det. Når du trykker på PROTECT-knappen på Cockpit, skifter cyklen til tilstand 0, mens skærmen fortsat lader som om du stadig kan ændre understøttelsesniveauet. For at låse cyklen op skal du åbne fanen PROTECT og trykke på oplåsningsknappen. SAFE-ikonet på Cockpit viser, at cyklens lås er aktiv.", ru = "PROTECT - это функция безопасности, позволяющая быстро и незаметно заблокировать велосипед, когда это нужно. При нажатии кнопки PROTECT на Кокпите велосипед переключается в режим 0, при этом экран продолжает делать вид, что уровень помощи можно изменить. Для разблокировки велосипеда нужно открыть вкладку PROTECT и нажать кнопку разблокировки. Значок SAFE на Кокпите сигнализирует, что блокировка велосипеда активна.",
                ),
            )

            MicroLabel(tr("PROTECT", "PROTECT"))
            TokenCard(borderColor = Tokens.WhiteBorder) {
                ToggleRow(
                    label = tr("Funkcja PROTECT", "PROTECT feature", de = "PROTECT-Funktion", fr = "Fonction PROTECT", es = "Función PROTECT", pt = "Função PROTECT", it = "Funzione PROTECT", nl = "PROTECT-functie", sv = "PROTECT-funktion", cs = "Funkce PROTECT", sk = "Funkcia PROTECT", da = "PROTECT-funktion", ru = "Функция PROTECT"),
                    checked = state.protectFeatureEnabled,
                    onCheckedChange = onProtectFeatureEnabledChange,
                    accent = Tokens.Blue,
                    description = tr(
                        "Gdy włączona, na Kokpicie pojawia się dodatkowy przycisk, który pozwala użyć funkcji PROTECT w razie potrzeby.",
                        "When on, an extra button appears on the Cockpit that lets you use the PROTECT feature whenever needed.",
                        de = "Wenn aktiviert, erscheint im Cockpit eine zusätzliche Schaltfläche, mit der du die PROTECT-Funktion bei Bedarf nutzen kannst.",
                        fr = "Lorsqu'elle est activée, un bouton supplémentaire apparaît sur le Cockpit, permettant d'utiliser la fonction PROTECT en cas de besoin.",
                        es = "Cuando está activada, aparece un botón adicional en el Cockpit que te permite usar la función PROTECT cuando lo necesites.",
                        pt = "Quando ativada, aparece um botão extra no Cockpit que te permite usar a função PROTECT sempre que necessário.",
                        it = "Quando attiva, appare un pulsante extra sul Cockpit che ti permette di usare la funzione PROTECT quando serve.",
                        nl = "Wanneer ingeschakeld, verschijnt er een extra knop op de Cockpit waarmee je de PROTECT-functie kunt gebruiken wanneer nodig.",
                        sv = "När aktiverad visas en extra knapp på Cockpit som låter dig använda PROTECT-funktionen när det behövs.",
                        cs = "Když je zapnutá, v Cockpitu se objeví další tlačítko, které ti umožní použít funkci PROTECT, kdykoli je potřeba.",
                        sk = "Keď je zapnutá, v Cockpite sa objaví ďalšie tlačidlo, ktoré ti umožní použiť funkciu PROTECT, kedykoľvek je to potrebné.", da = "Når aktiveret, vises der en ekstra knap på Cockpit, som lader dig bruge PROTECT-funktionen, når det er nødvendigt.", ru = "Когда включено, на Кокпите появляется дополнительная кнопка, позволяющая использовать функцию PROTECT при необходимости.",
                    ),
                )
            }

            Spacer(Modifier.height(2.dp))
            TokenCard(borderColor = if (state.protectActive) Color(0x6634C759) else Color(0x66FF3B30)) {
                Text(
                    if (state.protectActive) {
                        tr("PROTECT jest WŁĄCZONY", "PROTECT is ON", de = "PROTECT ist AN", fr = "PROTECT est ACTIVÉ", es = "PROTECT está ACTIVADO", pt = "PROTECT está ATIVADO", it = "PROTECT è ATTIVO", nl = "PROTECT is AAN", sv = "PROTECT är PÅ", cs = "PROTECT je ZAPNUTÝ", sk = "PROTECT je ZAPNUTÝ", da = "PROTECT er TIL", ru = "PROTECT ВКЛЮЧЁН")
                    } else {
                        tr("PROTECT jest WYŁĄCZONY", "PROTECT is OFF", de = "PROTECT ist AUS", fr = "PROTECT est DÉSACTIVÉ", es = "PROTECT está DESACTIVADO", pt = "PROTECT está DESATIVADO", it = "PROTECT è DISATTIVO", nl = "PROTECT is UIT", sv = "PROTECT är AV", cs = "PROTECT je VYPNUTÝ", sk = "PROTECT je VYPNUTÝ", da = "PROTECT er FRA", ru = "PROTECT ВЫКЛЮЧЕН")
                    },
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
                            tr("Odblokuj (wyłącz PROTECT)", "Unlock (turn PROTECT off)", de = "Entsperren (PROTECT ausschalten)", fr = "Déverrouiller (désactiver PROTECT)", es = "Desbloquear (desactivar PROTECT)", pt = "Desbloquear (desativar PROTECT)", it = "Sblocca (disattiva PROTECT)", nl = "Ontgrendelen (PROTECT uitschakelen)", sv = "Lås upp (stäng av PROTECT)", cs = "Odemknout (vypnout PROTECT)", sk = "Odomknúť (vypnúť PROTECT)", da = "Lås op (sluk PROTECT)", ru = "Разблокировать (выключить PROTECT)"),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Tokens.OnAccent,
                        )
                    }
                }
            }

            MicroLabel(tr("PIN PROTECT", "PROTECT PIN", de = "PROTECT-PIN", fr = "PIN PROTECT", es = "PIN de PROTECT", pt = "PIN do PROTECT", it = "PIN PROTECT", nl = "PROTECT-pincode", sv = "PROTECT-PIN", cs = "PIN PROTECT", sk = "PIN PROTECT", da = "PROTECT-PIN", ru = "PIN PROTECT"))
            TokenCard(borderColor = Tokens.WhiteBorder) {
                Text(
                    tr(
                        "Ustawiony PIN blokuje dostęp do tej zakładki - wejście do PROTECT wymaga wtedy jego podania.",
                        "A set PIN locks access to this tab - entering PROTECT then requires it.",
                        de = "Ein festgelegter PIN sperrt den Zugriff auf diesen Tab - der Zugang zu PROTECT erfordert ihn dann.",
                        fr = "Un PIN défini verrouille l'accès à cet onglet - accéder à PROTECT nécessite alors de le saisir.",
                        es = "Un PIN establecido bloquea el acceso a esta pestaña - entrar en PROTECT requiere entonces introducirlo.",
                        pt = "Um PIN definido bloqueia o acesso a este separador - entrar no PROTECT passa então a exigi-lo.",
                        it = "Un PIN impostato blocca l'accesso a questa scheda - accedere a PROTECT richiederà quindi il suo inserimento.",
                        nl = "Een ingestelde pincode blokkeert de toegang tot dit tabblad - PROTECT openen vereist dan het invoeren ervan.",
                        sv = "En inställd PIN-kod låser åtkomsten till denna flik - att öppna PROTECT kräver då att den anges.",
                        cs = "Nastavený PIN uzamyká přístup na tuto kartu - vstup do PROTECT pak vyžaduje jeho zadání.",
                        sk = "Nastavený PIN uzamyká prístup na túto kartu - vstup do PROTECT potom vyžaduje jeho zadanie.", da = "En indstillet PIN låser adgangen til denne fane - adgang til PROTECT kræver den så.", ru = "Установленный PIN блокирует доступ к этой вкладке - вход в PROTECT потребует его ввода.",
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
        tr("Wpisz PIN, żeby wejść do PROTECT", "Enter the PIN to access PROTECT", de = "PIN eingeben, um PROTECT zu öffnen", fr = "Entrez le PIN pour accéder à PROTECT", es = "Introduce el PIN para acceder a PROTECT", pt = "Introduz o PIN para aceder ao PROTECT", it = "Inserisci il PIN per accedere a PROTECT", nl = "Voer de pincode in om PROTECT te openen", sv = "Ange PIN för att komma åt PROTECT", cs = "Zadej PIN pro vstup do PROTECT", sk = "Zadaj PIN pre vstup do PROTECT", da = "Indtast PIN for at få adgang til PROTECT", ru = "Введите PIN для доступа к PROTECT"),
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
            tr("Zły PIN", "Wrong PIN", de = "Falscher PIN", fr = "PIN incorrect", es = "PIN incorrecto", pt = "PIN incorreto", it = "PIN errato", nl = "Onjuiste pincode", sv = "Fel PIN", cs = "Nesprávný PIN", sk = "Nesprávny PIN", da = "Forkert PIN", ru = "Неверный PIN"),
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
            tr("Zatwierdź", "Confirm", de = "Bestätigen", fr = "Confirmer", es = "Confirmar", pt = "Confirmar", it = "Conferma", nl = "Bevestigen", sv = "Bekräfta", cs = "Potvrdit", sk = "Potvrdiť", da = "Bekræft", ru = "Подтвердить"),
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
            if (saved) {
                tr("Zapisano", "Saved", de = "Gespeichert", fr = "Enregistré", es = "Guardado", pt = "Guardado", it = "Salvato", nl = "Opgeslagen", sv = "Sparat", cs = "Uloženo", sk = "Uložené", da = "Gemt", ru = "Сохранено")
            } else {
                tr("Zapisz PIN", "Save PIN", de = "PIN speichern", fr = "Enregistrer le PIN", es = "Guardar PIN", pt = "Guardar PIN", it = "Salva PIN", nl = "PIN opslaan", sv = "Spara PIN", cs = "Uložit PIN", sk = "Uložiť PIN", da = "Gem PIN", ru = "Сохранить PIN")
            },
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontFamily = Sora, fontWeight = FontWeight.Bold, fontSize = 13.sp,
            color = if (saved) Tokens.OnAccent else Tokens.TextPrimary,
        )
    }
}
