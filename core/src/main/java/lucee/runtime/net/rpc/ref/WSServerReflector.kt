package lucee.runtime.net.rpc.ref

import java.lang.reflect.Method

class WSServerReflector(obj: Object?) : WSServer {
    private val obj: Object?
    private val clazz: Class<out Object?>?
    private var doGet: Method? = null
    private var doPost: Method? = null
    private var invoke: Method? = null
    private var registerTypeMapping: Method? = null
    private var getWSHandler: Method? = null
    @Override
    @Throws(PageException::class)
    fun doGet(pc: PageContext?, request: HttpServletRequest?, response: HttpServletResponse?, component: Component?) {
        try {
            if (doGet == null) doGet = clazz.getMethod("doGet", arrayOf<Class?>(PageContext::class.java, HttpServletRequest::class.java, HttpServletResponse::class.java, Component::class.java))
            doGet.invoke(obj, arrayOf<Object?>(pc, request, response, component))
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    fun doPost(pc: PageContext?, req: HttpServletRequest?, res: HttpServletResponse?, component: Component?) {
        try {
            if (doPost == null) doPost = clazz.getMethod("doPost", arrayOf<Class?>(PageContext::class.java, HttpServletRequest::class.java, HttpServletResponse::class.java, Component::class.java))
            doPost.invoke(obj, arrayOf<Object?>(pc, req, res, component))
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    operator fun invoke(name: String?, args: Array<Object?>?): Object? {
        return try {
            if (invoke == null) invoke = clazz.getMethod("invoke", arrayOf<Class?>(String::class.java, Array<Object>::class.java))
            invoke.invoke(obj, arrayOf(name, args))
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    fun registerTypeMapping(clazz: Class?) {
        try {
            if (registerTypeMapping == null) registerTypeMapping = this.clazz.getMethod("registerTypeMapping", arrayOf<Class?>(Class::class.java))
            registerTypeMapping.invoke(obj, arrayOf<Object?>(clazz))
        } catch (e: Exception) {
            throw PageRuntimeException(Caster.toPageException(e))
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
        clazz = obj.getClass()
    }
}