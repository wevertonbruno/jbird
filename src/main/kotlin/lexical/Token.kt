package lexical

open class Token(
    open val type: TokenType,
    open val lexeme: String,
    open val line: Int,
    open val column: Int
) {
    override fun toString(): String {
        return "[type: $type] $lexeme"
    }
}