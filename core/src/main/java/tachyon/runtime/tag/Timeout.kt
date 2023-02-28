package tachyon.runtime.tag

import tachyon.commons.io.SystemUtil

// MUST change behavior of multiple headers now is an array, it das so?
/**
 * Lets you execute HTTP POST and GET operations on files. Using cfhttp, you can execute standard
 * GET operations and create a query object from a text file. POST operations lets you upload MIME
 * file types to a server, or post cookie, formfield, URL, file, or CGI variables directly to a
 * specified server.
 *
 *
 *
 *
 */
class Timeout : BodyTagImpl() {
    private var forcestop = false
    private var pc: PageContext? = null
    private var thread: ThreadImpl? = null
    private var onTimeout: UDF? = null
    private var onError: UDF? = null
    private var timeoutInMillis: Long = 0
    @Override
    fun release() {
        super.release()
        timeoutInMillis = 0
        pc = null
        thread = null
        forcestop = false
        onTimeout = null
        onError = null
    }

    @Throws(PageException::class)
    fun setOntimeout(obj: Object?) {
        if (obj == null) return
        onTimeout = Caster.toFunction(obj)
    }

    @Throws(PageException::class)
    fun setOnerror(obj: Object?) {
        if (obj == null) return
        onError = Caster.toFunction(obj)
    }

    fun setForcestop(forcestop: Boolean) {
        this.forcestop = forcestop
    }

    @Throws(PageException::class)
    fun setTimespan(timeout: Object?) {
        if (timeout is TimeSpan) timeoutInMillis = (timeout as TimeSpan?).getMillis() else timeoutInMillis = Caster.toLongValue(Caster.toDoubleValue(timeout) * 1000.0)
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        pc = pageContext // do not remove, this is needed
        return EVAL_BODY_INCLUDE
    }

    @Override
    @Throws(PageException::class)
    fun doEndTag(): Int {
        return EVAL_PAGE
    }

    @Throws(PageException::class)
    fun register(currentPage: Page?, threadIndex: Int) {
        try {
            thread = ThreadImpl(pc as PageContextImpl?, currentPage, threadIndex)
            thread.setDaemon(false)
            thread.start()
            try {
                if (timeoutInMillis != 0L) thread.join(timeoutInMillis) else thread.join()
            } catch (e: InterruptedException) {
            }

            // handle exception
            handleException(thread)
            if (!thread!!.hasEnded()) {
                if (forcestop) {
                    SystemUtil.stop(pc, thread)
                }
                handleTimeout(thread)
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw Caster.toPageException(t)
        }
    }

    @Throws(PageException::class)
    private fun handleTimeout(thread2: ThreadImpl?) {
        if (onTimeout != null) onTimeout.call(pc, arrayOf<Object?>(TimeSpanImpl.fromMillis(timeoutInMillis)), true) else throw ApplicationException("a timeout occurred within the tag timeout", "timeout is set to $timeoutInMillis ms")
    }

    @Throws(PageException::class)
    private fun handleException(thread2: ThreadImpl?) {
        var ex: PageException? = thread!!.exception
        if (ex != null) {
            ex = CFMLEngineFactory.getInstance().getCastUtil().toPageException(Exception(ex))
            if (onError != null) onError.call(pc, arrayOf<Object?>(CatchBlockImpl(ex)), true) else throw ex
        }
    }

    @Override
    fun doInitBody() {
    }

    @Override
    fun doAfterBody(): Int {
        return SKIP_BODY
    }

    /**
     * sets if has body or not
     *
     * @param hasBody
     */
    fun hasBody(hasBody: Boolean) {}
    class ThreadImpl(pc: PageContextImpl?, page: Page?, threadIndex: Int) : Thread() {
        private val page: Page?
        private val threadIndex: Int
        private val pc: PageContextImpl?
        var terminated = false
        var startTime: Long = 0
            private set
        var endTime: Long = 0
            private set
        private var pe: PageException? = null
        @Override
        fun run() {
            startTime = System.currentTimeMillis()
            try {
                page.threadCall(pc, threadIndex)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                if (!Abort.isSilentAbort(t)) {
                    pe = Caster.toPageException(t)
                }
            } finally {
                endTime = System.currentTimeMillis()
            }
        }

        fun hasStarted(): Boolean {
            return startTime > 0
        }

        fun hasEnded(): Boolean {
            return endTime > 0
        }

        val exception: PageException?
            get() = pe

        fun executionTime(): Long {
            if (startTime == 0L) return 0L
            return if (endTime == 0L) System.currentTimeMillis() - startTime else endTime - startTime
        }

        init {
            this.pc = pc
            this.threadIndex = threadIndex
            this.page = page
        }
    }
}