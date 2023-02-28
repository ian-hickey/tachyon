package tachyon.commons.cpu

import java.util.Iterator

abstract class CFMLListener : Listener {
    private val config: ConfigWeb

    @Override
    override fun listen(list: List<StaticData?>?) {
        var pc: PageContext = ThreadLocalPageContext.get()
        var release = false
        if (pc == null) {
            release = true
            pc = ThreadUtil.createPageContext(config, DevNullOutputStream.DEV_NULL_OUTPUT_STREAM, "localhost", "/", "", arrayOfNulls<Cookie>(0), arrayOfNulls<Pair>(0), null, arrayOfNulls<Pair>(0),
                    StructImpl(), false, -1)
        }
        try {
            _listen(pc, list)
        } catch (pe: PageException) {
            LogUtil.log(pc, "application", "cpu", pe)
        } finally {
            if (release) {
                val f: CFMLFactory = pc.getConfig().getFactory()
                f.releaseTachyonPageContext(pc, true)
            }
            // ThreadLocalPageContext.register(oldPC);
        }
    }

    @Throws(PageException::class)
    protected fun toQuery(list: List<StaticData?>): Object {
        var sd: StaticData? = null
        val qry = QueryImpl(columns, list.size(), "cpu")
        val it: Iterator<StaticData?> = list.iterator()
        var row = 0
        while (it.hasNext()) {
            row++
            sd = it.next()
            qry.setAt(KeyConstants._name, row, sd.name)
            qry.setAt(PERCENTAGE, row, sd.getPercentage())
            qry.setAt(KeyConstants._stacktrace, row, sd.getStacktrace())
            qry.setAt(KeyConstants._time, row, sd.getTime())
            qry.setAt(KeyConstants._total, row, sd.getTotal())
        }
        return qry
    }

    @Throws(PageException::class)
    abstract fun _listen(pc: PageContext?, list: List<StaticData?>?)

    companion object {
        private val PERCENTAGE: Key = KeyImpl.getInstance("percentage")
        private val columns: Array<Key> = arrayOf<Key>(KeyConstants._name, PERCENTAGE, KeyConstants._stacktrace, KeyConstants._time, KeyConstants._total)
    }

    init {
        config = ThreadLocalPageContext.getConfig() as ConfigWeb
    }
}