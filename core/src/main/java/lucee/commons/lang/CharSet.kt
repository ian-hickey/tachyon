/**
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
package lucee.commons.lang

import java.io.Externalizable

class CharSet : Externalizable {
    @Transient
    private var charset: java.nio.charset.Charset? = null

    /**
     * NEVER USE THIS CONSTRUCTOR DIRECTLY, THIS IS FOR Externalizable ONLY
     */
    constructor() {}
    constructor(charsetName: String?) {
        charset = java.nio.charset.Charset.forName(charsetName)
    }

    constructor(charset: java.nio.charset.Charset?) {
        this.charset = charset
    }

    @Override
    @Throws(IOException::class)
    fun writeExternal(out: ObjectOutput) {
        out.writeUTF(charset.name())
    }

    @Override
    @Throws(IOException::class, ClassNotFoundException::class)
    fun readExternal(`in`: ObjectInput) {
        charset = java.nio.charset.Charset.forName(`in`.readUTF())
    }

    override fun toString(): String {
        return charset.name()
    }

    fun name(): String {
        return charset.name()
    }

    fun toCharset(): java.nio.charset.Charset? {
        return charset
    }

    companion object {
        val UTF8: CharSet = CharSet(CharsetUtil.UTF8)
        val ISO88591: CharSet = CharSet(CharsetUtil.ISO88591)
        val UTF16BE: CharSet = CharSet(CharsetUtil.UTF16BE)
        val UTF16LE: CharSet = CharSet(CharsetUtil.UTF16LE)
        val UTF32BE: CharSet = CharSet(CharsetUtil.UTF32BE)
    }
}