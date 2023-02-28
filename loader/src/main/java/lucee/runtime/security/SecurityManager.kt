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
package lucee.runtime.security

import lucee.commons.io.res.Resource

/**
 * interface for Security Manager
 */
interface SecurityManager {
    /**
     * @param access access
     * @return return access value (all,local,none ...) for given type (cfx,file ...)
     */
    fun getAccess(access: Int): Short

    /**
     * @param access access
     * @return return access value (all,local,none ...) for given type (cfx,file ...)
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun getAccess(access: String?): Short

    /**
     * @param res Resource
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun checkFileLocation(res: Resource?)

    /**
     * @param config config
     * @param res resource
     * @param serverPassword server password
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun checkFileLocation(config: ConfigWeb?, res: Resource?, serverPassword: String?)

    /**
     * @return clone the security Manager
     */
    fun cloneSecurityManager(): SecurityManager?

    companion object {
        const val ACCESS_OPEN: Short = 1
        const val ACCESS_PROTECTED: Short = 2
        const val ACCESS_CLOSE: Short = 3

        /**
         * Field `TYPE_SETTING`
         */
        const val TYPE_SETTING = 0

        /**
         * Field `TYPE_FILE`
         */
        const val TYPE_FILE = 1

        /**
         * Field `TYPE_DIRECT_JAVA_ACCESS`
         */
        const val TYPE_DIRECT_JAVA_ACCESS = 2

        /**
         * Field `TYPE_MAIL`
         */
        const val TYPE_MAIL = 3

        /**
         * Field `TYPE_DATASOURCE`
         */
        const val TYPE_DATASOURCE = 4

        /**
         * Field `TYPE_MAPPING`
         */
        const val TYPE_MAPPING = 5

        /**
         * Field `TYPE_CUSTOM_TAG`
         */
        const val TYPE_CUSTOM_TAG = 6

        /**
         * Field `TYPE_CFX_SETTING`
         */
        const val TYPE_CFX_SETTING = 7

        /**
         * Field `TYPE_CFX_USAGE`
         */
        const val TYPE_CFX_USAGE = 8

        /**
         * Field `TYPE_DEBUGGING`
         */
        const val TYPE_DEBUGGING = 9

        /**
         * Field `TYPE_TAG_EXECUTE`
         */
        const val TYPE_TAG_EXECUTE = 10

        /**
         * Field `TYPE_TAG_IMPORT`
         */
        const val TYPE_TAG_IMPORT = 11

        /**
         * Field `TYPE_TAG_OBJECT`
         */
        const val TYPE_TAG_OBJECT = 12

        /**
         * Field `TYPE_TAG_REGISTRY`
         */
        const val TYPE_TAG_REGISTRY = 13

        /**
         * Field `TYPE_SEARCH`
         */
        const val TYPE_SEARCH = 14

        /**
         * Field `TYPE_SCHEDULED_TASK`
         */
        const val TYPE_SCHEDULED_TASK = 15
        const val TYPE_ACCESS_READ = 16
        const val TYPE_ACCESS_WRITE = 17
        const val TYPE_REMOTE = 18
        const val TYPE_CACHE = 19
        const val TYPE_GATEWAY = 20
        const val TYPE_ORM = 21

        /**
         * Field `VALUE_NO`
         */
        const val VALUE_NO: Short = 0

        /**
         * Field `VALUE_NONE`
         */
        const val VALUE_NONE: Short = 0

        /**
         * Field `VALUE_LOCAL`
         */
        const val VALUE_LOCAL: Short = 1

        /**
         * Field `VALUE_YES`
         */
        const val VALUE_YES: Short = 2

        /**
         * Field `VALUE_ALL`
         */
        const val VALUE_ALL: Short = 2
        const val VALUE_1: Short = 11
        const val VALUE_2: Short = 12
        const val VALUE_3: Short = 13
        const val VALUE_4: Short = 14
        const val VALUE_5: Short = 15
        const val VALUE_6: Short = 16
        const val VALUE_7: Short = 17
        const val VALUE_8: Short = 18
        const val VALUE_9: Short = 19
        const val VALUE_10: Short = 20
        const val NUMBER_OFFSET: Short = 10
    }
}