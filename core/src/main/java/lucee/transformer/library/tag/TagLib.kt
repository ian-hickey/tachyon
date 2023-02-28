/**
 * Copyright (c) 2014, the Railo Company Ltd.
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
package lucee.transformer.library.tag

import java.io.IOException

/**
 * Die Klasse TagLib repaesentiert eine Komplette TLD, mit ihrer Hilfe kann man alle Informationen,
 * zu einer TLD Abfragen.
 */
class TagLib
/**
 * Geschuetzer Konstruktor ohne Argumente.
 */(private var isCore: Boolean = false) : Cloneable, Lib, Cloneable {
    private var shortName: String? = ""
    private var displayName: String? = null
    private var type: String? = "cfml"
    private var nameSpace: String? = null
    private var nameSpaceSeperator: String? = ":"
    private var ELClass: ClassDefinition<out ExprTransformer?>? = EXPR_TRANSFORMER
    private var tags: HashMap<String?, TagLibTag?>? = HashMap<String?, TagLibTag?>()
    private var appendixTags: HashMap<String?, TagLibTag?>? = HashMap<String?, TagLibTag?>()
    private var exprTransformer: ExprTransformer? = null
    private var nameSpaceAndNameSpaceSeperator: CharArray?
    private var source: String? = null
    private var uri: URI? = null
    private var description: String? = null
    private var scriptTags: Array<TagLibTag?>?
    private var ignoreUnknowTags = false

    /**
     * @param source the source to set
     */
    fun setSource(source: String?) {
        this.source = source
    }

    /**
     * Gibt den Name-Space einer TLD als String zurueck.
     *
     * @return String Name der TLD.
     */
    fun getNameSpace(): String? {
        return nameSpace
    }

    /**
     * Gibt den Trenner zwischen Name-Space und Name einer TLD zurueck.
     *
     * @return Name zwischen Name-Space und Name.
     */
    fun getNameSpaceSeparator(): String? {
        return nameSpaceSeperator
    }

    /**
     * Gibt den Name-Space inkl. dem Seperator zurueck.
     *
     * @return String
     */
    fun getNameSpaceAndSeparator(): String? {
        return nameSpace + nameSpaceSeperator
    }

    /**
     * Gibt den Name-Space inkl. dem Seperator zurueck.
     *
     * @return String
     */
    fun getNameSpaceAndSeperatorAsCharArray(): CharArray? {
        if (nameSpaceAndNameSpaceSeperator == null) {
            nameSpaceAndNameSpaceSeperator = getNameSpaceAndSeparator().toCharArray()
        }
        return nameSpaceAndNameSpaceSeperator
    }

    /**
     * Gibt einen Tag (TagLibTag)zurueck, dessen Name mit dem uebergebenen Wert uebereinstimmt, falls
     * keine uebereinstimmung gefunden wird, wird null zurueck gegeben.
     *
     * @param name Name des Tag das zurueck gegeben werden soll.
     * @return TagLibTag Tag das auf den Namen passt.
     */
    fun getTag(name: String?): TagLibTag? {
        return tags.get(name.toLowerCase())
    }

    fun getTag(clazz: Class?): TagLibTag? {
        val _tags: Iterator<TagLibTag?> = tags.values().iterator()
        var tlt: TagLibTag?
        while (_tags.hasNext()) {
            tlt = _tags.next()
            if (tlt!!.getTagClassDefinition().isClassNameEqualTo(clazz.getName(), true)) {
                return tlt
            }
        }
        return null
    }

    /**
     * Gibt einen Tag (TagLibTag)zurueck, welches definiert hat, dass es einen Appendix besitzt. D.h.
     * dass der Name des Tag mit weiteren Buchstaben erweitert sein kann, also muss nur der erste Teil
     * des Namen vom Tag mit dem uebergebenen Namen uebereinstimmen. Wenn keine uebereinstimmung
     * gefunden wird, wird null zurueck gegeben.
     *
     * @param name Name des Tag inkl. Appendix das zurueck gegeben werden soll.
     * @return TagLibTag Tag das auf den Namen passt.
     */
    fun getAppendixTag(name: String?): TagLibTag? {
        val it: Iterator<String?> = appendixTags.keySet().iterator()
        var match: String? = ""
        while (it.hasNext()) {
            val tagName: String = StringUtil.toStringNative(it.next(), "")
            if (match!!.length() < tagName.length() && name.indexOf(tagName) === 0) {
                match = tagName
            }
        }
        return getTag(match)
    }

    /**
     * Gibt alle Tags (TagLibTag) als HashMap zurueck.
     *
     * @return Alle Tags als HashMap.
     */
    fun getTags(): Map<String?, TagLibTag?>? {
        return tags
    }

    /**
     * Gibt die Klasse des ExprTransformer als Zeichenkette zurueck.
     *
     * @return String
     */
    fun getELClassDefinition(): ClassDefinition<out ExprTransformer?>? {
        return ELClass
    }

    /**
     * Laedt den innerhalb der TagLib definierten ExprTransfomer und gibt diesen zurueck. Load
     * Expression Transfomer defined in the tag library and return it.
     *
     * @return ExprTransformer
     * @throws TagLibException
     */
    @Throws(TagLibException::class)
    fun getExprTransfomer(): ExprTransformer? {
        // Class cls;
        if (exprTransformer != null) return exprTransformer
        exprTransformer = try {
            ClassUtil.loadInstance(ELClass.getClazz()) as ExprTransformer
            // exprTransformer = (ExprTransformer) cls.newInstance();
        } catch (e: Exception) {
            throw TagLibException(e)
        }
        return exprTransformer
    }

    /**
     * Fuegt der TagLib einen weiteren Tag hinzu. Diese Methode wird durch die Klasse TagLibFactory
     * verwendet.
     *
     * @param tag Neuer Tag.
     */
    fun setTag(tag: TagLibTag?) {
        tag!!.setTagLib(this)
        tags.put(tag!!.getName(), tag)
        if (tag!!.hasAppendix()) appendixTags.put(tag!!.getName(), tag) else if (appendixTags.containsKey(tag!!.getName())) appendixTags.remove(tag!!.getName())
    }

    /**
     * Fuegt der TagLib die Evaluator Klassendefinition als Zeichenkette hinzu. Diese Methode wird durch
     * die Klasse TagLibFactory verwendet.
     *
     * @param eLClass Zeichenkette der Evaluator Klassendefinition.
     */
    fun setELClass(eLClass: String?, id: Identification?, attributes: Map<String?, String?>?) {
        ELClass = ClassDefinitionImpl.toClassDefinition(eLClass, id, attributes)
    }

    fun setELClassDefinition(cd: ClassDefinition?) {
        ELClass = cd
    }

    /**
     * Fuegt der TagLib die die Definition des Name-Space hinzu. Diese Methode wird durch die Klasse
     * TagLibFactory verwendet.
     *
     * @param nameSpace Name-Space der TagLib.
     */
    fun setNameSpace(nameSpace: String?) {
        this.nameSpace = nameSpace.toLowerCase()
    }

    /**
     * Fuegt der TagLib die die Definition des Name-Space-Seperator hinzu. Diese Methode wird durch die
     * Klasse TagLibFactory verwendet.
     *
     * @param nameSpaceSeperator Name-Space-Seperator der TagLib.
     */
    fun setNameSpaceSeperator(nameSpaceSeperator: String?) {
        this.nameSpaceSeperator = nameSpaceSeperator
    }

    /**
     * @return Returns the displayName.
     */
    fun getDisplayName(): String? {
        return if (displayName == null) shortName else displayName
    }

    /**
     * @param displayName The displayName to set.
     */
    fun setDisplayName(displayName: String?) {
        this.displayName = displayName
    }

    /**
     * @return Returns the shortName.
     */
    fun getShortName(): String? {
        return shortName
    }

    /**
     * @param shortName The shortName to set.
     */
    fun setShortName(shortName: String?) {
        this.shortName = shortName
        if (nameSpace == null) nameSpace = shortName.toLowerCase()
    }

    fun setIgnoreUnknowTags(ignoreUnknowTags: Boolean) {
        this.ignoreUnknowTags = ignoreUnknowTags
    }

    fun getIgnoreUnknowTags(): Boolean {
        return ignoreUnknowTags
    }

    /**
     * @return Returns the type.
     */
    fun getType(): String? {
        return type
    }

    /**
     * @param type The type to set.
     */
    fun setType(type: String?) {
        this.type = type
    }

    /**
     * @see java.lang.Object.toString
     */
    @Override
    override fun toString(): String {
        return getDisplayName().toString() + ":" + getShortName() + ":" + super.toString()
    }

    fun getHash(): String? {
        val sb = StringBuffer()
        val it: Iterator<String?> = tags.keySet().iterator()
        while (it.hasNext()) {
            // "__filename"
            sb.append(tags.get(it.next()).getHash().toString() + "\n")
        }
        return try {
            Md5.getDigestAsString(sb.toString())
        } catch (e: IOException) {
            ""
        }
    }

    fun isCore(): Boolean {
        return isCore
    }

    fun setIsCore(isCore: Boolean) {
        this.isCore = isCore
    }

    /**
     * @see java.lang.Object.clone
     */
    @Override
    fun clone(): Object {
        return duplicate(false)
    }

    /**
     * duplicate the taglib, does not
     *
     * @param deepCopy duplicate also the children (TagLibTag) of this TagLib
     * @return clone of this taglib
     */
    fun duplicate(deepCopy: Boolean): TagLib? {
        val tl = TagLib(isCore)
        tl.appendixTags = duplicate(appendixTags, deepCopy)
        tl.displayName = displayName
        tl.ELClass = ELClass
        tl.exprTransformer = exprTransformer
        tl.isCore = isCore
        tl.nameSpace = nameSpace
        tl.nameSpaceAndNameSpaceSeperator = nameSpaceAndNameSpaceSeperator
        tl.nameSpaceSeperator = nameSpaceSeperator
        tl.shortName = shortName
        tl.tags = duplicate(tags, deepCopy)
        tl.type = type
        tl.source = source
        tl.ignoreUnknowTags = ignoreUnknowTags
        return tl
    }

    /**
     * duplcate a hashmap with TagLibTag's
     *
     * @param tags
     * @param deepCopy
     * @return cloned map
     */
    private fun duplicate(tags: HashMap<String?, TagLibTag?>?, deepCopy: Boolean): HashMap<String?, TagLibTag?>? {
        if (deepCopy) throw PageRuntimeException(ExpressionException("deep copy not supported"))
        val it: Iterator<Entry<String?, TagLibTag?>?> = tags.entrySet().iterator()
        val cm: HashMap<String?, TagLibTag?> = HashMap<String?, TagLibTag?>()
        var entry: Entry<String?, TagLibTag?>?
        while (it.hasNext()) {
            entry = it.next()
            cm.put(entry.getKey(), if (deepCopy) entry.getValue() else  // TODO add support for deepcopy ((TagLibTag)entry.getValue()).duplicate(deepCopy):
                entry.getValue())
        }
        return cm
    }

    fun getSource(): String? {
        return source
    }

    fun getUri(): URI? {
        // TODO Auto-generated method stub
        return uri
    }

    @Throws(URISyntaxException::class)
    fun setUri(strUri: String?) {
        uri = URI(strUri)
    }

    fun setUri(uri: URI?) {
        this.uri = uri
    }

    fun setDescription(description: String?) {
        this.description = description
    }

    fun getDescription(): String? {
        return description
    }

    fun getScriptTags(): Array<TagLibTag?>? {
        if (scriptTags == null) {
            val it: Iterator<TagLibTag?> = getTags()!!.values().iterator()
            var tag: TagLibTag?
            var script: TagLibTagScript
            val tags: List<TagLibTag?> = ArrayList<TagLibTag?>()
            while (it.hasNext()) {
                tag = it.next()
                script = tag!!.getScript()
                if (script != null && script.getType() !== TagLibTagScript.TYPE_NONE) {
                    tags.add(tag)
                    // print.o(tag.getName()+":"+tag.getScript().getType());
                }
            }
            scriptTags = tags.toArray(arrayOfNulls<TagLibTag?>(tags.size()))
        }
        return scriptTags
    }

    companion object {
        const val STATUS_IMPLEMENTED: Short = 0
        const val STATUS_DEPRECATED: Short = 1
        const val STATUS_UNIMPLEMENTED: Short = 2
        const val STATUS_HIDDEN: Short = 4

        /**
         * Field `EXPR_TRANSFORMER`
         */
        var EXPR_TRANSFORMER: ClassDefinition<out ExprTransformer?>? = ClassDefinitionImpl(CFMLExprTransformer::class.java)
    }
}