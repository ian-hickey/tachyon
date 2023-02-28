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
package lucee.runtime.net.smtp

import java.io.Serializable

class Attachment : Serializable {
    /*
	 * *
	 * 
	 * @return the resource / public Resource getResourcex() { return resource; }
	 */  var absolutePath: String? = null
        private set
    private var url: URL? = null

    /**
     * @return the type
     */
    var type: String?
        private set

    /**
     * @return the disposition
     */
    var disposition: String? = null
        private set

    /**
     * @return the contentID
     */
    var contentID: String? = null
        private set
    /**
     * @return the fileName
     */
    /**
     * @param fileName the fileName to set
     */
    var fileName: String?
    /**
     * @return the removeAfterSend
     */
    /**
     * @param removeAfterSend the removeAfterSend to set
     */
    var isRemoveAfterSend = false

    constructor(resource: Resource?, fileName: String?, type: String?, disposition: String?, contentID: String?, removeAfterSend: Boolean) {
        var type = type
        absolutePath = resource.getAbsolutePath() // do not store resource, this is pehrhaps not serialiable
        this.fileName = if (StringUtil.isEmpty(fileName, true)) resource.getName() else fileName.trim()
        isRemoveAfterSend = removeAfterSend
        this.disposition = disposition
        this.contentID = contentID

        // type
        this.type = type
        if (StringUtil.isEmpty(type)) {
            type = IOUtil.getMimeType(resource, null)
        }
    }

    constructor(url: URL?) {
        this.url = url

        // filename
        fileName = ListUtil.last(url.toExternalForm(), '/')
        if (StringUtil.isEmpty(fileName)) fileName = "url.txt"
        type = IOUtil.getMimeType(url, null)
    }

    /**
     * @return the url
     */
    val uRL: URL?
        get() = url
    // resource.getAbsolutePath()
}