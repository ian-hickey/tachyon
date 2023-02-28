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
package lucee.commons.io.res

import java.io.InputStream

class ContentTypeImpl : ContentType {
    private val type: String?
    private val subtype: String?
    private var charset: String? = null

    /**
     * Constructor of the class
     *
     * @param type
     * @param subtype
     * @param charset
     */
    constructor(type: String, subtype: String, charset: String?) {
        this.type = if (StringUtil.isEmpty(type, true)) null else type.trim().toLowerCase()
        this.subtype = if (StringUtil.isEmpty(subtype, true)) null else subtype.trim().toLowerCase()
        this.charset = if (StringUtil.isEmpty(charset, true)) null else charset.trim().toLowerCase()
    }

    /**
     * Constructor of the class
     *
     * @param type
     * @param subtype
     */
    constructor(type: String, subtype: String) : this(type, subtype, null) {}
    constructor(`is`: InputStream?) {
        val raw: String = IOUtil.getMimeType(`is`, null)
        val arr: Array<String> = ListUtil.listToStringArray(raw, '/')
        type = arr[0]
        subtype = arr[1]
    }

    @Override
    override fun equals(other: Object): Boolean {
        return if (other is ContentType) false else toString().equals(other.toString())
    }

    @Override
    override fun toString(): String {
        if (type == null) return APPLICATION_UNKNOW.toString()
        return if (charset == null) "$type/$subtype" else "$type/$subtype charset=$charset"
    }

    /**
     * @return the mime type
     */
    @get:Override
    val mimeType: String
        get() = if (type == null) APPLICATION_UNKNOW.toString() else "$type/$subtype"

    /**
     * @return the charset
     */
    @Override
    fun getCharset(): String? {
        return if (StringUtil.isEmpty(charset, true)) null else charset
    }

    companion object {
        val APPLICATION_UNKNOW: ContentType = ContentTypeImpl("application", "unknow")
    }
}