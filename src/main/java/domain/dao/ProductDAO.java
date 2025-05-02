package domain.dao;

import domain.model.Product;

import java.util.List;

public interface ProductDAO {
    // 상품 코드로 상품 조회
    Product findByProductCode(String productCode);

    // 상품명으로 상품 검색
    List<Product> findByProductName(String productName);

    // 모든 상품 조회
    List<Product> findAll();

    // 정렬된 상품 목록 조회 (가격순)
    List<Product> findAllOrderByPrice(boolean ascending);

    // 상품 저장 (등록)
    void save(Product product);

    // 상품 수정
    void modify(Product product);

    // 상품 삭제
    boolean delete(String productCode);

    // 재고 수정
    boolean modifyStock(String productCode, int stock);

    // 판매 상태 관리 (판매 중지, 품절 처리)
    boolean modifySaleStatus(String productCode, String startDate, String endDate);

}