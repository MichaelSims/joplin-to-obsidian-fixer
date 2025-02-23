package sims.michael.joplin2obsidian

import org.commonmark.node.Link
import org.commonmark.node.Text
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class UtilsKtTest {
    private val logger: Logger = LoggerFactory.getLogger(UtilsKtTest::class.java)

    @Test
    fun `toStringWithClickableURI works as expected`() {
        assertEquals(
            "/mnt/temp/name.ext (file:///mnt/temp/name.ext)",
            File("/mnt/temp/name.ext").toStringWithClickableURI()
        )
    }

    fun createLink(name: String, destination: String) = Link(name, null).apply{ appendChild(Text(destination)) }

    @Test
    fun `extract markdown links`() {
        assertEquals(
            listOf(createLink("name", "target")),
            "[name](target)".extractMarkdownLinks(),
        )

        assertEquals(
            listOf(createLink("one", "one"), createLink("two", "two")),
            "This has both [one](one) and [two](two) links.".extractMarkdownLinks()
        )

        assertEquals(
            listOf(
                createLink("facebook.com/arcbotics", "http://facebook.com/arcbotics"),
                createLink("youtube.com/arcbotics", "http://youtube.com/arcbotics"),
                createLink("instagram.com/arcbotics", "http://instagram.com/arcbotics"),
                createLink("twitter.com/arcbotics", "http://twitter.com/arcbotics"),
            ),
            ("|  ![Joseph Sch](../_resources/5360c6092d2a32ee47346b1c71182339) |  **Joseph Schlesinger**  " +
                    "(ArcBotics)<br>Dec 29, 6:29 AM PST<br>Hi Michael,<br>It has to do with the SPI bus being used " +
                    "for both the shift registers as output multiplexors and as serial communication for the LCD. " +
                    "The LCD's clock-select pin should resolve this, but for some reason the drive controllers have " +
                    "never been able to handle all the extra noise.<br>--<br>Follow us for new tutorials, updates " +
                    "and more:<br>Facebook: [facebook.com/arcbotics](http://facebook.com/arcbotics)<br>Youtube: " +
                    "[youtube.com/arcbotics](http://youtube.com/arcbotics)<br>Instagram: [instagram.com/arcbotics]" +
                    "(http://instagram.com/arcbotics)<br>Twitter: [twitter.com/arcbotics]" +
                    "(http://twitter.com/arcbotics) |\n").extractMarkdownLinks()
        )
    }

    @Test
    fun `basic getNewNoteContentAndRenameList test`(@TempDir tempDir: File) {
        tempDir.resolve("_resources").also(File::mkdir)
        val notes = tempDir.resolve("notes").also(File::mkdir)
        val note = notes.resolve("note.md").also { note -> note.writeText("""""") }
        assertEquals(
            NoteProcessingResult(
                newContent = emptyList(),
                originalAttachmentLinks = emptyList(),
                renames = emptyList()
            ),
            Note(note).getNewNoteContentAndRenameList()
        )
    }

    @Test
    fun `Whirlpool Refrigerator Model _GSF26C4EXY03 manual`() {
        val note = getTestNote("Whirlpool Refrigerator Model _GSF26C4EXY03 manual.md")
        val result = Note(note).getNewNoteContentAndRenameList().originalAttachmentLinks
        assertEquals(
            listOf(createLink("L1004475.pdf", "../_resources/L1004475.pdf")),
            result
        )
    }

    @Test
    fun `Weber grill manuals`() {
        val note = getTestNote("Weber grill manuals.md")
        val result = Note(note)
            .getNewNoteContentAndRenameList()

        result
            .originalAttachmentLinks
            .forEach { originalLink -> logger.info("Original link is {}", originalLink) }

        assertEquals(
            listOf(Rename(File("Charcoal_Grill_Owners_Guide_5422"), File("Charcoal_Grill_Owners_Guide_5422.pdf"))),
            result.renames
        )
    }

    @Test
    fun `find notes with blank extensions`() {
        logMatchingNotes { result ->
            result.originalAttachmentLinks.any { link -> File(link.destination).extension.isBlank() }
        }
    }

    private fun logMatchingNotes(predicate: (NoteProcessingResult) -> Boolean) {
        val numResults = TestConfig.workingDirOverrideFile
            .walk()
            .filter { file -> file.extension == "md" }
            .map { file -> Note(file) }
            .map { note -> note to note.getNewNoteContentAndRenameList() }
            .filter { (_, result) -> predicate(result) }
            .onEach { (note, _) ->
                logger.info("Matching note {}", note.file.toStringWithClickableURI())
            }
            .count()
        logger.info("Found {} matching files", numResults)
    }


    private fun getTestNote(name: String): File {
        val note = TestConfig.workingDirOverrideFile.walk().filter { file -> file.name == name }.single()
        logger.info("Note is {}", note.toStringWithClickableURI())
        return note
    }
}
