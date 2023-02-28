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
package tachyon.commons.lang.font

import java.awt.Font

object FontUtil {
    private var fonts: Array? = null
    private var graphics: Graphics2D? = null
    private val sync: Object = SerializableObject()
    val availableFontsAsStringArray: Array
        get() {
            val it: Iterator<Object> = getAvailableFonts(false).valueIterator()
            val arr: Array = ArrayImpl()
            while (it.hasNext()) {
                arr.appendEL((it.next() as Font).getFontName())
            }
            return arr
        }

    private fun getAvailableFonts(duplicate: Boolean): Array? {
        synchronized(sync) {
            if (fonts == null) {
                fonts = ArrayImpl()
                val graphicsEvn: GraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment()
                val availableFonts: Array<Font> = graphicsEvn.getAllFonts()
                for (i in availableFonts.indices) {
                    fonts.appendEL(availableFonts[i])
                }
            }
            return if (!duplicate) fonts else Duplicator.duplicate(fonts, false)
        }
    }

    fun toString(font: Font?): String? {
        return if (font == null) null else font.getFontName()
    }

    fun getFont(font: String?, defaultValue: Font?): Font? {
        var f: Font? = Font.decode(font)
        if (f != null) return f
        // font name
        var it: Iterator<Object?> = getAvailableFonts(false).valueIterator()
        while (it.hasNext()) {
            f = it.next() as Font?
            if (f.getFontName().equalsIgnoreCase(font)) return f
        }
        // family
        it = getAvailableFonts(false).valueIterator()
        while (it.hasNext()) {
            f = it.next() as Font?
            if (f.getFamily().equalsIgnoreCase(font)) return f
        }
        return defaultValue
    }

    @Throws(ExpressionException::class)
    fun getFont(font: String): Font {
        val f: Font? = getFont(font, null)
        if (f != null) return f
        throw ExpressionException("no font with name [$font] available", "to get available fonts call function ImageFonts()")
    }

    fun getFontMetrics(font: Font?): FontMetrics {
        if (graphics == null) {
            graphics = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics()
        }
        return graphics.getFontMetrics(font)
    }
}