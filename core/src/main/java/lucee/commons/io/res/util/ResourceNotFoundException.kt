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
package lucee.commons.io.res.util

import java.io.IOException

class ResourceNotFoundException : IOException {
    /**
     * Constructs a `FileNotFoundException` with `null` as its error detail
     * message.
     */
    constructor() : super() {}

    /**
     * Constructs a `FileNotFoundException` with the specified detail message. The string
     * `s` can be retrieved later by the `[java.lang.Throwable.getMessage]`
     * method of class `java.lang.Throwable`.
     *
     * @param s the detail message.
     */
    constructor(s: String?) : super(s) {}

    /**
     * Constructs a `FileNotFoundException` with a detail message consisting of the given
     * pathname string followed by the given reason string. If the `reason` argument is
     * `null` then it will be omitted. This private constructor is invoked only by native I/O
     * methods.
     *
     *
     */
    constructor(path: String, reason: String?) : super(path + if (reason == null) "" else " ($reason)") {}
}