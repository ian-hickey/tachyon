package lucee.runtime.osgi

import java.util.ArrayList

class VersionRange(rawVersionRanges: String?) {
    var vrs: List<VR?>? = ArrayList<VR?>()
    fun isWithin(version: Version?): Boolean {
        for (vr in vrs!!) {
            if (vr!!.isWithin(version)) return true
        }
        return false
    }

    @Override
    override fun toString(): String {
        val sb = StringBuilder()
        for (vr in vrs!!) {
            if (sb.length() > 0) sb.append(',')
            sb.append(vr.toString())
        }
        return sb.toString()
    }

    class VR(from: Version?, to: Version?) {
        private val from: Version?
        private val to: Version?
        fun isWithin(version: Version?): Boolean {
            if (from != null && Util.isNewerThan(from, version)) return false
            return if (to != null && Util.isNewerThan(version, to)) false else true
        }

        @Override
        override fun toString(): String {
            return if (from != null && to != null && from.equals(to)) from.toString() else (if (from == null) "" else from.toString()) + "-" + if (to == null) "" else to.toString()
        }

        init {
            this.from = from
            this.to = to
        }
    }

    init {
        val it: Iterator<String?> = ListUtil.listToList(rawVersionRanges, ',', true).iterator()
        var str: String?
        var l: String
        var r: String
        var index: Int
        var f: Version?
        var t: Version?
        while (it.hasNext()) {
            str = it.next()
            if (StringUtil.isEmpty(str, true) || str!!.equals("-")) continue
            index = str.indexOf('-')
            if (index == -1) {
                f = OSGiUtil.toVersion(str, null)
                t = null
            } else {
                l = str.substring(0, index).trim()
                r = str.substring(index + 1).trim()
                f = if (!StringUtil.isEmpty(l, true)) OSGiUtil.toVersion(l, null) else null
                t = if (!StringUtil.isEmpty(r, true)) OSGiUtil.toVersion(r, null) else null
            }
            vrs.add(VR(f, t))
        }

        // 1-3,5,6-,-9
    }
}