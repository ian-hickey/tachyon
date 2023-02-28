/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
 * Class to reflect on Objects and classes
 */
object Reflector {
    private val SET_ACCESSIBLE: Collection.Key? = KeyImpl.getInstance("setAccessible")
    private val EXIT: Collection.Key? = KeyImpl.getInstance("exit")
    private val cStorage: WeakConstructorStorage? = WeakConstructorStorage()
    private val fStorage: WeakFieldStorage? = WeakFieldStorage()
    private val mStorage: SoftMethodStorage? = SoftMethodStorage()

    /**
     * check if Class is instanceof another Class
     *
     * @param srcClassName Class name to check
     * @param trg is Class of?
     * @return is Class Class of...
     */
    fun isInstaneOf(srcClassName: String?, trg: Class?): Boolean {
        val clazz: Class = ClassUtil.loadClass(srcClassName, null) ?: return false
        return isInstaneOf(clazz, trg, false)
    }

    /**
     * check if Class is instanceof another Class
     *
     * @param srcClassName Class name to check
     * @param trgClassName is Class of?
     * @return is Class Class of...
     */
    fun isInstaneOf(srcClassName: String?, trgClassName: String?): Boolean {
        val clazz: Class = ClassUtil.loadClass(srcClassName, null) ?: return false
        return isInstaneOf(clazz, trgClassName)
    }

    /**
     * check if Class is instanceof another Class
     *
     * @param src is Class of?
     * @param trgClassName Class name to check
     * @return is Class Class of...
     */
    fun isInstaneOfOld(src: Class?, trgClassName: String?): Boolean {
        val clazz: Class = ClassUtil.loadClass(trgClassName, null) ?: return false
        return isInstaneOf(src, clazz, false)
    }

    fun isInstaneOf(cl: ClassLoader?, src: Class?, trgClassName: String?): Boolean {
        val start: Long = System.currentTimeMillis()
        val clazz: Class = ClassUtil.loadClass(cl, trgClassName, null) ?: return false
        return isInstaneOf(src, clazz, false)
    }

    fun isInstaneOfIgnoreCase(src: Class?, trg: String?): Boolean {
        var src: Class? = src
        if (src.isArray()) {
            return isInstaneOfIgnoreCase(src.getComponentType(), trg)
        }
        if (src.getName().equalsIgnoreCase(trg)) return true

        // Interface
        if (_checkInterfaces(src, trg, false)) {
            return true
        }
        // Extends
        src = src.getSuperclass()
        return if (src != null) isInstaneOfIgnoreCase(src, trg) else false
    }

    fun isInstaneOf(src: Class?, trg: String?): Boolean {
        var src: Class? = src
        if (src.isArray()) {
            return isInstaneOf(src.getComponentType(), trg)
        }
        if (src.getName().equals(trg)) return true

        // Interface
        if (_checkInterfaces(src, trg, false)) {
            return true
        }
        // Extends
        src = src.getSuperclass()
        return if (src != null) isInstaneOf(src, trg) else false
    }

    /**
     * check if Class is instanceof another Class
     *
     * @param src Class to check
     * @param trg is Class of?
     * @param exatctMatch if false a valid match is when thy have the same full name, if true they also
     * need to have the same classloader
     * @return is Class Class of...
     */
    fun isInstaneOf(src: Class?, trg: Class?, exatctMatch: Boolean): Boolean {
        var src: Class? = src
        if (src.isArray() && trg.isArray()) {
            return isInstaneOf(src.getComponentType(), trg.getComponentType(), exatctMatch)
        }
        if (src === trg || !exatctMatch && src.getName().equals(trg.getName())) return true

        // Interface
        if (trg.isInterface()) {
            return _checkInterfaces(src, trg, exatctMatch)
        }
        // Extends
        while (src != null) {
            if (src === trg || !exatctMatch && src.getName().equals(trg.getName())) return true
            src = src.getSuperclass()
        }
        return trg === Object::class.java
    }

    private fun _checkInterfaces(src: Class?, trg: String?, caseSensitive: Boolean): Boolean {
        val interfaces: Array<Class?> = src.getInterfaces() ?: return false
        for (i in interfaces.indices) {
            if (caseSensitive) {
                if (interfaces[i].getName().equalsIgnoreCase(trg)) return true
            } else {
                if (interfaces[i].getName().equals(trg)) return true
            }
            if (_checkInterfaces(interfaces[i], trg, caseSensitive)) return true
        }
        return false
    }

    private fun _checkInterfaces(src: Class?, trg: Class?, exatctMatch: Boolean): Boolean {
        var src: Class? = src
        val interfaces: Array<Class?> = src.getInterfaces() ?: return false
        for (i in interfaces.indices) {
            if (interfaces[i] === trg || !exatctMatch && interfaces[i].getName().equals(trg.getName())) return true
            if (_checkInterfaces(interfaces[i], trg, exatctMatch)) return true
        }
        src = src.getSuperclass()
        return if (src != null) _checkInterfaces(src, trg, exatctMatch) else false
    }

    /**
     * get all Classes from an Object Array
     *
     * @param objs Objects to get
     * @return classes from Objects
     */
    fun getClasses(objs: Array<Object?>?): Array<Class?>? {
        val cls: Array<Class?> = arrayOfNulls<Class?>(objs!!.size)
        for (i in objs.indices) {
            if (objs!![i] == null) cls[i] = Object::class.java else cls[i] = objs[i].getClass()
        }
        return cls
    }

    /**
     * convert a primitive class Type to a Reference Type (Example: int -> java.lang.Integer)
     *
     * @param c Class to convert
     * @return converted Class (if primitive)
     */
    fun toReferenceClass(c: Class?): Class? {
        if (c.isPrimitive()) {
            if (c === Boolean::class.javaPrimitiveType) return Boolean::class.java
            if (c === Byte::class.javaPrimitiveType) return Byte::class.java
            if (c === Short::class.javaPrimitiveType) return Short::class.java
            if (c === Char::class.javaPrimitiveType) return Character::class.java
            if (c === Int::class.javaPrimitiveType) return Integer::class.java
            if (c === Long::class.javaPrimitiveType) return Long::class.java
            if (c === Float::class.javaPrimitiveType) return Float::class.java
            if (c === Double::class.javaPrimitiveType) return Double::class.java
        }
        return c
    }

    /**
     * creates a string list with class arguments in a displable form
     *
     * @param clazzArgs arguments to display
     * @return list
     */
    fun getDspMethods(vararg clazzArgs: Class?): String? {
        val sb = StringBuffer()
        for (i in 0 until clazzArgs.size) {
            if (i > 0) sb.append(", ")
            sb.append(Caster.toTypeName(clazzArgs[i]))
        }
        return sb.toString()
    }

    /**
     * checks if src Class is "like" trg class
     *
     * @param src Source Class
     * @param trg Target Class
     * @return is similar
     */
    fun like(src: Class?, trg: Class?): Boolean {
        return if (src === trg) true else isInstaneOf(src, trg, true)
    }

    /**
     * convert Object from src to trg Type, if possible
     *
     * @param src Object to convert
     * @param srcClass Source Class
     * @param trgClass Target Class
     * @return converted Object
     * @throws PageException
     */
    @Throws(PageException::class)
    fun convert(src: Object?, trgClass: Class?, rating: RefInteger?): Object? {
        if (rating != null) {
            val trg: Object? = _convert(src, trgClass, rating)
            if (src === trg) {
                rating.plus(10)
                return trg
            }
            if (src == null || trg == null) {
                rating.plus(0)
                return trg
            }
            if (isInstaneOf(src.getClass(), trg.getClass(), true)) {
                rating.plus(9)
                return trg
            }
            if (src.equals(trg)) {
                rating.plus(8)
                return trg
            }

            // different number
            val bothNumbers = src is Number && trg is Number
            if (bothNumbers && (src as Number?).doubleValue() === (trg as Number?).doubleValue()) {
                rating.plus(7)
                return trg
            }
            val sSrc: String = Caster.toString(src, null)
            val sTrg: String = Caster.toString(trg, null)
            if (sSrc != null && sTrg != null) {

                // different number types
                if (src is Number && trg is Number && sSrc.equals(sTrg)) {
                    rating.plus(6)
                    return trg
                }

                // looks the same
                if (sSrc.equals(sTrg)) {
                    rating.plus(5)
                    return trg
                }
                if (sSrc.equalsIgnoreCase(sTrg)) {
                    rating.plus(4)
                    return trg
                }
            }

            // CF Equal
            try {
                if (OpUtil.equals(ThreadLocalPageContext.get(), src, trg, false, true)) {
                    rating.plus(3)
                    return trg
                }
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
            return trg
        }
        return _convert(src, trgClass, rating)
    }

    @Throws(PageException::class)
    fun _convert(src: Object?, trgClass: Class?, rating: RefInteger?): Object? {
        var src: Object? = src
        if (src == null) {
            if (trgClass.isPrimitive()) throw ApplicationException("can't convert [null] to [" + trgClass.getName().toString() + "]")
            return null
        }
        if (like(src.getClass(), trgClass)) return src
        val className: String = trgClass.getName()
        if (src is ObjectWrap) {
            src = (src as ObjectWrap?).getEmbededObject()
            return _convert(src, trgClass, rating)
        }

        // component as class
        var pc: PageContext?
        if (src is Component && trgClass.isInterface() && ThreadLocalPageContext.get().also { pc = it } != null) {
            return componentToClass(pc, src as Component?, trgClass, rating)
        }

        // UDF as @FunctionalInterface
        if (src is UDF && ThreadLocalPageContext.get().also { pc = it } != null) {
            val fi: FunctionalInterface = trgClass.getAnnotation(FunctionalInterface::class.java) as FunctionalInterface
            if (fi != null) {
                return try {
                    JavaProxyFactory.createProxy(pc, src as UDF?, trgClass)
                } catch (e: Exception) {
                    throw Caster.toPageException(e)
                }
            }
        }

        // java.lang.String
        if (className.startsWith("java.lang.")) {
            if (trgClass === Boolean::class.java) return Caster.toBoolean(src)
            if (trgClass === Integer::class.java) return Caster.toInteger(src)
            if (trgClass === String::class.java) return Caster.toString(src)
            if (trgClass === Byte::class.java) return Caster.toByte(src)
            if (trgClass === Short::class.java) return Caster.toShort(src)
            if (trgClass === Long::class.java) return Caster.toLong(src)
            if (trgClass === Float::class.java) return Caster.toFloat(src)
            if (trgClass === Double::class.java) return Caster.toDouble(src)
            if (trgClass === Character::class.java) {
                val str: String = Caster.toString(src, null)
                if (str != null && str.length() === 1) return Character.valueOf(str.charAt(0))
            }
        }
        if (Decision.isArray(src)) {
            if (trgClass.isArray()) {
                return toNativeArray(trgClass, src)
            } else if (isInstaneOf(trgClass, List::class.java, true)) {
                return Caster.toList(src)
            } else if (isInstaneOf(trgClass, Array::class.java, true)) {
                return Caster.toArray(src)
            }
        }
        if (trgClass === Calendar::class.java && Decision.isDate(src, true)) {
            val tz: TimeZone = ThreadLocalPageContext.getTimeZone()
            return Caster.toCalendar(Caster.toDate(src, tz), tz, Locale.US)
        }
        if (trgClass === Date::class.java) return Caster.toDate(src, true, null) else if (trgClass === Query::class.java) return Caster.toQuery(src) else if (trgClass === Map::class.java) return Caster.toMap(src) else if (trgClass === Struct::class.java) return Caster.toStruct(src) else if (trgClass === Resource::class.java) return Caster.toResource(ThreadLocalPageContext.get(), src, false) else if (trgClass === Hashtable::class.java) return Caster.toHashtable(src) else if (trgClass === Vector::class.java) return Caster.toVetor(src) else if (trgClass === MutableCollection::class.java) return Caster.toJavaCollection(src) else if (trgClass === TimeZone::class.java && Decision.isString(src)) return Caster.toTimeZone(Caster.toString(src)) else if (trgClass === Collection.Key::class.java) return KeyImpl.toKey(src) else if (trgClass === Locale::class.java && Decision.isString(src)) return Caster.toLocale(Caster.toString(src)) else if (isInstaneOf(trgClass, Pojo::class.java, true) && src is Map) {
            val sct: Struct = Caster.toStruct(src)
            try {
                val pojo: Pojo = ClassUtil.newInstance(trgClass) as Pojo
                return if (sct is Component) Caster.toPojo(pojo, sct as Component, HashSet<Object?>()) else Caster.toPojo(pojo, sct, HashSet<Object?>())
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }
        if (trgClass.isPrimitive()) {
            // return convert(src,srcClass,toReferenceClass(trgClass));
            return _convert(src, toReferenceClass(trgClass), rating)
        }
        throw ApplicationException("can't convert [" + Caster.toClassName(src).toString() + "] to [" + Caster.toClassName(trgClass).toString() + "]")
    }

    @Throws(PageException::class)
    fun componentToClass(pc: PageContext?, src: Component?, trgClass: Class?): Object? {
        return componentToClass(pc, src, trgClass, null)
    }

    @Throws(PageException::class)
    private fun componentToClass(pc: PageContext?, src: Component?, trgClass: Class?, rating: RefInteger?): Object? {
        return try {
            val ja = getJavaAnnotation(pc, trgClass.getClassLoader(), src)
            val _extends: Class<*>? = if (ja != null && ja.extend != null) ja.extend else null
            JavaProxyFactory.createProxy(pc, src, _extends, extractImplements(pc, src, ja, trgClass, rating))
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(PageException::class)
    private fun extractImplements(pc: PageContext?, cfc: Component?, ja: JavaAnnotation?, trgClass: Class<*>?, ratings: RefInteger?): Array<Class<*>?>? {
        val md: Struct = cfc.getMetaData(pc)
        val implementsjavaObj: Object = md.get(KeyConstants._implementsjava, null)
        var implementsjava: Array<Class<*>?>? = null
        if (implementsjavaObj != null) {
            var arr: Array<Object?>? = null
            if (Decision.isArray(implementsjavaObj)) {
                arr = Caster.toNativeArray(implementsjavaObj)
            } else if (Decision.isCastableToString(implementsjavaObj)) {
                arr = ListUtil.listToStringArray(Caster.toString(md.get(KeyConstants._implementsjava), null), ',')
            }
            if (ratings != null) ratings.plus(0)
            if (arr != null) {
                val list: List<Class<*>?> = ArrayList<Class<*>?>()
                var tmp: Class<*>
                for (i in arr.indices) {
                    tmp = ClassUtil.loadClass(Caster.toString(md.get(KeyConstants._implementsjava), null), null)
                    if (tmp != null) {
                        list.add(tmp)
                        if (ratings != null && isInstaneOf(tmp, trgClass, true)) ratings.plus(6) else if (ratings != null && isInstaneOf(tmp, trgClass, false)) ratings.plus(5)
                    }
                }
                implementsjava = list.toArray(arrayOfNulls<Class?>(list.size()))
            }
        }
        var _implements: Array<Class<*>?>? = if (ja != null && ja.interfaces != null) ja.interfaces else arrayOf<Class?>(trgClass)
        if (implementsjava != null) {
            _implements = merge(_implements, implementsjava)
        }
        return _implements
    }

    private fun merge(left: Array<Class<*>?>?, right: Array<Class<*>?>?): Array<Class<*>?>? {
        val map: Map<String?, Class<*>?> = HashMap()
        if (left != null) {
            for (tmp in left) {
                map.put(tmp.getName(), tmp)
            }
        }
        if (right != null) {
            for (tmp in right) {
                map.put(tmp.getName(), tmp)
            }
        }
        return map.values().toArray(arrayOfNulls<Class<*>?>(map.size()))
    }

    @Throws(PageException::class)
    fun udfToClass(pc: PageContext?, src: UDF?, trgClass: Class?): Object? {
        // it already is a java Function
        return if (src is JF && isInstaneOf(src.getClass(), trgClass, true)) {
            src
        } else try {

            // JavaAnnotation ja = getJavaAnnotation(pc, trgClass.getClassLoader(), src);
            // return JavaProxyFactory.createProxy(pc, src, ja != null && ja.extend != null ? ja.extend :
            // null,ja != null && ja.interfaces != null ? ja.interfaces : new Class[] { trgClass });
            JavaProxyFactory.createProxy(pc, src, trgClass)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    private fun getJavaAnnotation(pc: PageContext?, cl: ClassLoader?, src: Component?): JavaAnnotation? {
        var md: Struct? = null
        try {
            md = src.getMetaData(pc)
        } catch (pe: PageException) {
        }
        var str: String?
        var ja: JavaAnnotation? = null
        if (md != null && Caster.toString(md.get(KeyConstants._java, null), null).also { str = it } != null) {
            var sct: Struct? = null
            try {
                sct = Caster.toStruct(DeserializeJSON.call(pc, str), null)
            } catch (e: Exception) {
            }
            if (sct == null) return null

            // interfaces
            var o: Object = sct.get(KeyConstants._interface, null)
            if (o == null) o = sct.get(KeyConstants._interfaces, null)
            if (o != null) {
                var arr: Array? = null
                if (Decision.isArray(o)) {
                    arr = Caster.toArray(o, null)
                } else {
                    str = Caster.toString(o, null)
                    if (!StringUtil.isEmpty(str)) arr = ListUtil.listToArray(str, ",")
                }
                if (arr != null && arr.size() > 0) {
                    val _interfaces: List<Class<*>?> = ArrayList()
                    val it: Iterator<*> = arr.getIterator()
                    while (it.hasNext()) {
                        str = Caster.toString(it.next(), null)
                        if (!StringUtil.isEmpty(str, true)) _interfaces.add(ClassUtil.loadClass(cl, str, null))
                    }
                    if (ja == null) ja = JavaAnnotation()
                    ja.interfaces = _interfaces.toArray(arrayOfNulls<Class<*>?>(_interfaces.size()))
                }
            }

            // extends
            o = sct.get(KeyConstants._extends, null)
            if (o != null) {
                if (ja == null) ja = JavaAnnotation()
                str = Caster.toString(o, null)
                if (!StringUtil.isEmpty(str, true)) ja.extend = ClassUtil.loadClass(cl, str, null)
            }
        }
        return ja
    }

    /**
     * gets Constructor Instance matching given parameter
     *
     * @param clazz Clazz to Invoke
     * @param args Matching args
     * @return Matching ConstructorInstance
     * @throws NoSuchMethodException
     * @throws PageException
     */
    @Throws(NoSuchMethodException::class)
    fun getConstructorInstance(clazz: Class?, args: Array<Object?>?): ConstructorInstance? {
        val ci: ConstructorInstance? = getConstructorInstance(clazz, args, null)
        if (ci != null) return ci
        throw NoSuchMethodException("No matching Constructor for " + clazz.getName().toString() + "(" + getDspMethods(*getClasses(args)!!).toString() + ") found")
    }

    fun getConstructorInstance(clazz: Class?, args: Array<Object?>?, defaultValue: ConstructorInstance?): ConstructorInstance? {
        var args: Array<Object?>? = args
        args = cleanArgs(args)
        val constructors: Array<Constructor?> = cStorage.getConstructors(clazz, args!!.size) // getConstructors(clazz);
        if (constructors != null) {
            val clazzArgs: Array<Class?>? = getClasses(args)
            // exact comparsion
            outer@ for (i in constructors.indices) {
                if (constructors[i] != null) {
                    val parameterTypes: Array<Class?> = constructors[i].getParameterTypes()
                    for (y in parameterTypes.indices) {
                        if (toReferenceClass(parameterTypes[y]) !== clazzArgs!![y]) continue@outer
                    }
                    return ConstructorInstance(constructors[i], args)
                }
            }
            // like comparsion
            outer@ for (i in constructors.indices) {
                if (constructors[i] != null) {
                    val parameterTypes: Array<Class?> = constructors[i].getParameterTypes()
                    for (y in parameterTypes.indices) {
                        if (!like(clazzArgs!![y], toReferenceClass(parameterTypes[y]))) continue@outer
                    }
                    return ConstructorInstance(constructors[i], args)
                }
            }
            // convert comparsion
            var ci: ConstructorInstance? = null
            var _rating = 0
            outer@ for (i in constructors.indices) {
                if (constructors[i] != null) {
                    val rating: RefInteger? = if (constructors.size > 1) RefIntegerImpl(0) else null
                    val parameterTypes: Array<Class?> = constructors[i].getParameterTypes()
                    val newArgs: Array<Object?> = arrayOfNulls<Object?>(args.size)
                    for (y in parameterTypes.indices) {
                        try {
                            newArgs[y] = convert(args[y], toReferenceClass(parameterTypes[y]), rating)
                        } catch (e: PageException) {
                            continue@outer
                        }
                    }
                    if (ci == null || rating.toInt() > _rating) {
                        if (rating != null) _rating = rating.toInt()
                        ci = ConstructorInstance(constructors[i], newArgs)
                    }
                    // return new ConstructorInstance(constructors[i],newArgs);
                }
            }
            return ci
        }
        return defaultValue
        // throw new NoSuchMethodException("No matching Constructor for
        // "+clazz.getName()+"("+getDspMethods(getClasses(args))+") found");
    }

    /**
     * gets the MethodInstance matching given Parameter @param objMaybeNull maybe null @param clazz
     * Class Of the Method to get @param methodName Name of the Method to get @param args Arguments of
     * the Method to get @return return Matching Method @throws
     */
    fun getMethodInstanceEL(objMaybeNull: Object?, clazz: Class?, methodName: Collection.Key?, args: Array<Object?>?): MethodInstance? {
        var args: Array<Object?>? = args
        checkAccessibility(objMaybeNull, clazz, methodName)
        args = cleanArgs(args)
        val methods: Array<Method?> = mStorage.getMethods(clazz, methodName, args!!.size) // getDeclaredMethods(clazz);
        if (methods != null) {
            val clazzArgs: Array<Class?>? = getClasses(args)
            // exact comparsion
            // print.e("exact:" + methodName);
            outer@ for (i in methods.indices) {
                if (methods[i] != null) {
                    val parameterTypes: Array<Class?> = methods[i].getParameterTypes()
                    for (y in parameterTypes.indices) {
                        if (toReferenceClass(parameterTypes[y]) !== clazzArgs!![y]) continue@outer
                    }
                    return MethodInstance(methods[i], args)
                }
            }
            // like comparsion
            // MethodInstance mi=null;
            // print.e("like:" + methodName);
            outer@ for (i in methods.indices) {
                if (methods[i] != null) {
                    val parameterTypes: Array<Class?> = methods[i].getParameterTypes()
                    for (y in parameterTypes.indices) {
                        if (!like(clazzArgs!![y], toReferenceClass(parameterTypes[y]))) continue@outer
                    }
                    return MethodInstance(methods[i], args)
                }
            }

            // convert comparsion
            // print.e("convert:" + methodName);
            var mi: MethodInstance? = null
            var _rating = 0
            outer@ for (i in methods.indices) {
                if (methods[i] != null) {
                    val rating: RefInteger? = if (methods.size > 1) RefIntegerImpl(0) else null
                    val parameterTypes: Array<Class?> = methods[i].getParameterTypes()
                    val newArgs: Array<Object?> = arrayOfNulls<Object?>(args.size)
                    for (y in parameterTypes.indices) {
                        try {
                            newArgs[y] = convert(args[y], toReferenceClass(parameterTypes[y]), rating)
                        } catch (e: PageException) {
                            continue@outer
                        }
                    }
                    if (mi == null || rating.toInt() > _rating) {
                        if (rating != null) _rating = rating.toInt()
                        mi = MethodInstance(methods[i], newArgs)
                    }
                    // return new MethodInstance(methods[i],newArgs);
                }
            }
            return mi
        }
        return null
    }

    private fun cleanArgs(args: Array<Object?>?): Array<Object?>? {
        if (args == null) return args
        val done = ObjectIdentityHashSet()
        for (i in args.indices) {
            args[i] = _clean(done, args[i])
        }
        return args
    }

    private fun _clean(done: ObjectIdentityHashSet?, obj: Object?): Object? {
        if (done.contains(obj)) return obj
        done.add(obj)
        try {
            if (obj is ObjectWrap) {
                return try {
                    (obj as ObjectWrap?).getEmbededObject()
                } catch (e: PageException) {
                    obj
                }
            }
            if (obj is Collection) return _clean(done, obj as Collection?)
            if (obj is Map) return _clean(done, obj as Map?)
            if (obj is List) return _clean(done, obj as List?)
            if (obj is Array<Object>) return _clean(done, obj as Array<Object?>?)
        } finally {
            done.remove(obj)
        }
        return obj
    }

    private fun _clean(done: ObjectIdentityHashSet?, coll: Collection?): Object? {
        var coll: Collection? = coll
        val vit: Iterator<Object?> = coll.valueIterator()
        var v: Object?
        var change = false
        while (vit.hasNext()) {
            v = vit.next()
            if (v !== _clean(done, v)) {
                change = true
                break
            }
        }
        if (!change) return coll
        coll = coll.duplicate(false)
        val eit: Iterator<Entry<Key?, Object?>?> = coll.entryIterator()
        var e: Entry<Key?, Object?>?
        while (eit.hasNext()) {
            e = eit.next()
            coll.setEL(e.getKey(), _clean(done, e.getValue()))
        }
        return coll
    }

    private fun _clean(done: ObjectIdentityHashSet?, map: Map?): Object? {
        var map: Map? = map
        val vit: Iterator = map.values().iterator()
        var v: Object
        var change = false
        while (vit.hasNext()) {
            v = vit.next()
            if (v !== _clean(done, v)) {
                change = true
                break
            }
        }
        if (!change) return map
        map = Duplicator.duplicateMap(map, false)
        val eit: Iterator<Entry?> = map.entrySet().iterator()
        var e: Entry?
        while (eit.hasNext()) {
            e = eit.next()
            map.put(e.getKey(), _clean(done, e.getValue()))
        }
        return map
    }

    private fun _clean(done: ObjectIdentityHashSet?, list: List?): Object? {
        var list: List? = list
        var it: Iterator = list.iterator()
        var v: Object
        var change = false
        while (it.hasNext()) {
            v = it.next()
            if (v !== _clean(done, v)) {
                change = true
                break
            }
        }
        if (!change) return list
        list = Duplicator.duplicateList(list, false)
        it = list.iterator()
        while (it.hasNext()) {
            list.add(_clean(done, it.next()))
        }
        return list
    }

    private fun _clean(done: ObjectIdentityHashSet?, src: Array<Object?>?): Object? {
        var change = false
        for (i in src.indices) {
            if (src!![i] !== _clean(done, src!![i])) {
                change = true
                break
            }
        }
        if (!change) return src
        val trg: Array<Object?> = arrayOfNulls<Object?>(src!!.size)
        for (i in trg.indices) {
            trg[i] = _clean(done, src!![i])
        }
        return trg
    }

    /**
     * gets the MethodInstance matching given Parameter
     *
     * @param clazz Class Of the Method to get
     * @param methodName Name of the Method to get
     * @param args Arguments of the Method to get
     * @return return Matching Method
     * @throws NoSuchMethodException
     * @throws PageException
     */
    @Throws(NoSuchMethodException::class)
    fun getMethodInstance(obj: Object?, clazz: Class?, methodName: Collection.Key?, args: Array<Object?>?): MethodInstance? {
        val mi: MethodInstance? = getMethodInstanceEL(obj, clazz, methodName, args)
        if (mi != null) return mi
        val classes: Array<Class?>? = getClasses(args)
        // StringBuilder sb=null;
        var jo: JavaObject?
        var c: Class
        var ci: ConstructorInstance?
        for (i in classes.indices) {
            if (args!![i] is JavaObject) {
                jo = args[i] as JavaObject?
                c = jo.getClazz()
                ci = getConstructorInstance(c, arrayOfNulls<Object?>(0), null)
                if (ci == null) {
                    throw NoSuchMethodException("The " + pos(i + 1) + " parameter of " + methodName + "(" + getDspMethods(*classes!!) + ") ia an object created "
                            + "by the createObject function (JavaObject/JavaProxy). This object has not been instantiated because it does not have a constructor "
                            + "that takes zero arguments. " + Constants.NAME
                            + " cannot instantiate it for you, please use the .init(...) method to instantiate it with the correct parameters first")
                }
            }
        }
        throw NoSuchMethodException("No matching Method for " + methodName + "(" + getDspMethods(*classes!!) + ") found for " + Caster.toTypeName(clazz))
    }

    private fun pos(index: Int): String? {
        if (index == 1) return "first"
        if (index == 2) return "second"
        return if (index == 3) "third" else index.toString() + "th"
    }

    /**
     * same like method getField from Class but ignore case from field name
     *
     * @param clazz class to search the field
     * @param name name to search
     * @return Matching Field
     * @throws NoSuchFieldException
     */
    @Throws(NoSuchFieldException::class)
    fun getFieldsIgnoreCase(clazz: Class?, name: String?): Array<Field?>? {
        val fields: Array<Field?> = fStorage.getFields(clazz, name)
        if (fields != null) return fields
        throw NoSuchFieldException("there is no field with name " + name + " in object [" + Type.getName(clazz) + "]")
    }

    fun getFieldsIgnoreCase(clazz: Class?, name: String?, defaultValue: Array<Field?>?): Array<Field?>? {
        val fields: Array<Field?> = fStorage.getFields(clazz, name)
        return fields ?: defaultValue
    }

    fun getPropertyKeys(clazz: Class?): Array<String?>? {
        val keys: Set<String?> = HashSet<String?>()
        val fields: Array<Field?> = clazz.getFields()
        var field: Field?
        val methods: Array<Method?> = clazz.getMethods()
        var method: Method?
        var name: String
        for (i in fields.indices) {
            field = fields[i]
            if (Modifier.isPublic(field.getModifiers())) keys.add(field.getName())
        }
        for (i in methods.indices) {
            method = methods[i]
            if (Modifier.isPublic(method.getModifiers())) {
                if (isGetter(method)) {
                    name = method.getName()
                    if (name.startsWith("get")) keys.add(StringUtil.lcFirst(method.getName().substring(3))) else keys.add(StringUtil.lcFirst(method.getName().substring(2)))
                } else if (isSetter(method)) keys.add(StringUtil.lcFirst(method.getName().substring(3)))
            }
        }
        return keys.toArray(arrayOfNulls<String?>(keys.size()))
    }

    fun hasPropertyIgnoreCase(clazz: Class?, name: String?): Boolean {
        if (hasFieldIgnoreCase(clazz, name)) return true
        val methods: Array<Method?> = clazz.getMethods()
        var method: Method?
        var n: String?
        for (i in methods.indices) {
            method = methods[i]
            if (Modifier.isPublic(method.getModifiers()) && StringUtil.endsWithIgnoreCase(method.getName(), name)) {
                n = null
                if (isGetter(method)) {
                    n = method.getName()
                    n = if (n.startsWith("get")) StringUtil.lcFirst(method.getName().substring(3)) else StringUtil.lcFirst(method.getName().substring(2))
                } else if (isSetter(method)) n = method.getName().substring(3)
                if (n != null && n.equalsIgnoreCase(name)) return true
            }
        }
        return false
    }

    fun hasFieldIgnoreCase(clazz: Class?, name: String?): Boolean {
        return !ArrayUtil.isEmpty(getFieldsIgnoreCase(clazz, name, null))
        // getFieldIgnoreCaseEL(clazz, name)!=null;
    }

    /**
     * call constructor of a class with matching arguments
     *
     * @param clazz Class to get Instance
     * @param args Arguments for the Class
     * @return invoked Instance
     * @throws PageException
     */
    @Throws(PageException::class)
    fun callConstructor(clazz: Class?, args: Array<Object?>?): Object? {
        var args: Array<Object?>? = args
        args = cleanArgs(args)
        return try {
            getConstructorInstance(clazz, args).invoke()
        } catch (e: InvocationTargetException) {
            val target: Throwable = e.getTargetException()
            if (target is PageException) throw target as PageException
            throw Caster.toPageException(e.getTargetException())
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    fun callConstructor(clazz: Class?, args: Array<Object?>?, defaultValue: Object?): Object? {
        var args: Array<Object?>? = args
        args = cleanArgs(args)
        return try {
            val ci: ConstructorInstance = getConstructorInstance(clazz, args, null)
                    ?: return defaultValue
            ci.invoke()
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }

    /**
     * calls a Method of an Object
     *
     * @param obj Object to call Method on it
     * @param methodName Name of the Method to get
     * @param args Arguments of the Method to get
     * @return return return value of the called Method
     * @throws PageException
     */
    @Throws(PageException::class)
    fun callMethod(obj: Object?, methodName: String?, args: Array<Object?>?): Object? {
        return callMethod(obj, KeyImpl.init(methodName), args)
    }

    @Throws(PageException::class)
    fun callMethod(obj: Object?, methodName: Collection.Key?, args: Array<Object?>?): Object? {
        if (obj == null) {
            throw ExpressionException("can't call method [$methodName] on object, object is null")
        }
        val mi: MethodInstance = getMethodInstanceEL(obj, obj.getClass(), methodName, args)
                ?: throw throwCall(obj, methodName, args)
        return try {
            mi.invoke(obj)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    private fun checkAccessibility(objMaybeNull: Object?, clazz: Class?, methodName: Key?) {
        var objMaybeNull: Object? = objMaybeNull
        if (methodName.equals(EXIT) && (clazz === System::class.java || clazz === Runtime::class.java)) { // TODO better implementation
            throw PageRuntimeException(SecurityException("Calling the exit method is not allowed"))
        } else if (methodName.equals(SET_ACCESSIBLE)) {
            if (objMaybeNull is JavaObject) objMaybeNull = (objMaybeNull as JavaObject?).getEmbededObject(null)
            if (objMaybeNull is Member) {
                val member: Member? = objMaybeNull as Member?
                val cls: Class<*> = member.getDeclaringClass()
                if (cls != null) {
                    val name: String = cls.getName()
                    if (name != null && name.startsWith("tachyon.")) {
                        throw PageRuntimeException(SecurityException("Changing the accessibility of an object's members in the tachyon.* package is not allowed"))
                    }
                }
            }
        }
    }

    /*
	 * private static void checkAccesibilityx(Object obj, Key methodName) {
	 * if(methodName.equals(SET_ACCESSIBLE) && obj instanceof Member) { if(true) return; Member
	 * member=(Member) obj; Class<?> cls = member.getDeclaringClass();
	 * if(cls.getPackage().getName().startsWith("tachyon.")) { throw new PageRuntimeException(new
	 * SecurityException("Changing the accesibility of an object's members in the Tachyon.* package is not allowed"
	 * )); } } }
	 */
    fun callMethod(obj: Object?, methodName: Collection.Key?, args: Array<Object?>?, defaultValue: Object?): Object? {
        if (obj == null) {
            return defaultValue
        }
        // checkAccesibility(obj,methodName);
        val mi: MethodInstance = getMethodInstanceEL(obj, obj.getClass(), methodName, args)
                ?: return defaultValue
        return try {
            mi.invoke(obj)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }

    fun throwCall(obj: Object?, methodName: String?, args: Array<Object?>?): ExpressionException? {
        return ExpressionException("No matching Method/Function for " + Type.getName(obj).toString() + "." + methodName.toString() + "(" + getDspMethods(*getClasses(args)!!).toString() + ") found")
    }

    fun throwCall(obj: Object?, methodName: Collection.Key?, args: Array<Object?>?): ExpressionException? {
        return throwCall(obj, methodName.getString(), args)
    }

    /**
     * calls a Static Method on the given CLass
     *
     * @param clazz Class to call Method on it
     * @param methodName Name of the Method to get
     * @param args Arguments of the Method to get
     * @return return return value of the called Method
     * @throws PageException
     */
    @Throws(PageException::class)
    fun callStaticMethod(clazz: Class?, methodName: Collection.Key?, args: Array<Object?>?): Object? {
        return try {
            getMethodInstance(null, clazz, methodName, args).invoke(null)
        } catch (e: InvocationTargetException) {
            val target: Throwable = e.getTargetException()
            if (target is PageException) throw target as PageException
            throw Caster.toPageException(e.getTargetException())
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    /**
     * to get a Getter Method of an Object
     *
     * @param clazz Class to invoke method from
     * @param prop Name of the Method without get
     * @return return Value of the getter Method
     * @throws NoSuchMethodException
     * @throws PageException
     */
    @Throws(PageException::class, NoSuchMethodException::class)
    fun getGetter(clazz: Class?, prop: String?): MethodInstance? {
        val getterName = "get" + StringUtil.ucFirst(prop)
        var mi: MethodInstance? = getMethodInstanceEL(null, clazz, KeyImpl.init(getterName), ArrayUtil.OBJECT_EMPTY)
        if (mi == null) {
            val isName = "is" + StringUtil.ucFirst(prop)
            mi = getMethodInstanceEL(null, clazz, KeyImpl.init(isName), ArrayUtil.OBJECT_EMPTY)
            if (mi != null) {
                val m: Method = mi.getMethod()
                val rtn: Class = m.getReturnType()
                if (rtn !== Boolean::class.java && rtn !== Boolean::class.javaPrimitiveType) mi = null
            }
        }
        if (mi == null) throw ExpressionException("No matching property [" + prop + "] found in [" + Caster.toTypeName(clazz) + "]")
        val m: Method = mi.getMethod()
        if (m.getReturnType() === Void.TYPE) throw NoSuchMethodException("invalid return Type, method [" + m.getName().toString() + "] for Property [" + getterName + "] must have return type not void")
        return mi
    }

    /**
     * to get a Getter Method of an Object
     *
     * @param clazz Class to invoke method from
     * @param prop Name of the Method without get
     * @return return Value of the getter Method
     */
    fun getGetterEL(clazz: Class?, prop: String?): MethodInstance? {
        var prop = prop
        prop = "get" + StringUtil.ucFirst(prop)
        val mi: MethodInstance = getMethodInstanceEL(null, clazz, KeyImpl.init(prop), ArrayUtil.OBJECT_EMPTY)
                ?: return null
        return if (mi.getMethod().getReturnType() === Void.TYPE) null else mi
    }

    /**
     * to invoke a getter Method of an Object
     *
     * @param obj Object to invoke method from
     * @param prop Name of the Method without get
     * @return return Value of the getter Method
     * @throws PageException
     */
    @Throws(PageException::class)
    fun callGetter(obj: Object?, prop: String?): Object? {
        return try {
            getGetter(obj.getClass(), prop).invoke(obj)
        } catch (e: InvocationTargetException) {
            val target: Throwable = e.getTargetException()
            if (target is PageException) throw target as PageException
            throw Caster.toPageException(e.getTargetException())
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    /**
     * to invoke a setter Method of an Object
     *
     * @param obj Object to invoke method from
     * @param prop Name of the Method without get
     * @param value Value to set to the Method
     * @return MethodInstance
     * @throws NoSuchMethodException
     * @throws PageException
     */
    @Throws(NoSuchMethodException::class)
    fun getSetter(obj: Object?, prop: String?, value: Object?): MethodInstance? {
        var prop = prop
        prop = "set" + StringUtil.ucFirst(prop)
        val mi: MethodInstance? = getMethodInstance(obj, obj.getClass(), KeyImpl.init(prop), arrayOf<Object?>(value))
        val m: Method = mi.getMethod()
        if (m.getReturnType() !== Void.TYPE) throw NoSuchMethodException("invalid return Type, method [" + m.getName().toString() + "] must have return type void, now [" + m.getReturnType().getName().toString() + "]")
        return mi
    }
    /*
	 * to invoke a setter Method of an Object
	 * 
	 * @param obj Object to invoke method from
	 * 
	 * @param prop Name of the Method without get
	 * 
	 * @param value Value to set to the Method
	 * 
	 * @return MethodInstance
	 * 
	 * @deprecated use instead <code>getSetter(Object obj, String prop,Object value, MethodInstance
	 * defaultValue)</code>
	 * 
	 * public static MethodInstance getSetterEL(Object obj, String prop,Object value) {
	 * prop="set"+StringUtil.ucFirst(prop); MethodInstance mi =
	 * getMethodInstanceEL(obj.getClass(),KeyImpl.init(prop),new Object[]{value}); if(mi==null) return
	 * null; Method m=mi.getMethod();
	 * 
	 * if(m.getReturnType()!=void.class) return null; return mi; }
	 */
    /**
     * to invoke a setter Method of an Object
     *
     * @param obj Object to invoke method from
     * @param prop Name of the Method without get
     * @param value Value to set to the Method
     * @return MethodInstance
     */
    fun getSetter(obj: Object?, prop: String?, value: Object?, defaultValue: MethodInstance?): MethodInstance? {
        var prop = prop
        prop = "set" + StringUtil.ucFirst(prop)
        val mi: MethodInstance = getMethodInstanceEL(obj, obj.getClass(), KeyImpl.init(prop), arrayOf<Object?>(value))
                ?: return defaultValue
        val m: Method = mi.getMethod()
        return if (m.getReturnType() !== Void.TYPE) defaultValue else mi
    }

    /**
     * to invoke a setter Method of an Object
     *
     * @param obj Object to invoke method from
     * @param prop Name of the Method without get
     * @param value Value to set to the Method
     * @throws PageException
     */
    @Throws(PageException::class)
    fun callSetter(obj: Object?, prop: String?, value: Object?) {
        try {
            getSetter(obj, prop, value).invoke(obj)
        } catch (e: InvocationTargetException) {
            val target: Throwable = e.getTargetException()
            if (target is PageException) throw target as PageException
            throw Caster.toPageException(e.getTargetException())
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    /**
     * do nothing when not exist
     *
     * @param obj
     * @param prop
     * @param value
     * @throws PageException
     */
    @Throws(PageException::class)
    fun callSetterEL(obj: Object?, prop: String?, value: Object?) {
        try {
            val setter: MethodInstance? = getSetter(obj, prop, value, null)
            if (setter != null) setter.invoke(obj)
        } catch (e: InvocationTargetException) {
            val target: Throwable = e.getTargetException()
            if (target is PageException) throw target as PageException
            throw Caster.toPageException(e.getTargetException())
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    /**
     * to get a visible Field of an object
     *
     * @param obj Object to invoke
     * @param prop property to call
     * @return property value
     * @throws PageException
     */
    @Throws(PageException::class)
    fun getField(obj: Object?, prop: String?): Object? {
        return try {
            getFieldsIgnoreCase(obj.getClass(), prop)!![0].get(obj)
        } catch (e: Throwable) {
            ExceptionUtil.rethrowIfNecessary(e)
            throw Caster.toPageException(e)
        }
    }

    fun getField(obj: Object?, prop: String?, defaultValue: Object?): Object? {
        return getField(obj, prop, false, defaultValue)
    }

    fun getField(obj: Object?, prop: String?, accessible: Boolean, defaultValue: Object?): Object? {
        if (obj == null) return defaultValue
        val fields: Array<Field?>? = getFieldsIgnoreCase(obj.getClass(), prop, null)
        return if (ArrayUtil.isEmpty(fields)) defaultValue else try {
            if (accessible) fields!![0].setAccessible(true)
            fields!![0].get(obj)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }

    /**
     * assign a value to a visible Field of an object
     *
     * @param obj Object to assign value to his property
     * @param prop name of property
     * @param value Value to assign
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setField(obj: Object?, prop: String?, value: Object?): Boolean {
        val clazz: Class = value.getClass()
        try {
            val fields: Array<Field?>? = getFieldsIgnoreCase(obj.getClass(), prop)
            // exact comparsion
            for (i in fields.indices) {
                if (toReferenceClass(fields!![i].getType()) === clazz) {
                    fields!![i].set(obj, value)
                    return true
                }
            }
            // like comparsion
            for (i in fields.indices) {
                if (like(fields!![i].getType(), clazz)) {
                    fields[i].set(obj, value)
                    return true
                }
            }
            // convert comparsion
            for (i in fields.indices) {
                try {
                    fields!![i].set(obj, convert(value, toReferenceClass(fields[i].getType()), null))
                    return true
                } catch (e: PageException) {
                }
            }
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
        return false
    }

    /**
     * to get a visible Propety (Field or Getter) of an object
     *
     * @param obj Object to invoke
     * @param prop property to call
     * @return property value
     * @throws PageException
     */
    @Throws(PageException::class)
    fun getProperty(obj: Object?, prop: String?): Object? {
        val rtn: Object? = getField(obj, prop, CollectionUtil.NULL) // NULL is used because the field can contain null as well
        if (rtn !== CollectionUtil.NULL) return rtn
        val first: Char = prop.charAt(0)
        if (first >= '0' && first <= '9') throw ApplicationException("there is no property with name [" + prop + "]  found in [" + Caster.toTypeName(obj) + "]")
        return callGetter(obj, prop)
    }

    /**
     * to get a visible Propety (Field or Getter) of an object
     *
     * @param obj Object to invoke
     * @param prop property to call
     * @return property value
     */
    fun getProperty(obj: Object?, prop: String?, defaultValue: Object?): Object? {

        // first try field
        val fields: Array<Field?>? = getFieldsIgnoreCase(obj.getClass(), prop, null)
        if (!ArrayUtil.isEmpty(fields)) {
            try {
                return fields!![0].get(obj)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }

        // then getter
        return try {
            val first: Char = prop.charAt(0)
            if (first >= '0' && first <= '9') defaultValue else getGetter(obj.getClass(), prop).invoke(obj)
        } catch (e1: Throwable) {
            ExceptionUtil.rethrowIfNecessary(e1)
            defaultValue
        }
    }

    /**
     * assign a value to a visible Property (Field or Setter) of an object
     *
     * @param obj Object to assign value to his property
     * @param prop name of property
     * @param value Value to assign
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setProperty(obj: Object?, prop: String?, value: Object?) {
        var done = false
        try {
            if (setField(obj, prop, value)) done = true
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        if (!done) callSetter(obj, prop, value)
    }

    /**
     * assign a value to a visible Property (Field or Setter) of an object
     *
     * @param obj Object to assign value to his property
     * @param prop name of property
     * @param value Value to assign
     */
    fun setPropertyEL(obj: Object?, prop: String?, value: Object?) {

        // first check for field
        val fields: Array<Field?>? = getFieldsIgnoreCase(obj.getClass(), prop, null)
        if (!ArrayUtil.isEmpty(fields)) {
            try {
                fields!![0].set(obj, value)
                return
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }

        // then check for setter
        try {
            getSetter(obj, prop, value).invoke(obj)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    @Throws(PageException::class)
    private fun toNativeArray(clazz: Class?, obj: Object?): Object? {
        // if(obj.getClass()==clazz) return obj;
        var objs: Array<Object?>? = null
        if (obj is Array) objs = toRefArray(obj as Array?) else if (obj is List) objs = toRefArray(obj as List?) else if (Decision.isNativeArray(obj)) {
            if (obj.getClass() === BooleanArray::class.java) objs = toRefArray(obj as BooleanArray?) else if (obj.getClass() === ByteArray::class.java) objs = toRefArray(obj as ByteArray?) else if (obj.getClass() === CharArray::class.java) objs = toRefArray(obj as CharArray?) else if (obj.getClass() === ShortArray::class.java) objs = toRefArray(obj as ShortArray?) else if (obj.getClass() === IntArray::class.java) objs = toRefArray(obj as IntArray?) else if (obj.getClass() === LongArray::class.java) objs = toRefArray(obj as LongArray?) else if (obj.getClass() === FloatArray::class.java) objs = toRefArray(obj as FloatArray?) else if (obj.getClass() === DoubleArray::class.java) objs = toRefArray(obj as DoubleArray?) else objs = obj // toRefArray((Object[])obj);
        }
        if (clazz === objs.getClass()) {
            return objs
        }

        // if(objs==null) return defaultValue;
        // Class srcClass = objs.getClass().getComponentType();
        val compClass: Class = clazz.getComponentType()
        val rtn: Object = java.lang.reflect.Array.newInstance(compClass, objs!!.size)
        // try{
        for (i in objs.indices) {
            java.lang.reflect.Array.set(rtn, i, convert(objs[i], compClass, null))
        }
        // }catch(Throwable t){ExceptionUtil.rethrowIfNecessary(t);}
        return rtn
    }

    private fun toRefArray(src: BooleanArray?): Array<Object?>? {
        val trg = arrayOfNulls<Boolean?>(src!!.size)
        for (i in trg.indices) {
            trg[i] = if (src[i]) Boolean.TRUE else Boolean.FALSE
        }
        return trg
    }

    private fun toRefArray(src: ByteArray?): Array<Byte?>? {
        val trg = arrayOfNulls<Byte?>(src!!.size)
        for (i in trg.indices) {
            trg[i] = Byte.valueOf(src[i])
        }
        return trg
    }

    private fun toRefArray(src: CharArray?): Array<Character?>? {
        val trg: Array<Character?> = arrayOfNulls<Character?>(src!!.size)
        for (i in trg.indices) {
            trg[i] = Character.valueOf(src!![i])
        }
        return trg
    }

    private fun toRefArray(src: ShortArray?): Array<Short?>? {
        val trg = arrayOfNulls<Short?>(src!!.size)
        for (i in trg.indices) {
            trg[i] = Short.valueOf(src[i])
        }
        return trg
    }

    private fun toRefArray(src: IntArray?): Array<Integer?>? {
        val trg: Array<Integer?> = arrayOfNulls<Integer?>(src!!.size)
        for (i in trg.indices) {
            trg[i] = Integer.valueOf(src!![i])
        }
        return trg
    }

    private fun toRefArray(src: LongArray?): Array<Long?>? {
        val trg = arrayOfNulls<Long?>(src!!.size)
        for (i in trg.indices) {
            trg[i] = Long.valueOf(src[i])
        }
        return trg
    }

    private fun toRefArray(src: FloatArray?): Array<Float?>? {
        val trg = arrayOfNulls<Float?>(src!!.size)
        for (i in trg.indices) {
            trg[i] = Float.valueOf(src[i])
        }
        return trg
    }

    private fun toRefArray(src: DoubleArray?): Array<Double?>? {
        val trg = arrayOfNulls<Double?>(src!!.size)
        for (i in trg.indices) {
            trg[i] = Double.valueOf(src[i])
        }
        return trg
    }

    @Throws(PageException::class)
    private fun toRefArray(array: Array?): Array<Object?>? {
        val objs: Array<Object?> = arrayOfNulls<Object?>(array.size())
        for (i in objs.indices) {
            objs[i] = array.getE(i + 1)
        }
        return objs
    }

    private fun toRefArray(list: List?): Array<Object?>? {
        val objs: Array<Object?> = arrayOfNulls<Object?>(list.size())
        for (i in objs.indices) {
            objs[i] = list.get(i)
        }
        return objs
    }

    fun isGetter(method: Method?): Boolean {
        if (method.getParameterTypes().length > 0) return false
        if (method.getReturnType() === Void.TYPE) return false
        if (!method.getName().startsWith("get") && !method.getName().startsWith("is")) return false
        return if (method.getDeclaringClass() === Object::class.java) false else true
    }

    fun isSetter(method: Method?): Boolean {
        if (method.getParameterTypes().length !== 1) return false
        if (method.getReturnType() !== Void.TYPE) return false
        if (!method.getName().startsWith("set")) return false
        return if (method.getDeclaringClass() === Object::class.java) false else true
    }

    /**
     * return all methods that are defined by the class itself (not extended)
     *
     * @param clazz
     * @return
     */
    fun getDeclaredMethods(clazz: Class?): Array<Method?>? {
        val methods: Array<Method?> = clazz.getMethods()
        val list: ArrayList<Method?> = ArrayList<Method?>()
        for (i in methods.indices) {
            if (methods[i].getDeclaringClass() === clazz) list.add(methods[i])
        }
        return if (list.size() === 0) arrayOfNulls<Method?>(0) else list.toArray(arrayOfNulls<Method?>(list.size()))
    }

    fun getSetters(clazz: Class?): Array<Method?>? {
        val methods: Array<Method?> = clazz.getMethods()
        val list: ArrayList<Method?> = ArrayList<Method?>()
        for (i in methods.indices) {
            if (isSetter(methods[i])) list.add(methods[i])
        }
        return if (list.size() === 0) arrayOfNulls<Method?>(0) else list.toArray(arrayOfNulls<Method?>(list.size()))
    }

    fun getGetters(clazz: Class?): Array<Method?>? {
        val methods: Array<Method?> = clazz.getMethods()
        val list: List<Method?> = ArrayList<Method?>()
        for (i in methods.indices) {
            if (isGetter(methods[i])) list.add(methods[i])
        }
        return if (list.size() === 0) arrayOfNulls<Method?>(0) else list.toArray(arrayOfNulls<Method?>(list.size()))
    }

    /**
     * check if given class "from" can be converted to class "to" without explicit casting
     *
     * @param from source class
     * @param to target class
     * @return is it possible to convert from "from" to "to"
     */
    fun canConvert(from: Class?, to: Class?): Boolean {
        // Identity Conversions
        if (from === to) return true

        // Widening Primitive Conversion
        if (from === Byte::class.javaPrimitiveType) {
            return to === Short::class.javaPrimitiveType || to === Int::class.javaPrimitiveType || to === Long::class.javaPrimitiveType || to === Float::class.javaPrimitiveType || to === Double::class.javaPrimitiveType
        }
        if (from === Short::class.javaPrimitiveType) {
            return to === Int::class.javaPrimitiveType || to === Long::class.javaPrimitiveType || to === Float::class.javaPrimitiveType || to === Double::class.javaPrimitiveType
        }
        if (from === Char::class.javaPrimitiveType) {
            return to === Int::class.javaPrimitiveType || to === Long::class.javaPrimitiveType || to === Float::class.javaPrimitiveType || to === Double::class.javaPrimitiveType
        }
        if (from === Int::class.javaPrimitiveType) {
            return to === Long::class.javaPrimitiveType || to === Float::class.javaPrimitiveType || to === Double::class.javaPrimitiveType
        }
        if (from === Long::class.javaPrimitiveType) {
            return to === Float::class.javaPrimitiveType || to === Double::class.javaPrimitiveType
        }
        return if (from === Float::class.javaPrimitiveType) {
            to === Double::class.javaPrimitiveType
        } else false
    }

    fun removeGetterPrefix(name: String?): String? {
        if (name.startsWith("get")) return name.substring(3)
        return if (name.startsWith("is")) name.substring(2) else name
    }

    fun getDeclaredMethod(clazz: Class<*>?, method: String?, arguments: Array<Class?>?, defaultValue: Method?): Method? {
        return try {
            clazz.getDeclaredMethod(method, arguments)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }

    fun getMethod(clazz: Class<*>?, methodName: String?, args: Array<Class<*>?>?, defaultValue: Method?): Method? {
        return try {
            clazz.getMethod(methodName, args)
        } catch (e: Exception) {
            defaultValue
        }
    }

    fun getConstructor(clazz: Class?, args: Array<Class?>?, defaultValue: Constructor?): Constructor? {
        outer@ for (c in clazz.getConstructors()) {
            val params: Array<Parameter?> = c.getParameters()
            if (params.size != args!!.size) continue
            for (i in params.indices) {
                if (!isInstaneOf(args[i], params[i].getType(), true)) continue@outer
            }
            return c
        }
        return defaultValue
    }

    class JavaAnnotation {
        val extend: Class<*>? = null
        val interfaces: Array<Class<*>?>?

        @Override
        override fun toString(): String {
            val sb = StringBuilder()
            if (interfaces != null && interfaces.size > 0) {
                for (clazz in interfaces) {
                    if (sb.length() > 0) sb.append(',')
                    sb.append(clazz.getName())
                }
            } else sb.append("null")
            return "extends:" + (if (extend == null) "null" else extend.getName()) + "; interfaces:" + sb
        }
    }
}