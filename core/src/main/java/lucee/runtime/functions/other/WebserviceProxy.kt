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
/**
 * Implements the CFML Function createobject
 * FUTURE neue attr unterstuestzen
 */
package lucee.runtime.functions.other

import lucee.commons.lang.StringUtil

object WebserviceProxy : Function {
    private const val serialVersionUID = -5702516737227809987L
    private val EMPTY: Data? = Data(null, null, null)
    @Throws(PageException::class)
    fun call(pc: PageContext?, wsdlUrl: String?): Object? {
        return call(pc, wsdlUrl, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, wsdlUrl: String?, args: Struct?): Object? {
        checkAccess(pc)
        // MUST terminate webservice type smarter
        val data = readArgs(args)

        // Soap/WSDL
        return if (StringUtil.indexOfIgnoreCase(wsdlUrl!!, "?wsdl") !== -1) {
            doWebService(pc, wsdlUrl, data!!.user, data.pass, data.proxy)
        } else doHTTP(pc, wsdlUrl, data!!.user, data.pass, data.proxy)
        // HTTP
    }

    @Throws(SecurityException::class)
    private fun checkAccess(pc: PageContext?) {
        if (pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_TAG_OBJECT) === SecurityManager.VALUE_NO) throw SecurityException("Can't access function [webserviceProxy]", "Access is denied by the Security Manager")
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

    @Throws(PageException::class)
    private fun readArgs(args: Struct?): Data? {
        if (args != null) {
            // basic security
            var proxy: ProxyDataImpl? = null
            val user: String = Caster.toString(args.get("username", null))
            val pass: String = Caster.toString(args.get("password", null))

            // proxy
            val proxyServer: String = Caster.toString(args.get("proxyServer", null))
            val proxyPort: String = Caster.toString(args.get("proxyPort", null))
            var proxyUser: String = Caster.toString(args.get("proxyUser", null))
            if (StringUtil.isEmpty(proxyUser)) proxyUser = Caster.toString(args.get("proxyUsername", null))
            val proxyPassword: String = Caster.toString(args.get("proxyPassword", null))
            if (!StringUtil.isEmpty(proxyServer)) {
                proxy = ProxyDataImpl(proxyServer, Caster.toIntValue(proxyPort, -1), proxyUser, proxyPassword)
            }
            return Data(user, pass, proxy)
        }
        return EMPTY
    }

    internal class Data(user: String?, pass: String?, proxy: ProxyDataImpl?) {
        var user: String? = null
        var pass: String? = null
        var proxy: ProxyDataImpl? = null

        init {
            this.user = user
            this.pass = pass
            this.proxy = proxy
        }
    }
}