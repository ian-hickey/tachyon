/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.transformer.util

import java.util.ArrayList

/**
 * this class is a Parser String optimized for the transfomer (CFML Parser)
 */
class SourceCode(private val parent: SourceCode?, strText: String?, writeLog: Boolean, dialect: Int) {
    protected var pos = 0
    protected val text: CharArray?
    protected val lcText: CharArray?
    protected val lines // TODO to int[]
            : Array<Integer?>?
    private val writeLog: Boolean
    private val dialect: Int
    private val hash: Int
    fun getParent(): SourceCode? {
        return parent
    }

    fun hasPrevious(): Boolean {
        return pos > 0
    }

    /**
     * returns if the internal pointer is not on the last positions
     */
    operator fun hasNext(): Boolean {
        return pos + 1 < lcText!!.size
    }

    fun hasNextNext(): Boolean {
        return pos + 2 < lcText!!.size
    }

    /**
     * moves the internal pointer to the next position, no check if the next position is still valid
     */
    operator fun next() {
        pos++
    }

    /**
     * moves the internal pointer to the previous position, no check if the next position is still valid
     */
    fun previous() {
        pos--
    }

    /**
     * returns the character of the current position of the internal pointer
     */
    fun getCurrent(): Char {
        return text!![pos]
    }

    /**
     * returns the lower case representation of the character of the current position
     */
    fun getCurrentLower(): Char {
        return lcText!![pos]
    }

    /**
     * returns the character at the given position
     */
    fun charAt(pos: Int): Char {
        return text!![pos]
    }

    /**
     * returns the character at the given position as lower case representation
     */
    fun charAtLower(pos: Int): Char {
        return lcText!![pos]
    }

    fun isPrevious(c: Char): Boolean {
        return if (!hasPrevious()) false else lcText!![pos - 1] == c
    }

    /**
     * is the character at the next position the same as the character provided by the input parameter
     */
    fun isNext(c: Char): Boolean {
        return if (!hasNext()) false else lcText!![pos + 1] == c
    }

    fun isNext(a: Char, b: Char): Boolean {
        return if (!hasNextNext()) false else lcText!![pos + 1] == a && lcText[pos + 2] == b
    }

    private fun isNextRaw(c: Char): Boolean {
        return if (!hasNext()) false else text!![pos + 1] == c
    }

    /**
     * is the character at the current position (internal pointer) in the range of the given input
     * characters?
     *
     * @param left lower value.
     * @param right upper value.
     */
    fun isCurrentBetween(left: Char, right: Char): Boolean {
        return if (!isValidIndex()) false else lcText!![pos] >= left && lcText[pos] <= right
    }

    /**
     * returns if the character at the current position (internal pointer) is a valid variable character
     */
    fun isCurrentVariableCharacter(): Boolean {
        return if (!isValidIndex()) false else isCurrentLetter() || isCurrentNumber() || isCurrent('$') || isCurrent('_')
    }

    /**
     * returns if the current character is a letter (a-z,A-Z)
     *
     * @return is a letter
     */
    fun isCurrentLetter(): Boolean {
        return if (!isValidIndex()) false else lcText!![pos] >= 'a' && lcText[pos] <= 'z'
    }

    /**
     * returns if the current character is a number (0-9)
     *
     * @return is a letter
     */
    fun isCurrentNumber(): Boolean {
        return if (!isValidIndex()) false else lcText!![pos] >= '0' && lcText[pos] <= '9'
    }

    /**
     * retuns if the current character (internal pointer) is a valid special sign (_, $, Pound Symbol,
     * Euro Symbol)
     */
    fun isCurrentSpecial(): Boolean {
        return if (!isValidIndex()) false else lcText!![pos] == '_' || lcText[pos] == '$' || lcText[pos] == SystemUtil.CHAR_EURO || lcText[pos] == SystemUtil.CHAR_POUND
    }

    /**
     * is the current character (internal pointer) the same as the given
     */
    fun isCurrent(c: Char): Boolean {
        return if (!isValidIndex()) false else lcText!![pos] == c
    }

    /**
     * forward the internal pointer plus one if the next character is the same as the given input
     */
    fun forwardIfCurrent(c: Char): Boolean {
        if (isCurrent(c)) {
            pos++
            return true
        }
        return false
    }

    /**
     * returns if the current character (internal pointer) and the following are the same as the given
     * input
     */
    fun isCurrent(str: String?): Boolean {
        if (pos + str!!.length() > lcText!!.size) return false
        for (i in str!!.length() - 1 downTo 0) {
            if (str.charAt(i) !== lcText.get(pos + i)) return false
        }
        return true
    }

    /**
     * forwards if the current character (internal pointer) and the following are the same as the given
     * input
     */
    fun forwardIfCurrent(str: String?): Boolean {
        val `is` = isCurrent(str)
        if (`is`) pos += str!!.length()
        return `is`
    }

    /**
     * @param str string to check against current position
     * @param startWithSpace if true there must be whitespace at the current position
     * @return does the criteria match?
     */
    fun forwardIfCurrent(str: String?, startWithSpace: Boolean): Boolean {
        if (!startWithSpace) return forwardIfCurrent(str)
        val start = pos
        if (!removeSpace()) return false
        if (!forwardIfCurrent(str)) {
            pos = start
            return false
        }
        return true
    }

    /**
     * @param str string to check against current position
     * @param startWithSpace if true there must be whitespace at the current position
     * @param followedByNoVariableCharacter the character following the string must be a none variable
     * character (!a-z,A-Z,0-9,_$) (not eaten)
     * @return does the criteria match?
     */
    fun forwardIfCurrent(str: String?, startWithSpace: Boolean, followedByNoVariableCharacter: Boolean): Boolean {
        val start = pos
        if (startWithSpace && !removeSpace()) return false
        if (!forwardIfCurrent(str)) {
            pos = start
            return false
        }
        if (followedByNoVariableCharacter && isCurrentVariableCharacter()) {
            pos = start
            return false
        }
        return true
    }

    /**
     * forwards if the current character (internal pointer) and the following are the same as the given
     * input, followed by a none word character
     */
    fun forwardIfCurrentAndNoWordAfter(str: String?): Boolean {
        val c = pos
        if (forwardIfCurrent(str)) {
            if (!isCurrentBetween('a', 'z') && !isCurrent('_')) return true
        }
        pos = c
        return false
    }

    /**
     * forwards if the current character (internal pointer) and the following are the same as the given
     * input, followed by a none word character or a number
     */
    fun forwardIfCurrentAndNoVarExt(str: String?): Boolean {
        val c = pos
        if (forwardIfCurrent(str)) {
            if (!isCurrentBetween('a', 'z') && !isCurrentBetween('0', '9') && !isCurrent('_')) return true
        }
        pos = c
        return false
    }

    /**
     * Gibt zurueck ob first den folgenden Zeichen entspricht, gefolgt von Leerzeichen und second.
     *
     * @param first Erste Zeichen zum Vergleich (Vor den Leerzeichen).
     * @param second Zweite Zeichen zum Vergleich (Nach den Leerzeichen).
     * @return Gibt zurueck ob die eingegebenen Werte dem Inhalt beim aktuellen Stand des Zeigers
     * entsprechen.
     */
    fun isCurrent(first: String?, second: Char): Boolean {
        val start = pos
        if (!forwardIfCurrent(first)) return false
        removeSpace()
        val rtn = isCurrent(second)
        pos = start
        return rtn
    }

    /**
     * Gibt zurueck ob first den folgenden Zeichen entspricht, gefolgt von Leerzeichen und second.
     *
     * @param first Erstes Zeichen zum Vergleich (Vor den Leerzeichen).
     * @param second Zweites Zeichen zum Vergleich (Nach den Leerzeichen).
     * @return Gibt zurueck ob die eingegebenen Werte dem Inhalt beim aktuellen Stand des Zeigers
     * entsprechen.
     */
    fun isCurrent(first: Char, second: Char): Boolean {
        val start = pos
        if (!forwardIfCurrent(first)) return false
        removeSpace()
        val rtn = isCurrent(second)
        pos = start
        return rtn
    }

    /**
     * Gibt zurueck ob first den folgenden Zeichen entspricht, gefolgt von Leerzeichen und second, wenn
     * ja wird der Zeiger um die Laenge der uebereinstimmung nach vorne gestellt.
     *
     * @param first Erste Zeichen zum Vergleich (Vor den Leerzeichen).
     * @param second Zweite Zeichen zum Vergleich (Nach den Leerzeichen).
     * @return Gibt zurueck ob der Zeiger vorwaerts geschoben wurde oder nicht.
     */
    fun forwardIfCurrent(first: String?, second: Char): Boolean {
        val start = pos
        if (!forwardIfCurrent(first)) return false
        removeSpace()
        val rtn = forwardIfCurrent(second)
        if (!rtn) pos = start
        return rtn
    }

    /**
     * Gibt zurueck ob ein Wert folgt und vor und hinterher Leerzeichen folgen.
     *
     * @param before Definition der Leerzeichen vorher.
     * @param val Gefolgter Wert der erartet wird.
     * @param after Definition der Leerzeichen nach dem Wert.
     * @return Gibt zurueck ob der Zeiger vorwaerts geschoben wurde oder nicht.
     */
    fun forwardIfCurrent(before: Short, `val`: String?, after: Short): Boolean {
        val start = pos
        // space before
        if (before == AT_LEAST_ONE_SPACE) {
            if (!removeSpace()) return false
        } else removeSpace()

        // value
        if (!forwardIfCurrent(`val`)) {
            setPos(start)
            return false
        }

        // space after
        if (after == AT_LEAST_ONE_SPACE) {
            if (!removeSpace()) {
                setPos(start)
                return false
            }
        } else removeSpace()
        return true
    }

    /**
     * Gibt zurueck ob first den folgenden Zeichen entspricht, gefolgt von Leerzeichen und second, wenn
     * ja wird der Zeiger um die Laenge der uebereinstimmung nach vorne gestellt.
     *
     * @param first Erste Zeichen zum Vergleich (Vor den Leerzeichen).
     * @param second Zweite Zeichen zum Vergleich (Nach den Leerzeichen).
     * @return Gibt zurueck ob der Zeiger vorwaerts geschoben wurde oder nicht.
     */
    fun forwardIfCurrent(first: Char, second: Char): Boolean {
        val start = pos
        if (!forwardIfCurrent(first)) return false
        removeSpace()
        val rtn = forwardIfCurrent(second)
        if (!rtn) pos = start
        return rtn
    }

    /**
     * Gibt zurueck ob first den folgenden Zeichen entspricht, gefolgt von Leerzeichen und second.
     *
     * @param first Erste Zeichen zum Vergleich (Vor den Leerzeichen).
     * @param second Zweite Zeichen zum Vergleich (Nach den Leerzeichen).
     * @return Gibt zurueck ob die eingegebenen Werte dem Inhalt beim aktuellen Stand des Zeigers
     * entsprechen.
     */
    fun isCurrent(first: String?, second: String?): Boolean {
        val start = pos
        if (!forwardIfCurrent(first)) return false
        removeSpace()
        val rtn = isCurrent(second)
        pos = start
        return rtn
    }

    /**
     * Gibt zurueck ob first den folgenden Zeichen entspricht, gefolgt von Leerzeichen und second, wenn
     * ja wird der Zeiger um die Laenge der uebereinstimmung nach vorne gestellt.
     *
     * @param first Erste Zeichen zum Vergleich (Vor den Leerzeichen).
     * @param second Zweite Zeichen zum Vergleich (Nach den Leerzeichen).
     * @return Gibt zurueck ob der Zeiger vorwaerts geschoben wurde oder nicht.
     */
    fun forwardIfCurrent(first: String?, second: String?): Boolean {
        val start = pos
        if (!forwardIfCurrent(first)) return false
        if (!removeSpace()) {
            pos = start
            return false
        }
        val rtn = forwardIfCurrent(second)
        if (!rtn) pos = start
        return rtn
    }

    fun forwardIfCurrent(first: String?, second: String?, third: String?): Boolean {
        val start = pos
        if (!forwardIfCurrent(first)) return false
        if (!removeSpace()) {
            pos = start
            return false
        }
        if (!forwardIfCurrent(second)) {
            pos = start
            return false
        }
        if (!removeSpace()) {
            pos = start
            return false
        }
        val rtn = forwardIfCurrent(third)
        if (!rtn) pos = start
        return rtn
    }

    fun forwardIfCurrent(first: String?, second: String?, third: String?, startWithSpace: Boolean): Boolean {
        if (!startWithSpace) return forwardIfCurrent(first, second, third)
        val start = pos
        if (!removeSpace()) return false
        if (!forwardIfCurrent(first, second, third)) {
            pos = start
            return false
        }
        return true
    }

    fun forwardIfCurrent(first: String?, second: String?, third: String?, startWithSpace: Boolean, followedByNoVariableCharacter: Boolean): Boolean {
        val start = pos
        if (startWithSpace && !removeSpace()) return false
        if (!forwardIfCurrent(first, second, third)) {
            pos = start
            return false
        }
        if (followedByNoVariableCharacter && isCurrentVariableCharacter()) {
            pos = start
            return false
        }
        return true
    }

    fun forwardIfCurrent(first: String?, second: String?, startWithSpace: Boolean, followedByNoVariableCharacter: Boolean): Boolean {
        val start = pos
        if (startWithSpace && !removeSpace()) return false
        if (!forwardIfCurrent(first, second)) {
            pos = start
            return false
        }
        if (followedByNoVariableCharacter && isCurrentVariableCharacter()) {
            pos = start
            return false
        }
        return true
    }

    fun forwardIfCurrent(first: String?, second: String?, third: String?, forth: String?): Boolean {
        val start = pos
        if (!forwardIfCurrent(first)) return false
        if (!removeSpace()) {
            pos = start
            return false
        }
        if (!forwardIfCurrent(second)) {
            pos = start
            return false
        }
        if (!removeSpace()) {
            pos = start
            return false
        }
        if (!forwardIfCurrent(third)) {
            pos = start
            return false
        }
        if (!removeSpace()) {
            pos = start
            return false
        }
        val rtn = forwardIfCurrent(forth)
        if (!rtn) pos = start
        return rtn
    }

    /**
     * Gibt zurueck ob sich vor dem aktuellen Zeichen Leerzeichen befinden.
     *
     * @return Gibt zurueck ob sich vor dem aktuellen Zeichen Leerzeichen befinden.
     */
    fun hasSpaceBefore(): Boolean {
        return pos > 0 && lcText!![pos - 1] == ' '
    }

    fun hasNLBefore(): Boolean {
        var index = 0
        while (pos - ++index >= 0) {
            if (text!![pos - index] == '\n') return true
            if (text[pos - index] == '\r') return true
            if (lcText!![pos - index] != ' ') return false
        }
        return false
    }

    /**
     * Stellt den Zeiger nach vorne, wenn er sich innerhalb von Leerzeichen befindet, bis die
     * Leerzeichen fertig sind.
     *
     * @return Gibt zurueck ob der Zeiger innerhalb von Leerzeichen war oder nicht.
     */
    fun removeSpace(): Boolean {
        val start = pos
        while (pos < lcText!!.size && lcText[pos] == ' ') {
            pos++
        }
        return start < pos
    }

    fun revertRemoveSpace() {
        while (hasSpaceBefore()) {
            previous()
        }
    }

    fun removeAndGetSpace(): String? {
        val start = pos
        while (pos < lcText!!.size && lcText[pos] == ' ') {
            pos++
        }
        return substring(start, pos - start)
    }

    /**
     * Stellt den internen Zeiger an den Anfang der naechsten Zeile, gibt zurueck ob eine weitere Zeile
     * existiert oder ob es bereits die letzte Zeile war.
     *
     * @return Existiert eine weitere Zeile.
     */
    fun nextLine(): Boolean {
        while (isValidIndex() && text!![pos] != '\n' && text[pos] != '\r') {
            next()
        }
        if (!isValidIndex()) return false
        if (text!![pos] == '\n') {
            next()
            return isValidIndex()
        }
        if (text[pos] == '\r') {
            next()
            if (isValidIndex() && text[pos] == '\n') {
                next()
            }
            return isValidIndex()
        }
        return false
    }

    /**
     * Gibt eine Untermenge des CFMLString als Zeichenkette zurueck, ausgehend von start bis zum Ende
     * des CFMLString.
     *
     * @param start Von wo aus die Untermege ausgegeben werden soll.
     * @return Untermenge als Zeichenkette
     */
    fun substring(start: Int): String? {
        return substring(start, lcText!!.size - start)
    }

    /**
     * Gibt eine Untermenge des CFMLString als Zeichenkette zurueck, ausgehend von start mit einer
     * maximalen Laenge count.
     *
     * @param start Von wo aus die Untermenge ausgegeben werden soll.
     * @param count Wie lange die zurueckgegebene Zeichenkette maximal sein darf.
     * @return Untermenge als Zeichenkette.
     */
    fun substring(start: Int, count: Int): String? {
        return String.valueOf(text, start, count)
    }

    /**
     * Gibt eine Untermenge des CFMLString als Zeichenkette in Kleinbuchstaben zurueck, ausgehend von
     * start bis zum Ende des CFMLString.
     *
     * @param start Von wo aus die Untermenge ausgegeben werden soll.
     * @return Untermenge als Zeichenkette in Kleinbuchstaben.
     */
    fun substringLower(start: Int): String? {
        return substringLower(start, lcText!!.size - start)
    }

    /**
     * Gibt eine Untermenge des CFMLString als Zeichenkette in Kleinbuchstaben zurueck, ausgehend von
     * start mit einer maximalen Laenge count.
     *
     * @param start Von wo aus die Untermenge ausgegeben werden soll.
     * @param count Wie lange die zurueckgegebene Zeichenkette maximal sein darf.
     * @return Untermenge als Zeichenkette in Kleinbuchstaben.
     */
    fun substringLower(start: Int, count: Int): String? {
        return String.valueOf(lcText, start, count)
    }
    /**
     * return a subset of the current SourceCode
     *
     * @param start start position of the new subset.
     * @param count length of the new subset.
     * @return subset of the SourceCode as new SourcCode
     */
    /**
     * Gibt eine Untermenge des CFMLString als CFMLString zurueck, ausgehend von start bis zum Ende des
     * CFMLString.
     *
     * @param start Von wo aus die Untermenge ausgegeben werden soll.
     * @return Untermenge als CFMLString
     */
    @JvmOverloads
    fun subCFMLString(start: Int, count: Int = text!!.size - start): SourceCode? {
        return SourceCode(this, String.valueOf(text, start, count), writeLog, dialect)
    }

    /**
     * Gibt den CFMLString als String zurueck.
     *
     * @see java.lang.Object.toString
     */
    @Override
    override fun toString(): String {
        return String(text)
    }

    /**
     * Gibt die aktuelle Position des Zeigers innerhalb des CFMLString zurueck.
     *
     * @return Position des Zeigers
     */
    fun getPos(): Int {
        return pos
    }

    /**
     * Setzt die Position des Zeigers innerhalb des CFMLString, ein ungueltiger index wird ignoriert.
     *
     * @param pos Position an die der Zeiger gestellt werde soll.
     */
    fun setPos(pos: Int) {
        this.pos = pos
    }

    /**
     * Gibt die aktuelle Zeile zurueck in der der Zeiger des CFMLString steht.
     *
     * @return Zeilennummer
     */
    fun getLine(): Int {
        return getLine(pos)
    }

    fun getPosition(): Position? {
        return getPosition(pos)
    }

    fun getPosition(pos: Int): Position? {
        var line = 0
        var posAtStart = 0
        for (i in lines.indices) {
            if (pos <= lines!![i].intValue()) {
                line = i + 1
                if (i > 0) posAtStart = lines!![i - 1].intValue()
                break
            }
        }
        if (line == 0) throw RuntimeException("syntax error")
        val column = pos - posAtStart
        return Position(line, column, pos)
    }

    /**
     * Gibt zurueck in welcher Zeile die angegebene Position ist.
     *
     * @param pos Position von welcher die Zeile erfragt wird
     * @return Zeilennummer
     */
    fun getLine(pos: Int): Int {
        for (i in lines.indices) {
            if (pos <= lines!![i].intValue()) return i + 1
        }
        return lines!!.size
    }

    /**
     * Gibt die Stelle in der aktuelle Zeile zurueck, in welcher der Zeiger steht.
     *
     * @return Position innerhalb der Zeile.
     */
    fun getColumn(): Int {
        return getColumn(pos)
    }

    /**
     * Gibt die Stelle in der Zeile auf die pos zeigt zurueck.
     *
     * @param pos Position von welcher die Zeile erfragt wird
     * @return Position innerhalb der Zeile.
     */
    fun getColumn(pos: Int): Int {
        val line = getLine(pos) - 1
        return if (line == 0) pos + 1 else pos - lines!![line - 1].intValue()
    }

    /**
     * Gibt die Zeile auf welcher der Zeiger steht als String zurueck.
     *
     * @return Zeile als Zeichenkette
     */
    fun getLineAsString(): String? {
        return getLineAsString(getLine(pos))
    }

    /**
     * Gibt die angegebene Zeile als String zurueck.
     *
     * @param line Zeile die zurueck gegeben werden soll
     * @return Zeile als Zeichenkette
     */
    fun getLineAsString(line: Int): String? {
        val index = line - 1
        if (lines!!.size <= index) return null
        val max: Int = lines[index].intValue()
        var min = 0
        if (index != 0) min = lines[index - 1].intValue() + 1
        return if (min < max && max - 1 < lcText!!.size) this.substring(min, max - min) else ""
    }

    /**
     * Gibt zurueck ob der Zeiger auf dem letzten Zeichen steht.
     *
     * @return Gibt zurueck ob der Zeiger auf dem letzten Zeichen steht.
     */
    fun isLast(): Boolean {
        return pos == lcText!!.size - 1
    }

    /**
     * Gibt zurueck ob der Zeiger nach dem letzten Zeichen steht.
     *
     * @return Gibt zurueck ob der Zeiger nach dem letzten Zeichen steht.
     */
    fun isAfterLast(): Boolean {
        return pos >= lcText!!.size
    }

    /**
     * Gibt zurueck ob der Zeiger einen korrekten Index hat.
     *
     * @return Gibt zurueck ob der Zeiger einen korrekten Index hat.
     */
    fun isValidIndex(): Boolean {
        return pos < lcText!!.size && pos > -1
    }

    /**
     * Gibt zurueck, ausgehend von der aktuellen Position, wann das naechste Zeichen folgt das gleich
     * ist wie die Eingabe, falls keines folgt wird -1 zurueck gegeben. Gross- und Kleinschreibung der
     * Zeichen werden igoriert.
     *
     * @param c gesuchtes Zeichen
     * @return Zeichen das gesucht werden soll.
     */
    fun indexOfNext(c: Char): Int {
        for (i in pos until lcText!!.size) {
            if (lcText!![i] == c) return i
        }
        return -1
    }

    fun indexOfNext(str: String?): Int {
        val carr: CharArray = str.toCharArray()
        outer@ for (i in pos until lcText!!.size) {
            if (lcText!![i] == carr[0]) {
                // print.e("- "+lcText[i]);
                for (y in 1 until carr.size) {
                    // print.e("-- "+y);
                    if (lcText.size <= i + y || lcText[i + y] != carr[y]) {
                        // print.e("ggg");
                        continue@outer
                    }
                }
                return i
            }
        }
        return -1
    }

    /**
     * Gibt das letzte Wort das sich vor dem aktuellen Zeigerstand befindet zurueck, falls keines
     * existiert wird null zurueck gegeben.
     *
     * @return Word vor dem aktuellen Zeigerstand.
     */
    fun lastWord(): String? {
        var size = 1
        while (pos - size > 0 && lcText!![pos - size] == ' ') {
            size++
        }
        while (pos - size > 0 && lcText!![pos - size] != ' ' && lcText[pos - size] != ';') {
            size++
        }
        return this.substring(pos - size + 1, pos - 1)
    }

    /**
     * Gibt die Laenge des CFMLString zurueck.
     *
     * @return Laenge des CFMLString.
     */
    fun length(): Int {
        return lcText!!.size
    }

    /**
     * Prueft ob das uebergebene Objekt diesem Objekt entspricht.
     *
     * @param o Object zum vergleichen.
     * @return Ist das uebergebene Objekt das selbe wie dieses.
     */
    @Override
    override fun equals(o: Object?): Boolean {
        return if (o !is SourceCode) false else o.toString().equals(this.toString())
    }

    fun getWriteLog(): Boolean {
        return writeLog
    }

    fun getText(): String? {
        return String(text)
    }

    fun id(): String? {
        return HashUtil.create64BitHashAsString(getText())
    }

    fun getDialect(): Int {
        return dialect
    }

    @Override
    override fun hashCode(): Int {
        return hash
    }

    companion object {
        const val AT_LEAST_ONE_SPACE: Short = 0
        const val ZERO_OR_MORE_SPACE: Short = 1
    }

    /**
     * Constructor of the class
     *
     * @param parent
     *
     * @param text
     * @param charset
     */
    init {
        text = strText.toCharArray()
        hash = strText!!.hashCode()
        this.dialect = dialect
        lcText = CharArray(text!!.size)
        val arr: ArrayList<Integer?> = ArrayList<Integer?>()
        var i = 0
        while (i < text.size) {
            pos = i
            if (text[i] == '\n') {
                arr.add(Integer.valueOf(i))
                lcText[i] = ' '
            } else if (text[i] == '\r') {
                if (isNextRaw('\n')) {
                    lcText[i++] = ' '
                }
                arr.add(Integer.valueOf(i))
                lcText[i] = ' '
            } else if (text[i] == '\t') lcText[i] = ' ' else lcText[i] = Character.toLowerCase(text[i])
            i++
        }
        pos = 0
        arr.add(Integer.valueOf(text.size))
        lines = arr.toArray(arrayOfNulls<Integer?>(arr.size()))
        this.writeLog = writeLog
    }
}