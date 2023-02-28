package tachyon.commons.cpu

import java.lang.management.ManagementFactory

class CPULogger(private val slotTime: Long, private val threshold: Double, listeners: List<Listener>) {
    var logger: Logger? = null
        private set
    private val listeners: List<Listener>
    fun startIt() {
        if (logger == null || !logger.isAlive() || !logger!!.run) {
            logger = Logger(slotTime, threshold, listeners)
            logger.start()
        }
    }

    fun stopIt() {
        if (logger != null && logger.isAlive()) {
            logger!!.run = false
            SystemUtil.stop(logger)
        }
    }

    class Logger(private val range: Long, private val threshold: Double, listeners: List<Listener>?) : ParentThreasRefThread() {
        val run = true
        private var log: Map<String, Data> = ConcurrentHashMap()
        private val listeners: List<Listener>?
        @Override
        fun run() {
            val tmxb: ThreadMXBean = ManagementFactory.getThreadMXBean()
            while (run) {
                try {
                    val it: Iterator<Thread> = Thread.getAllStackTraces().keySet().iterator()
                    var t: Thread
                    // ThreadInfo ti;
                    var key: String
                    var data: Data? = null
                    val total: RefLong = RefLongImpl(0)
                    val tmp: Map<String, Data> = ConcurrentHashMap()
                    while (it.hasNext()) {
                        t = it.next()
                        if (State.TIMED_WAITING.equals(t.getState()) || State.WAITING.equals(t.getState()) || State.TERMINATED.equals(t.getState())) continue
                        // ti = tmxb.getThreadInfo(t.getId());
                        key = t.getName() // + ":" + ti.getWaitedCount();
                        val cpuTime: Long = tmxb.getThreadCpuTime(t.getId())
                        data = log[key]
                        data?.add(total, cpuTime) ?: (data = Data(t, cpuTime))
                        tmp.put(key, data)
                    }
                    log = tmp
                    val list = cloneIt(log)
                    if (list != null && listeners != null) {
                        val itt: Iterator<Listener> = listeners.iterator()
                        while (itt.hasNext()) {
                            itt.next().listen(list)
                        }
                    }
                    if (range > 0) SystemUtil.sleep(range)
                } catch (e: Exception) {
                    addParentStacktrace(e)
                    LogUtil.log("application", "cpu", e)
                }
            }
        }

        private fun cloneIt(log: Map<String, Data>): List<StaticData> {
            val staticData: List<StaticData> = ArrayList()
            run({
                val it: Iterator<Entry<String, Data>> = log.entrySet().iterator()
                var entry: Entry<String, Data>
                while (it.hasNext()) {
                    entry = it.next()
                    val sd = StaticData(entry.getValue())
                    if (threshold <= sd.percentage) staticData.add(sd)
                }
            })
            return staticData
        }

        init {
            this.listeners = listeners
        }
    }

    class Data(thread: Thread, start: Long) {
        val start: Long
        var time: Long = 0
            private set
        val thread: Thread
        private var total: RefLong? = null
        fun add(total: RefLong, time: Long): Long {
            this.total = total
            this.time = time - start
            total.plus(this.time)
            return this.time
        }

        val percentage: Double
            get() {
                if (total == null) return 0
                val percentage: Double = if (time == 0L) 0 else 1.0 / total.toLongValue() * time
                val tmp = (percentage * 100.0).toInt()
                return tmp / 100.0
            }

        fun getThread(): Thread {
            return thread
        }

        init {
            this.thread = thread
            this.start = start
        }
    }

    class StaticData(data: Data) {
        var name: String
        val start: Long
        val time: Long
        val total: Long = 0
        val percentage: Double
        val stacktrace: String

        init {
            name = data.thread.getName()
            start = data.start
            time = data.time
            percentage = data.percentage
            stacktrace = ExceptionUtil.toString(data.thread.getStackTrace())
        }
    }

    init {
        this.listeners = listeners
    }
}