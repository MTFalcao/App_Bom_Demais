package com.estoque.bomdemais.categorias

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.estoque.bomdemais.R
import com.estoque.bomdemais.produtos.ProdutosActivity

class CategoriasAdapter(
    var categorias: MutableList<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<CategoriasAdapter.CategoriaViewHolder>() {

    class CategoriaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val frameLayout: FrameLayout =
            view.findViewById(R.id.frame_layout) // Adicionando o FrameLayout
        val textViewCategoria: TextView = view.findViewById(R.id.text_categoria)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_categoria, parent, false)
        return CategoriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        val categoria = categorias[position]
        holder.textViewCategoria.text = categoria

        if (categoria == "Produtos em falta") {
            holder.frameLayout.setBackgroundResource(R.drawable.button_produtos_em_falta)
        }

        holder.itemView.setOnClickListener {
            // Check if the category is "produtos em falta"
            if (categoria == "Produtos em falta") {
                // Start the ProdutosActivity for "produtos em falta"
                val context = holder.itemView.context
                val intent = Intent(context, ProdutosActivity::class.java)
                intent.putExtra("categoria", categoria) // Pass the existing category name
                context.startActivity(intent)
            } else {
                // Handle other categories normally
                val context = holder.itemView.context
                val intent = Intent(context, ProdutosActivity::class.java)
                intent.putExtra("categoria", categoria) // Pass the name of the clicked category
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = categorias.size

    fun addCategoria(categoria: String) {
        if (!categorias.contains(categoria)) {
            categorias.add(0, categoria)
            notifyItemInserted(0)
        }
    }
}
