package tachyon.runtime.functions.query

import tachyon.runtime.PageContext

/**
 * Implements the CFML Function querynew
 */
class QueryIsEmpty : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 1) call(pc, Caster.toQuery(args[0])) else throw FunctionException(pc, "QueryIsEmpty", 1, 1, args.size)
    }

    companion object {
        fun call(pc: PageContext?, qry: Query?): Boolean {
            return qry.getRowCount() === 0
        }
    }
}