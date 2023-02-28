package tachyon.runtime.functions.query

import java.sql.SQLException

class QueryClose : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, Caster.toQuery(args[0]))
        throw FunctionException(pc, "QueryClose", 1, 1, args.size)
    }

    companion object {
        private const val serialVersionUID = 6778838386679577852L
        @Throws(PageException::class)
        fun call(pc: PageContext?, qry: Query?): Boolean {
            if (qry !is SimpleQuery) {
                throw FunctionException(pc, "queryClose", 1, "query", "you can only close lazy queries.")
            }
            try {
                if (!qry.isClosed()) {
                    qry.close()
                }
            } catch (e: SQLException) {
                // safe to ignore
            }
            return true
        }
    }
}