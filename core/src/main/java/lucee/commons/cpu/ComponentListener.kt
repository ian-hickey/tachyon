package lucee.commons.cpu

import java.util.List

class ComponentListener(cfc: Component) : CFMLListener() {
    private val cfc: Component

    @Override
    @Throws(PageException::class)
    override fun _listen(pc: PageContext?, sd: List<StaticData?>?) {
        cfc.call(pc, "listen", arrayOf<Object>(toQuery(sd!!)))
    }

    init {
        this.cfc = cfc
    }
}