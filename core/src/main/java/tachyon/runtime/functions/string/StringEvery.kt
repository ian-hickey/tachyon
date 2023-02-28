package tachyon.runtime.functions.string

import tachyon.runtime.PageContext

class StringEvery : BIF(), Function {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size < 2) {
            throw FunctionException(pc, "StringEvery", 2, 2, args.size)
        }
        return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]))
    }

    companion object {
        private const val serialVersionUID = -2889095341490820411L
        @Throws(PageException::class)
        fun call(pc: PageContext?, str: String?, udf: UDF?): Boolean {
            val stringList = StringListData(str, "", false, false)
            return Every.call(pc, stringList as Object, udf)
        }
    }
}