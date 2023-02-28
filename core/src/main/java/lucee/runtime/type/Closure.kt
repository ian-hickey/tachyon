package lucee.runtime.type

import java.io.IOException

class Closure : EnvUDF {
    constructor() : super() { // used for externalize
    }

    constructor(properties: UDFProperties?) : super(properties) {}
    private constructor(properties: UDFProperties?, variables: Variables?) : super(properties, variables) { // used for duplicate
    }

    @Override
    override fun _toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return UDFUtil.toDumpData(pageContext, maxlevel, dp, this, UDFUtil.TYPE_CLOSURE)
    }

    @Override
    @Throws(PageException::class)
    override fun _getMetaData(pc: PageContext?): Struct? {
        val meta: Struct = ComponentUtil.getMetaData(pc, properties, null)
        meta.setEL(KeyConstants._closure, Boolean.TRUE) // MUST move this to class UDFProperties
        meta.setEL("ANONYMOUSCLOSURE", Boolean.TRUE) // MUST move this to class UDFProperties
        return meta
    }

    @Override
    override fun _duplicate(c: Component?): UDF? {
        val clo = Closure(properties, variables) // TODO duplicate variables as well?
        clo.ownerComponent = c
        clo.setAccess(getAccess())
        return clo
    }

    @Override
    @Throws(IOException::class, ClassNotFoundException::class)
    override fun readExternal(`in`: ObjectInput?) {
        super.readExternal(`in`)
    }

    @Override
    @Throws(IOException::class)
    override fun writeExternal(out: ObjectOutput?) {
        super.writeExternal(out)
    }
}