# EggSPEED

Nowoczesna aplikacja Android do sterowników Bafang BBS01 / BBS02 / BBSHD, komunikująca się bezpośrednio z kontrolerem po UART (kabel programujący USB OTG).

## Milestone 1 - wersja "nie da się zbrickować" (bieżąca)

**Gwarancja bezpieczeństwa:** w kodzie aplikacji **nie istnieje** żaden builder ramki zapisu do pamięci flash sterownika (`0x16 + 0x52/0x53/0x54`). Jedyne komendy wysyłane do sterownika to:

| Komenda | Bajty | Charakter |
|---|---|---|
| Odczyt bloków GEN/BAS/PAS/THR | `0x11 + adres` | czysty odczyt |
| Telemetria (hamulec/bateria/prędkość/prąd) | `0x11 + 0x08/0x11/0x20/0x0A` | czysty odczyt |
| Init wyświetlacza / światło | `0x16 0x1A 0xF0/0xF1` | ulotna, jak z fabrycznego wyświetlacza |
| Poziom wspomagania | `0x16 0x0B <kod> <suma>` | ulotna, jak z fabrycznego wyświetlacza |

Komendy "ulotne" to dokładnie te ramki, które fabryczny wyświetlacz Bafang wysyła cyklicznie podczas normalnej jazdy - nie modyfikują pamięci trwałej.

### Funkcje M1
- Połączenie USB OTG (kabel programujący Bafang, UART 1200 baud 8N1)
- Identyfikacja sterownika (producent, model, wersje HW/FW, napięcie, prąd maks.)
- Pełny odczyt konfiguracji: Basic / Pedal Assist / Throttle (tylko podgląd)
- Kokpit na żywo: prędkość (RPM koła × obwód), bateria %, moc W (estymacja), hamulec
- Sterowanie jak z wyświetlacza: poziom wspomagania 0-9, światło
- Licznik dystansu (całkowanie prędkości po stronie aplikacji)

### Czego M1 celowo NIE robi
- Zapisu jakichkolwiek parametrów do sterownika
- Flashowania firmware (nie planowane w ogóle)
- Bluetooth (planowane w przyszłości - wymaga własnego mostka sprzętowego)

## Budowanie

```
JAVA_HOME=<jdk17+> ../tools/gradle-8.10.2/bin/gradle assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. Wymaga Android 8.0+ (API 26) i telefonu z USB Host (OTG).

Testy jednostkowe warstwy protokołu (framing, LRC, dekodowanie WD/SMM/SMS, sentinele 0xFF):

```
gradle testDebugUnitTest
```

## Architektura

```
app/src/main/java/com/bafspeed/app/
  protocol/            # warstwa protokołu Bafang - czysty Kotlin, bez zależności Android
    Lrc.kt             # suma kontrolna (suma bajtów mod 256)
    BafangCommands.kt  # WYŁĄCZNIE bezpieczne komendy (odczyt + ulotne komendy wyświetlacza)
    BafangModels.kt    # modele danych + dekodery bloków GEN/BAS/PAS/THR
    ConfigFrameParser.kt   # framing odpowiedzi konfiguracji
    DisplayStateMachine.kt # pętla telemetrii (maszyna stanów cyklu odpytywania kontrolera)
  serial/
    UsbSerialManager.kt    # USB OTG UART (usb-serial-for-android), 1200 baud 8N1 DTR+RTS
  ui/                  # Jetpack Compose, własne tokeny designu
  AppViewModel.kt      # stan aplikacji, sekwencja połączenia, tryb wyświetlacza
```

## Znane pułapki protokołu (ważne przed M2 - zapisem)

1. **SMM w bloku BAS**: zapis SMM=1 jako `0x10` jest niezgodny ze standardem Bafang - kontroler oczekuje `SMM*64` w górnych bitach. Przy zapisie używać `SMM*64 + SMS`.
2. **Sentinel 0xFF**: DA/SL/WM = "sterowane wyświetlaczem" koduje się jako `0xFF`, inne wartości z przesunięciami (DA-1, SL+14, WM+9).
3. **Średnica koła**: `WD==12(700C) → 55; WD<12 → (WD+16)*2; WD>12 → (WD+15)*2`.
4. **Wzór mocy dla 24 V**: `21.7 + 7.7·bat%`.
5. Sterownik waliduje zapisy i zwraca kody błędów per parametr - M2 musi walidować przed wysyłką.
