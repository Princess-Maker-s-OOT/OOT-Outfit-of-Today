package org.example.ootoutfitoftoday.domain.clothes.exception;

import org.example.ootoutfitoftoday.common.exception.GlobalException;

public class ClothesException extends GlobalException {

    public ClothesException(ClothesErrorCode clothesErrorCode) {
        super(clothesErrorCode);
    }
}
