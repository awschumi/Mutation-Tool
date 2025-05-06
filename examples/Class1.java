public class Class1
{
    public int f1(int a, int b)
    {
        int res = 2 * a;
        res += b - res;
        return res;
    }

    public void f2(String s)
    {
        if(s != null) System.out.println(s.toCharArray()[0]);
        else System.out.println("null");
    }
}