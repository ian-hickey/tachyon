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

import java.io.IOException

interface DataSourceService : Service {
    /*
	 * TODO impl public abstract Query executeQuery(Connection arg0, String arg1, ParameterList arg2,
	 * Integer arg3, Integer arg4, Integer arg5, int[] arg6, int arg7, int arg8, boolean arg9, boolean
	 * arg10) throws SQLException;
	 * 
	 * public abstract Query executeQuery(Connection arg0, String arg1, ParameterList arg2, Integer
	 * arg3, Integer arg4, Integer arg5, int[] arg6, String arg7) throws SQLException;
	 * 
	 * public abstract Query executeQuery(Connection arg0, String arg1, ParameterList arg2, Integer
	 * arg3, Integer arg4, Integer arg5, int[] arg6, DataSourceDef arg7) throws SQLException;
	 * 
	 * public abstract Query executeQuery(Connection arg0, String arg1, ParameterList arg2, Integer
	 * arg3, Integer arg4, Integer arg5, int[] arg6, Object arg7) throws SQLException;
	 * 
	 * public abstract Query executeCall(Connection arg0, String arg1, ParameterList arg2, int[] arg3,
	 * Integer arg4, Integer arg5, int[] arg6, int arg7, int arg8, boolean arg9, boolean arg10) throws
	 * SQLException;
	 * 
	 * public abstract Query executeCall(Connection arg0, String arg1, ParameterList arg2, int[] arg3,
	 * Integer arg4, Integer arg5, int[] arg6, String arg7) throws SQLException;
	 * 
	 * public abstract Query executeCall(Connection arg0, String arg1, ParameterList arg2, int[] arg3,
	 * Integer arg4, Integer arg5, int[] arg6, DataSourceDef arg7) throws SQLException;
	 * 
	 * public abstract Query executeCall(Connection arg0, String arg1, ParameterList arg2, int[] arg3,
	 * Integer arg4, Integer arg5, int[] arg6, Object arg7) throws SQLException;
	 */
    @Throws(SecurityException::class)
    fun getDatasources(): Struct?

    @Throws(ServiceException::class, SecurityException::class)
    fun getDrivers(): Struct?

    @Throws(SecurityException::class)
    fun getNames(): Array?
    fun getDefaults(): Struct?
    fun getMaxQueryCount(): Number?
    fun setMaxQueryCount(arg0: Number?)
    fun encryptPassword(arg0: String?): String?

    @Throws(SQLException::class, SecurityException::class)
    fun verifyDatasource(arg0: String?): Boolean

    @Throws(SQLException::class, SecurityException::class)
    fun getDatasource(arg0: String?): DataSource?
    fun getDbdir(): String?
    fun getCachedQuery(arg0: String?): Object?
    fun setCachedQuery(arg0: String?, arg1: Object?)

    @Throws(IOException::class)
    fun purgeQueryCache()
    fun disableConnection(arg0: String?): Boolean
    fun isJadoZoomLoaded(): Boolean

    @Throws(SQLException::class, SecurityException::class)
    fun removeDatasource(arg0: String?)
}