module SecurityServiceModule {
    exports com.udacity.security.service;
    requires miglayout.swing;
    requires java.desktop;
    requires com.google.common;
    requires com.google.gson;
    requires java.prefs;
    requires ImageServiceModule;
}