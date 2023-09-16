package syntactic.parser

import execution.Printer
import syntactic.tokenizer.Token
import syntactic.tokenizer.TokenType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import syntactic.parser.*


class AstTest {

    @Test
    fun testItShouldPrintAST() {
        val printer = Printer()
        val expression: Expr = Expr.Binary(
            Expr.Unary(
                Token(TokenType.MINUS, "-", 1, 1),
                Expr.Literal(123)
            ),
            Token(TokenType.STAR, "*", 1, 1),
            Expr.Grouping(
                Expr.Literal(45.67)
            )
        )
        assertEquals("(* (- 123) (group 45.67))", printer.print(expression))
    }
}
