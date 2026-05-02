package com.medj.view.outView;

import lombok.Data;

@Data
public class DocumentOutView {

    private Long id;
    private String fileName;
    private String contentType;
    private String path;
    private String checksum;
    private String size;
    private Long uploadedByUserId;
    private String content;
}
