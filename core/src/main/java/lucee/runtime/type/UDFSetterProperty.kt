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

import lucee.commons.lang.CFTypes

class UDFSetterProperty : UDFGSProperty {
    private val prop: Property?
    private val propName: Key?
    private var validate: String?
    private var validateParams: Struct? = null

    private constructor(component: Component?, prop: Property?, validate: String?, validateParams: Struct?) : super(component, "set" + StringUtil.ucFirst(prop.getName()), arrayOf<FunctionArgument?>(
            FunctionArgumentLight(KeyImpl.init(prop.getName()), prop.getType(), CFTypes.toShortStrict(prop.getType(), CFTypes.TYPE_UNKNOW), true)),
            CFTypes.TYPE_ANY) {
        this.prop = prop
        propName = KeyImpl.init(prop.getName())
        this.validate = validate
        this.validateParams = validateParams
    }

    constructor(component: Component?, prop: Property?) : super(component, "set" + StringUtil.ucFirst(prop.getName()), arrayOf<FunctionArgument?>(
            FunctionArgumentLight(KeyImpl.init(prop.getName()), prop.getType(), CFTypes.toShortStrict(prop.getType(), CFTypes.TYPE_UNKNOW), true)),
            CFTypes.TYPE_ANY) {
        this.prop = prop
        propName = KeyImpl.init(prop.getName())
        validate = Caster.toString(prop.getDynamicAttributes().get(KeyConstants._validate, null), null)
        if (!StringUtil.isEmpty(validate, true)) {
            validate = validate.trim().toLowerCase()
            val da: Struct = prop.getDynamicAttributes()
            if (da != null) {
                val o: Object = da.get(VALIDATE_PARAMS, null)
                if (o != null) {
                    if (Decision.isStruct(o)) validateParams = Caster.toStruct(o) else {
                        val str: String = Caster.toString(o)
                        if (!StringUtil.isEmpty(str, true)) {
                            validateParams = ORMUtil.convertToSimpleMap(str)
                            if (validateParams == null) throw ExpressionException("cannot parse string [$str] as struct")
                        }
                    }
                }
            }
        }
    }

    @Override
    fun duplicate(): UDF? {
        return UDFSetterProperty(srcComponent, prop, validate, validateParams)
    }

    @Override
    @Throws(PageException::class)
    override fun _call(pageContext: PageContext?, args: Array<Object?>?, doIncludePath: Boolean): Object? {
        if (args!!.size < 1) throw ExpressionException("The parameter " + prop.getName().toString() + " to function " + getFunctionName().toString() + " is required but was not passed in.")
        validate(validate, validateParams, args[0])
        val c: Component = getComponent(pageContext)
        c.getComponentScope().set(propName, cast(pageContext, this.arguments!!.get(0), args[0], 1))

        // make sure it is reconized that set is called by hibernate
        // if(component.isPersistent())ORMUtil.getSession(pageContext);
        val appContext: ApplicationContext = pageContext.getApplicationContext()
        if (appContext.isORMEnabled() && c.isPersistent()) ORMUtil.getSession(pageContext)
        return c
    }

    @Override
    @Throws(PageException::class)
    override fun _callWithNamedValues(pageContext: PageContext?, values: Struct?, doIncludePath: Boolean): Object? {
        UDFUtil.argumentCollection(values, getFunctionArguments())
        var value: Object = values.get(propName, null)
        val c: Component = getComponent(pageContext)
        if (value == null) {
            val keys: Array<Key?> = CollectionUtil.keys(values)
            value = if (keys.size == 1) {
                values.get(keys[0])
            } else throw ExpressionException("The parameter " + prop.getName().toString() + " to function " + getFunctionName().toString() + " is required but was not passed in.")
        }
        c.getComponentScope().set(propName, cast(pageContext, arguments!!.get(0), value, 1))

        // make sure it is reconized that set is called by hibernate
        // if(component.isPersistent())ORMUtil.getSession(pageContext);
        val appContext: ApplicationContext = pageContext.getApplicationContext()
        if (appContext.isORMEnabled() && c.isPersistent()) ORMUtil.getSession(pageContext)
        return c
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

    @Override
    @Throws(Throwable::class)
    fun implementation(pageContext: PageContext?): Object? {
        return null
    }

    companion object {
        /**
         *
         */
        private const val serialVersionUID = 378348754607851563L
        private val VALIDATE_PARAMS: Collection.Key? = KeyImpl.getInstance("validateParams")
    }
}