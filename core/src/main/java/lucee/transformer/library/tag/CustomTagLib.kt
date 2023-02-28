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
package lucee.transformer.library.tag

import java.io.File

/**
 * extends the normal tag library, because Custom Tags has no restrictions by a TLD this Taglib
 * accept everything
 */
class CustomTagLib(private var textTagLib: String?, nameSpace: String?, nameSpaceSeperator: String?) : TagLib(false) {
    private var taglibs: Array<TagLib?>?

    /**
     * @see lucee.transformer.library.tag.TagLib.getAppendixTag
     */
    @Override
    override fun getAppendixTag(name: String?): TagLibTag? {
        val tlt = TagLibTag(this)
        tlt!!.setName("")
        tlt!!.setAppendix(true)
        tlt!!.setTagClassDefinition(CFImportTag::class.java.getName(), null, null)
        tlt!!.setHandleExceptions(true)
        tlt!!.setBodyContent("free")
        tlt!!.setParseBody(false)
        tlt!!.setDescription("Creates a CFML Custom Tag")
        tlt!!.setAttributeType(TagLibTag.ATTRIBUTE_TYPE_DYNAMIC)
        val tlta = TagLibTagAttr(tlt)
        tlta!!.setName("__custom_tag_path")
        tlta!!.setRequired(true)
        tlta!!.setRtexpr(true)
        tlta!!.setType("string")
        tlta!!.setHidden(true)
        tlta!!.setDefaultValue(textTagLib)
        tlt!!.setAttribute(tlta)
        setTag(tlt)
        return tlt
    }

    /**
     * @see lucee.transformer.library.tag.TagLib.getTag
     */
    @Override
    override fun getTag(name: String?): TagLibTag? {
        if (taglibs != null) {
            var tag: TagLibTag? = null
            for (i in taglibs.indices) {
                if (taglibs!![i].getTag(name).also { tag = it } != null) return tag
            }
        }
        return null
    }

    /**
     * @see lucee.transformer.library.tag.TagLib.getTags
     */
    @Override
    override fun getTags(): Map? {
        return MapFactory.< String, String>getConcurrentMap<String?, String?>()
    }

    /**
     * @see lucee.transformer.library.tag.TagLib.setTag
     */
    @Override
    override fun setTag(tag: TagLibTag?) {
    }

    fun append(other: TagLib?) {
        if (other is CustomTagLib) textTagLib += File.pathSeparatorChar + (other as CustomTagLib?)!!.textTagLib else {
            if (taglibs == null) {
                taglibs = arrayOf<TagLib?>(other)
            } else {
                val tmp: Array<TagLib?> = arrayOfNulls<TagLib?>(taglibs!!.size + 1)
                for (i in taglibs.indices) {
                    tmp[i] = taglibs!![i]
                }
                tmp[taglibs!!.size] = other
            }
        }
    }

    /**
     * constructor of the class
     *
     * @param textTagLib
     * @param nameSpace the namespace definition
     * @param nameSpaceSeperator the seperator beetween namespace and name of the tag
     */
    init {
        setNameSpace(nameSpace)
        setNameSpaceSeperator(nameSpaceSeperator)
    }
}