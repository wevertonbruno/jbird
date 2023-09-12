package execution

import lexical.Token
import lexical.TokenType
import reports.ErrorReporter
import syntactic.*

class Interpreter(private val errorReporter: ErrorReporter) : Visitor<Any?> {

    fun interpret(expr: Expr?) = try {
        val value = expr?.let { evaluate(expr) } ?: ""
        println("[output]: $value")
    } catch (ex: RuntimeError) {
        errorReporter.report(ex.token, ex.message)
    }

    fun interpretAndGetValue(expr: Expr?) = try {
        expr?.let { evaluate(expr) } ?: ""
    } catch (ex: RuntimeError) {
        errorReporter.report(ex.token, ex.message)
        throw ex
    }

    override fun visitBinaryExpr(binary: Binary): Any? = Pair(evaluate(binary.left), evaluate(binary.right))
        .run {
            when (binary.operator.type) {
                TokenType.MINUS -> assertNumberOperands(binary.operator, first, second) {
                    double(first as Number) - double(second as Number)
                }

                TokenType.SLASH -> assertNumberOperands(binary.operator, first, second) {
                    val secondDouble = double(second as Number)
                    if (secondDouble == 0.0) throw RuntimeError(binary.operator, "Division by zero is not allowed.")
                    double(first as Number) / secondDouble
                }

                TokenType.STAR -> assertNumberOperands(binary.operator, first, second) {
                    double(first as Number) * double(second as Number)
                }

                TokenType.PLUS -> when {
                    first is Number && second is Number -> double(first as Number) + double(second as Number)
                    isStringOrNumber(first) && isStringOrNumber(second) -> "$first$second"
                    else -> null
                }

                TokenType.GREATER -> assertNumberOperands(binary.operator, first, second) {
                    double(first as Number) > double(second as Number)
                }

                TokenType.GREATER_EQUAL -> assertNumberOperands(binary.operator, first, second) {
                    double(first as Number) >= double(second as Number)
                }

                TokenType.LESS -> assertNumberOperands(binary.operator, first, second) {
                    double(first as Number) < double(second as Number)
                }

                TokenType.LESS_EQUAL -> assertNumberOperands(binary.operator, first, second) {
                    double(first as Number) <= double(second as Number)
                }

                TokenType.EQUAL -> first?.equals(second) ?: (second == null)

                else -> null
            }
        }

    override fun visitUnaryExpr(unary: Unary): Any? = evaluate(unary.right)
        .run {
            when (unary.prefix.type) {
                TokenType.MINUS -> assertNumberOperands(unary.prefix, this) {
                    -double(this as Number)
                }

                TokenType.EXCLAMATION -> !isTruthy(this)
                else -> null
            }
        }


    override fun visitGroupingExpr(grouping: Grouping): Any? = evaluate(grouping.expr)

    override fun visitLiteralExpr(literal: Literal): Any? = literal.value

    override fun visitTernaryExpr(ternary: Ternary): Any? = evaluate(ternary.condition)
        .run {
            if (isTruthy(this)) evaluate(ternary.thenBranch)
            else evaluate(ternary.otherwiseBranch)
        }

    private fun evaluate(expr: Expr): Any? = expr.accept(this)

    private fun isTruthy(value: Any?): Boolean = when (value) {
        null -> false
        is Boolean -> value
        else -> true
    }

    private fun assertNumberOperands(operator: Token, vararg operands: Any?, supplier: () -> Any): Any {
        if (operands.any { it !is Number }) {
            val sufix = if (operands.size > 1) "s" else ""
            throw RuntimeError(operator, "Operand$sufix must be number$sufix.")
        }
        return supplier()
    }

    private fun isStringOrNumber(value: Any?): Boolean = value is String || value is Number

    private fun double(value: Number) = value.toDouble()
}
