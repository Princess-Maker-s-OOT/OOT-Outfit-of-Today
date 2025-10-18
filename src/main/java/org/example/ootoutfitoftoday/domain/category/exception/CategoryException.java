package org.example.ootoutfitoftoday.domain.category.exception;

import org.example.ootoutfitoftoday.common.exception.GlobalException;

public class CategoryException extends GlobalException {

    public CategoryException(CategoryErrorCode categoryErrorCode) {
        super(categoryErrorCode);
    }

    public CategoryException(CategoryErrorCode categoryErrorCode, CategorySuccessCode categorySuccessCode) {
        super(categoryErrorCode, categorySuccessCode);
    }
}
