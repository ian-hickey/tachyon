package tachyon.runtime.functions.string

import tachyon.runtime.PageContext

class StringReduce : BIF(), Function {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 2) {
            call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]), null)
        } else if (args.size == 3) {
            call(pc, Caster.toString(args[0]), Caster.toFunction(args[1]), args[2])
        } else {
            throw FunctionException(pc, "StringReduce", 2, 3, args.size)
        }
    }

    companion object {
        private const val serialVersionUID = -2153555241217815037L
        @Throws(PageException::class)
        fun call(pc: PageContext?, str: String?, udf: UDF?, initValue: Object?): Object? {
            val stringList = StringListData(str, "", false, false)
            return Reduce.call(pc, stringList as Object, udf, initValue)
        }
    }
}