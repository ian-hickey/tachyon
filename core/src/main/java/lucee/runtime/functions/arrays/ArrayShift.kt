package lucee.runtime.functions.arrays

import lucee.runtime.PageContext

class ArrayShift : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, Caster.toArray(args[0]))
        return if (args.size == 2) call(pc, Caster.toArray(args[0]), args[1]) else throw FunctionException(pc, "ArrayShift", 1, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = -9214780740665463790L
        @Throws(PageException::class)
        fun call(pc: PageContext?, array: Array?): Object? {
            return ArrayUtil.toArrayPro(array).shift()
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, array: Array?, defaultValue: Object?): Object? {
            return ArrayUtil.toArrayPro(array).shift(defaultValue)
        }
    }
}