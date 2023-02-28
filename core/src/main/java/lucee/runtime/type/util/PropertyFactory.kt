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
package lucee.runtime.type.util

import lucee.commons.lang.StringUtil

object PropertyFactory {
    val SINGULAR_NAME: Collection.Key? = KeyImpl.getInstance("singularName")
    val FIELD_TYPE: Key? = KeyConstants._fieldtype
    @Throws(PageException::class)
    fun createPropertyUDFs(comp: ComponentImpl?, property: Property?) {
        // getter
        if (property.getGetter()) {
            addGet(comp, property)
        }
        // setter
        if (property.getSetter()) {
            addSet(comp, property)
        }
        val fieldType: String = Caster.toString(property.getDynamicAttributes().get(FIELD_TYPE, null), null)

        // add
        if (fieldType != null) {
            if ("one-to-many".equalsIgnoreCase(fieldType) || "many-to-many".equalsIgnoreCase(fieldType)) {
                addHas(comp, property)
                addAdd(comp, property)
                addRemove(comp, property)
            } else if ("one-to-one".equalsIgnoreCase(fieldType) || "many-to-one".equalsIgnoreCase(fieldType)) {
                addHas(comp, property)
            }
        }
    }

    @Throws(ApplicationException::class)
    fun addGet(comp: ComponentImpl?, prop: Property?) {
        val m: Member = comp.getMember(Component.ACCESS_PRIVATE, KeyImpl.init("get" + prop.getName()), true, false)
        if (m !is UDF) {
            val udf: UDF = UDFGetterProperty(comp, prop)
            comp.registerUDF(KeyImpl.init(udf.getFunctionName()), udf)
        }
    }

    @Throws(PageException::class)
    fun addSet(comp: ComponentImpl?, prop: Property?) {
        val m: Member = comp.getMember(Component.ACCESS_PRIVATE, KeyImpl.init("set" + prop.getName()), true, false)
        if (m !is UDF) {
            val udf: UDF = UDFSetterProperty(comp, prop)
            comp.registerUDF(KeyImpl.init(udf.getFunctionName()), udf)
        }
    }

    @Throws(ApplicationException::class)
    fun addHas(comp: ComponentImpl?, prop: Property?) {
        val m: Member = comp.getMember(Component.ACCESS_PRIVATE, KeyImpl.init("has" + getSingularName(prop)), true, false)
        if (m !is UDF) {
            val udf: UDF = UDFHasProperty(comp, prop)
            comp.registerUDF(KeyImpl.init(udf.getFunctionName()), udf)
        }
    }

    @Throws(ApplicationException::class)
    fun addAdd(comp: ComponentImpl?, prop: Property?) {
        val m: Member = comp.getMember(Component.ACCESS_PRIVATE, KeyImpl.init("add" + getSingularName(prop)), true, false)
        if (m !is UDF) {
            val udf: UDF = UDFAddProperty(comp, prop)
            comp.registerUDF(KeyImpl.init(udf.getFunctionName()), udf)
        }
    }

    @Throws(ApplicationException::class)
    fun addRemove(comp: ComponentImpl?, prop: Property?) {
        val m: Member = comp.getMember(Component.ACCESS_PRIVATE, KeyImpl.init("remove" + getSingularName(prop)), true, false)
        if (m !is UDF) {
            val udf: UDF = UDFRemoveProperty(comp, prop)
            comp.registerUDF(KeyImpl.init(udf.getFunctionName()), udf)
        }
    }

    fun getSingularName(prop: Property?): String? {
        val singularName: String = Caster.toString(prop.getDynamicAttributes().get(SINGULAR_NAME, null), null)
        return if (!StringUtil.isEmpty(singularName)) singularName else prop.getName()
    }

    fun getType(prop: Property?): String? {
        val type: String = prop.getType()
        if (StringUtil.isEmpty(type) || "any".equalsIgnoreCase(type) || "object".equalsIgnoreCase(type)) {
            val fieldType: String = Caster.toString(prop.getDynamicAttributes().get(FIELD_TYPE, null), null)
            return if ("one-to-many".equalsIgnoreCase(fieldType) || "many-to-many".equalsIgnoreCase(fieldType)) {
                "array"
            } else "any"
        }
        return type
    }
}