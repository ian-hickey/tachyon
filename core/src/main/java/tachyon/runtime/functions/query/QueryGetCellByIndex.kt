package tachyon.runtime.functions.query

import tachyon.runtime.PageContext

class QueryGetCellByIndex : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 3) return call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]), Caster.toString(args[2])) else if (args.size == 4) return call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), args[3])
        throw FunctionException(pc, "QueryGetCellByIndex", 3, 4, args.size)
    }

    companion object {
        private const val serialVersionUID = 2515614953776095300L
        val DF: Object? = Object()
        @Throws(PageException::class)
        fun call(pc: PageContext?, query: Query?, colName: String?, index: String?): Object? {
            return query.getAt(KeyImpl.init(colName), QueryRowByIndex.getIndex(query, index))
        }

        fun call(pc: PageContext?, query: Query?, colName: String?, index: String?, defaultValue: Object?): Object? {
            val indx: Int = QueryRowByIndex.getIndex(query, index, -1)
            if (indx == -1) return defaultValue
            val res: Object = query.getAt(KeyImpl.init(colName), indx, DF)
            return if (res === DF) defaultValue else res
        }
    }
}