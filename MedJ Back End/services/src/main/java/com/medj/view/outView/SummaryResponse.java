package com.medj.view.outView;

import lombok.Data;

import java.util.List;

@Data
public class SummaryResponse {
    private String summary;
    private List<DocumentListOutView> usedDocuments;
}
