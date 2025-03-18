package com.soft77reload.iptv

import android.content.Context
import android.content.BroadcastReceiver
import android.content.Intent
import androidx.datastore.core.IOException
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.MediaController
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.recyclerview.widget.LinearLayoutManager
import com.soft77reload.iptv.FullscreenActivity.Companion.VIDEOURL
import com.soft77reload.iptv.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import  org.videolan.libvlc.LibVLC
import  org.videolan.libvlc.MediaPlayer
import  org.videolan.libvlc.Media


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var adapter: CanalesAdapter
    private val canalesList = mutableListOf<String>()
    private val canales = mutableListOf<Canal>()

    private lateinit var username: String
    private lateinit var password: String
    private lateinit var ip: String
    private lateinit var port: String

    //    vlc var
    private lateinit var libVLC: LibVLC
    private lateinit var mediaPlayer: MediaPlayer
    private var urlSelected:Boolean = false
    private lateinit var videoUrl: String

    private var firstTime = true

    private val finishReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            finish()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListeners()
        initRecyclerView()

        initVLC()

        CoroutineScope(Dispatchers.IO).launch {
            getSettings().filter { firstTime }.collect { settingModel ->
                if (settingModel != null) {
                    username = settingModel.username
                    password = settingModel.password
                    ip = settingModel.ip
                    port = settingModel.port
                    val logged = settingModel.logged
                    Log.i(
                        "Soft77reload Devs",
                        "validacion on main $username - $password - $ip:$port-  logged: $logged"
                    )

                    getListCanales(username, password)

                    firstTime = !firstTime
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // esta llorando por algo pero funciona ^^
        registerReceiver(finishReceiver, IntentFilter("ACTION_FINISH_MAIN"))

    }

    override fun onRestart() {
        super.onRestart()
        initVLC()
        loadUrlToVideoView(videoUrl)

    }



    private fun initVLC() {
        val args = arrayListOf(
            "--vout=android-display"
        )
        libVLC = LibVLC(this, args)
        mediaPlayer = MediaPlayer(libVLC)
        mediaPlayer.attachViews(binding.vlcVL1, null, false, false)
    }


    private fun initRecyclerView() {
        adapter = CanalesAdapter (canalesList) {canal -> loadCanalUrl(canal)}
        binding.rvCanales.layoutManager = LinearLayoutManager(this)
        binding.rvCanales.adapter = adapter
    }

    private fun getRetrofit(): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://$ip:$port/playlist/")
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
        return retrofit

    }

    private fun isSingleLineOrM3u(data: String) {


        // val sanitizedString = m3uString.replace("\n", "\\n")

        if (data.contains("\n")) {
            // en formato lista
            Log.i("Soft77reload Devs", "Data en Lista m3u8")
            val lineas = data.lines()
            // Log.i("Soft77reload Devs", "$lineas")
            var name: String? = null
            lineas.forEach { linea ->
                when {
                    linea.startsWith("#EXTINF") -> {
                        // Extrae el nombre del canal
                        name = linea.substringAfter(",")

                    }

                    linea.startsWith("http") -> {
                        // Crea un objeto Canal con el nombre y la url
                        name?.let {
                            if (linea.contains("http")){
                                val canal = Canal(name = it, url = linea)
                                canales.add(canal)
                            }
                                // filtrando y reemplazando .m3u8
//                            if (linea.contains(".m3u8")) {
//                                val canal = Canal(name = it, url = linea)
//                                canales.add(canal)
//                                //Log.i("Soft77reload Devs", "$canal" )
//                            }else if (linea.contains(".mp4")){
//                                val canal = Canal(name = it, url = linea)
//                                canales.add(canal)
//                            } else {
//                                var newlinea = linea.replaceLast("/", ".")
//                                val canal = Canal(name = it, url = newlinea)
//                                //Log.i("Soft77reload Devs", "$canal" )
//                                canales.add(canal)
//                            }
                        }
                        name = null
                    }
                }
            }
            // lista de canales completa | Pasando datos al RecyclerView
            // canales.forEach { item -> println("Soft77reload Devs - ${item.name} - ${item.url} ") }
            loadUrlToVideoView(canales.first().url)
            canalesList.clear()
            canales.forEach { item -> canalesList.add(item.name) }

            adapter.notifyDataSetChanged()


        } else {
            // en una sola linea
            val lineas = data.split("\\n")
            Log.i("Soft77reload Devs", "Data en singleLine")
            //Log.i("Soft77reload Devs", "$lineas")
            var name: String? = null
            lineas.forEach { linea ->
                when {
                    linea.startsWith("#EXTINF") -> {
                        // Extrae el nombre del canal
                        name = linea.substringAfter(",")

                    }

                    linea.startsWith("http") -> {
                        // Crea un objeto Canal con el nombre y la url
                        name?.let {
                            if (linea.contains(".m3u8")) {
                                val newLinea = linea.trim('"')
                                val canal = Canal(name = it, url = linea)
                                canales.add(canal)
                                //Log.i("Soft77reload Devs", "$canal" )
                            } else {
                                val newlinea = linea.replaceLast("/", ".").trim('"')
                                val canal = Canal(name = it, url = newlinea)
                                //Log.i("Soft77reload Devs", "$canal" )
                                canales.add(canal)
                            }
                        }
                        name = null
                    }
                }
            }
            // lista de canales completa // por pasar los datos al RecyclerVew

            // canales.forEach { item -> println("Soft77reload Devs - ${item.name} - ${item.url}") }
        }
    }
//    inicia cargando el primer canal
    private fun loadUrlToVideoView(url: String) {
        // Log.i("Soft77reload Devs", "video url $url")
//        val videoUrl: Uri = Uri.parse(url)
//        binding.vvOne.setVideoURI(videoUrl)
//        val mediaController = MediaController(this)
//        mediaController.setAnchorView(binding.vvOne)
//        binding.vvOne.setMediaController(mediaController)
//        binding.vvOne.start()
        videoUrl = url
        urlSelected = true
        val media = Media(libVLC, Uri.parse(videoUrl))
        mediaPlayer.media = media
        mediaPlayer.play()
    }

    private fun loadCanalUrl(canalName: String) {

        val url = findUrlByChannelName(canalName)
        if(url != null){
            Log.i("Soft77reload Devs", "Canal: $canalName - url:$url")
//            val videoUrl: Uri = Uri.parse(url)
//            binding.vvOne.setVideoURI(videoUrl)
//            val mediaController = MediaController(this)
//            mediaController.setAnchorView(binding.vvOne)
//            binding.vvOne.setMediaController(mediaController)
//            binding.vvOne.start()

            videoUrl = url
            val media = Media(libVLC, Uri.parse(videoUrl))
            mediaPlayer.media = media
            mediaPlayer.play()

        }else{
            showError("Error canal no encontrado")
        }
    }

    private fun findUrlByChannelName(canalName:String): String? {
        for(i in canales.indices){

            if (canales[i].name == canalName){
                return canales[i].url
            }
        }
        return null
    }

    // ruta a consultar http://127.0.0.1:5000/playlist/user/user123/m3u?output=hls
    private fun getListCanales(user: String, pass: String) {
        CoroutineScope(Dispatchers.IO).launch {

            try {
                val call = getRetrofit().create(ApiService::class.java)
                    .toChannelList("$user/$pass/m3u?output=hls")

                runOnUiThread {
                    if (call.isSuccessful) {

                        Log.i("Soft77reload Devs", "Datos recibidos ")

                        val m3uString: String = call.body().toString()
                        // la lista puede llegar en una solo linea o en una formato de lista
                        // Log.i("Soft77reload Devs", "$m3uString")

                        isSingleLineOrM3u(m3uString)


                    } else {
                        Log.i("Soft77reload Devs", "call: Error $call ")
                        showError("Usuario no registrado")
                    }
                }
            } catch (e: IOException) {
                Log.i("Soft77reload Devs", "Error IO IOException $e")
                runOnUiThread {
                    showError("Error no hay respuesta del servidor")
                }
            }
        }

    }

    fun String.replaceLast(oldValue: String, newValue: String): String {
        val lastIndex = this.lastIndexOf(oldValue)
        return if (lastIndex == -1) this else this.substring(
            0,
            lastIndex
        ) + newValue + this.substring(lastIndex + oldValue.length)
    }

    private fun getSettings(): Flow<SettingModel> {
        return dataStore.data.map { preferences ->
            SettingModel(
                username = preferences[stringPreferencesKey(LoginActivity.USERNAME)] ?: "nouser",
                password = preferences[stringPreferencesKey(LoginActivity.PASSWORD)] ?: "nopass",
                ip = preferences[stringPreferencesKey(LoginActivity.IP)] ?: "192.168.45.25",
                port = preferences[stringPreferencesKey(LoginActivity.PORT)] ?: "80",
                logged = preferences[booleanPreferencesKey(LoginActivity.LOGGED)] ?: false
            )

        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(finishReceiver)
        mediaPlayer.release()
        libVLC.release()
    }

    private fun initListeners() {
        binding.fabSetting.setOnClickListener {
            navigateToSetting()
        }

        binding.fabFullScreen.setOnClickListener {
            navigateToFullScreen()
        }
    }

    private fun navigateToFullScreen() {
        if(urlSelected){
            val intent = Intent(this, FullscreenActivity::class.java)
            intent.putExtra(VIDEOURL, videoUrl)
            startActivity(intent)
            mediaPlayer.release()
            libVLC.release()
        }
    }

    private fun navigateToSetting() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

 }