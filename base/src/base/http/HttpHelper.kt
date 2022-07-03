package base.http

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection

class HttpHelper {
    var httpCache = HttpCache()

    fun httpGet(path: String, headerMap: Map<String, String>?): String? {
        val response = httpCache.check(path)
        if (response?.isNotEmpty() == true)
            return response//有缓存 返回

        val url = URL(path)
        //获取URL连接，open方法返回一个URLConnection类的对象
        val conn = url.openConnection()
        //设置请求头
        if (headerMap != null) {
            for (entry: Map.Entry<String, String> in headerMap) {
                conn.addRequestProperty(entry.key, entry.value)
            }
        }

        val newRepsones = getHttpRespone(conn)
        //保存缓存
        //保存缓存
        httpCache.save(path, newRepsones)
        return newRepsones
    }

    fun httpPost(path: String, headerMap: Map<String, String>?): String? {

        val url = URL(path)
        //获取URL连接，open方法返回一个URLConnection类的对象
        val conn = url.openConnection() as HttpURLConnection
        //设置请求头
        if (headerMap != null) {
            for (entry: Map.Entry<String, String> in headerMap) {
                conn.addRequestProperty(entry.key, entry.value)
            }
        }
        //设置请求方式为POST
        conn.setRequestMethod("POST");
        val newRepsones = getHttpRespone(conn)

        return newRepsones
    }

    private fun getHttpRespone(conn: URLConnection): String? {
        System.out.println("正在请求网络：" + conn.url)
        try {
            //从连接获取输入流，请求的输入也就是对请求的输入，即是相应，
            val `in` = conn.getInputStream()
            val `is` = InputStreamReader(`in`, "UTF-8")

            //将字节流转换成字符流，方便操作
            val br = BufferedReader(`is`)
            var line = StringBuilder()
            while (br.readLine()?.also {
                        line.append(it)
                    }?.isNotEmpty() == true) {
            }
            return line.toString()
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

}