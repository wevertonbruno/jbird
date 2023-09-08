package reports

class DefaultErrorReporter : ErrorReporter {
    private var hadError: Boolean = false

    override fun report(line: Int, column: Int, message: String) {
        System.err.println("[$line:$column] Error: $message")
        hadError = true
    }

    override fun hadError(): Boolean = hadError

    override fun resetError() {
        hadError = false
    }
}