package lucee.runtime.functions.system

import lucee.runtime.PageContext

/**
 * returns the root of this current Page Context
 */
class GetContextInfo : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 0) return call(pc)
        throw FunctionException(pc, "GetContextInfo", 0, 0, args.size)
    }

    companion object {
        private const val serialVersionUID = 6287311028101499094L
        @Throws(PageException::class)
        fun call(pc: PageContext?): Struct? {
            val data: Struct = StructImpl()
            data.set("flushed", pc.getHttpServletResponse().isCommitted())
            return data
        }
    }
}