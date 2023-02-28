package tachyon.runtime.functions.other

import java.security.KeyPair

class GenerateRSAKeys : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 0) createKeyPair() else throw FunctionException(pc, "GenerateRSAKey", 0, 0, args.size)
    }

    companion object {
        private const val serialVersionUID = 8436907807706520039L
        @Throws(PageException::class)
        fun call(pc: PageContext?): Struct? {
            return createKeyPair()
        }

        @Throws(PageException::class)
        private fun createKeyPair(): Struct? {
            return try {
                val keyPair: KeyPair = RSA.createKeyPair()
                val sct: Struct = StructImpl()
                sct.set("private", RSA.toString(keyPair.getPrivate()))
                sct.set("public", RSA.toString(keyPair.getPublic()))
                sct
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
        }
    }
}