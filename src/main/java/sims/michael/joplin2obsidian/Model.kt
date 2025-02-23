package sims.michael.joplin2obsidian

import org.commonmark.node.Link
import java.io.File

data class Rename(val old: File, val new: File)

data class LineProcessingResult(
    val newContent: String,
    val renames: List<Rename>,
    val originalAttachmentLinks: List<Link>
)

data class NoteProcessingResult(
    val newContent: List<String>,
    val renames: List<Rename>,
    val originalAttachmentLinks: List<Link>
)

@JvmInline
value class MimeType(val value: String)

@JvmInline
value class Note(val file: File)
