package lucee.runtime.functions.file

import lucee.commons.io.res.util.ResourceUtil

object FileInfo {
    @Throws(PageException::class)
    fun call(pc: PageContext?, path: String?): Struct? {
        return FileTag.getInfo(pc, ResourceUtil.toResourceExisting(pc, path), null)
    }
}