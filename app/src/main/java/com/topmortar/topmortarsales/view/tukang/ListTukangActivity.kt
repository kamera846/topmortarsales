package com.topmortar.topmortarsales.view.tukang

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.ELLIPSIS_TEXT
import com.topmortar.topmortarsales.commons.SEARCH_CLEAR
import com.topmortar.topmortarsales.commons.SEARCH_CLOSE
import com.topmortar.topmortarsales.commons.SEARCH_OPEN
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.utils.EventBusUtils
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.applyMyEdgeToEdge
import com.topmortar.topmortarsales.databinding.ActivityListTukangBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class ListTukangActivity : AppCompatActivity() {

    private var _binding: ActivityListTukangBinding? = null
    private lateinit var sessionManager: SessionManager
    private val userKind get() = sessionManager.userKind()
    private val userID get() = sessionManager.userID()
    private val binding get() = _binding!!

    // Title & Search Bar
    private lateinit var titleBarWrapper: LinearLayout
    private lateinit var searchBoxWrapper: LinearLayout
    private lateinit var icClearSearch: ImageView
    private lateinit var icCloseSearch: ImageView
    private lateinit var etSearchBox: EditText

    // Initialize Search Engine
    private val searchDelayMillis = 500L
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private var previousSearchTerm = ""
    private var isSearchActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        applyMyEdgeToEdge()

        sessionManager = SessionManager(this)
        _binding = ActivityListTukangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleBarDark.tvTitleBar.text = "Daftar Tukang"
        binding.titleBarDark.icBack.setOnClickListener { finish() }

        titleBarWrapper = findViewById(R.id.titleBarDark)
        searchBoxWrapper = findViewById(R.id.search_box)
        icClearSearch = binding.searchBox.icClearSearch
        icCloseSearch = binding.searchBox.icCloseSearch
        etSearchBox = binding.searchBox.etSearchBox

        binding.titleBarDark.icSearch.visibility = View.VISIBLE
        binding.titleBarDark.icSearch.setOnClickListener { toggleSearchEvent(SEARCH_OPEN) }
        etSearchBox.hint = "Ketik nama atau nomor tukang$ELLIPSIS_TEXT"
        icCloseSearch.setOnClickListener { toggleSearchEvent(SEARCH_CLOSE) }
        icClearSearch.setOnClickListener { etSearchBox.setText("") }

        /*
        Call Fragment
         */
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val myFragment = TukangFragment()
//        if (userKind == USER_KIND_ADMIN || userKind == USER_KIND_ADMIN_CITY) myFragment.setUserID(intent.getStringExtra(CONST_USER_ID)) else myFragment.setUserID(userID)
        if (userKind != USER_KIND_ADMIN) {
            myFragment.setCounterItem(object : TukangFragment.CounterItem {
                override fun counterItem(count: Int) {
                    binding.titleBarDark.tvTitleBarDescription.visibility = View.VISIBLE
                    binding.titleBarDark.tvTitleBarDescription.text = "Total $count data"
                }

            })
        }
        fragmentTransaction.replace(R.id.listTukangFragmentContainer, myFragment)
        fragmentTransaction.addToBackStack(null)

        binding.titleBarDark.icSyncNow.setOnClickListener { myFragment.syncNow() }

        fragmentTransaction.commit()
        /*
        End Call Fragment
         */
    }

    private fun toggleSearchEvent(state: String) {

        val animationDuration = 200L

        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        fadeIn.duration = animationDuration
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        fadeOut.duration = animationDuration
        val slideInFromLeft = AnimationUtils.loadAnimation(this,
            R.anim.fade_slide_in_from_left
        )
        slideInFromLeft.duration = animationDuration
        val slideOutToRight = AnimationUtils.loadAnimation(this,
            R.anim.fade_slide_out_to_right
        )
        slideOutToRight.duration = animationDuration
        val slideInFromRight = AnimationUtils.loadAnimation(this,
            R.anim.fade_slide_in_from_right
        )
        slideInFromRight.duration = animationDuration
        val slideOutToLeft = AnimationUtils.loadAnimation(this,
            R.anim.fade_slide_out_to_left
        )
        slideOutToLeft.duration = animationDuration

        if (state == SEARCH_OPEN && !isSearchActive) {

            searchBoxWrapper.visibility = View.VISIBLE

            searchBoxWrapper.startAnimation(slideInFromLeft)
            titleBarWrapper.startAnimation(slideOutToRight)

            Handler(Looper.getMainLooper()).postDelayed({
                titleBarWrapper.visibility = View.GONE
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

//                            toggleSearchEvent(SEARCH_CLEAR)
                            val event = EventBusUtils.MessageEvent(searchTerm)
                            EventBus.getDefault().post(event)
//                            handleMessage(this@ListTukangActivity, "TAG", searchTerm)
                        }

                        searchRunnable?.let { searchHandler.postDelayed(it, searchDelayMillis) }

                    }

                }

                override fun afterTextChanged(s: Editable?) {}

            })

        }

        if (state == SEARCH_CLOSE && isSearchActive) {

            titleBarWrapper.visibility = View.VISIBLE

            titleBarWrapper.startAnimation(slideInFromRight)
            searchBoxWrapper.startAnimation(slideOutToLeft)

            Handler(Looper.getMainLooper()).postDelayed({
                searchBoxWrapper.visibility = View.GONE
                etSearchBox.clearFocus()
                isSearchActive = false
            }, animationDuration)

            if (etSearchBox.text.toString() != "") etSearchBox.setText("")

        }

        if (state == SEARCH_CLEAR) {

            if (TextUtils.isEmpty(etSearchBox.text)) {

                if (icClearSearch.visibility == View.VISIBLE) {

                    icClearSearch.startAnimation(fadeOut)
                    Handler(Looper.getMainLooper()).postDelayed({
                        icClearSearch.visibility = View.GONE
                    }, animationDuration)

                }

            } else {

                if (icClearSearch.visibility == View.GONE) {

                    etSearchBox.clearFocus()

                    icClearSearch.startAnimation(fadeIn)
                    Handler(Looper.getMainLooper()).postDelayed({
                        icClearSearch.visibility = View.VISIBLE
                    }, animationDuration)

                }

            }

        }

    }

    @Subscribe
    fun onEventBus(event: EventBusUtils.MessageEvent) {
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (isSearchActive) toggleSearchEvent(SEARCH_CLOSE)
        else finish()
    }

}