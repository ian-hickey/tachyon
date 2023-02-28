package tachyon.runtime.functions.thread

import tachyon.runtime.PageContext

class RunAsync : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 2) call(pc, args[0], Caster.toDoubleValue(args[1])) else if (args.size == 1) call(pc, args[0], 0.0) else throw FunctionException(pc, "RunAsync", 1, 2, args.size)
    }

    companion object {
        /**
         * Verify if in thread or not
         *
         * @param pc
         * @return
         * @throws PageException
         */
        @Throws(PageException::class)
        fun call(pc: PageContext?, udf: Object?, timeout: Double): Object? {
            return Future._then(pc, Caster.toFunction(udf), timeout.toLong())
        }
    }
}