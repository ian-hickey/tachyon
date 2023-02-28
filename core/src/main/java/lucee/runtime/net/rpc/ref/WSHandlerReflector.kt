package lucee.runtime.net.rpc.ref

import java.lang.reflect.Method

class WSHandlerReflector(obj: Object?) : WSHandler {
    private val obj: Object?
    private val clazz: Class<out Object?>?
    private var isSOAPRequest: Method? = null
    private var addSOAPResponseHeader: Method? = null
    private var getSOAPRequestHeader: Method? = null
    private var getTypeAsString: Method? = null
    private var toWSTypeClass: Method? = null
    private var getWSClient: Method? = null
    private var getWSServer: Method? = null
    @Override
    fun isSOAPRequest(): Boolean {
        return try {
            if (isSOAPRequest == null) isSOAPRequest = clazz.getMethod("isSOAPRequest", EMPTY_CLASS)
            Caster.toBooleanValue(isSOAPRequest.invoke(obj, EMPTY_OBJECT))
        } catch (e: Exception) {
            throw PageRuntimeException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    fun addSOAPResponseHeader(namespace: String?, name: String?, value: Object?, mustUnderstand: Boolean) {
        try {
            if (addSOAPResponseHeader == null) addSOAPResponseHeader = clazz.getMethod("addSOAPResponseHeader", arrayOf<Class?>(String::class.java, String::class.java, Object::class.java, Boolean::class.javaPrimitiveType))
            addSOAPResponseHeader.invoke(obj, arrayOf(namespace, name, value, mustUnderstand))
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    fun getSOAPRequestHeader(pc: PageContext?, namespace: String?, name: String?, asXML: Boolean): Object? {
        return try {
            if (getSOAPRequestHeader == null) getSOAPRequestHeader = clazz.getMethod("getSOAPRequestHeader", arrayOf<Class?>(PageContext::class.java, String::class.java, String::class.java, Boolean::class.javaPrimitiveType))
            getSOAPRequestHeader.invoke(obj, arrayOf(pc, namespace, name, asXML))
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @get:Override
    val typeAsString: String?
        get() = try {
            if (getTypeAsString == null) getTypeAsString = clazz.getMethod("getTypeAsString", EMPTY_CLASS)
            Caster.toString(getTypeAsString.invoke(obj, EMPTY_OBJECT))
        } catch (e: Exception) {
            throw PageRuntimeException(e)
        }

    @Override
    fun toWSTypeClass(_clazz: Class<*>?): Class<*>? {
        return try {
            if (toWSTypeClass == null) toWSTypeClass = clazz.getMethod("toWSTypeClass", arrayOf<Class?>(Class::class.java))
            toWSTypeClass.invoke(obj, arrayOf<Object?>(_clazz)) as Class<*>
        } catch (e: Exception) {
            e.printStackTrace()
            throw PageRuntimeException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    fun getWSServer(pc: PageContext?): WSServer? {
        return try {
            if (getWSServer == null) getWSServer = clazz.getMethod("getWSServer", arrayOf<Class?>(PageContext::class.java))
            val o: Object = getWSServer.invoke(obj, arrayOf<Object?>(pc))
            if (o is WSServer) o as WSServer else WSServerReflector(o)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    fun getWSClient(wsdlUrl: String?, username: String?, password: String?, proxyData: ProxyData?): WSClient? {
        return try {
            if (getWSClient == null) getWSClient = clazz.getMethod("getWSClient", arrayOf<Class?>(String::class.java, String::class.java, String::class.java, ProxyData::class.java))
            val o: Object = getWSClient.invoke(obj, arrayOf(wsdlUrl, username, password, proxyData))
            if (o is WSClient) o as WSClient else WSClientReflector(o)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    companion object {
        val EMPTY_CLASS: Array<Class?>? = arrayOfNulls<Class?>(0)
        val EMPTY_OBJECT: Array<Object?>? = arrayOfNulls<Object?>(0)
    }

    init {
        this.obj = obj
        clazz = obj.getClass()
    }
}