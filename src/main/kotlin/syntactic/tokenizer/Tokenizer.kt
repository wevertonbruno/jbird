package syntactic.tokenizer

import reports.ErrorReporter

private const val BREAK_LINE = '\n'
private const val DOUBLE_QUOTES = '"'
private const val EOL = Char.MIN_VALUE

class Tokenizer(
    private val script: String,
    private val errorReporter: ErrorReporter
) {
    private var startCursor: Int = 0
    private var currentCursor: Int = 0
    private var line: Int = 1
    private var column: Int = 0

    val tokens: MutableList<Token> = mutableListOf()

    fun scanTokens(): List<Token> = tokens.apply {
        while (!isEOF()) {
            startCursor = currentCursor
            scanToken()
        }

        this.add(
            Token(
                type = TokenType.EOF,
                lexeme = "",
                line = line,
                column = 0
            )
        )
    }

    private fun scanToken() = getNextChar().let {
        when (it) {
            '(' -> createToken(TokenType.LEFT_PAREN)
            ')' -> createToken(TokenType.RIGHT_PAREN)
            '{' -> createToken(TokenType.LEFT_BRACE)
            '}' -> createToken(TokenType.RIGHT_BRACE)
            ',' -> createToken(TokenType.COMMA)
            '.' -> createToken(TokenType.DOT)
            '-' -> createToken(TokenType.MINUS)
            '+' -> createToken(TokenType.PLUS)
            ';' -> createToken(TokenType.SEMICOLON)
            '^' -> createToken(TokenType.POW)
            '*' -> createToken(TokenType.STAR)
            '?' -> createToken(TokenType.QUESTION)
            ':' -> createToken(TokenType.COLON)
            '!' -> createToken(chooseNextType('=', TokenType.NOT_EQUAL, TokenType.EXCLAMATION))
            '=' -> createToken(chooseNextType('=', TokenType.EQUAL_EQUAL, TokenType.EQUAL))
            '<' -> createToken(chooseNextType('=', TokenType.LESS_EQUAL, TokenType.LESS))
            '>' -> createToken(chooseNextType('=', TokenType.GREATER_EQUAL, TokenType.GREATER))
            '/' -> {
                if (getNextCharIf('/')) {
                    while (peek() != BREAK_LINE && !isEOF()) getNextChar()
                } else {
                    createToken(TokenType.SLASH)
                }
            }

            ' ', '\r', '\t' -> return
            BREAK_LINE -> { incrementLine(); createToken(TokenType.NEW_LINE) }
            DOUBLE_QUOTES -> createStringLiteral()
            else -> {
                // Check for numbers
                if(it.isDigit()){ createNumberLiteral(); return }

                //Check for identifiers and reserved words
                if (it.isAlpha()){ createIdentifier(); return }

                errorReporter.report(line, column, "Unexpected character.")
            }
        }
    }

    private fun createStringLiteral() {
        while (peek() != DOUBLE_QUOTES && !isEOF()) {
            if (peek() == BREAK_LINE) incrementLine()
            getNextChar()
        }

        if (isEOF()) {
            errorReporter.report(line, column, "Unterminated String.")
            return
        }

        //consumes "
        getNextChar()

        createToken(TokenType.STRING, script.substring(startCursor + 1, currentCursor - 1))
    }

    private fun createNumberLiteral() {
        while (peek().isDigit()) {
            getNextChar()
        }

        if (peek() == '.' && peekNext().isDigit()) {
            getNextChar()
            while (peek().isDigit()) getNextChar()
            createToken(TokenType.NUMBER, script.substring(startCursor, currentCursor).toDouble())
            return
        }

        createToken(TokenType.NUMBER, script.substring(startCursor, currentCursor).toInt())
    }

    private fun createIdentifier() {
        while (peek().isAlphaNumeric()) getNextChar()
        createToken(TokenType.IDENTIFIER)
    }

    private fun chooseNextType(expected: Char, then: TokenType, otherwise: TokenType) =
        if (getNextCharIf(expected))
            then
        else
            otherwise

    private fun createToken(type: TokenType, literal: Any? = null) {
        val created = script.substring(startCursor, currentCursor)
            .let {
                if (TokenType.IDENTIFIER == type) {
                    when {
                        Keywords.isLiteral(it) ->
                            TokenLiteral(Keywords.getReserved(it), it, line, column, Keywords.getLiteralValue(it))
                        Keywords.isReserved(it) ->
                            Token(Keywords.getReserved(it), it, line, column)
                        else ->
                            Token(type, it, line, column)
                    }
                } else {
                    val token = Token(type, it, line, column)
                    literal?.let { lit -> TokenLiteral.of(token, lit) }
                        ?: token
                }
            }
        tokens.add(created)
    }

    private fun peek(): Char {
        if (isEOF()) return EOL
        return script[currentCursor]
    }

    private fun peekNext(): Char {
        if (currentCursor + 1 >= script.length) return EOL
        return script[currentCursor + 1]
    }

    private fun getNextChar(): Char = script[++currentCursor - 1].also {
        column++
    }

    private fun getNextCharIf(expected: Char): Boolean {
        if (isEOF()) return false
        if (script[currentCursor] != expected) return false
        currentCursor++
        column++
        return true
    }

    private fun isEOF(): Boolean = currentCursor >= script.length

    private fun incrementLine() {
        line++
        column = 0
    }

    private fun Char.isAlpha(): Boolean {
        return (this in 'a'..'z') ||
         (this in 'A'..'Z') ||
         (this == '_')
    }

    private fun Char.isAlphaNumeric(): Boolean {
        return this.isAlpha() || this.isDigit()
    }
}