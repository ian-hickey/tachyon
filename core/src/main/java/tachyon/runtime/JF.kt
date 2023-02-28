package tachyon.runtime

import java.util.HashSet

abstract class JF(functionName: String?, access: Int, modifier: Int, type: Int, strType: String?, hint: String?, output: Boolean?, bufferOutput: Boolean?, displayName: String?, description: String?,
                  returnFormat: Int, secureJson: Boolean?, verifyClient: Boolean?, localMode: Int) : UDF {
    private var pagesource: PageSource? = null
    private val props: UDFPropertiesImpl?

    /*
	 * public FunctionArgument[] getFunctionArguments() { return new FunctionArgument[] {
	 * 
	 * }; }
	 */
    @Override
    fun getFunctionName(): String? {
        return props.getFunctionName()
    }

    @Override
    fun getDescription(): String? {
        return props.getDescription()
    }

    @Override
    fun getHint(): String? {
        return props.getHint()
    }

    @Override
    fun getOwnerComponent(): Component? {
        return null
    }

    @Override
    fun getPageSource(): PageSource? {
        return pagesource
    }

    @Override
    fun getReturnTypeAsString(): String? {
        return props.getReturnTypeAsString()
    }

    @Override
    fun getReturnType(): Int {
        return props.getReturnType()
    }

    @Override
    @Throws(PageException::class)
    fun getMetaData(pc: PageContext?): Struct? {
        return ComponentUtil.getMetaData(pc, this, getUDFProperties(), null)
    }

    @Throws(PageException::class)
    private fun getUDFProperties(): UDFPropertiesImpl? {
        if (props.arguments == null) {
            props.arguments = getFunctionArguments()
            props.argumentsSet = HashSet<Key?>()
            for (arg in props.arguments) {
                props.argumentsSet.add(arg.getName())
            }
        }
        return props
    }

    @Override
    fun toDumpData(pc: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return UDFUtil.toDumpData(pc, maxlevel, dp, this, UDFUtil.TYPE_UDF)
    }

    @Override
    @Throws(Throwable::class)
    fun implementation(pc: PageContext?): Object? {
        return this
    }

    @Override
    fun duplicate(): UDF? {
        return try {
            ClassUtil.newInstance(this.getClass()) as UDF
        } catch (e: Exception) {
            this
        }
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, args: Struct?, b: Boolean): Object? {
        return null // TODO
    }

    @Override
    fun getReturnFormat(): Int {
        return props.getReturnFormat()
    }

    @Override
    fun getReturnFormat(df: Int): Int {
        // TODO
        return df
    }

    @Override
    fun id(): String? {
        return toString()
    }

    @Override
    fun getAccess(): Int {
        return props.getAccess()
    }

    @Override
    fun getModifier(): Int {
        return props.getModifier()
    }

    @Override
    fun getValue(): Object? {
        return this
    }

    @Override
    fun getIndex(): Int {
        return props.getIndex()
    }

    @Override
    @Throws(PageException::class)
    fun getDefaultValue(pc: PageContext?, index: Int): Object? {
        return null
    }

    @Override
    @Throws(PageException::class)
    fun getDefaultValue(pc: PageContext?, index: Int, df: Object?): Object? {
        return df
    }

    @Override
    fun getBufferOutput(pc: PageContext?): Boolean {
        return false
    }

    @Override
    fun getDisplayName(): String? {
        return props.getDisplayName()
    }

    @Override
    fun getSource(): String? {
        return if (getPageSource() != null) getPageSource().getDisplayPath() else ""
    }

    @Override
    fun getVerifyClient(): Boolean? {
        return props.getVerifyClient()
    }

    @Override
    fun getSecureJson(): Boolean? {
        return props.getSecureJson()
    }

    @Override
    fun getOutput(): Boolean {
        return props.getOutput()
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, calledName: Key?, args: Array<Object?>?, b: Boolean): Object? {
        return call(pc, args, b)
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, calledName: Key?, args: Struct?, b: Boolean): Object? {
        return callWithNamedValues(pc, args, b)
    }

    fun setPageSource(pageSource: PageSource?) {
        pagesource = pageSource
    }

    @Override
    abstract fun getFunctionArguments(): Array<FunctionArgument?>?

    companion object {
        private const val serialVersionUID = 3952006862868945777L
    }

    init {
        props = UDFPropertiesImpl()
        props.access = access
        props.modifier = modifier
        props.hint = hint
        props.functionName = functionName
        props.index = 0
        props.strReturnType = strType
        props.output = if (output == null) false else output.booleanValue()
        props.bufferOutput = bufferOutput
        props.displayName = displayName
        props.description = description
        props.returnFormat = returnFormat
        props.secureJson = secureJson
        props.verifyClient = verifyClient
        props.localMode = localMode
        props.cachedWithin = null // TODO
        props.strReturnFormat = UDFUtil.toReturnFormat(props.getReturnFormat(), "wddx")
        props.meta = null // TODO
    }
}