package org.example.ootoutfitoftoday.domain.recommendation.exception;

import org.example.ootoutfitoftoday.common.exception.GlobalException;

public class RecommendationException extends GlobalException {

    public RecommendationException(RecommendationErrorCode errorCode) {
        super(errorCode);
    }
}