package testing;

import core.Language;
import storage.FileInfo;

public abstract class AbstractTesting
{
    protected Language language;

    private void setLanguage(Language lang)
    {
        this.language = lang;
    }

    public Language getLanguage() {
        return language;
    }

    public abstract void test(FileInfo f);
}
