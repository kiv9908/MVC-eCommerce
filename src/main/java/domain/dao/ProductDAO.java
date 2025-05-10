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

    // 페이지네이션
    List<ProductDTO> findAllWithPagination(int offset, int limit);

    // 정렬된 상품 목록 조회 (가격순) + 페이지네이션
    List<ProductDTO> findAllOrderByPriceWithPagination(boolean ascending, int offset, int limit);

    // 상품명으로 상품 검색 + 페이지네이션
    List<ProductDTO> findByProductNameWithPagination(String keyword, int offset, int limit);

     // 상품명으로 상품 검색 + 가격 정렬 + 페이지네이션
    List<ProductDTO> findByProductNameOrderByPriceWithPagination(String keyword, boolean ascending, int offset, int limit);

    // 전체 상품 개수
    int countAll();

    // 검색 결과 상품 개수
    int countByProductName(String keyword);

}