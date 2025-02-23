package sims.michael.joplin2obsidian

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MainCommandTest {
    @Test
    fun name() {
        MainCommand().main(arrayOf("/mnt/c/Users/micha/Downloads/joplin-md-export", "--working-dir-override", "/tmp/sims.michael.joplin2obsidian.MainCommand11316666218871478507", "--dry-run"))
    }
}