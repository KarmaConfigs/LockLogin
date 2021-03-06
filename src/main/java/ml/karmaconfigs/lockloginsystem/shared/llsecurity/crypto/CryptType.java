package ml.karmaconfigs.lockloginsystem.shared.llsecurity.crypto;

/**
 * GNU LESSER GENERAL PUBLIC LICENSE
 * Version 2.1, February 1999
 * <p>
 * Copyright (C) 1991, 1999 Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 * <p>
 * [This is the first released version of the Lesser GPL.  It also counts
 * as the successor of the GNU Library Public License, version 2, hence
 * the version number 2.1.]
 */
public enum CryptType {
    /**
     * LockLogin compatible encryption type
     */
    SHA256,
    /**
     * LockLogin compatible encryption type
     */
    SHA512,
    /**
     * LockLogin compatible encryption type
     */
    BCrypt,
    /**
     * LockLogin compatible encryption type
     */
    BCryptPHP,
    /**
     * LockLogin compatible encryption type
     */
    ARGON2I,
    /**
     * LockLogin compatible encryption type
     */
    ARGON2ID,
    /**
     * LockLogin unknown encryption type
     */
    UNKNOWN,
    /**
     * No encryption type
     */
    NONE
}
