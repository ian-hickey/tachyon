package tachyon.runtime.future

import java.util.concurrent.Callable

class CallableUDF(parent: PageContext?, udf: UDF?, arg: Object?) : Callable<Object?> {
    private val udf: UDF?
    private val serverName: String?
    private val queryString: String?
    private val cookies: Array<SerializableCookie?>?
    private val parameters: Array<Pair<String?, String?>?>?
    private val requestURI: String?
    private val headers: Array<Pair<String?, String?>?>?
    private val attributes: Struct?
    private val requestTimeout: Long
    private val cw: ConfigWeb?
    private val arg: Object?
    @Override
    @Throws(Exception::class)
    fun call(): Object? {
        var pc: PageContext? = null
        ThreadLocalPageContext.register(pc)
        val os: DevNullOutputStream = DevNullOutputStream.DEV_NULL_OUTPUT_STREAM
        pc = ThreadUtil.createPageContext(cw, os, serverName, requestURI, queryString, SerializableCookie.toCookies(cookies), headers, null, parameters, attributes, true, -1)
        pc.setRequestTimeout(requestTimeout)
        return try {
            udf.call(pc, if (arg === Future.ARG_NULL) arrayOf<Object?>() else arrayOf<Object?>(arg), true)
        } finally {
            pc.getConfig().getFactory().releasePageContext(pc)
        }
    }

    init {
        // this.template=page.getPageSource().getRealpathWithVirtual();
        val req: HttpServletRequest = parent.getHttpServletRequest()
        serverName = req.getServerName()
        queryString = ReqRspUtil.getQueryString(req)
        cookies = SerializableCookie.toSerializableCookie(ReqRspUtil.getCookies(req, parent.getWebCharset()))
        parameters = HttpUtil.cloneParameters(req)
        requestURI = req.getRequestURI()
        headers = HttpUtil.cloneHeaders(req)
        attributes = HttpUtil.getAttributesAsStruct(req)
        requestTimeout = parent.getRequestTimeout()
        cw = parent.getConfig()
        this.udf = udf
        this.arg = arg
    }
}