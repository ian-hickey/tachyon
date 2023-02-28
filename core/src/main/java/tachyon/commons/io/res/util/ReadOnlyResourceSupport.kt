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
package tachyon.commons.io.res.util

import java.io.IOException

abstract class ReadOnlyResourceSupport : ResourceSupport() {
    @Override
    @Throws(IOException::class)
    fun createDirectory(createParentWhenNotExists: Boolean) {
        throw IOException("this is a read-only resource, can't create directory [$this]")
    }

    @Override
    @Throws(IOException::class)
    fun createFile(createParentWhenNotExists: Boolean) {
        throw IOException("this is a read-only resource, can't create file [$this]")
    }

    @get:Override
    val isWriteable: Boolean
        get() = false

    @Override
    @Throws(IOException::class)
    fun remove(force: Boolean) {
        throw IOException("this is a read-only resource, can't remove [$this]")
    }

    @Override
    fun setLastModified(time: Long): Boolean {
        return false
    }

    @Override
    fun setReadable(value: Boolean): Boolean {
        // throw new IOException("this is a read-only resource, can't change access of ["+this+"]");
        return false
    }

    @Override
    fun setWritable(value: Boolean): Boolean {
        // throw new IOException("this is a read-only resource, can't change access of ["+this+"]");
        return false
    }

    @Override
    @Throws(IOException::class)
    fun getOutputStream(append: Boolean): OutputStream {
        throw IOException("this is a read-only resource, can't write to it [$this]")
    }

    @get:Override
    @set:Throws(IOException::class)
    @set:Override
    var mode: Int
        get() = 292
        set(mode) {
            throw IOException("this is a read-only resource, can't change mode of [$this]")
        }
}