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
package lucee.runtime.type.scope.storage.clean

import java.io.IOException

class FileStorageScopeCleaner(type: Int, listener: StorageScopeListener?) : StorageScopeCleanerSupport(type, listener, INTERVALL_DAY) {
    @Override
    override fun init(engine: StorageScopeEngine?) {
        super.init(engine)
    }

    @Override
    protected override fun _clean() {
        val cwi: ConfigWebPro = engine.getFactory().getConfig() as ConfigWebPro
        val dir: Resource = if (type === Scope.SCOPE_CLIENT) cwi.getClientScopeDir() else cwi.getSessionScopeDir()

        // for old files only the definition from admin can be used
        val timeout: Long = if (type === Scope.SCOPE_CLIENT) cwi.getClientTimeout().getMillis() else cwi.getSessionTimeout().getMillis()
        val time: Long = DateTimeImpl(cwi).getTime() - timeout
        try {
            // delete files that has expired
            val andFilter = AndResourceFilter(arrayOf(EXT_FILTER, ExpiresFilter(time, true)))
            var appName: String
            var cfid2: String
            var cfid: String
            val apps: Array<Resource?> = dir.listResources(DIR_FILTER)
            var cfidDir: Array<Resource?>
            var files: Array<Resource?>
            if (apps != null) for (a in apps.indices) {
                appName = StorageScopeImpl.decode(apps[a].getName())
                cfidDir = apps[a].listResources(DIR_FILTER)
                if (cfidDir != null) for (b in cfidDir.indices) {
                    cfid2 = cfidDir[b].getName()
                    files = cfidDir[b].listResources(andFilter)
                    if (files != null) {
                        for (c in files.indices) {
                            cfid = files[c].getName()
                            cfid = cfid2 + cfid.substring(0, cfid.length() - 5)
                            if (listener != null) listener.doEnd(engine, this, appName, cfid)

                            // info("remove from memory "+appName+"/"+cfid);
                            engine.remove(type, appName, cfid)
                            info("remove file " + files[c])
                            files[c].delete()
                        }
                    }
                }
            }
            ResourceUtil.deleteEmptyFolders(dir)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            error(t)
        }

        // long maxSize = type==Scope.SCOPE_CLIENT?cwi.getClientScopeDirSize():cwi.getSessionScopeDirSize();
        // checkSize(config,dir,maxSize,extfilter);
    }

    internal class ExpiresFilter(private val time: Long, private val allowDir: Boolean) : ResourceFilter {
        @Override
        fun accept(res: Resource?): Boolean {
            if (res.isDirectory()) return allowDir

            // load content
            var str: String? = null
            str = try {
                IOUtil.toString(res, "UTF-8")
            } catch (e: IOException) {
                return false
            }
            val index: Int = str.indexOf(':')
            if (index != -1) {
                val expires: Long = Caster.toLongValue(str.substring(0, index), -1L)
                // check is for backward compatibility, old files have no expires date inside. they do ot expire
                if (expires != -1L) {
                    if (expires < System.currentTimeMillis()) {
                        return true
                    }
                    str = str.substring(index + 1)
                    return false
                }
            } else if (res.lastModified() <= time) {
                return true
            }
            return false
        }
    }

    companion object {
        private val DIR_FILTER: ResourceFilter? = DirectoryResourceFilter()
        private val EXT_FILTER: ExtensionResourceFilter? = ExtensionResourceFilter(".scpt", true)
    }
}