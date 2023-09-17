package syntactic.tokenizer

enum class TokenType(val isRightAssociative: Boolean = false) {
    // Single-character tokens.
    LEFT_PAREN,
    RIGHT_PAREN,
    LEFT_BRACE,
    RIGHT_BRACE,
    COMMA,
    DOT,
    MINUS,
    PLUS,
    SEMICOLON,
    SLASH,
    STAR,
    QUESTION(true),
    COLON,
    NEW_LINE,
    POW(true),

    // One or two character tokens.
    EXCLAMATION,
    NOT_EQUAL,
    EQUAL(true),
    EQUAL_EQUAL,
    GREATER,
    GREATER_EQUAL,
    LESS,
    LESS_EQUAL,

    // Expr.Literals.
    IDENTIFIER,
    STRING,
    NUMBER,

    // Keywords.
    AND,
    CLASS,
    ELSE,
    FALSE,
    FUNC,
    FOR,
    IF,
    NIL,
    OR,
    PRINT,
    RETURN,
    SUPER,
    THIS,
    TRUE,
    VAR,
    WHILE,
    DO,

    //end of file
    EOF
}