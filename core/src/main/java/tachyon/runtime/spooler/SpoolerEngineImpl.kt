/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.runtime.spooler

import java.io.IOException

class SpoolerEngineImpl(persisDirectory: Resource?, label: String?, log: Log?, maxThreads: Int) : SpoolerEngine {
    private var label: String?

    // private LinkedList<SpoolerTask> openTaskss=new LinkedList<SpoolerTask>();
    // private LinkedList<SpoolerTask> closedTasks=new LinkedList<SpoolerTask>();
    private var simpleThread: SimpleThread? = null
    private val token: SerializableObject? = SerializableObject()
    private var thread: SpoolerThread? = null

    // private ExecutionPlan[] plans;
    private var _persisDirectory: Resource?
    private var count: Long = 0
    private var log: Log?
    private var add = 0
    private val closedDirectory: Resource?
    private val openDirectory: Resource?
    private var maxThreads: Int
    private var init = false
    fun init(config: ConfigWeb?) {
        if (init) return
        if (getOpenTaskCount() > 0) start(config)
        init = true
    }

    /*
	 * private void calculateSize() { closedCount=calculateSize(closedDirectory);
	 * openCount=calculateSize(openDirectory); }
	 */
    fun setMaxThreads(maxThreads: Int) {
        this.maxThreads = maxThreads
    }

    /**
     * @return the maxThreads
     */
    fun getMaxThreads(): Int {
        return maxThreads
    }

    private fun calculateSize(res: Resource?): Int {
        return ResourceUtil.directrySize(res, FILTER)
    }

    @Override
    @Synchronized
    fun add(task: SpoolerTask?) {
        add(ConfigWebUtil.toConfigWeb(ThreadLocalPageContext.getConfig()), task)
    }

    @Synchronized
    fun add(config: ConfigWeb?, task: SpoolerTask?) {
        // if there is no plan execute and forget
        if (task.getPlans() == null) {
            if (task is Task) start(config, task as Task?) else {
                start(config, TaskWrap(task))
                // log.error("spooler", "make class " + task.getClass().getName() + " a Task class");
            }
            return
        }

        // openTasks.add(task);
        add++
        if (task.nextExecution() === 0) task.setNextExecution(System.currentTimeMillis())
        task.setId(createId(config, task))
        store(config, task)
        start(config)
    }

    // add to interface
    fun add(config: ConfigWeb?, task: Task?) {
        start(config, task)
    }

    private fun start(config: ConfigWeb?, task: Task?) {
        if (task == null) return
        synchronized(task) {
            if (simpleThread == null || !simpleThread.isAlive()) {
                simpleThread = SimpleThread(config, task)
                simpleThread.setPriority(Thread.MIN_PRIORITY)
                simpleThread.start()
            } else {
                simpleThread!!.tasks.add(task)
                simpleThread.interrupt()
            }
        }
    }

    fun start(config: ConfigWeb?) {
        if (thread == null || !thread.isAlive()) {
            thread = SpoolerThread(config, this)
            thread.setPriority(Thread.MIN_PRIORITY)
            thread.start()
        } else if (thread!!.sleeping) {
            thread.interrupt()
        }
    }

    @Override
    fun getLabel(): String? {
        return label
    }

    private fun getTaskById(dir: Resource?, id: String?): SpoolerTask? {
        return getTask(dir.getRealResource(id.toString() + ".tsk"), null)
    }

    private fun getTaskByName(dir: Resource?, name: String?): SpoolerTask? {
        return getTask(dir.getRealResource(name), null)
    }

    private fun getTask(res: Resource?, defaultValue: SpoolerTask?): SpoolerTask? {
        var `is`: InputStream? = null
        var ois: ObjectInputStream? = null
        var task: SpoolerTask? = defaultValue
        try {
            `is` = res.getInputStream()
            ois = ObjectInputStream(`is`)
            task = ois.readObject() as SpoolerTask
        } catch (e: Exception) {
            LogUtil.log(ThreadLocalPageContext.get(), SpoolerEngineImpl::class.java.getName(), e)
            IOUtil.closeEL(`is`)
            IOUtil.closeEL(ois)
            res.delete()
        }
        IOUtil.closeEL(`is`)
        IOUtil.closeEL(ois)
        return task
    }

    private fun store(config: ConfigWeb?, task: SpoolerTask?) {
        var oos: ObjectOutputStream? = null
        val persis: Resource? = getFile(config, task)
        if (persis.exists()) persis.delete()
        try {
            oos = ObjectOutputStream(persis.getOutputStream())
            oos.writeObject(task)
        } catch (e: IOException) {
            LogUtil.log(ThreadLocalPageContext.get(), SpoolerEngineImpl::class.java.getName(), e)
        } finally {
            try {
                IOUtil.close(oos)
            } catch (e: IOException) {
                LogUtil.log(ThreadLocalPageContext.get(), SpoolerEngineImpl::class.java.getName(), e)
            }
        }
    }

    private fun unstore(config: ConfigWeb?, task: SpoolerTask?) {
        val persis: Resource? = getFile(config, task)
        val exists: Boolean = persis.exists()
        if (exists) persis.delete()
    }

    private fun log(config: ConfigWeb?, task: SpoolerTask?, e: Exception?, before: Boolean) {
        if (task is SpoolerTaskPro) {
            val listener: SpoolerTaskListener = task!!.getListener()
            if (listener != null) listener.listen(config, e, before)
        }
        if (e == null) log.log(Log.LEVEL_INFO, "remote-client", "successfully executed: " + task.subject()) else log.log(Log.LEVEL_ERROR, "remote-client", "failed to execute: " + task.subject(), e)
    }

    private fun getFile(config: ConfigWeb?, task: SpoolerTask?): Resource? {
        val dir: Resource = getPersisDirectory(config).getRealResource(if (task.closed()) "closed" else "open")
        dir.mkdirs()
        return dir.getRealResource(task.getId().toString() + ".tsk")
    }

    private fun createId(config: ConfigWeb?, task: SpoolerTask?): String? {
        val dirClosed: Resource = getPersisDirectory(config).getRealResource("closed")
        val dirOpen: Resource = getPersisDirectory(config).getRealResource("open")
        if (task.closed()) dirClosed.mkdirs() else dirOpen.mkdirs()
        var id: String? = null
        do {
            id = StringUtil.addZeros(++count, 8)
        } while (dirOpen.getRealResource(id.toString() + ".tsk").exists() || dirClosed.getRealResource(id.toString() + ".tsk").exists())
        return id
    }

    fun calculateNextExecution(task: SpoolerTask?): Long {
        var _tries = 0
        var plan: ExecutionPlan? = null
        val plans: Array<ExecutionPlan?> = task.getPlans()
        for (i in plans.indices) {
            _tries += plans[i].getTries()
            if (_tries > task.tries()) {
                plan = plans[i]
                break
            }
        }
        return if (plan == null) -1 else task.lastExecution() + plan.getIntervall() * 1000
    }

    @Override
    @Throws(PageException::class)
    fun getOpenTasksAsQuery(startrow: Int, maxrow: Int): Query? {
        return getTasksAsQuery(createQuery(), openDirectory, startrow, maxrow)
    }

    @Override
    @Throws(PageException::class)
    fun getClosedTasksAsQuery(startrow: Int, maxrow: Int): Query? {
        return getTasksAsQuery(createQuery(), closedDirectory, startrow, maxrow)
    }

    @Override
    @Throws(PageException::class)
    fun getAllTasksAsQuery(startrow: Int, maxrow: Int): Query? {
        var startrow = startrow
        var maxrow = maxrow
        if (maxrow < 0) maxrow = Integer.MAX_VALUE
        val query: Query? = createQuery()
        // print.o(startrow+":"+maxrow);
        getTasksAsQuery(query, openDirectory, startrow, maxrow)
        val records: Int = query.getRecordcount()
        // no open tasks
        if (records == 0) {
            startrow -= getOpenTaskCount()
            if (startrow < 1) startrow = 1
        } else {
            startrow = 1
            maxrow -= records
        }
        if (maxrow > 0) getTasksAsQuery(query, closedDirectory, startrow, maxrow)
        return query
    }

    @Override
    fun getOpenTaskCount(): Int {
        return calculateSize(openDirectory)
    }

    @Override
    fun getClosedTaskCount(): Int {
        return calculateSize(closedDirectory)
    }

    private fun getTasksAsQuery(qry: Query?, dir: Resource?, startrow: Int, maxrow: Int): Query? {
        var startrow = startrow
        var maxrow = maxrow
        val children: Array<String?> = dir.list(FILTER)
        if (ArrayUtil.isEmpty(children)) return qry
        if (children.size < maxrow) maxrow = children.size
        var task: SpoolerTask?
        var to = startrow + maxrow
        if (to > children.size) to = children.size
        if (startrow < 1) startrow = 1
        for (i in startrow - 1 until to) {
            task = getTaskByName(dir, children[i])
            if (task != null) addQueryRow(qry, task)
        }
        return qry
    }

    @Throws(DatabaseException::class)
    private fun createQuery(): Query? {
        val v = "VARCHAR"
        val d = "DATE"
        return QueryImpl(arrayOf<String?>("type", "name", "detail", "id", "lastExecution", "nextExecution", "closed", "tries", "exceptions", "triesmax"), arrayOf(v, v, "object", v, d, d, "boolean", "int", "object", "int"), 0, "query")
    }

    private fun addQueryRow(qry: tachyon.runtime.type.Query?, task: SpoolerTask?) {
        val row: Int = qry.addRow()
        try {
            qry.setAt(KeyConstants._type, row, task.getType())
            qry.setAt(KeyConstants._name, row, task.subject())
            qry.setAt(KeyConstants._detail, row, task.detail())
            qry.setAt(KeyConstants._id, row, task.getId())
            qry.setAt(LAST_EXECUTION, row, DateTimeImpl(task.lastExecution(), true))
            qry.setAt(NEXT_EXECUTION, row, DateTimeImpl(task.nextExecution(), true))
            qry.setAt(CLOSED, row, Caster.toBoolean(task.closed()))
            qry.setAt(TRIES, row, Caster.toDouble(task.tries()))
            qry.setAt(TRIES_MAX, row, Caster.toDouble(task.tries()))
            qry.setAt(KeyConstants._exceptions, row, translateTime(task.getExceptions()))
            var triesMax = 0
            val plans: Array<ExecutionPlan?> = task.getPlans()
            for (y in plans.indices) {
                triesMax += plans[y].getTries()
            }
            qry.setAt(TRIES_MAX, row, Caster.toDouble(triesMax))
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    private fun translateTime(exp: Array?): Array? {
        var exp: Array? = exp
        exp = Duplicator.duplicate(exp, true)
        val it: Iterator<Object?> = exp.valueIterator()
        var sct: Struct?
        while (it.hasNext()) {
            sct = it.next() as Struct?
            sct.setEL(KeyConstants._time, DateTimeImpl(Caster.toLongValue(sct.get(KeyConstants._time, null), 0), true))
        }
        return exp
    }

    internal inner class SimpleThread(config: Config?, task: Task?) : Thread() {
        // 'tasks' needs to be synchronized because the other thread will access this list.
        // otherwise tasks.size() will not match the actual size of the server and NPEs
        // and unlimited loops may result.
        var tasks: List<Task?>? = Collections.synchronizedList(LinkedList<Task?>())
        private val config: Config?
        @Override
        fun run() {
            var task: Task
            while (tasks!!.size() > 0) {
                try {
                    task = CollectionUtil.remove(tasks, 0, null)
                    if (task != null) task.execute(config)
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                }
            }
        }

        init {
            this.config = config
            tasks.add(task)
        }
    }

    internal inner class SpoolerThread(config: ConfigWeb?, engine: SpoolerEngineImpl?) : Thread() {
        private val engine: SpoolerEngineImpl?
        var sleeping = false
        private val maxThreads: Int
        private val config: ConfigWeb?
        @Override
        fun run() {
            var taskNames: Array<String?>
            // SpoolerTask[] tasks;
            var task: SpoolerTask? = null
            var nextExection: Long
            ThreadLocalConfig.register(config)
            // ThreadLocalPageContext.register(engine.);
            val runningTasks: List<TaskThread?> = ArrayList<TaskThread?>()
            var tt: TaskThread?
            var adds: Int
            while (getOpenTaskCount() > 0) {
                adds = engine!!.adds()
                taskNames = openDirectory.list(FILTER)
                // tasks=engine.getOpenTasks();
                nextExection = Long.MAX_VALUE
                for (i in taskNames.indices) {
                    task = getTaskByName(openDirectory, taskNames[i])
                    if (task == null) continue
                    if (task.nextExecution() <= System.currentTimeMillis()) {
                        tt = TaskThread(config, engine, task)
                        tt.start()
                        runningTasks.add(tt)
                    } else if (task.nextExecution() < nextExection && nextExection != -1L && !task.closed()) nextExection = task.nextExecution()
                    nextExection = joinTasks(runningTasks, maxThreads, nextExection)
                }
                nextExection = joinTasks(runningTasks, 0, nextExection)
                if (adds != engine.adds()) continue
                if (nextExection == Long.MAX_VALUE) break
                val sleep: Long = nextExection - System.currentTimeMillis()

                // print.o("sleep:"+sleep+">"+(sleep/1000));
                if (sleep > 0) doWait(sleep)

                // if(sleep<0)break;
            }
            // print.o("end:"+getOpenTaskCount());
        }

        private fun joinTasks(runningTasks: List<TaskThread?>?, maxThreads: Int, nextExection: Long): Long {
            var nextExection = nextExection
            if (runningTasks!!.size() >= maxThreads) {
                val it = runningTasks.iterator()
                var tt: TaskThread?
                var task: SpoolerTask?
                while (it.hasNext()) {
                    tt = it.next()
                    SystemUtil.join(tt)
                    task = tt!!.getTask()
                    if (task != null && task.nextExecution() !== -1 && task.nextExecution() < nextExection && !task.closed()) {
                        nextExection = task.nextExecution()
                    }
                }
                runningTasks.clear()
            }
            return nextExection
        }

        private fun doWait(sleep: Long) {
            try {
                sleeping = true
                synchronized(this) { wait(sleep) }
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            } finally {
                sleeping = false
            }
        }

        init {
            this.maxThreads = engine!!.getMaxThreads()
            this.engine = engine
            this.config = config
            try {
                this.setPriority(MIN_PRIORITY)
            } // can throw security exceptions
            catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }
    }

    internal inner class TaskThread(config: ConfigWeb?, engine: SpoolerEngineImpl?, task: SpoolerTask?) : Thread() {
        private val engine: SpoolerEngineImpl?
        private val task: SpoolerTask?
        private val config: ConfigWeb?
        fun getTask(): SpoolerTask? {
            return task
        }

        @Override
        fun run() {
            ThreadLocalConfig.register(config)
            engine.execute(task)
            ThreadLocalConfig.release()
        }

        init {
            this.config = config
            this.engine = engine
            this.task = task
        }
    }

    /**
     * remove that task from Spooler
     *
     * @param task
     */
    @Override
    fun remove(task: SpoolerTask?) {
        unstore(ConfigWebUtil.toConfigWeb(ThreadLocalPageContext.getConfig()), task)
        // if(!openTasks.remove(task))closedTasks.remove(task);
    }

    fun remove(config: ConfigWeb?, task: SpoolerTask?) {
        unstore(config, task)
        // if(!openTasks.remove(task))closedTasks.remove(task);
    }

    fun removeAll() {
        ResourceUtil.removeChildrenEL(openDirectory)
        ResourceUtil.removeChildrenEL(closedDirectory)
        SystemUtil.wait(this, 100)
        ResourceUtil.removeChildrenEL(openDirectory)
        ResourceUtil.removeChildrenEL(closedDirectory)
    }

    fun adds(): Int {
        // return openTasks.size()>0;
        return add
    }

    @Override
    fun remove(id: String?) {
        var task: SpoolerTask? = getTaskById(openDirectory, id)
        if (task == null) task = getTaskById(closedDirectory, id)
        if (task != null) remove(task)
    }
    /*
	 * private SpoolerTask getTaskById(SpoolerTask[] tasks, String id) { for(int i=0;i<tasks.length;i++)
	 * { if(tasks[i].getId().equals(id)) { return tasks[i]; } } return null; }
	 */
    /**
     * execute task by id and return eror throwd by task
     *
     * @param id
     */
    @Override
    fun execute(id: String?): PageException? {
        var task: SpoolerTask? = getTaskById(openDirectory, id)
        if (task == null) task = getTaskById(closedDirectory, id)
        return if (task != null) {
            execute(task)
        } else null
    }

    @Override
    fun execute(task: SpoolerTask?): PageException? {
        return execute(ConfigWebUtil.toConfigWeb(ThreadLocalPageContext.getConfig()), task)
    }

    fun execute(config: ConfigWeb?, task: SpoolerTask?): PageException? {
        var task: SpoolerTask? = task
        try {
            log(config, task, null, true)
            if (task is SpoolerTaskSupport) // FUTURE this is bullshit, call the execute method directly, but you have to rewrite them for that
                (task as SpoolerTaskSupport?)!!._execute(config) else task.execute(config)
            unstore(config, task)
            task.setLastExecution(System.currentTimeMillis())
            task.setNextExecution(-1)
            task.setClosed(true)
            log(config, task, null, false)
            task = null
        } catch (e: Exception) {
            task.setLastExecution(System.currentTimeMillis())
            task.setNextExecution(calculateNextExecution(task))
            if (task.nextExecution() === -1) {
                unstore(config, task)
                task.setClosed(true)
                log(config, task, e, false)
                store(config, task)
                task = null
            } else {
                log(config, task, e, false)
                store(config, task)
            }
            return Caster.toPageException(e)
        }
        return null
    }

    fun setLabel(label: String?) {
        this.label = label
    }

    fun setPersisDirectory(persisDirectory: Resource?) {
        _persisDirectory = persisDirectory
    }

    fun getPersisDirectory(config: ConfigWeb?): Resource? {
        if (_persisDirectory == null) {
            _persisDirectory = config.getRemoteClientDirectory()
        }
        return _persisDirectory
    }

    fun setLog(log: Log?) {
        this.log = log
    }

    companion object {
        private val FILTER: TaskFileFilter? = TaskFileFilter()
        private val LAST_EXECUTION: Collection.Key? = KeyImpl.getInstance("lastExecution")
        private val NEXT_EXECUTION: Collection.Key? = KeyImpl.getInstance("nextExecution")
        private val CLOSED: Collection.Key? = KeyConstants._closed
        private val TRIES: Collection.Key? = KeyConstants._tries
        private val TRIES_MAX: Collection.Key? = KeyImpl.getInstance("triesmax")
    }

    init {
        _persisDirectory = persisDirectory
        closedDirectory = persisDirectory.getRealResource("closed")
        openDirectory = persisDirectory.getRealResource("open")
        this.maxThreads = maxThreads
        this.label = label
        this.log = log
    }
}

internal class TaskFileFilter : ResourceNameFilter {
    @Override
    fun accept(parent: Resource?, name: String?): Boolean {
        return name != null && name.endsWith(".tsk")
    }
}