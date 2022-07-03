package base.http

import base.Global.Companion.projectPath
import java.io.File

class HttpCache {
    val dirCache = projectPath + File.separator + "httpCache"
    fun check(url: String): String? {
        val file = File(dirCache,parseName(url))
        //有缓存
        if (file.exists()) {
            val respones = file.readText(Charsets.UTF_8)
            return respones
        }

        return null
    }

    fun save(url: String, response: String?) {
        val root = File(dirCache)
        if (!root.exists()) {
            root.mkdirs()
        }

        val file = File(dirCache, parseName(url))
        //有缓存
        if (file.exists()) {
            file.delete()
        }
        if (response != null) {
            file.writeText(response, Charsets.UTF_8)
        }
    }

    fun delete(url: String) {
        val file = File(dirCache, parseName(url))
        //有缓存
        if (file.exists()) {
            file.delete()
        }
    }

    private fun parseName(url: String): String {
        return url.let {
            return@let it.replace("/", "").replace("http", "").replace(":", "")
                    .replace(".", "").replace("&", "").replace("=", "")
                    .replace("?", "")
        }
    }
}