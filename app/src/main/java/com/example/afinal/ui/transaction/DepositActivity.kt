package com.example.afinal.ui.transaction

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.afinal.databinding.ActivityDepositBinding
import com.example.afinal.viewmodel.transaction.TransactionResult
import com.example.afinal.viewmodel.transaction.TransactionViewModel

/**
 * Activity xử lý Deposit (Nạp tiền)
 * Member 3: Transaction & Payment - Week 2
 *
 * FIXED VERSION:
 * - Added TAG constant
 * - Added null safety checks
 * - Added error handling for ViewBinding
 * - Added lifecycle awareness
 */
class DepositActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDepositBinding
    private val viewModel: TransactionViewModel by viewModels()

    private var currentAccountId: String = ""

    companion object {
        private const val TAG = "DepositActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // Setup ViewBinding with error handling
            binding = ActivityDepositBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Get account ID from intent or session
            currentAccountId = intent.getStringExtra("ACCOUNT_ID") ?: ""

            if (currentAccountId.isEmpty()) {
                Toast.makeText(
                    this,
                    "Không tìm thấy tài khoản. Vui lòng đăng nhập lại.",
                    Toast.LENGTH_LONG
                ).show()
                finish()
                return
            }

            Log.d(TAG, "onCreate: Account ID = $currentAccountId")

            setupToolbarWithBack()
            setupUI()
            observeViewModel()

        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(
                this,
                "Lỗi khởi tạo: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }

    private fun setupToolbarWithBack() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Nạp Tiền"
        }

        // Xử lý click nút back
        binding.toolbar.setNavigationOnClickListener {
            finish()  // Đóng activity và quay lại
        }
    }

    private fun setupUI() {

        // Preset amount buttons
        binding.btnAmount50k.setOnClickListener {
            binding.etAmount.setText("50000")
        }
        binding.btnAmount100k.setOnClickListener {
            binding.etAmount.setText("100000")
        }
        binding.btnAmount500k.setOnClickListener {
            binding.etAmount.setText("500000")
        }
        binding.btnAmount1m.setOnClickListener {
            binding.etAmount.setText("1000000")
        }

        // Deposit button
        binding.btnDeposit.setOnClickListener {
            try {
                processDeposit()
            } catch (e: Exception) {
                Log.e(TAG, "Error processing deposit", e)
                Toast.makeText(
                    this,
                    "Lỗi xử lý: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Test cards info button
        binding.btnShowTestCards.setOnClickListener {
            showTestCardsDialog()
        }
    }

    private fun observeViewModel() {
        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            try {
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.btnDeposit.isEnabled = !isLoading
            } catch (e: Exception) {
                Log.e(TAG, "Error updating loading state", e)
            }
        }

        // Observe transaction result
        viewModel.transactionStatus.observe(this) { result ->
            try {
                when (result) {
                    is TransactionResult.Success -> {
                        Log.d(TAG, "Transaction success: ${result.transaction.id}")
                        Toast.makeText(
                            this,
                            result.message,
                            Toast.LENGTH_LONG
                        ).show()

                        // Show transaction details
                        showTransactionSuccess(
                            result.transaction.id,
                            result.transaction.amount
                        )

                        // Clear form
                        binding.etAmount.setText("")
                    }

                    is TransactionResult.Error -> {
                        Log.e(TAG, "Transaction error: ${result.message}")
                        Toast.makeText(
                            this,
                            "Lỗi: ${result.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is TransactionResult.PaymentStatus -> {
                        Log.d(TAG, "Payment status: ${result.status}")
                        Toast.makeText(
                            this,
                            "Trạng thái: ${result.status}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    TransactionResult.Idle -> {
                        // Do nothing
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling transaction result", e)
                Toast.makeText(
                    this,
                    "Lỗi xử lý kết quả: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun processDeposit() {
        val amountStr = binding.etAmount.text?.toString()?.trim() ?: ""

        // Validate input
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = try {
            amountStr.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }

        if (amount <= 0) {
            Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show()
            return
        }

        if (amount < 5000) {
            Toast.makeText(
                this,
                "Số tiền tối thiểu là 5,000 VND",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (amount > 50000000) {
            Toast.makeText(
                this,
                "Số tiền tối đa là 50,000,000 VND",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (currentAccountId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy tài khoản", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Processing deposit: $amount VND for account: $currentAccountId")

        // Process deposit through ViewModel
        viewModel.deposit(currentAccountId, amount)
    }

    private fun showTransactionSuccess(transactionId: String, amount: Double) {
        try {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("✅ Nạp Tiền Thành Công")
                .setMessage(
                    """
                    Mã giao dịch: $transactionId
                    Số tiền: ${formatCurrency(amount)}
                    
                    Tiền sẽ được cập nhật vào tài khoản của bạn trong vài phút.
                    """.trimIndent()
                )
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    finish() // Back to previous screen
                }
                .setCancelable(false)
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing success dialog", e)
        }
    }

    private fun showTestCardsDialog() {
        val message = """
            STRIPE TEST CARDS (Miễn phí)
            
            ✅ Thành công:
            4242 4242 4242 4242
            
            ❌ Bị từ chối:
            4000 0000 0000 0002
            
            💰 Không đủ tiền:
            4000 0000 0000 9995
            
            🔒 Yêu cầu xác thực:
            4000 0025 0000 3155
            
            Expiry: 12/34 (bất kỳ)
            CVC: 123 (bất kỳ)
            ZIP: 12345
            
            Lưu ý: Chỉ hoạt động trong Test Mode!
        """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Test Cards")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun formatCurrency(amount: Double): String {
        return try {
            "%,.0f VND".format(amount)
        } catch (e: Exception) {
            "$amount VND"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy called")
    }

}