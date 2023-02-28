package tachyon.runtime.net.rpc

import tachyon.runtime.PageContext

class DummyWSHandler : WSHandler {
    @get:Override
    override val isSOAPRequest: Boolean
        get() {
            throw notInstalledEL()
        }

    @Override
    @Throws(PageException::class)
    override fun addSOAPResponseHeader(namespace: String?, name: String?, value: Object?, mustUnderstand: Boolean) {
        throw notInstalled()
    }

    @Override
    @Throws(PageException::class)
    override fun getSOAPRequestHeader(pc: PageContext?, namespace: String?, name: String?, asXML: Boolean): Object? {
        throw notInstalled()
    }

    @get:Override
    override val typeAsString: String?
        get() {
            throw notInstalledEL()
        }

    @Override
    override fun toWSTypeClass(clazz: Class<*>?): Class<*>? {
        throw notInstalledEL()
    }

    @Override
    @Throws(PageException::class)
    override fun getWSServer(pc: PageContext?): WSServer? {
        throw notInstalled()
    }

    @Override
    @Throws(PageException::class)
    override fun getWSClient(wsdlUrl: String?, username: String?, password: String?, proxyData: ProxyData?): WSClient? {
        throw notInstalled()
    }

    private fun notInstalled(): RPCException? {
        return RPCException("No Webservice Engine is installed! Check out the Extension Store in the Tachyon Administrator for \"Webservices\".")
    }

    private fun notInstalledEL(): PageRuntimeException? {
        return PageRuntimeException(notInstalled())
    }
}