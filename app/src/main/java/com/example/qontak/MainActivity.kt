package com.example.qontak

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qontak.adapter.ListContactRecyclerViewAdapter
import com.example.qontak.commons.RESPONSE_STATUS_EMPTY
import com.example.qontak.commons.RESPONSE_STATUS_OK
import com.example.qontak.commons.SEARCH_CLOSE
import com.example.qontak.commons.SEARCH_OPEN
import com.example.qontak.commons.TAG_ACTION_MAIN_ACTIVITY
import com.example.qontak.commons.TAG_RESPONSE_CONTACT
import com.example.qontak.commons.TOAST_LONG
import com.example.qontak.commons.TOAST_SHORT
import com.example.qontak.data.ApiService
import com.example.qontak.data.HttpClient
import com.example.qontak.model.ContactModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var scaleAnimation: Animation

    private lateinit var rlLoading: RelativeLayout
    private lateinit var txtLoading: TextView
    private lateinit var rvListChat: RecyclerView
    private lateinit var llTitleBar: LinearLayout
    private lateinit var llSearchBox: LinearLayout
    private lateinit var btnFab: FloatingActionButton
    private lateinit var icMore: ImageView
    private lateinit var icCloseSearch: ImageView
    private lateinit var etSearchBox: EditText

    // Global
    private var doubleBackToExitPressedOnce = false

    // Initialize Search Engine
    private val searchDelayMillis = 500L
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private var previousSearchTerm = ""
    private var isSearchActive = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()
        scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_anim)

        initVariable()
        initClickHandler()
        loadingState(true)
        getContacts()

    }

    private fun initVariable() {

        rlLoading = findViewById(R.id.rl_loading)
        txtLoading = findViewById(R.id.txt_loading)
        rvListChat = findViewById(R.id.rv_chat_list)
        llTitleBar = findViewById(R.id.title_bar)
        llSearchBox = findViewById(R.id.search_box)
        btnFab = findViewById(R.id.btn_fab)
        icMore = findViewById(R.id.ic_more)
        icCloseSearch = findViewById(R.id.ic_close_search)
        etSearchBox = findViewById(R.id.et_search_box)

        // Set Title Bar
        icMore.visibility = View.VISIBLE

    }

    private fun initClickHandler() {

        btnFab.setOnClickListener { navigateAddNewRoom() }
        icMore.setOnClickListener { showPopupMenu() }
        icCloseSearch.setOnClickListener { toggleSearchEvent(SEARCH_CLOSE) }

    }

    private fun loadingState(state: Boolean, message: String = getString(R.string.txt_loading)) {

        txtLoading.text = message

        if (state) {

            rlLoading.visibility = View.VISIBLE
            rvListChat.visibility = View.GONE

        } else {

            rlLoading.visibility = View.GONE
            rvListChat.visibility = View.VISIBLE

        }

    }

    private fun handleMessage(tag: String, message: String, duration: Int = TOAST_LONG) {

        Log.d(tag, message)
        Toast.makeText(this@MainActivity, message, duration).show()

    }

    private fun navigateAddNewRoom() {

        toggleSearchEvent(SEARCH_CLOSE)

        val intent = Intent(this, NewRoomChatFormActivity::class.java)
        startActivity(intent)

    }

    private fun showPopupMenu() {
        val popupMenu = PopupMenu(this@MainActivity, icMore)
        popupMenu.inflate(R.menu.option_main_menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.option_sync_now -> {
                    getContacts()
                    true
                }
                R.id.option_search -> {
                    toggleSearchEvent(SEARCH_OPEN)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun toggleSearchEvent(state: String) {

        val animationDuration = 300L
        val slideInFromLeft = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_slide_in_from_left)
        slideInFromLeft.duration = animationDuration
        val slideOutToRight = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_slide_out_to_right)
        slideOutToRight.duration = animationDuration
        val slideInFromRight = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_slide_in_from_right)
        slideInFromRight.duration = animationDuration
        val slideOutToLeft = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_slide_out_to_left)
        slideOutToLeft.duration = animationDuration

        if (state == SEARCH_OPEN && !isSearchActive) {

            isSearchActive = true

            llSearchBox.visibility = View.VISIBLE

            llSearchBox.startAnimation(slideInFromLeft)
            llTitleBar.startAnimation(slideOutToRight)

            Handler().postDelayed({
                llTitleBar.visibility = View.GONE
            }, animationDuration)

            etSearchBox.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    val searchTerm = s.toString()

                    if (searchTerm != previousSearchTerm) {
                        previousSearchTerm = searchTerm

                        if (!TextUtils.isEmpty(searchTerm)) {

                            searchRunnable?.let { searchHandler.removeCallbacks(it) }

                            searchRunnable = Runnable {
                                searchContact(searchTerm)
                            }

                            searchRunnable?.let { searchHandler.postDelayed(it, searchDelayMillis) }

                        } else {

                            getContacts()

                        }

                    }

                }

                override fun afterTextChanged(s: Editable?) {}

            })

        }

        if (state == SEARCH_CLOSE && isSearchActive) {

            isSearchActive = false

            llTitleBar.visibility = View.VISIBLE

            llTitleBar.startAnimation(slideInFromRight)
            llSearchBox.startAnimation(slideOutToLeft)

            Handler().postDelayed({
                llSearchBox.visibility = View.GONE
            }, animationDuration)

            if (etSearchBox.text.toString() != "") etSearchBox.setText("")

        }

    }

    private fun getContacts() {

        loadingState(true)

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.getContacts()

                when (response.status) {
                    RESPONSE_STATUS_OK -> {

                        setRecyclerView(response.results)
                        loadingState(false)

                    }
                    RESPONSE_STATUS_EMPTY -> {

                        loadingState(false, "Contact data is empty!")

                    }
                    else -> {

                        handleMessage(TAG_RESPONSE_CONTACT, "Failed get data")
                        loadingState(false)

                    }
                }


            } catch (e: Exception) {

                handleMessage(TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(false)

            }

        }

    }

    private fun searchContact(key: String) {

        loadingState(true)

        lifecycleScope.launch {
            try {

                val searchKey = createPartFromString(key)

                val apiService: ApiService = HttpClient.create()
                val response = apiService.searchContact(key = searchKey)

                if (response.isSuccessful) {

                    val responseBody = response.body()!!

                    when (responseBody.status) {
                        RESPONSE_STATUS_OK -> {

                            setRecyclerView(responseBody.results)
                            loadingState(false)

                        }
                        RESPONSE_STATUS_EMPTY -> {

                            loadingState(true, "Contact data is empty!")

                        }
                        else -> {

                            handleMessage(TAG_RESPONSE_CONTACT, "Failed get data")
                            loadingState(false)

                        }
                    }

                } else {

                    handleMessage(TAG_RESPONSE_CONTACT, "Failed get data! Message: " + response.message())
                    loadingState(false)

                }


            } catch (e: Exception) {

                handleMessage(TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(false)

            }

        }

    }

    private fun setRecyclerView(listItem: ArrayList<ContactModel>) {

        rvListChat.layoutManager = LinearLayoutManager(this@MainActivity)
        rvListChat.adapter = ListContactRecyclerViewAdapter(this@MainActivity, listItem)

    }

    private fun createPartFromString(string: String): RequestBody {
        return string.toRequestBody("multipart/form-data".toMediaType())
    }

    override fun onBackPressed() {
        if (isSearchActive) toggleSearchEvent(SEARCH_CLOSE)
        else {

            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }

            this@MainActivity.doubleBackToExitPressedOnce = true
            handleMessage(TAG_ACTION_MAIN_ACTIVITY, "Tekan sekali lagi untuk keluar!", TOAST_SHORT)

            Handler(Looper.getMainLooper()).postDelayed({
                doubleBackToExitPressedOnce = false
            }, 2000)

        }
    }

}