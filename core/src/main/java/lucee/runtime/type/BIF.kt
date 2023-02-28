/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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

class BIF : MemberSupport, UDFPlus {
    private val flf: FunctionLibFunction?
    private var rtnType: Short = CFTypes.TYPE_UNKNOW
    private var owner: Component? = null
    private val cp: ConfigPro?
    private var args: Array<FunctionArgument?>?
    private var id: String? = null

    constructor(pc: PageContext?, name: String?) : super(Component.ACCESS_PUBLIC) {
        cp = pc.getConfig() as ConfigPro
        val fl: FunctionLib = cp.getCombinedFLDs(pc.getCurrentTemplateDialect())
        flf = fl.getFunction(name)

        // BIF not found
        if (flf == null) {
            val keys: Array<Key?> = CollectionUtil.toKeys(fl.getFunctions().keySet())
            val msg: String = ExceptionUtil.similarKeyMessage(keys, name, "Built in function", "Built in functions", null, false)
            val detail: String = ExceptionUtil.similarKeyMessage(keys, name, "Built in functions", null, false)
            throw ApplicationException(msg, detail)
        }
        try {
            id = Hash.md5(name)
        } catch (e: NoSuchAlgorithmException) {
            id = name
        }
    }

    constructor(config: Config?, flf: FunctionLibFunction?) : super(Component.ACCESS_PUBLIC) {
        cp = config as ConfigPro?
        this.flf = flf
    }

    @Override
    fun getFunctionArguments(): Array<FunctionArgument?>? {
        if (args == null) {
            val src: ArrayList<FunctionLibFunctionArg?> = flf.getArg()
            args = arrayOfNulls<FunctionArgument?>(src.size())
            var def: String
            var index = -1
            var arg: FunctionLibFunctionArg
            val it: Iterator<FunctionLibFunctionArg?> = src.iterator()
            while (it.hasNext()) {
                arg = it.next()
                def = arg.getDefaultValue()
                args!![++index] = FunctionArgumentImpl(KeyImpl.init(arg.getName()), arg.getTypeAsString(), arg.getType(), arg.getRequired(),
                        if (def == null) FunctionArgument.DEFAULT_TYPE_NULL else FunctionArgument.DEFAULT_TYPE_LITERAL, true, arg.getName(), arg.getDescription(), null)
            }
        }
        return args
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pageContext: PageContext?, values: Struct?, doIncludePath: Boolean): Object? {
        val flfas: ArrayList<FunctionLibFunctionArg?> = flf.getArg()
        val it: Iterator<FunctionLibFunctionArg?> = flfas.iterator()
        var arg: FunctionLibFunctionArg?
        var `val`: Object
        val refs: List<Ref?> = ArrayList<Ref?>()
        while (it.hasNext()) {
            arg = it.next()

            // match by name
            `val` = values.get(arg.getName(), null)

            // match by alias
            if (`val` == null) {
                val alias: String = arg.getAlias()
                if (!StringUtil.isEmpty(alias, true)) {
                    val aliases: Array<String?> = lucee.runtime.type.util.ListUtil.trimItems(lucee.runtime.type.util.ListUtil.listToStringArray(alias, ','))
                    for (x in aliases.indices) {
                        `val` = values.get(aliases[x], null)
                        if (`val` != null) break
                    }
                }
            }
            if (`val` == null) {
                if (arg.getRequired()) {
                    val names: Array<String?> = flf.getMemberNames()
                    val n = if (ArrayUtil.isEmpty(names)) "" else names[0]
                    throw ExpressionException("Missing required argument [" + arg.getName().toString() + "] for built in function call [" + n.toString() + "]")
                }
            } else {
                refs.add(Casting(arg.getTypeAsString(), arg.getType(), LFunctionValue(LString(arg.getName()), `val`)))
            }
        }
        val call = BIFCall(flf, refs.toArray(arrayOfNulls<Ref?>(refs.size())))
        return call.getValue(pageContext)
    }

    @Override
    @Throws(PageException::class)
    fun call(pageContext: PageContext?, args: Array<Object?>?, doIncludePath: Boolean): Object? {
        val flfas: ArrayList<FunctionLibFunctionArg?> = flf.getArg()
        var flfa: FunctionLibFunctionArg
        val refs: List<Ref?> = ArrayList<Ref?>()
        for (i in args.indices) {
            if (i >= flfas.size()) throw ApplicationException("Too many Attributes in function call [" + flf.getName().toString() + "]")
            flfa = flfas.get(i)
            refs.add(Casting(flfa.getTypeAsString(), flfa.getType(), args!![i]))
        }
        val call = BIFCall(flf, refs.toArray(arrayOfNulls<Ref?>(refs.size())))
        return call.getValue(pageContext)
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pageContext: PageContext?, calledName: Key?, values: Struct?, doIncludePath: Boolean): Object? {
        return callWithNamedValues(pageContext, values, doIncludePath)
    }

    @Override
    @Throws(PageException::class)
    fun call(pageContext: PageContext?, calledName: Key?, args: Array<Object?>?, doIncludePath: Boolean): Object? {
        return call(pageContext, args, doIncludePath)
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        // dt.setTitle(title);
        return UDFUtil.toDumpData(pageContext, maxlevel, dp, this, UDFUtil.TYPE_BIF) as DumpTable
    }

    @Override
    fun duplicate(): UDF? {
        return BIF(cp, flf)
    }

    @Override
    fun duplicate(deepCopy: Boolean): Object? {
        return duplicate()
    }

    @Override
    fun getOwnerComponent(): Component? {
        return owner
    }

    @Override
    fun getDisplayName(): String? {
        return flf.getName()
    }

    @Override
    fun getHint(): String? {
        return flf.getDescription()
    }

    @Override
    fun getFunctionName(): String? {
        return flf.getName()
    }

    @Override
    fun getReturnType(): Int {
        if (rtnType == CFTypes.TYPE_UNKNOW) rtnType = CFTypes.toShort(flf.getReturnTypeAsString(), false, CFTypes.TYPE_UNKNOW)
        return rtnType.toInt()
    }

    @Override
    fun getDescription(): String? {
        return flf.getDescription()
    }

    @Override
    override fun setOwnerComponent(owner: Component?) {
        this.owner = owner
    }

    @Override
    fun getReturnFormat(defaultFormat: Int): Int {
        return getReturnFormat()
    }

    @Override
    fun getReturnFormat(): Int {
        return UDF.RETURN_FORMAT_WDDX
    }

    @Override
    fun getReturnTypeAsString(): String? {
        return flf.getReturnTypeAsString()
    }

    @Override
    fun getValue(): Object? {
        return this
    }

    @Override
    fun getOutput(): Boolean {
        return false
    }

    @Override
    @Throws(PageException::class)
    fun getDefaultValue(pc: PageContext?, index: Int): Object? {
        return null
    }

    @Override
    fun getSecureJson(): Boolean? {
        return null
    }

    @Override
    fun getVerifyClient(): Boolean? {
        return null
    }

    /*
	 * @Override public PageSource getPageSource() { return null; }
	 */
    @Override
    override fun equals(other: Object?): Boolean {
        return if (other !is UDF) false else UDFImpl.equals(this, other as UDF?)
    }

    @Override
    fun id(): String? {
        return id
    }

    @Override
    fun getSource(): String? {
        return ""
    }

    @Override
    fun getIndex(): Int {
        return -1
    }

    @Override
    @Throws(PageException::class)
    fun getDefaultValue(pc: PageContext?, index: Int, defaultValue: Object?): Object? {
        return null
    }

    // MUST
    @Override
    @Throws(PageException::class)
    fun getMetaData(pc: PageContext?): Struct? {
        // TODO Auto-generated method stub
        return StructImpl()
    }

    @Override
    @Throws(Throwable::class)
    fun implementation(pageContext: PageContext?): Object? {
        // TODO Auto-generated method stub
        return null
    }

    @Override
    fun getPageSource(): PageSource? {
        // TODO Auto-generated method stub
        return null
    }

    @Override
    fun getBufferOutput(pc: PageContext?): Boolean {
        return pc.getApplicationContext().getBufferOutput()
    }

    companion object {
        fun getInstance(pc: PageContext?, name: String?, defaultValue: BIF?): BIF? {
            val fl: FunctionLib = (pc.getConfig() as ConfigPro).getCombinedFLDs(pc.getCurrentTemplateDialect())
            val flf: FunctionLibFunction = fl.getFunction(name) ?: return defaultValue

            // BIF not found
            return BIF(pc.getConfig(), flf)
        }
    }
}