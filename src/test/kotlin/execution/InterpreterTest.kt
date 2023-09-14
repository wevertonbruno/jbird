package execution

import io.mockk.mockk
import lexical.Scanner
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import syntactic.Parser
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InterpreterTest {
    private fun getTestSource() = Stream.of(
        Arguments.of("""(1 + 3) * 5;""", 20.0),
        Arguments.of("""2^2^3;""", 256.0),
        Arguments.of("""1 - 2 + 3;""", -2.0),
        Arguments.of("""false ? 5 : 3 < 4 ? 2 : 3 ;""", 2),
        Arguments.of("""42 > 42 ? 42 : 43 > 42 ? 0 : 1;""", 0),
        Arguments.of(""" "one" + ("_" + 1); """, "one_1"),
        Arguments.of(""" print 1 + 2 + 3 + 4 - 5 * 3 > 0; """, "one_1"),
        Arguments.of(""" print "Hello, World!"${"\n"} """, "one_1"),
    )

    @ParameterizedTest
    @MethodSource("getTestSource")
    fun itShouldInterpreterSuccessfully(script: String, expected: Any?) {
        val scanner = Scanner(script, mockk())
        val parser = Parser(scanner, mockk())
        val interpreter = Interpreter(mockk())

        assertDoesNotThrow {
            val program = parser.parse()
            interpreter.interpret(program)
        }
    }
}
