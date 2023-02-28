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
package lucee.runtime.tag

import java.util.ArrayList

// MUST change behavior of multiple headers now is an array, it das so?
/**
 * Lets you execute HTTP POST and GET operations on files. Using cfhttp, you can execute standard
 * GET operations and create a query object from a text file. POST operations lets you upload MIME
 * file types to a server, or post cookie, formfield, URL, file, or CGI variables directly to a
 * specified server.
 *
 *
 *
 *
 */
class ThreadTag : BodyTagImpl(), DynamicAttributes {
    private var action = ACTION_RUN
    private var duration: Long = -1
    private var _name: Collection.Key? = null
    private var priority: Int = Thread.NORM_PRIORITY
    private var timeout: Long = 0
    private var pc: PageContext? = null
    private var type = TYPE_DAEMON
    private var plans: Array<ExecutionPlan?>? = EXECUTION_PLAN
    private var attrs: Struct? = null
    @Override
    fun release() {
        super.release()
        action = ACTION_RUN
        duration = -1
        _name = null
        priority = Thread.NORM_PRIORITY
        type = TYPE_DAEMON
        plans = EXECUTION_PLAN
        timeout = 0
        attrs = null
        pc = null
    }

    /**
     * @param action the action to set
     */
    @Throws(ApplicationException::class)
    fun setAction(strAction: String?) {
        val lcAction: String = strAction.trim().toLowerCase()
        if ("join".equals(lcAction)) action = ACTION_JOIN else if ("run".equals(lcAction)) action = ACTION_RUN else if ("sleep".equals(lcAction)) action = ACTION_SLEEP else if ("terminate".equals(lcAction)) action = ACTION_TERMINATE else throw ApplicationException("invalid value [$strAction] for attribute action", "values for attribute action are:join,run,sleep,terminate")
    }

    /**
     * @param duration the duration to set
     */
    fun setDuration(duration: Double) {
        this.duration = duration.toLong()
    }

    /**
     * @param name the name to set
     */
    fun setName(name: String?) {
        if (StringUtil.isEmpty(name, true)) return
        _name = KeyImpl.init(name)
    }

    private fun name(create: Boolean): Collection.Key? {
        if (_name == null && create) _name = KeyImpl.init("thread" + RandomUtil.createRandomStringLC(20))
        return _name
    }

    private fun nameAsString(create: Boolean): String? {
        name(create)
        return if (_name == null) null else _name.getString()
    }

    /**
     * @param strPriority the priority to set
     */
    @Throws(ApplicationException::class)
    fun setPriority(strPriority: String?) {
        val p: Int = ThreadUtil.toIntPriority(strPriority)
        if (p == -1) {
            throw ApplicationException("invalid value [$strPriority] for attribute priority", "values for attribute priority are:low,high,normal")
        }
        priority = p
    }

    /**
     * @param strType the type to set
     * @throws ApplicationException
     * @throws SecurityException
     */
    @Throws(ApplicationException::class, SecurityException::class)
    fun setType(strType: String?) {
        var strType = strType
        strType = strType.trim().toLowerCase()
        type = if ("task".equals(strType)) {
            // SNSN
            /*
			 * SerialNumber sn = pageContext.getConfig().getSerialNumber();
			 * if(sn.getVersion()==SerialNumber.VERSION_COMMUNITY) throw new
			 * SecurityException("no access to this functionality with the "+sn.getStringVersion()
			 * +" version of Lucee");
			 */

            // throw new ApplicationException("invalid value ["+strType+"] for attribute type","task is not
            // supported at the moment");
            TYPE_TASK
        } else if ("daemon".equals(strType)) {
            TYPE_DAEMON
        } else {
            throw ApplicationException("invalid value [$strType] for attribute type", "values for attribute type are:task,daemon (default)")
        }
    }

    @Throws(PageException::class)
    fun setRetryintervall(obj: Object?) {
        setRetryinterval(obj)
    }

    @Throws(PageException::class)
    fun setRetryinterval(obj: Object?) {
        if (StringUtil.isEmpty(obj)) return
        val arr: Array = Caster.toArray(obj, null)
        if (arr == null) {
            plans = arrayOf<ExecutionPlan?>(toExecutionPlan(obj, 1))
        } else {
            val it: Iterator<Object?> = arr.valueIterator()
            plans = arrayOfNulls<ExecutionPlan?>(arr.size())
            var index = 0
            while (it.hasNext()) {
                plans!![index++] = toExecutionPlan(it.next(), if (index == 1) 1 else 0)
            }
        }
    }

    @Throws(PageException::class)
    private fun toExecutionPlan(obj: Object?, plus: Int): ExecutionPlan? {
        if (obj is Struct) {
            val sct: Struct? = obj as Struct?
            // GERT

            // tries
            val oTries: Object = sct.get(KeyConstants._tries, null)
                    ?: throw ExpressionException("missing key tries inside struct")
            val tries: Int = Caster.toIntValue(oTries)
            if (tries < 0) throw ExpressionException("tries must contain a none negative value")

            // interval
            var oInterval: Object = sct.get(KeyConstants._interval, null)
            if (oInterval == null) oInterval = sct.get(KeyConstants._intervall, null)
            if (oInterval == null) throw ExpressionException("missing key interval inside struct")
            val interval = toSeconds(oInterval)
            if (interval < 0) throw ExpressionException("interval should contain a positive value or 0")
            return ExecutionPlanImpl(tries + plus, interval)
        }
        return ExecutionPlanImpl(1 + plus, toSeconds(obj))
    }

    @Throws(PageException::class)
    private fun toSeconds(obj: Object?): Int {
        return Caster.toTimespan(obj).getSeconds()
    }

    /**
     * @param timeout the timeout to set
     */
    fun setTimeout(timeout: Double) {
        this.timeout = timeout.toLong()
    }

    @Override
    fun setDynamicAttribute(uri: String?, name: String?, value: Object?) {
        if (attrs == null) attrs = StructImpl()
        val key: Key = KeyImpl.init(StringUtil.trim(name, ""))
        attrs.setEL(key, value)
    }

    @Override
    fun setDynamicAttribute(uri: String?, name: Collection.Key?, value: Object?) {
        if (attrs == null) attrs = StructImpl()
        val key: Key = KeyImpl.init(StringUtil.trim(name.getString(), ""))
        attrs.setEL(key, value)
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        pc = pageContext
        when (action) {
            ACTION_JOIN -> doJoin()
            ACTION_SLEEP -> {
                required("thread", "sleep", "duration", duration, -1)
                doSleep()
            }
            ACTION_TERMINATE -> {
                required("thread", "terminate", "name", nameAsString(false))
                doTerminate()
            }
            ACTION_RUN ->            // required("thread", "run", "name", name(true).getString());
                return EVAL_BODY_INCLUDE
        }
        return SKIP_BODY
    }

    @Override
    @Throws(PageException::class)
    fun doEndTag(): Int {
        pc = pageContext
        return EVAL_PAGE
    }

    @Throws(PageException::class)
    fun register(currentPage: Page?, threadIndex: Int) {
        if (ACTION_RUN != action) return
        val name: Key = name(true)
        try {
            val ts: Threads? = getThreadScope(pc, name) // pc.getThreadScope(name);
            if (type == TYPE_DAEMON) {
                if (ts != null) throw ApplicationException("could not create a thread with the name [" + name.getString().toString() + "]. name must be unique within a request")
                val ct = ChildThreadImpl(pc as PageContextImpl?, currentPage, name.getString(), threadIndex, attrs, false)
                val t = ThreadsImpl(ct)
                val root: PageContextImpl? = getRootPageContext(pc) as PageContextImpl?
                root.setAllThreadScope(name, t)
                pc.setThreadScope(name, t)
                ct.setPriority(priority)
                ct.setDaemon(false)
                ct.start()
            } else {
                val ct = ChildThreadImpl(pc as PageContextImpl?, currentPage, name.getString(), threadIndex, attrs, true)
                ct.setPriority(priority)
                ((pc.getConfig() as ConfigPro).getSpoolerEngine() as SpoolerEngineImpl).add(pc.getConfig(), ChildSpoolerTask(ct, plans))
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw Caster.toPageException(t)
        } finally {
            (pc as PageContextImpl?).reuse(this) // this method is not called from template when type is run, a call from template is to early,
        }
    }

    @Throws(ExpressionException::class)
    private fun doSleep() {
        if (duration >= 0) {
            SystemUtil.sleep(duration)
        } else throw ExpressionException("The attribute duration must be greater or equal than 0, now [$duration]")
    }

    @Throws(ApplicationException::class)
    private fun doJoin() {
        var all: List<String?>? = null
        val names: List<String?>?
        val name: Key = name(false)
        if (name == null) {
            names = getTagNames(getAllNoneAncestorThreads(pc))
            all = names
        } else names = ListUtil.listToList(name.getLowerString(), ',', true)
        var ct: ChildThread
        var ts: Threads?
        val start: Long = System.currentTimeMillis()
        var _timeout = if (timeout > 0) timeout else -1
        val it = names!!.iterator()
        var n: String?
        while (it.hasNext()) {
            n = it.next()
            if (StringUtil.isEmpty(n, true)) continue
            // PageContextImpl mpc=(PageContextImpl)getMainPageContext(pc);
            ts = getThreadScope(pc, KeyImpl.init(n)) // , ThreadTag.LEVEL_CURRENT + ThreadTag.LEVEL_KIDS
            if (ts == null) {
                if (all == null) all = getTagNames(getAllNoneAncestorThreads(pc))
                throw ApplicationException("there is no thread running with the name [" + n + "], " + "only the following threads existing [" + ListUtil.listToListEL(all, ", ")
                        + "] ->" + ListUtil.toList(all, ", "))
            }
            ct = ts.getChildThread()
            if (ct.isAlive()) {
                try {
                    if (_timeout != -1L) ct.join(_timeout) else ct.join()
                } catch (e: InterruptedException) {
                }
            }
            if (_timeout != -1L) {
                _timeout = _timeout - (System.currentTimeMillis() - start)
                if (_timeout < 1) break
            }
        }
    }

    @Throws(ApplicationException::class)
    private fun doTerminate() {
        // PageContextImpl mpc=(PageContextImpl)getMainPageContext(pc);
        val ts: Threads = getThreadScope(pc, KeyImpl.init(nameAsString(false)))
                ?: throw ApplicationException("there is no thread running with the name [" + nameAsString(false) + "]") // , ThreadTag.LEVEL_CURRENT + ThreadTag.LEVEL_KIDS
        val ct: ChildThread = ts.getChildThread()
        if (ct.isAlive()) {
            ct.terminated()
            SystemUtil.stop(ct)
        }
    }

    @Override
    fun doInitBody() {
    }

    @Override
    fun doAfterBody(): Int {
        return SKIP_BODY
    }

    /**
     * sets if has body or not
     *
     * @param hasBody
     */
    fun hasBody(hasBody: Boolean) {}

    companion object {
        private const val ACTION_JOIN = 0
        private const val ACTION_RUN = 1
        private const val ACTION_SLEEP = 2
        private const val ACTION_TERMINATE = 3
        private const val TYPE_DAEMON = 0
        private const val TYPE_TASK = 1
        const val LEVEL_KIDS = 1
        const val LEVEL_PARENTS = 2
        const val LEVEL_CURRENT = 4
        const val LEVEL_ALL = LEVEL_KIDS + LEVEL_PARENTS + LEVEL_CURRENT
        private val EXECUTION_PLAN: Array<ExecutionPlan?>? = arrayOfNulls<ExecutionPlan?>(0)
        private fun getRootPageContext(pc: PageContext?): PageContext? {
            val root: PageContext = (pc as PageContextImpl?).getRootPageContext()
            return if (root == null) pc else root
        }

        /*
	 * public static java.util.Collection<String> getThreadScopeNames(PageContext pc, boolean recurive)
	 * { return getThreadScopeNames(pc, recurive ? LEVEL_CURRENT + LEVEL_KIDS : LEVEL_CURRENT); }
	 */
        fun getTagNames(threads: Collection<Threads?>?): List<String?>? {
            val names: List<String?> = ArrayList()
            val it: Iterator<Threads?> = threads!!.iterator()
            var t: Threads?
            while (it.hasNext()) {
                t = it.next()
                // names.add("name:" + t.getChildThread().getName());
                names.add(t.getChildThread().getTagName())
            }
            return names
        }

        fun getAllNoneAncestorThreads(current: PageContext?): Collection<Threads?>? {
            // first we go to the root and collect all parents
            val ignores: List<String?> = ArrayList<String?>()
            val c: PageContextImpl? = current as PageContextImpl?
            var tn: String = c.getTagName()
            if (!StringUtil.isEmpty(tn)) ignores.add(tn.toLowerCase())
            val parents: List<String?> = c.getParentTagNames()
            if (parents != null) {
                val it = parents.iterator()
                while (it.hasNext()) {
                    tn = it.next()
                    if (!StringUtil.isEmpty(tn)) ignores.add(tn.toLowerCase())
                }
            }
            val root: PageContext? = getRootPageContext(current)

            // now we get all threads and filter out ancestors
            val result: MutableCollection<Threads?> = HashSet<Threads?>()
            val threads: Map<Key?, Threads?> = (root as PageContextImpl?).getAllThreadScope()
            if (threads != null) {
                val it: Iterator<Entry<Key?, Threads?>?> = threads.entrySet().iterator()
                var e: Entry<Key?, Threads?>?
                while (it.hasNext()) {
                    e = it.next()
                    if (ignores.contains(e.getKey().getLowerString())) continue
                    result.add(e.getValue())
                }
            }
            return result
        }

        fun getThreadScope(pc: PageContext?, name: Key?): Threads? {
            var t: Threads? = null

            // get from root
            val root: PageContext? = getRootPageContext(pc)
            val scopes: Map<Key?, Threads?> = (root as PageContextImpl?).getAllThreadScope()
            if (scopes != null) {
                t = scopes[name]
                if (t != null) return t
            }
            return null
        }
    }
}