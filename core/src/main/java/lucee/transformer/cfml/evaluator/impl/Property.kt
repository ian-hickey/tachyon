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
package lucee.transformer.cfml.evaluator.impl

import lucee.loader.engine.CFMLEngine

/**
 * Prueft den Kontext des Tag mailparam. Das Tag `mailParam` darf nur innerhalb des Tag
 * `mail` liegen.
 */
class Property : EvaluatorSupport() {
    @Override
    @Throws(EvaluatorException::class)
    fun evaluate(tag: Tag?, libTag: TagLibTag?) {
        // get component name
        val compName = getComponentName(tag)
        if (!ASMUtil.isParentTag(tag, compName)) throw EvaluatorException("Wrong Context, tag [" + libTag.getFullName().toString() + "] must be inside [" + compName.toString() + "] tag")
    }

    companion object {
        @Throws(EvaluatorException::class)
        fun getComponentName(tag: Tag?): String? {
            val page: Page
            page = try {
                ASMUtil.getAncestorPage(null, tag)
            } catch (te: TransformerException) {
                val ee = EvaluatorException(te.getMessage())
                ee.initCause(te)
                throw ee
            }
            val ns: String = tag.getTagLibTag().getTagLib().getNameSpaceAndSeparator()
            return ns + if (page.getSourceCode().getDialect() === CFMLEngine.DIALECT_CFML) Constants.CFML_COMPONENT_TAG_NAME else Constants.LUCEE_COMPONENT_TAG_NAME
        }
    }
}