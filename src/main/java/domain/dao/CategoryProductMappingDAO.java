package domain.dao;

import domain.model.CategoryProductMapping;
import domain.model.Product;

import java.util.List;

public interface CategoryProductMappingDAO {
    
    // 특정 카테고리에 속한 모든 상품 매핑 조회
    List<CategoryProductMapping> findByCategoryId(int nbCategory);
    
    // 특정 상품이 속한 모든 카테고리 매핑 조회
    List<CategoryProductMapping> findByProductCode(String noProduct);
    
    // 카테고리와 상품 매핑 저장
    boolean save(CategoryProductMapping mapping);
    
    // 카테고리와 상품 매핑 일괄 저장
    boolean saveAll(List<CategoryProductMapping> mappings);
    
    // 특정 카테고리에서 상품 제거
    boolean removeProductFromCategory(int nbCategory, String noProduct);
    
    // 특정 상품의 모든 카테고리 연결 제거
    boolean removeAllCategoriesForProduct(String noProduct);
    
    // 특정 카테고리의 모든 상품 연결 제거
    boolean removeAllProductsForCategory(int nbCategory);
    
    // 순서 업데이트
    boolean updateOrder(int nbCategory, String noProduct, int cnOrder);
    
    // 특정 카테고리에 속한 모든 상품 조회 (상품 정보 포함)
    List<Product> findProductsByCategoryId(int nbCategory);
    
    // 특정 카테고리에 속한 상품 개수 조회
    int countProductsInCategory(int nbCategory);
    
    // 특정 카테고리에 상품이 속해 있는지 확인
    boolean isProductInCategory(int nbCategory, String noProduct);
}