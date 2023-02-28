package tachyon.runtime.functions.arrays

import java.util.Iterator

class ArraySplice : BIF(), Function {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toArray(args[0]), Caster.toDoubleValue(args[1]))
        if (args.size == 3) return call(pc, Caster.toArray(args[0]), Caster.toDoubleValue(args[1]), Caster.toDoubleValue(args[2]))
        if (args.size == 4) return call(pc, Caster.toArray(args[0]), Caster.toDoubleValue(args[1]), Caster.toDoubleValue(args[2]), Caster.toArray(args[3]))
        throw FunctionException(pc, "ArraySplice", 2, 4, args.size)
    } /*
	 * public static void main(String[] args2) throws PageException { ArrayImpl arr = new ArrayImpl();
	 * arr.add("a"); arr.add("b"); arr.add("c"); arr.add("d"); ArrayImpl rep = new ArrayImpl();
	 * rep.add("111"); rep.add("222"); print.e(arr); Object[] args = new Object[] { arr, 2, 1, rep };
	 * Object res = new ArraySplice().invoke(null, args); print.e(arr); print.e(res); }
	 */

    companion object {
        private const val serialVersionUID = -8604228677976070247L
        @Throws(PageException::class)
        fun call(pc: PageContext?, arr: Array?, index: Double): Array? {
            return call(pc, arr, index, -1.0, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, arr: Array?, index: Double, len: Double): Array? {
            return call(pc, arr, index, len, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, arr: Array?, index: Double, length: Double, replacements: Array?): Array? {
            var index = index
            val removed: Array = ArrayImpl()
            // check index
            if (index < 1) index = arr.size() + index + 1 else if (index > arr.size()) index = arr.size() + 1
            var idx = index.toInt()

            // check len
            var len = length.toInt()
            if (len == -1) len = arr.size() as Int - idx + 1 else if (len < -1) len = 0 // stupid ut how acf works
            else {
                val size: Int = arr.size()
                if (len - 1 > size - idx) len = size - idx + 1
            }

            // first we remove what is not needed
            while (len > 0) {
                removed.append(arr.removeE(idx))
                len--
            }
            // insert data
            if (replacements != null) {
                val it: Iterator<Object?> = replacements.valueIterator()
                while (it.hasNext()) {
                    arr.insert(idx++, it.next())
                }
            }
            return removed
        }
    }
}