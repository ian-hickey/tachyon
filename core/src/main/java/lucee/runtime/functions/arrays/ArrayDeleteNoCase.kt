package lucee.runtime.functions.arrays

import lucee.runtime.PageContext

class ArrayDeleteNoCase : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 2) ArrayDelete._call(pc, Caster.toArray(args[0]), args[1], null, false) else if (args.size == 3) ArrayDelete._call(pc, Caster.toArray(args[0]), args[1], Caster.toString(args[2]), false) else throw FunctionException(pc, "ArrayDeleteNoCase", 2, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = 1120923916196967210L
        @Throws(PageException::class)
        fun call(pc: PageContext?, array: Array?, value: Object?): Boolean {
            return ArrayDelete._call(pc, array, value, null, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, array: Array?, value: Object?, scope: String?): Boolean {
            return ArrayDelete._call(pc, array, value, scope, false)
        }
    }
}