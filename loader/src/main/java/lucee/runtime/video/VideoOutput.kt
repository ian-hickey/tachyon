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
package lucee.runtime.video

import lucee.commons.io.res.Resource

interface VideoOutput {
    /**
     * limit size of the output file
     *
     * @param size the size to set
     */
    fun limitFileSizeTo(size: Int)
    /**
     * @return the res
     */
    /**
     * @param resource the resource to set
     */
    var resource: lucee.commons.io.res.Resource?
    /**
     * @return the offset
     */
    /**
     * set time offset of the output file based on input file in seconds
     *
     * @param offset offset
     */
    var offset: Double
    /**
     * @return the comment
     */
    /**
     * sets a comment to the output video
     *
     * @param comment comment
     */
    var comment: String?
    /**
     * @return the title
     */
    /**
     * sets a title to the output video
     *
     * @param title title
     */
    var title: String?
    /**
     * @return the author
     */
    /**
     * sets an author to the output video
     *
     * @param author author
     */
    var author: String?
    /**
     * @return the copyright
     */
    /**
     * sets a copyright to the output video
     *
     * @param copyright copyright
     */
    var copyright: String?
    /**
     * @return the fileLimitation
     */
    /**
     * @param fileLimitation the fileLimitation to set
     */
    var fileLimitation: Int
    /**
     * @return the maxFrames
     */
    /**
     * @param maxFrames the maxFrames to set
     */
    var maxFrames: Long
    /**
     * @return the format
     */
    /**
     * @param format the format to set
     */
    var format: String?
    var frameRate: Int
}