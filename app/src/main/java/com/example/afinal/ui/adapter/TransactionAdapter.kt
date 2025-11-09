package com.example.afinal.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.afinal.R
import com.example.afinal.databinding.ItemTransactionBinding
import com.example.afinal.data.model.Transaction
import com.example.afinal.data.model.TransactionType
import com.example.afinal.data.model.TransactionStatus
import java.text.SimpleDateFormat
import java.util.*

/**
 * RecyclerView Adapter cho Transaction List
 * Member 3: Transaction & Payment - Week 2
 * FIXED: Hiển thị đúng +/- dựa vào account ID
 */
class TransactionAdapter(
    private val onItemClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    // ✅ NEW: Lưu current account ID
    private var currentAccountId: String = ""

    /**
     * ✅ NEW: Set current account ID
     */
    fun setCurrentAccountId(accountId: String) {
        currentAccountId = accountId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding, onItemClick, currentAccountId)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TransactionViewHolder(
        private val binding: ItemTransactionBinding,
        private val onItemClick: (Transaction) -> Unit,
        private val currentAccountId: String  // ✅ NEW: Nhận account ID
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.apply {
                // ✅ FIX: Xác định đúng hướng tiền dựa vào account ID
                val displayInfo = getTransactionDisplay(transaction, currentAccountId)

                tvTransactionType.text = displayInfo.title
                ivTransactionIcon.setImageResource(displayInfo.icon)

                // Set amount với prefix đúng
                val amountText = "${displayInfo.prefix}${formatCurrency(transaction.amount)}"
                tvAmount.text = amountText
                tvAmount.setTextColor(
                    ContextCompat.getColor(root.context, displayInfo.color)
                )

                // Set status
                tvStatus.text = getStatusText(transaction.status)
                val statusColor = getStatusColor(transaction.status)
                tvStatus.setTextColor(ContextCompat.getColor(root.context, statusColor))

                // Set timestamp
                tvTimestamp.text = formatTimestamp(transaction.timestamp.toDate())

                // Set description
                if (transaction.description.isNullOrEmpty()) {
                    tvDescription.text = "Không có mô tả"
                } else {
                    tvDescription.text = transaction.description
                }

                // Click listener
                root.setOnClickListener {
                    onItemClick(transaction)
                }
            }
        }

        /**
         * ✅ FIX: Xác định đúng hướng tiền dựa vào account ID
         */
        private fun getTransactionDisplay(
            transaction: Transaction,
            currentAccountId: String
        ): TransactionDisplay {

            return when (transaction.transactionType) {
                TransactionType.DEPOSIT -> TransactionDisplay(
                    icon = R.drawable.ic_deposit,
                    title = "Nạp tiền",
                    prefix = "+",
                    color = R.color.green_500
                )

                TransactionType.WITHDRAW -> TransactionDisplay(
                    icon = R.drawable.ic_withdraw,
                    title = "Rút tiền",
                    prefix = "-",
                    color = R.color.red_500
                )

                TransactionType.TRANSFER_INTERNAL -> {
                    // ✅ FIX: Kiểm tra xem mình là người gửi hay nhận
                    if (transaction.fromAccountId == currentAccountId) {
                        // Mình chuyển đi
                        val recipientId = transaction.toAccountId?.takeLast(8) ?: "..."
                        TransactionDisplay(
                            icon = R.drawable.ic_transfer,
                            title = "Chuyển tiền cho *$recipientId",
                            prefix = "-",
                            color = R.color.red_500
                        )
                    } else {
                        // Mình nhận được
                        val senderId = transaction.fromAccountId.takeLast(8)
                        TransactionDisplay(
                            icon = R.drawable.ic_transfer,
                            title = "Nhận tiền từ *$senderId",
                            prefix = "+",
                            color = R.color.green_500
                        )
                    }
                }

                TransactionType.TRANSFER_EXTERNAL -> TransactionDisplay(
                    icon = R.drawable.ic_transfer,
                    title = "Chuyển khoản ngân hàng",
                    prefix = "-",
                    color = R.color.red_500
                )

                TransactionType.BILL_PAYMENT -> TransactionDisplay(
                    icon = R.drawable.ic_bill,
                    title = "Thanh toán hóa đơn",
                    prefix = "-",
                    color = R.color.red_500
                )

                TransactionType.REFUND -> TransactionDisplay(
                    icon = R.drawable.ic_refund,
                    title = "Hoàn tiền",
                    prefix = "+",
                    color = R.color.green_500
                )
            }
        }

        /**
         * ✅ NEW: Data class cho thông tin hiển thị
         */
        private data class TransactionDisplay(
            val icon: Int,
            val title: String,
            val prefix: String,
            val color: Int
        )

        private fun getStatusText(status: TransactionStatus): String {
            return when (status) {
                TransactionStatus.PENDING -> "Đang chờ"
                TransactionStatus.PROCESSING -> "Đang xử lý"
                TransactionStatus.COMPLETED -> "Hoàn thành"
                TransactionStatus.FAILED -> "Thất bại"
                TransactionStatus.CANCELLED -> "Đã hủy"
                TransactionStatus.REFUNDED -> "Đã hoàn tiền"
            }
        }

        private fun getStatusColor(status: TransactionStatus): Int {
            return when (status) {
                TransactionStatus.COMPLETED -> R.color.green_500
                TransactionStatus.PENDING,
                TransactionStatus.PROCESSING -> R.color.orange_500
                TransactionStatus.FAILED,
                TransactionStatus.CANCELLED -> R.color.red_500
                TransactionStatus.REFUNDED -> R.color.blue_500
            }
        }

        private fun formatCurrency(amount: Double): String {
            return "%,.0f VND".format(amount)
        }

        private fun formatTimestamp(date: Date): String {
            val now = Calendar.getInstance()
            val transactionDate = Calendar.getInstance().apply { time = date }

            return when {
                isSameDay(now, transactionDate) -> {
                    SimpleDateFormat("'Hôm nay,' HH:mm", Locale("vi", "VN")).format(date)
                }
                isYesterday(now, transactionDate) -> {
                    SimpleDateFormat("'Hôm qua,' HH:mm", Locale("vi", "VN")).format(date)
                }
                else -> {
                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN")).format(date)
                }
            }
        }

        private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
        }

        private fun isYesterday(now: Calendar, date: Calendar): Boolean {
            val yesterday = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
            }
            return isSameDay(yesterday, date)
        }
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}