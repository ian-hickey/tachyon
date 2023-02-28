package tachyon.runtime.type.scope.storage

import java.io.ByteArrayInputStream

class StorageValue(value: Struct?) : Serializable {
    @Transient
    private var value: Struct?
    private val lastModified: Long
    private val barr: ByteArray?
    fun lastModified(): Long {
        return lastModified
    }

    @Throws(PageException::class)
    fun getValue(): Struct? {
        if (value == null) {
            if (barr!!.size == 0) return null
            value = deserialize(barr)
        }
        return value
    }

    companion object {
        private const val serialVersionUID = 2728185742217909233L
        private val EMPTY: ByteArray? = ByteArray(0)
        @Throws(PageException::class)
        private fun deserialize(barr: ByteArray?): Struct? {
            if (barr == null || barr.size == 0) return null
            var ois: ObjectInputStream? = null
            var sct: Struct? = null
            try {
                ois = ObjectInputStream(ByteArrayInputStream(barr))
                sct = ois.readObject() as Struct
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            } finally {
                try {
                    IOUtil.close(ois)
                } catch (e: IOException) {
                    throw Caster.toPageException(e)
                }
            }
            return sct
        }

        @Throws(PageException::class)
        private fun serialize(sct: Struct?): ByteArray? {
            if (sct == null) return EMPTY
            val os = ByteArrayOutputStream()
            var oos: ObjectOutputStream? = null
            try {
                oos = ObjectOutputStream(os)
                oos.writeObject(sct)
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            } finally {
                try {
                    IOUtil.close(oos)
                } catch (e: IOException) {
                    throw Caster.toPageException(e)
                }
            }
            return os.toByteArray()
        }
    }

    init {
        this.value = value
        barr = serialize(value)
        lastModified = System.currentTimeMillis()
    }
}