package lucee.runtime.config
// only exists for the Hibernate extension
import lucee.runtime.db.DataSource

class MockPool {
    @Throws(PageException::class)
    fun getDatasourceConnection(config: Config?, ds: DataSource?, user: String?, pass: String?): DatasourceConnection? {
        return (config as ConfigPro?)!!.getDatasourceConnectionPool(ds, user, pass)!!.borrowObject()
    }
}