package com.topmortar.topmortarsales.commons

import android.widget.Toast

// Services
//const val BASE_URL = "https://saleswa.topmortarindonesia.com/" // Production
const val BASE_URL = "https://dev-saleswa.topmortarindonesia.com/" // Development

const val RESPONSE_STATUS_OK = "ok"
const val RESPONSE_STATUS_EMPTY = "empty"

const val GET_CONTACT = "contacts.php"
const val EDIT_CONTACT = "contacts.php"
const val SEND_MESSAGE = "messages.php"
const val SEARCH_CONTACT = "contactsSearch.php"

// Request Code
const val ACTIVITY_REQUEST_CODE = "activity_request_code"
const val MAIN_ACTIVITY_REQUEST_CODE = 111
const val DETAIL_ACTIVITY_REQUEST_CODE = 222

// Tag Log
const val TAG_RESPONSE_CONTACT = "TAG RESPONSE CONTACT"
const val TAG_RESPONSE_MESSAGE = "TAG RESPONSE MESSAGE"
const val TAG_ACTION_MAIN_ACTIVITY = "TAG ACTION MAIN ACTIVITY"

// Global
const val TOAST_LONG = Toast.LENGTH_LONG
const val TOAST_SHORT = Toast.LENGTH_SHORT

// Props
const val CONST_CONTACT_ID = "const_contact_id"
const val CONST_OWNER = "const_owner"
const val CONST_PHONE = "const_phone"
const val CONST_NAME = "const_name"
const val CONST_MESSAGE = "const_message"
const val CONST_BIRTHDAY = "const_birthday"

// Status
const val LOGGED_IN = true
const val LOGGED_OUT = false
const val SEARCH_OPEN = "search_open"
const val SEARCH_CLOSE = "search_close"
const val SEARCH_CLEAR = "search_clear"
const val SYNC_NOW = "sync_now"

// User Kind
const val USER_KIND_ADMIN = "user_kind_admin"
const val USER_KIND_SALES = "user_kind_sales"

// DUMMY
const val DUMMY_ADMIN_USERNAME = "topmortar"
const val DUMMY_ADMIN_PASSWORD = "admintopmortar123"
const val DUMMY_SALES_USERNAME = "topmortarsales"
const val DUMMY_SALES_PASSWORD = "topmortar123"