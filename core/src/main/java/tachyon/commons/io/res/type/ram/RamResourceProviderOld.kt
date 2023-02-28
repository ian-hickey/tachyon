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
package tachyon.commons.io.res.type.ram

import java.io.IOException

/**
 * Resource Provider for ram resource
 */
class RamResourceProviderOld : ResourceProviderPro {
    @get:Override
    var scheme = "ram"
        private set
    private var root: RamResourceCore? = null

    @get:Override
    var isCaseSensitive = true

    // private Resources resources;
    private var lockTimeout: Long = 1000
    private val lock: ResourceLockImpl = ResourceLockImpl(lockTimeout, isCaseSensitive)
    private var arguments: Map? = null

    /**
     * initialize ram resource
     *
     * @param scheme
     * @param arguments
     * @return RamResource
     */
    @Override
    fun init(scheme: String, arguments: Map?): ResourceProvider {
        if (!StringUtil.isEmpty(scheme)) this.scheme = scheme
        if (arguments != null) {
            this.arguments = arguments
            val oCaseSensitive: Object = arguments.get("case-sensitive")
            if (oCaseSensitive != null) {
                isCaseSensitive = Caster.toBooleanValue(oCaseSensitive, true)
            }

            // lock-timeout
            val oTimeout: Object = arguments.get("lock-timeout")
            if (oTimeout != null) {
                lockTimeout = Caster.toLongValue(oTimeout, lockTimeout)
            }
        }
        lock.setLockTimeout(lockTimeout)
        lock.setCaseSensitive(isCaseSensitive)
        root = RamResourceCore(null, RamResourceCore.TYPE_DIRECTORY, "")
        return this
    }

    @Override
    fun getResource(path: String?): Resource {
        var path = path
        path = ResourceUtil.removeScheme(scheme, path)
        return RamResource(this, path)
    }

    /**
     * returns core for this path if exists, otherwise return null
     *
     * @param path
     * @return core or null
     */
    fun getCore(path: String?): RamResourceCore? {
        val names: Array<String> = ListUtil.listToStringArray(path, '/')
        var rrc: RamResourceCore? = root
        for (i in names.indices) {
            rrc = rrc!!.getChild(names[i], isCaseSensitive)
            if (rrc == null) return null
        }
        return rrc
    }

    /**
     * create a new core
     *
     * @param path
     * @param type
     * @return created core
     * @throws IOException
     */
    @Throws(IOException::class)
    fun createCore(path: String, type: Int): RamResourceCore {
        val names: Array<String> = ListUtil.listToStringArray(path, '/')
        var rrc: RamResourceCore? = root
        for (i in 0 until names.size - 1) {
            rrc = rrc!!.getChild(names[i], isCaseSensitive)
            if (rrc == null) throw IOException("Can't create resource [$path], missing parent resource")
        }
        rrc = RamResourceCore(rrc, type, names[names.size - 1])
        return rrc
    }

    @Override
    fun setResources(resources: Resources?) {
        // this.resources=resources;
    }

    @Override
    @Throws(IOException::class)
    fun lock(res: Resource?) {
        lock.lock(res)
    }

    @Override
    fun unlock(res: Resource?) {
        lock.unlock(res)
    }

    @Override
    @Throws(IOException::class)
    fun read(res: Resource?) {
        lock.read(res)
    }

    @get:Override
    val isAttributesSupported: Boolean
        get() = true

    @get:Override
    val isModeSupported: Boolean
        get() = true

    @Override
    fun getArguments(): Map? {
        return arguments
    }

    @get:Override
    val separator: Char
        get() = '/'
}