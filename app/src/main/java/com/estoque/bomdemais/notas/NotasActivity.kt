package com.estoque.bomdemais.notas

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.estoque.bomdemais.R

class NotasActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notas)

        // Configurar o botão de retorno
        val btnReturn = findViewById<ImageView>(R.id.btn_return)
        btnReturn.setOnClickListener {
            finish() // Fecha a atividade atual e volta para a anterior (menu)
        }
    }
}
