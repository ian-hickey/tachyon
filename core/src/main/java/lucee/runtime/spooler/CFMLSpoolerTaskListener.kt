package lucee.runtime.spooler

import lucee.commons.io.DevNullOutputStream

abstract class CFMLSpoolerTaskListener(currTemplate: TemplateLine?, task: SpoolerTask?) : SpoolerTaskListener() {
    private val task: SpoolerTask?
    private val currTemplate: TemplateLine?

    @Override
    override fun listen(config: Config?, e: Exception?, before: Boolean) {
        if (config !is ConfigWeb) return
        val cw: ConfigWeb? = config as ConfigWeb?
        var pc: PageContext = ThreadLocalPageContext.get()
        var pcCreated = false
        if (pc == null) {
            pcCreated = true
            val parr: Array<Pair?> = arrayOfNulls<Pair?>(0)
            val os: DevNullOutputStream = DevNullOutputStream.DEV_NULL_OUTPUT_STREAM
            pc = ThreadUtil.createDummyPageContext(cw)
            pc.setRequestTimeout(config.getRequestTimeout().getMillis())
        }
        try {
            val args: Struct = StructImpl()
            var l: Long = task.lastExecution()
            if (l > 0) args.set("lastExecution", DateTimeImpl(pc, l, true))
            l = task.nextExecution()
            if (l > 0) args.set("nextExecution", DateTimeImpl(pc, l, true))
            args.set("created", DateTimeImpl(pc, task.getCreation(), true))
            args.set(KeyConstants._id, task.getId())
            args.set(KeyConstants._type, task.getType())
            val details: Struct = task.detail()
            if (task is MailSpoolerTask) {
                details.set(KeyConstants._charset, (task as MailSpoolerTask?).getCharset())
                details.set(KeyConstants._replyto, (task as MailSpoolerTask?).getReplyTos())
                details.set("failto", (task as MailSpoolerTask?).getFailTos())
            }
            args.set(KeyConstants._detail, details)
            args.set(KeyConstants._tries, task.tries())
            args.set("remainingtries", if (e == null) 0 else task.getPlans().length - task.tries())
            args.set("closed", task.closed())
            if (!before) args.set("passed", e == null)
            if (e != null) args.set("exception", CatchBlockImpl(Caster.toPageException(e)))
            val curr: Struct = StructImpl()
            args.set("caller", curr)
            curr.set("template", currTemplate.template)
            curr.set("line", Double.valueOf(currTemplate.line))
            val adv: Struct = StructImpl()
            args.set("advanced", adv)
            adv.set("exceptions", task.getExceptions())
            adv.set("executedPlans", task.getPlans())
            val o: Object? = _listen(pc, args, before)
            if (before && o is Struct && task is MailSpoolerTask) {
                (task as MailSpoolerTask?).mod(o as Struct?)
            }
        } catch (pe: Exception) {
            LogUtil.log(ThreadLocalPageContext.get(), CFMLSpoolerTaskListener::class.java.getName(), pe)
        } finally {
            if (pcCreated) ThreadLocalPageContext.release()
        }
    }

    @Throws(PageException::class)
    abstract fun _listen(pc: PageContext?, args: Struct?, before: Boolean): Object?

    init {
        this.task = task
        this.currTemplate = currTemplate
    }
}