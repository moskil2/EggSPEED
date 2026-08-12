# EggSPEED

<p align="center">
  <img src="LOGO.png" width="180" alt="EggSPEED icon" />
</p>

<p align="center">
  A modern Android app for Bafang BBS01 / BBS02 / BBSHD mid-drive controllers, talking directly to the controller over UART (USB OTG programming cable), now supporting both factory OEM Bafang firmware and <a href="https://github.com/danielnilsson9/bbs-fw">bbs-fw</a>, switchable in Settings.
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
3. **Dual firmware support** - switch between factory OEM Bafang firmware and [bbs-fw](https://github.com/danielnilsson9/bbs-fw) in Settings. EggSPEED speaks both configuration protocols natively, with dedicated screens for bbs-fw's own settings (System, Assist Levels, firmware/version info) built to match the field names and layout of bbs-fw's own official Windows configuration tool.
4. **Current calibration** - correct the app's displayed current/power reading for controllers with a shunt mod. This only adjusts what's shown in the app - nothing is written to the controller.
5. **Voltage calibration** - apply a manual offset to correct the estimated pack voltage shown in the cockpit.
6. **Battery range estimate** - predicted remaining range in km, blending your riding history with your current riding style.
7. **Instantaneous power draw** - live battery power usage in watts.
8. **Average energy usage** - trip-average and short-term energy consumption in Wh/km.
9. **Firmware-aware profiles** - save/export/import named configuration profiles; loading is blocked with a clear error if the profile's firmware doesn't match the one currently selected, instead of silently writing into the wrong fields.

### BBS-FW screens

<table align="center">
  <tr>
    <td><img src="screenshots/7.jpg" width="140" alt="Settings screen with the OEM Bafang / BBS-FW firmware switch" /></td>
    <td><img src="screenshots/8.jpg" width="140" alt="BBS-FW System screen, Global and Throttle settings" /></td>
    <td><img src="screenshots/9.jpg" width="140" alt="BBS-FW System screen, Features and Speed Sensor settings" /></td>
    <td><img src="screenshots/10.jpg" width="140" alt="BBS-FW Assist Levels screen, Standard/Sport pages" /></td>
  </tr>
</table>

## Download

EggSPEED is currently in **closed testing** on Google Play, so the install link only works for testers. It takes two clicks to get in:

1. **[Join the tester group](https://groups.google.com/g/eggspeed)** - sign in with the same Google account you use on the Play Store, then hit "Join group".
2. **[Open the Play Store testing link](https://play.google.com/apps/testing/app.spotrobotics.eggspeed)** on your phone - once you're a member, Google Play unlocks the install button for you automatically.

That's it - after the first install, EggSPEED updates itself through Google Play like any other app.

## Safety model

EggSPEED can read from and write to the controller's configuration - it is no longer read-only, on either firmware. It never flashes firmware, and firmware flashing is not planned at all. On OEM Bafang, commands sent to the controller fall into four categories:

| Command | Bytes | Nature |
|---|---|---|
| Read GEN/BAS/PAS/THR blocks | `0x11 + address` | pure read |
| Telemetry (brake/battery/speed/current) | `0x11 + 0x08/0x11/0x20/0x0A` | pure read |
| Display init / light / assist level | `0x16 0x1A 0xF0/0xF1` / `0x16 0x0B <code> <checksum>` | transient, same as the factory display - doesn't modify persistent memory |
| Write BAS/PAS/THR block | `0x16 + address + data + LRC` | persistent write to controller flash |

On bbs-fw, the protocol is bbs-fw's own (request type `0x01` read / `0x02` write + opcode, same checksum algorithm) - it explicitly rejects the OEM Bafang Configuration Tool's frames, so the two protocols never collide on the wire:

| Command | Bytes | Nature |
|---|---|---|
| Read firmware version / config | `0x01 + 0x01/0x03` | pure read |
| Telemetry (bbs-fw's own display-compat layer, 9 known registers) | `0x11 + 0x08/0x0A/0x11/0x20/0x21/0x22/0x24/0x25/0x31` | pure read |
| Write config | `0x02 + 0xF1 + config bytes + checksum` | persistent write to controller flash, ACK'd by the controller |

Every write goes through two safety layers before anything is sent:
1. **Client-side clamping** - every value is coerced into a conservative, protocol-safe range before the write frame is even built, regardless of where the value came from (bbs-fw ranges are taken directly from the author's own official Windows tool source, including controller-aware current limits: BBSHD 33A / BBS02 30A / TSDZ2 20A).
2. **Controller-side confirmation** - the controller validates the write itself and returns a status code; the app decodes it and surfaces a specific error message instead of a generic failure.

### Core features
- USB OTG connection (Bafang programming cable, UART 1200 baud 8N1; DTR/RTS asserted on OEM only - bbs-fw's own official tool never touches them)
- Controller identification (OEM: manufacturer, model, HW/FW versions, voltage, max current; bbs-fw: firmware version, config format version, controller type)
- Full configuration read and write: OEM Basic / Pedal Assist / Throttle, or bbs-fw System / Assist Levels
- Live dashboard: speed (wheel RPM × circumference), battery %, power W (estimated on OEM, real ADC reading on bbs-fw), brake
- Display-style controls: assist level 0-9, light, Sport/Normal ride mode toggle (bbs-fw)
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
  protocol/            # Protocol layer - pure Kotlin, no Android dependency
    Lrc.kt             # checksum (byte sum mod 256), shared by both protocols
    BafangCommands.kt  # OEM Bafang commands (reads, writes, transient display commands)
    BafangModels.kt    # OEM data models + GEN/BAS/PAS/THR block decoders
    ConfigFrameParser.kt   # OEM config response framing
    DisplayStateMachine.kt # telemetry loop (controller polling cycle state machine), shared by both firmwares
    BbsFwCommands.kt   # bbs-fw commands (read FW version/config, write config)
    BbsFwModels.kt     # bbs-fw config_t model (1:1 with cfgstore.h) + assist level model
    BbsFwFrameParser.kt    # bbs-fw response framing
    BbsFwValidation.kt # bbs-fw field validation ranges (from the official Windows tool's Configuration.cs)
    BbsFwWriteResponseParser.kt # bbs-fw write ACK parsing
    BbsFwWriter.kt      # builds the bbs-fw config write frame
  profile/
    ProfileIo.kt        # OEM profile format - .ini, compatible with the factory Bafang Configuration Tool
    BbsFwProfileIo.kt   # bbs-fw profile format - Base64 of the same config bytes sent over the wire, tagged with a firmware marker + CONFIG_VERSION check
  serial/
    UsbSerialManager.kt    # USB OTG UART (usb-serial-for-android), 1200 baud 8N1, DTR/RTS asserted on OEM only
  ui/                  # Jetpack Compose, custom design tokens
    screens/BbsFwInfoScreen.kt, BbsFwSystemScreen.kt, BbsFwAssistLevelsScreen.kt  # bbs-fw-only screens
  AppViewModel.kt      # app state, connection sequence, display mode, firmware switch
```

## Known protocol gotchas (relevant to writing)

### OEM Bafang
1. **SMM in the BAS block**: writing SMM=1 as `0x10` is inconsistent with the Bafang standard - the controller expects `SMM*64` in the upper bits. Use `SMM*64 + SMS` when writing.
2. **0xFF sentinel**: DA/SL/WM = "display-controlled" encodes as `0xFF`, other values use offsets (DA-1, SL+14, WM+9).
3. **Wheel diameter**: `WD==12(700C) → 55; WD<12 → (WD+16)*2; WD>12 → (WD+15)*2`.
4. **24V power formula**: `21.7 + 7.7·bat%`.
5. The controller validates writes and returns per-parameter error codes - the app still validates client-side before sending (see Safety model above).

### bbs-fw
1. **Always verify against the latest tagged GitHub release, not `main`** - `main` can contain merged-but-unreleased struct changes (this bit us once: our config model briefly matched an unreleased `CONFIG_VERSION=5` struct instead of the shipping `v1.5.0`/`CONFIG_VERSION=4`, which every real controller in the wild actually runs).
2. **Don't assert DTR/RTS** - the official `BBSFWTool.exe` never touches them (both stay at the .NET default `false`); forcing them high broke the connection for testers.
3. **Retry the initial identification read** - the official tool resends the firmware-version request every 200ms for up to 120s rather than giving up after one timeout; controllers can be slow to respond on the programming port.
4. **`try_process_bafang_read_request` (bbs-fw's display-compat layer) only implements 9 opcodes** - it silently ignores everything else, including the OEM Configuration Tool's GEN/BAS/PAS/THR block reads (`0x51`-`0x54`) - by design, not a bug.
