package com.jladder.openapi30;

public class Info {
    private String title="接口列表";
    private String description="由接口平台导出";
//    private String termsOfService;
    private Contact contact=new Contact();
    private License license=new License();
    private String version="v1.0";

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

//    public String getTermsOfService() {
//        return termsOfService;
//    }
//
//    public void setTermsOfService(String termsOfService) {
//        this.termsOfService = termsOfService;
//    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public License getLicense() {
        return license;
    }

    public void setLicense(License license) {
        this.license = license;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }


}
