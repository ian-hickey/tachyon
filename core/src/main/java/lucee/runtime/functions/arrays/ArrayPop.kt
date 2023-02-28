package lucee.runtime.functions.arrays

import lucee.runtime.PageContext

class ArrayPop : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, Caster.toArray(args[0]))
        return if (args.size == 2) call(pc, Caster.toArray(args[0]), args[1]) else throw FunctionException(pc, "ArrayPop", 1, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = -5628212614796853287L
        @Throws(PageException::class)
        fun call(pc: PageContext?, array: Array?): Object? {
            return ArrayUtil.toArrayPro(array).pop()
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, array: Array?, defaultValue: Object?): Object? {
            return ArrayUtil.toArrayPro(array).pop(defaultValue)
        }
    }
}