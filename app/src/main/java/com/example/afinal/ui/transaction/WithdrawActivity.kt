package com.example.afinal.ui.transaction

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.afinal.databinding.ActivityWithdrawBinding
import com.example.afinal.data.service.BankAccountInfo
import com.example.afinal.data.service.BankInfo
import com.example.afinal.data.service.WithdrawService
import com.example.afinal.viewmodel.WithdrawResult
import com.example.afinal.viewmodel.WithdrawViewModel

/**
 * Activity xử lý Withdraw (Rút tiền về ngân hàng)
 * Member 3: Transaction & Payment - Week 3
 *
 * Features:
 * - Chọn ngân hàng Việt Nam
 * - Nhập thông tin tài khoản
 * - Xác thực thông tin
 * - Xử lý rút tiền
 */
class WithdrawActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWithdrawBinding
    private val viewModel: WithdrawViewModel by viewModels()
    private val withdrawService = WithdrawService()

    private var currentAccountId: String = ""
    private var selectedBank: BankInfo? = null
    private val bankList: List<BankInfo> by lazy { withdrawService.getVietnameseBanks() }

    companion object {
        private const val TAG = "WithdrawActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWithdrawBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get account ID from intent
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

        setupToolbarWithBack()
        setupUI()
        setupBankSpinner()
        observeViewModel()
    }

    private fun setupToolbarWithBack() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Rút Tiền"
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupUI() {


        // Setup preset amount buttons (if they exist in your layout)
        try {
            // Try to access preset buttons - will fail silently if not exist
            binding.root.findViewById<View>(com.example.afinal.R.id.btnAmount50k)?.setOnClickListener {
                binding.etAmount.setText("50000")
            }
            binding.root.findViewById<View>(com.example.afinal.R.id.btnAmount100k)?.setOnClickListener {
                binding.etAmount.setText("100000")
            }
            binding.root.findViewById<View>(com.example.afinal.R.id.btnAmount500k)?.setOnClickListener {
                binding.etAmount.setText("500000")
            }
            binding.root.findViewById<View>(com.example.afinal.R.id.btnAmount1m)?.setOnClickListener {
                binding.etAmount.setText("1000000")
            }
        } catch (e: Exception) {
            // Preset buttons don't exist in layout, skip
        }

        // Withdraw button - use main withdraw button from layout
        binding.btnWithdraw.setOnClickListener {
            processWithdraw()
        }

        // Info button (if exists)
        try {
            binding.root.findViewById<View>(com.example.afinal.R.id.btnInfo)?.setOnClickListener {
                showWithdrawInfo()
            }
        } catch (e: Exception) {
            // Info button doesn't exist
        }
    }

    private fun setupBankSpinner() {
        val bankNames = bankList.map { "${it.name} (${it.code})" }
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            bankNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerBank.adapter = adapter
        binding.spinnerBank.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedBank = bankList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedBank = null
            }
        }
    }

    private fun observeViewModel() {
        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnWithdraw.isEnabled = !isLoading
        }

        // Observe withdraw result
        viewModel.withdrawResult.observe(this) { result ->
            when (result) {
                is WithdrawResult.Success -> {
                    Toast.makeText(
                        this,
                        result.message,
                        Toast.LENGTH_LONG
                    ).show()

                    showWithdrawSuccess(
                        result.transaction.id,
                        result.transaction.amount
                    )

                    clearForm()
                }

                is WithdrawResult.Error -> {
                    Toast.makeText(
                        this,
                        "Lỗi: ${result.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                WithdrawResult.Idle -> {
                    // Do nothing
                }
            }
        }
    }

    private fun processWithdraw() {
        val amountStr = binding.etAmount.text?.toString()?.trim() ?: ""
        val accountNumber = binding.etAccountNumber.text?.toString()?.trim() ?: ""
        val accountHolder = binding.etAccountHolder.text?.toString()?.trim() ?: ""

        // Validate bank selection
        if (selectedBank == null) {
            Toast.makeText(this, "Vui lòng chọn ngân hàng", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate account number
        if (accountNumber.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tài khoản", Toast.LENGTH_SHORT).show()
            binding.etAccountNumber.requestFocus()
            return
        }

        if (accountNumber.length < 8) {
            Toast.makeText(
                this,
                "Số tài khoản không hợp lệ (tối thiểu 8 ký tự)",
                Toast.LENGTH_SHORT
            ).show()
            binding.etAccountNumber.requestFocus()
            return
        }

        // Validate account holder
        if (accountHolder.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên chủ tài khoản", Toast.LENGTH_SHORT).show()
            binding.etAccountHolder.requestFocus()
            return
        }

        if (accountHolder.length < 3) {
            Toast.makeText(
                this,
                "Tên chủ tài khoản không hợp lệ (tối thiểu 3 ký tự)",
                Toast.LENGTH_SHORT
            ).show()
            binding.etAccountHolder.requestFocus()
            return
        }

        // Validate amount
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show()
            binding.etAmount.requestFocus()
            return
        }

        val amount = try {
            amountStr.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show()
            binding.etAmount.requestFocus()
            return
        }

        if (amount <= 0) {
            Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show()
            return
        }

        if (amount < 50000) {
            Toast.makeText(
                this,
                "Số tiền tối thiểu là 50,000 VND",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (amount > 20000000) {
            Toast.makeText(
                this,
                "Số tiền tối đa là 20,000,000 VND",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Show confirmation dialog
        showConfirmDialog(amount, accountNumber, accountHolder)
    }

    private fun showConfirmDialog(
        amount: Double,
        accountNumber: String,
        accountHolder: String
    ) {
        val fee = 5000.0 + (amount * 0.005) // 5000 VND + 0.5%
        val totalAmount = amount + fee

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Xác Nhận Rút Tiền")
            .setMessage(
                """
                Ngân hàng: ${selectedBank?.name}
                Số TK: $accountNumber
                Chủ TK: $accountHolder
                
                Số tiền rút: ${formatCurrency(amount)}
                Phí giao dịch: ${formatCurrency(fee)}
                Tổng trừ: ${formatCurrency(totalAmount)}
                
                ⚠️ Lưu ý:
                - Tiền sẽ về tài khoản trong 1-2 ngày
                - Không thể hoàn tác sau khi xác nhận
                - Kiểm tra kỹ thông tin trước khi xác nhận
                
                Xác nhận rút tiền?
                """.trimIndent()
            )
            .setPositiveButton("Xác Nhận") { dialog, _ ->
                val bankAccountInfo = BankAccountInfo(
                    bankName = selectedBank!!.name,
                    bankCode = selectedBank!!.code,
                    accountNumber = accountNumber,
                    accountHolder = accountHolder
                )

                viewModel.withdraw(
                    accountId = currentAccountId,
                    amount = amount,
                    bankAccountInfo = bankAccountInfo
                )

                dialog.dismiss()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showWithdrawSuccess(transactionId: String, amount: Double) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("✅ Rút Tiền Thành Công")
            .setMessage(
                """
                Mã giao dịch: $transactionId
                Số tiền: ${formatCurrency(amount)}
                
                Tiền sẽ được chuyển về tài khoản ngân hàng của bạn trong vòng 1-2 ngày làm việc.
                
                Bạn có thể kiểm tra trạng thái giao dịch trong lịch sử.
                """.trimIndent()
            )
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                finish() // Back to previous screen
            }
            .setCancelable(false)
            .show()
    }

    private fun showWithdrawInfo() {
        val message = """
            💰 Thông Tin Rút Tiền
            
            • Số tiền tối thiểu: 50,000 VND
            • Số tiền tối đa: 20,000,000 VND
            • Phí giao dịch: 5,000 VND + 0.5%
            • Thời gian: 1-2 ngày làm việc
            
            Lưu ý:
            - Kiểm tra kỹ thông tin tài khoản
            - Tên chủ tài khoản phải khớp
            - Không thể hoàn tác sau khi rút
            - Chỉ rút về tài khoản của bạn
            
            🏦 Hỗ trợ các ngân hàng:
            Vietcombank, Techcombank, BIDV, 
            Vietinbank, ACB, MB Bank, và nhiều
            ngân hàng khác...
        """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Hướng Dẫn")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun clearForm() {
        binding.etAmount.setText("")
        binding.etAccountNumber.setText("")
        binding.etAccountHolder.setText("")
        binding.spinnerBank.setSelection(0)
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
    }
}