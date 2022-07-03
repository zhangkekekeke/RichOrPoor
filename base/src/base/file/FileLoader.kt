package base.file

import base.Global
import base.util.log
import java.io.File

object FileLoader {
    val dirCache = Global.projectPath
    fun load(url: String): String? {
        val file = File(dirCache, url)
        //有缓存
        if (file.exists()) {
            val data=file.readText(Charsets.UTF_8)
            log(data)
            return data
        }
        return null
    }

}