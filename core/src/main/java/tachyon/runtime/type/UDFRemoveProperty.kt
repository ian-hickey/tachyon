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

import java.util.Iterator

class UDFRemoveProperty(component: Component?, prop: Property?) : UDFGSProperty(component, "remove" + StringUtil.ucFirst(PropertyFactory.getSingularName(prop)), getFunctionArgument(prop), CFTypes.TYPE_BOOLEAN) {
    private val prop: Property?
    private val propName: Key?
    private fun isStruct(): Boolean {
        val t: String = PropertyFactory.getType(prop)
        return "struct".equalsIgnoreCase(t)
    }

    @Override
    fun duplicate(): UDF? {
        return UDFRemoveProperty(srcComponent, prop)
    }

    @Override
    @Throws(PageException::class)
    override fun _call(pageContext: PageContext?, args: Array<Object?>?, doIncludePath: Boolean): Object? {
        if (args!!.size < 1) throw ExpressionException("The parameter [" + this.arguments!!.get(0).getName().toString() + "] to function [" + getFunctionName().toString() + "] is required but was not passed in.")
        return remove(pageContext, args[0])
    }

    @Override
    @Throws(PageException::class)
    override fun _callWithNamedValues(pageContext: PageContext?, values: Struct?, doIncludePath: Boolean): Object? {
        UDFUtil.argumentCollection(values, getFunctionArguments())
        val key: Key = arguments!!.get(0).getName()
        var value: Object = values.get(key, null)
        if (value == null) {
            val keys: Array<Key?> = CollectionUtil.keys(values)
            value = if (keys.size == 1) {
                values.get(keys[0])
            } else throw ExpressionException("The parameter [" + key + "] to function [" + getFunctionName() + "] is required but was not passed in.")
        }
        return remove(pageContext, value)
    }

    @Throws(PageException::class)
    private fun remove(pageContext: PageContext?, value: Object?): Boolean {
        var value: Object? = value
        val c: Component = getComponent(pageContext)
        val propValue: Object = c.getComponentScope().get(propName, null)
        value = cast(pageContext, arguments!!.get(0), value, 1)

        // make sure it is reconized that set is called by hibernate
        // if(component.isPersistent())ORMUtil.getSession(pageContext);
        val appContext: ApplicationContext = pageContext.getApplicationContext()
        if (appContext.isORMEnabled() && c.isPersistent()) ORMUtil.getSession(pageContext)

        // struct
        if (isStruct()) {
            val strKey: String = Caster.toString(value, null) ?: return false
            if (propValue is Struct) {
                return (propValue as Struct).removeEL(KeyImpl.getInstance(strKey)) != null
            } else if (propValue is Map) {
                return (propValue as Map).remove(strKey) != null
            }
            return false
        }
        var o: Object
        var has = false
        if (propValue is Array) {
            val arr: Array = propValue
            val keys: Array<Key?> = CollectionUtil.keys(arr)
            for (i in keys.indices) {
                o = arr.get(keys[i], null)
                if (ORMUtil.equals(value, o)) {
                    arr.removeEL(keys[i])
                    has = true
                }
            }
        } else if (propValue is List<*>) {
            val it: Iterator = (propValue as List<*>).iterator()
            while (it.hasNext()) {
                o = it.next()
                if (ORMUtil.equals(value, o)) {
                    it.remove()
                    has = true
                }
            }
        }
        return has
    }

    @Override
    @Throws(Throwable::class)
    fun implementation(pageContext: PageContext?): Object? {
        return null
    }

    @Override
    @Throws(PageException::class)
    fun getDefaultValue(pc: PageContext?, index: Int): Object? {
        return prop.getDefault()
    }

    @Override
    @Throws(PageException::class)
    fun getDefaultValue(pc: PageContext?, index: Int, defaultValue: Object?): Object? {
        return prop.getDefault()
    }

    @Override
    fun getReturnTypeAsString(): String? {
        return "boolean"
    }

    companion object {
        private const val serialVersionUID = -7030615729484825208L
        private fun getFunctionArgument(prop: Property?): Array<FunctionArgument?>? {
            val t: String = PropertyFactory.getType(prop)
            if ("struct".equalsIgnoreCase(t)) {
                val key: FunctionArgument = FunctionArgumentLight(KeyConstants._key, "string", CFTypes.TYPE_STRING, true)
                return arrayOf<FunctionArgument?>(key)
            }
            val value: FunctionArgument = FunctionArgumentLight(KeyImpl.init(PropertyFactory.getSingularName(prop)), "any", CFTypes.TYPE_ANY, true)
            return arrayOf<FunctionArgument?>(value)
        }
    }

    init {
        this.prop = prop
        propName = KeyImpl.init(prop.getName())
    }
}