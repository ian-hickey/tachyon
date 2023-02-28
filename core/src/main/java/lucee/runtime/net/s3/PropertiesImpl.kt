/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.net.s3

import lucee.commons.lang.StringUtil

class PropertiesImpl : Properties {
    /**
     * @return the accessKeyId
     */
    /**
     * @param accessKeyId the accessKeyId to set
     */
    @get:Override
    var accessKeyId: String? = null
    /**
     * @return the secretAccessKey
     */
    /**
     * @param secretAccessKey the secretAccessKey to set
     */
    @get:Override
    var secretAccessKey: String? = null
    private var defaultLocation: String? = null
    /**
     * @return the host
     */
    // FUTURE add to interface
    // FUTURE add to interface
    /**
     * @param host the host to set
     */
    @get:Override
    var aCL: String? = "s3.amazonaws.com"
        set(acl) {
            this.acl = acl
        }
        get() = acl
    var acl: String? = null public set(millis) {
        field = millis
    }

    // FUTURE add to interface
    private var cache: Long = 0
    @Override
    fun toStruct(): Struct? {
        val sct: Struct = StructImpl()
        sct.setEL("accessKeyId", accessKeyId)
        sct.setEL("awsSecretKey", secretAccessKey)
        sct.setEL("defaultLocation", defaultLocation)
        sct.setEL("host", aCL)
        if (!StringUtil.isEmpty(acl)) sct.setEL("acl", acl)
        return sct
    }

    /**
     * @return the defaultLocation
     */
    @Override
    fun getDefaultLocation(): String? {
        return defaultLocation
    }

    /**
     * @param defaultLocation the defaultLocation to set
     */
    fun setDefaultLocation(defaultLocation: String?) {
        this.defaultLocation = improveLocation(defaultLocation)
    }

    @Override
    override fun toString(): String {
        return "accessKeyId:" + accessKeyId + ";defaultLocation:" + defaultLocation + ";host:" + aCL + ";secretAccessKey:" + secretAccessKey
    }

    companion object {
        private fun improveLocation(location: String?): String? {
            var location = location
            if (location == null) return location
            location = location.toLowerCase().trim()
            if ("usa".equals(location)) return "us"
            if ("u.s.".equals(location)) return "us"
            if ("u.s.a.".equals(location)) return "us"
            if ("united states of america".equals(location)) return "us"
            if ("europe.".equals(location)) return "eu"
            if ("euro.".equals(location)) return "eu"
            if ("e.u.".equals(location)) return "eu"
            return if ("usa-west".equals(location)) "us-west" else location
        }
    }
}