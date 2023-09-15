package syntactic

import execution.Interpreter
import io.mockk.mockk
import syntactic.tokenizer.Tokenizer
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.api.BeforeEach
import syntactic.parser.*
import java.util.stream.Stream
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ParserTest {
    private lateinit var interpreter: InterpreterVisitor

    @BeforeEach
    fun setUp() {
        interpreter = Interpreter(mockk())
    }

    private fun getExpressionTestSource() = Stream.of(
        Arguments.of("""(1 + 3) * 5""", Expr.Binary::class, fun (expr: Expr) {
            assertEquals(20, expr.accept(interpreter).value)
        }),
        Arguments.of("""42 > 42 ? 42 : 43 > 42 ? 0 : 1""", Expr.Ternary::class, fun (expr: Expr) {
            assertEquals(0, expr.accept(interpreter).value)
        }),
        Arguments.of("""5+4-(2*5+3)""", Expr.Binary::class, fun (expr: Expr) {
            assertEquals(-4, expr.accept(interpreter).value)
        }),
        Arguments.of(""" 10 -5 *2""", Expr.Binary::class, fun (expr: Expr) {
            assertEquals(0, expr.accept(interpreter).value)
        }),
        Arguments.of("""5+4-(2*5+3) > 10 -5 *2""", Expr.Binary::class, fun (expr: Expr) {
            assertEquals(false, expr.accept(interpreter).value)
        }),
        Arguments.of("""2+((3-1)*2)""", Expr.Binary::class, fun (expr: Expr) {
            assertEquals(6, expr.accept(interpreter).value)
        }),
        Arguments.of("""2+((3.5-1)*2)""", Expr.Binary::class, fun (expr: Expr) {
            assertEquals(7.0, expr.accept(interpreter).value)
        }),
        Arguments.of("""3 > (5*2 > 9 ? 1 : 0)""", Expr.Binary::class, fun (expr: Expr) {
            assertEquals(true, expr.accept(interpreter).value)
        }),
    )

    private fun getStatementTestSource() = Stream.of(
        Arguments.of("""var test = 0;""", Stmt.Var::class, fun (stmt: Stmt.Var) {
            assertEquals("test", stmt.identifier.lexeme)
            assertTrue(stmt.expr is Expr.Literal)
        }),

        Arguments.of("""var test = 5 * 5 + 2;""", Stmt.Var::class, fun (stmt: Stmt.Var) {
            assertEquals("test", stmt.identifier.lexeme)
            assertTrue(stmt.expr is Expr.Binary)
        }),
    )

    @ParameterizedTest
    @MethodSource("getExpressionTestSource")
    fun <T: Expr> itShouldParseAnExpression(script: String, type: KClass<T>, accept: (expr: Expr) -> Unit) {
        val exprParser = Tokenizer(script, mockk())
            .also (Tokenizer::scanTokens)
            .let { Parser(it, mockk()) }
            .let (::ExprParser)

        val expr = assertNotNull(exprParser.parse(Precedence.None))
        assertEquals(type, expr::class)
        accept(expr)
    }

    @ParameterizedTest
    @MethodSource("getStatementTestSource")
    fun <T: Stmt> itShouldParseAnStmt(script: String, type: KClass<T>, accept: (stmt: Stmt) -> Unit) {
        val stmtParser = Tokenizer(script, mockk())
            .also (Tokenizer::scanTokens)
            .let {
                val parser = Parser(it, mockk())
                StmtParser(parser, ExprParser(parser))
            }
        val stmt = assertNotNull(stmtParser.parseDeclarationStmt())
        assertEquals(type, stmt::class)
        accept(stmt)
    }
}
