package lucee.runtime.functions.other

import de.mkammerer.argon2.Argon2

class Argon2CheckHash : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size != 2) {
            throw FunctionException(pc, "Argon2CheckHash", 3, 3, args.size)
        }
        return call(pc, Caster.toString(args[0]), Caster.toString(args[1]))
    }

    companion object {
        private const val serialVersionUID = 4730626229333277363L
        @Throws(PageException::class)
        fun call(pc: PageContext?, input: String?, hash: String?): Boolean {
            val type: Argon2Types
            var variant = getVariant(pc, hash)
            if (StringUtil.isEmpty(variant, true)) throw FunctionException(pc, "GenerateArgon2Hash", 1, "variant", "The Variant should be ARGON2i or ARGON2d or ARGON2id")
            variant = variant.trim()
            type = when (variant.toLowerCase()) {
                "argon2i" -> Argon2Types.ARGON2i
                "argon2d" -> Argon2Types.ARGON2d
                "argon2id" -> Argon2Types.ARGON2id
                else -> throw FunctionException(pc, "Argon2CheckHash", 1, "variant", "The Variant should be ARGON2i or ARGON2d or ARGON2id")
            }
            val argon2: Argon2 = Argon2Factory.create(type)
            val carrInput = if (input == null) CharArray(0) else input.toCharArray()
            return argon2.verify(hash, carrInput)
        }

        @Throws(FunctionException::class)
        private fun getVariant(pc: PageContext?, hash: String?): String? {
            val variant = StringBuilder()
            var i = 0
            val n: Int = hash!!.length()
            while (i < n) {
                val c: Char = hash.charAt(i)
                if (i == 0 && c != '$') {
                    throw FunctionException(pc, "Argon2CheckHash", 1, "variant", "The format of hash string is wrong")
                }
                if (i > 0 && c == '$') {
                    return variant.toString()
                }
                if (i > 0) {
                    variant.append(c)
                }
                i++
            }
            throw FunctionException(pc, "Argon2CheckHash", 1, "variant", "The format of hash string is wrong")
        }
    }
}