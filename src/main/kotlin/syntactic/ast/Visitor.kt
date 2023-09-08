package syntactic.ast

interface Visitor<T> {
    fun visitBinaryExpr(binary: Binary): T
    fun visitUnaryExpr(unary: Unary): T
    fun visitGroupingExpr(grouping: Grouping): T
    fun visitLiteralExpr(literal: Literal): T
}