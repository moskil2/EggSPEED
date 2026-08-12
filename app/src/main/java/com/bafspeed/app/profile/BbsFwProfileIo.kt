package com.bafspeed.app.profile

import com.bafspeed.app.protocol.BbsFwCommands
import com.bafspeed.app.protocol.BbsFwConfig
import java.util.Base64

/**
 * Zapis/odczyt profilu bbs-fw - w przeciwieństwie do [ProfileIo] (format .ini kompatybilny z
 * fabrycznym Bafang Configuration Tool) nie ma tu żadnego zewnętrznego formatu do zachowania
 * (oficjalne narzędzie Windows autora bbs-fw ma własny, osobny eksport/import XML - świadomie
 * poza zakresem tej apki). Profil to Base64 z surowej ramki [BbsFwConfig.serialize] plus numer
 * CONFIG_VERSION do walidacji zgodności, zamiast osobnej, ręcznie utrzymywanej listy pól - to
 * właśnie rozjazd dwóch niezależnych źródeł prawdy (nasz model vs. niewydana wersja configu)
 * spowodował błąd "Unsupported bbs-fw config version" naprawiany wcześniej (patrz CHANGELOG
 * v0.3.19-OSF) - tu nie ma jak się rozjechać, bo to dokładnie te same bajty co na drucie.
 */
object BbsFwProfileIo {
    /** Pierwsza linia pliku - odróżnia profil bbs-fw od profilu OEM (który jej nie ma). */
    const val FIRMWARE_MARKER = "; EggSPEED-Firmware=BBS_FW"

    fun serialize(cfg: BbsFwConfig): String = buildString {
        appendLine(FIRMWARE_MARKER)
        appendLine("; EggSPEED bbs-fw profile - nie edytuj ręcznie")
        appendLine("ConfigVersion=${BbsFwCommands.CONFIG_VERSION}")
        val bytes = cfg.serialize().map { it.toByte() }.toByteArray()
        appendLine("Data=${Base64.getEncoder().encodeToString(bytes)}")
    }

    /** Rzuca IllegalArgumentException gdy brak/błędne dane albo niezgodna wersja configu. */
    fun parse(text: String): BbsFwConfig {
        val fields = mutableMapOf<String, String>()
        text.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith(";")) return@forEach
            val idx = line.indexOf('=')
            if (idx > 0) fields[line.substring(0, idx).trim()] = line.substring(idx + 1).trim()
        }
        val version = fields["ConfigVersion"]?.toIntOrNull()
            ?: throw IllegalArgumentException("Brak lub błędne pole ConfigVersion")
        if (version != BbsFwCommands.CONFIG_VERSION) {
            throw IllegalArgumentException(
                "Profil zapisany dla innej wersji configu bbs-fw (v$version) - ta wersja aplikacji zna v${BbsFwCommands.CONFIG_VERSION}.",
            )
        }
        val data = fields["Data"] ?: throw IllegalArgumentException("Brak danych profilu (Data)")
        val bytes = runCatching { Base64.getDecoder().decode(data) }.getOrNull()
            ?: throw IllegalArgumentException("Nieprawidłowe dane profilu (błąd Base64)")
        return BbsFwConfig.deserialize(bytes.map { it.toInt() and 0xFF })
    }
}
