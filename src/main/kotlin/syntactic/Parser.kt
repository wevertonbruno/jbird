package syntactic

import lexical.Scanner
import lexical.Token
import lexical.TokenLiteral
import lexical.TokenType
import reports.ErrorReporter
import syntactic.ast.Binary
import syntactic.ast.Expr
import syntactic.ast.Grouping
import syntactic.ast.Literal
import syntactic.ast.Unary
import java.lang.Exception

private const val EXPECTED_R_PAREN = "Expect ')' after expression."
private const val EXPECTED_TOKEN = "Expect token '%s'"
private const val UNEXPECTED_PRIMARY_TOKEN = "Unexpected token %s. Expected a literal or grouping expression."

class ParserException(val token: Token, override val message: String): RuntimeException(message)

class Parser(private val scanner: Scanner, private val errorReporter: ErrorReporter) {
    private var currentCursor = 0

    fun parse() = try {
        scanner.scanTokens()
        expression()
    } catch (ex: ParserException) {
        errorReporter.report(ex.token, ex.message)
        null
    }

    private fun expression(): Expr = equality()

    private fun equality(): Expr = comparison()
        .run {
            while (matchToken(TokenType.EXCLAMATION_EQUAL, TokenType.EQUAL_EQUAL)) {
                val operator = getPreviousToken()
                val right = comparison()
                return@run Binary(this, operator, right)
            }
            return@run this
        }

    private fun comparison(): Expr = term()
        .run {
            while (matchToken(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
                val operator = getPreviousToken()
                val right = term()
                return@run Binary(this, operator, right)
            }
            return@run this
        }

    private fun term(): Expr = factor()
        .run {
            while (matchToken(TokenType.PLUS, TokenType.MINUS)) {
                val operator = getPreviousToken()
                val right = factor()
                return@run Binary(this, operator, right)
            }
            return@run this
        }

    private fun factor(): Expr = unary()
        .run {
            while (matchToken(TokenType.SLASH, TokenType.STAR)) {
                val operator = getPreviousToken()
                val right = unary()
                return@run Binary(this, operator, right)
            }
            return@run this
        }

    private fun unary(): Expr =
        if (matchToken(TokenType.EXCLAMATION, TokenType.MINUS)) {
            val operator = getPreviousToken()
            val right = unary()
            Unary(operator, right)
        } else {
            primary()
        }

    private fun primary(): Expr {
        if(matchToken(TokenType.FALSE)) return Literal(false)
        if(matchToken(TokenType.TRUE)) return Literal(true)
        if(matchToken(TokenType.NIL)) return Literal(null)

        if (matchToken(TokenType.NUMBER, TokenType.STRING)) {
            return Literal((getPreviousToken() as TokenLiteral).literal)
        }

        if(matchToken(TokenType.LEFT_PAREN)) {
            val expression = expression()
            consumeToken(TokenType.RIGHT_PAREN, EXPECTED_R_PAREN)
            return Grouping(expression)
        }

        throw ParserException(peek(), UNEXPECTED_PRIMARY_TOKEN.format(peek()))
    }

    private fun consumeToken(type: TokenType, message: String? = null): Token {
        if(checkToken(type)) return advanceCursor()
        throw ParserException(peek(), message ?: EXPECTED_TOKEN.format(peek().lexeme))
    }

    private fun matchToken(vararg tokenTypes: TokenType): Boolean {
        tokenTypes.forEach {
            if(checkToken(it)) {
                advanceCursor()
                return true
            }
        }
        return false
    }

    private fun checkToken(type: TokenType): Boolean {
        if (isEOF()) return false
        return peek().type == type
    }

    private fun advanceCursor(): Token {
        if(!isEOF()) currentCursor++
        return getPreviousToken()
    }

    private fun getPreviousToken() = scanner.tokens[currentCursor - 1]

    private fun peek(): Token = scanner.tokens[currentCursor]

    private fun isEOF() = peek().type == TokenType.EOF

    private fun sync() {
        advanceCursor()

        while (!isEOF()) {
            if (getPreviousToken().type == TokenType.SEMICOLON) return

            when(peek().type) {
                TokenType.CLASS, TokenType.FUNC, TokenType.VAR, TokenType.FOR,
                TokenType.IF, TokenType.WHILE, TokenType.PRINT, TokenType.RETURN -> return
                else -> advanceCursor()
            }
        }
    }
}