package lucee.transformer.bytecode.statement.tag

import lucee.transformer.bytecode.Body

interface ATagThread {
    fun getRealBody(): Body?
}