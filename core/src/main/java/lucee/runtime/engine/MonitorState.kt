package lucee.runtime.engine

import java.util.ArrayList

object MonitorState {
    fun checkForBlockedThreads(values: Collection<PageContextImpl?>?): List<BlockedThread?>? {
        val blockets: List<BlockedThread?> = ArrayList<BlockedThread?>()
        val it: Iterator<PageContextImpl?> = values!!.iterator()
        var bt: BlockedThread?
        while (it.hasNext()) {
            bt = checkForBlockedThreads(it.next())
            if (bt != null) blockets.add(bt)
        }
        return blockets
    }

    fun getBlockedThreads(pc: PageContextImpl?): String? {
        val bt: BlockedThread = checkForBlockedThreads(pc)
        return if (bt == null) "" else bt.getMessage()
    }

    fun checkForBlockedThreads(pc: PageContextImpl?): BlockedThread? {
        // if(pc.getStartTime() + pc.getRequestTimeout() > System.currentTimeMillis()) return null;
        val t: Thread = pc.getThread()
        if (!ignore(t) && Thread.State.BLOCKED.equals(t.getState())) {
            val ste: Array<StackTraceElement?> = t.getStackTrace()
            return BlockedThread(t, ste, getPossibleThreadsCausingThis(t, ste[0]))
        }
        return null
    }

    fun checkForBlockedThreadsx(values: Collection<PageContextImpl?>?): List<BlockedThread?>? {
        val blockets: List<BlockedThread?> = ArrayList<BlockedThread?>()
        val it: Iterator<PageContextImpl?> = values!!.iterator()
        var pc: PageContextImpl?
        var t: Thread
        while (it.hasNext()) {
            pc = it.next()
            t = pc.getThread()
            if (!ignore(t) && Thread.State.BLOCKED.equals(t.getState())) {
                val ste: Array<StackTraceElement?> = t.getStackTrace()
                blockets.add(BlockedThread(t, ste, getPossibleThreadsCausingThis(t, ste[0])))
            }
        }
        return blockets
    }

    fun checkForBlockedThreads(): List<BlockedThread?>? {
        val blockets: List<BlockedThread?> = ArrayList<BlockedThread?>()
        val it: Iterator<Entry<Thread?, Array<StackTraceElement?>?>?> = Thread.getAllStackTraces().entrySet().iterator()
        var e: Entry<Thread?, Array<StackTraceElement?>?>?
        var t: Thread
        while (it.hasNext()) {
            e = it.next()
            t = e.getKey()
            if (!ignore(t) && Thread.State.BLOCKED.equals(t.getState())) {
                blockets.add(BlockedThread(e.getKey(), e.getValue(), getPossibleThreadsCausingThis(t, e.getValue().get(0))))
            }
        }
        return blockets
    }

    private fun ignore(t: Thread?): Boolean {
        return t == null || "Finalizer".equals(t.getName()) || "Reference Handler".equals(t.getName()) || "Signal Dispatcher".equals(t.getName())
    }

    private fun getPossibleThreadsCausingThis(blockedThread: Thread?, blockedSTE: StackTraceElement?): List<Entry<Thread?, Array<StackTraceElement?>?>?>? {
        val list: List<Entry<Thread?, Array<StackTraceElement?>?>?> = ArrayList<Entry<Thread?, Array<StackTraceElement?>?>?>()
        val it: Iterator<Entry<Thread?, Array<StackTraceElement?>?>?> = Thread.getAllStackTraces().entrySet().iterator()
        var e: Entry<Thread?, Array<StackTraceElement?>?>?
        var ste: Array<StackTraceElement?>
        var t: Thread
        while (it.hasNext()) {
            e = it.next()
            t = e.getKey()
            if (t === blockedThread || ignore(t)) continue
            ste = e.getValue()
            val index = match(blockedSTE, ste, t)
            if (index == -1) continue
            list.add(e)
        }
        return list
    }

    private fun match(blockedSTE: StackTraceElement?, stes: Array<StackTraceElement?>?, t: Thread?): Int {
        for (i in stes.indices) {
            val ste: StackTraceElement? = stes!![i]
            if (ste.getClassName().equals(blockedSTE.getClassName()) && ste.getMethodName().equals(blockedSTE.getMethodName())) {
                val stel: Int = ste.getLineNumber()
                val bstel: Int = blockedSTE.getLineNumber()
                if (stel > bstel) return i
                if (stel == bstel && i > 0) {
                    if (i > 0) return i
                    if (!Thread.State.BLOCKED.equals(t.getState())) return i
                }
                break
            }
        }
        return -1
    }

    private class T : Thread() {
        @Override
        fun run() {
            checkit()
        }

        private fun checkit() {
            synchronized(o) {
                w()
                SystemUtil.wait(this, 10)
            }
        }

        private fun w() {
            // TODO Auto-generated method stub
        }

        companion object {
            private val o: Object? = Object()
        }
    }

    class BlockedThread(blockedThread: Thread?, blockedST: Array<StackTraceElement?>?, possibleBlockers: List<Entry<Thread?, Array<StackTraceElement?>?>?>?) {
        val possibleBlockers: List<Entry<Thread?, Array<StackTraceElement?>?>?>?
        val blockedThread: Thread?
        val blockedST: Array<StackTraceElement?>?
        fun getMessage(): String? {
            if (possibleBlockers!!.isEmpty()) return "The thread is blocked."
            val sb = StringBuilder(
                    if (possibleBlockers.size() > 1) "The thread is possibly blocked by the following threads:\n" else "The thread is possibly blocked by the following thread:\n")
            val it: Iterator<Entry<Thread?, Array<StackTraceElement?>?>?> = possibleBlockers.iterator()
            var e: Entry<Thread?, Array<StackTraceElement?>?>?
            while (it.hasNext()) {
                e = it.next()
                sb.append(ExceptionUtil.toString(e.getValue())).append("\n")
            }
            return sb.toString()
        }

        @Override
        override fun toString(): String {
            val sb: StringBuilder = StringBuilder().append("Blocked:\n").append(ExceptionUtil.toString(blockedST)).append("\nPossible Blockers:\n")
            val it: Iterator<Entry<Thread?, Array<StackTraceElement?>?>?> = possibleBlockers!!.iterator()
            var e: Entry<Thread?, Array<StackTraceElement?>?>?
            while (it.hasNext()) {
                e = it.next()
                sb.append(ExceptionUtil.toString(e.getValue())).append("\n")
            }
            return sb.toString()
        }

        init {
            this.blockedThread = blockedThread
            this.blockedST = blockedST
            this.possibleBlockers = possibleBlockers
        }
    }
}