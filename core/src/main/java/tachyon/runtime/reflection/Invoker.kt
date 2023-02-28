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
package tachyon.runtime.reflection

import java.lang.reflect.Constructor

/**
 * To invoke an Object in different ways
 */
object Invoker {
    private var lastMethods: Array<Method?>?
    private var lastClass: Class? = null

    /**
     * @param clazz
     * @param parameters
     * @return new Instance
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    @Throws(NoSuchMethodException::class, IllegalArgumentException::class, InstantiationException::class, IllegalAccessException::class, InvocationTargetException::class)
    fun newInstance(clazz: Class?, parameters: Array<Object?>?): Object? {
        val pair: ConstructorParameterPair? = getConstructorParameterPairIgnoreCase(clazz, parameters)
        return pair.getConstructor().newInstance(pair.getParameters())
    }

    /**
     * search the matching constructor to defined parameter list, also translate parameters for matching
     *
     * @param clazz class to get constructo from
     * @param parameters parameter for the constructor
     * @return Constructor parameter pair
     * @throws NoSuchMethodException
     */
    @Throws(NoSuchMethodException::class)
    fun getConstructorParameterPairIgnoreCase(clazz: Class?, parameters: Array<Object?>?): ConstructorParameterPair? {
        // set all values
        // Class objectClass=object.getClass();
        var parameters: Array<Object?>? = parameters
        if (parameters == null) parameters = arrayOfNulls<Object?>(0)

        // set parameter classes
        val parameterClasses: Array<Class?> = arrayOfNulls<Class?>(parameters!!.size)
        for (i in parameters.indices) {
            parameterClasses[i] = parameters!![i].getClass()
        }

        // search right method
        val constructor: Array<Constructor?> = clazz.getConstructors()
        for (mode in 0..1) {
            outer@ for (i in constructor.indices) {
                val c: Constructor? = constructor[i]
                val paramTrg: Array<Class?> = c.getParameterTypes()
                // Same Parameter count
                if (parameterClasses.size == paramTrg.size) {
                    for (y in parameterClasses.indices) {
                        if (mode == 0 && parameterClasses[y] !== primitiveToWrapperType(paramTrg[y])) {
                            continue@outer
                        } else if (mode == 1) {
                            val o: Object = compareClasses(parameters!![y], paramTrg[y])
                                    ?: continue@outer
                            parameters[y] = o
                            parameterClasses[y] = o.getClass()
                        }
                    }
                    return ConstructorParameterPair(c, parameters)
                }
            }
        }

        // Exeception
        var parameter = ""
        for (i in parameterClasses.indices) {
            if (i != 0) parameter += ", "
            parameter += parameterClasses[i].getName()
        }
        throw NoSuchMethodException("class constructor " + clazz.getName().toString() + "(" + parameter + ") doesn't exist")
    }

    /**
     * call of a method from given object
     *
     * @param object object to call method from
     * @param methodName name of the method to call
     * @param parameters parameter for method
     * @return return value of the method
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    @Throws(NoSuchMethodException::class, IllegalArgumentException::class, IllegalAccessException::class, InvocationTargetException::class)
    fun callMethod(`object`: Object?, methodName: String?, parameters: Array<Object?>?): Object? {
        val pair: MethodParameterPair? = getMethodParameterPairIgnoreCase(`object`.getClass(), methodName, parameters)
        return pair.getMethod().invoke(`object`, pair.getParameters())
    }

    /**
     * search the matching method to defined Method Name, also translate parameters for matching
     *
     * @param objectClass class object where searching method from
     * @param methodName name of the method to search
     * @param parameters whished parameter list
     * @return pair with method matching and parameterlist matching
     * @throws NoSuchMethodException
     */
    @Throws(NoSuchMethodException::class)
    fun getMethodParameterPairIgnoreCase(objectClass: Class?, methodName: String?, parameters: Array<Object?>?): MethodParameterPair? {
        // set all values
        var parameters: Array<Object?>? = parameters
        if (parameters == null) parameters = arrayOfNulls<Object?>(0)

        // set parameter classes
        val parameterClasses: Array<Class?> = arrayOfNulls<Class?>(parameters!!.size)
        for (i in parameters.indices) {
            parameterClasses[i] = parameters!![i].getClass()
        }

        // search right method
        var methods: Array<Method?>? = null
        methods = if (lastClass != null && lastClass.equals(objectClass)) {
            lastMethods
        } else {
            objectClass.getDeclaredMethods()
        }
        lastClass = objectClass
        lastMethods = methods
        // Method[] methods=objectClass.getMethods();
        // Method[] methods=objectClass.getDeclaredMethods();

        // methods=objectClass.getDeclaredMethods();
        for (mode in 0..1) {
            outer@ for (i in methods.indices) {
                val method: Method? = methods!![i]
                // Same Name
                if (method.getName().equalsIgnoreCase(methodName)) {
                    val paramTrg: Array<Class?> = method.getParameterTypes()
                    // Same Parameter count
                    if (parameterClasses.size == paramTrg.size) {
                        // if(parameterClasses.length==0)return m;
                        for (y in parameterClasses.indices) {
                            if (mode == 0 && parameterClasses[y] !== primitiveToWrapperType(paramTrg[y])) {
                                continue@outer
                            } else if (mode == 1) {
                                val o: Object = compareClasses(parameters!![y], paramTrg[y])
                                        ?: continue@outer
                                parameters[y] = o
                                parameterClasses[y] = o.getClass()
                            }
                            // if(parameterClasses.length-1==y) return m;
                        }
                        return MethodParameterPair(method, parameters)
                    }
                }
            }
        }

        // Exeception
        var parameter = ""
        for (i in parameterClasses.indices) {
            if (i != 0) parameter += ", "
            parameter += parameterClasses[i].getName()
        }
        throw NoSuchMethodException("method " + methodName + "(" + parameter + ") doesn't exist in class " + objectClass.getName())
    }

    /**
     * compare parameter with whished parameter class and convert parameter to whished type
     *
     * @param parameter parameter to compare
     * @param trgClass whished type of the parameter
     * @return converted parameter (to whished type) or null
     */
    private fun compareClasses(parameter: Object?, trgClass: Class?): Object? {
        var parameter: Object? = parameter
        var trgClass: Class? = trgClass
        val srcClass: Class = parameter.getClass()
        trgClass = primitiveToWrapperType(trgClass)
        try {
            if (parameter is ObjectWrap) parameter = (parameter as ObjectWrap?).getEmbededObject()

            // parameter is already ok
            if (srcClass === trgClass) return parameter else if (instaceOf(srcClass, trgClass)) {
                return parameter
            } else if (trgClass.getName().equals("java.lang.String")) {
                return Caster.toString(parameter)
            } else if (trgClass.getName().equals("java.lang.Boolean")) {
                return Caster.toBoolean(parameter)
            } else if (trgClass.getName().equals("java.lang.Byte")) {
                return Byte.valueOf(Caster.toString(parameter))
            } else if (trgClass.getName().equals("java.lang.Character")) {
                val str: String = Caster.toString(parameter)
                return if (str.length() === 1) Character.valueOf(str.toCharArray().get(0)) else null
            } else if (trgClass.getName().equals("java.lang.Short")) {
                return Short.valueOf(Caster.toIntValue(parameter) as Short)
            } else if (trgClass.getName().equals("java.lang.Integer")) {
                return Integer.valueOf(Caster.toIntValue(parameter))
            } else if (trgClass.getName().equals("java.lang.Long")) {
                return Long.valueOf(Caster.toDoubleValue(parameter) as Long)
            } else if (trgClass.getName().equals("java.lang.Float")) {
                return Float.valueOf(Caster.toDoubleValue(parameter) as Float)
            } else if (trgClass.getName().equals("java.lang.Double")) {
                return Caster.toDouble(parameter)
            }
        } catch (e: PageException) {
            return null
        }
        return null
    }

    /**
     * @param srcClass
     * @param trgClass
     * @return is instance of or not
     */
    private fun instaceOf(srcClass: Class?, trgClass: Class?): Boolean {
        var srcClass: Class? = srcClass
        while (srcClass != null) {
            if (srcClass === trgClass) return true
            srcClass = primitiveToWrapperType(srcClass.getSuperclass())
        }
        return false
    }

    /**
     * cast a primitive type class definition to his object reference type
     *
     * @param clazz class object to check and convert if it is of primitive type
     * @return object reference class object
     */
    private fun primitiveToWrapperType(clazz: Class?): Class? {
        // boolean, byte, char, short, int, long, float, and double
        if (clazz == null) return null else if (clazz.isPrimitive()) {
            if (clazz.getName().equals("boolean")) return Boolean::class.java else if (clazz.getName().equals("byte")) return Byte::class.java else if (clazz.getName().equals("char")) return Character::class.java else if (clazz.getName().equals("short")) return Short::class.java else if (clazz.getName().equals("int")) return Integer::class.java else if (clazz.getName().equals("long")) return Long::class.java else if (clazz.getName().equals("float")) return Float::class.java else if (clazz.getName().equals("double")) return Double::class.java
        }
        return clazz
    }

    /**
     * to invoke a getter Method of an Object
     *
     * @param o Object to invoke method from
     * @param prop Name of the Method without get
     * @return return Value of the getter Method
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    @Throws(SecurityException::class, NoSuchMethodException::class, IllegalArgumentException::class, IllegalAccessException::class, InvocationTargetException::class)
    fun callGetter(o: Object?, prop: String?): Object? {
        var prop = prop
        prop = "get$prop"
        val c: Class = o.getClass()
        val m: Method = getMethodParameterPairIgnoreCase(c, prop, null).getMethod()

        // Method m=getMethodIgnoreCase(c,prop,null);
        if (m.getReturnType().getName().equals("void")) throw NoSuchMethodException("invalid return Type, method [" + m.getName().toString() + "] can't have return type void")
        return m.invoke(o, arrayOfNulls<Object?>(0))
    }

    /**
     * to invoke a setter Method of an Object
     *
     * @param o Object to invoke method from
     * @param prop Name of the Method without get
     * @param value Value to set to the Method
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    @Throws(SecurityException::class, NoSuchMethodException::class, IllegalArgumentException::class, IllegalAccessException::class, InvocationTargetException::class)
    fun callSetter(o: Object?, prop: String?, value: Object?) {
        var prop = prop
        prop = "set" + StringUtil.ucFirst(prop)
        val c: Class = o.getClass()
        // Class[] cArg=new Class[]{value.getClass()};
        val oArg: Array<Object?> = arrayOf<Object?>(value)
        val mp: MethodParameterPair? = getMethodParameterPairIgnoreCase(c, prop, oArg)
        // Method m=getMethodIgnoreCase(c,prop,cArg);
        if (!mp.getMethod().getReturnType().getName().equals("void")) throw NoSuchMethodException("invalid return Type, method [" + mp.getMethod().getName().toString() + "] must have return type void, now [" + mp.getMethod().getReturnType().getName().toString() + "]")
        mp.getMethod().invoke(o, mp.getParameters())
    }

    /**
     * to get a visible Property of an object
     *
     * @param o Object to invoke
     * @param prop property to call
     * @return property value
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    @Throws(NoSuchFieldException::class, IllegalArgumentException::class, IllegalAccessException::class)
    fun getProperty(o: Object?, prop: String?): Object? {
        val f: Field? = getFieldIgnoreCase(o.getClass(), prop)
        return f.get(o)
    }

    /**
     * assign a value to a visible property of an object
     *
     * @param o Object to assign value to his property
     * @param prop name of property
     * @param value Value to assign
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    @Throws(IllegalArgumentException::class, IllegalAccessException::class, NoSuchFieldException::class)
    fun setProperty(o: Object?, prop: String?, value: Object?) {
        getFieldIgnoreCase(o.getClass(), prop).set(o, value)
    }

    /**
     * same like method getField from Class but ignore case from field name
     *
     * @param c class to search the field
     * @param name name to search
     * @return Matching Field
     * @throws NoSuchFieldException
     */
    @Throws(NoSuchFieldException::class)
    fun getFieldIgnoreCase(c: Class?, name: String?): Field? {
        val fields: Array<Field?> = c.getFields()
        for (i in fields.indices) {
            val f: Field? = fields[i]
            // Same Name
            if (f.getName().equalsIgnoreCase(name)) {
                return f
            }
        }
        throw NoSuchFieldException("Field doesn't exist")
    }

    /**
     * call of a static method of a Class
     *
     * @param staticClass class how contains method to invoke
     * @param methodName method name to invoke
     * @param values Arguments for the Method
     * @return return value from method
     * @throws PageException
     */
    @Throws(PageException::class)
    fun callStaticMethod(staticClass: Class?, methodName: String?, values: Array<Object?>?): Object? {
        var values: Array<Object?>? = values
        if (values == null) values = arrayOfNulls<Object?>(0)
        val mp: MethodParameterPair
        mp = try {
            getMethodParameterPairIgnoreCase(staticClass, methodName, values)
        } catch (e: NoSuchMethodException) {
            throw Caster.toPageException(e)
        }
        return try {
            mp.getMethod().invoke(null, mp.getParameters())
        } catch (e: InvocationTargetException) {
            val target: Throwable = e.getTargetException()
            if (target is PageException) throw target as PageException
            throw Caster.toPageException(e.getTargetException())
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }
}