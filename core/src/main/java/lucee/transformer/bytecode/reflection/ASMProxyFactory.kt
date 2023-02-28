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
package lucee.transformer.bytecode.reflection

import java.io.ByteArrayInputStream

object ASMProxyFactory {
    val ASM_METHOD: Type? = Type.getType(ASMMethod::class.java)
    val CLASS404: Type? = Type.getType(ClassNotFoundException::class.java)

    // private static final org.objectweb.asm.commons.Method CONSTRUCTOR =
    // new org.objectweb.asm.commons.Method("<init>",Types.VOID,new
    // Type[]{Types.CLASS_LOADER,Types.CLASS});
    private val CONSTRUCTOR: org.objectweb.asm.commons.Method? = Method("<init>", Types.VOID, arrayOf<Type?>(Types.CLASS, Types.CLASS_ARRAY))
    private val LOAD_CLASS: org.objectweb.asm.commons.Method? = Method("loadClass", Types.CLASS, arrayOf<Type?>(Types.STRING))

    // public static Class loadClass(String className, Class defaultValue) {
    private val LOAD_CLASS_EL: org.objectweb.asm.commons.Method? = Method("loadClass", Types.CLASS, arrayOf<Type?>(Types.STRING, Types.CLASS))

    // public String getName();
    private val GET_NAME: org.objectweb.asm.commons.Method? = Method("getName", Types.STRING, arrayOf<Type?>())

    // public int getModifiers();
    private val GET_MODIFIERS: org.objectweb.asm.commons.Method? = Method("getModifiers", Types.INT_VALUE, arrayOf<Type?>())

    // public Class getReturnType();
    private val GET_RETURN_TYPE_AS_STRING: org.objectweb.asm.commons.Method? = Method("getReturnTypeAsString", Types.STRING, arrayOf<Type?>())
    private val INVOKE: org.objectweb.asm.commons.Method? = Method("invoke", Types.OBJECT, arrayOf<Type?>(Types.OBJECT, Types.OBJECT_ARRAY))

    // primitive to reference type
    private val BOOL_VALUE_OF: org.objectweb.asm.commons.Method? = Method("valueOf", Types.BOOLEAN, arrayOf<Type?>(Types.BOOLEAN_VALUE))
    private val SHORT_VALUE_OF: org.objectweb.asm.commons.Method? = Method("valueOf", Types.SHORT, arrayOf<Type?>(Types.SHORT_VALUE))
    private val INT_VALUE_OF: org.objectweb.asm.commons.Method? = Method("valueOf", Types.INTEGER, arrayOf<Type?>(Types.INT_VALUE))
    private val LONG_VALUE_OF: org.objectweb.asm.commons.Method? = Method("valueOf", Types.LONG, arrayOf<Type?>(Types.LONG_VALUE))
    private val FLT_VALUE_OF: org.objectweb.asm.commons.Method? = Method("valueOf", Types.FLOAT, arrayOf<Type?>(Types.FLOAT_VALUE))
    private val DBL_VALUE_OF: org.objectweb.asm.commons.Method? = Method("valueOf", Types.DOUBLE, arrayOf<Type?>(Types.DOUBLE_VALUE))
    private val CHR_VALUE_OF: org.objectweb.asm.commons.Method? = Method("valueOf", Types.CHARACTER, arrayOf<Type?>(Types.CHARACTER))
    private val BYT_VALUE_OF: org.objectweb.asm.commons.Method? = Method("valueOf", Types.BYTE, arrayOf<Type?>(Types.BYTE_VALUE))

    // reference type to primitive
    private val BOOL_VALUE: org.objectweb.asm.commons.Method? = Method("booleanValue", Types.BOOLEAN_VALUE, arrayOf<Type?>())
    private val SHORT_VALUE: org.objectweb.asm.commons.Method? = Method("shortValue", Types.SHORT_VALUE, arrayOf<Type?>())
    private val INT_VALUE: org.objectweb.asm.commons.Method? = Method("intValue", Types.INT_VALUE, arrayOf<Type?>())
    private val LONG_VALUE: org.objectweb.asm.commons.Method? = Method("longValue", Types.LONG_VALUE, arrayOf<Type?>())
    private val FLT_VALUE: org.objectweb.asm.commons.Method? = Method("floatValue", Types.FLOAT_VALUE, arrayOf<Type?>())
    private val DBL_VALUE: org.objectweb.asm.commons.Method? = Method("doubleValue", Types.DOUBLE_VALUE, arrayOf<Type?>())
    private val CHR_VALUE: org.objectweb.asm.commons.Method? = Method("charValue", Types.CHAR, arrayOf<Type?>())
    private val BYT_VALUE: org.objectweb.asm.commons.Method? = Method("byteValue", Types.BYTE_VALUE, arrayOf<Type?>())
    private val ASM_METHOD_CONSTRUCTOR: org.objectweb.asm.commons.Method? = Method("<init>", Types.VOID, arrayOf<Type?>(Types.CLASS, Types.CLASS_ARRAY))
    private val methods: Map<String?, SoftReference<ASMMethod?>?>? = ConcurrentHashMap<String?, SoftReference<ASMMethod?>?>()
    @Throws(IOException::class, InstantiationException::class, IllegalAccessException::class, IllegalArgumentException::class, SecurityException::class, InvocationTargetException::class, NoSuchMethodException::class, UnmodifiableClassException::class)
    fun getClass(pcl: ExtendableClassLoader?, classRoot: Resource?, clazz: Class?): ASMClass? {
        val type: Type = Type.getType(clazz)

        // Fields
        val fields: Array<Field?> = clazz.getFields()
        for (i in fields.indices) {
            if (Modifier.isPrivate(fields[i].getModifiers())) continue
            createField(type, fields[i])
        }

        // Methods
        val methods: Array<Method?> = clazz.getMethods()
        val amethods: Map<String?, ASMMethod?> = HashMap<String?, ASMMethod?>()
        for (i in methods.indices) {
            if (Modifier.isPrivate(methods[i].getModifiers())) continue
            amethods.put(methods[i].getName(), getMethod(pcl, classRoot, type, clazz, methods[i]))
        }
        return ASMClass(clazz.getName(), amethods)
    }

    private fun createField(type: Type?, field: Field?) {
        // TODO Auto-generated method stub
    }

    @Throws(IOException::class, InstantiationException::class, IllegalAccessException::class, SecurityException::class, NoSuchMethodException::class, IllegalArgumentException::class, InvocationTargetException::class, UnmodifiableClassException::class)
    fun getMethod(pcl: ExtendableClassLoader?, classRoot: Resource?, clazz: Class?, methodName: String?, parameters: Array<Class?>?): ASMMethod? {
        val className = createMethodName(clazz, methodName, parameters)

        // check if already in memory cache
        val tmp: SoftReference<ASMMethod?>? = methods!![className]
        var asmm: ASMMethod? = if (tmp == null) null else tmp.get()
        if (asmm != null) {
            // print.e("use loaded from memory");
            return asmm
        }

        // try to load existing ASM Class
        val asmClass: Class<*>
        asmClass = try {
            pcl.loadClass(className)
            // print.e("use existing class");
        } catch (cnfe: ClassNotFoundException) {
            val type: Type = Type.getType(clazz)
            val method: Method = clazz.getMethod(methodName, parameters)
            val barr = _createMethod(type, clazz, method, classRoot, className)
            pcl.loadClass(className, barr)
            // print.e("create class");
        }
        asmm = newInstance(asmClass, clazz, parameters)
        // methods.put(className, asmm);
        return asmm
    }

    @Throws(IOException::class, InstantiationException::class, IllegalAccessException::class, IllegalArgumentException::class, SecurityException::class, InvocationTargetException::class, NoSuchMethodException::class, UnmodifiableClassException::class)
    private fun getMethod(pcl: ExtendableClassLoader?, classRoot: Resource?, type: Type?, clazz: Class?, method: Method?): ASMMethod? {
        val className = createMethodName(clazz, method.getName(), method.getParameterTypes())

        // check if already in memory cache
        val tmp: SoftReference<ASMMethod?>? = methods!![className]
        var asmm: ASMMethod? = if (tmp == null) null else tmp.get()
        if (asmm != null) return asmm

        // try to load existing ASM Class
        val asmClass: Class<*>
        asmClass = try {
            pcl.loadClass(className)
        } catch (cnfe: ClassNotFoundException) {
            val barr = _createMethod(type, clazz, method, classRoot, className)
            pcl.loadClass(className, barr)
        }
        asmm = newInstance(asmClass, clazz, method.getParameterTypes())
        methods.put(className, SoftReference<ASMMethod?>(asmm))
        return asmm
    }

    @Throws(InstantiationException::class, IllegalAccessException::class, IllegalArgumentException::class, InvocationTargetException::class, SecurityException::class, NoSuchMethodException::class)
    private fun newInstance(asmClass: Class<*>?, decClass: Class<*>?, params: Array<Class?>?): ASMMethod? {
        val constr: Constructor<ASMMethod?> = asmClass.getConstructor(arrayOf<Class?>(Class::class.java, Array<Class>::class.java)) as Constructor<ASMMethod?>
        return constr.newInstance(arrayOf(decClass, params))

        // return (ASMMethod) asmClass.newInstance();
    }

    private fun createMethodName(clazz: Class?, methodName: String?, paramTypes: Array<Class?>?): String? {
        val sb: StringBuilder = StringBuilder("").append(clazz.getName()).append('$').append(methodName)
        paramNames(sb, paramTypes)
        return sb.toString()
    }

    @Throws(IOException::class)
    private fun _createMethod(type: Type?, clazz: Class?, method: Method?, classRoot: Resource?, className: String?): ByteArray? {
        var className = className
        val rtn: Class<*> = method.getReturnType()
        val rtnType: Type = Type.getType(rtn)
        className = className.replace('.', File.separatorChar)
        val cw: ClassWriter = ASMUtil.getClassWriter()
        cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, className, null, ASM_METHOD.getInternalName(), null)

        // CONSTRUCTOR
        var adapter: GeneratorAdapter? = GeneratorAdapter(Opcodes.ACC_PUBLIC, CONSTRUCTOR, null, null, cw)
        val begin = Label()
        adapter.visitLabel(begin)
        adapter.loadThis()
        adapter.visitVarInsn(Opcodes.ALOAD, 1)
        adapter.visitVarInsn(Opcodes.ALOAD, 2)
        adapter.invokeConstructor(ASM_METHOD, CONSTRUCTOR)
        adapter.visitInsn(Opcodes.RETURN)
        val end = Label()
        adapter.visitLabel(end)
        adapter.endMethod()

        /*
		 * 
		 * GeneratorAdapter adapter = new GeneratorAdapter(Opcodes.ACC_PUBLIC,CONSTRUCTOR,null,null,cw);
		 * 
		 * Label begin = new Label(); adapter.visitLabel(begin); adapter.loadThis();
		 * 
		 * // clazz adapter.visitVarInsn(Opcodes.ALOAD, 2);
		 * 
		 * // parameterTypes Class<?>[] params = method.getParameterTypes(); Type[] paramTypes = new
		 * Type[params.length]; ArrayVisitor av=new ArrayVisitor(); av.visitBegin(adapter, Types.CLASS,
		 * params.length); for(int i=0;i<params.length;i++){ paramTypes[i]=Type.getType(params[i]);
		 * av.visitBeginItem(adapter, i); loadClass(adapter,params[i]); av.visitEndItem(adapter); }
		 * av.visitEnd();
		 * 
		 * adapter.invokeConstructor(ASM_METHOD, ASM_METHOD_CONSTRUCTOR); adapter.visitInsn(Opcodes.RETURN);
		 * 
		 * Label end = new Label(); adapter.visitLabel(end);
		 * 
		 * adapter.endMethod();
		 */

        // METHOD getName();
        adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC, GET_NAME, null, null, cw)
        adapter.push(method.getName())
        adapter.visitInsn(Opcodes.ARETURN)
        adapter.endMethod()

        // METHOD getModifiers();
        adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC, GET_MODIFIERS, null, null, cw)
        adapter.push(method.getModifiers())
        adapter.visitInsn(Opcodes.IRETURN)
        adapter.endMethod()

        // METHOD getReturnType();
        adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC, GET_RETURN_TYPE_AS_STRING, null, null, cw)
        adapter.push(method.getReturnType().getName())
        adapter.visitInsn(Opcodes.ARETURN)
        adapter.endMethod()

        // METHOD INVOKE
        val isStatic: Boolean = Modifier.isStatic(method.getModifiers())
        adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC, INVOKE, null, null, cw)
        val start: Label = adapter.newLabel()
        adapter.visitLabel(start)

        // load Object
        if (!isStatic) {
            adapter.visitVarInsn(Opcodes.ALOAD, 1)
            adapter.checkCast(type)
        }

        // load params
        val params: Array<Class<*>?> = method.getParameterTypes()
        val paramTypes: Array<Type?> = arrayOfNulls<Type?>(params.size)
        for (i in params.indices) {
            paramTypes[i] = Type.getType(params[i])
        }
        for (i in params.indices) {
            adapter.visitVarInsn(Opcodes.ALOAD, 2)
            adapter.push(i)
            // adapter.visitInsn(Opcodes.ICONST_0);
            adapter.visitInsn(Opcodes.AALOAD)
            adapter.checkCast(toReferenceType(params[i], paramTypes[i]))

            // cast
            if (params[i] === Boolean::class.javaPrimitiveType) adapter.invokeVirtual(Types.BOOLEAN, BOOL_VALUE) else if (params[i] === Short::class.javaPrimitiveType) adapter.invokeVirtual(Types.SHORT, SHORT_VALUE) else if (params[i] === Int::class.javaPrimitiveType) adapter.invokeVirtual(Types.INTEGER, INT_VALUE) else if (params[i] === Float::class.javaPrimitiveType) adapter.invokeVirtual(Types.FLOAT, FLT_VALUE) else if (params[i] === Long::class.javaPrimitiveType) adapter.invokeVirtual(Types.LONG, LONG_VALUE) else if (params[i] === Double::class.javaPrimitiveType) adapter.invokeVirtual(Types.DOUBLE, DBL_VALUE) else if (params[i] === Char::class.javaPrimitiveType) adapter.invokeVirtual(Types.CHARACTER, CHR_VALUE) else if (params[i] === Byte::class.javaPrimitiveType) adapter.invokeVirtual(Types.BYTE, BYT_VALUE)
            // else adapter.checkCast(paramTypes[i]);
        }

        // call method
        val m: org.objectweb.asm.commons.Method = Method(method.getName(), rtnType, paramTypes)
        if (isStatic) adapter.invokeStatic(type, m) else adapter.invokeVirtual(type, m)

        // return
        if (rtn === Void.TYPE) ASMConstants.NULL(adapter)

        // cast result to object
        if (rtn === Boolean::class.javaPrimitiveType) adapter.invokeStatic(Types.BOOLEAN, BOOL_VALUE_OF) else if (rtn === Short::class.javaPrimitiveType) adapter.invokeStatic(Types.SHORT, SHORT_VALUE_OF) else if (rtn === Int::class.javaPrimitiveType) adapter.invokeStatic(Types.INTEGER, INT_VALUE_OF) else if (rtn === Long::class.javaPrimitiveType) adapter.invokeStatic(Types.LONG, LONG_VALUE_OF) else if (rtn === Float::class.javaPrimitiveType) adapter.invokeStatic(Types.FLOAT, FLT_VALUE_OF) else if (rtn === Double::class.javaPrimitiveType) adapter.invokeStatic(Types.DOUBLE, DBL_VALUE_OF) else if (rtn === Char::class.javaPrimitiveType) adapter.invokeStatic(Types.CHARACTER, CHR_VALUE_OF) else if (rtn === Byte::class.javaPrimitiveType) adapter.invokeStatic(Types.BYTE, BYT_VALUE_OF)
        adapter.visitInsn(Opcodes.ARETURN)
        adapter.endMethod()
        if (classRoot != null) {
            val classFile: Resource = classRoot.getRealResource(className.toString() + ".class")
            return store(cw.toByteArray(), classFile)
        }
        return cw.toByteArray()
    }

    private fun toReferenceType(clazz: Class<*>?, defaultValue: Type?): Type? {
        if (Int::class.javaPrimitiveType === clazz) return Types.INTEGER else if (Long::class.javaPrimitiveType === clazz) return Types.LONG else if (Char::class.javaPrimitiveType === clazz) return Types.CHARACTER else if (Byte::class.javaPrimitiveType === clazz) return Types.BYTE else if (Float::class.javaPrimitiveType === clazz) return Types.FLOAT else if (Double::class.javaPrimitiveType === clazz) return Types.DOUBLE else if (Boolean::class.javaPrimitiveType === clazz) return Types.BOOLEAN else if (Short::class.javaPrimitiveType === clazz) return Types.SHORT
        return defaultValue
    }

    private fun loadClass(adapter: GeneratorAdapter?, clazz: Class<*>?) {
        if (Void.TYPE === clazz) adapter.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Void", "TYPE", "Ljava/lang/Class;") else if (Int::class.javaPrimitiveType === clazz) adapter.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;") else if (Long::class.javaPrimitiveType === clazz) adapter.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Long", "TYPE", "Ljava/lang/Class;") else if (Char::class.javaPrimitiveType === clazz) adapter.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Character", "TYPE", "Ljava/lang/Class;") else if (Byte::class.javaPrimitiveType === clazz) adapter.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Byte", "TYPE", "Ljava/lang/Class;") else if (Float::class.javaPrimitiveType === clazz) adapter.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;") else if (Double::class.javaPrimitiveType === clazz) adapter.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Double", "TYPE", "Ljava/lang/Class;") else if (Boolean::class.javaPrimitiveType === clazz) adapter.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;") else if (Short::class.javaPrimitiveType === clazz) adapter.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Short", "TYPE", "Ljava/lang/Class;") else {
            adapter.visitVarInsn(Opcodes.ALOAD, 1)
            adapter.push(clazz.getName())
            adapter.invokeVirtual(Types.CLASS_LOADER, LOAD_CLASS)
        }
    }

    private fun paramNames(sb: StringBuilder?, params: Array<Class<*>?>?) {
        if (ArrayUtil.isEmpty(params)) return
        for (i in params.indices) {
            sb.append('$')
            if (params!![i].isArray()) sb.append(StringUtil.replace(Caster.toClassName(params[i]).replace('.', '_'), "[]", "_arr", false)) else sb.append(params[i].getName().replace('.', '_'))
        }
    }

    @Throws(IOException::class)
    private fun store(barr: ByteArray?, classFile: Resource?): ByteArray? {
        // create class file
        ResourceUtil.touch(classFile)
        // print.e(classFile);
        IOUtil.copy(ByteArrayInputStream(barr), classFile, true)
        return barr
    } /*
	 * private void store(ClassWriter cw) { // create class file byte[] barr = cw.toByteArray();
	 * 
	 * try { ResourceUtil.touch(classFile); IOUtil.copy(new ByteArrayInputStream(barr), classFile,true);
	 * 
	 * cl = (PhysicalClassLoader) mapping.getConfig().getRPCClassLoader(true); Class<?> clazz =
	 * cl.loadClass(className, barr); return newInstance(clazz, config,cfc); } catch(Throwable t) {
	 * throw Caster.toPageException(t); } }
	 */
}