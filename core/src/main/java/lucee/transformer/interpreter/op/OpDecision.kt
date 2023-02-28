package lucee.transformer.interpreter.op

import lucee.runtime.engine.ThreadLocalPageContext

class OpDecision private constructor(left: Expression?, right: Expression?, operation: Int) : ExpressionBase(left.getFactory(), left.getStart(), right.getEnd()), ExprBoolean {
    private val left: Expression?
    private val right: Expression?
    private val op: Int
    @Override
    @Throws(PageException::class)
    fun _writeOut(ic: InterpreterContext?, mode: Int): Class<*>? {
        val b: Boolean
        b = if (op == Factory.OP_DEC_CT) {
            OpUtil.ct(ThreadLocalPageContext.get(ic.getPageContext()), ic.getValue(left), ic.getValue(right))
        } else if (op == Factory.OP_DEC_NCT) {
            OpUtil.nct(ThreadLocalPageContext.get(ic.getPageContext()), ic.getValue(left), ic.getValue(right))
        } else if (op == Factory.OP_DEC_EEQ) {
            OpUtil.eeq(ThreadLocalPageContext.get(ic.getPageContext()), ic.getValue(left), ic.getValue(right))
        } else if (op == Factory.OP_DEC_NEEQ) {
            OpUtil.neeq(ThreadLocalPageContext.get(ic.getPageContext()), ic.getValue(left), ic.getValue(right))
        } else {
            val i: Int = OpUtil.compare(ThreadLocalPageContext.get(ic.getPageContext()), ic.getValue(left), ic.getValue(right))
            if (Factory.OP_DEC_LT === op) i < 0 else if (Factory.OP_DEC_LTE === op) i <= 0 else if (Factory.OP_DEC_GT === op) i > 0 else if (Factory.OP_DEC_GTE === op) i >= 0 else if (Factory.OP_DEC_EQ === op) i == 0 else if (Factory.OP_DEC_NEQ === op) i != 0 else throw InterpreterException("invalid operation: $op")
        }
        if (mode == MODE_VALUE) {
            ic.stack(b)
            return Boolean::class.javaPrimitiveType
        }
        ic.stack(Boolean.valueOf(b))
        return Boolean::class.java
    }

    fun getLeft(): Expression? {
        return left
    }

    fun getRight(): Expression? {
        return right
    }

    fun getOperation(): Int {
        return op
    }

    companion object {
        /**
         * Create a String expression from an operation
         *
         * @param left
         * @param right
         *
         * @return String expression
         */
        fun toExprBoolean(left: Expression?, right: Expression?, operation: Int): ExprBoolean? {
            return OpDecision(left, right, operation)
        }
    }

    init {
        this.left = left
        this.right = right
        op = operation
    }
}