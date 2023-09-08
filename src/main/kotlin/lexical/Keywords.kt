package lexical

class Keywords {
    companion object {
        private val keywords = mapOf<String, TokenType>(
            Pair("and", TokenType.AND),
            Pair("class", TokenType.CLASS),
            Pair("else", TokenType.ELSE),
            Pair("false", TokenType.FALSE),
            Pair("for", TokenType.FOR),
            Pair("fun", TokenType.FUNC),
            Pair("if", TokenType.IF),
            Pair("nil", TokenType.NIL),
            Pair("or", TokenType.OR),
            Pair("print", TokenType.PRINT),
            Pair("return", TokenType.RETURN),
            Pair("super", TokenType.SUPER),
            Pair("this", TokenType.THIS),
            Pair("true", TokenType.TRUE),
            Pair("var", TokenType.VAR),
            Pair("while", TokenType.WHILE)
        )

        fun isReserved(lexeme: String) = keywords.containsKey(lexeme)

        fun getReserved(lexeme: String) = keywords[lexeme]!!
    }
}