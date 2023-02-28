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
package tachyon.runtime.dump

import java.util.Set

class DumpProperties(val maxlevel: Int,
                     /**
                      * @return the show
                      */
                     val show: Set<String>?,
                     /**
                      * @return the hide
                      */
                     val hide: Set<String>?,
                     /**
                      * @return the keys
                      */
                     val maxKeys: Int,
                     /**
                      * @return the metainfo
                      */
                     val metainfo: Boolean,
                     /**
                      * @return the showUDFs
                      */
                     val showUDFs: Boolean) {

    companion object {
        const val DEFAULT_MAX_LEVEL = 9999
        val DEFAULT = DumpProperties(DEFAULT_MAX_LEVEL, null, null, 9999, true, true)
    }
}