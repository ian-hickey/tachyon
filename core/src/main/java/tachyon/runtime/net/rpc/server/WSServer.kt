package tachyon.runtime.net.rpc.server

import javax.servlet.http.HttpServletRequest

interface WSServer {
    @Throws(PageException::class)
    fun doGet(pc: PageContext?, request: HttpServletRequest?, response: HttpServletResponse?, component: Component?)

    @Throws(PageException::class)
    fun doPost(pc: PageContext?, req: HttpServletRequest?, res: HttpServletResponse?, component: Component?)

    @Throws(PageException::class)
    operator fun invoke(name: String?, args: Array<Object?>?): Object?
    fun registerTypeMapping(clazz: Class?)
    val wSHandler: WSHandler?
}