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
package coldfusion.sql

import java.util.Map

interface DataSourceDef {
    operator fun get(arg1: Object?): Object?
    fun getType(): Int
    fun getClassDefinition(): ClassDefinition?
    fun getHost(): String?
    fun getPort(): Int
    fun isDynamic(): Boolean
    fun isConnectionEnabled(): Boolean
    fun isBlobEnabled(): Boolean
    fun isClobEnabled(): Boolean
    fun getDriver(): String?
    fun setDriver(arg1: String?)
    fun getAllowedSQL(): Struct?
    fun setAllowedSQL(arg1: Struct?)
    fun isSQLRestricted(): Boolean
    fun setMap(arg1: Map?)
    fun isRemoveOnPageEnd(): Boolean
    fun setRemoveOnPageEnd(arg1: Boolean)
    fun setDynamic(arg1: Boolean)
    fun getIfxSrv(): String?
    fun setIfxSrv(arg1: String?)
    fun getStrPrmUni(): Boolean
    fun setStrPrmUni(arg1: Boolean)
    fun setStrPrmUni(arg1: String?)
    fun getSelectMethod(): String?
    fun setSelectMethod(arg1: String?)
    fun getSid(): String?
    fun setSid(arg1: String?)
    fun getJndiName(): String?
    fun setJndiName(arg1: String?)
    fun getMaxClobSize(): Int
    fun setMaxClobSize(arg1: Int)
    fun getMaxBlobSize(): Int
    fun setMaxBlobSize(arg1: Int)
    fun setClobEnabled(arg1: Boolean)
    fun setBlobEnabled(arg1: Boolean)
    fun setConnectionEnabled(arg1: Boolean)
    fun getLogintimeout(): Int
    fun setLogintimeout(arg1: Int)
    fun getMaxconnections(): Int
    fun setMaxConnections(arg1: Int)
    fun setMaxConnections(arg1: Object?)
    fun setDatabase(arg1: String?)
    fun getDatabase(): String?
    fun setHost(arg1: String?)
    fun setVendor(arg1: String?)
    fun getVendor(): String?
    fun getJndienv(): Struct?
    fun setLoginTimeout(arg1: Object?)
    fun getLoginTimeout(): Int
    fun setPort(arg1: Int)
    fun setPort(arg1: Object?)
    fun getMaxConnections(): Int
    fun setJndienv(arg1: Struct?)
    fun setJNDIName(arg1: String?)
    fun getJNDIName(): String?
    fun setType(arg1: String?)
    fun setType(arg1: Int)
    fun getDsn(): String?
    fun setDsn(arg1: String?)
    fun setClassDefinition(cd: ClassDefinition?)
    fun getDesc(): String?
    fun setDesc(arg1: String?)
    fun getUsername(): String?
    fun setUsername(arg1: String?)
    fun setPassword(arg1: String?)
    fun getUrl(): String?
    fun setUrl(arg1: String?)
    fun isPooling(): Boolean
    fun setPooling(arg1: Boolean)
    fun getTimeout(): Int
    fun setTimeout(arg1: Int)
    fun getInterval(): Int
    fun setInterval(arg1: Int)
    fun getExtraData(): Struct?
    fun setExtraData(arg1: Struct?)
    fun setMaxPooledStatements(arg1: Int)
    fun getMaxPooledStatements(): Int
}