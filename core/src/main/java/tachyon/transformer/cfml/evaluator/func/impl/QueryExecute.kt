package tachyon.transformer.cfml.evaluator.func.impl

import tachyon.commons.io.log.LogUtil

class QueryExecute : FunctionEvaluator {
    @Override
    @Throws(TemplateException::class)
    fun execute(bif: BIF?, flf: FunctionLibFunction?) {
    }

    @Override
    @Throws(EvaluatorException::class)
    fun evaluate(bif: BIF?, flf: FunctionLibFunction?) {
        val `var`: Variable = bif.getParent()
        if (`var` != null) {
            val ass: Assign = `var`.assign()
            if (ass != null) {
                try {
                    val str: String = VariableString.variableToString(null, ass.getVariable(), false)
                    addArgument(bif, str)
                } catch (e: TransformerException) {
                    LogUtil.log(QueryExecute::class.java.getName(), e)
                }
            }
        }
    }

    private fun addArgument(bif: BIF?, str: String?) {
        val args: Array<Argument?> = bif.getArguments()

        // named arguments
        if (args[0] is NamedArgument) {
            bif.addArgument(NamedArgument(bif.getFactory().createLitString("name"), bif.getFactory().createLitString(str), "string", false))
        } else {
            // add params
            if (args.size == 1) {
                bif.addArgument(Argument(bif.getFactory().createNull(), "any"))
            }
            // add options
            if (args.size <= 2) {
                bif.addArgument(Argument(bif.getFactory().createStruct(), "struct"))
            }
            // add the name
            bif.addArgument(Argument(bif.getFactory().createLitString(str), "string"))
        }
    }

    @Override
    @Throws(TemplateException::class)
    fun pre(bif: BIF?, flf: FunctionLibFunction?): FunctionLibFunction? {
        return null
    }
}