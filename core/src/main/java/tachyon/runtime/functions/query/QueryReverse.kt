package tachyon.runtime.functions.query

import tachyon.runtime.PageContext

class QueryReverse : BIF(), Function {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, Caster.toQuery(args[0]))
        throw FunctionException(pc, "QueryReverse", 1, 1, args.size)
    }

    companion object {
        private const val serialVersionUID = -91336674628990980L
        @Throws(PageException::class)
        fun call(pc: PageContext?, qry: Query?): Query? {
            val names: Array<Key?> = qry.getColumnNames()
            val rq = QueryImpl(names, qry.getRecordcount(), qry.getName())
            var newRow = 0
            for (row in qry.getRecordcount() downTo 1) {
                newRow++
                for (name in names) {
                    rq.setAt(name, newRow, qry.getAt(name, row))
                }
            }
            return rq
        }
    }
}