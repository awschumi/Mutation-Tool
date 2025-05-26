import cryptography.Caesar;
import cryptography.Vigenere;
import exception.CryptographyException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.JUnit4;

public class TestCaesar
{
    @Test
    public void testName()
    {
        Caesar caesar = new Caesar();
        Assert.assertEquals("Caesar", caesar.getName());
    }

    @Test
    public void testDescription()
    {
        Caesar caesar = new Caesar();
        Assert.assertEquals("The description for the Caesar cipher.", caesar.getDescription());
    }

    @Test
    public void testAlphabet()
    {
        Caesar caesar = new Caesar();
        caesar.setAlphabet(Vigenere.LATIN_ALPHABET_UPPERCASE);
        Assert.assertArrayEquals(new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'}, caesar.getAlphabet());
    }

    @Test
    public void testKey()
    {
        Caesar caesar = new Caesar();
        caesar.setAlphabet(Vigenere.LATIN_ALPHABET_UPPERCASE);

        try
        {
            caesar.setKey(null);
        } catch (CryptographyException e) {
            Assert.assertEquals("The key is null", e.getMessage());
        }

        try
        {
            caesar.setKey("");
        } catch (CryptographyException e) {
            Assert.assertEquals("The key is empty", e.getMessage());
        }

        try
        {
            caesar.setKey("AB");
        } catch (CryptographyException e) {
            Assert.assertEquals("The key must have a size of exactly 1 character (2 characters provided)", e.getMessage());
        }

        try
        {
            caesar.setKey("y");
        } catch (CryptographyException e) {
            Assert.assertEquals("The key contains a forbidden character: y", e.getMessage());
        }

        try
        {
            caesar.setKey('S');
            Assert.assertEquals("S", caesar.getKey());
        } catch (CryptographyException e) {}
    }

    @Test
    public void testEncryptionDecryption()
    {
        Caesar caesar = new Caesar();
        caesar.setAlphabet(Vigenere.LATIN_ALPHABET_LOWERCASE);

        try
        {
            caesar.setKey("g");
            String original = "mysecretplan";
            String encrypted = caesar.encrypt(original);
            String decrypted = caesar.decrypt(encrypted);

            Assert.assertEquals("seykixkzvrgt", encrypted);
            Assert.assertEquals("mysecretplan", decrypted);
        }
        catch (CryptographyException e) {}
    }
}
