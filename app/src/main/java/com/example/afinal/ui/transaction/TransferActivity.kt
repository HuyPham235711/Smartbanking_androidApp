    package com.example.afinal.ui.transaction

    import android.os.Bundle
    import android.view.View
    import android.widget.Toast
    import androidx.activity.viewModels
    import androidx.appcompat.app.AppCompatActivity
    import com.example.afinal.databinding.ActivityTransferBinding
    import com.example.afinal.viewmodel.transaction.TransactionResult
    import com.example.afinal.viewmodel.transaction.TransactionViewModel

    /**
     * Activity xử lý Internal Transfer (Chuyển tiền nội bộ)
     * Member 3: Transaction & Payment - Week 2
     */
    class TransferActivity : AppCompatActivity() {

        private lateinit var binding: ActivityTransferBinding
        private val viewModel: TransactionViewModel by viewModels()

        private var currentAccountId: String = ""

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            binding = ActivityTransferBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Get account ID from intent
            currentAccountId = intent.getStringExtra("ACCOUNT_ID") ?: ""

            setupToolbarWithBack()
            setupUI()
            observeViewModel()
        }

        private fun setupToolbarWithBack() {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowHomeEnabled(true)
                title = "Chuyển Tiền Nội Bộ"
            }

            binding.toolbar.setNavigationOnClickListener {
                finish()
            }
        }

        private fun setupUI() {
            // Setup toolbar

            // Preset amount buttons
            binding.btnAmount10k.setOnClickListener {
                binding.etAmount.setText("10000")
            }
            binding.btnAmount50k.setOnClickListener {
                binding.etAmount.setText("50000")
            }
            binding.btnAmount100k.setOnClickListener {
                binding.etAmount.setText("100000")
            }
            binding.btnAmount500k.setOnClickListener {
                binding.etAmount.setText("500000")
            }

            // Transfer button
            binding.btnTransfer.setOnClickListener {
                processTransfer()
            }

            // Info button
            binding.btnInfo.setOnClickListener {
                showTransferInfo()
            }
        }

        private fun observeViewModel() {
            // Observe loading state
            viewModel.isLoading.observe(this) { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.btnTransfer.isEnabled = !isLoading
            }

            // Observe transaction result
            viewModel.transactionStatus.observe(this) { result ->
                when (result) {
                    is TransactionResult.Success -> {
                        Toast.makeText(
                            this,
                            result.message,
                            Toast.LENGTH_LONG
                        ).show()

                        showTransferSuccess(
                            result.transaction.id,
                            result.transaction.amount,
                            result.transaction.toAccountId ?: ""
                        )

                        // Clear form
                        binding.etRecipientId.setText("")
                        binding.etAmount.setText("")
                        binding.etDescription.setText("")
                    }

                    is TransactionResult.Error -> {
                        Toast.makeText(
                            this,
                            "Lỗi: ${result.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    else -> {}
                }
            }
        }

        private fun processTransfer() {
            val recipientId = binding.etRecipientId.text.toString().trim()
            val amountStr = binding.etAmount.text.toString()
            val description = binding.etDescription.text.toString().trim()

            // Validate recipient ID
            if (recipientId.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập ID người nhận", Toast.LENGTH_SHORT).show()
                return
            }

            if (recipientId == currentAccountId) {
                Toast.makeText(
                    this,
                    "Không thể chuyển tiền cho chính mình",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            // Validate amount
            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show()
                return
            }

            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show()
                return
            }

            if (amount < 1000) {
                Toast.makeText(
                    this,
                    "Số tiền tối thiểu là 1,000 VND",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            if (currentAccountId.isEmpty()) {
                Toast.makeText(this, "Không tìm thấy tài khoản", Toast.LENGTH_SHORT).show()
                return
            }

            // Show confirmation dialog
            showConfirmDialog(recipientId, amount, description)
        }

        private fun showConfirmDialog(recipientId: String, amount: Double, description: String) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác Nhận Chuyển Tiền")
                .setMessage(
                    """
                    Người nhận: $recipientId
                    Số tiền: ${formatCurrency(amount)}
                    Phí giao dịch: ${formatCurrency(1000.0)}
                    
                    Bạn có chắc chắn muốn chuyển tiền?
                    """.trimIndent()
                )
                .setPositiveButton("Xác Nhận") { dialog, _ ->
                    // Process transfer
                    viewModel.transferInternal(
                        fromAccountId = currentAccountId,
                        toAccountId = recipientId,
                        amount = amount
                    )
                    dialog.dismiss()
                }
                .setNegativeButton("Hủy", null)
                .show()
        }

        private fun showTransferSuccess(
            transactionId: String,
            amount: Double,
            recipientId: String
        ) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("✅ Chuyển Tiền Thành Công")
                .setMessage(
                    """
                    Mã giao dịch: $transactionId
                    Người nhận: $recipientId
                    Số tiền: ${formatCurrency(amount)}
                    
                    Giao dịch đã hoàn tất thành công!
                    """.trimIndent()
                )
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    finish()
                }
                .setCancelable(false)
                .show()
        }

        private fun showTransferInfo() {
            val message = """
                💡 Thông Tin Chuyển Tiền Nội Bộ
                
                • Số tiền tối thiểu: 1,000 VND
                • Số tiền tối đa: 100,000,000 VND
                • Phí giao dịch: 1,000 VND + 0.1%
                • Thời gian xử lý: Ngay lập tức
                
                Lưu ý:
                - Chỉ chuyển cho các tài khoản trong hệ thống
                - Kiểm tra kỹ ID người nhận trước khi chuyển
                - Không thể hoàn tác sau khi chuyển
            """.trimIndent()

            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Thông Tin")
                .setMessage(message)
                .setPositiveButton("Đã Hiểu", null)
                .show()
        }

        private fun formatCurrency(amount: Double): String {
            return "%,.0f VND".format(amount)
        }

        override fun onSupportNavigateUp(): Boolean {
            finish()
            return true
        }
    }