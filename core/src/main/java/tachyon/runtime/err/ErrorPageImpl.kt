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
package tachyon.runtime.err

import tachyon.runtime.PageSource

class ErrorPageImpl : ErrorPage {
    /** Type of exception. Required if type = "exception" or "monitor".  */
    @get:Override
    @set:Override
    var exception: String? = "any"

    /** The relative path to the custom error page.  */
    @get:Override
    @set:Override
    var template: PageSource? = null

    /**
     * The e-mail address of the administrator to notify of the error. The value is available to your
     * custom error page in the MailTo property of the error object.
     */
    @get:Override
    @set:Override
    var mailto: String? = ""

    @get:Override
    @set:Override
    var type: Short = 0

    @get:Override
    @set:Override
    var typeAsString: String?
        get() = exception
        set(exception) {
            exception = exception
        }
}