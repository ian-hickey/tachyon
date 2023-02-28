package tachyon.runtime.type

import java.io.IOException

class Lambda : EnvUDF {
    constructor() : super() { // used for externalize
    }

    constructor(properties: UDFProperties?) : super(properties) {}
    private constructor(properties: UDFProperties?, variables: Variables?) : super(properties, variables) { // used for duplicate
    }

    @Override
    override fun _toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return UDFUtil.toDumpData(pageContext, maxlevel, dp, this, UDFUtil.TYPE_LAMBDA)
    }

    @Override
    @Throws(PageException::class)
    override fun _getMetaData(pc: PageContext?): Struct? {
        val meta: Struct = ComponentUtil.getMetaData(pc, properties, null)
        meta.setEL(KeyConstants._closure, Boolean.TRUE) // MUST move this to class UDFProperties
        meta.setEL("ANONYMOUSLAMBDA", Boolean.TRUE) // MUST move this to class UDFProperties
        return meta
    }

    @Override
    override fun _duplicate(c: Component?): UDF? {
        val lam = Lambda(properties, variables) // TODO duplicate variables as well?
        lam.ownerComponent = c
        lam.setAccess(getAccess())
        return lam
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