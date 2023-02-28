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
/**
 * Implements the CFML Function getfunctiondescription
 */
package lucee.runtime.functions.other

import java.util.Iterator

object GetTagData : Function {
    private const val serialVersionUID = -4928080244340202246L
    @Throws(PageException::class)
    fun call(pc: PageContext?, nameSpace: String?, strTagName: String?): Struct? {
        return _call(pc, nameSpace, strTagName, pc.getCurrentTemplateDialect())
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, nameSpace: String?, strTagName: String?, strDialect: String?): Struct? {
        val dialect: Int = ConfigWebUtil.toDialect(strDialect, -1)
        if (dialect == -1) throw FunctionException(pc, "GetTagData", 3, "dialect", "invalid dialect [$strDialect] definition")
        return _call(pc, nameSpace, strTagName, dialect)
    }

    @Throws(PageException::class)
    private fun _call(pc: PageContext?, nameSpace: String?, strTagName: String?, dialect: Int): Struct? {
        val tlt: TagLibTag = TagUtil.getTagLibTag(pc, dialect, nameSpace, strTagName)
                ?: throw ExpressionException("tag [$nameSpace$strTagName] is not a built in tag")

        // CFML Based Function
        var clazz: Class? = null
        try {
            clazz = tlt.getTagClassDefinition().getClazz()
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        if (clazz === CFTagCore::class.java) {
            val pci: PageContextImpl? = pc as PageContextImpl?
            val prior: Boolean = pci.useSpecialMappings(true)
            return try {
                cfmlBasedTag(pc, tlt.getTagLib(), tlt)
            } finally {
                pci.useSpecialMappings(prior)
            }
        }
        return javaBasedTag(tlt.getTagLib(), tlt)
    }

    @Throws(PageException::class)
    private fun cfmlBasedTag(pc: PageContext?, tld: TagLib?, tag: TagLibTag?): Struct? {

        // Map attrs = tag.getAttributes();
        val attrFilename: TagLibTagAttr = tag.getAttribute("__filename")
        val attrMapping: TagLibTagAttr = tag.getAttribute("__mapping")
        val attrIsWeb: TagLibTagAttr = tag.getAttribute("__isweb")
        val filename: String = Caster.toString(attrFilename.getDefaultValue())
        val name: String = Caster.toString(attrFilename.getDefaultValue())
        var mapping: String = Caster.toString(attrMapping.getDefaultValue())
        if (StringUtil.isEmpty(mapping)) mapping = "mapping-tag"
        val isWeb: Boolean = Caster.toBooleanValue(attrIsWeb.getDefaultValue())
        val source: InitFile = CFTagCore.createInitFile(pc, isWeb, filename, mapping)
        val callPath: String = ResourceUtil.removeExtension(source.getFilename(), source.getFilename())
        val cfc: Component = ComponentLoader.loadComponent(pc, source.getPageSource(), callPath, false, true)
        val cw: ComponentSpecificAccess = ComponentSpecificAccess.toComponentSpecificAccess(Component.ACCESS_PRIVATE, cfc)
        val metadata: Struct = Caster.toStruct(cw.get("metadata", null), null, false)
        val sct: Struct = StructImpl(StructImpl.TYPE_LINKED)
        sct.set("nameSpaceSeperator", tld.getNameSpaceSeparator())
        sct.set("nameSpace", tld.getNameSpace())
        sct.set(KeyConstants._name, name.substring(0, name.lastIndexOf('.')))
        sct.set("hasNameAppendix", Boolean.FALSE)
        sct.set(KeyConstants._status, "implemented")
        sct.set(KeyConstants._type, "cfml")
        sct.set("bodyType", getBodyType(tag))
        sct.set("attrMin", Caster.toDouble(0))
        sct.set("attrMax", Caster.toDouble(0))
        sct.set("attributeCollection", getSupportAttributeCollection(tag))

        // TODO add support for script for cfml tags
        val scp: Struct = StructImpl(StructImpl.TYPE_LINKED)
        sct.set(KeyConstants._script, scp)
        scp.set("rtexpr", Boolean.FALSE)
        scp.set(KeyConstants._type, "none")
        if (metadata != null) {
            sct.set(KeyConstants._description, metadata.get("hint", ""))
            sct.set("attributeType", metadata.get("attributeType", ""))
            sct.set("parseBody", Caster.toBoolean(metadata.get("parseBody", Boolean.FALSE), Boolean.FALSE))
            val _attrs: Struct = StructImpl(StructImpl.TYPE_LINKED)
            sct.set(KeyConstants._attributes, _attrs)
            val srcAttrs: Struct = Caster.toStruct(metadata.get(KeyConstants._attributes, null), null, false)
            var src: Struct
            if (srcAttrs != null) {
                // Key[] keys = srcAttrs.keys();
                val it: Iterator<Entry<Key?, Object?>?> = srcAttrs.entryIterator()
                var e: Entry<Key?, Object?>?
                while (it.hasNext()) {
                    e = it.next()
                    src = Caster.toStruct(e.getValue(), null, false)
                    if (Caster.toBooleanValue(src.get(KeyConstants._hidden, null), false)) continue
                    val _attr: Struct = StructImpl(StructImpl.TYPE_LINKED)
                    _attr.set(KeyConstants._status, "implemented")
                    _attr.set(KeyConstants._description, src.get(KeyConstants._hint, ""))
                    _attr.set(KeyConstants._type, src.get(KeyConstants._type, "any"))
                    _attr.set(KeyConstants._required, Caster.toBoolean(src.get(KeyConstants._required, ""), null))
                    _attr.set("scriptSupport", "none")
                    _attrs.setEL(e.getKey(), _attr)
                }
            }
        }
        return sct
    }

    @Throws(PageException::class)
    private fun javaBasedTag(tld: TagLib?, tag: TagLibTag?): Struct? {
        val sct: Struct = StructImpl(StructImpl.TYPE_LINKED)
        sct.set("nameSpaceSeperator", tld.getNameSpaceSeparator())
        sct.set("nameSpace", tld.getNameSpace())
        sct.set(KeyConstants._name, tag.getName())
        sct.set(KeyConstants._description, tag.getDescription())
        sct.set(KeyConstants._status, TagLibFactory.toStatus(tag.getStatus()))
        sct.set("attributeType", getAttributeType(tag))
        sct.set("parseBody", Caster.toBoolean(tag.getParseBody()))
        sct.set("bodyType", getBodyType(tag))
        sct.set("attrMin", Caster.toDouble(tag.getMin()))
        sct.set("attrMax", Caster.toDouble(tag.getMax()))
        sct.set("hasNameAppendix", Caster.toBoolean(tag.hasAppendix()))
        sct.set("attributeCollection", getSupportAttributeCollection(tag))
        if (tag.getIntroduced() != null) sct.set(GetFunctionData.INTRODUCED, tag.getIntroduced().toString())

        // script
        val script: TagLibTagScript = tag.getScript()
        if (script != null) {
            val scp: Struct = StructImpl(StructImpl.TYPE_LINKED)
            sct.set(KeyConstants._script, scp)
            scp.set("rtexpr", Caster.toBoolean(script.getRtexpr()))
            scp.set(KeyConstants._type, TagLibTagScript.toType(script.getType(), "none"))
            if (script.getType() === TagLibTagScript.TYPE_SINGLE) {
                val attr: TagLibTagAttr = script.getSingleAttr()
                if (attr != null) scp.set("singletype", attr.getScriptSupportAsString()) else scp.set("singletype", "none")
            }
        }
        sct.set(KeyConstants._type, "java")
        val _args: Struct = StructImpl(StructImpl.TYPE_LINKED)
        sct.set(KeyConstants._attributes, _args)

        // Map<String,TagLibTagAttr> atts = tag.getAttributes();
        val it: Iterator<Entry<String?, TagLibTagAttr?>?> = tag.getAttributes().entrySet().iterator()
        var e: Entry<String?, TagLibTagAttr?>?
        while (it.hasNext()) {
            e = it.next()
            val attr: TagLibTagAttr = e.getValue()
            if (attr.getHidden()) continue
            // for(int i=0;i<args.size();i++) {
            val _arg: Struct = StructImpl(StructImpl.TYPE_LINKED)
            _arg.set(KeyConstants._status, TagLibFactory.toStatus(attr.getStatus()))
            _arg.set(KeyConstants._description, attr.getDescription())
            _arg.set(KeyConstants._type, attr.getType())
            if (attr.getAlias() != null) _arg.set(KeyConstants._alias, ListUtil.arrayToList(attr.getAlias(), ","))
            if (attr.getValues() != null) _arg.set(KeyConstants._values, Caster.toArray(attr.getValues()))
            if (attr.getDefaultValue() != null) _arg.set("defaultValue", attr.getDefaultValue())
            _arg.set(KeyConstants._required, if (attr.isRequired()) Boolean.TRUE else Boolean.FALSE)
            _arg.set("scriptSupport", attr.getScriptSupportAsString())
            if (attr.getIntroduced() != null) _arg.set(GetFunctionData.INTRODUCED, attr.getIntroduced().toString())
            _args.setEL(attr.getName(), _arg)
        }
        return sct
    }

    private fun getBodyType(tag: TagLibTag?): String? {
        if (!tag.getHasBody()) return "prohibited"
        return if (tag.isBodyFree()) "free" else "required"
    }

    private fun getAttributeType(tag: TagLibTag?): String? {
        val type: Int = tag.getAttributeType()
        if (TagLibTag.ATTRIBUTE_TYPE_DYNAMIC === type) return "dynamic"
        if (TagLibTag.ATTRIBUTE_TYPE_FIXED === type) return "fixed"
        if (TagLibTag.ATTRIBUTE_TYPE_MIXED === type) return "mixed"
        return if (TagLibTag.ATTRIBUTE_TYPE_NONAME === type) "noname" else "fixed"
    }

    private fun getSupportAttributeCollection(tag: TagLibTag?): Boolean? {
        return if (!tag.hasTTTClassDefinition()) Boolean.TRUE else Boolean.FALSE
    }
}