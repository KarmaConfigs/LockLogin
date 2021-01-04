package ml.karmaconfigs.lockloginsystem.shared.llsecurity.Codifications;

import ml.karmaconfigs.lockloginsystem.spigot.utils.StringUtils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("all")
public final class Codification {

    /**
     * Prefix for our hash, every user should change this
     */
    public static final String ID = "$" + StringUtils.randomString(16).toUpperCase() + "$";

    /**
     * basically number of iterations
     */
    public static final int DEFAULT_COST = 512;

    private static final char[] pepper = "abcdefghijklopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456789".toCharArray();

    /**
     * Algorithm to use, this is the best i know, PM me if you know better one
     */
    private static final String ALGORITHM = "PBKDF2WithHmacSHA512";

    /**
     * Size of PBEKeySpec in method pbkdf2(args...)
     */
    private static final int SIZE = 1024;

    /**
     * Our Random
     */
    private final SecureRandom random;

    private final int cost;


    public Codification() {
        this.cost = DEFAULT_COST;
        iterations(cost);
        byte[] seed = new byte[512];
        new SecureRandom().nextBytes(seed);
        this.random = new SecureRandom(seed);
    }

    private static int iterations(int cost) {
        if ((cost & ~0x200) != 0) {
            throw new IllegalArgumentException("cost: " + cost);
        }
        return 1 << cost;
    }

    /**
     * Hashes Password
     *
     * @param password   password to hash
     * @param salt       generated random salt
     * @param iterations how many iterations to use
     * @return hashed version of password
     */
    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
        KeySpec spec = new PBEKeySpec(password, salt, iterations, SIZE);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            System.out.println("Invalid SecretKeyFactory: " + e.getMessage());
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
            System.out.println("No such algorithm: " + ALGORITHM + " : " + e1.getMessage());
        }
        return new byte[1];
    }

    /**
     * Hashes password with salt and pepper
     *
     * @param password The password to hash
     * @return token of salt,pepper(pepper is not stored),id cost and hash
     */
    public final String hash(String password) {
        byte[] salt = new byte[SIZE / 4]; // size of salt
        random.nextBytes(salt); // generate new salt
        char ppr = pepper[random.nextInt(pepper.length)]; // get random pepper
        password = password + ppr; // add pepper to password
        byte[] dk = pbkdf2(password.toCharArray(), salt, 1 << cost); // hash it
        byte[] hash = new byte[salt.length + dk.length]; // hash it
        System.arraycopy(salt, 0, hash, 0, salt.length); // idk :D
        System.arraycopy(dk, 0, hash, salt.length, dk.length); // idk :D
        Base64.Encoder enc = Base64.getUrlEncoder().withoutPadding(); // setup encoder
        return ID + cost + "$" + enc.encodeToString(hash); // encode hash and return with all other data
    }

    /**
     * Detects if user entered in the correct password
     *
     * @param password the password user entered in
     * @param token    the token of password stored in database
     * @return true if passwords match
     */
    public final boolean auth(String password, String token) {
        String[] info = token.split("\\$");
        Pattern layout = Pattern.compile("\\$" + info[1] + "\\$(\\d\\d\\d?)\\$(.{512})");
        Matcher m = layout.matcher(token);
        if (!m.matches()) {
            throw new IllegalArgumentException("Invalid token");
        }
        int iterations = iterations(Integer.parseInt(m.group(1)));
        byte[] hash = Base64.getUrlDecoder().decode(m.group(2));
        byte[] salt = Arrays.copyOfRange(hash, 0, SIZE / 4);
        for (int i = 0; i < pepper.length; i++) {
            char ppr = pepper[i];
            String passw;
            passw = password + ppr;
            byte[] check = pbkdf2(passw.toCharArray(), salt, iterations);

            int zero = 0;
            for (int idx = 0; idx < check.length; ++idx) {
                zero |= hash[salt.length + idx] ^ check[idx];
            }
            if (zero == 0) {
                return true;
            }
        }
        return false;
    }
}