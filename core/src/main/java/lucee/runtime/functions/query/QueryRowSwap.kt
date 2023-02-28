package lucee.runtime.functions.query

import lucee.runtime.PageContext

class QueryRowSwap : BIF(), Function {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 3) return call(pc, Caster.toQuery(args[0]), Caster.toDoubleValue(args[1]), Caster.toDoubleValue(args[2]))
        throw FunctionException(pc, "QueryRowSwap", 3, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = -812740090032092109L
        @Throws(PageException::class)
        fun call(pc: PageContext?, qry: Query?, source: Double, destination: Double): Query? {
            // validate source
            if (source < 1) throw FunctionException(pc, "QueryRowSwap", 2, "source", "source most be at least one, now it is [" + Caster.toString(source).toString() + "].")
            if (source > qry.getRowCount()) throw FunctionException(pc, "QueryRowSwap", 2, "source", "source [" + Caster.toString(source).toString() + "] cannot be bigger than recordcount [" + qry.getRecordcount().toString() + "] of the query.")
            val src = source.toInt()

            // validate destination
            if (destination < 1) throw FunctionException(pc, "QueryRowSwap", 3, "destination", "destination most be at least one, now it is [" + Caster.toString(destination).toString() + "].")
            if (destination > qry.getRowCount()) throw FunctionException(pc, "QueryRowSwap", 3, "destination", "destination [" + Caster.toString(destination).toString() + "] cannot be bigger than recordcount [" + qry.getRecordcount().toString() + "] of the query.")
            val dest = destination.toInt()
            val colNames: Array<Collection.Key?> = qry.getColumnNames()

            // temp copy of dest
            val tmp: Struct = StructImpl()
            for (cn in colNames) {
                tmp.set(cn, qry.getAt(cn, dest))
            }

            // write source to dest
            for (cn in colNames) {
                qry.setAt(cn, dest, qry.getAt(cn, src))
            }

            // write tmp to src
            for (cn in colNames) {
                qry.setAt(cn, src, tmp.get(cn))
            }
            return qry
        }
    }
}