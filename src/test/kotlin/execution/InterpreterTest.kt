package execution

import io.mockk.mockk
import lexical.Scanner
import lexical.TokenType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import syntactic.Parser
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InterpreterTest {
    private fun getTestSource() = Stream.of(
        Arguments.of("""(1 + 3) * 5""", 20.0),
        Arguments.of("""42 > 42 ? 42 : 43 > 42 ? 0 : 1""", 0),
        Arguments.of(""" "one" + ("_" + 1) """, "one_1"),
    )

    @ParameterizedTest
    @MethodSource("getTestSource")
    fun itShouldInterpreterSuccessfully(script: String, expected: Any?) {
        val scanner = Scanner(script, mockk())
        val parser = Parser(scanner, mockk())
        val interpreter = Interpreter(mockk())

        assertDoesNotThrow {
            val expr = parser.parse()
            val value = interpreter.interpretAndGetValue(expr)
            assertEquals(expected, value)
        }
    }
}
