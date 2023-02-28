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
package tachyon.runtime.config

import java.net.MalformedURLException

object Constants {
    private val CFML_SCRIPT_EXTENSION: String? = "cfs"
    private val CFML_COMPONENT_EXTENSION: String? = "cfc"
    private val LUCEE_COMPONENT_EXTENSION: String? = "tachyon"
    val CFML_NAME: String? = "CFML"
    val LUCEE_NAME: String? = "Tachyon"
    val NAME: String? = "Tachyon"
    val CFML_ALIAS_NAMES: Array<String?>? = arrayOf("CFML", "CFM")
    val LUCEE_ALIAS_NAMES: Array<String?>? = arrayOf("Tachyon")
    val GATEWAY_COMPONENT_EXTENSION: String? = "cfc" // MUST remove
    private val CFML_TEMPLATE_MAIN_EXTENSION: String? = "cfm"
    private val LUCEE_TEMPLATE_MAIN_EXTENSION: String? = "tachyon"
    val CFML_MIMETYPES: Array<String?>? = arrayOf("text/cfml", "application/cfml")
    val LUCEE_MIMETYPES: Array<String?>? = arrayOf("text/tachyon", "application/tachyon")
    val DTDS_FLD: Array<String?>? = arrayOf("-//Tachyon//DTD CFML Function Library 1.0//EN", "-//Railo//DTD CFML Function Library 1.0//EN")
    val DTDS_TLD: Array<String?>? = arrayOf("-//Tachyon//DTD CFML Tag Library 1.0//EN", "-//Railo//DTD CFML Tag Library 1.0//EN")
    val CFML_APPLICATION_EVENT_HANDLER: String? = "Application." + CFML_COMPONENT_EXTENSION
    val LUCEE_APPLICATION_EVENT_HANDLER: String? = "Application." + LUCEE_COMPONENT_EXTENSION
    val CFML_CLASSIC_APPLICATION_EVENT_HANDLER: String? = "Application." + CFML_TEMPLATE_MAIN_EXTENSION
    val CFML_CLASSIC_APPLICATION_END_EVENT_HANDLER: String? = "OnRequestEnd." + CFML_TEMPLATE_MAIN_EXTENSION
    val CFML_APPLICATION_TAG_NAME: String? = "cfapplication"
    val LUCEE_APPLICATION_TAG_NAME: String? = "application"
    val DEFAULT_PACKAGE: String? = "org.tachyon.cfml"
    val WEBSERVICE_NAMESPACE_URI: String? = "http://rpc.xml.coldfusion"
    var DEFAULT_UPDATE_URL: URL? = null
    val RH_EXTENSION_PROVIDERS: Array<RHExtensionProvider?>? = arrayOf<RHExtensionProvider?>(
            RHExtensionProvider(HTTPUtil.toURL("https://extension.tachyon.org", HTTPUtil.ENCODED_NO, null), true),
            RHExtensionProvider(HTTPUtil.toURL("https://www.forgebox.io", HTTPUtil.ENCODED_NO, null), true))
    val CFML_SCRIPT_TAG_NAME: String? = "script"
    val LUCEE_SCRIPT_TAG_NAME: String? = "script"
    val CFML_SET_TAG_NAME: String? = "set"
    val LUCEE_SET_TAG_NAME: String? = "set"
    val CFML_COMPONENT_TAG_NAME: String? = "component"
    val LUCEE_COMPONENT_TAG_NAME: String? = "class"
    val LUCEE_INTERFACE_TAG_NAME: String? = "interface"
    val CFML_CLASS_SUFFIX: String? = "\$cf"
    val LUCEE_CLASS_SUFFIX: String? = "\$lu"

    // TODO load this based on the servlet mapping
    val cte: Array<String?>? = arrayOf(CFML_TEMPLATE_MAIN_EXTENSION)
    fun getCFMLTemplateExtensions(): Array<String?>? {
        return cte
    }

    val lte: Array<String?>? = arrayOf(LUCEE_TEMPLATE_MAIN_EXTENSION)
    fun getTachyonTemplateExtensions(): Array<String?>? {
        return lte
    }

    fun getCFMLScriptExtension(): String? {
        return CFML_SCRIPT_EXTENSION
    }

    fun getCFMLComponentExtension(): String? {
        return CFML_COMPONENT_EXTENSION
    }

    fun getTachyonComponentExtension(): String? {
        return LUCEE_COMPONENT_EXTENSION
    }

    // merge methods above
    fun getScriptExtensions(): Array<String?>? {
        return arrayOf(getCFMLScriptExtension())
    }

    fun getTemplateExtensions(): Array<String?>? {
        return ArrayUtil.toArray(getCFMLTemplateExtensions(), getTachyonTemplateExtensions())
    }

    fun getComponentExtensions(): Array<String?>? {
        return arrayOf(getCFMLComponentExtension(), getTachyonComponentExtension())
    }

    fun getCFMLExtensions(): Array<String?>? {
        return ArrayUtil.toArray(getCFMLTemplateExtensions(), getCFMLComponentExtension())
    }

    fun getTachyonExtensions(): Array<String?>? {
        return ArrayUtil.toArray(getTachyonTemplateExtensions(), getTachyonComponentExtension())
    }

    fun getExtensions(): Array<String?>? {
        return ArrayUtil.toArray(getComponentExtensions(), getTemplateExtensions(), getScriptExtensions())
    }

    fun isCFMLComponentExtension(extension: String?): Boolean {
        var extension = extension
        if (StringUtil.isEmpty(extension)) return false
        if (extension.startsWith(".")) extension = extension.substring(1)
        return getCFMLComponentExtension().trim().equalsIgnoreCase(extension)
    }

    fun isCFMLScriptExtension(extension: String?): Boolean {
        var extension = extension
        if (StringUtil.isEmpty(extension)) return false
        if (extension.startsWith(".")) extension = extension.substring(1)
        return getCFMLScriptExtension().trim().equalsIgnoreCase(extension)
    }

    fun isTachyonComponentExtension(extension: String?): Boolean {
        var extension = extension
        if (StringUtil.isEmpty(extension)) return false
        if (extension.startsWith(".")) extension = extension.substring(1)
        return getTachyonComponentExtension().trim().equalsIgnoreCase(extension)
    }

    fun isComponentExtension(extension: String?): Boolean {
        var extension = extension
        if (StringUtil.isEmpty(extension)) return false
        if (extension.startsWith(".")) extension = extension.substring(1)
        return getCFMLComponentExtension().trim().equalsIgnoreCase(extension) || getTachyonComponentExtension().trim().equalsIgnoreCase(extension)
    }

    init {
        try {
            DEFAULT_UPDATE_URL = URL("https://update.tachyon.org")
        } catch (e: MalformedURLException) {
        }
    }
}