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
package lucee.runtime.functions.other

import java.util.HashSet

object ObjectEquals {
    fun call(pc: PageContext?, left: Object?, right: Object?): Boolean {
        return OpUtil.equalsComplexEL(pc, left, right, false, false)
        // return _equals(new HashSet<Object>(), left, right);
    }

    private fun _equals(done: HashSet<Object?>?, left: Object?, right: Object?): Boolean {
        // null
        if (left == null) {
            return right == null
        }
        if (left === right) return true
        val rawLeft: Object = LazyConverter.toRaw(left)
        val rawRight: Object = LazyConverter.toRaw(right)
        if (done.contains(rawLeft)) return done.contains(rawRight)
        done.add(rawLeft)
        done.add(rawRight)
        return try {

            // Components
            if (left is Component) {
                return if (right !is Component) false else _equals(done, left as Component?, right as Component?)
            }

            // Collection
            if (left is Collection) {
                return if (right !is Collection) false else _equals(done, left as Collection?, right as Collection?)
            }
            if (left is UDF) {
                if (right !is UDF) return false
            }

            // other
            left.equals(right)
        } finally {
            done.remove(rawLeft)
            done.remove(rawRight)
        }
    }

    private fun _equals(done: HashSet<Object?>?, left: Collection?, right: Collection?): Boolean {
        if (left.size() !== right.size()) return false
        val it: Iterator<Entry<Key?, Object?>?> = left.entryIterator()
        var e: Entry<Key?, Object?>?
        var l: Object
        var r: Object
        while (it.hasNext()) {
            e = it.next()
            l = e.getValue()
            r = right.get(e.getKey(), null)
            if (r == null || !_equals(done, l, r)) return false
        }
        return true
    }

    private fun _equals(done: HashSet<Object?>?, left: Component?, right: Component?): Boolean {
        if (left == null || right == null) return false
        if (!left.getPageSource().equals(right.getPageSource())) return false
        if (!_equals(done, left.getComponentScope(), right.getComponentScope())) return false
        return if (!_equals(done, left as Collection?, right as Collection?)) false else true
    }
}