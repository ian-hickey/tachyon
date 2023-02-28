package tachyon.runtime.functions.query

import java.util.Map

/**
 * implements BIF QueryRowData
 */
class QueryRowByIndex : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toQuery(args[0]), Caster.toString(args[1])) else if (args.size == 3) return call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]), Caster.toDoubleValue(args[2]))
        throw FunctionException(pc, "QueryRowByIndex", 2, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = -1462555083727605910L
        @Throws(PageException::class)
        fun call(pc: PageContext?, query: Query?, index: String?): Double {
            return Caster.toDoubleValue(getIndex(query, index))
        }

        fun call(pc: PageContext?, query: Query?, index: String?, defaultValue: Double): Double {
            return Caster.toDoubleValue(getIndex(query, index, defaultValue.toInt()))
        }

        @Throws(ApplicationException::class)
        fun getIndex(query: Query?, index: String?): Int {
            val indexes: Map<Key?, Integer?> = (query as QueryImpl?).getIndexes()
                    ?: throw ApplicationException("Query is not indexed, index [$index] not found")
            return indexes[KeyImpl.getInstance(index)]
                    ?: throw ApplicationException("Query does not have an index for the column [$index]")
        }

        fun getIndex(query: Query?, index: String?, defaultValue: Int): Int {
            val indexes: Map<Key?, Integer?> = (query as QueryImpl?).getIndexes() ?: return defaultValue
            return indexes[KeyImpl.getInstance(index)] ?: return defaultValue
        }
    }
}