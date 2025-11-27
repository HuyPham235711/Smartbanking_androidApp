package com.example.afinal.data.sync

import com.example.afinal.data.account.Account
import com.example.afinal.data.bill.BillPaymentEntity
import com.example.afinal.data.interest.InterestRate
import com.example.afinal.data.mortgage.MortgageAccountEntity
import com.example.afinal.data.mortgage.MortgageScheduleEntity
import com.example.afinal.data.savings.SavingsAccount
import com.example.afinal.data.sync.dto.*
import com.example.afinal.data.transaction.TransactionEntity
import java.util.UUID

object SyncMapper {

    // -----------------------------
    // 🔸 Account Mapping (UUID-based)
    // -----------------------------
    fun Account.toAccountDTO() = AccountDTO(
        id = id,
        username = username,
        password = password,
        fullName = fullName,
        email = email,
        phone = phone,
        role = role,
        balance = balance
    )

    fun AccountDTO.toEntity() = Account(
        id = id,
        username = username,
        password = password,
        fullName = fullName,
        email = email,
        phone = phone,
        role = role,
        balance = balance
    )

    fun AccountDTO.toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "username" to username,
        "password" to password,
        "fullName" to fullName,
        "email" to email,
        "phone" to phone,
        "role" to role,
        "balance" to balance
    )

    // ✅ Firestore → AccountDTO
    fun Map<String, Any>.toAccountDTO(): AccountDTO = AccountDTO(
        id = this["id"] as? String ?: UUID.randomUUID().toString(),
        username = this["username"] as? String ?: "",
        password = this["password"] as? String ?: "",
        fullName = this["fullName"] as? String ?: "",
        email = this["email"] as? String ?: "",
        phone = this["phone"] as? String ?: "",
        role = this["role"] as? String ?: "Customer",
        balance = (this["balance"] as? Number)?.toDouble() ?: 0.0
    )

    // -----------------------------
    // 🔸 SavingsAccount Mapping
    // -----------------------------
    fun SavingsAccount.toSavingsDTO() = SavingsDTO(
        id = id,
        ownerAccountId = ownerAccountId,
        balance = balance,
        interestRate = interestRate,
        termMonths = termMonths,
        openDate = openDate,
        maturityDate = maturityDate
    )

    fun SavingsDTO.toEntity() = SavingsAccount(
        id = id,
        ownerAccountId = ownerAccountId,
        balance = balance,
        interestRate = interestRate,
        termMonths = termMonths,
        openDate = openDate,
        maturityDate = maturityDate
    )

    fun SavingsDTO.toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "ownerAccountId" to ownerAccountId,
        "balance" to balance,
        "interestRate" to interestRate,
        "termMonths" to termMonths,
        "openDate" to openDate,
        "maturityDate" to maturityDate
    )

    fun Map<String, Any>.toSavingsDTO(): SavingsDTO = SavingsDTO(
        id = this["id"] as? String ?: UUID.randomUUID().toString(),
        ownerAccountId = this["ownerAccountId"] as? String ?: "",
        balance = (this["balance"] as? Number)?.toDouble() ?: 0.0,
        interestRate = (this["interestRate"] as? Number)?.toDouble() ?: 0.0,
        termMonths = (this["termMonths"] as? Number)?.toInt() ?: 0,
        openDate = this["openDate"] as? String ?: "",
        maturityDate = this["maturityDate"] as? String ?: ""
    )

    // -----------------------------
    // 🔸 MortgageAccount Mapping
    // -----------------------------
    fun MortgageAccountEntity.toMortgageDTO() = MortgageDTO(
        id = id,
        accountName = accountName,
        principal = principal,
        annualInterestRate = annualInterestRate,
        termMonths = termMonths,
        startDate = startDate,
        status = status,
        remainingBalance = remainingBalance,
        ownerAccountId = ownerAccountId
    )

    fun MortgageDTO.toEntity() = MortgageAccountEntity(
        id = id,
        accountName = accountName,
        principal = principal,
        annualInterestRate = annualInterestRate,
        termMonths = termMonths,
        startDate = startDate,
        status = status,
        remainingBalance = remainingBalance,
        ownerAccountId = ownerAccountId
    )

    fun MortgageDTO.toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "accountName" to accountName,
        "principal" to principal,
        "annualInterestRate" to annualInterestRate,
        "termMonths" to termMonths,
        "startDate" to startDate,
        "status" to status,
        "remainingBalance" to remainingBalance,
        "ownerAccountId" to ownerAccountId
    )

    fun Map<String, Any>.toMortgageDTO() = MortgageDTO(
        id = (this["id"] as? String ?: UUID.randomUUID().toString()),
        accountName = this["accountName"] as? String ?: "",
        principal = (this["principal"] as? Double ?: 0.0),
        annualInterestRate = (this["annualInterestRate"] as? Double ?: 0.0),
        termMonths = (this["termMonths"] as? Long ?: 0L).toInt(),
        startDate = (this["startDate"] as? Long ?: 0L),
        status = this["status"] as? String ?: "ACTIVE",
        remainingBalance = (this["remainingBalance"] as? Double ?: 0.0),
        ownerAccountId = this["ownerAccountId"] as? String ?: ""
    )

    // -----------------------------
    // 🔸 MortgageSchedule Mapping
    // -----------------------------
    fun MortgageScheduleEntity.toScheduleDTO() = ScheduleDTO(
        id = id,
        mortgageId = mortgageId,
        period = period,
        dueDate = dueDate,
        principalAmount = principalAmount,
        interestAmount = interestAmount,
        totalAmount = totalAmount,
        status = status
    )

    fun ScheduleDTO.toEntity() = MortgageScheduleEntity(
        id = id,
        mortgageId = mortgageId,
        period = period,
        dueDate = dueDate,
        principalAmount = principalAmount,
        interestAmount = interestAmount,
        totalAmount = totalAmount,
        status = status
    )

    fun ScheduleDTO.toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "mortgageId" to mortgageId,
        "period" to period,
        "dueDate" to dueDate,
        "principalAmount" to principalAmount,
        "interestAmount" to interestAmount,
        "totalAmount" to totalAmount,
        "status" to status
    )

    fun Map<String, Any>.toScheduleDTO() = ScheduleDTO(
        id = (this["id"] as? String ?: UUID.randomUUID().toString()),
        mortgageId = (this["mortgageId"] as? String ?: UUID.randomUUID().toString()),
        period = (this["period"] as? Long ?: 0L).toInt(),
        dueDate = (this["dueDate"] as? Long ?: 0L),
        principalAmount = (this["principalAmount"] as? Double ?: 0.0),
        interestAmount = (this["interestAmount"] as? Double ?: 0.0),
        totalAmount = (this["totalAmount"] as? Double ?: 0.0),
        status = this["status"] as? String ?: "PENDING"
    )

    // -----------------------------
    // 🔸 Transaction Mapping
    // -----------------------------
    fun TransactionEntity.toTransactionDTO() = TransactionDTO(
        id = id,
        accountId = accountId,
        amount = amount,
        currency = currency,
        type = type,
        description = description,
        timestamp = timestamp
    )

    fun TransactionDTO.toEntity() = TransactionEntity(
        id = id,
        accountId = accountId,
        amount = amount,
        currency = currency,
        type = type,
        description = description,
        timestamp = timestamp
    )

    fun TransactionDTO.toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "accountId" to accountId,
        "amount" to amount,
        "currency" to currency,
        "type" to type,
        "description" to (description ?: ""),
        "timestamp" to timestamp
    )

    fun Map<String, Any>.toTransactionDTO(): TransactionDTO = TransactionDTO(
        id = this["id"] as? String ?: UUID.randomUUID().toString(),
        accountId = this["accountId"] as? String ?: "",
        amount = (this["amount"] as? Number)?.toDouble() ?: 0.0,
        currency = this["currency"] as? String ?: "VND",
        type = this["type"] as? String ?: "",
        description = this["description"] as? String,
        timestamp = (this["timestamp"] as? Number)?.toLong() ?: 0L
    )

    // -----------------------------
    // 🔸 InterestRate Mapping
    // -----------------------------
    fun InterestRate.toInterestDTO() = InterestDTO(
        termMonths = termMonths,
        rate = rate,
        updatedAt = System.currentTimeMillis()
    )

    fun InterestDTO.toEntity() = InterestRate(
        termMonths = termMonths,
        rate = rate
    )

    fun InterestDTO.toMap(): Map<String, Any> = mapOf(
        "termMonths" to termMonths,
        "rate" to rate,
        "updatedAt" to updatedAt
    )

    fun Map<String, Any>.toInterestDTO() = InterestDTO(
        termMonths = (this["termMonths"] as? Long ?: 0L).toInt(),
        rate = (this["rate"] as? Double ?: 0.0),
        updatedAt = (this["updatedAt"] as? Long ?: 0L)
    )

    // -----------------------------
    // 🔸 BillPayment Mapping
    // -----------------------------
    fun BillPaymentEntity.toBillPaymentDTO() = BillPaymentDTO(
        id = id,
        accountId = accountId,
        billType = billType,
        serviceProvider = serviceProvider,
        customerCode = customerCode,
        amount = amount,
        currency = currency,
        status = status,
        timestamp = timestamp,
        description = description,
        billPeriod = billPeriod
    )

    fun BillPaymentDTO.toEntity() = BillPaymentEntity(
        id = id,
        accountId = accountId,
        billType = billType,
        serviceProvider = serviceProvider,
        customerCode = customerCode,
        amount = amount,
        currency = currency,
        status = status,
        timestamp = timestamp,
        description = description,
        billPeriod = billPeriod
    )

    fun BillPaymentDTO.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "accountId" to accountId,
        "billType" to billType,
        "serviceProvider" to serviceProvider,
        "customerCode" to customerCode,
        "amount" to amount,
        "currency" to currency,
        "status" to status,
        "timestamp" to timestamp,
        "description" to (description ?: ""),
        "billPeriod" to (billPeriod ?: "")
    )

    fun Map<String, Any?>.toBillPaymentDTO() = BillPaymentDTO(
        id = this["id"] as? String ?: "",
        accountId = this["accountId"] as? String ?: "",
        billType = this["billType"] as? String ?: "",
        serviceProvider = this["serviceProvider"] as? String ?: "",
        customerCode = this["customerCode"] as? String ?: "",
        amount = (this["amount"] as? Number)?.toDouble() ?: 0.0,
        currency = this["currency"] as? String ?: "VND",
        status = this["status"] as? String ?: "COMPLETED",
        timestamp = (this["timestamp"] as? Number)?.toLong() ?: 0L,
        description = this["description"] as? String,
        billPeriod = this["billPeriod"] as? String
    )
}
