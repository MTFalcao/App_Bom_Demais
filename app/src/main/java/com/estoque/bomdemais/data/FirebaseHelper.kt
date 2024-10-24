package com.estoque.bomdemais.data

import android.util.Log
import com.google.firebase.database.*

class FirebaseHelper {

    private val database = FirebaseDatabase.getInstance()  // Declarado uma vez
    private val categoriesRef = database.getReference("categorias")
    private val productsRef = database.getReference("produtos")

    // Adicionar categoria ao Firebase
    fun addCategoria(category: String, callback: (Boolean) -> Unit) {
        val categoryId = categoriesRef.push().key
        if (categoryId != null) {
            categoriesRef.child(categoryId).setValue(category)
                .addOnCompleteListener { task ->
                    callback(task.isSuccessful)
                }
        } else {
            callback(false)
        }
    }

    // Buscar categorias do Firebase
    fun getCategorias(callback: (List<String>) -> Unit) {
        categoriesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categories = mutableListOf<String>()
                for (categorySnapshot in snapshot.children) {
                    val category = categorySnapshot.getValue(String::class.java)
                    category?.let { categories.add(it) }
                }
                callback(categories)
            }

            override fun onCancelled(error: DatabaseError) {
                // Tratar erros
                callback(emptyList())
            }
        })
    }

    // Adicionar produto ao Firebase
    fun addProduct(name: String, category: String) {
        val productId = productsRef.push().key  // Gera um ID único
        val product = Product(id = productId ?: "", name = name, category = category,description = category)
        if (productId != null) {
            productsRef.child(productId).setValue(product)
        }
    }

    // Buscar produtos do Firebase
    fun getProductsByCategory(category: String, callback: (List<Product>) -> Unit) {
        val categoryRef = productsRef.orderByChild("category").equalTo(category)
        categoryRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = mutableListOf<Product>()
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(Product::class.java)
                    product?.let { products.add(it) }
                }
                callback(products)
            }

            override fun onCancelled(error: DatabaseError) {
                // Retorna uma lista vazia em caso de erro
                callback(emptyList())
            }
        })
    }

    fun moveProductToLackCategory(product: Product) {
        val productRef = FirebaseDatabase.getInstance().getReference("produtos").child(product.id)

        // Update only the 'category' field to 'produtos em falta'
        productRef.child("category").setValue("Produtos em falta")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseHelper", "Product moved to 'produtos em falta' successfully.")
                } else {
                    Log.e("FirebaseHelper", "Failed to move product: ${task.exception?.message}")
                }
            }
    }


    companion object {
        fun updateProductQuantityInFirebase(product: Product) {
            // Caminho do produto no Firebase, usando o ID do produto
            val productRef = FirebaseDatabase.getInstance().getReference("produtos").child(product.id)

            // Atualiza a quantidade no Firebase
            productRef.child("quantity").setValue(product.quantity)

        }
    }
}
