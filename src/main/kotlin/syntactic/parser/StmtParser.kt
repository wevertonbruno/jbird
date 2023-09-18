package syntactic.parser

import syntactic.tokenizer.TokenType

class StmtParser(private val parser: Parser) {

    fun parseDeclarationStmt() = run {
        val stmt = when {
            parser.matchToken(TokenType.VAR) -> parseVarDeclaration()
            else -> parseStatement()
        }
        return@run stmt
    }

    internal fun parseBlockStmt(): Stmt.Block {
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
            parser.checkToken(TokenType.FUNC) && parser.checkNextToken(TokenType.IDENTIFIER) -> {
                parser.consumeNextToken() // Consume 'func'
                parseFunctionStmt("function")
            }

            parser.matchToken(TokenType.RETURN) -> parseReturnStmt()
            else -> parseExpressionStmt()
        }
    }

    private fun parseReturnStmt(): Stmt.Return {
        val keyword = parser.getPreviousToken()
        var expr: Expr? = null
        if (!parser.checkSeparator()) {
            expr = parser.parseExpr()
        }
        parser.consumeSeparator()
        return Stmt.Return(keyword, expr)
    }

    private fun parseVarDeclaration() =
        parser.consumeToken(TokenType.IDENTIFIER, "Expect variable name.")
            .let {
                var expr: Expr? = null
                if (parser.matchToken(TokenType.EQUAL)) {
                    expr = parser.parseExpr()
                }
                parser.consumeSeparator()
                Stmt.Var(it, expr)
            }

    private fun parsePrintStmt() =
        parser.parseExpr()
            .let {
                parser.consumeSeparator()
                Stmt.Print(it)
            }

    private fun parseFunctionStmt(kind: String): Stmt.Function {
        val name = parser.consumeToken(TokenType.IDENTIFIER, "Expect $kind name.")
        return Stmt.Function(name, parser.parseFunctionExpr(kind))
    }

    private fun parseIfStmt(): Stmt {
        parser.consumeToken(TokenType.LEFT_PAREN, "Expect '(' after 'if'.")
        val condition = parser.parseExpr()
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
        val condition = parser.parseExpr()
        parser.consumeToken(TokenType.RIGHT_PAREN, "Expect ')' after 'while'.")
        val body = parseStatement()
        return Stmt.While(condition, body)
    }

    private fun parseDoWhileStmt(): Stmt {
        val stmt = parseStatement()
        parser.consumeToken(TokenType.WHILE, "Expecting 'while' followed by a post-condition")
        parser.consumeToken(TokenType.LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = parser.parseExpr()
        parser.consumeToken(TokenType.RIGHT_PAREN, "Expect ')' after 'while'.")
        parser.consumeSeparator()
        return Stmt.DoWhile(condition, stmt)
    }

    private fun parseExpressionStmt() =
        parser.parseExpr()
            .let {
                parser.consumeSeparator()
                Stmt.Expression(it)
            }
}
