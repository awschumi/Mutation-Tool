#include <iostream>

class Class4
{
    /* FIELDS */

    public:
        int i1 = 0;

    private:
        char c1 = 'a';

    /* METHODS */

    public:
    void setChar(char c)
    {
        c1 = c;
    }

    char getChar()
    {
        return c1;
    }

    double random(double rnd)
    {
        double tmp = rnd
            * 0.002
            + 1.234;
        return 1 + rnd;
    }

    void strange()
    {
        int i2 = 3;
        {
            i2 + 1;
            {
                i2 + 4;
            }
        }

        for(int i = 0; i < i2; i++)
        {
            1+1;
        }
    }
};

int main(int argc, char * argv[])
{
    Class1 obj;
    obj.setChar('b');

    if(1 > (3 +2))
        std::cout << "1 > (3 +2)" << std::endl;
    else
        std::cout << "1 < (3 +2)" << std::endl;

    return 0;
}