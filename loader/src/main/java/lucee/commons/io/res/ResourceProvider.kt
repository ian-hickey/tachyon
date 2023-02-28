/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.commons.io.res

import java.io.IOException

/**
 * Interface for resource provider, loaded by "Resources", classes that implement a provider that
 * produce resources, that match given path.
 *
 */
interface ResourceProvider : Serializable {
    /**
     * this class is called by the "Resources" at startup
     *
     * @param scheme of the provider (can be "null")
     * @param arguments initals argument (can be "null")
     * @return the ResourceProvider itself
     */
    fun init(scheme: String?, arguments: Map<String?, String?>?): ResourceProvider?

    /**
     * return a resource that match given path
     *
     * @param path path to the resource
     * @return matching resource to path
     */
    fun getResource(path: String?): Resource?

    /**
     * returns the scheme of the resource
     *
     * @return scheme
     */
    val scheme: String?

    /**
     * returns the arguments defined for this resource
     *
     * @return scheme
     */
    val arguments: Map<String?, String?>?
    fun setResources(resources: Resources?)
    fun unlock(res: Resource?)

    @Throws(IOException::class)
    fun lock(res: Resource?)

    @Throws(IOException::class)
    fun read(res: Resource?)

    /**
     * returns if the resources of the provider are case-sensitive or not
     *
     * @return is resource case-sensitive or not
     */
    val isCaseSensitive: Boolean

    /**
     * returns if the resource support mode for his resources
     *
     * @return is mode supported or not
     */
    val isModeSupported: Boolean

    /**
     * returns if the resource support attributes for his resources
     *
     * @return is attributes supported or not
     */
    val isAttributesSupported: Boolean
}