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
/**
 * Implements the CFML Function getfunctiondescription
 */
package lucee.runtime.functions.other

import java.util.ArrayList

object GetFunctionData : Function {
    private val SOURCE: Collection.Key? = KeyConstants._source
    private val RETURN_TYPE: Collection.Key? = KeyImpl.getInstance("returnType")
    private val ARGUMENT_TYPE: Collection.Key? = KeyImpl.getInstance("argumentType")
    private val ARG_MIN: Collection.Key? = KeyImpl.getInstance("argMin")
    private val ARG_MAX: Collection.Key? = KeyImpl.getInstance("argMax")
    val INTRODUCED: Collection.Key? = KeyImpl.getInstance("introduced")
    @Throws(PageException::class)
    fun call(pc: PageContext?, strFunctionName: String?): Struct? {
        return _call(pc, strFunctionName, pc.getCurrentTemplateDialect())
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, strFunctionName: String?, strDialect: String?): Struct? {
        val dialect: Int = ConfigWebUtil.toDialect(strDialect, -1)
        if (dialect == -1) throw FunctionException(pc, "GetFunctionData", 2, "dialect", "value [$strDialect] is invalid, valid values are [cfml,lucee]")
        return _call(pc, strFunctionName, dialect)
    }

    @Throws(PageException::class)
    private fun _call(pc: PageContext?, strFunctionName: String?, dialect: Int): Struct? {
        val flds: Array<FunctionLib?>
        flds = (pc.getConfig() as ConfigPro).getFLDs(dialect)
        var function: FunctionLibFunction? = null
        for (i in flds.indices) {
            function = flds[i].getFunction(strFunctionName.toLowerCase())
            if (function != null) break
        }
        if (function == null) throw ExpressionException("Function [$strFunctionName] is not a built in function")

        // CFML Based Function
        var clazz: Class? = null
        try {
            clazz = function.getFunctionClassDefinition().getClazz()
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        return if (clazz === lucee.runtime.functions.system.CFFunction::class.java) {
            cfmlBasedFunction(pc, function)
        } else javaBasedFunction(function)
    }

    @Throws(PageException::class)
    private fun javaBasedFunction(function: FunctionLibFunction?): Struct? {
        val sct: Struct = StructImpl(StructImpl.TYPE_LINKED)
        sct.set(KeyConstants._name, function.getName())
        sct.set(KeyConstants._status, TagLibFactory.toStatus(function.getStatus()))
        if (function.getIntroduced() != null) sct.set(INTRODUCED, function.getIntroduced().toString())
        // else if(inside.equals("introduced")) att.setIntroduced(value);
        sct.set(KeyConstants._description, StringUtil.emptyIfNull(function.getDescription()))
        if (!ArrayUtil.isEmpty(function.getKeywords())) sct.set("keywords", Caster.toArray(function.getKeywords()))
        sct.set(RETURN_TYPE, StringUtil.emptyIfNull(function.getReturnTypeAsString()))
        sct.set(ARGUMENT_TYPE, StringUtil.emptyIfNull(function.getArgTypeAsString()))
        sct.set(ARG_MIN, Caster.toDouble(function.getArgMin()))
        sct.set(ARG_MAX, Caster.toDouble(function.getArgMax()))
        sct.set(KeyConstants._type, "java")
        val names: Array<String?> = function.getMemberNames()
        if (!ArrayUtil.isEmpty(names) && function.getMemberType() !== CFTypes.TYPE_UNKNOW) {
            val mem = StructImpl(StructImpl.TYPE_LINKED)
            sct.set(KeyConstants._member, mem)
            mem.set(KeyConstants._name, names[0])
            mem.set(KeyConstants._chaining, Caster.toBoolean(function.getMemberChaining()))
            mem.set(KeyConstants._type, function.getMemberTypeAsString())
            mem.set("position", Caster.toDouble(function.getMemberPosition()))
        }
        val _args: Array = ArrayImpl()
        sct.set(KeyConstants._arguments, _args)
        if (function.getArgType() !== FunctionLibFunction.ARG_DYNAMIC) {
            val args: ArrayList<FunctionLibFunctionArg?> = function.getArg()
            for (i in 0 until args.size()) {
                val arg: FunctionLibFunctionArg = args.get(i)
                val _arg: Struct = StructImpl(StructImpl.TYPE_LINKED)
                _arg.set(KeyConstants._required, if (arg.getRequired()) Boolean.TRUE else Boolean.FALSE)
                _arg.set(KeyConstants._type, StringUtil.emptyIfNull(arg.getTypeAsString()))
                _arg.set(KeyConstants._name, StringUtil.emptyIfNull(arg.getName()))
                _arg.set(KeyConstants._status, TagLibFactory.toStatus(arg.getStatus()))
                if (arg.getIntroduced() != null) _arg.set(INTRODUCED, arg.getIntroduced().toString())
                if (!StringUtil.isEmpty(arg.getAlias(), true)) _arg.set(KeyConstants._alias, arg.getAlias())
                _arg.set("defaultValue", arg.getDefaultValue())
                _arg.set(KeyConstants._description, StringUtil.toStringEmptyIfNull(arg.getDescription()))
                _args.append(_arg)
            }
        }
        return sct
    }

    @Throws(PageException::class)
    private fun cfmlBasedFunction(pc: PageContext?, function: FunctionLibFunction?): Struct? {
        val sct: Struct = StructImpl(StructImpl.TYPE_LINKED)
        val args: ArrayList<FunctionLibFunctionArg?> = function.getArg()
        val filename: String = Caster.toString(args.get(0).getDefaultValue())
        val name: Key = KeyImpl.toKey(args.get(1).getDefaultValue())
        val isWeb: Boolean = Caster.toBooleanValue(args.get(2).getDefaultValue())
        val mappingName: String = Caster.toString(args.get(3).getDefaultValue())
        val udf: UDF = CFFunction.loadUDF(pc, filename, mappingName, name, isWeb)
        sct.set(KeyConstants._name, function.getName())
        sct.set(ARGUMENT_TYPE, "fixed")
        sct.set(KeyConstants._description, StringUtil.emptyIfNull(udf.getHint()))
        sct.set(RETURN_TYPE, StringUtil.emptyIfNull(udf.getReturnTypeAsString()))
        sct.set(KeyConstants._type, "cfml")
        sct.set(SOURCE, udf.getSource())
        sct.set(KeyConstants._status, "implemented")
        val fas: Array<FunctionArgument?> = udf.getFunctionArguments()
        val _args: Array = ArrayImpl()
        sct.set(KeyConstants._arguments, _args)
        var min = 0
        var max = 0
        for (i in fas.indices) {
            val fa: FunctionArgument? = fas[i]
            val meta: Struct = fa.getMetaData()
            val _arg: Struct = StructImpl(StructImpl.TYPE_LINKED)
            if (fa.isRequired()) min++
            max++
            _arg.set(KeyConstants._required, if (fa.isRequired()) Boolean.TRUE else Boolean.FALSE)
            _arg.set(KeyConstants._type, StringUtil.emptyIfNull(fa.getTypeAsString()))
            _arg.set(KeyConstants._name, StringUtil.emptyIfNull(fa.getName()))
            _arg.set(KeyConstants._description, StringUtil.emptyIfNull(fa.getHint()))
            var status: String
            status = if (meta == null) "implemented" else TagLibFactory.toStatus(TagLibFactory.toStatus(Caster.toString(meta.get(KeyConstants._status, "implemented"))))
            _arg.set(KeyConstants._status, status)
            _args.append(_arg)
        }
        sct.set(ARG_MIN, Caster.toDouble(min))
        sct.set(ARG_MAX, Caster.toDouble(max))
        return sct
    }
}