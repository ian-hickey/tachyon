package tachyon.transformer.bytecode.expression.`var`

import java.util.ArrayList

class Call(expr: Expression?) : ExpressionBase(expr.getFactory(), expr.getStart(), expr.getEnd()), Func {
    private val expr: Expression?
    private val args: List<Argument?>? = ArrayList<Argument?>()

    @Override
    override fun addArgument(argument: Argument?) {
        args.add(argument)
    }

    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        val ga: GeneratorAdapter = bc.getAdapter()
        ga.loadArg(0)
        expr.writeOut(bc, MODE_REF)
        ExpressionUtil.writeOutExpressionArray(bc, Types.OBJECT, args.toArray(arrayOfNulls<Expression?>(args!!.size())))
        ga.invokeStatic(Types.PAGE_CONTEXT_UTIL, if (namedArgs(bc)) GET_FUNCTION_WITH_NAMED_ARGS_KEY else GET_FUNCTION_KEY)
        return Types.OBJECT
    }

    @Throws(TransformerException::class)
    private fun namedArgs(bc: BytecodeContext?): Boolean {
        if (args!!.isEmpty()) return false
        val it: Iterator<Argument?> = args.iterator()
        val named = it.next() is NamedArgument
        while (it.hasNext()) {
            if (named != it.next() is NamedArgument) throw TransformerException(bc, "You cannot mix named and unnamed arguments in function calls", getEnd())
        }
        return named
    }

    companion object {
        // Object getFunction (PageContext,Object,Object[])
        private val GET_FUNCTION_KEY: Method? = Method("getFunction", Types.OBJECT, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT_ARRAY))

        // Object getFunctionWithNamedValues (PageContext,Object,Object[])
        private val GET_FUNCTION_WITH_NAMED_ARGS_KEY: Method? = Method("getFunctionWithNamedValues", Types.OBJECT, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT_ARRAY))
    }

    init {
        this.expr = expr
    }
}