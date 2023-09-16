package execution

import syntactic.tokenizer.Token

class Environment(private val enclosing: Environment? = null) {
    private val values = mutableMapOf<String, Any?>()

    fun define(name: String, value: Any) {
        values[name] = value
    }

    fun retrieve(name: Token): Any =
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme]!!
        } else {
            enclosing?.retrieve(name)
                ?: throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
        }

    fun assign(name: Token, value: Any) {
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
        } else {
            enclosing?.assign(name, value)
                ?: throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
        }
    }
}
