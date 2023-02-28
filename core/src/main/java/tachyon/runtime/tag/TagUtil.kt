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
package tachyon.runtime.tag

import java.util.ArrayList

object TagUtil {
    const val ORIGINAL_CASE: Short = 0
    const val UPPER_CASE: Short = 1
    const val LOWER_CASE: Short = 2

    // private static final String "invalid call of the function ["+tlt.getName()+", you can not mix
    // named on regular arguments]" = "invalid argument for
    // function, only named arguments are allowed like struct(name:\"value\",name2:\"value2\")";
    @Throws(PageException::class)
    fun setAttributeCollection(pc: PageContext?, tag: Tag?, missingAttrs: Array<MissingAttribute?>?, _attrs: Struct?, attrType: Int) {
        var tlt: TagLibTag? = null
        var k: Key?
        if (pc.getConfig() is ConfigWebPro) {
            val cw: ConfigWebPro = pc.getConfig() as ConfigWebPro
            val allTlds: List<TagLib?> = ArrayList()
            // allTlds.addAll(Arrays.asList(cw.getTLDs(CFMLEngine.DIALECT_CFML)));
            // allTlds.addAll(Arrays.asList(cw.getTLDs(CFMLEngine.DIALECT_LUCEE)));
            allTlds.addAll(Arrays.asList(cw.getTLDs(CFMLEngine.DIALECT_BOTH)))
            for (tld in allTlds) {
                tlt = tld.getTag(tag.getClass())
                if (tlt != null) break
            }
        }
        val att: Map<Key?, Object?> = HashMap<Key?, Object?>()
        run {
            val it: Iterator<Entry<Key?, Object?>?> = _attrs.entryIterator()
            var e: Entry<Key?, Object?>?
            var alias: TagLibTagAttr? = null
            while (it.hasNext()) {
                e = it.next()
                k = e.getKey()
                if (tlt != null) {
                    alias = tlt.getAttributeByAlias(k.toString())
                    if (alias != null) k = KeyImpl.init(alias.getName()) // translate alias to canonical name
                }
                att.put(k, e.getValue())
            }
        }
        if (!ArrayUtil.isEmpty(missingAttrs)) {
            var value: Object?
            var miss: MissingAttribute?
            for (i in missingAttrs.indices) {
                miss = missingAttrs!![i]
                value = att[miss!!.getName()]
                // check alias; TODO: is this still needed? we now translate aliases above
                if (value == null && !ArrayUtil.isEmpty(miss.getAlias())) {
                    val alias: Array<String?> = miss.getAlias()
                    for (y in alias.indices) {
                        value = att[KeyImpl.init(alias[y]).also { k = it }]
                        if (value != null) {
                            att.remove(k)
                            break
                        }
                    }
                }
                if (value == null) throw ApplicationException("Attribute [" + missingAttrs[i]!!.getName().getString().toString() + "] is required but missing")
                // throw new ApplicationException("attribute "+missingAttrs[i].getName().getString()+" is required
                // for tag "+tag.getFullName());
                att.put(missingAttrs[i]!!.getName(), Caster.castTo(pc, missingAttrs[i].getType(), value, false))
            }
        }
        setAttributes(pc, tag, att, attrType)
    }

    @Throws(PageException::class)
    fun setAttributes(pc: PageContext?, tag: Tag?, att: Map<Key?, Object?>?, attrType: Int) {
        val it: Iterator<Entry<Key?, Object?>?>
        var e: Entry<Key?, Object?>?
        // TagLibTag tlt=null;
        if (TagLibTag.ATTRIBUTE_TYPE_DYNAMIC === attrType) {
            val da: DynamicAttributes? = tag as DynamicAttributes?
            it = att.entrySet().iterator()
            while (it.hasNext()) {
                e = it.next()
                da.setDynamicAttribute(null, e.getKey(), e.getValue())
            }
        } else if (TagLibTag.ATTRIBUTE_TYPE_FIXED === attrType) {
            it = att.entrySet().iterator()
            while (it.hasNext()) {
                e = it.next()
                setAttribute(pc, false, true, tag, e.getKey().getLowerString(), e.getValue())
            }
        } else if (TagLibTag.ATTRIBUTE_TYPE_MIXED === attrType) {
            it = att.entrySet().iterator()
            while (it.hasNext()) {
                e = it.next()
                setAttribute(pc, true, true, tag, e.getKey().getLowerString(), e.getValue())
            }
        }
    }

    @Throws(PageException::class)
    fun setAttribute(pc: PageContext?, tag: Tag?, name: String?, value: Object?) {
        setAttribute(pc, false, false, tag, name, value)
    }

    @Throws(PageException::class)
    fun setAttribute(pc: PageContext?, doDynamic: Boolean, silently: Boolean, tag: Tag?, name: String?, value: Object?) {
        val setter: MethodInstance = Reflector.getSetter(tag, name.toLowerCase(), value, null)
        if (setter != null) {
            try {
                setter.invoke(tag)
            } catch (_e: Exception) {
                if (!(value == null && _e is IllegalArgumentException)) throw Caster.toPageException(_e)
            }
        } else if (doDynamic) {
            val da: DynamicAttributes? = tag as DynamicAttributes?
            da.setDynamicAttribute(null, name, value)
        } else if (!silently) {
            throw ApplicationException("failed to call [$name] on tag $tag")
        }
    }

    fun setDynamicAttribute(attributes: StructImpl?, name: Collection.Key?, value: Object?, caseType: Short) {
        var name: Collection.Key? = name
        if (name.equalsIgnoreCase(KeyConstants._attributecollection)) {
            if (value is tachyon.runtime.type.Collection) {
                val coll: tachyon.runtime.type.Collection? = value as tachyon.runtime.type.Collection?
                val it: Iterator<Entry<Key?, Object?>?> = coll.entryIterator()
                var e: Entry<Key?, Object?>?
                while (it.hasNext()) {
                    e = it.next()
                    if (attributes.get(e.getKey(), null) == null) attributes.setEL(e.getKey(), e.getValue())
                }
                return
            } else if (value is Map) {
                val it: Iterator = value.entrySet().iterator()
                var entry: Map.Entry
                var key: Key
                while (it.hasNext()) {
                    entry = it.next() as Entry
                    key = Caster.toKey(entry.getKey(), null)
                    if (!attributes.containsKey(key)) {
                        attributes.setEL(key, entry.getValue())
                    }
                }
                return
            }
        }
        if (LOWER_CASE == caseType) name = KeyImpl.init(name.getLowerString()) else if (UPPER_CASE == caseType) name = KeyImpl.init(name.getUpperString())
        attributes.setEL(name, value)
    }

    /**
     * load metadata from cfc based custom tags and add the info to the tag
     *
     * @param cs
     * @param config
     */
    fun addTagMetaData(cw: ConfigWebPro?, log: tachyon.commons.io.log.Log?) {
        var pc: PageContextImpl? = null
        pc = try {
            ThreadUtil.createPageContext(cw, DevNullOutputStream.DEV_NULL_OUTPUT_STREAM, "localhost", "/", "", arrayOfNulls<Cookie?>(0), arrayOfNulls<Pair?>(0), null, arrayOfNulls<Pair?>(0), StructImpl(),
                    false, -1)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            return
        }
        val orgPC: PageContext = ThreadLocalPageContext.get()
        try {
            ThreadLocalPageContext.register(pc)

            // MUST MOST of them are the same, so this is a huge overhead
            _addTagMetaData(pc, cw, CFMLEngine.DIALECT_CFML)
            _addTagMetaData(pc, cw, CFMLEngine.DIALECT_LUCEE)
        } catch (e: Exception) {
            ConfigWebFactory.log(cw, log, e)
        } finally {
            pc.getConfig().getFactory().releaseTachyonPageContext(pc, true)
            ThreadLocalPageContext.register(orgPC)
        }
    }

    private fun _addTagMetaData(pc: PageContext?, cw: ConfigWebPro?, dialect: Int) {
        var attrFileName: TagLibTagAttr
        var attrMapping: TagLibTagAttr
        var attrIsWeb: TagLibTagAttr
        var filename: String
        var mappingName: String
        var isWeb: Boolean
        var tlt: TagLibTag
        val tlds: Array<TagLib?> = cw.getTLDs(dialect)
        for (i in tlds.indices) {
            val tags: Map<String?, TagLibTag?> = tlds[i].getTags()
            val it: Iterator<TagLibTag?> = tags.values().iterator()
            while (it.hasNext()) {
                tlt = it.next()
                if (tlt.getTagClassDefinition().isClassNameEqualTo("tachyon.runtime.tag.CFTagCore")) {
                    attrFileName = tlt.getAttribute("__filename")
                    attrMapping = tlt.getAttribute("__mapping")
                    attrIsWeb = tlt.getAttribute("__isweb")
                    if (attrFileName != null && attrIsWeb != null) {
                        filename = Caster.toString(attrFileName.getDefaultValue(), null)
                        mappingName = Caster.toString(attrMapping.getDefaultValue(), "mapping-tag")
                        isWeb = Caster.toBoolean(attrIsWeb.getDefaultValue(), null)
                        if (filename != null && isWeb != null) {
                            addTagMetaData(pc, tlds[i], tlt, filename, mappingName, isWeb.booleanValue())
                        }
                    }
                }
            }
        }
    }

    private fun addTagMetaData(pc: PageContext?, tl: TagLib?, tlt: TagLibTag?, filename: String?, mappingName: String?, isWeb: Boolean) {
        if (pc == null) return
        try {
            val config: ConfigWebPro = pc.getConfig() as ConfigWebPro
            val ps: PageSource = if (isWeb) config.getTagMapping(mappingName).getPageSource(filename) else config.getServerTagMapping(mappingName).getPageSource(filename)

            // Page p = ps.loadPage(pc);
            val c: ComponentImpl = ComponentLoader.loadComponent(pc, ps, filename, true, true)
            val cw: ComponentSpecificAccess = ComponentSpecificAccess.toComponentSpecificAccess(Component.ACCESS_PRIVATE, c)
            val meta: Struct = Caster.toStruct(cw.get(KeyConstants._metadata, null), null)

            // TODO handle all metadata here and make checking at runtime useless
            if (meta != null) {

                // parse body
                val rtexprvalue: Boolean = Caster.toBooleanValue(meta.get(KeyConstants._parsebody, Boolean.FALSE), false)
                tlt.setParseBody(rtexprvalue)

                // hint
                val hint: String = Caster.toString(meta.get(KeyConstants._hint, null), null)
                if (!StringUtil.isEmpty(hint)) tlt.setDescription(hint)
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    /**
     * used by the bytecode builded
     *
     * @param pc pageContext
     * @param className
     * @param bundleName
     * @param bundleVersion
     * @return
     * @throws BundleException
     * @throws ClassException
     */
    @Throws(PageException::class)
    fun invokeBIF(pc: PageContext?, args: Array<Object?>?, className: String?, bundleName: String?, bundleVersion: String?): Object? {
        return try {
            val clazz: Class<*> = ClassUtil.loadClassByBundle(className, bundleName, bundleVersion, pc.getConfig().getIdentification(), JavaSettingsImpl.getBundleDirectories(pc))
            val bif: BIF?
            if (Reflector.isInstaneOf(clazz, BIF::class.java, false)) bif = ClassUtil.newInstance(clazz) as BIF else bif = BIFProxy(clazz)
            bif.invoke(pc, args)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(PageException::class)
    fun setAppendix(tag: Tag?, appendix: String?) { // used by generated bytecode
        // FUTURE if(tag instanceof TagPro) ((TagPro)tag).setAppendix(appendix);
        Reflector.callMethod(tag, "setAppendix", arrayOf(appendix))
    }

    @Throws(PageException::class)
    fun setMetaData(tag: Tag?, name: String?, value: Object?) { // used by generated bytecode
        // FUTURE if(tag instanceof TagPro) ((TagPro)tag).setMetaData(name,value);
        Reflector.callMethod(tag, "setMetaData", arrayOf(name, value))
    }

    @Throws(PageException::class)
    fun hasBody(tag: Tag?, hasBody: Boolean) { // used by generated bytecode
        // FUTURE if(tag instanceof BodyTagPro) ((BodyTagPro)tag).hasBody(hasBody);
        Reflector.callMethod(tag, "hasBody", arrayOf(hasBody))
    }

    @Throws(ApplicationException::class)
    fun getTagLibTag(pc: PageContext?, dialect: Int, nameSpace: String?, strTagName: String?): TagLibTag? {
        val tlds: Array<TagLib?>
        tlds = (pc.getConfig() as ConfigPro).getTLDs(dialect)
        var tld: TagLib? = null
        var tag: TagLibTag? = null
        for (i in tlds.indices) {
            tld = tlds[i]
            if (tld.getNameSpaceAndSeparator().equalsIgnoreCase(nameSpace)) {
                tag = tld.getTag(strTagName.toLowerCase())
                if (tag != null) break
            }
        }
        return tag
    }
}