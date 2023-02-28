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
package tachyon.transformer.cfml.script

import java.util.HashMap

class DocComment {
    private val tmpHint: StringBuilder? = StringBuilder()
    private var hint: String? = null

    // private List<DocCommentParam> params=new ArrayList<DocComment.DocCommentParam>();
    var params: Map<String?, Attribute?>? = HashMap<String?, Attribute?>()
    fun addHint(c: Char) {
        tmpHint.append(c)
    }

    fun addParam(attribute: Attribute?) {
        params.put(attribute.getName(), attribute)
    }

    /**
     * @return the hint
     */
    fun getHint(): String? {
        if (hint == null) {
            val attr: Attribute = params.remove("hint")
            hint = if (attr != null) {
                val lit: Literal = attr.getValue() as Literal
                lit.getString().trim()
            } else {
                StringUtil.unwrap(tmpHint.toString())
            }
        }
        return hint
    }

    fun getHintAsAttribute(factory: Factory?): Attribute? {
        return Attribute(true, "hint", factory.createLitString(getHint()), "string")
    }

    /**
     * @return the params
     */
    fun getParams(): Map<String?, Attribute?>? {
        return params
    }
}