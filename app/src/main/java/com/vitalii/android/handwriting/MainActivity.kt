package com.vitalii.android.handwriting

import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.gson.Gson
import com.vitalii.android.handwriting.recognition.PointCloud
import com.vitalii.android.handwriting.recognition.Recognizer
import com.vitalii.android.handwriting.recognition.StrokePoint
import com.vitalii.android.handwriting.recognition.normalize
import com.vitalii.android.handwriting.utilities.buildUrl
import com.vitalii.android.handwriting.utilities.getResponseFromHttpUrl
import com.vitalii.android.handwriting.utilities.sendPostToHttpUrl
import java.io.InputStreamReader
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL

class MainActivity : AppCompatActivity(),
        TeachFragment.OnInputListener,
        OptionsFragment.OnInputListener {

    private val TAG = this::class.java.simpleName

    private var mHostUrl: String = ""
    private var recognizer = Recognizer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        paintView.init(metrics)

        loadTemplates()
    }

    private fun loadTemplates() {
        Log.d(TAG, "loadTemplates: loading templates...")
        val streamReader = InputStreamReader(resources.openRawResource(R.raw.templates), "utf8")
        val templatesRaw = streamReader.readText()
        val templates = Gson().fromJson(templatesRaw, Array<PointCloud>::class.java)
        recognizer.setTemplates(templates)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_cancel -> paintView.cancel()
            R.id.action_clear -> paintView.clear()
            R.id.action_add -> TeachFragment().show(fragmentManager, "TeachFragment")
            R.id.action_options -> OptionsFragment().show(fragmentManager, "OptionsFragment")
            R.id.action_recognize -> recognize()
            R.id.action_sync -> fetchTemplates()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onTeachInput(name: String) {
        val template = paintView.getStrokes()
        recognizer.addTemplate(name, template)

        if (mHostUrl == "") return
        sendTemplate(name, template)
    }

    override fun onOptionsInput(input: String) {
        Log.d(TAG, "onOptionsInput: got input: $input")
        mHostUrl = input
        Log.d(TAG, "onOptionsInput: built ${buildUrl(mHostUrl)}")
    }

    private fun recognize() {
        val points = paintView.getStrokes()
        RecognizeTask().execute(points)
    }

    private inner class RecognizeTask : AsyncTask<ArrayList<StrokePoint>, Unit, String>() {
        override fun doInBackground(vararg params: ArrayList<StrokePoint>?): String {
            params[0]?.let { return recognizer.recognize(it) }
            return "Input error"
        }

        override fun onPostExecute(result: String?) {
            tv_recognized.text = result ?: "Input error"
        }

    }

    private fun sendTemplate(name: String, template: ArrayList<StrokePoint>) {
        Log.d(TAG, "sendTemplate: make POST request")
        val normalizedTemplate = normalize(template, recognizer.pointNum)
        val pointCloud = PointCloud(name, normalizedTemplate)
        val json = Gson().toJson(pointCloud)
        SendTemplateTask().execute(json)
    }

    private inner class SendTemplateTask : AsyncTask<String, Unit, Unit>() {
        override fun doInBackground(vararg params: String?) {
            val data = params[0] ?: return
            val url = buildUrl(mHostUrl)
            sendPostToHttpUrl(data, url)
        }
    }

    private fun fetchTemplates() {
        Log.d(TAG, "fetchTemplates: make GET request")
        val url = buildUrl(mHostUrl)
        FetchDataTask().execute(url)
    }

    private inner class FetchDataTask : AsyncTask<URL, Unit, String>() {
        override fun doInBackground(vararg urls: URL?): String {
            val url = urls[0] ?: return "[]"
            return getResponseFromHttpUrl(url) ?: "[]"
        }

        override fun onPostExecute(result: String) = updateTemplates(result)
    }

    private fun updateTemplates(data: String) {
        val templates = Gson().fromJson(data, Array<PointCloud>::class.java)
        Log.d(TAG, "updateTemplates: got ${templates.size} templates")
        recognizer.setTemplates(templates)
    }
}
