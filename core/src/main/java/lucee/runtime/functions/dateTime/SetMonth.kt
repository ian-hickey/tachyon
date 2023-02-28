package lucee.runtime.functions.dateTime

import java.util.TimeZone

class SetMonth : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size < 2 || args.size > 3) throw FunctionException(pc, "SetMonth", 2, 3, args.size)
        val tz: TimeZone = if (args.size == 3) Caster.toTimeZone(args[2], pc.getTimeZone()) else pc.getTimeZone()
        return _call(Caster.toDate(args[0], tz), Caster.toIntValue(args[1]), tz)
    }

    companion object {
        private const val serialVersionUID = 4327734406273363858L
        fun call(pc: PageContext?, date: DateTime?, value: Double): DateTime? {
            return _call(date, value.toInt(), pc.getTimeZone())
        }

        fun call(pc: PageContext?, date: DateTime?, value: Double, tz: TimeZone?): DateTime? {
            return _call(date, value.toInt(), if (tz == null) pc.getTimeZone() else tz)
        }

        private fun _call(date: DateTime?, value: Int, tz: TimeZone?): DateTime? {
            DateTimeUtil.getInstance().setMonth(tz, date, value)
            return date
        }
    }
}