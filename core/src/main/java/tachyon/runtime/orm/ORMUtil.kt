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
package tachyon.runtime.orm

import java.util.ArrayList

object ORMUtil {
    @Throws(PageException::class)
    fun getSession(pc: PageContext?): ORMSession? {
        return getSession(pc, true)
    }

    @Throws(PageException::class)
    fun getSession(pc: PageContext?, create: Boolean): ORMSession? {
        return (pc as PageContextImpl?).getORMSession(create)
    }

    @Throws(PageException::class)
    fun getEngine(pc: PageContext?): ORMEngine? {
        val config: ConfigPro = pc.getConfig() as ConfigPro
        return config.getORMEngine(pc)
    }

    /**
     *
     * @param pc
     * @param force if set to false the engine is on loaded when the configuration has changed
     * @throws PageException
     */
    @Throws(PageException::class)
    fun resetEngine(pc: PageContext?, force: Boolean) {
        val config: ConfigPro = pc.getConfig() as ConfigPro
        config.resetORMEngine(pc, force)
    }

    fun printError(t: Exception?, engine: ORMEngine?) {
        printError(t, engine, t.getMessage())
    }

    fun printError(msg: String?, engine: ORMEngine?) {
        printError(null, engine, msg)
    }

    fun printError(t: Exception?) {
        printError(t, null, t.getMessage())
    }

    fun printError(msg: String?) {
        printError(null, null, msg)
    }

    private fun printError(t: Exception?, engine: ORMEngine?, msg: String?) {
        var t: Exception? = t
        if (engine != null) LogUtil.log(ThreadLocalPageContext.get(), Log.LEVEL_ERROR, ORMUtil::class.java.getName(), "{" + engine.getLabel().toUpperCase().toString() + "} - " + msg) else LogUtil.log(ThreadLocalPageContext.get(), Log.LEVEL_ERROR, ORMUtil::class.java.getName(), msg)
        if (t == null) t = Exception()
        LogUtil.log(ThreadLocalPageContext.get(), ORMUtil::class.java.getName(), t)
    }

    fun equals(left: Object?, right: Object?): Boolean {
        val done: HashSet<Object?> = HashSet<Object?>()
        return _equals(done, left, right)
    }

    private fun _equals(done: HashSet<Object?>?, left: Object?, right: Object?): Boolean {
        if (left === right) return true
        if (left == null || right == null) return false

        // components
        if (left is Component && right is Component) {
            return _equals(done, left as Component?, right as Component?)
        }

        // arrays
        if (Decision.isArray(left) && Decision.isArray(right)) {
            return _equals(done, Caster.toArray(left, null), Caster.toArray(right, null))
        }

        // struct
        return if (Decision.isStruct(left) && Decision.isStruct(right)) {
            _equals(done, Caster.toStruct(left, null), Caster.toStruct(right, null))
        } else try {
            OpUtil.equals(ThreadLocalPageContext.get(), left, right, false)
        } catch (e: PageException) {
            false
        }
    }

    private fun _equals(done: HashSet<Object?>?, left: Collection?, right: Collection?): Boolean {
        if (done.contains(left)) return done.contains(right)
        done.add(left)
        done.add(right)
        if (left.size() !== right.size()) return false
        // Key[] keys = left.keys();
        val it: Iterator<Entry<Key?, Object?>?> = left.entryIterator()
        var e: Entry<Key?, Object?>?
        var l: Object
        var r: Object
        while (it.hasNext()) {
            e = it.next()
            l = e.getValue()
            r = right.get(e.getKey(), null)
            if (r == null || !_equals(done, l, r)) return false
        }
        return true
    }

    private fun _equals(done: HashSet<Object?>?, left: Component?, right: Component?): Boolean {
        if (done.contains(left)) return done.contains(right)
        done.add(left)
        done.add(right)
        if (left == null || right == null) return false
        if (!left.getPageSource().equals(right.getPageSource())) return false
        var props: Array<Property?>? = getProperties(left)
        var l: Object
        var r: Object
        props = getIds(props)
        for (i in props.indices) {
            l = left.getComponentScope().get(KeyImpl.init(props!![i].getName()), null)
            r = right.getComponentScope().get(KeyImpl.init(props[i].getName()), null)
            if (!_equals(done, l, r)) return false
        }
        return true
    }

    fun getIds(props: Array<Property?>?): Array<Property?>? {
        val ids: ArrayList<Property?> = ArrayList<Property?>()
        for (y in props.indices) {
            val fieldType: String = Caster.toString(props!![y].getDynamicAttributes().get(KeyConstants._fieldtype, null), null)
            if ("id".equalsIgnoreCase(fieldType) || ListUtil.listFindNoCaseIgnoreEmpty(fieldType, "id", ',') !== -1) ids.add(props[y])
        }

        // no id field defined
        if (ids.size() === 0) {
            var fieldType: String
            for (y in props.indices) {
                fieldType = Caster.toString(props!![y].getDynamicAttributes().get(KeyConstants._fieldtype, null), null)
                if (StringUtil.isEmpty(fieldType, true) && props[y].getName().equalsIgnoreCase("id")) {
                    ids.add(props[y])
                    props[y].getDynamicAttributes().setEL(KeyConstants._fieldtype, "id")
                }
            }
        }

        // still no id field defined
        if (ids.size() === 0 && props!!.size > 0) {
            var owner: String = props[0].getOwnerName()
            if (!StringUtil.isEmpty(owner)) owner = ListUtil.last(owner, '.').trim()
            var fieldType: String
            if (!StringUtil.isEmpty(owner)) {
                val id = owner + "id"
                for (y in props.indices) {
                    fieldType = Caster.toString(props[y].getDynamicAttributes().get(KeyConstants._fieldtype, null), null)
                    if (StringUtil.isEmpty(fieldType, true) && props[y].getName().equalsIgnoreCase(id)) {
                        ids.add(props[y])
                        props[y].getDynamicAttributes().setEL(KeyConstants._fieldtype, "id")
                    }
                }
            }
        }
        return ids.toArray(arrayOfNulls<Property?>(ids.size()))
    }

    fun getPropertyValue(cfc: Component?, name: String?, defaultValue: Object?): Object? {
        val props: Array<Property?>? = getProperties(cfc)
        for (i in props.indices) {
            if (!props!![i].getName().equalsIgnoreCase(name)) continue
            return cfc.getComponentScope().get(KeyImpl.init(name), null)
        }
        return defaultValue
    }

    /*
	 * jira2049 public static Object getPropertyValue(ORMSession session,Component cfc, String name,
	 * Object defaultValue) { Property[] props=getProperties(cfc); Object raw=null; SessionImpl
	 * sess=null; if(session!=null){ raw=session.getRawSession(); if(raw instanceof SessionImpl)
	 * sess=(SessionImpl) raw; } Object val; for(int i=0;i<props.length;i++){
	 * if(!props[i].getName().equalsIgnoreCase(name)) continue; val =
	 * cfc.getComponentScope().get(KeyImpl.getInstance(name),null); if(sess!=null && !(val instanceof
	 * PersistentCollection)){ if(val instanceof List) return new PersistentList(sess,(List)val); if(val
	 * instanceof Map && !(val instanceof Component)) return new PersistentMap(sess,(Map)val); if(val
	 * instanceof Set) return new PersistentSet(sess,(Set)val); if(val instanceof Array) return new
	 * PersistentList(sess,Caster.toList(val,null));
	 * 
	 * } return val; } return defaultValue; }
	 */
    private fun getProperties(cfc: Component?): Array<Property?>? {
        return cfc.getProperties(true, true, false, false)
    }

    fun isRelated(prop: Property?): Boolean {
        var fieldType: String = Caster.toString(prop.getDynamicAttributes().get(KeyConstants._fieldtype, "column"), "column")
        if (StringUtil.isEmpty(fieldType, true)) return false
        fieldType = fieldType.toLowerCase().trim()
        if ("one-to-one".equals(fieldType)) return true
        if ("many-to-one".equals(fieldType)) return true
        if ("one-to-many".equals(fieldType)) return true
        return if ("many-to-many".equals(fieldType)) true else false
    }

    fun convertToSimpleMap(paramsStr: String?): Struct? {
        var paramsStr = paramsStr
        paramsStr = paramsStr.trim()
        if (!StringUtil.startsWith(paramsStr, '{') || !StringUtil.endsWith(paramsStr, '}')) return null
        paramsStr = paramsStr.substring(1, paramsStr!!.length() - 1)
        val items: Array<String?> = ListUtil.listToStringArray(paramsStr, ',')
        val params: Struct = StructImpl()
        var index: Int
        for (i in items.indices) {
            val pair = items[i]
            index = pair.indexOf('=')
            if (index == -1) return null
            params.setEL(KeyImpl.init(deleteQuotes(pair.substring(0, index).trim()).trim()), deleteQuotes(pair.substring(index + 1).trim()))
        }
        return params
    }

    private fun deleteQuotes(str: String?): String? {
        if (StringUtil.isEmpty(str, true)) return ""
        val first: Char = str.charAt(0)
        return if ((first == '\'' || first == '"') && StringUtil.endsWith(str, first)) str.substring(1, str!!.length() - 1) else str
    }

    @Throws(PageException::class)
    fun getDefaultDataSource(pc: PageContext?): DataSource? {
        var pc: PageContext? = pc
        pc = ThreadLocalPageContext.get(pc)
        val o: Object = pc.getApplicationContext().getORMDataSource()
        if (StringUtil.isEmpty(o)) {
            val isCFML = pc.getRequestDialect() === CFMLEngine.DIALECT_CFML
            throw ORMExceptionUtil.createException(null as ORMSession? /* no session here, otherwise we get an infinite loop */, null,
                    "missing datasource definition in " + (if (isCFML) Constants.CFML_APPLICATION_EVENT_HANDLER else Constants.LUCEE_APPLICATION_EVENT_HANDLER).toString() + "/"
                            + if (isCFML) Constants.CFML_APPLICATION_TAG_NAME else Constants.LUCEE_APPLICATION_TAG_NAME,
                    null)
        }
        return if (o is DataSource) o as DataSource else pc.getDataSource(Caster.toString(o))
    }

    fun getDefaultDataSource(pc: PageContext?, defaultValue: DataSource?): DataSource? {
        var pc: PageContext? = pc
        pc = ThreadLocalPageContext.get(pc)
        val o: Object = pc.getApplicationContext().getORMDataSource()
        return if (StringUtil.isEmpty(o)) defaultValue else try {
            if (o is DataSource) o as DataSource else pc.getDataSource(Caster.toString(o))
        } catch (e: PageException) {
            defaultValue
        }
    }

    fun getDataSource(pc: PageContext?, dsn: String?, defaultValue: DataSource?): DataSource? {
        return if (StringUtil.isEmpty(dsn, true)) getDefaultDataSource(pc, defaultValue) else (pc as PageContextImpl?).getDataSource(dsn.trim(), defaultValue)
    }

    @Throws(PageException::class)
    fun getDataSource(pc: PageContext?, dsn: String?): DataSource? {
        return if (StringUtil.isEmpty(dsn, true)) getDefaultDataSource(pc) else (pc as PageContextImpl?).getDataSource(dsn.trim())
    }

    /**
     * if the given component has defined a datasource in the meta data, tachyon is returning this
     * datasource, otherwise the default orm datasource is returned
     *
     * @param pc
     * @param cfc
     * @return
     * @throws PageException
     */
    fun getDataSource(pc: PageContext?, cfc: Component?, defaultValue: DataSource?): DataSource? {
        var pc: PageContext? = pc
        pc = ThreadLocalPageContext.get(pc)

        // datasource defined with cfc
        try {
            val meta: Struct = cfc.getMetaData(pc)
            val datasourceName: String = Caster.toString(meta.get(KeyConstants._datasource, null), null)
            if (!StringUtil.isEmpty(datasourceName, true)) {
                val ds: DataSource = pc.getDataSource(datasourceName, null)
                if (ds != null) return ds
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        return getDefaultDataSource(pc, defaultValue)
    }

    /**
     * if the given component has defined a datasource in the meta data, tachyon is returning this
     * datasource, otherwise the default orm datasource is returned
     *
     * @param pc
     * @param cfc
     * @return
     * @throws PageException
     */
    @Throws(PageException::class)
    fun getDataSource(pc: PageContext?, cfc: Component?): DataSource? {
        var pc: PageContext? = pc
        pc = ThreadLocalPageContext.get(pc)

        // datasource defined with cfc
        val meta: Struct = cfc.getMetaData(pc)
        val datasourceName: String = Caster.toString(meta.get(KeyConstants._datasource, null), null)
        return if (!StringUtil.isEmpty(datasourceName, true)) {
            pc.getDataSource(datasourceName)
        } else getDefaultDataSource(pc)
    }

    @Throws(PageException::class)
    fun getDataSourceName(pc: PageContext?, cfc: Component?): String? {
        var pc: PageContext? = pc
        pc = ThreadLocalPageContext.get(pc)

        // datasource defined with cfc
        val meta: Struct = cfc.getMetaData(pc)
        val datasourceName: String = Caster.toString(meta.get(KeyConstants._datasource, null), null)
        return if (!StringUtil.isEmpty(datasourceName, true)) {
            datasourceName.trim()
        } else getDefaultDataSource(pc).getName()
    }

    fun getDataSourceName(pc: PageContext?, cfc: Component?, defaultValue: String?): String? {
        var pc: PageContext? = pc
        pc = ThreadLocalPageContext.get(pc)

        // datasource defined with cfc
        var meta: Struct? = null
        try {
            meta = cfc.getMetaData(pc)
            val datasourceName: String = Caster.toString(meta.get(KeyConstants._datasource, null), null)
            if (!StringUtil.isEmpty(datasourceName, true)) {
                return datasourceName.trim()
            }
        } catch (e: PageException) {
        }
        val ds: DataSource? = getDefaultDataSource(pc, null)
        return if (ds != null) ds.getName() else defaultValue
    }
}