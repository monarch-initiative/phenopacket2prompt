package org.monarchinitiative.phenopacket2prompt.config;

public class Context {
    private static final Context INSTANCE = new Context();
    private boolean fullTranslations;
    private boolean onlyPatient;
    private boolean jsonOutput;

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
    public void setJsonOutput(boolean value) {
        this.jsonOutput = value;
    }

    public boolean isFullTranslations() {
        return fullTranslations;
    }
    public boolean isOnlyPatient() {
        return onlyPatient;
    }
    public boolean isJsonOutput() {
        return jsonOutput;
    }

}
