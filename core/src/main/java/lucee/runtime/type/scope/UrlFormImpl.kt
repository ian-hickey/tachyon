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
package lucee.runtime.type.scope

import java.io.UnsupportedEncodingException

class UrlFormImpl(form: FormImpl?, url: URLImpl?) : StructSupport(), URLForm {
    private val form: FormImpl?
    private val url: URLImpl?
    private var isInit = false
    @Override
    fun initialize(pc: PageContext?) {
        if (isInit) return
        isInit = true
        form!!.initialize(pc)
        url!!.initialize(pc)
        form!!.addRaw(pc.getApplicationContext(), url!!.getRaw())
    }

    @Override
    fun reinitialize(ac: ApplicationContext?) {
        form!!.reinitialize(ac)
        url!!.reinitialize(ac)
    }

    @Override
    fun release(pc: PageContext?) {
        isInit = false
        form!!.release(pc)
        url!!.release(pc)
    }

    @Override
    fun getEncoding(): String? {
        return form!!.getEncoding()
    }

    @Override
    @Throws(UnsupportedEncodingException::class)
    fun setEncoding(ac: ApplicationContext?, encoding: String?) {
        form!!.setEncoding(ac, encoding)
    }

    @Override
    fun getType(): Int {
        return form!!.getType()
    }

    @Override
    fun getTypeAsString(): String? {
        return form!!.getTypeAsString()
    }

    @Override
    fun isInitalized(): Boolean {
        return isInit
    }

    @Override
    fun clear() {
        form.clear()
        url.clear()
    }

    @Override
    fun containsKey(key: Collection.Key?): Boolean {
        return form.containsKey(key)
    }

    @Override
    fun containsKey(pc: PageContext?, key: Collection.Key?): Boolean {
        return form.containsKey(pc, key)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Collection.Key?): Object? {
        return form.get(key)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Collection.Key?): Object? {
        return form.get(pc, key)
    }

    @Override
    operator fun get(key: Collection.Key?, defaultValue: Object?): Object? {
        return form.get(key, defaultValue)
    }

    @Override
    operator fun get(pc: PageContext?, key: Collection.Key?, defaultValue: Object?): Object? {
        return form.get(pc, key, defaultValue)
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        return form.keyIterator()
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return form.keysAsStringIterator()
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return form.entryIterator()
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return form.valueIterator()
    }

    @Override
    fun keys(): Array<Collection.Key?>? {
        return form.keys()
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Collection.Key?): Object? {
        return form.remove(key)
    }

    @Override
    fun removeEL(key: Collection.Key?): Object? {
        return form.removeEL(key)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Collection.Key?, value: Object?): Object? {
        return form.set(key, value)
    }

    @Override
    fun setEL(key: Collection.Key?, value: Object?): Object? {
        return form.setEL(key, value)
    }

    @Override
    fun size(): Int {
        return form.size()
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return form!!.toDumpData(pageContext, maxlevel, dp)
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return form.castToBooleanValue()
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return form.castToBoolean(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        return form.castToDateTime()
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return form.castToDateTime(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return form.castToDoubleValue()
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return form.castToDoubleValue(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        return form.castToString()
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return form.castToString(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        return form.compareTo(b)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        return form.compareTo(dt)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        return form.compareTo(d)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        return form.compareTo(str)
    }

    fun getFileUpload(key: String?): DiskFileItem? {
        return form!!.getFileUpload(key)
    }

    @Override
    fun getInitException(): PageException? {
        return form!!.getInitException()
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        return Duplicator.duplicate(form, deepCopy)
    }

    @Override
    fun setScriptProtecting(ac: ApplicationContext?, b: Boolean) {
        form!!.setScriptProtecting(ac, b)
    }

    @Override
    fun containsValue(value: Object?): Boolean {
        return form.containsValue(value)
    }

    @Override
    fun values(): Collection<Object?>? {
        return form.values()
    }

    @Override
    fun getUploadResource(key: String?): FormItem? {
        return form!!.getUploadResource(key)
    }

    @Override
    fun getFileItems(): Array<FormItem?>? {
        return form!!.getFileItems()
    }

    fun getForm(): FormImpl? {
        return form
    }

    fun getURL(): URLImpl? {
        return url
    }

    @Override
    fun getInputStream(): ServletInputStream? {
        return form!!.getInputStream()
    }

    companion object {
        private const val serialVersionUID = -5709431392572723178L
    }

    init {
        this.form = form
        this.url = url
    }
}