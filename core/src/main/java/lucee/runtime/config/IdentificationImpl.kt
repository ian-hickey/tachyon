/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.config

import java.io.IOException

abstract class IdentificationImpl(c: ConfigPro?, private val securityKey: String?, private val apiKey: String?) : Identification, Serializable {
    private var id: String? = null
    private val securityToken: String?
    @Override
    fun getApiKey(): String? {
        return apiKey
    }

    @Override
    fun getId(): String? {
        // this is here for performance reasons
        if (id == null) id = createId(securityKey, securityToken, false, securityKey)
        return id
    }

    @Override
    fun getSecurityKey(): String? {
        return securityKey
    }

    @Override
    fun getSecurityToken(): String? {
        return securityToken
    }

    companion object {
        fun createId(key: String?, token: String?, addMacAddress: Boolean, defaultValue: String?): String? {
            return try {
                if (addMacAddress) { // because this was new we could swutch to a new ecryption // FUTURE cold we get rid of the old one?
                    Hash.sha256(key.toString() + ";" + token + ":" + SystemUtil.getMacAddress(""))
                } else Md5.getDigestAsString(key + token)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                defaultValue
            }
        }

        private fun createSecurityToken(dir: Resource?): String? {
            return try {
                Md5.getDigestAsString(dir.getAbsolutePath())
            } catch (e: IOException) {
                null
            }
        }

        protected fun append(qs: StringBuilder?, name: String?, value: String?) {
            if (Util.isEmpty(value, true)) return
            if (qs.length() > 0) qs.append('&') else qs.append('?')
            qs.append(name).append('=').append(value) // TODO encoding
        }
    }

    init {
        securityToken = createSecurityToken(c.getConfigDir())
    }
}