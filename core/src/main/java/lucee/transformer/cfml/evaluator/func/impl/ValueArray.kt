package lucee.transformer.cfml.evaluator.func.impl

import lucee.runtime.exp.TemplateException

class ValueArray : FunctionEvaluator {
    @Override
    @Throws(TemplateException::class)
    fun pre(bif: BIF?, flf: FunctionLibFunction?): FunctionLibFunction? {
        val args: Array<Argument?> = bif.getArguments()
        // if we have to argument, we switch to QueryColumnData
        return if (args.size == 2) {
            flf.getFunctionLib().getFunction("QueryColumnData")
        } else null
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