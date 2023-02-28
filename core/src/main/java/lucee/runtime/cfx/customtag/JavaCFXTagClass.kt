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
package lucee.runtime.cfx.customtag

import org.osgi.framework.BundleException

/**
 *
 */
class JavaCFXTagClass : CFXTagClass {
    /**
     * @return Returns the name.
     */
    var name: String?
        private set
    private var cd: ClassDefinition?

    @get:Override
    var isReadOnly = false
        private set

    constructor(name: String?, cd: ClassDefinition?) {
        var name = name
        name = name.toLowerCase()
        if (name.startsWith("cfx_")) name = name.substring(4)
        this.name = name
        this.cd = cd
    }

    private constructor(name: String?, cd: ClassDefinition?, readOnly: Boolean) {
        this.name = name
        this.cd = cd
        isReadOnly = readOnly
    }

    @Override
    @Throws(CFXTagException::class)
    fun newInstance(): CustomTag? {
        return try {
            _newInstance()
        } catch (e: Throwable) {
            ExceptionUtil.rethrowIfNecessary(e)
            throw CFXTagException(e)
        }
    }

    @Throws(InstantiationException::class, IllegalAccessException::class, ClassException::class, BundleException::class)
    fun _newInstance(): CustomTag? {
        val o: Object = clazz.newInstance()
        return o as CustomTag
    }

    /**
     * @return Returns the clazz.
     * @throws BundleException
     * @throws ClassException
     */
    @get:Throws(ClassException::class, BundleException::class)
    val clazz: Class<CustomTag?>?
        get() = cd.getClazz()

    /**
     * @return Returns the strClass.
     */
    val classDefinition: ClassDefinition?
        get() = cd

    @Override
    fun cloneReadOnly(): CFXTagClass? {
        return JavaCFXTagClass(name, cd, true)
    }

    @get:Override
    val displayType: String?
        get() = "Java"

    @get:Override
    val sourceName: String?
        get() = cd.getClassName()

    @get:Override
    val isValid: Boolean
        get() = try {
            Reflector.isInstaneOf(clazz, CustomTag::class.java, false)
        } catch (e: Exception) {
            false
        }
}