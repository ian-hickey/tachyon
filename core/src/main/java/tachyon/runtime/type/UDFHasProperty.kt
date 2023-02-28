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

class UDFHasProperty(component: Component?, prop: Property?) : UDFGSProperty(component, "has" + StringUtil.ucFirst(PropertyFactory.getSingularName(prop)), getFunctionArgument(prop), CFTypes.TYPE_BOOLEAN) {
    private val prop: Property?

    // private ComponentScope scope;
    private val propName: Key?
    private fun isStruct(): Boolean {
        val t: String = PropertyFactory.getType(prop)
        return "struct".equalsIgnoreCase(t)
    }

    @Override
    fun duplicate(): UDF? {
        return UDFHasProperty(srcComponent, prop)
    }

    @Override
    @Throws(PageException::class)
    override fun _call(pageContext: PageContext?, args: Array<Object?>?, doIncludePath: Boolean): Object? {
        return if (args!!.size < 1) has(pageContext) else has(pageContext, args[0])
    }

    @Override
    @Throws(PageException::class)
    override fun _callWithNamedValues(pageContext: PageContext?, values: Struct?, doIncludePath: Boolean): Object? {
        UDFUtil.argumentCollection(values, getFunctionArguments())
        val key: Key = arguments!!.get(0).getName()
        var value: Object = values.get(key, null)
        if (value == null) {
            val keys: Array<Key?> = CollectionUtil.keys(values)
            value = if (keys.size > 0) {
                values.get(keys[0])
            } else return has(pageContext)
        }
        return has(pageContext, value)
    }

    private fun has(pageContext: PageContext?): Boolean {
        val propValue: Object = getComponent(pageContext).getComponentScope().get(propName, null)

        // struct
        if (isStruct()) {
            return if (propValue is Map) {
                !(propValue as Map).isEmpty()
            } else false
        }

        // Object o;
        if (propValue is Array) {
            return propValue.size() > 0
        } else if (propValue is List<*>) {
            return (propValue as List<*>).size() > 0
        }
        return propValue is Component
    }

    @Throws(PageException::class)
    private fun has(pageContext: PageContext?, value: Object?): Boolean {
        val propValue: Object = getComponent(pageContext).getComponentScope().get(propName, null)

        // struct
        if (isStruct()) {
            val strKey: String = Caster.toString(value)
            // if(strKey==NULL) throw new ;
            if (propValue is Struct) {
                return (propValue as Struct).containsKey(KeyImpl.getInstance(strKey))
            } else if (propValue is Map) {
                return (propValue as Map).containsKey(strKey)
            }
            return false
        }
        var o: Object
        if (propValue is Array) {
            val it: Iterator<Object?> = propValue.valueIterator()
            while (it.hasNext()) {
                if (ORMUtil.equals(value, it.next())) return true
            }
        } else if (propValue is List<*>) {
            val it: Iterator = (propValue as List<*>).iterator()
            while (it.hasNext()) {
                o = it.next()
                if (ORMUtil.equals(value, o)) return true
            }
        }
        return false
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
        private fun getFunctionArgument(prop: Property?): Array<FunctionArgument?>? {
            val t: String = PropertyFactory.getType(prop)
            if ("struct".equalsIgnoreCase(t)) {
                val key: FunctionArgument = FunctionArgumentLight(KeyConstants._key, "string", CFTypes.TYPE_STRING, false)
                return arrayOf<FunctionArgument?>(key)
            }
            val value: FunctionArgument = FunctionArgumentLight(KeyImpl.init(PropertyFactory.getSingularName(prop)), "any", CFTypes.TYPE_ANY, false)
            return arrayOf<FunctionArgument?>(value)
        }
    }

    // private static final String NULL="sdsdsdfsfsfjkln fsdfsa";
    init {
        this.prop = prop
        propName = KeyImpl.init(prop.getName())
    }
}