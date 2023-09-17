package execution

import reports.ErrorReporter
import syntactic.parser.*
import syntactic.tokenizer.TokenType
import kotlin.math.pow

class KotlinVM(private val errorReporter: ErrorReporter) : VM {
    private val globals = Environment()
    private var environment = globals

    init {
        globals.define("time", object : BirdCallable {
            override fun call(vm: VM, arguments: List<Any>): Any {
                return (System.currentTimeMillis() / 1000.0)
            }

            override fun arity() = 0

            override fun toString() = "<builtin function 'time'>"
        })
    }

    fun run(program: Program?) = try {
        program?.accept(this)
    } catch (ex: RuntimeError) {
        errorReporter.report(ex.token, ex.message)
    }

    override fun getGlobals() = globals

    override fun visitBinaryExpr(binary: Expr.Binary): Any = run {
        val first = evaluate(binary.left)
        val second = evaluate(binary.right)

        when (binary.operator.type) {
            TokenType.MINUS -> when {
                first is Int && second is Int -> first - second
                first is Double && second is Double -> first - second
                first is Int && second is Double -> first - second
                first is Double && second is Int -> first - second
                else -> throw RuntimeError(binary.operator, "Operands must be numbers.")
            }

            TokenType.SLASH -> when {
                second == 0 -> throw RuntimeError(binary.operator, "Division by zero is not allowed.")
                first is Int && second is Int -> first / second
                first is Double && second is Double -> first / second
                first is Int && second is Double -> first / second
                first is Double && second is Int -> first / second
                else -> throw RuntimeError(binary.operator, "Operands must be numbers.")
            }

            TokenType.STAR -> when {
                first is Int && second is Int -> first * second
                first is Double && second is Double -> first * second
                first is Int && second is Double -> first * second
                first is Double && second is Int -> first * second
                else -> throw RuntimeError(binary.operator, "Operands must be numbers.")
            }

            TokenType.POW -> when {
                first is Int && second is Int -> first.toDouble().pow(second.toDouble())
                first is Double && second is Double -> first.pow(second)
                first is Int && second is Double -> first.toDouble().pow(second)
                first is Double && second is Int -> first.pow(second.toDouble())
                else -> throw RuntimeError(binary.operator, "Operands must be numbers.")
            }

            TokenType.PLUS -> when {
                first is Int && second is Int -> first + second
                first is Double && second is Double -> first + second
                first is Int && second is Double -> first + second
                first is Double && second is Int -> first + second
                isStringOrNumber(first) && isStringOrNumber(second) -> "$first$second"
                else -> throw RuntimeError(binary.operator, "Operands must be numbers or strings.")
            }

            TokenType.GREATER -> when {
                first is Number && second is Number -> first.toDouble() > second.toDouble()
                else -> throw RuntimeError(binary.operator, "Operands must be numbers.")
            }

            TokenType.GREATER_EQUAL -> when {
                first is Number && second is Number -> first.toDouble() >= second.toDouble()
                else -> throw RuntimeError(binary.operator, "Operands must be numbers.")
            }

            TokenType.LESS -> when {
                first is Number && second is Number -> first.toDouble() < second.toDouble()
                else -> throw RuntimeError(binary.operator, "Operands must be numbers.")
            }

            TokenType.LESS_EQUAL -> when {
                first is Number && second is Number -> first.toDouble() <= second.toDouble()
                else -> throw RuntimeError(binary.operator, "Operands must be numbers.")
            }

            TokenType.EQUAL_EQUAL -> first == second

            else -> Nil
        }
    }

    override fun visitUnaryExpr(unary: Expr.Unary): Any =
        evaluate(unary.right)
            .run {
                when (unary.prefix.type) {
                    TokenType.MINUS -> when (this) {
                        is Int -> -this
                        is Double -> -this
                        else -> Nil
                    }

                    TokenType.EXCLAMATION -> !isTruthy(this)
                    else -> Nil
                }
            }

    override fun visitGroupingExpr(grouping: Expr.Grouping): Any = evaluate(grouping.expr)

    override fun visitLiteralExpr(literal: Expr.Literal): Any = literal.value ?: Nil

    override fun visitTernaryExpr(ternary: Expr.Ternary): Any =
        evaluate(ternary.condition)
            .run {
                if (isTruthy(this)) evaluate(ternary.thenBranch)
                else evaluate(ternary.otherwiseBranch)
            }

    override fun visitVariableExpr(variable: Expr.Variable): Any {
        return environment.retrieve(variable.identifier)
    }

    override fun visitAssignExpr(assign: Expr.Assign): Any {
        val value = evaluate(assign.value)
        environment.assign(assign.name, value)
        return value
    }

    override fun visitLogicalExpr(logical: Expr.Logical): Any {
        val left = evaluate(logical.left)
        if (logical.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left
        }else {
            if (!isTruthy(left)) return left
        }
        return evaluate(logical.right)
    }

    override fun visitCallExpr(call: Expr.Call): Any {
        val callee = evaluate(call.callee)

        if (callee !is BirdCallable) throw RuntimeError(call.paren, "Can only call functions and classes.")
        val arguments = call.arguments.map { evaluate(it) }
        return (callee as BirdCallable).also{
            if(arguments.size != it.arity())
                throw RuntimeError(call.paren, "Expected ${it.arity()} arguments but got ${arguments.size}.")
        }.call(this, arguments)
    }

    override fun visitExpressionStmt(expr: Stmt.Expression) = evaluate(expr.expr)

    override fun visitPrintStmt(expr: Stmt.Print) =
        evaluate(expr.expr)
            .let(::stringify)
            .also(::println)

    override fun visitVarStmt(stmt: Stmt.Var): Any {
        val value = stmt.expr?.let(::evaluate) ?: Nil
        environment.define(stmt.identifier.lexeme, value)
        return value
    }

    override fun visitBlockStmt(stmt: Stmt.Block) = executeBlock(stmt.statements, Environment(environment))

    override fun visitIfStmt(stmt: Stmt.If): Any {
        val condition = evaluate(stmt.condition)

        return if (isTruthy(condition)) {
            stmt.thenBranch.accept(this)
        } else {
            stmt.elseBranch?.accept(this) ?: Nil
        }
    }

    override fun visitWhileStmt(stmt: Stmt.While) {
        while (isTruthy(evaluate(stmt.condition))) {
            stmt.body.accept(this)
        }
    }

    override fun visitDoWhileStmt(stmt: Stmt.DoWhile) {
        do {
            stmt.body.accept(this)
        } while (isTruthy(evaluate(stmt.condition)))
    }

    override fun visitFunctionStmt(function: Stmt.Function): BirdCallable {
        val func = BirdFunction(function)
        environment.define(function.name.lexeme, func)
        return func
    }

    override fun visitReturnStmt(stmt: Stmt.Return): Any {
        throw ReturnCall(stmt.value?.let(::evaluate) ?: Nil)
    }

    private fun evaluate(expr: Expr): Any = expr.accept(this)

    private fun isTruthy(value: Any): Boolean = when (value) {
        is Nil -> false
        is Boolean -> value
        else -> true
    }

    private fun isStringOrNumber(value: Any): Boolean = value is String || value is Number

    private fun stringify(value: Any): String = value.let { value.toString() }

    override fun executeBlock(statements: List<Stmt>, env: Environment): Any {
        val previous = this.environment
        var value: Any = Nil
        try {
            this.environment = env
            statements.forEach {
                value = it.accept(this)
            }
        } finally {
            this.environment = previous
        }

        return value
    }
}
