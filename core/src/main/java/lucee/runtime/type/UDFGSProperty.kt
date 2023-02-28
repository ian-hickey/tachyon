/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 *
 */
package lucee.runtime.type

import java.security.NoSuchAlgorithmException

abstract class UDFGSProperty(component: Component?, name: String?, arguments: Array<FunctionArgument?>?, rtnType: Short) : MemberSupport(Component.ACCESS_PUBLIC), UDFPlus {
    protected val arguments: Array<FunctionArgument?>?
    protected val name: String?
    protected var srcComponent: Component?
    private val properties: UDFPropertiesBase?
    private var id: String? = null
    @Override
    fun getFunctionArguments(): Array<FunctionArgument?>? {
        return arguments
    }

    @Override
    fun getFunctionName(): String? {
        return name
    }

    /*
	 * @Override public PageSource getPageSource() { return component.getPageSource(); }
	 */
    @Override
    override fun equals(other: Object?): Boolean {
        return if (other !is UDF) false else UDFImpl.equals(this, other as UDF?)
    }

    @Override
    fun getSource(): String? {
        val ps: PageSource = srcComponent.getPageSource()
        return if (ps != null) ps.getDisplayPath() else ""
    }

    @Override
    fun id(): String? {
        if (id == null) {
            id = try {
                Hash.md5(srcComponent.id().toString() + ":" + getFunctionName())
            } catch (e: NoSuchAlgorithmException) {
                srcComponent.id().toString() + ":" + getFunctionName()
            }
        }
        return id
    }

    @Override
    fun getIndex(): Int {
        return -1
    }

    @Override
    fun getOwnerComponent(): Component? {
        return getOwnerComponent(null)
    }

    fun getOwnerComponent(pc: PageContext?): Component? {
        return srcComponent
    }

    @Override
    override fun setOwnerComponent(component: Component?) {
        srcComponent = component
    }

    fun getPage(): Page? {
        throw PageRuntimeException(DeprecatedException("method getPage():Page is no longer suppoted, use instead getPageSource():PageSource"))
    }

    @Override
    fun getOutput(): Boolean {
        return false
    }

    @Override
    fun duplicate(deep: Boolean): UDF? {
        val udf: UDF? = duplicate() // deep has no influence here, because a UDF is not a collection
        if (udf is UDFPlus) {
            val udfp: UDFPlus? = udf
            udfp!!.setOwnerComponent(srcComponent)
            udfp!!.setAccess(getAccess())
        }
        return udf
    }

    @Override
    fun getDisplayName(): String? {
        return ""
    }

    @Override
    fun getDescription(): String? {
        return ""
    }

    @Override
    fun getHint(): String? {
        return ""
    }

    @Override
    fun getReturnFormat(): Int {
        return UDF.RETURN_FORMAT_WDDX
    }

    @Override
    fun getReturnFormat(defaultValue: Int): Int {
        return defaultValue
    }

    @Override
    fun getReturnType(): Int {
        return CFTypes.toShortStrict(getReturnTypeAsString(), CFTypes.TYPE_UNKNOW)
    }

    @Override
    fun getValue(): Object? {
        return this
    }

    @Override
    fun getSecureJson(): Boolean? {
        return null
    }

    @Override
    fun getVerifyClient(): Boolean? {
        return null
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, properties: DumpProperties?): DumpData? {
        return UDFUtil.toDumpData(pageContext, maxlevel, properties, this, UDFUtil.TYPE_UDF)
    }

    @Override
    @Throws(PageException::class)
    fun getMetaData(pc: PageContext?): Struct? {
        return ComponentUtil.getMetaData(pc, properties, null)
    }

    @Throws(PageException::class)
    fun cast(pc: PageContext?, arg: FunctionArgument?, value: Object?, index: Int): Object? {
        if (value == null || Decision.isCastableTo(pc, arg.getType(), arg.getTypeAsString(), value)) return value
        throw UDFCasterException(this, arg, value, index)
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, calledName: Key?, values: Struct?, doIncludePath: Boolean): Object? {
        val pci: PageContextImpl? = pc as PageContextImpl?
        val parent: UDF = pc.getActiveUDF()
        val parentName: Key = pci.getActiveUDFCalledName()
        pci.setActiveUDF(this)
        pci.setActiveUDFCalledName(calledName)
        return try {
            callWithNamedValues(pci, values, doIncludePath)
        } finally {
            pci.setActiveUDF(parent)
            pci.setActiveUDFCalledName(parentName)
        }
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, calledName: Key?, args: Array<Object?>?, doIncludePath: Boolean): Object? {
        val pci: PageContextImpl? = pc as PageContextImpl?
        val parent: UDF = pc.getActiveUDF()
        val parentName: Key = pci.getActiveUDFCalledName()
        pci.setActiveUDFCalledName(calledName)
        pci.setActiveUDF(this)
        return try {
            call(pci, args, doIncludePath)
        } finally {
            pci.setActiveUDF(parent)
            pci.setActiveUDFCalledName(parentName)
        }
    }

    @Override
    @Throws(PageException::class)
    fun call(pageContext: PageContext?, args: Array<Object?>?, doIncludePath: Boolean): Object? {
        val pci: PageContextImpl? = pageContext as PageContextImpl?
        val parent: UDF = pci.getActiveUDF()
        pci.setActiveUDF(this)
        return try {
            _call(pageContext, args, doIncludePath)
        } finally {
            pci.setActiveUDF(parent)
        }
    }

    @Throws(PageException::class)
    abstract fun _call(pageContext: PageContext?, args: Array<Object?>?, doIncludePath: Boolean): Object?
    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pageContext: PageContext?, values: Struct?, doIncludePath: Boolean): Object? {
        val pci: PageContextImpl? = pageContext as PageContextImpl?
        val parent: UDF = pci.getActiveUDF()
        pci.setActiveUDF(this)
        return try {
            _callWithNamedValues(pageContext, values, doIncludePath)
        } finally {
            pci.setActiveUDF(parent)
        }
    }

    @Throws(PageException::class)
    abstract fun _callWithNamedValues(pageContext: PageContext?, values: Struct?, doIncludePath: Boolean): Object?
    @Override
    fun getPageSource(): PageSource? {
        return srcComponent.getPageSource()
        // return this.properties.getPageSource();
    }

    @Override
    fun getBufferOutput(pc: PageContext?): Boolean {
        return pc.getApplicationContext().getBufferOutput()
    }

    fun getComponent(pc: PageContext?): Component? {
        var pc: PageContext? = pc
        if (pc == null) pc = ThreadLocalPageContext.get()
        if (pc != null) {
            val `var`: Variables = pc.variablesScope()
            if (`var` is ComponentScope) {
                val comp: Component = (`var` as ComponentScope).getComponent()
                if (comp != null) return comp
            }
        }
        return srcComponent
    }

    companion object {
        private const val serialVersionUID = 285652503901488683L
        private val MIN_LENGTH: Collection.Key? = KeyImpl.getInstance("minLength")
        private val MAX_LENGTH: Collection.Key? = KeyImpl.getInstance("maxLength")
        private fun UDFProperties(page: Page?, pageSource: PageSource?, arguments: Array<FunctionArgument?>?, functionName: String?, returnType: Short): UDFPropertiesBase? {
            return UDFPropertiesLight(page, pageSource, arguments, functionName, returnType)
        }

        @Throws(PageException::class)
        fun validate(validate: String?, validateParams: Struct?, obj: Object?) {
            var validate = validate
            if (StringUtil.isEmpty(validate, true)) return
            validate = validate.trim().toLowerCase()
            if (!validate.equals("regex") && !Decision.isValid(validate, obj)) throw ExpressionException(createMessage(validate, obj))

            // range
            if (validateParams == null) return
            if (validate.equals("integer") || validate.equals("numeric") || validate.equals("number")) {
                val min: Double = Caster.toDoubleValue(validateParams.get(KeyConstants._min, null), false, Double.NaN)
                val max: Double = Caster.toDoubleValue(validateParams.get(KeyConstants._max, null), false, Double.NaN)
                val d: Double = Caster.toDoubleValue(obj)
                if (!Double.isNaN(min) && d < min) throw ExpressionException(validate.toString() + " [" + Caster.toString(d) + "] is out of range, value must be more than or equal to [" + min + "]")
                if (!Double.isNaN(max) && d > max) throw ExpressionException(validate.toString() + " [" + Caster.toString(d) + "] is out of range, value must be less than or equal to [" + max + "]")
            } else if (validate.equals("string")) {
                val min: Double = Caster.toDoubleValue(validateParams.get(MIN_LENGTH, null), false, Double.NaN)
                val max: Double = Caster.toDoubleValue(validateParams.get(MAX_LENGTH, null), false, Double.NaN)
                val str: String = Caster.toString(obj)
                val l: Int = str.length()
                if (!Double.isNaN(min) && l < min.toInt()) throw ExpressionException("string [$str] is to short [$l], the string must be at least [$min] characters")
                if (!Double.isNaN(max) && l > max.toInt()) throw ExpressionException("string [$str] is to long [$l], the string can have a maximum length of [$max] characters")
            } else if (validate.equals("regex")) {
                val pattern: String = Caster.toString(validateParams.get(KeyConstants._pattern, null), null)
                val value: String = Caster.toString(obj)
                if (!StringUtil.isEmpty(pattern, true) && !IsValid.regex(ThreadLocalPageContext.get(), value, pattern)) throw ExpressionException("the string [$value] does not match the regular expression pattern [$pattern]")
            }
        }

        private fun createMessage(format: String?, value: Object?): String? {
            return if (Decision.isSimpleValue(value)) "the value [" + Caster.toString(value, null).toString() + "] is not in  [" + format.toString() + "] format" else "cannot convert object from type [" + Caster.toTypeName(value).toString() + "] to a [" + format.toString() + "] format"
        }
    }

    init {
        properties = UDFProperties(null, component.getPageSource(), arguments, name, rtnType)
        this.name = name
        this.arguments = arguments
        srcComponent = component
    }
}