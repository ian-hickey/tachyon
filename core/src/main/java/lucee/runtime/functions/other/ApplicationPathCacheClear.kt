package lucee.runtime.functions.other

import lucee.runtime.PageContext

object ApplicationPathCacheClear {
    fun call(pc: PageContext?): String? {
        val config: ConfigPro = pc.getConfig() as ConfigPro
        config.clearApplicationCache()
        return null
    }
}