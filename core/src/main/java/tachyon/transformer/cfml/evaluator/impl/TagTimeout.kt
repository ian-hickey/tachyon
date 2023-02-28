package tachyon.transformer.cfml.evaluator.impl

import tachyon.transformer.TransformerException

class TagTimeout : EvaluatorSupport() {
    @Override
    @Throws(EvaluatorException::class)
    fun evaluate(tag: Tag?, tagLibTag: TagLibTag?, flibs: Array<FunctionLib?>?) {
        try {
            tag!!.init()
        } catch (te: TransformerException) {
            val ee = EvaluatorException(te.getMessage())
            ee.initCause(te)
            throw ee
        }
    }
}