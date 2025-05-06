public class Class2
{
    public double add(double a, double b)
    {
        return a+b;
    }

    public double sub(double a, double b)
    {
        return a-b;
    }

    public double mult(double a, double b)
    {
        return a*b;
    }

    public double div(double a, double b)
    {
        return a/b;
    }

    public double min(double a, double b)
    {
        if(a < b) return a;
        return b;
    }

    public double max(double a, double b)
    {
        if(a > b) return a;
        return b;
    }
}