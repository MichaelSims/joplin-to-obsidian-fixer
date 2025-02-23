package sims.michael.joplin2obsidian

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.validate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.file
import org.slf4j.LoggerFactory
import java.nio.file.Files

class MainCommand : CliktCommand() {
    private val logger = LoggerFactory.getLogger(MainCommand::class.java)
    private val inputPath by argument().file(mustExist = true).validate { path -> path.isDirectory }
    private val workingDirOverride by option().file(mustExist = true).validate { path -> path.isDirectory }
    private val dryRun by option().flag("--disable-dry-run")

    override fun run() {
        logger.info("Using input path {}", inputPath)
        require(inputPath.resolve("_resources").isDirectory) {
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
        // Transform a list of markdown notes (via walking the inputCopy) to a list of Renames that we need to perform
        // in Step 2. As a side effect, copy each note to an outputDir and correct the MD links during the copy
        val renames = copyAndRewriteNotes(workingDir, inputCopy, outputDir, dryRun)

        for (rename in renames) {
            logger.debug("I would rename {} to {}", rename.old, rename.new)
        }

        // Step 2: Copy attachments, performing renames while doing so
    }
}

fun main(args: Array<String>) {
    MainCommand().main(args)
}
