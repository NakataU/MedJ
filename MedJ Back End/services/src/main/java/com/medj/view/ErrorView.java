package com.medj.view;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorView {

    private LocalDateTime time;
    private String message;
    private String exMessage;
    private String logId;
    private String path;
    private String method;

}