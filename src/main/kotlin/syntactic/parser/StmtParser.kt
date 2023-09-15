package syntactic.parser

import syntactic.tokenizer.TokenType

class StmtParser(private val parser: Parser, private val exprParser: ExprParser) {

    fun parseDeclarationStmt() =
        run { parser.consumeNL() }
            .run {
                when {
                    parser.matchToken(TokenType.VAR) -> parseVarDeclaration()
                    parser.matchToken(TokenType.LEFT_BRACE) -> parseBlockStmt()
                    else -> parseStatement()
                }
            }
            .also { parser.consumeNL() }

    private fun parseBlockStmt(): Stmt.Block {
        val statements = mutableListOf<Stmt>()
        while (!parser.checkToken(TokenType.RIGHT_BRACE) && !parser.isEOF()) {
            statements.add(parseDeclarationStmt())
        }
        parser.consumeToken(TokenType.RIGHT_BRACE, "Expect '}' after block.")
        return Stmt.Block(statements)
    }

    private fun parseStatement() = when {
        parser.matchToken(TokenType.PRINT) -> parsePrintStmt()
        else -> parseExpressionStmt()
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

    private fun parseExpressionStmt() =
        exprParser.parse(Precedence.None)
            .let {
                parser.consumeSeparator()
                Stmt.Expression(it)
            }
}
