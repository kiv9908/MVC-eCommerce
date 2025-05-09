package domain.dao;

import domain.dto.ProductDTO;

import java.util.List;

public interface ProductDAO {
    // 상품 코드로 상품 조회
    ProductDTO findByProductCode(String productCode);

    // 상품명으로 상품 검색
    List<ProductDTO> findByProductName(String productName);

    // 모든 상품 조회
    List<ProductDTO> findAll();

    // 정렬된 상품 목록 조회 (가격순)
    List<ProductDTO> findAllOrderByPrice(boolean ascending);

    // 상품 저장 (등록)
    void save(ProductDTO productDTO);

    // 상품 수정
    void modify(ProductDTO productDTO);

    // 상품 삭제
    boolean delete(String productCode);

    // 재고 수정
    boolean modifyStock(String productCode, int stock);

    // 판매 상태 관리 (판매 기간에 따른 상태 계산)
    boolean modifySaleStatus(String productCode, String startDate, String endDate);

}