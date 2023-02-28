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
package lucee.commons.io.res.type.smb

import java.io.IOException

class SMBResourceProvider : ResourceProvider {
    @get:Override
    var scheme = "smb"
        private set

    @get:Override
    var arguments: Map<String, String>? = null
        private set
    private val lock: ResourceLockImpl = ResourceLockImpl(10000, false)
    @Override
    fun init(scheme: String, arguments: Map): ResourceProvider {
        _setProperties(arguments)
        if (!StringUtil.isEmpty(scheme)) this.scheme = scheme
        this.arguments = arguments
        return this
    }

    private fun _setProperties(arguments: Map) {
        var resolveOrder = arguments.get("resolveOrder") as String
        if (resolveOrder == null) resolveOrder = "DNS"
        var dfsDisabled = arguments.get("smb.client.dfs.disabled") as String
        if (dfsDisabled == null) dfsDisabled = "true"
        System.setProperty("jcifs.resolveOrder", resolveOrder)
        System.setProperty("jcifs.smb.client.dfs.disabled", dfsDisabled)
    }

    fun getResource(path: String?, auth: NtlmPasswordAuthentication?): Resource {
        return SMBResource(this, path, auth)
    }

    @Override
    fun getResource(path: String): Resource {
        return SMBResource(this, path)
    }

    @Override
    fun setResources(resources: Resources?) {
        // TODO Not sure what this does
    }

    @Override
    fun unlock(res: Resource?) {
        lock.unlock(res)
    }

    @Override
    @Throws(IOException::class)
    fun lock(res: Resource?) {
        lock.lock(res)
    }

    @Override
    @Throws(IOException::class)
    fun read(res: Resource?) {
        lock.read(res)
    }

    @get:Override
    val isCaseSensitive: Boolean
        get() = false

    @get:Override
    val isModeSupported: Boolean
        get() = false

    @get:Override
    val isAttributesSupported: Boolean
        get() = false

    fun getFile(path: String?, auth: NtlmPasswordAuthentication?): SmbFile? {
        return try {
            SmbFile(path, auth)
        } catch (e: MalformedURLException) {
            null // null means it is a bad SMBFile
        }
    }

    companion object {
        private const val ENCRYPTED_PREFIX = "\$smb-enc$"
        private val UTF8: Charset = CharsetUtil.UTF8
        private val Base32DecEnc: Base32 = Base32()
        fun isEncryptedUserInfo(userInfo: String): Boolean {
            return userInfo.startsWith(ENCRYPTED_PREFIX)
        }

        fun unencryptUserInfo(userInfo: String): String {
            if (!isEncryptedUserInfo(userInfo)) return userInfo
            val encrypted: String = userInfo.replaceAll(Pattern.quote(ENCRYPTED_PREFIX), "")
            val unencryptedBytes: ByteArray = Base32DecEnc.decode(encrypted.toUpperCase())
            return String(unencryptedBytes, UTF8)
        }

        fun encryptUserInfo(userInfo: String): String {
            val bytes: ByteArray = Base32DecEnc.encode(userInfo.getBytes(UTF8))
            return ENCRYPTED_PREFIX.concat(String(bytes, UTF8))
        }
    }
}