package syntactic.parser

import syntactic.tokenizer.Tokenizer
import syntactic.tokenizer.Token
import reports.ErrorReporter

class ParserException(val token: Token, override val message: String) : RuntimeException(message)

class Parser(private val tokenizer: Tokenizer, private val errorReporter: ErrorReporter) {

    private val exprParser: ExprParser = ExprParser(this)
    private val stmtParser: StmtParser = StmtParser(this)
    var currentCursor = 0

    fun parse() = try {
        val program = Program.newInstance()
        tokenizer.scanTokens()
        while (!isEOF()) {
            program.addStatement(stmtParser.parseDeclarationStmt())
        }
        program
    } catch (ex: ParserException) {
        errorReporter.report(ex.token, ex.message)
        null
    }

    internal fun parseFunctionExpr(kind: String) = exprParser.functionExpr(kind)

    internal fun parseExpr(): Expr = exprParser.parse(Precedence.None)

    internal fun parseDeclarationStmt(): Stmt = stmtParser.parseDeclarationStmt()

    internal fun parseBlockStmt(): Stmt.Block = stmtParser.parseBlockStmt()

    fun getTokens() = tokenizer.tokens
}
