package reports

interface ErrorReporter {
    fun report(line: Int, column: Int, message: String)
    fun hadError(): Boolean
    fun resetError()
}