package com.estoque.bomdemais.categorias

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.data.FirebaseHelper
import com.estoque.bomdemais.produtos.ProdutosActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CategoriasActivity : AppCompatActivity() {

    private lateinit var recyclerViewCategorias: RecyclerView
    private lateinit var recyclerViewProdutosEmFalta: RecyclerView
    private lateinit var adapterCategorias: CategoriasAdapter
    private lateinit var adapterProdutosEmFalta: CategoriasAdapter
    private lateinit var firebaseHelper: FirebaseHelper
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categorias)

        // Inicializando o Firebase
        firebaseHelper = FirebaseHelper()
        database = FirebaseDatabase.getInstance().getReference("categorias")

        val returnButton = findViewById<ImageView>(R.id.btn_return)
        returnButton.setOnClickListener {
            finish() // Retorna para a MainActivity
        }

        // Configurando RecyclerView para categorias normais
        recyclerViewCategorias = findViewById(R.id.recycler_view_categorias)
        recyclerViewCategorias.layoutManager = GridLayoutManager(this, 2)

        // Configurando RecyclerView para produtos em falta
        recyclerViewProdutosEmFalta = findViewById(R.id.recycler_view_produtos_em_falta)
        recyclerViewProdutosEmFalta.layoutManager = LinearLayoutManager(this)

        // Adapter vazio por enquanto
        adapterCategorias = CategoriasAdapter(mutableListOf()) { categoria ->
            // Clique em uma categoria
            val intent = Intent(this, ProdutosActivity::class.java)
            intent.putExtra("categoria_nome", categoria) // Passa o nome da categoria
            startActivity(intent) // Inicia a ProdutosActivity
        }
        recyclerViewCategorias.adapter = adapterCategorias


        adapterProdutosEmFalta = CategoriasAdapter(mutableListOf()) { categoria ->
            // Lógica se necessário para produtos em falta
            val intent = Intent(this, ProdutosActivity::class.java)
            intent.putExtra("categoria_nome", categoria)
            startActivity(intent)
        }
        recyclerViewProdutosEmFalta.adapter = adapterProdutosEmFalta

        // Botão para adicionar nova categoria
        val fabAddCategoria = findViewById<FloatingActionButton>(R.id.fab_add_categoria)
        fabAddCategoria.setOnClickListener {
            showAddCategoriaDialog()
        }

        loadCategoriesFromFirebase()
    }

    private fun showAddCategoriaDialog() {
        val input = EditText(this)
        input.hint = "Nome da Categoria"

        MaterialAlertDialogBuilder(this)
            .setTitle("Adicionar Categoria")
            .setView(input)
            .setPositiveButton("Adicionar") { _, _ ->
                val categoria = input.text.toString()
                if (categoria.isNotEmpty()) {
                    addCategoriaToDatabase(categoria)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun addCategoriaToDatabase(categoria: String) {
        // Adiciona a categoria no Firebase
        firebaseHelper.addCategoria(categoria) { success ->
            if (success) {
                Toast.makeText(this, "Categoria '$categoria' adicionada!", Toast.LENGTH_SHORT).show()
                adapterCategorias.addCategoria(categoria) // Adiciona no adaptador de categorias
            } else {
                Toast.makeText(this, "Falha ao adicionar categoria.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadCategoriesFromFirebase() {
        firebaseHelper.getCategorias { categorias ->
            // Atualiza o adaptador de produtos em falta
            adapterProdutosEmFalta.categorias.clear()
            adapterProdutosEmFalta.categorias.add("Produtos em falta") // Adiciona a categoria "Produtos em falta"
            adapterProdutosEmFalta.notifyDataSetChanged()

            // Atualiza o adaptador de categorias normais
            adapterCategorias.categorias.clear()
            adapterCategorias.categorias.addAll(categorias) // Adiciona apenas as categorias normais
            adapterCategorias.notifyDataSetChanged()
        }
    }

}
