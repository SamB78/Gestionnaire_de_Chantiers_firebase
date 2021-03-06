package com.techphone78.gestionnairedechantiers.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.techphone78.gestionnairedechantiers.BR
import timber.log.Timber

class BaseAdapter(private val lid: Int, val vm: ViewModel) :
    RecyclerView.Adapter<BaseAdapter.ViewHolder>() {

    var data: List<Any> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        //val binding: V = DataBindingUtil.inflate(layoutInflater, lid, parent, false)
        val binding: ViewDataBinding = DataBindingUtil.inflate(layoutInflater, lid, parent, false)
        Timber.d("view holder $parent $viewType")
        return ViewHolder(binding)
    }

    //override fun getItemCount(): Int = data.get()!!.size
    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //holder.bind(data.get()!![position],vm)
        //holder.bind(data.get()!![position])
        holder.bind(data[position], vm)

        //holder.binding.name.setOnClickListener{ vm?.onChoiceSelected(data[position]) }
        //holder.binding.name.setOnClickListener{ vm?.onChoiceSelected(data[position]) }
        //holder.binding.name.setOnClickListener{ action?.onItemClicked(holder.itemView) }
        //holder.binding.name.setOnClickListener{ action?.onItemClicked() }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun updateItems( items: List<Any>) {
        data = items
        notifyDataSetChanged()
    }


    inner class ViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
        //inner class ViewHolder(val binding: V) : RecyclerView.ViewHolder(binding.root){

        //fun bind(data: RecoResult?,vm: MainActivityVM?) {
        fun bind(data: Any, vm: ViewModel) {
            binding.setVariable(BR.item, data)
            binding.setVariable(BR.viewModel, vm)
            //binding.root.setOnClickListener { onClick(data) }
            binding.executePendingBindings()

        }

    }

    companion object {
        @BindingAdapter(value = ["items", "vm", "layoutId"], requireAll = true)
        @JvmStatic
        fun bindAdapter(view: RecyclerView, list: List<Any>, vm: ViewModel, layoutId: Int) {
//                val layoutManager = LinearLayoutManager(view.context)
//                view.layoutManager = layoutManager
            if (view.layoutManager == null) view.layoutManager = LinearLayoutManager(view.context)


            //view.adapter = ChoiceAdapter(vm!!.choices,vm,{x:RecoResult -> vm!!.selectItem(x)})
            //view.adapter = ChoiceAdapter(vm!!.choices,vm,{x:RecoResult -> vm!!.selectItem(x)})
            //view.adapter = ChoiceAdapter(list,vm,{x -> vm!!.selectItem(x)})
//                view.adapter = when (layoutId){
//                    R.layout.choice_item -> BaseAdapter<RecoResult,ChoiceItemBinding>(list as List<RecoResult>,layoutId)
//                    else -> null
//                }


//            if(view.adapter != null && view.adapter is BaseAdapter){
//                Timber.i("notifiy")
//                view.adapter?.notifyDataSetChanged()
//            }else {
//                Timber.i("generate BaseAdapter")
//                view.adapter = BaseAdapter(list, layoutId, vm)
//            }


            val adapter = getOrCreateAdapter(view, layoutId, vm)
            Timber.i("list: $list")
            adapter.updateItems(list)

        }

        private fun getOrCreateAdapter(
            recyclerView: RecyclerView,
            layoutId: Int,
            vm: ViewModel
        ): BaseAdapter {
            return if (recyclerView.adapter != null && recyclerView.adapter is BaseAdapter) {
                recyclerView.adapter as BaseAdapter
            } else {
                val bindableRecyclerAdapter = BaseAdapter(layoutId, vm)
                recyclerView.adapter = bindableRecyclerAdapter
                bindableRecyclerAdapter
            }
        }


        //view.adapter = BaseAdapter(list as List<RecoResult>,layoutId,vm)


//        @BindingAdapter("toggleVisibilityOf")
//        @JvmStatic
//        fun toggleVisibilityOfView(view: View, target : View) {
//            view.setOnClickListener {
//                target.visibility = if (target.visibility == View.VISIBLE) View.GONE else View.VISIBLE
//            }
//        }
    }

//    override fun getFilter(): Filter {
//        return object : Filter() {
//            override fun performFiltering(constraint: CharSequence?): FilterResults {
//
//            }
//
//            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
//            }
//        }
//    }
}