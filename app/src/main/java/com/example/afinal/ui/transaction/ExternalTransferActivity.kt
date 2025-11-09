package com.example.afinal.ui.transaction

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.afinal.databinding.ActivityExternalTransferBinding
import com.example.afinal.data.service.BankInfo
import com.example.afinal.data.model.RecipientBankInfo
import com.example.afinal.data.service.WithdrawService
import com.example.afinal.viewmodel.ExternalTransferResult
import com.example.afinal.viewmodel.ExternalTransferViewModel

/**
 * Activity xử lý External Transfer (Chuyển khoản liên ngân hàng)
 * Member 3: Transaction & Payment - Week 4
 *
 * Features:
 * - Tra cứu tên chủ tài khoản
 * - Xác thực OTP (tích hợp M1)
 * - Chuyển khoản liên ngân hàng
 */
class ExternalTransferActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExternalTransferBinding
    private val viewModel: ExternalTransferViewModel by viewModels()
    private val withdrawService = WithdrawService()

    private var currentAccountId: String = ""
    private var selectedBank: BankInfo? = null
    private val bankList: List<BankInfo> by lazy { withdrawService.getVietnameseBanks() }

    private var otpTimer: CountDownTimer? = null
    private var otpSent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityExternalTransferBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentAccountId = intent.getStringExtra("ACCOUNT_ID") ?: ""

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
            title = "Chuyển Khoản Liên Ngân Hàng"
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupUI() {

        // Preset amounts
        binding.btnAmount100k.setOnClickListener {
            binding.etAmount.setText("100000")
        }
        binding.btnAmount500k.setOnClickListener {
            binding.etAmount.setText("500000")
        }
        binding.btnAmount1m.setOnClickListener {
            binding.etAmount.setText("1000000")
        }
        binding.btnAmount5m.setOnClickListener {
            binding.etAmount.setText("5000000")
        }

        // Inquiry account name
        binding.btnInquiry.setOnClickListener {
            inquiryAccountName()
        }

        // Send OTP
        binding.btnSendOtp.setOnClickListener {
            sendOTP()
        }

        // Transfer button
        binding.btnTransfer.setOnClickListener {
            processTransfer()
        }

        // Info button
        binding.btnInfo.setOnClickListener {
            showTransferInfo()
        }

        // Initially hide OTP section
        binding.layoutOtp.visibility = View.GONE
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
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedBank = bankList[position]
                // Reset inquiry when bank changes
                binding.tvAccountName.text = ""
                binding.tvAccountName.visibility = View.GONE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedBank = null
            }
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnTransfer.isEnabled = !isLoading && otpSent
        }

        viewModel.transferResult.observe(this) { result ->
            when (result) {
                is ExternalTransferResult.Success -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                    showTransferSuccess(
                        result.transaction.id,
                        result.transaction.amount,
                        result.transaction.toAccountId ?: ""
                    )
                    clearForm()
                }

                is ExternalTransferResult.Error -> {
                    Toast.makeText(
                        this,
                        "Lỗi: ${result.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                is ExternalTransferResult.AccountInquiry -> {
                    // Show account holder name
                    binding.tvAccountName.text = "Chủ TK: ${result.accountName}"
                    binding.tvAccountName.visibility = View.VISIBLE
                    Toast.makeText(
                        this,
                        "Tra cứu thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is ExternalTransferResult.OtpSent -> {
                    otpSent = true
                    binding.layoutOtp.visibility = View.VISIBLE
                    binding.btnSendOtp.isEnabled = false
                    startOtpTimer()
                    Toast.makeText(
                        this,
                        "Mã OTP đã được gửi đến số điện thoại của bạn",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is ExternalTransferResult.StatusCheck -> {
                    // Thêm branch này
                    Toast.makeText(
                        this,
                        "Trạng thái giao dịch: ${result.status}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                ExternalTransferResult.Idle -> {
                    // Do nothing
                }
            }
        }
    }

    /**
     * Tra cứu tên chủ tài khoản
     */
    private fun inquiryAccountName() {
        val accountNumber = binding.etRecipientAccount.text.toString().trim()

        if (selectedBank == null) {
            Toast.makeText(this, "Vui lòng chọn ngân hàng", Toast.LENGTH_SHORT).show()
            return
        }

        if (accountNumber.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tài khoản", Toast.LENGTH_SHORT).show()
            return
        }

        if (accountNumber.length < 8) {
            Toast.makeText(
                this,
                "Số tài khoản không hợp lệ",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Call inquiry API
        viewModel.inquiryAccountName(
            bankCode = selectedBank!!.code,
            accountNumber = accountNumber
        )
    }

    /**
     * Gửi mã OTP
     * TODO Week 4: Tích hợp với M1 (OTP Service)
     */
    private fun sendOTP() {
        val amountStr = binding.etAmount.text.toString()
        val accountNumber = binding.etRecipientAccount.text.toString().trim()

        // Validate inputs
        if (selectedBank == null) {
            Toast.makeText(this, "Vui lòng chọn ngân hàng", Toast.LENGTH_SHORT).show()
            return
        }

        if (accountNumber.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tài khoản", Toast.LENGTH_SHORT).show()
            return
        }

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO Week 4: Call M1 OTP Service
        // otpService.sendOTP(currentAccountId, "EXTERNAL_TRANSFER", amount)

        // Simulate OTP sent
        viewModel.simulateOtpSent()
    }

    /**
     * Xử lý chuyển khoản
     */
    private fun processTransfer() {
        val amountStr = binding.etAmount.text.toString()
        val accountNumber = binding.etRecipientAccount.text.toString().trim()
        val accountHolder = binding.etRecipientName.text.toString().trim()
        val otpCode = binding.etOtp.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        // Validate all inputs
        if (selectedBank == null) {
            Toast.makeText(this, "Vui lòng chọn ngân hàng", Toast.LENGTH_SHORT).show()
            return
        }

        if (accountNumber.isEmpty() || accountNumber.length < 8) {
            Toast.makeText(this, "Số tài khoản không hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }

        if (accountHolder.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên chủ tài khoản", Toast.LENGTH_SHORT).show()
            return
        }

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
            Toast.makeText(this, "Số tiền tối thiểu là 1,000 VND", Toast.LENGTH_SHORT).show()
            return
        }

        if (!otpSent) {
            Toast.makeText(this, "Vui lòng gửi mã OTP trước", Toast.LENGTH_SHORT).show()
            return
        }

        if (otpCode.isEmpty() || otpCode.length != 6) {
            Toast.makeText(this, "Mã OTP phải có 6 chữ số", Toast.LENGTH_SHORT).show()
            return
        }

        // Show confirmation
        showConfirmDialog(amount, accountNumber, accountHolder, otpCode, description)
    }

    private fun showConfirmDialog(
        amount: Double,
        accountNumber: String,
        accountHolder: String,
        otpCode: String,
        description: String
    ) {
        val fee = 1000.0 + (amount * 0.001) // 1000 VND + 0.1%
        val totalAmount = amount + fee

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Xác Nhận Chuyển Khoản")
            .setMessage(
                """
                Ngân hàng: ${selectedBank?.name}
                Số TK: $accountNumber
                Chủ TK: $accountHolder
                
                Số tiền: ${formatCurrency(amount)}
                Phí: ${formatCurrency(fee)}
                Tổng: ${formatCurrency(totalAmount)}
                
                Nội dung: ${description.ifEmpty { "Không có" }}
                
                Xác nhận chuyển khoản?
                """.trimIndent()
            )
            .setPositiveButton("Xác Nhận") { dialog, _ ->
                val recipientInfo = RecipientBankInfo(
                    bankName = selectedBank!!.name,
                    bankCode = selectedBank!!.code,
                    accountNumber = accountNumber,
                    accountHolder = accountHolder
                )

                viewModel.transferExternal(
                    fromAccountId = currentAccountId,
                    recipientInfo = recipientInfo,
                    amount = amount,
                    otpCode = otpCode,
                    description = description
                )

                dialog.dismiss()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showTransferSuccess(
        transactionId: String,
        amount: Double,
        recipientAccount: String
    ) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("✅ Chuyển Khoản Thành Công")
            .setMessage(
                """
                Mã GD: $transactionId
                Người nhận: $recipientAccount
                Số tiền: ${formatCurrency(amount)}
                
                Giao dịch đã được xử lý thành công!
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
            🏦 Chuyển Khoản Liên Ngân Hàng
            
            • Số tiền tối thiểu: 1,000 VND
            • Số tiền tối đa: 100,000,000 VND
            • Phí: 1,000 VND + 0.1%
            • Thời gian: 1-30 phút
            
            Lưu ý:
            - Sử dụng tính năng tra cứu tên
            - Kiểm tra kỹ thông tin trước khi chuyển
            - Yêu cầu xác thực OTP
            - Không thể hoàn tác
        """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Hướng Dẫn")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun startOtpTimer() {
        otpTimer?.cancel()

        otpTimer = object : CountDownTimer(180000, 1000) { // 3 minutes
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.tvOtpTimer.text = "Mã OTP còn hiệu lực: ${seconds}s"
            }

            override fun onFinish() {
                binding.tvOtpTimer.text = "Mã OTP đã hết hạn"
                binding.btnSendOtp.isEnabled = true
                otpSent = false
                binding.layoutOtp.visibility = View.GONE
            }
        }.start()
    }

    private fun clearForm() {
        binding.etAmount.setText("")
        binding.etRecipientAccount.setText("")
        binding.etRecipientName.setText("")
        binding.etDescription.setText("")
        binding.etOtp.setText("")
        binding.spinnerBank.setSelection(0)
        binding.tvAccountName.visibility = View.GONE
        binding.layoutOtp.visibility = View.GONE
        otpSent = false
        otpTimer?.cancel()
    }

    private fun formatCurrency(amount: Double): String {
        return "%,.0f VND".format(amount)
    }

    override fun onDestroy() {
        super.onDestroy()
        otpTimer?.cancel()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}