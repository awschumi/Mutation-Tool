import org.junit.Assert;
import org.junit.Test;

public class TestFail
{
    @Test
    public void testPass()
    {
        Assert.assertEquals(2, 1+1);
    }

    @Test
    public void testFail()
    {
        Assert.assertEquals(3, 0);
    }
}
