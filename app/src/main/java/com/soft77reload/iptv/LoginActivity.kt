package com.soft77reload.iptv

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.soft77reload.iptv.databinding.ActivityLoginBinding
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

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "setting")

class LoginActivity : AppCompatActivity() {



    companion object {
        const val USERNAME = "username"
        const val PASSWORD = "password"
        const val IP = "ip"
        const val PORT = "port"
        const val LOGGED = "logged"
    }

    private lateinit var username: String
    private lateinit var password: String
    private lateinit var ip: String
    private lateinit var port: String
    private lateinit var binding: ActivityLoginBinding

    private var firstTime = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnLogin.setOnClickListener {
            username = binding.usernameInput.text.toString().trim()
            password = binding.passwordInput.text.toString().trim()

            if (username.isNotEmpty() and password.isNotEmpty()) {

                login(username, password)


            } else {
                Toast.makeText(
                    this,
                    "Ni el usuario o el password deben estar vacios",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

        binding.fabSetting.setOnClickListener {
            navigateToSetting()
        }

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
                        "validacion on login $username - $password - $ip:$port-  logged: $logged"
                    )

                    if (logged) {
                        navigateToMain()
                        finish()
                    } else {
                        runOnUiThread {
                            showGreet()
                        }

                    }
                    firstTime = !firstTime
                }
            }
        }

    }

    private fun getRetrofit(): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://$ip:$port/webplayer/")
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
        return retrofit

    }

    private fun login(user: String, pass: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val call = getRetrofit().create(ApiService::class.java)
                    .toLogin("login?username=$user&password=$pass")

                runOnUiThread {
                    if (call.isSuccessful) {

                        CoroutineScope(Dispatchers.IO).launch {
                            saveUser(USERNAME, username)
                            saveUser(PASSWORD, password)
                            saveLogged(true)
                        }
                        navigateToMain()
                        Log.i("Soft77reload Devs", "loging satisfactorio")
                        finish()


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

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private suspend fun saveUser(key: String, data: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(key)] = data
            Log.i("Soft77reload Devs", "Store user data key:$key data:$data")
        }
    }

    private suspend fun saveLogged(status: Boolean) {
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(LOGGED)] = status
        }
    }

    private fun getSettings(): Flow<SettingModel> {
        return dataStore.data.map { preferences ->
            SettingModel(
                username = preferences[stringPreferencesKey(USERNAME)] ?: "nouser",
                password = preferences[stringPreferencesKey(PASSWORD)] ?: "nopass",
                ip = preferences[stringPreferencesKey(IP)] ?: "192.168.45.25",
                port = preferences[stringPreferencesKey(PORT)] ?: "80",
                logged = preferences[booleanPreferencesKey(LOGGED)] ?: false
            )

        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToSetting() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun showGreet() {
        Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()
    }

}