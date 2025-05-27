import org.junit.Assert;
import org.junit.Test;

public class TestFail
{
    @Test(timeout=5000)
    public void testPass()
    {
        Assert.assertEquals(2, 1+1);
    }

    @Test(timeout=5000)
    public void testFail()
    {
        Assert.assertEquals(3, 0);
    }
}
