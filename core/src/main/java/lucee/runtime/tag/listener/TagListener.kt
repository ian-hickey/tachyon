package lucee.runtime.tag.listener

import lucee.runtime.PageContext

interface TagListener {
    @Throws(PageException::class)
    fun before(pc: PageContext?, args: Struct?): Struct?

    @Throws(PageException::class)
    fun after(pc: PageContext?, args: Struct?): Struct?
    fun hasError(): Boolean

    @Throws(PageException::class)
    fun error(pc: PageContext?, args: Struct?): Struct?

    companion object {
        fun toCFML(tl: TagListener?, defaultValue: Object?): Object? {
            if (tl == null) return defaultValue
            return if (tl is ComponentTagListener) (tl as ComponentTagListener?)!!.getComponent() else if (tl is UDFTagListener) (tl as UDFTagListener?).getStruct() else defaultValue
        }
    }
}