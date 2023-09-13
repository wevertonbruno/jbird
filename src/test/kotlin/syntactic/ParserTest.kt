package syntactic

import execution.Interpreter
import io.mockk.mockk
import lexical.Scanner
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.api.BeforeEach
import java.util.stream.Stream
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ParserTest {
    private lateinit var interpreter: InterpreterVisitor

    @BeforeEach
    fun setUp() {
        interpreter = Interpreter(mockk())
    }

    private fun getExpressionTestSource() = Stream.of(
        Arguments.of("""(1 + 3) * 5""", Binary::class, fun (expr: Expr) {
            assertEquals(20.0, expr.accept(interpreter))
        }),
        Arguments.of("""42 > 42 ? 42 : 43  > 42 ? 0 : 1""", Ternary::class, fun (expr: Expr) {
            assertEquals(0, expr.accept(interpreter))
        }),
        Arguments.of("""5+4-(2*5+3)""", Binary::class, fun (expr: Expr) {
            assertEquals(-4.0, expr.accept(interpreter))
        }),
        Arguments.of(""" 10 -5 *2""", Binary::class, fun (expr: Expr) {
            assertEquals(0.0, expr.accept(interpreter))
        }),
        Arguments.of("""5+4-(2*5+3) > 10 -5 *2""", Binary::class, fun (expr: Expr) {
            assertEquals(false, expr.accept(interpreter))
        }),
        Arguments.of("""3 > (5*2 > 9 ? 1 : 0)""", Binary::class, fun (expr: Expr) {
            assertEquals(true, expr.accept(interpreter))
        }),
    )

    @ParameterizedTest
    @MethodSource("getExpressionTestSource")
    fun <T: Expr> itShouldParseAnExpression(script: String, type: KClass<T>, accept: (expr: Expr) -> Unit) {
        val scanner = Scanner(script, mockk())
        val parser = Parser(scanner, mockk())
        val expr = assertNotNull(parser.parseExpression())
        accept(expr)
    }
}
