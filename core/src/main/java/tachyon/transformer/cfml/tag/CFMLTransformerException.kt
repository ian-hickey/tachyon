/**
 * Copyright (c) 2023, TachyonCFML.org
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
package tachyon.transformer.cfml.tag

import tachyon.commons.lang.StringUtil

/**
 * Die Klasse TemplateException wird durch den CFMLTransformer geworfen, wenn dieser auf einen
 * grammatikalischen Fehler in dem zu verarbeitenden CFML Code stoesst oder wenn ein Tag oder eine
 * Funktion von der Definition innerhalb der Tag- bzw. der Funktions- Library abweicht.
 */
class CFMLTransformerException(sc: SourceCode?, message: String?) : Exception(message) {
    private val sc: SourceCode? = null
    // private String htmlMessage;
    /**
     * Konstruktor mit einem CFMLString und einer anderen Exception.
     *
     * @param cfml
     * @param e
     */
    constructor(sc: SourceCode?, e: Exception?) : this(sc, if (StringUtil.isEmpty(e.getMessage())) Caster.toClassName(e) else e.getMessage()) {}

    /**
     * Gibt eine detaillierte Fehlermeldung zurueck. ueberschreibt toString Methode von
     * java.lang.Objekt, alias fuer getMessage().
     *
     * @return Fehlermeldung als Plain Text Ausgabe
     */
    @Override
    override fun toString(): String {
        val hasCFML = sc != null
        val sb = StringBuffer()
        sb.append("Error\n")
        sb.append("----------------------------------\n")
        if (hasCFML && sc is PageSourceCode) {
            sb.append("""
    File: ${(sc as PageSourceCode?).getPageSource().getDisplayPath().toString()}
    
    """.trimIndent())
        }
        if (hasCFML) {
            var line: Int = sc.getLine()
            var counter = 0
            sb.append("Line: $line\n")
            sb.append("""
    Column: ${sc.getColumn().toString()}
    
    """.trimIndent())
            sb.append("Type: Syntax\n")
            sb.append("Code Outprint: \n")
            line = if (line - 2 < 1) 1 else line - 2
            val lineDescLen: Int = ((line + 5).toString() + "").length()
            var i = line
            while (true) {
                if (i > 0) {
                    val strLine: String = sc.getLineAsString(i) ?: break
                    val desc = if (("" + i).length() < lineDescLen) "0$i" else "" + i
                    sb.append("""
    $desc: $strLine
    
    """.trimIndent())
                    counter++
                }
                if (counter == 5) break
                i++
            }
            sb.append("\n")
        }
        sb.append("Message:\n")
        sb.append("""
    ${super.getMessage().toString()}
    
    """.trimIndent())
        return sb.toString()
    }

    /**
     * Gibt die Zeilennummer zurueck
     *
     * @return Zeilennummer
     */
    fun getLine(): Int {
        return sc.getLine()
    }

    /**
     * Gibt die Column der aktuellen Zeile zurueck
     *
     * @return Column der Zeile
     */
    fun getColumn(): Int {
        return sc.getColumn()
    }

    /**
     * Returns the value of cfml.
     *
     * @return value cfml
     */
    fun getCfml(): SourceCode? {
        return sc
    }
    /**
     * Konstruktor ohne Message, nur mit CFMLString.
     *
     * @param cfml
     *
     * public TemplateException(CFMLString cfml) { this(cfml,"Error while transforming CFML
     * File"); }
     */
    /**
     * Hauptkonstruktor, mit CFMLString und message.
     *
     * @param cfml CFMLString
     * @param message Fehlermeldung
     */
    init {
        this.sc = sc
    }
}