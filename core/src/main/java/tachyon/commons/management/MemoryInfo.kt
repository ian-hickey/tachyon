/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.commons.management

import java.io.IOException

object MemoryInfo {
    var ALL = 0
    var PRIVATE_ONLY = 1
    var NON_PUBLIC = 2
    var NONE = 3
    private var access: IntArray

    /**
     * Returns an estimation of the "shallow" memory usage, in bytes, of the given object. The estimate
     * is provided by the running JVM and is likely to be as accurate a measure as can be reasonably
     * made by the running Java program. It will generally include memory taken up for "housekeeping" of
     * that object.
     *
     * The shallow memory usage does not count the memory used by objects referenced by obj.
     *
     * @param obj The object whose memory usage is to be estimated.
     * @return An estimate, in bytes, of the heap memory taken up by obj.
     */
    fun memoryUsageOf(inst: Instrumentation, obj: Object?): Long {
        return inst.getObjectSize(obj)
    }

    /**
     * returns an estimation, in bytes, of the memory usage of the given object plus (recursively)
     * objects it references via non-static private or protected fields. The estimate for each
     * individual object is provided by the running JVM and is likely to be as accurate a measure as can
     * be reasonably made by the running Java program. It will generally include memory taken up for
     * "housekeeping" of that object.
     *
     * @param obj The object whose memory usage (and that of objects it references) is to be estimated.
     * @return An estimate, in bytes, of the heap memory taken up by obj and objects it references via
     * private or protected non-static fields.
     */
    fun deepMemoryUsageOf(inst: Instrumentation, obj: Object?): Long {
        return deepMemoryUsageOf(inst, obj, NON_PUBLIC)
    }

    /**
     * Returns an estimation, in bytes, of the memory usage of the given object plus (recursively)
     * objects it references via non-static references. Which references are traversed depends on the
     * Visibility Filter passed in. The estimate for each individual object is provided by the running
     * JVM and is likely to be as accurate a measure as can be reasonably made by the running Java
     * program. It will generally include memory taken up for "housekeeping" of that object.
     *
     * @param obj The object whose memory usage (and that of objects it references) is to be estimated.
     * @param referenceFilter specifies which references are to be recursively included in the resulting
     * count (ALL,PRIVATE_ONLY,NON_PUBLIC,NONE).
     * @return An estimate, in bytes, of the heap memory taken up by obj and objects it references.
     */
    fun deepMemoryUsageOf(inst: Instrumentation, obj: Object?, referenceFilter: Int): Long {
        return deepMemoryUsageOf0(inst, HashSet<Integer>(), obj, referenceFilter)
    }

    /**
     * Returns an estimation, in bytes, of the memory usage of the given objects plus (recursively)
     * objects referenced via non-static references from any of those objects via non-public fields. If
     * two or more of the given objects reference the same Object X, then the memory used by Object X
     * will only be counted once. However, the method guarantees that the memory for a given object
     * (either in the passed-in collection or found while traversing the object graphs from those
     * objects) will not be counted more than once. The estimate for each individual object is provided
     * by the running JVM and is likely to be as accurate a measure as can be reasonably made by the
     * running Java program. It will generally include memory taken up for "housekeeping" of that
     * object.
     *
     * @param objs The collection of objects whose memory usage is to be totalled.
     * @return An estimate, in bytes, of the total heap memory taken up by the obejcts in objs and,
     * recursively, objects referenced by private or protected (non-static) fields.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun deepMemoryUsageOfAll(inst: Instrumentation, objs: Collection<Any?>): Long {
        return deepMemoryUsageOfAll(inst, objs, NON_PUBLIC)
    }

    /**
     * Returns an estimation, in bytes, of the memory usage of the given objects plus (recursively)
     * objects referenced via non-static references from any of those objects. Which references are
     * traversed depends on the VisibilityFilter passed in. If two or more of the given objects
     * reference the same Object X, then the memory used by Object X will only be counted once. However,
     * the method guarantees that the memory for a given object (either in the passed-in collection or
     * found while traversing the object graphs from those objects) will not be counted more than once.
     * The estimate for each individual object is provided by the running JVM and is likely to be as
     * accurate a measure as can be reasonably made by the running Java program. It will generally
     * include memory taken up for "housekeeping" of that object.
     *
     * @param objs The collection of objects whose memory usage is to be totalled.
     * @param referenceFilter Specifies which references are to be recursively included in the resulting
     * count (ALL,PRIVATE_ONLY,NON_PUBLIC,NONE).
     * @return An estimate, in bytes, of the total heap memory taken up by the obejcts in objs and,
     * recursively, objects referenced by any of those objects that match the VisibilityFilter
     * criterion.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun deepMemoryUsageOfAll(inst: Instrumentation, objs: Collection<Any?>, referenceFilter: Int): Long {
        var total = 0L
        val counted: Set<Integer> = HashSet<Integer>(objs.size() * 4)
        for (o in objs) {
            total += deepMemoryUsageOf0(inst, counted, o, referenceFilter)
        }
        return total
    }

    @Throws(SecurityException::class)
    private fun deepMemoryUsageOf0(instrumentation: Instrumentation, counted: Set<Integer>, obj: Object?, filter: Int): Long {
        val st: Stack<Object> = Stack<Object>()
        st.push(obj)
        var total = 0L
        while (!st.isEmpty()) {
            val o: Object = st.pop()
            if (counted.add(System.identityHashCode(o))) {
                val sz: Long = instrumentation.getObjectSize(o)
                total += sz
                var clz: Class = o.getClass()
                val compType: Class = clz.getComponentType()
                if (compType != null && !compType.isPrimitive()) {
                    val array: Array<Object>
                    array = o
                    val arr: Array<Object> = array
                    for (i in array.indices) {
                        val el: Object = array[i]
                        if (el != null) {
                            st.push(el)
                        }
                    }
                }
                while (clz != null) {
                    val declaredFields: Array<Field> = clz.getDeclaredFields()
                    for (j in declaredFields.indices) {
                        val fld: Field = declaredFields[j]
                        val mod: Int = fld.getModifiers()
                        if (mod and 0x8 == 0x0 && isOf(filter, mod)) {
                            val fieldClass: Class = fld.getType()
                            if (!fieldClass.isPrimitive()) {
                                if (!fld.isAccessible()) {
                                    fld.setAccessible(true)
                                }
                                try {
                                    val subObj: Object = fld.get(o)
                                    if (subObj != null) {
                                        st.push(subObj)
                                    }
                                } catch (illAcc: IllegalAccessException) {
                                    throw InternalError("Couldn't read $fld")
                                }
                            }
                        }
                    }
                    clz = clz.getSuperclass()
                }
            }
        }
        return total
    }

    private fun isOf(f: Int, mod: Int): Boolean {
        return when (access[f]) {
            1 -> true
            4 -> false
            2 -> mod and 0x2 != 0x0
            3 -> mod and 0x1 == 0x0
            else -> throw IllegalArgumentException("Illegal filter $mod")
        }
    }

    init {
        access = IntArray(4)
        access[ALL] = 1
        access[PRIVATE_ONLY] = 2
        access[NON_PUBLIC] = 3
        access[NONE] = 4
    }
}