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
package tachyon.runtime.tag

import tachyon.runtime.Component

/**
 * Defines components as complex types that are used for web services authoring. The attributes of
 * this tag are exposed as component metadata and are subject to inheritance rules.
 *
 *
 *
 */
class Property : TagImpl(), DynamicAttributes {
    private var property: tachyon.runtime.component.PropertyImpl? = PropertyImpl()
    @Override
    fun release() {
        super.release()
        property = PropertyImpl()
    }

    @Override
    fun setDynamicAttribute(uri: String?, name: String?, value: Object?) {
        property!!.getDynamicAttributes().setEL(KeyImpl.init(name), value)
    }

    @Override
    fun setDynamicAttribute(uri: String?, name: Collection.Key?, value: Object?) {
        property!!.getDynamicAttributes().setEL(name, value)
    }

    fun setMetaData(name: String?, value: Object?) {
        property!!.getMeta().setEL(KeyImpl.init(name), value)
    }

    /**
     * set the value type A string; a property type name; data type.
     *
     * @param type value to set
     */
    fun setType(type: String?) {
        property!!.setType(type)
        setDynamicAttribute(null, KeyConstants._type, type)
    }

    /**
     * set the value name A string; a property name. Must be a static value.
     *
     * @param name value to set
     */
    fun setName(name: String?) {
        // Fix for axis 1.4, axis can not handle when first char is upper case
        // name=StringUtil.lcFirst(name.toLowerCase());
        property!!.setName(name)
        setDynamicAttribute(null, KeyConstants._name, name)
    }

    /**
     * @param _default The _default to set.
     */
    fun setDefault(_default: String?) {
        property!!.setDefault(_default)
        setDynamicAttribute(null, "default", _default)
    }

    /**
     * @param access The access to set.
     * @throws ExpressionException
     */
    @Throws(ApplicationException::class)
    fun setAccess(access: String?) {
        setDynamicAttribute(null, "access", access)
        property!!.setAccess(access)
    }

    /**
     * @param displayname The displayname to set.
     */
    fun setDisplayname(displayname: String?) {
        property!!.setDisplayname(displayname)
        setDynamicAttribute(null, "displayname", displayname)
    }

    /**
     * @param hint The hint to set.
     */
    fun setHint(hint: String?) {
        property!!.setHint(hint)
        setDynamicAttribute(null, "hint", hint)
    }

    /**
     * @param required The required to set.
     */
    fun setRequired(required: Boolean) {
        property!!.setRequired(required)
        setDynamicAttribute(null, "required", if (required) "yes" else "no")
    }

    fun setSetter(setter: Boolean) {
        property!!.setSetter(setter)
        setDynamicAttribute(null, "setter", if (setter) "yes" else "no")
    }

    fun setGetter(setter: Boolean) {
        property!!.setGetter(setter)
        setDynamicAttribute(null, "getter", if (setter) "yes" else "no")
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        if (pageContext.variablesScope() is ComponentScope) {
            val comp: Component = (pageContext.variablesScope() as ComponentScope).getComponent()
            comp.setProperty(property)
            property!!.setOwnerName(comp.getAbsName())
        }
        return SKIP_BODY
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }
}