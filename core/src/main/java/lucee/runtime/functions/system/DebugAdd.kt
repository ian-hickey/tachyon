package lucee.runtime.functions.system

import java.util.HashMap

class DebugAdd : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toString(args[0]), Caster.toStruct(args[1]))
        throw FunctionException(pc, "DebugAdd", 2, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = 3480038887443615199L
        @Throws(PageException::class)
        fun call(pc: PageContext?, category: String?, data: Struct?): String? {
            val debugger: Debugger = pc.getDebugger()
            debugger.addGenericData(category, toMapStrStr(data))
            return null
        }

        @Throws(PageException::class)
        private fun toMapStrStr(struct: Struct?): Map<String?, String?>? {
            val it: Iterator<Entry<Key?, Object?>?> = struct.entryIterator()
            val map: Map<String?, String?> = HashMap<String?, String?>()
            var e: Entry<Key?, Object?>?
            while (it.hasNext()) {
                e = it.next()
                map.put(e.getKey().getString(), Caster.toString(e.getValue()))
            }
            return map
        }
    }
}