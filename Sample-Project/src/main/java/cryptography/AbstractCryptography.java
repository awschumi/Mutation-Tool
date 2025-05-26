package cryptography;

/**
 * Abstract class providing info about the cryptographic tool
 */
public abstract class AbstractCryptography
{
    /// The name of the tool, e.g. "Caesar" or "RSA"
    private String name;

    /// The description of the tool
    private String description;

    public void setName(String name)
    {
        if(name != null)
            if(!name.isEmpty())
                this.name = name;
    }

    public void setDescription(String description)
    {
        if(description != null)
            if(!description.isEmpty())
                this.description = description;
    }

    public String getName()
    {
        return this.name;
    }

    public String getDescription()
    {
        return this.description;
    }
}
