import cmd.JBird
import reports.DefaultErrorReporter
import reports.ErrorReporter
import reports.RuntimeReporter
import kotlin.system.exitProcess

/**
 * Executing Interpreter
 */
fun main(args: Array<String>) {
    val errorReporter: ErrorReporter = DefaultErrorReporter()
    val runtimeReporter: ErrorReporter = RuntimeReporter()
    val jBird = JBird(errorReporter, errorReporter, runtimeReporter)

    println("########################")
    println("##       BIRD         ##")
    println("########################")

    if (args.size > 1) {
        println("To run put the path to the script or nothing to iterative mode")
        exitProcess(64)
    }
    if (args.size == 1) {
        jBird.executeFromFile(args[0])
    } else {
        jBird.executeFromCmd()
    }
}
