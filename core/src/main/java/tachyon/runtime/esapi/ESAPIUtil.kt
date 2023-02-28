package tachyon.runtime.esapi

import tachyon.runtime.PageContext

object ESAPIUtil {
    private var esapi: BIF? = null
    @Throws(PageException::class)
    fun esapiEncode(pc: PageContext?, encodeFor: String?, string: String?): String? {
        // we need to get the BIF ESAPIEncode
        return try {
            if (esapi == null) esapi = ClassUtilImpl().loadBIF(pc, "org.tachyon.extension.esapi.functions.ESAPIEncode", "esapi.extension", null)
            esapi.invoke(pc, arrayOf(encodeFor, string))
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }
}