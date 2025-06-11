package Calculator;

public class Calculator
{
    public static final double EPS = 0.001;

    /*
     * Computes the addition of a and b
     */
    public static double add(double a, double b)
    {
        return a
                +
                b;
    }

    /*
     * Computes the subtraction of a and b
     */
    public static double sub(double a, double b)
    {
        return a-b;
    }

    /*
     * Computes the multiplication of a and b
     */
    public static double mult(double a, double b)
    {
        return a*b;
    }

    /*
     * Computes the division of a and b
     */
    public static double div(double a, double b) throws Exception
    {
        if(b == 0) throw new Exception("You can't divide by 0 :(");
        return a/b;
    }

    /*
     * Computes a modulo b
     */
    public static int mod(int a, int b) throws Exception
    {
        if(b == 0) throw new Exception("You can't divide by 0 :(");
        return a%b;
    }

    /*
     * Computes the absolute value of a
     */
    public static double abs(double a)
    {
        if(a >= 0) return a;
        return -a;
    }

    /*
     * Computes the maximum between a and b
     */
    public static double max(double a, double b)
    {
        if(a >= b) return a;
        return b;
    }

    /*
     * Computes the minimum between a and b
     */
    public static double min(double a, double b)
    {
        if(a <= b) return a;
        return b;
    }

    /*
     * Computes the square root of a with an approximation of ...
     */
    public static double sqrt(double a) throws Exception
    {
        if(a < 0) throw new Exception("Can't compute a negative root :(");
        if(a == 0) return 0;

        double xn = 2; // It could be any positive real number but 0

        for(int i = 0; i < 25; i++)
        {
            xn = 0.5 * (xn + (a/xn)); // 0.5 * ...
        }

        return xn;
    }

    /*
     * Computes the exponential of a with an approximation of ...
     * Reminder: exp(x) = 1 + x + x^2/2! + x^3/3! + x^4/4! + ...
     */
    public static double exp(double a)
    {
        double res = 1;
        double numerator = a;
        double denominator = 1;

        for(int i = 1; i < 10; i++)
        {
            res += numerator/denominator;
            numerator *= a;
            denominator *= i+1;
        }

        return res;
    }

    /*
     * Computes the number x pow n, where x is a real number and n any positive or negative
     * Recursive function
     * Particular case: 0^0 := 1
     */
    public static double pow(double x, int n)
    {
        if(n == 0) return 1;
        if(n > 0) return x * pow(x,n-1);
        // Here is a comment
        return 1 / (pow(x, -n));
    }
}
