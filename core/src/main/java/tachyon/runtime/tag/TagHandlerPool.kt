/**
 * Copyright (c) 2023, TachyonCFML.org
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
package tachyon.runtime.tag

import java.util.Queue

// TODO kann man nicht auf context ebene
/**
 * Pool to Handle Tags
 */
class TagHandlerPool(config: ConfigWeb?) {
    private val map: ConcurrentHashMap<String?, Queue<Tag?>?>? = ConcurrentHashMap<String?, Queue<Tag?>?>()
    private val config: ConfigWeb?

    /**
     * return a tag to use from a class
     *
     * @param tagClass
     * @return Tag
     * @throws PageException
     */
    @Throws(PageException::class)
    fun use(className: String?, tagBundleName: String?, tagBundleVersion: String?, id: Identification?): Tag? {
        val queue: Queue<Tag?>? = getQueue(toId(className, tagBundleName, tagBundleVersion))
        val tag: Tag = queue.poll()
        return if (tag != null) tag else loadTag(className, tagBundleName, tagBundleVersion, id)
    }

    private fun toId(className: String?, tagBundleName: String?, tagBundleVersion: String?): String? {
        if (tagBundleName == null && tagBundleVersion == null) return className
        return if (tagBundleVersion == null) className.toString() + ":" + tagBundleName else className.toString() + ":" + tagBundleName + ":" + tagBundleVersion
    }

    /**
     * free a tag for reusing
     *
     * @param tag
     * @throws ExpressionException
     */
    fun reuse(tag: Tag?) {
        tag.release()
        val queue: Queue<Tag?>? = getQueue(tag.getClass().getName())
        queue.add(tag)
    }

    fun reuse(tag: Tag?, bundleName: String?, bundleVersion: String?) {
        tag.release()
        val queue: Queue<Tag?>? = getQueue(toId(tag.getClass().getName(), bundleName, bundleVersion))
        queue.add(tag)
    }

    @Throws(PageException::class)
    private fun loadTag(className: String?, tagBundleName: String?, tagBundleVersion: String?, id: Identification?): Tag? {
        return try {
            ClassUtil.newInstance(ClassDefinitionImpl(className, tagBundleName, tagBundleVersion, id).setVersionOnlyMattersWhenDownloading(true).getClazz()) as Tag
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    private fun getQueue(id: String?): Queue<Tag?>? {
        val queue: Queue<Tag?> = map.get(id) // doing get before, do avoid constructing ConcurrentLinkedQueue Object all the time
        if (queue != null) return queue
        var nq: Queue<Tag?>?
        val oq: Queue<Tag?>
        oq = map.putIfAbsent(id, ConcurrentLinkedQueue<Tag?>().also { nq = it })
        return if (oq != null) oq else nq
    }

    fun reset() {
        map.clear()
    }

    init {
        this.config = config
    }
}