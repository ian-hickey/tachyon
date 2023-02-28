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
package lucee.commons.lang

import lucee.commons.io.SystemUtil

/**
 * Der CFMLString ist eine Hilfe fuer die Transformer, er repraesentiert den CFML Code und bietet
 * Methoden an, um alle noetigen Informationen auszulesen und Manipulationen durchzufuehren. Dies
 * um, innerhalb des Transformer, wiederkehrende Zeichenketten-Manipulationen zu abstrahieren.
 *
 */
class ParserString(text: String) {
    /**
     * Gibt die aktuelle Position des Zeigers innerhalb des CFMLString zurueck.
     *
     * @return Position des Zeigers
     */
    /**
     * Setzt die Position des Zeigers innerhalb des CFMLString, ein ungueltiger index wird ignoriert.
     *
     * @param pos Position an die der Zeiger gestellt werde soll.
     */
    /**
     * Field `pos`
     */
    var pos = 0

    /**
     * Field `text`
     */
    protected var text: CharArray

    /**
     * Field `lcText`
     */
    protected var lcText: CharArray

    /**
     * Gemeinsame Initialmethode der drei Konstruktoren, diese erhaelt den CFML Code als char[] und
     * uebertraegt ihn, in die interen Datenhaltung.
     *
     * @param str
     */
    protected fun init(str: String) {
        val len: Int = str.length()
        text = CharArray(len)
        lcText = CharArray(len)
        for (i in 0 until len) {
            val c: Char = str.charAt(i)
            text[i] = c
            if (c == '\n' || c == '\r' || c == '\t') {
                lcText[i] = ' '
            } else lcText[i] = if (c >= 'a' && c <= 'z' || c >= '0' && c <= '9') c else Character.toLowerCase(c)
        }
    }

    /**
     * Gibt zurueck ob, ausgehend von der aktuellen Position des internen Zeigers im Text, noch ein
     * Zeichen vorangestellt ist.
     *
     * @return boolean Existiert ein weieters Zeichen nach dem Zeiger.
     */
    operator fun hasNext(): Boolean {
        return pos + 1 < text.size
    }

    fun hasNextNext(): Boolean {
        return pos + 2 < text.size
    }

    fun hasPrevious(): Boolean {
        return pos - 1 >= 0
    }

    fun hasPreviousPrevious(): Boolean {
        return pos - 2 >= 0
    }

    /**
     * Stellt den internen Zeiger auf die naechste Position. ueberlappungen ausserhalb des Index des
     * Textes werden ignoriert.
     */
    operator fun next() {
        pos++
    }

    /**
     * Stellt den internen Zeiger auf die vorhergehnde Position. ueberlappungen ausserhalb des Index des
     * Textes werden ignoriert.
     */
    fun previous() {
        pos--
    }

    /**
     * Gibt das Zeichen (Character) an der aktuellen Position des Zeigers aus.
     *
     * @return char Das Zeichen auf dem der Zeiger steht.
     */
    val current: Char
        get() = text[pos]

    /**
     * Gibt das Zeichen (Character) an der naechsten Position des Zeigers aus.
     *
     * @return char Das Zeichen auf dem der Zeiger steht plus 1.
     */
    val next: Char
        get() = text[pos + 1]

    /**
     * Gibt das Zeichen, als Kleinbuchstaben, an der aktuellen Position des Zeigers aus.
     *
     * @return char Das Zeichen auf dem der Zeiger steht als Kleinbuchstaben.
     */
    val currentLower: Char
        get() = lcText[pos]

    /**
     * Gibt das Zeichen, als Grossbuchstaben, an der aktuellen Position des Zeigers aus.
     *
     * @return char Das Zeichen auf dem der Zeiger steht als Grossbuchstaben.
     */
    val currentUpper: Char
        get() = Character.toUpperCase(text[pos])

    /**
     * Gibt das Zeichen, als Kleinbuchstaben, an der naechsten Position des Zeigers aus.
     *
     * @return char Das Zeichen auf dem der Zeiger steht plus 1 als Kleinbuchstaben.
     */
    val nextLower: Char
        get() = lcText[pos]

    /**
     * Gibt das Zeichen an der angegebenen Position zurueck.
     *
     * @param pos Position des auszugebenen Zeichen.
     * @return char Das Zeichen an der angegebenen Position.
     */
    fun charAt(pos: Int): Char {
        return text[pos]
    }

    /**
     * Gibt das Zeichen, als Kleinbuchstaben, an der angegebenen Position zurueck.
     *
     * @param pos Position des auszugebenen Zeichen.
     * @return char Das Zeichen an der angegebenen Position als Kleinbuchstaben.
     */
    fun charAtLower(pos: Int): Char {
        return lcText[pos]
    }

    /**
     * Gibt zurueck ob das naechste Zeichen das selbe ist wie das Eingegebene.
     *
     * @param c Zeichen zum Vergleich.
     * @return boolean
     */
    fun isNext(c: Char): Boolean {
        return if (!hasNext()) false else lcText[pos + 1] == c
    }

    fun isPrevious(c: Char): Boolean {
        return if (!hasPrevious()) false else lcText[pos - 1] == c
    }

    /**
     * Gibt zurueck ob das naechste Zeichen das selbe ist wie das Eingegebene.
     *
     * @param c Zeichen zum Vergleich.
     * @return boolean
     */
    fun isCurrentIgnoreSpace(c: Char): Boolean {
        if (!hasNext()) return false
        val start = pos
        removeSpace()
        val `is` = isCurrent(c)
        pos = start
        return `is`
    }

    /**
     * Gibt zurueck ob das naechste Zeichen das selbe ist wie das Eingegebene.
     *
     * @param c Zeichen zum Vergleich.
     * @return boolean
     */
    fun isCurrentIgnoreSpace(str: String): Boolean {
        if (!hasNext()) return false
        val start = pos
        removeSpace()
        val `is` = isCurrent(str)
        pos = start
        return `is`
    }

    /**
     * Gibt zurueck ob das aktuelle Zeichen zwischen den Angegebenen liegt.
     *
     * @param left Linker (unterer) Wert.
     * @param right Rechter (oberer) Wert.
     * @return Gibt zurueck ob das aktuelle Zeichen zwischen den Angegebenen liegt.
     */
    fun isCurrentBetween(left: Char, right: Char): Boolean {
        return if (!isValidIndex) false else lcText[pos] >= left && lcText[pos] <= right
    }

    /**
     * Gibt zurueck ob das aktuelle Zeichen eine Zahl ist.
     *
     * @return Gibt zurueck ob das aktuelle Zeichen eine Zahl ist.
     */
    val isCurrentDigit: Boolean
        get() = if (!isValidIndex) false else lcText[pos] >= '0' && lcText[pos] <= '9'

    /**
     * Gibt zurueck ob das aktuelle Zeichen eine Zahl ist.
     *
     * @return Gibt zurueck ob das aktuelle Zeichen eine Zahl ist.
     */
    val isCurrentQuoter: Boolean
        get() = if (!isValidIndex) false else lcText[pos] == '"' || lcText[pos] == '\''

    /**
     * Gibt zurueck ob das aktuelle Zeichen ein Buchstabe ist.
     *
     * @return Gibt zurueck ob das aktuelle Zeichen ein Buchstabe ist.
     */
    val isCurrentLetter: Boolean
        get() = if (!isValidIndex) false else lcText[pos] >= 'a' && lcText[pos] <= 'z'
    val isCurrentNumber: Boolean
        get() = if (!isValidIndex) false else lcText[pos] >= '0' && lcText[pos] <= '9'

    // return lcText[pos]>='a' && lcText[pos]<='z';
    val isCurrentWhiteSpace: Boolean
        get() = if (!isValidIndex) false else lcText[pos] == ' ' || lcText[pos] == '\t' || lcText[pos] == '\b' || lcText[pos] == '\r' || lcText[pos] == '\n'

    // return lcText[pos]>='a' && lcText[pos]<='z';
    fun forwardIfCurrentWhiteSpace(): Boolean {
        var rtn = false
        while (isCurrentWhiteSpace) {
            pos++
            rtn = true
        }
        return rtn
    }

    val isNextWhiteSpace: Boolean
        get() = if (!hasNext()) false else lcText[pos + 1] == ' ' || lcText[pos + 1] == '\t' || lcText[pos + 1] == '\b' || lcText[pos + 1] == '\r' || lcText[pos + 1] == '\n'
    val isNextNextWhiteSpace: Boolean
        get() = if (!hasNextNext()) false else lcText[pos + 2] == ' ' || lcText[pos + 2] == '\t' || lcText[pos + 2] == '\b' || lcText[pos + 2] == '\r' || lcText[pos + 2] == '\n'
    val isPreviousWhiteSpace: Boolean
        get() = if (!hasPrevious()) false else lcText[pos - 1] == ' ' || lcText[pos - 1] == '\t' || lcText[pos - 1] == '\b' || lcText[pos - 1] == '\r' || lcText[pos - 1] == '\n'
    val isPreviousPreviousWhiteSpace: Boolean
        get() = if (!hasPreviousPrevious()) false else lcText[pos - 2] == ' ' || lcText[pos - 2] == '\t' || lcText[pos - 2] == '\b' || lcText[pos - 2] == '\r' || lcText[pos - 2] == '\n'

    /**
     * Gibt zurueck ob das aktuelle Zeichen ein Special Buchstabe ist (_,<euro>,$,<pound>).
     *
     * @return Gibt zurueck ob das aktuelle Zeichen ein Buchstabe ist.
    </pound></euro> */
    val isCurrentSpecial: Boolean
        get() = if (!isValidIndex) false else lcText[pos] == '_' || lcText[pos] == '$' || lcText[pos] == SystemUtil.SYMBOL_EURO || lcText[pos] == SystemUtil.SYMBOL_POUND

    /**
     * Gibt zurueck ob das aktuelle Zeichen das selbe ist wie das Eingegebene.
     *
     * @param c char Zeichen zum Vergleich.
     * @return boolean
     */
    fun isCurrent(c: Char): Boolean {
        return if (!isValidIndex) false else lcText[pos] == c
    }

    fun isLast(c: Char): Boolean {
        return if (lcText.size == 0) false else lcText[lcText.size - 1] == c
    }

    /**
     * Stellt den Zeiger eins nach vorn, wenn das aktuelle Zeichen das selbe ist wie das Eingegebene,
     * gibt zurueck ob es das selbe Zeichen war oder nicht.
     *
     * @param c char Zeichen zum Vergleich.
     * @return boolean
     */
    fun forwardIfCurrent(c: Char): Boolean {
        if (isCurrent(c)) {
            pos++
            return true
        }
        return false
    }

    /**
     * Gibt zurueck ob das aktuelle und die folgenden Zeichen die selben sind, wie in der angegebenen
     * Zeichenkette.
     *
     * @param str String Zeichen zum Vergleich.
     * @return boolean
     */
    fun isCurrent(str: String): Boolean {
        if (pos + str.length() > text.size) return false
        for (i in str.length() - 1 downTo 0) {
            if (str.charAt(i) !== lcText.get(pos + i)) return false
        }
        return true
    }

    /**
     * Gibt zurueck ob das aktuelle und die folgenden Zeichen die selben sind, wie in der angegebenen
     * Zeichenkette, wenn ja wird der Zeiger um die Laenge des String nach vorne gesetzt.
     *
     * @param str String Zeichen zum Vergleich.
     * @return boolean
     */
    fun forwardIfCurrent(str: String): Boolean {
        val `is` = isCurrent(str)
        if (`is`) pos += str.length()
        return `is`
    }

    fun forwardIfCurrent(str: String, startWithSpace: Boolean): Boolean {
        if (!startWithSpace) return forwardIfCurrent(str)
        val start = pos
        if (!removeSpace()) return false
        if (!forwardIfCurrent(str)) {
            pos = start
            return false
        }
        return true
    }

    fun forwardIfCurrent(first: String, second: String, third: String, startWithSpace: Boolean): Boolean {
        if (!startWithSpace) return forwardIfCurrent(first, second, third)
        val start = pos
        if (!removeSpace()) return false
        if (!forwardIfCurrent(first, second, third)) {
            pos = start
            return false
        }
        return true
    }

    /**
     * Gibt zurueck ob das aktuelle und die folgenden Zeichen die selben sind gefolgt nicht von einem
     * word character, wenn ja wird der Zeiger um die Laenge des String nach vorne gesetzt.
     *
     * @param str String Zeichen zum Vergleich.
     * @return boolean
     */
    fun forwardIfCurrentAndNoWordAfter(str: String): Boolean {
        val c = pos
        if (forwardIfCurrent(str)) {
            if (!isCurrentLetter && !isCurrent('_')) return true
        }
        pos = c
        return false
    }

    fun forwardIfCurrentAndNoWordNumberAfter(str: String): Boolean {
        val c = pos
        if (forwardIfCurrent(str)) {
            if (!isCurrentLetter && !isCurrentLetter && !isCurrent('_')) return true
        }
        pos = c
        return false
    }

    fun forwardIfCurrentAndNoWordNumberAfter(str: String, str2: String): Boolean {
        val c = pos
        if (forwardIfCurrent(str, str2)) {
            if (!isCurrentLetter && !isCurrentLetter && !isCurrent('_')) return true
        }
        pos = c
        return false
    }

    fun forwardIfCurrentAndNoWordNumberAfter(str: String, str2: String, str3: String): Boolean {
        val c = pos
        if (forwardIfCurrent(str, str2, str3)) {
            if (!isCurrentLetter && !isCurrentLetter && !isCurrent('_')) return true
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
    fun isCurrent(first: String, second: Char): Boolean {
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
    fun forwardIfCurrent(first: String, second: Char): Boolean {
        val start = pos
        if (!forwardIfCurrent(first)) return false
        removeSpace()
        val rtn = forwardIfCurrent(second)
        if (!rtn) pos = start
        return rtn
    }

    fun forwardIfCurrent(first: String, second: String, third: Char): Boolean {
        val start = pos
        if (!forwardIfCurrent(first, second)) return false
        removeSpace()
        val rtn = forwardIfCurrent(third)
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
    fun forwardIfCurrent(before: Short, `val`: String, after: Short): Boolean {
        val start = pos
        // space before
        if (before == AT_LEAST_ONE_SPACE) {
            if (!removeSpace()) return false
        } else removeSpace()

        // value
        if (!forwardIfCurrent(`val`)) {
            pos = start
            return false
        }

        // space after
        if (after == AT_LEAST_ONE_SPACE) {
            if (!removeSpace()) {
                pos = start
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

    fun forwardIfCurrent(first: Char, second: Char, third: Char): Boolean {
        val start = pos
        if (!forwardIfCurrent(first)) return false
        removeSpace()
        var rtn = forwardIfCurrent(second)
        if (!rtn) {
            pos = start
            return rtn
        }
        removeSpace()
        rtn = forwardIfCurrent(third)
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
    fun isCurrent(first: String, second: String): Boolean {
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
    fun forwardIfCurrent(first: String, second: String): Boolean {
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

    fun forwardIfCurrent(first: String, second: String, third: String): Boolean {
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

    fun forwardIfCurrent(first: String, second: String, third: String, forth: String): Boolean {
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
        return pos > 0 && lcText[pos - 1] == ' '
    }

    /**
     * Stellt den Zeiger nach vorne, wenn er sich innerhalb von Leerzeichen befindet, bis die
     * Leerzeichen fertig sind.
     *
     * @return Gibt zurueck ob der Zeiger innerhalb von Leerzeichen war oder nicht.
     */
    fun removeSpace(): Boolean {
        val start = pos
        while (pos < text.size && lcText[pos] == ' ') {
            pos++
        }
        return start < pos
    }

    fun revertRemoveSpace() {
        while (hasSpaceBefore()) {
            previous()
        }
    }

    /**
     * Stellt den internen Zeiger an den Anfang der naechsten Zeile, gibt zurueck ob eine weitere Zeile
     * existiert oder ob es bereits die letzte Zeile war.
     *
     * @return Existiert eine weitere Zeile.
     */
    fun nextLine(): Boolean {
        while (isValidIndex && text[pos] != '\n') {
            next()
        }
        if (isValidIndex && text[pos] == '\n') {
            next()
            return isValidIndex
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
    fun substring(start: Int): String {
        return substring(start, text.size - start)
    }

    /**
     * Gibt eine Untermenge des CFMLString als Zeichenkette zurueck, ausgehend von start mit einer
     * maximalen Laenge count.
     *
     * @param start Von wo aus die Untermenge ausgegeben werden soll.
     * @param count Wie lange die zurueckgegebene Zeichenkette maximal sein darf.
     * @return Untermenge als Zeichenkette.
     */
    fun substring(start: Int, count: Int): String {
        return String.valueOf(text, start, count)
    }

    /**
     * Gibt eine Untermenge des CFMLString als Zeichenkette in Kleinbuchstaben zurueck, ausgehend von
     * start bis zum Ende des CFMLString.
     *
     * @param start Von wo aus die Untermenge ausgegeben werden soll.
     * @return Untermenge als Zeichenkette in Kleinbuchstaben.
     */
    fun substringLower(start: Int): String {
        return substringLower(start, text.size - start)
    }

    /**
     * Gibt eine Untermenge des CFMLString als Zeichenkette in Kleinbuchstaben zurueck, ausgehend von
     * start mit einer maximalen Laenge count.
     *
     * @param start Von wo aus die Untermenge ausgegeben werden soll.
     * @param count Wie lange die zurueckgegebene Zeichenkette maximal sein darf.
     * @return Untermenge als Zeichenkette in Kleinbuchstaben.
     */
    fun substringLower(start: Int, count: Int): String {
        return String.valueOf(lcText, start, count)
    }
    /**
     * Gibt eine Untermenge des CFMLString als CFMLString zurueck, ausgehend von start mit einer
     * maximalen Laenge count.
     *
     * @param start Von wo aus die Untermenge ausgegeben werden soll.
     * @param count Wie lange die zurueckgegebene Zeichenkette maximal sein darf.
     * @return Untermenge als CFMLString
     */
    /**
     * Gibt eine Untermenge des CFMLString als CFMLString zurueck, ausgehend von start bis zum Ende des
     * CFMLString.
     *
     * @param start Von wo aus die Untermenge ausgegeben werden soll.
     * @return Untermenge als CFMLString
     */
    @JvmOverloads
    fun subCFMLString(start: Int, count: Int = text.size - start): ParserString {
        return ParserString(String.valueOf(text, start, count))
        /*
		 * NICE die untermenge direkter ermiiteln, das problem hierbei sind die lines
		 * 
		 * int endPos=start+count; int LineFrom=-1; int LineTo=-1; for(int i=0;i<lines.length;i++) { if() }
		 * 
		 * return new CFMLString( 0, String.valueOf(text,start,count).toCharArray(),
		 * String.valueOf(lcText,start,count).toCharArray(), lines);
		 */
    }

    @Override
    override fun toString(): String {
        return String(text)
    }

    /**
     * Gibt zurueck ob der Zeiger auf dem letzten Zeichen steht.
     *
     * @return Gibt zurueck ob der Zeiger auf dem letzten Zeichen steht.
     */
    val isLast: Boolean
        get() = pos == text.size - 1

    /**
     * Gibt zurueck ob der Zeiger nach dem letzten Zeichen steht.
     *
     * @return Gibt zurueck ob der Zeiger nach dem letzten Zeichen steht.
     */
    val isAfterLast: Boolean
        get() = pos >= text.size

    /**
     * Gibt zurueck ob der Zeiger einen korrekten Index hat.
     *
     * @return Gibt zurueck ob der Zeiger einen korrekten Index hat.
     */
    val isValidIndex: Boolean
        get() = pos < text.size

    /**
     * Gibt zurueck, ausgehend von der aktuellen Position, wann das naechste Zeichen folgt das gleich
     * ist wie die Eingabe, falls keines folgt wird -1 zurueck gegeben. Gross- und Kleinschreibung der
     * Zeichen werden igoriert.
     *
     * @param c gesuchtes Zeichen
     * @return Zeichen das gesucht werden soll.
     */
    fun indexOfNext(c: Char): Int {
        for (i in pos until lcText.size) {
            if (lcText[i] == c) return i
        }
        return -1
    }

    /**
     * Gibt das letzte Wort das sich vor dem aktuellen Zeigerstand befindet zurueck, falls keines
     * existiert wird null zurueck gegeben.
     *
     * @return Word vor dem aktuellen Zeigerstand.
     */
    fun lastWord(): String {
        var size = 1
        while (pos - size > 0 && lcText[pos - size] == ' ') {
            size++
        }
        while (pos - size > 0 && lcText[pos - size] != ' ' && lcText[pos - size] != ';') {
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
        return text.size
    }

    /**
     * Prueft ob das uebergebene Objekt diesem Objekt entspricht.
     *
     * @param o Object zum vergleichen.
     * @return Ist das uebergebene Objekt das selbe wie dieses.
     */
    @Override
    override fun equals(o: Object): Boolean {
        return if (o !is ParserString) false else o.toString().equals(this.toString())
    }

    companion object {
        /**
         * Mindestens einen Space
         */
        const val AT_LEAST_ONE_SPACE: Short = 0

        /**
         * Mindestens ein Space
         */
        const val ZERO_OR_MORE_SPACE: Short = 1
    }

    /**
     * Diesen Konstruktor kann er CFML Code als Zeichenkette uebergeben werden.
     *
     * @param text CFML Code
     */
    init {
        init(text)
    }
}