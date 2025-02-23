package sims.michael.joplin2obsidian

import java.io.File

data class MarkdownLink(val name: String, val target: File)
data class Rename(val old: File, val new: File)

@JvmInline
value class MimeType(val value: String)
