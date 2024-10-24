package com.estoque.bomdemais.produtos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.data.FirebaseHelper
import com.estoque.bomdemais.data.Product

class ProdutosAdapter(
    private var productList: MutableList<Product>,
    private val onClick: (Product) -> Unit
) : RecyclerView.Adapter<ProdutosAdapter.ProdutoViewHolder>() {

    private var productListFiltrada: MutableList<Product> = productList.toMutableList()

    class ProdutoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.text_product_name)
        val quantityTextView: TextView = view.findViewById(R.id.text_quantity)
        val btnIncrease: Button = view.findViewById(R.id.btn_increase)
        val btnDecrease: Button = view.findViewById(R.id.btn_decrease)
        val categoryTextView: TextView = view.findViewById(R.id.text_product_category)
    }

    fun getProductsWithZeroQuantity(): List<Product> {
        return productListFiltrada.filter { it.quantity == 0 }
    }

    // New method to remove a product
    fun removeProduct(product: Product) {
        val position = productListFiltrada.indexOf(product)
        if (position >= 0) {
            productListFiltrada.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdutoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProdutoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProdutoViewHolder, position: Int) {
        val product = productListFiltrada[position]

        holder.nameTextView.text = product.name
        holder.quantityTextView.text = product.quantity.toString()
        holder.categoryTextView.text = product.description

        // Botão para aumentar a quantidade
        holder.btnIncrease.setOnClickListener {
            product.quantity++
            FirebaseHelper.updateProductQuantityInFirebase(product)  // Salva a nova quantidade no Firebase
            notifyItemChanged(position)  // Atualiza a interface
        }

        holder.btnDecrease.setOnClickListener {
            if (product.quantity > 0) {
                product.quantity--
                FirebaseHelper.updateProductQuantityInFirebase(product)  // Salva a nova quantidade no Firebase
                notifyItemChanged(position)  // Atualiza a interface

                if (product.quantity == 0) {
                    (holder.itemView.context as ProdutosActivity).addZeroQuantityProduct(product)
                }
            }
        }
        holder.itemView.setOnClickListener { onClick(product) }
    }

    override fun getItemCount() = productListFiltrada.size

    // Atualizar lista de produtos
    fun updateProducts(newProducts: List<Product>) {
        productList.clear()
        productList.addAll(newProducts)
        productListFiltrada = productList.toMutableList() // Reseta a lista filtrada
        notifyDataSetChanged()
    }

    fun addProduto(produto: String) {
        // Cria um novo produto com o nome fornecido e quantidade inicial 0
        val novoProduto = Product(name = produto, quantity = 0)
        productList.add(0, novoProduto)
        productListFiltrada.add(0, novoProduto) // Adiciona também na lista filtrada
        notifyItemInserted(0)
    }

    // Método para filtrar produtos
    fun filtrar(query: String) {
        productListFiltrada = if (query.isEmpty()) {
            productList.toMutableList()
        } else {
            productList.filter { it.name.contains(query, ignoreCase = true) }.toMutableList()
        }
        notifyDataSetChanged() // Atualiza o RecyclerView com a lista filtrada
    }
}
