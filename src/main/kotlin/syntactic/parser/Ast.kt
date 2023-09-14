package syntactic.parser

import syntactic.tokenizer.Token

abstract class Expr {
    abstract fun <T> accept(visitor: Visitor<T>): T

    interface Visitor<T> {
        fun visitBinaryExpr(binary: Binary): T
        fun visitUnaryExpr(unary: Unary): T
        fun visitGroupingExpr(grouping: Grouping): T
        fun visitLiteralExpr(literal: Literal): T
        fun visitTernaryExpr(ternary: Ternary): T
        fun visitVariableExpr(ternary: Variable): T
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

    class Variable(val identifier: Token): Expr() {
        override fun <T> accept(visitor: Visitor<T>): T = run(visitor::visitVariableExpr)
    }
}

abstract class Stmt {
    abstract fun <T> accept(visitor: Visitor<T>): T

    interface Visitor<T> {
        fun visitExpressionStmt(expr: Expression): T
        fun visitPrintStmt(expr: Print): T
        fun visitVarStmt(stmt: Var): T
    }

    class Expression(val expr: Expr): Stmt() {
        override fun <T> accept(visitor: Visitor<T>): T = run(visitor::visitExpressionStmt)
    }

    class Print(val expr: Expr): Stmt() {
        override fun <T> accept(visitor: Visitor<T>): T = run(visitor::visitPrintStmt)
    }
    
    class Var(val identifier: Token, val expr: Expr?): Stmt() {
        override fun <T> accept(visitor: Visitor<T>): T = run (visitor::visitVarStmt)
    }
}

interface InterpreterVisitor : Expr.Visitor<Any?>, Stmt.Visitor<Unit>

class Program {
    val statements = mutableListOf<Stmt>()
    companion object { fun newInstance() = Program() }
    fun addStatement(stmt: Stmt) = statements.add(stmt)
    fun accept(visitor: InterpreterVisitor) = statements.forEach { it.accept(visitor) }
}


