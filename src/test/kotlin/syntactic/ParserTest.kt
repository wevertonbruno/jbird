package syntactic

import io.mockk.mockk
import lexical.Scanner
import lexical.TokenType
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import execution.Printer
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ParserTest {
    private fun getExpressionTestSource() = Stream.of(
        Arguments.of("""(1 + 3) * 5""", TokenType.IDENTIFIER, "andyou"),
        Arguments.of("""42 > 42 ? 42 : 43  > 42 ? 0 : 1""", TokenType.IDENTIFIER, "andyou"),
    )

    @ParameterizedTest
    @MethodSource("getExpressionTestSource")
    fun itShouldParseAnExpression(script: String) {
        val scanner = Scanner(script, mockk())
        val parser = Parser(scanner, mockk())
        assertDoesNotThrow {
            val program = parser.parse()
//            program?.statements?.forEach { println(Printer().print(it.)) }
//            println(Printer().print(expr))
        }
    }
}
