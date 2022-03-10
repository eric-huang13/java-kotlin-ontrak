package com.insperity.escmobile.store.impl

import com.insperity.escmobile.action.Action
import com.insperity.escmobile.action.Actions
import com.insperity.escmobile.action.Keys
import com.insperity.escmobile.dispatcher.Dispatcher
import com.insperity.escmobile.event.OnAboutItems
import com.insperity.escmobile.store.LegalStore
import com.insperity.escmobile.store.Store
import org.greenrobot.eventbus.EventBus
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LegalStoreImpl @Inject constructor(dispatcher: Dispatcher, bus: EventBus) : Store(dispatcher, bus), LegalStore {

    companion object {
        private val REGEX_SCRIPT = "<a.*?class=\"legalLink\".*?>([\\w\\W]*?)</a>[\\w\\W]*?(<div[\\w\\W]*?class=\"legalInfo exp\">[\\w\\W]*?</div>)"
        private var aboutItemArray: MutableList<String>? = null
        private var aboutPageArray: MutableList<String>? = null
    }

    override fun onActionReceived(action: Action) {
        when (action.type) {
            Actions.LEGAL_RECEIVED -> {
                val htmlContent = action.getByKey(Keys.LEGAL_RESPONSE) as String
                parsePages(htmlContent)
                emitChange(OnAboutItems(aboutItemArray))
            }
            Actions.LEGAL_ERROR -> {
            }
            else -> logOnActionNotCaught(action.type)
        }
    }

    private fun parsePages(htmlPages: String) {
        val p = Pattern.compile(REGEX_SCRIPT) // Create a pattern to match
        val m = p.matcher(htmlPages) // Create a matcher with an input string

        if (aboutItemArray == null) {
            aboutItemArray = ArrayList()
        }
        if (aboutPageArray == null) {
            aboutPageArray = ArrayList()
        }
        aboutItemArray?.clear()
        aboutPageArray?.clear()
        while (m.find()) {
            val title = m.group(1)
            val body = m.group(2)
            aboutItemArray?.add(title)
            aboutPageArray?.add(createHtmlPage(title, body))
        }
    }

    private fun createHtmlPage(title: String, body: String): String {
        var htmlPage = "<html>" +
                "<head>" +
                "<title>{TITLE}</title>" +
                "<style>" +
                "@font-face {font-family:NotoSans;src: url('fonts/NotoSans-Regular.ttf');}" +
                "body {background-color:#fff;color:#333;padding-left:4px;font-family:NotoSans,Times New Roman;}" +
                ".legalLink {clear:both; display:block;padding:5px;text-decoration:none;}" +
                ".legalInfo {font-size:12px;}" +
                ".legalInfo h1 {color:#439639;font-size:18px;padding-left:0px;}" +
                ".legalInfo sup {font-size:10px;}" +
                ".legalInfo ul li { list-style-type: none; font-size: 12px; padding: 2px 0px; }" +
                "</style>" +
                "</head>" +
                "<body>{BODY}</body></html>"

        htmlPage = htmlPage.replace("{TITLE}", title)
        htmlPage = htmlPage.replace("{BODY}", body)
        return htmlPage
    }

    override fun getPageTitle(id: Int) = aboutItemArray?.get(id)

    override fun getPageContent(id: Int) = aboutPageArray?.get(id)
}
