package lucee.runtime.functions.query

import lucee.runtime.PageContext

class QueryClear : BIF(), Function {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, Caster.toQuery(args[0]))
        throw FunctionException(pc, "QueryClear", 1, 1, args.size)
    }

    companion object {
        private const val serialVersionUID = 3755794610970965992L
        @Throws(PageException::class)
        fun call(pc: PageContext?, qry: Query?): Query? {
            qry.clear()
            return qry
        }
    }
}