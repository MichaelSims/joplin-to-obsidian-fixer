package sims.michael.joplin2obsidian

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import sims.michael.joplin2obsidian.Utils.logger
import java.io.File
import java.net.URLDecoder

/**
 * Replace file:/some/path with file:///some/path. Both are legal formats but IDEA will make the second format
 * clickable when it appears in the test output window.
 */
fun File.toStringWithClickableURI(): String = "$this (${toURI().toString().replaceFirst("/", "///")})"

fun copyAndRewriteNotes(workingDir: File, input: File, outputDir: File): List<Rename> {

    val notes = input.walk().filter { file -> file.extension == "md" }.map(::Note)

    val noteProcessingResults = notes
        .onEach { note -> logger.debug("Processing {}", note) }
        .map(Note::getNewNoteContentAndRenameList)
        .toList()

    // TODO: copy notes to output while rewriting

    return noteProcessingResults.flatMap(NoteProcessingResult::renames)
}

fun Note.getNewNoteContentAndRenameList(): NoteProcessingResult {
    val note = file
    data class LineProcessingResult(val newContent: String, val renames: List<Rename>)
    val lineResults = note
        .readLines()
        .mapIndexed { index, line ->
            val lineNum = index + 1
            val originalLinks = line
                .extractMarkdownLinks()
                .filter { link -> link.target.startsWith("../_resources/") }
            val attachments = originalLinks
                .map { link -> URLDecoder.decode(link.target, Charsets.UTF_8) }
                .map { urlDecodedTarget -> note.parentFile.resolve(urlDecodedTarget) }
                .onEach { attachment ->
                    check(attachment.exists()) {
                        "Can't find $attachment referenced by ${note.toStringWithClickableURI()}:$lineNum"
                    }
                }
            LineProcessingResult(line, emptyList())
        }

    return NoteProcessingResult(
        lineResults.map(LineProcessingResult::newContent),
        lineResults.flatMap(LineProcessingResult::renames)
    )
}

fun String.extractMarkdownLinks() = attachmentLinkRegex
    .findAll(this)
    .map { result ->
        val (name, target) = result.destructured
        MarkdownLink(name, target)
    }
    .toList()

private val attachmentLinkRegex = "\\[(.+?)]\\((.+?)\\)".toRegex()

private object Utils {
    val logger: Logger = LoggerFactory.getLogger(Utils::class.java)
}
