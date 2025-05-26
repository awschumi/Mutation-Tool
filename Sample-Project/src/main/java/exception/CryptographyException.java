package exception;

public class CryptographyException extends Exception
{
    String errorMsg;

    public CryptographyException(String msg)
    {
        super(msg);
    }
}
