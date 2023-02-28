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
package tachyon.runtime.functions.system

import java.util.Iterator

object BundleInfo : Function {
    private const val serialVersionUID = 3928190461638362170L
    @Throws(PageException::class)
    fun call(pc: PageContext?, obj: Object?): Struct? {
        if (obj == null) throw FunctionException(pc, "bundleInfo", 1, "object", "value is null")
        val clazz: Class<*>
        clazz = if (obj is JavaObject) (obj as JavaObject?).getClazz() else if (obj is ObjectWrap) (obj as ObjectWrap?).getEmbededObject().getClass() else obj.getClass()
        val cl: ClassLoader = clazz.getClassLoader()
        if (cl is BundleClassLoader) {
            val bcl: BundleClassLoader = cl as BundleClassLoader
            val b: Bundle = bcl.getBundle()
            val sct: Struct = StructImpl()
            sct.setEL(KeyConstants._id, b.getBundleId())
            sct.setEL(KeyConstants._name, b.getSymbolicName())
            sct.setEL(KeyConstants._location, b.getLocation())
            sct.setEL(KeyConstants._version, b.getVersion().toString())
            sct.setEL(KeyConstants._state, OSGiUtil.toState(b.getState(), null))
            try {
                sct.setEL("requiredBundles", toArray1(OSGiUtil.getRequiredBundles(b)))
                sct.setEL("requiredPackages", toArray2(OSGiUtil.getRequiredPackages(b)))
            } catch (be: BundleException) {
                throw Caster.toPageException(be)
            }
            return sct
        }
        throw ApplicationException("object [$clazz] is not from an OSGi bundle")
    }

    private fun toArray1(list: List<BundleDefinition?>?): Array? {
        var sct: Struct?
        val arr: Array = ArrayImpl()
        val it: Iterator<BundleDefinition?> = list!!.iterator()
        var bd: BundleDefinition?
        var vd: VersionDefinition
        while (it.hasNext()) {
            bd = it.next()
            sct = StructImpl()
            sct.setEL(KeyConstants._bundleName, bd.getName())
            vd = bd.getVersionDefiniton()
            if (vd != null) {
                sct.setEL(KeyConstants._bundleVersion, vd.getVersionAsString())
                sct.setEL("operator", vd.getOpAsString())
            }
            arr.appendEL(sct)
        }
        return arr
    }

    private fun toArray2(list: List<PackageQuery?>?): Array? {
        var sct: Struct?
        var _sct: Struct?
        val arr: Array = ArrayImpl()
        var _arr: Array?
        val it: Iterator<PackageQuery?> = list!!.iterator()
        var pd: PackageQuery?
        var _it: Iterator<VersionDefinition?>
        var vd: VersionDefinition?
        while (it.hasNext()) {
            pd = it.next()
            sct = StructImpl()
            sct.setEL(KeyConstants._package, pd.getName())
            sct.setEL("versions", ArrayImpl().also { _arr = it })
            _it = pd.getVersionDefinitons().iterator()
            while (_it.hasNext()) {
                vd = _it.next()
                _sct = StructImpl()
                _sct.setEL(KeyConstants._bundleVersion, vd.getVersion().toString())
                _sct.setEL("operator", vd.getOpAsString())
                _arr.appendEL(_sct)
            }
            arr.appendEL(sct)
        }
        return arr
    }
}