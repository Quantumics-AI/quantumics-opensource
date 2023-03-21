/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.user.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Component;

@Component
public class ProtectedUser {

  private static final String ALPHABET =
      "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
  
  private static final String DEFAULT_PSWD = "Welcome@123";
  private static final int ITERATIONS = 1000;
  private static final int KEY_LENGTH = 128;
  private static final Random RANDOM = new SecureRandom();

  /**
   * @param password
   * @param salt
   * @return {@link String}
   */
  public String generateSecurePassword(String password, final String salt) {
    String returnValue = null;
    byte[] securePassword = null;
    if(password == null) {
      securePassword = hash(DEFAULT_PSWD.toCharArray(), ALPHABET.getBytes());
    } else {
      securePassword = hash(password.toCharArray(), salt.getBytes());
    }
    
    returnValue = Base64.getEncoder().encodeToString(securePassword);
    return returnValue;
  }

  /** @return {@link String} */
  public String getSalt() {
    final StringBuilder returnValue = new StringBuilder(32);
    for (int i = 0; i < 32; i++) {
      returnValue.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
    }
    return new String(returnValue);
  }
  
  public String getDefaultSalt() {
    return ALPHABET;
  }
  
  private byte[] hash(final char[] password, final byte[] salt) {
    final PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
    Arrays.fill(password, Character.MIN_VALUE);
    try {
      final SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      return skf.generateSecret(spec).getEncoded();
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);
    } finally {
      spec.clearPassword();
    }
  }

  /**
   * @param providedPassword
   * @param securedPassword
   * @param salt
   * @return {@link Boolean}
   */
  public boolean verifyUserPassword(
      final String providedPassword, final String securedPassword, final String salt) {
    boolean returnValue = false;
    final String newSecurePassword = generateSecurePassword(providedPassword, salt);
    returnValue = newSecurePassword.equalsIgnoreCase(securedPassword);
    return returnValue;
  }
  
}
