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
package tachyon.runtime.util

import java.io.IOException

class ClassUtilImpl : ClassUtil {
    @Override
    @Throws(ClassException::class)
    fun loadClass(className: String?): Class<*>? {
        return tachyon.commons.lang.ClassUtil.loadClass(className!!)
    }

    @Override
    @Throws(ClassException::class, BundleException::class)
    fun loadClass(pc: PageContext?, className: String?, bundleName: String?, bundleVersion: String?): Class<*>? {
        val config: Config = ThreadLocalPageContext.getConfig(pc)
        return tachyon.commons.lang.ClassUtil.loadClassByBundle(className, bundleName, bundleVersion, config.getIdentification(), JavaSettingsImpl.getBundleDirectories(pc))
    }

    @Override
    @Throws(InstantiationException::class, IllegalAccessException::class)
    fun loadBIF(pc: PageContext?, name: String?): BIF? {
        // first of all we chek if itis a class
        val res: Class<*> = tachyon.commons.lang.ClassUtil.loadClass(name, null)
        if (res != null) {
            return if (Reflector.isInstaneOf(res, BIF::class.java, false)) {
                try {
                    tachyon.commons.lang.ClassUtil.newInstance(res) as BIF
                } catch (e: Exception) {
                    throw PageRuntimeException(e)
                }
            } else BIFProxy(res)
        }
        val flds: Array<FunctionLib?> = (pc.getConfig() as ConfigWebPro).getFLDs(pc.getCurrentTemplateDialect())
        var flf: FunctionLibFunction
        for (i in flds.indices) {
            flf = flds[i].getFunction(name)
            if (flf != null) return flf.getBIF()
        }
        return null
    }

    // FUTURE add to loader
    @Throws(InstantiationException::class, IllegalAccessException::class, ClassException::class, BundleException::class, IllegalArgumentException::class, InvocationTargetException::class, NoSuchMethodException::class, SecurityException::class)
    fun loadBIF(pc: PageContext?, name: String?, bundleName: String?, bundleVersion: Version?): BIF? {
        // first of all we chek if itis a class
        val res: Class<*> = tachyon.commons.lang.ClassUtil.loadClassByBundle(name, bundleName, bundleVersion, pc.getConfig().getIdentification(),
                JavaSettingsImpl.getBundleDirectories(pc))
        return if (res != null) {
            if (Reflector.isInstaneOf(res, BIF::class.java, false)) {
                tachyon.commons.lang.ClassUtil.newInstance(res) as BIF
            } else BIFProxy(res)
        } else null
    }

    @Override
    fun isInstaneOf(srcClassName: String?, trg: Class<*>?): Boolean {
        return Reflector.isInstaneOf(srcClassName, trg)
    }

    @Override
    fun isInstaneOf(srcClassName: String?, trgClassName: String?): Boolean {
        return Reflector.isInstaneOf(srcClassName, trgClassName)
    }

    @Override
    fun isInstaneOf(src: Class<*>?, trgClassName: String?): Boolean {
        return Reflector.isInstaneOf(src, trgClassName)
    }

    @Override
    fun isInstaneOfIgnoreCase(src: Class<*>?, trg: String?): Boolean {
        return Reflector.isInstaneOfIgnoreCase(src, trg)
    }

    @Override
    fun isInstaneOf(src: Class<*>?, trg: Class<*>?): Boolean {
        return Reflector.isInstaneOf(src, trg, true)
    }

    fun isInstaneOf(src: Class<*>?, trg: Class<*>?, exatctMatch: Boolean): Boolean { // FUTURE
        return Reflector.isInstaneOf(src, trg, exatctMatch)
    }

    @Override
    fun getClasses(objs: Array<Object?>?): Array<Class<*>?>? {
        return Reflector.getClasses(objs)
    }

    @Override
    fun toReferenceClass(c: Class<*>?): Class<*>? {
        return Reflector.toReferenceClass(c)
    }

    @Override
    fun like(src: Class<*>?, trg: Class<*>?): Boolean {
        return Reflector.like(src, trg)
    }

    @Override
    @Throws(PageException::class)
    fun convert(src: Object?, trgClass: Class<*>?, rating: RefInteger?): Object? {
        return Reflector.convert(src, trgClass, rating)
    }

    @Override
    @Throws(NoSuchFieldException::class)
    fun getFieldsIgnoreCase(clazz: Class<*>?, name: String?): Array<Field?>? {
        return Reflector.getFieldsIgnoreCase(clazz, name)
    }

    @Override
    fun getFieldsIgnoreCase(clazz: Class<*>?, name: String?, defaultValue: Array<Field?>?): Array<Field?>? {
        return Reflector.getFieldsIgnoreCase(clazz, name, defaultValue)
    }

    @Override
    fun getPropertyKeys(clazz: Class<*>?): Array<String?>? {
        return Reflector.getPropertyKeys(clazz)
    }

    @Override
    fun hasPropertyIgnoreCase(clazz: Class<*>?, name: String?): Boolean {
        return Reflector.hasPropertyIgnoreCase(clazz, name)
    }

    @Override
    fun hasFieldIgnoreCase(clazz: Class<*>?, name: String?): Boolean {
        return Reflector.hasFieldIgnoreCase(clazz, name)
    }

    @Override
    @Throws(PageException::class)
    fun callConstructor(clazz: Class<*>?, args: Array<Object?>?): Object? {
        return Reflector.callConstructor(clazz, args)
    }

    @Override
    fun callConstructor(clazz: Class<*>?, args: Array<Object?>?, defaultValue: Object?): Object? {
        return Reflector.callConstructor(clazz, args, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun callMethod(obj: Object?, methodName: Key?, args: Array<Object?>?): Object? {
        return Reflector.callMethod(obj, methodName, args)
    }

    @Override
    fun callMethod(obj: Object?, methodName: Key?, args: Array<Object?>?, defaultValue: Object?): Object? {
        return Reflector.callMethod(obj, methodName, args, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun callStaticMethod(clazz: Class<*>?, methodName: String?, args: Array<Object?>?): Object? {
        return Reflector.callStaticMethod(clazz, KeyImpl.getInstance(methodName), args)
    }

    @Override
    @Throws(PageException::class)
    fun getField(obj: Object?, prop: String?): Object? {
        return Reflector.getField(obj, prop)
    }

    @Override
    fun getField(obj: Object?, prop: String?, defaultValue: Object?): Object? {
        return Reflector.getField(obj, prop, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun setField(obj: Object?, prop: String?, value: Object?): Boolean {
        return Reflector.setField(obj, prop, value)
    }

    @Override
    @Throws(PageException::class)
    fun getProperty(obj: Object?, prop: String?): Object? {
        return Reflector.getProperty(obj, prop)
    }

    @Override
    fun getProperty(obj: Object?, prop: String?, defaultValue: Object?): Object? {
        return Reflector.getProperty(obj, prop, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun setProperty(obj: Object?, prop: String?, value: Object?) {
        Reflector.setProperty(obj, prop, value)
    }

    @Override
    fun setPropertyEL(obj: Object?, prop: String?, value: Object?) {
        Reflector.setPropertyEL(obj, prop, value)
    }

    @Override
    fun getDeclaredMethods(clazz: Class<*>?): Array<Method?>? {
        return Reflector.getDeclaredMethods(clazz)
    }

    @Override
    fun canConvert(from: Class<*>?, to: Class<*>?): Boolean {
        return Reflector.canConvert(from, to)
    }

    @Override
    @Throws(IOException::class, BundleException::class)
    fun loadClassByBundle(className: String?, name: String?, strVersion: String?, id: Identification?): Class<*>? {
        return tachyon.commons.lang.ClassUtil.loadClassByBundle(className, name, strVersion, id, JavaSettingsImpl.getBundleDirectories(null))
    }

    @Override
    @Throws(BundleException::class, IOException::class)
    fun loadClassByBundle(className: String?, name: String?, version: Version?, id: Identification?): Class<*>? {
        return tachyon.commons.lang.ClassUtil.loadClassByBundle(className, name, version, id, JavaSettingsImpl.getBundleDirectories(null))
    }

    @Override
    fun loadClass(className: String?, defaultValue: Class<*>?): Class<*>? {
        return tachyon.commons.lang.ClassUtil.loadClass(className, defaultValue)
    }

    @Override
    fun loadClass(cl: ClassLoader?, className: String?, defaultValue: Class<*>?): Class<*>? {
        return tachyon.commons.lang.ClassUtil.loadClass(cl, className!!, defaultValue)
    }

    @Override
    @Throws(IOException::class)
    fun loadClass(cl: ClassLoader?, className: String?): Class<*>? {
        return tachyon.commons.lang.ClassUtil.loadClass(cl, className)
    }

    @Override
    @Throws(ClassException::class)
    fun loadInstance(clazz: Class<*>?): Object? {
        return tachyon.commons.lang.ClassUtil.loadInstance(clazz)
    }

    @Override
    @Throws(ClassException::class)
    fun loadInstance(className: String?): Object? {
        return tachyon.commons.lang.ClassUtil.loadInstance(className)
    }

    @Override
    @Throws(ClassException::class)
    fun loadInstance(cl: ClassLoader?, className: String?): Object? {
        return tachyon.commons.lang.ClassUtil.loadInstance(cl, className)
    }

    @Override
    fun loadInstance(clazz: Class<*>?, defaultValue: Object?): Object? {
        return tachyon.commons.lang.ClassUtil.loadInstance(clazz, defaultValue)
    }

    @Override
    fun loadInstance(className: String?, defaultValue: Object?): Object? {
        return tachyon.commons.lang.ClassUtil.loadInstance(className, defaultValue)
    }

    @Override
    fun loadInstance(cl: ClassLoader?, className: String?, defaultValue: Object?): Object? {
        return tachyon.commons.lang.ClassUtil.loadInstance(cl, className, defaultValue)
    }

    @Override
    @Throws(ClassException::class, InvocationTargetException::class)
    fun loadInstance(clazz: Class<*>?, args: Array<Object?>?): Object? {
        return tachyon.commons.lang.ClassUtil.loadInstance(clazz, args)
    }

    @Override
    @Throws(ClassException::class, InvocationTargetException::class)
    fun loadInstance(className: String?, args: Array<Object?>?): Object? {
        return tachyon.commons.lang.ClassUtil.loadInstance(className, args)
    }

    @Override
    @Throws(ClassException::class, InvocationTargetException::class)
    fun loadInstance(cl: ClassLoader?, className: String?, args: Array<Object?>?): Object? {
        return tachyon.commons.lang.ClassUtil.loadInstance(cl, className, args)
    }

    @Override
    fun loadInstance(clazz: Class<*>?, args: Array<Object?>?, defaultValue: Object?): Object? {
        return tachyon.commons.lang.ClassUtil.loadInstance(clazz, args, defaultValue)
    }

    @Override
    fun loadInstance(className: String?, args: Array<Object?>?, defaultValue: Object?): Object? {
        return tachyon.commons.lang.ClassUtil.loadInstance(className, args, defaultValue)
    }

    @Override
    fun loadInstance(cl: ClassLoader?, className: String?, args: Array<Object?>?, defaultValue: Object?): Object? {
        return tachyon.commons.lang.ClassUtil.loadInstance(cl, className!!, args, defaultValue)
    }

    @Override
    @Throws(IOException::class)
    fun isBytecode(`is`: InputStream?): Boolean {
        return tachyon.commons.lang.ClassUtil.isBytecode(`is`)
    }

    @Override
    fun isBytecode(barr: ByteArray?): Boolean {
        return tachyon.commons.lang.ClassUtil.isRawBytecode(barr!!)
    }

    @Override
    fun getName(clazz: Class<*>?): String? {
        return tachyon.commons.lang.ClassUtil.getName(clazz)
    }

    @Override
    fun getMethodIgnoreCase(clazz: Class<*>?, methodName: String?, args: Array<Class<*>?>?, defaultValue: Method?): Method? {
        return tachyon.commons.lang.ClassUtil.getMethodIgnoreCase(clazz, methodName, args!!, defaultValue)
    }

    @Override
    @Throws(ClassException::class)
    fun getMethodIgnoreCase(clazz: Class<*>?, methodName: String?, args: Array<Class<*>?>?): Method? {
        return tachyon.commons.lang.ClassUtil.getMethodIgnoreCase(clazz, methodName!!, args!!)
    }

    @Override
    fun getFieldNames(clazz: Class<*>?): Array<String?>? {
        return tachyon.commons.lang.ClassUtil.getFieldNames(clazz)
    }

    @Override
    @Throws(IOException::class)
    fun toBytes(clazz: Class<*>?): ByteArray? {
        return tachyon.commons.lang.ClassUtil.toBytes(clazz)
    }

    @Override
    fun toArrayClass(clazz: Class<*>?): Class<*>? {
        return tachyon.commons.lang.ClassUtil.toArrayClass(clazz)
    }

    @Override
    fun toComponentType(clazz: Class<*>?): Class<*>? {
        return tachyon.commons.lang.ClassUtil.toComponentType(clazz)
    }

    @Override
    fun getSourcePathForClass(clazz: Class<*>?, defaultValue: String?): String? {
        return tachyon.commons.lang.ClassUtil.getSourcePathForClass(clazz, defaultValue)
    }

    @Override
    fun getSourcePathForClass(className: String?, defaultValue: String?): String? {
        return tachyon.commons.lang.ClassUtil.getSourcePathForClass(className, defaultValue)
    }

    @Override
    fun extractPackage(className: String?): String? {
        return tachyon.commons.lang.ClassUtil.extractPackage(className)
    }

    @Override
    fun extractName(className: String?): String? {
        return tachyon.commons.lang.ClassUtil.extractName(className)
    }

    @Override
    @Throws(BundleException::class)
    fun start(bundle: Bundle?) {
        OSGiUtil.start(bundle)
    }

    @Override
    @Throws(BundleException::class, IOException::class)
    fun addBundle(context: BundleContext?, `is`: InputStream?, closeStream: Boolean, checkExistence: Boolean): Bundle? {
        return OSGiUtil.installBundle(context, `is`, closeStream, checkExistence)
    }
}