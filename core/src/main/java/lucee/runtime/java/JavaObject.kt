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
package lucee.runtime.java

import java.lang.reflect.Field

/**
 * class to handle initialising and call native object from lucee
 */
class JavaObject : Objects, ObjectWrap {
    private var clazz: Class?
    var isInitalized = false
        private set
    private var `object`: Object? = null

    @Transient
    private var _variableUtil: VariableUtil?

    /**
     * constructor with className to load
     *
     * @param variableUtil
     * @param clazz
     * @throws ExpressionException
     */
    constructor(variableUtil: VariableUtil?, clazz: Class?) {
        _variableUtil = variableUtil
        this.clazz = clazz
    }

    constructor(variableUtil: VariableUtil?, `object`: Object?) {
        _variableUtil = variableUtil
        clazz = `object`.getClass()
        this.`object` = `object`
        isInitalized = `object` != null
    }

    @Throws(PageException::class)
    operator fun get(pc: PageContext?, propertyName: String?): Object? {
        if (isInitalized) {
            return variableUtil(pc).get(pc, `object`, propertyName)
        }
        if (VariableUtilImpl.doLogReflectionCalls()) LogUtil.log(pc, Log.LEVEL_INFO, "reflection", "get-property:" + propertyName + " from class " + Caster.toTypeName(clazz))
        // Check Field
        val fields: Array<Field?> = Reflector.getFieldsIgnoreCase(clazz, propertyName, null)
        if (!ArrayUtil.isEmpty(fields) && Modifier.isStatic(fields[0].getModifiers())) {
            return try {
                fields[0].get(null)
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
        }
        // Getter
        val mi: MethodInstance = Reflector.getGetterEL(clazz, propertyName)
        if (mi != null) {
            if (Modifier.isStatic(mi.getMethod().getModifiers())) {
                return try {
                    mi.invoke(null)
                } catch (e: IllegalAccessException) {
                    throw Caster.toPageException(e)
                } catch (e: InvocationTargetException) {
                    throw Caster.toPageException(e.getTargetException())
                }
            }
        }
        // male Instance
        return variableUtil(pc).get(pc, init(), propertyName)
    }

    private fun variableUtil(pc: PageContext?): VariableUtil? {
        return if (_variableUtil != null) _variableUtil else ThreadLocalPageContext.get(pc).getVariableUtil()
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Collection.Key?): Object? {
        return get(pc, key.getString())
    }

    operator fun get(pc: PageContext?, propertyName: String?, defaultValue: Object?): Object? {
        if (isInitalized) {
            return variableUtil(pc).get(pc, `object`, propertyName, defaultValue)
        }
        if (VariableUtilImpl.doLogReflectionCalls()) LogUtil.log(pc, Log.LEVEL_INFO, "reflection", "get-property:" + propertyName + " from class " + Caster.toTypeName(clazz))

        // Field
        val fields: Array<Field?> = Reflector.getFieldsIgnoreCase(clazz, propertyName, null)
        if (!ArrayUtil.isEmpty(fields) && Modifier.isStatic(fields[0].getModifiers())) {
            try {
                return fields[0].get(null)
            } catch (e: Exception) {
            }
        }
        // Getter
        val mi: MethodInstance = Reflector.getGetterEL(clazz, propertyName)
        if (mi != null) {
            if (Modifier.isStatic(mi.getMethod().getModifiers())) {
                try {
                    return mi.invoke(null)
                } catch (e: Exception) {
                }
            }
        }
        return try {
            variableUtil(pc).get(pc, init(), propertyName, defaultValue)
        } catch (e1: PageException) {
            defaultValue
        }
    }

    @Override
    operator fun get(pc: PageContext?, key: Collection.Key?, defaultValue: Object?): Object? {
        return get(pc, key.getString(), defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, propertyName: Collection.Key?, value: Object?): Object? {
        if (isInitalized) {
            return (variableUtil(pc) as VariableUtilImpl?).set(pc, `object`, propertyName, value)
        }
        if (VariableUtilImpl.doLogReflectionCalls()) LogUtil.log(pc, Log.LEVEL_INFO, "reflection", "set-property:" + propertyName + " in class " + Caster.toTypeName(clazz))

        // Field
        val fields: Array<Field?> = Reflector.getFieldsIgnoreCase(clazz, propertyName.getString(), null)
        if (!ArrayUtil.isEmpty(fields) && Modifier.isStatic(fields[0].getModifiers())) {
            try {
                fields[0].set(null, value)
                return value
            } catch (e: Exception) {
                Caster.toPageException(e)
            }
        }
        // Getter
        val mi: MethodInstance = Reflector.getSetter(clazz, propertyName.getString(), value, null)
        if (mi != null) {
            if (Modifier.isStatic(mi.getMethod().getModifiers())) {
                return try {
                    mi.invoke(null)
                } catch (e: IllegalAccessException) {
                    throw Caster.toPageException(e)
                } catch (e: InvocationTargetException) {
                    throw Caster.toPageException(e.getTargetException())
                }
            }
        }
        return (variableUtil(pc) as VariableUtilImpl?).set(pc, init(), propertyName, value)
    }

    @Override
    fun setEL(pc: PageContext?, propertyName: Collection.Key?, value: Object?): Object? {
        if (isInitalized) {
            return variableUtil(pc).setEL(pc, `object`, propertyName, value)
        }
        if (VariableUtilImpl.doLogReflectionCalls()) LogUtil.log(pc, Log.LEVEL_INFO, "reflection", "set-property:" + propertyName + " in class " + Caster.toTypeName(clazz))

        // Field
        val fields: Array<Field?> = Reflector.getFieldsIgnoreCase(clazz, propertyName.getString(), null)
        if (!ArrayUtil.isEmpty(fields) && Modifier.isStatic(fields[0].getModifiers())) {
            try {
                fields[0].set(null, value)
            } catch (e: Exception) {
            }
            return value
        }
        // Getter
        val mi: MethodInstance = Reflector.getSetter(clazz, propertyName.getString(), value, null)
        if (mi != null) {
            if (Modifier.isStatic(mi.getMethod().getModifiers())) {
                try {
                    return mi.invoke(null)
                } catch (e: Exception) {
                }
            }
        }
        return try {
            variableUtil(pc).setEL(pc, init(), propertyName, value)
        } catch (e1: PageException) {
            value
        }
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, methodName: String?, arguments: Array<Object?>?): Object? {
        var arguments: Array<Object?>? = arguments
        if (arguments == null) arguments = arrayOfNulls<Object?>(0)
        if (VariableUtilImpl.doLogReflectionCalls()) LogUtil.log(pc, Log.LEVEL_INFO, "reflection", "call-method:" + methodName + " from class " + Caster.toTypeName(clazz))

        // edge cases
        if (methodName.equalsIgnoreCase("init")) {
            return init(arguments)
        } else if (methodName.equalsIgnoreCase("getClass")) {
            return clazz
        } else if (isInitalized) {
            return Reflector.callMethod(`object`, methodName, arguments)
        }
        return try {
            // get method
            val mi: MethodInstance = Reflector.getMethodInstance(this, clazz, KeyImpl.init(methodName), arguments)
            // call static method if exist
            if (Modifier.isStatic(mi.getMethod().getModifiers())) {
                return mi.invoke(null)
            }
            if (arguments!!.size == 0 && methodName.equalsIgnoreCase("getClass")) {
                clazz
            } else mi.invoke(init())

            // invoke constructor and call instance method
        } catch (e: InvocationTargetException) {
            val target: Throwable = e.getTargetException()
            if (target is PageException) throw target as PageException
            throw Caster.toPageException(e.getTargetException())
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, methodName: Collection.Key?, arguments: Array<Object?>?): Object? {
        return call(pc, methodName.getString(), arguments)
    }

    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, methodName: String?, args: Struct?): Object? {
        val it: Iterator<Object?> = args.valueIterator()
        val values: List<Object?> = ArrayList<Object?>()
        while (it.hasNext()) {
            values.add(it.next())
        }
        return call(pc, methodName, values.toArray(arrayOfNulls<Object?>(values.size())))
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, methodName: Collection.Key?, args: Struct?): Object? {
        return callWithNamedValues(pc, methodName.getString(), args)
    }

    /**
     * initialize method (default no object)
     *
     * @return initialize object
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun init(): Object? {
        return init(arrayOfNulls<Object?>(0))
    }

    private fun init(defaultValue: Object?): Object? {
        return init(arrayOfNulls<Object?>(0), defaultValue)
    }

    /**
     * initialize method
     *
     * @param arguments
     * @return Initalised Object
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun init(arguments: Array<Object?>?): Object? {
        `object` = Reflector.callConstructor(clazz, arguments)
        isInitalized = true
        return `object`
    }

    private fun init(arguments: Array<Object?>?, defaultValue: Object?): Object? {
        `object` = Reflector.callConstructor(clazz, arguments, defaultValue)
        isInitalized = `object` !== defaultValue
        return `object`
    }

    @get:Throws(PageException::class)
    @get:Override
    val embededObject: Object?
        get() {
            if (`object` == null) init()
            return `object`
        }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, props: DumpProperties?): DumpData? {
        return try {
            DumpUtil.toDumpData(embededObject, pageContext, maxlevel, props)
        } catch (e: PageException) {
            DumpUtil.toDumpData(clazz, pageContext, maxlevel, props)
        }
    }

    /**
     * @return the containing Class
     */
    fun getClazz(): Class? {
        return clazz
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        return Caster.toString(embededObject)
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return try {
            Caster.toString(embededObject, defaultValue)
        } catch (e: PageException) {
            defaultValue
        }
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return Caster.toBooleanValue(embededObject)
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return try {
            Caster.toBoolean(embededObject, defaultValue)
        } catch (e: PageException) {
            defaultValue
        }
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return Caster.toDoubleValue(embededObject)
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return try {
            Caster.toDoubleValue(embededObject, true, defaultValue)
        } catch (e: PageException) {
            defaultValue
        }
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        return Caster.toDatetime(embededObject, null)
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return try {
            DateCaster.toDateAdvanced(embededObject, DateCaster.CONVERTING_TYPE_OFFSET, null, defaultValue)
        } catch (e: PageException) {
            defaultValue
        }
    }

    @Override
    fun getEmbededObject(def: Object?): Object? {
        if (`object` == null) init(def)
        return `object`
    }

    /**
     * @return the object
     */
    val `object`: Object?

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), if (castToBooleanValue()) Boolean.TRUE else Boolean.FALSE, if (b) Boolean.TRUE else Boolean.FALSE)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), castToDateTime() as Date?, dt as Date?)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), castToDoubleValue(), Double.valueOf(d))
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), castToString(), str)
    }

    companion object {
        private const val serialVersionUID = -3716657460843769960L
    }
}