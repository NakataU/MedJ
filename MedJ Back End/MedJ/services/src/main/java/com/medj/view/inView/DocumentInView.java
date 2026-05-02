package com.medj.view.inView;

import lombok.Data;

@Data
public class DocumentInView {

    private String fileName;
    private String contentType;
    private String path;
    private String checksum;
    private Long size;
    private Long uploadedByUserId;
    private Long categoryId;

}
