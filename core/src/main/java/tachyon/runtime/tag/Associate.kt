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
package tachyon.runtime.tag

import javax.servlet.jsp.tagext.Tag

/**
 * Allows subtag data to be saved with the base tag. Applies only to custom tags.
 *
 *
 *
 */
class Associate : TagImpl() {
    /** The name of the structure in which the base tag stores subtag data.  */
    private var datacollection: Collection.Key? = ASSOC_ATTRS

    /** The name of the base tag.  */
    private var basetag: String? = null
    @Override
    fun release() {
        super.release()
        datacollection = ASSOC_ATTRS
    }

    /**
     * set the value datacollection The name of the structure in which the base tag stores subtag data.
     *
     * @param datacollection value to set
     */
    fun setDatacollection(datacollection: String?) {
        this.datacollection = KeyImpl.init(datacollection)
    }

    /**
     * set the value basetag The name of the base tag.
     *
     * @param basetag value to set
     */
    fun setBasetag(basetag: String?) {
        this.basetag = basetag
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {

        // current
        val current: CFTag? = cFTag
        var value: Struct?
        if (current == null || current.getAttributesScope().also { value = it } == null) throw ApplicationException("invalid context, tag is no inside a custom tag")

        // parent
        val parent: CFTag = GetBaseTagData.getParentCFTag(current.getParent(), basetag, -1)
                ?: throw ApplicationException("there is no parent tag with name [$basetag]")
        val thisTag: Struct = parent.getThis()
        val obj: Object = thisTag.get(datacollection, null)
        var array: Array?
        if (obj == null) {
            array = ArrayImpl(arrayOf<Object?>(value))
            thisTag.set(datacollection, array)
        } else if (Decision.isArray(obj) && Caster.toArray(obj).also { array = it }.getDimension() === 1) {
            array.append(value)
        } else {
            array = ArrayImpl(arrayOf<Object?>(obj, value))
            thisTag.set(datacollection, array)
        }
        return SKIP_BODY
    }

    /*
	 * private static CFTag getParentCFTag(Tag srcTag,String trgTagName) { String pureName=trgTagName;
	 * CFTag cfTag; if(StringUtil.startsWithIgnoreCase(pureName,"cf_")) {
	 * pureName=pureName.substring(3); } if(StringUtil.startsWithIgnoreCase(pureName,"cf")) {
	 * pureName=pureName.substring(2); } int count=0; while((srcTag=srcTag.getParent())!=null) {
	 * if(srcTag instanceof CFTag) { if(count++==0)continue; cfTag=(CFTag)srcTag; if(cfTag instanceof
	 * CFTagCore){ CFTagCore tc=(CFTagCore) cfTag; if(tc.getName().equalsIgnoreCase(pureName)) return
	 * cfTag; continue; } if(cfTag.getAppendix().equalsIgnoreCase(pureName)) { return cfTag; } } }
	 * return null; }
	 */
    private val cFTag: tachyon.runtime.tag.CFTag?
        private get() {
            var tag: Tag? = this
            while (tag.getParent().also { tag = it } != null) {
                if (tag is CFTag) {
                    return tag
                }
            }
            return null
        }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }

    companion object {
        private val ASSOC_ATTRS: Key? = KeyImpl.getInstance("AssocAttribs")
    }
}