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
package lucee.runtime.tag

import com.allaire.cfx.CustomTag

/**
 * Creates a CFML CFX Tag
 *
 *
 *
 */
class CFXTag : TagImpl(), DynamicAttributes, AppendixTag {
    private val attributes: Struct? = StructImpl()

    // print.out(appendix);
    @get:Override
    @set:Override
    var appendix: String? = null
    @Override
    fun release() {
        attributes.clear()
        appendix = null
    }

    @Override
    fun setDynamicAttribute(domain: String?, key: String?, value: Object?) {
        setDynamicAttribute(domain, KeyImpl.init(key), value)
    }

    @Override
    fun setDynamicAttribute(domain: String?, key: Collection.Key?, value: Object?) {
        attributes.setEL(key, value)
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        // RR SerialNumber sn = pageContext.getConfig().getSerialNumber();
        // if(sn.getVersion()==SerialNumber.VERSION_COMMUNITY)
        // throw new SecurityException("no access to this functionality with the "+sn.getStringVersion()+"
        // version of Lucee");
        val pool: CFXTagPool = pageContext.getConfig().getCFXTagPool()
        val ct: CustomTag
        ct = try {
            pool.getCustomTag(appendix)
        } catch (e: CFXTagException) {
            throw Caster.toPageException(e)
        }
        val req: Request = RequestImpl(pageContext, attributes)
        val rsp: Response = ResponseImpl(pageContext, req.debug())
        try {
            ct.processRequest(req, rsp)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
        pool.releaseCustomTag(ct)
        return SKIP_BODY
    }
}