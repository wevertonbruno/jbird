package cmd

import execution.KotlinVM
import syntactic.tokenizer.Tokenizer
import reports.ErrorReporter
import syntactic.parser.Parser
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

class JBird(
    private val tokenizerReporter: ErrorReporter,
    private val parserReporter: ErrorReporter,
    private val runtimeReporter: ErrorReporter
) {
    fun executeFromFile(path: String) {
        val bytes = Files.readAllBytes(Paths.get(path))
        execute(String(bytes, Charset.defaultCharset()))
        if (tokenizerReporter.hadError() || parserReporter.hadError()) exitProcess(65)
        if (runtimeReporter.hadError()) exitProcess(70)
    }

    fun executeFromCmd() {
        val inputStreamReader = InputStreamReader(System.`in`)
        val reader = BufferedReader(inputStreamReader)

        while (true) {
            print("> ")
            reader.readLine()
                ?.run(::execute)
                .also { tokenizerReporter.resetError(); parserReporter.resetError() }
                ?: break
        }
    }

    private fun execute(script: String) {
        val tokenizer = Tokenizer(script, tokenizerReporter)
        val parser = Parser(tokenizer, parserReporter)
        val kotlinVM = KotlinVM(runtimeReporter)
        val program = parser.parse()

        if (checkError()) return

        kotlinVM.run(program)
    }

    private fun checkError() = tokenizerReporter.hadError() || parserReporter.hadError()
}
