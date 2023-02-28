package tachyon.runtime.functions.file

import tachyon.commons.io.res.util.ResourceUtil

object FileInfo {
    @Throws(PageException::class)
    fun call(pc: PageContext?, path: String?): Struct? {
        return FileTag.getInfo(pc, ResourceUtil.toResourceExisting(pc, path), null)
    }
}