package lucee.commons.lang

import java.util.ArrayList

class ParentThreasRefThread : Thread() {
    private val thread: Thread? = null
    private var stes: Array<StackTraceElement>
    val parentThread: Thread?
        get() = thread

    @Override
    @Synchronized
    fun start() {
        stes = Thread.currentThread().getStackTrace()
        super.start()
    }

    fun addParentStacktrace(t: Throwable) {
        val tmp: Array<StackTraceElement> = t.getStackTrace()
        val merged: List<StackTraceElement> = ArrayList(tmp.size + stes.size - 1)
        for (ste in tmp) {
            merged.add(ste)
        }
        run {
            var ste: StackTraceElement
            for (i in 1 until stes.size) {
                ste = stes[i]
                merged.add(ste)
            }
        }
        t.setStackTrace(merged.toArray(arrayOfNulls<StackTraceElement>(0)))
    }
}