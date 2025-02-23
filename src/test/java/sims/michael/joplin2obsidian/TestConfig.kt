package sims.michael.joplin2obsidian

import java.io.File
import java.util.Properties

object TestConfig {
    private val props = Properties()
        .apply { load(TestConfig::class.java.getResourceAsStream("/test-config.properties")) }
    val inputPath: String by props
    val workingDirOverride: String by props
    val workingDirOverrideFile: File
        get() = File(workingDirOverride)
}
