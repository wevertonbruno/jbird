package syntactic.parser

import syntactic.tokenizer.TokenLiteral
import syntactic.tokenizer.TokenType

private const val UNEXPECTED_TOKEN = "Unexpected token %s."
private const val UNEXPECTED_PRIMARY_TOKEN = "Unexpected token %s. Expected a literal or grouping expression."

enum class Precedence {
    None,
    Assignment,
    Ternary,
    Equality,
    Comparison,
    Term,
    Factor,
    Exponential,
    Unary,
    Primary;

    fun getLesser() = if (this == None) None else entries[ordinal - 1]
}

class ExprParser(private val parser: Parser) {

    fun parse(precedence: Precedence): Expr {
        var expr = parsePrefix()
        while (!parser.isEOF()) {
            val nextPrecedence = getPrecedence(parser.peek().type)
            if (nextPrecedence <= precedence) {
                break
            }
            expr = parseInfix(expr)
        }
        return expr
    }

    private fun parsePrefix(): Expr =
        when (parser.peek().type) {
            TokenType.NUMBER,
            TokenType.STRING,
            TokenType.NIL,
            TokenType.TRUE,
            TokenType.FALSE,
            TokenType.IDENTIFIER -> primary()

            TokenType.EXCLAMATION,
            TokenType.MINUS -> unary()

            TokenType.LEFT_PAREN -> grouping()
            else -> throw ParserException(parser.peek(), UNEXPECTED_TOKEN.format(parser.peek()))
        }

    private fun parseInfix(left: Expr): Expr =
        when (parser.peek().type) {
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
            TokenType.EQUAL -> assigment(left)

            else -> throw ParserException(parser.peek(), UNEXPECTED_TOKEN.format(parser.peek()))
        }

    private fun assigment(left: Expr): Expr {
        parser.advanceCursor()
        val right = parse(Precedence.None)
        return when(left) {
            is Expr.Variable -> Expr.Assign(left.identifier, right)
            else -> throw ParserException(parser.peek(), "Invalid assignment target.")
        }
    }

    private fun binary(left: Expr): Expr {
        val precedence = getPrecedence(parser.peek().type)
        parser.advanceCursor()
        val operator = parser.getPreviousToken()
        val right = if (operator.type.isRightAssociative)
            parse(precedence.getLesser())
        else
            parse(precedence)
        return Expr.Binary(left, operator, right)
    }

    private fun ternary(left: Expr): Expr {
        parser.advanceCursor()
        val consequence = parse(Precedence.None)
        parser.advanceCursor()
        val alternative = parse(Precedence.None)
        return Expr.Ternary(left, consequence, alternative)
    }

    private fun unary(): Expr {
        val operator = parser.getPreviousToken()
        val right = parse(Precedence.Unary)
        return Expr.Unary(operator, right)
    }

    private fun primary(): Expr =
        when (parser.peek().type) {
            TokenType.TRUE,
            TokenType.FALSE,
            TokenType.NIL,
            TokenType.NUMBER,
            TokenType.STRING -> {
                parser.advanceCursor()
                Expr.Literal((parser.getPreviousToken() as TokenLiteral).literal)
            }

            TokenType.IDENTIFIER -> {
                parser.advanceCursor()
                Expr.Variable(parser.getPreviousToken())
            }

            else -> throw ParserException(parser.peek(), UNEXPECTED_PRIMARY_TOKEN.format(parser.peek()))
        }

    private fun grouping(): Expr {
        parser.consumeToken(TokenType.LEFT_PAREN, "Expected '(' before expression.")
        val expr = parse(Precedence.None)
        parser.consumeToken(TokenType.RIGHT_PAREN, "Expected ')' after expression.")
        return Expr.Grouping(expr)
    }

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
            TokenType.EQUAL -> Precedence.Assignment
            else -> Precedence.None
        }
    }
}
