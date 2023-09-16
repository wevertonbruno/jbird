package execution

import reports.ErrorReporter
import syntactic.parser.Expr
import syntactic.parser.InterpreterVisitor
import syntactic.parser.Program
import syntactic.parser.Stmt
import syntactic.tokenizer.TokenType
import kotlin.math.pow

class Interpreter(private val errorReporter: ErrorReporter) : InterpreterVisitor {
    private var environment = Environment()

    fun interpret(program: Program?) = try {
        program?.accept(this)
    } catch (ex: RuntimeError) {
        errorReporter.report(ex.token, ex.message)
    }

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

    private fun evaluate(expr: Expr): Any = expr.accept(this)

    private fun isTruthy(value: Any): Boolean = when (value) {
        is Nil -> false
        is Boolean -> value
        else -> true
    }

    private fun isStringOrNumber(value: Any): Boolean = value is String || value is Number

    private fun stringify(value: Any): String = value.let { value.toString() }

    private fun executeBlock(statements: List<Stmt>, environment: Environment): Any {
        val previous = this.environment
        var value: Any = Nil
        try {
            this.environment = environment
            statements.forEach {
                value = it.accept(this)
            }
        } finally {
            this.environment = previous
        }

        return value
    }

    object Nil {
        override fun toString() = "nil"
    }
}
