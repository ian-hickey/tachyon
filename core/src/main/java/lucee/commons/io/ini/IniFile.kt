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
package lucee.commons.io.ini

import java.io.BufferedReader

/**
 * read an ini file and allow to modifie and read the data
 */
class IniFile {
    private var sections: Map
    private val file: Resource?

    /**
     * Constructor for the IniFile object
     *
     * @param file ini FIle
     * @throws IOException
     */
    constructor(file: Resource) {
        this.file = file
        sections = newMap()
        var `is`: InputStream? = null
        if (!file.exists()) file.createFile(false)
        try {
            load(file.getInputStream().also { `is` = it })
        } finally {
            IOUtil.close(`is`)
        }
    }

    constructor(`is`: InputStream?) {
        sections = newMap()
        load(`is`)
        file = null
    }

    /**
     * Sets the KeyValue attribute of the IniFile object
     *
     * @param strSection the section to set
     * @param key the key of the new value
     * @param value the value to set
     */
    fun setKeyValue(strSection: String, key: String, value: String?) {
        var section: Map? = getSectionEL(strSection)
        if (section == null) {
            section = newMap()
            sections.put(strSection.toLowerCase(), section)
        }
        section.put(key.toLowerCase(), value)
    }

    /**
     * Gets the Sections attribute of the IniFile object
     *
     * @return The Sections value
     */
    fun getSections(): Map {
        return sections
    }

    /**
     * Gets the Section attribute of the IniFile object
     *
     * @param strSection section name to get
     * @return The Section value
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getSection(strSection: String): Map {
        val o: Object = sections.get(strSection.toLowerCase())
                ?: throw IOException("Section with name [$strSection] does not exist")
        return o
    }

    /**
     * Gets the Section attribute of the IniFile object, return null if section not exist
     *
     * @param strSection section name to get
     * @return The Section value
     */
    fun getSectionEL(strSection: String): Map? {
        val o: Object = sections.get(strSection.toLowerCase()) ?: return null
        return o
    }

    /**
     * Gets the NullOrEmpty attribute of the IniFile object
     *
     * @param section section to check
     * @param key key to check
     * @return is empty or not
     */
    fun isNullOrEmpty(section: String, key: String): Boolean {
        val value = getKeyValueEL(section, key)
        return value == null || value.length() === 0
    }

    /**
     * Gets the KeyValue attribute of the IniFile object
     *
     * @param strSection section to get
     * @param key key to get
     * @return matching alue
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getKeyValue(strSection: String, key: String): String {
        val o: Object = getSection(strSection).get(key.toLowerCase())
                ?: throw IOException("Key [$key] doesn't exist in section [$strSection]")
        return o
    }

    /**
     * Gets the KeyValue attribute of the IniFile object, if not exist return null
     *
     * @param strSection section to get
     * @param key key to get
     * @return matching alue
     */
    fun getKeyValueEL(strSection: String, key: String): String? {
        val map: Map = getSectionEL(strSection) ?: return null
        val o: Object = map.get(key.toLowerCase()) ?: return null
        return o
    }

    /**
     * loads the ini file
     *
     * @param in inputstream to read
     * @throws IOException
     */
    @Throws(IOException::class)
    fun load(`in`: InputStream?) {
        val input: BufferedReader = IOUtil.toBufferedReader(InputStreamReader(`in`))
        var read: String
        var section: Map? = null
        var sectionName: String
        while (input.readLine().also { read = it } != null) {
            if (read.startsWith(";") || read.startsWith("#")) {
                continue
            } else if (read.startsWith("[")) {
                // new section
                sectionName = read.substring(1, read.indexOf("]")).trim().toLowerCase()
                section = getSectionEL(sectionName)
                if (section == null) {
                    section = newMap()
                    sections.put(sectionName, section)
                }
            } else if (read.indexOf("=") !== -1 && section != null) {
                // new key
                val key: String = read.substring(0, read.indexOf("=")).trim().toLowerCase()
                val value: String = read.substring(read.indexOf("=") + 1).trim()
                section.put(key, value)
            }
        }
    }

    /**
     * save back content to ini file
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun save() {
        if (!file.exists()) file.createFile(true)
        val out: OutputStream = IOUtil.toBufferedOutputStream(file.getOutputStream())
        val it: Iterator = sections.keySet().iterator()
        val output = PrintWriter(out)
        try {
            while (it.hasNext()) {
                val strSection = it.next() as String
                output.println("[$strSection]")
                val section: Map? = getSectionEL(strSection)
                val iit: Iterator = section.keySet().iterator()
                while (iit.hasNext()) {
                    val key = iit.next() as String
                    output.println(key + "=" + section.get(key))
                }
            }
        } finally {
            IOUtil.flushEL(output)
            IOUtil.closeEL(output)
            IOUtil.flushEL(out)
            IOUtil.closeEL(out)
        }
    }

    /**
     * removes a selection
     *
     * @param strSection section to remove
     */
    fun removeSection(strSection: String?) {
        sections.remove(strSection)
    }

    companion object {
        private fun newMap(): Map {
            return LinkedHashMap()
        }

        /**
         *
         * @param file
         * @return return a struct with all section an dkey list as value
         * @throws IOException
         */
        @Throws(IOException::class)
        fun getProfileSections(file: Resource?): Struct {
            val ini: IniFile = IniFile(file)
            val rtn: Struct = StructImpl(Struct.TYPE_SYNC)
            val sections: Map = ini.getSections()
            val it: Iterator = sections.keySet().iterator()
            while (it.hasNext()) {
                val strSection = it.next() as String
                val section: Map? = ini.getSectionEL(strSection)
                val iit: Iterator = section.keySet().iterator()
                val sb = StringBuilder()
                while (iit.hasNext()) {
                    if (sb.length() !== 0) sb.append(',')
                    sb.append(iit.next())
                }
                rtn.setEL(strSection, sb.toString())
            }
            return rtn
        }
    }
}