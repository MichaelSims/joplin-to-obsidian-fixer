package sims.michael.joplin2obsidian

import java.io.File

data class MarkdownLink(val name: String, val target: String)
data class Rename(val old: File, val new: File)

data class LineProcessingResult(
    val newContent: String,
    val renames: List<Rename>,
    val originalAttachmentLinks: List<MarkdownLink>
)

data class NoteProcessingResult(
    val newContent: List<String>,
    val renames: List<Rename>,
    val originalAttachmentLinks: List<MarkdownLink>
)

@JvmInline
value class MimeType(val value: String)

@JvmInline
value class Note(val file: File)
