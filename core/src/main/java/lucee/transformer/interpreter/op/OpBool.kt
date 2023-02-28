package lucee.transformer.interpreter.op

import lucee.runtime.exp.PageException

class OpBool private constructor(left: Expression?, right: Expression?, operation: Int) : ExpressionBase(left.getFactory(), left.getStart(), right.getEnd()), ExprBoolean {
    private val left: ExprBoolean?
    private val right: ExprBoolean?
    private val operation: Int
    @Override
    @Throws(PageException::class)
    fun _writeOut(ic: InterpreterContext?, mode: Int): Class<*>? {
        val res: Boolean
        // AND
        res = if (operation == Factory.OP_BOOL_AND) {
            ic.getValueAsBooleanValue(left) && ic.getValueAsBooleanValue(right)
        } else if (operation == Factory.OP_BOOL_OR) {
            ic.getValueAsBooleanValue(left) || ic.getValueAsBooleanValue(right)
        } else if (operation == Factory.OP_BOOL_XOR) {
            ic.getValueAsBooleanValue(left) xor ic.getValueAsBooleanValue(right)
        } else if (operation == Factory.OP_BOOL_EQV) {
            OpUtil.eqv(ic.getPageContext(), ic.getValueAsBooleanValue(left), ic.getValueAsBooleanValue(right))
        } else if (operation == Factory.OP_BOOL_IMP) {
            OpUtil.imp(ic.getPageContext(), ic.getValueAsBooleanValue(left), ic.getValueAsBooleanValue(right))
        } else throw InterpreterException("invalid operatior:$operation")
        if (mode == MODE_REF) {
            ic.stack(Caster.toBoolean(res))
            return Boolean::class.java
        }
        ic.stack(res)
        return Boolean::class.javaPrimitiveType
    }

    @Override
    override fun toString(): String {
        return left.toString() + " " + toStringOperation() + " " + right
    }

    private fun toStringOperation(): String? {
        if (Factory.OP_BOOL_AND === operation) return "and"
        if (Factory.OP_BOOL_OR === operation) return "or"
        if (Factory.OP_BOOL_XOR === operation) return "xor"
        if (Factory.OP_BOOL_EQV === operation) return "eqv"
        return if (Factory.OP_BOOL_IMP === operation) "imp" else operation.toString() + ""
    }

    companion object {
        /**
         * Create a String expression from an Expression
         *
         * @param left
         * @param right
         *
         * @return String expression
         * @throws TemplateException
         */
        fun toExprBoolean(left: Expression?, right: Expression?, operation: Int): ExprBoolean? {
            if (left is Literal && right is Literal) {
                val l: Boolean = (left as Literal?).getBoolean(null)
                val r: Boolean = (right as Literal?).getBoolean(null)
                if (l != null && r != null) {
                    when (operation) {
                        Factory.OP_BOOL_AND -> return left.getFactory().createLitBoolean(l.booleanValue() && r.booleanValue(), left.getStart(), right.getEnd())
                        Factory.OP_BOOL_OR -> return left.getFactory().createLitBoolean(l.booleanValue() || r.booleanValue(), left.getStart(), right.getEnd())
                        Factory.OP_BOOL_XOR -> return left.getFactory().createLitBoolean(l.booleanValue() xor r.booleanValue(), left.getStart(), right.getEnd())
                    }
                }
            }
            return OpBool(left, right, operation)
        }
    }

    init {
        this.left = left.getFactory().toExprBoolean(left)
        this.right = left.getFactory().toExprBoolean(right)
        this.operation = operation
    }
}