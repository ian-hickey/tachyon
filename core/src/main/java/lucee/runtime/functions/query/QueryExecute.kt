/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
/**
 * Implements the CFML Function isquery
 */
package lucee.runtime.functions.query

import lucee.commons.lang.ExceptionUtil

class QueryExecute : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size < 1 || args.size > 3) throw FunctionException(pc, "QueryExecute", 1, 3, args.size)
        if (args.size == 3) return call(pc, Caster.toString(args[0]), args[1], Caster.toStruct(args[2]))
        return if (args.size == 2) call(pc, Caster.toString(args[0]), args[1]) else call(pc, Caster.toString(args[0]))
    }

    companion object {
        private const val serialVersionUID = -4714201927377662500L
        @Throws(PageException::class)
        fun call(pc: PageContext?, sql: String?): Object? {
            return call(pc, sql, null, null, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, sql: String?, params: Object?): Object? {
            return call(pc, sql, params, null, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, sql: String?, params: Object?, options: Struct?): Object? {
            return call(pc, sql, params, options, null)
        }

        // name is set by evaluator
        @Throws(PageException::class)
        fun call(pc: PageContext?, sql: String?, params: Object?, options: Struct?, name: String?): Object? {
            val pci: PageContextImpl? = pc as PageContextImpl?
            val qry: lucee.runtime.tag.Query = pci.use(lucee.runtime.tag.Query::class.java.getName(), "cfquery", TagLibTag.ATTRIBUTE_TYPE_FIXED)
            return try {
                try {
                    qry!!.hasBody(true)
                    // set attributes
                    qry.setReturnVariable(true)
                    qry.setName(if (StringUtil.isEmpty(name)) "QueryExecute" else name)
                    if (options != null) TagUtil.setAttributeCollection(pc, qry, null, options, TagLibTag.ATTRIBUTE_TYPE_FIXED)
                    qry!!.setParams(params)
                    val res: Int = qry!!.doStartTag()
                    pc.initBody(qry, res)
                    pc.forceWrite(sql)
                    qry!!.doAfterBody()
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    try {
                        qry.doCatch(t)
                    } catch (t2: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                        throw Caster.toPageException(t2)
                    }
                } finally {
                    pc.popBody()
                    qry.doFinally()
                }
                qry!!.doEndTag()
                qry.getReturnVariable()
            } finally {
                pci.reuse(qry)
            }
        }
    }
}