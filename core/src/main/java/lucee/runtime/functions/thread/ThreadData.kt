package lucee.runtime.functions.thread

import lucee.runtime.PageContext

class ThreadData : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 0) call(pc) else throw FunctionException(pc, "ThreadData", 0, 0, args.size)
    }

    companion object {
        /**
         * Verify if in thread or not
         *
         * @param pc
         * @return
         * @throws PageException
         */
        @Throws(PageException::class)
        fun call(pc: PageContext?): Struct? {
            val pci: PageContextImpl? = pc as PageContextImpl?
            var root: PageContextImpl? = pci.getRootPageContext() as PageContextImpl
            if (root == null) root = pci
            return root.getCFThreadScope()
        }
    }
}