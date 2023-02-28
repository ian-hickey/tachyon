package tachyon.runtime.regex

import org.apache.oro.text.regex.MalformedPatternException

interface Regex {
    @Throws(PageException::class)
    fun matches(strPattern: String?, strInput: String?): Boolean
    fun matches(strPattern: String?, strInput: String?, defaultValue: Boolean): Boolean

    @Throws(PageException::class)
    fun match(strPattern: String?, strInput: String?, offset: Int, caseSensitive: Boolean, multiLine: Boolean): String?

    @Throws(PageException::class)
    fun matchAll(strPattern: String?, strInput: String?, offset: Int, caseSensitive: Boolean, multiLine: Boolean): Array?

    /**
     * return index of the first occurrence of the pattern in input text
     *
     * @param strPattern pattern to search
     * @param strInput text to search pattern
     * @param offset
     * @param caseSensitive
     * @return position of the first occurrence
     */
    @Throws(PageException::class)
    fun indexOf(strPattern: String?, strInput: String?, offset: Int, caseSensitive: Boolean, multiLine: Boolean): Int

    /**
     * return index of all occurrences of the pattern in input text as an array of int
     *
     * @param strPattern pattern to search
     * @param strInput text to search pattern
     * @param offset
     * @param caseSensitive
     * @return position of the first occurrence
     */
    @Throws(PageException::class)
    fun indexOfAll(strPattern: String?, strInput: String?, offset: Int, caseSensitive: Boolean, multiLine: Boolean): Object?

    /**
     * find occurrence of a pattern in a string (same like indexOf), but it returns a struct with more
     * details
     *
     * @param strPattern
     * @param strInput
     * @param offset
     * @param caseSensitive
     * @return
     * @throws MalformedPatternException
     */
    @Throws(PageException::class)
    fun find(strPattern: String?, strInput: String?, offset: Int, caseSensitive: Boolean, multiLine: Boolean): Struct?

    /**
     * find occurrence of a pattern in a string (same like indexOfAll), but it returns a struct with
     * more details
     *
     * @param strPattern
     * @param strInput
     * @param offset
     * @param caseSensitive
     * @return
     * @throws MalformedPatternException
     */
    @Throws(PageException::class)
    fun findAll(strPattern: String?, strInput: String?, offset: Int, caseSensitive: Boolean, multiLine: Boolean): Array?

    @Throws(PageException::class)
    fun replace(strInput: String?, strPattern: String?, replacement: String?, caseSensitive: Boolean, multiLine: Boolean): String?

    @Throws(PageException::class)
    fun replaceAll(strInput: String?, strPattern: String?, replacement: String?, caseSensitive: Boolean, multiLine: Boolean): String?
    fun getTypeName(): String?
}