package sims.michael.joplin2obsidian

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.validate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.file
import org.commonmark.ext.footnotes.FootnotesExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.node.AbstractVisitor
import org.commonmark.node.Link
import org.commonmark.node.Text
import org.commonmark.parser.Parser
import org.commonmark.renderer.markdown.MarkdownRenderer
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.file.Files

class MainCommand : CliktCommand() {
    private val logger = LoggerFactory.getLogger(MainCommand::class.java)
    private val inputPath by argument().file(mustExist = true).validate { path -> path.isDirectory }
    private val workingDirOverride by option().file(mustExist = true).validate { path -> path.isDirectory }
    private val dryRun by option().flag("--disable-dry-run")

    override fun run() {
        logger.info("Using input path {}", inputPath.toStringWithClickableURI())
        require(inputPath.resolve(RESOURCES_DIR_NAME).isDirectory) {
            "Input path $inputPath doesn't look like a Joplin Markdown export"
        }

        val workingDir = workingDirOverride ?: Files.createTempDirectory(MainCommand::class.qualifiedName).toFile()
        logger.info("Using {} as working directory", workingDir)

        val inputCopy = workingDir.resolve("input")
        if (!inputCopy.exists() && !dryRun) {
            logger.info("Copying input into working directory (this might take a while...)")
            inputPath.copyRecursively(inputCopy)
            logger.info("Done copying input")
        }

        val outputDir = Files.createTempDirectory(workingDir.toPath(), "output").toFile()
        logger.info("Output will be written to {}", outputDir.toStringWithClickableURI())

        // Step 1:
        // Transform a list of markdown notes (via walking the inputCopy) to a list of renames that we need to perform
        // in Step 2. As a side effect, copy each note to an outputDir and correct the MD links during the copy
        val noteFiles = inputCopy.walk().filter { file -> file.extension == "md" }
        val extensions = listOf(
            TablesExtension.create(),
            StrikethroughExtension.create(),
            FootnotesExtension.create(),
        )
        val markdownParser = Parser.builder().extensions(extensions).build()
        val markdownRenderer = MarkdownRenderer.builder().extensions(extensions).build()
        val renames = buildList {
            for (noteFile in noteFiles) {
                val outputNoteFile = outputDir.resolve(noteFile.relativeTo(inputCopy))
                logger.debug("Copying updated note from {} to {}", noteFile, outputNoteFile)

                val noteMarkdown = markdownParser.parse(noteFile.readText())
                noteMarkdown.accept(object : AbstractVisitor() {
                    override fun visit(link: Link) {
                        val linkName = (link.firstChild as? Text)?.literal
                        val linkDestination = link.destination
                        if (!linkDestination.startsWith("../$RESOURCES_DIR_NAME")) {
                            logger.debug("Skipping non-attachment link {}", linkDestination)
                            return
                        }
                        val decodedBaseDestination = URLDecoder.decode(File(linkDestination).name, Charsets.UTF_8)
                        val extension = listOfNotNull(linkName, decodedBaseDestination)
                            .mapNotNull { name -> File(name).extension }
                            .firstOrNull { extension -> !extension.isEmpty() }
                            ?: "pdf".also { logger.warn("Defaulting to PDF for {} belonging to {}!", link, noteFile) }
                        val newDestination = "${File(decodedBaseDestination).nameWithoutExtension}.$extension"
                        if (decodedBaseDestination != newDestination) {
                            link.destination =
                                "../$RESOURCES_DIR_NAME/${URLEncoder.encode(newDestination, Charsets.UTF_8)}"
                            add(decodedBaseDestination to newDestination)
                        }
                    }
                })
                val outputParent = outputNoteFile.parentFile
                check(outputParent.isDirectory || outputParent.mkdirs()) {
                    "Couldn't create output parent $outputParent"
                }
                if (!dryRun) {
                    outputNoteFile.writeText(markdownRenderer.render(noteMarkdown))
                }
            }
        }

        // Step 2: Copy attachments, performing renames while doing so
        val inputResources = inputCopy.resolve(RESOURCES_DIR_NAME)
        val outputResources = outputDir.resolve(RESOURCES_DIR_NAME)

        logger.info("Copying {} to {}", inputResources, outputResources)
        check(inputResources.copyRecursively(outputResources)) {
            "Resources copy failed"
        }

        logger.info("Performing renames...")
        for ((old, new) in renames.distinct()) {
            logger.debug("Renaming {} to {}", old, new)
            val oldFile = outputResources.resolve(old).also { file -> check(file.exists()) { "$file doesn't exist" } }
            val newFile = outputResources.resolve(new)
            check(oldFile.renameTo(newFile)) {
                "Couldn't rename $oldFile to $newFile"
            }
        }
        logger.info("Done")
    }

    companion object {
        private const val RESOURCES_DIR_NAME = "_resources"
    }
}

fun main(args: Array<String>) {
    MainCommand().main(args)
}

/**
 * Replace file:/some/path with file:///some/path. Both are legal formats but IDEA will make the second format
 * clickable when it appears in the test output window.
 */
fun File.toStringWithClickableURI(): String =
    "$this (${toURI().toString().replaceFirst("/", "///").replace("(", "%28").replace(")", "%29")})"