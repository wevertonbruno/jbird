package syntactic.ast

import lexical.Token
import lexical.TokenType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class AstTest {

    @Test
    fun testItShouldPrintAST() {
        val printer = AstPrinter()
        val expression: Expr = Binary(
            Unary(
                Token(TokenType.MINUS, "-", 1, 1),
                Literal(123)
            ),
            Token(TokenType.STAR, "*", 1, 1),
            Grouping(
                Literal(45.67)
            )
        )
        assertEquals("(* (- 123) (group 45.67))", printer.print(expression))
    }
}