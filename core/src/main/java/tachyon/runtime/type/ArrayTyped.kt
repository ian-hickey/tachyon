package tachyon.runtime.type

import java.util.ArrayList

class ArrayTyped : ArrayImpl {
    private var strType: String?
    private val type: Short

    constructor(type: String?) : super() {
        strType = type
        this.type = CFTypes.toShort(type, false, 0.toShort())
    }

    constructor(type: String?, initalCap: Int) : super(initalCap) {
        strType = type
        this.type = CFTypes.toShort(type, false, 0.toShort())
    }

    constructor(type: String?, objects: Array<Object?>?) : super(objects!!.size) {
        strType = type
        this.type = CFTypes.toShort(type, false, 0.toShort())
        for (i in objects.indices) {
            appendEL(objects[i])
        }
    }

    @Override
    override fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        val dt: DumpTable? = super.toDumpData(pageContext, maxlevel, dp) as DumpTable?
        dt.setTitle("Array (type:$strType)")
        return dt
    }

    @Override
    override fun duplicate(deepCopy: Boolean): Collection? {
        return super.duplicate(ArrayTyped(strType), deepCopy)
    }

    @Override
    @Throws(PageException::class)
    fun append(o: Object?): Object? {
        return super.append(checkType(o))
    }

    @Override
    fun appendEL(o: Object?): Object? {
        return super.appendEL(checkTypeEL(o))
    }

    @Override
    @Throws(PageException::class)
    fun insert(key: Int, value: Object?): Boolean {
        return super.insert(key, checkType(value))
    }

    @Override
    @Throws(PageException::class)
    fun prepend(o: Object?): Object? {
        return super.prepend(checkType(o))
    }

    @Override
    @Throws(PageException::class)
    fun setE(key: Int, value: Object?): Object? {
        return super.setE(key, checkType(value))
    }

    @Override
    fun setEL(key: Int, value: Object?): Object? {
        return super.setEL(key, checkTypeEL(value))
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: String?, value: Object?): Object? {
        return super.set(key, checkType(value))
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Key?, value: Object?): Object? {
        return super.set(key, checkType(value))
    }

    @Override
    fun setEL(key: String?, value: Object?): Object? {
        return super.setEL(key, checkTypeEL(value))
    }

    @Override
    fun setEL(key: Key?, value: Object?): Object? {
        return super.setEL(key, checkTypeEL(value))
    }

    @Override
    fun add(o: Object?): Boolean {
        return super.add(checkTypeEL(o))
    }

    @Override
    fun addAll(index: Int, c: Collection<*>?): Boolean {
        val list: MutableList<*> = ArrayList()
        val it: Iterator = c!!.iterator()
        while (it.hasNext()) {
            list.add(checkTypeEL(it.next()))
        }
        return super.addAll(index, list)
    }

    private fun checkTypeEL(o: Object?): Object? {
        return try {
            Caster.castTo(null, type, strType, o)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Throws(PageException::class)
    private fun checkType(o: Object?): Object? {
        return Caster.castTo(null, type, strType, o)
    }

    fun getTypeAsString(): String? {
        if (StringUtil.isEmpty(strType)) {
            strType = CFTypes.toString(type, "any")
        }
        return strType
    }

    fun getType(): Short {
        return type
    }

    companion object {
        private const val serialVersionUID = 2416933826309884176L
    }
}