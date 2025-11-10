package com.example.afinal.ui.transaction

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.afinal.R
import com.example.afinal.databinding.ActivityTransactionHistoryBinding
import com.example.afinal.data.model.Transaction
import com.example.afinal.data.model.TransactionType
import com.example.afinal.data.model.TransactionStatus
import com.example.afinal.ui.adapter.TransactionAdapter
import com.example.afinal.viewmodel.transfer.TransactionTransferViewModel

/**
 * Activity hiển thị lịch sử giao dịch
 * Member 3: Transaction & Payment - Week 2
 * FIXED: Tính tổng giá trị đúng (+ cho tiền vào, - cho tiền ra)
 */
class TransactionHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionHistoryBinding
    private val viewModel: TransactionTransferViewModel by viewModels()

    private lateinit var transactionAdapter: TransactionAdapter
    private var currentAccountId: String = ""
    private var currentFilter: FilterType = FilterType.ALL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTransactionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get account ID
        currentAccountId = intent.getStringExtra("ACCOUNT_ID") ?: ""

        setupToolbarWithBack()
        setupUI()
        setupRecyclerView()
        observeViewModel()

        // Load initial data
        loadTransactions()
    }

    private fun setupToolbarWithBack() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Lịch Sử Giao Dịch"
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupUI() {
        // Setup filter buttons
        binding.chipAll.setOnClickListener {
            currentFilter = FilterType.ALL
            updateFilterUI()
            loadTransactions()
        }

        binding.chipDeposit.setOnClickListener {
            currentFilter = FilterType.DEPOSIT
            updateFilterUI()
            loadTransactions()
        }

        binding.chipTransfer.setOnClickListener {
            currentFilter = FilterType.TRANSFER
            updateFilterUI()
            loadTransactions()
        }

        binding.chipWithdraw.setOnClickListener {
            currentFilter = FilterType.WITHDRAW
            updateFilterUI()
            loadTransactions()
        }

        // Swipe to refresh
        binding.swipeRefresh.setOnRefreshListener {
            loadTransactions()
        }
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(
            onItemClick = { transaction ->
                openTransactionDetail(transaction)
            }
        )

        // Set current account ID cho adapter
        transactionAdapter.setCurrentAccountId(currentAccountId)

        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(this@TransactionHistoryActivity)
            adapter = transactionAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
            binding.progressBar.visibility = if (isLoading && !binding.swipeRefresh.isRefreshing) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        // Observe transaction history
        viewModel.transactionHistory.observe(this) { transactions ->
            if (transactions.isEmpty()) {
                showEmptyState()
            } else {
                hideEmptyState()
                val filteredTransactions = filterTransactions(transactions)
                transactionAdapter.submitList(filteredTransactions)
                updateStats(filteredTransactions)
            }
        }
    }

    private fun loadTransactions() {
        if (currentAccountId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy tài khoản", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.loadTransactionHistory(currentAccountId, limit = 100)
    }

    private fun filterTransactions(transactions: List<Transaction>): List<Transaction> {
        return when (currentFilter) {
            FilterType.ALL -> transactions
            FilterType.DEPOSIT -> transactions.filter {
                it.transactionType == TransactionType.DEPOSIT
            }
            FilterType.TRANSFER -> transactions.filter {
                it.transactionType == TransactionType.TRANSFER_INTERNAL ||
                        it.transactionType == TransactionType.TRANSFER_EXTERNAL
            }
            FilterType.WITHDRAW -> transactions.filter {
                it.transactionType == TransactionType.WITHDRAW
            }
        }
    }

    private fun updateFilterUI() {
        // Reset all chips
        binding.chipAll.isChecked = false
        binding.chipDeposit.isChecked = false
        binding.chipTransfer.isChecked = false
        binding.chipWithdraw.isChecked = false

        // Check current filter
        when (currentFilter) {
            FilterType.ALL -> binding.chipAll.isChecked = true
            FilterType.DEPOSIT -> binding.chipDeposit.isChecked = true
            FilterType.TRANSFER -> binding.chipTransfer.isChecked = true
            FilterType.WITHDRAW -> binding.chipWithdraw.isChecked = true
        }
    }

    /**
     * ✅ FIX: Tính tổng đúng theo hướng tiền
     */
    private fun updateStats(transactions: List<Transaction>) {
        val completedTransactions = transactions.filter {
            it.status == TransactionStatus.COMPLETED
        }

        // Tính tổng với dấu đúng (+ cho vào, - cho ra)
        val totalAmount = completedTransactions.sumOf { transaction ->
            when {
                // Tiền VÀO (+)
                isIncomingTransaction(transaction) -> transaction.amount

                // Tiền RA (-)
                isOutgoingTransaction(transaction) -> -transaction.amount

                else -> 0.0
            }
        }

        val totalCount = completedTransactions.size

        // Format với prefix + hoặc -
        binding.tvTotalAmount.text = formatCurrency(totalAmount)
        binding.tvTotalCount.text = "$totalCount giao dịch"

    }

    /**
     * ✅ NEW: Kiểm tra giao dịch tiền vào
     */
    private fun isIncomingTransaction(transaction: Transaction): Boolean {
        return when {
            // Nạp tiền từ bên ngoài
            transaction.transactionType == TransactionType.DEPOSIT -> true

            // Hoàn tiền
            transaction.transactionType == TransactionType.REFUND -> true

            // Nhận chuyển khoản nội bộ (toAccountId = currentAccountId)
            transaction.transactionType == TransactionType.TRANSFER_INTERNAL &&
                    transaction.toAccountId == currentAccountId -> true

            else -> false
        }
    }

    /**
     * ✅ NEW: Kiểm tra giao dịch tiền ra
     */
    private fun isOutgoingTransaction(transaction: Transaction): Boolean {
        return when {
            // Rút tiền
            transaction.transactionType == TransactionType.WITHDRAW -> true

            // Chuyển khoản liên ngân hàng
            transaction.transactionType == TransactionType.TRANSFER_EXTERNAL -> true

            // Thanh toán hóa đơn
            transaction.transactionType == TransactionType.BILL_PAYMENT -> true

            // Chuyển tiền nội bộ đi (fromAccountId = currentAccountId)
            transaction.transactionType == TransactionType.TRANSFER_INTERNAL &&
                    transaction.fromAccountId == currentAccountId -> true

            else -> false
        }
    }

    private fun showEmptyState() {
        binding.rvTransactions.visibility = View.GONE
        binding.layoutEmpty.visibility = View.VISIBLE
    }

    private fun hideEmptyState() {
        binding.rvTransactions.visibility = View.VISIBLE
        binding.layoutEmpty.visibility = View.GONE
    }

    private fun openTransactionDetail(transaction: Transaction) {
        val intent = Intent(this, TransactionDetailActivity::class.java).apply {
            putExtra("TRANSACTION_ID", transaction.id)
            putExtra("ACCOUNT_ID", currentAccountId)
        }
        startActivity(intent)
    }

    private fun formatCurrency(amount: Double): String {
        return "%,.0f VND".format(amount)
    }

    /**
     * ✅ NEW: Format với dấu + hoặc -
     */
    private fun formatCurrencyWithSign(amount: Double): String {
        val prefix = when {
            amount > 0 -> "+"
            amount < 0 -> ""  // Dấu - đã có sẵn
            else -> ""
        }
        return "$prefix%,.0f VND".format(amount)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_transaction_history, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_refresh -> {
                loadTransactions()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    enum class FilterType {
        ALL, DEPOSIT, TRANSFER, WITHDRAW
    }
}