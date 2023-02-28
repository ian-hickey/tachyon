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
package lucee.runtime.tag

import java.io.IOException

/**
 * Speeds up page rendering when dynamic content does not have to be retrieved each time a user
 * accesses the page. To accomplish this, cfcache creates temporary files that contain the static
 * HTML returned from a CFML page. You can use cfcache for simple URLs and URLs that contain URL
 * parameters.
 *
 *
 *
 */
class Cache : BodyTagImpl() {
    /**   */
    private var directory: Resource? = null

    /**
     * Specifies the protocol used to create pages from cache. Either http:// or https://. The default
     * is http://.
     */
    private var protocol: String? = null

    /**   */
    private var expireurl: String? = null

    /**   */
    private var action = CACHE
    /** When required for basic authentication, a valid username.  */ // private String username;
    /** When required for basic authentication, a valid password.  */ // private String password;
    private var timespan: TimeSpan? = TIMESPAN_FAR_AWAY
    private var idletime: TimeSpan? = TIMESPAN_0

    /**   */
    private var port = -1
    private var now: DateTimeImpl? = null
    private var body: String? = null
    private var _id: String? = null
    private var id: Object? = null
    private var name: String? = null
    private var key: String? = null
    private var hasBody = false
    private var doCaching = false
    private var cacheItem: CacheItem? = null
    private var cachename: String? = null
    private var value: Object? = null
    private var throwOnError = false
    private var metadata: String? = null
    @Override
    fun release() {
        super.release()
        directory = null
        // username=null;
        // password=null;
        protocol = null
        expireurl = null
        action = CACHE
        port = -1
        timespan = TIMESPAN_FAR_AWAY
        idletime = TIMESPAN_0
        body = null
        hasBody = false
        id = null
        key = null
        body = null
        doCaching = false
        cacheItem = null
        name = null
        cachename = null
        throwOnError = false
        value = null
        metadata = null
    }

    /**
     * @param obj
     * @throws DeprecatedException
     */
    @Deprecated
    @Deprecated("""this attribute is deprecated and will ignored in this tag
	  """)
    @Throws(DeprecatedException::class)
    fun setTimeout(obj: Object?) {
        // DeprecatedUtil.tagAttribute(pageContext,"Cache","timeout");
    }

    /**
     * set the value directory
     *
     * @param directory value to set
     */
    @Throws(ExpressionException::class)
    fun setDirectory(directory: String?) {
        this.directory = ResourceUtil.toResourceExistingParent(pageContext, directory)
    }

    @Throws(ExpressionException::class)
    fun setCachedirectory(directory: String?) {
        setDirectory(directory)
    }

    /**
     * set the value protocol Specifies the protocol used to create pages from cache. Either http:// or
     * https://. The default is http://.
     *
     * @param protocol value to set
     */
    fun setProtocol(protocol: String?) {
        var protocol = protocol
        if (protocol.endsWith("://")) protocol = protocol.substring(0, protocol.indexOf("://"))
        this.protocol = protocol.toLowerCase()
    }
    /*
	 * private String getProtocol() { if(StringUtil.isEmpty(protocol)) { return pageContext.
	 * getHttpServletRequest().getScheme(); } return protocol; }
	 */
    /**
     * set the value expireurl
     *
     * @param expireurl value to set
     */
    fun setExpireurl(expireurl: String?) {
        this.expireurl = expireurl
    }

    /**
     * set the value action
     *
     * @param action value to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setAction(action: String?) {
        var action = action
        action = action.toLowerCase().trim()
        if (action.equals("get")) this.action = GET else if (action.equals("put")) this.action = PUT else if (action.equals("cache")) this.action = CACHE else if (action.equals("clientcache")) this.action = CACHE_CLIENT else if (action.equals("servercache")) this.action = CACHE_SERVER else if (action.equals("flush")) this.action = FLUSH else if (action.equals("optimal")) this.action = CACHE else if (action.equals("client-cache")) this.action = CACHE_CLIENT else if (action.equals("client_cache")) this.action = CACHE_CLIENT else if (action.equals("server-cache")) this.action = CACHE_SERVER else if (action.equals("server_cache")) this.action = CACHE_SERVER else if (action.equals("content")) this.action = CONTENT else if (action.equals("content_cache")) this.action = CONTENT else if (action.equals("contentcache")) this.action = CONTENT else if (action.equals("content-cache")) this.action = CONTENT else throw ApplicationException("invalid value for attribute action for tag cache [" + action + "], "
                + "valid actions are [get,put,cache, clientcache, servercache, flush, optimal, contentcache]")

        // get: get an object from the cache.
        // put: Add an object to the cache.
    }

    /**
     * set the value username When required for basic authentication, a valid username.
     *
     * @param username value to set
     */
    fun setUsername(username: String?) {
        // this.username=username;
    }

    /**
     * set the value password When required for basic authentication, a valid password.
     *
     * @param password value to set
     */
    fun setPassword(password: String?) {
        // this.password=password;
    }

    fun setKey(key: String?) {
        this.key = key
    }

    /**
     * set the value port
     *
     * @param port value to set
     */
    fun setPort(port: Double) {
        this.port = port.toInt()
    }

    fun getPort(): Int {
        return if (port <= 0) pageContext.getHttpServletRequest().getServerPort() else port
    }

    /**
     * @param timespan The timespan to set.
     * @throws PageException
     */
    fun setTimespan(timespan: TimeSpan?) {
        this.timespan = timespan
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        now = DateTimeImpl(pageContext.getConfig())
        if (action == CACHE && hasBody) action = CONTENT
        return try {
            if (action == CACHE) {
                doClientCache()
                doServerCache()
            } else if (action == CACHE_CLIENT) doClientCache() else if (action == CACHE_SERVER) doServerCache() else if (action == FLUSH) doFlush() else if (action == CONTENT) return doContentCache() else if (action == GET) doGet() else if (action == PUT) doPut()
            EVAL_PAGE
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    fun doInitBody() {
    }

    @Override
    fun doAfterBody(): Int {
        // print.out("doAfterBody");
        if (bodyContent != null) body = bodyContent.getString()
        return SKIP_BODY
    }

    @Override
    @Throws(PageException::class)
    fun doEndTag(): Int { // print.out("doEndTag"+doCaching+"-"+body);
        if (doCaching && body != null) {
            try {
                writeCacheResource(cacheItem, body)
                pageContext.write(body)
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
        }
        return EVAL_PAGE
    }

    private fun doClientCache() {
        pageContext.setHeader("Last-Modified", GetHttpTimeString.call(pageContext, now))
        if (timespan != null) {
            val expires: DateTime? = expiresDate
            pageContext.setHeader("Expires", GetHttpTimeString.call(pageContext, expires))
        }
    }

    @Throws(IOException::class, PageException::class)
    private fun doServerCache() {
        if (hasBody) hasBody = !StringUtil.isEmpty(body)

        // call via cfcache disable debugger output
        if (pageContext.getConfig().debug()) pageContext.getDebugger().setOutput(false)
        val rsp: HttpServletResponse = pageContext.getHttpServletResponse()

        // generate cache resource matching request object
        val ci: CacheItem? = generateCacheResource(null, false)

        // use cached resource
        if (ci.isValid(timespan)) { // if(isOK(cacheResource)){
            if (pageContext.getHttpServletResponse().isCommitted()) return
            var os: OutputStream? = null
            try {
                ci.writeTo(outputStream.also { os = it }, ReqRspUtil.getCharacterEncoding(pageContext, rsp).name())
                // IOUtil.copy(is=cacheResource.getInputStream(),os=getOutputStream(),false,false);
            } finally {
                IOUtil.flushEL(os)
                IOUtil.close(os)
                (pageContext as PageContextImpl?).getRootOut().setClosed(true)
            }
            throw Abort(Abort.SCOPE_REQUEST)
        }

        // call page again and
        // MetaData.getInstance(getDirectory()).add(ci.getName(), ci.getRaw());
        val pci: PageContextImpl? = pageContext as PageContextImpl?
        pci.getRootOut().doCache(ci)
    }

    /*
	 * private boolean isOK(Resource cacheResource) { return cacheResource.exists() &&
	 * (cacheResource.lastModified()+timespan.getMillis()>=System.currentTimeMillis()); }
	 */
    @Throws(IOException::class)
    private fun doContentCache(): Int {

        // file
        cacheItem = generateCacheResource(key, true)
        // use cache
        if (cacheItem.isValid(timespan)) {
            pageContext.write(cacheItem.getValue())
            doCaching = false
            return SKIP_BODY
        }
        doCaching = true
        return EVAL_BODY_BUFFERED
    }

    @Throws(PageException::class, IOException::class)
    private fun doGet() {
        required("cache", "id", id)
        required("cache", "name", name)
        val id: String = Caster.toString(id)
        if (metadata == null) {
            pageContext.setVariable(name, CacheGet.call(pageContext, id, throwOnError, cachename))
        } else {
            val cache: lucee.commons.io.cache.Cache = CacheUtil.getCache(pageContext, cachename, Config.CACHE_TYPE_OBJECT)
            val entry: CacheEntry = if (throwOnError) cache.getCacheEntry(CacheUtil.key(id)) else cache.getCacheEntry(CacheUtil.key(id), null)
            if (entry != null) {
                pageContext.setVariable(name, entry.getValue())
                pageContext.setVariable(metadata, entry.getCustomInfo())
            } else {
                pageContext.setVariable(metadata, StructImpl())
            }
        }
    }

    @Throws(PageException::class)
    private fun doPut() {
        required("cache", "id", id)
        required("cache", "value", value)
        var ts: TimeSpan? = timespan
        var it: TimeSpan? = idletime
        if (ts === TIMESPAN_FAR_AWAY) ts = TIMESPAN_0
        if (it === TIMESPAN_FAR_AWAY) it = TIMESPAN_0
        CachePut.call(pageContext, Caster.toString(id), value, ts, it, cachename)
    }

    @Throws(IOException::class, PageException::class)
    private fun doFlush() {
        if (id != null) {
            required("cache", "id", id)
            CacheRemove.call(pageContext, id, throwOnError, cachename)
        } else if (StringUtil.isEmpty(expireurl)) {
            CacheItem.flushAll(pageContext, directory, cachename)
        } else {
            CacheItem.flush(pageContext, directory, cachename, expireurl)

            // ResourceUtil.removeChildrenEL(getDirectory(),(ResourceNameFilter)new ExpireURLFilter(expireurl));
        }
    }

    @Throws(IOException::class)
    private fun generateCacheResource(key: String?, useId: Boolean): CacheItem? {
        return CacheItem.getInstance(pageContext, _id, key, useId, directory, cachename, timespan)
    }

    @Throws(IOException::class)
    private fun writeCacheResource(cacheItem: CacheItem?, result: String?) {
        cacheItem.store(result)
        // IOUtil.write(cacheItem.getResource(), result,"UTF-8", false);
        // MetaData.getInstance(cacheItem.getDirectory()).add(cacheItem.getName(), cacheItem.getRaw());
    }

    private val expiresDate: DateTime?
        private get() = DateTimeImpl(pageContext, expiresTime, false)
    private val expiresTime: Long
        private get() = now.getTime() + timespan.getMillis()

    @get:Throws(PageException::class, IOException::class)
    private val outputStream: OutputStream?
        private get() = try {
            (pageContext as PageContextImpl?).getResponseStream()
        } catch (ise: IllegalStateException) {
            throw TemplateException("content is already send to user, flush")
        }

    /**
     * sets if tag has a body or not
     *
     * @param hasBody
     */
    fun hasBody(hasBody: Boolean) {
        this.hasBody = hasBody
    }

    /**
     * @param id the id to set
     */
    fun set_id(_id: String?) {
        this._id = _id
    }

    fun setId(id: Object?) {
        this.id = id
    }

    fun setName(name: String?) {
        this.name = name
    }

    fun setCachename(cachename: String?) {
        this.cachename = cachename
    }

    /**
     * @param throwOnError the throwOnError to set
     */
    fun setThrowonerror(throwOnError: Boolean) {
        this.throwOnError = throwOnError
    }

    fun setValue(value: Object?) {
        this.value = value
    }

    /**
     * @param idletime the idletime to set
     */
    fun setIdletime(idletime: TimeSpan?) {
        this.idletime = idletime
    }

    /**
     * @param metadata the metadata to set
     */
    fun setMetadata(metadata: String?) {
        this.metadata = metadata
    }

    companion object {
        private val TIMESPAN_FAR_AWAY: TimeSpan? = TimeSpanImpl(1000000000, 1000000000, 1000000000, 1000000000)
        private val TIMESPAN_0: TimeSpan? = TimeSpanImpl(0, 0, 0, 0)
        private const val CACHE = 0
        private const val CACHE_SERVER = 1
        private const val CACHE_CLIENT = 2
        private const val FLUSH = 3
        private const val CONTENT = 4
        private const val GET = 5
        private const val PUT = 6
    }
}