module SecurityServiceModule {
    exports com.udacity.security.service;
    requires miglayout.swing;
    requires com.google.common;
    requires com.google.gson;
    requires java.prefs;
    requires java.desktop;
    requires ImageServiceModule;
    opens com.udacity.security.data to com.google.gson;
}

