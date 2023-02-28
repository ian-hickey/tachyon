package tachyon.runtime.functions.string

import tachyon.runtime.PageContext

class FindLast : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]))
        if (args.size == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toDoubleValue(args[2]))
        throw FunctionException(pc, "FindLast", 2, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = -176191593295823013L
        fun call(pc: PageContext?, sub: String?, str: String?): Double {
            return str.lastIndexOf(sub) + 1
        }

        fun call(pc: PageContext?, sub: String?, str: String?, number: Double): Double {
            return if (sub!!.length() === 0) number.toInt() else str.lastIndexOf(sub, number.toInt() - 1) + 1
        }
    }
}