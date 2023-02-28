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
package lucee.runtime.config

import java.net.MalformedURLException

object Constants {
    private val CFML_SCRIPT_EXTENSION: String? = "cfs"
    private val CFML_COMPONENT_EXTENSION: String? = "cfc"
    private val LUCEE_COMPONENT_EXTENSION: String? = "lucee"
    val CFML_NAME: String? = "CFML"
    val LUCEE_NAME: String? = "Lucee"
    val NAME: String? = "Lucee"
    val CFML_ALIAS_NAMES: Array<String?>? = arrayOf("CFML", "CFM")
    val LUCEE_ALIAS_NAMES: Array<String?>? = arrayOf("Lucee")
    val GATEWAY_COMPONENT_EXTENSION: String? = "cfc" // MUST remove
    private val CFML_TEMPLATE_MAIN_EXTENSION: String? = "cfm"
    private val LUCEE_TEMPLATE_MAIN_EXTENSION: String? = "lucee"
    val CFML_MIMETYPES: Array<String?>? = arrayOf("text/cfml", "application/cfml")
    val LUCEE_MIMETYPES: Array<String?>? = arrayOf("text/lucee", "application/lucee")
    val DTDS_FLD: Array<String?>? = arrayOf("-//Lucee//DTD CFML Function Library 1.0//EN", "-//Railo//DTD CFML Function Library 1.0//EN")
    val DTDS_TLD: Array<String?>? = arrayOf("-//Lucee//DTD CFML Tag Library 1.0//EN", "-//Railo//DTD CFML Tag Library 1.0//EN")
    val CFML_APPLICATION_EVENT_HANDLER: String? = "Application." + CFML_COMPONENT_EXTENSION
    val LUCEE_APPLICATION_EVENT_HANDLER: String? = "Application." + LUCEE_COMPONENT_EXTENSION
    val CFML_CLASSIC_APPLICATION_EVENT_HANDLER: String? = "Application." + CFML_TEMPLATE_MAIN_EXTENSION
    val CFML_CLASSIC_APPLICATION_END_EVENT_HANDLER: String? = "OnRequestEnd." + CFML_TEMPLATE_MAIN_EXTENSION
    val CFML_APPLICATION_TAG_NAME: String? = "cfapplication"
    val LUCEE_APPLICATION_TAG_NAME: String? = "application"
    val DEFAULT_PACKAGE: String? = "org.lucee.cfml"
    val WEBSERVICE_NAMESPACE_URI: String? = "http://rpc.xml.coldfusion"
    var DEFAULT_UPDATE_URL: URL? = null
    val RH_EXTENSION_PROVIDERS: Array<RHExtensionProvider?>? = arrayOf<RHExtensionProvider?>(
            RHExtensionProvider(HTTPUtil.toURL("https://extension.lucee.org", HTTPUtil.ENCODED_NO, null), true),
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
    fun getLuceeTemplateExtensions(): Array<String?>? {
        return lte
    }

    fun getCFMLScriptExtension(): String? {
        return CFML_SCRIPT_EXTENSION
    }

    fun getCFMLComponentExtension(): String? {
        return CFML_COMPONENT_EXTENSION
    }

    fun getLuceeComponentExtension(): String? {
        return LUCEE_COMPONENT_EXTENSION
    }

    // merge methods above
    fun getScriptExtensions(): Array<String?>? {
        return arrayOf(getCFMLScriptExtension())
    }

    fun getTemplateExtensions(): Array<String?>? {
        return ArrayUtil.toArray(getCFMLTemplateExtensions(), getLuceeTemplateExtensions())
    }

    fun getComponentExtensions(): Array<String?>? {
        return arrayOf(getCFMLComponentExtension(), getLuceeComponentExtension())
    }

    fun getCFMLExtensions(): Array<String?>? {
        return ArrayUtil.toArray(getCFMLTemplateExtensions(), getCFMLComponentExtension())
    }

    fun getLuceeExtensions(): Array<String?>? {
        return ArrayUtil.toArray(getLuceeTemplateExtensions(), getLuceeComponentExtension())
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

    fun isLuceeComponentExtension(extension: String?): Boolean {
        var extension = extension
        if (StringUtil.isEmpty(extension)) return false
        if (extension.startsWith(".")) extension = extension.substring(1)
        return getLuceeComponentExtension().trim().equalsIgnoreCase(extension)
    }

    fun isComponentExtension(extension: String?): Boolean {
        var extension = extension
        if (StringUtil.isEmpty(extension)) return false
        if (extension.startsWith(".")) extension = extension.substring(1)
        return getCFMLComponentExtension().trim().equalsIgnoreCase(extension) || getLuceeComponentExtension().trim().equalsIgnoreCase(extension)
    }

    init {
        try {
            DEFAULT_UPDATE_URL = URL("https://update.lucee.org")
        } catch (e: MalformedURLException) {
        }
    }
}