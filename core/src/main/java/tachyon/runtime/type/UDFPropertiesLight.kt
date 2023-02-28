package tachyon.runtime.type

import java.io.IOException

class UDFPropertiesLight(page: Page?, pageSource: PageSource?, arguments: Array<FunctionArgument?>?, functionName: String?, returnType: Short) : UDFPropertiesBase(page, pageSource, 0, 0) {
    private val arguments: Array<FunctionArgument?>?
    private val functionName: String?
    private val returnType: Short
    private var argumentsSet: HashSet<Key?>? = null
    @Override
    @Throws(IOException::class)
    fun writeExternal(out: ObjectOutput?) {
        // TODO Auto-generated method stub
    }

    @Override
    @Throws(IOException::class, ClassNotFoundException::class)
    fun readExternal(`in`: ObjectInput?) {
        // TODO Auto-generated method stub
    }

    @Override
    fun getAccess(): Int {
        return Component.ACCESS_PUBLIC
    }

    @Override
    fun getModifier(): Int {
        return Component.MODIFIER_NONE
    }

    @Override
    override fun getFunctionName(): String? {
        return functionName
    }

    @Override
    override fun getOutput(): Boolean {
        return false
    }

    @Override
    override fun getBufferOutput(): Boolean? {
        return Boolean.TRUE
    }

    @Override
    override fun getReturnType(): Int {
        return returnType.toInt()
    }

    @Override
    override fun getReturnTypeAsString(): String? {
        return CFTypes.toString(returnType, "any")
    }

    @Override
    override fun getDescription(): String? {
        return ""
    }

    @Override
    override fun getReturnFormat(): Int {
        return UDF.RETURN_FORMAT_WDDX
    }

    @Override
    override fun getReturnFormatAsString(): String? {
        return "wddx"
    }

    @Override
    override fun getIndex(): Int {
        return -1
    }

    @Override
    override fun getCachedWithin(): Object? {
        return Constants.LONG_ZERO
    }

    @Override
    override fun getSecureJson(): Boolean? {
        return Boolean.FALSE
    }

    @Override
    override fun getVerifyClient(): Boolean? {
        return Boolean.FALSE
    }

    @Override
    override fun getFunctionArguments(): Array<FunctionArgument?>? {
        return arguments
    }

    @Override
    override fun getDisplayName(): String? {
        return ""
    }

    @Override
    override fun getHint(): String? {
        return ""
    }

    @Override
    override fun getMeta(): Struct? {
        return null
    }

    @Override
    override fun getLocalMode(): Integer? {
        return null
    }

    @Override
    override fun getArgumentsSet(): Set<Key?>? {
        if (arguments != null && arguments.size > 0) {
            argumentsSet = HashSet<Collection.Key?>()
            for (i in arguments.indices) {
                argumentsSet.add(arguments[i].getName())
            }
        }
        return argumentsSet
    }

    init {
        this.arguments = arguments
        this.functionName = functionName
        this.returnType = returnType
    }
}