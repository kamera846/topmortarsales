package com.topmortar.topmortarsales.commons

import android.widget.Toast

// Services
//const val BASE_URL = "https://saleswa.topmortarindonesia.com/" // Production
const val BASE_URL = "https://dev-saleswa.topmortarindonesia.com/" // Development

// Ping Host
//const val PING_HOST = "saleswa.topmortarindonesia.com" // Production
const val PING_HOST = "dev-saleswa.topmortarindonesia.com" // Development

const val PING_NORMAL = 0
const val PING_MEDIUM = 2

const val RESPONSE_STATUS_OK = "ok"
const val RESPONSE_STATUS_SUCCESS = "success"
const val RESPONSE_STATUS_EMPTY = "empty"
const val RESPONSE_STATUS_FAIL = "fail"
const val RESPONSE_STATUS_FAILED = "failed"

const val CONTACT = "contacts.php"
const val EDIT_CONTACT = "contacts.php"
const val SEND_MESSAGE = "messages.php"
const val SEARCH_CONTACT = "contactsSearch.php"
const val GET_CITY = "city.php"
const val ADD_CITY = "city.php"
const val AUTH = "auth.php"
const val GET_USERS = "users.php"
const val ADD_USERS = "users.php"
const val REQUEST_OTP = "reqOtp.php"
const val VERIFY_OTP = "verifyOtp.php"
const val UPDATE_PASSWORD = "updatePassword.php"
const val SURAT_JALAN = "suratjalan.php"
const val INVOICE = "invoice.php"
const val PAYMENT = "payment.php"
const val SKILL = "skill.php"
const val TUKANG = "tukang.php"
const val TUKANG_MESSAGE = "tukangMessage.php"
const val PROMO = "promo.php"
const val STORE_STATUS = "storeStatus.php"
const val BID = "bid.php"
const val BID_ON_GOING = "0"
const val BID_VISITED = "1"
const val VISIT = "visit.php"
const val BASECAMP = "gudang.php"
const val DISTRIBUTOR = "distributor.php"
const val VOUCHER = "voucher.php"

// Request Code
const val ACTIVITY_REQUEST_CODE = "activity_request_code"
const val MAIN_ACTIVITY_REQUEST_CODE = 111
const val DETAIL_ACTIVITY_REQUEST_CODE = 222
const val MANAGE_USER_ACTIVITY_REQUEST_CODE = 333
const val REQUEST_ENABLE_BLUETOOTH = 444
const val REQUEST_BLUETOOTH_PERMISSIONS = 555
const val IMG_PREVIEW_STATE = 666
const val LOCATION_PERMISSION_REQUEST_CODE = 777
const val CONNECTION_FAILURE_RESOLUTION_REQUEST = 888
const val REQUEST_EDIT_CONTACT_COORDINATE = 999
const val REQUEST_BASECAMP_FRAGMENT = "request_basecamp_fragment"
const val RESULT_BASECAMP_FRAGMENT = 101010
const val REQUEST_STORAGE_PERMISSION = 111111

// Tag Log
const val TAG_RESPONSE_CONTACT = "TAG RESPONSE CONTACT"
const val TAG_RESPONSE_MESSAGE = "TAG RESPONSE MESSAGE"
const val TAG_ACTION_MAIN_ACTIVITY = "TAG ACTION MAIN ACTIVITY"

// Global
const val TOAST_LONG = Toast.LENGTH_LONG
const val TOAST_SHORT = Toast.LENGTH_SHORT
const val MAX_DISTANCE = 0.4
const val MAX_REPORT_DISTANCE = 0.2
const val EMPTY_FIELD_VALUE = "Not set"
const val PRINT_METHOD_BLUETOOTH = "print_bluetooth"
const val PRINT_METHOD_WIFI = "print_wifi"

// Props
const val CONST_CONTACT_ID = "const_contact_id"
const val CONST_USER_ID = "const_user_id"
const val CONST_INVOICE_ID = "const_invoice_id"
const val CONST_INVOICE_NUMBER = "const_invoice_number"
const val CONST_INVOICE_IS_COD = "const_invoice_is_cod"

const val CONST_OWNER = "const_owner"
const val CONST_LOCATION = "const_location"
const val CONST_SKILL = "const_skill"
const val CONST_PHONE = "const_phone"
const val CONST_NAME = "const_name"
const val CONST_BIRTHDAY = "const_birthday"
const val CONST_KTP = "const_ktp"
const val CONST_MAPS = "const_maps"
const val CONST_MAPS_NAME = "const_maps_name"
const val CONST_MAPS_STATUS = "const_maps_status"
const val CONST_STATUS = "const_status"
const val CONST_TERMIN = "const_termin"
const val CONST_REPUTATION = "const_reputation"
const val CONST_ADDRESS = "const_address"
const val CONST_USER_LEVEL = "const_user_level"
const val CONST_FULL_NAME = "const_full_name"
const val CONST_URI = "const_uri"
const val CONST_DISTANCE = "const_distance"
const val CONST_STATUS_INVOICE = "const_status_invoice"
const val CONST_TOTAL_INVOICE = "const_total_invoice"
const val CONST_DATE_INVOICE = "const_date_invoice"
const val CONST_NO_SURAT_JALAN = "const_no_surat_jalan"
const val CONST_PROMO = "const_promo"
const val CONST_NEAREST_STORE = "const_nearest_store"
const val CONST_LIST_COORDINATE = "const_list_coordinate"
const val CONST_LIST_COORDINATE_NAME = "const_list_coordinate_name"
const val CONST_LIST_COORDINATE_STATUS = "const_list_coordinate_status"
const val CONST_LIST_COORDINATE_CITY_ID = "const_list_coordinate_city_id"
const val CONST_IS_BASE_CAMP = "const_is_base_camp"

// Status
const val LOGGED_IN = true
const val LOGGED_OUT = false
const val SEARCH_OPEN = "search_open"
const val SEARCH_CLOSE = "search_close"
const val SEARCH_CLEAR = "search_clear"
const val SYNC_NOW = "sync_now"
const val STATUS_CONTACT_BID = "bid"
const val STATUS_CONTACT_DATA = "data"
const val STATUS_CONTACT_PASSIVE = "passive"
const val STATUS_CONTACT_ACTIVE = "active"
const val STATUS_CONTACT_BLACKLIST = "blacklist"
const val STATUS_TERMIN_COD = "0"
const val STATUS_TERMIN_COD_TF = "1"
const val STATUS_TERMIN_COD_TUNAI = "2"
const val STATUS_TERMIN_15 = "15"
const val STATUS_TERMIN_30 = "30"
const val STATUS_TERMIN_45 = "45"
const val STATUS_TERMIN_60 = "60"
const val INVOICE_PAID = "paid"
const val GET_COORDINATE = "get_coordinate"

// User Kind
const val USER_KIND_ADMIN = "user_kind_admin"
const val USER_KIND_SALES = "user_kind_sales"
const val USER_KIND_COURIER = "user_kind_courier"
const val USER_KIND_BA = "user_kind_ba"
const val AUTH_LEVEL_ADMIN = "admin"
const val AUTH_LEVEL_SALES = "sales"
const val AUTH_LEVEL_COURIER = "courier"
const val AUTH_LEVEL_BA = "ba"