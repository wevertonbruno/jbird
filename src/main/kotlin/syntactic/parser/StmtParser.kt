package syntactic.parser

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

    private fun parseExpressionStmt() =
        exprParser.parse(Precedence.None)
            .let {
                parser.consumeSeparator()
                Stmt.Expression(it)
            }
}
