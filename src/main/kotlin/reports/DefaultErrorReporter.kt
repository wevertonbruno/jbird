package reports

import syntactic.tokenizer.Token
import syntactic.tokenizer.TokenType

class DefaultErrorReporter : ErrorReporter {
    private var hadError: Boolean = false

    override fun report(line: Int, column: Int, message: String, where: String?) {
        System.err.println("[$line:$column]${where?.let { " $it " } ?: " "}Error: $message")
        hadError = true
    }

    override fun report(token: Token, message: String) {
        if (token.type == TokenType.EOF) {
            report(token.line, token.column, message, "at end" )
        } else {
            report(token.line, token.column, message, "at'")
        }
    }

    override fun hadError(): Boolean = hadError

    override fun resetError() {
        hadError = false
    }
}