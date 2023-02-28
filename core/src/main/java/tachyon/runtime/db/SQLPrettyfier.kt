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
package tachyon.runtime.db

import java.util.Locale

object SQLPrettyfier {
    const val PLACEHOLDER_COUNT = "placeholder_count"
    const val PLACEHOLDER_ASTERIX = "placeholder_asterix"
    const val PLACEHOLDER_QUESTION = "QUESTION_MARK_SIGN"
    fun prettyfie(sql: String?): String {
        return prettyfie(sql, false)
    }

    fun prettyfie(sql: String?, validZql: Boolean): String {
        val ps = ParserString(sql.trim())
        var insideString = false
        // short insideKlammer=0;
        val sb = StringBuilder(sql!!.length())
        // char last=0;
        outer@ while (!ps.isAfterLast()) {
            if (insideString) {
                if (ps.isCurrent('\'')) {
                    if (!ps.hasNext() || !ps.isNext('\'')) insideString = false
                }
            } else {
                if (ps.isCurrent('\'')) insideString = true else if (ps.isCurrent('?')) {
                    sb.append(" " + PLACEHOLDER_QUESTION + " ")
                    ps.next()
                    continue
                } else if (ps.isCurrent('{')) {
                    val date = StringBuilder()
                    val pos: Int = ps.getPos()
                    while (true) {
                        if (ps.isAfterLast()) {
                            ps.setPos(pos)
                            break
                        } else if (ps.isCurrent('}')) {
                            date.append('}')
                            var d: DateTime
                            d = try {
                                DateCaster.toDateAdvanced(date.toString(), null)
                            } catch (e: PageException) {
                                ps.setPos(pos)
                                break
                            }
                            sb.append('\'')
                            sb.append(DateFormat(Locale.US).format(d, "yyyy-mm-dd"))
                            sb.append(' ')
                            sb.append(TimeFormat(Locale.US).format(d, "HH:mm:ss"))
                            sb.append('\'')
                            ps.next()
                            continue@outer
                        } else {
                            date.append(ps.getCurrent())
                            ps.next()
                        }
                    }
                } else if (ps.isCurrent('*')) {
                    sb.append(" " + PLACEHOLDER_ASTERIX + " ")
                    ps.next()
                    // last=ps.getCurrent();
                    continue
                } else if (validZql && ps.isCurrent('a')) {
                    if (ps.isPreviousWhiteSpace() && ps.isNext('s') && ps.isNextNextWhiteSpace()) {
                        ps.next()
                        ps.next()
                        ps.removeSpace()
                        continue
                    }
                }
                /*
				 * for(int i=0;i<reseved_words.length;i++) { if(ps.isCurrent(reseved_words[i])) { int
				 * pos=ps.getPos(); ps.setPos(pos+4); if(ps.isCurrentWhiteSpace()) {
				 * sb.append(" placeholder_"+reseved_words[i]+" "); continue; } if(ps.isCurrent(',')) {
				 * sb.append(" placeholder_"+reseved_words[i]+","); continue; } ps.setPos(pos); } }
				 */
                /*
				 * if(ps.isCurrent("char")) { int pos=ps.getPos(); ps.setPos(pos+4); if(ps.isCurrentWhiteSpace()) {
				 * sb.append(" "+PLACEHOLDER_CHAR+" "); continue; } if(ps.isCurrent(',')) {
				 * sb.append(" "+PLACEHOLDER_CHAR+","); continue; } ps.setPos(pos); }
				 */
            }
            sb.append(ps.getCurrent())
            ps.next()
        }
        if (!ps.isLast(';')) sb.append(';')

        // print.err(sb.toString());
        // print.err("---------------------------------------------------------------------------------");
        return sb.toString()
    }
}