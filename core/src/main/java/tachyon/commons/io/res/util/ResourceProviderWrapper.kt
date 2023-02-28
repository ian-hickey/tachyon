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
package tachyon.commons.io.res.util

import java.io.IOException

class ResourceProviderWrapper(provider: ResourceProvider) : ResourceProviderPro {
    private val provider: ResourceProvider

    @get:Override
    val arguments: Map
        get() = provider.getArguments()

    @Override
    fun getResource(path: String?): Resource {
        return provider.getResource(path)
    }

    @get:Override
    val scheme: String
        get() = provider.getScheme()

    @Override
    fun init(scheme: String?, arguments: Map?): ResourceProvider {
        return provider.init(scheme, arguments)
    }

    @get:Override
    val isAttributesSupported: Boolean
        get() = provider.isAttributesSupported()

    @get:Override
    val isCaseSensitive: Boolean
        get() = provider.isCaseSensitive()

    @get:Override
    val isModeSupported: Boolean
        get() = provider.isModeSupported()

    @Override
    @Throws(IOException::class)
    fun lock(res: Resource?) {
        provider.lock(res)
    }

    @Override
    @Throws(IOException::class)
    fun read(res: Resource?) {
        provider.read(res)
    }

    @Override
    fun setResources(resources: Resources?) {
        provider.setResources(resources)
    }

    @Override
    fun unlock(res: Resource?) {
        provider.unlock(res)
    }

    @get:Override
    val separator: Char
        get() = ResourceUtil.getSeparator(provider)

    init {
        this.provider = provider
    }
}