package lucee.runtime.functions.string

import lucee.runtime.PageContext

class ReplaceListNoCase : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 6) return ReplaceList._call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]),
                Caster.toString(args[4]), true, Caster.toBooleanValue(args[5]))
        if (args.size == 5) {
            return if (Decision.isBoolean(args[4])) ReplaceList._call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]), Caster.toString(args[3]), false, Caster.toBooleanValue(args[4])) else ReplaceList._call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]),
                    Caster.toString(args[4]), true, false)
        }
        if (args.size == 4) {
            return if (Decision.isBoolean(args[3])) ReplaceList._call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), ",", ",", false, Caster.toBooleanValue(args[3])) else ReplaceList._call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]), ",", true, false)
        }
        if (args.size == 3) return ReplaceList._call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), ",", ",", true, false)
        throw FunctionException(pc, "ReplaceListNoCase", 3, 6, args.size)
    }

    companion object {
        private const val serialVersionUID = -8530160236310177587L
        fun call(pc: PageContext?, str: String?, list1: String?, list2: String?): String? {
            return ReplaceList._call(pc, str, list1, list2, ",", ",", true, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, str: String?, list1: String?, list2: String?, delimiter_list1: String?): String? {
            return if (Decision.isBoolean(delimiter_list1)) ReplaceList._call(pc, str, list1, list2, ",", ",", true, Caster.toBooleanValue(delimiter_list1)) else ReplaceList._call(pc, str, list1, list2, delimiter_list1, delimiter_list1, true, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, str: String?, list1: String?, list2: String?, delimiter_list1: String?, delimiter_list2: String?): String? {
            return if (Decision.isBoolean(delimiter_list2)) ReplaceList._call(pc, str, list1, list2, delimiter_list1, delimiter_list1, true, Caster.toBooleanValue(delimiter_list2)) else ReplaceList._call(pc, str, list1, list2, delimiter_list1, delimiter_list2, true, false)
        }

        fun call(pc: PageContext?, str: String?, list1: String?, list2: String?, delimiter_list1: String?, delimiter_list2: String?, includeEmptyFields: Boolean): String? {
            return ReplaceList._call(pc, str, list1, list2, delimiter_list1, delimiter_list2, true, includeEmptyFields)
        }
    }
}