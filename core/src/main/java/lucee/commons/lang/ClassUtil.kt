/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.commons.lang

import java.io.File

object ClassUtil {
    /**
     * @param className
     * @return
     * @throws ClassException
     * @throws PageException
     */
    @Throws(ClassException::class)
    fun toClass(className: String): Class {
        return loadClass(className)
    }

    private fun checkPrimaryTypesBytecodeDef(className: String, defaultValue: Class?): Class? {
        if (className.charAt(0) === '[') {
            if (className.equals("[V")) return Void.TYPE
            if (className.equals("[Z")) return Boolean::class.javaPrimitiveType
            if (className.equals("[B")) return Byte::class.javaPrimitiveType
            if (className.equals("[I")) return Int::class.javaPrimitiveType
            if (className.equals("[J")) return Long::class.javaPrimitiveType
            if (className.equals("[F")) return Float::class.javaPrimitiveType
            if (className.equals("[D")) return Double::class.javaPrimitiveType
            if (className.equals("[C")) return Char::class.javaPrimitiveType
            if (className.equals("[S")) return Short::class.javaPrimitiveType
        }
        return defaultValue
    }

    private fun checkPrimaryTypes(className: String, defaultValue: Class?): Class? {
        val res: Class? = checkPrimaryTypesBytecodeDef(className, null)
        if (res != null) return res
        var lcClassName: String = className.toLowerCase()
        var isRef = false
        if (lcClassName.startsWith("java.lang.")) {
            lcClassName = lcClassName.substring(10)
            isRef = true
        }
        if (lcClassName.equals("void")) {
            return Void.TYPE
        }
        if (lcClassName.equals("boolean")) {
            return if (isRef) Boolean::class.java else Boolean::class.javaPrimitiveType
        }
        if (lcClassName.equals("byte")) {
            return if (isRef) Byte::class.java else Byte::class.javaPrimitiveType
        }
        if (lcClassName.equals("int")) {
            return Int::class.javaPrimitiveType
        }
        if (lcClassName.equals("long")) {
            return if (isRef) Long::class.java else Long::class.javaPrimitiveType
        }
        if (lcClassName.equals("float")) {
            return if (isRef) Float::class.java else Float::class.javaPrimitiveType
        }
        if (lcClassName.equals("double")) {
            return if (isRef) Double::class.java else Double::class.javaPrimitiveType
        }
        if (lcClassName.equals("char")) {
            return Char::class.javaPrimitiveType
        }
        if (lcClassName.equals("short")) {
            return if (isRef) Short::class.java else Short::class.javaPrimitiveType
        }
        if (lcClassName.equals("integer")) return Integer::class.java
        if (lcClassName.equals("character")) return Character::class.java
        if (lcClassName.equals("object")) return Object::class.java
        if (lcClassName.equals("string")) return String::class.java
        if (lcClassName.equals("null")) return Object::class.java
        return if (lcClassName.equals("numeric")) Double::class.java else defaultValue
    }

    @Throws(ClassException::class, BundleException::class)
    fun loadClassByBundle(className: String?, name: String?, strVersion: String?, id: Identification?, addional: List<Resource?>?): Class<*> {
        return loadClassByBundle(className, name, strVersion, id, addional, false)
    }

    @Throws(ClassException::class, BundleException::class)
    fun loadClassByBundle(className: String?, name: String?, strVersion: String, id: Identification?, addional: List<Resource?>?, versionOnlyMattersForDownload: Boolean): Class<*> {
        // version
        var version: Version? = null
        if (!StringUtil.isEmpty(strVersion, true)) {
            version = OSGiUtil.toVersion(strVersion.trim(), null)
            if (version == null) throw ClassException("Version definition [$strVersion] is invalid.")
        }
        return loadClassByBundle(className, BundleDefinition(name, version), null, id, addional, versionOnlyMattersForDownload)
    }

    @Throws(BundleException::class, ClassException::class)
    fun loadClassByBundle(className: String?, name: String?, version: Version?, id: Identification?, addional: List<Resource?>?): Class {
        return loadClassByBundle(className, BundleDefinition(name, version), null, id, addional)
    }

    @Throws(BundleException::class, ClassException::class)
    fun loadClassByBundle(className: String?, name: String?, version: Version?, id: Identification?, addional: List<Resource?>?, versionOnlyMattersForDownload: Boolean): Class {
        return loadClassByBundle(className, BundleDefinition(name, version), null, id, addional, versionOnlyMattersForDownload)
    }

    @Throws(BundleException::class, ClassException::class)
    fun loadClassByBundle(className: String?, bundle: BundleDefinition?, relatedBundles: Array<BundleDefinition?>?, id: Identification?, addional: List<Resource?>?): Class<*> {
        return loadClassByBundle(className, bundle, relatedBundles, id, addional, false)
    }

    @Throws(BundleException::class, ClassException::class)
    fun loadClassByBundle(className: String, bundle: BundleDefinition, relatedBundles: Array<BundleDefinition>?, id: Identification?, addional: List<Resource?>?,
                          versionOnlyMattersForDownload: Boolean): Class<*> {
        return try {
            if (relatedBundles != null) {
                for (rb in relatedBundles) {
                    rb.getBundle(id, addional, true)
                }
            }
            bundle.getBundle(id, addional, true, versionOnlyMattersForDownload).loadClass(className)
        } catch (e: ClassNotFoundException) {
            var appendix = ""
            if (!StringUtil.isEmpty(e.getMessage(), true)) appendix = " " + e.getMessage()
            if (bundle.getVersion() == null) throw ClassException("In the OSGi Bundle with the name [" + bundle.getName().toString() + "] was no class with name [" + className + "] found." + appendix)
            throw ClassException("In the OSGi Bundle with the name [" + bundle.getName().toString() + "] and the version [" + bundle.getVersion().toString() + "] was no class with name ["
                    + className + "] found." + appendix)
        }
    }

    /**
     * loads a class from a String classname
     *
     * @param className
     * @param defaultValue
     * @return matching Class
     */
    fun loadClass(className: String, defaultValue: Class): Class {
        // OSGI env
        var clazz: Class? = _loadClass(OSGiBasedClassLoading(), className, null, null)
        if (clazz != null) return clazz

        // core classloader
        clazz = _loadClass(ClassLoaderBasedClassLoading(SystemUtil.getCoreClassLoader()), className, null, null)
        if (clazz != null) return clazz

        // loader classloader
        clazz = _loadClass(ClassLoaderBasedClassLoading(SystemUtil.getLoaderClassLoader()), className, null, null)
        return if (clazz != null) clazz else defaultValue
    }

    /**
     * loads a class from a String classname
     *
     * @param className
     * @return matching Class
     * @throws ClassException
     */
    @Throws(ClassException::class)
    fun loadClass(className: String): Class {
        val exceptions: Set<Throwable> = HashSet<Throwable>()
        // OSGI env
        var clazz: Class? = _loadClass(OSGiBasedClassLoading(), className, null, exceptions)
        if (clazz != null) {
            return clazz
        }

        // core classloader
        clazz = _loadClass(ClassLoaderBasedClassLoading(SystemUtil.getCoreClassLoader()), className, null, exceptions)
        if (clazz != null) {
            return clazz
        }

        // loader classloader
        clazz = _loadClass(ClassLoaderBasedClassLoading(SystemUtil.getLoaderClassLoader()), className, null, exceptions)
        if (clazz != null) {
            return clazz
        }
        val msg = "cannot load class through its string name, because no definition for the class with the specified name [$className] could be found"
        if (exceptions.size() > 0) {
            val detail = StringBuilder()
            val it = exceptions.iterator()
            var t: Throwable
            while (it.hasNext()) {
                t = it.next()
                detail.append(t.getClass().getName()).append(':').append(t.getMessage()).append(';')
            }
            throw ClassException(msg + " caused by (" + detail.toString() + ")")
        }
        throw ClassException(msg)
    }

    fun loadClass(cl: ClassLoader, className: String, defaultValue: Class?): Class? {
        return loadClass(cl, className, defaultValue, null)
    }

    private fun loadClass(cl: ClassLoader, className: String, defaultValue: Class?, exceptions: Set<Throwable>?): Class? {
        var cl: ClassLoader? = cl
        if (cl != null) {
            // TODO do not produce a resource classloader in the first place if there are no resources
            if (cl is ResourceClassLoader && (cl as ResourceClassLoader).isEmpty()) {
                val p: ClassLoader = (cl as ResourceClassLoader).getParent()
                if (p != null) cl = p
            }
            val clazz: Class? = _loadClass(ClassLoaderBasedClassLoading(cl), className, defaultValue, exceptions)
            if (clazz != null) return clazz
        }

        // OSGI env
        var clazz: Class? = _loadClass(OSGiBasedClassLoading(), className, null, exceptions)
        if (clazz != null) return clazz

        // core classloader
        if (cl !== SystemUtil.getCoreClassLoader()) {
            clazz = _loadClass(ClassLoaderBasedClassLoading(SystemUtil.getCoreClassLoader()), className, null, exceptions)
            if (clazz != null) return clazz
        }

        // loader classloader
        if (cl !== SystemUtil.getLoaderClassLoader()) {
            clazz = _loadClass(ClassLoaderBasedClassLoading(SystemUtil.getLoaderClassLoader()), className, null, exceptions)
            if (clazz != null) return clazz
        }
        return defaultValue
    }

    /**
     * loads a class from a specified Classloader with given classname
     *
     * @param className
     * @param cl
     * @return matching Class
     * @throws ClassException
     */
    @Throws(ClassException::class)
    fun loadClass(cl: ClassLoader, className: String): Class {
        val exceptions: Set<Throwable> = HashSet<Throwable>()
        val clazz: Class? = loadClass(cl, className, null, exceptions)
        if (clazz != null) return clazz
        val msg = "cannot load class through its string name, because no definition for the class with the specified name [$className] could be found"
        if (!exceptions.isEmpty()) {
            val detail = StringBuilder()
            val it = exceptions.iterator()
            var t: Throwable
            while (it.hasNext()) {
                t = it.next()
                detail.append(t.getClass().getName()).append(':').append(t.getMessage()).append(';')
            }
            throw ClassException(msg + " caused by (" + detail.toString() + ")")
        }
        throw ClassException(msg)
    }

    /**
     * loads a class from a specified Classloader with given classname
     *
     * @param className
     * @param cl
     * @return matching Class
     */
    private fun _loadClass(cl: ClassLoading, className: String, defaultValue: Class?, exceptions: Set<Throwable>?): Class? {
        var className = className
        className = className.trim()
        if (StringUtil.isEmpty(className)) return defaultValue
        var clazz: Class? = checkPrimaryTypesBytecodeDef(className, null)
        if (clazz != null) return clazz

        // array in the format boolean[] or java.lang.String[]
        if (className.endsWith("[]")) {
            val pureCN = StringBuilder(className)
            var dimensions = 0
            do {
                pureCN.delete(pureCN.length() - 2, pureCN.length())
                dimensions++
            } while (pureCN.lastIndexOf("[]") === pureCN.length() - 2)
            clazz = __loadClass(cl, pureCN.toString(), null, exceptions)
            if (clazz != null) {
                for (i in 0 until dimensions) clazz = toArrayClass(clazz)
                return clazz
            }
        } else if (className.charAt(0) === '[') {
            val pureCN = StringBuilder(className)
            var dimensions = 0
            do {
                pureCN.delete(0, 1)
                dimensions++
            } while (pureCN.charAt(0) === '[')
            clazz = __loadClass(cl, pureCN.toString(), null, exceptions)
            if (clazz != null) {
                for (i in 0 until dimensions) clazz = toArrayClass(clazz)
                return clazz
            }
        }
        return __loadClass(cl, className, defaultValue, exceptions)
    }

    private fun __loadClass(cl: ClassLoading, className: String, defaultValue: Class<*>?, exceptions: Set<Throwable>?): Class<*>? {
        var className = className
        var clazz: Class<*>? = checkPrimaryTypes(className, null)
        if (clazz != null) return clazz

        // class in format Ljava.lang.String;
        if (className.charAt(0) === 'L' && className.endsWith(";")) {
            className = className.substring(1, className.length() - 1).replace('/', '.')
            clazz = cl.loadClass(className, null, exceptions)
            return if (clazz != null) clazz else defaultValue
        }
        clazz = cl.loadClass(className, null, exceptions)
        return if (clazz != null) clazz else defaultValue
    }

    /**
     * loads a class from a String classname
     *
     * @param clazz class to load
     * @return matching Class
     * @throws ClassException
     */
    @Throws(ClassException::class)
    fun loadInstance(clazz: Class): Object {
        return try {
            newInstance(clazz)
        } catch (e: InstantiationException) {
            throw ClassException("the specified class object [" + clazz.getName().toString() + "()] cannot be instantiated")
        } catch (e: IllegalAccessException) {
            throw ClassException("can't load class [" + clazz.getName().toString() + "] because the currently executing method does not have access to the definition of the specified class")
        } catch (e: Exception) {
            var message = ""
            if (e.getMessage() != null) {
                message = e.getMessage().toString() + " "
            }
            message += e.getClass().getName().toString() + " while creating an instance of " + clazz.getName()
            val ce = ClassException(message)
            ce.setStackTrace(e.getStackTrace())
            throw ce
        }
    }

    @Throws(ClassException::class)
    fun loadInstance(className: String): Object {
        return loadInstance(loadClass(className))
    }

    @Throws(ClassException::class)
    fun loadInstance(cl: ClassLoader?, className: String?): Object {
        return loadInstance(loadClass(cl, className))
    }

    /**
     * loads a class from a String classname
     *
     * @param clazz class to load
     * @return matching Class
     */
    fun loadInstance(clazz: Class, defaultValue: Object): Object {
        return try {
            newInstance(clazz)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }

    fun loadInstance(className: String?, defaultValue: Object): Object {
        val clazz: Class = loadClass(className, null) ?: return defaultValue
        return loadInstance(clazz, defaultValue)
    }

    fun loadInstance(cl: ClassLoader, className: String, defaultValue: Object): Object {
        val clazz: Class = loadClass(cl, className, null) ?: return defaultValue
        return loadInstance(clazz, defaultValue)
    }

    /**
     * loads a class from a String classname
     *
     * @param clazz class to load
     * @param args
     * @return matching Class
     * @throws ClassException
     * @throws ClassException
     * @throws InvocationTargetException
     */
    @Throws(ClassException::class, InvocationTargetException::class)
    fun loadInstance(clazz: Class, args: Array<Object>?): Object {
        if (args == null || args.size == 0) return loadInstance(clazz)
        val cArgs: Array<Class?> = arrayOfNulls<Class>(args.size)
        for (i in args.indices) {
            cArgs[i] = args[i].getClass()
        }
        return try {
            val c: Constructor = clazz.getConstructor(cArgs)
            c.newInstance(args)
        } catch (e: SecurityException) {
            throw ClassException("there is a security violation (thrown by security manager)")
        } catch (e: NoSuchMethodException) {
            val sb = StringBuilder(clazz.getName())
            var del = '('
            var i = 0
            while (i < cArgs.size) {
                sb.append(del)
                sb.append(cArgs[i].getName())
                del = ','
                i++
            }
            sb.append(')')
            throw ClassException("there is no constructor with the [" + sb + "] signature for the class [" + clazz.getName() + "]")
        } catch (e: IllegalArgumentException) {
            throw ClassException("has been passed an illegal or inappropriate argument")
        } catch (e: InstantiationException) {
            throw ClassException("the specified class object [" + clazz.getName().toString() + "] cannot be instantiated because it is an interface or is an abstract class")
        } catch (e: IllegalAccessException) {
            throw ClassException("can't load class because the currently executing method does not have access to the definition of the specified class")
        }
    }

    @Throws(ClassException::class, InvocationTargetException::class)
    fun loadInstance(className: String, args: Array<Object?>?): Object {
        return loadInstance(loadClass(className), args)
    }

    @Throws(ClassException::class, InvocationTargetException::class)
    fun loadInstance(cl: ClassLoader?, className: String?, args: Array<Object?>?): Object {
        return loadInstance(loadClass(cl, className), args)
    }

    /**
     * loads a class from a String classname
     *
     * @param clazz class to load
     * @param args
     * @return matching Class
     */
    fun loadInstance(clazz: Class, args: Array<Object?>?, defaultValue: Object): Object {
        return if (args == null || args.size == 0) loadInstance(clazz, defaultValue) else try {
            val cArgs: Array<Class?> = arrayOfNulls<Class>(args.size)
            for (i in args.indices) {
                if (args[i] == null) cArgs[i] = Object::class.java else cArgs[i] = args[i].getClass()
            }
            val c: Constructor = clazz.getConstructor(cArgs)
            c.newInstance(args)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }

    fun loadInstance(className: String?, args: Array<Object?>?, defaultValue: Object): Object {
        val clazz: Class = loadClass(className, null) ?: return defaultValue
        return loadInstance(clazz, args, defaultValue)
    }

    fun loadInstance(cl: ClassLoader, className: String, args: Array<Object?>?, defaultValue: Object): Object {
        val clazz: Class = loadClass(cl, className, null) ?: return defaultValue
        return loadInstance(clazz, args, defaultValue)
    }

    /**
     * @return returns a string array of all pathes in classpath
     */
    fun getClassPath(config: Config): Array<String> {
        val pathes: Map<String, String> = MapFactory.< String, String>getConcurrentMap<String?, String?>()
        var pathSeperator: String = System.getProperty("path.separator")
        if (pathSeperator == null) pathSeperator = ";"

        // pathes from system properties
        val strPathes: String = System.getProperty("java.class.path")
        if (strPathes != null) {
            val arr: Array = ListUtil.listToArrayRemoveEmpty(strPathes, pathSeperator)
            val len: Int = arr.size()
            for (i in 1..len) {
                val file: File = FileUtil.toFile(Caster.toString(arr.get(i, ""), "").trim())
                if (file.exists()) try {
                    pathes.put(file.getCanonicalPath(), "")
                } catch (e: IOException) {
                }
            }
        }

        // pathes from url class Loader (dynamic loaded classes)
        getClassPathesFromLoader(ClassUtil().getClass().getClassLoader(), pathes)
        getClassPathesFromLoader(config.getClassLoader(), pathes)
        val set: Set<String> = pathes.keySet()
        return set.toArray(arrayOfNulls<String>(set.size()))
    }

    /**
     * get class pathes from all url ClassLoaders
     *
     * @param cl URL Class Loader
     * @param pathes Hashmap with allpathes
     */
    private fun getClassPathesFromLoader(cl: ClassLoader, pathes: Map) {
        if (cl is URLClassLoader) _getClassPathesFromLoader(cl as URLClassLoader, pathes)
    }

    private fun _getClassPathesFromLoader(ucl: URLClassLoader, pathes: Map) {
        getClassPathesFromLoader(ucl.getParent(), pathes)

        // get all pathes
        val urls: Array<URL> = ucl.getURLs()
        for (i in urls.indices) {
            val file: File = FileUtil.toFile(urls[i].getPath())
            if (file.exists()) try {
                pathes.put(file.getCanonicalPath(), "")
            } catch (e: IOException) {
            }
        }
    }

    // CafeBabe (Java Magic Number)
    private const val ICA = 202 // CA
    private const val IFE = 254 // FE
    private const val IBA = 186 // BA
    private const val IBE = 190 // BE

    // CF33 (Lucee Magic Number)
    private const val ICF = 207 // CF
    private const val I33 = 51 // 33
    private const val BCA = ICA.toByte() // CA
    private const val BFE = IFE.toByte() // FE
    private const val BBA = IBA.toByte() // BA
    private const val BBE = IBE.toByte() // BE
    private const val BCF = ICF.toByte() // CF
    private const val B33 = I33.toByte() // 33
    private val EMPTY_CLASS: Array<Class?> = arrayOfNulls<Class>(0)
    private val EMPTY_OBJ: Array<Object?> = arrayOfNulls<Object>(0)

    /**
     * check if given stream is a bytecode stream, if yes remove bytecode mark
     *
     * @param is
     * @return is bytecode stream
     * @throws IOException
     */
    @Throws(IOException::class)
    fun isBytecode(`is`: InputStream): Boolean {
        if (!`is`.markSupported()) throw IOException("can only read input streams that support mark/reset")
        `is`.mark(-1)
        // print(bytes);
        val first: Int = `is`.read()
        val second: Int = `is`.read()
        val rtn = first == ICA && second == IFE && `is`.read() === IBA && `is`.read() === IBE
        `is`.reset()
        return rtn
    }

    @Throws(IOException::class)
    fun isEncryptedBytecode(`is`: InputStream): Boolean {
        if (!`is`.markSupported()) throw IOException("can only read input streams that support mark/reset")
        `is`.mark(-1)
        // print(bytes);
        val first: Int = `is`.read()
        val second: Int = `is`.read()
        val rtn = first == ICF && second == I33
        `is`.reset()
        return rtn
    }

    fun isBytecode(barr: ByteArray): Boolean {
        return if (barr.size < 4) false else barr[0] == BCF && barr[1] == B33 || barr[0] == BCA && barr[1] == BFE && barr[2] == BBA && barr[3] == BBE
    }

    fun isRawBytecode(barr: ByteArray): Boolean {
        return if (barr.size < 4) false else barr[0] == BCA && barr[1] == BFE && barr[2] == BBA && barr[3] == BBE
    }

    fun hasCF33Prefix(barr: ByteArray?): Boolean {
        return if (barr!!.size < 4) false else barr[0] == BCF && barr[1] == B33
    }

    fun removeCF33Prefix(barr: ByteArray): ByteArray {
        if (!hasCF33Prefix(barr)) return barr
        val dest = ByteArray(barr.size - 10)
        System.arraycopy(barr, 10, dest, 0, 10)
        return dest
    }

    fun getName(clazz: Class): String {
        return if (clazz.isArray()) {
            getName(clazz.getComponentType()) + "[]"
        } else clazz.getName()
    }

    fun getMethodIgnoreCase(clazz: Class, methodName: String?, args: Array<Class?>, defaultValue: Method?): Method? {
        val methods: Array<Method> = clazz.getMethods()
        var method: Method
        var params: Array<Class>
        outer@ for (i in methods.indices) {
            method = methods[i]
            if (method.getName().equalsIgnoreCase(methodName)) {
                params = method.getParameterTypes()
                if (params.size == args.size) {
                    for (y in params.indices) {
                        if (!params[y].equals(args[y])) {
                            continue@outer
                        }
                    }
                    return method
                }
            }
        }
        return defaultValue
    }

    @Throws(ClassException::class)
    fun getMethodIgnoreCase(clazz: Class, methodName: String, args: Array<Class?>): Method {
        val res: Method? = getMethodIgnoreCase(clazz, methodName, args, null)
        if (res != null) return res
        throw ClassException("class " + clazz.getName().toString() + " has no method with name " + methodName)
    }

    /**
     * return all field names as String array
     *
     * @param clazz class to get field names from
     * @return field names
     */
    fun getFieldNames(clazz: Class): Array<String?> {
        val fields: Array<Field> = clazz.getFields()
        val names = arrayOfNulls<String>(fields.size)
        for (i in names.indices) {
            names[i] = fields[i].getName()
        }
        return names
    }

    @Throws(IOException::class)
    fun toBytes(clazz: Class): ByteArray {
        return IOUtil.toBytes(clazz.getClassLoader().getResourceAsStream(clazz.getName().replace('.', '/').toString() + ".class"), true)
    }

    /**
     * return an array class based on the given class (opposite from Class.getComponentType())
     *
     * @param clazz
     * @return
     */
    fun toArrayClass(clazz: Class?): Class {
        return java.lang.reflect.Array.newInstance(clazz, 0).getClass()
    }

    fun toComponentType(clazz: Class<*>): Class<*> {
        var clazz: Class<*> = clazz
        var tmp: Class<*>
        while (true) {
            tmp = clazz.getComponentType()
            if (tmp == null) break
            clazz = tmp
        }
        return clazz
    }

    /**
     * returns the path to the directory or jar file that the class was loaded from
     *
     * @param clazz - the Class object to check, for a live object pass obj.getClass();
     * @param defaultValue - a value to return in case the source could not be determined
     * @return
     */
    fun getSourcePathForClass(clazz: Class, defaultValue: String): String {
        try {
            var result: String = clazz.getProtectionDomain().getCodeSource().getLocation().getPath()
            result = URLDecoder.decode(result, CharsetUtil.UTF8.name())
            result = SystemUtil.fixWindowsPath(result)
            return result
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        return defaultValue
    }

    fun getBytesForClass(clazz: Class, defaultValue: ByteArray): ByteArray {
        var `is`: InputStream? = null
        try {
            var cl: ClassLoader = clazz.getClassLoader()
            if (cl == null) cl = ClassLoader.getSystemClassLoader()
            `is` = cl.getResourceAsStream(clazz.getName().replace('.', '/').toString() + ".class")
            return IOUtil.toBytes(`is`)
        } catch (e: Exception) {
        }
        `is` = null
        var zf: ZipFile? = null
        try {
            val path: String = getSourcePathForClass(clazz, null) ?: return defaultValue
            val file = File(path)
            // zip
            if (file.isFile()) {
                zf = ZipFile(file)
                var ze: ZipEntry = zf.getEntry(clazz.getName().replace('.', '/').toString() + ".class")
                if (ze == null) ze = zf.getEntry(clazz.getName().replace('.', '\\').toString() + ".class")
                `is` = zf.getInputStream(ze)
                return IOUtil.toBytes(`is`)
            } else if (file.isDirectory()) {
                var f = File(file, clazz.getName().replace('.', '/').toString() + ".class")
                if (!f.isFile()) f = File(file, clazz.getName().replace('.', '\\').toString() + ".class")
                if (f.isFile()) return IOUtil.toBytes(f)
            }
        } catch (e: Exception) {
        } finally {
            IOUtil.closeEL(`is`)
            IOUtil.closeELL(zf)
        }
        return defaultValue
    }

    /**
     * tries to load the class and returns the path that it was loaded from
     *
     * @param className - the name of the class to check
     * @param defaultValue - a value to return in case the source could not be determined
     * @return
     */
    fun getSourcePathForClass(className: String, defaultValue: String): String {
        try {
            return getSourcePathForClass(loadClass(className), defaultValue)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        return defaultValue
    }

    /**
     * extracts the package from a className, return null, if there is none.
     *
     * @param className
     * @return
     */
    fun extractPackage(className: String?): String? {
        if (className == null) return null
        val index: Int = className.lastIndexOf('.')
        return if (index != -1) className.substring(0, index) else null
    }

    /**
     * extracts the class name of a classname with package
     *
     * @param className
     * @return
     */
    fun extractName(className: String?): String? {
        if (className == null) return null
        val index: Int = className.lastIndexOf('.')
        return if (index != -1) className.substring(index + 1) else className
    }

    /**
     * if no bundle is defined it is loaded the old way
     *
     * @param className
     * @param bundleName
     * @param bundleVersion
     * @param id
     * @return
     * @throws ClassException
     * @throws BundleException
     */
    @Throws(ClassException::class, BundleException::class)
    fun loadClass(className: String, bundleName: String?, bundleVersion: String?, id: Identification?, addional: List<Resource?>?): Class {
        return if (StringUtil.isEmpty(bundleName)) loadClass(className) else loadClassByBundle(className, bundleName, bundleVersion, id, addional)
    }

    fun getClassLoader(clazz: Class): ClassLoader {
        val cl: ClassLoader = clazz.getClassLoader()
        if (cl != null) return cl
        val config: Config = ThreadLocalPageContext.getConfig()
        return if (config is ConfigPro) {
            (config as ConfigPro).getClassLoaderCore()
        } else ClassLoaderHelper().getClass().getClassLoader()
    }

    @Throws(InstantiationException::class, IllegalAccessException::class, IllegalArgumentException::class, InvocationTargetException::class, NoSuchMethodException::class, SecurityException::class)
    fun newInstance(clazz: Class): Object {
        return clazz.getConstructor(EMPTY_CLASS).newInstance(EMPTY_OBJ)
    }

    private interface ClassLoading {
        fun loadClass(className: String?, defaultValue: Class?): Class<*>?
        fun loadClass(className: String?, defaultValue: Class?, exceptions: Set<Throwable?>?): Class<*>?
    }

    private class ClassLoaderBasedClassLoading(cl: ClassLoader?) : ClassLoading {
        private val cl: ClassLoader?
        @Override
        override fun loadClass(className: String, defaultValue: Class?): Class<*> {
            return loadClass(className, defaultValue, null)
        }

        @Override
        override fun loadClass(className: String, defaultValue: Class?, exceptions: Set<Throwable?>?): Class<*>? {
            var className = className
            className = className.trim()
            return try {
                cl.loadClass(className)
            } catch (e: Exception) {
                try {
                    Class.forName(className, false, cl)
                } catch (e2: Exception) {
                    exceptions?.add(e2)
                    defaultValue
                }
            }
        } /*
		 * @Override public Class<?> loadClass(String className) throws ClassException {
		 * className=className.trim(); try { return cl.loadClass(className); } catch(Throwable t)
		 * {ExceptionUtil.rethrowIfNecessary(t); try { return Class.forName(className, false, cl); } catch
		 * (Throwable t2) {ExceptionUtil.rethrowIfNecessary(t2); String msg=null; if(t2 instanceof
		 * ClassNotFoundException || t2 instanceof NoClassDefFoundError) {
		 * msg="["+t2.getClass().getName()+"] "+t2.getMessage(); } if(StringUtil.isEmpty(msg))
		 * msg="cannot load class through its string name, because no definition for the class with the specified name "
		 * + "["+className+"] could be found";
		 * 
		 * throw new ClassException(msg); } } }
		 */

        init {
            this.cl = cl
        }
    }

    private class OSGiBasedClassLoading : ClassLoading {
        @Override
        override fun loadClass(className: String?, defaultValue: Class?): Class<*> {
            return OSGiUtil.loadClass(className, defaultValue)
        }

        @Override
        override fun loadClass(className: String?, defaultValue: Class?, exceptions: Set<Throwable?>?): Class<*> {
            return loadClass(className, defaultValue)
        }
    }
}