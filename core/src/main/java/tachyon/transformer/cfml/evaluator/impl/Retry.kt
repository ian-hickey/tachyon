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
package tachyon.transformer.cfml.evaluator.impl

import tachyon.transformer.bytecode.Statement

/**
 * Prueft den Kontext des Tag break. Das Tag `break` darf nur innerhalb des Tag
 * `loop, while, foreach` liegen.
 */
class Retry : EvaluatorSupport() {
    @Override
    @Throws(EvaluatorException::class)
    fun evaluate(tag: Tag?, libTag: TagLibTag?) {
        val ns: String = libTag.getTagLib().getNameSpaceAndSeparator()
        val name = ns + "catch"
        if (getAncestorCatch(libTag.getTagLib(), tag) == null) throw EvaluatorException("Wrong Context, tag [" + libTag.getFullName().toString() + "] must be inside a [" + name + "] tag")
    }

    companion object {
        fun getAncestorCatch(tagLib: TagLib?, stat: Statement?): Statement? {
            val name: String = tagLib.getNameSpaceAndSeparator().toString() + "catch"
            var tag: Tag?
            var parent: Statement? = stat
            while (true) {
                parent = parent!!.getParent()
                if (parent == null) return null
                if (parent is Tag) {
                    tag = parent as Tag?
                    if (tag.getFullname().equalsIgnoreCase(name)) return tag
                } else if (parent is TryCatchFinally) return parent
            }
        }
    }
}