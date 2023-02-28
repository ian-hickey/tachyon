package tachyon.runtime.functions.dateTime

import tachyon.runtime.PageContext

class ClearTimeZone : BIF(), Function {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size != 0) throw FunctionException(pc, "ClearTimeZone", 0, 0, args.size)
        return call(pc)
    }

    companion object {
        private const val serialVersionUID = 2953112893625358220L
        fun call(pc: PageContext?): String? {
            (pc as PageContextImpl?).clearTimeZone()
            return null
        }
    }
}