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

    @Test
    fun `extract markdown links`() {
        assertEquals(
            listOf(MarkdownLink("name", "target")),
            "[name](target)".extractMarkdownLinks(),
        )

        assertEquals(
            listOf(MarkdownLink("one", "one"), MarkdownLink("two", "two")),
            "This has both [one](one) and [two](two) links.".extractMarkdownLinks()
        )

        assertEquals(
            listOf(
                MarkdownLink("Joseph Sch", "../_resources/5360c6092d2a32ee47346b1c71182339"),
                MarkdownLink("facebook.com/arcbotics", "http://facebook.com/arcbotics"),
                MarkdownLink("youtube.com/arcbotics", "http://youtube.com/arcbotics"),
                MarkdownLink("instagram.com/arcbotics", "http://instagram.com/arcbotics"),
                MarkdownLink("twitter.com/arcbotics", "http://twitter.com/arcbotics"),
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
}
