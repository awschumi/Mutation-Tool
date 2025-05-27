package calculatorTest;

import Calculator.Calculator;
import org.junit.Test;
import org.junit.Assert;

/*
 * When manipulating floating numbers, it may happen that approximations errors occur
 * (e.g. 0.3+0.4=0.700000000001), so we will check if the difference between the real and
 * computed number is smaller than an epsilon (e.g. abs(0.7-0.700000000001) < 0.001)
 */
public class TestCalculatorStrong
{
    public final static double EPS = 0.000001;

    /*
     * Checks if a = b for EPS
     */
    public static boolean equalsToEpsilson(double a, double b)
    {
        if(Math.abs(a-b) <= EPS) return true;
        return false;
    }

    @Test(timeout=5000)
    public void addTest()
    {
        // 0+0=0
        Assert.assertEquals(0D, Calculator.add(0D,0D), EPS);

        // 1+2=3
        Assert.assertEquals(3D, Calculator.add(1D,2D), EPS);

        // 4+(-4)=0
        Assert.assertEquals(0D, Calculator.add(4D,-4D), EPS);

        // 0.3+0.3=0.6
        boolean isEquals = equalsToEpsilson(0.6D, Calculator.add(0.3D,0.3D));
        Assert.assertTrue(isEquals);
    }

    @Test(timeout=5000)
    public void subTest()
    {
        // 0-0=0
        Assert.assertEquals(0D, Calculator.sub(0D,0D), EPS);

        // 1-2=-1
        Assert.assertEquals(-1D, Calculator.sub(1D,2D), EPS);

        // -5-(-5)=0
        Assert.assertEquals(0D, Calculator.sub(-5D,-5D), EPS);

        // 3.14-1.23=1.91
        boolean isEquals = equalsToEpsilson(1.91D, Calculator.sub(3.14D,1.23D));
        Assert.assertTrue(isEquals);
    }

    @Test(timeout=5000)
    public void multTest()
    {
        // 0*0=0
        Assert.assertEquals(0D, Calculator.mult(0D,0D), EPS);

        // 6*1=6
        Assert.assertEquals(6D, Calculator.mult(6D,1D), EPS);

        // -5*(-2)=10
        Assert.assertEquals(10D, Calculator.mult(-5D,-2D), EPS);

        // 2*0.5=1
        boolean isEquals = equalsToEpsilson(1D, Calculator.mult(2D,0.5D));
        Assert.assertTrue(isEquals);
    }

    @Test(timeout=5000)
    public void divTest()
    {
        // 0/1=0
        try { Assert.assertEquals(0D, Calculator.div(0D,1D), EPS); }
        catch (Exception e){}

        //1/0=MUST THROW EXCEPTION
        try { Calculator.div(1D,0D); }
        catch(Exception e)
        {
            Assert.assertEquals("You can't divide by 0 :(", e.getMessage());
        }

        // 0.5/0.5=1
        try { Assert.assertEquals(1D, Calculator.div(0.5D,0.5D), EPS); }
        catch (Exception e){}

        // 3/-0.5=-6
        try { Assert.assertEquals(-6D, Calculator.div(3D,-0.5D), EPS); }
        catch (Exception e){}
    }

    @Test(timeout=5000)
    public void modTest()
    {
        // 0%1=0
        try{ Assert.assertEquals(0, Calculator.mod(0,1)); }
        catch (Exception e){}

        //4%0=MUST THROW EXCEPTION
        try{ Calculator.mod(4,0); }
        catch(Exception e)
        {
            Assert.assertEquals("You can't divide by 0 :(", e.getMessage());
        }

        //4%2=0
        try{ Assert.assertEquals(0, Calculator.mod(4,2)); }
        catch (Exception e){}

        //-3%2=-1
        try{ Assert.assertEquals(-1, Calculator.mod(-3,2)); }
        catch (Exception e){}
    }

    @Test(timeout=5000)
    public void absTest()
    {
        // abs(0)=0
        Assert.assertEquals(0D, Calculator.abs(0D), EPS);

        // abs(0.0001)=0.0001
        Assert.assertEquals(0.0001D, Calculator.abs(0.0001D), EPS);

        // abs(-0.0002)=0.0002
        Assert.assertEquals(0.0002D, Calculator.abs(-0.0002D), EPS);

        // abs(-0.1-0.2)=0.3
        boolean isEquals = equalsToEpsilson(0.3D, Calculator.abs(-0.1-0.2));
        Assert.assertTrue(isEquals);
    }

    @Test(timeout=5000)
    public void maxTest()
    {
        // max(0,0)=0
        Assert.assertEquals(0D, Calculator.max(0D,0D), EPS);

        // max(-3.13,6)=6
        Assert.assertEquals(6D, Calculator.max(-3.14D,6D), EPS);

        // max(0.3+0.4,0.3+0.4)=0.7
        Assert.assertEquals(0.7D, Calculator.max(0.3D+0.4D,0.3D+0.4D), EPS);
    }

    @Test(timeout=5000)
    public void minTest()
    {
        // min(0,0)=0
        Assert.assertEquals(0D, Calculator.min(0D,0D), EPS);

        // min(-3.13,6)=-3.13
        Assert.assertEquals(-3.13D, Calculator.min(-3.13D,6D), EPS);

        // min(0.3-0.4,0.3-0.4)=-0.1
        boolean isEquals = equalsToEpsilson(-0.1D, Calculator.min(0.3D-0.4D,0.3D-0.4D));
        Assert.assertTrue(isEquals);
    }

    @Test(timeout=5000)
    public void sqrtTest()
    {
        // sqrt(0)=0
        try { Assert.assertEquals(0D, Calculator.sqrt(0D), EPS); }
        catch (Exception e){}

        // sqrt(-1)=MUST THROW EXCEPTION
        try { Calculator.sqrt(-1D); }
        catch (Exception e)
        {
            Assert.assertEquals("Can't compute a negative root :(", e.getMessage());
        }

        // sqrt(2) = 1.414213562373095...
        try
        {
            boolean isEquals = equalsToEpsilson(1.414213562373095D, Calculator.sqrt(2));
            Assert.assertTrue(isEquals);
        } catch (Exception e) {}
    }

    @Test(timeout=5000)
    public void expTest()
    {
        // exp(0)=1
        Assert.assertEquals(1D, Calculator.exp(0D), EPS);

        // exp(1)=2.718281828459045
        boolean isEquals = equalsToEpsilson(2.718281828459045, Calculator.exp(1));
        Assert.assertTrue(isEquals);
    }

    @Test(timeout=5000)
    public void powTest()
    {
        // 0^0=1
        Assert.assertEquals(1D, Calculator.pow(0D, 0), EPS);

        // 2^3=8
        Assert.assertEquals(8D, Calculator.pow(2D, 3), EPS);

        // 2^(-4)=0.0625
        boolean isEquals = equalsToEpsilson(0.0625, Calculator.pow(2D, -4));
        Assert.assertTrue(isEquals);

        //1.5^2=2.25
        isEquals = equalsToEpsilson(2.25, Calculator.pow(1.5D, 2));
        Assert.assertTrue(isEquals);
    }
}
