package cn.sa.im.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.LayoutInflater.from
import android.view.View
import android.view.ViewGroup
import cn.sa.im.R
import kotlinx.android.synthetic.main.fragment_study.*
import kotlinx.android.synthetic.main.item_studylayout.view.*
import kotlinx.android.synthetic.main.layout_base.view.*

class StudyFragment : Fragment(){

    //初始化，有add，remove方法的集合
    val studylist = SparseArray<String>()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_study, container, false)
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val layoutManager=LinearLayoutManager(activity)
        recycleView.layoutManager=layoutManager
        studylist.append(0,"Handler")
        studylist.append(1,"aidl")
        recycleView.adapter=StudyAdapter(studylist)
    }
    class StudyAdapter(var data :SparseArray<String>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            val  inflate = from(parent?.context).inflate(R.layout.item_studylayout, parent,false)
            //这样写不会居中
            //val  inflate = from(parent?.context).inflate(R.layout.item_studylayout, null)
            return MyHolder(inflate)
        }

        override fun getItemCount(): Int {
            return data.size()
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if(holder is MyHolder)
                holder.bind(data[position])
        }
    }
    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(date: String){
            itemView.study_btn.text=date
        }
    }

}