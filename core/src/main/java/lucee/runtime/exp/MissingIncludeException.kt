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
package lucee.runtime.exp

import lucee.commons.lang.StringUtil

/**
 * Exception thrown when missing include
 */
class MissingIncludeException : PageExceptionImpl {
    private var pageSource: PageSource?

    /**
     * constructor of the exception
     *
     * @param pageSource
     */
    constructor(pageSource: PageSource?) : super(createMessage(pageSource), "missinginclude") {
        setDetail(pageSource)
        this.pageSource = pageSource
    }

    constructor(pageSource: PageSource?, msg: String?) : super(msg, "missinginclude") {
        setDetail(pageSource)
        this.pageSource = pageSource
    }

    private override fun setDetail(ps: PageSource?) {
        setAdditional(KeyConstants._Mapping, ps.getMapping().getVirtual())
    }

    /**
     * @return the pageSource
     */
    fun getPageSource(): PageSource? {
        return pageSource
    }

    @Override
    override fun getCatchBlock(config: Config?): CatchBlock? {
        val sct: CatchBlock = super.getCatchBlock(config)
        var mapping: String? = ""
        if (StringUtil.startsWith(pageSource.getRealpath(), '/')) {
            mapping = pageSource.getMapping().getVirtual()
            if (StringUtil.endsWith(mapping, '/')) mapping = mapping.substring(0, mapping.length() - 1)
        }
        sct.setEL(MISSING_FILE_NAME, mapping + pageSource.getRealpath())
        sct.setEL(MISSING_FILE_NAME_REL, mapping + pageSource.getRealpath())
        sct.setEL(MISSING_FILE_NAME_ABS, pageSource.getDisplayPath())
        return sct
    }

    @Override
    override fun typeEqual(type: String?): Boolean {
        var type = type
        if (super.typeEqual(type)) return true
        type = type.toLowerCase().trim()
        return type.equals("template")
    }

    companion object {
        private val MISSING_FILE_NAME: Collection.Key? = KeyImpl.getInstance("MissingFileName")
        private val MISSING_FILE_NAME_REL: Collection.Key? = KeyImpl.getInstance("MissingFileName_rel")
        private val MISSING_FILE_NAME_ABS: Collection.Key? = KeyImpl.getInstance("MissingFileName_abs")
        private fun createMessage(pageSource: PageSource?): String? {
            val dsp: String = pageSource.getDisplayPath()
                    ?: return "Page [" + pageSource.getRealpathWithVirtual().toString() + "] not found"
            return "Page [" + pageSource.getRealpathWithVirtual().toString() + "] [" + dsp + "] not found"
        }
    }
}