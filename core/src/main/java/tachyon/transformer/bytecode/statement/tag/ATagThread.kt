package tachyon.transformer.bytecode.statement.tag

import tachyon.transformer.bytecode.Body

interface ATagThread {
    fun getRealBody(): Body?
}