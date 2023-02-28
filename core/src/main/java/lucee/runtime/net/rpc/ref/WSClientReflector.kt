package lucee.runtime.net.rpc.ref

import java.lang.reflect.Method

class WSClientReflector(obj: Object?) : WSClient {
    private val obj: Object?
    private val objects: Objects?
    private val it: Iteratorable?
    private val clazz: Class<out Object?>?
    private var addSOAPRequestHeader: Method? = null
    private var getSOAPRequest: Method? = null
    private var getSOAPResponse: Method? = null
    private var getSOAPResponseHeader: Method? = null

    // private Method getLastCall;
    private var callWithNamedValues: Method? = null
    private var addHeader: Method? = null
    private var getWSHandler: Method? = null
    @Override
    @Throws(PageException::class)
    fun call(arg0: PageContext?, arg1: Key?, arg2: Array<Object?>?): Object? {
        return objects.call(arg0, arg1, arg2)
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(arg0: PageContext?, arg1: Key?, arg2: Struct?): Object? {
        return objects.callWithNamedValues(arg0, arg1, arg2)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(arg0: PageContext?, arg1: Key?): Object? {
        return objects.get(arg0, arg1)
    }

    @Override
    operator fun get(arg0: PageContext?, arg1: Key?, arg2: Object?): Object? {
        return objects.get(arg0, arg1, arg2)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(arg0: PageContext?, arg1: Key?, arg2: Object?): Object? {
        return objects.set(arg0, arg1, arg2)
    }

    @Override
    fun setEL(arg0: PageContext?, arg1: Key?, arg2: Object?): Object? {
        return objects.setEL(arg0, arg1, arg2)
    }

    @Override
    fun toDumpData(arg0: PageContext?, arg1: Int, arg2: DumpProperties?): DumpData? {
        return objects.toDumpData(arg0, arg1, arg2)
    }

    @Override
    fun castToBoolean(arg0: Boolean?): Boolean? {
        return objects.castToBoolean(arg0)
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return objects.castToBooleanValue()
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        return objects.castToDateTime()
    }

    @Override
    fun castToDateTime(arg0: DateTime?): DateTime? {
        return objects.castToDateTime(arg0)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return objects.castToDoubleValue()
    }

    @Override
    fun castToDoubleValue(arg0: Double): Double {
        return objects.castToDoubleValue(arg0)
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        return objects.castToString()
    }

    @Override
    fun castToString(arg0: String?): String? {
        return objects.castToString(arg0)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(arg0: String?): Int {
        return objects.compareTo(arg0)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(arg0: Boolean): Int {
        return objects.compareTo(arg0)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(arg0: Double): Int {
        return objects.compareTo(arg0)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(arg0: DateTime?): Int {
        return objects.compareTo(arg0)
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return it.entryIterator()
    }

    @Override
    fun keyIterator(): Iterator<Key?>? {
        return it.keyIterator()
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return it.keysAsStringIterator()
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return it.valueIterator()
    }

    @Override
    @Throws(PageException::class)
    fun addHeader(header: Object?) { // Object instead of header because Java 11 no longer support javax.xml.soap.SOAPHeaderElement
        try {
            if (addHeader == null) addHeader = clazz.getMethod("addHeader", arrayOf<Class?>(Class.forName("javax.xml.soap.SOAPHeaderElement")))
            addHeader.invoke(obj, arrayOf<Object?>(header))
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    /*
	 * @Override public Call getLastCall() throws PageException { try { if(getLastCall==null)
	 * getLastCall=clazz.getMethod("getLastCall", WSHandlerReflector.EMPTY_CLASS); return (Call)
	 * getLastCall.invoke(obj,WSHandlerReflector.EMPTY_OBJECT); } catch(Exception e) { throw
	 * Caster.toPageException(e); } }
	 */
    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(config: Config?, methodName: Key?, arguments: Struct?): Object? {
        return try {
            if (callWithNamedValues == null) callWithNamedValues = clazz.getMethod("callWithNamedValues", arrayOf<Class?>(Config::class.java, Key::class.java, Struct::class.java))
            callWithNamedValues.invoke(obj, arrayOf<Object?>(config, methodName, arguments))
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    fun addSOAPRequestHeader(nameSpace: String?, name: String?, value: Object?, mustUnderstand: Boolean) {
        try {
            if (addSOAPRequestHeader == null) addSOAPRequestHeader = clazz.getMethod("addSOAPRequestHeader", arrayOf<Class?>(String::class.java, String::class.java, Object::class.java, Boolean::class.javaPrimitiveType))
            addSOAPRequestHeader.invoke(obj, arrayOf(nameSpace, name, value, mustUnderstand))
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @get:Throws(PageException::class)
    @get:Override
    val sOAPRequest: Node?
        get() = try {
            if (getSOAPRequest == null) getSOAPRequest = clazz.getMethod("getSOAPRequest", WSHandlerReflector.EMPTY_CLASS)
            getSOAPRequest.invoke(obj, WSHandlerReflector.EMPTY_OBJECT) as Node
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }

    @get:Throws(PageException::class)
    @get:Override
    val sOAPResponse: Node?
        get() = try {
            if (getSOAPResponse == null) getSOAPResponse = clazz.getMethod("getSOAPResponse", WSHandlerReflector.EMPTY_CLASS)
            getSOAPResponse.invoke(obj, WSHandlerReflector.EMPTY_OBJECT) as Node
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }

    @Override
    @Throws(PageException::class)
    fun getSOAPResponseHeader(pc: PageContext?, namespace: String?, name: String?, asXML: Boolean): Object? {
        return try {
            if (getSOAPResponseHeader == null) getSOAPResponseHeader = clazz.getMethod("getSOAPResponseHeader", arrayOf<Class?>(PageContext::class.java, String::class.java, String::class.java, Boolean::class.javaPrimitiveType))
            getSOAPResponseHeader.invoke(obj, arrayOf(pc, namespace, name, asXML))
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @get:Override
    val wSHandler: WSHandler?
        get() = try {
            if (getWSHandler == null) getWSHandler = clazz.getMethod("getWSHandler", WSHandlerReflector.EMPTY_CLASS)
            getWSHandler.invoke(obj, WSHandlerReflector.EMPTY_OBJECT) as WSHandler
        } catch (e: Exception) {
            throw PageRuntimeException(e)
        }

    init {
        this.obj = obj
        objects = obj as Objects?
        it = obj as Iteratorable?
        clazz = obj.getClass()
    }
}