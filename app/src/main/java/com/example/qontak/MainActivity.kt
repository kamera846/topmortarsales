package com.example.qontak

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qontak.adapter.ListContactRecyclerViewAdapter
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

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var scaleAnimation: Animation

    private lateinit var rlLoading: RelativeLayout
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
        setRecyclerView()

    }

    private fun initVariable() {

        rlLoading = findViewById(R.id.rl_loading)
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

    private fun loadingState(state: Boolean) {

        if (state) {

            rlLoading.visibility = View.VISIBLE
            rvListChat.visibility = View.GONE

        } else {

            rlLoading.visibility = View.GONE
            rvListChat.visibility = View.VISIBLE

        }

    }

    private fun setRecyclerView() {

        loadingState(true)

        lifecycleScope.launch {
            try {

                val apiService: ApiService = HttpClient.create()
                val response = apiService.getContacts()

                if (response.status == RESPONSE_STATUS_OK) {

                    val listItem: ArrayList<ContactModel> = response.results

                    rvListChat.layoutManager = LinearLayoutManager(this@MainActivity)
                    rvListChat.adapter = ListContactRecyclerViewAdapter(this@MainActivity, listItem)

                    loadingState(false)

                } else {

                    handleMessage(TAG_RESPONSE_CONTACT, "Failed get data")
                    loadingState(false)

                }


            } catch (e: Exception) {

                handleMessage(TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(false)

            }

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
                    setRecyclerView()
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

        val animationDuration = 500L
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

                        searchRunnable?.let { searchHandler.removeCallbacks(it) }

                        searchRunnable = Runnable {
                            handleMessage(TAG_RESPONSE_CONTACT, searchTerm)
                        }

                        searchRunnable?.let { searchHandler.postDelayed(it, searchDelayMillis) }

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