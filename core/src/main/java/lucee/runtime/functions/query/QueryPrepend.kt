package lucee.runtime.functions.query

import lucee.runtime.PageContext

class QueryPrepend : BIF(), Function {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toQuery(args[0]), Caster.toQuery(args[1]))
        throw FunctionException(pc, "QueryPrepend", 2, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = -5241509284480974613L
        @Throws(PageException::class)
        fun call(pc: PageContext?, qry1: Query?, qry2: Query?): Query? {
            // compare column names
            val cn1: Array<Key?> = qry1.getColumnNames()
            val cn2: Array<Key?> = qry2.getColumnNames()
            QueryAppend.validate(qry1, cn1, cn2)
            val rowCount2: Int = qry2.getRowCount()
            if (rowCount2 == 0) return qry1
            makeSpace(qry1, rowCount2, 0)
            for (row in rowCount2 downTo 1) {
                for (k in cn2) {
                    qry1.setAt(k, row, qry2.getAt(k, row))
                }
            }
            return qry1
        }

        @Throws(PageException::class)
        fun makeSpace(qry: Query?, makeSpaceFor: Int, offset: Int) {
            val columns: Array<Key?> = qry.getColumnNames()
            val rowCount: Int = qry.getRowCount()
            // add rows needed
            qry.addRow(makeSpaceFor)

            // move records "down"
            for (row in rowCount downTo offset + 1) {
                for (k in columns) {
                    qry.setAt(k, makeSpaceFor + row, qry.getAt(k, row))
                }
            }
        }
    }
}