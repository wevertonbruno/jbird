package execution

import syntactic.tokenizer.Token

class RuntimeError(val token: Token, override val message: String) : RuntimeException(message)
