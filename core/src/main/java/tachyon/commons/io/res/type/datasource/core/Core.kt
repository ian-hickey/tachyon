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
package tachyon.commons.io.res.type.datasource.core

import java.io.IOException

interface Core {
    /**
     * @return return true if this core support concatenation of existing data with new data
     * (getOutputStream(append:true))
     */
    fun concatSupported(): Boolean

    /**
     * return a single Attr, if Attr does not exist it returns null
     *
     * @param dc
     * @param path
     * @param name
     * @param name2
     * @return
     * @throws SQLException
     */
    @Throws(SQLException::class)
    fun getAttr(dc: DatasourceConnection?, prefix: String?, fullPathHash: Int, path: String?, name: String?): Attr?

    /**
     * return all child Attrs of a given path
     *
     * @param dc
     * @param prefix
     * @param path
     * @return
     * @throws SQLException
     */
    @Throws(SQLException::class)
    fun getAttrs(dc: DatasourceConnection?, prefix: String?, pathHash: Int, path: String?): List?

    /**
     * create a new entry (file or directory)
     *
     * @param dc
     * @param prefix
     * @param path
     * @param name
     * @param type
     * @throws SQLException
     */
    @Throws(SQLException::class)
    fun create(dc: DatasourceConnection?, prefix: String?, fullPatHash: Int, pathHash: Int, path: String?, name: String?, type: Int)

    /**
     * deletes an entry (file or directory)
     *
     * @param dc
     * @param prefix
     * @param attr
     * @return
     * @throws SQLException
     */
    @Throws(SQLException::class)
    fun delete(dc: DatasourceConnection?, prefix: String?, attr: Attr?): Boolean

    /**
     * returns an inputStream to an entry data
     *
     * @param dc
     * @param prefix
     * @param attr
     * @return
     * @throws SQLException
     * @throws IOException
     */
    @Throws(SQLException::class, IOException::class)
    fun getInputStream(dc: DatasourceConnection?, prefix: String?, attr: Attr?): InputStream?

    @Throws(SQLException::class)
    fun write(dc: DatasourceConnection?, prefix: String?, attr: Attr?, `is`: InputStream?, append: Boolean)

    @Throws(SQLException::class)
    fun setLastModified(dc: DatasourceConnection?, prefix: String?, attr: Attr?, time: Long)

    @Throws(SQLException::class)
    fun setMode(dc: DatasourceConnection?, prefix: String?, attr: Attr?, mode: Int)

    @Throws(SQLException::class)
    fun setAttributes(dc: DatasourceConnection?, prefix: String?, attr: Attr?, attributes: Int)
}