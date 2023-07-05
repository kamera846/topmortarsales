package com.topmortar.topmortarsales

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.adapter.ListContactRecyclerViewAdapter
import com.topmortar.topmortarsales.adapter.ListContactRecyclerViewAdapter.ItemClickListener
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.CONST_PHONE
import com.topmortar.topmortarsales.commons.MAIN_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_EMPTY
import com.topmortar.topmortarsales.commons.RESPONSE_STATUS_OK
import com.topmortar.topmortarsales.commons.SEARCH_CLEAR
import com.topmortar.topmortarsales.commons.SEARCH_CLOSE
import com.topmortar.topmortarsales.commons.SEARCH_OPEN
import com.topmortar.topmortarsales.commons.SYNC_NOW
import com.topmortar.topmortarsales.commons.TAG_ACTION_MAIN_ACTIVITY
import com.topmortar.topmortarsales.commons.TAG_RESPONSE_CONTACT
import com.topmortar.topmortarsales.commons.TOAST_SHORT
import com.topmortar.topmortarsales.commons.utils.createPartFromString
import com.topmortar.topmortarsales.commons.utils.handleMessage
import com.topmortar.topmortarsales.data.ApiService
import com.topmortar.topmortarsales.data.HttpClient
import com.topmortar.topmortarsales.model.ContactModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.topmortar.topmortarsales.commons.CONST_BIRTHDAY
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.LOGGED_OUT
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.AppUpdateHelper
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), ItemClickListener {

    private lateinit var scaleAnimation: Animation

    private lateinit var rlLoading: RelativeLayout
    private lateinit var rlParent: RelativeLayout
    private lateinit var txtLoading: TextView
    private lateinit var rvListChat: RecyclerView
    private lateinit var llTitleBar: LinearLayout
    private lateinit var llSearchBox: LinearLayout
    private lateinit var btnFab: FloatingActionButton
    private lateinit var icMore: ImageView
    private lateinit var icCloseSearch: ImageView
    private lateinit var icClearSearch: ImageView
    private lateinit var etSearchBox: EditText

    // Global
    private lateinit var sessionManager: SessionManager
    private var doubleBackToExitPressedOnce = false

    // Initialize Search Engine
    private val searchDelayMillis = 500L
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private var previousSearchTerm = ""
    private var isSearchActive = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this@MainActivity)

        setContentView(R.layout.activity_main)

        scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_anim)

        initVariable()
        initClickHandler()
        loadingState(true)
        getContacts()

        handleMessage(this, TAG_ACTION_MAIN_ACTIVITY, "${ sessionManager.userKind() }")

    }

    private fun initVariable() {

        rlLoading = findViewById(R.id.rl_loading)
        rlParent = findViewById(R.id.rl_parent)
        txtLoading = findViewById(R.id.txt_loading)
        rvListChat = findViewById(R.id.rv_chat_list)
        llTitleBar = findViewById(R.id.title_bar)
        llSearchBox = findViewById(R.id.search_box)
        btnFab = findViewById(R.id.btn_fab)
        icMore = findViewById(R.id.ic_more)
        icCloseSearch = findViewById(R.id.ic_close_search)
        icClearSearch = findViewById(R.id.ic_clear_search)
        etSearchBox = findViewById(R.id.et_search_box)

        // Set Title Bar
        icMore.visibility = View.VISIBLE

    }

    private fun initClickHandler() {

        btnFab.setOnClickListener { navigateAddNewRoom() }
        icMore.setOnClickListener { showPopupMenu() }
        icCloseSearch.setOnClickListener { toggleSearchEvent(SEARCH_CLOSE) }
        icClearSearch.setOnClickListener { etSearchBox.setText("") }
        rlLoading.setOnTouchListener { _, event -> blurSearchBox(event) }
//        rlParent.setOnTouchListener { _, event -> blurSearchBox(event) }
        rvListChat.setOnTouchListener { _, event -> blurSearchBox(event) }

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

    private fun navigateAddNewRoom(data: ContactModel? = null) {

        toggleSearchEvent(SEARCH_CLOSE)

        val intent = Intent(this@MainActivity, NewRoomChatFormActivity::class.java)

        if (data != null) {
            intent.putExtra(CONST_NAME, data.nama)
            intent.putExtra(CONST_PHONE, data.nomorhp)
        }

        startActivityForResult(intent, MAIN_ACTIVITY_REQUEST_CODE)

    }

    private fun navigateDetailContact(data: ContactModel? = null) {

        toggleSearchEvent(SEARCH_CLOSE)

        val intent = Intent(this@MainActivity, DetailContactActivity::class.java)

        if (data != null) {
            intent.putExtra(CONST_CONTACT_ID, data.id_contact)
            intent.putExtra(CONST_NAME, data.nama)
            intent.putExtra(CONST_PHONE, data.nomorhp)
            intent.putExtra(CONST_BIRTHDAY, data.tgl_lahir)
        }

        startActivityForResult(intent, MAIN_ACTIVITY_REQUEST_CODE)

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
                R.id.option_logout -> {
                    logoutHandler()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun blurSearchBox(event: MotionEvent): Boolean {

        if (isSearchActive && TextUtils.isEmpty(etSearchBox.text)) {
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_SCROLL || event.action == MotionEvent.ACTION_MOVE) {
                toggleSearchEvent(SEARCH_CLOSE)
                return true
            }
        }
        return false
    }

    private fun toggleSearchEvent(state: String) {

        val animationDuration = 200L

        val fadeIn = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_in)
        fadeIn.duration = animationDuration
        val fadeOut = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_out)
        fadeOut.duration = animationDuration
        val slideInFromLeft = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_slide_in_from_left)
        slideInFromLeft.duration = animationDuration
        val slideOutToRight = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_slide_out_to_right)
        slideOutToRight.duration = animationDuration
        val slideInFromRight = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_slide_in_from_right)
        slideInFromRight.duration = animationDuration
        val slideOutToLeft = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_slide_out_to_left)
        slideOutToLeft.duration = animationDuration

        etSearchBox.setOnFocusChangeListener { _, hasFocus ->
            run {
                if (hasFocus) showKeyboard()
                else hideKeyboard()
            }
        }

        if (state == SEARCH_OPEN && !isSearchActive) {

            llSearchBox.visibility = View.VISIBLE

            llSearchBox.startAnimation(slideInFromLeft)
            llTitleBar.startAnimation(slideOutToRight)

            Handler().postDelayed({
                llTitleBar.visibility = View.GONE
                etSearchBox.requestFocus()
                isSearchActive = true
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

                            toggleSearchEvent(SEARCH_CLEAR)
                            searchContact(searchTerm)
                        }

                        searchRunnable?.let { searchHandler.postDelayed(it, searchDelayMillis) }

                    }

                }

                override fun afterTextChanged(s: Editable?) {}

            })

        }

        if (state == SEARCH_CLOSE && isSearchActive) {

            llTitleBar.visibility = View.VISIBLE

            llTitleBar.startAnimation(slideInFromRight)
            llSearchBox.startAnimation(slideOutToLeft)

            Handler().postDelayed({
                llSearchBox.visibility = View.GONE
                etSearchBox.clearFocus()
                isSearchActive = false
            }, animationDuration)

            if (etSearchBox.text.toString() != "") etSearchBox.setText("")

        }

        if (state == SEARCH_CLEAR) {

            if (TextUtils.isEmpty(etSearchBox.text)) {

                if (icClearSearch.visibility == View.VISIBLE) {

                    icClearSearch.startAnimation(fadeOut)
                    Handler().postDelayed({
                        icClearSearch.visibility = View.GONE
                    }, animationDuration)

                }

            } else {

                if (icClearSearch.visibility == View.GONE) {

                    etSearchBox.clearFocus()

                    icClearSearch.startAnimation(fadeIn)
                    Handler().postDelayed({
                        icClearSearch.visibility = View.VISIBLE
                    }, animationDuration)

                }

            }

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

                        loadingState(true, "Contact data is empty!")

                    }
                    else -> {

                        handleMessage(this@MainActivity, TAG_RESPONSE_CONTACT, "Failed get data")
                        loadingState(true, getString(R.string.failed_request))

                    }
                }


            } catch (e: Exception) {

                handleMessage(this@MainActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true, getString(R.string.failed_request))

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

                            handleMessage(this@MainActivity, TAG_RESPONSE_CONTACT, "Failed get data")
                            loadingState(true, getString(R.string.failed_request))

                        }
                    }

                } else {

                    handleMessage(this@MainActivity, TAG_RESPONSE_CONTACT, "Failed get data! Message: " + response.message())
                    loadingState(true, getString(R.string.failed_request))

                }


            } catch (e: Exception) {

                handleMessage(this@MainActivity, TAG_RESPONSE_CONTACT, "Failed run service. Exception " + e.message)
                loadingState(true, getString(R.string.failed_request))

            }

        }

    }

    private fun setRecyclerView(listItem: ArrayList<ContactModel>) {
        val rvAdapter = ListContactRecyclerViewAdapter(listItem, this@MainActivity)

        rvListChat.layoutManager = LinearLayoutManager(this@MainActivity)
        rvListChat.adapter = rvAdapter

    }

    private fun showKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(etSearchBox, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(etSearchBox.windowToken, 0)
    }

    private fun logoutHandler() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout Confirmation")
            .setMessage("Are you sure you want to log out?")
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                sessionManager.setLoggedIn(LOGGED_OUT)
                sessionManager.setUserKind("")
                val intent = Intent(this@MainActivity, SplashScreenActivity::class.java)
                startActivity(intent)
                finish()
            }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MAIN_ACTIVITY_REQUEST_CODE) {

            val resultData = data?.getStringExtra("$MAIN_ACTIVITY_REQUEST_CODE")

            if (resultData == SYNC_NOW) {

                getContacts()

            }

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
            handleMessage(this@MainActivity, TAG_ACTION_MAIN_ACTIVITY, "Tekan sekali lagi untuk keluar!", TOAST_SHORT)

            Handler(Looper.getMainLooper()).postDelayed({
                doubleBackToExitPressedOnce = false
            }, 2000)

        }
    }

    override fun onItemClick(data: ContactModel?) {

        if (sessionManager.userKind() == USER_KIND_ADMIN) navigateDetailContact(data)
        else navigateAddNewRoom(data)

    }

    override fun onResume() {

        super.onResume()
        // Check apps for update
        AppUpdateHelper.checkForUpdates(this)

    }

}