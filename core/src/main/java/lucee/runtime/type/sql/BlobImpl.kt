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
package lucee.runtime.type.sql

import java.io.ByteArrayInputStream

/**
 * Implementation of the Interface java.sql.Blob
 */
class BlobImpl private constructor(data: ByteArray?) : java.sql.Blob, Serializable {
    var binaryData: ByteArray? = null
    @Override
    @Throws(SQLException::class)
    fun length(): Long {
        return binaryData!!.size.toLong()
    }

    @Override
    @Throws(SQLException::class)
    fun getBytes(pos: Long, length: Int): ByteArray? {
        val newData = ByteArray(length)
        System.arraycopy(binaryData, (pos - 1).toInt(), newData, 0, length)
        return newData
    }

    @get:Throws(SQLException::class)
    @get:Override
    val binaryStream: java.io.InputStream?
        get() = ByteArrayInputStream(binaryData)

    @Override
    fun getBinaryStream(pos: Long, length: Long): java.io.InputStream? {
        // TODO impl this
        return ByteArrayInputStream(binaryData)
    }

    @Override
    @Throws(SQLException::class)
    fun position(pattern: ByteArray?, start: Long): Long {
        return String(binaryData).indexOf(String(pattern), start.toInt())
    }

    @Override
    @Throws(SQLException::class)
    fun position(pattern: java.sql.Blob?, start: Long): Long {
        return position(pattern.getBytes(0, pattern.length() as Int), start)
    }

    @Override
    @Throws(SQLException::class)
    fun setBytes(pos: Long, bytes: ByteArray?): Int {
        // TODO impl.
        throw SQLException("JDBC 3.0 Method setBytes not implemented")
    }

    @Override
    @Throws(SQLException::class)
    fun setBytes(pos: Long, bytes: ByteArray?, offset: Int, len: Int): Int {
        // TODO impl.
        throw SQLException("JDBC 3.0 Method setBytes not implemented")
    }

    @Override
    @Throws(SQLException::class)
    fun setBinaryStream(pos: Long): java.io.OutputStream? {
        // TODO impl.
        throw SQLException("JDBC 3.0 Method setBinaryStream not implemented")
    }

    @Override
    @Throws(SQLException::class)
    fun truncate(len: Long) {
        // TODO impl.
        throw SQLException("JDBC 3.0 Method truncate not implemented")
    }

    /*
	 * public static Blob toBlob(byte[] value) {
	 * 
	 * Class blobClass = ClassUtil.loadClass("oracle.sql.BLOB",null); if(blobClass!=null){ try { //BLOB
	 * blob = BLOB.getEmptyBLOB(); Method getEmptyBLOB = blobClass.getMethod("getEmptyBLOB",new
	 * Class[]{}); Object blob = getEmptyBLOB.invoke(null, ArrayUtil.OBJECT_EMPTY);
	 * 
	 * //blob.setBytes(value); Method setBytes = blobClass.getMethod("setBytes", new
	 * Class[]{byte[].class}); setBytes.invoke(blob, new Object[]{value});
	 * 
	 * return (Blob) blob; } catch (Exception e) {} } return new BlobImpl(value); }
	 */
    @Override
    fun free() {
        binaryData = ByteArray(0)
    }

    companion object {
        @Throws(PageException::class)
        fun toBlob(value: Object?): Blob? {
            return if (value is Blob) value as Blob? else BlobImpl(Caster.toBinary(value))
        }
    }

    /**
     * constructor of the class
     *
     * @param data
     */
    init {
        binaryData = data
    }
}