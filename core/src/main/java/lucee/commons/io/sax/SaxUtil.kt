package lucee.commons.io.sax

import java.util.HashMap

object SaxUtil {
    fun toMap(atts: Attributes): Map<String, String> {
        val rtn: Map<String, String> = HashMap()
        val len: Int = atts.getLength()
        for (i in 0 until len) {
            rtn.put(atts.getLocalName(i), atts.getValue(i))
        }
        return rtn
    }
}