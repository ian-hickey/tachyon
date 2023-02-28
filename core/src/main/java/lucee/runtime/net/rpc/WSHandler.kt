package lucee.runtime.net.rpc

import lucee.runtime.PageContext

// FUTURE add to loader
interface WSHandler {
    val isSOAPRequest: Boolean

    @Throws(PageException::class)
    fun addSOAPResponseHeader(namespace: String?, name: String?, value: Object?, mustUnderstand: Boolean)

    @Throws(PageException::class)
    fun getSOAPRequestHeader(pc: PageContext?, namespace: String?, name: String?, asXML: Boolean): Object?
    val typeAsString: String?
    fun toWSTypeClass(clazz: Class<*>?): Class<*>?

    @Throws(PageException::class)
    fun getWSServer(pc: PageContext?): WSServer?

    @Throws(PageException::class)
    fun getWSClient(wsdlUrl: String?, username: String?, password: String?, proxyData: ProxyData?): WSClient?
}