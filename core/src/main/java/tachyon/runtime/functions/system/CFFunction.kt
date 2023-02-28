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
package tachyon.runtime.functions.system

import tachyon.commons.io.res.Resource

object CFFunction {
    private val VAR: Variables? = VariablesImpl()

    // private static Map udfs=new ReferenceMap();
    @Throws(PageException::class)
    fun call(pc: PageContext?, objArr: Array<Object?>?): Object? {
        if (objArr!!.size < 3) throw ExpressionException("invalid call of a CFML Based built in function")

        // translate arguments
        val filename: String = Caster.toString((objArr[0] as FunctionValue?).getValue())
        val name: Collection.Key = KeyImpl.toKey((objArr[1] as FunctionValue?).getValue())
        val isweb: Boolean = Caster.toBooleanValue((objArr[2] as FunctionValue?).getValue())

        // function from archive may come without mapping definition
        var offset = 3
        var mappingName = "mapping-function"
        // clearly no mapping definition
        if (objArr.size > 3 && objArr[3] is FunctionValue) {
            val fv: FunctionValue? = objArr[3] as FunctionValue?
            if (fv.getName().equals("__mapping")) {
                mappingName = Caster.toString(fv.getValue())
                offset = 4
            }
        }
        val udf: UDF = loadUDF(pc, filename, mappingName, name, isweb)
        val meta: Struct = udf.getMetaData(pc)
        val callerScopes = if (meta == null) false else Caster.toBooleanValue(meta.get("callerScopes", Boolean.FALSE), false)
        val caller = if (meta == null) false else Caster.toBooleanValue(meta.get(KeyConstants._caller, Boolean.FALSE), false)
        var namedArguments: Struct? = null
        var cs: Struct? = null
        if (callerScopes) {
            cs = StructImpl()
            if (pc.undefinedScope().getCheckArguments()) {
                cs.set(KeyConstants._local, pc.localScope().duplicate(false))
                cs.set(KeyConstants._arguments, pc.argumentsScope().duplicate(false))
            }
        }
        var arguments: Array<Object?>? = null
        if (objArr.size <= offset) arguments = ArrayUtil.OBJECT_EMPTY else if (objArr[offset] is FunctionValue) {
            var fv: FunctionValue?
            namedArguments = StructImpl(Struct.TYPE_LINKED)
            if (callerScopes) namedArguments.setEL(KeyConstants._caller, cs) else if (caller) namedArguments.setEL(KeyConstants._caller, Duplicator.duplicate(pc.undefinedScope(), false))
            for (i in offset until objArr.size) {
                fv = toFunctionValue(name, objArr[i])
                namedArguments.set(fv.getName(), fv.getValue())
            }
        } else {
            val off = if (caller || callerScopes) 3 else 4
            arguments = arrayOfNulls<Object?>(objArr.size - off)
            if (callerScopes) arguments!![0] = cs else if (caller) arguments!![0] = Duplicator.duplicate(pc.undefinedScope(), false)
            for (i in offset until objArr.size) {
                arguments!![i - off] = toObject(name, objArr[i])
            }
        }
        // execute UDF
        return if (namedArguments == null) {
            udf.call(pc, name, arguments, false)
        } else udf.callWithNamedValues(pc, name, namedArguments, false)
    }

    @Throws(PageException::class)
    fun loadUDF(pc: PageContext?, res: Resource?, name: Collection.Key?, isweb: Boolean, cache: Boolean): UDF? {
        val ps: PageSource = pc.toPageSource(res, null) ?: throw ExpressionException("could not load template [$res]")
        return loadUDF(pc, ps, name, isweb, cache)
    }

    @Throws(PageException::class)
    fun loadUDF(pc: PageContext?, filename: String?, mappingName: String?, name: Collection.Key?, isweb: Boolean): UDF? {
        val config: ConfigWebPro = pc.getConfig() as ConfigWebPro
        val mapping: Mapping = if (isweb) config.getFunctionMapping(mappingName) else config.getServerFunctionMapping(mappingName)
        return loadUDF(pc, mapping.getPageSource(filename), name, isweb, true)
    }

    @Throws(PageException::class)
    fun loadUDF(pc: PageContext?, ps: PageSource?, name: Collection.Key?, isweb: Boolean, cache: Boolean): UDF? {
        val config: ConfigWebPro = pc.getConfig() as ConfigWebPro
        val key: String = if (isweb) name.getString() + config.getIdentification().getId() else name.getString()
        var udf: UDF? = if (cache) config.getFromFunctionCache(key) else null
        if (udf != null) return udf
        val p: Page = ps.loadPage(pc, false)

        // execute page
        val old: Variables = pc.variablesScope()
        if (old !== VAR) pc.setVariablesScope(VAR)
        val wasSilent: Boolean = pc.setSilent()
        try {
            p.call(pc)
            val o: Object = pc.variablesScope().get(name, null)
            if (o is UDF) {
                udf = o as UDF
                if (cache) config.putToFunctionCache(key, udf)
                return udf
            }
            throw ExpressionException("there is no Function defined with name [" + name + "] in template [" + ps.getDisplayPath() + "]")
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw Caster.toPageException(t)
        } finally {
            if (old !== VAR) pc.setVariablesScope(old)
            if (!wasSilent) pc.unsetSilent()
        }
    }

    @Throws(ExpressionException::class)
    private fun toFunctionValue(name: Collection.Key?, obj: Object?): FunctionValue? {
        if (obj is FunctionValue) return obj as FunctionValue?
        throw ExpressionException("invalid argument for function $name, you can not mix named and unnamed arguments")
    }

    @Throws(ExpressionException::class)
    private fun toObject(name: Collection.Key?, obj: Object?): Object? {
        if (obj is FunctionValue) throw ExpressionException("invalid argument for function $name, you can not mix named and unnamed arguments")
        return obj
    }
}