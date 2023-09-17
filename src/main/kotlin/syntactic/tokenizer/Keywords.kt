package syntactic.tokenizer

class Keywords {
    companion object {
        private val keywords = mapOf<String, TokenType>(
            Pair("and", TokenType.AND),
            Pair("class", TokenType.CLASS),
            Pair("else", TokenType.ELSE),
            Pair("false", TokenType.FALSE),
            Pair("for", TokenType.FOR),
            Pair("func", TokenType.FUNC),
            Pair("if", TokenType.IF),
            Pair("nil", TokenType.NIL),
            Pair("or", TokenType.OR),
            Pair("print", TokenType.PRINT),
            Pair("return", TokenType.RETURN),
            Pair("super", TokenType.SUPER),
            Pair("this", TokenType.THIS),
            Pair("true", TokenType.TRUE),
            Pair("var", TokenType.VAR),
            Pair("while", TokenType.WHILE),
            Pair("do", TokenType.DO),
        )

        private val literals = mapOf<String, TokenType>(
            Pair("false", TokenType.FALSE),
            Pair("nil", TokenType.NIL),
            Pair("true", TokenType.TRUE)
        )

        private val literalValues = mapOf<String, Any?>(
            Pair("false", false),
            Pair("nil", null),
            Pair("true", true)
        )

        fun isReserved(lexeme: String) = keywords.containsKey(lexeme)

        fun isLiteral(lexeme: String) =  literals.containsKey(lexeme)

        fun getReserved(lexeme: String) = keywords[lexeme]!!

        fun getLiteralValue(lexeme: String) = literalValues[lexeme]
    }
}