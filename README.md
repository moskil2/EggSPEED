# EggSPEED

<p align="center">
  <img src="LOGO.png" width="180" alt="EggSPEED icon" />
</p>

<p align="center">
  A modern Android app for Bafang BBS01 / BBS02 / BBSHD mid-drive controllers, talking directly to the controller over UART (USB OTG programming cable).
</p>

<table align="center">
  <tr>
    <td><img src="screenshots/1.jpg" width="140" alt="EggSPEED screenshot 1" /></td>
    <td><img src="screenshots/2.jpg" width="140" alt="EggSPEED screenshot 2" /></td>
    <td><img src="screenshots/3.jpg" width="140" alt="EggSPEED screenshot 3" /></td>
    <td><img src="screenshots/4.jpg" width="140" alt="EggSPEED screenshot 4" /></td>
    <td><img src="screenshots/5.jpg" width="140" alt="EggSPEED screenshot 5" /></td>
    <td><img src="screenshots/6.jpg" width="140" alt="EggSPEED screenshot 6" /></td>
  </tr>
</table>

## Features

1. **Full Bafang display replacement** - a live cockpit showing everything the factory display shows, and more.
2. **Live controller programming** - write Basic, Pedal Assist, and Throttle parameters directly to the controller, even while riding.
   - **Motor assist level control** - switch between assist levels 0-9 on the fly, exactly like the stock display.
3. **Current calibration** - correct the app's displayed current/power reading for controllers with a shunt mod. This only adjusts what's shown in the app - nothing is written to the controller.
4. **Voltage calibration** - apply a manual offset to correct the estimated pack voltage shown in the cockpit.
5. **Battery range estimate** - predicted remaining range in km, blending your riding history with your current riding style.
6. **Instantaneous power draw** - live battery power usage in watts.
7. **Average energy usage** - trip-average and short-term energy consumption in Wh/km.

## Safety model

EggSPEED can read from and write to the controller's Basic / Pedal Assist / Throttle configuration blocks - it is no longer read-only. It never flashes firmware, and firmware flashing is not planned at all. Commands sent to the controller fall into four categories:

| Command | Bytes | Nature |
|---|---|---|
| Read GEN/BAS/PAS/THR blocks | `0x11 + address` | pure read |
| Telemetry (brake/battery/speed/current) | `0x11 + 0x08/0x11/0x20/0x0A` | pure read |
| Display init / light / assist level | `0x16 0x1A 0xF0/0xF1` / `0x16 0x0B <code> <checksum>` | transient, same as the factory display - doesn't modify persistent memory |
| Write BAS/PAS/THR block | `0x16 + address + data + LRC` | persistent write to controller flash |

Every write goes through two safety layers before anything is sent:
1. **Client-side clamping** - every value is coerced into a conservative, protocol-safe range before the write frame is even built, regardless of where the value came from.
2. **Controller-side confirmation** - the controller validates the write itself and returns a per-parameter status code; the app decodes it and surfaces a specific error message (e.g. "current limit for level 3 out of range") instead of a generic failure.

### Core features
- USB OTG connection (Bafang programming cable, UART 1200 baud 8N1)
- Controller identification (manufacturer, model, HW/FW versions, voltage, max current)
- Full configuration read and write: Basic / Pedal Assist / Throttle
- Live dashboard: speed (wheel RPM × circumference), battery %, power W (estimated), brake
- Display-style controls: assist level 0-9, light
- Distance counter (speed integration on the app side)

### What EggSPEED deliberately does NOT do
- Flash firmware (not planned at all)
- Bluetooth (planned for the future - needs its own hardware bridge)

## Building

```
JAVA_HOME=<jdk17+> ../tools/gradle-8.10.2/bin/gradle assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. Requires Android 8.0+ (API 26) and a phone with USB Host (OTG).

Unit tests for the protocol layer (framing, LRC, WD/SMM/SMS decoding, 0xFF sentinels):

```
gradle testDebugUnitTest
```

## Architecture

```
app/src/main/java/com/bafspeed/app/
  protocol/            # Bafang protocol layer - pure Kotlin, no Android dependency
    Lrc.kt             # checksum (byte sum mod 256)
    BafangCommands.kt  # ONLY safe commands (reads + transient display commands)
    BafangModels.kt    # data models + GEN/BAS/PAS/THR block decoders
    ConfigFrameParser.kt   # config response framing
    DisplayStateMachine.kt # telemetry loop (controller polling cycle state machine)
  serial/
    UsbSerialManager.kt    # USB OTG UART (usb-serial-for-android), 1200 baud 8N1 DTR+RTS
  ui/                  # Jetpack Compose, custom design tokens
  AppViewModel.kt      # app state, connection sequence, display mode
```

## Known protocol gotchas (relevant to writing)

1. **SMM in the BAS block**: writing SMM=1 as `0x10` is inconsistent with the Bafang standard - the controller expects `SMM*64` in the upper bits. Use `SMM*64 + SMS` when writing.
2. **0xFF sentinel**: DA/SL/WM = "display-controlled" encodes as `0xFF`, other values use offsets (DA-1, SL+14, WM+9).
3. **Wheel diameter**: `WD==12(700C) → 55; WD<12 → (WD+16)*2; WD>12 → (WD+15)*2`.
4. **24V power formula**: `21.7 + 7.7·bat%`.
5. The controller validates writes and returns per-parameter error codes - the app still validates client-side before sending (see Safety model above).
