package tachyon.runtime.functions.query

import tachyon.runtime.PageContext

class QueryInsertAt : BIF(), Function {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 3) return call(pc, Caster.toQuery(args[0]), args[1], Caster.toDoubleValue(args[2]))
        throw FunctionException(pc, "QueryInsertAt", 3, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = -2549767593942513005L
        @Throws(PageException::class)
        fun call(pc: PageContext?, qry: Query?, value: Object?, index: Double): Query? {
            if (index < 1) throw FunctionException(pc, "QueryInsertAt", 3, "index", "index most be at least one, now it is [" + Caster.toString(index).toString() + "].")
            if (index - 1 > qry.getRowCount()) throw FunctionException(pc, "QueryInsertAt", 3, "index", "index [" + Caster.toString(index).toString() + "] cannot be bigger than recordcount [" + qry.getRecordcount().toString() + "] of the query plus 1.")
            val off = (index - 1).toInt()

            // QUERY
            if (Decision.isQuery(value)) {
                val qry2: Query? = value as Query?
                val cn1: Array<Key?> = qry.getColumnNames()
                val cn2: Array<Key?> = qry2.getColumnNames()
                QueryAppend.validate(qry, cn1, cn2)
                val rowCount2: Int = qry2.getRowCount()
                if (rowCount2 == 0) return qry
                QueryPrepend.makeSpace(qry, rowCount2, off)
                for (row in rowCount2 downTo 1) {
                    for (k in cn2) {
                        qry.setAt(k, row + off, qry2.getAt(k, row))
                    }
                }
            } else if (Decision.isStruct(value)) {
                val sct: Struct? = value as Struct?
                val cn1: Array<Key?> = qry.getColumnNames()
                val cn2: Array<Key?> = sct.keys()
                if (cn1.size != cn2.size) {
                    throw ApplicationException("query column count [" + cn1.size + "] and struct size [" + cn2.size + "] are not same")
                }
                for (k in cn2) {
                    if (qry.getColumn(k, null) == null) {
                        throw ApplicationException("column names [" + ListUtil.arrayToList(cn1, ", ").toString() + "] of the query does not match the keys ["
                                + ListUtil.arrayToList(cn2, ", ").toString() + "] of the struct")
                    }
                }
                QueryPrepend.makeSpace(qry, 1, off)
                for (row in 1 downTo 1) {
                    for (k in cn2) {
                        qry.setAt(k, row + off, sct.get(k))
                    }
                }
            } else if (Decision.isArray(value)) {
                val arr: Array? = value
                val cn1: Array<Key?> = qry.getColumnNames()
                if (cn1.size != arr.size()) {
                    throw ApplicationException("there is not the same amount of records in the array [" + arr.size().toString() + "] as there are columns in the query [" + cn1.size.toString() + "].")
                }
                QueryPrepend.makeSpace(qry, 1, off)
                for (row in 1 downTo 1) {
                    for (col in cn1.indices) {
                        qry.setAt(cn1[col], row + off, arr.getE(col + 1))
                    }
                }
            }
            return qry
        }
    }
}