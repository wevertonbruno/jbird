package execution

import lexical.Token

class RuntimeError(val token: Token, override val message: String) : RuntimeException(message)
