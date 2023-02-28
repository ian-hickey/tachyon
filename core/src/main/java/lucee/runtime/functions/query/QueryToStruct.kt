package lucee.runtime.functions.query

import lucee.runtime.PageContext

class QueryToStruct : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 2) call(pc, Caster.toQuery(args[0]), Caster.toString(args[1])) else if (args.size == 3) call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]), Caster.toString(args[2])) else if (args.size == 4) call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3])) else throw FunctionException(pc, "queryToStruct", 2, 4, args.size)
    }

    companion object {
        @Throws(PageException::class)
        fun call(pc: PageContext?, qry: Query?, columnKey: String?): Struct? {
            return call(pc, qry, columnKey, "ordered", false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, qry: Query?, columnKey: String?, structType: String?): Struct? {
            return call(pc, qry, columnKey, structType, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, qry: Query?, columnKey: String?, structType: String?, valueRowNumber: Boolean): Struct? {
            if (StringUtil.isEmpty(columnKey, true)) throw FunctionException(pc, "queryToStruct", 2, "columnKey", "columnKey cannot be a empty value")
            val sct: Struct = StructImpl(toType(pc, structType))
            val rows: Int = qry.getRecordcount()
            if (rows == 0) return sct
            val columns: Array<Key?> = qry.getColumnNames()
            for (r in 1..rows) {
                if (valueRowNumber) sct.set(Caster.toKey(qry.getAt(columnKey, r)), r) else {
                    val tmp: Struct = StructImpl()
                    sct.set(Caster.toKey(qry.getAt(columnKey, r)), tmp)
                    for (c in columns) {
                        tmp.setEL(c, qry.getAt(c, r, null))
                    }
                }
            }
            return sct
        }

        @Throws(PageException::class)
        fun toType(pc: PageContext?, type: String?): Int {
            var type = type
            type = type.toLowerCase()
            return if (type!!.equals("ordered")) Struct.TYPE_LINKED else if (type.equals("linked")) Struct.TYPE_LINKED else if (type.equals("weaked")) Struct.TYPE_WEAKED else if (type.equals("weak")) Struct.TYPE_WEAKED else if (type.equals("syncronized")) Struct.TYPE_SYNC else if (type.equals("synchronized")) Struct.TYPE_SYNC else if (type.equals("sync")) Struct.TYPE_SYNC else if (type.equals("soft")) Struct.TYPE_SOFT else if (type.equals("normal")) Struct.TYPE_REGULAR else if (type.equals("regular")) Struct.TYPE_REGULAR else throw FunctionException(pc, "queryToStruct", 3, "structType", "valid struct types are [normal, weak, linked, soft, synchronized]")
        }
    }
}