package at.gadermaier.argon2;

import at.gadermaier.argon2.model.Argon2Type;

public class Constants {

    /* Memory block size in bytes */
    public static final int ARGON2_BLOCK_SIZE = 1024;
    public static final int ARGON2_QWORDS_IN_BLOCK = ARGON2_BLOCK_SIZE / 8;
    /* Number of pseudo-random values generated by one call to Blake in Argon2i
       to
       generate reference block positions
     */
    public static final int ARGON2_ADDRESSES_IN_BLOCK = 128;
    /* Pre-hashing digest length and its extension*/
    public static final int ARGON2_PREHASH_DIGEST_LENGTH = 64;
    public static final int ARGON2_PREHASH_SEED_LENGTH = 72;
    /* Number of synchronization points between lanes per pass */
    public static final int ARGON2_SYNC_POINTS = 4;
    /* Flags to determine which fields are securely wiped (default = no wipe). */
    public static final int ARGON2_DEFAULT_FLAGS = 0;
    public static final int ARGON2_VERSION_10 = 0x10;
    public static final int ARGON2_VERSION_13 = 0x13;

    public static class Defaults {

        public static final int OUTLEN_DEF = 32;
        public static final int T_COST_DEF = 3;
        public static final int LOG_M_COST_DEF = 12;
        public static final int LANES_DEF = 1;
        public static final Argon2Type TYPE_DEF = Argon2Type.Argon2i;
        public static final int VERSION_DEF = ARGON2_VERSION_13;
        public static final int ARGON2_VERSION_NUMBER = ARGON2_VERSION_13;
        public static final boolean ENCODED_ONLY = false;
        public static final boolean RAW_ONLY = false;
    }
    //public static int ARGON2_FLAG_CLEAR_PASSWORD (UINT32_C(1) << 0)
    //public static int ARGON2_FLAG_CLEAR_SECRET (UINT32_C(1) << 1)

    /*
    * Argon2 input parameter restrictions
    */
    public static class Constraints {

        public static final int MAX_PASSWORD_LEN = 128;

        /* Minimum and maximum number of lanes (degree of parallelism) */
        public static final int MIN_PARALLELISM = 1;
        public static final int MAX_PARALLELISM = 16777216;

        /* Minimum and maximum digest size in bytes */
        public static final int MIN_OUTLEN = 4;
        public static final int MAX_OUTLEN = Integer.MAX_VALUE;

        /* Minimum and maximum number of memory blocks (each of BLOCK_SIZE bytes) */
        public static final int MIN_MEMORY = (2 * ARGON2_SYNC_POINTS); /* 2 blocks per slice */

        /* Minimum and maximum number of passes */
        public static final int MIN_ITERATIONS = 1;
        public static final int MAX_ITERATIONS = Integer.MAX_VALUE;

        /* Minimum and maximum password length in bytes */
        public static final int MIN_PWD_LENGTH = 0;
        public static final int MAX_PWD_LENGTH = Integer.MAX_VALUE;

        /* Minimum and maximum salt length in bytes */
        public static final int MIN_SALT_LENGTH = 0;
        public static final int MAX_SALT_LENGTH = Integer.MAX_VALUE;

        /* Minimum and maximum key length in bytes */
        public static final int MAX_SECRET_LENGTH = Integer.MAX_VALUE;

        /* Minimum and maximum associated model length in bytes */
        public static final int MAX_AD_LENGTH = Integer.MAX_VALUE;
    }

    public static class Messages {
        public static final String P_MIN_MSG = "degree of parallelism cannot be smaller than one";
        public static final String P_MAX_MSG = "parallelism cannot be greater than 16777216";

        public static final String M_MIN_MSG = "memory too small";

        public static final String T_MIN_MSG = "number of iterations cannot be less than one";
        public static final String T_MAX_MSG = "number of iterations too high";

        public static final String PWD_MIN_MSG = "password too short";
        public static final String PWD_MAX_MSG = "password too long";

        public static final String SALT_MIN_MSG = "salt too short";
        public static final String SALT_MAX_MSG = "salt too long";

        public static final String SECRET_MAX_MSG = "secret too long";
        public static final String ADDITIONAL_MAX_MSG = "additional data too long";

    }

}
