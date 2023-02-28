package lucee.runtime.type.query

import lucee.commons.io.SystemUtil.TemplateLine

class QueryStruct(private val name: String?, sql: SQL?, templateLine: TemplateLine?) : StructImpl(Struct.TYPE_LINKED), QueryResult {
    private val sql: SQL?
    private var executionTime: Long = 0
    private val templateLine: TemplateLine?
    private var cacheType: String? = null
    private var updateCount = 0
    private var columnNames: Array<Key?>?
    private var isSingleRecord = false
    private var datasourceName: String? = null
    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        val dt: DumpTable? = super.toDumpData(pageContext, maxlevel, dp) as DumpTable?
        val comment = StringBuilder()

        // table.appendRow(1, new SimpleDumpData("SQL"), new SimpleDumpData(sql.toString()));
        val tl: TemplateLine? = getTemplateLine()
        if (tl != null) comment.append("Template: ").append(tl.toString(pageContext, true)).append("\n")
        val top: Int = dp.getMaxlevel()
        comment.append("Execution Time: ").append(Caster.toString(FormatUtil.formatNSAsMSDouble(getExecutionTime()))).append(" ms \n")
        comment.append("Record Count: ").append(Caster.toString(getRecordcount()))
        if (getRecordcount() > top) comment.append(" (showing top ").append(Caster.toString(top)).append(")")
        comment.append("\n")
        comment.append("Cached: ").append(if (isCached()) "Yes\n" else "No\n")
        if (isCached()) {
            val ct = getCacheType()
            comment.append("Cache Type: ").append(ct).append("\n")
        }
        val datasourceName = getDatasourceName()
        if (datasourceName != null) comment.append("Datasource: ").append(datasourceName).append("\n")
        val sql: SQL? = getSql()
        if (sql != null) comment.append("SQL: ").append("\n").append(StringUtil.suppressWhiteSpace(sql.toString().trim())).append("\n")
        dt.setTitle("Struct (from Query)")
        if (dp.getMetainfo()) dt.setComment(comment.toString())
        return dt
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        val qa = QueryStruct(name, sql, templateLine)
        qa.cacheType = cacheType
        qa.columnNames = columnNames
        qa.executionTime = executionTime
        qa.updateCount = updateCount
        qa.datasourceName = datasourceName
        copy(this, qa, deepCopy)
        return qa
    }

    @Override
    override fun getSql(): SQL? {
        return sql
    }

    @Override
    override fun setCacheType(cacheType: String?) {
        this.cacheType = cacheType
    }

    @Override
    override fun getCacheType(): String? {
        return cacheType
    }

    @Override
    override fun isCached(): Boolean {
        return cacheType != null
    }

    fun getDatasourceName(): String? {
        return datasourceName
    }

    fun setDatasourceName(datasourceName: String?) {
        this.datasourceName = datasourceName
    }

    @Override
    override fun getExecutionTime(): Long {
        return executionTime
    }

    @Override
    override fun setExecutionTime(executionTime: Long) {
        this.executionTime = executionTime
    }

    @Override
    override fun getTemplate(): String? {
        return if (templateLine == null) null else templateLine.template
    }

    @Override
    override fun getTemplateLine(): TemplateLine? {
        return templateLine
    }

    @Override
    override fun getName(): String? {
        return name
    }

    @Override
    override fun getRecordcount(): Int {
        return if (isSingleRecord) {
            if (size() > 0) 1 else 0
        } else size()
    }

    @Override
    override fun getColumncount(): Int {
        return if (columnNames == null) 0 else columnNames!!.size
    }

    @Override
    override fun getUpdateCount(): Int {
        return updateCount
    }

    @Override
    override fun setUpdateCount(updateCount: Int) {
        this.updateCount = updateCount
    }

    @Override
    override fun getColumnNames(): Array<Key?>? {
        return columnNames
    }

    @Override
    @Throws(PageException::class)
    override fun setColumnNames(columnNames: Array<Key?>?) {
        this.columnNames = columnNames
    }

    fun setSingleRecord(value: Boolean) {
        isSingleRecord = value
    }

    companion object {
        private const val serialVersionUID = -2123873025169506446L
        @Throws(PageException::class)
        fun toQueryStruct(q: QueryImpl?, columnName: Key?): QueryStruct? {
            val qs = QueryStruct(q.getName(), q.getSql(), q.getTemplateLine())
            qs.setCacheType(q.getCacheType())
            qs.setColumnNames(q.getColumnNames())
            qs.setExecutionTime(q.getExecutionTime())
            qs.setUpdateCount(q.getUpdateCount())
            val rows: Int = q.getRecordcount()
            if (rows == 0) return qs
            val columns: Array<Key?> = q.getColumnNames()
            var tmp: Struct?
            for (r in 1..rows) {
                tmp = StructImpl(Struct.TYPE_LINKED)
                qs.set(Caster.toKey(q.getAt(columnName, r)), tmp)
                for (c in columns) {
                    tmp.setEL(c, q.getAt(c, r, null))
                }
            }
            return qs
        }
    }

    init {
        this.sql = sql
        this.templateLine = templateLine
    }
}