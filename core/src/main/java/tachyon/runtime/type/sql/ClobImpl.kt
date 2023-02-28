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
package tachyon.runtime.type.sql

import java.io.ByteArrayInputStream

/**
 * The representation (mapping) in the Java <sup><small>TM </small> </sup> programming language of
 * an SQL `CLOB` value. An SQL `CLOB` is a built-in type that stores a
 * Character Large Object as a column value in a row of a database table. By default drivers
 * implement `Clob` using an SQL `locator(CLOB)`, which means that a
 * `Clob` object contains a logical pointer to the SQL `CLOB` data rather than
 * the data itself. A `Clob` object is valid for the duration of the transaction in which
 * is was created.
 *
 *
 * Methods in the interfaces [DriverResultSet],[CallableStatement], and
 * [PreparedStatement], such as `getClob` and `setClob` allow a
 * programmer to access an SQL `CLOB` value. The `Clob` interface provides
 * methods for getting the length of an SQL `CLOB` (Character Large Object) value, for
 * materializing a `CLOB` value on the client, and for determining the position of a
 * pattern of bytes within a `CLOB` value. In addition, this interface has methods for
 * updating a `CLOB` value.
 */
class ClobImpl private constructor(data: String?) : java.sql.Clob, Serializable {
    /** The data represented as a string of this `CLOB`  */
    private var stringData: String? = null

    /**
     * Returns the size of the `CLOB` value designated by this `Clob` object
     *
     * @return length of the `CLOB` value that this `clob` represents
     * @exception SQLException if there is an error accessing the length of the `CLOB`
     */
    @Override
    @Throws(SQLException::class)
    fun length(): Long {
        return stringData!!.length()
    }

    /**
     * Retrieves the `CLOB` value designated by this `Clob` instance as a stream.
     *
     * @return a stream containing the `CLOB` data
     * @exception SQLException if there is an error accessing the `CLOB` value
     */
    @get:Throws(SQLException::class)
    @get:Override
    val asciiStream: java.io.InputStream?
        get() = ByteArrayInputStream(stringData.getBytes())

    /**
     * Materializes the `CLOB` value designated by this <Code>object
     * as a stream of Unicode character.
     *
     * &#64;return A reader object with all the data in the `CLOB` value designated by this
     * clob object as unicode characters.
     *
     * @exception SQLException if there is an error accessing the `CLOB` value
    </Code> */
    @get:Throws(SQLException::class)
    @get:Override
    val characterStream: java.io.Reader?
        get() = StringReader(stringData)

    @Override
    fun getCharacterStream(pos: Long, len: Long): Reader? {
        return StringReader(stringData.substring(pos.toInt(), len.toInt()))
    }

    /**
     * Returns a copy of the portion of the `CLOB` value represented by this
     * `CLOB` object that starts at position *position * and has ip to *length *
     * consecutive characters.
     *
     * @param pos the position where to get the substring from
     * @param length the length of the substring
     * @return the substring
     * @exception SQLException if there is an error accessing the `CLOB`
     */
    @Override
    @Throws(SQLException::class)
    fun getSubString(pos: Long, length: Int): String? {
        if (length > stringData!!.length()) throw SQLException("Clob contains only " + stringData!!.length().toString() + " characters (asking for " + length.toString() + ").")
        return stringData.substring(pos.toInt() - 1, length)
    }

    /**
     * Retrieves the character position at which the specified string `searchstr` begins
     * within the `CLOB` value that this `Clob` object represents. The search for
     * `searchstr` begins at position `start`.
     *
     * @param searchstr the byte array for which to search
     * @param start the position at which to begin searching; the first position is 1
     * @return the position at which the pattern appears, else -1
     * @exception SQLException if there is an error accessing the `CLOB`
     */
    @Override
    @Throws(SQLException::class)
    fun position(searchstr: String?, start: Long): Long {
        return stringData.indexOf(searchstr, start.toInt())
    }

    /**
     * Retrieves the character position at which the specified `Clob` object
     * `searchstr` begins within the `CLOB` value that this `Clob`
     * object represents. The search for `searchstr` begins at position `start`.
     *
     * @param searchstr the byte array for which to search
     * @param start the position at which to begin searching; the first position is 1
     * @return the position at which the pattern appears, else -1
     * @exception SQLException if there is an error accessing the `CLOB`
     */
    @Override
    @Throws(SQLException::class)
    fun position(searchstr: java.sql.Clob?, start: Long): Long {
        return position(searchstr.getSubString(0, searchstr.length() as Int), start.toInt())
    }
    // -------------------------- JDBC 3.0 -----------------------------------
    /**
     * Retrieves a stream to be used to write Ascii characters to the CLOB value that this Clob object
     * represents, starting at position pos.
     *
     * @param pos the position where to start the stream
     * @return the ascii outputstream to this `clob` object
     * @throws SQLException if there is an error accessing the `clob`
     */
    @Override
    @Throws(SQLException::class)
    fun setAsciiStream(pos: Long): OutputStream? {
        // TODO impl.
        throw SQLException("JDBC 3.0 Method setAsciiStream not implemented")
    }

    /**
     * Retrieves a stream to be used to write a stream of Unicode characters to the CLOB value that this
     * Clob object represents, at position pos.
     *
     * @param pos the position where to start the writer
     * @return the writer to this `clob` object
     * @throws SQLException if there is an error accessing the `clob`
     */
    @Override
    @Throws(SQLException::class)
    fun setCharacterStream(pos: Long): Writer? {
        // TODO impl.
        throw SQLException("JDBC 3.0 Method setCharacterStream not implemented")
    }

    /**
     * Writes the given Java String to the CLOB value that this Clob object designates at the position
     * pos.
     *
     * @param pos the position where to set the string
     * @param str string to insert in the `clob`
     * @return return value
     * @throws SQLException if there is an error accessing the `clob`
     */
    @Override
    @Throws(SQLException::class)
    fun setString(pos: Long, str: String?): Int {
        // TODO impl.
        throw SQLException("JDBC 3.0 Method setString not implemented")
    }

    /**
     * Writes len characters of str, starting at character offset, to the CLOB value that this Clob
     * represents.
     *
     * @param pos the position
     * @param str the string
     * @param offset the offset
     * @param len the length
     * @return return value
     * @throws SQLException if there is an error accessing the `clob`
     */
    @Override
    @Throws(SQLException::class)
    fun setString(pos: Long, str: String?, offset: Int, len: Int): Int {
        // TODO impl.
        throw SQLException("JDBC 3.0 Method setString not implemented")
    }

    /**
     * Truncates the CLOB value that this Clob designates to have a length of len characters.
     *
     * @param len the length
     * @throws SQLException if there is an error accessing the `clob`
     */
    @Override
    @Throws(SQLException::class)
    fun truncate(len: Long) {
        // TODO impl.
        throw SQLException("JDBC 3.0 Method truncate not implemented")
    }

    @Override
    override fun toString(): String {
        return stringData!!
    }

    @Override
    fun free() {
        stringData = ""
    }

    companion object {
        /**
         * cast given value to a clob
         *
         * @param value
         * @return clob
         * @throws PageException
         */
        @Throws(PageException::class)
        fun toClob(value: Object?): Clob? {
            if (value is Clob) return value as Clob? else if (value is CharArray) return toClob(String(value as CharArray?)) else if (value is Reader) {
                val sw = StringWriter()
                try {
                    IOUtil.copy(value as Reader?, sw, false, true)
                } catch (e: IOException) {
                    throw ExpressionException.newInstance(e)
                }
                return toClob(sw.toString())
            }
            return toClob(Caster.toString(value))
        }

        fun toClob(value: String?): Clob? {
            return ClobImpl(value)
        }
    }

    /**
     * Creates a new `Clob` instance.
     *
     * @param data a `String` of character data
     */
    init {
        stringData = data
    }
}