module marksheet.system {
    requires java.desktop;
    requires org.apache.pdfbox;

    exports com.marksheet.model;
    exports com.marksheet.service;
    exports com.marksheet.pdf;
    exports com.marksheet.ui;
}
