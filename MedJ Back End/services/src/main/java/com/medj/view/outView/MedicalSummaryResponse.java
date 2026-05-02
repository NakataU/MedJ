package com.medj.view.outView;

public class MedicalSummaryResponse {

    private String qrBase64;
    private String pdfBase64;

    public MedicalSummaryResponse(String qrBase64, String pdfBase64) {
        this.qrBase64 = qrBase64;
        this.pdfBase64 = pdfBase64;
    }

    public String getQrBase64() { return qrBase64; }
    public String getPdfBase64() { return pdfBase64; }
}
