package lucee.runtime.query.caster

import java.sql.DatabaseMetaData

class SQLXMLCast : Cast {
    @Override
    @Throws(SQLException::class)
    override fun toCFType(tz: TimeZone?, rst: ResultSet?, columnIndex: Int): Object? {
        return try {
            rst.getSQLXML(columnIndex).getString()
        } catch (se: SQLException) {
            throw se
        } catch (t: Throwable) { // must be a throwable because it throws for example a AbstractMethodError with JDTS, but could also
            // be other
            ExceptionUtil.rethrowIfNecessary(t)
            val md: DatabaseMetaData = rst.getStatement().getConnection().getMetaData()
            if (md.getJDBCMajorVersion() < 4) throw PageRuntimeException(
                    DatabaseException("The data type [SQLXML] is not supported with this datasource.", ("The datasource JDBC driver compatibility is up to the versions ["
                            + md.getJDBCMajorVersion()) + "." + md.getJDBCMinorVersion().toString() + "], but this feature needs at least [4.0]", null, null))
            throw PageRuntimeException(Caster.toPageException(t))
        }
    }
}