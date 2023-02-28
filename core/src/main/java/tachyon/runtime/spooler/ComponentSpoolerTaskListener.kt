package tachyon.runtime.spooler

import tachyon.commons.io.SystemUtil.TemplateLine

class ComponentSpoolerTaskListener(currTemplate: TemplateLine?, task: SpoolerTask?, component: Component?) : CFMLSpoolerTaskListener(currTemplate, task) {
    private val component: Component?

    @Override
    @Throws(PageException::class)
    override fun _listen(pc: PageContext?, args: Struct?, before: Boolean): Object? {
        if (before) {
            if (component.get("before", null) is UDF) return component.callWithNamedValues(pc, "before", args)
        } else {
            if (component.get("after", null) is UDF) return component.callWithNamedValues(pc, "after", args) else if (component.get("listen", null) is UDF) return component.callWithNamedValues(pc, "listen", args)
        }
        return null
    }

    companion object {
        private const val serialVersionUID = -4726393142628827635L
    }

    init {
        this.component = component
    }
}