package org.monarchinitiative.phenopacket2prompt.config;

public class Context {
    private static final Context INSTANCE = new Context();
    private boolean fullTranslations;
    private boolean onlyPatient;

    private Context() {} // Private constructor for singleton

    public static Context getInstance() {
        return INSTANCE;
    }

    public void setFullTranslations(boolean value) {
        this.fullTranslations = value;
    }
    public void setOnlyPatient(boolean value) {
        this.onlyPatient = value;
    }

    public boolean isFullTranslations() {
        return fullTranslations;
    }
    public boolean isOnlyPatient() {
        return onlyPatient;
    }

}
