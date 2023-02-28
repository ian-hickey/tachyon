package tachyon.runtime.functions.string

import java.util.Arrays

class StringSort : BIF(), Function {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size != 1) {
            throw FunctionException(pc, "StringSort", 1, 1, args.size)
        }
        return call(pc, Caster.toString(args[0]))
    }

    companion object {
        private const val serialVersionUID = 8201208274877675500L
        @Throws(PageException::class)
        fun call(pc: PageContext?, input: String?): String? {
            val inputArray: CharArray = input.toCharArray()
            Arrays.sort(inputArray)
            return String(inputArray)
        }
    }
}