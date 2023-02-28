package tachyon.runtime.functions.query

import tachyon.runtime.PageContext

class IsWithinTransaction : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 0) call(pc) else throw FunctionException(pc, "IsWithinTransaction", 0, 0, args.size)
    }

    companion object {
        private const val serialVersionUID = 7490842489165167839L
        fun call(pc: PageContext?): Boolean {
            return !pc.getDataSourceManager().isAutoCommit()
        }
    }
}