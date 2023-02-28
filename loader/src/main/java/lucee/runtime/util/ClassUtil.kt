/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.util

import java.io.IOException

interface ClassUtil {
    /**
     * loads Class that match given classname, this Class can be from the Lucee core as well
     *
     * @param className name of the Class to load
     * @return Class
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun loadClass(className: String?): Class<*>?

    /**
     * loads Class that match given classname and the given bundle name and version, this Class can be
     * from the Lucee core as well
     *
     * @param pc Page Context
     * @param className name of the Class to load
     * @param bundleName name of the bundle to load from
     * @param bundleVersion version of the bundle to load from (if null ignored)
     * @return class
     * @throws BundleException Bundle Exception
     * @throws IOException IO Exception
     */
    @Throws(BundleException::class, IOException::class)
    fun loadClass(pc: PageContext?, className: String?, bundleName: String?, bundleVersion: String?): Class<*>?

    @Throws(InstantiationException::class, IllegalAccessException::class)
    fun loadBIF(pc: PageContext?, name: String?): BIF?

    /**
     * check if Class is instanceof another Class
     *
     * @param srcClassName class name to check
     * @param trg class to check
     * @return is instance of
     */
    fun isInstaneOf(srcClassName: String?, trg: Class<*>?): Boolean

    /**
     * @param srcClassName class name to check
     * @param trgClassName class name to check
     * @return is instance of
     */
    fun isInstaneOf(srcClassName: String?, trgClassName: String?): Boolean

    /**
     * check if Class is instanceof another Class
     *
     * @param src is Class of?
     * @param trgClassName Class name to check
     * @return is Class Class of...
     */
    fun isInstaneOf(src: Class<*>?, trgClassName: String?): Boolean
    fun isInstaneOfIgnoreCase(src: Class<*>?, trg: String?): Boolean

    /**
     * check if Class is instanceof another Class
     *
     * @param src Class to check
     * @param trg is Class of ?
     * @return is Class Class of...
     */
    fun isInstaneOf(src: Class<*>?, trg: Class<*>?): Boolean

    /**
     * get all Classes from an Object Array
     *
     * @param objs Objects to get
     * @return classes from Objects
     */
    fun getClasses(objs: Array<Object?>?): Array<Class<*>?>?

    /**
     * convert a primitive Class Type to a Reference Type (Example: int to java.lang.Integer)
     *
     * @param c Class to convert
     * @return converted Class (if primitive)
     */
    fun toReferenceClass(c: Class<*>?): Class<*>?

    /**
     * checks if src Class is "like" trg class
     *
     * @param src Source Class
     * @param trg Target Class
     * @return is similar
     */
    fun like(src: Class<*>?, trg: Class<*>?): Boolean

    /**
     * convert Object from src to trg Type, if possible
     *
     * @param src Object to convert
     * @param trgClass Target Class
     * @param rating rating
     * @return converted Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun convert(src: Object?, trgClass: Class<*>?, rating: RefInteger?): Object?

    /**
     * same like method getField from Class but ignore case from field name
     *
     * @param clazz Class to search the field
     * @param name name to search
     * @return Matching Field
     * @throws NoSuchFieldException No Such Field Exception
     */
    @Throws(NoSuchFieldException::class)
    fun getFieldsIgnoreCase(clazz: Class<*>?, name: String?): Array<Field?>?
    fun getFieldsIgnoreCase(clazz: Class<*>?, name: String?, defaultValue: Array<Field?>?): Array<Field?>?
    fun getPropertyKeys(clazz: Class<*>?): Array<String?>?
    fun hasPropertyIgnoreCase(clazz: Class<*>?, name: String?): Boolean
    fun hasFieldIgnoreCase(clazz: Class<*>?, name: String?): Boolean

    /**
     * call constructor of a Class with matching arguments
     *
     * @param clazz Class to get Instance
     * @param args Arguments for the Class
     * @return invoked Instance
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun callConstructor(clazz: Class<*>?, args: Array<Object?>?): Object?
    fun callConstructor(clazz: Class<*>?, args: Array<Object?>?, defaultValue: Object?): Object?

    /**
     * calls a Method of an Object
     *
     * @param obj Object to call Method on it
     * @param methodName Name of the Method to get
     * @param args Arguments of the Method to get
     * @return return return value of the called Method
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun callMethod(obj: Object?, methodName: Collection.Key?, args: Array<Object?>?): Object?
    fun callMethod(obj: Object?, methodName: Collection.Key?, args: Array<Object?>?, defaultValue: Object?): Object?

    /**
     * calls a Static Method on the given CLass
     *
     * @param clazz Class to call Method on it
     * @param methodName Name of the Method to get
     * @param args Arguments of the Method to get
     * @return return return value of the called Method
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun callStaticMethod(clazz: Class<*>?, methodName: String?, args: Array<Object?>?): Object?

    /**
     * to get a visible Field of an object
     *
     * @param obj Object to invoke
     * @param prop property to call
     * @return property value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun getField(obj: Object?, prop: String?): Object?
    fun getField(obj: Object?, prop: String?, defaultValue: Object?): Object?

    /**
     * assign a value to a visible Field of an object
     *
     * @param obj Object to assign value to his property
     * @param prop name of property
     * @param value Value to assign
     * @return success
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun setField(obj: Object?, prop: String?, value: Object?): Boolean

    /**
     * to get a visible Property (Field or Getter) of an object
     *
     * @param obj Object to invoke
     * @param prop property to call
     * @return property value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun getProperty(obj: Object?, prop: String?): Object?

    /**
     * to get a visible Property (Field or Getter) of an object
     *
     * @param obj Object to invoke
     * @param prop property to call
     * @param defaultValue default value
     * @return property value
     */
    fun getProperty(obj: Object?, prop: String?, defaultValue: Object?): Object?

    /**
     * assign a value to a visible Property (Field or Setter) of an object
     *
     * @param obj Object to assign value to his property
     * @param prop name of property
     * @param value Value to assign
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun setProperty(obj: Object?, prop: String?, value: Object?)

    /**
     * assign a value to a visible Property (Field or Setter) of an object
     *
     * @param obj Object to assign value to his property
     * @param prop name of property
     * @param value Value to assign
     */
    fun setPropertyEL(obj: Object?, prop: String?, value: Object?)

    /**
     * return all methods that are defined by the Class itself (not extended)
     *
     * @param clazz class
     * @return Returns declared methods.
     */
    fun getDeclaredMethods(clazz: Class<*>?): Array<Method?>?

    /**
     * check if given Class "from" can be converted to Class "to" without explicit casting
     *
     * @param from source class
     * @param to target class
     * @return is it possible to convert from "from" to "to"
     */
    fun canConvert(from: Class<*>?, to: Class<*>?): Boolean

    @Throws(IOException::class, BundleException::class)
    fun loadClassByBundle(className: String?, name: String?, strVersion: String?, id: Identification?): Class<*>?

    @Throws(BundleException::class, IOException::class)
    fun loadClassByBundle(className: String?, name: String?, version: Version?, id: Identification?): Class<*>?

    /**
     * loads a Class from a String classname
     *
     * @param className class name
     * @param defaultValue default value
     * @return matching Class
     */
    fun loadClass(className: String?, defaultValue: Class<*>?): Class<*>?

    /**
     * loads a Class from a specified Classloader with given classname
     *
     * @param cl class loader
     * @param className class name
     * @param defaultValue default value
     * @return matching Class
     */
    fun loadClass(cl: ClassLoader?, className: String?, defaultValue: Class<*>?): Class<*>?

    /**
     * loads a Class from a specified Classloader with given classname
     *
     * @param className class name
     * @param cl class loader
     * @return matching Class
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun loadClass(cl: ClassLoader?, className: String?): Class<*>?

    /**
     * loads a Class from a String classname
     *
     * @param clazz Class to load
     * @return matching Class
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun loadInstance(clazz: Class<*>?): Object?

    @Throws(IOException::class)
    fun loadInstance(className: String?): Object?

    @Throws(IOException::class)
    fun loadInstance(cl: ClassLoader?, className: String?): Object?

    /**
     * loads a Class from a String classname
     *
     * @param clazz Class to load
     * @param defaultValue default value
     * @return matching Class
     */
    fun loadInstance(clazz: Class<*>?, defaultValue: Object?): Object?
    fun loadInstance(className: String?, defaultValue: Object?): Object?
    fun loadInstance(cl: ClassLoader?, className: String?, defaultValue: Object?): Object?

    /**
     * loads a Class from a String classname
     *
     * @param clazz Class to load
     * @param args arguments
     * @return matching Class
     * @throws IOException IO Exception
     * @throws InvocationTargetException Invocation Target Exception
     */
    @Throws(IOException::class, InvocationTargetException::class)
    fun loadInstance(clazz: Class<*>?, args: Array<Object?>?): Object?

    @Throws(IOException::class, InvocationTargetException::class)
    fun loadInstance(className: String?, args: Array<Object?>?): Object?

    @Throws(IOException::class, InvocationTargetException::class)
    fun loadInstance(cl: ClassLoader?, className: String?, args: Array<Object?>?): Object?

    /**
     * loads a Class from a String classname
     *
     * @param clazz Class to load
     * @param args arguments
     * @param defaultValue default value
     * @return matching Class
     */
    fun loadInstance(clazz: Class<*>?, args: Array<Object?>?, defaultValue: Object?): Object?
    fun loadInstance(className: String?, args: Array<Object?>?, defaultValue: Object?): Object?
    fun loadInstance(cl: ClassLoader?, className: String?, args: Array<Object?>?, defaultValue: Object?): Object?

    /**
     * check if given stream is a bytecode stream, if yes remove bytecode mark
     *
     * @param is Input Stream
     * @return is bytecode stream
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    fun isBytecode(`is`: InputStream?): Boolean
    fun isBytecode(barr: ByteArray?): Boolean
    fun getName(clazz: Class<*>?): String?

    @Throws(IOException::class)
    fun getMethodIgnoreCase(clazz: Class<*>?, methodName: String?, args: Array<Class<*>?>?): Method?
    fun getMethodIgnoreCase(clazz: Class<*>?, methodName: String?, args: Array<Class<*>?>?, defaultValue: Method?): Method?

    /**
     * return all field names as String array
     *
     * @param clazz Class to get field names from
     * @return field names
     */
    fun getFieldNames(clazz: Class<*>?): Array<String?>?

    @Throws(IOException::class)
    fun toBytes(clazz: Class<*>?): ByteArray?

    /**
     * return an array Class based on the given Class (opposite from Class.getComponentType())
     *
     * @param clazz class
     * @return Returns an Array Class.
     */
    fun toArrayClass(clazz: Class<*>?): Class<*>?
    fun toComponentType(clazz: Class<*>?): Class<*>?

    /**
     * returns the path to the directory or jar file that the Class was loaded from
     *
     * @param clazz - the Class object to check, for a live object pass obj.getClass();
     * @param defaultValue - a value to return in case the source could not be determined
     * @return Returns the source path for the Class.
     */
    fun getSourcePathForClass(clazz: Class<*>?, defaultValue: String?): String?

    /**
     * tries to load the Class and returns the path that it was loaded from
     *
     * @param className - the name of the Class to check
     * @param defaultValue - a value to return in case the source could not be determined
     * @return Returns the source path for the Class.
     */
    fun getSourcePathForClass(className: String?, defaultValue: String?): String?

    /**
     * extracts the package from a className, return null, if there is none.
     *
     * @param className Class Name
     * @return Returns the source path for the Class.
     */
    fun extractPackage(className: String?): String?

    /**
     * extracts the Class name of a classname with package
     *
     * @param className Class Name
     * @return Returns Class name.
     */
    fun extractName(className: String?): String?

    @Throws(BundleException::class)
    fun start(bundle: Bundle?)

    @Throws(BundleException::class, IOException::class)
    fun addBundle(context: BundleContext?, `is`: InputStream?, closeStream: Boolean, checkExistence: Boolean): Bundle?
}