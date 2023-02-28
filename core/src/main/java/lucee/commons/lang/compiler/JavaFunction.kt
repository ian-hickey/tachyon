package lucee.commons.lang.compiler

import lucee.runtime.PageSource

class JavaFunction(parent: PageSource, sourceCode: SourceCode, byteCode: ByteArray) {
    val byteCode: ByteArray
    val sourceCode: SourceCode
    private val parent: PageSource
    val name: String
        get() {
            val index: Int = sourceCode.getClassName().lastIndexOf('.')
            return if (index == -1) sourceCode.getClassName() else sourceCode.getClassName().substring(index + 1)
        }
    val `package`: String
        get() {
            val index: Int = sourceCode.getClassName().lastIndexOf('.')
            return if (index == -1) "" else sourceCode.getClassName().substring(0, index)
        }
    val className: String
        get() = sourceCode.getClassName()

    fun getParent(): PageSource {
        return parent
    } /*
	 * public String getTemplateName() { return templateName; }
	 */

    /*
	 * public void setTemplateName(String templateName) { this.templateName = templateName; }
	 */
    /*
	 * public String getFunctionName() { return functionName; }
	 */
    /*
	 * public void setFunctionName(String functionName) { this.functionName = functionName; }
	 */
    // private String templateName;
    // private String functionName;
    init {
        this.parent = parent
        this.sourceCode = sourceCode
        this.byteCode = byteCode
    }
}