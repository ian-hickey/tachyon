package tachyon.runtime.functions.thread

import tachyon.runtime.PageContext

class IsInThread : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        // No arguments allowed
        if (args!!.size > 0) {
            throw FunctionException(pc, "isInThread", 0, 0, args.size)
        }
        return call(pc)
    }

    companion object {
        private const val serialVersionUID = 9100222392353284434L

        /**
         * Verify if in thread or not
         *
         * @param pc
         * @return
         * @throws PageException
         */
        @Throws(PageException::class)
        fun call(pc: PageContext?): Boolean {
            val root: PageContext = (pc as PageContextImpl?).getRootPageContext()
            return root != null && root !== pc
        }
    }
}