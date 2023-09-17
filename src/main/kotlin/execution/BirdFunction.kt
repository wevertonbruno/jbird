package execution

import syntactic.parser.Nil
import syntactic.parser.VM
import syntactic.parser.Stmt

class BirdFunction(private val declaration: Stmt.Function): BirdCallable {
    override fun call(vm: VM, arguments: List<Any>): Any {
        val env = Environment(vm.getGlobals())
        declaration.params.forEachIndexed { index, token ->
            env.define(token.lexeme, arguments[index]) }
        try {
            vm.executeBlock(declaration.body.statements, env)
        } catch (returnValue: ReturnCall){
            return returnValue.value
        }
        return Nil
    }

    override fun arity() = declaration.params.size

    override fun toString(): String {
        return "<function '${declaration.name.lexeme}'>"
    }
}