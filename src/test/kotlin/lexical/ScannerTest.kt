package lexical

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.provider.MethodSource
import reports.DefaultErrorReporter
import reports.ErrorReporter
import java.util.stream.Stream
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ScannerTest {
    private val errorReporter: ErrorReporter = DefaultErrorReporter()

    @Test
    fun itShouldScanTokens() {
        val script = """// this is a comment
            (( )){} // grouping stuff
            !*+-/=<> <= == // operators"""".trimIndent()

        val scanner = Scanner(script, errorReporter)
        scanner.scanTokens()
        assertEquals(17, scanner.tokens.size)
    }

    private fun getTokenLiteralTestSource() = Stream.of(
        Arguments.of("""42""", TokenType.NUMBER, Int::class, 42),
        Arguments.of("""3.14""", TokenType.NUMBER, Double::class, 3.14),
        Arguments.of(""" "test" """, TokenType.STRING, String::class, "test"),
    )

    @ParameterizedTest
    @MethodSource("getTokenLiteralTestSource")
    fun itShouldParseTokenLiteral(script: String, type: TokenType, clazz: KClass<Any>, lexeme: Any) {
        val scanner = Scanner(script, errorReporter)
        scanner.scanTokens()
        val token = scanner.tokens[0]

        assertTrue(token is TokenLiteral)
        assertEquals(type, token.type)
        assertTrue(clazz.isInstance(token.literal))
        assertEquals(lexeme, token.literal)
    }

    private fun getTokenTestSource() = Stream.of(
        Arguments.of("""andyou""", TokenType.IDENTIFIER, "andyou"),
        Arguments.of("""else""", TokenType.ELSE, "else"),
        Arguments.of("""if""", TokenType.IF, "if"),
    )

    @ParameterizedTest
    @MethodSource("getTokenTestSource")
    fun itShouldParseIdentifier(script: String, type: TokenType, lexeme: Any) {
        val scanner = Scanner(script, errorReporter)
        assertDoesNotThrow { scanner.scanTokens() }
        val token = scanner.tokens[0]

        assertEquals(type, token.type)
        assertEquals(lexeme, token.lexeme)
    }
}