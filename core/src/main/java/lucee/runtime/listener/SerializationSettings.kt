package lucee.runtime.listener

import lucee.commons.lang.StringUtil

class SerializationSettings(preserveCaseForStructKey: Boolean, preserveCaseForQueryColumn: Boolean, serializeQueryAs: Int) {
    private val preserveCaseForStructKey = true
    private val preserveCaseForQueryColumn = false
    private val serializeQueryAs = SERIALIZE_AS_ROW
    fun getPreserveCaseForStructKey(): Boolean {
        return preserveCaseForStructKey
    }

    fun getPreserveCaseForQueryColumn(): Boolean {
        return preserveCaseForQueryColumn
    }

    fun getSerializeQueryAs(): Int {
        return serializeQueryAs
    }

    fun toStruct(): Object? {
        val sct: Struct = StructImpl()
        sct.setEL("preserveCaseForStructKey", preserveCaseForStructKey)
        sct.setEL("preserveCaseForQueryColumn", preserveCaseForQueryColumn)
        sct.setEL("serializeQueryAs", toSerializeQueryAs(serializeQueryAs))
        return sct
    }

    companion object {
        var SERIALIZE_AS_UNDEFINED = 0
        var SERIALIZE_AS_ROW = 1
        var SERIALIZE_AS_COLUMN = 2
        var SERIALIZE_AS_STRUCT = 4
        val DEFAULT: SerializationSettings? = SerializationSettings(true, true, SERIALIZE_AS_ROW)
        fun toSerializeQueryAs(str: String?): Int {
            var str = str
            if (StringUtil.isEmpty(str)) return SERIALIZE_AS_ROW
            str = str.trim()
            if ("column".equalsIgnoreCase(str)) return SERIALIZE_AS_COLUMN
            return if ("struct".equalsIgnoreCase(str)) SERIALIZE_AS_STRUCT else SERIALIZE_AS_ROW
        }

        fun toSerializeQueryAs(i: Int): String? {
            if (i == SERIALIZE_AS_COLUMN) return "column"
            return if (i == SERIALIZE_AS_STRUCT) "struct" else "row"
        }

        fun toSerializationSettings(sct: Struct?): SerializationSettings? {
            return SerializationSettings(Caster.toBooleanValue(sct.get("preserveCaseForStructKey", null), true),
                    Caster.toBooleanValue(sct.get("preserveCaseForQueryColumn", null), false), toSerializeQueryAs(Caster.toString(sct.get("serializeQueryAs", null), null)))
        }
    }

    init {
        this.preserveCaseForStructKey = preserveCaseForStructKey
        this.preserveCaseForQueryColumn = preserveCaseForQueryColumn
        this.serializeQueryAs = serializeQueryAs
    }
}