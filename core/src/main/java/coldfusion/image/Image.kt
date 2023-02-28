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
package coldfusion.image

import java.awt.Color

interface Image {
    fun addBorder(arg0: Int, arg1: String?, arg2: String?)
    fun blur(blurRadius: Int)
    fun brighten()
    fun clearRect(x: Int, y: Int, width: Int, height: Int)
    fun copyArea(srcX: Int, srcY: Int, width: Int, height: Int, destX: Int, destY: Int): Image?
    fun copyArea(srcX: Int, srcY: Int, width: Int, height: Int): Image?
    fun crop(x: Float, y: Float, width: Float, height: Float)
    fun draw3DRect(x: Int, y: Int, width: Int, height: Int, raised: Boolean, filled: Boolean)
    fun drawArc(x: Int, y: Int, width: Int, height: Int, startAngle: Int, arcAngle: Int, filled: Boolean)
    fun drawCubicCurve(x1: Double, y1: Double, ctrlx1: Double, ctrly1: Double, ctrlx2: Double, ctrly2: Double, x2: Double, y2: Double)
    fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int)
    fun drawLines(xcoords: IntArray?, ycoords: IntArray?, isPolygon: Boolean, filled: Boolean)
    fun drawOval(x: Int, y: Int, width: Int, height: Int, filled: Boolean)
    fun drawPoint(x: Int, y: Int)
    fun drawQuadraticCurve(x1: Double, y1: Double, ctrlx: Double, ctrly: Double, x2: Double, y2: Double)
    fun drawRect(x: Int, y: Int, width: Int, height: Int, filled: Boolean)
    fun drawRoundRect(x: Int, y: Int, width: Int, height: Int, arcWidth: Int, arcHeight: Int, filled: Boolean)
    fun drawString(arg0: String?, arg1: Int, arg2: Int, arg3: Struct?)
    fun flip(transpose: String?)
    fun getBase64String(formatName: String?): String?
    fun getColor(strColor: String?): Color?
    fun getCurrentGraphics(): Graphics2D?
    fun getCurrentImage(): BufferedImage?
    fun getExifMetadata(arg0: PageContext?): Struct?
    fun getExifTag(tagname: String?, pageContext: PageContext?): String?
    fun getHeight(): Int
    fun getImageBytes(arg0: String?): ByteArray?
    fun getIptcMetadata(arg0: PageContext?): Struct?
    fun getIptcTag(tagname: String?, pageContext: PageContext?): String?
    fun getSource(): String?
    fun getWidth(): Int
    fun grayscale()
    fun info(): Struct?
    fun initializeMetadata(pc: PageContext?)
    fun invert()
    fun overlay(img: Image?)
    fun paste(img2: Image?, x: Int, y: Int)
    fun readBase64(arg0: String?)
    fun resize(arg0: String?, arg1: String?, arg2: String?, arg3: Double)
    fun resize(width: String?, height: String?, interpolation: String?)
    fun rotate(arg0: Float, arg1: Float, arg2: Float, arg3: String?)
    fun rotateAxis(theta: Double, x: Double, y: Double)
    fun rotateAxis(theta: Double)
    fun scaleToFit(fitSize: Int)
    fun scaleToFit(arg0: String?, arg1: String?, arg2: String?, arg3: Double)
    fun scaleToFit(fitWidth: String?, fitHeight: String?, interpolation: String?)
    fun setAntiAliasing(value: String?)
    fun setBackground(color: String?)
    fun setColor(color: String?)
    fun setDrawingStroke(width: Float, cap: Int, joins: Int, miterlimit: Float, dash: FloatArray?, dash_phase: Float)
    fun setDrawingStroke(arg0: Struct?)
    fun setRenderingHint(hintKey: Key?, hintValue: Object?)
    fun setTranparency(percent: Double)
    fun setXorMode(color: String?)
    fun sharpen(gain: Float)
    fun sharpenEdge()
    fun shear(arg0: Float, arg1: String?, arg2: String?)
    fun shearAxis(shx: Double, shy: Double)
    fun translate(arg0: Int, arg1: Int, arg2: String?)
    fun translateAxis(x: Int, y: Int)
    fun write(arg0: String?, arg1: Float)
    fun writeBase64(arg0: String?, arg1: String?, arg2: Boolean)
}