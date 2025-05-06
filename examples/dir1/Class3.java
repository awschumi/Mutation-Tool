public class Class3
{
    private String name;
    private int id;

    public Class3()
    {
        setName("default");
        setId("0");
    }

    public Class3(String name, int id)
    {
        setName(name);
        setId(id);
    }

    /** SETTERS **/

    public void setName(String name)
    {
        this.name = name;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    /** GETTERS **/

    public String getName()
    {
        return name;
    }

    public int getId()
    {
        return id;
    }
}