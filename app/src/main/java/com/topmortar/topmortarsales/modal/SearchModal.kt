package com.topmortar.topmortarsales.modal

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.SearchModalRecyclerViewAdapter
import com.topmortar.topmortarsales.model.ModalSearchModel

class SearchModal(private val context: Context, private val listItem: ArrayList<ModalSearchModel>) : Dialog(context), SearchModalRecyclerViewAdapter.ItemClickListener {

    interface SearchModalListener {
        fun onDataReceived(data: ModalSearchModel)
    }

    private var listener: SearchModalListener? = null
    private var isLoading = false
    private var loadingListener: ((Boolean) -> Unit)? = null
    private var searchKeyListener: ((String) -> Unit)? = null
    var label = "Pilih Opsi Kota"
    var searchHint = "Masukan judul…"

    private lateinit var txtLoading: TextView
    private lateinit var rvItems: RecyclerView
    private lateinit var tvSearch: TextView
    private lateinit var etSearch: EditText
    private lateinit var icClearSearch: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.modal_search)

        setLayout()

        initVariable()
        initClickHandler()
        setRecyclerView()
    }

    fun setCustomDialogListener(listener: SearchModalListener) {
        this.listener = listener
    }

    fun isLoading(value: Boolean) {
        isLoading = value
        loadingListener?.invoke(value)
        Handler().postDelayed({
            isLoadingHandler()
        }, 100)
    }

    fun setSearchKey(value: String) {
        searchKeyListener?.invoke(value)
        Handler().postDelayed({
            etSearch.setText(value)
            etSearch.setSelection(value.length)
            etSearch.requestFocus()
        }, 100)
    }

    fun setLoadingListener(listener: (Boolean) -> Unit) {
        loadingListener = listener
    }

    private fun isLoadingHandler() {

        if (isLoading) {
            txtLoading.visibility = View.VISIBLE
            rvItems.visibility = View.GONE
        } else {
            txtLoading.visibility = View.GONE
            rvItems.visibility = View.VISIBLE
        }
    }

    private fun setLayout() {
        val displayMetrics = DisplayMetrics()
        window?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val widthPercentage = 0.8f // Set the width percentage (e.g., 80%)
        val heightPercentage = 0.6f // Set the height percentage (e.g., 60%)

        val width = (screenWidth * widthPercentage).toInt()
        val height = (screenHeight * heightPercentage).toInt()

        val layoutParams = window?.attributes
        layoutParams?.width = width
        layoutParams?.height = height
        window?.attributes = layoutParams as WindowManager.LayoutParams
    }

    private fun initVariable() {
        txtLoading = findViewById(R.id.modal_loading)
        rvItems = findViewById(R.id.rv_items)
        tvSearch = findViewById(R.id.tv_search_label)
        etSearch = findViewById(R.id.et_search_box)
        icClearSearch = findViewById(R.id.ic_clear_search)
        tvSearch.setText(label)
        etSearch.hint = searchHint
    }

    private fun initClickHandler() {
        icClearSearch.setOnClickListener { etSearch.setText("") }
    }

    private fun setRecyclerView() {
        val rvAdapter = SearchModalRecyclerViewAdapter(listItem, this)

        rvItems.layoutManager = LinearLayoutManager(context)
        rvItems.adapter = rvAdapter
        rvItems.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private var lastScrollPosition = 0

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy < 0) {
                    // Scrolled up
                    val firstVisibleItemPosition =
                        (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    if (lastScrollPosition != firstVisibleItemPosition) {
                        recyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition)?.itemView?.startAnimation(
                            AnimationUtils.loadAnimation(
                                recyclerView.context,
                                R.anim.rv_item_fade_slide_down
                            )
                        )
                        lastScrollPosition = firstVisibleItemPosition
                    }
                } else lastScrollPosition = -1
            }
        })

        etSearch.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val searchKey = s.toString().trim()

                if (!isLoading) {
                    rvAdapter.filter(searchKey)
                    if (searchKey.isNotEmpty()) icClearSearch.visibility = View.VISIBLE
                    else icClearSearch.visibility = View.GONE
                }
            }

        })
    }

    override fun onItemClick(data: ModalSearchModel?) {
        listener?.onDataReceived(data!!)
        etSearch.setText("")
        this.dismiss()
    }

}