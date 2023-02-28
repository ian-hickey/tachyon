package lucee.runtime.pool

import java.util.Iterator

class Pool(private val maxIdle: Long, private val maxItems: Int, interval: Long) {
    private val map: ConcurrentHashMap<String?, PoolItemWrap?>?
    private var controller: Controller? = null
    var interval: Long
    @Throws(Exception::class)
    fun put(id: String?, value: PoolItem?) {
        val item = PoolItemWrap(value)
        val previous: PoolItemWrap = map.putIfAbsent(id, item)
        item!!.setLastAccess(System.currentTimeMillis())
        item!!.start()

        // we already have an item with that key, because we only have one we end the existing one
        if (previous != null) previous.getValue()!!.end()
        shrinkIfNecessary()
        startControllerIfNecessary()
    }

    @Throws(Exception::class)
    operator fun get(id: String?): PoolItem? {
        val now: Long = System.currentTimeMillis()
        val item: PoolItemWrap = map.get(id) ?: return null
        if (item.lastAccess() + maxIdle < now || !item.getValue()!!.isValid()) {
            item.end()
            stopControllerIfNecessary()
            return null
        }
        return item.setLastAccess(now)!!.getValue()
    }

    @Throws(Exception::class)
    fun remove(id: String?): Boolean {
        val item: PoolItemWrap = map.remove(id)
        if (item != null) {
            item.end()
            return true
        }
        stopControllerIfNecessary()
        return false
    }

    @Throws(Exception::class)
    fun remove(item: PoolItem?): Boolean {
        val it: Iterator<Entry<String?, PoolItemWrap?>?> = map.entrySet().iterator()
        var e: Entry<String?, PoolItemWrap?>?
        while (it.hasNext()) {
            e = it.next()
            if (e.getValue().getValue() === item) {
                return remove(e.getKey())
            }
        }
        return false
    }

    @Throws(Exception::class)
    fun clean(force: Boolean) {
        val it: Iterator<Entry<String?, PoolItemWrap?>?> = map.entrySet().iterator()
        var e: Entry<String?, PoolItemWrap?>?
        while (it.hasNext()) {
            e = it.next()
            val now: Long = System.currentTimeMillis()
            if (force || e.getValue().lastAccess() + maxIdle < now || !e.getValue().getValue().isValid()) {
                e.getValue().end()
                map.remove(e.getKey())
            }
        }
        stopControllerIfNecessary()
    }

    private fun shrinkIfNecessary() {
        while (map.size() > maxItems) {
            removeOldest()
        }
    }

    private fun removeOldest() {

        // get oldest
        val it: Iterator<Entry<String?, PoolItemWrap?>?> = map.entrySet().iterator()
        var e: Entry<String?, PoolItemWrap?>?
        var oldest: Entry<String?, PoolItemWrap?>? = null
        while (it.hasNext()) {
            e = it.next()
            if (oldest == null || oldest.getValue().lastAccess() > e.getValue().lastAccess()) oldest = e
        }
        if (oldest != null) map.remove(oldest.getKey())
    }

    private fun startControllerIfNecessary() {
        if (!map.isEmpty()) {
            if (controller == null || !controller.isAlive()) {
                controller = Controller(this)
                controller.start()
            }
        }
    }

    private fun stopControllerIfNecessary() {
        if (map.isEmpty()) {
            if (controller != null && controller.isAlive()) {
                controller.interrupt()
            }
        }
    }

    inner class Controller(private val pool: Pool?) : ParentThreasRefThread() {
        @Override
        fun run() { // TODO handle exceptions
            while (true) {
                try {
                    sleep(pool!!.interval)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                if (isInterrupted()) break
                try {
                    pool!!.clean(false)
                } catch (e: Exception) {
                    addParentStacktrace(e)
                    e.printStackTrace()
                }
                if (isInterrupted()) break
            }
        }
    }

    init {
        map = ConcurrentHashMap<String?, PoolItemWrap?>()
        this.interval = interval
    }
}