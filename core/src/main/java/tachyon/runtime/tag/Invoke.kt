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
package tachyon.runtime.tag

import tachyon.commons.lang.StringUtil

/*
 * FUTURE tag invoke
 * Attributes: servicePort,timeout
 * */
/**
 * Invokes component methods from within a page or component. You use this tag to reference a WSDL
 * file and consume a web service from within a block of CFML code.
 *
 *
 *
 */
class Invoke : BodyTagImpl(), DynamicAttributes {
    private val data: Struct? = StructImpl(StructImpl.TYPE_LINKED)

    // private Map attributes = new HashTable();
    // private HashSet keys = new HashSet();
    private var hasBody = false
    private var component: Object? = null
    private var method: String? = null
    private var returnvariable: String? = null
    private var username: String? = null
    private var password: String? = null
    private var webservice: String? = null
    private var timeout = -1
    private var serviceport: String? = null
    private val proxy: ProxyData? = ProxyDataImpl()
    @Override
    fun release() {
        super.release()
        data.clear()
        component = null
        method = null
        returnvariable = null
        username = null
        password = null
        webservice = null
        timeout = -1
        serviceport = null
        proxy.release()
    }

    /**
     * @param component the component to set
     */
    fun setComponent(component: Object?) {
        this.component = component
    }

    /**
     * @param method the method to set
     */
    fun setMethod(method: String?) {
        this.method = method
    }

    /**
     * @param password the password to set
     */
    fun setPassword(password: String?) {
        this.password = password
    }

    /**
     * @param proxyserver the proxyserver to set
     */
    fun setProxyserver(proxyserver: String?) {
        proxy.setServer(proxyserver)
    }

    /**
     * @param proxyport the proxyport to set
     */
    fun setProxyport(proxyport: Double) {
        proxy.setPort(proxyport.toInt())
    }

    /**
     * @param proxyuser the proxyuser to set
     */
    fun setProxyuser(proxyuser: String?) {
        proxy.setUsername(proxyuser)
    }

    /**
     * @param proxypassword the proxypassword to set
     */
    fun setProxypassword(proxypassword: String?) {
        proxy.setPassword(proxypassword)
    }

    /**
     * @param returnvariable the returnvariable to set
     */
    fun setReturnvariable(returnvariable: String?) {
        this.returnvariable = returnvariable.trim()
    }

    /**
     * @param serviceport the serviceport to set
     */
    fun setServiceport(serviceport: String?) {
        this.serviceport = serviceport
    }

    /**
     * @param timeout the timeout to set
     */
    fun setTimeout(timeout: Double) {
        this.timeout = timeout.toInt()
    }

    /**
     * @param username the username to set
     */
    fun setUsername(username: String?) {
        this.username = username
    }

    /**
     * @param webservice the webservice to set
     */
    fun setWebservice(webservice: String?) {
        this.webservice = webservice.trim()
    }

    @Override
    fun setDynamicAttribute(uri: String?, localName: String?, value: Object?) {
        setDynamicAttribute(uri, KeyImpl.init(localName), value)
    }

    @Override
    fun setDynamicAttribute(uri: String?, localName: tachyon.runtime.type.Collection.Key?, value: Object?) {
        data.setEL(localName, value)
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        return EVAL_BODY_INCLUDE
    }

    @Override
    @Throws(PageException::class)
    fun doEndTag(): Int {
        // CFC
        if (component != null) {
            doComponent(component)
        } else if (!StringUtil.isEmpty(webservice)) {
            doWebService(webservice)
        } else {
            doFunction(pageContext)
        }
        return EVAL_PAGE
    }

    /**
     * @param oComponent
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doComponent(oComponent: Object?) {
        if (StringUtil.isEmpty(method, true)) throw ApplicationException("Attribute [method] for tag [invoke] is required.")
        var component: tachyon.runtime.Component? = null
        component = if (oComponent is tachyon.runtime.Component) oComponent as tachyon.runtime.Component? else pageContext.loadComponent(Caster.toString(oComponent))

        // execute
        val rtn: Object = component.callWithNamedValues(pageContext, method, data)

        // return
        if (!StringUtil.isEmpty(returnvariable)) pageContext.setVariable(returnvariable, rtn)
    }

    @Throws(PageException::class)
    private fun doFunction(pc: PageContext?) {

        // execute
        if (StringUtil.isEmpty(method, true)) throw ApplicationException("Attribute [method] for tag [invoke] is required.")
        val oUDF: Object = pc.getVariable(method) as? UDF
                ?: throw ApplicationException("there is no function with name $method")
        val rtn: Object = (oUDF as UDF).callWithNamedValues(pageContext, data, false)

        // return
        if (!StringUtil.isEmpty(returnvariable)) pageContext.setVariable(returnvariable, rtn)
    }

    /**
     * @param webservice
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doWebService(webservice: String?) {
        if (username != null) {
            if (password == null) password = ""
        }
        if (StringUtil.isEmpty(method, true)) throw ApplicationException("Attribute [method] for tag [invoke] is required.")
        val pd: ProxyData? = if (StringUtil.isEmpty(proxy.getServer())) null else proxy
        val ws: WSClient = if (username != null) (ThreadLocalPageContext.getConfig() as ConfigWebPro).getWSHandler().getWSClient(webservice, username, password, pd) else (ThreadLocalPageContext.getConfig() as ConfigWebPro).getWSHandler().getWSClient(webservice, null, null, pd)
        val rtn: Object = ws.callWithNamedValues(pageContext, KeyImpl.init(method), data)

        // return
        if (!StringUtil.isEmpty(returnvariable)) pageContext.setVariable(returnvariable, rtn)

        // throw new ApplicationException("type webservice is not yet implemented for tag invoke");
    }

    /**
     * @param name
     * @param value
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setArgument(name: String?, value: Object?) {
        data.set(name, value)
    }

    /**
     * sets if taf has a body or not
     *
     * @param hasBody
     */
    fun hasBody(hasBody: Boolean) {
        this.hasBody = hasBody
    }
}