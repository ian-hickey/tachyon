package lucee.runtime.functions.string

import lucee.runtime.PageContext

class TrimWhiteSpace : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, Caster.toString(args[0]))
        throw FunctionException(pc, "CleanWhiteSpace", 1, 1, args.size)
    }

    companion object {
        const val CHAR_EMPTY = 0.toChar()
        const val CHAR_NL = '\n'
        const val CHAR_SPACE = ' '
        const val CHAR_TAB = '\t'
        const val CHAR_BS = '\b' // \x0B\
        val CHAR_FW = '\f'
        const val CHAR_RETURN = '\r'
        fun call(pc: PageContext?, input: String?): String? {
            val sb = StringBuilder()
            val len: Int = input!!.length()
            var charBuffer = CHAR_EMPTY
            var c: Char
            for (i in 0 until len) {
                c = input.charAt(i)
                when (c) {
                    CHAR_NL -> if (charBuffer != CHAR_NL) charBuffer = c
                    CHAR_BS, CHAR_FW, CHAR_RETURN, CHAR_SPACE, CHAR_TAB -> if (charBuffer == CHAR_EMPTY) charBuffer = c
                    else -> {
                        if (charBuffer != CHAR_EMPTY) {
                            val b = charBuffer // muss so bleiben!
                            charBuffer = CHAR_EMPTY
                            sb.append(b)
                        }
                        sb.append(c)
                    }
                }
            }
            if (charBuffer != CHAR_EMPTY) {
                sb.append(charBuffer)
            }
            return sb.toString()
        }
    }
}