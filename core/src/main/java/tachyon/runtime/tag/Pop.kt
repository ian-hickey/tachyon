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
package tachyon.runtime.tag

import tachyon.runtime.net.mail.MailClient

/**
 * Retrieves and deletes e-mail messages from a POP mail server.
 */
class Pop : _Mail() {
    @get:Override
    protected override val defaultPort: Int
        protected get() = if (isSecure()) 995 else 110

    @get:Override
    protected override val tagName: String?
        protected get() = "Pop"

    @get:Override
    protected override val type: Int
        protected get() = MailClient.TYPE_POP3
}