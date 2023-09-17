package execution

import syntactic.parser.VM

interface BirdCallable {
    fun call(vm: VM, arguments: List<Any>): Any
    fun arity(): Int
}