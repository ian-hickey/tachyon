package lucee.runtime.engine

import java.io.IOException

class Request(pc: PageContext?, type: Short) : Thread() {
    private val pc: PageContext?
    private val parent: Thread?
    private var done = false
    private val type: Short
    fun run() {
        try {
            exe(pc, type, false, true)
        } catch (_t: Throwable) {
        }
        done = true
        SystemUtil.notify(parent)
    }

    fun isDone(): Boolean {
        return done
    }

    companion object {
        const val TYPE_CFML: Short = 1
        const val TYPE_LUCEE: Short = 2
        const val TYPE_REST: Short = 3
        @Throws(IOException::class, PageException::class)
        fun exe(pc: PageContext?, type: Short, throwExcpetion: Boolean, registerWithThread: Boolean) {
            var queue: ThreadQueue? = null
            try {
                if (registerWithThread) ThreadLocalPageContext.register(pc)
                val tmp: ThreadQueue = pc.getConfig().getThreadQueue()
                tmp.enter(pc)
                queue = tmp
                if (type == TYPE_CFML) pc.executeCFML(pc.getHttpServletRequest().getServletPath(), throwExcpetion, true) else if (type == TYPE_LUCEE) pc.execute(pc.getHttpServletRequest().getServletPath(), throwExcpetion, true) else pc.executeRest(pc.getHttpServletRequest().getServletPath(), throwExcpetion)
            } finally {
                if (queue != null) queue.exit(pc)
                if (registerWithThread) ThreadLocalPageContext.release()
            }
        }
    }

    init {
        parent = Thread.currentThread()
        this.pc = pc
        this.type = type
    }
}