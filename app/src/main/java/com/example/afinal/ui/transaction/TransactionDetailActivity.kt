package com.example.afinal.ui.transaction

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.afinal.R
import com.example.afinal.databinding.ActivityTransactionDetailBinding
import com.example.afinal.data.transaction.Transaction
import com.example.afinal.data.transaction.TransactionType
import com.example.afinal.data.transaction.TransactionStatus
import com.example.afinal.data.transaction.TransactionRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity hiển thị chi tiết giao dịch
 * Member 3: Transaction & Payment - Week 2
 */
class TransactionDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionDetailBinding
    private val repository = TransactionRepository()

    private var transactionId: String = ""
    private var currentTransaction: Transaction? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTransactionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get transaction ID from intent
        transactionId = intent.getStringExtra("TRANSACTION_ID") ?: ""

        setupToolbarWithBack()
        setupUI()
        loadTransactionDetail()
    }

    private fun setupToolbarWithBack() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Chi Tiết Giao Dịch"
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupUI() {

        // Button listeners
        binding.btnCopyId.setOnClickListener {
            copyToClipboard(transactionId)
        }

        binding.btnShare.setOnClickListener {
            shareTransaction()
        }
    }

    private fun loadTransactionDetail() {
        if (transactionId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy giao dịch", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        showLoading()

        lifecycleScope.launch {
            val result = repository.getTransactionById(transactionId)

            result.onSuccess { transaction ->
                currentTransaction = transaction
                hideLoading()
                displayTransactionDetail(transaction)
            }

            result.onFailure { error ->
                hideLoading()
                Toast.makeText(
                    this@TransactionDetailActivity,
                    "Lỗi: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    private fun displayTransactionDetail(transaction: Transaction) {
        binding.apply {
            // Transaction ID
            tvTransactionId.text = transaction.id

            // Status
            tvStatus.text = getStatusText(transaction.status)
            val statusColor = getStatusColor(transaction.status)
            tvStatus.setTextColor(ContextCompat.getColor(this@TransactionDetailActivity, statusColor))
            cardStatus.setCardBackgroundColor(
                ContextCompat.getColor(
                    this@TransactionDetailActivity,
                    getStatusBackgroundColor(transaction.status)
                )
            )

            // Type
            tvType.text = getTypeText(transaction.transactionType)

            // Amount
            val amountPrefix = if (isIncomingTransaction(transaction.transactionType)) "+" else "-"
            tvAmount.text = "$amountPrefix${formatCurrency(transaction.amount)}"
            val amountColor = if (isIncomingTransaction(transaction.transactionType)) {
                R.color.green_500
            } else {
                R.color.red_500
            }
            tvAmount.setTextColor(ContextCompat.getColor(this@TransactionDetailActivity, amountColor))

            // From Account
            tvFromAccount.text = transaction.fromAccountId

            // To Account
            if (transaction.toAccountId != null) {
                tvToAccount.text = transaction.toAccountId
                layoutToAccount.visibility = View.VISIBLE
            } else {
                layoutToAccount.visibility = View.GONE
            }

            // Fee
            if (transaction.fee > 0) {
                tvFee.text = formatCurrency(transaction.fee)
                layoutFee.visibility = View.VISIBLE
            } else {
                layoutFee.visibility = View.GONE
            }

            // Timestamp
            tvTimestamp.text = formatFullTimestamp(transaction.timestamp.toDate())

            // Description
            if (transaction.description.isNullOrEmpty()) {
                tvDescription.text = "Không có mô tả"
            } else {
                tvDescription.text = transaction.description
            }

            // Stripe Payment Intent ID
            if (transaction.stripePaymentIntentId != null) {
                tvPaymentIntentId.text = transaction.stripePaymentIntentId
                layoutPaymentIntent.visibility = View.VISIBLE
            } else {
                layoutPaymentIntent.visibility = View.GONE
            }

            // Balance After
            if (transaction.balanceAfter != null) {
                tvBalanceAfter.text = formatCurrency(transaction.balanceAfter!!)
                layoutBalanceAfter.visibility = View.VISIBLE
            } else {
                layoutBalanceAfter.visibility = View.GONE
            }

            // Error Message (if any)
            if (!transaction.errorMessage.isNullOrEmpty()) {
                tvErrorMessage.text = transaction.errorMessage
                cardError.visibility = View.VISIBLE
            } else {
                cardError.visibility = View.GONE
            }
        }
    }

    private fun getStatusText(status: TransactionStatus): String {
        return when (status) {
            TransactionStatus.PENDING -> "Đang chờ xử lý"
            TransactionStatus.PROCESSING -> "Đang xử lý"
            TransactionStatus.COMPLETED -> "Hoàn thành"
            TransactionStatus.FAILED -> "Thất bại"
            TransactionStatus.CANCELLED -> "Đã hủy"
            TransactionStatus.REFUNDED -> "Đã hoàn tiền"
        }
    }

    private fun getStatusColor(status: TransactionStatus): Int {
        return when (status) {
            TransactionStatus.COMPLETED -> R.color.green_700
            TransactionStatus.PENDING, TransactionStatus.PROCESSING -> R.color.orange_700
            TransactionStatus.FAILED, TransactionStatus.CANCELLED -> R.color.red_700
            TransactionStatus.REFUNDED -> R.color.blue_700
        }
    }

    private fun getStatusBackgroundColor(status: TransactionStatus): Int {
        return when (status) {
            TransactionStatus.COMPLETED -> R.color.green_100
            TransactionStatus.PENDING, TransactionStatus.PROCESSING -> R.color.orange_100
            TransactionStatus.FAILED, TransactionStatus.CANCELLED -> R.color.red_100
            TransactionStatus.REFUNDED -> R.color.blue_100
        }
    }

    private fun getTypeText(type: TransactionType): String {
        return when (type) {
            TransactionType.DEPOSIT -> "Nạp tiền"
            TransactionType.WITHDRAW -> "Rút tiền"
            TransactionType.TRANSFER_INTERNAL -> "Chuyển tiền nội bộ"
            TransactionType.TRANSFER_EXTERNAL -> "Chuyển khoản ngân hàng"
            TransactionType.BILL_PAYMENT -> "Thanh toán hóa đơn"
            TransactionType.REFUND -> "Hoàn tiền"
        }
    }

    private fun isIncomingTransaction(type: TransactionType): Boolean {
        return type == TransactionType.DEPOSIT || type == TransactionType.REFUND
    }

    private fun formatCurrency(amount: Double): String {
        return "%,.0f VND".format(amount)
    }

    private fun formatFullTimestamp(date: Date): String {
        return SimpleDateFormat("dd/MM/yyyy 'lúc' HH:mm:ss", Locale("vi", "VN")).format(date)
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Transaction ID", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Đã sao chép ID", Toast.LENGTH_SHORT).show()
    }

    private fun shareTransaction() {
        currentTransaction?.let { transaction ->
            val shareText = buildString {
                appendLine("📋 Chi tiết giao dịch")
                appendLine()
                appendLine("ID: ${transaction.id}")
                appendLine("Loại: ${getTypeText(transaction.transactionType)}")
                appendLine("Số tiền: ${formatCurrency(transaction.amount)}")
                appendLine("Trạng thái: ${getStatusText(transaction.status)}")
                appendLine("Thời gian: ${formatFullTimestamp(transaction.timestamp.toDate())}")
                if (!transaction.description.isNullOrEmpty()) {
                    appendLine("Mô tả: ${transaction.description}")
                }
            }

            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            }
            startActivity(android.content.Intent.createChooser(intent, "Chia sẻ giao dịch"))
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.scrollView.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.scrollView.visibility = View.VISIBLE
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}