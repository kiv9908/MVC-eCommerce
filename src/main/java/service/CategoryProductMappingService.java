package service;

import domain.dao.CategoryProductMappingDAO;
import domain.model.CategoryProductMapping;
import domain.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CategoryProductMappingService {
    
    private static final Logger logger = Logger.getLogger(CategoryProductMappingService.class.getName());
    
    private final CategoryProductMappingDAO mappingDAO;
    
    // 생성자 주입
    public CategoryProductMappingService(CategoryProductMappingDAO mappingDAO) {
        this.mappingDAO = mappingDAO;
    }
    
    /**
     * 특정 카테고리에 속한 상품 매핑 목록 조회
     */
    public List<CategoryProductMapping> getMappingsByCategoryId(int categoryId) {
        return mappingDAO.findByCategoryId(categoryId);
    }
    
    /**
     * 특정 상품이 속한 모든 카테고리 매핑 조회
     */
    public List<CategoryProductMapping> getMappingsByProductCode(String productCode) {
        return mappingDAO.findByProductCode(productCode);
    }
    
    /**
     * 특정 카테고리의 상품 목록 조회
     */
    public List<Product> getProductsByCategoryId(int categoryId) {
        return mappingDAO.findProductsByCategoryId(categoryId);
    }
    
    /**
     * 카테고리에 상품 추가
     */
    public boolean addProductToCategory(int categoryId, String productCode, String userId) {
        // 이미 매핑이 존재하는지 확인
        if (mappingDAO.isProductInCategory(categoryId, productCode)) {
            return true; // 이미 매핑이 존재하면 성공으로 간주
        }
        
        // 새로운 매핑의 순서 결정 (기존 매핑 수 + 1)
        int order = mappingDAO.countProductsInCategory(categoryId) + 1;
        
        CategoryProductMapping mapping = new CategoryProductMapping(categoryId, productCode);
        mapping.setCnOrder(order);
        mapping.setNoRegister(userId);
        
        return mappingDAO.save(mapping);
    }
    
    /**
     * 카테고리에 여러 상품 일괄 추가
     */
    public boolean addProductsToCategory(int categoryId, List<String> productCodes, String userId) {
        List<CategoryProductMapping> mappings = new ArrayList<>();
        
        // 기존 상품 수 확인
        int startOrder = mappingDAO.countProductsInCategory(categoryId) + 1;
        
        for (int i = 0; i < productCodes.size(); i++) {
            String productCode = productCodes.get(i);
            
            // 이미 매핑이 존재하는지 확인 (존재하면 건너뜀)
            if (mappingDAO.isProductInCategory(categoryId, productCode)) {
                continue;
            }
            
            CategoryProductMapping mapping = new CategoryProductMapping(categoryId, productCode);
            mapping.setCnOrder(startOrder + i);
            mapping.setNoRegister(userId);
            
            mappings.add(mapping);
        }
        
        if (mappings.isEmpty()) {
            return true; // 추가할 매핑이 없으면 성공으로 간주
        }
        
        return mappingDAO.saveAll(mappings);
    }
    
    /**
     * 카테고리에서 상품 제거
     */
    public boolean removeProductFromCategory(int categoryId, String productCode) {
        return mappingDAO.removeProductFromCategory(categoryId, productCode);
    }
    
    /**
     * 상품의 모든 카테고리 연결 제거
     */
    public boolean removeAllCategoriesForProduct(String productCode) {
        return mappingDAO.removeAllCategoriesForProduct(productCode);
    }
    
    /**
     * 카테고리의 모든 상품 연결 제거
     */
    public boolean removeAllProductsForCategory(int categoryId) {
        return mappingDAO.removeAllProductsForCategory(categoryId);
    }
    
    /**
     * 카테고리 내 상품 순서 변경
     */
    public boolean updateProductOrderInCategory(int categoryId, String productCode, int newOrder) {
        return mappingDAO.updateOrder(categoryId, productCode, newOrder);
    }
    
    /**
     * 카테고리 내 상품 목록 재정렬
     * @param categoryId 카테고리 ID
     * @param productCodes 정렬된 상품 코드 목록
     */
    public boolean reorderProductsInCategory(int categoryId, List<String> productCodes) {
        boolean success = true;
        
        try {
            for (int i = 0; i < productCodes.size(); i++) {
                success = success && mappingDAO.updateOrder(categoryId, productCodes.get(i), i + 1);
            }
        } catch (Exception e) {
            logger.severe("카테고리 내 상품 목록 재정렬 중 오류 발생: " + e.getMessage());
            success = false;
        }
        
        return success;
    }
    
    /**
     * 상품이 특정 카테고리에 속해 있는지 확인
     */
    public boolean isProductInCategory(int categoryId, String productCode) {
        return mappingDAO.isProductInCategory(categoryId, productCode);
    }
    
    /**
     * 카테고리에 속한 상품 수 조회
     */
    public int getProductCountInCategory(int categoryId) {
        return mappingDAO.countProductsInCategory(categoryId);
    }
}