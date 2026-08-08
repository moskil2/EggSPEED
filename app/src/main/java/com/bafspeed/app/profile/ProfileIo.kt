package com.bafspeed.app.profile

import com.bafspeed.app.protocol.BasicSettings
import com.bafspeed.app.protocol.GeneralInfo
import com.bafspeed.app.protocol.PedalAssistSettings
import com.bafspeed.app.protocol.ThrottleSettings

/** Komplet ustawień do zapisu/odczytu profilu. */
data class ProfileData(
    val general: GeneralInfo?,
    val basic: BasicSettings,
    val pedalAssist: PedalAssistSettings,
    val throttle: ThrottleSettings,
)

/**
 * Zapis/odczyt profilu w formacie .ini zgodnym z Windowsowym Bafang Configuration Tool.
 * Dzięki temu profile są wymienne z presetami krążącymi w społeczności Bafang.
 */
object ProfileIo {

    fun serialize(data: ProfileData): String = buildString {
        appendLine("; EggSPEED profile (.ini) - zgodny z Bafang Configuration Tool")
        appendLine("; UWAGA: wczytanie profilu w M1 służy tylko podglądowi - nie zapisuje do sterownika.")
        appendLine()
        data.general?.let { g ->
            appendLine("[General]")
            appendLine("Manufacturer=${g.manufacturer}")
            appendLine("Model=${g.model}")
            appendLine("HardwareVersion=${g.hardwareVersion}")
            appendLine("FirmwareVersion=${g.firmwareVersion}")
            appendLine("NominalVoltage=${g.nominalVoltage}")
            appendLine("MaxCurrent=${g.maxCurrentA}")
            appendLine()
        }
        val b = data.basic
        appendLine("[Basic]")
        appendLine("LBP=${b.lowBatteryProtection}")
        appendLine("LC=${b.currentLimit}")
        b.assistCurrentPct.forEachIndexed { i, v -> appendLine("ALC$i=$v") }
        b.assistSpeedPct.forEachIndexed { i, v -> appendLine("ALBP$i=$v") }
        appendLine("WD=${b.wheelDiameterCode}")
        appendLine("SMM=${b.speedMeterModel}")
        appendLine("SMS=${b.speedMeterSignals}")
        appendLine()
        val p = data.pedalAssist
        appendLine("[Pedal Assist]")
        appendLine("PT=${p.pedalType}")
        appendLine("DA=${p.designatedAssist}")
        appendLine("SL=${p.speedLimit}")
        appendLine("SSM=${p.slowStartMode}")
        appendLine("WM=${p.workMode}")
        appendLine("SC=${p.startCurrentPct}")
        appendLine("SDN=${p.startDegree}")
        appendLine("TS=${p.timeOfStop}")
        appendLine("CD=${p.currentDecay}")
        appendLine("SD=${p.stopDecay}")
        appendLine("KC=${p.keepCurrentPct}")
        appendLine()
        val t = data.throttle
        appendLine("[Throttle Handle]")
        appendLine("SV=${t.startVoltage}")
        appendLine("EV=${t.endVoltage}")
        appendLine("MODE=${t.mode}")
        appendLine("DA=${t.designatedAssist}")
        appendLine("SL=${t.speedLimit}")
        appendLine("SC=${t.startCurrentPct}")
    }

    /** Parsuje .ini. Rzuca IllegalArgumentException gdy brak wymaganych sekcji/pól. */
    fun parse(text: String): ProfileData {
        val sections = mutableMapOf<String, MutableMap<String, String>>()
        var current: MutableMap<String, String>? = null
        text.lineSequence().forEach { rawLine ->
            val line = rawLine.substringBefore(';').substringBefore('#').trim()
            if (line.isEmpty()) return@forEach
            if (line.startsWith("[") && line.endsWith("]")) {
                val name = line.substring(1, line.length - 1).trim().lowercase()
                current = sections.getOrPut(name) { mutableMapOf() }
            } else {
                val idx = line.indexOf('=')
                if (idx > 0) current?.put(line.substring(0, idx).trim().uppercase(), line.substring(idx + 1).trim())
            }
        }

        val basicSec = sections["basic"] ?: throw IllegalArgumentException("Brak sekcji [Basic]")
        val pasSec = sections["pedal assist"] ?: throw IllegalArgumentException("Brak sekcji [Pedal Assist]")
        val thrSec = sections["throttle handle"] ?: throw IllegalArgumentException("Brak sekcji [Throttle Handle]")

        fun Map<String, String>.int(key: String): Int =
            this[key]?.toIntOrNull() ?: throw IllegalArgumentException("Brak lub błędne pole: $key")

        val basic = BasicSettings(
            lowBatteryProtection = basicSec.int("LBP"),
            currentLimit = basicSec.int("LC"),
            assistCurrentPct = (0..9).map { basicSec.int("ALC$it") },
            assistSpeedPct = (0..9).map { basicSec.int("ALBP$it") },
            wheelDiameterCode = basicSec.int("WD"),
            speedMeterModel = basicSec.int("SMM"),
            speedMeterSignals = basicSec.int("SMS"),
        )
        val pas = PedalAssistSettings(
            pedalType = pasSec.int("PT"),
            designatedAssist = pasSec.int("DA"),
            speedLimit = pasSec.int("SL"),
            startCurrentPct = pasSec.int("SC"),
            slowStartMode = pasSec.int("SSM"),
            startDegree = pasSec.int("SDN"),
            workMode = pasSec.int("WM"),
            timeOfStop = pasSec.int("TS"),
            currentDecay = pasSec.int("CD"),
            stopDecay = pasSec.int("SD"),
            keepCurrentPct = pasSec.int("KC"),
        )
        val thr = ThrottleSettings(
            startVoltage = thrSec.int("SV"),
            endVoltage = thrSec.int("EV"),
            mode = thrSec.int("MODE"),
            designatedAssist = thrSec.int("DA"),
            speedLimit = thrSec.int("SL"),
            startCurrentPct = thrSec.int("SC"),
        )
        return ProfileData(general = null, basic = basic, pedalAssist = pas, throttle = thr)
    }
}
