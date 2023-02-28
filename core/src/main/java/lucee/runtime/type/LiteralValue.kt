package lucee.runtime.type

import java.math.BigDecimal

/**
 * This class should only be used by created bytecode, because it does not the necessary checking at
 * runtime and expect the compiler did.
 *
 */
object LiteralValue {
    fun toNumber(pc: PageContext?, l: Long): Number? {
        return if ((pc.getApplicationContext() as ApplicationContextSupport).getPreciseMath()) BigDecimal.valueOf(l) else Double.valueOf(l)
    }

    fun toNumber(pc: PageContext?, d: Double): Number? {
        return if ((pc.getApplicationContext() as ApplicationContextSupport).getPreciseMath()) BigDecimal.valueOf(d) else Double.valueOf(d)
    }

    @Throws(CasterException::class)
    fun toNumber(pc: PageContext?, nbr: String?): Number? { // excpetion is not expected to bi driggerd
        return if ((pc.getApplicationContext() as ApplicationContextSupport).getPreciseMath()) Caster.toBigDecimal(nbr) else Double.valueOf(nbr)
    }
}