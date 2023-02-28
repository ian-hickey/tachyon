package tachyon.runtime.functions.string

import tachyon.runtime.PageContext

class StringEach : BIF(), Function {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size < 2) {
            throw FunctionException(pc, "StringEach", 2, 2, args.size)
        }
        return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]))
    }

    companion object {
        private const val serialVersionUID = 2207105205243253849L
        @Throws(PageException::class)
        fun call(pc: PageContext?, str: String?, udf: UDF?): String? {
            val stringList = StringListData(str, "", false, false)
            return Each.call(pc, stringList, udf)
        } // call(Ltachyon/runtime/PageContext;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/String;
    }
}