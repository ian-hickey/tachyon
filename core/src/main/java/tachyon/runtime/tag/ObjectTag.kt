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

import tachyon.runtime.PageContext

/**
 * Lets you call methods in COM, CORBA, and JAVA objects.
 *
 *
 *
 */
class ObjectTag : TagImpl() {
    /*
	 * Component - name - component TODO support full functionality Component ------------- name
	 * component
	 * 
	 * Com ---------- type action class name context server
	 * 
	 * Corba -------------------- type context class name locale
	 * 
	 * Java -------------------- type action class name
	 * 
	 * Webservice --------------------------- webservice name
	 * 
	 * all ------------- name component type action class context server locale webservice
	 */
    private var name: String? = null
    private var component: String? = null
    private var type: String? = ""
    private var action: String? = null
    private var clazz: String? = null
    private var context: String? = null
    private var server: String? = null
    private var locale: String? = null
    private var webservice: String? = null
    private var delimiters: String? = ","
    private var username: String? = null
    private var password: String? = null
    private var proxyServer: String? = null
    private var proxyPort = 0
    private var proxyUser: String? = null
    private var proxyPassword: String? = null
    @Override
    fun release() {
        super.release()
        name = null
        component = null
        type = ""
        action = null
        clazz = null
        context = null
        server = null
        locale = null
        webservice = null
        delimiters = ","
        username = null
        password = null
        proxyServer = null
        proxyPort = -1
        proxyUser = null
        proxyPassword = null
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        if (component != null) {
            pageContext.setVariable(name, CreateObject.doComponent(pageContext, component))
        } else if (type!!.equals("java")) {
            checkAccess(pageContext, type)
            checkClass()
            pageContext.setVariable(name, CreateObject.doJava(pageContext, clazz, context, delimiters))
        } else if (type!!.equals("com")) {
            checkAccess(pageContext, type)
            checkClass()
            pageContext.setVariable(name, CreateObject.doCOM(pageContext, clazz))
        } else if (type!!.equals("webservice")) {
            checkAccess(pageContext, type)
            checkWebservice()
            var proxy: ProxyData? = null
            if (proxyServer != null) {
                proxy = ProxyDataImpl(proxyServer, proxyPort, proxyUser, proxyPassword)
            }
            pageContext.setVariable(name, CreateObject.doWebService(pageContext, webservice, username, password, proxy))
        } else {
            if (type == null) throw ApplicationException("Too few attributes defined for tag [object]")
            throw ApplicationException("Wrong value for attribute [type]",
                    "types are [com,java,webservice,corba] and the only supported type (at the moment) are [webservice,com,component,java]")
        }
        return SKIP_BODY
    }

    /**
     * check if attribute class is defined
     *
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    private fun checkClass() {
        if (clazz == null) throw ApplicationException("Attribute [class] is required")
    }

    /**
     * check if attribute webservice is defined
     *
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    private fun checkWebservice() {
        if (webservice == null) throw ApplicationException("Attribute [webservice] is required")
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }

    /**
     * @param locale The locale to set.
     */
    fun setLocale(locale: String?) {
        this.locale = locale
    }

    /**
     * @param name The name to set.
     */
    fun setName(name: String?) {
        this.name = name
    }

    /**
     * @param server The server to set.
     */
    fun setServer(server: String?) {
        this.server = server
    }

    /**
     * @param type The type to set.
     */
    fun setType(type: String?) {
        this.type = type.toLowerCase().trim()
    }

    /**
     * @param webservice The webservice to set.
     */
    fun setWebservice(webservice: String?) {
        type = "webservice"
        this.webservice = webservice
    }

    /**
     * @param action The action to set.
     */
    fun setAction(action: String?) {
        this.action = action
    }

    /**
     * @param clazz The clazz to set.
     */
    fun setClass(clazz: String?) {
        this.clazz = clazz
    }

    /**
     * @param component The component to set.
     */
    fun setComponent(component: String?) {
        this.component = component
    }

    /**
     * @param context The context to set.
     */
    fun setContext(context: String?) {
        this.context = context
    }

    fun setPassword(password: String?) {
        this.password = password
    }

    fun setUsername(username: String?) {
        this.username = username
    }

    /**
     * @param proxyServer the proxyServer to set
     */
    fun setProxyServer(proxyServer: String?) {
        this.proxyServer = proxyServer
    }

    /**
     * @param proxyPort the proxyPort to set
     */
    fun setProxyPort(proxyPort: Double) {
        this.proxyPort = proxyPort.toInt()
    }

    /**
     * @param proxyUser the proxyUser to set
     */
    fun setProxyUser(proxyUser: String?) {
        this.proxyUser = proxyUser
    }

    /**
     * @param proxyPassword the proxyPassword to set
     */
    fun setProxyPassword(proxyPassword: String?) {
        this.proxyPassword = proxyPassword
    }

    /**
     * @param delimiters the delimiters to set
     */
    fun setDelimiters(delimiters: String?) {
        this.delimiters = delimiters
    }

    companion object {
        @Throws(SecurityException::class)
        private fun checkAccess(pc: PageContext?, type: String?) {
            if (pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_TAG_OBJECT) === SecurityManager.VALUE_NO) throw SecurityException("Cannot access tag [object] with type [$type]", "access is prohibited by security manager")
        }
    }
}