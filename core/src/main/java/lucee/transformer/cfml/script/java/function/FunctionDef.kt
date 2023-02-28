package lucee.transformer.cfml.script.java.function

import java.util.List

interface FunctionDef {
    fun createSourceCode(ps: PageSource?, javaCode: String?, id: String?, funcName: String?, access: Int, modifier: Int, hint: String?, args: List<Argument?>?, output: Boolean?,
                         bufferOutput: Boolean?, displayName: String?, description: String?, returnFormat: Int, secureJson: Boolean?, verifyClient: Boolean?, localMode: Int): SourceCode?
}