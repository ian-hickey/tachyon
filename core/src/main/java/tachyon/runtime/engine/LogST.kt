package tachyon.runtime.engine

import java.io.File

class LogST(thread: Thread?, logDirectory: File?, logName: String?, timeRange: Int) : ParentThreasRefThread() {
    private val thread: Thread?
    private var size: Long = 0
    private val max = (1024 * 1024 * 100).toLong()
    private val logDirectory: File?
    private val logName: String?
    private val timeRange: Int
    @Override
    fun run() {
        var ps: PrintStream? = null
        try {
            ps = PrintStream(createFile())
            while (true) {
                printStackTrace(ps, thread.getStackTrace())
                SystemUtil.wait(this, timeRange)
                if (size > max) {
                    IOUtil.close(ps)
                    ps = PrintStream(createFile())
                    size = 0
                }
            }
        } catch (e: IOException) {
            addParentStacktrace(e)
            LogUtil.log(ThreadLocalPageContext.get(), LogST::class.java.getName(), e)
        } finally {
            try {
                IOUtil.close(ps)
            } catch (e: IOException) {
                addParentStacktrace(e)
                LogUtil.log(ThreadLocalPageContext.get(), LogST::class.java.getName(), e)
            }
        }
    }

    @Throws(IOException::class)
    private fun createFile(): File? {
        var f: File?
        var count = 0
        while (File(logDirectory, logName.toString() + "-" + ++count + ".log").also { f = it }.isFile()) {
        }
        return f
    }

    private fun printStackTrace(ps: PrintStream?, trace: Array<StackTraceElement?>?) {
        run({
            var line: String
            // Print our stack trace
            val head: String = System.currentTimeMillis().toString() + "\n"
            ps.print(head)
            size += head.length()
            for (traceElement in trace!!) {
                line = "\tat $traceElement\n"
                ps.print(line)
                size += line.length()
            }
            ps.print(NL)
            ps.flush()
            size += 1
        })
    }

    companion object {
        private const val NL = '\n'
        fun _do(logDirectory: File?) {
            _do(logDirectory, "stacktrace", 10)
        }

        fun _do(logDirectory: File?, logName: String?) {
            _do(logDirectory, logName, 10)
        }

        fun _do(logDirectory: File?, logName: String?, timeRange: Int) {
            val log = LogST(Thread.currentThread(), logDirectory, logName, timeRange)
            log.start()
            // log.join();
        }
    }

    /*
	 * public static void main(String[] args) throws InterruptedException {
	 * 
	 * print.e("----------- start ------------"); LogST log = new LogST(Thread.currentThread());
	 * log.start(); log.join(); print.e("----------- stop ------------"); }
	 */
    init {
        this.thread = thread
        this.logDirectory = logDirectory
        this.logName = logName
        this.timeRange = timeRange
        if (timeRange < 1) throw RuntimeException("time range $timeRange is invalid.")
    }
}