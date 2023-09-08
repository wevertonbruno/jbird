package syntactic.ast

class AstPrinter : Visitor<String> {

    fun print(expr: Expr) = expr.accept(this)

    override fun visitBinaryExpr(binary: Binary): String =
        "(${binary.operator.lexeme} ${binary.left.accept(this)} ${binary.right.accept(this)})"

    override fun visitUnaryExpr(unary: Unary): String =
        "(${unary.prefix.lexeme} ${unary.right.accept(this)})"

    override fun visitGroupingExpr(grouping: Grouping): String =
        "(group ${grouping.expr.accept(this)})"

    override fun visitLiteralExpr(literal: Literal): String =
        literal.value?.let { literal.value.toString() } ?: "nil"
}