package execution

import syntactic.parser.Expr
import syntactic.parser.Nil
import syntactic.parser.VM

class BirdFunction(
    private val declaration: Expr.Function,
    private val closure: Environment,
    private val name: String = "__anonymous__"
): BirdCallable {
    override fun call(vm: VM, arguments: List<Any>): Any {
        val env = Environment(closure)
        declaration.params.forEachIndexed { index, token ->
            env.define(token.lexeme, arguments[index]) }
        try {
            vm.executeBlock(declaration.body, env)
        } catch (returnValue: ReturnCall){
            return returnValue.value
        }
        return Nil
    }

    override fun arity() = declaration.params.size

    override fun toString(): String {
        return "<function '${name}'>"
    }
}