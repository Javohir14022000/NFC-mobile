package dev.mobile.nfcmobile.hce

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import dev.mobile.nfcmobile.model.ReceivedData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject

class CardEmulationService : HostApduService() {

    companion object {
        private val AID = byteArrayOf(0xF0.toByte(), 0x01, 0x02, 0x03, 0x04, 0x05, 0x06)
        private val OK = byteArrayOf(0x90.toByte(), 0x00)
        private val UNKNOWN = byteArrayOf(0x6D.toByte(), 0x00)

        private val _dataFlow = MutableSharedFlow<ReceivedData>(replay = 1)
        val dataFlow = _dataFlow.asSharedFlow()
    }

    override fun processCommandApdu(apdu: ByteArray, extras: Bundle?): ByteArray {
        return when {
            isSelectAid(apdu) -> OK
            isPutData(apdu) -> handlePutData(apdu)
            else -> UNKNOWN
        }
    }

    override fun onDeactivated(reason: Int) {}

    private fun isSelectAid(apdu: ByteArray): Boolean {
        if (apdu.size < 5) return false
        if (apdu[1] != 0xA4.toByte()) return false
        val aidLength = apdu[4].toInt() and 0xFF
        if (apdu.size < 5 + aidLength) return false
        val receivedAid = apdu.copyOfRange(5, 5 + aidLength)
        return receivedAid.contentEquals(AID)
    }

    private fun isPutData(apdu: ByteArray): Boolean =
        apdu.size >= 5 && apdu[1] == 0xDA.toByte()

    private fun handlePutData(apdu: ByteArray): ByteArray {
        return try {
            val dataLength = apdu[4].toInt() and 0xFF
            val jsonBytes = apdu.copyOfRange(5, 5 + dataLength)
            val json = JSONObject(String(jsonBytes, Charsets.UTF_8))
            val data = ReceivedData(
                merchantId = json.optString("merchantId", ""),
                terminalId = json.optString("terminalId", ""),
                amount = json.optString("amount", "")
            )
            _dataFlow.tryEmit(data)
            OK
        } catch (e: Exception) {
            UNKNOWN
        }
    }
}
