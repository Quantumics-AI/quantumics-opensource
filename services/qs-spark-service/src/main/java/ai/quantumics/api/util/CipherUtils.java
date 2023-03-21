package ai.quantumics.api.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CipherUtils {

  public static Map<String, SecretKey> secretKeys = new HashMap<>();
  public static Map<String, IvParameterSpec> ivKeys = new HashMap<>();
  
  private static final String SECRET_KEY = "H3ur!stic23#";
  
  public static SecretKey getKeyFromPassword(String password, String salt)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    
    if(secretKeys.containsKey(password)) {
      return secretKeys.get(password);
    }else {
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
      KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
      SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
      secretKeys.put(password, secret);
      
      return secret;
    }
  }

  public static IvParameterSpec generateIv(String key) {
    if(ivKeys.containsKey(key)) {
      return ivKeys.get(key);
    } else {
      byte[] iv = new byte[16];
      new SecureRandom().nextBytes(iv);
      IvParameterSpec ivSpec = new IvParameterSpec(iv);
      ivKeys.put(key, ivSpec);
      
      return ivSpec;
    }
  }

  public static String encrypt(String algorithm, String input, SecretKey key, IvParameterSpec iv)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
      InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

    if(input == null || input.isEmpty()) {
      return "";
    }

    Cipher cipher = Cipher.getInstance(algorithm);
    cipher.init(Cipher.ENCRYPT_MODE, key, iv);
    byte[] cipherText = cipher.doFinal(input.getBytes());
    return Base64.getEncoder().encodeToString(cipherText);
  }

  public static String decrypt(String algorithm, String cipherText, SecretKey key,
      IvParameterSpec iv)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
      InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
    
    if(cipherText == null || cipherText.isEmpty()) {
      return "";
    }
    
    Cipher cipher = Cipher.getInstance(algorithm);
    cipher.init(Cipher.DECRYPT_MODE, key, iv);
    byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
    return new String(plainText);
  }

  public static String padString(String input) {
    if (input == null)
      return "";

    String lastChars = input.substring(input.length() - 6);
    return input.replace(lastChars, "xxxxxx");
  }
  
  public static String encrypt(String strToEncrypt) {
    try {
      //byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
      byte[] iv = Hex.decodeHex("dc0da04af8fee58593442bf834b30739");
      IvParameterSpec ivspec = new IvParameterSpec(iv);
 
      //SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), iv, 65536, 256);
      Key key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
      
      //SecretKey tmp = factory.generateSecret(spec);
      //SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
 
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      //cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
      cipher.init(Cipher.ENCRYPT_MODE, key, ivspec);
      return Base64.getEncoder()
          .encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      //log.info("Error while encrypting: {}", e.getMessage());
    }
    return strToEncrypt;
  }
  
  public static String decrypt(String strToDecrypt) {
    try {
      //byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
      byte[] iv = Hex.decodeHex("dc0da04af8fee58593442bf834b30739");
      IvParameterSpec ivspec = new IvParameterSpec(iv);
 
      //SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), iv, 65536, 256);
      Key key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
      
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
      //cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
      cipher.init(Cipher.DECRYPT_MODE, key, ivspec);
      return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
    } catch (Exception e) {
      //log.info("Error while decrypting: {}", e.getMessage());
    }
    return strToDecrypt;
  }
  
}
