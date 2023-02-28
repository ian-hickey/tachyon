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
package lucee.transformer.bytecode.reflection

import java.lang.reflect.InvocationTargetException

/**
 * A `Method` provides information about, and access to, a single method on a class or
 * interface. The reflected method may be a class method or an instance method (including an
 * abstract method).
 *
 *
 *
 * A `Method` permits widening conversions to occur when matching the actual parameters to
 * invoke with the underlying method's formal parameters, but it throws an
 * `IllegalArgumentException` if a narrowing conversion would occur.
 *
 * @see Member
 *
 * @see java.lang.Class
 *
 * @see java.lang.Class.getMethods
 * @see java.lang.Class.getMethod
 * @see java.lang.Class.getDeclaredMethods
 * @see java.lang.Class.getDeclaredMethod
 */
abstract class ASMMethod(declaringClass: Class?, parameterTypes: Array<Class?>?) {
    private var exceptionTypes: Array<Class?>?
    private var returnType: Class? = null
    private val parameterTypes: Array<Class?>?

    // private int modifiers;
    private val clazz: Class?

    /**
     * Returns the `Class` object representing the class or interface that declares the method
     * represented by this `Method` object.
     */
    fun getDeclaringClass(): Class<*>? {
        return clazz
    }

    /**
     * Returns the name of the method represented by this `Method` object, as a `String`.
     */
    abstract fun getName(): String?

    /**
     * Returns the Java language modifiers for the method represented by this `Method` object, as
     * an integer. The `Modifier` class should be used to decode the modifiers.
     *
     * @see Modifier
     */
    abstract fun getModifiers(): Int

    /**
     * Returns a `Class` object that represents the formal return type of the method represented
     * by this `Method` object.
     *
     * @return the return type for the method this object represents
     */
    fun getReturnType(): Class<*>? {
        if (returnType == null) {
            returnType = ClassUtil.loadClass(getReturnTypeAsString(), null)
            if (returnType == null) initAddionalParams()
        }
        return returnType
    }

    protected fun _getReturnType(): Class<*>? {
        if (returnType == null) initAddionalParams()
        return returnType
    }

    abstract fun getReturnTypeAsString(): String?

    /**
     * Returns an array of `Class` objects that represent the formal parameter types, in
     * declaration order, of the method represented by this `Method` object. Returns an array of
     * length 0 if the underlying method takes no parameters.
     *
     * @return the parameter types for the method this object represents
     */
    fun getParameterTypes(): Array<Class<*>?>? {
        return parameterTypes.clone()
    }

    /**
     * Returns an array of `Class` objects that represent the types of the exceptions declared to
     * be thrown by the underlying method represented by this `Method` object. Returns an array of
     * length 0 if the method declares no exceptions in its `throws` clause.
     *
     * @return the exception types declared as being thrown by the method this object represents
     */
    fun getExceptionTypes(): Array<Class<*>?>? {
        if (exceptionTypes == null) initAddionalParams()
        return exceptionTypes.clone()
    }

    private fun initAddionalParams() {
        try {
            val m: Method = clazz.getMethod(getName(), getParameterTypes())
            exceptionTypes = m.getExceptionTypes()
            returnType = m.getReturnType()
        } catch (e: Exception) {
            LogUtil.log(ASMMethod::class.java.getName(), e)
        }
    }

    /**
     * Compares this `Method` against the specified object. Returns true if the objects are the
     * same. Two `Methods` are the same if they were declared by the same class and have the same
     * name and formal parameter types and return type.
     */
    @Override
    override fun equals(obj: Object?): Boolean {
        if (this === obj) return true
        if (obj !is ASMMethod) return false
        val other = obj as ASMMethod?
        return (getModifiers() == other!!.getModifiers() && getName()!!.equals(other.getName()) && clazz.equals(other.clazz) && returnType.equals(other.returnType)
                && eq(getExceptionTypes(), other.getExceptionTypes()) && eq(parameterTypes, other.parameterTypes))
    }

    private fun eq(left: Array<Class?>?, right: Array<Class?>?): Boolean {
        val l = left?.size ?: 0
        val r = right?.size ?: 0
        if (l != r) return false
        if (l == 0) return true
        for (i in left.indices) {
            if (!left!![i].equals(right!![i])) return false
        }
        return true
    }

    /**
     * Invokes the underlying method represented by this `Method` object, on the specified object
     * with the specified parameters. Individual parameters are automatically unwrapped to match
     * primitive formal parameters, and both primitive and reference parameters are subject to method
     * invocation conversions as necessary.
     *
     *
     *
     * If the underlying method is static, then the specified `obj` argument is ignored. It may be
     * null.
     *
     *
     *
     * If the number of formal parameters required by the underlying method is 0, the supplied
     * `args` array may be of length 0 or null.
     *
     *
     *
     * If the underlying method is an instance method, it is invoked using dynamic method lookup as
     * documented in The Java Language Specification, Second Edition, section 15.12.4.4; in particular,
     * overriding based on the runtime type of the target object will occur.
     *
     *
     *
     * If the underlying method is static, the class that declared the method is initialized if it has
     * not already been initialized.
     *
     *
     *
     * If the method completes normally, the value it returns is returned to the caller of invoke; if
     * the value has a primitive type, it is first appropriately wrapped in an object. However, if the
     * value has the type of an array of a primitive type, the elements of the array are *not*
     * wrapped in objects; in other words, an array of primitive type is returned. If the underlying
     * method return type is void, the invocation returns null.
     *
     * @param obj the object the underlying method is invoked from
     * @param args the arguments used for the method call
     * @return the result of dispatching the method represented by this object on `obj` with
     * parameters `args`
     *
     * @exception IllegalAccessException if this `Method` object enforces Java language access
     * control and the underlying method is inaccessible.
     * @exception IllegalArgumentException if the method is an instance method and the specified object
     * argument is not an instance of the class or interface declaring the underlying
     * method (or of a subclass or implementor thereof); if the number of actual and
     * formal parameters differ; if an unwrapping conversion for primitive arguments
     * fails; or if, after possible unwrapping, a parameter value cannot be converted to
     * the corresponding formal parameter type by a method invocation conversion.
     * @exception InvocationTargetException if the underlying method throws an exception.
     * @exception NullPointerException if the specified object is null and the method is an instance
     * method.
     * @exception ExceptionInInitializerError if the initialization provoked by this method fails.
     */
    @Throws(Throwable::class)
    abstract operator fun invoke(obj: Object?, args: Array<Object?>?): Object? // IllegalAccessException, IllegalArgumentException, InvocationTargetException;

    /**
     * Package-private constructor used by ReflectAccess to enable instantiation of these objects in
     * Java code from the java.lang package via sun.reflect.LangReflectAccess.
     */
    init {
        clazz = declaringClass
        this.parameterTypes = parameterTypes
    }
}