package lucee.commons.cpu

import java.util.List

class UDFListener(udf: UDF) : CFMLListener() {
    private val udf: UDF

    @Override
    @Throws(PageException::class)
    override fun _listen(pc: PageContext?, sd: List<StaticData?>?) {
        udf.call(pc, arrayOf<Object>(toQuery(sd!!)), true)
    }

    init {
        this.udf = udf
    }
}