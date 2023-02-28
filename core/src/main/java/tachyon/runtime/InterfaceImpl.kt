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

import java.io.IOException

/**
 *
 * MUST add handling for new attributes (style, namespace, serviceportname, porttypename, wsdlfile,
 * bindingname, and output)
 */
class InterfaceImpl(pc: PageContext?, page: InterfacePageImpl?, strExtend: String?, hint: String?, dspName: String?, callPath: String?, realPath: Boolean, meta: Map?) : Interface {
    private val pageSource: PageSource?
    private val strExtend: String?
    private val hint: String?
    private val dspName: String?
    private val callPath: String?
    private val realPath: Boolean
    private val meta: Map?
    var initialized = false
    private var extend: List<InterfaceImpl?>? = null
    private val udfs: Map<Collection.Key?, UDF?>? = HashMap<Collection.Key?, UDF?>()
    @Override
    fun instanceOf(type: String?): Boolean {
        if (realPath) {
            if (type.equalsIgnoreCase(callPath)) return true
            if (type.equalsIgnoreCase(pageSource.getComponentName())) return true
            if (type.equalsIgnoreCase(_getName())) return true
        } else {
            if (type.equalsIgnoreCase(callPath)) return true
            if (type.equalsIgnoreCase(_getName())) return true
        }

        // extends
        if (extend == null || extend.isEmpty()) return false // no kids
        val it = extend.iterator()
        while (it.hasNext()) {
            if (it.next()!!.instanceOf(type)) return true
        }
        return false
    }

    /**
     * @return the callPath
     */
    @Override
    fun getCallPath(): String? {
        return callPath
    }

    private fun _getName(): String? { // MUST nicht so toll
        return if (callPath == null) "" else tachyon.runtime.type.util.ListUtil.last(callPath, "./", true)
    }

    @Override
    @Throws(ApplicationException::class)
    fun registerUDF(key: Collection.Key?, udf: UDF?) {
        if (udf.getModifier() === Component.MODIFIER_FINAL) throw ApplicationException("the final function [" + key + "] is not allowed within the interface [" + getPageSource().getDisplayPath() + "]")
        udfs.put(key, udf)
    }

    @Override
    @Throws(ApplicationException::class)
    fun registerUDF(key: Collection.Key?, props: UDFProperties?) {
        registerUDF(key, UDFImpl(props))
    }

    @Throws(ClassException::class, ClassNotFoundException::class, IOException::class, ApplicationException::class)
    fun regJavaFunction(key: Collection.Key?, className: String?) {
        registerUDF(key, ClassUtil.loadInstance(getPageSource().getMapping().getPhysicalClass(className)) as UDF)
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        val table = DumpTable("interface", "#99cc99", "#ffffff", "#000000")
        table.setTitle("Interface " + callPath + "" + (" " + StringUtil.escapeHTML(dspName)))
        table.setComment("Interface can not directly invoked as an object")
        // if(top.properties.extend.length()>0)table.appendRow(1,new SimpleDumpData("Extends"),new
        // SimpleDumpData(top.properties.extend));
        // if(top.properties.hint.trim().length()>0)table.appendRow(1,new SimpleDumpData("Hint"),new
        // SimpleDumpData(top.properties.hint));

        // table.appendRow(1,new SimpleDumpData(""),_toDumpData(top,pageContext,maxlevel,access));
        return table
    }

    /*
	 * *
	 * 
	 * @return the page / public InterfacePage getPage() { return page; }
	 */
    @Override
    fun getPageSource(): PageSource? {
        return pageSource
    }

    @Override
    fun getExtends(): Array<Interface?>? {
        return if (extend == null) EMPTY else extend.toArray(arrayOfNulls<InterfaceImpl?>(extend.size()))
    }

    fun _getExtends(): List<InterfaceImpl?>? {
        return extend
    }

    @Override
    @Throws(PageException::class)
    fun getMetaData(pc: PageContext?): Struct? {
        return _getMetaData(pc, this, false)
    }

    @Override
    @Throws(PageException::class)
    fun getMetaData(pc: PageContext?, ignoreCache: Boolean): Struct? {
        return _getMetaData(pc, this, ignoreCache)
    }

    @Override
    fun beforeStaticConstructor(pc: PageContext?): Variables? {
        return null
    }

    @Override
    fun afterStaticConstructor(pc: PageContext?, `var`: Variables?) {
    }

    fun getUDFIt(): Iterator<UDF?>? {
        return udfs!!.values().iterator()
    }

    companion object {
        private const val serialVersionUID = -2488865504508636253L
        private val EMPTY: Array<InterfaceImpl?>? = arrayOf()
        @Throws(PageException::class)
        fun loadInterfaces(pc: PageContext?, loadingLocation: PageSource?, listExtends: String?): List<InterfaceImpl?>? {
            val extend: List<InterfaceImpl?> = ArrayList<InterfaceImpl?>()
            val it: Iterator<String?> = tachyon.runtime.type.util.ListUtil.toListRemoveEmpty(listExtends, ',')!!.iterator()
            var inter: InterfaceImpl
            var str: String?
            while (it.hasNext()) {
                str = it.next().trim()
                if (str.isEmpty()) continue
                inter = ComponentLoader.searchInterface(pc, loadingLocation, str)
                extend.add(inter)
            }
            return extend
        }

        @Throws(PageException::class)
        private fun _getMetaData(pc: PageContext?, icfc: InterfaceImpl?, ignoreCache: Boolean): Struct? {
            val page: Page = MetadataUtil.getPageWhenMetaDataStillValid(pc, icfc, ignoreCache)
            if (page != null && page.metaData != null && page.metaData.get() != null) return page.metaData.get()
            val creationTime: Long = System.currentTimeMillis()
            val sct: Struct = StructImpl()
            val arr = ArrayImpl()
            run {
                val it: Iterator<UDF?> = icfc!!.udfs!!.values().iterator()
                while (it.hasNext()) {
                    arr.append(it.next().getMetaData(pc))
                }
            }
            if (icfc!!.meta != null) {
                val it: Iterator = icfc.meta.entrySet().iterator()
                var entry: Map.Entry
                while (it.hasNext()) {
                    entry = it.next() as Entry
                    sct.setEL(KeyImpl.toKey(entry.getKey()), entry.getValue())
                }
            }
            if (!StringUtil.isEmpty(icfc.hint, true)) sct.set(KeyConstants._hint, icfc.hint)
            if (!StringUtil.isEmpty(icfc.dspName, true)) sct.set(KeyConstants._displayname, icfc.dspName)
            // init(pc,icfc);
            if (!ArrayUtil.isEmpty(icfc.extend)) {
                val _set: Set<String?> = tachyon.runtime.type.util.ListUtil.listToSet(icfc.strExtend, ',', true)
                val ex: Struct = StructImpl()
                sct.set(KeyConstants._extends, ex)
                val it = icfc.extend!!.iterator()
                var inter: InterfaceImpl?
                while (it.hasNext()) {
                    inter = it.next()
                    if (!_set.contains(inter!!.getCallPath())) continue
                    ex.setEL(KeyImpl.init(inter.getCallPath()), _getMetaData(pc, inter, true))
                }
            }
            if (arr.size() !== 0) sct.set(KeyConstants._functions, arr)
            val ps: PageSource? = icfc.pageSource
            sct.set(KeyConstants._name, ps.getComponentName())
            sct.set(KeyConstants._fullname, ps.getComponentName())
            sct.set(KeyConstants._path, ps.getDisplayPath())
            sct.set(KeyConstants._type, "interface")
            page.metaData = MetaDataSoftReference<Struct?>(sct, creationTime)
            return sct
        }
    }

    // private Map<Collection.Key,UDF> interfacesUDFs=null;
    init {
        // print.ds("Interface::Constructor:"+page.getPageSource().getDisplayPath());
        var pc: PageContext? = pc
        pc = ThreadLocalPageContext.get(pc)
        pageSource = page.getPageSource()
        this.strExtend = strExtend
        this.hint = hint
        this.dspName = dspName
        this.callPath = callPath
        this.realPath = realPath
        this.meta = meta

        // load extends
        if (!StringUtil.isEmpty(strExtend, true)) extend = loadInterfaces(pc, pageSource, strExtend)
    }
}