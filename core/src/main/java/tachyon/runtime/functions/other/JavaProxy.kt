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
/**
 * Implements the CFML Function createobject
 * FUTURE neue attr unterstuestzen
 */
package tachyon.runtime.functions.other

import java.util.ArrayList

object JavaProxy : Function {
    private const val serialVersionUID = 2696152022196556309L
    @Throws(PageException::class)
    fun call(pc: PageContext?, className: String?): Object? {
        return call(pc, className, null, null, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, className: String?, pathOrName: Object?): Object? {
        return call(pc, className, pathOrName, null, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, className: String?, pathOrName: Object?, delimiterOrVersion: String?): Object? {
        return call(pc, className, pathOrName, delimiterOrVersion, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, className: String?, pathOrName: Object?, delimiterOrVersion: String?, relatedBundles: Array?): Object? {
        checkAccess(pc)
        return JavaObject(pc.getVariableUtil(), loadClass(pc, className, pathOrName, delimiterOrVersion, relatedBundles))
    }

    @Throws(PageException::class)
    fun loadClass(pc: PageContext?, className: String?, pathOrName: Object?, delimiterOrVersion: String?, aRelatedBundles: Array?): Class<*>? {
        var delimiterOrVersion = delimiterOrVersion
        if (StringUtil.isEmpty(pathOrName)) return loadClassByPath(pc, className, null)
        val str: String = Caster.toString(pathOrName, null)
        var relatedBundles: Array<BundleDefinition?>? = null
        if (aRelatedBundles != null) {
            try {
                relatedBundles = arrayOfNulls<BundleDefinition?>(aRelatedBundles.size())
                var index = 0
                var obj: Object
                var sct: Struct
                val it: Iterator<Object?> = aRelatedBundles.valueIterator()
                while (it.hasNext()) {
                    obj = it.next()
                    if (Decision.isSimpleValue(obj)) relatedBundles!![index++] = BundleDefinition(Caster.toString(obj)) else {
                        sct = Caster.toStruct(obj)
                        relatedBundles!![index++] = BundleDefinition(Caster.toString(sct.get(KeyConstants._name)), Caster.toString(sct.get(KeyConstants._version)))
                    }
                }
            } catch (be: BundleException) {
                throw Caster.toPageException(be)
            }
        }

        // String input
        if (str != null) {

            // Bundle Name?
            if (!str.contains("/") && !str.contains("\\") && !str.endsWith(".jar")) {
                return try {
                    ClassUtil.loadClassByBundle(className, BundleDefinition(str, delimiterOrVersion), relatedBundles, pc.getConfig().getIdentification(),
                            JavaSettingsImpl.getBundleDirectories(pc))

                    // public static Class<?> loadClassByBundle(String className, String name, String strVersion,
                    // Identification id, List<Resource> addionalDirectories)
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    throw Caster.toPageException(t)
                }
            }

            // path
            if (StringUtil.isEmpty(delimiterOrVersion)) delimiterOrVersion = ","
            val arrPaths: Array<String?> = ListUtil.trimItems(ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(str, delimiterOrVersion)))
            return loadClassByPath(pc, className, arrPaths)
        }
        return loadClassByPath(pc, className, ListUtil.toStringArray(Caster.toArray(pathOrName)))
    }

    @Throws(PageException::class)
    private fun loadClassByPath(pc: PageContext?, className: String?, paths: Array<String?>?): Class<*>? {
        val pci: PageContextImpl? = pc as PageContextImpl?
        val resources: MutableList<Resource?> = ArrayList<Resource?>()
        if (paths != null && paths.size > 0) {
            // load resources
            for (i in paths.indices) {
                val res: Resource = ResourceUtil.toResourceExisting(pc, paths[i])
                if (res.isDirectory()) {
                    // a directory was passed, add all of the jar files from it
                    val dir: FileResource = res as FileResource
                    val jars: Array<Resource?> = dir.listResources(WildCardFilter("*.jar") as ResourceNameFilter?)
                    for (jar in jars) {
                        resources.add(jar)
                    }
                } else {
                    resources.add(res)
                }
            }
            // throw new FunctionException(pc, "JavaProxy", 2, "path", "argument path has to be an array of
            // strings or a single string, where every string is defining a path");
        }

        // load class
        return try {
            val cl: ClassLoader = if (resources.isEmpty()) pci.getClassLoader() else pci.getClassLoader(resources.toArray(arrayOfNulls<Resource?>(resources.size())))
            var clazz: Class? = null
            clazz = try {
                ClassUtil.loadClass(cl, className)
            } catch (ce: ClassException) {
                // try java.lang if no package definition
                if (className.indexOf('.') === -1) {
                    try {
                        ClassUtil.loadClass(cl, "java.lang.$className")
                    } catch (e: ClassException) {
                        throw ce
                    }
                } else throw ce
            }
            clazz
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(SecurityException::class)
    private fun checkAccess(pc: PageContext?) {
        if (pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_DIRECT_JAVA_ACCESS) === SecurityManager.VALUE_NO) throw SecurityException("Can't create Java object, direct Java access is denied by the Security Manager")
        if (pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_TAG_OBJECT) === SecurityManager.VALUE_NO) throw SecurityException("Can't access function, access is denied by the Security Manager")
    }
}