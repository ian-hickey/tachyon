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
package tachyon.intergral.fusiondebug.server.type.nat

import java.lang.reflect.Field

class FDNative(frame: IFDStackFrame?, name: String?, value: Object?) : FDValueNotMutability() {
    private val children: ArrayList? = ArrayList()
    private val name: String?
    private val value: Object?
    @Override
    fun getChildren(): List? {
        return children
    }

    fun getName(): String? {
        return name
    }

    @Override
    fun hasChildren(): Boolean {
        return true
    }

    @Override
    override fun toString(): String {
        return Caster.toClassName(value)
    }

    /**
     * Constructor of the class
     *
     * @param frame
     * @param name
     * @param coll
     */
    init {
        var value: Object? = value
        this.name = name
        if (value is ObjectWrap) {
            value = (value as ObjectWrap?).getEmbededObject(value)
        }
        this.value = value
        val clazz: Class = value.getClass()

        // super
        children.add(FDSimpleVariable(frame, "Extends", ClassUtil.getName(clazz.getSuperclass()), null))

        // interfaces
        val faces: Array<Class?> = clazz.getInterfaces()
        if (faces.size > 0) {
            val list = ArrayList()
            children.add(FDSimpleVariable(frame, "Interfaces", "", list))
            for (i in faces.indices) {
                list.add(FDSimpleVariable(frame, "[" + (i + 1) + "]", ClassUtil.getName(faces[i]), null))
            }
        }
        var el: ArrayList?
        var list: ArrayList?

        // fields
        val flds: Array<Field?> = clazz.getFields()
        if (flds.size > 0) {
            var fld: Field?
            list = ArrayList()
            children.add(FDSimpleVariable(frame, "Fields", "", list))
            for (i in flds.indices) {
                fld = flds[i]
                el = ArrayList()
                list.add(FDSimpleVariable(frame, fld.getName(), "", el))
                el.add(FDSimpleVariable(frame, "Type", ClassUtil.getName(fld.getType()), null))
                el.add(FDSimpleVariable(frame, "Modifier", Modifier.toString(fld.getModifiers()), null))
            }
        }
        // methods
        val mths: Array<Method?> = clazz.getMethods()
        if (mths.size > 0) {
            var mth: Method?
            list = ArrayList()
            children.add(FDSimpleVariable(frame, "Methods", "", list))
            for (i in mths.indices) {
                mth = mths[i]
                el = ArrayList()
                list.add(FDSimpleVariable(frame, mth.getName(), "", el))
                el.add(FDSimpleVariable(frame, "Modifier", Modifier.toString(mth.getModifiers()), null))

                // exceptions
                val clsTypes: Array<Class?> = mth.getExceptionTypes()
                if (clsTypes.size > 0) {
                    val exps = ArrayList()
                    el.add(FDSimpleVariable(frame, "Exceptions", Caster.toString(clsTypes.size), exps))
                    for (y in clsTypes.indices) {
                        exps.add(FDSimpleVariable(frame, "[" + (y + 1) + "]", ClassUtil.getName(clsTypes[y]), null))
                    }
                }

                // params
                val clsParams: Array<Class?> = mth.getParameterTypes()
                if (clsParams.size > 0) {
                    val params = ArrayList()
                    el.add(FDSimpleVariable(frame, "Parameters", Caster.toString(clsParams.size), params))
                    for (y in clsParams.indices) {
                        params.add(FDSimpleVariable(frame, "[" + (y + 1) + "]", ClassUtil.getName(clsParams[y]), null))
                    }
                }

                // return
                el.add(FDSimpleVariable(frame, "Return", ClassUtil.getName(mth.getReturnType()), null))
            }
        }
    }
}