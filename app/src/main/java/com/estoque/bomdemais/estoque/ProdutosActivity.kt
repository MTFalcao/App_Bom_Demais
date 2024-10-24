package com.estoque.bomdemais.produtos

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.estoque.bomdemais.data.FirebaseHelper
import com.estoque.bomdemais.data.Product

class ProdutosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProdutosAdapter
    private lateinit var firebaseHelper: FirebaseHelper
    private lateinit var categoria: String
    private lateinit var topBarText: TextView // TextView para exibir o nome da categoria
    private val zeroQuantityProducts = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_produtos)

        // Inicializando o FirebaseHelper
        firebaseHelper = FirebaseHelper()

        val returnButton = findViewById<ImageView>(R.id.btn_return)
        returnButton.setOnClickListener {
            if (categoria != "Produtos em falta") { // Only show the dialog for other categories
                showConfirmationDialog()
            } else {
                finish() // Return directly
            }
        }

        // Obtendo a categoria passada pela Intent
        categoria = intent.getStringExtra("categoria") ?: ""  // Ajuste aqui para "categoria"

        // Configurando o RecyclerView para exibir produtos
        recyclerView = findViewById(R.id.recycler_view_produtos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inicializando o Adapter vazio por enquanto
        adapter = ProdutosAdapter(mutableListOf()) { produto ->
            Toast.makeText(this, "Produto selecionado: ${produto.name}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter

        // Exibir o nome da categoria na barra superior
        topBarText = findViewById(R.id.top_bar_text) // ID do TextView no seu layout
        topBarText.text = categoria // Definindo o nome da categoria no TextView

        // Botão para adicionar novo produto
        val fabAddProduto = findViewById<FloatingActionButton>(R.id.fab_add_produto)
        fabAddProduto.setOnClickListener {
            showAddProdutoDialog()
        }

        // Carregar produtos da categoria no Firebase
        loadProductsFromFirebase()
        setupSearchBar()
    }

    private fun showAddProdutoDialog() {
        val input = EditText(this)
        input.hint = "Nome do Produto"

        MaterialAlertDialogBuilder(this)
            .setTitle("Adicionar Produto")
            .setView(input)
            .setPositiveButton("Adicionar") { _, _ ->
                val produto = input.text.toString()
                if (produto.isNotEmpty()) {
                    addProdutoToDatabase(produto)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun addProdutoToDatabase(produto: String) {
        firebaseHelper.addProduct(produto, categoria)  // Categoria já está definida
        Toast.makeText(this, "'$produto' adicionado!", Toast.LENGTH_SHORT).show()
        adapter.addProduto(produto) // Adiciona no adapter para mostrar na lista
    }

    private fun loadProductsFromFirebase() {
        // Passa a categoria correta para buscar os produtos daquela categoria
        firebaseHelper.getProductsByCategory(categoria) { produtos ->
            adapter.updateProducts(produtos)
        }
    }

    private fun setupSearchBar() {
        val searchBar = findViewById<EditText>(R.id.search_bar)

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filtrar(s.toString())  // Filter products as user types
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun showConfirmationDialog() {
        if (zeroQuantityProducts.isNotEmpty()) {
            val productNames = zeroQuantityProducts.joinToString("\n") { it.name }

            MaterialAlertDialogBuilder(this)
                .setTitle("Mover Produtos")
                .setMessage("Os seguintes produtos estao zerados:\n\n$productNames\n\n" +
                        "Deseja movê-los para 'produtos em falta'?")
                .setPositiveButton("Confirmar") { _, _ ->
                    moveProductsToLack(zeroQuantityProducts)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        } else {
            finish()  // If no products to move, finish activity
        }
    }

    fun addZeroQuantityProduct(product: Product) {
        if (!zeroQuantityProducts.contains(product)) {
            zeroQuantityProducts.add(product)
        }
    }

    private fun moveProductsToLack(products: List<Product>) {
        products.forEach { product ->
            firebaseHelper.moveProductToLackCategory(product)  // Update category in Firebase
            adapter.removeProduct(product)  // Remove from adapter
        }
        zeroQuantityProducts.clear()  // Clear the tracking list
        Toast.makeText(this, "Produtos movidos com sucesso", Toast.LENGTH_SHORT).show()
    }

}
