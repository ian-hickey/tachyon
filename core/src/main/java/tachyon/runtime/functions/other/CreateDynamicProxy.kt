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
package tachyon.runtime.functions.other

import java.io.IOException

object CreateDynamicProxy : Function {
    private const val serialVersionUID = -1787490871697335220L
    @Throws(PageException::class)
    fun call(pc: PageContext?, oCFC: Object?, oInterfaces: Object?): Object? {
        return try {
            _call(pc, oCFC, oInterfaces)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(PageException::class, IOException::class, BundleException::class)
    fun _call(pc: PageContext?, oCFC: Object?, oInterfaces: Object?): Object? {
        var oInterfaces: Object? = oInterfaces
        if (SystemUtil.getLoaderVersion() < 5.9) throw ApplicationException(
                "You need to update your tachyon.jar to execute the function [createDynamicProxy], you can download the latest jar from https://download.tachyon.org.")

        // Component
        val cfc: Component
        cfc = if (oCFC is Component) oCFC as Component? else pc.loadComponent(Caster.toString(oCFC))

        // string list to array
        if (Decision.isString(oInterfaces)) {
            val list: String = Caster.toString(oInterfaces)
            oInterfaces = ListUtil.listToStringArray(list, ',')
        }
        var interfaces: Array<Class?>? = null
        if (Decision.isArray(oInterfaces)) {
            val arr: Array<Object?> = Caster.toNativeArray(oInterfaces)
            val cl: ClassLoader = (pc as PageContextImpl?).getClassLoader()
            interfaces = arrayOfNulls<Class?>(arr.size)
            for (i in arr.indices) {
                if (arr[i] is JavaObject) interfaces!![i] = (arr[i] as JavaObject?).getClazz() else if (Decision.isStruct(arr[i])) interfaces!![i] = toClass(pc, cl, arr[i] as Struct?) else interfaces!![i] = ClassUtil.loadClass(cl, Caster.toString(arr[i]))
            }
            // strInterfaces=ListUtil.toStringArray(Caster.toArray(oInterfaces));
        } else if (oInterfaces is JavaObject) {
            interfaces = arrayOf<Class?>((oInterfaces as JavaObject?).getClazz())
        } else if (oInterfaces is Struct) {
            val cl: ClassLoader = (pc as PageContextImpl?).getClassLoader()
            interfaces = arrayOf<Class?>(toClass(pc, cl, oInterfaces as Struct?))
        } else throw FunctionException(pc, "CreateDynamicProxy", 2, "interfaces", "invalid type [" + Caster.toClassName(oInterfaces).toString() + "] for class definition")
        return _call(pc, cfc, interfaces)
    }

    @Throws(PageException::class, IOException::class, BundleException::class)
    fun _call(pc: PageContext?, cfc: Component?, interfaces: Array<Class?>?): Object? {

        // check if all classes are interfaces
        for (i in interfaces.indices) {
            if (!interfaces!![i].isInterface()) throw FunctionException(pc, "CreateDynamicProxy", 2, "interfaces", "definition [" + interfaces[i].getClass().toString() + "] is a class and not a interface")
        }
        return JavaProxyFactory.createProxy(pc, cfc, null, interfaces)
    }

    @Throws(FunctionException::class, ClassException::class, BundleException::class)
    private fun toClass(pc: PageContext?, cl: ClassLoader?, sct: Struct?): Class? {
        var className: String = Caster.toString(sct.get(KeyConstants._class, null), null)
        if (StringUtil.isEmpty(className)) className = Caster.toString(sct.get(KeyConstants._interface, null), null)
        if (StringUtil.isEmpty(className)) throw FunctionException(pc, "CreateDynamicProxy", 2, "interfaces", "struct passed has no class defined")
        val bundleName: String = Caster.toString(sct.get(KeyConstants._bundleName, null), null)
        var bundleVersion: String? = Caster.toString(sct.get(KeyConstants._bundleVersion, null), null)
        if (StringUtil.isEmpty(bundleVersion)) bundleVersion = null
        return if (StringUtil.isEmpty(bundleName)) {
            ClassUtil.loadClass(cl, className)
        } else ClassUtil.loadClass(className, bundleName, bundleVersion, pc.getConfig().getIdentification(), JavaSettingsImpl.getBundleDirectories(pc))
    }
}