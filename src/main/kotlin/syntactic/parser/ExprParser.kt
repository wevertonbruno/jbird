package syntactic.parser

import syntactic.tokenizer.Token
import syntactic.tokenizer.TokenLiteral
import syntactic.tokenizer.TokenType

private const val UNEXPECTED_TOKEN = "Unexpected token %s."
private const val UNEXPECTED_PRIMARY_TOKEN = "Unexpected token %s. Expected a literal or grouping expression."

enum class Precedence {
    None,
    Assignment,
    Or,
    And,
    Ternary,
    Equality,
    Comparison,
    Term,
    Factor,
    Exponential,
    Unary,
    Call,
    Function,
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
            TokenType.IDENTIFIER,
            TokenType.FUNC -> primary()

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
            TokenType.EQUAL -> assignment(left)
            TokenType.OR,
            TokenType.AND -> logical(left)

            TokenType.LEFT_PAREN -> call(left)

            else -> throw ParserException(parser.peek(), UNEXPECTED_TOKEN.format(parser.peek()))
        }

    private fun assignment(left: Expr): Expr {
        parser.consumeNextToken()
        val right = parse(Precedence.None)
        return when (left) {
            is Expr.Variable -> Expr.Assign(left.identifier, right)
            else -> throw ParserException(parser.peek(), "Invalid assignment target.")
        }
    }

    private fun logical(left: Expr): Expr {
        val operator = parser.consumeNextToken()
        val right = parse(getPrecedence(operator.type))
        return Expr.Logical(left, operator, right)
    }

    private fun call(left: Expr): Expr {
        val paren = parser.consumeNextToken()
        val arguments = mutableListOf<Expr>()
        if (!parser.checkToken(TokenType.RIGHT_PAREN)) {
            do {
                // limit argument count to 255
                if (arguments.size >= 255) {
                    throw ParserException(parser.peek(), "Can not have more than 255 arguments.")
                }
                arguments.add(parse(Precedence.None))
            } while (parser.matchToken(TokenType.COMMA))
        }
        parser.consumeToken(TokenType.RIGHT_PAREN, "Expected ')' after arguments.")
        return Expr.Call(left, paren, arguments)
    }

    private fun binary(left: Expr): Expr {
        val precedence = getPrecedence(parser.peek().type)
        parser.consumeNextToken()
        val operator = parser.getPreviousToken()
        val right = if (operator.type.isRightAssociative)
            parse(precedence.getLesser())
        else
            parse(precedence)
        return Expr.Binary(left, operator, right)
    }

    private fun ternary(left: Expr): Expr {
        parser.consumeNextToken()
        val consequence = parse(Precedence.None)
        parser.consumeNextToken()
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
            TokenType.STRING -> Expr.Literal((parser.consumeNextToken() as TokenLiteral).literal)
            TokenType.IDENTIFIER -> Expr.Variable(parser.consumeNextToken())
            TokenType.FUNC -> { parser.consumeNextToken(); functionExpr("function") }

            else -> throw ParserException(parser.peek(), UNEXPECTED_PRIMARY_TOKEN.format(parser.peek()))
        }

    internal fun functionExpr(kind: String): Expr.Function {
        parser.consumeToken(TokenType.LEFT_PAREN, "Expect '(' after $kind.")
        val params = mutableListOf<Token>()
        if (!parser.checkToken(TokenType.RIGHT_PAREN)) {
            params.add(parser.consumeToken(TokenType.IDENTIFIER, "Expect parameter name."))
            while (parser.matchToken(TokenType.COMMA)) {
                if (params.size >= 255) {
                    throw ParserException(parser.peek(), "Cannot have more than 255 parameters.")
                }
                params.add(parser.consumeToken(TokenType.IDENTIFIER, "Expect parameter name."))
            }
        }
        parser.consumeToken(TokenType.RIGHT_PAREN, "Expect ')' after parameters.")
        parser.consumeToken(TokenType.LEFT_BRACE, "Expect '{' before $kind body.")
        return Expr.Function(params, parser.parseBlockStmt().statements)
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
            TokenType.LEFT_PAREN -> Precedence.Call
            TokenType.EQUAL -> Precedence.Assignment
            TokenType.OR -> Precedence.Or
            TokenType.AND -> Precedence.And
            TokenType.FUNC -> Precedence.Function
            else -> Precedence.None
        }
    }
}
