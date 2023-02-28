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
package tachyon.transformer.cfml.evaluator.impl

import java.util.ArrayList

class Sprite : EvaluatorSupport() {
    @Override
    @Throws(EvaluatorException::class)
    fun evaluate(tag: Tag?, tagLibTag: TagLibTag?, flibs: Array<FunctionLib?>?) {
        val id = "sprite_" + IDGenerator.intId()
        try {
            val page: Page = ASMUtil.getAncestorPage(null, tag)
            val sc: SourceCode = page.getSourceCode()
            var key: String = sc.id()
            key = HashUtil.create64BitHashAsString(Thread.currentThread().getId().toString() + ":" + key)
            val src: Expression = tag.getAttribute("src").getValue()

            // get data from previous sprites
            var previous = sprites!![key]
            if (previous != null) {
                previous.tag.removeAttribute("_ids")
                previous.tag.removeAttribute("_srcs")
                previous.tag = tag
            } else {
                sprites.put(key, Previous(tag).also { previous = it })
            }
            previous!!.ids.add(id)
            if (previous!!.src == null) previous!!.src = src else {
                previous!!.src = tag.getFactory().opString(previous!!.src, tag.getFactory().createLitString(","))
                previous!!.src = tag.getFactory().opString(previous!!.src, src)
            }
            tag.addAttribute(Attribute(false, "_id", tag.getFactory().createLitString(id), "string"))
            tag.addAttribute(Attribute(false, "_ids", tag.getFactory().createLitString(tachyon.runtime.type.util.ListUtil.listToList(previous!!.ids, ",")), "string"))
            tag.addAttribute(Attribute(false, "_srcs", previous!!.src, "string"))
        } catch (e: Throwable) { // TODO handle Excpetion much more precise
            ExceptionUtil.rethrowIfNecessary(e)
            throw PageRuntimeException(Caster.toPageException(e))
        }
    }

    private class Previous(tag: Tag?) {
        val ids: List<String?>? = ArrayList<String?>()
        val src: Expression? = null
        val tag: Tag?

        init {
            this.tag = tag
        }
    }

    companion object {
        // private static final Expression DELIMITER = LitString.toExprString(",");
        private val sprites: Map<String?, Previous?>? = HashMap<String?, Previous?>()
    }
}