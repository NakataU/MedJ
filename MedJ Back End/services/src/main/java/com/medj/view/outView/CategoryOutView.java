package com.medj.view.outView;

import com.medj.entities.CategoryTarget;
import lombok.Data;

@Data
public class CategoryOutView {
    private Long id;
    private String label;
    private CategoryTarget target;
}
