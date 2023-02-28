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
package tachyon.commons.io.res

import java.util.Map

class ResourcesImpl : Resources {
    private var defaultResource: ResourceProvider = frp
    private var resources = arrayOfNulls<ResourceProviderFactory>(0)

    /**
     * adds a default factory, this factory is used, when sheme can't be mapped to another factory
     *
     * @param provider
     */
    @Override
    fun registerDefaultResourceProvider(provider: ResourceProvider) {
        provider.setResources(this)
        defaultResource = provider
    }

    /**
     * adds an additional resource to System
     *
     * @param provider
     */
    @Override
    fun registerResourceProvider(provider: ResourceProvider) {
        provider.setResources(this)
        val scheme: String = provider.getScheme()
        if (StringUtil.isEmpty(scheme)) return
        val tmp = arrayOfNulls<ResourceProviderFactory>(resources.size + 1)
        for (i in resources.indices) {
            if (scheme.equalsIgnoreCase(resources[i]!!.scheme)) {
                resources[i] = ResourceProviderFactory(this, provider)
                return
            }
            tmp[i] = resources[i]
        }
        tmp[resources.size] = ResourceProviderFactory(this, provider)
        resources = tmp
    }

    fun registerResourceProvider(rpf: ResourceProviderFactory) {
        var rpf = rpf
        rpf = rpf.duplicate(this)
        val scheme = rpf.scheme
        if (StringUtil.isEmpty(scheme)) return
        val tmp = arrayOfNulls<ResourceProviderFactory>(resources.size + 1)
        for (i in resources.indices) {
            if (scheme.equalsIgnoreCase(resources[i]!!.scheme)) {
                resources[i] = rpf
                return
            }
            tmp[i] = resources[i]
        }
        tmp[resources.size] = rpf
        resources = tmp
    }

    fun registerResourceProvider(scheme: String, cd: ClassDefinition, arguments: Map) {
        if (StringUtil.isEmpty(scheme)) return
        val tmp = arrayOfNulls<ResourceProviderFactory>(resources.size + 1)
        for (i in resources.indices) {
            if (scheme.equalsIgnoreCase(resources[i]!!.scheme)) {
                resources[i] = ResourceProviderFactory(this, scheme, cd, arguments)
                return
            }
            tmp[i] = resources[i]
        }
        tmp[resources.size] = ResourceProviderFactory(this, scheme, cd, arguments)
        resources = tmp
    }

    class ResourceProviderFactory {
        private var reses: Resources
        val scheme: String
        private val cd: ClassDefinition
        private val arguments: Map
        private var instance: ResourceProvider? = null

        constructor(reses: Resources, scheme: String, cd: ClassDefinition, arguments: Map) {
            this.reses = reses
            this.scheme = scheme
            this.cd = cd
            this.arguments = arguments
        }

        constructor(reses: Resources, provider: ResourceProvider) {
            this.reses = reses
            scheme = provider.getScheme()
            cd = ClassDefinitionImpl(provider.getClass())
            arguments = provider.getArguments()
        }

        fun duplicate(reses: ResourcesImpl): ResourceProviderFactory {
            return ResourceProviderFactory(reses, scheme, cd, arguments)
        }

        fun instance(): ResourceProvider? {
            if (instance == null) {
                instance = try {
                    val o: Object = ClassUtil.loadInstance(cd.getClazz())
                    if (o is ResourceProvider) {
                        val rp: ResourceProvider = o as ResourceProvider
                        rp.init(scheme, arguments)
                        rp.setResources(reses)
                        rp
                    } else throw ClassException("object [" + Caster.toClassName(o).toString() + "] must implement the interface " + ResourceProvider::class.java.getName())
                } catch (e: Exception) {
                    throw PageRuntimeException(Caster.toPageException(e))
                }
            }
            return instance
        }
    }

    /**
     * returns a resource that matching the given path
     *
     * @param path
     * @return matching resource
     */
    @Override
    fun getResource(path: String): Resource {
        val index: Int = path.indexOf("://")
        if (index != -1) {
            val scheme: String = path.substring(0, index).toLowerCase().trim()
            val subPath: String = path.substring(index + 3)
            for (i in resources.indices) {
                if (scheme.equalsIgnoreCase(resources[i]!!.scheme)) {
                    return resources[i]!!.instance().getResource(subPath)
                }
            }
        }
        return defaultResource.getResource(path)
    }

    // TODO Auto-generated method stub
    val scheme: String?
        get() =// TODO Auto-generated method stub
            null

    /**
     * @return the defaultResource
     */
    @get:Override
    val defaultResourceProvider: ResourceProvider
        get() = defaultResource

    @get:Override
    val resourceProviders: Array<Any?>
        get() {
            val tmp: Array<ResourceProvider?> = arrayOfNulls<ResourceProvider>(resources.size)
            for (i in tmp.indices) {
                tmp[i] = resources[i]!!.instance()
            }
            return tmp
        }
    val resourceProviderFactories: Array<ResourceProviderFactory?>
        get() {
            val tmp = arrayOfNulls<ResourceProviderFactory>(resources.size)
            for (i in tmp.indices) {
                tmp[i] = resources[i]
            }
            return tmp
        }

    @Override
    fun createResourceLock(timeout: Long, caseSensitive: Boolean): ResourceLock {
        return ResourceLockImpl(timeout, caseSensitive)
    }

    @Override
    fun reset() {
        resources = arrayOfNulls(0)
    }

    companion object {
        private val frp: ResourceProvider = FileResourceProvider()
        val global: Resources = ResourcesImpl()
        val fileResourceProvider: ResourceProvider
            get() = frp
    }
}