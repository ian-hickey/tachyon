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
package lucee.commons.io.res.util

import java.io.Closeable

/**
 * Classloader that load classes from resources
 */
class ResourceClassLoader : URLClassLoader, Closeable {
    private val resources: List<Resource> = ArrayList<Resource>()
    private var customCLs: Map<String, SoftReference<ResourceClassLoader>>? = null

    companion object {
        /**
         * translate resources to url Objects
         *
         * @param reses
         * @return
         * @throws PageException
         */
        @Throws(IOException::class)
        fun doURLs(reses: Array<Resource?>): Array<URL> {
            val list: List<URL> = ArrayList<URL>()
            for (i in reses.indices) {
                if (reses[i].isDirectory() || "jar".equalsIgnoreCase(ResourceUtil.getExtension(reses[i], null))) list.add(doURL(reses[i]))
            }
            return list.toArray(arrayOfNulls<URL>(list.size()))
        }

        @Throws(IOException::class)
        private fun doURL(res: Resource?): URL {
            if (res !is FileResource) throw IOException("resource [" + res.getPath().toString() + "] must be a local file")
            return (res as FileResource?).toURL()
        }

        init {
            val res: Boolean = registerAsParallelCapable()
        }
    }

    /**
     * Constructor of the class
     *
     * @param reses
     * @param parent
     * @throws PageException
     */
    constructor(resources: Array<Resource?>, parent: ClassLoader?) : super(doURLs(resources), parent) {
        for (i in resources.indices) {
            if (resources[i] != null) this.resources.add(resources[i])
        }
    }

    constructor(parent: ClassLoader?) : super(arrayOfNulls<URL>(0), parent) {}

    /**
     * @return the resources
     */
    fun getResources(): Array<Resource?> {
        return resources.toArray(arrayOfNulls<Resource>(resources.size()))
    }

    val isEmpty: Boolean
        get() = resources.isEmpty()

    @Override
    fun close() {
    }

    /*
	 * public synchronized void addResources(Resource[] reses) throws IOException { for(int
	 * i=0;i<reses.length;i++){ if(!this.resources.contains(reses[i])){ this.resources.add(reses[i]);
	 * addURL(doURL(reses[i])); } } }
	 */
    @Throws(IOException::class)
    fun getCustomResourceClassLoader(resources: Array<Resource?>): ResourceClassLoader {
        var resources: Array<Resource?> = resources
        if (ArrayUtil.isEmpty(resources)) return this
        val key = hash(resources)
        val tmp: SoftReference<ResourceClassLoader>? = if (customCLs == null) null else customCLs!![key]
        var rcl: ResourceClassLoader? = if (tmp == null) null else tmp.get()
        if (rcl != null) return rcl
        resources = ResourceUtil.merge(getResources(), resources)
        rcl = ResourceClassLoader(resources, getParent())
        if (customCLs == null) customCLs = ConcurrentHashMap<String, SoftReference<ResourceClassLoader>>()
        customCLs.put(key, SoftReference<ResourceClassLoader>(rcl))
        return rcl
    }

    @Throws(IOException::class)
    fun getCustomResourceClassLoader2(resources: Array<Resource?>): ResourceClassLoader {
        if (ArrayUtil.isEmpty(resources)) return this
        val key = hash(resources)
        val tmp: SoftReference<ResourceClassLoader>? = if (customCLs == null) null else customCLs!![key]
        var rcl: ResourceClassLoader? = if (tmp == null) null else tmp.get()
        if (rcl != null) return rcl
        rcl = ResourceClassLoader(resources, this)
        if (customCLs == null) customCLs = ConcurrentHashMap<String, SoftReference<ResourceClassLoader>>()
        customCLs.put(key, SoftReference<ResourceClassLoader>(rcl))
        return rcl
    }

    private fun hash(resources: Array<Resource?>): String {
        Arrays.sort(resources)
        val sb = StringBuilder()
        for (i in resources.indices) {
            sb.append(ResourceUtil.getCanonicalPathEL(resources[i]))
            sb.append(';')
        }
        return MD5.getDigestAsString(sb.toString(), null)
    }
}