package syntactic.tokenizer

data class TokenLiteral(
    override val type: TokenType,
    override val lexeme: String,
    override val line: Int,
    override val column: Int,
    val literal: Any?
) : Token(type, lexeme, line, column) {
    override fun toString(): String {
        return "[type: $type] $lexeme($literal)"
    }

    companion object {
        fun of(token: Token, literal: Any) =
            TokenLiteral(token.type, token.lexeme, token.line, token.column, literal)
    }
}