package tachyon.transformer.cfml.evaluator.func.impl

import tachyon.runtime.exp.TemplateException

class ValueList : FunctionEvaluator {
    @Override
    @Throws(TemplateException::class)
    fun pre(bif: BIF?, flf: FunctionLibFunction?): FunctionLibFunction? {
        val args: Array<Argument?> = bif.getArguments()
        // if we have 3 arguments, we switch to _valueList
        return if (args.size == 3) {
            flf.getFunctionLib().getFunction("_ValueList")
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