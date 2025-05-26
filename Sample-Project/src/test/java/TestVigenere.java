import cryptography.Vigenere;
import exception.CryptographyException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.JUnit4;

/**
 * Class for testing Vigenere features
 */
public class TestVigenere
{
    @Test
    public void testName()
    {
        Vigenere vigenere = new Vigenere();
        Assert.assertEquals("Vigenère", vigenere.getName());
    }

    @Test
    public void testDescription()
    {
        Vigenere vigenere = new Vigenere();
        Assert.assertEquals("The Vigenère cipher is an encryption method similar to the Caesar cipher, except that every letter has a different shifting. It uses an alphabet, usually the latin alphabet, and a key consisting of a word of the alphabet.", vigenere.getDescription());
    }

    @Test
    public void testAlphabet()
    {
        Vigenere vigenere = new Vigenere();
        vigenere.setAlphabet(Vigenere.LATIN_ALPHABET_UPPERCASE);
        Assert.assertArrayEquals(new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'}, vigenere.getAlphabet());
    }

    @Test
    public void testKey()
    {
        Vigenere vigenere = new Vigenere();
        vigenere.setAlphabet(Vigenere.LATIN_ALPHABET_UPPERCASE);

        try
        {
            vigenere.setKey(null);
        } catch (CryptographyException e) {
            Assert.assertEquals("The key is null", e.getMessage());
        }

        try
        {
            vigenere.setKey("");
        } catch (CryptographyException e) {
            Assert.assertEquals("The key is empty", e.getMessage());
        }

        try
        {
            vigenere.setKey("BADKEy");
        } catch (CryptographyException e) {
            Assert.assertEquals("The key contains a forbidden character: y", e.getMessage());
        }

        try
        {
            vigenere.setKey("SECRET");
            Assert.assertEquals("SECRET", vigenere.getKey());
        } catch (CryptographyException e) {}
    }

    @Test
    public void testEncryptionDecryption()
    {
        Vigenere vigenere = new Vigenere();
        vigenere.setAlphabet(Vigenere.LATIN_ALPHABET_LOWERCASE);

        try
        {
            vigenere.setKey("key");
            String original = "mysecretplan";
            String encrypted = vigenere.encrypt(original);
            String decrypted = vigenere.decrypt(encrypted);

            Assert.assertEquals("wcqogpoxnvel", encrypted);
            Assert.assertEquals("mysecretplan", decrypted);
        }
        catch (CryptographyException e) {}
    }
}
