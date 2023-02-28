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
package tachyon.runtime.thread

import kotlin.Throws
import tachyon.loader.util.Util
import kotlin.jvm.Synchronized
import kotlin.jvm.Transient
import org.osgi.framework.Version

abstract class ChildThread : Thread(group, null, "cfthread-" + if (count < 0) 0.also { count = it } else count++) {
    abstract fun getTagName(): String?

    // public PageContext getParent();
    abstract fun getStartTime(): Long

    /**
     * this method is invoked when thread is terminated by user interaction
     */
    abstract fun terminated()

    companion object {
        private val group: ThreadGroup = ThreadGroup("cfthread")
        private var count = 0
    }
}