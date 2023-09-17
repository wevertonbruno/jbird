package syntactic.parser

import syntactic.tokenizer.Token
import syntactic.tokenizer.TokenType

class StmtParser(private val parser: Parser, private val exprParser: ExprParser) {

    fun parseDeclarationStmt() = run {
        val stmt = when {
            parser.matchToken(TokenType.VAR) -> parseVarDeclaration()
            else -> parseStatement()
        }
        return@run stmt
    }

    private fun parseBlockStmt(): Stmt.Block {
        val statements = mutableListOf<Stmt>()
        while (!parser.checkToken(TokenType.RIGHT_BRACE) && !parser.isEOF()) {
            statements.add(parseDeclarationStmt())
        }
        parser.consumeToken(TokenType.RIGHT_BRACE, "Expect '}' after block.")
        return Stmt.Block(statements)
    }

    private fun parseStatement(): Stmt {
        return when {
            parser.matchToken(TokenType.IF) -> parseIfStmt()
            parser.matchToken(TokenType.PRINT) -> parsePrintStmt()
            parser.matchToken(TokenType.LEFT_BRACE) -> parseBlockStmt()
            parser.matchToken(TokenType.WHILE) -> parseWhileStmt()
            parser.matchToken(TokenType.DO) -> parseDoWhileStmt()
            parser.matchToken(TokenType.FUNC) -> parseFunctionStmt("function")
            else -> parseExpressionStmt()
        }
    }

    private fun parseVarDeclaration() =
        parser.consumeToken(TokenType.IDENTIFIER, "Expect variable name.")
            .let {
                var expr: Expr? = null
                if (parser.matchToken(TokenType.EQUAL)) {
                    expr = exprParser.parse(Precedence.None)
                }
                parser.consumeSeparator()
                Stmt.Var(it, expr)
            }

    private fun parsePrintStmt() =
        exprParser.parse(Precedence.None)
            .let {
                parser.consumeSeparator()
                Stmt.Print(it)
            }

    private fun parseFunctionStmt(kind: String): Stmt {
        val name = parser.consumeToken(TokenType.IDENTIFIER, "Expect $kind name.")
        parser.consumeToken(TokenType.LEFT_PAREN, "Expect '(' after $kind name.")
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
        return Stmt.Function(name, params, parseBlockStmt())
    }

    private fun parseIfStmt(): Stmt {
        parser.consumeToken(TokenType.LEFT_PAREN, "Expect '(' after 'if'.")
        val condition = exprParser.parse(Precedence.None)
        parser.consumeToken(TokenType.RIGHT_PAREN, "Expect ')' after 'if'.")
        val thenBranch = parseStatement()
        var elseBranch: Stmt? = null
        if (parser.matchToken(TokenType.ELSE)) {
            elseBranch = parseStatement()
        }
        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun parseWhileStmt(): Stmt {
        parser.consumeToken(TokenType.LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = exprParser.parse(Precedence.None)
        parser.consumeToken(TokenType.RIGHT_PAREN, "Expect ')' after 'while'.")
        val body = parseStatement()
        return Stmt.While(condition, body)
    }

    private fun parseDoWhileStmt(): Stmt {
        val stmt = parseStatement()
        parser.consumeToken(TokenType.WHILE, "Expecting 'while' followed by a post-condition")
        parser.consumeToken(TokenType.LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = exprParser.parse(Precedence.None)
        parser.consumeToken(TokenType.RIGHT_PAREN, "Expect ')' after 'while'.")
        parser.consumeSeparator()
        return Stmt.DoWhile(condition, stmt)
    }

    private fun parseExpressionStmt() =
        exprParser.parse(Precedence.None)
            .let {
                parser.consumeSeparator()
                Stmt.Expression(it)
            }
}
