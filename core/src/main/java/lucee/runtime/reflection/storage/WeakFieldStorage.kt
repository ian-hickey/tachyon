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
package lucee.runtime.reflection.storage

import java.lang.reflect.Field

/**
 * Method Storage Class
 */
class WeakFieldStorage {
    private val map: WeakHashMap? = WeakHashMap()

    /**
     * returns all fields matching given criteria or null if field does exist
     *
     * @param clazz clazz to get field from
     * @param fieldname Name of the Field to get
     * @return matching Fields as Array
     */
    fun getFields(clazz: Class?, fieldname: String?): Array<Field?>? {
        var fieldMap: Struct
        var o: Object
        synchronized(map) {
            o = map.get(clazz)
            fieldMap = if (o == null) {
                store(clazz)
            } else o as Struct
        }
        o = fieldMap.get(fieldname, null)
        return if (o == null) null else o
    }

    /**
     * store a class with his methods
     *
     * @param clazz
     * @return returns stored Struct
     */
    private fun store(clazz: Class?): StructImpl? {
        val fieldsArr: Array<Field?> = clazz.getFields()
        val fieldsMap = StructImpl()
        for (i in fieldsArr.indices) {
            storeField(fieldsArr[i], fieldsMap)
        }
        map.put(clazz, fieldsMap)
        return fieldsMap
    }

    /**
     * stores a single method
     *
     * @param field
     * @param fieldsMap
     */
    private fun storeField(field: Field?, fieldsMap: StructImpl?) {
        val fieldName: String = field.getName()
        val o: Object = fieldsMap.get(fieldName, null)
        val args: Array<Field?>?
        if (o == null) {
            args = arrayOfNulls<Field?>(1)
            fieldsMap.setEL(fieldName, args)
        } else {
            val fs: Array<Field?> = o
            args = arrayOfNulls<Field?>(fs.size + 1)
            for (i in fs.indices) {
                fs[i].setAccessible(true)
                args!![i] = fs[i]
            }
            fieldsMap.setEL(fieldName, args)
        }
        args!![args.size - 1] = field
    }
}