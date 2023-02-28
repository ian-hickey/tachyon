/**
 * Copyright (c) 2016, Tachyon Assosication Switzerland
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
package tachyon.runtime.functions

import java.util.concurrent.ConcurrentHashMap

// TODO kann man nicht auf context ebene
/**
 * Pool to Handle Tags
 */
object FunctionHandlerPool {
    private val map: ConcurrentHashMap<String?, BIF?>? = ConcurrentHashMap<String?, BIF?>()

    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?, className: String?, bundleName: String?, bundleVersion: String?): Object? {
        return use(pc, className, bundleName, bundleVersion).invoke(pc, args)
    }

    /**
     * return a tag to use from a class
     *
     * @param tagClass
     * @return Tag
     * @throws PageException
     */
    @Throws(PageException::class)
    fun use(pc: PageContext?, className: String?, bundleName: String?, bundleVersion: String?): BIF? {
        val id = toId(className, bundleName, bundleVersion)
        var bif: BIF? = map.get(id)
        if (bif != null) return bif
        try {
            val clazz: Class<*>
            // OSGi bundle
            clazz = if (!StringUtil.isEmpty(bundleName)) ClassUtil.loadClassByBundle(className, bundleName, bundleVersion, pc.getConfig().getIdentification(), JavaSettingsImpl.getBundleDirectories(pc), true) else ClassUtil.loadClass(className)
            if (Reflector.isInstaneOf(clazz, BIF::class.java, false)) bif = ClassUtil.newInstance(clazz) as BIF else bif = BIFProxy(clazz)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
        map.put(id, bif)
        return bif
    }

    private fun toId(className: String?, bundleName: String?, bundleVersion: String?): String? {
        if (bundleName == null && bundleVersion == null) return className
        return if (bundleVersion == null) className.toString() + ":" + bundleName else className.toString() + ":" + bundleName + ":" + bundleVersion
    }
}