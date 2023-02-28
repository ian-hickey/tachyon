/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 *
 */
package tachyon.runtime.video

import java.lang.ref.SoftReference

class VideoUtilImpl private constructor() : VideoUtil {
    /**
     * @see tachyon.runtime.video.VideoUtil.createVideoInput
     */
    @Override
    fun createVideoInput(input: Resource?): VideoInput? {
        return VideoInputImpl(input)
    }

    /**
     * @see tachyon.runtime.video.VideoUtil.createVideoOutput
     */
    @Override
    fun createVideoOutput(output: Resource?): VideoOutput? {
        return VideoOutputImpl(output)
    }

    /**
     * @see tachyon.runtime.video.VideoUtil.createVideoProfile
     */
    @Override
    fun createVideoProfile(): VideoProfile? {
        return VideoProfileImpl()
    }

    @Override
    @Throws(PageException::class)
    fun toBytes(byt: String?): Long {
        var byt = byt
        byt = byt.trim().toLowerCase()
        if (byt.endsWith("kb/s") || byt.endsWith("kbps")) {
            return (Caster.toDoubleValue(byt.substring(0, byt.length() - 4).trim()) * 1024)
        }
        if (byt.endsWith("mb/s") || byt.endsWith("mbps")) {
            return (Caster.toDoubleValue(byt.substring(0, byt.length() - 4).trim()) * 1024 * 1024)
        }
        if (byt.endsWith("gb/s") || byt.endsWith("gbps")) {
            return (Caster.toDoubleValue(byt.substring(0, byt.length() - 4).trim()) * 1024 * 1024 * 1024)
        }
        if (byt.endsWith("b/s") || byt.endsWith("bps")) {
            return Caster.toDoubleValue(byt.substring(0, byt.length() - 3).trim())
        }
        if (byt.endsWith("kbit/s")) {
            return (Caster.toDoubleValue(byt.substring(0, byt.length() - 6).trim()) * 1024)
        }
        if (byt.endsWith("mbit/s")) {
            return (Caster.toDoubleValue(byt.substring(0, byt.length() - 6).trim()) * 1024 * 1024)
        }
        if (byt.endsWith("gbit/s")) {
            return (Caster.toDoubleValue(byt.substring(0, byt.length() - 6).trim()) * 1024 * 1024 * 1024)
        }
        if (byt.endsWith("bit/s")) {
            return Caster.toDoubleValue(byt.substring(0, byt.length() - 5).trim())
        }
        if (byt.endsWith("kb")) {
            return (Caster.toDoubleValue(byt.substring(0, byt.length() - 2).trim()) * 1024)
        }
        if (byt.endsWith("mb")) {
            return (Caster.toDoubleValue(byt.substring(0, byt.length() - 2).trim()) * 1024 * 1024)
        }
        if (byt.endsWith("gb")) {
            return (Caster.toDoubleValue(byt.substring(0, byt.length() - 2).trim()) * 1024 * 1024 * 1024)
        }
        if (byt.endsWith("g")) {
            return (Caster.toDoubleValue(byt.substring(0, byt.length() - 1).trim()) * 1024 * 1024 * 1024)
        }
        if (byt.endsWith("m")) {
            return (Caster.toDoubleValue(byt.substring(0, byt.length() - 1).trim()) * 1024 * 1024)
        }
        if (byt.endsWith("k")) {
            return (Caster.toDoubleValue(byt.substring(0, byt.length() - 1).trim()) * 1024)
        }
        return if (byt.endsWith("b")) {
            Caster.toDoubleValue(byt.substring(0, byt.length() - 1).trim())
        } else Caster.toLongValue(byt)
    }

    @Override
    @Throws(PageException::class)
    fun toHerz(byt: String?): Long {
        var byt = byt
        byt = byt.trim().toLowerCase()
        if (byt.endsWith("mhz")) {
            return (Caster.toDoubleValue(byt.substring(0, byt.length() - 3).trim()) * 1000 * 1000)
        }
        if (byt.endsWith("khz")) {
            return (Caster.toDoubleValue(byt.substring(0, byt.length() - 3).trim()) * 1000)
        }
        return if (byt.endsWith("hz")) {
            Caster.toDoubleValue(byt.substring(0, byt.length() - 2).trim())
        } else Caster.toLongValue(byt)
    }

    @Override
    @Throws(PageException::class)
    fun toMillis(time: String?): Long {
        var last = 0
        var index: Int = time.indexOf(':')
        val hour: Long = Caster.toIntValue(time.substring(last, index).trim())
        last = index + 1
        index = time.indexOf(':', last)
        val minute: Long = Caster.toIntValue(time.substring(last, index).trim())
        val seconds: Double = Caster.toDoubleValue(time.substring(index + 1).trim())
        return hour * 60L * 60L * 1000L + minute * 60L * 1000L + (seconds * 1000f).toInt()
    }

    @Override
    @Throws(PageException::class)
    fun calculateDimension(pc: PageContext?, sources: Array<VideoInput?>?, width: Int, strWidth: String?, height: Int, strHeight: String?): IntArray? {
        var width = width
        var height = height
        val rtn: IntArray
        if (width != -1 && height != -1) {
            return intArrayOf(width, height)
        }

        // video component not installed
        try {
            if (createVideoExecuter(pc.getConfig()) is VideoExecuterNotSupported) {
                throw ApplicationException("attributes width/height are required when no video analyser is installed")
            }
        } catch (e: ClassException) {
        }
        var source: VideoInput?

        // hash
        val sb = StringBuffer(strHeight.toString() + "-" + strWidth)
        for (i in sources.indices) {
            sb.append(sources!![i].getResource().toString())
        }

        // get from casche
        val key: String = Hash.call(pc, sb.toString())
        val tmp: SoftReference<IntArray?>? = sizes!![key]
        val ci: IntArray? = if (tmp == null) null else tmp.get()
        if (ci != null) {
            return ci
        }
        // getSize
        var w = 0
        var h = 0
        try {
            for (i in sources.indices) {
                source = sources!![i]
                checkResource(source.getResource())
                val info: VideoInfo = createVideoExecuter(pc.getConfig()).info(pc.getConfig(), source)
                if (w < info.getWidth()) {
                    h = info.getHeight()
                    w = info.getWidth()
                }
            }
        } catch (ve: Exception) {
            throw Caster.toPageException(ve)
        }

        // calculate only height
        if (width != -1) {
            height = calculateSingle(w, width, strHeight, h)
        } else if (height != -1) {
            width = calculateSingle(h, height, strWidth, w)
        } else {
            width = procent2pixel(strWidth, w)
            height = procent2pixel(strHeight, h)
            if (width != -1 && height != -1) {
            } else if (width == -1 && height == -1) {
                width = w
                height = h
            } else if (width != -1) height = calucalteFromOther(h, w, width) else width = calucalteFromOther(w, h, height)
        }
        rtn = intArrayOf(width, height)
        sizes.put(key, SoftReference<IntArray?>(rtn))
        return rtn
    }

    companion object {
        private val sizes: Map<String?, SoftReference<IntArray?>?>? = ConcurrentHashMap<String?, SoftReference<IntArray?>?>()
        private val instance: VideoUtilImpl? = VideoUtilImpl()
        fun getInstance(): VideoUtilImpl? {
            return instance
        }

        @Throws(ClassException::class)
        fun createVideoExecuter(config: Config?): VideoExecuter? {
            val clazz: Class = config.getVideoExecuterClass()
            return ClassUtil.loadInstance(clazz) as VideoExecuter
        }

        @Throws(ExpressionException::class)
        private fun procent2pixel(str: String?, source: Int): Int {
            var str = str
            if (!StringUtil.isEmpty(str)) {
                if (StringUtil.endsWith(str, '%')) {
                    str = str.substring(0, str!!.length() - 1).trim()
                    val procent: Double = Caster.toDoubleValue(str)
                    if (procent < 0) throw ExpressionException("procent has to be positive number (now $str)")
                    return (source * (procent / 100.0)).toInt()
                }
                return Caster.toIntValue(str)
            }
            return -1
        }

        @Throws(ExpressionException::class)
        private fun calculateSingle(srcOther: Int, destOther: Int, strDim: String?, srcDim: Int): Int {
            val res = procent2pixel(strDim, srcDim)
            return if (res != -1) res else calucalteFromOther(srcDim, srcOther, destOther)
            // (int)(Caster.toDoubleValue(srcDim)*Caster.toDoubleValue(destOther)/Caster.toDoubleValue(srcOther));
        }

        private fun calucalteFromOther(srcDim: Int, srcOther: Int, destOther: Int): Int {
            return (Caster.toDoubleValue(srcDim) * Caster.toDoubleValue(destOther) / Caster.toDoubleValue(srcOther))
        }

        @Throws(ApplicationException::class)
        private fun checkResource(resource: Resource?) {
            if (resource is FileResource) return
            if (resource is HTTPResource) throw ApplicationException("attribute width and height are required when external sources are invoked")
            throw ApplicationException("the resource type [" + resource.getResourceProvider().getScheme().toString() + "] is not supported")
        }
    }
}