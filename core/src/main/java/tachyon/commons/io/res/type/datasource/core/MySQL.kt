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

class MySQL(dc: DatasourceConnection, prefix: String) : CoreSupport() {
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
    fun getAttrs(dc: DatasourceConnection, prefix: String, pathHash: Int, path: String?): List {
        val sql = ("select rdr_id,rdr_name,rdr_type,rdr_length,rdr_last_modified,rdr_mode,rdr_attributes,rdr_data from " + prefix
                + "attrs where rdr_path_hash=? and rdr_path=? order by rdr_name")
        val stat: PreparedStatement = dc.getConnection().prepareStatement(sql)
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
            DBUtil.closeEL(stat)
        }
    }

    @Override
    @Throws(SQLException::class)
    fun create(dc: DatasourceConnection, prefix: String, fullPatHash: Int, pathHash: Int, path: String?, name: String?, type: Int) {
        val sql = ("insert into " + prefix + "attrs(rdr_type,rdr_path,rdr_name,rdr_full_path_hash,rdr_path_hash,rdr_last_modified,rdr_mode,rdr_attributes,rdr_data,rdr_length) "
                + "values(?,?,?,?,?,?,?,?,?,?)")
        val stat: PreparedStatement = dc.getConnection().prepareStatement(sql)
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
        try {
            stat.executeUpdate()
        } finally {
            DBUtil.closeEL(stat)
        }
    }

    @Override
    @Throws(SQLException::class)
    fun delete(dc: DatasourceConnection, prefix: String, attr: Attr?): Boolean {
        var rst = false
        if (attr != null) {
            var sql = "delete from " + prefix + "attrs where rdr_id=?"
            log(sql, attr.getId().toString() + "")
            var stat: PreparedStatement = dc.getConnection().prepareStatement(sql)
            stat.setInt(1, attr.getId())
            try {
                rst = stat.executeUpdate() > 0
            } finally {
                DBUtil.closeEL(stat)
            }
            if (attr.getData() > 0) {
                sql = "delete from " + prefix + "data where rdr_id=?"
                log(sql, attr.getData().toString() + "")
                stat = dc.getConnection().prepareStatement(sql)
                stat.setInt(1, attr.getData())
                try {
                    stat.executeUpdate()
                } finally {
                    DBUtil.closeEL(stat)
                }
            }
        }
        return rst
    }

    @Override
    @Throws(SQLException::class, IOException::class)
    fun getInputStream(dc: DatasourceConnection, prefix: String, attr: Attr?): InputStream {
        if (attr == null || attr.getData() === 0) return ByteArrayInputStream(ByteArray(0))
        val sql = "select rdr_data from " + prefix + "data where rdr_id=?"
        log(sql, attr.getData().toString() + "")
        val stat: PreparedStatement = dc.getConnection().prepareStatement(sql)
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
            DBUtil.closeEL(stat)
        }
    }

    @Override
    @Throws(SQLException::class)
    fun write(dc: DatasourceConnection, prefix: String, attr: Attr, `is`: InputStream, append: Boolean) {
        if (attr.getData() === 0) writeInsert(dc, prefix, attr, `is`) else writeUpdate(dc, prefix, attr, `is`, append)
    }

    @Throws(SQLException::class)
    private fun writeUpdate(dc: DatasourceConnection, prefix: String, attr: Attr, `is`: InputStream, append: Boolean) {

        // update rdr_data set rdr_data = concat(rdr_data,'susi') where rdr_id = 1
        var sql = if (append) "update " + prefix + "data set rdr_data=concat(rdr_data,?) where rdr_id=?" else "update " + prefix + "data set rdr_data=? where rdr_id=?"
        log(sql)
        var stat1: PreparedStatement? = null
        var stat2: PreparedStatement? = null
        var stat3: PreparedStatement? = null
        var rs: ResultSet? = null
        try {
            // Connection conn = dc.getConnection();
            stat1 = dc.getConnection().prepareStatement(sql)
            stat1.setBinaryStream(1, `is`, -1)
            stat1.setInt(2, attr.getData())
            stat1.executeUpdate()

            // select
            sql = "select Length(rdr_data) as DataLen from " + prefix + "data where rdr_id=?"
            log(sql)
            stat2 = dc.getConnection().prepareStatement(sql)
            stat2.setInt(1, attr.getData())
            rs = stat2.executeQuery()
            if (rs.next()) {
                sql = "update " + prefix + "attrs set rdr_length=? where rdr_id=?"
                log(sql)
                stat3 = dc.getConnection().prepareStatement(sql)
                stat3.setInt(1, rs.getInt(1))
                stat3.setInt(2, attr.getId())
                stat3.executeUpdate()
            }
        } finally {
            DBUtil.closeEL(stat1)
        }
    }

    @Throws(SQLException::class)
    private fun writeInsert(dc: DatasourceConnection, prefix: String, attr: Attr, `is`: InputStream) {
        var stat1: PreparedStatement? = null
        var stat2: Statement? = null
        var stat3: PreparedStatement? = null
        var rs: ResultSet? = null
        try {
            // Insert
            var sql = "insert into " + prefix + "data (rdr_data) values(?)"
            log(sql)
            val conn: Connection = dc.getConnection()
            stat1 = dc.getConnection().prepareStatement(sql)
            stat1.setBinaryStream(1, `is`, -1)
            stat1.execute()

            // select
            sql = "select rdr_id,Length(rdr_data) as DataLen from " + prefix + "data order by rdr_id desc LIMIT 1"
            log(sql)
            stat2 = conn.createStatement()
            rs = stat2.executeQuery(sql)

            // update
            if (rs.next()) {
                sql = "update " + prefix + "attrs set rdr_data=?,rdr_length=? where rdr_id=?"
                log(sql)
                stat3 = dc.getConnection().prepareStatement(sql)
                stat3.setInt(1, rs.getInt(1))
                stat3.setInt(2, rs.getInt(2))
                stat3.setInt(3, attr.getId())
                stat3.executeUpdate()
            }
        } finally {
            DBUtil.closeEL(rs)
            DBUtil.closeEL(stat1)
            DBUtil.closeEL(stat2)
        }
    }

    @Override
    @Throws(SQLException::class)
    fun setLastModified(dc: DatasourceConnection, prefix: String, attr: Attr, time: Long) {
        val sql = "update " + prefix + "attrs set rdr_last_modified=? where rdr_id=?"
        log(sql)
        var stat: PreparedStatement? = null
        try {
            stat = dc.getConnection().prepareStatement(sql)
            stat.setTimestamp(1, Timestamp(time), getCalendar())
            stat.setInt(2, attr.getId())
            stat.executeUpdate()
        } finally {
            DBUtil.closeEL(stat)
        }
    }

    @Override
    @Throws(SQLException::class)
    fun setMode(dc: DatasourceConnection, prefix: String, attr: Attr, mode: Int) {
        val sql = "update " + prefix + "attrs set rdr_mode=? where rdr_id=?"
        log(sql)
        var stat: PreparedStatement? = null
        try {
            stat = dc.getConnection().prepareStatement(sql)
            stat.setInt(1, mode)
            stat.setInt(2, attr.getId())
            stat.executeUpdate()
        } finally {
            DBUtil.closeEL(stat)
        }
    }

    @Override
    @Throws(SQLException::class)
    fun setAttributes(dc: DatasourceConnection, prefix: String, attr: Attr, attributes: Int) {
        val sql = "update " + prefix + "attrs set rdr_attributes=? where rdr_id=?"
        log(sql)
        var stat: PreparedStatement? = null
        try {
            stat = dc.getConnection().prepareStatement(sql)
            stat.setInt(1, attributes)
            stat.setInt(2, attr.getId())
            stat.executeUpdate()
        } finally {
            DBUtil.closeEL(stat)
        }
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
        var sql = "show table status like '" + prefix + "attrs'"
        try {
            stat1 = conn.createStatement()
            rs = stat1.executeQuery(sql)
            if (rs.next()) installAttrs = false
        } finally {
            DBUtil.closeEL(rs)
            DBUtil.closeEL(stat1)
        }

        // check data
        sql = "show table status like '" + prefix + "data'"
        try {
            stat1 = conn.createStatement()
            rs = stat1.executeQuery(sql)
            if (rs.next()) installData = false
        } finally {
            DBUtil.closeEL(rs)
            DBUtil.closeEL(stat1)
        }
        if (installAttrs) {
            execute(conn,
                    "CREATE TABLE  `" + prefix + "attrs` (" + "`rdr_id` int(11) NOT NULL auto_increment," + "`rdr_name` varchar(255) default NULL,"
                            + "`rdr_path_hash` int(11) default NULL," + "`rdr_full_path_hash` int(11) default NULL," + "`rdr_path` varchar(1023) default NULL,"
                            + "`rdr_type` int(11) default NULL," + "`rdr_last_modified` datetime default NULL," + "`rdr_mode` int(11) default '0',"
                            + "`rdr_attributes` int(11) default '0'," + "`rdr_data` int(11) default '0'," + "`rdr_length` int(11) default '0'," + "PRIMARY KEY  (`rdr_id`),"
                            + "KEY `idx_name` (`rdr_name`)," + "KEY `idx_path_hash` (`rdr_path_hash`)," + "KEY `idx_full_path_hash` (`rdr_full_path_hash`),"
                            + "KEY `idx_data` (`rdr_data`)" + ")")
        }
        if (installData) {
            execute(conn, "CREATE TABLE  `" + prefix + "data` (" + "`rdr_id` int(10) unsigned NOT NULL auto_increment," + "`rdr_data` longblob," + "PRIMARY KEY  (`rdr_id`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=latin1;")
        }
    }
}