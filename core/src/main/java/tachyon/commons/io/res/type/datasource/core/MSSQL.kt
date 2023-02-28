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
package tachyon.commons.io.res.type.datasource.core

import java.io.ByteArrayInputStream

class MSSQL(dc: DatasourceConnection, prefix: String) : CoreSupport() {
    @Throws(SQLException::class)
    private fun execute(conn: Connection, sql: String) {
        log(sql)
        var stat: Statement? = null
        try {
            stat = conn.createStatement()
            stat.executeUpdate(sql)
        } finally {
            DBUtil.closeEL(stat)
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getAttr(dc: DatasourceConnection?, prefix: String, fullPathHash: Int, path: String?, name: String?): Attr? {
        // ROOT
        if (StringUtil.isEmpty(path)) return ATTR_ROOT
        val sql = ("select rdr_id,rdr_type,rdr_length,rdr_last_modified,rdr_mode,rdr_attributes,rdr_data from " + prefix
                + "attrs where rdr_full_path_hash=? and rdr_path=? and rdr_name=?")
        val stat: PreparedStatement = prepareStatement(dc, sql) // dc.getConnection().prepareStatement(sql);
        stat.setInt(1, fullPathHash)
        stat.setString(2, path)
        stat.setString(3, name)
        log(sql, fullPathHash.toString() + "", path, name)
        val rs: ResultSet = stat.executeQuery()
        return try {
            if (!rs.next()) null else Attr(rs.getInt(1), name, path, true, rs.getInt(2), rs.getInt(3), rs.getTimestamp(4, getCalendar()).getTime(), rs.getShort(5), rs.getShort(6), rs.getInt(7))
        } finally {
            DBUtil.closeEL(rs)
            // DBUtil.closeEL(stat);
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getAttrs(dc: DatasourceConnection?, prefix: String, pathHash: Int, path: String?): List {
        val sql = ("select rdr_id,rdr_name,rdr_type,rdr_length,rdr_last_modified,rdr_mode,rdr_attributes,rdr_data from " + prefix
                + "attrs where rdr_path_hash=? and rdr_path=? order by rdr_name")
        val stat: PreparedStatement = prepareStatement(dc, sql) // dc.getConnection().prepareStatement(sql);
        stat.setInt(1, pathHash)
        stat.setString(2, path)
        log(sql, pathHash.toString() + "", path)
        val rs: ResultSet = stat.executeQuery()
        return try {
            val attrs: List = ArrayList()
            // hashCode=(path+name).hashCode();
            while (rs.next()) {
                attrs.add(Attr(rs.getInt(1), rs.getString(2), path, true, rs.getInt(3), rs.getInt(4), rs.getTimestamp(5, getCalendar()).getTime(), rs.getShort(6),
                        rs.getShort(7), rs.getInt(8)))
            }
            attrs
        } finally {
            DBUtil.closeEL(rs)
            // DBUtil.closeEL(stat);
        }
    }

    @Override
    @Throws(SQLException::class)
    fun create(dc: DatasourceConnection?, prefix: String, fullPatHash: Int, pathHash: Int, path: String?, name: String?, type: Int) {
        val sql = ("insert into " + prefix + "attrs(rdr_type,rdr_path,rdr_name,rdr_full_path_hash,rdr_path_hash,rdr_last_modified,rdr_mode,rdr_attributes,rdr_data,rdr_length) "
                + "values(?,?,?,?,?,?,?,?,?,?)")
        val stat: PreparedStatement = prepareStatement(dc, sql) // dc.getConnection().prepareStatement(sql);
        log(sql)
        stat.setInt(1, type)
        stat.setString(2, path)
        stat.setString(3, name)
        stat.setInt(4, fullPatHash)
        stat.setInt(5, pathHash)
        stat.setTimestamp(6, Timestamp(System.currentTimeMillis()), getCalendar())
        stat.setInt(7, DEFAULT_MODE)
        stat.setInt(8, DEFAULT_ATTRS)
        stat.setInt(9, 0)
        stat.setInt(10, 0)
        // try{
        stat.executeUpdate()
        /*
		 * } finally { //DBUtil.closeEL(stat); }
		 */
    }

    @Override
    @Throws(SQLException::class)
    fun delete(dc: DatasourceConnection?, prefix: String, attr: Attr?): Boolean {
        var rst = false
        if (attr != null) {
            var sql = "delete from " + prefix + "attrs where rdr_id=?"
            log(sql, attr.getId().toString() + "")
            var stat: PreparedStatement = prepareStatement(dc, sql) // dc.getConnection().prepareStatement(sql);
            stat.setInt(1, attr.getId())

            // try{
            rst = stat.executeUpdate() > 0
            /*
			 * } finally { //DBUtil.closeEL(stat); }
			 */if (attr.getData() > 0) {
                sql = "delete from " + prefix + "data where rdr_id=?"
                log(sql, attr.getData().toString() + "")
                stat = prepareStatement(dc, sql) // dc.getConnection().prepareStatement(sql);
                stat.setInt(1, attr.getData())
                // try{
                stat.executeUpdate()
                /*
				 * } finally { //DBUtil.closeEL(stat); }
				 */
            }
        }
        return rst
    }

    @Override
    @Throws(SQLException::class, IOException::class)
    fun getInputStream(dc: DatasourceConnection?, prefix: String, attr: Attr?): InputStream {
        if (attr == null || attr.getData() === 0) return ByteArrayInputStream(ByteArray(0))
        val sql = "select rdr_data from " + prefix + "data where rdr_id=?"
        log(sql, attr.getData().toString() + "")
        val stat: PreparedStatement = prepareStatement(dc, sql) // dc.getConnection().prepareStatement(sql);
        stat.setInt(1, attr.getData())
        var rs: ResultSet? = null
        return try {
            rs = stat.executeQuery()
            if (!rs.next()) {
                throw IOException("Can't read data from [" + attr.getParent() + attr.getName().toString() + "]")
            }
            rs.getBlob(1).getBinaryStream()
        } finally {
            DBUtil.closeEL(rs)
            // DBUtil.closeEL(stat);
        }
    }

    @Override
    @Throws(SQLException::class)
    fun write(dc: DatasourceConnection, prefix: String, attr: Attr, `is`: InputStream, append: Boolean) {
        if (attr.getData() === 0) {
            writeInsert(dc, prefix, attr, `is`)
        } else writeUpdate(dc, prefix, attr, `is`, append)
    }

    @Throws(SQLException::class)
    private fun writeUpdate(dc: DatasourceConnection, prefix: String, attr: Attr, `is`: InputStream, append: Boolean) {
        var sql: String
        sql = if (append) {
            ("DECLARE @ptrval binary(16);" + "DECLARE @iLen int;" + "SELECT @ptrval = TEXTPTR(rdr_data), @iLen = dataLength(rdr_data)" + "FROM " + prefix + "data "
                    + "WHERE rdr_id = ? " + "UPDATETEXT " + prefix + "data.rdr_data @ptrval @iLen 0 ?;")
        } else {
            "update " + prefix + "data set rdr_data=? where rdr_id=?"
        }
        log(sql)
        var stat1: PreparedStatement? = null
        var stat2: PreparedStatement? = null
        var stat3: PreparedStatement? = null
        var rs: ResultSet? = null
        // try{
        // Connection conn = dc.getConnection();
        stat1 = prepareStatement(dc, sql) // dc.getConnection().prepareStatement(sql);
        if (append) {
            stat1.setInt(1, attr.getData())
            stat1.setBinaryStream(2, `is`, -1)
        } else {
            stat1.setBinaryStream(1, `is`, -1)
            stat1.setInt(2, attr.getData())
        }
        stat1.executeUpdate()

        // select
        sql = "select dataLength(rdr_data) as DataLen from " + prefix + "data where rdr_id=?"
        log(sql)
        stat2 = prepareStatement(dc, sql) // dc.getConnection().prepareStatement(sql);
        stat2.setInt(1, attr.getData())
        rs = stat2.executeQuery()
        if (rs.next()) {
            sql = "update " + prefix + "attrs set rdr_length=? where rdr_id=?"
            log(sql)
            stat3 = prepareStatement(dc, sql) // dc.getConnection().prepareStatement(sql);
            stat3.setInt(1, rs.getInt(1))
            stat3.setInt(2, attr.getId())
            stat3.executeUpdate()
        }
        /*
		 * } finally { //DBUtil.closeEL(stat1); //DBUtil.closeEL(stat2); //DBUtil.closeEL(stat3); }
		 */
    }

    @Throws(SQLException::class)
    private fun writeInsert(dc: DatasourceConnection, prefix: String, attr: Attr, `is`: InputStream) {
        var stat1: PreparedStatement? = null
        var stat2: PreparedStatement? = null
        var stat3: PreparedStatement? = null
        var rs: ResultSet? = null
        try {
            // Insert
            var sql = "insert into " + prefix + "data (rdr_data) values(?)"
            log(sql)
            // Connection conn = dc.getConnection();
            stat1 = prepareStatement(dc, sql) // dc.getConnection().prepareStatement(sql);
            stat1.setBinaryStream(1, `is`, -1)
            stat1.execute()

            // select
            sql = "select TOP 1 rdr_id,dataLength(rdr_data) as DataLen from " + prefix + "data order by rdr_id desc"
            log(sql)
            stat2 = prepareStatement(dc, sql) // conn.createStatement();
            rs = stat2.executeQuery()

            // update
            if (rs.next()) {
                sql = "update " + prefix + "attrs set rdr_data=?,rdr_length=? where rdr_id=?"
                log(sql)
                stat3 = prepareStatement(dc, sql) // dc.getConnection().prepareStatement(sql);
                stat3.setInt(1, rs.getInt(1))
                stat3.setInt(2, rs.getInt(2))
                stat3.setInt(3, attr.getId())
                stat3.executeUpdate()
            }
        } finally {
            DBUtil.closeEL(rs)
            // DBUtil.closeEL(stat1);
            // DBUtil.closeEL(stat2);
            // DBUtil.closeEL(stat3);
        }
    }

    @Override
    @Throws(SQLException::class)
    fun setLastModified(dc: DatasourceConnection?, prefix: String, attr: Attr, time: Long) {
        val sql = "update " + prefix + "attrs set rdr_last_modified=? where rdr_id=?"
        log(sql)
        var stat: PreparedStatement? = null
        // try{
        stat = prepareStatement(dc, sql) // dc.getConnection().prepareStatement(sql);
        stat.setTimestamp(1, Timestamp(time), getCalendar())
        stat.setInt(2, attr.getId())
        stat.executeUpdate()
        /*
		 * } finally { //DBUtil.closeEL(stat); }
		 */
    }

    @Override
    @Throws(SQLException::class)
    fun setMode(dc: DatasourceConnection?, prefix: String, attr: Attr, mode: Int) {
        val sql = "update " + prefix + "attrs set rdr_mode=? where rdr_id=?"
        log(sql)
        var stat: PreparedStatement? = null
        // try{
        stat = prepareStatement(dc, sql) // dc.getConnection().prepareStatement(sql);
        stat.setInt(1, mode)
        stat.setInt(2, attr.getId())
        stat.executeUpdate()
        /*
		 * } finally { //DBUtil.closeEL(stat); }
		 */
    }

    @Override
    @Throws(SQLException::class)
    fun setAttributes(dc: DatasourceConnection?, prefix: String, attr: Attr, attributes: Int) {
        val sql = "update " + prefix + "attrs set rdr_attributes=? where rdr_id=?"
        log(sql)
        var stat: PreparedStatement? = null
        // try{
        stat = prepareStatement(dc, sql) // dc.getConnection().prepareStatement(sql);
        stat.setInt(1, attributes)
        stat.setInt(2, attr.getId())
        stat.executeUpdate()
        /*
		 * } finally { //DBUtil.closeEL(stat); }
		 */
    }

    @Override
    override fun concatSupported(): Boolean {
        return true
    }

    companion object {
        private const val DEFAULT_MODE = 511
        private const val DEFAULT_ATTRS = 0
    }

    init {
        val conn: Connection = dc.getConnection()
        var stat1: Statement? = null
        var rs: ResultSet? = null
        var installAttrs = true
        var installData = true

        // check attr
        var sql = "select count(*) as cnt from dbo.sysobjects where id = object_id(N'[dbo].[" + prefix + "attrs]') and OBJECTPROPERTY(id, N'IsUserTable') = 1"
        try {
            stat1 = conn.createStatement()
            rs = stat1.executeQuery(sql)
            if (rs.next()) {
                if (rs.getInt(1) > 0) installAttrs = false
            }
        } finally {
            DBUtil.closeEL(rs)
            DBUtil.closeEL(stat1)
        }

        // check data
        sql = "select count(*) as CNT from dbo.sysobjects where id = object_id(N'[dbo].[" + prefix + "data]') and OBJECTPROPERTY(id, N'IsUserTable') = 1"
        try {
            stat1 = conn.createStatement()
            rs = stat1.executeQuery(sql)
            if (rs.next()) {
                if (rs.getInt(1) > 0) installData = false
            }
        } finally {
            DBUtil.closeEL(rs)
            DBUtil.closeEL(stat1)
        }
        if (installAttrs) {
            execute(conn,
                    "CREATE TABLE [dbo].[" + prefix + "attrs] (" + "[rdr_id] [int] IDENTITY (1, 1) NOT NULL ," + "[rdr_name] [varchar] (255) COLLATE Latin1_General_CI_AS NULL ,"
                            + "[rdr_path_hash] [int] NULL ," + "[rdr_full_path_hash] [int] NULL ," + "[rdr_path] [varchar] (2048) COLLATE Latin1_General_CI_AS NULL ,"
                            + "[rdr_type] [int] NULL ," + "[rdr_last_modified] [datetime] NULL ," + "[rdr_mode] [int] NULL ," + "[rdr_attributes] [int] NULL ,"
                            + "[rdr_data] [int] NULL ," + "[rdr_length] [int] NULL" + ") ON [PRIMARY]")
            execute(conn,
                    "ALTER TABLE [dbo].[" + prefix + "attrs] WITH NOCHECK ADD " + "CONSTRAINT [PK_" + prefix + "attrs] PRIMARY KEY  CLUSTERED " + "([rdr_id])  ON [PRIMARY] ")
            execute(conn, "ALTER TABLE [dbo].[" + prefix + "attrs] ADD " + "CONSTRAINT [DF_" + prefix + "attrs_rdr_mode] DEFAULT (0) FOR [rdr_mode]," + "CONSTRAINT [DF_" + prefix
                    + "attrs_rdr_attributes] DEFAULT (0) FOR [rdr_attributes]," + "CONSTRAINT [DF_" + prefix + "attrs_rdr_length] DEFAULT (0) FOR [rdr_length]")
            execute(conn, "CREATE  INDEX [IDX_name] ON [dbo].[" + prefix + "attrs]([rdr_name]) ON [PRIMARY]")
            execute(conn, "CREATE  INDEX [IDX_id] ON [dbo].[" + prefix + "attrs]([rdr_data]) ON [PRIMARY]")
            execute(conn, "CREATE  INDEX [idx_path] ON [dbo].[" + prefix + "attrs]([rdr_path_hash]) ON [PRIMARY]")
            execute(conn, "CREATE  INDEX [idx_full_path] ON [dbo].[" + prefix + "attrs]([rdr_full_path_hash]) ON [PRIMARY]")
        }
        if (installData) {
            execute(conn, "CREATE TABLE [dbo].[" + prefix + "data] (" + "[rdr_id] [int] IDENTITY (1, 1) NOT NULL ," + "[rdr_data] [image] NULL"
                    + ") ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]")
            execute(conn, "ALTER TABLE [dbo].[" + prefix + "data] WITH NOCHECK ADD " + "CONSTRAINT [PK_" + prefix + "data] PRIMARY KEY  CLUSTERED " + "([rdr_id])  ON [PRIMARY] ")
        }
    }
}