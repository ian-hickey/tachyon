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
package lucee.runtime.schedule

import java.io.File

/**
 *
 */
class StorageUtil {
    /**
     * create xml file from a resource definition
     *
     * @param file
     * @param resourcePath
     * @throws IOException
     */
    @Throws(IOException::class)
    fun loadFile(file: File?, resourcePath: String?) {
        loadFile(ResourceUtil.toResource(file), resourcePath)
    }

    /**
     * create xml file from a resource definition
     *
     * @param res
     * @param resourcePath
     * @throws IOException
     */
    @Throws(IOException::class)
    fun loadFile(res: Resource?, resourcePath: String?) {
        res.createFile(true)
        val `is`: InputStream = InfoImpl::class.java.getResourceAsStream(resourcePath)
        IOUtil.copy(`is`, res, true)
    }

    /**
     * reads a XML Element Attribute ans cast it to a String
     *
     * @param el XML Element to read Attribute from it
     * @param attributeName Name of the Attribute to read
     * @return Attribute Value
     */
    fun toString(data: Struct?, name: String?): String? {
        return Caster.toString(data.get(name, null), "")
    }

    /**
     * reads a XML Element Attribute ans cast it to a File
     *
     * @param el XML Element to read Attribute from it
     * @param attributeName Name of the Attribute to read
     * @return Attribute Value
     */
    fun toResource(config: Config?, el: Struct?, attributeName: String?): Resource? {
        val attributeValue: String = Caster.toString(el.get(attributeName, null), null)
        return if (attributeValue == null || attributeValue.trim().length() === 0) null else config.getResource(attributeValue)
    }

    /**
     * reads a XML Element Attribute ans cast it to a boolean value
     *
     * @param el XML Element to read Attribute from it
     * @param attributeName Name of the Attribute to read
     * @return Attribute Value
     */
    fun toBoolean(data: Struct?, name: String?): Boolean {
        return Caster.toBooleanValue(data.get(name, null), false)
    }

    /**
     * reads a XML Element Attribute ans cast it to a boolean value
     *
     * @param el XML Element to read Attribute from it
     * @param attributeName Name of the Attribute to read
     * @param defaultValue if attribute doesn't exist return default value
     * @return Attribute Value
     */
    fun toBoolean(el: Struct?, attributeName: String?, defaultValue: Boolean): Boolean {
        val value: String = Caster.toString(el.get(attributeName, null), null) ?: return defaultValue
        return Caster.toBooleanValue(value, false)
    }

    /**
     * reads a XML Element Attribute ans cast it to an int value
     *
     * @param el XML Element to read Attribute from it
     * @param attributeName Name of the Attribute to read
     * @return Attribute Value
     */
    fun toInt(el: Struct?, attributeName: String?): Int {
        return Caster.toIntValue(el.get(attributeName, null), Integer.MIN_VALUE)
    }

    fun toLong(data: Struct?, name: String?): Long {
        return Caster.toLongValue(data.get(name, null), Long.MIN_VALUE)
    }

    /**
     * reads a XML Element Attribute ans cast it to an int value
     *
     * @param el XML Element to read Attribute from it
     * @param attributeName Name of the Attribute to read
     * @param defaultValue if attribute doesn't exist return default value
     * @return Attribute Value
     */
    fun toInt(el: Struct?, attributeName: String?, defaultValue: Int): Int {
        val value: String = Caster.toString(el.get(attributeName, null), null) ?: return defaultValue
        val intValue: Int = Caster.toIntValue(value, Integer.MIN_VALUE)
        return if (intValue == Integer.MIN_VALUE) defaultValue else intValue
    }

    /**
     * reads a XML Element Attribute ans cast it to a DateTime Object
     *
     * @param config
     * @param el XML Element to read Attribute from it
     * @param attributeName Name of the Attribute to read
     * @return Attribute Value
     */
    fun toDateTime(config: Config?, el: Struct?, attributeName: String?): DateTime? {
        val str: String = Caster.toString(el.get(attributeName, null), null) ?: return null
        return DateCaster.toDateAdvanced(str, ThreadLocalPageContext.getTimeZone(config), null)
    }

    /**
     * reads a XML Element Attribute ans cast it to a DateTime
     *
     * @param el XML Element to read Attribute from it
     * @param attributeName Name of the Attribute to read
     * @param defaultValue if attribute doesn't exist return default value
     * @return Attribute Value
     */
    fun toDateTime(el: Struct?, attributeName: String?, defaultValue: DateTime?): DateTime? {
        val value: String = Caster.toString(el.get(attributeName, null), null) ?: return defaultValue
        return Caster.toDate(value, false, null, null) ?: return defaultValue
    }

    /**
     * reads a XML Element Attribute ans cast it to a Date Object
     *
     * @param el XML Element to read Attribute from it
     * @param attributeName Name of the Attribute to read
     * @return Attribute Value
     */
    fun toDate(config: Config?, el: Struct?, attributeName: String?): Date? {
        val dt: DateTime = toDateTime(config, el, attributeName) ?: return null
        return DateImpl(dt)
    }

    /**
     * reads a XML Element Attribute ans cast it to a Date
     *
     * @param el XML Element to read Attribute from it
     * @param attributeName Name of the Attribute to read
     * @param defaultValue if attribute doesn't exist return default value
     * @return Attribute Value
     */
    fun toDate(el: Struct?, attributeName: String?, defaultValue: Date?): Date? {
        return DateImpl(toDateTime(el, attributeName, defaultValue))
    }

    /**
     * reads a XML Element Attribute ans cast it to a Time Object
     *
     * @param config
     * @param el XML Element to read Attribute from it
     * @param attributeName Name of the Attribute to read
     * @return Attribute Value
     */
    fun toTime(config: Config?, el: Struct?, attributeName: String?): Time? {
        val dt: DateTime = toDateTime(config, el, attributeName) ?: return null
        return TimeImpl(dt)
    }

    /**
     * reads a XML Element Attribute ans cast it to a Date
     *
     * @param el XML Element to read Attribute from it
     * @param attributeName Name of the Attribute to read
     * @param defaultValue if attribute doesn't exist return default value
     * @return Attribute Value
     */
    fun toTime(el: Struct?, attributeName: String?, defaultValue: Time?): Time? {
        return TimeImpl(toDateTime(el, attributeName, defaultValue))
    }

    /**
     * reads 2 XML Element Attribute ans cast it to a Credential
     *
     * @param el XML Element to read Attribute from it
     * @param attributeUser Name of the user Attribute to read
     * @param attributePassword Name of the password Attribute to read
     * @return Attribute Value
     */
    fun toCredentials(el: Struct?, attributeUser: String?, attributePassword: String?): Credentials? {
        val user: String = Caster.toString(el.get(attributeUser, null), null)
        var pass: String = Caster.toString(el.get(attributePassword, null), null)
        if (user == null) return null
        if (pass == null) pass = ""
        return CredentialsImpl.toCredentials(user, pass)
    }

    /**
     * reads 2 XML Element Attribute ans cast it to a Credential
     *
     * @param el XML Element to read Attribute from it
     * @param attributeUser Name of the user Attribute to read
     * @param attributePassword Name of the password Attribute to read
     * @param defaultCredentials
     * @return Attribute Value
     */
    fun toCredentials(el: Struct?, attributeUser: String?, attributePassword: String?, defaultCredentials: Credentials?): Credentials? {
        val user: String = Caster.toString(el.get(attributeUser, null), null)
        var pass: String = Caster.toString(el.get(attributePassword, null), null)
        if (user == null) return defaultCredentials
        if (pass == null) pass = ""
        return CredentialsImpl.toCredentials(user, pass)
    }
}