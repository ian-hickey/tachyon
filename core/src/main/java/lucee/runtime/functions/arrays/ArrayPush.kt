package lucee.runtime.functions.arrays

import lucee.runtime.PageContext

class ArrayPush : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 2) call(pc, Caster.toArray(args[0]), args[1]) else throw FunctionException(pc, "ArrayPush", 2, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = -5673140457325547233L
        @Throws(PageException::class)
        fun call(pc: PageContext?, array: Array?, `object`: Object?): Double {
            // TODO need to be atomic
            array.append(`object`)
            return array.size()
        }
    }
}