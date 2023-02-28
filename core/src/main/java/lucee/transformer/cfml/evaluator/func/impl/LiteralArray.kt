package lucee.transformer.cfml.evaluator.func.impl

import lucee.runtime.exp.TemplateException

class LiteralArray : FunctionEvaluator {
    @Override
    @Throws(TemplateException::class)
    fun pre(bif: BIF?, flf: FunctionLibFunction?): FunctionLibFunction? {
        val args: Array<Argument?> = bif.getArguments()
        if (args == null || args.size == 0) return null

        // named arguments
        if (args[0] is NamedArgument) {
            for (i in 1 until args.size) {
                if (args[i] !is NamedArgument) throw TemplateException("invalid argument for literal ordered struct, only named arguments are allowed like {name:\"value\",name2:\"value2\"}")
            }
            return flf.getFunctionLib().getFunction("_literalOrderedStruct")
        }
        for (i in 1 until args.size) {
            if (args[i] is NamedArgument) throw TemplateException("invalid argument for literal array, no named arguments are allowed")
        }
        return null
    }

    @Override
    @Throws(TemplateException::class)
    fun execute(bif: BIF?, flf: FunctionLibFunction?) {
    }

    @Override
    @Throws(EvaluatorException::class)
    fun evaluate(bif: BIF?, flf: FunctionLibFunction?) {
    }
}