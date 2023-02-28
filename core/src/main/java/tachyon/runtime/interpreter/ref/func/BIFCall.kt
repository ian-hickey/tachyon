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
package tachyon.runtime.interpreter.ref.func

import java.util.ArrayList

/**
 * a built In Function call
 *
 *
 */
class BIFCall : RefSupport, Ref {
    private var refArgs: Array<Ref?>?
    private var flf: FunctionLibFunction?
    private var obj: Object? = null

    /**
     * constructor of the class
     *
     * @param pc
     * @param flf
     * @param refArgs
     */
    constructor(flf: FunctionLibFunction?, refArgs: Array<Ref?>?) {
        this.flf = flf
        this.refArgs = refArgs
    }

    constructor(obj: Object?, flf: FunctionLibFunction?, refArgs: Array<Ref?>?) {
        this.obj = obj
        this.flf = flf
        this.refArgs = refArgs
    }

    @Override
    @Throws(PageException::class)
    fun getValue(pc: PageContext?): Object? {
        var arguments: Array<Object?>? = null
        if (isDynamic()) {
            arguments = RefUtil.getValue(pc, refArgs)
            if (flf.hasDefaultValues()) {
                val tmp: List<Object?> = ArrayList<Object?>()
                val args: ArrayList<FunctionLibFunctionArg?> = flf.getArg()
                val it: Iterator<FunctionLibFunctionArg?> = args.iterator()
                var arg: FunctionLibFunctionArg?
                while (it.hasNext()) {
                    arg = it.next()
                    if (arg.getDefaultValue() != null) tmp.add(FunctionValueImpl(arg.getName(), arg.getDefaultValue()))
                }
                for (i in arguments.indices) {
                    tmp.add(arguments[i])
                }
                arguments = tmp.toArray()
            }
            arguments = arrayOf(arguments)
        } else {
            if (isNamed(pc, refArgs)) {
                val fvalues: Array<FunctionValue?>? = getFunctionValues(pc, refArgs)
                val names = getNames(fvalues)
                val list: ArrayList<FunctionLibFunctionArg?> = flf.getArg()
                val it: Iterator<FunctionLibFunctionArg?> = list.iterator()
                arguments = arrayOfNulls<Object?>(list.size())
                var flfa: FunctionLibFunctionArg?
                var index = 0
                var vt: VT?
                while (it.hasNext()) {
                    flfa = it.next()
                    vt = getMatchingValueAndType(flfa, fvalues, names)
                    if (vt!!.index != -1) names!![vt.index] = null
                    arguments!![index++] = Casting(vt.type, CFTypes.toShort(vt.type, false, CFTypes.TYPE_UNKNOW), vt.value).getValue(pc)
                }
                for (y in names.indices) {
                    if (names!![y] != null) {
                        val ee: ExpressionException = InterpreterException("argument [" + names[y] + "] is not allowed for function [" + flf.getName() + "]")
                        UDFUtil.addFunctionDoc(ee, flf)
                        throw ee
                    }
                }
            } else {
                arguments = RefUtil.getValue(pc, refArgs)
            }
        }
        val bif: BIF = flf.getBIF()
        if (flf.getMemberChaining() && obj != null) {
            bif.invoke(pc, arguments)
            return obj
        }
        if (!isDynamic() && flf.getArgMin() > arguments!!.size) {
            throw FunctionException(pc, flf.getName(), flf.getArgMin(), flf.getArgMax(), arguments.size)
        }
        return Caster.castTo(pc, flf.getReturnTypeAsString(), bif.invoke(pc, arguments), false)
    }

    @Throws(ExpressionException::class)
    private fun getMatchingValueAndType(flfa: FunctionLibFunctionArg?, fvalues: Array<FunctionValue?>?, names: Array<String?>?): VT? {
        val flfan: String = flfa.getName()

        // first search if an argument match
        for (i in names.indices) {
            if (names!![i] != null && names[i].equalsIgnoreCase(flfan)) {
                return VT(fvalues!![i].getValue(), flfa.getTypeAsString(), i)
            }
        }

        // then check if an alias match
        val alias: String = flfa.getAlias()
        if (!StringUtil.isEmpty(alias)) {
            for (i in names.indices) {
                if (names!![i] != null && tachyon.runtime.type.util.ListUtil.listFindNoCase(alias, names[i], ",") !== -1) {
                    return VT(fvalues!![i].getValue(), flfa.getTypeAsString(), i)
                }
            }
        }

        // if not required return the default value
        if (!flfa.getRequired()) {
            val defaultValue: String = flfa.getDefaultValue()
            val type: String = flfa.getTypeAsString().toLowerCase()
            if (defaultValue == null) {
                if (type.equals("boolean") || type.equals("bool")) return VT(Boolean.FALSE, type, -1)
                return if (type.equals("number") || type.equals("numeric") || type.equals("double")) VT(Constants.DOUBLE_ZERO, type, -1) else VT(null, type, -1)
            }
            return VT(defaultValue, type, -1)
        }
        val ee: ExpressionException = InterpreterException("missing required argument [" + flfan + "] for function [" + flfa.getFunction().getName() + "]")
        UDFUtil.addFunctionDoc(ee, flfa.getFunction())
        throw ee
    }

    private fun getNames(fvalues: Array<FunctionValue?>?): Array<String?>? {
        val names = arrayOfNulls<String?>(fvalues!!.size)
        for (i in fvalues.indices) {
            names[i] = fvalues[i].getNameAsString()
        }
        return names
    }

    @Throws(PageException::class)
    private fun getFunctionValues(pc: PageContext?, refArgs: Array<Ref?>?): Array<FunctionValue?>? {
        val fvalues: Array<FunctionValue?> = arrayOfNulls<FunctionValue?>(refArgs!!.size)
        for (i in refArgs.indices) {
            fvalues[i] = ((refArgs!![i] as Casting?).getRef() as LFunctionValue).getValue(pc) as FunctionValue
        }
        return fvalues
    }

    @Throws(PageException::class)
    private fun isNamed(pc: PageContext?, refArgs: Array<Ref?>?): Boolean {
        if (ArrayUtil.isEmpty(refArgs)) return false
        var cast: Casting?
        var count = 0
        for (i in refArgs.indices) {
            if (refArgs!![i] is Casting) {
                cast = refArgs[i] as Casting?
                if (cast.getRef() is LFunctionValue && (cast.getRef() as LFunctionValue).getValue(pc) is FunctionValue) {
                    count++
                }
            }
        }
        if (count != 0 && count != refArgs!!.size) {
            val ee: ExpressionException = InterpreterException("invalid argument for function " + flf.getName().toString() + ", you can not mix named and unnamed arguments")
            UDFUtil.addFunctionDoc(ee, flf)
            throw ee
        }
        return count != 0
    }

    private fun isDynamic(): Boolean {
        return flf.getArgType() === FunctionLibFunction.ARG_DYNAMIC
    }

    @Override
    fun getTypeName(): String? {
        return "built in function"
    }
}

internal class VT(value: Object?, type: String?, index: Int) {
    var value: Object?
    var type: String?
    var index: Int

    init {
        this.value = value
        this.type = type
        this.index = index
    }
}