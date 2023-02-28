package lucee.runtime.functions.arrays

import lucee.runtime.PageContext

class ArrayUnShift : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 2) call(pc, Caster.toArray(args[0]), args[1]) else throw FunctionException(pc, "ArrayUnShift", 2, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = 6952045964109881804L
        @Throws(PageException::class)
        fun call(pc: PageContext?, array: Array?, `object`: Object?): Double {
            // TODO make it atomar
            array.prepend(`object`)
            return array.size()
        }
    }
}