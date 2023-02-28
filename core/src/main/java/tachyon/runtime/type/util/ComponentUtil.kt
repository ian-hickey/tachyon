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
package tachyon.runtime.type.util

import java.io.ByteArrayInputStream

object ComponentUtil {
    private val CONSTRUCTOR_OBJECT: Method? = Method.getMethod("void <init> ()")
    private val INVOKE: Method? = Method("invoke", Types.OBJECT, arrayOf<Type?>(Types.STRING, Types.OBJECT_ARRAY))
    val SERVER_WSUTIL: Type? = Type.getType(WSUtil::class.java)

    /**
     * generate a ComponentJavaAccess (CJA) class from a component a CJA is a dynamic genarted java
     * class that has all method defined inside a component as java methods.
     *
     * This is used to generated server side Webservices.
     *
     * @param component
     * @param isNew
     * @return
     * @throws PageException
     */
    @Throws(PageException::class)
    fun getComponentJavaAccess(pc: PageContext?, component: Component?, isNew: RefBoolean?, create: Boolean, writeLog: Boolean, suppressWSbeforeArg: Boolean, output: Boolean,
                               returnValue: Boolean): Class? {
        isNew.setValue(false)
        val classNameOriginal: String = component.getPageSource().getClassName()
        val className: String = getClassname(component, null).concat("_wrap")
        val real: String = className.replace('.', '/')
        val realOriginal: String = classNameOriginal.replace('.', '/')
        val mapping: Mapping = component.getPageSource().getMapping()
        var cl: PhysicalClassLoader? = null
        cl = try {
            (pc as PageContextImpl?).getRPCClassLoader(false) as PhysicalClassLoader
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
        val classFile: Resource = cl.getDirectory().getRealResource(real.concat(".class"))
        val classFileOriginal: Resource = mapping.getClassRootDirectory().getRealResource(realOriginal.concat(".class"))

        // LOAD CLASS
        // print.out(className);
        // check last Mod
        if (classFile.lastModified() >= classFileOriginal.lastModified()) {
            try {
                val clazz: Class = cl.loadClass(className)
                if (clazz != null && !hasChangesOfChildren(classFile.lastModified(), clazz)) return registerTypeMapping(clazz)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }
        if (!create) return null
        isNew.setValue(true)
        // print.out("new");
        // CREATE CLASS
        val cw: ClassWriter = ASMUtil.getClassWriter()
        cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, real, null, "java/lang/Object", null)

        // GeneratorAdapter ga = new
        // GeneratorAdapter(Opcodes.ACC_PUBLIC,Page.STATIC_CONSTRUCTOR,null,null,cw);
        // StaticConstrBytecodeContext statConstr = null;//new
        // BytecodeContext(null,null,null,cw,real,ga,Page.STATIC_CONSTRUCTOR);

        /// ga = new GeneratorAdapter(Opcodes.ACC_PUBLIC,Page.CONSTRUCTOR,null,null,cw);
        val constr: ConstrBytecodeContext? = null // new BytecodeContext(null,null,null,cw,real,ga,Page.CONSTRUCTOR);

        // field component
        // FieldVisitor fv = cw.visitField(Opcodes.ACC_PRIVATE, "c", "Ltachyon/runtime/ComponentImpl;", null,
        // null);
        // fv.visitEnd();
        val _keys: List<LitString?> = ArrayList<LitString?>()

        // remote methods
        val keys: Array<Collection.Key?> = component.keys(Component.ACCESS_REMOTE)
        var max: Int
        for (i in keys.indices) {
            max = -1
            while (createMethod(constr, _keys, cw, real, component.get(keys[i]), max, writeLog, suppressWSbeforeArg, output, returnValue).also { max = it } != -1) {
                break // for overload remove this
            }
        }

        // Constructor
        val adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC, CONSTRUCTOR_OBJECT, null, null, cw)
        adapter.loadThis()
        adapter.invokeConstructor(Types.OBJECT, CONSTRUCTOR_OBJECT)
        tachyon.transformer.bytecode.Page.registerFields(
                BytecodeContext(null, constr, getPage(constr), _keys, cw, real, adapter, CONSTRUCTOR_OBJECT, writeLog, suppressWSbeforeArg, output, returnValue), _keys)
        adapter.returnValue()
        adapter.endMethod()
        cw.visitEnd()
        val barr: ByteArray = cw.toByteArray()
        return try {
            ResourceUtil.touch(classFile)
            IOUtil.copy(ByteArrayInputStream(barr), classFile, true)
            cl = (pc as PageContextImpl?).getRPCClassLoader(true) as PhysicalClassLoader
            registerTypeMapping(cl.loadClass(className, barr))
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw Caster.toPageException(t)
        }
    }

    private fun getPage(bc2: BytecodeContext?): tachyon.transformer.bytecode.Page? {
        var page: tachyon.transformer.bytecode.Page? = null
        // if(bc1!=null)page=bc1.getPage();
        if (bc2 != null) page = bc2.getPage()
        return page
    }

    /**
     * check if one of the children is changed
     *
     * @param component
     * @param clazz
     * @return return true if children has changed
     */
    private fun hasChangesOfChildren(last: Long, clazz: Class?): Boolean {
        return hasChangesOfChildren(last, ThreadLocalPageContext.get(), clazz)
    }

    /**
     * check if one of the children is changed
     *
     * @param component
     * @param pc
     * @param clazz
     * @return return true if children has changed
     */
    private fun hasChangesOfChildren(last: Long, pc: PageContext?, clazz: Class?): Boolean {
        val methods: Array<java.lang.reflect.Method?> = clazz.getMethods()
        var method: java.lang.reflect.Method?
        var params: Array<Class?>
        for (i in methods.indices) {
            method = methods[i]
            if (method.getDeclaringClass() === clazz) {
                if (_hasChangesOfChildren(pc, last, method.getReturnType())) return true
                params = method.getParameterTypes()
                for (y in params.indices) {
                    if (_hasChangesOfChildren(pc, last, params[y])) return true
                }
            }
        }
        return false
    }

    private fun _hasChangesOfChildren(pc: PageContext?, last: Long, clazz: Class?): Boolean {
        var clazz: Class? = clazz
        clazz = ClassUtil.toComponentType(clazz)
        val m: java.lang.reflect.Method = getComplexTypeMethod(clazz) ?: return false
        try {
            val path: String = Caster.toString(m.invoke(null, arrayOfNulls<Object?>(0)))
            val res: Resource = ResourceUtil.toResourceExisting(pc, path)
            if (last < res.lastModified()) {
                return true
            }
        } catch (e: Exception) {
            return true
        }
        // possible that a child of the Cmplex Object is also a complex object
        return hasChangesOfChildren(last, pc, clazz)
    }

    private fun isComplexType(clazz: Class?): Boolean {
        return getComplexTypeMethod(clazz) != null
    }

    private fun getComplexTypeMethod(clazz: Class?): java.lang.reflect.Method? {
        return try {
            clazz.getMethod("_srcName", arrayOfNulls<Class?>(0))
        } catch (e: Exception) {
            null
        }
    }

    /**
     * search in methods of a class for complex types
     *
     * @param clazz
     * @return
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun registerTypeMapping(clazz: Class?): Class? {
        val pc: PageContext = ThreadLocalPageContext.get()
        val server: WSServer = (ThreadLocalPageContext.getConfig(pc) as ConfigWebPro).getWSHandler().getWSServer(pc)
        return registerTypeMapping(server, clazz)
    }

    /**
     * search in methods of a class for complex types
     *
     * @param server
     * @param clazz
     * @return
     */
    private fun registerTypeMapping(server: WSServer?, clazz: Class?): Class? {
        val methods: Array<java.lang.reflect.Method?> = clazz.getMethods()
        var method: java.lang.reflect.Method?
        var params: Array<Class?>
        for (i in methods.indices) {
            method = methods[i]
            if (method.getDeclaringClass() === clazz) {
                _registerTypeMapping(server, method.getReturnType())
                params = method.getParameterTypes()
                for (y in params.indices) {
                    _registerTypeMapping(server, params[y])
                }
            }
        }
        return clazz
    }

    /**
     * register ComplexType
     *
     * @param server
     * @param clazz
     */
    private fun _registerTypeMapping(server: WSServer?, clazz: Class?) {
        if (clazz == null) return
        if (!isComplexType(clazz)) {
            if (clazz.isArray()) {
                _registerTypeMapping(server, clazz.getComponentType())
            }
            return
        }
        server.registerTypeMapping(clazz)
        registerTypeMapping(server, clazz)
    }

    fun getClassname(component: Component?, props: Array<ASMProperty?>?): String? {
        val prefix = ""
        /*
		 * if(props!=null) { StringBuilder sb=new StringBuilder();
		 * 
		 * for(int i=0;i<props.length;i++){ sb.append(props[i].toString()).append(';'); }
		 * 
		 * 
		 * prefix = Long.toString(HashUtil.create64BitHash(sb),Character.MAX_RADIX); char
		 * c=prefix.charAt(0); if(c>='0' && c<='9') prefix="a"+prefix; prefix=prefix+"."; }
		 */
        val ps: PageSource = component.getPageSource()
        return prefix + ps.getComponentName()
    }

    /*
	 * includes the application context javasettings
	 * 
	 * @param pc
	 * 
	 * @param className
	 * 
	 * @param properties
	 * 
	 * @return
	 * 
	 * @throws PageException
	 */
    @Throws(PageException::class)
    fun getClientComponentPropertiesClass(pc: PageContext?, className: String?, properties: Array<ASMProperty?>?, extendsClass: Class?): Class? {
        return try {
            _getComponentPropertiesClass(pc, pc.getConfig(), className, properties, extendsClass)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    // FUTURE add this methid to loader, maybe make ASMProperty visible in loader
    /*
	 * does not include the application context javasettings
	 * 
	 * @param pc
	 * 
	 * @param className
	 * 
	 * @param properties
	 * 
	 * @return
	 * 
	 * @throws PageException
	 */
    @Throws(PageException::class)
    fun getComponentPropertiesClass(config: Config?, className: String?, properties: Array<ASMProperty?>?, extendsClass: Class?): Class? {
        return try {
            _getComponentPropertiesClass(null, config, className, properties, extendsClass)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(PageException::class, IOException::class, ClassNotFoundException::class)
    private fun _getComponentPropertiesClass(pc: PageContext?, secondChanceConfig: Config?, className: String?, properties: Array<ASMProperty?>?, extendsClass: Class?): Class? {
        var extendsClass: Class? = extendsClass
        val real: String = className.replace('.', '/')
        var cl: PhysicalClassLoader
        cl = if (pc == null) secondChanceConfig.getRPCClassLoader(false) as PhysicalClassLoader else (pc as PageContextImpl?).getRPCClassLoader(false) as PhysicalClassLoader
        val rootDir: Resource = cl.getDirectory()
        val classFile: Resource = rootDir.getRealResource(real.concat(".class"))
        if (classFile.exists()) {
            try {
                val clazz: Class = cl.loadClass(className)
                val field: Field = clazz.getField("_md5_")
                if (ASMUtil.createMD5(properties).equals(field.get(null))) {
                    // if(equalInterface(properties,clazz)) {
                    return clazz
                }
            } catch (e: Exception) {
            }
        }
        // create file
        if (extendsClass == null) extendsClass = Object::class.java
        val barr: ByteArray = ASMUtil.createPojo(real, properties, extendsClass, arrayOf<Class?>(Pojo::class.java), null)
        val exist: Boolean = classFile.exists()
        ResourceUtil.touch(classFile)
        IOUtil.copy(ByteArrayInputStream(barr), classFile, true)
        cl = if (pc == null) secondChanceConfig.getRPCClassLoader(exist) as PhysicalClassLoader else (pc as PageContextImpl?).getRPCClassLoader(exist) as PhysicalClassLoader
        return cl.loadClass(className)
    }

    @Throws(PageException::class)
    fun getComponentPropertiesClass(pc: PageContext?, component: Component?): Class? {
        return try {
            _getComponentPropertiesClass(pc, component)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(PageException::class, IOException::class, ClassNotFoundException::class)
    private fun _getComponentPropertiesClass(pc: PageContext?, component: Component?): Class? {
        val props: Array<ASMProperty?> = ASMUtil.toASMProperties(component.getProperties(false, true, false, false))
        val className = getClassname(component, props)
        val real: String = className.replace('.', '/')
        val mapping: Mapping = component.getPageSource().getMapping()
        var cl: PhysicalClassLoader = (pc as PageContextImpl?).getRPCClassLoader(false) as PhysicalClassLoader
        val classFile: Resource = cl.getDirectory().getRealResource(real.concat(".class"))

        // get component class information
        val classNameOriginal: String = component.getPageSource().getClassName()
        val realOriginal: String = classNameOriginal.replace('.', '/')
        val classFileOriginal: Resource = mapping.getClassRootDirectory().getRealResource(realOriginal.concat(".class"))

        // load existing class when pojo is still newer than component class file
        if (classFile.lastModified() >= classFileOriginal.lastModified()) {
            try {
                val clazz: Class = cl.loadClass(className)
                if (clazz != null && !hasChangesOfChildren(classFile.lastModified(), clazz)) return clazz // ClassUtil.loadInstance(clazz);
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }

        // extends
        val strExt: String = component.getExtends()
        var ext: Class<*> = Object::class.java
        if (!StringUtil.isEmpty(strExt, true)) {
            ext = Caster.cfTypeToClass(strExt)
        }
        //
        // create file
        val barr: ByteArray = ASMUtil.createPojo(real, props, ext, arrayOf<Class?>(Pojo::class.java), component.getPageSource().getDisplayPath())
        ResourceUtil.touch(classFile)
        IOUtil.copy(ByteArrayInputStream(barr), classFile, true)
        cl = (pc as PageContextImpl?).getRPCClassLoader(true) as PhysicalClassLoader
        return cl.loadClass(className) // ClassUtil.loadInstance(cl.loadClass(className));
    }

    @Throws(PageException::class)
    fun getStructPropertiesClass(pc: PageContext?, sct: Struct?, cl: PhysicalClassLoader?): Class? {
        return try {
            _getStructPropertiesClass(pc, sct, cl)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(PageException::class, IOException::class, ClassNotFoundException::class)
    private fun _getStructPropertiesClass(pc: PageContext?, sct: Struct?, cl: PhysicalClassLoader?): Class? {
        // create hash based on the keys of the struct
        var cl: PhysicalClassLoader? = cl
        var hash: String = StructUtil.keyHash(sct)
        val c: Char = hash.charAt(0)
        if (c >= '0' && c <= '9') hash = "a$hash"

        // create class name (struct class name + hash)
        val className: String = sct.getClass().getName().toString() + "." + hash

        // create physcal location for the file
        val real: String = className.replace('.', '/')
        val classFile: Resource = cl.getDirectory().getRealResource(real.concat(".class"))

        // load existing class
        if (classFile.exists()) {
            try {
                val clazz: Class = cl.loadClass(className)
                if (clazz != null) return clazz
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }

        // Properties
        val props: List<ASMProperty?> = ArrayList<ASMProperty?>()
        val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
        var e: Entry<Key?, Object?>?
        while (it.hasNext()) {
            e = it.next()
            props.add(ASMPropertyImpl(ASMUtil.toType(if (e.getValue() == null) Object::class.java else Object::class.java /* e.getValue().getClass() */, true), e.getKey().getString()))
        }

        // create file
        val barr: ByteArray = ASMUtil.createPojo(real, props.toArray(arrayOfNulls<ASMProperty?>(props.size())), Object::class.java, arrayOf<Class?>(Pojo::class.java), null)

        // create class file from bytecode
        ResourceUtil.touch(classFile)
        IOUtil.copy(ByteArrayInputStream(barr), classFile, true)
        cl = (pc as PageContextImpl?).getRPCClassLoader(true) as PhysicalClassLoader
        return cl.loadClass(className)
    }

    @Throws(PageException::class)
    private fun createMethod(constr: ConstrBytecodeContext?, keys: List<LitString?>?, cw: ClassWriter?, className: String?, member: Object?, max: Int, writeLog: Boolean,
                             suppressWSbeforeArg: Boolean, output: Boolean, returnValue: Boolean): Int {
        var max = max
        var hasOptionalArgs = false
        if (member is UDF) {
            val udf: UDF? = member as UDF?
            val args: Array<FunctionArgument?> = udf.getFunctionArguments()
            val types: Array<Type?> = arrayOfNulls<Type?>(if (max < 0) args.size else max)
            for (y in types.indices) {
                types[y] = toType(args[y].getTypeAsString(), true)
                if (!args[y].isRequired()) hasOptionalArgs = true
            }
            val rtnType: Type? = toType(udf.getReturnTypeAsString(), true)
            val method = Method(udf.getFunctionName(), rtnType, types)
            val adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, method, null, null, cw)
            val bc = BytecodeContext(null, constr, getPage(constr), keys, cw, className, adapter, method, writeLog, suppressWSbeforeArg, output, returnValue)
            val start: Label = adapter.newLabel()
            adapter.visitLabel(start)

            // ComponentController.invoke(name, args);
            // name
            adapter.push(udf.getFunctionName())

            // args
            val av = ArrayVisitor()
            av.visitBegin(adapter, Types.OBJECT, types.size)
            for (y in types.indices) {
                av.visitBeginItem(adapter, y)
                adapter.loadArg(y)
                av.visitEndItem(bc.getAdapter())
            }
            av.visitEnd()
            adapter.invokeStatic(SERVER_WSUTIL, INVOKE)
            adapter.checkCast(rtnType)

            // ASMConstants.NULL(adapter);
            adapter.returnValue()
            val end: Label = adapter.newLabel()
            adapter.visitLabel(end)
            for (y in types.indices) {
                adapter.visitLocalVariable(args[y].getName().getString(), types[y].getDescriptor(), null, start, end, y + 1)
            }
            adapter.endMethod()
            if (hasOptionalArgs) {
                if (max == -1) max = args.size - 1 else max--
                return max
            }
        }
        return -1
    }

    @Throws(PageException::class)
    private fun toType(cfType: String?, axistype: Boolean): Type? {
        var clazz: Class = Caster.cfTypeToClass(cfType)
        if (axistype) clazz = (ThreadLocalPageContext.getConfig() as ConfigWebPro).getWSHandler().toWSTypeClass(clazz)
        return Type.getType(clazz)
    }

    @Throws(IOException::class)
    fun md5(c: Component?): String? {
        return md5(ComponentSpecificAccess.toComponentSpecificAccess(Component.ACCESS_PRIVATE, c))
    }

    @Throws(IOException::class)
    fun md5(cw: ComponentSpecificAccess?): String? {
        val keys: Array<Key?> = cw.keys()
        Arrays.sort(keys)
        val _interface = StringBuffer()
        var member: Object
        var udf: UDF
        var args: Array<FunctionArgument?>
        var arg: FunctionArgument?
        for (y in keys.indices) {
            member = cw.get(keys[y], null)
            if (member is UDF) {
                udf = member as UDF
                // print.out(udf.);
                _interface.append(udf.getAccess())
                _interface.append(udf.getOutput())
                _interface.append(udf.getFunctionName())
                _interface.append(udf.getReturnTypeAsString())
                args = udf.getFunctionArguments()
                for (i in args.indices) {
                    arg = args[i]
                    _interface.append(arg.isRequired())
                    _interface.append(arg.getName())
                    _interface.append(arg.getTypeAsString())
                }
            }
        }
        return MD5.getDigestAsString(_interface.toString().toLowerCase())
    }

    /**
     * cast a strong access definition to the int type
     *
     * @param access access type
     * @return int access type
     * @throws ExpressionException
     */
    @Throws(ApplicationException::class)
    fun toIntAccess(access: String?): Int {
        var access = access
        access = StringUtil.toLowerCase(access.trim())
        if (access.equals("package")) return Component.ACCESS_PACKAGE else if (access.equals("private")) return Component.ACCESS_PRIVATE else if (access.equals("public")) return Component.ACCESS_PUBLIC else if (access.equals("remote")) return Component.ACCESS_REMOTE
        throw ApplicationException("Invalid function access type [$access], access types are [remote, public, package, private]")
    }

    fun toIntAccess(access: String?, defaultValue: Int): Int {
        var access = access
        access = StringUtil.toLowerCase(access.trim())
        if (access.equals("package")) return Component.ACCESS_PACKAGE else if (access.equals("private")) return Component.ACCESS_PRIVATE else if (access.equals("public")) return Component.ACCESS_PUBLIC else if (access.equals("remote")) return Component.ACCESS_REMOTE
        return defaultValue
    }

    /**
     * cast int type to string type
     *
     * @param access
     * @return String access type
     * @throws ExpressionException
     */
    @Throws(ApplicationException::class)
    fun toStringAccess(access: Int): String? {
        val res = toStringAccess(access, null)
        if (res != null) return res
        throw ApplicationException("Invalid function access type [" + access
                + "], access types are [Component.ACCESS_PACKAGE, Component.ACCESS_PRIVATE, Component.ACCESS_PUBLIC, Component.ACCESS_REMOTE]")
    }

    fun toStringAccess(access: Int, defaultValue: String?): String? {
        when (access) {
            Component.ACCESS_PACKAGE -> return "package"
            Component.ACCESS_PRIVATE -> return "private"
            Component.ACCESS_PUBLIC -> return "public"
            Component.ACCESS_REMOTE -> return "remote"
        }
        return defaultValue
    }

    fun notFunction(c: Component?, key: Collection.Key?, member: Object?, access: Int): ExpressionException? {
        if (member == null) {
            val strAccess = toStringAccess(access, "")
            val other: Array<Collection.Key?> = c.keys(access)
            return if (other.size == 0) ExpressionException("Component [" + c.getCallName().toString() + "] has no " + strAccess.toString() + " function with name [" + key.toString() + "]") else ExpressionException("Component [" + c.getCallName().toString() + "] has no " + strAccess.toString() + " function with name [" + key.toString() + "]", "Accessible functions are [" + ListUtil.arrayToList(other, ", ").toString() + "]")
        }
        return ExpressionException("Member [" + key + "] of component [" + c.getCallName() + "] is not a function", "Member is of type [" + Caster.toTypeName(member).toString() + "]")
    }

    fun getProperties(c: Component?, onlyPeristent: Boolean, includeBaseProperties: Boolean, preferBaseProperties: Boolean, inheritedMappedSuperClassOnly: Boolean): Array<Property?>? {
        return c.getProperties(onlyPeristent, includeBaseProperties, preferBaseProperties, preferBaseProperties)
    }

    /*
	 * public static ComponentAccess toComponentAccess(Component comp) throws ExpressionException {
	 * ComponentAccess ca = toComponentAccess(comp, null); if(ca!=null) return ca; throw new
	 * ExpressionException("can't cast class ["+Caster.toClassName(comp)
	 * +"] to a class of type ComponentAccess"); }
	 */
    /*
	 * public static Component toComponentAccess(Component comp, Component defaultValue) { if(comp
	 * instanceof ComponentAccess) return (ComponentAccess) comp; if(comp instanceof
	 * ComponentSpecificAccess) return ((ComponentSpecificAccess) comp).getComponentAccess(); return
	 * defaultValue; }
	 */
    @Throws(ExpressionException::class)
    fun toComponent(obj: Object?): Component? {
        if (obj is Component) return obj as Component?
        throw ExpressionException("Can't cast class [" + Caster.toClassName(obj).toString() + "] to a class of type [Component]")
    }

    fun getPageSource(cfc: Component?): PageSource? {
        // TODO Auto-generated method stub
        return try {
            toComponent(cfc).getPageSource()
        } catch (e: ExpressionException) {
            null
        }
    }

    fun getActiveComponent(pc: PageContext?, current: Component?): Component? {
        if (pc.getActiveComponent() == null) return current
        return if (pc.getActiveUDF() != null && pc.getActiveComponent().getPageSource() === pc.getActiveUDF().getOwnerComponent().getPageSource()) {
            pc.getActiveUDF().getOwnerComponent()
        } else pc.getActiveComponent()
    }

    fun getCompileTime(pc: PageContext?, ps: PageSource?, defaultValue: Long): Long {
        return try {
            getCompileTime(pc, ps)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }

    @Throws(PageException::class)
    fun getCompileTime(pc: PageContext?, ps: PageSource?): Long {
        return getPage(pc, ps).getCompileTime()
    }

    @Throws(PageException::class)
    fun getPage(pc: PageContext?, ps: PageSource?): Page? {
        var pc: PageContext? = pc
        val psi: PageSourceImpl? = ps as PageSourceImpl?
        val p: Page = psi.getPage()
        if (p != null) {
            // print.o("getPage(existing):"+ps.getDisplayPath()+":"+psi.hashCode()+":"+p.hashCode());
            return p
        }
        pc = ThreadLocalPageContext.get(pc)
        return psi.loadPage(pc, false)
    }

    fun getPropertiesAsStruct(c: Component?, onlyPersistent: Boolean): Struct? {
        val props: Array<Property?> = c.getProperties(onlyPersistent, false, false, false)
        val sct: Struct = StructImpl()
        if (props != null) for (i in props.indices) {
            sct.setEL(KeyImpl.getInstance(props[i].getName()), props[i])
        }
        return sct
    }

    @Throws(PageException::class)
    fun getMetaData(pc: PageContext?, udf: UDFPropertiesBase?): Struct? {
        return getMetaData(pc, udf, false)
    }

    @Throws(PageException::class)
    fun getMetaData(pc: PageContext?, udf: UDFPropertiesBase?, isStatic: Boolean?): Struct? {
        return getMetaData(pc, null, udf, isStatic)
    }

    @Throws(PageException::class)
    fun getMetaData(pc: PageContext?, udf: UDF?, udfProps: UDFPropertiesBase?, isStatic: Boolean?): Struct? {
        var pc: PageContext? = pc
        val func = StructImpl()
        pc = ThreadLocalPageContext.get(pc)
        // TODO func.set("roles", value);
        // TODO func.set("userMetadata", value); neo unterstuetzt irgendwelche a
        // meta data
        val meta: Struct = udfProps.getMeta()
        if (meta != null) StructUtil.copy(meta, func, true)
        var isJava = false
        var isJavaLambda = false
        var lavaLambda: String? = null
        if (udf != null) {
            isJava = udf is tachyon.runtime.JF
            val clazz: Class<out UDF?> = udf.getClass()
            val interfaces: Array<Class<*>?> = clazz.getInterfaces()
            if (interfaces != null) {
                for (interf in interfaces) {
                    if (interf.getName().startsWith("java.util.function.")) {
                        isJavaLambda = true
                        lavaLambda = interf.getName()
                    }
                }
            }
        }
        if (isJavaLambda) func.setEL("javaLambdaInterface", lavaLambda)
        func.setEL(KeyConstants._java, if (isJava) Boolean.TRUE else Boolean.FALSE)
        func.setEL(KeyConstants._closure, Boolean.FALSE)
        func.set(KeyConstants._access, toStringAccess(udfProps.getAccess()))
        var hint: String = udfProps.getHint()
        if (!StringUtil.isEmpty(hint)) func.set(KeyConstants._hint, hint)
        var displayname: String = udfProps.getDisplayName()
        if (!StringUtil.isEmpty(displayname)) func.set(KeyConstants._displayname, displayname)
        func.set(KeyConstants._name, udfProps.getFunctionName())
        func.set(KeyConstants._output, Caster.toBoolean(udfProps.getOutput()))
        func.set(KeyConstants._returntype, udfProps.getReturnTypeAsString())
        func.set(KeyConstants._modifier, if (udfProps.getModifier() === Component.MODIFIER_NONE) "" else toModifier(udfProps.getModifier(), ""))
        func.set(KeyConstants._description, udfProps.getDescription())
        if (isStatic != null) func.set(KeyConstants._static, isStatic)
        if (udfProps.getLocalMode() != null) func.set("localMode", AppListenerUtil.toLocalMode(udfProps.getLocalMode().intValue(), ""))
        if (udfProps.getPageSource() != null) func.set(KeyConstants._owner, udfProps.getPageSource().getDisplayPath())
        if (udfProps.getStartLine() > 0 && udfProps.getEndLine() > 0) {
            val pos: Struct = StructImpl()
            pos.set(KeyConstants._start, udfProps.getStartLine())
            pos.set(KeyConstants._end, udfProps.getEndLine())
            func.setEL(KeyConstants._position, pos)
        }
        val format: Int = udfProps.getReturnFormat()
        if (format < 0 || format == UDF.RETURN_FORMAT_WDDX) func.set(KeyConstants._returnFormat, "wddx") else if (format == UDF.RETURN_FORMAT_PLAIN) func.set(KeyConstants._returnFormat, "plain") else if (format == UDF.RETURN_FORMAT_JSON) func.set(KeyConstants._returnFormat, "json") else if (format == UDF.RETURN_FORMAT_SERIALIZE) func.set(KeyConstants._returnFormat, "cfml")
        val args: Array<FunctionArgument?> = udfProps.getFunctionArguments()
        val params: Array = ArrayImpl()
        // Object defaultValue;
        var m: Struct
        // Object defaultValue;
        for (y in args.indices) {
            val param = StructImpl()
            param.set(KeyConstants._name, args[y].getName().getString())
            param.set(KeyConstants._required, Caster.toBoolean(args[y].isRequired()))
            param.set(KeyConstants._type, args[y].getTypeAsString())
            displayname = args[y].getDisplayName()
            if (!StringUtil.isEmpty(displayname)) param.set(KeyConstants._displayname, displayname)
            val defType: Int = args[y].getDefaultType()
            if (defType == FunctionArgument.DEFAULT_TYPE_RUNTIME_EXPRESSION) {
                param.set(KeyConstants._default, "[runtime expression]")
            } else if (defType == FunctionArgument.DEFAULT_TYPE_LITERAL) {
                val p: Page = udfProps.getPage(pc)
                param.set(KeyConstants._default, p.udfDefaultValue(pc, udfProps.getIndex(), y, null))
            }
            hint = args[y].getHint()
            if (!StringUtil.isEmpty(hint)) param.set(KeyConstants._hint, hint)
            // TODO func.set("userMetadata", value); neo unterstuetzt irgendwelche attr, die dann hier
            // ausgebenen werden bloedsinn

            // meta data
            m = args[y].getMetaData()
            if (m != null) StructUtil.copy(m, param, true)
            params.append(param)
        }
        func.set(KeyConstants._parameters, params)
        return func
    }

    fun toModifier(str: String?, emptyValue: Int, defaultValue: Int): Int {
        var str = str
        if (StringUtil.isEmpty(str, true)) return emptyValue
        str = str.trim()
        if ("abstract".equalsIgnoreCase(str)) return Component.MODIFIER_ABSTRACT
        if ("final".equalsIgnoreCase(str)) return Component.MODIFIER_FINAL
        return if ("none".equalsIgnoreCase(str)) Component.MODIFIER_NONE else defaultValue
    }

    fun toModifier(modifier: Int, defaultValue: String?): String? {
        if (Component.MODIFIER_ABSTRACT === modifier) return "abstract"
        if (Component.MODIFIER_FINAL === modifier) return "final"
        return if (Component.MODIFIER_NONE === modifier) "none" else defaultValue
    }

    fun add(map: Map<String?, ImportDefintion?>?, importDefintions: Array<ImportDefintion?>?) {
        if (importDefintions != null) {
            for (id in importDefintions) {
                map.put(id.toString(), id)
            }
        }
    }

    fun toUDFs(udfbs: Collection<UDFB?>?, onlyUnused: Boolean): Collection<UDF?>? {
        val list: List<UDF?> = ArrayList<UDF?>()
        val it: Iterator<UDFB?> = udfbs!!.iterator()
        var udfb: UDFB?
        while (it.hasNext()) {
            udfb = it.next()
            if (!onlyUnused || !udfb.used) list.add(udfb.udf)
        }
        return list
    }
}