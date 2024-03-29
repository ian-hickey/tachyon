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
package tachyon.commons.activation
// Imports
import java.io.IOException

/**
 * File Data Source.
 */
class ResourceDataSource(res: Resource) : DataSource {
    /**
     * File source.
     */
    private val _file: Resource

    /**
     * Get name.
     *
     * @returns Name
     */
    @get:Override
    val name: String
        get() = _file.getName()

    /**
     * Get Resource.
     *
     * @returns Resource
     */
    val resource: Resource
        get() = _file

    /**
     * Get input stream.
     *
     * @returns Input stream
     * @throws IOException IO exception occurred
     */
    @get:Throws(IOException::class)
    @get:Override
    val inputStream: InputStream
        get() = IOUtil.toBufferedInputStream(_file.getInputStream())

    /**
     * Get content type.
     *
     * @returns Content type
     */
    @get:Override
    val contentType: String
        get() = IOUtil.getMimeType(_file, "application/unknow")

    /**
     * Get output stream.
     *
     * @returns Output stream
     * @throws IOException IO exception occurred
     */
    @get:Throws(IOException::class)
    @get:Override
    val outputStream: OutputStream
        get() {
            if (!_file.isWriteable()) {
                throw IOException("Cannot write")
            }
            return IOUtil.toBufferedOutputStream(_file.getOutputStream())
        }

    /**
     * Constructor of the class
     *
     * @param res source
     */
    init {
        _file = res
    }
}