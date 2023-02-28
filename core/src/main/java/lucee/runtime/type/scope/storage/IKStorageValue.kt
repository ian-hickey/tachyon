package lucee.runtime.type.scope.storage

import java.io.ByteArrayInputStream

class IKStorageValue : Serializable {
    @Transient
    var value: Map<Collection.Key?, IKStorageScopeItem?>? = null
    val lastModified: Long
    val barr: ByteArray?

    constructor(value: Map<Collection.Key?, IKStorageScopeItem?>?) : this(value, serialize(value), System.currentTimeMillis()) {}

    // DO NOT CHANGE, USED BY REDIS EXTENSION
    constructor(value: Map<Collection.Key?, IKStorageScopeItem?>?, barr: ByteArray?, lastModified: Long) {
        this.value = value
        this.barr = barr
        this.lastModified = lastModified
    }

    constructor(barrr: Array<ByteArray?>?) {
        barr = barrr!![0]
        lastModified = toLong(barrr[1])
    }

    fun lastModified(): Long {
        return lastModified
    }

    @Throws(PageException::class)
    fun getValue(): Map<Collection.Key?, IKStorageScopeItem?>? {
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
        fun toByteRepresentation(value: Map<Collection.Key?, IKStorageScopeItem?>?): Array<ByteArray?>? {
            return arrayOf(serialize(value), NumberUtil.longToByteArray(System.currentTimeMillis()))
        }

        @Throws(PageException::class)
        fun toByteRepresentation(`val`: IKStorageValue?): Array<ByteArray?>? {
            return arrayOf(`val`!!.barr, NumberUtil.longToByteArray(`val`.lastModified))
        }

        fun toLong(barr: ByteArray?): Long {
            return NumberUtil.byteArrayToLong(barr)
        }

        @Throws(PageException::class)
        fun deserialize(barr: ByteArray?): Map<Collection.Key?, IKStorageScopeItem?>? {
            if (barr == null || barr.size == 0) return null
            var ois: ObjectInputStream? = null
            var data: Map<Collection.Key?, IKStorageScopeItem?>? = null
            try {
                ois = ObjectInputStream(ByteArrayInputStream(barr))
                data = ois.readObject()
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            } finally {
                try {
                    IOUtil.close(ois)
                } catch (e: IOException) {
                    throw Caster.toPageException(e)
                }
            }
            return data
        }

        @Throws(PageException::class)
        fun serialize(data: Map<Collection.Key?, IKStorageScopeItem?>?): ByteArray? {
            if (data == null) return EMPTY
            val os = ByteArrayOutputStream()
            var oos: ObjectOutputStream? = null
            try {
                oos = ObjectOutputStream(os)
                oos.writeObject(data)
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
}