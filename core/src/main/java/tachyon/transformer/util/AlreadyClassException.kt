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
package tachyon.transformer.util

import java.io.IOException

class AlreadyClassException(resource: Resource?, encrypted: Boolean) : IOException() {
    private val res: Resource?
    private val encrypted: Boolean
    @Throws(IOException::class)
    fun getInputStream(): InputStream? {
        return res.getInputStream()
    }

    fun getEncrypted(): Boolean {
        return encrypted
    }

    @Override
    override fun toString(): String {
        return res.getAbsolutePath()
    }

    init {
        res = resource
        this.encrypted = encrypted
    }
}