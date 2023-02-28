/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.runtime.services

import java.util.Map

class DatSourceDefImpl(ds: DataSource?) : DataSourceDef {
    private val ds: DataSource?

    @Override
    operator fun get(arg1: Object?): Object? {
        // TODO Auto-generated method stub
        return null
    }

    @Override
    fun getAllowedSQL(): Struct? {
        val allow: Struct = StructImpl()
        allow.setEL(KeyConstants._alter, Caster.toBoolean(ds.hasAllow(DataSource.ALLOW_ALTER)))
        allow.setEL(KeyConstants._create, Caster.toBoolean(ds.hasAllow(DataSource.ALLOW_CREATE)))
        allow.setEL(KeyConstants._delete, Caster.toBoolean(ds.hasAllow(DataSource.ALLOW_DELETE)))
        allow.setEL(KeyConstants._drop, Caster.toBoolean(ds.hasAllow(DataSource.ALLOW_DROP)))
        allow.setEL(KeyConstants._grant, Caster.toBoolean(ds.hasAllow(DataSource.ALLOW_GRANT)))
        allow.setEL(KeyConstants._insert, Caster.toBoolean(ds.hasAllow(DataSource.ALLOW_INSERT)))
        allow.setEL(KeyConstants._revoke, Caster.toBoolean(ds.hasAllow(DataSource.ALLOW_REVOKE)))
        allow.setEL(KeyConstants._select, Caster.toBoolean(ds.hasAllow(DataSource.ALLOW_SELECT)))
        allow.setEL("storedproc", Caster.toBoolean(true)) // TODO
        allow.setEL(KeyConstants._update, Caster.toBoolean(ds.hasAllow(DataSource.ALLOW_UPDATE)))
        return allow
    }

    @Override
    fun getClassDefinition(): ClassDefinition? {
        return ds.getClassDefinition()
    }

    @Override
    fun getDatabase(): String? {
        return ds.getDatabase()
    }

    @Override
    fun getDesc(): String? {
        return ""
    }

    @Override
    fun getDriver(): String? {
        return ""
    }

    @Override
    fun getDsn(): String? {
        return ds.getDsnTranslated()
    }

    @Override
    fun getExtraData(): Struct? {
        val rtn: Struct = StructImpl()
        val connprop: Struct = StructImpl()
        val names: Array<String?> = ds.getCustomNames()
        rtn.setEL("connectionprops", connprop)
        for (i in names.indices) {
            connprop.setEL(names[i], ds.getCustomValue(names[i]))
        }
        rtn.setEL("maxpooledstatements", Double.valueOf(1000))
        rtn.setEL("sid", "")
        rtn.setEL("timestampasstring", Boolean.FALSE)
        rtn.setEL("useTrustedConnection", Boolean.FALSE)
        rtn.setEL("datasource", ds.getName())
        rtn.setEL("_port", Double.valueOf(ds.getPort()))
        rtn.setEL("port", Double.valueOf(ds.getPort()))
        rtn.setEL("_logintimeout", Double.valueOf(30))
        rtn.setEL("args", "")
        rtn.setEL("databaseFile", "")
        rtn.setEL("defaultpassword", "")
        rtn.setEL("defaultusername", "")
        rtn.setEL("host", ds.getHost())
        rtn.setEL("maxBufferSize", Double.valueOf(0))
        rtn.setEL("pagetimeout", Double.valueOf(0))
        rtn.setEL("selectMethod", "direct")
        rtn.setEL("sendStringParamterAsUnicode", Boolean.TRUE)
        rtn.setEL("systemDatabaseFile", "")
        return rtn
    }

    @Override
    fun getHost(): String? {
        return ds.getHost()
    }

    @Override
    fun getIfxSrv(): String? {
        return ""
    }

    @Override
    fun getInterval(): Int {
        return 0
    }

    @Override
    fun getJNDIName(): String? {
        return ""
    }

    @Override
    fun getJndiName(): String? {
        return getJNDIName()
    }

    @Override
    fun getJndienv(): Struct? {
        return StructImpl()
    }

    @Override
    fun getLoginTimeout(): Int {
        return ds.getConnectionTimeout()
    }

    @Override
    fun getLogintimeout(): Int {
        return getLoginTimeout()
    }

    @Override
    fun getMaxBlobSize(): Int {
        return 64000
    }

    @Override
    fun getMaxClobSize(): Int {
        return 64000
    }

    @Override
    fun getMaxConnections(): Int {
        return ds.getConnectionLimit()
    }

    @Override
    fun getMaxPooledStatements(): Int {
        return 0
    }

    @Override
    fun getMaxconnections(): Int {
        return getMaxConnections()
    }

    @Override
    fun getPort(): Int {
        return ds.getPort()
    }

    @Override
    fun getSelectMethod(): String? {
        return ""
    }

    @Override
    fun getSid(): String? {
        return ""
    }

    @Override
    fun getStrPrmUni(): Boolean {
        return false
    }

    @Override
    fun getTimeout(): Int {
        return ds.getConnectionTimeout()
    }

    @Override
    fun getType(): Int {
        return 0
    }

    @Override
    fun getUrl(): String? {
        return ds.getDsnTranslated()
    }

    @Override
    fun getUsername(): String? {
        return ds.getUsername()
    }

    @Override
    fun getVendor(): String? {
        return ""
    }

    @Override
    fun isBlobEnabled(): Boolean {
        return ds.isBlob()
    }

    @Override
    fun isClobEnabled(): Boolean {
        return ds.isClob()
    }

    @Override
    fun isConnectionEnabled(): Boolean {
        return true
    }

    @Override
    fun isDynamic(): Boolean {
        return false
    }

    @Override
    fun isPooling(): Boolean {
        return true
    }

    @Override
    fun isRemoveOnPageEnd(): Boolean {
        return false
    }

    @Override
    fun isSQLRestricted(): Boolean {
        return false
    }

    @Override
    fun setAllowedSQL(arg1: Struct?) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setBlobEnabled(arg1: Boolean) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setClobEnabled(arg1: Boolean) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setConnectionEnabled(arg1: Boolean) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setDatabase(arg1: String?) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setDesc(arg1: String?) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setDriver(arg1: String?) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setDsn(arg1: String?) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setDynamic(arg1: Boolean) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setExtraData(arg1: Struct?) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setHost(arg1: String?) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setIfxSrv(arg1: String?) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setInterval(arg1: Int) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setJNDIName(arg1: String?) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setJndiName(arg1: String?) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setJndienv(arg1: Struct?) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setLoginTimeout(arg1: Object?) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setLogintimeout(arg1: Int) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setMap(arg1: Map?) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setMaxBlobSize(arg1: Int) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setMaxClobSize(arg1: Int) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setMaxConnections(arg1: Int) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setMaxConnections(arg1: Object?) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setMaxPooledStatements(arg1: Int) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setPassword(arg1: String?) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setPooling(arg1: Boolean) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setPort(arg1: Int) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setPort(arg1: Object?) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setRemoveOnPageEnd(arg1: Boolean) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setSelectMethod(arg1: String?) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setSid(arg1: String?) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setStrPrmUni(arg1: Boolean) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setStrPrmUni(arg1: String?) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setTimeout(arg1: Int) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setType(arg1: String?) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setType(arg1: Int) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setUrl(arg1: String?) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setUsername(arg1: String?) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setVendor(arg1: String?) {
        // TODO Auto-generated method stub
    }

    @Override
    fun setClassDefinition(cd: ClassDefinition?) {
        // TODO Auto-generated method stub
    }

    init {
        this.ds = ds
    }
}