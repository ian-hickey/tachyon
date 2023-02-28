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
/**
 * Implements the CFML Function gettemplatepath
 */
package tachyon.runtime.functions.system

import tachyon.commons.lang.ExceptionUtil

object SystemCacheClear : Function {
    private const val serialVersionUID = 2151674703665027213L
    @Throws(PageException::class)
    fun call(pc: PageContext?): String? {
        return call(pc, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, cacheName: String?): String? {
        var cacheName = cacheName
        if (StringUtil.isEmpty(cacheName, true) || "all".equals(cacheName.trim().toLowerCase().also { cacheName = it })) {
            PagePoolClear.call(pc)
            ComponentCacheClear.call(pc)
            CTCacheClear.call(pc)
            queryCache(pc)
            tagCache(pc)
            functionCache(pc)
        } else if ("template".equals(cacheName) || "page".equals(cacheName)) {
            PagePoolClear.call(pc)
        } else if ("component".equals(cacheName) || "cfc".equals(cacheName) || "class".equals(cacheName)) {
            ComponentCacheClear.call(pc)
        } else if ("customtag".equals(cacheName) || "ct".equals(cacheName)) {
            CTCacheClear.call(pc)
        } else if ("query".equals(cacheName) || "object".equals(cacheName)) {
            queryCache(pc)
        } else if ("tag".equals(cacheName)) {
            tagCache(pc)
        } else if ("function".equals(cacheName)) {
            functionCache(pc)
        } else throw FunctionException(pc, "cacheClear", 1, "cacheName",
                ExceptionUtil.similarKeyMessage(arrayOf<Collection.Key?>(KeyConstants._all, KeyConstants._template, KeyConstants._component, KeyImpl.getInstance("customtag"),
                        KeyConstants._query, KeyConstants._tag, KeyConstants._function), cacheName!!, "cache name", "cache names", null, true).toString() + " " + ExceptionUtil.similarKeyMessage(arrayOf<Collection.Key?>(KeyConstants._all, KeyConstants._template, KeyConstants._component, KeyImpl.getInstance("customtag"),
                        KeyConstants._query, KeyConstants._tag, KeyConstants._function), cacheName!!, "cache names", null, true))
        return null
    }

    @Throws(PageException::class)
    private fun queryCache(pc: PageContext?) {
        pc.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_QUERY, null).clear(pc)
        // pc.getQueryCache().clear(pc);
    }

    private fun tagCache(pc: PageContext?) {
        val config: ConfigWebPro = pc.getConfig() as ConfigWebPro
        PagePoolClear.clear(config, config.getServerTagMappings(), false)
        PagePoolClear.clear(config, config.getTagMappings(), false)
    }

    private fun functionCache(pc: PageContext?) {
        val config: ConfigWebPro = pc.getConfig() as ConfigWebPro
        config.clearFunctionCache()
        PagePoolClear.clear(config, config.getServerFunctionMappings(), false)
        PagePoolClear.clear(config, config.getFunctionMappings(), false)
    }
}