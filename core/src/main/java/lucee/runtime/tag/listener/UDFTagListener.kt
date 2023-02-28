package lucee.runtime.tag.listener

import lucee.runtime.PageContext

class UDFTagListener(before: UDF?, after: UDF?, error: UDF?) : TagListenerSupport() {
    // UDF before, UDF after
    private val before: UDF?
    private val after: UDF?
    private val error: UDF?

    @Override
    @Throws(PageException::class)
    override fun before(pc: PageContext?, args: Struct?): Struct? {
        return if (before != null) Caster.toStruct(before.callWithNamedValues(pc, args, true), null) else null
    }

    @Override
    @Throws(PageException::class)
    override fun after(pc: PageContext?, args: Struct?): Struct? {
        return if (after != null) Caster.toStruct(after.callWithNamedValues(pc, args, true), null) else null
    }

    @Override
    override fun hasError(): Boolean {
        return error != null
    }

    @Override
    @Throws(PageException::class)
    override fun error(pc: PageContext?, args: Struct?): Struct? {
        return if (error != null) Caster.toStruct(error.callWithNamedValues(pc, args, true), null) else null
    }

    val struct: Object?
        get() {
            val sct: Struct = StructImpl()
            sct.put("before", before)
            sct.put("after", after)
            sct.put("error", error)
            return sct
        }

    init {
        this.before = before
        this.after = after
        this.error = error
    }
}