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
 * Implements the CFML Function isuserinrole
 */
package lucee.runtime.functions.decision

import lucee.commons.lang.StringUtil

object IsUserInAnyRole : Function {
    @Throws(PageException::class)
    fun call(pc: PageContext?): Boolean {
        return call(pc, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, strRoles: String?): Boolean {
        if (StringUtil.isEmpty(strRoles)) {
            val ru: Credential = pc.getRemoteUser() ?: return false
            return ru.getRoles().length > 0
        }
        val roles: Array<String?> = ListUtil.trimItems(ListUtil.listToStringArray(strRoles, ','))
        for (i in roles.indices) {
            if (IsUserInRole.call(pc, roles[i])) return true
        }
        return false
    }
}