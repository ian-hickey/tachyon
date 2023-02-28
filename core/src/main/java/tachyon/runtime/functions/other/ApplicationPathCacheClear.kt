package tachyon.runtime.functions.other

import tachyon.runtime.PageContext

object ApplicationPathCacheClear {
    fun call(pc: PageContext?): String? {
        val config: ConfigPro = pc.getConfig() as ConfigPro
        config.clearApplicationCache()
        return null
    }
}