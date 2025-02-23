package sims.michael.joplin2obsidian

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class UtilsKtTest {
    @Test
    fun `toStringWithClickableURI works as expected`() {
        assertEquals(
            "/mnt/temp/name.ext (file:///mnt/temp/name.ext)",
            File("/mnt/temp/name.ext").toStringWithClickableURI()
        )
    }
}
