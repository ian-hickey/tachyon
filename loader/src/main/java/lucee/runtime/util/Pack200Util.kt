/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.util

import java.io.File

// FUTURE remove the class
object Pack200Util {
    @Throws(IOException::class)
    fun pack2Jar(`is`: InputStream?, jar: Resource, closeIS: Boolean) {
        if (!jar.exists()) jar.createFile(false)
        pack2Jar(`is`, jar.getOutputStream(), closeIS, true)
    }

    @Throws(IOException::class)
    fun pack2Jar(p200: Resource, os: OutputStream?, closeOS: Boolean) {
        pack2Jar(p200.getInputStream(), os, true, closeOS)
    }

    @Throws(IOException::class)
    fun pack2Jar(p200: Resource, jar: Resource) {
        if (!jar.exists()) jar.createFile(false)
        pack2Jar(p200.getInputStream(), jar.getOutputStream(), true, true)
    }

    @Throws(IOException::class)
    fun pack2Jar(p200: File?, jar: File) {
        if (!jar.exists()) jar.createNewFile()
        pack2Jar(FileInputStream(p200), FileOutputStream(jar), true, true)
    }

    @Throws(IOException::class)
    fun jar2pack(`is`: InputStream?, p200: Resource, closeIS: Boolean) {
        if (!p200.exists()) p200.createFile(false)
        jar2pack(`is`, p200.getOutputStream(), closeIS, true)
    }

    @Throws(IOException::class)
    fun jar2pack(jar: Resource, os: OutputStream?, closeOS: Boolean) {
        jar2pack(jar.getInputStream(), os, true, closeOS)
    }

    @Throws(IOException::class)
    fun jar2pack(jar: Resource, p200: Resource) {
        if (!p200.exists()) p200.createFile(false)
        jar2pack(jar.getInputStream(), p200.getOutputStream(), true, true)
    }

    @Throws(IOException::class)
    fun jar2pack(jar: File?, p200: File) {
        if (!p200.exists()) p200.createNewFile()
        jar2pack(FileInputStream(jar), FileOutputStream(p200), true, true)
    }

    @Throws(IOException::class)
    fun pack2Jar(`is`: InputStream?, os: OutputStream?, closeIS: Boolean, closeOS: Boolean) {
        throw IOException("pack2Jar no longer supported!")
    }

    @Throws(IOException::class)
    fun jar2pack(`is`: InputStream?, os: OutputStream?, closeIS: Boolean, closeOS: Boolean) {
        throw IOException("pack2Jar no longer supported!")
    }

    class DevNullOutputStream
    /**
     * Constructor of the class
     */
    private constructor() : OutputStream(), Serializable {
        @Override
        fun close() {
        }

        @Override
        fun flush() {
        }

        @Override
        fun write(b: ByteArray?, off: Int, len: Int) {
        }

        @Override
        fun write(b: ByteArray?) {
        }

        @Override
        fun write(b: Int) {
        }
    }
}