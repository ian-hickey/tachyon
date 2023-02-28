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

import java.util.HashMap

class UDFAddProperty(component: Component?, prop: Property?) : UDFGSProperty(component, "add" + StringUtil.ucFirst(PropertyFactory.getSingularName(prop)), getFunctionArgument(prop), CFTypes.TYPE_ANY) {
    private val prop: Property?
    private val propName: Key?
    @Override
    fun duplicate(): UDF? {
        return UDFAddProperty(srcComponent, prop)
    }

    @Override
    @Throws(PageException::class)
    override fun _call(pageContext: PageContext?, args: Array<Object?>?, doIncludePath: Boolean): Object? {
        val c: Component = getComponent(pageContext)
        // struct
        if (this.arguments.length === 2) {
            if (args!!.size < 2) throw ExpressionException("The function [" + getFunctionName().toString() + "] needs 2 arguments, only " + args.size.toString() + " argument" + (if (args.size == 1) " is" else "s are").toString() + " passed in.")
            return _call(pageContext, c, args[0], args[1])
        } else if (this.arguments.length === 1) {
            if (args!!.size < 1) throw ExpressionException("The parameter [" + this.arguments!!.get(0).getName().toString() + "] to function [" + getFunctionName().toString() + "] is required but was not passed in.")
            return _call(pageContext, c, null, args[0])
        }

        // never reached
        return c
    }

    @Override
    @Throws(PageException::class)
    override fun _callWithNamedValues(pageContext: PageContext?, values: Struct?, doIncludePath: Boolean): Object? {
        UDFUtil.argumentCollection(values, getFunctionArguments())
        val c: Component = getComponent(pageContext)

        // struct
        if (this.arguments.length === 2) {
            val keyName: Key = arguments!!.get(0).getName()
            val valueName: Key = arguments!!.get(1).getName()
            val key: Object = values.get(keyName, null)
            val value: Object = values.get(valueName, null)
            if (key == null) throw ExpressionException("The parameter [" + keyName + "] to function [" + getFunctionName() + "] is required but was not passed in.")
            if (value == null) throw ExpressionException("The parameter [" + valueName + "] to function [" + getFunctionName() + "] is required but was not passed in.")
            return _call(pageContext, c, key, value)
        } else if (this.arguments.length === 1) {
            val valueName: Key = arguments!!.get(0).getName()
            var value: Object = values.get(valueName, null)
            if (value == null) {
                val keys: Array<Key?> = CollectionUtil.keys(values)
                value = if (keys.size == 1) {
                    values.get(keys[0])
                } else throw ExpressionException("The parameter [" + valueName + "] to function [" + getFunctionName() + "] is required but was not passed in.")
            }
            return _call(pageContext, c, null, value)
        }

        // never reached
        return getComponent(pageContext)
    }

    @Throws(PageException::class)
    private fun _call(pageContext: PageContext?, c: Component?, key: Object?, value: Object?): Object? {
        var key: Object? = key
        var value: Object? = value
        var propValue: Object? = c.getComponentScope().get(propName, null)

        // struct
        if (this.arguments.length === 2) {
            key = cast(pageContext, arguments!!.get(0), key, 1)
            value = cast(pageContext, arguments!!.get(1), value, 2)
            if (propValue == null) {
                val map = HashMap()
                c.getComponentScope().setEL(propName, map)
                propValue = map
            }
            if (propValue is Struct) {
                (propValue as Struct?).set(KeyImpl.toKey(key), value)
            } else if (propValue is Map) {
                (propValue as Map?).put(key, value)
            }
        } else {
            value = cast(pageContext, arguments!!.get(0), value, 1)
            if (propValue == null) {
                /*
				 * jira2049 PageContext pc = ThreadLocalPageContext.get(); ORMSession sess = ORMUtil.getSession(pc);
				 * SessionImpl s=(SessionImpl) sess.getRawSession(); propValue=new PersistentList(s);
				 * component.getComponentScope().setEL(propName,propValue);
				 */
                val arr: Array = ArrayImpl()
                c.getComponentScope().setEL(propName, arr)
                propValue = arr
            }
            if (propValue is Array) {
                (propValue as Array?).appendEL(value)
            } else if (propValue is List<*>) {
                (propValue as MutableList<*>?)!!.add(value)
            }
        }
        return c
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
        return "any"
    }

    companion object {
        private const val serialVersionUID = 94007529373807331L
        private fun getFunctionArgument(prop: Property?): Array<FunctionArgument?>? {
            val t: String = PropertyFactory.getType(prop)
            val value: FunctionArgument = FunctionArgumentLight(KeyImpl.init(PropertyFactory.getSingularName(prop)), "any", CFTypes.TYPE_ANY, true)
            if ("struct".equalsIgnoreCase(t)) {
                val key: FunctionArgument = FunctionArgumentLight(KeyConstants._key, "string", CFTypes.TYPE_STRING, true)
                return arrayOf<FunctionArgument?>(key, value)
            }
            return arrayOf<FunctionArgument?>(value)
        }
    }

    init {
        this.prop = prop
        propName = KeyImpl.init(prop.getName())
    }
}