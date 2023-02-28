package tachyon.runtime.functions.string

import java.util.Iterator

class StringFilter : BIF(), Function {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size < 2) {
            throw FunctionException(pc, "StringFilter", 2, 2, args.size)
        }
        return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]))
    }

    companion object {
        private const val serialVersionUID = -3273443514000974993L
        @Throws(PageException::class)
        fun call(pc: PageContext?, str: String?, udf: UDF?): String? {
            val stringList = StringListData(str, "", false, false)
            val array: ArrayImpl = Filter.call(pc, stringList, udf) as ArrayImpl
            val it: Iterator = array.getIterator()
            val result = StringBuilder()
            while (it.hasNext()) {
                result.append(Caster.toString(it.next()))
            }
            return result.toString()
        }
    }
}