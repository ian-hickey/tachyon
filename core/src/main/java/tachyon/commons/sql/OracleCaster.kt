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
package tachyon.commons.sql

import tachyon.runtime.op.Caster

object OracleCaster {
    private val ZERO_ARGS: Array<Object?> = arrayOfNulls<Object>(0)

    // private static final Class OPAQUE=ClassUtil.loadClass("oracle.sql.OPAQUE", null);
    fun OPAQUE(o: Object?): Object? {
        if (o == null) return null
        try {
            val bytes: ByteArray = Caster.toBytes(Reflector.callMethod(o, "getBytes", ZERO_ARGS), null)!!
            return String(bytes, "UTF-8")
        } catch (e: Exception) {
            // print.printST(e);
        }
        return o
    } /*
	 * private static boolean equals(Class left, Class right) { if(left==right)return true; return
	 * left.equals(right.getName()); }
	 */
}