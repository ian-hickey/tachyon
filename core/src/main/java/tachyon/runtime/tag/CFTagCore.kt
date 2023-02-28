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
package tachyon.runtime.tag

import tachyon.commons.lang.StringUtil

class CFTagCore : CFTag() {
    /**
     * @return the name
     */
    var name: String? = null
        private set

    /**
     * @return the filename
     */
    var filename: String? = null
        private set
    private var mappingName: String? = null
    private var isweb = false
    fun set__name(name: String?) {
        this.name = name
    }

    fun set__filename(filename: String?) {
        this.filename = filename
    }

    fun set__isweb(isweb: Boolean) {
        this.isweb = isweb
    }

    fun set__mapping(mapping: String?) {
        mappingName = mapping
    }

    @Override
    @Throws(PageException::class)
    override fun initFile(pageContext: PageContext?): InitFile? {
        return createInitFile(pageContext, isweb, filename, mappingName)
    }

    companion object {
        fun createInitFile(pageContext: PageContext?, isweb: Boolean, filename: String?, mappingName: String?): InitFile? {
            var mappingName = mappingName
            val config: ConfigWebPro = pageContext.getConfig() as ConfigWebPro
            if (StringUtil.isEmpty(mappingName)) mappingName = "mapping-tag"
            val mapping: Mapping = if (isweb) config.getTagMapping(mappingName) else config.getServerTagMapping(mappingName)
            return InitFile(pageContext, mapping.getPageSource(filename), filename)
        }
    }
}