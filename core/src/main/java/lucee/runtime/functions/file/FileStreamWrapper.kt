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
package lucee.runtime.functions.file

import java.io.IOException

abstract class FileStreamWrapper(res: Resource?) : StructSupport(), Struct {
    protected val res: Resource?
    private var status = STATE_OPEN
    fun getFilename(): String? {
        return res.getName()
    }

    fun getLabel(): String? {
        return StringUtil.ucFirst(res.getResourceProvider().getScheme()).toString() + ": " + getFilename()
    }

    fun getFilepath(): String? {
        return res.getAbsolutePath()
    }

    fun getStatus(): String? {
        return status
    }

    fun setStatus(status: String?) {
        this.status = status
    }

    fun getLastmodified(): Date? {
        return DateTimeImpl(res.lastModified(), false)
    }

    fun getMetadata(): Object? {
        return info()
    }

    fun info(): Struct? {
        val info = StructImpl()
        info.setEL(KeyConstants._mode, getMode())
        info.setEL(KeyConstants._name, res.getName())
        info.setEL(KeyConstants._path, res.getParent())
        info.setEL(KeyConstants._status, getStatus())
        info.setEL(KeyConstants._size, getSize().toString() + " bytes")
        info.setEL(KeyConstants._lastmodified, getLastmodified())
        return info
    }

    fun isEndOfFile(): Boolean {
        return false
    }

    fun getSize(): Long {
        return res.length()
    }

    @Throws(IOException::class)
    fun write(obj: Object?) {
        throw notSupported("write")
    }

    @Throws(IOException::class)
    fun readLine(): String? {
        throw notSupported("readLine")
    }

    @Throws(IOException::class)
    fun read(len: Int): Object? {
        throw notSupported("read")
    }

    abstract fun getMode(): String?
    @Throws(IOException::class)
    abstract fun close()
    private fun notSupported(method: String?): IOException? {
        return IOException(method.toString() + " can't be called when the file is opened in [" + getMode() + "] mode")
    }

    fun getResource(): Resource? {
        return res
    }

    @Override
    override fun toString(): String {
        return res.getAbsolutePath()
    }

    @Override
    fun clear() {
        throw RuntimeException("can't clear struct, struct is readonly")
    }

    @Override
    fun containsKey(key: Key?): Boolean {
        return info().containsKey(key)
    }

    @Override
    fun containsKey(pc: PageContext?, key: Key?): Boolean {
        return info().containsKey(key)
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        throw RuntimeException("can't duplicate File Object, Object depends on File Stream")
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Key?): Object? {
        return info().get(key)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Key?): Object? {
        return info().get(pc, key)
    }

    @Override
    operator fun get(key: Key?, defaultValue: Object?): Object? {
        return info().get(key, defaultValue)
    }

    @Override
    operator fun get(pc: PageContext?, key: Key?, defaultValue: Object?): Object? {
        return info().get(pc, key, defaultValue)
    }

    @Override
    fun keys(): Array<Key?>? {
        return info().keys()
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Key?): Object? {
        throw PageRuntimeException("can't remove key [" + key.getString().toString() + "] from struct, struct is readonly")
    }

    @Override
    fun removeEL(key: Key?): Object? {
        throw PageRuntimeException("can't remove key [" + key.getString().toString() + "] from struct, struct is readonly")
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Key?, value: Object?): Object? {
        throw ExpressionException("can't set key [" + key.getString().toString() + "] to struct, struct is readonly")
    }

    @Override
    fun setEL(key: Key?, value: Object?): Object? {
        throw PageRuntimeException("can't set key [" + key.getString().toString() + "] to struct, struct is readonly")
    }

    @Override
    fun size(): Int {
        return info().size()
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return info().toDumpData(pageContext, maxlevel, dp)
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        return info().keyIterator()
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return info().keysAsStringIterator()
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return info().entryIterator()
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return info().valueIterator()
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return info().castToBooleanValue()
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return info().castToBoolean(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        return info().castToDateTime()
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return info().castToDateTime(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return info().castToDoubleValue()
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return info().castToDoubleValue(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        return info().castToString()
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return info().castToString(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        return info().compareTo(str)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        return info().compareTo(b)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        return info().compareTo(d)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        return info().compareTo(dt)
    }

    @Override
    fun containsValue(value: Object?): Boolean {
        return info().containsValue(value)
    }

    @Override
    fun values(): Collection<*>? {
        return info().values()
    }

    @Throws(PageException::class)
    abstract fun skip(len: Int)
    @Throws(PageException::class)
    abstract fun seek(pos: Long)
    @Override
    fun getType(): Int {
        return Struct.TYPE_REGULAR
    }

    companion object {
        val STATE_OPEN: String? = "open"
        val STATE_CLOSE: String? = "close"
    }

    init {
        this.res = res
    }
}