package syntactic.parser

import syntactic.tokenizer.Tokenizer
import syntactic.tokenizer.Token
import syntactic.tokenizer.TokenLiteral
import syntactic.tokenizer.TokenType
import reports.ErrorReporter

private const val UNEXPECTED_TOKEN = "Unexpected token %s."
private const val UNEXPECTED_PRIMARY_TOKEN = "Unexpected token %s. Expected a literal or grouping expression."
private const val EXPECTED_TOKEN = "Expect token '%s'"
private const val EXPECTED_SEPARATOR = "Expect separator ';' or Newline"

class ParserException(val token: Token, override val message: String) : RuntimeException(message)

enum class Precedence {
    None,
    Var,
    Ternary,
    Equality,
    Comparison,
    Term,
    Factor,
    Exponential,
    Unary,
    Primary;

    fun getLesser() = if(this == None) None else entries[ordinal - 1]
}

class Parser(private val tokenizer: Tokenizer, private val errorReporter: ErrorReporter) {
    private var currentCursor = 0


    private fun getPrecedence(type: TokenType): Precedence {
        return when (type) {
            TokenType.PLUS -> Precedence.Term
            TokenType.MINUS -> Precedence.Term
            TokenType.STAR -> Precedence.Factor
            TokenType.SLASH -> Precedence.Factor
            TokenType.POW -> Precedence.Exponential
            TokenType.EQUAL_EQUAL -> Precedence.Equality
            TokenType.NOT_EQUAL -> Precedence.Equality
            TokenType.GREATER -> Precedence.Comparison
            TokenType.GREATER_EQUAL -> Precedence.Comparison
            TokenType.LESS -> Precedence.Comparison
            TokenType.LESS_EQUAL -> Precedence.Comparison
            TokenType.EXCLAMATION -> Precedence.Unary
            TokenType.QUESTION -> Precedence.Ternary
            TokenType.LEFT_PAREN -> Precedence.Primary
            TokenType.VAR -> Precedence.Var
            else -> Precedence.None
        }
    }

    fun parse() = try {
        val program = Program.newInstance()
        tokenizer.scanTokens()
        while (!isEOF()) {
            program.addStatement(parseStatement())
        }
        program
    } catch (ex: ParserException) {
        errorReporter.report(ex.token, ex.message)
        null
    }

    fun parseStatement() = when {
        matchToken(TokenType.VAR) -> parseVarDeclaration()
        matchToken(TokenType.PRINT) -> parsePrintStmt()
        else -> parseExpressionStmt()
    }

    private fun parseVarDeclaration() =
        consumeToken(TokenType.IDENTIFIER, "Expect variable name.")
            .let {
                var expr: Expr? = null
                if (matchToken(TokenType.EQUAL)) {
                    expr = parseExpr(Precedence.None)
                }
                consumeSeparator()
                Stmt.Var(it, expr)
            }

    private fun parsePrintStmt() =
        parseExpr(Precedence.None)
            .let {
                consumeSeparator()
                Stmt.Print(it)
            }

    private fun parseExpressionStmt() =
        parseExpr(Precedence.None)
            .let {
                consumeSeparator()
                Stmt.Expression(it)
            }

    fun parseExpr(precedence: Precedence): Expr {
        var expr = parsePrefix()
        while (!isEOF()) {
            val nextPrecedence = getPrecedence(peek().type)
            if (nextPrecedence <= precedence) {
                break
            }
            expr = parse_infix(expr)
        }
        return expr
    }

    private fun parsePrefix(): Expr =
        when (peek().type) {
            TokenType.NUMBER,
            TokenType.STRING,
            TokenType.NIL,
            TokenType.TRUE,
            TokenType.FALSE,
            TokenType.IDENTIFIER -> primary()

            TokenType.EXCLAMATION,
            TokenType.MINUS -> unary()

            TokenType.LEFT_PAREN -> grouping()
            else -> throw ParserException(peek(), UNEXPECTED_TOKEN.format(peek()))
        }

    private fun parse_infix(left: Expr): Expr =
        when (peek().type) {
            TokenType.NOT_EQUAL,
            TokenType.EQUAL_EQUAL,
            TokenType.LESS,
            TokenType.LESS_EQUAL,
            TokenType.GREATER,
            TokenType.GREATER_EQUAL,
            TokenType.PLUS,
            TokenType.MINUS,
            TokenType.SLASH,
            TokenType.STAR,
            TokenType.POW -> binary(left)

            TokenType.QUESTION -> ternary(left)

            else -> throw ParserException(peek(), UNEXPECTED_TOKEN.format(peek()))
        }

    private fun binary(left: Expr): Expr {
        val precedence = getPrecedence(peek().type)
        advanceCursor()
        val operator = getPreviousToken()
        val right = if (operator.type.isRightAssociative)
            parseExpr(precedence.getLesser())
        else
            parseExpr(precedence)
        return Expr.Binary(left, operator, right)
    }

    private fun ternary(left: Expr): Expr {
        advanceCursor()
        val consequence = parseExpr(Precedence.None)
        advanceCursor()
        val alternative = parseExpr(Precedence.None)
        return Expr.Ternary(left, consequence, alternative)
    }

    private fun unary(): Expr {
        val operator = getPreviousToken()
        val right = parseExpr(Precedence.Unary)
        return Expr.Unary(operator, right)
    }

    private fun primary(): Expr =
        when(peek().type) {
            TokenType.TRUE,
            TokenType.FALSE,
            TokenType.NIL,
            TokenType.NUMBER,
            TokenType.STRING -> {
                advanceCursor()
                Expr.Literal((getPreviousToken() as TokenLiteral).literal)
            }
            TokenType.IDENTIFIER -> {
                advanceCursor()
                Expr.Variable(getPreviousToken())
            }
            else -> throw ParserException(peek(), UNEXPECTED_PRIMARY_TOKEN.format(peek()))
        }

    private fun grouping(): Expr {
        consumeToken(TokenType.LEFT_PAREN, "Expected '(' before expression.")
        val expr = parseExpr(Precedence.None)
        consumeToken(TokenType.RIGHT_PAREN, "Expected ')' after expression.")
        return Expr.Grouping(expr)
    }

    private fun consumeToken(type: TokenType, message: String? = null): Token {
        if (checkToken(type)) return advanceCursor()
        throw ParserException(peek(), message ?: EXPECTED_TOKEN.format(peek().lexeme))
    }

    private fun consumeSeparator(): Token {
        if (checkToken(TokenType.SEMICOLON) || checkToken(TokenType.BREAK_LINE)) return advanceCursor()
        throw ParserException(peek(), EXPECTED_SEPARATOR)
    }

    private fun matchToken(vararg tokenTypes: TokenType): Boolean {
        tokenTypes.forEach {
            if (checkToken(it)) {
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
        if (!isEOF()) currentCursor++
        return getPreviousToken()
    }

    private fun getPreviousToken() = tokenizer.tokens[currentCursor - 1]

    private fun peek(): Token = tokenizer.tokens[currentCursor]

    private fun isEOF() = peek().type == TokenType.EOF

    private fun sync() {
        advanceCursor()

        while (!isEOF()) {
            if (getPreviousToken().type == TokenType.SEMICOLON) return

            when (peek().type) {
                TokenType.CLASS, TokenType.FUNC, TokenType.VAR, TokenType.FOR,
                TokenType.IF, TokenType.WHILE, TokenType.PRINT, TokenType.RETURN -> return

                else -> advanceCursor()
            }
        }
    }

}