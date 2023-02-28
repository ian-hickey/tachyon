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
package tachyon.runtime.type

import tachyon.commons.lang.CFTypes

class UDFGetterProperty(component: Component?, prop: Property?) : UDFGSProperty(component, "get" + StringUtil.ucFirst(prop.getName()), EMPTY, CFTypes.TYPE_STRING) {
    private val prop: Property?

    // private ComponentScope scope;
    private val propName: Key?
    @Override
    fun duplicate(): UDF? {
        return UDFGetterProperty(srcComponent, prop)
    }

    @Override
    @Throws(PageException::class)
    override fun _call(pageContext: PageContext?, args: Array<Object?>?, doIncludePath: Boolean): Object? {
        return getComponent(pageContext).getComponentScope().get(pageContext, propName, null)
    }

    @Override
    @Throws(PageException::class)
    override fun _callWithNamedValues(pageContext: PageContext?, values: Struct?, doIncludePath: Boolean): Object? {
        return getComponent(pageContext).getComponentScope().get(pageContext, propName, null)
    }

    @Override
    @Throws(Throwable::class)
    fun implementation(pageContext: PageContext?): Object? {
        return getComponent(pageContext).getComponentScope().get(pageContext, propName, null)
    }

    @Override
    @Throws(PageException::class)
    fun getDefaultValue(pc: PageContext?, index: Int): Object? {
        return null
    }

    @Override
    @Throws(PageException::class)
    fun getDefaultValue(pc: PageContext?, index: Int, defaultValue: Object?): Object? {
        return defaultValue
    }

    @Override
    fun getReturnTypeAsString(): String? {
        return prop.getType()
    }

    companion object {
        private val EMPTY: Array<FunctionArgument?>? = arrayOfNulls<FunctionArgument?>(0)
    }

    init {
        this.prop = prop
        propName = KeyImpl.init(prop.getName())
    }
}