package tachyon.runtime.functions.query

import tachyon.runtime.PageContext

/**
 * implements BIF QueryRowData
 */
class QueryRowDataByIndex : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toQuery(args[0]), Caster.toString(args[1])) else if (args.size == 3) return call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]), Caster.toStruct(args[2]))
        throw FunctionException(pc, "QueryRowDataByIndex", 2, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = -3492163362858443357L
        @Throws(PageException::class)
        fun call(pc: PageContext?, query: Query?, index: String?): Struct? {
            val row: Int = QueryRowByIndex.getIndex(query, index)
            val colNames: Array<Collection.Key?> = query.getColumnNames()
            val result: Struct = StructImpl()
            for (col in colNames.indices) result.setEL(colNames[col], query.getAt(colNames[col], row, NullSupportHelper.empty(pc)))
            return result
        }

        fun call(pc: PageContext?, query: Query?, index: String?, defaultValue: Struct?): Struct? {
            val row: Int = QueryRowByIndex.getIndex(query, index, -1)
            if (row == -1) return defaultValue
            val colNames: Array<Collection.Key?> = query.getColumnNames()
            val result: Struct = StructImpl()
            for (col in colNames.indices) result.setEL(colNames[col], query.getAt(colNames[col], row, NullSupportHelper.empty(pc)))
            return result
        }
    }
}