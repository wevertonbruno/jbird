package execution

import syntactic.parser.*

class Printer : Expr.Visitor<String> {

    fun print(expr: Expr?) = expr?.accept(this) ?: "nil"

    override fun visitBinaryExpr(binary: Expr.Binary): String =
        "(${binary.operator.lexeme} ${binary.left.accept(this)} ${binary.right.accept(this)})"

    override fun visitUnaryExpr(unary: Expr.Unary): String =
        "(${unary.prefix.lexeme} ${unary.right.accept(this)})"

    override fun visitGroupingExpr(grouping: Expr.Grouping): String =
        "(group ${grouping.expr.accept(this)})"

    override fun visitLiteralExpr(literal: Expr.Literal): String =
        literal.value?.let { literal.value.toString() } ?: "nil"

    override fun visitTernaryExpr(ternary: Expr.Ternary) =
        "${ternary.condition.accept(this)} ? " +
                "${ternary.thenBranch.accept(this)} : " +
                ternary.otherwiseBranch.accept(this)

    override fun visitVariableExpr(ternary: Expr.Variable): String {
        TODO("Not yet implemented")
    }
}
