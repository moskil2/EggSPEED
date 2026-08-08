# EggSPEED

A modern Android app for Bafang BBS01 / BBS02 / BBSHD mid-drive controllers, talking directly to the controller over UART (USB OTG programming cable).

## Milestone 1 - "can't be bricked" version (current)

**Safety guarantee:** the app code contains **no** builder for flash-write frames to the controller (`0x16 + 0x52/0x53/0x54`). The only commands ever sent to the controller are:

| Command | Bytes | Nature |
|---|---|---|
| Read GEN/BAS/PAS/THR blocks | `0x11 + address` | pure read |
| Telemetry (brake/battery/speed/current) | `0x11 + 0x08/0x11/0x20/0x0A` | pure read |
| Display init / light | `0x16 0x1A 0xF0/0xF1` | transient, same as the factory display |
| Assist level | `0x16 0x0B <code> <checksum>` | transient, same as the factory display |

The "transient" commands are exactly the frames the factory Bafang display sends cyclically during normal riding - they don't modify persistent memory.

### M1 features
- USB OTG connection (Bafang programming cable, UART 1200 baud 8N1)
- Controller identification (manufacturer, model, HW/FW versions, voltage, max current)
- Full configuration readout: Basic / Pedal Assist / Throttle (preview only)
- Live dashboard: speed (wheel RPM × circumference), battery %, power W (estimated), brake
- Display-style controls: assist level 0-9, light
- Distance counter (speed integration on the app side)

### What M1 deliberately does NOT do
- Write any parameters to the controller
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

## Known protocol gotchas (important before M2 - writing)

1. **SMM in the BAS block**: writing SMM=1 as `0x10` is inconsistent with the Bafang standard - the controller expects `SMM*64` in the upper bits. Use `SMM*64 + SMS` when writing.
2. **0xFF sentinel**: DA/SL/WM = "display-controlled" encodes as `0xFF`, other values use offsets (DA-1, SL+14, WM+9).
3. **Wheel diameter**: `WD==12(700C) → 55; WD<12 → (WD+16)*2; WD>12 → (WD+15)*2`.
4. **24V power formula**: `21.7 + 7.7·bat%`.
5. The controller validates writes and returns per-parameter error codes - M2 must still validate client-side before sending.
