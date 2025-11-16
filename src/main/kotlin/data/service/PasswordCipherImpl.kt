package com.ranjan.data.service

import com.ranjan.domain.common.services.PasswordCipher
import org.mindrot.jbcrypt.BCrypt

class PasswordCipherImpl : PasswordCipher {

    override suspend fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    override suspend fun verifyPassword(
        password: String,
        hashedPassword: String
    ): Boolean {
        return BCrypt.checkpw(password, hashedPassword)
    }

}