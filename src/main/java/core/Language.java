package core;

public enum Language
{
    JAVA("Java"),
    PYTHON("Python"),
    CPP("C++"),
    C("C"),

    UNDEFINED("UNDEFINED"), // When the language is not found
    ALL("ALL"); // To accept every language

    //private String lang;
    public final String lang;

    Language(String lang) {
        this.lang = lang;
    }

    @Override
    public String toString() {
        return lang;
    }
}
