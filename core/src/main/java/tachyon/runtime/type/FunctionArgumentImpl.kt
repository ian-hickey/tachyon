/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 *
 */
package tachyon.runtime.type

import java.io.Externalizable

/**
 * a single argument of a function
 */
class FunctionArgumentImpl : FunctionArgument, Externalizable {
    private var dspName: String? = null
    private var hint: String? = null
    private var name: Collection.Key? = null
    private var type: Short = 0
    private var strType: String? = null
    private var required = false
    private var meta: Struct? = null
    private var defaultType = 0
    private var passByReference = false

    @Deprecated
    @Deprecated("use other constructor ")
    constructor(name: String?, type: String?, required: Boolean) : this(name, type, required, "", "") {
    }

    @Deprecated
    @Deprecated("use other constructor ")
    constructor(name: String?, type: String?, required: Boolean, dspName: String?, hint: String?) : this(name, type, required, DEFAULT_TYPE_RUNTIME_EXPRESSION, true, dspName, hint, null) {
    }

    @Deprecated
    @Deprecated("use other constructor ")
    constructor(name: String?, type: String?, required: Boolean, dspName: String?, hint: String?, meta: StructImpl?) : this(name, type, required, DEFAULT_TYPE_RUNTIME_EXPRESSION, true, dspName, hint, meta) {
    }

    @Deprecated
    @Deprecated("use other constructor ")
    constructor(name: String?, type: String?, required: Boolean, defaultType: Int, dspName: String?, hint: String?, meta: StructImpl?) : this(name, type, required, defaultType, true, dspName, hint, meta) {
    }

    @Deprecated
    @Deprecated("use other constructor ")
    constructor(name: String?, type: String?, required: Boolean, defaultType: Double, dspName: String?, hint: String?, meta: StructImpl?) : this(name, type, required, defaultType.toInt(), true, dspName, hint, meta) {
    }

    @Deprecated
    @Deprecated("use other constructor ")
    constructor(name: String?, type: String?, required: Boolean, defaultType: Double, passByReference: Boolean, dspName: String?, hint: String?, meta: StructImpl?) : this(name, type, required, defaultType.toInt(), passByReference, dspName, hint, meta) {
    }

    @Deprecated
    @Deprecated("use other constructor ")
    constructor(name: String?, type: String?, required: Boolean, defaultType: Int, passByReference: Boolean, dspName: String?, hint: String?, meta: StructImpl?) : this(KeyImpl.init(name), type, required, defaultType, passByReference, dspName, hint, meta) {
    }

    @Deprecated
    @Deprecated("use other constructor ")
    constructor(name: String?, strType: String?, type: Short, required: Boolean, defaultType: Int, passByReference: Boolean, dspName: String?, hint: String?, meta: StructImpl?) : this(KeyImpl.init(name), strType, type, required, defaultType, passByReference, dspName, hint, meta) {
    }

    @Deprecated
    @Deprecated("use other constructor ")
    constructor(name: Collection.Key?, type: String?, required: Boolean, defaultType: Int, passByReference: Boolean, dspName: String?, hint: String?, meta: StructImpl?) {
        this.name = name
        strType = type
        this.type = CFTypes.toShortStrict(type, CFTypes.TYPE_UNKNOW)
        this.required = required
        this.defaultType = defaultType
        this.dspName = dspName
        this.hint = hint
        this.meta = meta
        this.passByReference = passByReference
    }

    /**
     * NEVER USE THIS CONSTRUCTOR, this constructor is only for deserialize this object from stream
     */
    constructor() {}
    constructor(name: Collection.Key?) : this(name, "any", CFTypes.TYPE_ANY, false, DEFAULT_TYPE_NULL, true, "", "", null) {}
    constructor(name: Collection.Key?, type: Short) : this(name, CFTypes.toString(type, "any"), type, false, DEFAULT_TYPE_NULL, true, "", "", null) {}
    constructor(name: Collection.Key?, strType: String?, type: Short) : this(name, strType, type, false, DEFAULT_TYPE_NULL, true, "", "", null) {}
    constructor(name: Collection.Key?, strType: String?, type: Short, required: Boolean) : this(name, strType, type, required, DEFAULT_TYPE_NULL, true, "", "", null) {}
    constructor(name: Collection.Key?, strType: String?, type: Short, required: Boolean, defaultType: Int) : this(name, strType, type, required, defaultType, true, "", "", null) {}
    constructor(name: Collection.Key?, strType: String?, type: Short, required: Boolean, defaultType: Int, passByReference: Boolean) : this(name, strType, type, required, defaultType, passByReference, "", "", null) {}
    constructor(name: Collection.Key?, strType: String?, type: Short, required: Boolean, defaultType: Int, passByReference: Boolean, dspName: String?) : this(name, strType, type, required, defaultType, passByReference, dspName, "", null) {}
    constructor(name: Collection.Key?, strType: String?, type: Short, required: Boolean, defaultType: Int, passByReference: Boolean, dspName: String?, hint: String?) : this(name, strType, type, required, defaultType, passByReference, dspName, hint, null) {}
    constructor(name: Collection.Key?, strType: String?, type: Short, required: Boolean, defaultType: Int, passByReference: Boolean, dspName: String?, hint: String?,
                meta: StructImpl?) {
        this.name = name
        this.strType = strType
        this.type = type
        this.required = required
        this.defaultType = defaultType
        this.dspName = dspName
        this.hint = hint
        this.meta = meta
        this.passByReference = passByReference
    }
    // private static StructImpl sct=new StructImpl();
    /**
     * @return the defaultType
     */
    @Override
    fun getDefaultType(): Int {
        return defaultType
    }

    @Override
    fun getName(): Collection.Key? {
        return name
    }

    @Override
    fun isRequired(): Boolean {
        return required
    }

    @Override
    fun getType(): Short {
        return type
    }

    @Override
    fun getTypeAsString(): String? {
        return strType
    }

    @Override
    fun getHint(): String? {
        return hint
    }

    @Override
    fun getDisplayName(): String? {
        return dspName
    }

    @Override
    fun getMetaData(): Struct? {
        return meta
    }

    @Override
    fun isPassByReference(): Boolean {
        return passByReference
    }

    @Override
    @Throws(IOException::class, ClassNotFoundException::class)
    fun readExternal(`in`: ObjectInput?) {
        dspName = ExternalizableUtil.readString(`in`)
        hint = ExternalizableUtil.readString(`in`)
        name = KeyImpl.init(ExternalizableUtil.readString(`in`))
        type = `in`.readShort()
        strType = ExternalizableUtil.readString(`in`)
        required = `in`.readBoolean()
        meta = `in`.readObject() as Struct
        defaultType = `in`.readInt()
        passByReference = `in`.readBoolean()
    }

    @Override
    @Throws(IOException::class)
    fun writeExternal(out: ObjectOutput?) {
        ExternalizableUtil.writeString(out, dspName)
        ExternalizableUtil.writeString(out, hint)
        ExternalizableUtil.writeString(out, name.getString())
        out.writeShort(type)
        ExternalizableUtil.writeString(out, strType)
        out.writeBoolean(required)
        out.writeObject(meta)
        out.writeInt(defaultType)
        out.writeBoolean(passByReference)
    }

    @Override
    override fun equals(obj: Object?): Boolean {
        return if (obj !is FunctionArgument) false else equals(this, obj as FunctionArgument?)
    }

    companion object {
        private const val serialVersionUID = -7275048405949174352L // do not change
        fun equals(left: FunctionArgument?, right: FunctionArgument?): Boolean {
            return if (left.getDefaultType() !== right.getDefaultType() || left.getType() !== right.getType() || !_eq(left.getName(), right.getName())
                    || !_eq(left.getTypeAsString(), right.getTypeAsString()) || left.isPassByReference() !== right.isPassByReference() || left.isRequired() !== right.isRequired()) false else true
        }

        private fun _eq(left: Object?, right: Object?): Boolean {
            return if (left == null) right == null else left.equals(right)
        }
    }
}