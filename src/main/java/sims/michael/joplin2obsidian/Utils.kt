package sims.michael.joplin2obsidian

import java.io.File

/**
 * Replace file:/some/path with file:///some/path. Both are legal formats but IDEA will make the second format
 * clickable when it appears in the test output window.
 */
fun File.toStringWithClickableURI(): String = "$this (${toURI().toString().replaceFirst("/", "///")})"

fun copyAndRewriteNotes(workingDir: File, inputCopy: File, outputDir: File): List<Rename> {
    TODO("Not yet implemented")
}
