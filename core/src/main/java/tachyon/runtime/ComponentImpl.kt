/**
 * Copyright (c) 2023, TachyonCFML.org
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
package tachyon.runtime

import java.io.Externalizable

/**
 * %**% MUST add handling for new attributes (style, namespace, serviceportname, porttypename,
 * wsdlfile, bindingname, and output)
 */
class ComponentImpl : StructSupport, Externalizable, Component, coldfusion.runtime.TemplateProxy {
    /*
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * !!!!!!!!!!! Any change here must be changed in the method writeExternal,readExternal as well
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * !!!!!!!!!!!
	 */
    var properties: ComponentProperties? = null
    private var _data: Map<Key?, Member?>? = null
    private var _udfs: Map<Key?, UDF?>? = null
    var top: ComponentImpl? = this
    var base: ComponentImpl? = null
    private var pageSource: PageSource? = null
    private var cp: ComponentPageImpl? = null
    private var scope: ComponentScope? = null

    // for all the same
    private var dataMemberDefaultAccess = 0

    // private final Boolean _triggerDataMember=null;
    // state control of component
    var isInit = false

    // private AbstractCollection abstrCollection;
    private var useShadow = false
    private var entity = false
    var afterConstructor = false

    // private Map<Key,UDF> constructorUDFs;
    private var loaded = false
    private var hasInjectedFunctions = false
    private var isExtended // is this component extended by another component?
            = false
    var _static: StaticScope? = null
    var insideStaticConstrThread: ThreadInsideStaticConstr? = ThreadInsideStaticConstr()
    private var absFin: AbstractFinal? = null
    private var importDefintions: Array<ImportDefintion?>?

    /**
     * Constructor of the Component, USED ONLY FOR DESERIALIZE
     */
    constructor() {}

    /**
     * constructor of the class
     *
     * @param componentPage
     * @param output
     * @param _synchronized
     * @param extend
     * @param implement
     * @param hint
     * @param dspName
     * @param callPath
     * @param realPath
     * @param style
     * @param persistent
     * @param accessors
     * @param modifier
     * @param meta
     * @throws ApplicationException
     */
    constructor(componentPage: ComponentPageImpl?, output: Boolean?, _synchronized: Boolean, extend: String?, implement: String?, hint: String?, dspName: String?, callPath: String?,
                realPath: Boolean, style: String?, persistent: Boolean, accessors: Boolean, modifier: Int, isExtended: Boolean, meta: StructImpl?) {
        val sub: String = componentPage!!.getSubname()
        val appendix = if (StringUtil.isEmpty(sub)) "" else "$$sub"
        properties = ComponentProperties(componentPage!!.getComponentName(), dspName, extend.trim(), implement, hint, output, callPath + appendix, realPath,
                componentPage!!.getSubname(), _synchronized, null, persistent, accessors, modifier, meta)
        cp = componentPage
        pageSource = componentPage.getPageSource()
        importDefintions = componentPage.getImportDefintions()
        // if(modifier!=0)
        if (!StringUtil.isEmpty(style) && !"rpc".equals(style)) throw ApplicationException("style [$style] is not supported, only the following styles are supported: [rpc]")
        this.isExtended = isExtended
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        val top = _duplicate(deepCopy, true)
        setTop(top, top)
        return top
    }

    private fun _duplicate(deepCopy: Boolean, isTop: Boolean): ComponentImpl? {
        val trg = ComponentImpl()
        val inside: Boolean = ThreadLocalDuplication.set(this, trg)
        try {
            // attributes
            trg.pageSource = pageSource
            trg.cp = cp
            // trg._triggerDataMember=_triggerDataMember;
            trg.useShadow = useShadow
            trg._static = _static
            trg.entity = entity
            trg.hasInjectedFunctions = hasInjectedFunctions
            trg.isExtended = isExtended
            trg.afterConstructor = afterConstructor
            trg.dataMemberDefaultAccess = dataMemberDefaultAccess
            trg.properties = properties!!.duplicate()
            trg.isInit = isInit
            trg.absFin = absFin

            // importDefintions
            trg.importDefintions = importDefintions
            val useShadow = scope is ComponentScopeShadow
            if (!useShadow) trg.scope = ComponentScopeThis(trg)
            if (base != null) {
                trg.base = base!!._duplicate(deepCopy, false)
                trg._data = trg.base!!._data
                trg._udfs = duplicateUTFMap(this, trg, _udfs, HashMap<Key?, UDF?>(trg.base!!._udfs))
                if (useShadow) trg.scope = ComponentScopeShadow(trg, trg.base!!.scope as ComponentScopeShadow?, false)
            } else {
                // clone data member, ignore udfs for the moment
                trg._data = duplicateDataMember(trg, _data, HashMap<Key?, Member?>(), deepCopy)
                trg._udfs = duplicateUTFMap(this, trg, _udfs, HashMap<Key?, UDF?>())
                if (useShadow) {
                    trg.scope = ComponentScopeShadow(trg, duplicateDataMember(trg, scope!!.getShadow(), MapFactory.getConcurrentMap(), deepCopy))
                }
            }

            // at the moment this makes no sense, because this map is no more used after constructor has runned
            // and for a clone the constructor is not executed,
            // but perhaps this is used in future
            /*
			 * if(constructorUDFs!=null){ trg.constructorUDFs=new HashMap<Collection.Key, UDF>(); addUDFS(trg,
			 * constructorUDFs, trg.constructorUDFs); }
			 */if (isTop) {
                setTop(trg, trg)
                addUDFS(trg, _data, trg._data)
                if (useShadow) {
                    addUDFS(trg, (scope as ComponentScopeShadow?)!!.getShadow(), (trg.scope as ComponentScopeShadow?)!!.getShadow())
                }
            }
        } finally {
            if (!inside) ThreadLocalDuplication.reset()
        }
        return trg
    }

    /**
     * initalize the Component
     *
     * @param pageContext
     * @param componentPage
     * @throws PageException
     */
    @Throws(PageException::class)
    fun init(pageContext: PageContext?, componentPage: ComponentPageImpl?, executeConstr: Boolean) {
        pageSource = componentPage.getPageSource()

        // extends
        if (!StringUtil.isEmpty(properties!!.extend)) {
            base = ComponentLoader.searchComponent(pageContext, componentPage.getPageSource(), properties!!.extend, Boolean.TRUE, null, true, executeConstr)
        } else {
            val p: CIPage = (pageContext.getConfig() as ConfigWebPro).getBaseComponentPage(pageSource.getDialect(), pageContext)
            if (!componentPage.getPageSource().equals(p.getPageSource())) {
                base = ComponentLoader.loadComponent(pageContext, p, "Component", false, false, true, executeConstr)
            }
        }
        if (base != null) {
            dataMemberDefaultAccess = base!!.dataMemberDefaultAccess
            _static = StaticScope(base!!._static, this, componentPage, dataMemberDefaultAccess)
            // this._triggerDataMember=base._triggerDataMember;
            absFin = base!!.absFin
            _data = base!!._data
            _udfs = HashMap<Key?, UDF?>(base!!._udfs)
            setTop(this, base)
        } else {
            dataMemberDefaultAccess = if (pageContext.getCurrentTemplateDialect() === CFMLEngine.DIALECT_CFML) pageContext.getConfig().getComponentDataMemberDefaultAccess() else Component.ACCESS_PRIVATE
            _static = StaticScope(null, this, componentPage, dataMemberDefaultAccess)
            // TODO get per CFC setting
            // this._triggerDataMember=pageContext.getConfig().getTriggerComponentDataMember();
            _udfs = HashMap<Key?, UDF?>()
            _data = MapFactory.getConcurrentMap()
        }
        // implements
        if (!StringUtil.isEmpty(properties!!.implement)) {
            if (absFin == null) absFin = AbstractFinal()
            absFin.add(InterfaceImpl.loadInterfaces(pageContext, getPageSource(), properties!!.implement))
            // abstrCollection.implement(pageContext,getPageSource(),properties.implement);
        }
        var indexBase: Long = 0
        if (base != null) {
            indexBase = base!!.cp!!.getStaticStruct().index()
        }

        // scope
        useShadow = if (base == null) if (pageSource.getDialect() === CFMLEngine.DIALECT_CFML) pageContext.getConfig().useComponentShadow() else false else base!!.useShadow
        if (useShadow) {
            if (base == null) scope = ComponentScopeShadow(this, MapFactory.getConcurrentMap()) else scope = ComponentScopeShadow(this, base!!.scope as ComponentScopeShadow?, false)
        } else {
            scope = ComponentScopeThis(this)
        }
        initProperties()
        val ss: StaticStruct = componentPage!!.getStaticStruct() // this method get overwritten by the compiled componentpage
        if (!ss.isInit() || indexBase > ss.index()) {
            synchronized(ss) {
                // invoke static constructor
                if (!ss.isInit() || indexBase > ss.index()) {
                    val map: Map<String?, Boolean?> = statConstr.get()
                    val id = "" + componentPage!!.getHash()
                    if (!Caster.toBooleanValue(map[id], false)) {
                        map.put(id, Boolean.TRUE)

                        // this needs to happen before the call
                        try {
                            componentPage!!.staticConstructor(pageContext, this)
                        } catch (t: Throwable) {
                            ss.setInit(false)
                            ExceptionUtil.rethrowIfNecessary(t)
                            throw Caster.toPageException(t)
                        }
                        ss.setInit(true)
                        map.remove(id)
                    }
                }
            }
        }
    }

    @Throws(PageException::class)
    fun checkInterface(pc: PageContext?, componentPage: ComponentPageImpl?) {
        /*
		 * print.e("xxxxxxxxxxxxxxxxxxxxxxxxxxxxx "+ComponentUtil.toModifier(getModifier(),
		 * "none")+" xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		 * print.e(componentPage.getPageSource().getDisplayPath());
		 * print.e(getModifier()==MODIFIER_ABSTRACT); if(_abstract!=null){
		 * //print.e(_abstract.lastUpdate()); //print.e(componentPage.lastCheck());
		 * //print.e(_abstract.lastUpdate()<=componentPage.lastCheck()); print.e(_abstract.getInterfaces());
		 * print.e("has-udfs:"+_abstract.hasAbstractUDFs()); }
		 */

        // no records == nothing to do
        if (absFin == null || !absFin.hasUDFs()) return

        // if(getModifier()==MODIFIER_ABSTRACT || _abstract==null || !_abstract.hasAbstractUDFs()) return;
        // // MUST add again cache, but also check change in all
        // udfs from abstract cfc || _abstract.lastUpdate()<=componentPage.lastCheck()

        // ABSTRACT: check if the component define all functions defined in interfaces and abstract
        // components
        if (getModifier() != MODIFIER_ABSTRACT && absFin.hasAbstractUDFs()) {
            val udfs: Map<Key?, AbstractFinal.UDFB?> = absFin.getAbstractUDFBs()
            // print.e(udfs);
            val it: Iterator<Entry<Key?, AbstractFinal.UDFB?>?> = udfs.entrySet().iterator()
            var entry: Entry<Key?, AbstractFinal.UDFB?>?
            var iUdf: UDF
            var cUdf: UDF?
            var udfb: UDFB
            while (it.hasNext()) {
                entry = it.next()
                udfb = entry.getValue()
                udfb.used = true
                iUdf = udfb.udf
                cUdf = _udfs!![entry.getKey()]
                checkFunction(pc, componentPage, iUdf, cUdf)
            }
        }

        // FINAL: does a function overwrite a final method
        if (absFin.hasFinalUDFs()) {
            val udfs: Map<Key?, UDF?> = absFin.getFinalUDFs()
            val it: Iterator<Entry<Key?, UDF?>?> = udfs.entrySet().iterator()
            var entry: Entry<Key?, UDF?>?
            var iUdf: UDF
            var cUdf: UDF?
            while (it.hasNext()) {
                entry = it.next()
                iUdf = entry.getValue()
                cUdf = _udfs!![entry.getKey()]

                // if this is not the same, it was overwritten
                if (iUdf !== cUdf) throw ApplicationException("the function [" + entry.getKey().toString() + "] from component [" + cUdf.getSource()
                        .toString() + "] tries to override a final method with the same name from component [" + iUdf.getSource().toString() + "]")
            }
        }

        // MUST componentPage.ckecked();
    }

    @Throws(ApplicationException::class)
    private fun checkFunction(pc: PageContext?, componentPage: ComponentPageImpl?, iUdf: UDF?, cUdf: UDF?) {
        val iFA: Array<FunctionArgument?>
        val cFA: Array<FunctionArgument?>

        // UDF does not exist
        if (cUdf == null) {
            throw ApplicationException("component [" + componentPage!!.getComponentName().toString() + "] does not implement the function [" + iUdf.toString().toLowerCase().toString() + "] of the " + "abstract component/interface" + " [" + iUdf.getSource().toString() + "]")
        }
        iFA = iUdf.getFunctionArguments()
        cFA = cUdf.getFunctionArguments()
        // access
        if (cUdf.getAccess() > Component.ACCESS_PUBLIC) {
            throw ApplicationException(_getErrorMessage(cUdf, iUdf), "access [" + ComponentUtil.toStringAccess(cUdf.getAccess()).toString() + "] has to be at least [public]")
        }

        // return type
        if (iUdf.getReturnType() !== cUdf.getReturnType()) {
            throw ApplicationException(_getErrorMessage(cUdf, iUdf), "return type [" + cUdf.getReturnTypeAsString().toString() + "] does not match the " + "abstract component/interface" + " function return type [" + iUdf.getReturnTypeAsString().toString() + "]")
        }
        // none base types
        if (iUdf.getReturnType() === CFTypes.TYPE_UNKNOW && !iUdf.getReturnTypeAsString().equalsIgnoreCase(cUdf.getReturnTypeAsString())) {
            throw ApplicationException(_getErrorMessage(cUdf, iUdf), "return type [" + cUdf.getReturnTypeAsString().toString() + "] does not match the " + "abstract component/interface" + " function return type [" + iUdf.getReturnTypeAsString().toString() + "]")
        }

        // arguments
        if (iFA.size != cFA.size) {
            throw ApplicationException(_getErrorMessage(cUdf, iUdf), "not the same argument count")
        }
        for (i in iFA.indices) {
            // type
            if (iFA[i].getType() !== cFA[i].getType()) {
                throw ApplicationException(_getErrorMessage(cUdf, iUdf), "argument type [" + cFA[i].getTypeAsString().toString() + "] does not match the " + "abstract component/interface" + " function argument type [" + iFA[i].getTypeAsString().toString() + "]")
            }
            // none base types
            if (iFA[i].getType() === CFTypes.TYPE_UNKNOW && !iFA[i].getTypeAsString().equalsIgnoreCase(cFA[i].getTypeAsString())) {
                throw ApplicationException(_getErrorMessage(cUdf, iUdf), "argument type [" + cFA[i].getTypeAsString().toString() + "] does not match the " + "abstract component/interface" + " function argument type [" + iFA[i].getTypeAsString().toString() + "]")
            }
            // name
            if (!iFA[i].getName().equalsIgnoreCase(cFA[i].getName())) {
                throw ApplicationException(_getErrorMessage(cUdf, iUdf), "argument name [" + cFA[i].getName().toString() + "] does not match the " + "abstract component/interface" + " function argument name [" + iFA[i].getName().toString() + "]")
            }
            // required
            if (iFA[i].isRequired() !== cFA[i].isRequired()) {
                throw ApplicationException(_getErrorMessage(cUdf, iUdf), "argument [" + cFA[i].getName().toString() + "] should " + (if (iFA[i].isRequired()) "" else "not ").toString() + "be required")
            }
        }
    }

    private fun _getErrorMessage(cUdf: UDF?, iUdf: UDF?): String? {
        return ("function [" + cUdf.toString().toLowerCase().toString() + "] of component " + "[" + pageSource.getDisplayPath().toString() + "]" + " does not match the function declaration ["
                + iUdf.toString().toLowerCase().toString() + "] of the " + "abstract component/interface" + " " + "[" + iUdf.getSource().toString() + "]")
    }

    @Throws(PageException::class)
    fun _call(pc: PageContext?, key: Collection.Key?, namedArgs: Struct?, args: Array<Object?>?, superAccess: Boolean): Object? {
        val member: Member = getMember(pc, key, false, superAccess)
        return if (member is UDF) {
            _call(pc, key, member as UDF, namedArgs, args)
        } else onMissingMethod(pc, -1, member, key.getString(), args, namedArgs, superAccess)
    }

    @Throws(PageException::class)
    fun _call(pc: PageContext?, access: Int, key: Collection.Key?, namedArgs: Struct?, args: Array<Object?>?, superAccess: Boolean): Object? {
        val member: Member = getMember(access, key, false, superAccess)
        return if (member is UDF) {
            _call(pc, key, member as UDF, namedArgs, args)
        } else onMissingMethod(pc, access, member, key.getString(), args, namedArgs, superAccess)
    }

    @Throws(PageException::class)
    fun onMissingMethod(pc: PageContext?, access: Int, member: Member?, name: String?, _args: Array<Object?>?, _namedArgs: Struct?, superAccess: Boolean): Object? {
        val ommm: Member = if (access == -1) getMember(pc, KeyConstants._onmissingmethod, false, superAccess) else getMember(access, KeyConstants._onmissingmethod, false, superAccess)
        if (ommm is UDF) {
            val args: Argument = ArgumentImpl()
            if (_args != null) {
                for (i in _args.indices) {
                    args.setEL(ArgumentIntKey.init(i + 1), _args[i])
                }
            } else if (_namedArgs != null) {
                UDFUtil.argumentCollection(_namedArgs, arrayOf<FunctionArgument?>())
                val it: Iterator<Entry<Key?, Object?>?> = _namedArgs.entryIterator()
                var e: Entry<Key?, Object?>?
                while (it.hasNext()) {
                    e = it.next()
                    args.setEL(e.getKey(), e.getValue())
                }
            }

            // Struct newArgs=new StructImpl(StructImpl.TYPE_SYNC);
            // newArgs.setEL(MISSING_METHOD_NAME, name);
            // newArgs.setEL(MISSING_METHOD_ARGS, args);
            val newArgs: Array<Object?> = arrayOf(name, args)
            return _call(pc, KeyConstants._onmissingmethod, ommm as UDF, null, newArgs)
        }
        if (member == null) throw ComponentUtil.notFunction(this, KeyImpl.init(name), null, access)
        throw ComponentUtil.notFunction(this, KeyImpl.init(name), member.getValue(), access)
    }

    @Throws(PageException::class)
    fun _call(pc: PageContext?, calledName: Collection.Key?, udf: UDF?, namedArgs: Struct?, args: Array<Object?>?): Object? {
        var rtn: Object? = null
        var parent: Variables? = null

        // INFO duplicate code is for faster execution -> less contions

        // debug yes
        if (pc.getConfig().debug() && (pc.getConfig() as ConfigPro).hasDebugOptions(ConfigPro.DEBUG_TEMPLATE)) {
            val debugEntry: DebugEntryTemplate = pc.getDebugger().getEntry(pc, pageSource, udf.getFunctionName()) // new DebugEntry(src,udf.getFunctionName());
            val currTime: Long = pc.getExecutionTime()
            val time: Long = System.nanoTime()

            // sync yes
            if (top!!.properties!!._synchronized) {
                synchronized(this) {
                    try {
                        parent = beforeCall(pc)
                        rtn = if (args != null) udf.call(pc, calledName, args, true) else udf.callWithNamedValues(pc, calledName, namedArgs, true)
                    } finally {
                        if (parent != null) pc.setVariablesScope(parent)
                        val diff: Long = System.nanoTime() - time - (pc.getExecutionTime() - currTime)
                        pc.setExecutionTime(pc.getExecutionTime() + diff)
                        debugEntry.updateExeTime(diff)
                    }
                }
            } else {
                try {
                    parent = beforeCall(pc)
                    rtn = if (args != null) udf.call(pc, calledName, args, true) else udf.callWithNamedValues(pc, calledName, namedArgs, true)
                } finally {
                    if (parent != null) pc.setVariablesScope(parent)
                    val diff: Long = System.nanoTime() - time - (pc.getExecutionTime() - currTime)
                    pc.setExecutionTime(pc.getExecutionTime() + diff)
                    debugEntry.updateExeTime(diff)
                }
            }
        } else {

            // sync yes
            if (top!!.properties!!._synchronized) {
                synchronized(this) {
                    try {
                        parent = beforeCall(pc)
                        rtn = if (args != null) udf.call(pc, calledName, args, true) else udf.callWithNamedValues(pc, calledName, namedArgs, true)
                    } finally {
                        if (parent != null) pc.setVariablesScope(parent)
                    }
                }
            } else {
                try {
                    parent = beforeCall(pc)
                    rtn = if (args != null) udf.call(pc, calledName, args, true) else udf.callWithNamedValues(pc, calledName, namedArgs, true)
                } finally {
                    if (parent != null) pc.setVariablesScope(parent)
                }
            }
        }
        return rtn
    }

    @Override
    fun beforeStaticConstructor(pc: PageContext?): Variables? {
        return StaticScope.beforeStaticConstructor(pc, this, _static)
    }

    @Override
    fun afterStaticConstructor(pc: PageContext?, parent: Variables?) {
        StaticScope.afterStaticConstructor(pc, this, parent)
    }

    /**
     * will be called before executing method or constructor
     *
     * @param pc
     * @return the old scope map
     */
    fun beforeCall(pc: PageContext?): Variables? {
        val parent: Variables = pc.variablesScope()
        if (parent !== scope) {
            pc.setVariablesScope(scope)
            return parent
        }
        return null
    }

    /**
     * will be called after invoking constructor, only invoked by constructor (component body execution)
     *
     * @param pc
     * @param parent
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun afterConstructor(pc: PageContext?, parent: Variables?) {
        if (parent != null) pc.setVariablesScope(parent)
        afterConstructor = true
    }

    /**
     * this function may be called by generated code inside a ra file
     *
     * @param pc
     * @param parent
     * @throws ApplicationException
     */
    @Deprecated
    @Deprecated("""replaced with <code>afterConstructor(PageContext pc, Variables parent)</code>
	  """)
    @Throws(ApplicationException::class)
    fun afterCall(pc: PageContext?, parent: Variables?) {
        afterConstructor(pc, parent)
    }
    /**
     * sets the callpath
     *
     * @param callPath / public void setCallPath(String callPath) { properties.callPath=callPath; }
     */
    /**
     * rerturn the size
     *
     * @param access
     * @return size
     */
    @Override
    fun size(access: Int): Int {
        return keys(access)!!.size
    }

    /**
     * list of keys
     *
     * @param c
     * @param access
     * @param doBase
     * @return key set
     */
    @Override
    fun keySet(access: Int): Set<Key?>? {
        val set: Set<Key?> = LinkedHashSet<Key?>()
        var entry: Map.Entry<Key?, Member?>
        val it: Iterator<Entry<Key?, Member?>?> = _data.entrySet().iterator()
        while (it.hasNext()) {
            entry = it.next()
            if (entry.getValue().getAccess() <= access) set.add(entry.getKey())
        }
        return set
    }

    /*
	 * protected Set<Key> udfKeySet(int access) { Set<Key> set=new HashSet<Key>(); Member m;
	 * Map.Entry<Key, UDF> entry; Iterator<Entry<Key, UDF>> it = _udfs.entrySet().iterator();
	 * while(it.hasNext()) { entry= it.next(); m=entry.getValue();
	 * if(m.getAccess()<=access)set.add(entry.getKey()); } return set; }
	 */
    protected fun getMembers(access: Int): List<Member?>? {
        val members: MutableList<Member?> = ArrayList<Member?>()
        var e: Member
        val it: Iterator<Entry<Key?, Member?>?> = _data.entrySet().iterator()
        while (it.hasNext()) {
            e = it.next().getValue()
            if (e.getAccess() <= access) members.add(e)
        }
        return members
    }

    @Override
    fun keyIterator(access: Int): Iterator<Collection.Key?>? {
        return keySet(access)!!.iterator()
    }

    @Override
    fun keysAsStringIterator(access: Int): Iterator<String?>? {
        return StringIterator(keys(access))
    }

    @Override
    fun entryIterator(access: Int): Iterator<Entry<Key?, Object?>?>? {
        return ComponentEntryIterator(this, keys(access), access)
    }

    @Override
    fun valueIterator(access: Int): Iterator<Object?>? {
        return ComponentValueIterator(this, keys(access), access)
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return valueIterator(getAccess(ThreadLocalPageContext.get()))
    }

    @Override
    fun getIterator(): Iterator<*>? {
        val pc: PageContext = ThreadLocalPageContext.get()
        // do we have functions _hasNext,_next,_reset
        return if (getMember(pc, KeyConstants.__hasNext, false, false) != null && getMember(pc, KeyConstants.__next, false, false) != null) ComponentIterator(this) else keysAsStringIterator()
    }

    @Override
    fun keys(access: Int): Array<Collection.Key?>? {
        val set: Set<Key?>? = keySet(access)
        return set.toArray(arrayOfNulls<Collection.Key?>(set!!.size()))
    }

    @Override
    fun clear() {
        _data.clear()
        _udfs.clear()
    }

    @Override
    fun getMember(access: Int, key: Collection.Key?, dataMember: Boolean, superAccess: Boolean): Member? {
        // check super
        if (dataMember && access == ACCESS_PRIVATE && key.equalsIgnoreCase(KeyConstants._super)) {
            val ac: Component = ComponentUtil.getActiveComponent(ThreadLocalPageContext.get(), this)
            return SuperComponent.superMember(ac.getBaseComponent() as ComponentImpl)
            // return SuperComponent . superMember(base);
        }
        if (superAccess) {
            return _udfs!![key]
        }
        // check data
        var member: Member? = _data!![key]
        if (member != null) {
            return if (member.getAccess() <= access) member else null
        }

        // static
        member = staticScope()!!.getMember(null, key, null)
        return if (member != null) {
            if (member.getAccess() <= access) member else null
        } else null
    }

    /**
     * get entry matching key
     *
     * @param access
     * @param keyLowerCase key lower case (case sensitive)
     * @param doBase do check also base component
     * @param dataMember do also check if key super
     * @return matching entry if exists otherwise null
     */
    protected fun getMember(pc: PageContext?, key: Collection.Key?, dataMember: Boolean, superAccess: Boolean): Member? {
        // check super
        if (dataMember && key.equalsIgnoreCase(KeyConstants._super) && isPrivate(pc)) {
            val ac: Component = ComponentUtil.getActiveComponent(pc, this)
            return SuperComponent.superMember(ac.getBaseComponent() as ComponentImpl)
        }
        if (superAccess) {
            return _udfs!![key]
        }
        // check data
        var member: Member? = _data!![key]
        if (member != null && isAccessible(pc, member)) return member

        // static
        member = staticScope()!!.getMember(pc, key, null)
        return if (member != null) member else null
    }

    fun isAccessible(pc: PageContext?, member: Member?): Boolean {
        val access: Int = member.getAccess()
        if (access <= ACCESS_PUBLIC) return true else if (access == ACCESS_PRIVATE && isPrivate(pc)) return true else if (access == ACCESS_PACKAGE && isPackage(pc)) return true
        return false
    }

    fun isAccessible(pc: PageContext?, access: Int): Boolean {
        if (access <= ACCESS_PUBLIC) return true else if (access == ACCESS_PRIVATE && isPrivate(pc)) return true else if (access == ACCESS_PACKAGE && isPackage(pc)) return true
        return false
    }

    /**
     * @param pc
     * @return returns if is private
     */
    private fun isPrivate(pc: PageContext?): Boolean {
        var pc: PageContext? = pc
        pc = ThreadLocalPageContext.get(pc)
        if (pc == null) return true
        val ac: Component = pc.getActiveComponent()
        return ac != null && (ac === this || (ac as ComponentImpl).top!!.pageSource.equals(top!!.pageSource))
    }

    /**
     * @param pc
     * @return returns if is package
     */
    private fun isPackage(pc: PageContext?): Boolean {
        var pc: PageContext? = pc
        pc = ThreadLocalPageContext.get(pc)
        if (pc == null) return true
        val ac: Component = pc.getActiveComponent()
        if (ac != null) {
            if (ac === this) return true
            val aci = ac as ComponentImpl
            if (aci.top!!.pageSource.equals(top!!.pageSource)) return true
            var index: Int
            var other = aci.top!!.getAbsName()
            index = other.lastIndexOf('.')
            if (index == -1) other = "" else other = other.substring(0, index)
            var my = top!!.getAbsName()
            index = my.lastIndexOf('.')
            if (index == -1) my = "" else my = my.substring(0, index)
            return my.equalsIgnoreCase(other)
        }
        return false
    }

    /**
     * return the access of a member
     *
     * @param key
     * @return returns the access (Component.ACCESS_REMOTE, ACCESS_PUBLIC,
     * ACCESS_PACKAGE,Component.ACCESS_PRIVATE)
     */
    private fun getAccess(key: Collection.Key?): Int {
        val member: Member = getMember(ACCESS_PRIVATE, key, false, false) ?: return Component.ACCESS_PRIVATE
        return member.getAccess()
    }

    /**
     * returns current access to this component
     *
     * @param pc
     * @return access
     */
    fun getAccess(pc: PageContext?): Int {
        if (pc == null) return ACCESS_PUBLIC
        if (isPrivate(pc)) return ACCESS_PRIVATE
        return if (isPackage(pc)) ACCESS_PACKAGE else ACCESS_PUBLIC
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return toDumpData(pageContext, maxlevel, dp, getAccess(pageContext))
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?, access: Int): DumpData? {
        val isCFML = getPageSource().getDialect() === CFMLEngine.DIALECT_CFML
        val table: DumpTable = if (isCFML) DumpTable("component", "#48d8d8", "#68dfdf", "#000000") else DumpTable("component", "#48d8d8", "#68dfdf", "#000000")
        table.setTitle((if (isCFML) "Component" else "Class") + " " + getCallPath() + if (top!!.properties!!.inline) "" else " " + StringUtil.escapeHTML(top!!.properties!!.dspName))
        table.setComment("Only the functions and data members that are accessible from your location are displayed")

        // Extends
        if (!StringUtil.isEmpty(top!!.properties!!.extend, true)) table.appendRow(1, SimpleDumpData("Extends"), SimpleDumpData(top!!.properties!!.extend))

        // Interfaces
        if (!StringUtil.isEmpty(top!!.properties!!.implement, true)) table.appendRow(1, SimpleDumpData("Implements"), SimpleDumpData(top!!.properties!!.implement))
        if (top!!.properties!!.modifier !== Member.MODIFIER_NONE) table.appendRow(1, SimpleDumpData("Modifier"), SimpleDumpData(ComponentUtil.toModifier(top!!.properties!!.modifier, "")))
        if (top!!.properties!!.hint.trim().length() > 0) table.appendRow(1, SimpleDumpData("Hint"), SimpleDumpData(top!!.properties!!.hint))

        // this
        val thisScope: DumpTable? = thisScope(top, pageContext, maxlevel, dp, access)
        if (!thisScope.isEmpty()) table.appendRow(1, SimpleDumpData("this"), thisScope)

        // static
        val staticScope: DumpTable = _static!!._toDumpData(top, pageContext, maxlevel, dp, getAccess(pageContext))
        if (!staticScope.isEmpty()) table.appendRow(1, SimpleDumpData("static"), staticScope)
        return table
    }

    /**
     * @return return call path
     */
    protected fun getCallPath(): String? {
        if (top!!.properties!!.inline) return "(inline)"
        return if (StringUtil.isEmpty(top!!.properties!!.callPath)) getName() else try {
            "(" + ListUtil.arrayToList(ListUtil.listToArrayTrim(top!!.properties!!.callPath.replace('/', '.').replace('\\', '.'), "."), ".").toString() + ")"
        } catch (e: PageException) {
            top!!.properties!!.callPath
        }
    }

    @Override
    fun getDisplayName(): String? {
        return top!!.properties!!.dspName
    }

    @Override
    fun getExtends(): String? {
        return top!!.properties!!.extend
    }

    @Override
    fun getModifier(): Int {
        return properties!!.modifier
    }

    @Override
    fun getBaseAbsName(): String? {
        return top!!.base!!.pageSource.getComponentName()
    }

    @Override
    fun isBasePeristent(): Boolean {
        return top!!.base != null && top!!.base!!.properties!!.persistent
    }

    @Override
    fun getHint(): String? {
        return top!!.properties!!.hint
    }

    @Override
    fun getWSDLFile(): String? {
        return top!!.properties!!.getWsdlFile()
    }

    @Override
    fun getName(): String? {
        return if (top!!.properties!!.callPath == null) "" else ListUtil.last(top!!.properties!!.callPath, "./", true)
    }

    fun _getName(): String? { // MUST nicht so toll
        return if (properties!!.callPath == null) "" else ListUtil.last(properties!!.callPath, "./", true)
    }

    fun _getPageSource(): PageSource? {
        return pageSource
    }

    fun _getImportDefintions(): Array<ImportDefintion?>? {
        return importDefintions
    }

    fun getImportDefintions(): Array<ImportDefintion?>? {
        val map: Map<String?, ImportDefintion?> = HashMap<String?, ImportDefintion?>()
        var c: ComponentImpl? = this
        do {
            ComponentUtil.add(map, c!!._getImportDefintions())
            c = c.base
        } while (c != null)
        return map.values().toArray(arrayOfNulls<ImportDefintion?>(map.size()))
    }

    @Override
    fun getCallName(): String? {
        return top!!.properties!!.callPath
    }

    @Override
    fun getAbsName(): String? {
        return top!!.pageSource.getComponentName()
    }

    @Override
    fun getOutput(): Boolean {
        return if (top!!.properties!!.output == null) true else top!!.properties!!.output.booleanValue()
    }

    @Override
    fun instanceOf(type: String?): Boolean {
        var c = top
        do {
            if (type.equalsIgnoreCase(c!!.properties!!.callPath)) return true
            if (type.equalsIgnoreCase(c!!.properties!!.name)) return true
            if (type.equalsIgnoreCase(c!!._getName())) return true

            // check interfaces
            if (c!!.absFin != null) {
                val it: Iterator<InterfaceImpl?> = c.absFin.getInterfaceIt()
                while (it.hasNext()) {
                    if (it.next()!!.instanceOf(type)) return true
                }
            }
            c = c.base
        } while (c != null)
        if (StringUtil.endsWithIgnoreCase(type, "component")) {
            if (type.equalsIgnoreCase("component")) return true
            if (type.equalsIgnoreCase("web-inf.cftags.component")) return true
            // if(type.equalsIgnoreCase("web-inf.tachyon.context.component")) return true;
        }
        return false
    }

    @Override
    fun equalTo(type: String?): Boolean {
        val c = top
        if (type.equalsIgnoreCase(c!!.properties!!.callPath)) return true
        if (type.equalsIgnoreCase(c!!.pageSource.getComponentName())) return true
        if (type.equalsIgnoreCase(c!!._getName())) return true

        // check interfaces
        if (c!!.absFin != null) {
            val it: Iterator<InterfaceImpl?> = c.absFin.getInterfaceIt()
            while (it.hasNext()) {
                if (it.next()!!.instanceOf(type)) return true
            }
        }
        if (StringUtil.endsWithIgnoreCase(type, "component")) {
            if (type.equalsIgnoreCase("component")) return true
            if (type.equalsIgnoreCase("web-inf.cftags.component")) return true
        }
        return false
    }

    @Override
    fun isValidAccess(access: Int): Boolean {
        return access == ACCESS_PRIVATE || access == ACCESS_PACKAGE || access == ACCESS_PUBLIC || access == ACCESS_REMOTE
    }

    @Override
    fun getPageSource(): PageSource? {
        return top!!.pageSource
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        return castToString(false)
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return castToString(false, defaultValue)
    }

    @Throws(PageException::class)
    fun castToString(superAccess: Boolean): String? {
        // magic function
        val pc: PageContext = ThreadLocalPageContext.get()
        if (pc != null) {
            val member: Member = getMember(pc, KeyConstants.__toString, true, superAccess)
            // Object o = get(pc,"_toString",null);
            if (member is UDF) {
                val udf: UDF = member as UDF
                if (udf.getReturnType() === CFTypes.TYPE_STRING && udf.getFunctionArguments().length === 0) {
                    return Caster.toString(_call(pc, KeyConstants.__toString, udf, null, arrayOfNulls<Object?>(0)))
                }
            }
        }
        throw ExceptionUtil.addHint(ExpressionException("Can't cast Component [" + getName() + "] to String"),
                "Add a User-Defined-Function to Component with the following pattern [_toString():String] to cast it to a String or use Built-In-Function \"serialize(Component):String\" to convert it to a serialized String")
    }

    fun castToString(superAccess: Boolean, defaultValue: String?): String? {
        // magic function
        val pc: PageContext = ThreadLocalPageContext.get()
        if (pc != null) {
            val member: Member = getMember(pc, KeyConstants.__toString, true, superAccess)
            // Object o = get(pc,"_toString",null);
            if (member is UDF) {
                val udf: UDF = member as UDF
                if (udf.getReturnType() === CFTypes.TYPE_STRING && udf.getFunctionArguments().length === 0) {
                    return try {
                        Caster.toString(_call(pc, KeyConstants.__toString, udf, null, arrayOfNulls<Object?>(0)), defaultValue)
                    } catch (e: PageException) {
                        defaultValue
                    }
                }
            }
        }
        return defaultValue
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return castToBooleanValue(false)
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return castToBoolean(false, defaultValue)
    }

    @Throws(PageException::class)
    fun castToBooleanValue(superAccess: Boolean): Boolean {
        // magic function
        val pc: PageContext = ThreadLocalPageContext.get()
        if (pc != null) {
            val member: Member = getMember(pc, KeyConstants.__toBoolean, true, superAccess)
            // Object o = get(pc,"_toBoolean",null);
            if (member is UDF) {
                val udf: UDF = member as UDF
                if (udf.getReturnType() === CFTypes.TYPE_BOOLEAN && udf.getFunctionArguments().length === 0) {
                    return Caster.toBooleanValue(_call(pc, KeyConstants.__toBoolean, udf, null, arrayOfNulls<Object?>(0)))
                }
            }
        }
        throw ExceptionUtil.addHint(ExpressionException("Can't cast Component [" + getName() + "] to a boolean value"),
                "Add a User-Defined-Function to Component with the following pattern [_toBoolean():boolean] to cast it to a boolean value")
    }

    fun castToBoolean(superAccess: Boolean, defaultValue: Boolean?): Boolean? {
        // magic function
        val pc: PageContext = ThreadLocalPageContext.get()
        if (pc != null) {
            val member: Member = getMember(pc, KeyConstants.__toBoolean, true, superAccess)
            // Object o = get(pc,"_toBoolean",null);
            if (member is UDF) {
                val udf: UDF = member as UDF
                if (udf.getReturnType() === CFTypes.TYPE_BOOLEAN && udf.getFunctionArguments().length === 0) {
                    return try {
                        Caster.toBoolean(_call(pc, KeyConstants.__toBoolean, udf, null, arrayOfNulls<Object?>(0)), defaultValue)
                    } catch (e: PageException) {
                        defaultValue
                    }
                }
            }
        }
        return defaultValue
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return castToDoubleValue(false)
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return castToDoubleValue(false, defaultValue)
    }

    @Throws(PageException::class)
    fun castToDoubleValue(superAccess: Boolean): Double {
        // magic function
        val pc: PageContext = ThreadLocalPageContext.get()
        if (pc != null) {
            val member: Member = getMember(pc, KeyConstants.__toNumeric, true, superAccess)
            // Object o = get(pc,"_toNumeric",null);
            if (member is UDF) {
                val udf: UDF = member as UDF
                if (udf.getReturnType() === CFTypes.TYPE_NUMERIC && udf.getFunctionArguments().length === 0) {
                    return Caster.toDoubleValue(_call(pc, KeyConstants.__toNumeric, udf, null, arrayOfNulls<Object?>(0)))
                }
            }
        }
        throw ExceptionUtil.addHint(ExpressionException("Can't cast Component [" + getName() + "] to a numeric value"),
                "Add a User-Defined-Function to Component with the following pattern [_toNumeric():numeric] to cast it to a numeric value")
    }

    fun castToDoubleValue(superAccess: Boolean, defaultValue: Double): Double {
        // magic function
        val pc: PageContext = ThreadLocalPageContext.get()
        if (pc != null) {
            val member: Member = getMember(pc, KeyConstants.__toNumeric, true, superAccess)
            // Object o = get(pc,"_toNumeric",null);
            if (member is UDF) {
                val udf: UDF = member as UDF
                if (udf.getReturnType() === CFTypes.TYPE_NUMERIC && udf.getFunctionArguments().length === 0) {
                    return try {
                        Caster.toDoubleValue(_call(pc, KeyConstants.__toNumeric, udf, null, arrayOfNulls<Object?>(0)), true, defaultValue)
                    } catch (e: PageException) {
                        defaultValue
                    }
                }
            }
        }
        return defaultValue
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        return castToDateTime(false)
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return castToDateTime(false, defaultValue)
    }

    @Throws(PageException::class)
    fun castToDateTime(superAccess: Boolean): DateTime? {
        // magic function
        val pc: PageContext = ThreadLocalPageContext.get()
        if (pc != null) {
            val member: Member = getMember(pc, KeyConstants.__toDateTime, true, superAccess)
            // Object o = get(pc,"_toDateTime",null);
            if (member is UDF) {
                val udf: UDF = member as UDF
                if (udf.getReturnType() === CFTypes.TYPE_DATETIME && udf.getFunctionArguments().length === 0) {
                    return Caster.toDate(_call(pc, KeyConstants.__toDateTime, udf, null, arrayOfNulls<Object?>(0)), pc.getTimeZone())
                }
            }
        }
        throw ExceptionUtil.addHint(ExpressionException("Can't cast Component [" + getName() + "] to a date"),
                "Add a User-Defined-Function to Component with the following pattern [_toDateTime():datetime] to cast it to a date")
    }

    fun castToDateTime(superAccess: Boolean, defaultValue: DateTime?): DateTime? {
        // magic function
        val pc: PageContext = ThreadLocalPageContext.get()
        if (pc != null) {
            val member: Member = getMember(pc, KeyConstants.__toDateTime, true, superAccess)
            // Object o = get(pc,"_toDateTime",null);
            if (member is UDF) {
                val udf: UDF = member as UDF
                if (udf.getReturnType() === CFTypes.TYPE_DATETIME && udf.getFunctionArguments().length === 0) {
                    return try {
                        DateCaster.toDateAdvanced(_call(pc, KeyConstants.__toDateTime, udf, null, arrayOfNulls<Object?>(0)), DateCaster.CONVERTING_TYPE_OFFSET, pc.getTimeZone(),
                                defaultValue)
                    } catch (e: PageException) {
                        defaultValue
                    }
                }
            }
        }
        return defaultValue
    }

    @Override
    @Throws(PageException::class)
    fun getMetaData(pc: PageContext?): Struct? {
        return getMetaData(ACCESS_PRIVATE, pc, top, false)
    }

    @Override
    fun getMetaStructItem(name: Collection.Key?): Object? {
        return if (top!!.properties!!.meta != null) {
            top!!.properties!!.meta.get(name, null)
        } else null
    }

    private class ComparatorImpl : Comparator {
        @Override
        fun compare(o1: Object?, o2: Object?): Int {
            return ((o1 as Struct?).get(KeyConstants._name, "") as String).compareTo(((o2 as Struct?).get(KeyConstants._name, "") as String))
        }
    }

    fun isInitalized(): Boolean {
        return isInit
    }

    fun setInitalized(isInit: Boolean) {
        this.isInit = isInit
    }

    /**
     * sets a value to the current Component, dont to base Component
     *
     * @param key
     * @param value
     * @return value set
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    private fun _set(pc: PageContext?, key: Collection.Key?, value: Object?): Object? {
        if (value is Member) {
            val m: Member? = value as Member?
            if (m is UDF) {
                val udf: UDF? = m as UDF?
                if (udf.getAccess() > Component.ACCESS_PUBLIC && udf is UDFPlus) (udf as UDFPlus?).setAccess(Component.ACCESS_PUBLIC)
                _data.put(key, udf)
                _udfs.put(key, udf)
                hasInjectedFunctions = true
            } else _data.put(key, m)
        } else {
            val existing: Member? = _data!![key]
            if (loaded && !isAccessible(pc, if (existing != null) existing.getAccess() else dataMemberDefaultAccess)) throw ExpressionException("Component [" + getCallName() + "] has no accessible Member with name [" + key + "]",
                    "enable [trigger data member] in administrator to also invoke getters and setters")
            _data.put(key,
                    DataMember(if (existing != null) existing.getAccess() else dataMemberDefaultAccess, if (existing != null) existing.getModifier() else Member.MODIFIER_NONE, value))
        }
        return value
    }

    @Override
    @Throws(ApplicationException::class)
    fun registerUDF(key: Collection.Key?, udf: UDF?) {
        registerUDF(key, udf, useShadow, false)
    }

    @Override
    @Throws(ApplicationException::class)
    fun registerUDF(key: Collection.Key?, prop: UDFProperties?) {
        registerUDF(key, UDFImpl(prop), useShadow, false)
    }

    @Throws(ClassException::class, ClassNotFoundException::class, IOException::class, ApplicationException::class)
    fun regJavaFunction(key: Collection.Key?, className: String?) {
        val jf: JF = ClassUtil.loadInstance(getPageSource().getMapping().getPhysicalClass(className))
        jf!!.setPageSource(getPageSource())
        registerUDF(key, jf)
    }

    @Throws(ApplicationException::class)
    fun registerStaticUDF(key: Key?, prop: UDFProperties?) {
        _static.put(key, UDFImpl(prop, this))
    }

    /*
	 * @deprecated injected is not used
	 */
    @Throws(ApplicationException::class)
    fun registerUDF(key: Key?, udf: UDF?, useShadow: Boolean, injected: Boolean) {
        if (udf is UDFPlus) (udf as UDFPlus?).setOwnerComponent(this)
        if (insideStaticConstrThread.get()) {
            _static.put(key, udf)
            return
        }

        // Abstact UDF
        if (udf.getModifier() === MODIFIER_ABSTRACT) {
            // abstract methods are not allowed
            if (getModifier() != MODIFIER_ABSTRACT) {
                throw ApplicationException("the abstract function [" + key + "] is not allowed within the no abstract component [" + _getPageSource().getDisplayPath() + "]")
            }
            if (absFin == null) absFin = AbstractFinal()
            absFin.add(key, udf)
            return  // abstract methods are not registered here
        } else if (udf.getModifier() === MODIFIER_FINAL) {
            if (absFin == null) absFin = AbstractFinal()
            absFin.add(key, udf)
        }
        _udfs.put(key, udf)
        _data.put(key, udf)
        if (useShadow) scope.setEL(key, udf)
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Key?): Object? {
        return _data.remove(key)
    }

    @Override
    fun removeEL(key: Collection.Key?): Object? {
        // MUST access muss beruecksichtigt werden
        return _data.remove(key)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, key: Collection.Key?, value: Object?): Object? {
        var pc: PageContext? = pc
        if (pc == null) pc = ThreadLocalPageContext.get()
        if (triggerDataMember(pc) && isInit) {
            if (!isPrivate(pc)) {
                return callSetter(pc, key, value)
            }
        }
        return _set(pc, key, value)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Collection.Key?, value: Object?): Object? {
        return set(null, key, value)
    }

    @Override
    fun setEL(pc: PageContext?, name: Collection.Key?, value: Object?): Object? {
        return try {
            set(pc, name, value)
        } catch (e: PageException) {
            null
        }
    }

    @Override
    fun setEL(key: Key?, value: Object?): Object? {
        return setEL(null, key, value)
    }

    @Override
    fun put(key: Object?, value: Object?): Object? {
        // TODO find a better solution
        // when an orm entity the data given by put or also written to the variables scope
        if (entity) {
            getComponentScope().put(key, value)
        }
        return super.put(key, value)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Collection.Key?): Object? {
        val member: Member = getMember(pc, key, true, false)
        if (member != null) return member.getValue()

        // trigger
        if (triggerDataMember(pc) && !isPrivate(pc)) {
            return callGetter(pc, key)
        }
        throw ExpressionException("Component [" + getCallName() + "] has no accessible Member with name [" + key + "]",
                "enable [trigger data member] in administrator to also invoke getters and setters")
        // throw new ExpressionException("Component ["+getCallName()+"] has no accessible Member with name
        // ["+name+"]");
    }

    @Throws(PageException::class)
    private fun callGetter(pc: PageContext?, key: Collection.Key?): Object? {
        val getterName: Key = KeyImpl.getInstance("get" + key.getLowerString())
        val member: Member = getMember(pc, getterName, false, false)
        if (member is UDF) {
            val udf: UDF = member as UDF
            if (udf.getFunctionArguments().length === 0 && udf.getReturnType() !== CFTypes.TYPE_VOID) {
                return _call(pc, getterName, udf, null, ArrayUtil.OBJECT_EMPTY)
            }
        }
        throw ExpressionException("Component [" + getCallName() + "] has no accessible Member with name [" + key + "]")
    }

    private fun callGetter(pc: PageContext?, key: Collection.Key?, defaultValue: Object?): Object? {
        val getterName: Key = KeyImpl.getInstance("get" + key.getLowerString())
        val member: Member = getMember(pc, getterName, false, false)
        if (member is UDF) {
            val udf: UDF = member as UDF
            if (udf.getFunctionArguments().length === 0 && udf.getReturnType() !== CFTypes.TYPE_VOID) {
                return try {
                    _call(pc, getterName, udf, null, ArrayUtil.OBJECT_EMPTY)
                } catch (e: PageException) {
                    defaultValue
                }
            }
        }
        return defaultValue
    }

    @Throws(PageException::class)
    private fun callSetter(pc: PageContext?, key: Collection.Key?, value: Object?): Object? {
        val setterName: Collection.Key = KeyImpl.getInstance("set" + key.getLowerString())
        val member: Member = getMember(pc, setterName, false, false)
        if (member is UDF) {
            val udf: UDF = member as UDF
            if (udf.getFunctionArguments().length === 1 && udf.getReturnType() === CFTypes.TYPE_VOID || udf.getReturnType() === CFTypes.TYPE_ANY) { // TDOO support
                return _call(pc, setterName, udf, null, arrayOf<Object?>(value))
            }
        }
        return _set(pc, key, value)
    }

    /**
     * return element that has at least given access or null
     *
     * @param access
     * @param name
     * @return matching value
     * @throws PageException
     */
    @Throws(PageException::class)
    operator fun get(access: Int, name: String?): Object? {
        return get(access, KeyImpl.init(name))
    }

    @Override
    @Throws(PageException::class)
    operator fun get(access: Int, key: Collection.Key?): Object? {
        val member: Member = getMember(access, key, true, false)
        if (member != null) return member.getValue()

        // Trigger
        val pc: PageContext = ThreadLocalPageContext.get()
        if (triggerDataMember(pc) && !isPrivate(pc)) {
            return callGetter(pc, key)
        }
        throw ExpressionException("Component [" + getCallName() + "] has no accessible Member with name [" + key + "]")
    }

    @Override
    operator fun get(pc: PageContext?, key: Collection.Key?, defaultValue: Object?): Object? {
        val member: Member = getMember(pc, key, true, false)
        if (member != null) return member.getValue()

        // trigger
        return if (triggerDataMember(pc) && !isPrivate(pc)) {
            callGetter(pc, key, defaultValue)
        } else defaultValue
    }

    /**
     * return element that has at least given access or null
     *
     * @param access
     * @param name
     * @return matching value
     */
    protected operator fun get(access: Int, name: String?, defaultValue: Object?): Object? {
        return get(access, KeyImpl.init(name), defaultValue)
    }

    /**
     * @param access
     * @param key
     * @param defaultValue
     * @return
     */
    @Override
    operator fun get(access: Int, key: Collection.Key?, defaultValue: Object?): Object? {
        val member: Member = getMember(access, key, true, false)
        if (member != null) return member.getValue()

        // trigger
        val pc: PageContext = ThreadLocalPageContext.get()
        return if (triggerDataMember(pc) && !isPrivate(pc)) {
            callGetter(pc, key, defaultValue)
        } else defaultValue
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Collection.Key?): Object? {
        return get(ThreadLocalPageContext.get(), key)
    }

    @Override
    operator fun get(key: Collection.Key?, defaultValue: Object?): Object? {
        return get(ThreadLocalPageContext.get(), key, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, name: String?, args: Array<Object?>?): Object? {
        return _call(pc, KeyImpl.init(name), null, args, false)
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, name: Collection.Key?, args: Array<Object?>?): Object? {
        return _call(pc, name, null, args, false)
    }

    @Throws(PageException::class)
    protected fun call(pc: PageContext?, access: Int, name: String?, args: Array<Object?>?): Object? {
        return _call(pc, access, KeyImpl.init(name), null, args, false)
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, access: Int, name: Collection.Key?, args: Array<Object?>?): Object? {
        return _call(pc, access, name, null, args, false)
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, name: String?, args: Struct?): Object? {
        return _call(pc, KeyImpl.init(name), args, null, false)
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, methodName: Collection.Key?, args: Struct?): Object? {
        return _call(pc, methodName, args, null, false)
    }

    @Throws(PageException::class)
    protected fun callWithNamedValues(pc: PageContext?, access: Int, name: String?, args: Struct?): Object? {
        return _call(pc, access, KeyImpl.init(name), args, null, false)
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, access: Int, name: Collection.Key?, args: Struct?): Object? {
        return _call(pc, access, name, args, null, false)
    }

    fun contains(pc: PageContext?, name: String?): Boolean {
        val _null: Object = NullSupportHelper.NULL(pc)
        return get(pc, KeyImpl.init(name), _null) !== _null
    }

    /**
     * @param pc
     * @param key
     * @return
     */
    @Override
    fun contains(pc: PageContext?, key: Key?): Boolean {
        val _null: Object = NullSupportHelper.NULL(pc)
        return get(pc, key, _null) !== _null
    }

    @Override
    fun containsKey(key: Key?): Boolean {
        return contains(ThreadLocalPageContext.get(), key)
    }

    @Override
    fun containsKey(pc: PageContext?, key: Key?): Boolean {
        return contains(ThreadLocalPageContext.get(pc), key)
    }

    fun contains(access: Int, name: String?): Boolean {
        val _null: Object = NullSupportHelper.NULL()
        return get(access, name, _null) !== _null
    }

    @Override
    fun contains(access: Int, name: Key?): Boolean {
        val _null: Object = NullSupportHelper.NULL()
        return get(access, name, _null) !== _null
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        return keyIterator(getAccess(ThreadLocalPageContext.get()))
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return keysAsStringIterator(getAccess(ThreadLocalPageContext.get()))
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return entryIterator(getAccess(ThreadLocalPageContext.get()))
    }

    @Override
    fun keys(): Array<Collection.Key?>? {
        return keys(getAccess(ThreadLocalPageContext.get()))
    }

    @Override
    fun size(): Int {
        return size(getAccess(ThreadLocalPageContext.get()))
    }

    @Override
    @Throws(PageException::class)
    fun getJavaAccessClass(isNew: RefBoolean?): Class<*>? {
        return getJavaAccessClass(ThreadLocalPageContext.get(), isNew)
    }

    @Throws(PageException::class)
    fun getJavaAccessClass(pc: PageContext?, isNew: RefBoolean?): Class<*>? {
        return getJavaAccessClass(pc, isNew, false, true, true, true, false, false)
    }

    @Override
    @Throws(PageException::class)
    fun getJavaAccessClass(pc: PageContext?, isNew: RefBoolean?, writeLog: Boolean, takeTop: Boolean, create: Boolean, suppressWSbeforeArg: Boolean): Class<*>? {
        isNew.setValue(false)
        val props: ComponentProperties = if (takeTop) top!!.properties else properties
        if (props!!.javaAccessClass == null) {
            props!!.javaAccessClass = ComponentUtil.getComponentJavaAccess(pc, this, isNew, create, writeLog, suppressWSbeforeArg, true, false)
        }
        return props!!.javaAccessClass
    }

    @Override
    @Throws(PageException::class)
    fun getJavaAccessClass(pc: PageContext?, isNew: RefBoolean?, writeLog: Boolean, takeTop: Boolean, create: Boolean, suppressWSbeforeArg: Boolean, output: Boolean,
                           returnValue: Boolean): Class<*>? {
        isNew.setValue(false)
        val props: ComponentProperties = if (takeTop) top!!.properties else properties
        if (props!!.javaAccessClass == null) {
            props!!.javaAccessClass = ComponentUtil.getComponentJavaAccess(pc, this, isNew, create, writeLog, suppressWSbeforeArg, output, returnValue)
        }
        return props!!.javaAccessClass
    }

    @Override
    fun isPersistent(): Boolean {
        return top!!.properties!!.persistent
    }

    @Override
    fun isAccessors(): Boolean {
        return top!!.properties!!.accessors
    }

    @Override
    @Throws(PageException::class)
    fun setProperty(property: Property?) {
        top!!.properties!!.properties.put(StringUtil.toLowerCase(property.getName()), property)
        if (property.getDefault() != null) scope.setEL(KeyImpl.init(property.getName()), property.getDefault())
        if (top!!.properties!!.persistent || top!!.properties!!.accessors) {
            PropertyFactory.createPropertyUDFs(this, property)
        }
    }

    @Throws(PageException::class)
    private fun initProperties() {
        top!!.properties!!.properties = LinkedHashMap<String?, Property?>()

        // MappedSuperClass
        if (isPersistent() && !isBasePeristent() && top!!.base != null && top!!.base!!.properties!!.properties != null && top!!.base!!.properties!!.meta != null) {
            val msc: Boolean = Caster.toBooleanValue(top!!.base!!.properties!!.meta.get(KeyConstants._mappedSuperClass, Boolean.FALSE), false)
            if (msc) {
                var p: Property
                val it: Iterator<Entry<String?, Property?>?> = top!!.base!!.properties!!.properties.entrySet().iterator()
                while (it.hasNext()) {
                    p = it.next().getValue()
                    if (p.isPeristent()) {
                        setProperty(p)
                    }
                }
            }
        }
    }

    @Override
    fun getProperties(onlyPeristent: Boolean): Array<Property?>? {
        return getProperties(onlyPeristent, false, false, false)
    }

    @Override
    fun getProperties(onlyPeristent: Boolean, includeBaseProperties: Boolean, preferBaseProperties: Boolean, inheritedMappedSuperClassOnly: Boolean): Array<Property?>? {
        val props: Map<String?, Property?> = LinkedHashMap<String?, Property?>()
        _getProperties(top, props, onlyPeristent, includeBaseProperties, preferBaseProperties, inheritedMappedSuperClassOnly)
        return props.values().toArray(arrayOfNulls<Property?>(props.size()))
    }

    @Override
    fun getComponentScope(): ComponentScope? {
        return scope
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        return tachyon.runtime.op.OpUtil.compare(ThreadLocalPageContext.get(), if (castToBooleanValue()) Boolean.TRUE else Boolean.FALSE, if (b) Boolean.TRUE else Boolean.FALSE)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        return tachyon.runtime.op.OpUtil.compare(ThreadLocalPageContext.get(), castToDateTime() as Date?, dt as Date?)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        return tachyon.runtime.op.OpUtil.compare(ThreadLocalPageContext.get(), Double.valueOf(castToDoubleValue()), Double.valueOf(d))
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        return tachyon.runtime.op.OpUtil.compare(ThreadLocalPageContext.get(), castToString(), str)
    }

    @Throws(ApplicationException::class)
    fun addConstructorUDF(key: Key?, udf: UDF?) {
        registerUDF(key, udf, false, true)
        /*
		 * if(constructorUDFs==null) constructorUDFs=new HashMap<Key,UDF>(); constructorUDFs.put(key,
		 * value);
		 */
    }

    // MUST more native impl
    @Override
    @Throws(IOException::class, ClassNotFoundException::class)
    fun readExternal(`in`: ObjectInput?) {
        var pcCreated = false
        var pc: PageContext = ThreadLocalPageContext.get()
        try {
            if (pc == null) {
                pcCreated = true
                val config: ConfigWeb = ThreadLocalPageContext.getConfig() as ConfigWeb
                val parr: Array<Pair?> = arrayOfNulls<Pair?>(0)
                pc = ThreadUtil.createPageContext(config, DevNullOutputStream.DEV_NULL_OUTPUT_STREAM, "localhost", "/", "", arrayOfNulls<Cookie?>(0), parr, null, parr, StructImpl(), true,
                        -1)
            }

            // reading fails for serialized data from Tachyon version 4.1.2.002
            var name: String? = `in`.readUTF()

            // oldest style of serialisation
            if (name.startsWith("evaluateComponent('") && name.endsWith("})")) {
                readExternalOldStyle(pc, name)
                return
            }

            // newest version (5.2.8.16) also holds the path
            var path: String? = null
            val index: Int = name.indexOf('|')
            if (index != -1) {
                path = name.substring(index + 1)
                name = name.substring(0, index)
            }
            val md5: String = `in`.readUTF()
            val _this: Struct = Caster.toStruct(`in`.readObject(), null)
            val _var: Struct = Caster.toStruct(`in`.readObject(), null)
            val template: String = `in`.readUTF()
            if (pc != null && pc.getBasePageSource() == null && !StringUtil.isEmpty(template)) {
                val res: Resource = ResourceUtil.toResourceNotExisting(pc, template)
                val ps: PageSource = pc.toPageSource(res, null)
                if (ps != null) {
                    (pc as PageContextImpl)!!.setBase(ps)
                }
            }
            try {
                val other = EvaluateComponent.invoke(pc, name, md5, _this, _var) as ComponentImpl
                _readExternal(other)
            } catch (pe: PageException) {
                var done = false
                if (!StringUtil.isEmpty(path)) {
                    val res: Resource = ResourceUtil.toResourceExisting(pc, path, false, null)
                    if (res != null) {
                        val ps: PageSource = pc.toPageSource(res, null)
                        if (ps != null) {
                            done = try {
                                val other: ComponentImpl = ComponentLoader.loadComponent(pc, ps, name, false, true)
                                _readExternal(other)
                                true
                            } catch (pe2: PageException) {
                                throw ExceptionUtil.toIOException(pe2)
                            }
                        }
                    }
                }
                if (!done) throw ExceptionUtil.toIOException(pe)
            }
        } finally {
            if (pcCreated) ThreadLocalPageContext.release()
        }
    }

    @Throws(IOException::class)
    private fun readExternalOldStyle(pc: PageContext?, str: String?) {
        try {
            val other = CFMLExpressionInterpreter(false).interpret(pc, str) as ComponentImpl
            _readExternal(other)
        } catch (e: PageException) {
            throw ExceptionUtil.toIOException(e)
        }
    }

    private fun _readExternal(other: ComponentImpl?) {
        _data = other!!._data
        _udfs = other._udfs
        setOwner(_udfs)
        setOwner(_data)
        afterConstructor = other.afterConstructor
        base = other.base
        // this.componentPage=other.componentPage;
        pageSource = other.pageSource
        // this.constructorUDFs=other.constructorUDFs;
        dataMemberDefaultAccess = other.dataMemberDefaultAccess
        absFin = other.absFin
        isInit = other.isInit
        properties = other.properties
        scope = other.scope
        top = this
        // this._triggerDataMember=other._triggerDataMember;
        hasInjectedFunctions = other.hasInjectedFunctions
        isExtended = other.isExtended
        useShadow = other.useShadow
        entity = other.entity
        _static = other._static
    }

    private fun setOwner(data: Map<Key?, Member?>?) {
        var m: Member
        val it: Iterator<Member?> = data!!.values().iterator()
        while (it.hasNext()) {
            m = it.next()
            if (m is UDFPlus) {
                (m as UDFPlus).setOwnerComponent(this)
            }
        }
    }

    @Override
    @Throws(IOException::class)
    fun writeExternal(out: ObjectOutput?) {
        val cw = ComponentSpecificAccess(Component.ACCESS_PRIVATE, this)
        val _this: Struct = StructImpl()
        val _var: Struct = StructImpl()

        // this scope (removing all UDFs)
        var member: Object
        run {
            val it: Iterator<Entry<Key?, Object?>?> = cw!!.entryIterator()
            var e: Entry<Key?, Object?>?
            while (it.hasNext()) {
                e = it.next()
                member = e.getValue()
                if (member is UDF) continue
                _this.setEL(e.getKey(), member)
            }
        }

        // variables scope (removing all UDFs and key "this")
        run {
            val scope: ComponentScope? = getComponentScope()
            val it: Iterator<Entry<Key?, Object?>?> = scope.entryIterator()
            var e: Entry<Key?, Object?>?
            var k: Key
            while (it.hasNext()) {
                e = it.next()
                k = e.getKey()
                if (KeyConstants._THIS.equalsIgnoreCase(k)) continue
                member = e.getValue()
                if (member is UDF) continue
                _var.setEL(e.getKey(), member)
            }
        }
        out.writeUTF(getAbsName().toString() + "|" + getPageSource().getResource().getCanonicalPath())
        out.writeUTF(ComponentUtil.md5(cw))
        out.writeObject(_this)
        out.writeObject(_var)

        // base template
        var template = ""
        val pc: PageContext = ThreadLocalPageContext.get()
        if (pc != null) {
            val ps: PageSource = pc.getBasePageSource()
            if (ps != null) {
                template = ps.getDisplayPath()
            }
        }
        out.writeUTF(template)
    }

    @Override
    fun getBaseComponent(): Component? {
        return base
    }

    private fun triggerDataMember(pc: PageContext?): Boolean {
        // dialect Tachyon always triggers data members
        if (pageSource.getDialect() === CFMLEngine.DIALECT_LUCEE) return true

        // if(_triggerDataMember!=null) return _triggerDataMember.booleanValue();
        if (pc != null && pc.getApplicationContext() != null) return pc.getApplicationContext().getTriggerComponentDataMember()
        val config: Config = ThreadLocalPageContext.getConfig()
        return if (config != null) config.getTriggerComponentDataMember() else false
    }

    fun setLoaded(loaded: Boolean) {
        this.loaded = loaded
    }

    fun hasInjectedFunctions(): Boolean {
        return hasInjectedFunctions
    }

    @Override
    fun setEntity(entity: Boolean) {
        this.entity = entity
    }

    @Override
    fun isEntity(): Boolean {
        return entity
    }

    @Override
    fun staticScope(): StaticScope? {
        return _static
    }

    @Override
    fun getInterfaces(): Array<Interface?>? {
        return if (top!!.absFin == null) EMPTY else top!!.absFin.getInterfaces()
    }

    @Override
    fun id(): String? {
        return try {
            Hash.md5(getPageSource().getDisplayPath())
        } catch (e: NoSuchAlgorithmException) {
            getPageSource().getDisplayPath()
        }
    }

    @Override
    fun getType(): Int {
        return StructUtil.getType(_data)
    }

    private class ThreadLocalConstrCall : ThreadLocal<Map<String?, Boolean?>?>() {
        @Override
        protected fun initialValue(): Map<String?, Boolean?>? {
            return HashMap()
        }
    }

    class ThreadInsideStaticConstr : ThreadLocal<Boolean?>() {
        @Override
        protected fun initialValue(): Boolean? {
            return Boolean.FALSE
        }
    }

    fun setInline(): ComponentImpl? {
        properties!!.inline = true
        return this
    }

    companion object {
        private const val serialVersionUID = -245618330485511484L // do not change this
        private val EMPTY: Array<Interface?>? = arrayOfNulls<Interface?>(0)
        private val statConstr: ThreadLocalConstrCall? = ThreadLocalConstrCall()
        private fun addUDFS(trgComp: ComponentImpl?, src: Map?, trg: Map?) {
            val it: Iterator = src.entrySet().iterator()
            var entry: Map.Entry
            var key: Object
            var value: Object
            var udf: UDF
            var comp: ComponentImpl?
            var owner: ComponentImpl
            var done: Boolean
            while (it.hasNext()) {
                entry = it.next() as Entry
                key = entry.getKey()
                value = entry.getValue()
                if (value is UDF) {
                    udf = value as UDF
                    done = false
                    // get udf from _udf
                    owner = udf.getOwnerComponent()
                    if (owner != null) {
                        comp = trgComp
                        do {
                            if (owner.pageSource === comp!!.pageSource) break
                        } while (comp!!.base.also { comp = it } != null)
                        if (comp != null) {
                            value = comp!!._udfs!![key]
                            trg.put(key, value)
                            done = true
                        }
                    }
                    // udf with no owner
                    if (!done) trg.put(key, udf.duplicate())

                    // print.o(owner.pageSource.getComponentName()+":"+udf.getFunctionName());
                }
            }
        }

        /**
         * duplicate the datamember in the map, ignores the udfs
         *
         * @param c
         * @param map
         * @param newMap
         * @param deepCopy
         * @return
         */
        fun duplicateDataMember(c: ComponentImpl?, map: Map?, newMap: Map?, deepCopy: Boolean): Map? {
            val it: Iterator = map.entrySet().iterator()
            var entry: Map.Entry
            var value: Object
            while (it.hasNext()) {
                entry = it.next() as Entry
                value = entry.getValue()
                if (value !is UDF) {
                    if (deepCopy) value = Duplicator.duplicate(value, deepCopy)
                    newMap.put(entry.getKey(), value)
                }
            }
            return newMap
        }

        fun duplicateUTFMap(src: ComponentImpl?, trg: ComponentImpl?, srcMap: Map<Key?, UDF?>?, trgMap: Map<Key?, UDF?>?): Map<Key?, UDF?>? {
            val it: Iterator<Entry<Key?, UDF?>?> = srcMap.entrySet().iterator()
            var entry: Entry<Key?, UDF?>?
            var udf: UDF
            while (it.hasNext()) {
                entry = it.next()
                udf = entry.getValue()
                if (udf.getOwnerComponent() === src) {
                    val clone: UDF = entry.getValue().duplicate()
                    if (clone is UDFPlus) {
                        val cp: UDFPlus = clone as UDFPlus
                        cp.setOwnerComponent(trg)
                        cp.setAccess(udf.getAccess())
                    }
                    trgMap.put(entry.getKey(), clone)
                }
            }
            return trgMap
        }

        private fun setTop(top: ComponentImpl?, trg: ComponentImpl?) {
            var trg = trg
            while (trg != null) {
                trg.top = top
                trg = trg.base
            }
        }

        fun thisScope(ci: ComponentImpl?, pc: PageContext?, maxlevel: Int, dp: DumpProperties?, access: Int): DumpTable? {
            var maxlevel = maxlevel
            val table = DumpTable("#ffffff", "#cccccc", "#000000")
            val accesses: Array<DumpTable?> = arrayOfNulls<DumpTable?>(4)
            accesses[Component.ACCESS_REMOTE] = DumpTable("#ccffcc", "#ffffff", "#000000")
            accesses[Component.ACCESS_REMOTE].setTitle("remote")
            accesses[Component.ACCESS_PUBLIC] = DumpTable("#ffcc99", "#ffffcc", "#000000")
            accesses[Component.ACCESS_PUBLIC].setTitle("public")
            accesses[Component.ACCESS_PACKAGE] = DumpTable("#ff9966", "#ffcc99", "#000000")
            accesses[Component.ACCESS_PACKAGE].setTitle("package")
            accesses[Component.ACCESS_PRIVATE] = DumpTable("#ff6633", "#ff9966", "#000000")
            accesses[Component.ACCESS_PRIVATE].setTitle("private")
            maxlevel--
            val cw = ComponentSpecificAccess(Component.ACCESS_PRIVATE, ci)
            val keys: Array<Collection.Key?> = cw!!.keys()
            val drAccess: Array<List<DumpRow?>?> = arrayOfNulls(4)
            for (i in drAccess.indices) drAccess[i] = ArrayList() // ACCESS_REMOTE=0, ACCESS_PUBLIC=1, ACCESS_PACKAGE=2, ACCESS_PRIVATE=3
            var key: Collection.Key?
            for (i in keys.indices) {
                key = keys[i]
                val box: List<DumpRow?>? = drAccess[ci.getAccess(key)]
                var o: Object = cw.get(key, null)
                if (o === ci) o = "[this]"
                if (DumpUtil.keyValid(dp, maxlevel, key)) {
                    val memberName: String = if (o is UDF) (o as UDF).getFunctionName() else key.getString()
                    box.add(DumpRow(1, SimpleDumpData(memberName), DumpUtil.toDumpData(o, pc, maxlevel, dp)))
                }
            }
            var dumpRows: List<DumpRow?>?
            for (i in drAccess.indices) {
                dumpRows = drAccess[i]
                if (!dumpRows!!.isEmpty()) {
                    Collections.sort(dumpRows, object : Comparator<DumpRow?>() {
                        @Override
                        fun compare(o1: DumpRow?, o2: DumpRow?): Int {
                            val rowItems1: Array<DumpData?> = o1.getItems()
                            val rowItems2: Array<DumpData?> = o2.getItems()
                            return if (rowItems1.size >= 0 && rowItems2.size > 0 && rowItems1[0] is SimpleDumpData && rowItems2[0] is SimpleDumpData) String.CASE_INSENSITIVE_ORDER.compare(rowItems1[0].toString(), rowItems2[0].toString()) else 0
                        }
                    })
                    val dtAccess: DumpTable? = accesses[i]
                    dtAccess.setWidth("100%")
                    for (dr in dumpRows) dtAccess.appendRow(dr)
                    table.appendRow(0, dtAccess)
                }
            }

            // properties
            if (ci!!.top!!.properties!!.persistent || ci.top!!.properties!!.accessors) {
                val properties: Array<Property?>? = ci.getProperties(false, true, false, false)
                val prop = DumpTable("#99cc99", "#ccffcc", "#000000")
                prop.setTitle("Properties")
                prop.setWidth("100%")
                var p: Property?
                var child: Object
                for (i in properties.indices) {
                    p = properties!![i]
                    child = ci.scope.get(KeyImpl.init(p.getName()), null)
                    var dd: DumpData
                    dd = if (child is Component) {
                        val t = DumpTable("component", "#99cc99", "#ffffff", "#000000")
                        t.appendRow(1, SimpleDumpData(if ((child as Component).getPageSource().getDialect() === CFMLEngine.DIALECT_CFML) "Component" else "Class"),
                                SimpleDumpData((child as Component).getCallName()))
                        t
                    } else {
                        DumpUtil.toDumpData(child, pc, maxlevel - 1, dp)
                    }
                    prop.appendRow(1, SimpleDumpData(p.getName()), dd)
                }
                if (access >= ACCESS_PUBLIC && !prop.isEmpty()) {
                    table.appendRow(0, prop)
                }
            }
            return table
        }

        @Throws(PageException::class)
        protected fun getMetaData(access: Int, pc: PageContext?, comp: ComponentImpl?, ignoreCache: Boolean): Struct? {
            // Cache
            /*
		 * final Page page = MetadataUtil.getPageWhenMetaDataStillValid(pc, comp, ignoreCache); if (page !=
		 * null && page.metaData != null && page.metaData.get() != null) { eturn page.metaData.get(); }
		 */
            // long creationTime = System.currentTimeMillis();
            val sct = StructImpl()

            // fill udfs
            metaUDFs(pc, comp, sct, access)

            // meta
            if (comp!!.properties!!.meta != null) StructUtil.copy(comp.properties!!.meta, sct, true)
            val hint: String = comp.properties!!.hint
            val displayname: String = comp.properties!!.dspName
            if (!StringUtil.isEmpty(hint)) sct.set(KeyConstants._hint, hint)
            if (!StringUtil.isEmpty(displayname)) sct.set(KeyConstants._displayname, displayname)
            sct.set(KeyConstants._persistent, comp.properties!!.persistent)
            sct.set(KeyConstants._hashCode, comp.hashCode())
            sct.set(KeyConstants._accessors, comp.properties!!.accessors)
            sct.set(KeyConstants._synchronized, comp.properties!!._synchronized)
            sct.set(KeyConstants._inline, comp.properties!!.inline)
            sct.set(KeyConstants._sub, !comp.properties!!.inline && !StringUtil.isEmpty(comp.properties!!.subName))
            if (comp.properties!!.output != null) sct.set(KeyConstants._output, comp.properties!!.output)
            if (comp.properties!!.modifier === MODIFIER_ABSTRACT) sct.set(KeyConstants._abstract, true)
            if (comp.properties!!.modifier === MODIFIER_FINAL) sct.set(KeyConstants._final, true)

            // extends
            var ex: Struct? = null
            if (comp.base != null) ex = getMetaData(access, pc, comp.base, true)
            if (ex != null) sct.set(KeyConstants._extends, ex)

            // implements
            if (comp.absFin != null) {
                val set: Set<String?> = ListUtil.listToSet(comp.properties!!.implement, ",", true)
                if (comp.absFin.hasInterfaces()) {
                    val it: Iterator<InterfaceImpl?> = comp.absFin.getInterfaceIt()
                    val imp: Struct = StructImpl()
                    var inter: InterfaceImpl?
                    while (it.hasNext()) {
                        inter = it.next()
                        if (!set.contains(inter!!.getCallPath())) continue
                        imp.setEL(KeyImpl.init(inter!!.getCallPath()), inter!!.getMetaData(pc, true))
                    }
                    sct.set(KeyConstants._implements, imp)
                }
            }

            // PageSource
            val ps: PageSource? = comp.pageSource
            sct.set(KeyConstants._fullname, if (comp.properties!!.inline) "" else comp.properties!!.name)
            sct.set(KeyConstants._name, if (comp.properties!!.inline) "" else comp.properties!!.name)
            sct.set(KeyConstants._subname, if (comp.properties!!.inline) "" else comp.properties!!.subName)
            sct.set(KeyConstants._path, ps.getDisplayPath())
            sct.set(KeyConstants._type, "component")
            val dialect: Int = comp.getPageSource().getDialect()
            val supressWSBeforeArg = dialect != CFMLEngine.DIALECT_CFML || pc.getConfig().getSuppressWSBeforeArg()
            val skeleton: Class<*>? = comp.getJavaAccessClass(pc, RefBooleanImpl(false), (pc.getConfig() as ConfigPro).getExecutionLogEnabled(), false, false, supressWSBeforeArg)
            if (skeleton != null) sct.set(KeyConstants._skeleton, skeleton)
            if (comp.properties!!.subName == null) {
                val req: HttpServletRequest = pc.getHttpServletRequest()
                try {
                    val path: String = ContractPath.call(pc, ps.getDisplayPath()) // MUST better impl !!!
                    sct.set("remoteAddress", "" + URL(req.getScheme(), req.getServerName(), req.getServerPort(), req.getContextPath() + path + "?wsdl"))
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                }
            }

            // Properties
            if (comp.properties!!.properties != null) {
                val parr = ArrayImpl()
                var p: Property
                val pit: Iterator<Entry<String?, Property?>?> = comp.properties!!.properties.entrySet().iterator()
                while (pit.hasNext()) {
                    p = pit.next().getValue()
                    parr.append(p.getMetaData())
                }
                // parr.sortIt(new ArrayOfStructComparator(KeyConstants._name));
                sct.set(KeyConstants._properties, parr)
            }

            // if (page != null) page.metaData = new MetaDataSoftReference<Struct>(sct, creationTime);
            return sct
        }

        @Throws(PageException::class)
        private fun metaUDFs(pc: PageContext?, comp: ComponentImpl?, sct: Struct?, access: Int) {
            val arr = ArrayImpl()
            if (comp!!.absFin != null) {
                // we not to add abstract separately because they are not real Methods, more a rule
                if (comp.absFin.hasAbstractUDFs()) {
                    val absUdfs: Collection<UDF?> = ComponentUtil.toUDFs(comp.absFin.getAbstractUDFBs().values(), false)
                    getUDFs(pc, absUdfs.iterator(), comp, access, arr, false)
                }
            }
            if (comp._udfs != null) {
                getUDFs(pc, comp._udfs!!.values().iterator(), comp, access, arr, false)
            }
            if (comp._static != null) {
                val entries: Map<Key?, Object?> = comp._static._entries(HashMap<Key?, Object?>(), access)
                val udfs: List<UDF?>? = extractUDFS(entries.values())
                if (udfs!!.size() > 0) getUDFs(pc, udfs.iterator(), comp, access, arr, true)
            }

            // property functions
            run {
                val it: Iterator<Entry<Key?, UDF?>?> = comp._udfs.entrySet().iterator()
                var entry: Entry<Key?, UDF?>?
                var udf: UDF
                while (it.hasNext()) {
                    entry = it.next()
                    udf = entry.getValue()
                    if (udf.getAccess() > access || udf !is UDFGSProperty) continue
                    if (comp.base != null) {
                        if (udf === comp.base.getMember(access, entry.getKey(), true, true)) continue
                    }
                    arr.append(udf.getMetaData(pc))
                }
            }

            // static functions
            run {
                var udf: UDF
                val statics: StaticScope? = comp.staticScope()
                val it: Iterator<Entry<Key?, Object?>?> = statics!!.entryIterator(ACCESS_PRIVATE)
                var e: Entry<Key?, Object?>?
                while (it.hasNext()) {
                    e = it.next()
                    if (e.getValue() !is UDF) continue
                    udf = e.getValue() as UDF
                    if (udf.getAccess() > access || udf !is UDFGSProperty) continue
                    arr.append(udf.getMetaData(pc))
                }
            }
            if (arr.size() !== 0) {
                Collections.sort(arr, ComparatorImpl())
            }
            sct.set(KeyConstants._functions, arr)
        }

        private fun extractUDFS(values: Collection<*>?): List<UDF?>? {
            val udfs: List<UDF?> = ArrayList()
            for (o in values!!) {
                if (o is UDF) udfs.add(o as UDF?)
            }
            return udfs
        }

        @Throws(PageException::class)
        private fun getUDFs(pc: PageContext?, it: Iterator<UDF?>?, comp: ComponentImpl?, access: Int, arr: ArrayImpl?, isStatic: Boolean) {
            var udf: UDF?
            while (it!!.hasNext()) {
                udf = it.next()
                if (udf is UDFGSProperty) continue
                if (udf.getAccess() > access) continue
                if (udf.getPageSource() != null && !udf.getPageSource().equals(comp!!._getPageSource())) continue
                if (udf is UDFImpl) arr.append(ComponentUtil.getMetaData(pc, (udf as UDFImpl?).properties, isStatic))
            }
        }

        private fun _getProperties(c: ComponentImpl?, props: Map<String?, Property?>?, onlyPeristent: Boolean, includeBaseProperties: Boolean, preferBaseProperties: Boolean,
                                   inheritedMappedSuperClassOnly: Boolean) {
            // if(c.properties.properties==null) return new Property[0];

            // collect with filter
            if (c!!.properties!!.properties != null) {
                var p: Property
                val it: Iterator<Entry<String?, Property?>?> = c.properties!!.properties.entrySet().iterator()
                while (it.hasNext()) {
                    p = it.next().getValue()
                    if (!onlyPeristent || p.isPeristent()) {
                        if (!preferBaseProperties || !props!!.containsKey(p.getName().toLowerCase())) {
                            props.put(p.getName().toLowerCase(), p)
                        }
                    }
                }
            }

            // MZ: Moved to the bottom to allow base properties to override inherited versions
            if (includeBaseProperties && c.base != null) {
                if (!inheritedMappedSuperClassOnly
                        || c.base!!.properties!!.meta != null && Caster.toBooleanValue(c.base!!.properties!!.meta.get(KeyConstants._mappedSuperClass, Boolean.FALSE), false)) {
                    _getProperties(c.base, props, onlyPeristent, includeBaseProperties, preferBaseProperties, inheritedMappedSuperClassOnly)
                }
            }
        }
    }
}