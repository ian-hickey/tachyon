package lucee.runtime.functions.other

import de.mkammerer.argon2.Argon2

class GenerateArgon2Hash : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size != 5) {
            throw FunctionException(pc, "GenerateArgon2Hash", 5, 5, args.size)
        }
        return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toByteValue(args[2]), Caster.toIntValue(args[3]), Caster.toByteValue(args[4]))
    }

    companion object {
        private const val serialVersionUID = 61397352504711269L
        @Throws(PageException::class)
        fun call(pc: PageContext?, input: String?, variant: String?, parallelismFactor: Double, memoryCost: Double, iterations: Double): String? {
            var variant = variant
            val type: Argon2Types

            // check variant
            if (StringUtil.isEmpty(variant, true)) throw FunctionException(pc, "GenerateArgon2Hash", 1, "variant", "The Variant should be ARGON2i or ARGON2d or ARGON2id")
            variant = variant.trim()
            type = when (variant.toLowerCase()) {
                "argon2i" -> Argon2Types.ARGON2i
                "argon2d" -> Argon2Types.ARGON2d
                "argon2id" -> Argon2Types.ARGON2id
                else -> throw FunctionException(pc, "GenerateArgon2Hash", 1, "variant", "The Variant should be ARGON2i or ARGON2d or ARGON2id")
            }
            val argon2: Argon2 = Argon2Factory.create(type)
            if (parallelismFactor < 1 || parallelismFactor > 10) {
                throw FunctionException(pc, "GenerateArgon2Hash", 2, "parallelismFactor", "The parallelism factor value should be between 1 and 10", null)
            }
            if (memoryCost < 8 || memoryCost > 100000) {
                throw FunctionException(pc, "GenerateArgon2Hash", 3, "memoryCost", "The memory cost value should be between 8 and 100000", null)
            }
            if (iterations < 1 || iterations > 20) {
                throw FunctionException(pc, "GenerateArgon2Hash", 4, "iterations", "The iterations value should be between 1 and 20", null)
            }
            val memory: Int = Caster.toIntValue(memoryCost)
            val carrInput = if (input == null) CharArray(0) else input.toCharArray()
            val hash: String = argon2.hash(Caster.toIntValue(iterations), memory, Caster.toIntValue(parallelismFactor), carrInput)
            val success: Boolean = argon2.verify(hash, carrInput)
            if (!success) {
                throw ExpressionException("Hashing failed!")
            }
            return hash
        }
    }
}