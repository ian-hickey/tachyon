/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
 * a single argument of a function, this is lightway function, just contain name and type (return
 * default value for the rest)
 */
class FunctionArgumentLight : FunctionArgument, Externalizable {
    private var name: Collection.Key? = null
    private var type: Short = 0
    private var strType: String? = null
    private var required = false

    /**
     * NEVER USE THIS CONSTRUCTOR, this constructor is only for deserialize this object from stream
     */
    constructor() {}
    constructor(name: Collection.Key?) : this(name, "any", CFTypes.TYPE_ANY) {}
    constructor(name: Collection.Key?, type: Short) : this(name, CFTypes.toString(type, "any"), type) {}
    constructor(name: String?, type: Short) : this(KeyImpl.init(name), CFTypes.toString(type, "any"), type) {}
    constructor(name: Collection.Key?, strType: String?, type: Short) {
        this.name = name
        this.strType = strType
        this.type = type
    }

    constructor(name: Collection.Key?, strType: String?, type: Short, required: Boolean) {
        this.name = name
        this.strType = strType
        this.type = type
        this.required = required
    }

    /**
     * @return the defaultType
     */
    @Override
    fun getDefaultType(): Int {
        return DEFAULT_TYPE_NULL
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
        return ""
    }

    @Override
    fun getDisplayName(): String? {
        return ""
    }

    @Override
    fun getMetaData(): Struct? {
        return null
    }

    @Override
    fun isPassByReference(): Boolean {
        return true
    }

    @Override
    @Throws(IOException::class, ClassNotFoundException::class)
    fun readExternal(`in`: ObjectInput?) {
        name = KeyImpl.init(ExternalizableUtil.readString(`in`))
        type = `in`.readShort()
        strType = ExternalizableUtil.readString(`in`)
    }

    @Override
    @Throws(IOException::class)
    fun writeExternal(out: ObjectOutput?) {
        ExternalizableUtil.writeString(out, name.getString())
        out.writeShort(type)
        ExternalizableUtil.writeString(out, strType)
    }

    @Override
    override fun equals(obj: Object?): Boolean {
        return if (obj !is FunctionArgument) false else FunctionArgumentImpl.equals(this, obj as FunctionArgument?)
    }

    companion object {
        private const val serialVersionUID = 817360221819952381L // do not change
    }
}