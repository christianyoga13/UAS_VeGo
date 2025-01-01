// PromoViewModel.kt
package com.example.uts_vego

import android.util.Log
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Data class representing a Voucher with a code and a discount percentage.
 */
data class Voucher(
    val code: String = "",
    val discountPercentage: Int = 0 // New field for discount percentage
)

/**
 * ViewModel to manage vouchers.
 */
class PromoViewModel : ViewModel() {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // List of available vouchers
    private val _availableVouchers: SnapshotStateList<Voucher> = mutableStateListOf(
        Voucher(code = "DISCOUNT10", discountPercentage = 10),
        Voucher(code = "FREESHIP50", discountPercentage = 0), // Assuming FREESHIP50 has no percentage
        Voucher(code = "CASHBACK20", discountPercentage = 20)
    )
    val availableVouchers: SnapshotStateList<Voucher> = _availableVouchers

    // List of user's claimed vouchers
    private val _userVouchers: SnapshotStateList<Voucher> = mutableStateListOf()
    val userVouchers: SnapshotStateList<Voucher> = _userVouchers

    // Current user's UID
    private val userId: String?
        get() = auth.currentUser?.uid

    init {
        fetchUserVouchers()
    }

    /**
     * Fetches the user's vouchers from Firestore.
     */
    fun fetchUserVouchers() {
        val uid = userId
        if (uid == null) {
            _userVouchers.clear()
            return
        }

        viewModelScope.launch {
            try {
                val vouchersSnapshot = firestore
                    .collection("users")
                    .document(uid)
                    .collection("vouchers")
                    .get()
                    .await()

                val vouchers = vouchersSnapshot.documents.mapNotNull { it.toObject<Voucher>() }
                _userVouchers.clear()
                _userVouchers.addAll(vouchers)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun addVoucher(voucher: Voucher) {
        val uid = userId ?: return

        viewModelScope.launch {
            try {
                // Menambahkan voucher ke subkoleksi vouchers
                firestore
                    .collection("users")
                    .document(uid)
                    .collection("vouchers")
                    .add(voucher)
                    .await()

                // Update state lokal
                _userVouchers.add(voucher)

                // Hapus voucher dari daftar available jika hanya satu kali klaim
                _availableVouchers.removeIf { it.code == voucher.code }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    /**
     * Generates a new promo code with a random discount percentage, adds it to available vouchers, and returns the Voucher.
     *
     * @return The generated Voucher.
     */
    fun generateAndAddPromoCode(): Voucher {
        val promoCode = generatePromoCode()
        val discountPercentage = generateRandomDiscount()
        val newVoucher = Voucher(code = promoCode, discountPercentage = discountPercentage)
        _availableVouchers.add(newVoucher)
        return newVoucher
    }

    /**
     * Generates a random 8-character promo code consisting of uppercase letters and digits.
     *
     * @return The generated promo code.
     */
    private fun generatePromoCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..8)
            .map { chars.random() }
            .joinToString("")
    }

    /**
     * Generates a random discount percentage between 5% and 50%.
     *
     * @return The generated discount percentage.
     */
    private fun generateRandomDiscount(): Int {
        return (5..50).random()
    }

    /**
     * Use a voucher: remove it from available and add to user vouchers.
     *
     * @param voucher The Voucher to use.
     */
    fun useVoucher(voucher: Voucher) {
        val uid = userId ?: return

        viewModelScope.launch {
            try {
                // Add to Firestore
                firestore
                    .collection("users")
                    .document(uid)
                    .collection("vouchers")
                    .add(voucher)
                    .await()

                // Update local state
                _userVouchers.add(voucher)

                // Remove from available vouchers if needed
                _availableVouchers.removeIf { it.code == voucher.code }
            } catch (e: Exception) {
                // Handle exceptions
                e.printStackTrace()
            }
        }
    }

    fun deleteVoucher(voucher: Voucher) {
        val uid = userId ?: return

        viewModelScope.launch {
            try {
                firestore
                    .collection("users")
                    .document(uid)
                    .collection("vouchers")
                    .whereEqualTo("code", voucher.code)
                    .get()
                    .await()
                    .documents
                    .firstOrNull()
                    ?.reference
                    ?.delete()
                    ?.await()

                _userVouchers.removeIf { it.code == voucher.code }
            } catch (e: Exception) {
                Log.e("PromoViewModel", "Error deleting voucher", e)
            }
        }
    }
}
