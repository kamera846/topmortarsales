package com.topmortar.topmortarsales.view.product

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.adapter.recyclerview.ProductsRVA
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.USER_KIND_PENAGIHAN
import com.topmortar.topmortarsales.commons.utils.FirebaseUtils
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.applyMyEdgeToEdge
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.databinding.ActivityProductsBinding
import com.topmortar.topmortarsales.modal.SearchModal
import com.topmortar.topmortarsales.model.CityModel
import com.topmortar.topmortarsales.model.ModalSearchModel
import com.topmortar.topmortarsales.model.ProductModel
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n")
@Suppress("DEPRECATION")
class ProductsActivity : AppCompatActivity() {

    private var _binding: ActivityProductsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private val userDistributorId get() = sessionManager.userDistributor()
    private val userKind get() = sessionManager.userKind()
    private val userCity get() = sessionManager.userCityID()
    private val userCityName get() = sessionManager.userCityName()

    private lateinit var searchModal: SearchModal
    private var citiesResults: ArrayList<CityModel>? = null
    private var selectedCity: ModalSearchModel? = null

    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        applyMyEdgeToEdge()

        sessionManager = SessionManager(this)
        _binding = ActivityProductsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        apiService = HttpClient.create()
        binding.titleBar.tvTitleBar.text = "Daftar Produk"
        binding.titleBar.icBack.setOnClickListener { finish() }

        // Get the current theme mode (light or dark)
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) binding.llFilter.componentFilter.background = AppCompatResources.getDrawable(this, R.color.black_400)
        else binding.llFilter.componentFilter.background = AppCompatResources.getDrawable(this, R.color.light)

        if (userKind == USER_KIND_ADMIN || userKind == USER_KIND_PENAGIHAN) getCities()
        else {
            binding.titleBar.tvTitleBarDescription.visibility = View.VISIBLE
            binding.titleBar.tvTitleBarDescription.text = "Di kota $userCityName"
            getList()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            if (userKind == USER_KIND_ADMIN || userKind == USER_KIND_PENAGIHAN) getCities()
            else getList()
        }

    }

    private fun getCities() {

        loadingState(true)

        Handler(Looper.getMainLooper()).postDelayed({
            lifecycleScope.launch {
                try {

                    val apiService: ApiService = HttpClient.create()
                    val response = apiService.getCities(distributorID = "$userDistributorId")

                    when (response.status) {
                        RESPONSE_STATUS_OK -> {

                            citiesResults = response.results
                            val items: ArrayList<ModalSearchModel> = ArrayList()

                            for (i in 0 until citiesResults!!.size) {
                                val data = citiesResults!![i]
                                items.add(ModalSearchModel(data.id_city, "${data.nama_city} - ${data.kode_city}"))
                            }

                            setupDialogSearch(items)
                            binding.llFilter.componentFilter.visibility = View.VISIBLE
//                        binding.llFilter.componentFilter.visibility = View.GONE

                            selectedCity = items[0]
                            binding.titleBar.tvTitleBarDescription.visibility = View.VISIBLE
                            binding.titleBar.tvTitleBarDescription.text = "Di kota ${ items[0].title}"
                            binding.llFilter.tvFilter.text = items[0].title
                            getList()

                        }
                        RESPONSE_STATUS_EMPTY -> {

                            handleMessage(this@ProductsActivity, "LIST CITY", "Daftar kota kosong!")
                            getList()

                        }
                        else -> {

                            handleMessage(this@ProductsActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                            getList()

                        }
                    }


                } catch (e: Exception) {

                    FirebaseUtils.logErr(this@ProductsActivity, "Failed ProductsActivity on getCities(). Catch: ${e.message}")
                    handleMessage(this@ProductsActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                    getList()

                }

            }
        }, 3000)

    }

    private fun setupDialogSearch(items: ArrayList<ModalSearchModel> = ArrayList()) {

        searchModal = SearchModal(this, items)
        searchModal.setCustomDialogListener(object: SearchModal.SearchModalListener{
            override fun onDataReceived(data: ModalSearchModel) {
                if (data.id == "-1") {
                    selectedCity = null
                    binding.titleBar.tvTitleBarDescription.visibility = View.GONE
                    binding.llFilter.tvFilter.text = getString(R.string.tidak_ada_filter)
                } else {
                    selectedCity = data
                    binding.titleBar.tvTitleBarDescription.visibility = View.VISIBLE
                    binding.titleBar.tvTitleBarDescription.text = "Di kota ${ data.title}"
                    binding.llFilter.tvFilter.text = data.title
                }
                getList()
            }

        })
        searchModal.searchHint = "Ketik untuk mencari…"

        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) binding.llFilter.componentFilter.background = AppCompatResources.getDrawable(this, R.color.black_400)
        else binding.llFilter.componentFilter.background = AppCompatResources.getDrawable(this, R.color.light)
        binding.llFilter.componentFilter.visibility = View.GONE
        binding.llFilter.componentFilter.setOnClickListener {
            searchModal.show()
        }
        binding.llFilter.tvFilter.text = selectedCity?.title ?: getString(R.string.tidak_ada_filter)

    }


    private fun getList() {

        loadingState(true)

        lifecycleScope.launch {
            try {

                val response = when (userKind) {
                    USER_KIND_ADMIN, USER_KIND_PENAGIHAN -> {
                        if (selectedCity != null) apiService.getProducts(idCity = selectedCity?.id!!)
                        else apiService.getProducts(idCity = "")
                    } else -> apiService.getProducts(idCity = "$userCity")
                }

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        setRecyclerView(response.results)
                        loadingState(false)

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        loadingState(true, "Belum ada produk di kota ini")

                    }
                    else -> {

                        handleMessage(this@ProductsActivity, TAG_RESPONSE_CONTACT, getString(R.string.failed_get_data))
                        loadingState(true, getString(R.string.failed_request))

                    }
                }

            } catch (e: Exception) {

                FirebaseUtils.logErr(this@ProductsActivity, "Failed ProductsActivity on getList(). Catch: ${e.message}")
                handleMessage(this@ProductsActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true, getString(R.string.failed_request))

            }

        }

    }

    private fun setRecyclerView(listItem: ArrayList<ProductModel>) {

        val rvAdapter = ProductsRVA(listItem, object: ProductsRVA.ItemClickListener {
            override fun onItemClick(data: ProductModel?) {
                // Do Something
            }

        })

        binding.rvChatList.layoutManager = LinearLayoutManager(this)
        binding.rvChatList.adapter = rvAdapter
        binding.rvChatList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

    }

    private fun loadingState(state: Boolean, message: String = getString(R.string.txt_loading)) {

        binding.txtLoading.text = message

        if (state) {

            binding.rlLoading.visibility = View.VISIBLE
            binding.rvChatList.visibility = View.GONE

            binding.swipeRefreshLayout.isRefreshing = message === getString(R.string.txt_loading)

        } else {

            binding.rlLoading.visibility = View.GONE
            binding.rvChatList.visibility = View.VISIBLE

            binding.swipeRefreshLayout.isRefreshing = false

        }

    }

}