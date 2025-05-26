package cryptography;

import exception.CryptographyException;

public class Vigenere extends AbstractCryptography
{
    public static final char[] LATIN_ALPHABET_LOWERCASE = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
    public static final char[] LATIN_ALPHABET_UPPERCASE = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
    public static final char[] LATIN_ALPHABET_WITH_NUMBERS = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','0','1','2','3','4','5','6','7','8','9'};
    public static final char[] LATIN_ALPHABET_EXTENDED = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','0','1','2','3','4','5','6','7','8','9',',',';','.',':','-','_','+','"','*','#','%','&','/','|','\\','(',')','\'','?','!',' ','[',']','{','}'};

    /// The alphabet used, e.g. ['a','b','c', ..., 'y','z']
    private char[] alphabet;

    /// The key used for encryption, e.g. "mysecretkey"
    private String key;

    public Vigenere()
    {
        this.setName("Vigenère");
        this.setDescription("The Vigenère cipher is an encryption method similar to the Caesar cipher, except that every letter has a different shifting. It uses an alphabet, usually the latin alphabet, and a key consisting of a word of the alphabet.");
    }

    public void setAlphabet(char[] alphabet) {
        if(alphabet != null)
            if(alphabet.length > 0)
                this.alphabet = alphabet;
    }

    public void setKey(String key) throws CryptographyException
    {
        if(key != null)
        {
            if (!key.isEmpty())
            {
                // Check if the key is valid
                for (char c : key.toCharArray()) {
                    if (getCharIndexInAlphabet(c) == -1) // A character of the key is not in the alphabet
                        throw new CryptographyException("The key contains a forbidden character: " + c);
                }
                this.key = key;
            }
            else
            {
                throw new CryptographyException("The key is empty");
            }
        }
        else
        {
            throw new CryptographyException("The key is null");
        }
    }

    public char[] getAlphabet() {
        return alphabet;
    }

    public String getKey() {
        return key;
    }

    /**
     *
     * @param c The character to find, e.g. 'b'
     * @return The position of the character in the alphabet, if not found then returns -1
     */
    public int getCharIndexInAlphabet(char c)
    {
        for(int i = 0; i < this.alphabet.length; i++)
        {
            if(this.alphabet[i] == c) return i;
        }
        return -1;
    }

    public String encrypt(String textToEncrypt)
    {
        String encrypted = "";
        int keyPosition = 0;    // The position of the key

        /*
         * Browse every character of the text to encrypt
         * If the character is a valid letter, then we will shift
         * the letter with the key
         */
        for(char c: textToEncrypt.toCharArray())
        {
            int indexOfChar = getCharIndexInAlphabet(c);
            if(indexOfChar != -1) // Valid character
            {
                int indexOfKeyChar = getCharIndexInAlphabet(key.toCharArray()[keyPosition]);
                if(indexOfKeyChar != -1) // This case must always happen
                {
                    int newPosition = (indexOfChar + indexOfKeyChar) % alphabet.length;
                    char newChar = alphabet[newPosition];
                    encrypted += newChar;
                    keyPosition = (keyPosition + 1) % key.length(); // If the end is reached, go back to the start of the key
                }
            }
            else    // add the char without doing anything
            {
                encrypted += c;
            }
        }

        return encrypted;
    }

    public String decrypt(String textToDecrypt)
    {
        String decrypted = "";
        int keyPosition = 0;    // The position of the key

        /*
         * Browse every character of the text to encrypt
         * If the character is a valid letter, then we will shift
         * the letter with the key
         */
        for(char c: textToDecrypt.toCharArray())
        {
            int indexOfChar = getCharIndexInAlphabet(c);
            if(indexOfChar != -1) // Valid character
            {
                int indexOfKeyChar = getCharIndexInAlphabet(key.toCharArray()[keyPosition]);
                if(indexOfKeyChar != -1) // This case must always happen
                {

                    int newPosition = (indexOfChar - indexOfKeyChar) % alphabet.length;
                    if(newPosition < 0) newPosition += alphabet.length; // The remainder can be negative, and we want it between 0 and alphabet.length-1
                    char newChar = alphabet[newPosition];
                    decrypted += newChar;
                    keyPosition = (keyPosition + 1) % key.length(); // If the end is reached, go back to the start of the key
                }
            }
            else    // add the char without doing anything
            {
                decrypted += c;
            }
        }

        return decrypted;
    }
}
