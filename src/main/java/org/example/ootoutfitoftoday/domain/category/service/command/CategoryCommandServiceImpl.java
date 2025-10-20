package org.example.ootoutfitoftoday.domain.category.service.command;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.category.dto.request.CategoryRequest;
import org.example.ootoutfitoftoday.domain.category.dto.response.CategoryResponse;
import org.example.ootoutfitoftoday.domain.category.entity.Category;
import org.example.ootoutfitoftoday.domain.category.exception.CategoryErrorCode;
import org.example.ootoutfitoftoday.domain.category.exception.CategoryException;
import org.example.ootoutfitoftoday.domain.category.repository.CategoryRepository;
import org.example.ootoutfitoftoday.domain.clothes.service.command.ClothesCommandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryCommandServiceImpl implements CategoryCommandService {

    private final CategoryRepository categoryRepository;
    private final ClothesCommandService clothesCommandService;

    // 초기 세팅 카테고리 데이터 삽입
    @Override
    public void initializeCategories() {

        // 만약 DB에 데이터가 존재 한다면 초기화를 안함.
        long rootCategoryExists = categoryRepository.countByParentIsNull();

        if (rootCategoryExists > 0) {

            return;
        }

        // 최상위 카테고리
        Category men = Category.create("남성", null);
        Category women = Category.create("여성", null);
        Category kids = Category.create("아동", null);

        // 남성 카테고리
        Category menOuter = Category.create("아우터", men);
        Category menTop = Category.create("상의", men);
        Category menBottom = Category.create("하의", men);
        Category menShoes = Category.create("신발", men);
        Category menBag = Category.create("가방", men);
        Category menAccessory = Category.create("액세서리", men);
        Category menUnderwear = Category.create("이너웨어", men);
        Category menLife = Category.create("라이프웨어", men);

        // 남성 - 아우터
        Category menJacket = Category.create("자켓", menOuter);
        Category menCoat = Category.create("코트", menOuter);
        Category menPadding = Category.create("패딩", menOuter);
        Category menCardigan = Category.create("가디건", menOuter);
        Category menBlouson = Category.create("블루종", menOuter);
        Category menFleece = Category.create("후리스", menOuter);
        Category menHoodZipUp = Category.create("후드집업", menOuter);

        // 남성 - 상의
        Category menTee = Category.create("반팔 티셔츠", menTop);
        Category menLongTee = Category.create("긴팔 티셔츠", menTop);
        Category menShirt = Category.create("셔츠", menTop);
        Category menSweatshirt = Category.create("맨투맨", menTop);
        Category menKnit = Category.create("니트", menTop);
        Category menHood = Category.create("후드티", menTop);
        Category menSleeveless = Category.create("민소매", menTop);

        // 남성 - 하의
        Category menJeans = Category.create("청바지", menBottom);
        Category menSlacks = Category.create("슬랙스", menBottom);
        Category menJogger = Category.create("조거팬츠", menBottom);
        Category menShorts = Category.create("반바지", menBottom);
        Category menTraining = Category.create("트레이닝팬츠", menBottom);

        // 남성 - 신발
        Category menSneakers = Category.create("스니커즈", menShoes);
        Category menBoots = Category.create("부츠", menShoes);
        Category menSandals = Category.create("샌들", menShoes);
        Category menLoafers = Category.create("로퍼", menShoes);
        Category menFormal = Category.create("구두", menShoes);

        // 남성 - 가방
        Category menBackpack = Category.create("백팩", menBag);
        Category menCrossBag = Category.create("크로스백", menBag);
        Category menClutch = Category.create("클러치백", menBag);
        Category menTote = Category.create("토트백", menBag);

        // 남성 - 액세서리
        Category menCap = Category.create("모자", menAccessory);
        Category menBelt = Category.create("벨트", menAccessory);
        Category menWatch = Category.create("시계", menAccessory);
        Category menBracelet = Category.create("팔찌", menAccessory);
        Category menSunglasses = Category.create("선글라스", menAccessory);
        Category menScarf = Category.create("머플러/스카프", menAccessory);

        // 남성 - 이너웨어
        Category menUnderwearTop = Category.create("러닝/탱크탑", menUnderwear);
        Category menUnderwearBottom = Category.create("드로즈/트렁크", menUnderwear);

        // 남성 - 라이프웨어
        Category menHomeWear = Category.create("홈웨어", menLife);
        Category menSports = Category.create("스포츠웨어", menLife);

        // 여성 카테고리
        Category womenOuter = Category.create("아우터", women);
        Category womenTop = Category.create("상의", women);
        Category womenBottom = Category.create("하의", women);
        Category womenShoes = Category.create("신발", women);
        Category womenBag = Category.create("가방", women);
        Category womenAccessory = Category.create("액세서리", women);
        Category womenDress = Category.create("원피스", women);
        Category womenUnderwear = Category.create("이너웨어", women);

        // 여성 - 아우터
        Category womenCoat = Category.create("코트", womenOuter);
        Category womenJacket = Category.create("재킷", womenOuter);
        Category womenCardigan = Category.create("가디건", womenOuter);
        Category womenBlouson = Category.create("블루종", womenOuter);
        Category womenPadding = Category.create("패딩", womenOuter);

        // 여성 - 상의
        Category womenBlouse = Category.create("블라우스", womenTop);
        Category womenKnit = Category.create("니트", womenTop);
        Category womenTee = Category.create("티셔츠", womenTop);
        Category womenHood = Category.create("후드티", womenTop);

        // 여성 - 하의
        Category womenSkirt = Category.create("스커트", womenBottom);
        Category womenPants = Category.create("팬츠", womenBottom);
        Category womenShorts = Category.create("반바지", womenBottom);
        Category womenSlacks = Category.create("슬랙스", womenBottom);

        // 여성 - 신발
        Category womenFlat = Category.create("플랫슈즈", womenShoes);
        Category womenHeels = Category.create("힐", womenShoes);
        Category womenBoots = Category.create("부츠", womenShoes);
        Category womenSneakers = Category.create("스니커즈", womenShoes);
        Category womenLoafers = Category.create("로퍼", womenShoes);

        // 여성 - 가방
        Category womenCross = Category.create("크로스백", womenBag);
        Category womenShoulder = Category.create("숄더백", womenBag);
        Category womenTote = Category.create("토트백", womenBag);
        Category womenMini = Category.create("미니백", womenBag);

        // 여성 - 액세서리
        Category womenNecklace = Category.create("목걸이", womenAccessory);
        Category womenEarrings = Category.create("귀걸이", womenAccessory);
        Category womenBracelet = Category.create("팔찌", womenAccessory);
        Category womenRing = Category.create("반지", womenAccessory);
        Category womenHair = Category.create("헤어악세서리", womenAccessory);
        Category womenScarf = Category.create("스카프", womenAccessory);

        // 여성 - 원피스
        Category womenCasualDress = Category.create("캐주얼원피스", womenDress);
        Category womenLongDress = Category.create("롱원피스", womenDress);
        Category womenMiniDress = Category.create("미니원피스", womenDress);

        // 여성 - 이너웨어
        Category womenBra = Category.create("브라탑", womenUnderwear);
        Category womenPanties = Category.create("팬티", womenUnderwear);
        Category womenStocking = Category.create("스타킹", womenUnderwear);

        // 아동 카테고리
        Category kidsOuter = Category.create("아우터", kids);
        Category kidsTop = Category.create("상의", kids);
        Category kidsBottom = Category.create("하의", kids);
        Category kidsShoes = Category.create("신발", kids);
        Category kidsAccessory = Category.create("액세서리", kids);

        Category kidsTee = Category.create("티셔츠", kidsTop);
        Category kidsPants = Category.create("바지", kidsBottom);
        Category kidsSneakers = Category.create("스니커즈", kidsShoes);

        /**
         * 저장
         *  - saveAll + List.of를 사용하는 이유는 카테고리 객체들을 하나의 리스트로 묶어
         *  - 코드 가독성을 높이고 하나의 트랜잭션 안에서 일괄 저장할 수 있도록 하기 위함
         */
        categoryRepository.saveAll(
                List.of(
                        men, women, kids,

                        menOuter, menTop, menBottom, menShoes, menBag, menAccessory, menUnderwear, menLife,
                        menJacket, menCoat, menPadding, menCardigan, menBlouson, menFleece, menHoodZipUp,
                        menTee, menLongTee, menShirt, menSweatshirt, menKnit, menHood, menSleeveless,
                        menJeans, menSlacks, menJogger, menShorts, menTraining,
                        menSneakers, menBoots, menSandals, menLoafers, menFormal,
                        menBackpack, menCrossBag, menClutch, menTote,
                        menCap, menBelt, menWatch, menBracelet, menSunglasses, menScarf,
                        menUnderwearTop, menUnderwearBottom,
                        menHomeWear, menSports,

                        womenOuter, womenTop, womenBottom, womenShoes, womenBag, womenAccessory, womenDress, womenUnderwear,
                        womenCoat, womenJacket, womenCardigan, womenBlouson, womenPadding,
                        womenBlouse, womenKnit, womenTee, womenHood,
                        womenSkirt, womenPants, womenShorts, womenSlacks,
                        womenFlat, womenHeels, womenBoots, womenSneakers, womenLoafers,
                        womenCross, womenShoulder, womenTote, womenMini,
                        womenNecklace, womenEarrings, womenBracelet, womenRing, womenHair, womenScarf,
                        womenCasualDress, womenLongDress, womenMiniDress,
                        womenBra, womenPanties, womenStocking,

                        kidsOuter, kidsTop, kidsBottom, kidsShoes, kidsAccessory,
                        kidsTee, kidsPants, kidsSneakers
                )
        );
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {

        Category parent = null;

        /**
         *  상위 카테고리를 입력했다면 존재 여부를 검증
         *  - null 뿐만 아니라 0도 조건으로 건 이유는 아이디의 값이 1부터 시작하기 때문이다.
         *  - 추가로 사용자가 아이디의 값을 0 이하로 입력시 아이디의 값이 null로 처리되어 최상위 카테고리로 인식한다.
         */
        if (categoryRequest.getParentId() != null && categoryRequest.getParentId() > 0) {
            parent = categoryRepository.findById(categoryRequest.getParentId())
                    .orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND)
                    );
        }

        Category category = Category.create(categoryRequest.getName(), parent);
        categoryRepository.save(category);

        return CategoryResponse.from(category);
    }

    // Todo: BFS 탐색용 큐를 활용하여 구현할 수도 있다. 이건 추후에 공부하고 적용하기!
    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest) {

        Category category = categoryRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND)
                );

        Category parent = null;

        if (categoryRequest.getParentId() != null && categoryRequest.getParentId() > 0) {

            parent = categoryRepository.findByIdAndIsDeletedFalse(categoryRequest.getParentId())
                    .orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND)
                    );

            /**
             * 순환 참조 방지 코드
             * 만약 parent가 category 자신의 하위카테고리거나 본인이라면, 자식을 상위 또는 자신을 상위 카테고리로 설정하는 것이다.
             * 그래서 순환 참조가 발생할 수 있다.
             * if(Objects.equals(current.getId(), category.getId())
             * 관리자가 입력한 카테고리 아이디와 url에 입력된 아이디의 값을 비교 같다면 예외 처리
             * current = current.getParent()로 상위 카테고리의 상위 카테고리까지 계속 올라가며 확인
             */
            Category current = parent;

            while (current != null) {

                if(Objects.equals(current.getId(), category.getId())) {
                    throw new CategoryException(CategoryErrorCode.CANNOT_SET_SELF_AS_PARENT);
                }

                current = current.getParent();
            }
        }

        category.update(categoryRequest.getName(), parent);

        return CategoryResponse.from(category);
    }

    @Override
    public void deleteCategory(Long id) {

        categoryRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND)
                );

        List<Long> result = new ArrayList<>();
        result.add(id);

        List<Long> currentCategory = List.of(id);

        while (!currentCategory.isEmpty()) {
            List<Long> childCategory = categoryRepository.findIdsByParentIds(currentCategory);

            if (childCategory.isEmpty()) {
                break;
            }

            result.addAll(childCategory);
            currentCategory = childCategory;
        }

        clothesCommandService.clearCategoryFromClothes(result);

        categoryRepository.softDeleteCategories(result);
    }
}
