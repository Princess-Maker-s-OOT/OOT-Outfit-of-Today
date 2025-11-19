package org.example.ootoutfitoftoday.domain.clothesImage.exception;

import org.example.ootoutfitoftoday.common.exception.GlobalException;

public class ClothesImageException extends GlobalException {

    public ClothesImageException(ClothesImageErrorCode clothesImageErrorCode) {
        super(clothesImageErrorCode);
    }
}