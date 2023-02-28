package tachyon.commons.lang

import tachyon.runtime.PageContext

/**
 * thead that init a PageContext with ThreadLocal, only use this Thread when you are sure it ends
 * before the parent thread
 */
abstract class PageContextThread(pc: PageContext) : ParentThreasRefThread() {
    private val pageContext: PageContext
    @Override
    fun run() {
        val t: Thread = pageContext.getThread()
        ThreadLocalPageContext.register(pageContext) // register the PageContext to this thread
        try {
            run(pageContext)
        } finally {
            ThreadLocalPageContext.release()
            if (t != null) (pageContext as PageContextImpl).setThread(t)
        }
    }

    abstract fun run(pageContext: PageContext?)

    init {
        pageContext = pc
        setDaemon(true)
    }
}