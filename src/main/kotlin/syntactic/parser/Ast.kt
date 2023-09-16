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
        fun visitVariableExpr(variable: Variable): T
        fun visitAssignExpr(assign: Assign): T
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

    class Assign(val name: Token, val value: Expr): Expr() {
        override fun <T> accept(visitor: Visitor<T>): T = run(visitor::visitAssignExpr)
    }
}

abstract class Stmt {
    abstract fun <T> accept(visitor: Visitor<T>): T

    interface Visitor<T> {
        fun visitExpressionStmt(expr: Expression): T
        fun visitPrintStmt(expr: Print): T
        fun visitVarStmt(stmt: Var): T
        fun visitBlockStmt(stmt: Block): T
        fun visitIfStmt(stmt: If): T
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

    class Block(val statements: List<Stmt>): Stmt() {
        override fun <T> accept(visitor: Visitor<T>): T = run (visitor::visitBlockStmt)
    }

    class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?): Stmt() {
        override fun <T> accept(visitor: Visitor<T>): T = run (visitor::visitIfStmt)
    }
}

interface InterpreterVisitor : Expr.Visitor<Any>, Stmt.Visitor<Any>

class Program {
    private val statements = mutableListOf<Stmt>()
    companion object { fun newInstance() = Program() }
    fun addStatement(stmt: Stmt) = statements.add(stmt)
    fun accept(visitor: InterpreterVisitor) = statements.forEach { it.accept(visitor) }
}


