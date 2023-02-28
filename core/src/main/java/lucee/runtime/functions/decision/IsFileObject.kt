package lucee.runtime.functions.decision

import lucee.runtime.PageContext

object IsFileObject : Function {
    fun call(pc: PageContext?, source: Object?): Boolean {
        return Decision.isFileObject(source)
    }
}