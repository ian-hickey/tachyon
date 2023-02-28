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
package lucee.runtime.config

import javax.servlet.ServletConfig

/**
 * Web Context
 */
interface ConfigWeb : Config, ServletConfig {
    /**
     * @return lockmanager
     */
    fun getLockManager(): LockManager?

    /**
     * @return return if is allowed to define request timeout via URL
     */
    fun isAllowURLRequestTimeout(): Boolean
    fun getLabel(): String?
    fun getConfigServerDir(): Resource?
    fun getFactory(): CFMLFactory?

    /**
     *
     * @param type Config.CACHE_TYPE_***
     * @param defaultValue default value
     * @return Returns a Cache Handler Collection.
     */
    fun getCacheHandlerCollection(type: Int, defaultValue: CacheHandlerCollection?): CacheHandlerCollection?

    @Override
    override fun getIdentification(): IdentificationWeb?

    @Throws(PageException::class)
    fun getConfigServer(password: Password?): ConfigServer?

    @Throws(PageException::class)
    fun getSearchEngine(pc: PageContext?): SearchEngine?
    fun getSuppressWSBeforeArg(): Boolean
    fun getWriter(pc: PageContext?, req: HttpServletRequest?, rsp: HttpServletResponse?): JspWriter?
    fun getAMFEngine(): AMFEngine?
}