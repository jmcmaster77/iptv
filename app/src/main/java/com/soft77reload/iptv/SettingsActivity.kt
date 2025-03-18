package com.soft77reload.iptv


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.soft77reload.iptv.databinding.ActivitySettingsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class SettingsActivity : AppCompatActivity() {
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
    private var firstTime = true
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CoroutineScope(Dispatchers.IO).launch {
            getSettings().filter { firstTime }.collect { settingModel ->
                if (settingModel != null) {
                    username = settingModel.username
                    password = settingModel.password
                    ip = settingModel.ip
                    port = settingModel.port

                    Log.i(
                        "Soft77reload Devs",
                        "validacion on settings $username - $password - $ip:$port"
                    )
                    runOnUiThread {
                        binding.tvIp.text = "$ip:$port"
                    }
                    firstTime = !firstTime
                }
            }
        }


        binding.edtIp.hint = "Escribe la IP"
        binding.edtPort.hint = "Escribe Port"

        binding.btnReset.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                resetData()
            }
            Toast.makeText(this, "valores restablecidos", Toast.LENGTH_SHORT).show()
        }

        binding.btnGuardarIp.setOnClickListener {
            val ip = binding.edtIp.editableText.toString().trim()
            if(ip.isNotEmpty()){

                //validar formato dip

                if(isValidIPAddress(ip)) {
                    CoroutineScope(Dispatchers.IO).launch {
                        saveIp(IP, ip)
                    }
                    Log.i("Soft77reload Devs", "Guardando la ip: $ip")
                    Toast.makeText(this, "Ip configurada", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this, "Ip no es valida", Toast.LENGTH_SHORT).show()
                }

            }else{
                Toast.makeText(this, "El campo no debe estar vacio por favor indicar la ip", Toast.LENGTH_SHORT).show()
            }

        }

        binding.btnGuardarPort.setOnClickListener {
            val port = binding.edtPort.editableText.toString().trim()

            if(port.isNotEmpty()){
                // validar formato port

                if(isValidPort(port)){
                    CoroutineScope(Dispatchers.IO).launch {
                        savePort(PORT, port)
                    }
                    Log.i("Soft77reload Devs", "Guardando el puerto: $port")
                    Toast.makeText(this, "Port configurado", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this, "Port no es valido", Toast.LENGTH_SHORT).show()
                }

            }else{
                Toast.makeText(this, "El campo no debe estar vacio por favor indicar el puerto", Toast.LENGTH_SHORT).show()
            }

        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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

    private suspend fun resetData() {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(USERNAME)] = "nouser";
            preferences[stringPreferencesKey(PASSWORD)] = "nopass";
            preferences[stringPreferencesKey(IP)] = "192.168.45.25";
            preferences[stringPreferencesKey(PORT)] = "80";
            preferences[booleanPreferencesKey(LOGGED)] = false;
        }
        val intent = Intent("ACTION_FINISH_MAIN")
        sendBroadcast(intent)
        navigateToLogin()

    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()

    }

    private fun isValidIPAddress(ip: String): Boolean {
        val regex = Regex(
            pattern = "^(([0-9]{1,3})\\.){3}([0-9]{1,3})\$"
        )
        return regex.matches(ip) && ip.split(".").all {it.toInt() in 0..255 }
    }

    private suspend fun saveIp(key: String, ip: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(key)] = ip
        }
    }

    private fun isValidPort(port: String): Boolean {
        return try {
            val portNumber = port.toInt()
            portNumber in 0..65535
        } catch (e: NumberFormatException) {
            false
        }
    }

    private suspend fun savePort(key:String, port:String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(key)] = port
        }
    }
}