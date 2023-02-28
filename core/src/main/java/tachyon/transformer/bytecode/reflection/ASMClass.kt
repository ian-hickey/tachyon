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
package tachyon.transformer.bytecode.reflection

import java.io.InputStream

class ASMClass(private val name: String?, methods: Map<String?, ASMMethod?>?) : java.io.Serializable {
    private val methods: Map<String?, ASMMethod?>?

    /**
     * Converts the object to a string. The string representation is the string "class" or "interface",
     * followed by a space, and then by the fully qualified name of the class in the format returned by
     * `getName`. If this `Class` object represents a primitive type, this method returns
     * the name of the primitive type. If this `Class` object represents void this method returns
     * "void".
     *
     * @return a string representation of this class object.
     */
    @Override
    override fun toString(): String {
        return (if (isInterface()) "interface " else if (isPrimitive()) "" else "class ") + getName()
    }

    /**
     * Creates a new instance of the class represented by this `Class` object. The class is
     * instantiated as if by a `new` expression with an empty argument list. The class is
     * initialized if it has not already been initialized.
     *
     *
     *
     * Note that this method propagates any exception thrown by the nullary constructor, including a
     * checked exception. Use of this method effectively bypasses the compile-time exception checking
     * that would otherwise be performed by the compiler. The
     * [Constructor.newInstance][java.lang.reflect.Constructor.newInstance]
     * method avoids this problem by wrapping any exception thrown by the constructor in a (checked)
     * [java.lang.reflect.InvocationTargetException].
     *
     * @return a newly allocated instance of the class represented by this object.
     * @exception IllegalAccessException if the class or its nullary constructor is not accessible.
     * @exception InstantiationException if this `Class` represents an abstract class, an
     * interface, an array class, a primitive type, or void; or if the class has no
     * nullary constructor; or if the instantiation fails for some other reason.
     * @exception ExceptionInInitializerError if the initialization provoked by this method fails.
     * @exception SecurityException If a security manager, *s*, is present and any of the following
     * conditions is met:
     *
     *
     *
     *  * invocation of [                s.checkMemberAccess(this, Member.PUBLIC)][SecurityManager.checkMemberAccess] denies creation of new instances of this
     * class
     *
     *  * the caller's class loader is not the same as or an ancestor of the class
     * loader for the current class and invocation of
     * [s.checkPackageAccess()][SecurityManager.checkPackageAccess] denies access to
     * the package of this class
     *
     *
     */
    @Throws(InstantiationException::class, IllegalAccessException::class)
    fun newInstance(): Object? {
        _throw()
        return null
    }

    /**
     * Determines if the specified `Class` object represents an interface type.
     *
     * @return `true` if this object represents an interface; `false` otherwise.
     */
    fun isInterface(): Boolean {
        _throw()
        return false
    }

    /**
     * Determines if this `Class` object represents an array class.
     *
     * @return `true` if this object represents an array class; `false` otherwise.
     * @since JDK1.1
     */
    fun isArray(): Boolean {
        _throw()
        return false
    }

    /**
     * Determines if the specified `Class` object represents a primitive type.
     *
     *
     *
     * There are nine predefined `Class` objects to represent the eight primitive types and void.
     * These are created by the Java Virtual Machine, and have the same names as the primitive types
     * that they represent, namely `boolean`, `byte`, `char`, `short`,
     * `int`, `long`, `float`, and `double`.
     *
     *
     *
     * These objects may only be accessed via the following public static final variables, and are the
     * only `Class` objects for which this method returns `true`.
     *
     * @return true if and only if this class represents a primitive type
     *
     * @see java.lang.Boolean.TYPE
     *
     * @see java.lang.Character.TYPE
     *
     * @see java.lang.Byte.TYPE
     *
     * @see java.lang.Short.TYPE
     *
     * @see java.lang.Integer.TYPE
     *
     * @see java.lang.Long.TYPE
     *
     * @see java.lang.Float.TYPE
     *
     * @see java.lang.Double.TYPE
     *
     * @see java.lang.Void.TYPE
     *
     * @since JDK1.1
     */
    fun isPrimitive(): Boolean {
        _throw()
        return false
    }

    /**
     * Returns the name of the entity (class, interface, array class, primitive type, or void)
     * represented by this `Class` object, as a `String`.
     *
     *
     *
     * If this class object represents a reference type that is not an array type then the binary name
     * of the class is returned, as specified by the Java Language Specification, Second Edition.
     *
     *
     *
     * If this class object represents a primitive type or void, then the name returned is a
     * `String` equal to the Java language keyword corresponding to the primitive type or void.
     *
     *
     *
     * If this class object represents a class of arrays, then the internal form of the name consists of
     * the name of the element type preceded by one or more '`[`' characters representing the
     * depth of the array nesting. The encoding of element type names is as follows:
     *
     * <blockquote>
     * <table summary="Element types and encodings">
     * <tr>
     * <th>Element Type
    </th> * <th>&nbsp;&nbsp;&nbsp;
    </th> * <th>Encoding
    </th></tr> * <tr>
     * <td>boolean
    </td> * <td>&nbsp;&nbsp;&nbsp;
    </td> * <td align=center>Z
    </td></tr> * <tr>
     * <td>byte
    </td> * <td>&nbsp;&nbsp;&nbsp;
    </td> * <td align=center>B
    </td></tr> * <tr>
     * <td>char
    </td> * <td>&nbsp;&nbsp;&nbsp;
    </td> * <td align=center>C
    </td></tr> * <tr>
     * <td>class or interface
    </td> * <td>&nbsp;&nbsp;&nbsp;
    </td> * <td align=center>L*classname*;
    </td></tr> * <tr>
     * <td>double
    </td> * <td>&nbsp;&nbsp;&nbsp;
    </td> * <td align=center>D
    </td></tr> * <tr>
     * <td>float
    </td> * <td>&nbsp;&nbsp;&nbsp;
    </td> * <td align=center>F
    </td></tr> * <tr>
     * <td>int
    </td> * <td>&nbsp;&nbsp;&nbsp;
    </td> * <td align=center>I
    </td></tr> * <tr>
     * <td>long
    </td> * <td>&nbsp;&nbsp;&nbsp;
    </td> * <td align=center>J
    </td></tr> * <tr>
     * <td>short
    </td> * <td>&nbsp;&nbsp;&nbsp;
    </td> * <td align=center>S
    </td></tr></table> *
    </blockquote> *
     *
     *
     *
     * The class or interface name *classname* is the binary name of the class specified above.
     *
     *
     *
     * Examples: <blockquote>
     *
     * <pre>
     * String.class.getName()
     * returns "java.lang.String"
     * byte.class.getName()
     * returns "byte"
     * (new Object[3]).getClass().getName()
     * returns "[Ljava.lang.Object;"
     * (new int[3][4][5][6][7][8][9]).getClass().getName()
     * returns "[[[[[[[I"
    </pre> *
     *
    </blockquote> *
     *
     * @return the name of the class or interface represented by this object.
     */
    fun getName(): String? {
        _throw()
        return name
    }

    /**
     * Returns the class loader for the class. Some implementations may use null to represent the
     * bootstrap class loader. This method will return null in such implementations if this class was
     * loaded by the bootstrap class loader.
     *
     *
     *
     * If a security manager is present, and the caller's class loader is not null and the caller's
     * class loader is not the same as or an ancestor of the class loader for the class whose class
     * loader is requested, then this method calls the security manager's `checkPermission` method
     * with a `RuntimePermission("getClassLoader")` permission to ensure it's ok to access the
     * class loader for the class.
     *
     *
     *
     * If this object represents a primitive type or void, null is returned.
     *
     * @return the class loader that loaded the class or interface represented by this object.
     * @throws SecurityException if a security manager exists and its `checkPermission` method
     * denies access to the class loader for the class.
     * @see java.lang.ClassLoader
     *
     * @see SecurityManager.checkPermission
     *
     * @see java.lang.RuntimePermission
     */
    fun getClassLoader(): ClassLoader? {
        _throw()
        return null
    }

    /**
     * Returns the `Class` representing the superclass of the entity (class, interface, primitive
     * type or void) represented by this `Class`. If this `Class` represents either the
     * `Object` class, an interface, a primitive type, or void, then null is returned. If this
     * object represents an array class then the `Class` object representing the `Object`
     * class is returned.
     *
     * @return the superclass of the class represented by this object.
     */
    fun getSuperclass(): Class? {
        _throw()
        return null
    }

    /**
     * Returns the `Type` representing the direct superclass of the entity (class, interface,
     * primitive type or void) represented by this `Class`.
     *
     *
     *
     * If the superclass is a parameterized type, the `Type` object returned must accurately
     * reflect the actual type parameters used in the source code. The parameterized type representing
     * the superclass is created if it had not been created before. See the declaration of
     * [ParameterizedType][java.lang.reflect.ParameterizedType] for the semantics of the creation
     * process for parameterized types. If this `Class` represents either the `Object`
     * class, an interface, a primitive type, or void, then null is returned. If this object represents
     * an array class then the `Class` object representing the `Object` class is returned.
     *
     * @throws GenericSignatureFormatError if the generic class signature does not conform to the format
     * specified in the Java Virtual Machine Specification, 3rd edition
     * @throws TypeNotPresentException if the generic superclass refers to a non-existent type
     * declaration
     * @throws MalformedParameterizedTypeException if the generic superclass refers to a parameterized
     * type that cannot be instantiated for any reason
     * @return the superclass of the class represented by this object
     * @since 1.5
     */
    fun getGenericSuperclass(): Type? {
        _throw()
        return null
    }

    /**
     * Gets the package for this class. The class loader of this class is used to find the package. If
     * the class was loaded by the bootstrap class loader the set of packages loaded from CLASSPATH is
     * searched to find the package of the class. Null is returned if no package object was created by
     * the class loader of this class.
     *
     *
     *
     * Packages have attributes for versions and specifications only if the information was defined in
     * the manifests that accompany the classes, and if the class loader created the package instance
     * with the attributes from the manifest.
     *
     * @return the package of the class, or null if no package information is available from the archive
     * or codebase.
     */
    fun getPackage(): Package? {
        _throw()
        return null
    }

    /**
     * Determines the interfaces implemented by the class or interface represented by this object.
     *
     *
     *
     * If this object represents a class, the return value is an array containing objects representing
     * all interfaces implemented by the class. The order of the interface objects in the array
     * corresponds to the order of the interface names in the `implements` clause of the
     * declaration of the class represented by this object. For example, given the declaration:
     * <blockquote> `class Shimmer implements FloorWax, DessertTopping { ... }` </blockquote>
     * suppose the value of `s` is an instance of `Shimmer`; the value of the expression:
     * <blockquote> `s.getClass().getInterfaces()[0]` </blockquote> is the `Class` object
     * that represents interface `FloorWax`; and the value of: <blockquote>
     * `s.getClass().getInterfaces()[1]` </blockquote> is the `Class` object that represents
     * interface `DessertTopping`.
     *
     *
     *
     * If this object represents an interface, the array contains objects representing all interfaces
     * extended by the interface. The order of the interface objects in the array corresponds to the
     * order of the interface names in the `extends` clause of the declaration of the interface
     * represented by this object.
     *
     *
     *
     * If this object represents a class or interface that implements no interfaces, the method returns
     * an array of length 0.
     *
     *
     *
     * If this object represents a primitive type or void, the method returns an array of length 0.
     *
     * @return an array of interfaces implemented by this class.
     */
    fun getInterfaces(): Array<Class?>? {
        _throw()
        return null
    }

    /**
     * Returns the `Class` representing the component type of an array. If this class does not
     * represent an array class this method returns null.
     *
     * @return the `Class` representing the component type of this class if this class is an array
     * @see java.lang.reflect.Array
     *
     * @since JDK1.1
     */
    fun getComponentType(): Class? {
        _throw()
        return null
    }

    /**
     * Returns the Java language modifiers for this class or interface, encoded in an integer. The
     * modifiers consist of the Java Virtual Machine's constants for `public`, `protected`,
     * `private`, `final`, `static`, `abstract` and `interface`; they
     * should be decoded using the methods of class `Modifier`.
     *
     *
     *
     * If the underlying class is an array class, then its `public`, `private` and
     * `protected` modifiers are the same as those of its component type. If this `Class`
     * represents a primitive type or void, its `public` modifier is always `true`, and its
     * `protected` and `private` modifiers are always `false`. If this object
     * represents an array class, a primitive type or void, then its `final` modifier is always
     * `true` and its interface modifier is always `false`. The values of its other
     * modifiers are not determined by this specification.
     *
     *
     *
     * The modifier encodings are defined in *The Java Virtual Machine Specification*, table 4.1.
     *
     * @return the `int` representing the modifiers for this class
     * @see java.lang.reflect.Modifier
     *
     * @since JDK1.1
     */
    fun getModifiers(): Int {
        _throw()
        return 0
    }

    /**
     * If the class or interface represented by this `Class` object is a member of another class,
     * returns the `Class` object representing the class in which it was declared. This method
     * returns null if this class or interface is not a member of any other class. If this `Class`
     * object represents an array class, a primitive type, or void,then this method returns null.
     *
     * @return the declaring class for this class
     * @since JDK1.1
     */
    fun getDeclaringClass(): Class? {
        _throw()
        return null
    }

    /**
     * Returns an array containing `Class` objects representing all the public classes and
     * interfaces that are members of the class represented by this `Class` object. This includes
     * public class and interface members inherited from superclasses and public class and interface
     * members declared by the class. This method returns an array of length 0 if this `Class`
     * object has no public member classes or interfaces. This method also returns an array of length 0
     * if this `Class` object represents a primitive type, an array class, or void.
     *
     * @return the array of `Class` objects representing the public members of this class
     * @exception SecurityException If a security manager, *s*, is present and any of the following
     * conditions is met:
     *
     *
     *
     *  * invocation of [                s.checkMemberAccess(this, Member.PUBLIC)][SecurityManager.checkMemberAccess] method denies access to the classes
     * within this class
     *
     *  * the caller's class loader is not the same as or an ancestor of the class
     * loader for the current class and invocation of
     * [s.checkPackageAccess()][SecurityManager.checkPackageAccess] denies access to
     * the package of this class
     *
     *
     *
     * @since JDK1.1
     */
    fun getClasses(): Array<Class?>? {
        _throw()
        return null
    }

    /**
     * Returns an array containing `Field` objects reflecting all the accessible public fields of
     * the class or interface represented by this `Class` object. The elements in the array
     * returned are not sorted and are not in any particular order. This method returns an array of
     * length 0 if the class or interface has no accessible public fields, or if it represents an array
     * class, a primitive type, or void.
     *
     *
     *
     * Specifically, if this `Class` object represents a class, this method returns the public
     * fields of this class and of all its superclasses. If this `Class` object represents an
     * interface, this method returns the fields of this interface and of all its superinterfaces.
     *
     *
     *
     * The implicit length field for array class is not reflected by this method. User code should use
     * the methods of class `Array` to manipulate arrays.
     *
     *
     *
     * See *The Java Language Specification*, sections 8.2 and 8.3.
     *
     * @return the array of `Field` objects representing the public fields
     * @exception SecurityException If a security manager, *s*, is present and any of the following
     * conditions is met:
     *
     *
     *
     *  * invocation of [                s.checkMemberAccess(this, Member.PUBLIC)][SecurityManager.checkMemberAccess] denies access to the fields within this
     * class
     *
     *  * the caller's class loader is not the same as or an ancestor of the class
     * loader for the current class and invocation of
     * [s.checkPackageAccess()][SecurityManager.checkPackageAccess] denies access to
     * the package of this class
     *
     *
     *
     * @since JDK1.1
     */
    @Throws(SecurityException::class)
    fun getFields(): Array<Field?>? {
        _throw()
        return null
    }

    /**
     * Returns an array containing `Method` objects reflecting all the public *member*
     * methods of the class or interface represented by this `Class` object, including those
     * declared by the class or interface and those inherited from superclasses and superinterfaces.
     * Array classes return all the (public) member methods inherited from the `Object` class. The
     * elements in the array returned are not sorted and are not in any particular order. This method
     * returns an array of length 0 if this `Class` object represents a class or interface that
     * has no public member methods, or if this `Class` object represents a primitive type or
     * void.
     *
     *
     *
     * The class initialization method `<clinit>` is not included in the returned array. If the
     * class declares multiple public member methods with the same parameter types, they are all
     * included in the returned array.
     *
     *
     *
     * See *The Java Language Specification*, sections 8.2 and 8.4.
     *
     * @return the array of `Method` objects representing the public methods of this class
     * @exception SecurityException If a security manager, *s*, is present and any of the following
     * conditions is met:
     *
     *
     *
     *  * invocation of [                s.checkMemberAccess(this, Member.PUBLIC)][SecurityManager.checkMemberAccess] denies access to the methods within this
     * class
     *
     *  * the caller's class loader is not the same as or an ancestor of the class
     * loader for the current class and invocation of
     * [s.checkPackageAccess()][SecurityManager.checkPackageAccess] denies access to
     * the package of this class
     *
     *
     *
     * @since JDK1.1
     */
    @Throws(SecurityException::class)
    fun getMethods(): Array<ASMMethod?>? {
        _throw()
        return methods!!.values().toArray(arrayOfNulls<ASMMethod?>(methods.size()))
    }

    /**
     * Returns an array containing `Constructor` objects reflecting all the public constructors of
     * the class represented by this `Class` object. An array of length 0 is returned if the class
     * has no public constructors, or if the class is an array class, or if the class reflects a
     * primitive type or void.
     *
     * Note that while this method returns an array of `Constructor<T>` objects (that is an array of constructors from this class), the return type of
     * this method is `Constructor<?>[]` and *not* `Constructor<T>[]` as might be expected. This less
     * informative return type is necessary since after being returned from this method, the array could
     * be modified to hold `Constructor` objects for different classes, which would violate the
     * type guarantees of `Constructor<T>[]`.
     *
     * @return the array of `Constructor` objects representing the public constructors of this
     * class
     * @exception SecurityException If a security manager, *s*, is present and any of the following
     * conditions is met:
     *
     *
     *
     *  * invocation of [                s.checkMemberAccess(this, Member.PUBLIC)][SecurityManager.checkMemberAccess] denies access to the constructors within
     * this class
     *
     *  * the caller's class loader is not the same as or an ancestor of the class
     * loader for the current class and invocation of
     * [s.checkPackageAccess()][SecurityManager.checkPackageAccess] denies access to
     * the package of this class
     *
     *
     *
     * @since JDK1.1
     */
    @Throws(SecurityException::class)
    fun getConstructors(): Array<Constructor?>? {
        _throw()
        return null
    }

    /**
     * Returns a `Field` object that reflects the specified public member field of the class or
     * interface represented by this `Class` object. The `name` parameter is a
     * `String` specifying the simple name of the desired field.
     *
     *
     *
     * The field to be reflected is determined by the algorithm that follows. Let C be the class
     * represented by this object:
     * <OL>
     * <LI>If C declares a public field with the name specified, that is the field to be reflected.</LI>
     * <LI>If no field was found in step 1 above, this algorithm is applied recursively to each direct
     * superinterface of C. The direct superinterfaces are searched in the order they were
     * declared.</LI>
     * <LI>If no field was found in steps 1 and 2 above, and C has a superclass S, then this algorithm
     * is invoked recursively upon S. If C has no superclass, then a `NoSuchFieldException` is
     * thrown.</LI>
    </OL> *
     *
     *
     *
     * See *The Java Language Specification*, sections 8.2 and 8.3.
     *
     * @param name the field name
     * @return the `Field` object of this class specified by `name`
     * @exception NoSuchFieldException if a field with the specified name is not found.
     * @exception NullPointerException if `name` is `null`
     * @exception SecurityException If a security manager, *s*, is present and any of the following
     * conditions is met:
     *
     *
     *
     *  * invocation of [                s.checkMemberAccess(this, Member.PUBLIC)][SecurityManager.checkMemberAccess] denies access to the field
     *
     *  * the caller's class loader is not the same as or an ancestor of the class
     * loader for the current class and invocation of
     * [s.checkPackageAccess()][SecurityManager.checkPackageAccess] denies access to
     * the package of this class
     *
     *
     *
     * @since JDK1.1
     */
    @Throws(NoSuchFieldException::class, SecurityException::class)
    fun getField(name: String?): Field? {
        _throw()
        return null
    }

    /**
     * Returns a `Method` object that reflects the specified public member method of the class or
     * interface represented by this `Class` object. The `name` parameter is a
     * `String` specifying the simple name of the desired method. The `parameterTypes`
     * parameter is an array of `Class` objects that identify the method's formal parameter types,
     * in declared order. If `parameterTypes` is `null`, it is treated as if it were an
     * empty array.
     *
     *
     *
     * If the `name` is "`<init>`;"or "`<clinit>`" a `NoSuchMethodException` is
     * raised. Otherwise, the method to be reflected is determined by the algorithm that follows. Let C
     * be the class represented by this object:
     * <OL>
     * <LI>C is searched for any <I>matching methods</I>. If no matching method is found, the algorithm
     * of step 1 is invoked recursively on the superclass of C.</LI>
     * <LI>If no method was found in step 1 above, the superinterfaces of C are searched for a matching
     * method. If any such method is found, it is reflected.</LI>
    </OL> *
     *
     * To find a matching method in a class C:&nbsp; If C declares exactly one public method with the
     * specified name and exactly the same formal parameter types, that is the method reflected. If more
     * than one such method is found in C, and one of these methods has a return type that is more
     * specific than any of the others, that method is reflected; otherwise one of the methods is chosen
     * arbitrarily.
     *
     *
     *
     * Note that there may be more than one matching method in a class because while the Java language
     * forbids a class to declare multiple methods with the same signature but different return types,
     * the Java virtual machine does not. This increased flexibility in the virtual machine can be used
     * to implement various language features. For example, covariant returns can be implemented with
     * [bridge methods][java.lang.reflect.Method.isBridge]; the bridge method and the method
     * being overridden would have the same signature but different return types.
     *
     *
     *
     * See *The Java Language Specification*, sections 8.2 and 8.4.
     *
     * @param name the name of the method
     * @param parameterTypes the list of parameters
     * @return the `Method` object that matches the specified `name` and
     * `parameterTypes`
     * @exception NoSuchMethodException if a matching method is not found or if the name is
     * "&lt;init&gt;"or "&lt;clinit&gt;".
     * @exception NullPointerException if `name` is `null`
     * @exception SecurityException If a security manager, *s*, is present and any of the following
     * conditions is met:
     *
     *
     *
     *  * invocation of [                s.checkMemberAccess(this, Member.PUBLIC)][SecurityManager.checkMemberAccess] denies access to the method
     *
     *  * the caller's class loader is not the same as or an ancestor of the class
     * loader for the current class and invocation of
     * [s.checkPackageAccess()][SecurityManager.checkPackageAccess] denies access to
     * the package of this class
     *
     *
     *
     * @since JDK1.1
     */
    @Throws(NoSuchMethodException::class, SecurityException::class)
    fun getMethod(name: String?, vararg parameterTypes: Class?): Method? {
        _throw()
        return null
    }

    /**
     * Returns a `Constructor` object that reflects the specified public constructor of the class
     * represented by this `Class` object. The `parameterTypes` parameter is an array of
     * `Class` objects that identify the constructor's formal parameter types, in declared order.
     *
     * If this `Class` object represents an inner class declared in a non-static context, the
     * formal parameter types include the explicit enclosing instance as the first parameter.
     *
     *
     *
     * The constructor to reflect is the public constructor of the class represented by this
     * `Class` object whose formal parameter types match those specified by
     * `parameterTypes`.
     *
     * @param parameterTypes the parameter array
     * @return the `Constructor` object of the public constructor that matches the specified
     * `parameterTypes`
     * @exception NoSuchMethodException if a matching method is not found.
     * @exception SecurityException If a security manager, *s*, is present and any of the following
     * conditions is met:
     *
     *
     *
     *  * invocation of [                s.checkMemberAccess(this, Member.PUBLIC)][SecurityManager.checkMemberAccess] denies access to the constructor
     *
     *  * the caller's class loader is not the same as or an ancestor of the class
     * loader for the current class and invocation of
     * [s.checkPackageAccess()][SecurityManager.checkPackageAccess] denies access to
     * the package of this class
     *
     *
     *
     * @since JDK1.1
     */
    @Throws(NoSuchMethodException::class, SecurityException::class)
    fun getConstructor(vararg parameterTypes: Class?): Constructor? {
        _throw()
        return null
    }

    /**
     * Returns an array of `Class` objects reflecting all the classes and interfaces declared as
     * members of the class represented by this `Class` object. This includes public, protected,
     * default (package) access, and private classes and interfaces declared by the class, but excludes
     * inherited classes and interfaces. This method returns an array of length 0 if the class declares
     * no classes or interfaces as members, or if this `Class` object represents a primitive type,
     * an array class, or void.
     *
     * @return the array of `Class` objects representing all the declared members of this class
     * @exception SecurityException If a security manager, *s*, is present and any of the following
     * conditions is met:
     *
     *
     *
     *  * invocation of [                s.checkMemberAccess(this, Member.DECLARED)][SecurityManager.checkMemberAccess] denies access to the declared classes
     * within this class
     *
     *  * the caller's class loader is not the same as or an ancestor of the class
     * loader for the current class and invocation of
     * [s.checkPackageAccess()][SecurityManager.checkPackageAccess] denies access to
     * the package of this class
     *
     *
     *
     * @since JDK1.1
     */
    @Throws(SecurityException::class)
    fun getDeclaredClasses(): Array<Class?>? {
        _throw()
        return null
    }

    /**
     * Returns an array of `Field` objects reflecting all the fields declared by the class or
     * interface represented by this `Class` object. This includes public, protected, default
     * (package) access, and private fields, but excludes inherited fields. The elements in the array
     * returned are not sorted and are not in any particular order. This method returns an array of
     * length 0 if the class or interface declares no fields, or if this `Class` object represents
     * a primitive type, an array class, or void.
     *
     *
     *
     * See *The Java Language Specification*, sections 8.2 and 8.3.
     *
     * @return the array of `Field` objects representing all the declared fields of this class
     * @exception SecurityException If a security manager, *s*, is present and any of the following
     * conditions is met:
     *
     *
     *
     *  * invocation of [                s.checkMemberAccess(this, Member.DECLARED)][SecurityManager.checkMemberAccess] denies access to the declared fields
     * within this class
     *
     *  * the caller's class loader is not the same as or an ancestor of the class
     * loader for the current class and invocation of
     * [s.checkPackageAccess()][SecurityManager.checkPackageAccess] denies access to
     * the package of this class
     *
     *
     *
     * @since JDK1.1
     */
    @Throws(SecurityException::class)
    fun getDeclaredFields(): Array<Field?>? {
        _throw()
        return null
    }

    /**
     * Returns an array of `Method` objects reflecting all the methods declared by the class or
     * interface represented by this `Class` object. This includes public, protected, default
     * (package) access, and private methods, but excludes inherited methods. The elements in the array
     * returned are not sorted and are not in any particular order. This method returns an array of
     * length 0 if the class or interface declares no methods, or if this `Class` object
     * represents a primitive type, an array class, or void. The class initialization method
     * `<clinit>` is not included in the returned array. If the class declares multiple public
     * member methods with the same parameter types, they are all included in the returned array.
     *
     *
     *
     * See *The Java Language Specification*, section 8.2.
     *
     * @return the array of `Method` objects representing all the declared methods of this class
     * @exception SecurityException If a security manager, *s*, is present and any of the following
     * conditions is met:
     *
     *
     *
     *  * invocation of [                s.checkMemberAccess(this, Member.DECLARED)][SecurityManager.checkMemberAccess] denies access to the declared methods
     * within this class
     *
     *  * the caller's class loader is not the same as or an ancestor of the class
     * loader for the current class and invocation of
     * [s.checkPackageAccess()][SecurityManager.checkPackageAccess] denies access to
     * the package of this class
     *
     *
     *
     * @since JDK1.1
     */
    @Throws(SecurityException::class)
    fun getDeclaredMethods(): Array<Method?>? {
        _throw()
        return null
    }

    /**
     * Returns an array of `Constructor` objects reflecting all the constructors declared by the
     * class represented by this `Class` object. These are public, protected, default (package)
     * access, and private constructors. The elements in the array returned are not sorted and are not
     * in any particular order. If the class has a default constructor, it is included in the returned
     * array. This method returns an array of length 0 if this `Class` object represents an
     * interface, a primitive type, an array class, or void.
     *
     *
     *
     * See *The Java Language Specification*, section 8.2.
     *
     * @return the array of `Constructor` objects representing all the declared constructors of
     * this class
     * @exception SecurityException If a security manager, *s*, is present and any of the following
     * conditions is met:
     *
     *
     *
     *  * invocation of [                s.checkMemberAccess(this, Member.DECLARED)][SecurityManager.checkMemberAccess] denies access to the declared
     * constructors within this class
     *
     *  * the caller's class loader is not the same as or an ancestor of the class
     * loader for the current class and invocation of
     * [s.checkPackageAccess()][SecurityManager.checkPackageAccess] denies access to
     * the package of this class
     *
     *
     *
     * @since JDK1.1
     */
    @Throws(SecurityException::class)
    fun getDeclaredConstructors(): Array<Constructor?>? {
        _throw()
        return null
    }

    /**
     * Returns a `Field` object that reflects the specified declared field of the class or
     * interface represented by this `Class` object. The `name` parameter is a
     * `String` that specifies the simple name of the desired field. Note that this method will
     * not reflect the `length` field of an array class.
     *
     * @param name the name of the field
     * @return the `Field` object for the specified field in this class
     * @exception NoSuchFieldException if a field with the specified name is not found.
     * @exception NullPointerException if `name` is `null`
     * @exception SecurityException If a security manager, *s*, is present and any of the following
     * conditions is met:
     *
     *
     *
     *  * invocation of [                s.checkMemberAccess(this, Member.DECLARED)][SecurityManager.checkMemberAccess] denies access to the declared field
     *
     *  * the caller's class loader is not the same as or an ancestor of the class
     * loader for the current class and invocation of
     * [s.checkPackageAccess()][SecurityManager.checkPackageAccess] denies access to
     * the package of this class
     *
     *
     *
     * @since JDK1.1
     */
    @Throws(NoSuchFieldException::class, SecurityException::class)
    fun getDeclaredField(name: String?): Field? {
        _throw()
        return null
    }

    /**
     * Returns a `Method` object that reflects the specified declared method of the class or
     * interface represented by this `Class` object. The `name` parameter is a
     * `String` that specifies the simple name of the desired method, and the
     * `parameterTypes` parameter is an array of `Class` objects that identify the method's
     * formal parameter types, in declared order. If more than one method with the same parameter types
     * is declared in a class, and one of these methods has a return type that is more specific than any
     * of the others, that method is returned; otherwise one of the methods is chosen arbitrarily. If
     * the name is "&lt;init&gt;"or "&lt;clinit&gt;" a `NoSuchMethodException` is raised.
     *
     * @param name the name of the method
     * @param parameterTypes the parameter array
     * @return the `Method` object for the method of this class matching the specified name and
     * parameters
     * @exception NoSuchMethodException if a matching method is not found.
     * @exception NullPointerException if `name` is `null`
     * @exception SecurityException If a security manager, *s*, is present and any of the following
     * conditions is met:
     *
     *
     *
     *  * invocation of [                s.checkMemberAccess(this, Member.DECLARED)][SecurityManager.checkMemberAccess] denies access to the declared method
     *
     *  * the caller's class loader is not the same as or an ancestor of the class
     * loader for the current class and invocation of
     * [s.checkPackageAccess()][SecurityManager.checkPackageAccess] denies access to
     * the package of this class
     *
     *
     *
     * @since JDK1.1
     */
    @Throws(NoSuchMethodException::class, SecurityException::class)
    fun getDeclaredMethod(name: String?, vararg parameterTypes: Class?): Method? {
        _throw()
        return null
    }

    /**
     * Returns a `Constructor` object that reflects the specified constructor of the class or
     * interface represented by this `Class` object. The `parameterTypes` parameter is an
     * array of `Class` objects that identify the constructor's formal parameter types, in
     * declared order.
     *
     * If this `Class` object represents an inner class declared in a non-static context, the
     * formal parameter types include the explicit enclosing instance as the first parameter.
     *
     * @param parameterTypes the parameter array
     * @return The `Constructor` object for the constructor with the specified parameter list
     * @exception NoSuchMethodException if a matching method is not found.
     * @exception SecurityException If a security manager, *s*, is present and any of the following
     * conditions is met:
     *
     *
     *
     *  * invocation of [                s.checkMemberAccess(this, Member.DECLARED)][SecurityManager.checkMemberAccess] denies access to the declared
     * constructor
     *
     *  * the caller's class loader is not the same as or an ancestor of the class
     * loader for the current class and invocation of
     * [s.checkPackageAccess()][SecurityManager.checkPackageAccess] denies access to
     * the package of this class
     *
     *
     *
     * @since JDK1.1
     */
    @Throws(NoSuchMethodException::class, SecurityException::class)
    fun getDeclaredConstructor(vararg parameterTypes: Class?): Constructor? {
        _throw()
        return null
    }

    /**
     * Finds a resource with a given name. The rules for searching resources associated with a given
     * class are implemented by the defining [class loader][ClassLoader] of the class. This
     * method delegates to this object's class loader. If this object was loaded by the bootstrap class
     * loader, the method delegates to [ClassLoader.getSystemResourceAsStream].
     *
     *
     *
     * Before delegation, an absolute resource name is constructed from the given resource name using
     * this algorithm:
     *
     *
     *
     *  * If the `name` begins with a `'/'` (<tt>'&#92;u002f'</tt>), then the absolute name
     * of the resource is the portion of the `name` following the `'/'`.
     *
     *  * Otherwise, the absolute name is of the following form:
     *
     * <blockquote> `modified_package_name/name` </blockquote>
     *
     *
     *
     * Where the `modified_package_name` is the package name of this object with `'/'`
     * substituted for `'.'` (<tt>'&#92;u002e'</tt>).
     *
     *
     *
     * @param name name of the desired resource
     * @return A [java.io.InputStream] object or `null` if no resource with this name is
     * found
     * @throws NullPointerException If `name` is `null`
     * @since JDK1.1
     */
    fun getResourceAsStream(name: String?): InputStream? {
        _throw()
        return null
    }

    /**
     * Finds a resource with a given name. The rules for searching resources associated with a given
     * class are implemented by the defining [class loader][ClassLoader] of the class. This
     * method delegates to this object's class loader. If this object was loaded by the bootstrap class
     * loader, the method delegates to [ClassLoader.getSystemResource].
     *
     *
     *
     * Before delegation, an absolute resource name is constructed from the given resource name using
     * this algorithm:
     *
     *
     *
     *  * If the `name` begins with a `'/'` (<tt>'&#92;u002f'</tt>), then the absolute name
     * of the resource is the portion of the `name` following the `'/'`.
     *
     *  * Otherwise, the absolute name is of the following form:
     *
     * <blockquote> `modified_package_name/name` </blockquote>
     *
     *
     *
     * Where the `modified_package_name` is the package name of this object with `'/'`
     * substituted for `'.'` (<tt>'&#92;u002e'</tt>).
     *
     *
     *
     * @param name name of the desired resource
     * @return A [java.net.URL] object or `null` if no resource with this name is found
     * @since JDK1.1
     */
    fun getResource(name: String?): java.net.URL? {
        _throw()
        return null
    }

    companion object {
        private fun _throw() {
            throw RuntimeException("not supported!")
        }
    }

    init {
        this.methods = methods
    }
}