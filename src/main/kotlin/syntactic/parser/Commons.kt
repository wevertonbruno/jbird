package syntactic.parser

import syntactic.tokenizer.Token
import syntactic.tokenizer.TokenType

private const val EXPECTED_TOKEN = "Expect token '%s'"
private const val EXPECTED_SEPARATOR = "Expect separator ';' or Newline"

internal fun Parser.consumeToken(type: TokenType, message: String? = null): Token {
    if (checkToken(type)) return advanceCursor()
    throw ParserException(peek(), message ?: EXPECTED_TOKEN.format(peek().lexeme))
}

internal fun Parser.consumeSeparator(): Token {
    if (checkToken(TokenType.SEMICOLON) || checkToken(TokenType.BREAK_LINE)) return advanceCursor()
    throw ParserException(peek(), EXPECTED_SEPARATOR)
}

internal fun Parser.matchToken(vararg tokenTypes: TokenType): Boolean {
    tokenTypes.forEach {
        if (checkToken(it)) {
            advanceCursor()
            return true
        }
    }
    return false
}

internal fun Parser.checkToken(type: TokenType): Boolean {
    if (isEOF()) return false
    return peek().type == type
}

internal fun Parser.advanceCursor(): Token {
    if (!isEOF()) currentCursor++
    return getPreviousToken()
}

internal fun Parser.getPreviousToken() = getTokens()[currentCursor - 1]

internal fun Parser.peek(): Token = getTokens()[currentCursor]

internal fun Parser.isEOF() = peek().type == TokenType.EOF

internal fun Parser.consumeNL() {
    while (matchToken(TokenType.BREAK_LINE));
}


internal fun Parser.sync() {
    advanceCursor()

    while (!isEOF()) {
        if (getPreviousToken().type == TokenType.SEMICOLON) return

        when (peek().type) {
            TokenType.CLASS, TokenType.FUNC, TokenType.VAR, TokenType.FOR,
            TokenType.IF, TokenType.WHILE, TokenType.PRINT, TokenType.RETURN -> return

            else -> advanceCursor()
        }
    }
}
