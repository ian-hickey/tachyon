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
package lucee.runtime.net.http

import java.io.ByteArrayInputStream

object MultiPartResponseUtils {
    fun isMultipart(mimetype: String?): Boolean {
        return !StringUtil.isEmpty(extractBoundary(mimetype, null)) && StringUtil.startsWithIgnoreCase(mimetype, "multipart/")
    }

    @Throws(IOException::class, PageException::class)
    fun getParts(barr: ByteArray?, contentTypeHeader: String?): Array? {
        val boundary = extractBoundary(contentTypeHeader, "")
        val bis = ByteArrayInputStream(barr)
        val stream: MultipartStream?
        val result: Array = ArrayImpl()
        stream = MultipartStream(bis, getBytes(boundary, "UTF-8")) //
        var hasNextPart: Boolean = stream.skipPreamble()
        while (hasNextPart) {
            result.append(getPartData(stream))
            hasNextPart = stream.readBoundary()
        }
        return result
    }

    private fun extractBoundary(contentTypeHeader: String?, defaultValue: String?): String? {
        if (contentTypeHeader == null) return defaultValue
        val headerSections: Array<String?> = ListUtil.listToStringArray(contentTypeHeader, ';')
        for (section in headerSections) {
            val subHeaderSections: Array<String?> = ListUtil.listToStringArray(section, '=')
            val headerName: String = subHeaderSections[0].trim()
            if (headerName.toLowerCase().equals("boundary")) {
                return subHeaderSections[1].replaceAll("^\"|\"$", "")
            }
        }
        return defaultValue
    }

    @Throws(IOException::class, PageException::class)
    private fun getPartData(stream: MultipartStream?): Struct? {
        val headers: Struct? = extractHeaders(stream.readHeaders())
        val baos = ByteArrayOutputStream()
        stream.readBodyData(baos)
        val fileStruct: Struct = StructImpl()
        fileStruct.set(KeyConstants._content, baos.toByteArray())
        fileStruct.set(KeyConstants._headers, headers)
        IOUtil.close(baos)
        return fileStruct
    }

    @Throws(PageException::class)
    private fun extractHeaders(rawHeaders: String?): Struct? {
        val result: Struct = StructImpl()
        val headers: Array<String?> = ListUtil.listToStringArray(rawHeaders, '\n')
        for (rawHeader in headers) {
            val headerArray: Array<String?> = ListUtil.listToStringArray(rawHeader, ':')
            val headerName = headerArray[0]
            if (!StringUtil.isEmpty(headerName, true)) {
                val value: String = StringUtils.join(Arrays.copyOfRange(headerArray, 1, headerArray.size), ":").trim()
                result.set(headerName, value)
            }
        }
        return result
    }

    private fun getBytes(string: String?, charset: String?): ByteArray? {
        var bytes: ByteArray?
        try {
            bytes = string.getBytes(charset)
        } catch (e: UnsupportedEncodingException) {
            bytes = string.getBytes()
        }
        return bytes
    }
}