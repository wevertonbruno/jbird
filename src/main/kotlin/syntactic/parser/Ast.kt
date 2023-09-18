package syntactic.parser

import execution.Environment
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
        fun visitLogicalExpr(logical: Logical): T
        fun visitCallExpr(call: Call): T
        fun visitFunctionExpr(function: Function): T
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

    class Logical(val left: Expr, val operator: Token, val right: Expr): Expr() {
        override fun <T> accept(visitor: Visitor<T>): T = run(visitor::visitLogicalExpr)
    }

    class Call(val callee: Expr, val paren: Token, val arguments: List<Expr>): Expr() {
        override fun <T> accept(visitor: Visitor<T>): T = run(visitor::visitCallExpr)
    }

    class Function(val params: List<Token>, val body: List<Stmt>): Expr() {
        override fun <T> accept(visitor: Visitor<T>): T = run (visitor::visitFunctionExpr)
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
        fun visitWhileStmt(stmt: While): T
        fun visitDoWhileStmt(stmt: DoWhile): T
        fun visitFunctionStmt(function: Function): T
        fun visitReturnStmt(stmt: Return): T
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

    class While(val condition: Expr, val body: Stmt): Stmt() {
        override fun <T> accept(visitor: Visitor<T>): T = run (visitor::visitWhileStmt)
    }

    class DoWhile(val condition: Expr, val body: Stmt): Stmt() {
        override fun <T> accept(visitor: Visitor<T>): T = run (visitor::visitDoWhileStmt)
    }

    class Function(val name: Token, val function: Expr.Function): Stmt() {
        override fun <T> accept(visitor: Visitor<T>): T = run (visitor::visitFunctionStmt)
    }

    class Return(val keyword: Token, val value: Expr?): Stmt() {
        override fun <T> accept(visitor: Visitor<T>): T = run (visitor::visitReturnStmt)
    }
}

interface VM : Expr.Visitor<Any>, Stmt.Visitor<Any> {
    fun getGlobals(): Environment
    fun executeBlock(statements: List<Stmt>, env: Environment): Any
}

class Program {
    private val statements = mutableListOf<Stmt>()
    companion object { fun newInstance() = Program() }
    fun addStatement(stmt: Stmt) = statements.add(stmt)
    fun accept(visitor: VM) = statements.forEach { it.accept(visitor) }
}

object Nil {
    override fun toString() = "nil"
}


