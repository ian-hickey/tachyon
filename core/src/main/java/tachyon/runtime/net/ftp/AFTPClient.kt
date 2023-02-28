package tachyon.runtime.net.ftp

import java.io.IOException

abstract class AFTPClient {
    /**
     * Renames a remote file.
     *
     *
     *
     * @param from The name of the remote file to rename.
     * @param to The new name of the remote file.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException If the FTP server prematurely closes the connection as a
     * result of the client being idle or some other reason causing the server to send
     * FTP reply code 421. This exception may be caught either as an IOException or
     * independently as itself.
     * @exception IOException If an I/O error occurs while either sending a command to the server or
     * receiving a reply from the server.
     * @throws FTPException
     */
    @Throws(IOException::class)
    abstract fun rename(from: String?, to: String?): Boolean

    /***
     * Returns the integer value of the reply code of the last FTP reply. You will usually only use this
     * method after you connect to the FTP server to check that the connection was successful since
     * ` connect ` is of type void.
     *
     *
     *
     * @return The integer value of the reply code of the last FTP reply.
     */
    abstract val replyCode: Int

    /***
     * Returns the entire text of the last FTP server response exactly as it was received, including all
     * end of line markers in NETASCII format.
     *
     *
     *
     * @return The entire text from the last FTP response as a String.
     */
    abstract val replyString: String?

    /**
     * Change the current working directory of the FTP session.
     *
     *
     *
     * @param pathname The new current working directory.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException If the FTP server prematurely closes the connection as a
     * result of the client being idle or some other reason causing the server to send
     * FTP reply code 421. This exception may be caught either as an IOException or
     * independently as itself.
     * @exception IOException If an I/O error occurs while either sending a command to the server or
     * receiving a reply from the server.
     */
    @Throws(IOException::class)
    abstract fun changeWorkingDirectory(pathname: String?): Boolean

    /**
     * Creates a new subdirectory on the FTP server in the current directory (if a relative pathname is
     * given) or where specified (if an absolute pathname is given).
     *
     *
     *
     * @param pathname The pathname of the directory to create.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException If the FTP server prematurely closes the connection as a
     * result of the client being idle or some other reason causing the server to send
     * FTP reply code 421. This exception may be caught either as an IOException or
     * independently as itself.
     * @exception IOException If an I/O error occurs while either sending a command to the server or
     * receiving a reply from the server.
     */
    @Throws(IOException::class)
    abstract fun makeDirectory(pathname: String?): Boolean

    /**
     * Using the default system autodetect mechanism, obtain a list of file information for the current
     * working directory or for just a single file.
     *
     *
     * This information is obtained through the LIST command. The contents of the returned array is
     * determined by the` FTPFileEntryParser ` used.
     *
     *
     *
     * @param pathname The file or directory to list. Since the server may or may not expand glob
     * expressions, using them here is not recommended and may well cause this method to
     * fail. Also, some servers treat a leading '-' as being an option. To avoid this
     * interpretation, use an absolute pathname or prefix the pathname with ./ (unix style
     * servers). Some servers may support "--" as meaning end of options, in which case "--
     * -xyz" should work.
     *
     * @return The list of file information contained in the given path in the format determined by the
     * autodetection mechanism
     * @exception FTPConnectionClosedException If the FTP server prematurely closes the connection as a
     * result of the client being idle or some other reason causing the server to send
     * FTP reply code 421. This exception may be caught either as an IOException or
     * independently as itself.
     * @exception IOException If an I/O error occurs while either sending a command to the server or
     * receiving a reply from the server.
     * @exception org.apache.commons.net.ftp.parser.ParserInitializationException Thrown if the
     * parserKey parameter cannot be resolved by the selected parser factory. In the
     * DefaultFTPEntryParserFactory, this will happen when parserKey is neither the fully
     * qualified class name of a class implementing the interface
     * org.apache.commons.net.ftp.FTPFileEntryParser nor a string containing one of the
     * recognized keys mapping to such a parser or if class loader security issues
     * prevent its being loaded.
     */
    @Throws(IOException::class)
    abstract fun listFiles(pathname: String?): Array<FTPFile?>?

    /**
     * Removes a directory on the FTP server (if empty).
     *
     *
     *
     * @param pathname The pathname of the directory to remove.
     * @param recursive if true it also can delete
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException If the FTP server prematurely closes the connection as a
     * result of the client being idle or some other reason causing the server to send
     * FTP reply code 421. This exception may be caught either as an IOException or
     * independently as itself.
     * @exception IOException If an I/O error occurs while either sending a command to the server or
     * receiving a reply from the server.
     */
    @Throws(IOException::class)
    abstract fun removeDirectory(pathname: String?): Boolean

    /**
     * Sets the file type to be transferred. This should be one of ` FTP.ASCII_FILE_TYPE `,
     * ` FTP.BINARY_FILE_TYPE`, etc. The file type only needs to be set when you want to
     * change the type. After changing it, the new type stays in effect until you change it again. The
     * default file type is ` FTP.ASCII_FILE_TYPE ` if this method is never called. <br></br>
     * The server default is supposed to be ASCII (see RFC 959), however many ftp servers default to
     * BINARY. **To ensure correct operation with all servers, always specify the appropriate file type
     * after connecting to the server.** <br></br>
     *
     *
     * **N.B.** currently calling any connect method will reset the type to FTP.ASCII_FILE_TYPE.
     *
     * @param fileType The ` _FILE_TYPE ` constant indcating the type of file.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException If the FTP server prematurely closes the connection as a
     * result of the client being idle or some other reason causing the server to send
     * FTP reply code 421. This exception may be caught either as an IOException or
     * independently as itself.
     * @exception IOException If an I/O error occurs while either sending a command to the server or
     * receiving a reply from the server.
     */
    @Throws(IOException::class)
    abstract fun setFileType(fileType: Int): Boolean

    /**
     * Retrieves a named file from the server and writes it to the given OutputStream. This method does
     * NOT close the given OutputStream. If the current file type is ASCII, line separators in the file
     * are converted to the local representation.
     *
     *
     * Note: if you have used [.setRestartOffset], the file data will start from the
     * selected offset.
     *
     * @param remote The name of the remote file.
     * @param local The local OutputStream to which to write the file.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException If the FTP server prematurely closes the connection as a
     * result of the client being idle or some other reason causing the server to send
     * FTP reply code 421. This exception may be caught either as an IOException or
     * independently as itself.
     * @exception org.apache.commons.net.io.CopyStreamException If an I/O error occurs while actually
     * transferring the file. The CopyStreamException allows you to determine the number
     * of bytes transferred and the IOException causing the error. This exception may be
     * caught either as an IOException or independently as itself.
     * @exception IOException If an I/O error occurs while either sending a command to the server or
     * receiving a reply from the server.
     */
    @Throws(IOException::class)
    abstract fun retrieveFile(remote: String?, local: OutputStream?): Boolean

    /**
     * Stores a file on the server using the given name and taking input from the given InputStream.
     * This method does NOT close the given InputStream. If the current file type is ASCII, line
     * separators in the file are transparently converted to the NETASCII format (i.e., you should not
     * attempt to create a special InputStream to do this).
     *
     *
     *
     * @param remote The name to give the remote file.
     * @param local The local InputStream from which to read the file.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException If the FTP server prematurely closes the connection as a
     * result of the client being idle or some other reason causing the server to send
     * FTP reply code 421. This exception may be caught either as an IOException or
     * independently as itself.
     * @exception org.apache.commons.net.io.CopyStreamException If an I/O error occurs while actually
     * transferring the file. The CopyStreamException allows you to determine the number
     * of bytes transferred and the IOException causing the error. This exception may be
     * caught either as an IOException or independently as itself.
     * @exception IOException If an I/O error occurs while either sending a command to the server or
     * receiving a reply from the server.
     */
    @Throws(IOException::class)
    abstract fun storeFile(remote: String?, local: InputStream?): Boolean

    /**
     * Deletes a file on the FTP server.
     *
     *
     *
     * @param pathname The pathname of the file to be deleted.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException If the FTP server prematurely closes the connection as a
     * result of the client being idle or some other reason causing the server to send
     * FTP reply code 421. This exception may be caught either as an IOException or
     * independently as itself.
     * @exception IOException If an I/O error occurs while either sending a command to the server or
     * receiving a reply from the server.
     */
    @Throws(IOException::class)
    abstract fun deleteFile(pathname: String?): Boolean

    /**
     * Returns the pathname of the current working directory.
     *
     *
     *
     * @return The pathname of the current working directory. If it cannot be obtained, returns null.
     * @exception FTPConnectionClosedException If the FTP server prematurely closes the connection as a
     * result of the client being idle or some other reason causing the server to send
     * FTP reply code 421. This exception may be caught either as an IOException or
     * independently as itself.
     * @exception IOException If an I/O error occurs while either sending a command to the server or
     * receiving a reply from the server.
     */
    @Throws(IOException::class)
    abstract fun printWorkingDirectory(): String?
    abstract val prefix: String?

    /**
     * @return The remote address to which the client is connected. Delegates to
     * [Socket.getInetAddress]
     * @throws NullPointerException if the socket is not currently open
     */
    abstract val remoteAddress: InetAddress?

    /**
     * Returns true if the client is currently connected to a server.
     *
     *
     * Delegates to [Socket.isConnected]
     *
     * @return True if the client is currently connected to a server, false otherwise.
     */
    abstract val isConnected: Boolean

    /***
     * A convenience method to send the FTP QUIT command to the server, receive the reply, and return
     * the reply code.
     *
     *
     *
     * @return The reply code received from the server.
     * @exception FTPConnectionClosedException If the FTP server prematurely closes the connection as a
     * result of the client being idle or some other reason causing the server to send
     * FTP reply code 421. This exception may be caught either as an IOException or
     * independently as itself.
     * @exception IOException If an I/O error occurs while either sending the command or receiving the
     * server reply.
     */
    @Throws(IOException::class)
    abstract fun quit(): Int

    /**
     * Closes the connection to the FTP server and restores connection parameters to the default values.
     *
     *
     *
     * @exception IOException If an error occurs while disconnecting.
     */
    @Throws(IOException::class)
    abstract fun disconnect()

    /**
     * timeout in milli seconds
     *
     * @param timeout
     */
    abstract fun setTimeout(timeout: Int)

    /**
     * Returns the current data connection mode (one of the ` _DATA_CONNECTION_MODE `
     * constants.
     *
     *
     *
     * @return The current data connection mode (one of the ` _DATA_CONNECTION_MODE `
     * constants.
     */
    abstract val dataConnectionMode: Int

    /**
     * Set the current data connection mode to ` PASSIVE_LOCAL_DATA_CONNECTION_MODE `. Use
     * this method only for data transfers between the client and server. This method causes a PASV (or
     * EPSV) command to be issued to the server before the opening of every data connection, telling the
     * server to open a data port to which the client will connect to conduct data transfers. The
     * FTPClient will stay in ` PASSIVE_LOCAL_DATA_CONNECTION_MODE ` until the mode is
     * changed by calling some other method such as [enterLocalActiveMode()][.enterLocalActiveMode]
     *
     *
     * **N.B.** currently calling any connect method will reset the mode to
     * ACTIVE_LOCAL_DATA_CONNECTION_MODE.
     */
    abstract fun enterLocalPassiveMode()

    /**
     * Set the current data connection mode to `ACTIVE_LOCAL_DATA_CONNECTION_MODE`. No
     * communication with the FTP server is conducted, but this causes all future data transfers to
     * require the FTP server to connect to the client's data port. Additionally, to accommodate
     * differences between socket implementations on different platforms, this method causes the client
     * to issue a PORT command before every data transfer.
     */
    abstract fun enterLocalActiveMode()
    @Throws(SocketException::class, IOException::class)
    abstract fun init(host: InetAddress?, port: Int, username: String?, password: String?, fingerprint: String?, stopOnError: Boolean)

    /**
     * Opens a Socket connected to a remote host at the specified port and originating from the current
     * host at a system assigned port. Before returning, [_connectAction_() ][._connectAction_] is
     * called to perform connection initialization actions.
     *
     *
     *
     * @param host The remote host.
     * @param port The port to connect to on the remote host.
     * @exception SocketException If the socket timeout could not be set.
     * @exception IOException If the socket could not be opened. In most cases you will only want to
     * catch IOException since SocketException is derived from it.
     * @throws FTPException
     */
    @Throws(SocketException::class, IOException::class)
    abstract fun connect()

    /***
     * Determine if a reply code is a positive completion response. All codes beginning with a 2 are
     * positive completion responses. The FTP server will send a positive completion response on the
     * final successful completion of a command.
     *
     *
     *
     * @param reply The reply code to test.
     * @return True if a reply code is a postive completion response, false if not.
     */
    abstract val isPositiveCompletion: Boolean
    @Throws(IOException::class)
    abstract fun directoryExists(pathname: String?): Boolean

    companion object {
        const val FILE_TYPE_BINARY = 1
        const val FILE_TYPE_TEXT = 2
        @Throws(IOException::class)
        fun getInstance(secure: Boolean, host: InetAddress?, port: Int, username: String?, password: String?, fingerprint: String?, stopOnError: Boolean): AFTPClient? {
            val client: AFTPClient = if (secure) SFTPClientImpl() else FTPClientImpl()
            client.init(host, port, username, password, fingerprint, stopOnError)
            return client
        }
    }
}