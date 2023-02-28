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
package lucee.transformer.library.tag

import java.util.Map

/**
 *
 */
class ImportTagLib(private val taglib: String?, private val prefix: String?) : TagLib(false) {
    /**
     * @see lucee.transformer.library.tag.TagLib.getAppendixTag
     */
    @Override
    override fun getAppendixTag(name: String?): TagLibTag? {
        return super.getAppendixTag(name)
    }

    /**
     * @see lucee.transformer.library.tag.TagLib.getELClass
     */
    @Override
    override fun getELClassDefinition(): ClassDefinition<out ExprTransformer?>? {
        return super.getELClassDefinition()
    }

    /**
     * @see lucee.transformer.library.tag.TagLib.getExprTransfomer
     */
    @Override
    @Throws(TagLibException::class)
    override fun getExprTransfomer(): ExprTransformer? {
        return super.getExprTransfomer()
    }

    /**
     * @see lucee.transformer.library.tag.TagLib.getNameSpace
     */
    @Override
    override fun getNameSpace(): String? {
        return super.getNameSpace()
    }

    /**
     * @see lucee.transformer.library.tag.TagLib.getNameSpaceAndSeparator
     */
    @Override
    override fun getNameSpaceAndSeparator(): String? {
        return super.getNameSpaceAndSeparator()
    }

    /**
     * @see lucee.transformer.library.tag.TagLib.getNameSpaceSeparator
     */
    @Override
    override fun getNameSpaceSeparator(): String? {
        return super.getNameSpaceSeparator()
    }

    /**
     * @see lucee.transformer.library.tag.TagLib.getTag
     */
    @Override
    override fun getTag(name: String?): TagLibTag? {
        return super.getTag(name)
    }

    /**
     * @see lucee.transformer.library.tag.TagLib.getTags
     */
    @Override
    override fun getTags(): Map? {
        return super.getTags()
    }

    @Override
    protected override fun setELClass(eLClass: String?, id: Identification?, attributes: Map<String?, String?>?) {
        super.setELClass(eLClass, id, attributes)
    }

    /**
     * @see lucee.transformer.library.tag.TagLib.setNameSpace
     */
    @Override
    override fun setNameSpace(nameSpace: String?) {
        super.setNameSpace(nameSpace)
    }

    /**
     * @see lucee.transformer.library.tag.TagLib.setNameSpaceSeperator
     */
    @Override
    override fun setNameSpaceSeperator(nameSpaceSeperator: String?) {
        super.setNameSpaceSeperator(nameSpaceSeperator)
    }

    /**
     * @see lucee.transformer.library.tag.TagLib.setTag
     */
    @Override
    override fun setTag(tag: TagLibTag?) {
        super.setTag(tag)
    }
}