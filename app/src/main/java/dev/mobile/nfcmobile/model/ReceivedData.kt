package dev.mobile.nfcmobile.model

import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class ReceivedData(
    val merchantId: String,
    val terminalId: String,
    val amount: String,
    val receivedAt: String = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
)
