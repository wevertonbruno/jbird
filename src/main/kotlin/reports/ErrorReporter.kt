package reports

import lexical.Token

interface ErrorReporter {
    fun report(line: Int, column: Int, message: String, where: String? = null)
    fun report(token: Token, message: String)
    fun hadError(): Boolean
    fun resetError()
}