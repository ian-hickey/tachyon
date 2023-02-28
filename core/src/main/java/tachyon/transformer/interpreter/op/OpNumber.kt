package tachyon.transformer.interpreter.op

import tachyon.runtime.exp.PageException

class OpNumber internal constructor(left: Expression?, right: Expression?, operation: Int) : ExpressionBase(left.getFactory(), left.getStart(), right.getEnd()), ExprNumber {
    private val op: Int
    private val left: Expression?
    private val right: Expression?
    fun getLeft(): Expression? {
        return left
    }

    fun getRight(): Expression? {
        return right
    }

    fun getOperation(): Int {
        return op
    }

    @Override
    @Throws(PageException::class)
    fun _writeOut(ic: InterpreterContext?, mode: Int): Class<*>? {
        return writeOutNumber(ic, mode)
    }

    @Throws(PageException::class)
    fun writeOutNumber(ic: InterpreterContext?, mode: Int): Class<*>? {
        // TODOX all as Number
        val n: Number
        n = if (op == Factory.OP_DBL_EXP) {
            OpUtil.exponentRef(ic.getPageContext(), ic.getValueAsNumber(left), ic.getValueAsNumber(right))
        } else if (op == Factory.OP_DBL_DIVIDE) {
            OpUtil.divideRef(ic.getPageContext(), ic.getValueAsNumber(left), ic.getValueAsNumber(right))
        } else if (op == Factory.OP_DBL_INTDIV) {
            OpUtil.intdivRef(ic.getPageContext(), ic.getValueAsNumber(left), ic.getValueAsNumber(right))
        } else if (op == Factory.OP_DBL_PLUS) {
            OpUtil.plusRef(ic.getPageContext(), ic.getValueAsNumber(left), ic.getValueAsNumber(right))
        } else if (op == Factory.OP_DBL_MINUS) {
            OpUtil.minusRef(ic.getPageContext(), ic.getValueAsNumber(left), ic.getValueAsNumber(right))
        } else if (op == Factory.OP_DBL_MODULUS) {
            OpUtil.modulusRef(ic.getPageContext(), ic.getValueAsNumber(left), ic.getValueAsNumber(right))
        } else if (op == Factory.OP_DBL_DIVIDE) {
            OpUtil.divideRef(ic.getPageContext(), ic.getValueAsNumber(left), ic.getValueAsNumber(right))
        } else if (op == Factory.OP_DBL_MULTIPLY) {
            OpUtil.multiplyRef(ic.getPageContext(), ic.getValueAsNumber(left), ic.getValueAsNumber(right))
        } else throw InterpreterException("invalid operation: $op")
        /*
		 * if (mode == MODE_VALUE) { ic.stack(d); return double.class; }
		 */ic.stack(n)
        return Number::class.java
    }

    companion object {
        fun toExprNumber(left: Expression?, right: Expression?, operation: Int): ExprNumber? {
            return OpNumber(left, right, operation)
        }
    }

    init {
        this.left = left
        this.right = right
        op = operation
    }
}