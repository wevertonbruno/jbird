import interpreter.JBird
import reports.DefaultErrorReporter
import reports.ErrorReporter
import kotlin.system.exitProcess

/**
 * Executing Interpreter
 */
fun main(args: Array<String>) {
    val errorReporter: ErrorReporter = DefaultErrorReporter()
    val jBird = JBird(errorReporter, errorReporter)

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