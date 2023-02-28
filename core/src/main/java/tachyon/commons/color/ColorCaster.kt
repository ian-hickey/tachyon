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
package tachyon.commons.color

import java.awt.Color

object ColorCaster {
    /**
     * calculate the contrast between 2 colors
     *
     * @param left
     * @param right
     * @return an int between 0 (badest) and 510 (best)
     * @throws ServletException
     */
    @Throws(ServletException::class)
    fun contrast(left: Color, right: Color): Int {
        return (Math.max(left.getRed(), right.getRed()) - Math.min(left.getRed(), right.getRed())
                + (Math.max(left.getGreen(), right.getGreen()) - Math.min(left.getGreen(), right.getGreen()))
                + (Math.max(left.getBlue(), right.getBlue()) - Math.max(left.getBlue(), right.getBlue())))
    }

    @Throws(ExpressionException::class)
    fun toColor(strColor: String): Color {
        var strColor = strColor
        if (StringUtil.isEmpty(strColor, true)) throw ExpressionException("can't cast empty string to a color Object")
        strColor = strColor.toLowerCase().trim()
        val first: Char = strColor.charAt(0)
        when (first) {
            'a' -> {
                if ("aqua".equals(strColor)) return Color(0, 0xFF, 0xFF)
                if ("aliceblue".equals(strColor)) return Color(0xF0, 0xF8, 0xFF)
                if ("antiquewhite".equals(strColor)) return Color(0xFA, 0xEB, 0xD7)
                if ("quamarine".equals(strColor)) return Color(0x7F, 0xFF, 0xD4)
                if ("azure".equals(strColor)) return Color(0xF0, 0xFF, 0xFF)
            }
            'b' -> {
                if ("black".equals(strColor)) return Color.BLACK
                if ("blue".equals(strColor)) return Color.BLUE
                if ("blue".equals(strColor)) return Color.CYAN
                if ("beige".equals(strColor)) return Color(0xF5, 0xF5, 0xDC)
                if ("blueviolet".equals(strColor)) return Color(0x8A, 0x2B, 0xE2)
                if ("brown".equals(strColor)) return Color(0xA5, 0x2A, 0x2A)
                if ("burlywood".equals(strColor)) return Color(0xDE, 0xB8, 0x87)
            }
            'c' -> {
                if ("cyan".equals(strColor)) return Color.CYAN
                if ("cadetblue".equals(strColor)) return Color(0x5F, 0x9E, 0xA0)
                if ("chartreuse".equals(strColor)) return Color(0x7F, 0xFF, 0x00)
                if ("chocolate".equals(strColor)) return Color(0xD2, 0x69, 0x1E)
                if ("coral".equals(strColor)) return Color(0xFF, 0x7F, 0x50)
                if ("cornflowerblue".equals(strColor)) return Color(0x64, 0x95, 0xED)
                if ("cornsilk".equals(strColor)) return Color(0xFF, 0xF8, 0xDC)
                if ("crimson".equals(strColor)) return Color(0xDC, 0x14, 0x3C)
            }
            'd' -> {
                if ("darkgray".equals(strColor)) return Color.DARK_GRAY
                if ("darkgrey".equals(strColor)) return Color.DARK_GRAY
                if ("darkblue".equals(strColor)) return Color(0x00, 0x00, 0x8B)
                if ("darkcyan".equals(strColor)) return Color(0x00, 0x8B, 0x8B)
                if ("darkgoldenrod".equals(strColor)) return Color(0xB8, 0x86, 0x0B)
                if ("darkgreen".equals(strColor)) return Color(0x00, 0x64, 0x00)
                if ("darkkhaki".equals(strColor)) return Color(0xBD, 0xB7, 0x6B)
                if ("darkmagenta".equals(strColor)) return Color(0x8B, 0x00, 0x8B)
                if ("darkolivegreen".equals(strColor)) return Color(0x55, 0x6B, 0x2F)
                if ("darkorange".equals(strColor)) return Color(0xFF, 0x8C, 0x00)
                if ("darkorchid".equals(strColor)) return Color(0x99, 0x32, 0xcc)
                if ("darkred".equals(strColor)) return Color(0x8B, 0x00, 0x00)
                if ("darksalmon".equals(strColor)) return Color(0xE9, 0x96, 0x7A)
                if ("darkseagreen".equals(strColor)) return Color(0x8F, 0xBC, 0x8F)
                if ("darkslateblue".equals(strColor)) return Color(0x2F, 0x4F, 0x4F)
                if ("darkslategray".equals(strColor)) return Color(0x48, 0x3D, 0x8B)
                if ("darkslategrey".equals(strColor)) return Color(0x48, 0x3D, 0x8B)
                if ("darkturquoise".equals(strColor)) return Color(0x00, 0xCE, 0xD1)
                if ("darkviolet".equals(strColor)) return Color(0x94, 0x00, 0xD3)
                if ("deeppink".equals(strColor)) return Color(0xFF, 0x14, 0x93)
                if ("deepskyblue".equals(strColor)) return Color(0x00, 0xBF, 0xFF)
                if ("dimgray".equals(strColor)) return Color(0x69, 0x69, 0x69)
                if ("dodgerblue".equals(strColor)) return Color(0x1E, 0x90, 0xFF)
            }
            'f' -> {
                if ("fuchsia".equals(strColor)) return Color(0xFF, 0, 0xFF)
                if ("firebrick".equals(strColor)) return Color(0xB2, 0x22, 0x22)
                if ("floralwhite".equals(strColor)) return Color(0xFF, 0xFA, 0xF0)
                if ("forestgreen".equals(strColor)) return Color(0x22, 0x8B, 0x22)
            }
            'g' -> {
                if ("gray".equals(strColor)) return Color.GRAY
                if ("grey".equals(strColor)) return Color.GRAY
                if ("green".equals(strColor)) return Color.GREEN
                if ("gainsboro".equals(strColor)) return Color(0xDC, 0xDC, 0xDC)
                if ("ghostwhite".equals(strColor)) return Color(0xF8, 0xF8, 0xFF)
                if ("gold".equals(strColor)) return Color(0xFF, 0xD7, 0x00)
                if ("goldenrod".equals(strColor)) return Color(0xDA, 0xA5, 0x20)
                if ("greenyellow".equals(strColor)) return Color(0xAD, 0xFF, 0x2F)
            }
            'h' -> {
                if ("honeydew".equals(strColor)) return Color(0xF0, 0xFF, 0xF0)
                if ("hotpink".equals(strColor)) return Color(0xFF, 0x69, 0xB4)
            }
            'i' -> {
                if ("indianred".equals(strColor)) return Color(0xCD, 0x5C, 0x5C)
                if ("indigo".equals(strColor)) return Color(0x4B, 0x00, 0x82)
                if ("ivory".equals(strColor)) return Color(0xFF, 0xFF, 0xF0)
            }
            'k' -> if ("khaki".equals(strColor)) return Color(0xF0, 0xE6, 0x8C)
            'l' -> {
                if ("lightgray".equals(strColor)) return Color.lightGray
                if ("lightgrey".equals(strColor)) return Color.lightGray
                if ("lime".equals(strColor)) return Color(0, 0xFF, 0)
                if ("lavender".equals(strColor)) return Color(0xE6, 0xE6, 0xFA)
                if ("lavenderblush".equals(strColor)) return Color(0xFF, 0xF0, 0xF5)
                if ("lawngreen".equals(strColor)) return Color(0x7C, 0xFC, 0x00)
                if ("lemonchiffon".equals(strColor)) return Color(0xFF, 0xFA, 0xCD)
                if ("lightblue".equals(strColor)) return Color(0xAD, 0xD8, 0xE6)
                if ("lightcoral".equals(strColor)) return Color(0xF0, 0x80, 0x80)
                if ("lightcyan".equals(strColor)) return Color(0xE0, 0xFF, 0xFF)
                if ("lightgoldenrodyellow".equals(strColor)) return Color(0xFA, 0xFA, 0xD2)
                if ("lightgreen".equals(strColor)) return Color(0x90, 0xEE, 0x90)
                if ("lightgrey".equals(strColor)) return Color(0xD3, 0xD3, 0xD3)
                if ("lightpink".equals(strColor)) return Color(0xFF, 0xB6, 0xC1)
                if ("lightsalmon".equals(strColor)) return Color(0xFF, 0xA0, 0x7A)
                if ("lightseagreen".equals(strColor)) return Color(0x20, 0xB2, 0xAA)
                if ("lightskyblue".equals(strColor)) return Color(0x87, 0xCE, 0xFA)
                if ("lightslategray".equals(strColor)) return Color(0x77, 0x88, 0x99)
                if ("lightslategrey".equals(strColor)) return Color(0x77, 0x88, 0x99)
                if ("lightsteelblue".equals(strColor)) return Color(0xB0, 0xC4, 0xDE)
                if ("lightyellow".equals(strColor)) return Color(0xFF, 0xFF, 0xE0)
                if ("limegreen".equals(strColor)) return Color(0x32, 0xCD, 0x32)
                if ("linen".equals(strColor)) return Color(0xFA, 0xF0, 0xE6)
            }
            'm' -> {
                if ("magenta".equals(strColor)) return Color.MAGENTA
                if ("maroon".equals(strColor)) return Color(0X80, 0, 0)
                if ("mediumaquamarine".equals(strColor)) return Color(0x66, 0xCD, 0xAA)
                if ("mediumblue".equals(strColor)) return Color(0x00, 0x00, 0xCD)
                if ("mediumorchid".equals(strColor)) return Color(0xBA, 0x55, 0xD3)
                if ("mediumpurple".equals(strColor)) return Color(0x93, 0x70, 0xDB)
                if ("mediumseagreen".equals(strColor)) return Color(0x3C, 0xB3, 0x71)
                if ("mediumslateblue".equals(strColor)) return Color(0x7B, 0x68, 0xEE)
                if ("mediumspringgreen".equals(strColor)) return Color(0x00, 0xFA, 0x9A)
                if ("mediumturquoise".equals(strColor)) return Color(0x48, 0xD1, 0xCC)
                if ("mediumvioletred".equals(strColor)) return Color(0xC7, 0x15, 0x85)
                if ("midnightblue".equals(strColor)) return Color(0x19, 0x19, 0x70)
                if ("mintcream".equals(strColor)) return Color(0xF5, 0xFF, 0xFA)
                if ("mistyrose".equals(strColor)) return Color(0xFF, 0xE4, 0xE1)
                if ("moccasin".equals(strColor)) return Color(0xFF, 0xE4, 0xB5)
            }
            'n' -> {
                if ("navy".equals(strColor)) return Color(0, 0, 0X80)
                if ("navajowhite".equals(strColor)) return Color(0xFF, 0xDE, 0xAD)
            }
            'o' -> {
                if ("orange".equals(strColor)) return Color.ORANGE
                if ("olive".equals(strColor)) return Color(0X80, 0X80, 0)
                if ("oldlace".equals(strColor)) return Color(0xFD, 0xF5, 0xE6)
                if ("olivedrab".equals(strColor)) return Color(0x6B, 0x8E, 0x23)
                if ("orangered".equals(strColor)) return Color(0xFF, 0x45, 0x00)
                if ("orchid".equals(strColor)) return Color(0xDA, 0x70, 0xD6)
            }
            'p' -> {
                if ("pink".equals(strColor)) return Color.PINK
                if ("purple".equals(strColor)) return Color(0X80, 0, 0X80)
                if ("palegoldenrod".equals(strColor)) return Color(0xEE, 0xE8, 0xAA)
                if ("palegreen".equals(strColor)) return Color(0x98, 0xFB, 0x98)
                if ("paleturquoise".equals(strColor)) return Color(0xAF, 0xEE, 0xEE)
                if ("palevioletred".equals(strColor)) return Color(0xDB, 0x70, 0x93)
                if ("papayawhip".equals(strColor)) return Color(0xFF, 0xEF, 0xD5)
                if ("peachpuff".equals(strColor)) return Color(0xFF, 0xDA, 0xB9)
                if ("peru".equals(strColor)) return Color(0xCD, 0x85, 0x3F)
                if ("pink".equals(strColor)) return Color(0xFF, 0xC0, 0xCB)
                if ("plum".equals(strColor)) return Color(0xDD, 0xA0, 0xDD)
                if ("powderblue".equals(strColor)) return Color(0xB0, 0xE0, 0xE6)
            }
            'r' -> {
                if ("red".equals(strColor)) return Color.RED
                if ("rosybrown".equals(strColor)) return Color(0xBC, 0x8F, 0x8F)
                if ("royalblue".equals(strColor)) return Color(0x41, 0x69, 0xE1)
            }
            's' -> {
                if ("silver".equals(strColor)) return Color(0XC0, 0XC0, 0XC0)
                if ("saddlebrown".equals(strColor)) return Color(0x8B, 0x45, 0x13)
                if ("salmon".equals(strColor)) return Color(0xFA, 0x80, 0x72)
                if ("sandybrown".equals(strColor)) return Color(0xF4, 0xA4, 0x60)
                if ("seagreen".equals(strColor)) return Color(0x2E, 0x8B, 0x57)
                if ("seashell".equals(strColor)) return Color(0xFF, 0xF5, 0xEE)
                if ("sienna".equals(strColor)) return Color(0xA0, 0x52, 0x2D)
                if ("skyblue".equals(strColor)) return Color(0x87, 0xCE, 0xEB)
                if ("slateblue".equals(strColor)) return Color(0x6A, 0x5A, 0xCD)
                if ("slategray".equals(strColor)) return Color(0x70, 0x80, 0x90)
                if ("slategrey".equals(strColor)) return Color(0x70, 0x80, 0x90)
                if ("snow".equals(strColor)) return Color(0xFF, 0xFA, 0xFA)
                if ("springgreen".equals(strColor)) return Color(0x00, 0xFF, 0x7F)
                if ("steelblue".equals(strColor)) return Color(0x46, 0x82, 0xB4)
            }
            't' -> {
                if ("teal".equals(strColor)) return Color(0, 0X80, 0X80)
                if ("tan".equals(strColor)) return Color(0xD2, 0xB4, 0x8C)
                if ("thistle".equals(strColor)) return Color(0xD8, 0xBF, 0xD8)
                if ("tomato".equals(strColor)) return Color(0xFF, 0x63, 0x47)
                if ("turquoise".equals(strColor)) return Color(0x40, 0xE0, 0xD0)
            }
            'v' -> if ("violet".equals(strColor)) return Color(0xEE, 0x82, 0xEE)
            'w' -> {
                if ("white".equals(strColor)) return Color.WHITE
                if ("wheat".equals(strColor)) return Color(0xF5, 0xDE, 0xB3)
                if ("whitesmoke".equals(strColor)) return Color(0xF5, 0xF5, 0xF5)
            }
            'y' -> {
                if ("yellow".equals(strColor)) return Color.YELLOW
                if ("yellowgreen".equals(strColor)) return Color(0x9A, 0xCD, 0x32)
            }
        }
        if (first == '#') {
            val strColor2: String = strColor.substring(1)
            // #fff
            if (strColor2.length() === 3) {
                val c1: Char = strColor2.charAt(0)
                val c2: Char = strColor2.charAt(1)
                val c3: Char = strColor2.charAt(2)
                return Color(NumberUtil.hexToInt("" + c1 + c1), NumberUtil.hexToInt("" + c2 + c2), NumberUtil.hexToInt("" + c3 + c3))
            }
            // #ffffff
            if (strColor2.length() === 6) {
                val s1: String = strColor2.substring(0, 2)
                val s2: String = strColor2.substring(2, 4)
                val s3: String = strColor2.substring(4, 6)
                return Color(NumberUtil.hexToInt(s1), NumberUtil.hexToInt(s2), NumberUtil.hexToInt(s3))
            }
            // #ffffffff
            if (strColor2.length() === 8) {
                val s1: String = strColor2.substring(0, 2)
                val s2: String = strColor2.substring(2, 4)
                val s3: String = strColor2.substring(4, 6)
                val s4: String = strColor2.substring(6, 8)
                return Color(NumberUtil.hexToInt(s1), NumberUtil.hexToInt(s2), NumberUtil.hexToInt(s3), NumberUtil.hexToInt(s4))
            }
        }

        // rgb(255,0,0)
        if (strColor.startsWith("rgb(") && strColor.endsWith(")")) {
            val strColor2: String = strColor.substring(4, strColor.length() - 1).trim()
            val arr: Array<String> = ListUtil.listToStringArray(strColor2, ',')
            if (arr.size == 3) {
                val i1: Int = Caster.toIntValue(arr[0])
                val i2: Int = Caster.toIntValue(arr[1])
                val i3: Int = Caster.toIntValue(arr[2])
                return Color(i1, i2, i3)
            }
        }

        // fff
        if (strColor.length() === 3) {
            val c1: Char = strColor.charAt(0)
            val c2: Char = strColor.charAt(1)
            val c3: Char = strColor.charAt(2)
            val i1: Int = NumberUtil.hexToInt("" + c1 + c1, -1)
            val i2: Int = NumberUtil.hexToInt("" + c2 + c2, -1)
            val i3: Int = NumberUtil.hexToInt("" + c3 + c3, -1)
            if (i1 != -1 && i2 != -1 && i3 != -1) return Color(i1, i2, i3)
        } else if (strColor.length() === 6) {
            val s1: String = strColor.substring(0, 2)
            val s2: String = strColor.substring(2, 4)
            val s3: String = strColor.substring(4, 6)
            val i1: Int = NumberUtil.hexToInt(s1, -1)
            val i2: Int = NumberUtil.hexToInt(s2, -1)
            val i3: Int = NumberUtil.hexToInt(s3, -1)
            if (i1 != -1 && i2 != -1 && i3 != -1) return Color(i1, i2, i3)
        } else if (strColor.length() === 8) {
            val s1: String = strColor.substring(0, 2)
            val s2: String = strColor.substring(2, 4)
            val s3: String = strColor.substring(4, 6)
            val s4: String = strColor.substring(6, 8)
            val i1: Int = NumberUtil.hexToInt(s1, -1)
            val i2: Int = NumberUtil.hexToInt(s2, -1)
            val i3: Int = NumberUtil.hexToInt(s3, -1)
            val i4: Int = NumberUtil.hexToInt(s4, -1)
            if (i1 != -1 && i2 != -1 && i3 != -1 && i4 != -1) return Color(i1, i2, i3, i4)
        }

        // 255,0,0
        val arr: Array<String> = ListUtil.listToStringArray(strColor, ',')
        if (arr.size == 3) {
            val i1: Int = Caster.toIntValue(arr[0], -1)
            val i2: Int = Caster.toIntValue(arr[1], -1)
            val i3: Int = Caster.toIntValue(arr[2], -1)
            if (i1 > -1 && i2 > -1 && i3 > -1) return Color(i1, i2, i3)
        }
        throw ExpressionException("invalid color definition [$strColor]",
                "color must be a know constant label (blue,green,yellow ...), a hexadecimal value (#ffffff) or a RGB value (rgb(255,255,255)), 255,255,255")
    }

    fun toHexString(color: Color): String {
        return "#" + toHexString(color.getRed()) + toHexString(color.getGreen()) + toHexString(color.getBlue())
    }

    private fun toHexString(clr: Int): String {
        val str: String = Integer.toHexString(clr)
        return if (str.length() === 1) "0$str" else str
    }
}