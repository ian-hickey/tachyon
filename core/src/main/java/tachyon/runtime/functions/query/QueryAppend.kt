package tachyon.runtime.functions.query

import tachyon.runtime.PageContext

class QueryAppend : BIF(), Function {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toQuery(args[0]), Caster.toQuery(args[1]))
        throw FunctionException(pc, "QueryAppend", 2, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = 5814257234774888827L
        @Throws(PageException::class)
        fun call(pc: PageContext?, qry1: Query?, qry2: Query?): Query? {
            // compare column names
            val cn1: Array<Key?> = qry1.getColumnNames()
            val cn2: Array<Key?> = qry2.getColumnNames()
            validate(qry1, cn1, cn2)
            val rowCount1: Int = qry1.getRowCount()
            val rowCount2: Int = qry2.getRowCount()
            if (rowCount2 == 0) return qry1
            qry1.addRow(rowCount2)
            for (row in 1..rowCount2) {
                for (k in cn2) {
                    qry1.setAt(k, rowCount1 + row, qry2.getAt(k, row))
                }
            }
            return qry1
        }

        @Throws(ApplicationException::class)
        fun validate(qry1: Query?, cn1: Array<Key?>?, cn2: Array<Key?>?) {
            var validColumnNames = cn1!!.size == cn2!!.size
            if (validColumnNames) {
                for (k in cn2) {
                    if (qry1.getColumn(k, null) == null) {
                        validColumnNames = false
                        break
                    }
                }
            }
            if (!validColumnNames) {
                throw ApplicationException("column names [" + ListUtil.arrayToList(cn1, ", ").toString() + "] of the first query does not match the column names ["
                        + ListUtil.arrayToList(cn2, ", ").toString() + "] of the second query")
            }
        }
    }
}