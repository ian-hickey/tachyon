package lucee.runtime.spooler

import lucee.commons.io.SystemUtil.TemplateLine

class UDFSpoolerTaskListener(currTemplate: TemplateLine?, task: SpoolerTask?, before: UDF?, after: UDF?) : CFMLSpoolerTaskListener(currTemplate, task) {
    private val before: UDF?
    private val after: UDF?

    @Override
    @Throws(PageException::class)
    override fun _listen(pc: PageContext?, args: Struct?, before: Boolean): Object? {
        if (before) {
            if (this.before != null) return this.before.callWithNamedValues(pc, args, true)
        } else {
            if (after != null) return after.callWithNamedValues(pc, args, true)
        }
        return null
    }

    companion object {
        private const val serialVersionUID = 1262226524494987654L
    }

    init {
        this.before = before
        this.after = after
    }
}