package tachyon.runtime.type.scope

import tachyon.runtime.PageContext

class CFThread : StructImpl() {
    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, properties: DumpProperties?): DumpData? {
        val dd: DumpData = super.toDumpData(pageContext, maxlevel, properties)
        if (dd is DumpTable) { // always the case ...
            val dt: DumpTable = dd as DumpTable
            dt.setTitle("Scope CFThread")
            dt.setComment(
                    "CFthread only provides the direct children of the current thread, to get all threads (parent and sister threads) use the function [threadData] instead.")
        }
        return dd
    }
}