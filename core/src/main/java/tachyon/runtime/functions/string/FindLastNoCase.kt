package tachyon.runtime.functions.string

import tachyon.runtime.PageContext

class FindLastNoCase : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]))
        if (args.size == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toDoubleValue(args[2]))
        throw FunctionException(pc, "FindLastNoCase", 2, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = -5722812211523628009L
        fun call(pc: PageContext?, sub: String?, str: String?): Double {
            return FindLast.call(pc, sub.toLowerCase(), str.toLowerCase())
        }

        fun call(pc: PageContext?, sub: String?, str: String?, number: Double): Double {
            return FindLast.call(pc, sub.toLowerCase(), str.toLowerCase(), number)
        }
    }
}