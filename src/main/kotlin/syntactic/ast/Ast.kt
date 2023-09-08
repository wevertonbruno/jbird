package syntactic.ast

import lexical.Token

abstract class Expr {
    abstract fun <T> accept(visitor: Visitor<T>): T
}

class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T = run(visitor::visitBinaryExpr)
}

class Unary(val prefix: Token, val right: Expr): Expr() {
    override fun <T> accept(visitor: Visitor<T>): T = run(visitor::visitUnaryExpr)
}

class Grouping(val expr: Expr): Expr() {
    override fun <T> accept(visitor: Visitor<T>): T = run(visitor::visitGroupingExpr)
}

class Literal(val value: Any?): Expr() {
    override fun <T> accept(visitor: Visitor<T>): T = run(visitor::visitLiteralExpr)
}