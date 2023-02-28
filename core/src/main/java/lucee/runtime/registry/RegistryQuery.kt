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
package lucee.runtime.registry

import java.io.IOException

/**
 *
 */
object RegistryQuery {
    private const val DQ = '"'
    private val lenDWORD: Int = RegistryEntry.REGDWORD_TOKEN!!.length()
    private val lenSTRING: Int = RegistryEntry.REGSTR_TOKEN!!.length()
    private val NO_NAME: String? = "<NO NAME>"

    /**
     * execute a String query on command line
     *
     * @param query String to execute
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    @Throws(IOException::class, InterruptedException::class)
    fun executeQuery(cmd: Array<String?>?): String? {
        return Command.execute(cmd).getOutput()
    }

    /**
     * gets a single value form the registry
     *
     * @param branch brach to get value from
     * @param entry entry to get
     * @param type type of the registry entry to get
     * @return registry entry or null of not exist
     * @throws RegistryException
     * @throws IOException
     * @throws InterruptedException
     */
    @Throws(RegistryException::class, IOException::class, InterruptedException::class)
    fun getValue(branch: String?, entry: String?, type: Short): RegistryEntry? {
        val cmd = arrayOf("reg", "query", cleanBrunch(branch), "/v", entry)
        val rst: Array<RegistryEntry?>? = filter(executeQuery(cmd), branch, type)
        return if (rst!!.size == 1) {
            rst[0]
            // if(type==RegistryEntry.TYPE_ANY || type==r.getType()) return r;
        } else null
    }

    /**
     * gets all entries of one branch
     *
     * @param branch
     * @param type
     * @return
     * @throws RegistryException
     * @throws IOException
     * @throws InterruptedException
     */
    @Throws(RegistryException::class, IOException::class, InterruptedException::class)
    fun getValues(branch: String?, type: Short): Array<RegistryEntry?>? {
        val cmd = arrayOf("reg", "query", branch)
        return filter(executeQuery(cmd), cleanBrunch(branch), type)
    }

    /**
     * writes a value to registry
     *
     * @param branch
     * @param entry
     * @param type
     * @param value
     * @throws RegistryException
     * @throws IOException
     * @throws InterruptedException
     */
    @Throws(RegistryException::class, IOException::class, InterruptedException::class)
    fun setValue(branch: String?, entry: String?, type: Short, value: String?) {
        var value = value
        if (type == RegistryEntry.TYPE_KEY) {
            val fullKey: String = ListUtil.trim(branch, "\\").toString() + "\\" + ListUtil.trim(entry, "\\")
            // String[] cmd = new String[]{"reg","add",cleanBrunch(fullKey),"/ve","/f"};
            val cmd = arrayOf("reg", "add", cleanBrunch(fullKey), "/f")
            executeQuery(cmd)
        } else {
            if (type == RegistryEntry.TYPE_DWORD) value = Caster.toString(Caster.toIntValue(value, 0))
            val cmd = arrayOf("reg", "add", cleanBrunch(branch), "/v", entry, "/t", RegistryEntry.toStringType(type), "/d", value, "/f")
            executeQuery(cmd)
        }
    }

    /**
     * deletes a value or a key
     *
     * @param branch
     * @param entry
     * @throws IOException
     * @throws InterruptedException
     */
    @Throws(IOException::class, InterruptedException::class)
    fun deleteValue(branch: String?, entry: String?) {
        if (entry == null) {
            val cmd = arrayOf("reg", "delete", cleanBrunch(branch), "/f")
            executeQuery(cmd)
            // executeQuery("reg delete \""+List.trim(branch,"\\")+"\" /f");
        } else {
            val cmd = arrayOf("reg", "delete", cleanBrunch(branch), "/v", entry, "/f")
            executeQuery(cmd)
            // executeQuery("reg delete \""+List.trim(branch,"\\")+"\" /v "+entry+" /f");
        }
    }

    private fun cleanBrunch(branch: String?): String? {
        var branch = branch
        branch = branch.replace('/', '\\')
        branch = ListUtil.trim(branch, "\\")
        return if (branch.length() === 0) "\\" else branch
    }

    /**
     * filter registry entries from the raw result
     *
     * @param string plain result to filter regisry entries
     * @param branch
     * @param type
     * @return filtered entries
     * @throws RegistryException
     */
    @Throws(RegistryException::class)
    private fun filter(string: String?, branch: String?, type: Short): Array<RegistryEntry?>? {
        var branch = branch
        branch = ListUtil.trim(branch, "\\")
        val result = StringBuffer()
        val array = ArrayList()
        val arr: Array<String?> = string.split("\n")
        for (i in arr.indices) {
            var line: String = arr[i].trim()
            val indexDWORD: Int = line.indexOf(RegistryEntry.REGDWORD_TOKEN)
            val indexSTRING: Int = line.indexOf(RegistryEntry.REGSTR_TOKEN)
            if (indexDWORD != -1 || indexSTRING != -1) {
                val index = if (indexDWORD == -1) indexSTRING else indexDWORD
                val len = if (indexDWORD == -1) lenSTRING else lenDWORD
                val _type: Short = if (indexDWORD == -1) RegistryEntry.TYPE_STRING else RegistryEntry.TYPE_DWORD
                if (result.length() > 0) result.append("\n")
                var _key: String = line.substring(0, index).trim()
                var _value: String? = StringUtil.substringEL(line, index + len + 1, "").trim()
                if (_key.equals(NO_NAME)) _key = ""
                if (_type == RegistryEntry.TYPE_DWORD) _value = String.valueOf(ParseNumber.invoke(_value.substring(2), "hex", 0))
                val re = RegistryEntry(_type, _key, _value)
                if (type == RegistryEntry.TYPE_ANY || type == re!!.getType()) array.add(re)
                // }
            } else if (line.indexOf(branch) === 0 && (type == RegistryEntry.TYPE_ANY || type == RegistryEntry.TYPE_KEY)) {
                line = ListUtil.trim(line, "\\")
                if (branch.length() < line.length()) {
                    array.add(RegistryEntry(RegistryEntry.TYPE_KEY, ListUtil.last(line, "\\", true), ""))
                }
            }
        }
        return array.toArray(arrayOfNulls<RegistryEntry?>(array.size()))
    }

    internal class StreamReader(`is`: InputStream?) : ParentThreasRefThread() {
        private val `is`: InputStream?
        private val sw: StringWriter?
        @Override
        fun run() {
            try {
                var c: Int
                while (`is`.read().also { c = it } != -1) sw.write(c)
            } catch (e: IOException) { // TODO log parent stacktrace as well
            }
        }

        fun getResult(): String? {
            return sw.toString()
        }

        init {
            this.`is` = `is`
            sw = StringWriter()
        }
    }
}