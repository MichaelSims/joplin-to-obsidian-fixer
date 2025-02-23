package sims.michael.joplin2obsidian

import org.commonmark.node.AbstractVisitor
import org.commonmark.node.Link
import org.commonmark.node.Node
import org.commonmark.node.Text
import org.commonmark.parser.Parser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import sims.michael.joplin2obsidian.Utils.logger
import java.io.File
import java.net.URLDecoder

/**
 * Replace file:/some/path with file:///some/path. Both are legal formats but IDEA will make the second format
 * clickable when it appears in the test output window.
 */
fun File.toStringWithClickableURI(): String =
    "$this (${toURI().toString().replaceFirst("/", "///").replace("(", "%28").replace(")", "%29")})"

fun copyAndRewriteNotes(workingDir: File, input: File, outputDir: File, dryRun: Boolean): List<Rename> {

    val notes = input.walk().filter { file -> file.extension == "md" }.map(::Note)

    val noteProcessingResults = notes
        .onEach { note -> logger.debug("Processing {}", note.file.toStringWithClickableURI()) }
        .map(Note::getNewNoteContentAndRenameList)
        .toList()

    // TODO: copy notes to output while rewriting`
    if (!dryRun) {

    }

    return noteProcessingResults.flatMap(NoteProcessingResult::renames) // TODO make sure the names are unique!
}

fun Note.getNewNoteContentAndRenameList(): NoteProcessingResult {
    val note = file
    val lineResults = note
        .readLines()
        .mapIndexed { index, line ->
            val lineNum = index + 1
            line.processLine(note, lineNum)
        }

    return NoteProcessingResult(
        lineResults.map(LineProcessingResult::newContent),
        lineResults.flatMap(LineProcessingResult::renames),
        lineResults.flatMap(LineProcessingResult::originalAttachmentLinks),
    )
}

fun String.processLine(note: File, lineNum: Int): LineProcessingResult {
    val originalLinks =
        extractMarkdownLinks()
            .onEach { link -> logger.debug("Extracted link {}", link) }
            .filter { link -> link.destination.startsWith("../_resources/") }
            .map { link ->
                val urlDecodedTarget = URLDecoder.decode(link.destination, Charsets.UTF_8)
                val attachment = note.parentFile.resolve(urlDecodedTarget)
                check(attachment.exists()) {
                    "Can't find $attachment referenced by ${note.toStringWithClickableURI()}:$lineNum"
                }
                link
            }

    // What to do with the original links?
    // If the original target has no extension, rename the file to be the title

    return LineProcessingResult(this, renames = emptyList(), originalAttachmentLinks = originalLinks)
}

fun String.extractMarkdownLinks(): List<Link> = Parser.builder().build().parse(this).collectLinks()

private fun Node.collectLinks(): List<Link> = buildList {
    accept(object : AbstractVisitor() {
        override fun visit(link: Link) {
            add(link)
        }
    })
}

private fun Node.collectTextValues(): List<String> = buildList {
    accept(object : AbstractVisitor() {
        override fun visit(text: Text) {
            val value = text.literal.replace("\u200B", "")
            add(value)
        }
    })
}

private object Utils {
    val logger: Logger = LoggerFactory.getLogger(Utils::class.java)
}
