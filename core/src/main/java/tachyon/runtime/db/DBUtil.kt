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
package tachyon.runtime.db

import tachyon.commons.lang.StringUtil

object DBUtil {
    private var DB2: DataSourceDefintion? = null
    private var FIREBIRD: DataSourceDefintion? = null
    private var H2: DataSourceDefintion? = null
    private var MSSQL: DataSourceDefintion? = null
    private var MYSQL: DataSourceDefintion? = null
    private var ORACLE: DataSourceDefintion? = null
    private var POSTGRESQL: DataSourceDefintion? = null
    private var SYBASE: DataSourceDefintion? = null
    fun getDataSourceDefintionForType(config: Config, type: String, defaultValue: DataSourceDefintion?): DataSourceDefintion? {
        var type = type
        if (StringUtil.isEmpty(type)) return defaultValue
        type = type.trim().toLowerCase()
        // TODO extract data from JDBC config
        if ("db2".equals(type)) {
            if (DB2 == null) {
                DB2 = DataSourceDefintion("com.ddtek.jdbc.db2.DB2Driver", "jdbc:datadirect:db2://{host}:{port};DatabaseName={database}", 50000)
            }
            return DB2
        }
        if ("firebird".equals(type)) {
            if (FIREBIRD == null) {
                FIREBIRD = DataSourceDefintion("org.firebirdsql.jdbc.FBDriver", "jdbc:firebirdsql://{host}:{port}/{path}{database}", 3050)
            }
            return FIREBIRD
        }
        if ("h2".equals(type)) {
            if (H2 == null) {
                val jdbc: JDBCDriver? = getJDBCDriver(config, "h2", "org.h2.Driver", "org.h2", "1.3.172", "jdbc:h2:{path}{database};MODE={mode}")
                H2 = DataSourceDefintion(jdbc!!.cd, jdbc!!.connStr, -1)
            }
            return H2
        }
        if ("mssql".equals(type)) {
            if (MSSQL == null) {
                val jdbc: JDBCDriver? = getJDBCDriver(config, "mssql", "net.sourceforge.jtds.jdbc.Driver", "jtds", "1.3.1", "jdbc:jtds:sqlserver://{host}:{port}/{database}")
                MSSQL = DataSourceDefintion(jdbc!!.cd, jdbc!!.connStr, 1433)
            }
            return MSSQL
        }
        if ("mysql".equals(type)) {
            if (MYSQL == null) {
                val jdbc: JDBCDriver? = getJDBCDriver(config, "mysql", "com.mysql.cj.jdbc.Driver", "com.mysql.cj", "8.0.15", "jdbc:mysql://{host}:{port}/{database}")
                MYSQL = DataSourceDefintion(jdbc!!.cd, jdbc!!.connStr, 3306)
            }
            return MYSQL
        }
        if ("oracle".equals(type)) {
            if (ORACLE == null) {
                val jdbc: JDBCDriver? = getJDBCDriver(config, "oracle", "oracle.jdbc.driver.OracleDriver", "odjbc6", "11.2.0.4",
                        "jdbc:oracle:{drivertype}:@{host}:{port}:{database}")
                ORACLE = DataSourceDefintion(jdbc!!.cd, jdbc!!.connStr, 1521)
            }
            return ORACLE
        }
        if ("postgresql".equals(type) || "postgre".equals(type)) {
            if (POSTGRESQL == null) {
                val jdbc: JDBCDriver? = getJDBCDriver(config, "postgresql", "org.postgresql.Driver", "org.postgresql.jdbc", "42.2.20", "jdbc:postgresql://{host}:{port}/{database}")
                POSTGRESQL = DataSourceDefintion(jdbc!!.cd, jdbc!!.connStr, 5432)
            }
            return POSTGRESQL
        }
        if ("sybase".equals(type)) {
            if (SYBASE == null) {
                val jdbc: JDBCDriver? = getJDBCDriver(config, "sybase", "net.sourceforge.jtds.jdbc.Driver", "jtds", "1.3.1", "jdbc:jtds:sybase://{host}:{port}/{database}")
                SYBASE = DataSourceDefintion(jdbc!!.cd, jdbc!!.connStr, 7100)
            }
            return SYBASE
        }
        return defaultValue
    }

    private fun getJDBCDriver(config: Config, id: String, className: String, bundleName: String, bundleVersion: String, connStr: String): JDBCDriver? {
        // FUTURE remove the hardcoded fallback
        val ci: ConfigPro = config as ConfigPro
        var jdbc: JDBCDriver = ci.getJDBCDriverById(id, null)
        if (jdbc != null) return improve(jdbc, connStr)
        jdbc = ci.getJDBCDriverByClassName(className, null)
        if (jdbc != null) return improve(jdbc, connStr)
        jdbc = ci.getJDBCDriverByBundle(bundleName, OSGiUtil.toVersion(bundleVersion, null), null)
        return if (jdbc != null) improve(jdbc, connStr) else JDBCDriver(id, id, connStr, ClassDefinitionImpl(className, bundleName, bundleVersion, config.getIdentification()))
    }

    private fun improve(jdbc: JDBCDriver?, connStr: String): JDBCDriver? {
        if (StringUtil.isEmpty(jdbc!!.connStr)) jdbc!!.connStr = connStr
        return jdbc
    }

    class DataSourceDefintion internal constructor(cd: ClassDefinition?, connectionString: String, port: Int) {
        var classDefinition: ClassDefinition? = null
        val connectionString: String
        val port: Int

        internal constructor(className: String?, connectionString: String?, port: Int) : this(ClassDefinitionImpl(className), connectionString, port) {}

        init {
            classDefinition = cd
            this.connectionString = connectionString
            this.port = port
        }
    }
}