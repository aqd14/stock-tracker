/**
 * 
 */
package main.java.utility;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * @author doquocanh-macbook
 *
 */
public class SecurityUtils {
	// Salt added to hash string
	static final byte[] salt = new BigInteger("39e858f86df9b909a8c87cb8d9ad599", 16).toByteArray();

	/**
	 * Hash a string
	 * 
	 * @param data
	 * @return Hashed value
	 */
	public static String hash(String data) {
		System.out.println("Password: " + data);
		KeySpec spec = new PBEKeySpec(data.toCharArray(), salt, 64, 128);
		SecretKeyFactory f;
		byte[] hash;
		try {
			f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			hash = f.generateSecret(spec).getEncoded();
			Base64.Encoder enc = Base64.getEncoder();
			// System.out.printf("salt: %s%n", enc.encodeToString(salt));
			// System.out.printf("hash: %s%n", enc.encodeToString(hash));
			return enc.encodeToString(hash);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}
}
