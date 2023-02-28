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
package tachyon.runtime.type.trace

import tachyon.runtime.Component

class TOUDF(debugger: Debugger?, udf: UDF?, type: Int, category: String?, text: String?) : TOObjects(debugger, udf, type, category, text), UDF {
    private val udf: UDF?
    @Override
    fun getModifier(): Int {
        log(null)
        return udf.getModifier()
    }

    @Override
    fun getAccess(): Int {
        log(null)
        return udf.getAccess()
    }

    fun setAccess(access: Int) {
        log(ComponentUtil.toStringAccess(access, null))
        if (udf is UDFPlus) (udf as UDFPlus?).setAccess(access)
    }

    @Override
    fun getValue(): Object? {
        log(null)
        return udf.getValue()
    }

    @Override
    @Throws(Throwable::class)
    fun implementation(pageContext: PageContext?): Object? {
        log(null)
        return udf.implementation(pageContext)
    }

    @Override
    fun getFunctionArguments(): Array<FunctionArgument?>? {
        log(null)
        return udf.getFunctionArguments()
    }

    @Override
    @Throws(PageException::class)
    fun getDefaultValue(pc: PageContext?, index: Int): Object? {
        log(null)
        return udf.getDefaultValue(pc, index)
    }

    @Override
    @Throws(PageException::class)
    fun getDefaultValue(pc: PageContext?, index: Int, defaultValue: Object?): Object? {
        log(null)
        return udf.getDefaultValue(pc, index, defaultValue)
    }

    @Override
    fun getFunctionName(): String? {
        log(null)
        return udf.getFunctionName()
    }

    @Override
    fun getOutput(): Boolean {
        log(null)
        return udf.getOutput()
    }

    @Override
    fun getReturnType(): Int {
        log(null)
        return udf.getReturnType()
    }

    @Override
    fun id(): String? {
        log(null)
        return udf.id()
    }

    @Override
    fun getReturnFormat(): Int {
        log(null)
        return udf.getReturnFormat()
    }

    @Override
    fun getReturnFormat(defaultValue: Int): Int {
        log(null)
        return udf.getReturnFormat(defaultValue)
    }

    @Override
    fun getSecureJson(): Boolean? {
        log(null)
        return udf.getSecureJson()
    }

    @Override
    fun getVerifyClient(): Boolean? {
        log(null)
        return udf.getVerifyClient()
    }

    @Override
    fun getReturnTypeAsString(): String? {
        log(null)
        return udf.getReturnTypeAsString()
    }

    @Override
    fun getDescription(): String? {
        log(null)
        return udf.getDescription()
    }

    @Override
    @Throws(PageException::class)
    override fun callWithNamedValues(pageContext: PageContext?, values: Struct?, doIncludePath: Boolean): Object? {
        log(null)
        return udf.callWithNamedValues(pageContext, values, doIncludePath)
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pageContext: PageContext?, calledName: Collection.Key?, values: Struct?, doIncludePath: Boolean): Object? {
        log(null)
        return udf.callWithNamedValues(pageContext, calledName, values, doIncludePath)
    }

    @Override
    @Throws(PageException::class)
    fun call(pageContext: PageContext?, args: Array<Object?>?, doIncludePath: Boolean): Object? {
        log(null)
        return udf.call(pageContext, args, doIncludePath)
    }

    @Override
    @Throws(PageException::class)
    fun call(pageContext: PageContext?, calledName: Collection.Key?, args: Array<Object?>?, doIncludePath: Boolean): Object? {
        log(null)
        return udf.call(pageContext, calledName, args, doIncludePath)
    }

    @Override
    fun getDisplayName(): String? {
        log(null)
        return udf.getDisplayName()
    }

    @Override
    fun getHint(): String? {
        log(null)
        return udf.getHint()
    }

    /*
	 * @Override public PageSource getPageSource() { log(null); return udf.getPageSource(); }
	 */
    @Override
    override fun equals(other: Object?): Boolean {
        return udf.equals(other)
    }

    @Override
    fun getSource(): String? {
        log(null)
        return udf.getSource()
    }

    @Override
    fun getIndex(): Int {
        log(null)
        return udf.getIndex()
    }

    @Override
    @Throws(PageException::class)
    fun getMetaData(pc: PageContext?): Struct? {
        log(null)
        return udf.getMetaData(pc)
    }

    @Override
    fun duplicate(): UDF? {
        log(null)
        return udf.duplicate()
    }

    @Override
    fun getBufferOutput(pc: PageContext?): Boolean {
        log(pc)
        return udf.getBufferOutput(pc)
    }

    @Override
    fun getOwnerComponent(): Component? {
        log(null)
        return udf.getOwnerComponent()
    }

    fun setOwnerComponent(cfc: ComponentImpl?) {
        log(null)
        if (udf is UDFPlus) (udf as UDFPlus?).setOwnerComponent(cfc)
    }

    @Override
    fun getPageSource(): PageSource? {
        return udf.getPageSource()
    }

    init {
        this.udf = udf
    }
}