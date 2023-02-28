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
package lucee.servlet.pic

import java.io.FileNotFoundException

/**
 * Die Klasse PicServlet wird verwendet um Bilder darzustellen, alle Bilder die innerhalb des
 * Deployer angezeigt werden, werden ueber diese Klasse aus der lucee.jar Datei geladen, das macht
 * die Applikation flexibler und verlangt nicht das die Bilder fuer die Applikation an einem
 * bestimmten Ort abgelegt sein muessen.
 */
class PicServlet : HttpServlet() {
    /**
     * Interpretiert den Script-Name und laedt das entsprechende Bild aus den internen Resourcen.
     *
     * @see javax.servlet.http.HttpServlet.service
     */
    @Override
    @Throws(ServletException::class, IOException::class)
    protected fun service(req: HttpServletRequest?, rsp: HttpServletResponse?) {
        // get out Stream

        // pic
        val arrPath: Array<String?> = req.getServletPath().split("\\.")
        var pic = PIC_SOURCE.toString() + "404.gif"
        if (arrPath.size >= 3) {
            pic = PIC_SOURCE + (arrPath[arrPath.size - 3].toString() + "." + arrPath[arrPath.size - 2]).replaceFirst("/", "")

            // mime type
            val mime = "image/" + arrPath[arrPath.size - 2]
            ReqRspUtil.setContentType(rsp, mime)
        }

        // write data from pic input to response output
        var os: OutputStream? = null
        var `is`: InputStream? = null
        try {
            os = rsp.getOutputStream()
            `is` = getClass().getResourceAsStream(pic)
            if (`is` == null) {
                `is` = getClass().getResourceAsStream(PIC_SOURCE.toString() + "404.gif")
            }
            val buf = ByteArray(4 * 1024)
            var nread = 0
            while (`is`.read(buf).also { nread = it } >= 0) {
                os.write(buf, 0, nread)
            }
        } catch (e: FileNotFoundException) {
        } catch (e: IOException) {
        } finally {
            IOUtil.close(`is`, os)
        }
    }

    companion object {
        /**
         * Verzeichnis in welchem die bilder liegen
         */
        val PIC_SOURCE: String? = "/resource/img/"
    }
}