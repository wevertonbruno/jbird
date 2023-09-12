package syntactic

import lexical.Token

abstract class Expr {
    abstract fun <T> accept(visitor: Expr.Visitor<T>): T

    interface Visitor<T> {
        fun visitBinaryExpr(binary: Binary): T
        fun visitUnaryExpr(unary: Unary): T
        fun visitGroupingExpr(grouping: Grouping): T
        fun visitLiteralExpr(literal: Literal): T
        fun visitTernaryExpr(ternary: Ternary): T
    }
}

abstract class Stmt {
    abstract fun <T> accept(visitor: Stmt.Visitor<T>): T

    interface Visitor<T> {
        fun visitExpressionStmt(expr: ExpressionStmt): T
        fun visitPrintStmt(expr: PrintStmt): T
    }
}

interface InterpreterVisitor : Expr.Visitor<Any?>, Stmt.Visitor<Unit>

class Program {
    val statements = mutableListOf<Stmt>()
    companion object { fun newInstance() = Program() }
    fun addStatement(stmt: Stmt) = statements.add(stmt)
    fun accept(visitor: InterpreterVisitor) = statements.forEach { it.accept(visitor) }
}

class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T = run(visitor::visitBinaryExpr)
}

class Ternary(val condition: Expr, val thenBranch: Expr, val otherwiseBranch: Expr) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T = run(visitor::visitTernaryExpr)
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

class ExpressionStmt(val expr: Expr): Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T = run(visitor::visitExpressionStmt)
}

class PrintStmt(val expr: Expr): Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T = run(visitor::visitPrintStmt)
}
