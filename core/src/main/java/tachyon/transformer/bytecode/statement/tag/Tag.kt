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
package tachyon.transformer.bytecode.statement.tag

import java.util.Map

interface Tag : Statement, HasBody {
    /**
     * appendix of the tag
     *
     * @return appendix
     */
    fun getAppendix(): String?

    /**
     * return all Attributes as a map
     *
     * @return attributes
     */
    fun getAttributes(): Map<String?, Attribute?>?

    /**
     * returns the fullname of the tag
     *
     * @return fullname
     */
    fun getFullname(): String?

    /**
     * return the TagLibTag to this tag
     *
     * @return taglibtag
     */
    fun getTagLibTag(): TagLibTag?

    /**
     * sets the appendix of the tag
     *
     * @param appendix
     */
    fun setAppendix(appendix: String?)

    /**
     * sets the fullname of the tag
     *
     * @param fullname
     */
    fun setFullname(fullname: String?)

    /**
     * sets the tagLibTag of this tag
     *
     * @param tagLibTag
     */
    fun setTagLibTag(tagLibTag: TagLibTag?)

    /**
     * adds an attribute to the tag
     *
     * @param attribute
     */
    fun addAttribute(attribute: Attribute?)

    /**
     * check if tag has a tag with given name
     *
     * @param name
     * @return contains attribute
     */
    fun containsAttribute(name: String?): Boolean

    /**
     * returns the body of the tag
     *
     * @return body of the tag
     */
    @Override
    fun getBody(): Body?

    /**
     * sets the body of the tag
     *
     * @param body
     */
    fun setBody(body: Body?)

    /**
     * returns a specified attribute from the tag
     *
     * @param name
     * @return
     */
    fun getAttribute(name: String?): Attribute?

    /**
     * returns a specified attribute from the tag
     *
     * @param name
     * @return
     */
    fun removeAttribute(name: String?): Attribute?
    fun addMissingAttribute(attr: TagLibTagAttr?)
    fun getMissingAttributes(): Array<TagLibTagAttr?>?
    fun setScriptBase(scriptBase: Boolean)
    fun isScriptBase(): Boolean

    // public abstract void setHint(String hint);
    fun addMetaData(metadata: Attribute?)

    // public abstract String getHint();
    fun getMetaData(): Map<String?, Attribute?>?
}