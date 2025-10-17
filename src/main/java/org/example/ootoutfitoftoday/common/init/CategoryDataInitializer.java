package org.example.ootoutfitoftoday.common.init;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.category.service.command.CategoryCommandService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryDataInitializer implements CommandLineRunner {

    /**
     * 인터페이스를 참조하는 이유?
     * 1. 느슨한 결합
     *  - CategoryDataInitializer는 인터페이스만 알고, 구현체에는 의존하지 않는다.
     *  - 이렇게 하면 나중에 다른 구현체로 바꾸기 쉽다.
     *  - 테스트 코드에서도 유리, 가짜 서비스(Mock)를 쉽게 주입할 수 있다.
     *
     * 2. Spring DI(의존성 주입)의 기본 원칙
     *  - Spring은 타입 기반으로 주입한다.
     *  - @Component, @Service, @Repository 등
     *  - Bean 등록된 구현체가 CategoryCommandService 타입이면 자동 주입한다.
     *
     *  - 결론 구현체에 직접 의존하면 테스트 유연성, 유지보수성이 떨어진다.
     */
    private final CategoryCommandService categoryCommandService;

    @Override
    public void run(String... args) {
        categoryCommandService.initializeCategories();
    }
}
