package com.pab.deucepadelapp.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.pab.deucepadelapp.R

class BookingActivity : AppCompatActivity() {

    private lateinit var tvBookCourtName: TextView
    private lateinit var tvBookDate: TextView
    private lateinit var tvBookSlot: TextView
    private lateinit var tvBookDuration: TextView
    private lateinit var tvBookCourtPrice: TextView

    private lateinit var btnHeaderEWallet: RelativeLayout
    private lateinit var layoutChildEWallet: LinearLayout

    private lateinit var btnHeaderVA: RelativeLayout
    private lateinit var layoutChildVA: LinearLayout

    private lateinit var rbDana: RadioButton
    private lateinit var rbOvo: RadioButton
    private lateinit var rbSpay: RadioButton
    private lateinit var rbBri: RadioButton
    private lateinit var rbBca: RadioButton
    private lateinit var rbMandiri: RadioButton
    private lateinit var rbBsi: RadioButton
    private lateinit var rbBni: RadioButton

    private lateinit var tvBillPriceDetail: TextView
    private lateinit var layoutMethodFeeDetail: LinearLayout
    private lateinit var tvMethodFeeLabel: TextView
    private lateinit var tvMethodFeePrice: TextView
    private lateinit var tvBookGrandTotal: TextView

    private lateinit var btnBackBooking: ImageView
    private lateinit var btnProceedPayment: Button

    private var courtPrice: Double = 150000.0
    private var adminMethodFee: Double = 0.0
    private var finalGrandTotal: Double = 0.0
    private var courtName: String = ""
    private var bookingId: Long = 10L

    private var selectedPaymentCategory: String = ""
    private var selectedPaymentName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        initViews()
        parseIntentData()
        setupDropdownLogic()
        setupRowClickLogic()

        btnBackBooking.setOnClickListener { finish() }

        btnProceedPayment.setOnClickListener {
            if (selectedPaymentCategory.isEmpty() || selectedPaymentName.isEmpty()) {
                Toast.makeText(this, "Harap pilih salah satu metode pembayaran terlebih dahulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intentKeStruk = Intent(this, ReceiptSimulationActivity::class.java).apply {
                putExtra("BOOKING_ID", bookingId)
                putExtra("COURT_NAME", courtName)
                putExtra("PAYMENT_NAME", selectedPaymentName)
                putExtra("GRAND_TOTAL", finalGrandTotal)
            }
            startActivity(intentKeStruk)
            finish()
        }
    }

    private fun initViews() {
        tvBookCourtName = findViewById(R.id.tvBookCourtName)
        tvBookDate = findViewById(R.id.tvBookDate)
        tvBookSlot = findViewById(R.id.tvBookSlot)
        tvBookDuration = findViewById(R.id.tvBookDuration)
        tvBookCourtPrice = findViewById(R.id.tvBookCourtPrice)

        btnHeaderEWallet = findViewById(R.id.btnHeaderEWallet)
        layoutChildEWallet = findViewById(R.id.layoutChildEWallet)

        btnHeaderVA = findViewById(R.id.btnHeaderVA)
        layoutChildVA = findViewById(R.id.layoutChildVA)

        rbDana = findViewById(R.id.rbDana)
        rbOvo = findViewById(R.id.rbOvo)
        rbSpay = findViewById(R.id.rbSpay)
        rbBri = findViewById(R.id.rbBri)
        rbBca = findViewById(R.id.rbBca)
        rbMandiri = findViewById(R.id.rbMandiri)
        rbBsi = findViewById(R.id.rbBsi)
        rbBni = findViewById(R.id.rbBni)

        tvBillPriceDetail = findViewById(R.id.tvBillPriceDetail)
        layoutMethodFeeDetail = findViewById(R.id.layoutMethodFeeDetail)
        tvMethodFeeLabel = findViewById(R.id.tvMethodFeeLabel)
        tvMethodFeePrice = findViewById(R.id.tvMethodFeePrice)
        tvBookGrandTotal = findViewById(R.id.tvBookGrandTotal)

        btnBackBooking = findViewById(R.id.btnBackBooking)
        btnProceedPayment = findViewById(R.id.btnProceedPayment)
    }

    private fun parseIntentData() {
        courtName = intent.getStringExtra("COURT_NAME") ?: "Padel Court Arena"
        val bookingDate = intent.getStringExtra("BOOKING_DATE") ?: "25 Juni 2025"
        val bookingSlot = intent.getStringExtra("BOOKING_SLOT") ?: "08:00 - 09:30"
        val bookingDuration = intent.getStringExtra("BOOKING_DURATION") ?: "1.5 Hours"
        courtPrice = intent.getDoubleExtra("TOTAL_PRICE", 150000.0)
        bookingId = intent.getLongExtra("BOOKING_ID", 10L)

        tvBookCourtName.text = courtName
        tvBookDate.text = bookingDate
        tvBookSlot.text = bookingSlot
        tvBookDuration.text = bookingDuration
        tvBookCourtPrice.text = "Rp ${String.format("%,.0f", courtPrice)}"
        tvBillPriceDetail.text = "Rp ${String.format("%,.0f", courtPrice)}"

        calculateAndDisplayGrandTotal()
    }

    private fun setupDropdownLogic() {
        btnHeaderEWallet.setOnClickListener {
            if (layoutChildEWallet.visibility == View.VISIBLE) {
                layoutChildEWallet.visibility = View.GONE
            } else {
                layoutChildEWallet.visibility = View.VISIBLE
                layoutChildVA.visibility = View.GONE
            }
        }

        btnHeaderVA.setOnClickListener {
            if (layoutChildVA.visibility == View.VISIBLE) {
                layoutChildVA.visibility = View.GONE
            } else {
                layoutChildVA.visibility = View.VISIBLE
                layoutChildEWallet.visibility = View.GONE
            }
        }
    }

    private fun setupRowClickLogic() {
        val allRadioButtons = listOf(rbDana, rbOvo, rbSpay, rbBri, rbBca, rbMandiri, rbBsi, rbBni)

        fun clearAllExcept(target: RadioButton) {
            allRadioButtons.forEach { it.isChecked = (it == target) }
        }

        listOf(rbDana, rbOvo, rbSpay).forEach { rb ->
            val rowLayout = rb.parent as? View
            rowLayout?.setOnClickListener {
                if (!rb.isChecked) { clearAllExcept(rb); rb.isChecked = true }
            }
            rb.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    clearAllExcept(rb)
                    selectedPaymentCategory = "EWALLET"
                    selectedPaymentName = when (rb.id) {
                        R.id.rbDana -> "Dana"
                        R.id.rbOvo -> "OVO"
                        R.id.rbSpay -> "ShopeePay"
                        else -> "E-Wallet"
                    }
                    adminMethodFee = 1000.0
                    tvMethodFeeLabel.text = "Admin Fee (E-Wallet)"
                    layoutMethodFeeDetail.visibility = View.VISIBLE
                    tvMethodFeePrice.text = "Rp ${String.format("%,.0f", adminMethodFee)}"
                    calculateAndDisplayGrandTotal()
                }
            }
        }

        listOf(rbBri, rbBca, rbMandiri, rbBsi, rbBni).forEach { rb ->
            val rowLayout = rb.parent as? View
            rowLayout?.setOnClickListener {
                if (!rb.isChecked) { clearAllExcept(rb); rb.isChecked = true }
            }
            rb.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    clearAllExcept(rb)
                    selectedPaymentCategory = "TRANSFER"
                    selectedPaymentName = when (rb.id) {
                        R.id.rbBri -> "BRI Virtual Account"
                        R.id.rbBca -> "BCA Virtual Account"
                        R.id.rbMandiri -> "Mandiri Virtual Account"
                        R.id.rbBsi -> "BSI Virtual Account"
                        R.id.rbBni -> "BNI Virtual Account"
                        else -> "Virtual Account"
                    }
                    adminMethodFee = 2500.0
                    tvMethodFeeLabel.text = "Admin Fee (VA)"
                    layoutMethodFeeDetail.visibility = View.VISIBLE
                    tvMethodFeePrice.text = "Rp ${String.format("%,.0f", adminMethodFee)}"
                    calculateAndDisplayGrandTotal()
                }
            }
        }
    }

    private fun calculateAndDisplayGrandTotal() {
        finalGrandTotal = courtPrice + adminMethodFee
        tvBookGrandTotal.text = "Rp ${String.format("%,.0f", finalGrandTotal)}"
    }
}