package lucee.runtime.functions.string

import lucee.runtime.PageContext

class StringSome : BIF(), Function {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size < 1 || args.size > 2) {
            throw FunctionException(pc, "StringSome", 1, 2, args.size)
        }
        return if (args.size == 2) {
            call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]))
        } else call(pc, Caster.toString(args[0]), null)
    }

    companion object {
        private const val serialVersionUID = 4167438066376325970L
        @Throws(PageException::class)
        fun call(pc: PageContext?, str: String?, udf: UDF?): Boolean {
            val stringList = StringListData(str, "", false, false)
            return Some.call(pc, stringList as Object, udf)
        }
    }
}