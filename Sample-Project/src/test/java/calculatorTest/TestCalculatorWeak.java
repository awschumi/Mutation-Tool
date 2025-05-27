package calculatorTest;

import Calculator.Calculator;
import org.junit.Test;
import org.junit.Assert;

public class TestCalculatorWeak
{
    public final static double EPS = 0.000001;

    @Test(timeout=5000)
    public void addTest()
    {
        // 0+0=0
        Assert.assertEquals(0D, Calculator.add(0D,0D), EPS);
    }

    @Test(timeout=5000)
    public void subTest()
    {
        // 0-0=0
        Assert.assertEquals(0D, Calculator.sub(0D,0D), EPS);
    }

    @Test(timeout=5000)
    public void multTest()
    {
        // 0*0=0
        Assert.assertEquals(0D, Calculator.mult(0D,0D), EPS);
    }

    @Test(timeout=5000)
    public void divTest()
    {
        // 0/1=0
        try
        {
            Assert.assertEquals(0D, Calculator.div(0D,1D), EPS);
        }
        catch (Exception e){}
    }

    @Test(timeout=5000)
    public void modTest()
    {
        // 0%1=0
        try{ Assert.assertEquals(0, Calculator.mod(0,1), EPS);}
        catch (Exception e) {}
    }

    @Test(timeout=5000)
    public void absTest()
    {
        // abs(0)=0
        Assert.assertEquals(0D, Calculator.abs(0D), EPS);
    }

    @Test(timeout=5000)
    public void maxTest()
    {
        // max(0,0)=0
        Assert.assertEquals(0D, Calculator.max(0D,0D), EPS);
    }

    @Test(timeout=5000)
    public void minTest()
    {
        // min(0,0)=0
        Assert.assertEquals(0D, Calculator.min(0D,0D), EPS);
    }

    @Test(timeout=5000)
    public void sqrtTest()
    {
        // sqrt(0)=0
        try
        {
            Assert.assertEquals(0D, Calculator.sqrt(0D), EPS);
        }
        catch (Exception e){}
    }

    @Test(timeout=5000)
    public void expTest()
    {
        // exp(0)=1
        Assert.assertEquals(1D, Calculator.exp(0D), EPS);
    }

    @Test(timeout=5000)
    public void powTest()
    {
        // 0^0=1
        Assert.assertEquals(1D, Calculator.pow(0D, 0), EPS);
    }
}
