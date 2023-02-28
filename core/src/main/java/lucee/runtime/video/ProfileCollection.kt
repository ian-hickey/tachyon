/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
package lucee.runtime.video

import java.io.IOException

class ProfileCollection(config: Config?) {
    private var profiles: Map<String?, VideoProfile?>? = null
    @Throws(ApplicationException::class)
    private fun init(config: Config?, initProfiles: Boolean) {
        // get the video directory
        val dir: Resource = config.getVideoDirectory()

        // get the video.xml
        val xml: Resource = dir.getRealResource("video.xml")

        // create (if not exist) and return video xml as dom
        val video: Element
        video = try {
            getVideoXML(xml)
        } catch (e: Exception) {
            throw ApplicationException("can not load video xml file [$xml]", Caster.toClassName(e).toString() + ":" + e.getMessage())
        }

        // translate form DOM to a List of VideoProfile
        if (initProfiles) {
            profiles = try {
                translateVideoXML(video)
            } catch (e: PageException) {
                throw ApplicationException("can not load profiles from video xml file [$xml] a type is invalid", e.getMessage())
            }
        }
    }

    /**
     * @return the qualities
     */
    fun getProfiles(): Map<String?, VideoProfile?>? {
        return profiles
    }

    companion object {
        private val util: VideoUtil? = VideoUtilImpl.getInstance()

        /**
         * translate form DOM to a List of VideoProfile
         *
         * @param video
         * @return
         * @throws PageException
         */
        @Throws(PageException::class)
        private fun translateVideoXML(video: Element?): Map<String?, VideoProfile?>? {
            val profiles: Map<String?, VideoProfile?> = LinkedHashMap<String?, VideoProfile?>()
            // quality
            val qd: Element? = getChildByName(video, "profiles", false)
            val items: Array<Element?>? = getChildren(qd, "profile")
            var item: Element?
            var vq: VideoProfile?
            var value: String
            for (i in items.indices) {
                item = items!![i]
                vq = VideoProfileImpl()
                // aspect-ratio
                value = item.getAttribute("aspect-ratio")
                if (!Util.isEmpty(value)) vq.setAspectRatio(value)

                // aspect-ratio
                value = item.getAttribute("audio-bitrate")
                if (!Util.isEmpty(value)) vq.setAudioBitrate(util.toBytes(value))

                // audio-samplerate
                value = item.getAttribute("audio-samplerate")
                if (!Util.isEmpty(value)) vq.setAudioSamplerate(util.toHerz(value))

                // dimension
                val w: String = item.getAttribute("width")
                val h: String = item.getAttribute("height")
                if (!Util.isEmpty(w) && !Util.isEmpty(h)) {
                    vq.setDimension(Caster.toIntValue(w), Caster.toIntValue(h))
                }

                // framerate
                value = item.getAttribute("framerate")
                val value2: String = item.getAttribute("fps")
                if (!Util.isEmpty(value)) vq.setFramerate(Caster.toDoubleValue(value)) else if (!Util.isEmpty(value2)) vq.setFramerate(Caster.toDoubleValue(value2))

                // video-bitrate
                value = item.getAttribute("video-bitrate")
                if (!Util.isEmpty(value)) vq.setVideoBitrate(util.toBytes(value))

                // video-bitrate-max
                value = item.getAttribute("video-bitrate-max")
                if (!Util.isEmpty(value)) vq.setVideoBitrateMax(util.toBytes(value))

                // video-bitrate-min
                value = item.getAttribute("video-bitrate-min")
                if (!Util.isEmpty(value)) vq.setVideoBitrateMin(util.toBytes(value))

                // video-bitrate-tolerance
                value = item.getAttribute("video-bitrate-tolerance")
                if (!Util.isEmpty(value)) vq.setVideoBitrateTolerance(util.toBytes(value))

                // video-codec
                value = item.getAttribute("video-codec")
                // print.out("video-codec:"+value);
                if (!Util.isEmpty(value)) vq.setVideoCodec(value)

                // audio-codec
                value = item.getAttribute("audio-codec")
                if (!Util.isEmpty(value)) vq.setAudioCodec(value)

                //
                value = item.getAttribute("label")
                // print.out("label:"+value);
                if (!Util.isEmpty(value)) {
                    val arr = toArray(value)
                    for (y in arr.indices) {
                        profiles.put(arr!![y].trim().toLowerCase(), vq)
                    }
                }
            }
            return profiles
        }

        private fun getChildByName(parent: Node?, nodeName: String?, insertBefore: Boolean): Element? {
            if (parent == null) return null
            val list: NodeList = parent.getChildNodes()
            val len: Int = list.getLength()
            for (i in 0 until len) {
                val node: Node = list.item(i)
                if (node.getNodeType() === Node.ELEMENT_NODE && node.getNodeName().equalsIgnoreCase(nodeName)) {
                    return node as Element
                }
            }
            val newEl: Element = parent.getOwnerDocument().createElement(nodeName)
            if (insertBefore) parent.insertBefore(newEl, parent.getFirstChild()) else parent.appendChild(newEl)
            return newEl
        }

        private fun getChildren(parent: Node?, nodeName: String?): Array<Element?>? {
            if (parent == null) return arrayOfNulls<Element?>(0)
            val list: NodeList = parent.getChildNodes()
            val len: Int = list.getLength()
            val rtn = ArrayList()
            for (i in 0 until len) {
                val node: Node = list.item(i)
                if (node.getNodeType() === Node.ELEMENT_NODE && node.getNodeName().equalsIgnoreCase(nodeName)) {
                    rtn.add(node)
                }
            }
            return rtn.toArray(arrayOfNulls<Element?>(rtn.size()))
        }

        private fun toArray(str: String?): Array<String?>? {
            var str = str
            val st = StringTokenizer(str, ",")
            val list: ArrayList<String?> = ArrayList<String?>()
            while (st.hasMoreTokens()) {
                list.add(st.nextToken().also { str = it })
            }
            return list.toArray(arrayOfNulls<String?>(list.size()))
        }

        /**
         * create (if not exist) and return video xml as dom
         *
         * @param xml
         * @return
         * @throws IOException
         * @throws SAXException
         */
        @Throws(IOException::class, SAXException::class)
        private fun getVideoXML(xml: Resource?): Element? {
            if (!xml.exists()) {
                createFileFromResource("/resource/video/video.xml", xml)
            }
            val doc: Document = loadDocument(xml)
            return doc.getDocumentElement()
        }

        @Throws(IOException::class)
        fun createFileFromResource(path: String?, bin: Resource?) {
            var `is`: InputStream? = null
            var os: OutputStream? = null
            if (bin.exists()) return
            IOUtil.copy(VideoInputImpl(null).getClass().getResourceAsStream(path).also { `is` = it }, bin.getOutputStream().also { os = it }, true, true)
        }

        @Throws(SAXException::class, IOException::class)
        private fun loadDocument(xmlFile: Resource?): Document? {
            var `is`: InputStream? = null
            return try {
                loadDocument(xmlFile.getInputStream().also { `is` = it })
            } finally {
                IOUtil.close(`is`)
            }
        }

        @Throws(SAXException::class, IOException::class)
        private fun loadDocument(`is`: InputStream?): Document? {
            return try {
                val source = InputSource(`is`)
                XMLUtil.parse(source, null, false)
            } finally {
                IOUtil.close(`is`)
            }
        }
    }

    init {
        init(config, true)
    }
}