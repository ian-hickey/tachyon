package tachyon.runtime.tag.query

import javax.servlet.http.HttpServletRequest

class QuerySpoolerTask(parent: PageContext?, data: QueryBean?, sql: String?, tl: TemplateLine?, ps: PageSource?) : SpoolerTaskSupport(EXECUTION_PLANS) {
    @Transient
    private val pc: PageContextImpl? = null
    private val serverName: String?
    private val queryString: String?
    private val cookies: Array<SerializableCookie?>?
    private val parameters: Array<Pair<String?, String?>?>?
    private val requestURI: String?
    private val headers: Array<Pair<String?, String?>?>?
    private val attributes: Struct?
    private val requestTimeout: Long
    private val data: QueryBean?
    private val sql: String?
    private val tl: TemplateLine?
    private val relPath: String?
    private val relPathwV: String?
    private val mapping: SerMapping?
    @Override
    fun detail(): Struct? {
        // TODO Auto-generated method stub
        return null
    }

    @Override
    @Throws(PageException::class)
    fun execute(config: Config?): Object? {
        val oldPc: PageContext = ThreadLocalPageContext.get()
        var pc: PageContextImpl? = null
        try {
            // daemon
            if (this.pc != null) {
                pc = this.pc
                ThreadLocalPageContext.register(pc)
            } else {
                val cwi: ConfigWebPro? = config as ConfigWebPro?
                val session: HttpSession? = if (oldPc != null && oldPc.getSessionType() === Config.SESSION_TYPE_JEE) oldPc.getSession() else null
                val os: DevNullOutputStream = DevNullOutputStream.DEV_NULL_OUTPUT_STREAM
                pc = ThreadUtil.createPageContext(cwi, os, serverName, requestURI, queryString, SerializableCookie.toCookies(cookies), headers, null, parameters, attributes, true,
                        -1, session)
                pc.setRequestTimeout(requestTimeout)
                val ps: PageSource = UDFPropertiesImpl.toPageSource(pc, cwi, if (mapping == null) null else mapping.toMapping(), relPath, relPathwV)
                pc.addPageSource(ps, true)
            }
            try {
                Query._doEndTag(pc, data, sql, tl, false)
            } catch (e: Exception) {
                if (!Abort.isSilentAbort(e)) {
                    val c: ConfigWeb = pc.getConfig()
                    val log: Log = ThreadLocalPageContext.getLog(pc, "application")
                    if (log != null) log.log(Log.LEVEL_ERROR, "query", e)
                    // if(!serializable)catchBlock=pe.getCatchBlock(pc.getConfig());
                    return Caster.toPageException(e)
                }
            } finally {
                if (pc.getHttpServletResponse() is HttpServletResponseDummy) {
                    // HttpServletResponseDummy rsp=(HttpServletResponseDummy) pc.getHttpServletResponse();
                    pc.flush()
                    /*
					 * contentType=rsp.getContentType(); Pair<String,Object>[] _headers = rsp.getHeaders();
					 * if(_headers!=null)for(int i=0;i<_headers.length;i++){
					 * if(_headers[i].getName().equalsIgnoreCase("Content-Encoding"))
					 * contentEncoding=Caster.toString(_headers[i].getValue(),null); }
					 */
                }
            }
        } finally {
            pc.getConfig().getFactory().releaseTachyonPageContext(pc, true)
            pc = null
            if (oldPc != null) ThreadLocalPageContext.register(oldPc)
        }
        return null
    }

    // TODO Auto-generated method stub
    @get:Override
    val type: String?
        get() =// TODO Auto-generated method stub
            null

    @Override
    fun subject(): String? {
        // TODO Auto-generated method stub
        return null
    }

    companion object {
        private const val serialVersionUID = 2450199479366505177L
        private val EXECUTION_PLANS: Array<ExecutionPlan?>? = arrayOf<ExecutionPlan?>()
    }

    // private String absPath;
    init {
        this.data = data
        this.sql = sql
        this.tl = tl
        relPath = ps.getRealpath()
        relPathwV = ps.getRealpathWithVirtual()
        val m: Mapping = ps.getMapping()
        mapping = if (m is MappingImpl) (m as MappingImpl).toSerMapping() else null
        val req: HttpServletRequest = parent.getHttpServletRequest()
        serverName = req.getServerName()
        queryString = ReqRspUtil.getQueryString(req)
        cookies = SerializableCookie.toSerializableCookie(ReqRspUtil.getCookies(req, parent.getWebCharset()))
        parameters = HttpUtil.cloneParameters(req)
        requestURI = req.getRequestURI()
        headers = HttpUtil.cloneHeaders(req)
        attributes = HttpUtil.getAttributesAsStruct(req)
        requestTimeout = parent.getRequestTimeout()
    }
}