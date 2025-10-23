package org.example.ootoutfitoftoday.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NewUsers {

    private final int daily;
    private final int weekly;
    private final int monthly;
}
