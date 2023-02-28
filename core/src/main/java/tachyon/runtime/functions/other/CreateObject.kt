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
/**
 * Implements the CFML Function createobject
 * FUTURE neue attr unterstuestzen
 */
package tachyon.runtime.functions.other

import tachyon.commons.lang.StringUtil

object CreateObject : Function {
    @Throws(PageException::class)
    fun call(pc: PageContext?, cfcName: String?): Object? {
        return call(pc, "component", cfcName, null, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, type: String?, className: String?): Object? {
        return call(pc, type, className, null, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, type: String?, className: String?, context: Object?): Object? {
        return call(pc, type, className, context, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, type: String?, className: String?, context: Object?, serverName: Object?): Object? {
        var type = type
        type = StringUtil.toLowerCase(type!!)

        // JAVA
        if (type.equals("java")) {
            checkAccess(pc, type)
            return doJava(pc, className, context, Caster.toString(serverName))
        }
        // COM
        if (type.equals("com")) {
            return doCOM(pc, className)
        }
        // Component
        if (type.equals("component") || type.equals("cfc")) {
            return doComponent(pc, className)
        }
        // Webservice
        if (type.equals("webservice") || type.equals("wsdl")) {
            var user: String? = null
            var pass: String? = null
            var proxy: ProxyDataImpl? = null
            if (context != null) {
                val args: Struct = if (serverName != null) Caster.toStruct(serverName) else Caster.toStruct(context)
                // basic security
                user = Caster.toString(args.get("username", null))
                pass = Caster.toString(args.get("password", null))

                // proxy
                val proxyServer: String = Caster.toString(args.get("proxyServer", null))
                val proxyPort: String = Caster.toString(args.get("proxyPort", null))
                var proxyUser: String = Caster.toString(args.get("proxyUser", null))
                if (StringUtil.isEmpty(proxyUser)) proxyUser = Caster.toString(args.get("proxyUsername", null))
                val proxyPassword: String = Caster.toString(args.get("proxyPassword", null))
                if (!StringUtil.isEmpty(proxyServer)) {
                    proxy = ProxyDataImpl(proxyServer, Caster.toIntValue(proxyPort, -1), proxyUser, proxyPassword)
                }
            }
            return doWebService(pc, className, user, pass, proxy)
        }
        if (type.equals("http")) {
            var user: String? = null
            var pass: String? = null
            var proxy: ProxyDataImpl? = null
            if (context != null) {
                val args: Struct = if (serverName != null) Caster.toStruct(serverName) else Caster.toStruct(context)
                // basic security
                user = Caster.toString(args.get("username", null))
                pass = Caster.toString(args.get("password", null))

                // proxy
                val proxyServer: String = Caster.toString(args.get("proxyServer", null))
                val proxyPort: String = Caster.toString(args.get("proxyPort", null))
                var proxyUser: String = Caster.toString(args.get("proxyUser", null))
                if (StringUtil.isEmpty(proxyUser)) proxyUser = Caster.toString(args.get("proxyUsername", null))
                val proxyPassword: String = Caster.toString(args.get("proxyPassword", null))
                if (!StringUtil.isEmpty(proxyServer)) {
                    proxy = ProxyDataImpl(proxyServer, Caster.toIntValue(proxyPort, -1), proxyUser, proxyPassword)
                }
            }
            return doHTTP(pc, className, user, pass, proxy)
        }
        // .net
        if (type.equals(".net") || type.equals("dotnet")) {
            return doDotNet(pc, className)
        }
        throw ExpressionException(
                "Invalid argument for function createObject, first argument (type), " + "must be (com, java, webservice or component) other types are not supported")
    }

    @Throws(FunctionNotSupported::class)
    private fun doDotNet(pc: PageContext?, className: String?): Object? {
        throw FunctionNotSupported("CreateObject", "type .net")
    }

    @Throws(SecurityException::class)
    private fun checkAccess(pc: PageContext?, type: String?) {
        if (pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_TAG_OBJECT) === SecurityManager.VALUE_NO) throw SecurityException("Can't access function [createObject] with type [$type]", "Access is denied by the Security Manager")
    }

    @Throws(PageException::class)
    fun doJava(pc: PageContext?, className: String?, pathsOrBundleName: Object?, delimiterOrBundleVersion: String?): Object? {
        return JavaProxy.call(pc, className, pathsOrBundleName, delimiterOrBundleVersion)
    }

    fun doCOM(pc: PageContext?, className: String?): Object? {
        return COMObject(className)
    }

    @Throws(PageException::class)
    fun doComponent(pc: PageContext?, className: String?): Component? {
        return pc.loadComponent(className)
    }

    @Throws(PageException::class)
    fun doWebService(pc: PageContext?, wsdlUrl: String?): Object? {
        // TODO CF8 impl. all new attributes for wsdl
        return (ThreadLocalPageContext.getConfig(pc) as ConfigWebPro).getWSHandler().getWSClient(wsdlUrl, null, null, null)
    }

    @Throws(PageException::class)
    fun doWebService(pc: PageContext?, wsdlUrl: String?, username: String?, password: String?, proxy: ProxyData?): Object? {
        // TODO CF8 impl. all new attributes for wsdl
        return (ThreadLocalPageContext.getConfig(pc) as ConfigWebPro).getWSHandler().getWSClient(wsdlUrl, username, password, proxy)
    }

    @Throws(PageException::class)
    fun doHTTP(pc: PageContext?, httpUrl: String?): Object? {
        return HTTPClient(httpUrl, null, null, null)
    }

    @Throws(PageException::class)
    fun doHTTP(pc: PageContext?, httpUrl: String?, username: String?, password: String?, proxy: ProxyData?): Object? {
        return HTTPClient(httpUrl, username, password, proxy)
    }
}