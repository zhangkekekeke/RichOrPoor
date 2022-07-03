package base.http

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * java解析 html数据
 */
class JsoupClient {

    fun getJsoup(url: String): Document {
        val document = Jsoup.connect(url).get();
        return document
    }
}