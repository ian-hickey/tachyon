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
package coldfusion.server

import java.util.Map

interface ClientScopeService : Service {
    // public abstract ClientScope GetClientScope(PageContext pc,ClientScopeKey arg1, Properties arg2);
    fun GetClientId(pc: PageContext?): Int
    fun GetCFTOKEN(): String?
    fun PersistClientVariablesForRequest()

    // public abstract void UpdateGlobals(PageContext pc, ClientScope arg1);
    fun GetDefaultDSN(): String?
    fun IsValidDSN(arg0: String?): Boolean
    fun getClientstores(): Map?
    fun getSettings(): Map?
    fun PurgeExpiredClients()
}