package lucee.runtime.tag.listener

import lucee.runtime.Component

class ComponentTagListener(component: Component?) : TagListenerSupport() {
    private val component: Component?

    @Override
    @Throws(PageException::class)
    override fun before(pc: PageContext?, args: Struct?): Struct? {
        return if (component.get("before", null) is UDF) Caster.toStruct(component.callWithNamedValues(pc, "before", args), null) else null
    }

    @Override
    @Throws(PageException::class)
    override fun after(pc: PageContext?, args: Struct?): Struct? {
        if (component.get("after", null) is UDF) return Caster.toStruct(component.callWithNamedValues(pc, "after", args), null) else if (component.get("listen", null) is UDF) return Caster.toStruct(component.callWithNamedValues(pc, "listen", args), null)
        return null
    }

    @Override
    override fun hasError(): Boolean {
        return component.get("error", null) is UDF
    }

    @Override
    @Throws(PageException::class)
    override fun error(pc: PageContext?, args: Struct?): Struct? {
        return if (component.get("error", null) is UDF) Caster.toStruct(component.callWithNamedValues(pc, "error", args), null) else null
    }

    fun getComponent(): Object? {
        return component
    }

    init {
        this.component = component
    }
}