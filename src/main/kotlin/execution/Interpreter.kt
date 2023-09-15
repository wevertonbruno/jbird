package execution

import reports.ErrorReporter
import syntactic.parser.*
import syntactic.tokenizer.TokenType
import kotlin.math.pow

class Interpreter(private val errorReporter: ErrorReporter) : InterpreterVisitor {
    private var environment = Environment()

    fun interpret(program: Program?) = try {
        program?.accept(this)
    } catch (ex: RuntimeError) {
        errorReporter.report(ex.token, ex.message)
    }

    override fun visitBinaryExpr(binary: Expr.Binary): Result = run {
        val first = evaluate(binary.left).value
        val second = evaluate(binary.right).value

        when (binary.operator.type) {
            TokenType.MINUS -> when {
                first is Int && second is Int -> Result(first - second)
                first is Double && second is Double -> Result(first - second)
                first is Int && second is Double -> Result(first - second)
                first is Double && second is Int -> Result(first - second)
                else -> throw RuntimeError(binary.operator, "Operands must be numbers.")
            }

            TokenType.SLASH -> when {
                second == 0 -> throw RuntimeError(binary.operator, "Division by zero is not allowed.")
                first is Int && second is Int -> Result(first / second)
                first is Double && second is Double -> Result(first / second)
                first is Int && second is Double -> Result(first / second)
                first is Double && second is Int -> Result(first / second)
                else -> throw RuntimeError(binary.operator, "Operands must be numbers.")
            }

            TokenType.STAR -> when {
                first is Int && second is Int -> Result(first * second)
                first is Double && second is Double -> Result(first * second)
                first is Int && second is Double -> Result(first * second)
                first is Double && second is Int -> Result(first * second)
                else -> throw RuntimeError(binary.operator, "Operands must be numbers.")
            }

            TokenType.POW -> when {
                first is Int && second is Int -> Result(first.toDouble().pow(second.toDouble()))
                first is Double && second is Double -> Result(first.pow(second))
                first is Int && second is Double -> Result(first.toDouble().pow(second))
                first is Double && second is Int -> Result(first.pow(second.toDouble()))
                else -> throw RuntimeError(binary.operator, "Operands must be numbers.")
            }

            TokenType.PLUS -> when {
                first is Int && second is Int -> Result(first + second)
                first is Double && second is Double -> Result(first + second)
                first is Int && second is Double -> Result(first + second)
                first is Double && second is Int -> Result(first + second)
                isStringOrNumber(first) && isStringOrNumber(second) -> Result("$first$second")
                else -> throw RuntimeError(binary.operator, "Operands must be numbers or strings.")
            }

            TokenType.GREATER -> when {
                first is Number && second is Number -> Result(first.toDouble() > second.toDouble())
                else -> throw RuntimeError(binary.operator, "Operands must be numbers.")
            }

            TokenType.GREATER_EQUAL -> when {
                first is Number && second is Number -> Result(first.toDouble() >= second.toDouble())
                else -> throw RuntimeError(binary.operator, "Operands must be numbers.")
            }

            TokenType.LESS -> when {
                first is Number && second is Number -> Result(first.toDouble() < second.toDouble())
                else -> throw RuntimeError(binary.operator, "Operands must be numbers.")
            }

            TokenType.LESS_EQUAL -> when {
                first is Number && second is Number -> Result(first.toDouble() <= second.toDouble())
                else -> throw RuntimeError(binary.operator, "Operands must be numbers.")
            }

            TokenType.EQUAL_EQUAL -> Result(first?.equals(second) ?: (second == null))

            else -> Result(null)
        }
    }

    override fun visitUnaryExpr(unary: Expr.Unary): Result = evaluate(unary.right)
        .run {
            when (unary.prefix.type) {
                TokenType.MINUS -> when (value) {
                    is Int -> Result(-value)
                    is Double -> Result(-value)
                    else -> Result(null)
                }

                TokenType.EXCLAMATION -> Result(!isTruthy(value))
                else -> Result(null)
            }
        }

    override fun visitGroupingExpr(grouping: Expr.Grouping): Result = evaluate(grouping.expr)

    override fun visitLiteralExpr(literal: Expr.Literal): Result = Result(literal.value)

    override fun visitTernaryExpr(ternary: Expr.Ternary): Result = evaluate(ternary.condition)
        .run {
            if (isTruthy(value)) evaluate(ternary.thenBranch)
            else evaluate(ternary.otherwiseBranch)
        }

    override fun visitVariableExpr(variable: Expr.Variable): Result {
        return Result(environment.retrieve(variable.identifier))
    }

    override fun visitAssignExpr(assign: Expr.Assign): Result {
        val value = evaluate(assign.value).value
        environment.assign(assign.name, value)
        return Result(value)
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        evaluate(stmt.expr)
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        evaluate(stmt.expr)
            .let(Result::value)
            .let(::stringify)
            .also(::println)
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        val value = stmt.expr?.let(::evaluate)?.value
        environment.define(stmt.identifier.lexeme, value)
    }

    override fun visitBlockStmt(stmt: Stmt.Block) {
        executeBlock(stmt.statements, Environment(environment))
    }

    private fun evaluate(expr: Expr): Result = Result(expr.accept(this).value)

    private fun isTruthy(value: Any?): Boolean = when (value) {
        null -> false
        is Boolean -> value
        else -> true
    }

    private fun isStringOrNumber(value: Any?): Boolean = value is String || value is Number

    private fun stringify(value: Any?): String = value?.let { value.toString() } ?: ""

    private fun executeBlock(statements: List<Stmt>, environment: Environment) {
        val previous = this.environment
        try {
            this.environment = environment
            statements.forEach {
                it.accept(this)
            }
        } finally {
            this.environment = previous
        }
    }
}
