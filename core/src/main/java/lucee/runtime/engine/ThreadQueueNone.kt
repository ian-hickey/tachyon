package lucee.runtime.engine

import java.io.IOException

class ThreadQueueNone : ThreadQueue {
    @Override
    @Throws(IOException::class)
    fun enter(pc: PageContext?) {
    }

    @Override
    fun exit(pc: PageContext?) {
    }

    @Override
    fun clear() {
    }

    @Override
    fun size(): Int {
        return 0
    }

    companion object {
        val instance: ThreadQueue? = ThreadQueueNone()
    }
}