package com.thething.cos.lightledger.internal

import java.security.MessageDigest

/**
 * Utility for computing Merkle roots using SHA-256.
 */
internal object MerkleTree {

    fun computeRoot(leaves: List<ByteArray>): ByteArray {
        require(leaves.isNotEmpty()) { "Cannot compute Merkle root of an empty list." }
        var level = leaves.toList()
        while (level.size > 1) {
            level = level.chunked(2).map { pair ->
                val left = pair[0]
                val right = if (pair.size == 2) pair[1] else pair[0]
                hashPair(left, right)
            }
        }
        return level.first()
    }

    private fun hashPair(left: ByteArray, right: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(left.size + right.size)
        System.arraycopy(left, 0, buffer, 0, left.size)
        System.arraycopy(right, 0, buffer, left.size, right.size)
        return digest.digest(buffer)
    }
}
