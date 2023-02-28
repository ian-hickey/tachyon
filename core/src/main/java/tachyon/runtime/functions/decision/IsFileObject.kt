package tachyon.runtime.functions.decision

import tachyon.runtime.PageContext

object IsFileObject : Function {
    fun call(pc: PageContext?, source: Object?): Boolean {
        return Decision.isFileObject(source)
    }
}