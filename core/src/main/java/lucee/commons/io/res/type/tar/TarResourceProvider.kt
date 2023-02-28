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
package lucee.commons.io.res.type.tar

import java.io.IOException

class TarResourceProvider : CompressResourceProvider() {
    @Override
    @Throws(IOException::class)
    fun getCompress(file: Resource?): Compress {
        return Compress.getInstance(file, Compress.FORMAT_TAR, caseSensitive)
    }

    @get:Override
    val isAttributesSupported: Boolean
        get() = false

    @get:Override
    val isCaseSensitive: Boolean
        get() = caseSensitive

    @get:Override
    val isModeSupported: Boolean
        get() = true

    @get:Override
    val separator: Char
        get() = '/'

    init {
        scheme = "tar"
    }
}