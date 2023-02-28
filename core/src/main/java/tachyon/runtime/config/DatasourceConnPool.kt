package tachyon.runtime.config

import java.util.Collection

class DatasourceConnPool(config: Config?, ds: DataSource?, user: String?, pass: String?, logName: String?, genericObjectPoolConfig: GenericObjectPoolConfig<DatasourceConnection?>?) : GenericObjectPool<DatasourceConnection?>(DatasourceConnectionFactory(config, ds, user, pass, logName), genericObjectPoolConfig) {
    @Override
    @Throws(PageException::class)
    fun borrowObject(): DatasourceConnection? {
        return try {
            super.borrowObject()
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    fun getFactory(): DatasourceConnectionFactory? {
        return super.getFactory() as DatasourceConnectionFactory?
    }

    companion object {
        fun meta(pools: Collection<DatasourceConnPool?>?): Struct? {
            // MUST do more data
            var ds: DataSource
            var sct: Struct?
            val arr: Struct = StructImpl()
            var fac: DatasourceConnectionFactory
            for (pool in pools!!) {
                fac = pool!!.getFactory()
                ds = fac.getDatasource()
                sct = StructImpl()
                try {
                    sct.setEL(KeyConstants._name, ds.getName())
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                }
                try {
                    sct.setEL("connectionLimit", ds.getConnectionLimit())
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                }
                try {
                    sct.setEL("connectionTimeout", ds.getConnectionTimeout())
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                }
                try {
                    sct.setEL("connectionString", ds.getConnectionStringTranslated())
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                }
                try {
                    val idle: Int = pool.getNumIdle()
                    val active: Int = pool.getNumActive()
                    val waiters: Int = pool.getNumWaiters()
                    sct.setEL("openConnections", active + idle)
                    sct.setEL("activeConnections", active)
                    sct.setEL("idleConnections", idle)
                    sct.setEL("waitingForConn", waiters)
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                }
                try {
                    sct.setEL(KeyConstants._database, ds.getDatabase())
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                }
                if (sct.size() > 0) arr.setEL(ds.getName(), sct)
            }
            return arr
        }

        fun createPoolConfig(blockWhenExhausted: Boolean?, fairness: Boolean?, lifo: Boolean?, minIdle: Int, maxIdle: Int, maxTotal: Int,
                             maxWaitMillis: Long, minEvictableIdleTimeMillis: Long, timeBetweenEvictionRunsMillis: Long, softMinEvictableIdleTimeMillis: Long, numTestsPerEvictionRun: Int,
                             evictionPolicyClassName: String?): GenericObjectPoolConfig<DatasourceConnection?>? {
            val config: GenericObjectPoolConfig<DatasourceConnection?> = GenericObjectPoolConfig<DatasourceConnection?>()
            config.setBlockWhenExhausted(if (blockWhenExhausted != null) blockWhenExhausted.booleanValue() else GenericObjectPoolConfig.DEFAULT_BLOCK_WHEN_EXHAUSTED)
            config.setFairness(if (fairness != null) fairness.booleanValue() else GenericObjectPoolConfig.DEFAULT_FAIRNESS)
            config.setLifo(if (lifo != null) lifo.booleanValue() else BaseObjectPoolConfig.DEFAULT_LIFO)
            config.setMinIdle(if (minIdle > 0) minIdle else GenericObjectPoolConfig.DEFAULT_MIN_IDLE)
            config.setMaxIdle(if (maxIdle > 0) maxIdle else GenericObjectPoolConfig.DEFAULT_MAX_IDLE)
            config.setMaxTotal(if (maxTotal > 0) maxTotal else GenericObjectPoolConfig.DEFAULT_MAX_TOTAL)
            config.setMaxWaitMillis(if (maxWaitMillis > 0) maxWaitMillis else GenericObjectPoolConfig.DEFAULT_MAX_WAIT_MILLIS)
            // TDOo merge with idleTimeout
            config.setMinEvictableIdleTimeMillis(if (minEvictableIdleTimeMillis > 0) minEvictableIdleTimeMillis else GenericObjectPoolConfig.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS)
            // TODO this was done so far by the controler
            config.setTimeBetweenEvictionRunsMillis(
                    if (timeBetweenEvictionRunsMillis > 0) timeBetweenEvictionRunsMillis else GenericObjectPoolConfig.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS)
            // TDOD maybe make ii not congigurable
            config.setNumTestsPerEvictionRun(if (numTestsPerEvictionRun > 0) numTestsPerEvictionRun else 30)
            // config.setSoftMinEvictableIdleTimeMillis(
            // softMinEvictableIdleTimeMillis > 0 ? softMinEvictableIdleTimeMillis :
            // GenericObjectPoolConfig.DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS);
            if (!StringUtil.isEmpty(evictionPolicyClassName)) config.setEvictionPolicyClassName(evictionPolicyClassName)
            config.setTestOnCreate(false)
            config.setTestOnBorrow(true)
            config.setTestOnReturn(false)
            config.setTestWhileIdle(true)
            config.setTestWhileIdle(true)
            return config
        }
    }

    init {
        getFactory().setPool(this)
    }
}