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
package tachyon.commons.lang.mimetype

import java.nio.charset.Charset

class MimeType  // if(quality<0 || quality>1)
// throw new RuntimeException("quality must be a number between 0 and 1, now ["+quality+"]");
// this.quality=quality;
// this.mxb=mxb;
// this.mxt=mxt;
private constructor(
        /**
         * @return the type
         */
        val type: String?,
        /**
         * @return the subtype
         */
        val subtype: String?, // private double quality;
        // private int mxb;
        // private double mxt;
        val properties: Map<String, String>?) {

    private var q = -1.0
    private var cs: CharSet? = null
    private var initCS = true

    /**
     * @return the type
     */
    val typeNotNull: String
        get() = type ?: "*"

    /**
     * @return the subtype
     */
    val subtypeNotNull: String
        get() = subtype ?: "*"
    val quality: Double
        get() {
            if (q == -1.0) {
                q = if (properties == null) DEFAULT_QUALITY else Caster.toDoubleValue(getProperty("q"), DEFAULT_QUALITY)
            }
            return q
        }
    val charset: Charset
        get() {
            if (initCS) {
                cs = if (properties == null) DEFAULT_CHARSET else {
                    val str = getProperty("charset")
                    if (StringUtil.isEmpty(str)) DEFAULT_CHARSET else CharsetUtil.toCharSet(str)
                }
                initCS = false
            }
            return CharsetUtil.toCharset(cs)
        }

    /*
	 * public int getMxb() { return Caster.toIntValue(properties.get("mxb"),DEFAULT_MXB); }
	 * 
	 * public double getMxt() { return Caster.toDoubleValue(properties.get("mxt"),DEFAULT_MXT); }
	 */
    private fun getProperty(name: String): String? {
        if (properties != null) {
            val value = properties[name]
            if (value != null) return value
            val it: Iterator<Entry<String, String>> = properties.entrySet().iterator()
            var e: Entry<String, String>
            while (it.hasNext()) {
                e = it.next()
                if (name.equalsIgnoreCase(e.getKey())) return e.getValue()
            }
        }
        return null
    }

    fun hasWildCards(): Boolean {
        return type == null || subtype == null
    }

    /**
     * checks if given mimetype is covered by current mimetype
     *
     * @param other
     * @return
     */
    fun match(other: MimeType): Boolean {
        if (this === other) return true
        if (type != null && other.type != null && !type.equals(other.type)) return false
        return if (subtype != null && other.subtype != null && !subtype.equals(other.subtype)) false else true
    }

    fun bestMatch(others: Array<MimeType>): MimeType? {
        var best: MimeType? = null
        for (i in others.indices) {
            if (match(others[i]) && (best == null || best.quality < others[i].quality)) {
                best = others[i]
            }
        }
        return best
    }

    /**
     * checks if other is from the same type, just type and subtype are checked, properties (q,mxb,mxt)
     * are ignored.
     *
     * @param other
     * @return
     */
    fun same(other: MimeType): Boolean {
        return if (this === other) true else typeNotNull.equals(other.typeNotNull) && subtypeNotNull.equals(other.subtypeNotNull)
    }

    @Override
    override fun equals(obj: Object): Boolean {
        if (obj === this) return true
        val other: MimeType
        other = if (obj is MimeType) obj else if (obj is String) getInstance(obj as String) else return false
        return if (!same(other)) false else other.toString().equals(toString())
    }

    @Override
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(type ?: "*")
        sb.append("/")
        sb.append(subtype ?: "*")
        if (properties != null) {
            val keys: Array<String> = properties.keySet().toArray(arrayOfNulls<String>(properties.size()))
            Arrays.sort(keys)
            // Iterator<Entry<String, String>> it = properties.entrySet().iterator();
            // Entry<String, String> e;
            for (i in keys.indices) {
                sb.append("; ")
                sb.append(keys[i])
                sb.append("=")
                sb.append(properties[keys[i]])
            }
        }
        return sb.toString()
    }

    companion object {
        private const val DEFAULT_MXB = 100000
        private const val DEFAULT_MXT = 5.0
        private const val DEFAULT_QUALITY = 1.0
        private val DEFAULT_CHARSET: CharSet? = null
        val ALL = MimeType(null, null, null)
        val APPLICATION_JSON = MimeType("application", "json", null)
        val APPLICATION_XML = MimeType("application", "xml", null)
        val APPLICATION_WDDX = MimeType("application", "wddx", null)
        val APPLICATION_CFML = MimeType("application", "cfml", null)
        val APPLICATION_PLAIN = MimeType("application", "lazy", null)
        val IMAGE_GIF = MimeType("image", "gif", null)
        val IMAGE_JPG = MimeType("image", "jpeg", null)
        val IMAGE_PNG = MimeType("image", "png", null)
        val IMAGE_TIFF = MimeType("image", "tiff", null)
        val IMAGE_BMP = MimeType("image", "bmp", null)
        val IMAGE_WBMP = MimeType("image", "vnd.wap.wbmp", null)
        val IMAGE_FBX = MimeType("image", "fbx", null)
        val IMAGE_PNM = MimeType("image", "x-portable-anymap", null)
        val IMAGE_PGM = MimeType("image", "x-portable-graymap", null)
        val IMAGE_PBM = MimeType("image", "x-portable-bitmap", null)
        val IMAGE_ICO = MimeType("image", "ico", null)
        val IMAGE_PSD = MimeType("image", "psd", null)
        val IMAGE_ASTERIX = MimeType("image", null, null)
        val APPLICATION_JAVA = MimeType("application", "java", null)
        val TEXT_HTML = MimeType("text", "html", null)
        private fun getInstance(type: String?, subtype: String?, properties: Map<String, String>?): MimeType {
            // TODO read this from an external File
            if ("text".equals(type)) {
                if ("xml".equals(subtype)) return MimeType("application", "xml", properties)
                if ("x-json".equals(subtype)) return MimeType("application", "json", properties)
                if ("javascript".equals(subtype)) return MimeType("application", "json", properties)
                if ("x-javascript".equals(subtype)) return MimeType("application", "json", properties)
                if ("wddx".equals(subtype)) return MimeType("application", "wddx", properties)
            } else if ("application".equals(type)) {
                if ("x-json".equals(subtype)) return MimeType("application", "json", properties)
                if ("javascript".equals(subtype)) return MimeType("application", "json", properties)
                if ("x-javascript".equals(subtype)) return MimeType("application", "json", properties)
                if ("jpg".equals(subtype)) return MimeType("image", "jpeg", properties)
                if ("x-jpg".equals(subtype)) return MimeType("image", "jpeg", properties)
                if ("png".equals(subtype)) return MimeType("image", "png", properties)
                if ("x-png".equals(subtype)) return MimeType("image", "png", properties)
                if ("tiff".equals(subtype)) return MimeType("image", "tiff", properties)
                if ("tif".equals(subtype)) return MimeType("image", "tiff", properties)
                if ("x-tiff".equals(subtype)) return MimeType("image", "tiff", properties)
                if ("x-tif".equals(subtype)) return MimeType("image", "tiff", properties)
                if ("fpx".equals(subtype)) return MimeType("image", "fpx", properties)
                if ("x-fpx".equals(subtype)) return MimeType("image", "fpx", properties)
                if ("vnd.fpx".equals(subtype)) return MimeType("image", "fpx", properties)
                if ("vnd.netfpx".equals(subtype)) return MimeType("image", "fpx", properties)
                if ("ico".equals(subtype)) return MimeType("image", "ico", properties)
                if ("x-ico".equals(subtype)) return MimeType("image", "ico", properties)
                if ("x-icon".equals(subtype)) return MimeType("image", "ico", properties)
                if ("psd".equals(subtype)) return MimeType("image", "psd", properties)
                if ("x-photoshop".equals(subtype)) return MimeType("image", "psd", properties)
                if ("photoshop".equals(subtype)) return MimeType("image", "psd", properties)
            } else if ("image".equals(type)) {
                if ("gi_".equals(subtype)) return MimeType("image", "gif", properties)
                if ("pjpeg".equals(subtype)) return MimeType("image", "jpeg", properties)
                if ("jpg".equals(subtype)) return MimeType("image", "jpeg", properties)
                if ("jpe".equals(subtype)) return MimeType("image", "jpeg", properties)
                if ("vnd.swiftview-jpeg".equals(subtype)) return MimeType("image", "jpeg", properties)
                if ("pipeg".equals(subtype)) return MimeType("image", "jpeg", properties)
                if ("jp_".equals(subtype)) return MimeType("image", "jpeg", properties)
                if ("x-png".equals(subtype)) return MimeType("image", "png", properties)
                if ("tif".equals(subtype)) return MimeType("image", "tiff", properties)
                if ("x-tif".equals(subtype)) return MimeType("image", "tiff", properties)
                if ("x-tiff".equals(subtype)) return MimeType("image", "tiff", properties)
                if ("x-fpx".equals(subtype)) return MimeType("image", "fpx", properties)
                if ("vnd.fpx".equals(subtype)) return MimeType("image", "fpx", properties)
                if ("vnd.netfpx".equals(subtype)) return MimeType("image", "fpx", properties)
                if ("x-portable/graymap".equals(subtype)) return MimeType("image", "x-portable-anymap", properties)
                if ("portable graymap".equals(subtype)) return MimeType("image", "x-portable-anymap", properties)
                if ("x-pnm".equals(subtype)) return MimeType("image", "x-portable-anymap", properties)
                if ("pnm".equals(subtype)) return MimeType("image", "x-portable-anymap", properties)
                if ("x-portable/graymap".equals(subtype)) return MimeType("image", "x-portable-anymap", properties)
                if ("portable graymap".equals(subtype)) return MimeType("image", "x-portable-anymap", properties)
                if ("x-pgm".equals(subtype)) return MimeType("image", "x-portable-anymap", properties)
                if ("pgm".equals(subtype)) return MimeType("image", "x-portable-graymap", properties)
                if ("portable bitmap".equals(subtype)) return MimeType("image", "x-portable-bitmap", properties)
                if ("x-portable/bitmap".equals(subtype)) return MimeType("image", "x-portable-bitmap", properties)
                if ("x-pbm".equals(subtype)) return MimeType("image", "x-portable-bitmap", properties)
                if ("pbm".equals(subtype)) return MimeType("image", "x-portable-bitmap", properties)
                if ("x-ico".equals(subtype)) return MimeType("image", "ico", properties)
                if ("x-icon".equals(subtype)) return MimeType("image", "ico", properties)
                if ("x-photoshop".equals(subtype)) return MimeType("image", "psd", properties)
                if ("photoshop".equals(subtype)) return MimeType("image", "psd", properties)
            } else if ("zz-application".equals(type)) {
                if ("zz-winassoc-psd".equals(subtype)) return MimeType("image", "psd", properties)
            }
            /*
		 * 
		 * if("image/x-p".equals(mt)) return "ppm"; if("image/x-ppm".equals(mt)) return "ppm";
		 * if("image/ppm".equals(mt)) return "ppm";
		 * 
		 */return MimeType(type, subtype, properties)
        }

        /**
         * returns a mimetype that match given string
         *
         * @param strMimeType
         * @return
         */
        fun getInstance(strMimeType: String?): MimeType {
            var strMimeType = strMimeType ?: return ALL
            strMimeType = strMimeType.trim()
            if ("*".equals(strMimeType) || strMimeType.length() === 0) return ALL
            val arr: Array<String> = ListUtil.listToStringArray(strMimeType, ';')
            if (arr.size == 0) return ALL
            val arrCT: Array<String> = ListUtil.listToStringArray(arr[0].trim(), '/')

            // subtype
            var type: String? = null
            var subtype: String? = null

            // type
            if (arrCT.size >= 1) {
                type = arrCT[0].trim()
                if ("*".equals(type)) type = null
                if (arrCT.size >= 2) {
                    subtype = arrCT[1].trim()
                    if ("*".equals(subtype)) subtype = null
                }
            }
            if (arr.size == 1) return getInstance(type, subtype, null)
            val properties: Map<String, String> = HashMap<String, String>()
            var entry: String?
            var _arr: Array<String>
            for (i in 1 until arr.size) {
                entry = arr[i].trim()
                _arr = ListUtil.listToStringArray(entry, '=')
                if (_arr.size >= 2) properties.put(_arr[0].trim().toLowerCase(), _arr[1].trim()) else if (_arr.size == 1 && !_arr[0].trim().toLowerCase().equals("*")) properties.put(_arr[0].trim().toLowerCase(), "")
            }
            return getInstance(type, subtype, properties)
        }

        fun getInstances(strMimeTypes: String?, delimiter: Char): Array<MimeType?> {
            if (StringUtil.isEmpty(strMimeTypes, true)) return arrayOfNulls(0)
            val arr: Array<String> = ListUtil.trimItems(ListUtil.listToStringArray(strMimeTypes, delimiter))
            val mtes = arrayOfNulls<MimeType>(arr.size)
            for (i in arr.indices) {
                mtes[i] = getInstance(arr[i])
            }
            return mtes
        }

        fun toMimetype(format: Int, defaultValue: MimeType): MimeType {
            when (format) {
                UDF.RETURN_FORMAT_JSON -> return APPLICATION_JSON
                UDF.RETURN_FORMAT_WDDX -> return APPLICATION_WDDX
                UDF.RETURN_FORMAT_SERIALIZE -> return APPLICATION_CFML
                UDF.RETURN_FORMAT_XML -> return APPLICATION_XML
                UDF.RETURN_FORMAT_PLAIN -> return APPLICATION_PLAIN
                UDF.RETURN_FORMAT_JAVA -> return APPLICATION_JAVA
            }
            return defaultValue
        }

        fun toFormat(mimeTypes: List<MimeType?>?, ignore: Int, defaultValue: Int): Int {
            if (mimeTypes == null || mimeTypes.size() === 0) return defaultValue
            val it = mimeTypes.iterator()
            var res: Int
            while (it.hasNext()) {
                res = toFormat(it.next(), -1)
                if (res != -1 && res != ignore) return res
            }
            return defaultValue
        }

        fun toFormat(mt: MimeType?, defaultValue: Int): Int {
            if (mt == null) return defaultValue
            if (APPLICATION_JSON.same(mt)) return UDF.RETURN_FORMAT_JSON
            if (APPLICATION_WDDX.same(mt)) return UDF.RETURN_FORMAT_WDDX
            if (APPLICATION_CFML.same(mt)) return UDF.RETURN_FORMAT_SERIALIZE
            if (APPLICATION_XML.same(mt)) return UDF.RETURN_FORMAT_XML
            if (APPLICATION_PLAIN.same(mt)) return UDF.RETURN_FORMAT_PLAIN
            return if (APPLICATION_JAVA.same(mt)) UDF.RETURN_FORMAT_JAVA else defaultValue
        }
    }
}