package com.rich.model

import base.file.FileLoader
import base.http.HttpHelper
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rich.model.bean.BondsBean
import com.rich.model.bean.BondsList
import com.rich.model.bean.WindABean
import com.rich.model.bean.WindJsonList

/**
 * 原始数据模块
 */
class OriginDataModel {
    fun loadWindAList(): List<WindABean> {
        val windList = ArrayList<WindABean>()

        val windPrice = FileLoader.load("wdqa.txt")
        val windPriceJsonList = Gson().fromJson(windPrice, WindJsonList::class.java)

        val windPe = FileLoader.load("wdqa-pe.txt")
        val windPEJsonList = Gson().fromJson(windPe, WindJsonList::class.java)?.datas


        windPriceJsonList?.datas?.forEachIndexed { index, pairs ->
            val time = pairs.first
            val pePair = windPEJsonList?.get(index)
            val pe = if (pePair?.first == time) pePair.second else 0f
            windList.add(WindABean(time, pairs.second, pe))
        }

        return windList
    }


    fun load10YearBonds(): List<BondsBean>? {
        val dondsList = ArrayList<BondsBean>()

        val windPe = FileLoader.load("10YearBonds.txt")
        Gson().fromJson(windPe, BondsList::class.java)?.datas?.let {
            dondsList.addAll(it)
        }
        return dondsList
    }

    /**
     * 查询基金详情
     */
    fun query10YearBondsDate(): JsonObject? {
        val url = "https://cn.investing.com/common/modules/js_instrument_chart/api/data.php?pair_id=29227&pair_id_for_news=29227&chart_type=area&pair_interval=month&candle_count=120&events=yes&volume_series=yes&period=max"
//        val url = "https://cn.investing.com/common/modules/js_instrument_chart/api/data.php?pair_id=29227&pair_id_for_news=29227&chart_type=area&pair_interval=86400&candle_count=120&events=yes&volume_series=yes&period="
        val headers = HashMap<String, String>()

        headers.put("cookie", "adBlockerNewUserDomains=1652080095; udid=6125aca2ff6b2c1a536cc5ec59ab0f40; adbBLk=6; _ga_H1WYEJQ780=GS1.1.1652405168.3.1.1652405486.19; _ga=GA1.2.677220839.1652152342; upa=eyJpbnZfcHJvX2Z1bm5lbCI6IiIsIm1haW5fYWMiOiIxIiwibWFpbl9zZWdtZW50IjoiMiIsImRpc3BsYXlfcmZtIjoiMTMyIiwiYWZmaW5pdHlfc2NvcmVfYWNfZXF1aXRpZXMiOiI3IiwiYWZmaW5pdHlfc2NvcmVfYWNfY3J5cHRvY3VycmVuY2llcyI6IjkiLCJhZmZpbml0eV9zY29yZV9hY19jdXJyZW5jaWVzIjoiOSIsImFjdGl2ZV9vbl9pb3NfYXBwIjoiMCIsImFjdGl2ZV9vbl9hbmRyb2lkX2FwcCI6IjAiLCJhY3RpdmVfb25fd2ViIjoiMSIsImludl9wcm9fdXNlcl9zY29yZSI6IjAifQ%3D%3D; PHPSESSID=5bg245f8tq6f4qg4011auk1754; comment_notification_213086829=1; geoC=CN; Adsfree_conversion_score=3; adsFreeSalePopUp8c3a9243760b85ddb47ebda50169568c=1; r_p_s_n=1; __cflb=04dToRbvTbLk4kLGJXyVjsASqHox7JtcweAhrAGCVS; Hm_lvt_a1e3d50107c2a0e021d734fe76f85914=1656838130; SideBlockUser=a%3A2%3A%7Bs%3A10%3A%22stack_size%22%3Ba%3A1%3A%7Bs%3A11%3A%22last_quotes%22%3Bi%3A8%3B%7Ds%3A6%3A%22stacks%22%3Ba%3A1%3A%7Bs%3A11%3A%22last_quotes%22%3Ba%3A1%3A%7Bi%3A0%3Ba%3A3%3A%7Bs%3A7%3A%22pair_ID%22%3Bs%3A5%3A%2229227%22%3Bs%3A10%3A%22pair_title%22%3Bs%3A0%3A%22%22%3Bs%3A9%3A%22pair_link%22%3Bs%3A37%3A%22%2Frates-bonds%2Fchina-10-year-bond-yield%22%3B%7D%7D%7D%7D; _gid=GA1.2.1263511259.1656838510; smd=6125aca2ff6b2c1a536cc5ec59ab0f40-1656841419; cf_chl_2=3497ade7645e158; cf_chl_prog=x25; cf_clearance=pvvy2M4MjJgy8RUPbYglDv8glEweKKnnB2u1aTKRvhY-1656842152-0-150; UserReactions=true; __cf_bm=Wz1vywqyRfNO91A2NXFM_KoHbAsbDFAQ37___W4afZE-1656842161-0-AbGgDkhXcsbsIZL0+zc8RAAg/dvrfB7S5KYyqLTR18ilgWuAMrUpMx4ae5F6sI5+JHU000/XkdNoN7aEsRL3mOyuxEPD8QcHb0A2YHfNHy65klKiLCj/J2OTU8BJZpeGxJG0f0iTrJjtEMvuHHpC0+kJV2T35EjtKuiS59/ycurS; hide_ads_free_header_strip=1; nyxDorf=Mzc%2FbTZkZCZhNT01ZDJkeGc3M24%2BOmd7NjU0Njs8; invpc=18; Hm_lpvt_a1e3d50107c2a0e021d734fe76f85914=1656842432; ses_id=M30zcmRrMTk%2Fezs9YTA0NzNhYzhibTI2MTg0NTE%2FMiQzJ2NtYTY1czA%2FYS9gYzYqMThjZGIyYTJnZDRuY2VlNDMxM2lkYTFoP2A7ZmFnNDczZWM9YjQyYjFjNGUxMDI8MzRjYGFlNWUwMmFvYGE2PzEjY39iJmFwZzU0ZGMiZSIzPDNyZDQxbz86OzZhNDQ%2BMzZjOWJsMjExOTRmMTAyKjN4")
        headers.put("accept", "application/json, text/javascript, */*; q=0.01")
        headers.put("accept-encoding", "gzip, deflate, br")
        headers.put("accept-language", "zh-CN,zh;q=0.9")
        headers.put("referer", "https://cn.investing.com/rates-bonds/china-10-year-bond-yield")
        headers.put("sec-ch-ua", "\"Chromium\";v=\"21\", \" Not;A Brand\";v=\"99\"")
        headers.put("sec-ch-ua-mobile", "?0")
        headers.put("sec-ch-ua-platform", "Windows")
        headers.put("sec-fetch-dest", "empty")
        headers.put("sec-fetch-mode", "cors")
        headers.put("sec-fetch-site", "same-origin")
        headers.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36")
        headers.put("x-requested-with", "XMLHttpRequest")
        val httpHelper = HttpHelper()
        val respone: String? = httpHelper.httpGet(url, headers)
        var jsonData: JsonObject? = null
        try {
            jsonData = Gson().fromJson(respone, JsonObject::class.java)
        } catch (ignore: Exception) {
            ignore.printStackTrace()
            httpHelper.httpCache.delete(url)
        }
        return jsonData
    }
}