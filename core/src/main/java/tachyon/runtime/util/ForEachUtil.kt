/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.runtime.util

import java.util.Enumeration

object ForEachUtil {
    @Throws(PageException::class)
    fun loopCollection(o: Object?): Iterator? {
        // only components are handled with ForEachIteratorable, because of he magic functions
        if (Decision.isComponent(o)) return Caster.toComponent(o).getIterator()
        val it: Iterator? = _toIterator(o)
        if (it != null) return it
        return if (o is ObjectWrap) loopCollection((o as ObjectWrap?).getEmbededObject()) else loopCollection(Caster.toCollection(o))
    }

    @Throws(PageException::class)
    fun forEach(o: Object?): Iterator? {
        if (o is ForEachIteratorable) return (o as ForEachIteratorable?).getIterator()

        // every are is handled with ForEachIteratorable
        if (Decision.isArray(o)) return Caster.toArray(o).getIterator()
        val it: Iterator? = _toIterator(o)
        if (it != null) return it
        return if (Decision.isWrapped(o)) forEach(Caster.unwrap(o)) else forEach(Caster.toCollection(o))
    }

    private fun _toIterator(o: Object?): Iterator? {
        if (o is Iteratorable) {
            return (o as Iteratorable?).keysAsStringIterator()
        }
        if (o is Iterator) {
            return o
        }
        if (o is Enumeration) {
            return EnumAsIt(o as Enumeration?)
        }
        if (o is JavaObject) {
            val coll: Collection = Caster.toCollection((o as JavaObject?).getEmbededObject(null), null)
            if (coll != null) return coll.getIterator()
            val names: Array<String?> = ClassUtil.getFieldNames((o as JavaObject?).getClazz())
            return ArrayIterator(names)
        } else if (o is CharSequence) {
            return ListUtil.listToArray(o.toString(), ',').getIterator()
        } else if (Decision.isSimpleValueLimited(o)) {
            val str: String = Caster.toString(o, null) ?: return null
            // should never happen
            return ListUtil.listToArray(str, ',').getIterator()
        }
        return null
    }

    @Throws(PageException::class)
    fun reset(it: Iterator?) {
        if (it is Resetable) {
            (it as Resetable?).reset()
        }
    }
}