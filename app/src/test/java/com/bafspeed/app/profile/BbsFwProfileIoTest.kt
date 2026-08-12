package com.bafspeed.app.profile

import com.bafspeed.app.protocol.BbsFwCommands
import com.bafspeed.app.protocol.BbsFwConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class BbsFwProfileIoTest {

    @Test
    fun `serialize-parse round-trip zachowuje pelna konfiguracje`() {
        val original = BbsFwConfig.DEFAULT.copy(maxCurrentAmps = 22, maxSpeedKph = 32)
            .withAssistLevel(0, 3, BbsFwConfig.DEFAULT.assistLevel(0, 3).copy(targetCurrentPercent = 77))
        val text = BbsFwProfileIo.serialize(original)
        val decoded = BbsFwProfileIo.parse(text)
        assertEquals(original, decoded)
    }

    @Test
    fun `plik ma znacznik firmware bbs-fw jako pierwsza linia`() {
        val text = BbsFwProfileIo.serialize(BbsFwConfig.DEFAULT)
        assertTrue(text.lineSequence().first() == BbsFwProfileIo.FIRMWARE_MARKER)
    }

    @Test
    fun `parse odrzuca profil z niezgodna wersja configu`() {
        val text = BbsFwProfileIo.serialize(BbsFwConfig.DEFAULT).replace("ConfigVersion=${BbsFwCommands.CONFIG_VERSION}", "ConfigVersion=99")
        assertThrows(IllegalArgumentException::class.java) { BbsFwProfileIo.parse(text) }
    }

    @Test
    fun `parse odrzuca profil bez danych`() {
        assertThrows(IllegalArgumentException::class.java) { BbsFwProfileIo.parse("; EggSPEED-Firmware=BBS_FW\nConfigVersion=4\n") }
    }
}
