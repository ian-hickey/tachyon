/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.component

import org.objectweb.asm.Type

/**
 */
class PropertyImpl : MemberSupport(Component.ACCESS_REMOTE), Property, ASMProperty {
    private var type: String? = "any"
    private var name: String? = null
    private var required = false
    private var setter = true
    private var getter = true
    private var _default: String? = null
    private var displayname: String? = ""
    private var hint: String? = ""
    private var dynAttrs: Struct? = StructImpl()
    private var metadata: Struct? = null
    private var ownerName: String? = null

    /**
     * @return the _default
     */
    @Override
    fun getDefault(): String? {
        return _default
    }

    /**
     * @param _default the _default to set
     */
    fun setDefault(_default: String?) {
        this._default = _default
    }

    /**
     * @return the displayname
     */
    @Override
    fun getDisplayname(): String? {
        return displayname
    }

    /**
     * @param displayname the displayname to set
     */
    fun setDisplayname(displayname: String?) {
        this.displayname = displayname
    }

    /**
     * @return the hint
     */
    @Override
    fun getHint(): String? {
        return hint
    }

    /**
     * @param hint the hint to set
     */
    fun setHint(hint: String?) {
        this.hint = hint
    }

    /**
     * @return the name
     */
    @Override
    fun getName(): String? {
        return name
    }

    /**
     * @param name the name to set
     */
    fun setName(name: String?) {
        this.name = name
    }

    /**
     * @return the required
     */
    @Override
    fun isRequired(): Boolean {
        return required
    }

    /**
     * @param required the required to set
     */
    fun setRequired(required: Boolean) {
        this.required = required
    }

    /**
     * @return the type
     */
    @Override
    fun getType(): String? {
        return type
    }

    /**
     * @param type the type to set
     */
    fun setType(type: String?) {
        this.type = type
    }

    @Override
    fun getValue(): Object? {
        return _default
    }

    @Override
    @Throws(PageException::class)
    fun getASMType(): Type? {
        return ASMUtil.toType(getType(), true)
    }

    /**
     * @return the setter
     */
    @Override
    fun getSetter(): Boolean {
        return setter
    }

    /**
     * @param setter the setter to set
     */
    fun setSetter(setter: Boolean) {
        this.setter = setter
    }

    /**
     * @return the getter
     */
    @Override
    fun getGetter(): Boolean {
        return getter
    }

    /**
     * @param getter the getter to set
     */
    fun setGetter(getter: Boolean) {
        this.getter = getter
    }

    @Override
    fun getMetaData(): Object? {
        val sct: Struct = StructImpl()

        // meta
        if (metadata != null) StructUtil.copy(metadata, sct, true)
        sct.setEL(KeyConstants._name, name)
        if (!StringUtil.isEmpty(hint, true)) sct.setEL(KeyConstants._hint, hint)
        if (!StringUtil.isEmpty(displayname, true)) sct.setEL(KeyConstants._displayname, displayname)
        if (!StringUtil.isEmpty(type, true)) sct.setEL(KeyConstants._type, type)

        // dyn attributes
        StructUtil.copy(dynAttrs, sct, true)
        return sct
    }

    @Override
    fun getDynamicAttributes(): Struct? {
        return dynAttrs
    }

    @Override
    fun getMeta(): Struct? {
        if (metadata == null) metadata = StructImpl()
        return metadata
    }

    @Override
    fun getClazz(): Class? {
        return null
    }

    @Override
    fun isPeristent(): Boolean {
        return Caster.toBooleanValue(dynAttrs.get(KeyConstants._persistent, Boolean.TRUE), true)
    }

    fun setOwnerName(ownerName: String?) {
        this.ownerName = ownerName
    }

    @Override
    fun getOwnerName(): String? {
        return ownerName
    }

    @Override
    override fun toString(): String {
        var strDynAttrs = ""
        try {
            strDynAttrs = ScriptConverter().serialize(dynAttrs)
        } catch (ce: ConverterException) {
        }
        return ("default:" + _default + ";displayname:" + displayname + ";hint:" + hint + ";name:" + name + ";type:" + type + ";ownerName:" + ownerName
                + ";attrs:" + strDynAttrs + ";")
    }

    @Override
    override fun equals(obj: Object?): Boolean {
        if (this === obj) return true
        if (obj !is Property) return false
        val other: Property? = obj as Property?
        return toString().equals(other.toString())
    }

    @Override
    fun duplicate(deepCopy: Boolean): Object? {
        val other = PropertyImpl()
        other._default = _default
        other.displayname = displayname
        other.getter = getter
        other.hint = hint
        other.dynAttrs = if (deepCopy) Duplicator.duplicate(dynAttrs, deepCopy) as Struct else dynAttrs
        other.name = name
        other.ownerName = ownerName
        other.required = required
        other.setter = setter
        other.type = type
        return other
    }

    companion object {
        private const val serialVersionUID = 3206074213415946902L
    }
}