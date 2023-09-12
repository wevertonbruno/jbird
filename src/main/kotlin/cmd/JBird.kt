package cmd

import execution.Interpreter
import lexical.Scanner
import reports.ErrorReporter
import syntactic.Parser
import execution.Printer
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

class JBird(
    private val scanReporter: ErrorReporter,
    private val parserReporter: ErrorReporter,
    private val runtimeReporter: ErrorReporter
) {
    fun executeFromFile(path: String) {
        val bytes = Files.readAllBytes(Paths.get(path))
        execute(String(bytes, Charset.defaultCharset()))
        if (scanReporter.hadError() || parserReporter.hadError()) exitProcess(65)
        if (runtimeReporter.hadError()) exitProcess(70)
    }

    fun executeFromCmd() {
        val inputStreamReader = InputStreamReader(System.`in`)
        val reader = BufferedReader(inputStreamReader)

        while (true) {
            print("> ")
            reader.readLine()
                ?.run(::execute)
                .also { scanReporter.resetError(); parserReporter.resetError() }
                ?: break
        }
    }

    private fun execute(script: String) {
        val scanner = Scanner(script, scanReporter)
        val parser = Parser(scanner, parserReporter)
        val interpreter = Interpreter(runtimeReporter)
        val program = parser.parse()

        if (checkError()) return

        interpreter.interpret(program)
    }

    private fun checkError() = scanReporter.hadError() || parserReporter.hadError()
}
