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
package tachyon.runtime.converter

import java.io.ByteArrayInputStream

// FUTURE make this available to loader
/**
 *
 */
class JavaConverter : ConverterSupport(), BinaryConverter {
    @Override
    @Throws(ConverterException::class, IOException::class)
    fun writeOut(pc: PageContext?, source: Object?, writer: Writer?) {
        if (source !is Serializable) throw ConverterException("Java Object is not of type Serializable")
        writer.write(serialize(source as Serializable?))
        writer.flush()
    }

    @Override
    @Throws(ConverterException::class, IOException::class)
    fun writeOut(pc: PageContext?, source: Object?, os: OutputStream?) {
        if (source !is Serializable) throw ConverterException("Java Object is not of type Serializable")
        serialize(source as Serializable?, os)
        os.flush()
    }

    companion object {
        /**
         * serialize a Java Object of Type Serializable
         *
         * @param o
         * @return serialized String
         * @throws IOException
         */
        @Throws(IOException::class)
        fun serialize(o: Serializable?): String? {
            val baos = ByteArrayOutputStream()
            serialize(o, baos)
            return Base64Coder.encode(baos.toByteArray())
        }

        @Throws(IOException::class)
        fun serializeAsBinary(o: Serializable?): ByteArray? {
            val baos = ByteArrayOutputStream()
            serialize(o, baos)
            return baos.toByteArray()
        }

        @Throws(IOException::class)
        fun serialize(o: Serializable?, out: tachyon.commons.io.res.Resource?) {
            serialize(o, out.getOutputStream())
        }

        @Throws(IOException::class)
        fun serialize(o: Serializable?, os: OutputStream?) {
            var oos: ObjectOutputStream? = null
            try {
                oos = ObjectOutputStream(os)
                oos.writeObject(o)
            } finally {
                IOUtil.close(oos, os)
            }
        }

        /**
         * unserialize a serialized Object
         *
         * @param str
         * @return unserialized Object
         * @throws IOException
         * @throws ClassNotFoundException
         * @throws CoderException
         */
        @Throws(IOException::class, ClassNotFoundException::class, CoderException::class)
        fun deserialize(str: String?): Object? {
            val bais = ByteArrayInputStream(Base64Coder.decode(str, true))
            return deserialize(bais)
        }

        @Throws(IOException::class, ClassNotFoundException::class)
        fun deserialize(`is`: InputStream?): Object? {
            var ois: ObjectInputStream? = null
            var o: Object? = null
            try {
                ois = ObjectInputStream(`is`)
                o = ois.readObject()
            } finally {
                IOUtil.close(ois)
            }
            return o
        }

        @Throws(IOException::class, ClassNotFoundException::class)
        fun deserialize(res: Resource?): Object? {
            return deserialize(res.getInputStream())
        }
    }
}