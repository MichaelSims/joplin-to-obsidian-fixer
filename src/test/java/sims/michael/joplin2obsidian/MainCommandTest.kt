package sims.michael.joplin2obsidian

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MainCommandTest {
    @Test
    fun `main with dry run completes successfully`() {
        val args = with(TestConfig) {
            arrayOf(
                inputPath,
                "--working-dir-override",
                workingDirOverride,
                "--dry-run"
            )
        }
        MainCommand().main(args)
    }
}
