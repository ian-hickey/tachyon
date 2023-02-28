/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.transformer.library.function

import java.io.IOException

/**
 * Eine FunctionLib repraesentiert eine FLD, sie stellt Methoden zur Verfuegung um auf alle
 * Informationen die eine FLD bietet zuzugreifen.
 */
class FunctionLib
/**
 * Geschuetzer Konstruktor ohne Argumente.
 */
    : Lib {
    private var functions: HashMap<String?, FunctionLibFunction?>? = HashMap<String?, FunctionLibFunction?>()
    private var version: String? = ""
    private var shortName: String? = ""
    private var uri: URI? = null
    private var displayName: String? = ""
    private var description: String? = ""
    private var source: String? = null

    /**
     * Gibt eine einzelne Funktion der FLD zurueck mit dem passenden Namen. Gibt null zurueck falls die
     * Funktion nicht existiert.
     *
     * @param name Name der Funktion.
     * @return FunctionLibFunction
     */
    fun getFunction(name: String?): FunctionLibFunction? {
        return functions.get(name.toLowerCase())
    }

    /**
     * Gibt die Beschreibung der FLD zurueck.
     *
     * @return Beschreibung der FLD.
     */
    fun getDescription(): String? {
        return description
    }

    /**
     * Gibt den Namen zur Ausgabe (Praesentation) der FLD zurueck.
     *
     * @return Ausgabename.
     */
    fun getDisplayName(): String? {
        return displayName
    }

    /**
     * Gibt den Kurzname der FLD zurueck.
     *
     * @return Kurzname.
     */
    fun getShortName(): String? {
        return shortName
    }

    /**
     * Gibt die eindeutige URI der FLD zurueck.
     *
     * @return URI.
     */
    fun getUri(): URI? {
        return uri
    }

    /**
     * Gibt die Version der FLD zurueck.
     *
     * @return String
     */
    fun getVersion(): String? {
        return version
    }

    /**
     * Fuegt der FunctionLib eine Funktion (FunctionLibFunction) zu.
     *
     * @param function
     */
    fun setFunction(function: FunctionLibFunction?) {
        function!!.setFunctionLib(this)
        functions.put(function!!.getName(), function)
    }

    /**
     * Setzt die Beschreibung der FLD.
     *
     * @param description Beschreibung der FLD.
     */
    fun setDescription(description: String?) {
        this.description = description
    }

    /**
     * Setzt den Ausgabename der FLD.
     *
     * @param displayName Ausgabename
     */
    fun setDisplayName(displayName: String?) {
        this.displayName = displayName
    }

    /**
     * Setzt den Kurznamen der FLD.
     *
     * @param shortName Kurznamen der FLD.
     */
    fun setShortName(shortName: String?) {
        this.shortName = shortName
    }

    /**
     * Setzt den eindeutigen URI der FLD.
     *
     * @param uriString URI.
     * @throws URISyntaxException
     */
    @Throws(URISyntaxException::class)
    protected fun setUri(uriString: String?) {
        setUri(URI(uriString))
    }

    protected fun setUri(uri: URI?) {
        this.uri = uri
    }

    /**
     * Setzt die Version der FLD.
     *
     * @param version FLD der Version.
     */
    fun setVersion(version: String?) {
        this.version = version
    }

    /**
     * @return Returns the functions.
     */
    fun getFunctions(): Map<String?, FunctionLibFunction?>? {
        return functions
    }

    /**
     * @see java.lang.Object.toString
     */
    @Override
    override fun toString(): String {
        return getDisplayName().toString() + ":" + getShortName() + ":" + super.toString()
    }

    fun getHash(): String? {
        val sb = StringBuilder()
        val it: Iterator<FunctionLibFunction?> = functions.values().iterator()
        while (it.hasNext()) {
            sb.append(it.next()!!.getHash().toString() + "\n")
        }
        return try {
            Md5.getDigestAsString(sb.toString())
        } catch (e: IOException) {
            ""
        }
    }

    /**
     * duplicate this FunctionLib
     *
     * @param deepCopy
     * @return
     */
    fun duplicate(deepCopy: Boolean): FunctionLib? {
        val fl = FunctionLib()
        fl.description = description
        fl.displayName = displayName
        fl.functions = duplicate(functions, deepCopy)
        fl.shortName = shortName
        fl.uri = uri
        fl.version = version
        return fl
    }

    /**
     * @param source the source to set
     */
    fun setSource(source: String?) {
        this.source = source
    }

    /**
     * @return the source
     */
    fun getSource(): String? {
        return source
    }

    /**
     * duplcate a hashmap with FunctionLibFunction's
     *
     * @param funcs
     * @param deepCopy
     * @return cloned map
     */
    private fun duplicate(funcs: HashMap?, deepCopy: Boolean): HashMap? {
        if (deepCopy) throw PageRuntimeException(ExpressionException("deep copy not supported"))
        val it: Iterator = funcs.entrySet().iterator()
        var entry: Map.Entry
        val cm = HashMap()
        while (it.hasNext()) {
            entry = it.next() as Entry
            cm.put(entry.getKey(), if (deepCopy) entry.getValue() else  // TODO add support for deepcopy ((FunctionLibFunction)entry.getValue()).duplicate(deepCopy):
                entry.getValue())
        }
        return cm
    }
}