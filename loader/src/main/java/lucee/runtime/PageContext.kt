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
package lucee.runtime

import java.io.IOException

/**
 * page context for every page object. the PageContext is a jsp page context expanded by CFML
 * functionality. for example you have the method getSession to get jsp compatible session object
 * (HTTPSession) and with sessionScope() you get CFML compatible session object (Struct,Scope).
 */
abstract class PageContext : javax.servlet.jsp.PageContext() {
    /**
     * returns matching scope
     *
     * @return scope matching to defined scope definition
     * @param type type of scope (Scope.xx)
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun scope(type: Int): Scope?

    /**
     * @return undefined scope, undefined scope is a placeholder for the scopecascading
     */
    abstract fun undefinedScope(): Undefined?

    /**
     * @return variables scope
     */
    abstract fun variablesScope(): Variables?

    /**
     * @return url scope
     */
    abstract fun urlScope(): URL?

    /**
     * @return form scope
     */
    abstract fun formScope(): Form?

    /**
     * @return scope mixed url and scope
     */
    abstract fun urlFormScope(): URLForm?

    /**
     * @return request scope
     */
    abstract fun requestScope(): Request?

    /**
     * @return request scope
     */
    abstract fun cgiScope(): CGI?

    /**
     * @return application scope
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun applicationScope(): Application?

    /**
     * @return arguments scope
     */
    abstract fun argumentsScope(): Argument?

    /**
     * return the argument scope
     *
     * @param bind indicate that the Argument Scope is bound for use outside of the udf
     * @return Argument Scope
     */
    abstract fun argumentsScope(bind: Boolean): Argument?

    /**
     * @return arguments scope
     */
    abstract fun localScope(): Local?
    abstract fun localScope(bind: Boolean): Local?
    @Throws(PageException::class)
    abstract fun localGet(): Object?
    @Throws(PageException::class)
    abstract fun localGet(bind: Boolean): Object?
    @Throws(PageException::class)
    abstract fun localTouch(): Object?
    @Throws(PageException::class)
    abstract fun localTouch(bind: Boolean): Object?

    /**
     * @return session scope
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun sessionScope(): Session?
    abstract fun setFunctionScopes(local: Local?, argument: Argument?)

    /**
     * @return server scope
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun serverScope(): Server?

    /**
     * @return cookie scope
     */
    abstract fun cookieScope(): Cookie?

    /**
     * @return cookie scope
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun clientScope(): Client?
    abstract fun clientScopeEL(): Client?

    /**
     * @return cluster scope
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun clusterScope(): Cluster?

    /**
     * cluster scope
     *
     * @param create return null when false and scope does not exist
     * @return cluster scope or null
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun clusterScope(create: Boolean): Cluster?

    /**
     * set property at a collection object
     *
     * @param coll Collection Object (Collection, HashMap aso.)
     * @param key key of the new value
     * @param value new Value
     * @return value setted
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract operator fun set(coll: Object?, key: Collection.Key?, value: Object?): Object?

    /**
     * touch a new property, if property doesn't existset a Struct, otherwise do nothing
     *
     * @param coll Collection Object
     * @param key key to touch
     * @return Property
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun touch(coll: Object?, key: Collection.Key?): Object?

    /**
     * same like getProperty but return a collection object (QueryColumn) if return object is a Query
     *
     * @param coll Collection Object
     * @param key key to touch
     * @return Property or QueryColumn
     * @throws PageException Page Exception
     */
    @Deprecated
    @Deprecated("""use instead
	              <code>{@link #getCollection(Object, lucee.runtime.type.Collection.Key, Object)}</code>""")
    @Throws(PageException::class)
    abstract fun getCollection(coll: Object?, key: String?): Object?

    /**
     * same like getProperty but return a collection object (QueryColumn) if return object is a Query
     *
     * @param coll Collection Object
     * @param key key to touch
     * @return Property or QueryColumn
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun getCollection(coll: Object?, key: Collection.Key?): Object?

    /**
     * same like getProperty but return a collection object (QueryColumn) if return object is a Query
     *
     * @param coll Collection Object
     * @param key key to touch
     * @param defaultValue default value
     * @return Property or QueryColumn
     */
    @Deprecated
    @Deprecated("""use instead
	              <code>{@link #getCollection(Object, lucee.runtime.type.Collection.Key, Object)}</code>""")
    abstract fun getCollection(coll: Object?, key: String?, defaultValue: Object?): Object?

    /**
     * same like getProperty but return a collection object (QueryColumn) if return object is a Query
     *
     * @param coll Collection Object
     * @param key key to touch
     * @param defaultValue default value
     * @return Property or QueryColumn
     */
    abstract fun getCollection(coll: Object?, key: Collection.Key?, defaultValue: Object?): Object?

    /**
     *
     * @param coll Collection to get value
     * @param key key of the value
     * @return return value of a Collection, throws Exception if value doesn't exist
     * @throws PageException Page Exception
     */
    @Deprecated
    @Deprecated("use instead <code>{@link #get(Object, lucee.runtime.type.Collection.Key)}</code>")
    @Throws(PageException::class)
    abstract operator fun get(coll: Object?, key: String?): Object?

    /**
     *
     * @param coll Collection to get value
     * @param key key of the value
     * @return return value of a Collection, throws Exception if value doesn't exist
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract operator fun get(coll: Object?, key: Collection.Key?): Object?

    /**
     *
     * @param coll Collection to get value
     * @param key key of the value
     * @return return value of a Collection, throws Exception if value doesn't exist
     * @throws PageException Page Exception
     */
    @Deprecated
    @Deprecated("""use instead
	              <code>{@link #getReference(Object, lucee.runtime.type.Collection.Key)}</code>""")
    @Throws(PageException::class)
    abstract fun getReference(coll: Object?, key: String?): Reference?

    /**
     *
     * @param coll Collection to get value
     * @param key key of the value
     * @return return value of a Collection, throws Exception if value doesn't exist
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun getReference(coll: Object?, key: Collection.Key?): Reference?
    /*
	 * * get data from a scope
	 * 
	 * @param scope
	 * 
	 * @param key1
	 * 
	 * @param key2
	 * 
	 * @return
	 * 
	 * @throws PageException / public abstract Object get(Scope scope, String key1, String key2) throws
	 * PageException;
	 */
    /*
	 * * get data from a scope
	 * 
	 * @param scope
	 * 
	 * @param key1
	 * 
	 * @param key2
	 * 
	 * @param key3
	 * 
	 * @return
	 * 
	 * @throws PageException / public abstract Object get(Scope scope, String key1, String key2, String
	 * key3) throws PageException;
	 */
    /*
	 * * get data from a scope
	 * 
	 * @param scope
	 * 
	 * @param key1
	 * 
	 * @param key2
	 * 
	 * @param key3
	 * 
	 * @param key4
	 * 
	 * @return
	 * 
	 * @throws PageException / public abstract Object get(Scope scope, String key1, String key2, String
	 * key3, String key4) throws PageException;
	 */
    /*
	 * * get data from a scope
	 * 
	 * @param scope
	 * 
	 * @param key1
	 * 
	 * @param key2
	 * 
	 * @param key3
	 * 
	 * @param key4
	 * 
	 * @param key5
	 * 
	 * @return
	 * 
	 * @throws PageException / public abstract Object get(Scope scope, String key1, String key2, String
	 * key3, String key4, String key5) throws PageException;
	 */
    /*
	 * * get data from a scope
	 * 
	 * @param scope
	 * 
	 * @param key1
	 * 
	 * @param key2
	 * 
	 * @param key3
	 * 
	 * @param key4
	 * 
	 * @param key5
	 * 
	 * @param key6
	 * 
	 * @return
	 * 
	 * @throws PageException / public abstract Object get(Scope scope, String key1, String key2, String
	 * key3, String key4, String key5, String key6) throws PageException;
	 */
    /*
	 * * set data from a scope
	 * 
	 * @param scope
	 * 
	 * @param key1
	 * 
	 * @param key2
	 * 
	 * @return
	 * 
	 * @throws PageException / public abstract Object set(Scope scope, String key1, String key2, Object
	 * value) throws PageException;
	 */
    /*
	 * * set data from a scope
	 * 
	 * @param scope
	 * 
	 * @param key1
	 * 
	 * @param key2
	 * 
	 * @param key3
	 * 
	 * @return
	 * 
	 * @throws PageException / public abstract Object set(Scope scope, String key1, String key2, String
	 * key3, Object value) throws PageException;
	 */
    /*
	 * * set data from a scope
	 * 
	 * @param scope
	 * 
	 * @param key1
	 * 
	 * @param key2
	 * 
	 * @param key3
	 * 
	 * @param key4
	 * 
	 * @return
	 * 
	 * @throws PageException / public abstract Object set(Scope scope, String key1, String key2, String
	 * key3, String key4, Object value) throws PageException;
	 */
    /*
	 * * set data from a scope
	 * 
	 * @param scope
	 * 
	 * @param key1
	 * 
	 * @param key2
	 * 
	 * @param key3
	 * 
	 * @param key4
	 * 
	 * @param key5
	 * 
	 * @return
	 * 
	 * @throws PageException / public abstract Object set(Scope scope, String key1, String key2, String
	 * key3, String key4, String key5, Object value) throws PageException;
	 */
    /*
	 * * set data from a scope
	 * 
	 * @param scope
	 * 
	 * @param key1
	 * 
	 * @param key2
	 * 
	 * @param key3
	 * 
	 * @param key4
	 * 
	 * @param key5
	 * 
	 * @param key6
	 * 
	 * @return
	 * 
	 * @throws PageException / public abstract Object set(Scope scope, String key1, String key2, String
	 * key3, String key4, String key5, String key6, Object value) throws PageException;
	 */
    /**
     *
     * @param coll Collection to get value
     * @param key key of the value
     * @param defaultValue default value
     * @return return value of a Collection, return null if value not exist
     */
    @Deprecated
    @Deprecated("""use instead
	              <code>{@link #get(Object, lucee.runtime.type.Collection.Key, Object)}</code>""")
    abstract operator fun get(coll: Object?, key: String?, defaultValue: Object?): Object?

    /**
     *
     * @param coll Collection to get value
     * @param key key of the value
     * @param defaultValue default value
     * @return return value of a Collection, return null if value not exist
     */
    abstract operator fun get(coll: Object?, key: Collection.Key?, defaultValue: Object?): Object?

    /**
     * sets a value by string syntax ("scopename.key.key" "url.name")
     *
     * @param var Variable String name to set
     * @param value value to set
     * @return setted value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun setVariable(`var`: String?, value: Object?): Object?

    /**
     *
     * @param var variable name to get
     * @return return a value by string syntax ("scopename.key.key" "url.name")
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun getVariable(`var`: String?): Object?

    /**
     * evaluate given expression
     *
     * @param expression expression to evaluate
     * @return return value generated by expression or null
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun evaluate(expression: String?): Object?
    @Throws(PageException::class)
    abstract fun serialize(expression: Object?): String?

    /**
     *
     * @param var variable name to get
     * @return return a value by string syntax ("scopename.key.key" "url.name")
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun removeVariable(`var`: String?): Object?

    /**
     * get variable from string definition and cast it to a Query Object
     *
     * @param key Variable Name to get
     * @return Query
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun getQuery(key: String?): Query?
    @Throws(PageException::class)
    abstract fun getQuery(value: Object?): Query?

    /**
     * write a value to the header of the response
     *
     * @param name name of the value to set
     * @param value value to set
     */
    abstract fun setHeader(name: String?, value: String?)

    /**
     * @return returns the cfid of the current user
     */
    abstract fun getCFID(): String?

    /**
     * @return returns the current cftoken of the user
     */
    abstract fun getCFToken(): String?

    /**
     * @return return the session id
     */
    abstract fun getJSessionId(): String?

    /**
     * @return returns the urltoken of the current user
     */
    abstract fun getURLToken(): String?

    /**
     * @return returns the page context id
     */
    abstract fun getId(): Int
    abstract fun getRootWriter(): JspWriter?

    /**
     * @return Returns the locale.
     */
    abstract fun getLocale(): Locale?

    /**
     * @param locale The locale to set
     */
    abstract fun setLocale(locale: Locale?)

    /**
     * @param strLocale The locale to set as String.
     * @throws PageException Page Exception
     */
    @Deprecated
    @Deprecated("use instead <code>{@link #setLocale(Locale)}</code>")
    @Throws(PageException::class)
    abstract fun setLocale(strLocale: String?)

    /**
     * @return Returns the Config Object of the PageContext.
     */
    abstract fun getConfig(): ConfigWeb?

    /**
     * return HttpServletRequest, getRequest only returns ServletRequest
     *
     * @return HttpServletRequest
     */
    abstract fun getHttpServletRequest(): HttpServletRequest?

    /**
     * return HttpServletResponse, getResponse only returns ServletResponse
     *
     * @return HttpServletResponse
     */
    abstract fun getHttpServletResponse(): HttpServletResponse?
    @Throws(IOException::class)
    abstract fun getResponseStream(): OutputStream?

    /**
     * returns the tag that is in use
     *
     * @return Returns the currentTag.
     */
    abstract fun getCurrentTag(): Tag?

    /**
     * @return Returns the applicationContext.
     */
    abstract fun getApplicationContext(): ApplicationContext?

    /**
     * Writes a String to the Response Buffer
     *
     * @param str string
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    abstract fun write(str: String?)

    /**
     * Writes a String to the Response Buffer,also when cfoutputonly is true and execution is outside of
     * a cfoutput
     *
     * @param str string
     * @throws IOException IO Exception
     */
    @Throws(IOException::class)
    abstract fun forceWrite(str: String?)

    /**
     * Writes a String to the Response Buffer,also when cfoutputonly is true and execution is outside of
     * a cfoutput
     *
     * @param o object
     * @throws IOException IO Exception
     * @throws PageException Page Exception
     */
    @Throws(IOException::class, PageException::class)
    abstract fun writePSQ(o: Object?)

    /**
     * @return the current template PageSource
     */
    @Deprecated
    @Deprecated("use instead {@link #getCurrentPageSource(PageSource)}")
    abstract fun getCurrentPageSource(): PageSource?

    /**
     * @param defaultValue default value
     * @return the current template PageSource
     */
    abstract fun getCurrentPageSource(defaultValue: PageSource?): PageSource?

    /**
     * @return the current template PageSource
     */
    abstract fun getCurrentTemplatePageSource(): PageSource?

    /**
     * @return base template file
     */
    abstract fun getBasePageSource(): PageSource?

    /**
     * sets the pagecontext silent
     *
     * @return return setting that was before
     */
    abstract fun setSilent(): Boolean

    /**
     * unsets the pagecontext silent
     *
     * @return return setting that was before
     */
    abstract fun unsetSilent(): Boolean

    /**
     * return debugger of the page Context
     *
     * @return debugger
     */
    abstract fun getDebugger(): Debugger?

    /**
     *
     * @return Returns the executionTime.
     */
    abstract fun getExecutionTime(): Long

    /**
     * @param executionTime The executionTime to set.
     */
    abstract fun setExecutionTime(executionTime: Long)

    /**
     * @return Returns the remoteUser.
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun getRemoteUser(): Credential?

    /**
     * clear the remote user
     */
    abstract fun clearRemoteUser()

    /**
     * @param remoteUser The remoteUser to set.
     */
    abstract fun setRemoteUser(remoteUser: Credential?)

    /**
     * array of current template stack
     *
     * @return array
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun getTemplatePath(): Array?

    /**
     * returns the current level, how deep is the page stack
     *
     * @return level
     */
    abstract fun getCurrentLevel(): Int

    /**
     * @return Returns the variableUtil.
     */
    abstract fun getVariableUtil(): VariableUtil?

    /**
     * @param applicationContext The applicationContext to set.
     */
    abstract fun setApplicationContext(applicationContext: ApplicationContext?)
    abstract fun toPageSource(res: Resource?, defaultValue: PageSource?): PageSource?

    /**
     * set another variable scope
     *
     * @param scope scope
     */
    abstract fun setVariablesScope(scope: Variables?)

    /**
     * includes a path from an absolute path
     *
     * @param source absolute path as file object
     * @param runOnce include only once per request
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun doInclude(source: Array<PageSource?>?, runOnce: Boolean)

    /**
     * includes a path from an absolute path
     *
     * @param source absolute path as file object
     * @throws PageException Page Exception
     */
    @Deprecated
    @Deprecated("""used <code> doInclude(String source, boolean runOnce)</code> instead. Still used by
	              extensions ...""")
    @Throws(PageException::class)
    abstract fun doInclude(source: String?)

    /**
     * includes a path from an absolute path
     *
     * @param source absolute path as file object
     * @param runOnce include only once per request
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun doInclude(source: String?, runOnce: Boolean)

    /**
     * clear the current output buffer
     */
    abstract fun clear()

    /**
     * @return return the request timeout for this pagecontext in milli seconds
     */
    abstract fun getRequestTimeout(): Long

    /**
     * @param requestTimeout The requestTimeout to set.
     */
    abstract fun setRequestTimeout(requestTimeout: Long)

    /**
     * sets state of cfoutput only
     *
     * @param boolEnablecfoutputonly enable cfoutput only
     */
    abstract fun setCFOutputOnly(boolEnablecfoutputonly: Boolean)

    /**
     * returns if single quotes will be preserved inside a query tag (psq=preserve single quote)
     *
     * @return preserve single quote
     */
    abstract fun getPsq(): Boolean

    /**
     * Close the response stream.
     */
    abstract fun close()

    /**
     * adds a PageSource
     *
     * @param ps page source
     * @param alsoInclude also include
     */
    abstract fun addPageSource(ps: PageSource?, alsoInclude: Boolean)

    /**
     * clear all catches
     */
    abstract fun clearCatch()

    /**
     * execute a request to the PageConext
     *
     * @param realPath path
     * @param throwException catch or throw exceptions
     * @param onlyTopLevel only check top level mappings for the matching realpath
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun execute(realPath: String?, throwException: Boolean, onlyTopLevel: Boolean)

    /**
     * execute a request to the PageConext form CFML
     *
     * @param realPath path
     * @param throwException catch or throw exceptions
     * @param onlyTopLevel only check top level mappings for the matching realpath
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun executeCFML(realPath: String?, throwException: Boolean, onlyTopLevel: Boolean)
    @Throws(PageException::class)
    abstract fun executeRest(realPath: String?, throwException: Boolean)

    /**
     * Flush Content of buffer to the response stream of the Socket.
     */
    abstract fun flush()

    /**
     * call a UDF Function and return "return value" of the function
     *
     * @param coll Collection of the UDF Function
     * @param key name of the function
     * @param args arguments to call the function
     * @return return value of the function
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun getFunction(coll: Object?, key: String?, args: Array<Object?>?): Object?

    /**
     * call a UDF Function and return "return value" of the function
     *
     * @param coll Collection of the UDF Function
     * @param key name of the function
     * @param args arguments to call the function
     * @return return value of the function
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun getFunction(coll: Object?, key: Collection.Key?, args: Array<Object?>?): Object?

    /**
     * call a UDF Function and return "return value" of the function
     *
     * @param coll Collection of the UDF Function
     * @param key name of the function
     * @param args arguments to call the function
     * @return return value of the function
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun getFunctionWithNamedValues(coll: Object?, key: String?, args: Array<Object?>?): Object?

    /**
     * call a UDF Function and return "return value" of the function
     *
     * @param coll Collection of the UDF Function
     * @param key name of the function
     * @param args arguments to call the function
     * @return return value of the function
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun getFunctionWithNamedValues(coll: Object?, key: Collection.Key?, args: Array<Object?>?): Object?

    /**
     * get variable from string definition and cast it to an Iterator Object
     *
     * @param key Variable Name to get
     * @return Iterator
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun getIterator(key: String?): Iterator?

    /**
     * @return directory of root template file
     */
    abstract fun getRootTemplateDirectory(): Resource?

    /**
     * @return Returns the startTime.
     */
    abstract fun getStartTime(): Long

    /**
     * @return Returns the thread.
     */
    abstract fun getThread(): Thread?

    /**
     * specialised method for handlePageException with argument Exception or Throwable
     *
     * @param pe Page Exception
     */
    abstract fun handlePageException(pe: PageException?)
    /*
	 * *
	 * 
	 * @param applicationFile
	 * 
	 * @throws ServletException
	 */
    // public abstract void includeOnRequestEnd(PageSource applicationFile) throws ServletException;
    /**
     * ends a cfoutput block
     */
    abstract fun outputEnd()

    /**
     * starts a cfoutput block
     */
    abstract fun outputStart()

    /**
     * remove the last PageSource
     *
     * @param alsoInclude also include
     */
    abstract fun removeLastPageSource(alsoInclude: Boolean)

    /**
     * sets an exception
     *
     * @param t throwable
     * @return PageException
     */
    abstract fun setCatch(t: Throwable?): PageException?
    abstract fun getCatch(): PageException?
    abstract fun setCatch(pe: PageException?)
    abstract fun setCatch(pe: PageException?, caught: Boolean, store: Boolean)
    abstract fun exeLogStart(position: Int, id: String?)
    abstract fun exeLogEnd(position: Int, id: String?)

    /**
     * sets state of cfoutput only
     *
     * @param enablecfoutputonly enable cfoutput flag
     */
    abstract fun setCFOutputOnly(enablecfoutputonly: Short)

    /**
     * sets the error page
     *
     * @param ep error page
     */
    abstract fun setErrorPage(ep: ErrorPage?)

    /**
     * sets if inside a query tag single quote will be preserved (preserve single quote)
     *
     * @param psq sets preserve single quote for query
     */
    abstract fun setPsq(psq: Boolean)

    /**
     * return thrown exception
     *
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun throwCatch()

    /**
     * @return undefined scope, undefined scope is a placeholder for the scopecascading
     */
    abstract fun us(): Undefined?

    /**
     * compile a CFML Template
     *
     * @param templatePath path
     * @throws PageException Page Exception
     */
    @Deprecated
    @Deprecated("use instead <code>compile(PageSource pageSource)</code>")
    @Throws(PageException::class)
    abstract fun compile(templatePath: String?)

    /**
     * compile a CFML Template
     *
     * @param pageSource page source
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun compile(pageSource: PageSource?)

    /**
     * init body of a tag
     *
     * @param bodyTag body tag
     * @param state state
     * @throws JspException JSP Exception
     */
    @Throws(JspException::class)
    abstract fun initBody(bodyTag: BodyTag?, state: Int)

    /**
     * release body of a tag
     *
     * @param bodyTag body tag
     * @param state state
     */
    abstract fun releaseBody(bodyTag: BodyTag?, state: Int)

    /**
     * @param type type
     * @param name name
     * @param defaultValue default value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun param(type: String?, name: String?, defaultValue: Object?)

    /**
     * @param type type
     * @param name name
     * @param defaultValue default value
     * @param maxLength max length
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun param(type: String?, name: String?, defaultValue: Object?, maxLength: Int)

    /**
     * @param type type
     * @param name name
     * @param defaultValue default value
     * @param pattern pattern
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun param(type: String?, name: String?, defaultValue: Object?, pattern: String?)

    /**
     * @param type type
     * @param name name
     * @param defaultValue default value
     * @param min min value
     * @param max max value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun param(type: String?, name: String?, defaultValue: Object?, min: Double, max: Double)

    // public abstract PageContext clonePageContext();
    // public abstract boolean isCFCRequest();
    abstract fun getDataSourceManager(): DataSourceManager?
    abstract fun getCFMLFactory(): CFMLFactory?
    abstract fun getParentPageContext(): PageContext?

    /**
     * @param name thread scope name
     * @return thread scope name
     */
    @Deprecated
    @Deprecated("use instead <code>setThreadScope(Collection.Key name,Threads t)</code>")
    abstract fun getThreadScope(name: String?): Threads?
    abstract fun getThreadScope(name: Collection.Key?): Threads?

    /**
     * set a thread to the context
     *
     * @param name thread scope name
     * @param t threads
     */
    @Deprecated
    @Deprecated("use instead <code>setThreadScope(Collection.Key name,Threads t)</code>")
    abstract fun setThreadScope(name: String?, t: Threads?)
    abstract fun setThreadScope(name: Collection.Key?, t: Threads?)

    /**
     * @return return an Array with names off all threads running.
     */
    abstract fun getThreadScopeNames(): Array<String?>?
    abstract fun hasFamily(): Boolean
    @Throws(PageException::class)
    abstract fun loadComponent(compPath: String?): Component?
    // public abstract void setActiveComponent(Component component);
    /**
     * @return Returns the active Component.
     */
    abstract fun getActiveComponent(): Component?
    abstract fun getActiveUDF(): UDF?
    abstract fun getTimeZone(): TimeZone?
    abstract fun setTimeZone(timeZone: TimeZone?)
    abstract fun getSessionType(): Short
    @Throws(PageException::class)
    abstract fun getDataSource(datasource: String?): DataSource?
    abstract fun getDataSource(datasource: String?, defaultValue: DataSource?): DataSource?
    abstract fun getResourceCharset(): Charset?
    abstract fun getWebCharset(): Charset?
    abstract fun getCachedWithin(type: Int): Object?

    /**
     *
     * @return get the dialect for the current template
     */
    abstract fun getCurrentTemplateDialect(): Int

    /**
     *
     * @return get the dialect for the current template
     */
    abstract fun getRequestDialect(): Int

    /**
     * @param create if set to true, lucee creates a session when not exist
     * @return ORM Session
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    abstract fun getORMSession(create: Boolean): ORMSession?
    abstract fun getRequestTimeoutException(): Throwable? // FUTURE deprecate

    /**
     * if set to true Lucee ignores all scope names and handles them as regular keys for the undefined
     * scope
     *
     * @return Ignore Scopes
     */
    abstract fun ignoreScopes(): Boolean
}