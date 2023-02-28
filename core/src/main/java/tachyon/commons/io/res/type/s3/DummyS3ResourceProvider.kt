/**
 * Copyright (c) 2023, TachyonCFML.org
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
package tachyon.commons.io.res.type.s3

import java.io.IOException

class DummyS3ResourceProvider : ResourceProviderPro {
    @Override
    fun init(scheme: String?, arguments: Map<String?, String?>?): ResourceProvider {
        return this
    }

    @Override
    fun getResource(path: String?): Resource {
        throw notInstalledEL()
    }

    @get:Override
    val scheme: String
        get() = "s3"

    @get:Override
    val arguments: Map<String, String>
        get() {
            throw notInstalledEL()
        }

    @Override
    fun setResources(resources: Resources?) {
    }

    @Override
    fun unlock(res: Resource?) {
    }

    @Override
    @Throws(IOException::class)
    fun lock(res: Resource?) {
    }

    @Override
    @Throws(IOException::class)
    fun read(res: Resource?) {
    }

    @get:Override
    val isCaseSensitive: Boolean
        get() {
            throw notInstalledEL()
        }

    @get:Override
    val isModeSupported: Boolean
        get() {
            throw notInstalledEL()
        }

    @get:Override
    val isAttributesSupported: Boolean
        get() {
            throw notInstalledEL()
        }

    @get:Override
    val separator: Char
        get() {
            throw notInstalledEL()
        }

    private fun notInstalled(): PageException {
        return ApplicationException("No S3 Resource installed!", "Check out the Extension Store in the Tachyon Administrator for \"S3\".")
    }

    private fun notInstalledEL(): PageRuntimeException {
        return PageRuntimeException(notInstalled())
    }

    companion object {
        private const val S3 = "17AB52DE-B300-A94B-E058BD978511E39E"
        private const val tryToInstall = true
        private const val serialVersionUID = 3685913246889089664L
    }
}