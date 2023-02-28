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
package lucee.runtime.db

import java.sql.Types

interface CFTypes {
    companion object {
        // public static final int BFILE=-13;//OracleTypes.BFILE;
        const val CURSOR = -10 // OracleTypes.CURSOR;
        const val BFILE = -13
        const val BINARY_DOUBLE = 101
        const val BINARY_FLOAT = 100
        const val FIXED_CHAR = 999
        const val INTERVALDS = -104
        const val INTERVALYM = -103
        const val JAVA_STRUCT = 2008
        val NUMBER: Int = Types.NUMERIC
        const val PLSQL_INDEX_TABLE = -14
        const val RAW = -2
        const val ROWID = -8
        const val ORACLE_TIMESTAMPLTZ = -102
        const val ORACLE_TIMESTAMPNS = -100
        const val ORACLE_TIMESTAMPTZ = -101
        const val VARCHAR2 = -100
        const val ORACLE_OPAQUE = 2007

        /**
         * <P>
         * The constant in the Java programming language, sometimes referred to as a type code, that
         * identifies the generic SQL type `BIT`.
        </P> */
        const val BIT = -7

        /**
         * <P>
         * The constant in the Java programming language, sometimes referred to as a type code, that
         * identifies the generic SQL type `TINYINT`.
        </P> */
        const val TINYINT = -6

        /**
         * <P>
         * The constant in the Java programming language, sometimes referred to as a type code, that
         * identifies the generic SQL type `SMALLINT`.
        </P> */
        const val SMALLINT = 5

        /**
         * <P>
         * The constant in the Java programming language, sometimes referred to as a type code, that
         * identifies the generic SQL type `INTEGER`.
        </P> */
        const val INTEGER = 4

        /**
         * <P>
         * The constant in the Java programming language, sometimes referred to as a type code, that
         * identifies the generic SQL type `BIGINT`.
        </P> */
        const val BIGINT = -5

        /**
         * <P>
         * The constant in the Java programming language, sometimes referred to as a type code, that
         * identifies the generic SQL type `FLOAT`.
        </P> */
        const val FLOAT = 6

        /**
         * <P>
         * The constant in the Java programming language, sometimes referred to as a type code, that
         * identifies the generic SQL type `REAL`.
        </P> */
        const val REAL = 7

        /**
         * <P>
         * The constant in the Java programming language, sometimes referred to as a type code, that
         * identifies the generic SQL type `DOUBLE`.
        </P> */
        const val DOUBLE = 8

        /**
         * <P>
         * The constant in the Java programming language, sometimes referred to as a type code, that
         * identifies the generic SQL type `NUMERIC`.
        </P> */
        const val NUMERIC = 2

        /**
         * <P>
         * The constant in the Java programming language, sometimes referred to as a type code, that
         * identifies the generic SQL type `DECIMAL`.
        </P> */
        const val DECIMAL = 3

        /**
         * <P>
         * The constant in the Java programming language, sometimes referred to as a type code, that
         * identifies the generic SQL type `CHAR`.
        </P> */
        const val CHAR = 1

        /**
         * <P>
         * The constant in the Java programming language, sometimes referred to as a type code, that
         * identifies the generic SQL type `VARCHAR`.
        </P> */
        const val VARCHAR = 12

        /**
         * <P>
         * The constant in the Java programming language, sometimes referred to as a type code, that
         * identifies the generic SQL type `LONGVARCHAR`.
        </P> */
        const val LONGVARCHAR = -1

        /**
         * <P>
         * The constant in the Java programming language, sometimes referred to as a type code, that
         * identifies the generic SQL type `DATE`.
        </P> */
        const val DATE = 91

        /**
         * <P>
         * The constant in the Java programming language, sometimes referred to as a type code, that
         * identifies the generic SQL type `TIME`.
        </P> */
        const val TIME = 92

        /**
         * <P>
         * The constant in the Java programming language, sometimes referred to as a type code, that
         * identifies the generic SQL type `TIMESTAMP`.
        </P> */
        const val TIMESTAMP = 93

        /**
         * <P>
         * The constant in the Java programming language, sometimes referred to as a type code, that
         * identifies the generic SQL type `BINARY`.
        </P> */
        const val BINARY = -2

        /**
         * <P>
         * The constant in the Java programming language, sometimes referred to as a type code, that
         * identifies the generic SQL type `VARBINARY`.
        </P> */
        const val VARBINARY = -3

        /**
         * <P>
         * The constant in the Java programming language, sometimes referred to as a type code, that
         * identifies the generic SQL type `LONGVARBINARY`.
        </P> */
        const val LONGVARBINARY = -4

        /**
         * <P>
         * The constant in the Java programming language, sometimes referred to as a type code, that
         * identifies the generic SQL type `NULL`.
        </P> */
        const val NULL = 0

        /**
         * The constant in the Java programming language that indicates that the SQL type is
         * database-specific and gets mapped to a Java object that can be accessed via the methods
         * `getObject` and `setObject`.
         */
        const val OTHER = 1111

        /**
         * The constant in the Java programming language, sometimes referred to as a type code, that
         * identifies the generic SQL type `JAVA_OBJECT`.
         *
         * @since 1.2
         */
        const val ORACLE_JAVA_OBJECT = 2000

        /**
         * The constant in the Java programming language, sometimes referred to as a type code, that
         * identifies the generic SQL type `DISTINCT`.
         *
         * @since 1.2
         */
        const val ORACLE_DISTINCT = 2001

        /**
         * The constant in the Java programming language, sometimes referred to as a type code, that
         * identifies the generic SQL type `STRUCT`.
         *
         * @since 1.2
         */
        const val ORACLE_STRUCT = 2002

        /**
         * The constant in the Java programming language, sometimes referred to as a type code, that
         * identifies the generic SQL type `ARRAY`.
         *
         * @since 1.2
         */
        const val ORACLE_ARRAY = 2003

        /**
         * The constant in the Java programming language, sometimes referred to as a type code, that
         * identifies the generic SQL type `BLOB`.
         *
         * @since 1.2
         */
        const val ORACLE_BLOB = 2004

        /**
         * The constant in the Java programming language, sometimes referred to as a type code, that
         * identifies the generic SQL type `CLOB`.
         *
         * @since 1.2
         */
        const val ORACLE_CLOB = 2005
        const val ORACLE_NCLOB = 2011

        /**
         * The constant in the Java programming language, sometimes referred to as a type code, that
         * identifies the generic SQL type `REF`.
         *
         * @since 1.2
         */
        const val ORACLE_REF = 2006

        /**
         * The constant in the Java programming language, somtimes referred to as a type code, that
         * identifies the generic SQL type `DATALINK`.
         *
         * @since 1.4
         */
        const val DATALINK = 70

        /**
         * The constant in the Java programming language, somtimes referred to as a type code, that
         * identifies the generic SQL type `BOOLEAN`.
         *
         * @since 1.4
         */
        const val BOOLEAN = 16
        const val IDSTAMP = CHAR // TODO is this right?
    }
}