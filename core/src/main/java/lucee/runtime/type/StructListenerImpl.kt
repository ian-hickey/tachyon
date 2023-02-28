package lucee.runtime.type

import lucee.runtime.PageContext

class StructListenerImpl(type: Int, udf: UDF?) : StructImpl(type) {
    private val udf: UDF?

    @Override
    override operator fun get(key: Key?, defaultValue: Object?): Object? {
        val res: Object = super.get(key, NULL)
        return if (res === NULL) onMissingKey(null, key, defaultValue) else res
    }

    @Override
    override fun g(key: Key?, defaultValue: Object?): Object? {
        val res: Object = super.g(key, NULL)
        return if (res === NULL) onMissingKey(null, key, defaultValue) else res
    }

    @Override
    override operator fun get(pc: PageContext?, key: Key?, defaultValue: Object?): Object? {
        val res: Object = super.get(pc, key, NULL)
        return if (res === NULL) onMissingKey(pc, key, defaultValue) else res
    }

    @Override
    @Throws(PageException::class)
    override fun g(key: Key?): Object? {
        val res: Object = super.g(key, NULL)
        return if (res === NULL) onMissingKey(null, key) else res
    }

    @Override
    @Throws(PageException::class)
    override operator fun get(key: Key?): Object? {
        val res: Object = super.get(key, NULL)
        return if (res === NULL) onMissingKey(null, key) else res
    }

    @Override
    @Throws(PageException::class)
    override operator fun get(pc: PageContext?, key: Key?): Object? {
        val res: Object = super.get(pc, key, NULL)
        return if (res === NULL) onMissingKey(pc, key) else res
    }

    @Override
    override fun toDumpData(pageContext: PageContext?, maxlevel: Int, properties: DumpProperties?): DumpData? {
        val dt: DumpTable? = super.toDumpData(pageContext, maxlevel, properties) as DumpTable?
        dt.setComment("this struct has a onMissingKey defined")
        return dt
    }

    @Throws(PageException::class)
    private fun onMissingKey(pc: PageContext?, key: Key?): Object? {
        var pc: PageContext? = pc
        pc = ThreadLocalPageContext.get(pc)
        return udf.call(pc, arrayOf(key, this), true)
    }

    private fun onMissingKey(pc: PageContext?, key: Key?, defaultValue: Object?): Object? {
        return try {
            onMissingKey(pc, key)
        } catch (e: Exception) {
            defaultValue
        }
    }

    companion object {
        private const val serialVersionUID = -2286369022408510584L
    }

    init {
        this.udf = udf
    }
}