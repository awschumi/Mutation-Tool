package cryptography;

import exception.CryptographyException;

public class Caesar extends Vigenere
{
    public Caesar()
    {
        setName("Caesar");
        setDescription("The description for the Caesar cipher.");
    }

    @Override
    public void setKey(String key) throws CryptographyException
    {
        if(key == null) throw new CryptographyException("The key is null");
        if(key.length() > 1) throw new CryptographyException("The key must have a size of exactly 1 character ("+key.length()+" characters provided)");
        super.setKey(key);
    }

    public void setKey(char key) throws CryptographyException
    {
        this.setKey(String.valueOf(key));
    }
}
