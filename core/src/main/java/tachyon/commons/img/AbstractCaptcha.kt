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
package tachyon.commons.img

import java.util.concurrent.ThreadLocalRandom

/**
 * Abstract template class for captcha generation
 */
abstract class AbstractCaptcha {
    /**
     * generates a Captcha as a Buffered Image file
     *
     * @param text text for the captcha
     * @param width width of the resulting image
     * @param height height of the resulting image
     * @param fonts list of font used for the captcha (all font are random used)
     * @param useAntiAlias use anti aliasing or not
     * @param fontColor color of the font
     * @param fontSize size of the font
     * @param difficulty difficulty of the reslting captcha
     * @return captcha image
     * @throws CaptchaException
     */
    @Throws(CaptchaException::class)
    fun generate(text: String?, width: Int, height: Int, fonts: Array<String>?, useAntiAlias: Boolean, fontColor: Color, fontSize: Int, difficulty: Int): BufferedImage {
        if (difficulty == DIFFICULTY_LOW) {
            return generate(text, width, height, fonts, useAntiAlias, fontColor, fontSize, 0, 0, 0, 0, 0, 0, 230, 25)
        }
        return if (difficulty == DIFFICULTY_MEDIUM) {
            generate(text, width, height, fonts, useAntiAlias, fontColor, fontSize, 0, 0, 5, 30, 0, 0, 200, 35)
        } else generate(text, width, height, fonts, useAntiAlias, fontColor, fontSize, 4, 10, 30, 60, 4, 10, 170, 45)
    }

    @Throws(CaptchaException::class)
    private fun generate(text: String?, width: Int, height: Int, fonts: Array<String>?, useAntiAlias: Boolean, fontColor: Color, fontSize: Int, minOvals: Int, maxOvals: Int,
                         minBGLines: Int, maxBGLines: Int, minFGLines: Int, maxFGLines: Int, startColor: Int, shear: Int): BufferedImage {
        if (text == null || text.trim().length() === 0) throw CaptchaException("missing Text")
        val characters: CharArray = text.toCharArray()
        var top = height / 3
        val dimension = Dimension(width, height)
        val imageType: Int = BufferedImage.TYPE_INT_RGB
        val bufferedImage = BufferedImage(dimension.getWidth() as Int, dimension.getHeight() as Int, imageType)
        val graphics: Graphics2D = bufferedImage.createGraphics()

        // Set anti-alias setting
        if (useAntiAlias) graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        drawBackground(graphics, dimension, startColor)

        // draw ovals
        if (maxOvals > 0 && maxOvals > minOvals) {
            val to = rnd(minOvals.toDouble(), maxOvals.toDouble())
            for (i in 1..to) {
                drawRandomOval(graphics, dimension, getRandomColor(startColor))
            }
        }

        // Draw background lines
        if (maxBGLines > 0 && maxBGLines > minBGLines) {
            val to = rnd(minBGLines.toDouble(), maxBGLines.toDouble())
            for (i in 1..to) {
                drawRandomLine(graphics, dimension, getRandomColor(startColor))
            }
        }
        if (fonts == null || fonts.size == 0) throw CaptchaException("no font's defined")

        // font
        var f: Font?
        val fontList: ArrayList<Font> = ArrayList<Font>()
        for (i in fonts.indices) {
            f = getFont(fonts[i], null)
            if (f != null) fontList.add(f)
        }
        if (fonts.size == 0) throw CaptchaException("defined fonts are not available on this system")
        var charWidth = 0
        var charHeight = 0
        var tmp: Int
        var space = 0
        val _fonts: Array<Font?> = arrayOfNulls<Font>(characters.size)
        for (i in characters.indices) {
            val c = characters[i]
            _fonts[i] = createFont(fontList, fontSize, shear, i)
            graphics.setFont(_fonts[i])
            charWidth += graphics.getFontMetrics().charWidth(c)
            tmp = graphics.getFontMetrics().getHeight()
            if (tmp > charHeight) charHeight = tmp
        }
        if (charWidth < width) {
            space = (width - charWidth) / (characters.size + 1)
        } else if (charWidth > width) throw CaptchaException("the specified width for the CAPTCHA image is not big enough to fit the text. Minimum width is [$charWidth]")
        if (charHeight > height) throw CaptchaException("the specified height for the CAPTCHA image is not big enough to fit the text. Minimum height is [$charHeight]")
        var left = space

        // Draw captcha text
        for (i in characters.indices) {
            val c = characters[i]
            // <cfset staticCollections.shuffle(definedFonts) />
            graphics.setFont(_fonts[i])
            graphics.setColor(fontColor)
            // Check if font can display current character --->
            /*
			 * <cfloop condition="NOT graphics.getFont().canDisplay(char)"> <cfset setFont(graphics,
			 * definedFonts) /> </cfloop>
			 */

            // Compute the top character position --->
            top = rnd(graphics.getFontMetrics().getAscent(), height - (height - graphics.getFontMetrics().getHeight()) / 2)

            // Draw character text
            graphics.drawString(String.valueOf(c), left, top)

            // Compute the next character lef tposition --->
            // ((rnd(150, 200) / 100) *
            left += graphics.getFontMetrics().charWidth(c) + rnd(space.toDouble(), space.toDouble())
        }

        // Draw forground lines
        if (maxFGLines > 0 && maxFGLines > minFGLines) {
            val to = rnd(minFGLines.toDouble(), maxFGLines.toDouble())
            for (i in 1..to) {
                drawRandomLine(graphics, dimension, getRandomColor(startColor))
            }
        }
        return bufferedImage
    }

    /**
     * creates a font from given string
     *
     * @param font
     * @param defaultValue
     * @return
     */
    abstract fun getFont(font: String?, defaultValue: Font?): Font?
    private fun drawBackground(graphics: Graphics2D, dimension: Dimension, _startColor: Int) {
        val startColor: Color = getRandomColor(_startColor)
        val endColor: Color = getRandomColor(_startColor)
        val gradientPaint = GradientPaint(getRandomPointOnBorder(dimension), startColor, getRandomPointOnBorder(dimension), endColor.brighter(), true)
        graphics.setPaint(gradientPaint)
        // arguments.graphics.setColor(startColor) />
        graphics.fill(Rectangle(dimension))
    }

    private fun createFont(fonts: List<Font>, fontSize: Int, shear: Int, index: Int): Font {
        val trans1: AffineTransform = getRandomTransformation(shear, shear)
        val trans2: AffineTransform = getRandomTransformation(shear, shear)
        var font: Font = fonts[index % fonts.size()]
        font = font.deriveFont(fontSize.toFloat()).deriveFont(trans1).deriveFont(trans2)
        return font
    }

    private fun getRandomColor(startColor: Int): Color {
        return Color(r(startColor), r(startColor), r(startColor))
    }

    private fun r(startColor: Int): Int {
        return rnd((startColor - 100).toDouble(), startColor.toDouble())
    }

    private fun getRandomPointOnBorder(dimension: Dimension): Point {
        val height = dimension.getHeight() as Int
        val width = dimension.getWidth() as Int
        return when (rnd(1.0, 4.0)) {
            1 -> Point(0, rnd(0.0, height.toDouble()))
            2 -> Point(width, rnd(0.0, height.toDouble()))
            3 -> Point(rnd(0.0, width.toDouble()), 0)
            4 -> Point(rnd(0.0, width.toDouble()), height)
            else -> Point(rnd(0.0, width.toDouble()), height)
        }
    }

    private fun getRandomTransformation(shearXRange: Int, shearYRange: Int): AffineTransform {
        // create a slightly random affine transform
        val shearX = rndd(-1 * (shearXRange * (rnd(50.0, 150.0) / 100.0)), shearXRange * (rndd(50.0, 150.0) / 100.0)) / 100.0
        val shearY = rndd(-1 * (shearYRange * (rnd(50.0, 150.0) / 100.0)), shearYRange * (rndd(50.0, 150.0) / 100.0)) / 100.0
        val transformation = AffineTransform()
        transformation.shear(shearX, shearY)
        return transformation
    }

    private val randomStroke: BasicStroke
        private get() = BasicStroke(rnd(1.0, 3.0))

    private fun getRandomPoint(dimension: Dimension): Point {
        val height = dimension.getHeight() as Int
        val width = dimension.getWidth() as Int
        return Point(rnd(0.0, width.toDouble()), rnd(0.0, height.toDouble()))
    }

    private fun drawRandomLine(graphics: Graphics2D, dimension: Dimension, lineColorType: Color) {
        val point1: Point = getRandomPointOnBorder(dimension)
        val point2: Point = getRandomPointOnBorder(dimension)
        graphics.setStroke(randomStroke)
        graphics.setColor(lineColorType)
        graphics.drawLine(point1.getX() as Int, point1.getY() as Int, point2.getX() as Int, point2.getY() as Int)
    }

    private fun drawRandomOval(graphics: Graphics2D, dimension: Dimension, ovalColorType: Color) {
        val point: Point = getRandomPoint(dimension)
        val height: Double = dimension.getHeight()
        // double width = dimension.getWidth() ;
        val minOval = height * .10
        val maxOval = height * .75
        graphics.setColor(ovalColorType)
        when (rnd(1.0, 3.0)) {
            1 -> {
                graphics.setStroke(randomStroke)
                graphics.drawOval(point.getX() as Int, point.getY() as Int, rnd(minOval, maxOval), rnd(minOval, maxOval))
            }
            2, 3 -> graphics.fillOval(point.getX() as Int, point.getY() as Int, rnd(minOval, maxOval), rnd(minOval, maxOval))
        }
    }

    companion object {
        const val DIFFICULTY_LOW = 0
        const val DIFFICULTY_MEDIUM = 1
        const val DIFFICULTY_HIGH = 2
        protected fun rnd(min: Double, max: Double): Int {
            return rndd(min, max).toInt()
        }

        private fun rndd(min: Double, max: Double): Double {
            var min = min
            var max = max
            if (min > max) {
                val tmp = min
                min = max
                max = tmp
            }
            return ThreadLocalRandom.current().doubles(1, min, max).toArray().get(0)
        }
    }
}