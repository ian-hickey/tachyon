package tachyon.runtime.util

import java.io.File

object FileList {
    private var count = 0
    @Throws(IOException::class)
    fun listFileName(file: File?) {
        if (file.isFile()) {
            // (file.toString().endsWith(".cfm") || file.toString().endsWith(".cfc"))
            if ((file.toString().endsWith(".cfm") || file.toString().endsWith(".cfc") || file.toString().endsWith(".js") || file.toString().endsWith(".css"))
                    && file.toString().indexOf("/old_") === -1) {
                if (file.getName().toLowerCase().startsWith("application.cf")) print.e(file)
            }
        } else if (file.isDirectory()) {
            val children: Array<File?> = file.listFiles()
            if (children != null) for (child in children) {
                list(child)
            }
        }
    }

    @Throws(IOException::class)
    fun list(file: File?) {
        if (file.isFile()) {
            // (file.toString().endsWith(".cfm") || file.toString().endsWith(".cfc"))
            if ((file.toString().endsWith(".cfm") || file.toString().endsWith(".cfc") || file.toString().endsWith(".js") || file.toString().endsWith(".css"))
                    && file.toString().indexOf("/old_") === -1) {
                val fis = FileInputStream(file)
                try {
                    val str: String = IOUtil.toString(fis, "UTF-8")
                    //
                    // if (str.indexOf("\"upload\"") != -1) print.e(file);
                    // else if (str.indexOf("'upload'") != -1) print.e(file);
                    // if (str.indexOf("filenameArray[filenameArray.length-1].toLowerCase() != 'jpg'") != -1)
                    // print.e((++count) + " " + file);
                    // if (str.indexOf("emailDuplicateArtworkError") != -1) print.e((++count) + " " + file);
                    if (str.indexOf("Invalid image format") !== -1) print.e((++count).toString() + " " + file)
                } finally {
                    IOUtil.close(fis)
                }
            }
        } else if (file.isDirectory()) {
            val children: Array<File?> = file.listFiles()
            if (children != null) for (child in children) {
                list(child)
            }
        }
    }

    @Throws(Exception::class)
    fun main(args: Array<String?>?) {
        val path = "/Users/mic/Projects/Distrokid/distrokid"
        val f = File(path)
        list(f)
    }
}