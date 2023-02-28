package tachyon.runtime.functions.string

import java.util.Iterator

class StringMap : BIF(), Function {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size < 1) {
            throw FunctionException(pc, "StringMap", 1, 2, args.size)
        }
        return call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]))
    }

    companion object {
        private const val serialVersionUID = 8643893144992203939L
        @Throws(PageException::class)
        fun call(pc: PageContext?, str: String?, udf: UDF?): String? {
            val stringList = StringListData(str, "", false, false)
            val array: ArrayImpl? = Map.call(pc, stringList, udf) as ArrayImpl?
            val it: Iterator = array.getIterator()
            val result = StringBuilder()
            while (it.hasNext()) {
                result.append(Caster.toString(it.next()))
            }
            return result.toString()
        }
    }
}