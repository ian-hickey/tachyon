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
package lucee.runtime.net.mail

import java.io.Externalizable

/**
 *
 */
class MailPart : Externalizable {
    /**
     * @return Returns the isHTML.
     */
    /** IThe MIME media type of the part  */
    var isHTML = false
        private set
    /**
     * @return Returns the wraptext.
     */
    /**
     * @param wraptext The wraptext to set.
     */
    /** Specifies the maximum line length, in characters of the mail text  */
    var wraptext = -1

    /** The character encoding in which the part text is encoded  */
    private var charset: CharSet? = null
    private var body: String? = null
    /**
     * @return the type
     */
    /**
     * @param type the type to set
     */
    var type: String? = null
    @Override
    @Throws(IOException::class)
    fun writeExternal(out: ObjectOutput?) {
        out.writeBoolean(isHTML)
        out.writeInt(wraptext)
        writeString(out, charset.name())
        writeString(out, body)
        writeString(out, type)
    }

    @Override
    @Throws(IOException::class, ClassNotFoundException::class)
    fun readExternal(`in`: ObjectInput?) {
        isHTML = `in`.readBoolean()
        wraptext = `in`.readInt()
        charset = CharsetUtil.toCharSet(readString(`in`))
        body = readString(`in`)
        type = readString(`in`)
    }

    /**
     *
     */
    fun clear() {
        isHTML = false
        wraptext = -1
        charset = null
        body = "null"
        type = null
    }

    /**
     *
     */
    constructor() { // needed for deserialize
    }

    /**
     * @param charset
     */
    constructor(charset: Charset?) {
        this.charset = CharsetUtil.toCharSet(charset)
    }

    /**
     * @return Returns the body.
     */
    fun getBody(): String? {
        return if (wraptext > 0) StringDataSource.wrapText(body, wraptext) else body
    }

    /**
     * @param body The body to set.
     */
    fun setBody(body: String?) {
        this.body = body
    }

    /**
     * @return Returns the charset.
     */
    fun getCharset(): Charset? {
        return CharsetUtil.toCharset(charset)
    }

    var charSet: CharSet?
        get() = charset
        set(charSet) {
            charset = charSet
        }

    /**
     * @param charset The charset to set.
     */
    fun setCharset(charset: Charset?) {
        this.charset = CharsetUtil.toCharSet(charset)
    }

    /**
     * @param isHTML The type to set.
     */
    fun isHTML(isHTML: Boolean) {
        this.isHTML = isHTML
    }

    /**
     * wrap a single line
     *
     * @param str
     * @return wraped Line
     */
    private fun wrapLine(str: String?): String? {
        val wtl = wraptext
        if (str!!.length() <= wtl) return str
        val sub: String = str.substring(0, wtl)
        val rest: String = str.substring(wtl)
        val firstR: Char = rest.charAt(0)
        val ls: String = System.getProperty("line.separator")
        if (firstR == ' ' || firstR == '\t') return sub + ls + wrapLine(if (rest.length() > 1) rest.substring(1) else "")
        val indexSpace: Int = sub.lastIndexOf(' ')
        val indexTab: Int = sub.lastIndexOf('\t')
        val index = if (indexSpace <= indexTab) indexTab else indexSpace
        return if (index == -1) sub + ls + wrapLine(rest) else sub.substring(0, index) + ls + wrapLine(sub.substring(index + 1) + rest)
    }

    @Override
    override fun toString(): String {
        return "lucee.runtime.mail.MailPart(wraptext:$wraptext;type:$type;charset:$charset;body:$body;)"
    }

    companion object {
        private val NULL: String? = "<<null>>"
        @Throws(IOException::class)
        fun writeString(out: ObjectOutput?, str: String?) {
            if (str == null) out.writeObject(NULL) else out.writeObject(str)
        }

        @Throws(ClassNotFoundException::class, IOException::class)
        fun readString(`in`: ObjectInput?): String? {
            val str = `in`.readObject() as String
            return if (str.equals(NULL)) null else str
        }
    }
}