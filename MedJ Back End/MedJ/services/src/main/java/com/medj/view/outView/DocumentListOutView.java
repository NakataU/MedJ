package com.medj.view.outView;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentListOutView {
    private Long id;
    private String fileName;
    private String size;
    private LocalDateTime createdOn;

}
