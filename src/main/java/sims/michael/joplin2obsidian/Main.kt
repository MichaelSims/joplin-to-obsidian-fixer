package sims.michael.joplin2obsidian

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.validate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.file
import org.slf4j.LoggerFactory
import java.nio.file.Files

class MainCommand : CliktCommand() {
    private val logger = LoggerFactory.getLogger(MainCommand::class.java)
    private val inputPath by argument().file(mustExist = true).validate { path -> path.isDirectory }
    private val workingDirOverride by option().file(mustExist = true).validate { path -> path.isDirectory }

    override fun run() {
        logger.info("Using input path {}", inputPath)
        require(inputPath.resolve("_resources").isDirectory) {
            "Input path $inputPath doesn't look like a Joplin Markdown export"
        }

        val workingDir = workingDirOverride ?: Files.createTempDirectory(MainCommand::class.qualifiedName).toFile()
        logger.info("Using {} as working directory", workingDir)

        val inputCopy = workingDir.resolve("input")
        if (!inputCopy.exists()) {
            logger.info("Copying input into working directory (this might take a while...)")
            inputPath.copyRecursively(inputCopy)
            logger.info("Done copying input")
        }

        val outputDir = Files.createTempDirectory(workingDir.toPath(), "output").toFile()
        logger.info("Output will be written to {}", outputDir.toStringWithClickableURI())
    }
}

fun main(args: Array<String>) {
    MainCommand().main(args)
}
