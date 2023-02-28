package tachyon.runtime.net.rpc.server

import tachyon.runtime.config.Config

object WSUtil {
    // used by genertaed bytecode
    @Throws(PageException::class)
    operator fun invoke(name: String?, args: Array<Object?>?): Object? {
        return invoke(null, name, args)
    }

    @Throws(PageException::class)
    operator fun invoke(config: Config?, name: String?, args: Array<Object?>?): Object? {
        return (ThreadLocalPageContext.getConfig(config) as ConfigWebPro).getWSHandler().getWSServer(ThreadLocalPageContext.get()).invoke(name, args)
    }
}