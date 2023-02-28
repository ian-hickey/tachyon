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
package coldfusion.server

import java.awt.Image

interface DocumentService : Service {
    fun registerFontFile(arg0: String?): Boolean
    fun registerFontDirectory(arg0: String?): Boolean
    fun FontDiscovery()
    fun isFontPathRegistered(arg0: String?): Boolean
    fun isFontPathRegisteredAsUserFont(arg0: String?): Boolean
    fun getAvailableFontsForPDF(): Map?
    fun getAvailableFontsForJDK(): Map?
    fun getAvailableFontFamiles(): Map?
    fun getConfigMap(): Map?
    fun getUserConfigMap(): Map?
    fun getFontInfoFromFile(arg0: String?): Map?
    fun isCommonFont(arg0: String?): Boolean
    fun getAwtFontMapper(): Properties?
    fun getAwtFontMapperBak(): Properties?
    fun getWmimagefile(): File?
    fun getWmimage(): Image?
}