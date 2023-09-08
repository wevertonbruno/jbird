package interpreter

import lexical.Scanner
import reports.ErrorReporter
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

class JBird(
    private val scanReporter: ErrorReporter,
    private val parserReporter: ErrorReporter
) {
    fun executeFromFile(path: String) {
        val bytes = Files.readAllBytes(Paths.get(path))
        execute(String(bytes, Charset.defaultCharset()))
        if(scanReporter.hadError()) exitProcess(65)
    }

    fun executeFromCmd() {
        val inputStreamReader = InputStreamReader(System.`in`)
        val reader = BufferedReader(inputStreamReader)

        while (true) {
            print("> ")
            reader.readLine()
                ?.run(::execute)
                .also { scanReporter.resetError() }
                ?: break
        }
    }

    private fun execute(script: String) {
        // Implementing Classes
        Scanner(script, scanReporter).apply {
            scanTokens().forEach {
                println(it)
            }
        }
    }
}