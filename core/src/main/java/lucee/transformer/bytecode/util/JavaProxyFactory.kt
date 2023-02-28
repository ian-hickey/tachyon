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
package lucee.transformer.bytecode.util

import java.io.ByteArrayInputStream

/**
 * creates a Java Proxy for components, so you can use componets as java classes following a certain
 * interface or class
 */
object JavaProxyFactory {
    private const val TYPE_CFC: Short = 1
    private const val TYPE_UDF: Short = 2
    private val UDF_NAME: String? = "L" + Types.UDF.getInternalName().toString() + ";"
    private val COMPONENT_NAME: String? = "L" + Types.COMPONENT.getInternalName().toString() + ";"
    private val CONFIG_WEB_NAME: String? = "L" + Types.CONFIG_WEB.getInternalName().toString() + ";"

    // private static final Type JAVA_PROXY = Type.getType(JavaProxy.class);
    private val CFML_ENGINE_FACTORY: Type? = Type.getType(CFMLEngineFactory::class.java)
    private val CFML_ENGINE: Type? = Type.getType(CFMLEngine::class.java)
    private val JAVA_PROXY_UTIL: Type? = Type.getType(JavaProxyUtil::class.java)
    private val JAVA_PROXY_UTIL_IMPL: Type? = Type.getType(JavaProxyUtilImpl::class.java)
    private val CALL_CFC: org.objectweb.asm.commons.Method? = Method("call", Types.OBJECT, arrayOf<Type?>(Types.CONFIG_WEB, Types.COMPONENT, Types.STRING, Types.OBJECT_ARRAY))
    private val CALL_UDF: org.objectweb.asm.commons.Method? = Method("call", Types.OBJECT, arrayOf<Type?>(Types.CONFIG_WEB, Types.UDF, Types.STRING, Types.OBJECT_ARRAY))
    private val CONSTRUCTOR_CONFIG_CFC: org.objectweb.asm.commons.Method? = Method("<init>", Types.VOID, arrayOf<Type?>(Types.CONFIG_WEB, Types.COMPONENT))
    private val CONSTRUCTOR_CONFIG_UDF: org.objectweb.asm.commons.Method? = Method("<init>", Types.VOID, arrayOf<Type?>(Types.CONFIG_WEB, Types.UDF))
    private val SUPER_CONSTRUCTOR: org.objectweb.asm.commons.Method? = Method("<init>", Types.VOID, arrayOf<Type?>())
    private val TO_BOOLEAN: org.objectweb.asm.commons.Method? = Method("toBoolean", Types.BOOLEAN_VALUE, arrayOf<Type?>(Types.OBJECT))
    private val TO_FLOAT: org.objectweb.asm.commons.Method? = Method("toFloat", Types.FLOAT_VALUE, arrayOf<Type?>(Types.OBJECT))
    private val TO_INT: org.objectweb.asm.commons.Method? = Method("toInt", Types.INT_VALUE, arrayOf<Type?>(Types.OBJECT))
    private val TO_DOUBLE: org.objectweb.asm.commons.Method? = Method("toDouble", Types.DOUBLE_VALUE, arrayOf<Type?>(Types.OBJECT))
    private val TO_LONG: org.objectweb.asm.commons.Method? = Method("toLong", Types.LONG_VALUE, arrayOf<Type?>(Types.OBJECT))
    private val TO_CHAR: org.objectweb.asm.commons.Method? = Method("toChar", Types.CHAR, arrayOf<Type?>(Types.OBJECT))
    private val TO_BYTE: org.objectweb.asm.commons.Method? = Method("toByte", Types.BYTE_VALUE, arrayOf<Type?>(Types.OBJECT))
    private val TO_SHORT: org.objectweb.asm.commons.Method? = Method("toShort", Types.SHORT, arrayOf<Type?>(Types.OBJECT))
    private val TO_STRING: org.objectweb.asm.commons.Method? = Method("toString", Types.STRING, arrayOf<Type?>(Types.OBJECT))
    private val TO_: org.objectweb.asm.commons.Method? = Method("to", Types.OBJECT, arrayOf<Type?>(Types.OBJECT, Types.CLASS))
    private val _BOOLEAN: org.objectweb.asm.commons.Method? = Method("toCFML", Types.OBJECT, arrayOf<Type?>(Types.BOOLEAN_VALUE))
    private val _FLOAT: org.objectweb.asm.commons.Method? = Method("toCFML", Types.OBJECT, arrayOf<Type?>(Types.FLOAT_VALUE))
    private val _INT: org.objectweb.asm.commons.Method? = Method("toCFML", Types.OBJECT, arrayOf<Type?>(Types.INT_VALUE))
    private val _DOUBLE: org.objectweb.asm.commons.Method? = Method("toCFML", Types.OBJECT, arrayOf<Type?>(Types.DOUBLE_VALUE))
    private val _LONG: org.objectweb.asm.commons.Method? = Method("toCFML", Types.OBJECT, arrayOf<Type?>(Types.LONG_VALUE))
    private val _CHAR: org.objectweb.asm.commons.Method? = Method("toCFML", Types.OBJECT, arrayOf<Type?>(Types.CHAR))
    private val _BYTE: org.objectweb.asm.commons.Method? = Method("toCFML", Types.OBJECT, arrayOf<Type?>(Types.BYTE_VALUE))
    private val _SHORT: org.objectweb.asm.commons.Method? = Method("toCFML", Types.OBJECT, arrayOf<Type?>(Types.SHORT))
    private val _OBJECT: org.objectweb.asm.commons.Method? = Method("toCFML", Types.OBJECT, arrayOf<Type?>(Types.OBJECT))
    private val GET_INSTANCE: org.objectweb.asm.commons.Method? = Method("getInstance", CFML_ENGINE, arrayOf<Type?>())
    private val GET_JAVA_PROXY_UTIL: org.objectweb.asm.commons.Method? = Method("getJavaProxyUtil", Types.OBJECT, arrayOf<Type?>())
    fun createProxy(defaultValue: Object?, pc: PageContext?, udf: UDF?, interf: Class?): Object? {
        return try {
            createProxy(pc, udf, interf)
        } catch (e: Exception) {
            defaultValue
        }
    }

    @Throws(PageException::class, IOException::class)
    fun createProxy(pc: PageContext?, udf: UDF?, interf: Class?): Object? {
        val pci: PageContextImpl? = pc as PageContextImpl?
        val parent: ClassLoader = ClassUtil.getClassLoader(interf)
        val parents: Array<ClassLoader?> = arrayOf<ClassLoader?>(parent)
        if (!interf.isInterface()) throw IOException("definition [" + interf.getName().toString() + "] is a class and not a interface")
        val typeExtends: Type = Types.OBJECT
        val typeInterface: Type = Type.getType(interf)
        val strInterface: String = typeInterface.getInternalName()
        val className = createClassName("udf", Object::class.java, interf)

        // get ClassLoader
        var pcl: PhysicalClassLoader? = null
        pcl = try {
            pci.getRPCClassLoader(false, parents) as PhysicalClassLoader
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
        val classFile: Resource = pcl.getDirectory().getRealResource(className.concat(".class"))

        // check if already exists, if yes return
        if (classFile.exists()) {
            try {
                val obj: Object = newInstance(pcl, className, pc.getConfig(), udf)
                if (obj != null) return obj
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }
        val cw: ClassWriter = ASMUtil.getClassWriter()
        cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, className, null, typeExtends.getInternalName(), arrayOf<String?>(strInterface))

        // field Component
        var _fv: FieldVisitor = cw.visitField(Opcodes.ACC_PRIVATE, "udf", UDF_NAME, null, null)
        _fv.visitEnd()
        _fv = cw.visitField(Opcodes.ACC_PRIVATE, "config", CONFIG_WEB_NAME, null, null)
        _fv.visitEnd()

        // Constructor
        val adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC, CONSTRUCTOR_CONFIG_UDF, null, null, cw)
        val begin = Label()
        adapter.visitLabel(begin)
        adapter.loadThis()
        adapter.invokeConstructor(Types.OBJECT, SUPER_CONSTRUCTOR)

        // adapter.putField(JAVA_PROXY, arg1, arg2)
        adapter.visitVarInsn(Opcodes.ALOAD, 0)
        adapter.visitVarInsn(Opcodes.ALOAD, 1)
        adapter.visitFieldInsn(Opcodes.PUTFIELD, className, "config", CONFIG_WEB_NAME)
        adapter.visitVarInsn(Opcodes.ALOAD, 0)
        adapter.visitVarInsn(Opcodes.ALOAD, 2)
        adapter.visitFieldInsn(Opcodes.PUTFIELD, className, "udf", UDF_NAME)
        adapter.visitInsn(Opcodes.RETURN)
        val end = Label()
        adapter.visitLabel(end)
        adapter.visitLocalVariable("config", CONFIG_WEB_NAME, null, begin, end, 1)
        adapter.visitLocalVariable("udf", UDF_NAME, null, begin, end, 2)

        // adapter.returnValue();
        adapter.endMethod()

        // create methods
        val cDone: Set<Class?> = HashSet<Class?>()
        val mDone: Map<String?, Class?> = HashMap<String?, Class?>()
        _createProxy(cw, cDone, mDone, udf, interf, className)
        cw.visitEnd()

        // create class file
        val barr: ByteArray = cw.toByteArray()
        return try {
            ResourceUtil.touch(classFile)
            IOUtil.copy(ByteArrayInputStream(barr), classFile, true)
            pcl = pci.getRPCClassLoader(true, parents) as PhysicalClassLoader
            val clazz: Class<*> = pcl.loadClass(className, barr)
            newInstance(clazz, pc.getConfig(), udf)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw Caster.toPageException(t)
        }
    }

    fun createProxy(defaultValue: Object?, pc: PageContext?, cfc: Component?, extendz: Class?, vararg interfaces: Class?): Object? {
        return try {
            createProxy(pc, cfc, extendz, *interfaces)
        } catch (e: Exception) {
            defaultValue
        }
    }

    @Throws(PageException::class, IOException::class)
    fun createProxy(pc: PageContext?, cfc: Component?, extendz: Class?, vararg interfaces: Class?): Object? {
        var extendz: Class? = extendz
        var interfaces: Array<out Class?> = interfaces
        val pci: PageContextImpl? = pc as PageContextImpl?
        val parents: Array<ClassLoader?>? = extractClassLoaders(null, *interfaces)
        if (extendz == null) extendz = Object::class.java
        if (interfaces == null) interfaces = arrayOfNulls<Class?>(0) else {
            for (i in 0 until interfaces.size) {
                if (!interfaces[i].isInterface()) throw IOException("definition [" + interfaces[i].getName().toString() + "] is a class and not an interface")
            }
        }
        val typeExtends: Type = Type.getType(extendz)
        val typeInterfaces: Array<Type?> = ASMUtil.toTypes(interfaces)
        val strInterfaces = arrayOfNulls<String?>(typeInterfaces.size)
        for (i in strInterfaces.indices) {
            strInterfaces[i] = typeInterfaces[i].getInternalName()
        }
        val className = createClassName("cfc", extendz, *interfaces)

        // get ClassLoader
        var pcl: PhysicalClassLoader? = null
        pcl = try {
            pci.getRPCClassLoader(false, parents) as PhysicalClassLoader // mapping.getConfig().getRPCClassLoader(false)
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
        val classFile: Resource = pcl.getDirectory().getRealResource(className.concat(".class"))

        // check if already exists, if yes return
        if (classFile.exists()) {
            try {
                val obj: Object = newInstance(pcl, className, pc.getConfig(), cfc)
                if (obj != null) return obj
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }
        val cw: ClassWriter = ASMUtil.getClassWriter()
        cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, className, null, typeExtends.getInternalName(), strInterfaces)

        // field Component
        var _fv: FieldVisitor = cw.visitField(Opcodes.ACC_PRIVATE, "cfc", COMPONENT_NAME, null, null)
        _fv.visitEnd()
        _fv = cw.visitField(Opcodes.ACC_PRIVATE, "config", CONFIG_WEB_NAME, null, null)
        _fv.visitEnd()

        // Constructor
        val adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC, CONSTRUCTOR_CONFIG_CFC, null, null, cw)
        val begin = Label()
        adapter.visitLabel(begin)
        adapter.loadThis()
        adapter.invokeConstructor(Types.OBJECT, SUPER_CONSTRUCTOR)

        // adapter.putField(JAVA_PROXY, arg1, arg2)
        adapter.visitVarInsn(Opcodes.ALOAD, 0)
        adapter.visitVarInsn(Opcodes.ALOAD, 1)
        adapter.visitFieldInsn(Opcodes.PUTFIELD, className, "config", CONFIG_WEB_NAME)
        adapter.visitVarInsn(Opcodes.ALOAD, 0)
        adapter.visitVarInsn(Opcodes.ALOAD, 2)
        adapter.visitFieldInsn(Opcodes.PUTFIELD, className, "cfc", COMPONENT_NAME)
        adapter.visitInsn(Opcodes.RETURN)
        val end = Label()
        adapter.visitLabel(end)
        adapter.visitLocalVariable("config", CONFIG_WEB_NAME, null, begin, end, 1)
        adapter.visitLocalVariable("cfc", COMPONENT_NAME, null, begin, end, 2)

        // adapter.returnValue();
        adapter.endMethod()

        // create methods
        val cDone: Set<Class?> = HashSet<Class?>()
        val mDone: Map<String?, Class?> = HashMap<String?, Class?>()
        for (i in 0 until interfaces.size) {
            _createProxy(cw, cDone, mDone, cfc, interfaces[i], className)
        }
        cw.visitEnd()

        // create class file
        val barr: ByteArray = cw.toByteArray()
        return try {
            ResourceUtil.touch(classFile)
            IOUtil.copy(ByteArrayInputStream(barr), classFile, true)
            pcl = pci.getRPCClassLoader(true, parents) as PhysicalClassLoader
            val clazz: Class<*> = pcl.loadClass(className, barr)
            newInstance(clazz, pc.getConfig(), cfc)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw Caster.toPageException(t)
        }
    }

    private fun extractClassLoaders(cl: ClassLoader?, vararg classes: Class?): Array<ClassLoader?>? {
        var cl: ClassLoader? = cl
        val set: HashSet<ClassLoader?> = HashSet()
        if (cl != null) {
            set.add(cl)
            cl = null
        }
        if (classes != null) {
            for (i in 0 until classes.size) {
                set.add(ClassUtil.getClassLoader(classes[i]))
            }
        }
        return set.toArray(arrayOfNulls<ClassLoader?>(set.size()))
    }

    // _createProxy(cw, cDone, mDone, udf, interf, className);
    @Throws(IOException::class)
    private fun _createProxy(cw: ClassWriter?, cDone: Set<Class?>?, mDone: Map<String?, Class?>?, udf: UDF?, clazz: Class?, className: String?) {
        if (cDone!!.contains(clazz)) return
        cDone.add(clazz)

        // super class
        val superClass: Class = clazz.getSuperclass()
        if (superClass != null) _createProxy(cw, cDone, mDone, udf, superClass, className)

        // interfaces
        val interfaces: Array<Class?> = clazz.getInterfaces()
        if (interfaces != null) for (i in interfaces.indices) {
            _createProxy(cw, cDone, mDone, udf, interfaces[i], className)
        }
        val methods: Array<Method?> = clazz.getMethods()
        if (methods != null) for (i in methods.indices) {
            if (methods[i].isDefault() || Modifier.isStatic(methods[i].getModifiers())) continue
            _createMethod(cw, mDone, methods[i], className, TYPE_UDF)
        }
    }

    @Throws(IOException::class)
    private fun _createProxy(cw: ClassWriter?, cDone: Set<Class?>?, mDone: Map<String?, Class?>?, cfc: Component?, clazz: Class?, className: String?) {
        if (cDone!!.contains(clazz)) return
        cDone.add(clazz)

        // super class
        val superClass: Class = clazz.getSuperclass()
        if (superClass != null) _createProxy(cw, cDone, mDone, cfc, superClass, className)

        // interfaces
        val interfaces: Array<Class?> = clazz.getInterfaces()
        if (interfaces != null) for (i in interfaces.indices) {
            _createProxy(cw, cDone, mDone, cfc, interfaces[i], className)
        }
        val methods: Array<Method?> = clazz.getMethods()
        if (methods != null) for (i in methods.indices) {
            _createMethod(cw, mDone, methods[i], className, TYPE_CFC)
        }
    }

    @Throws(IOException::class)
    private fun _createMethod(cw: ClassWriter?, mDone: Map<String?, Class?>?, src: Method?, className: String?, type: Short) {
        val classArgs: Array<Class<*>?> = src.getParameterTypes()
        val classRtn: Class<*> = src.getReturnType()
        val str: String = src.getName().toString() + "(" + Reflector.getDspMethods(classArgs) + ")"
        val rtnClass: Class? = mDone!![str]
        if (rtnClass != null) {
            if (rtnClass !== classRtn) throw IOException("there is a conflict with method [$str], this method is declared more than once with different return types.")
            return
        }
        mDone.put(str, classRtn)
        val typeArgs: Array<Type?> = ASMUtil.toTypes(classArgs)
        val typeRtn: Type = Type.getType(classRtn)
        val method: org.objectweb.asm.commons.Method = Method(src.getName(), typeRtn, typeArgs)
        val adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, method, null, null, cw)
        // BytecodeContext bc = new
        // BytecodeContext(statConstr,constr,null,null,keys,cw,className,adapter,method,writeLog);
        val start: Label = adapter.newLabel()
        adapter.visitLabel(start)

        // if the result of "call" need castring, we have to do this here
        if (needCastring(classRtn)) {
            adapter.invokeStatic(CFML_ENGINE_FACTORY, GET_INSTANCE)
            adapter.invokeInterface(CFML_ENGINE, GET_JAVA_PROXY_UTIL)
            adapter.checkCast(JAVA_PROXY_UTIL)
        }
        adapter.invokeStatic(CFML_ENGINE_FACTORY, GET_INSTANCE)
        adapter.invokeInterface(CFML_ENGINE, GET_JAVA_PROXY_UTIL)
        adapter.checkCast(if (type == TYPE_CFC) JAVA_PROXY_UTIL else JAVA_PROXY_UTIL_IMPL) // FUTURE get rid of IMPL

        // Java Proxy.call(cfc,"add",new Object[]{arg0})
        // config (first argument)
        adapter.visitVarInsn(Opcodes.ALOAD, 0)
        adapter.visitFieldInsn(Opcodes.GETFIELD, className, "config", CONFIG_WEB_NAME)

        // cfc (second argument)
        adapter.visitVarInsn(Opcodes.ALOAD, 0)
        if (type == TYPE_CFC) adapter.visitFieldInsn(Opcodes.GETFIELD, className, "cfc", COMPONENT_NAME) else adapter.visitFieldInsn(Opcodes.GETFIELD, className, "udf", UDF_NAME)

        // name (3th argument)
        adapter.push(src.getName())

        // arguments (4th argument)
        val av = ArrayVisitor()
        av.visitBegin(adapter, Types.OBJECT, typeArgs.size)
        for (y in typeArgs.indices) {
            av.visitBeginItem(adapter, y)
            adapter.invokeStatic(CFML_ENGINE_FACTORY, GET_INSTANCE)
            adapter.invokeInterface(CFML_ENGINE, GET_JAVA_PROXY_UTIL)
            adapter.checkCast(JAVA_PROXY_UTIL) // FUTURE adapter.checkCast(JAVA_PROXY_UTIL);
            adapter.loadArg(y)
            if (classArgs[y] === Boolean::class.javaPrimitiveType) adapter.invokeInterface(JAVA_PROXY_UTIL, _BOOLEAN) else if (classArgs[y] === Byte::class.javaPrimitiveType) adapter.invokeInterface(JAVA_PROXY_UTIL, _BYTE) else if (classArgs[y] === Char::class.javaPrimitiveType) adapter.invokeInterface(JAVA_PROXY_UTIL, _CHAR) else if (classArgs[y] === Double::class.javaPrimitiveType) adapter.invokeInterface(JAVA_PROXY_UTIL, _DOUBLE) else if (classArgs[y] === Float::class.javaPrimitiveType) adapter.invokeInterface(JAVA_PROXY_UTIL, _FLOAT) else if (classArgs[y] === Int::class.javaPrimitiveType) adapter.invokeInterface(JAVA_PROXY_UTIL, _INT) else if (classArgs[y] === Long::class.javaPrimitiveType) adapter.invokeInterface(JAVA_PROXY_UTIL, _LONG) else if (classArgs[y] === Short::class.javaPrimitiveType) adapter.invokeInterface(JAVA_PROXY_UTIL, _SHORT) else adapter.invokeInterface(JAVA_PROXY_UTIL, _OBJECT)
            av.visitEndItem(adapter)
        }
        av.visitEnd()
        if (type == TYPE_CFC) adapter.invokeInterface(JAVA_PROXY_UTIL, CALL_CFC) else adapter.invokeVirtual(JAVA_PROXY_UTIL_IMPL, CALL_UDF)

        // CFMLEngineFactory.getInstance().getCastUtil().toBooleanValue(o);

        // Java Proxy.to...(...);
        var rtn: Int = Opcodes.IRETURN
        if (classRtn === Boolean::class.javaPrimitiveType) adapter.invokeInterface(JAVA_PROXY_UTIL, TO_BOOLEAN) else if (classRtn === Byte::class.javaPrimitiveType) adapter.invokeInterface(JAVA_PROXY_UTIL, TO_BYTE) else if (classRtn === Char::class.javaPrimitiveType) adapter.invokeInterface(JAVA_PROXY_UTIL, TO_CHAR) else if (classRtn === Double::class.javaPrimitiveType) {
            rtn = Opcodes.DRETURN
            adapter.invokeInterface(JAVA_PROXY_UTIL, TO_DOUBLE)
        } else if (classRtn === Float::class.javaPrimitiveType) {
            rtn = Opcodes.FRETURN
            adapter.invokeInterface(JAVA_PROXY_UTIL, TO_FLOAT)
        } else if (classRtn === Int::class.javaPrimitiveType) adapter.invokeInterface(JAVA_PROXY_UTIL, TO_INT) else if (classRtn === Long::class.javaPrimitiveType) {
            rtn = Opcodes.LRETURN
            adapter.invokeInterface(JAVA_PROXY_UTIL, TO_LONG)
        } else if (classRtn === Short::class.javaPrimitiveType) adapter.invokeInterface(JAVA_PROXY_UTIL, TO_SHORT) else if (classRtn === Void.TYPE) {
            rtn = Opcodes.RETURN
            adapter.pop()
        } else if (classRtn === String::class.java) {
            rtn = Opcodes.ARETURN
            adapter.invokeInterface(JAVA_PROXY_UTIL, TO_STRING)
        } else {
            rtn = Opcodes.ARETURN
            adapter.checkCast(typeRtn)
        }
        adapter.visitInsn(rtn)
        adapter.endMethod()
    }

    private fun needCastring(classRtn: Class<*>?): Boolean {
        return classRtn === Boolean::class.javaPrimitiveType || classRtn === Byte::class.javaPrimitiveType || classRtn === Char::class.javaPrimitiveType || classRtn === Double::class.javaPrimitiveType || classRtn === Float::class.javaPrimitiveType || classRtn === Int::class.javaPrimitiveType || classRtn === Long::class.javaPrimitiveType || classRtn === Short::class.javaPrimitiveType || classRtn === String::class.java
    }

    @Throws(IllegalArgumentException::class, InstantiationException::class, IllegalAccessException::class, InvocationTargetException::class, SecurityException::class, NoSuchMethodException::class, ClassNotFoundException::class)
    private fun newInstance(cl: PhysicalClassLoader?, className: String?, config: ConfigWeb?, udf: UDF?): Object? {
        return newInstance(cl.loadClass(className), config, udf)
    }

    @Throws(IllegalArgumentException::class, InstantiationException::class, IllegalAccessException::class, InvocationTargetException::class, SecurityException::class, NoSuchMethodException::class)
    private fun newInstance(_clazz: Class<*>?, config: ConfigWeb?, udf: UDF?): Object? {
        val constr: Constructor<*> = _clazz.getConstructor(arrayOf<Class?>(ConfigWeb::class.java, UDF::class.java))
        return constr.newInstance(arrayOf<Object?>(config, udf))
    }

    @Throws(IllegalArgumentException::class, InstantiationException::class, IllegalAccessException::class, InvocationTargetException::class, SecurityException::class, NoSuchMethodException::class, ClassNotFoundException::class)
    private fun newInstance(cl: PhysicalClassLoader?, className: String?, config: ConfigWeb?, cfc: Component?): Object? {
        return newInstance(cl.loadClass(className), config, cfc)
    }

    @Throws(IllegalArgumentException::class, InstantiationException::class, IllegalAccessException::class, InvocationTargetException::class, SecurityException::class, NoSuchMethodException::class)
    private fun newInstance(_clazz: Class<*>?, config: ConfigWeb?, cfc: Component?): Object? {
        val constr: Constructor<*> = _clazz.getConstructor(arrayOf<Class?>(ConfigWeb::class.java, Component::class.java))
        return constr.newInstance(arrayOf<Object?>(config, cfc))
    }

    @Throws(IOException::class)
    private fun createClassName(appendix: String?, extendz: Class?, vararg interfaces: Class?): String? {
        var extendz: Class? = extendz
        if (extendz == null) extendz = Object::class.java
        val sb = StringBuilder(extendz.getName())
        if (interfaces != null && interfaces.size > 0) {
            sb.append(';')
            val arr = arrayOfNulls<String?>(interfaces.size)
            for (i in 0 until interfaces.size) {
                arr[i] = interfaces[i].getName()
            }
            Arrays.sort(arr)
            sb.append(lucee.runtime.type.util.ListUtil.arrayToList(arr, ";"))
        }
        sb.append(appendix).append(';')
        return KeyGenerator.createVariable(sb.toString())
    }
}