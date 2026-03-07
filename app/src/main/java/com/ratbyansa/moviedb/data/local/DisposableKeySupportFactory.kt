package com.ratbyansa.moviedb.data.local

import androidx.sqlite.db.SupportSQLiteOpenHelper
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

class DisposableKeySupportFactory(private val decryptedKey: ByteArray) :
    SupportOpenHelperFactory(decryptedKey) {

    override fun create(configuration: SupportSQLiteOpenHelper.Configuration): SupportSQLiteOpenHelper {
        // Buat helper bawaan SQLCipher
        val helper = super.create(configuration)

        // 🧹 LANGKAH PENTING: Hapus kunci dari RAM segera setelah digunakan!
        decryptedKey.fill(0)

        return helper
    }
}