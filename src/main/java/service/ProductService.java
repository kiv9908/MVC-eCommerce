package service;

import domain.dao.ProductDAO;
import domain.dto.ProductDTO;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Slf4j
public class ProductService {
    private final ProductDAO productDAO;

    public ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    /**
     * 모든 상품 조회 (Entity 반환)
     */
    public List<ProductDTO> findAll() {
        return productDAO.findAll();
    }

    /**
     * 상품 목록 조회 (DTO 변환)
     */
    public List<ProductDTO> getAllProductDTOs() {
        return productDAO.findAll();
    }

    /**
     * 가격순으로 정렬된 상품 목록 조회 (DTO 변환)
     */
    public List<ProductDTO> getAllProductDTOsOrderByPrice(boolean ascending) {
        return productDAO.findAllOrderByPrice(ascending);
    }

    /**
     * 상품명으로 상품 검색
     */
    public List<ProductDTO> searchProductDTOs(String keyword) {
        return productDAO.findByProductName(keyword);
    }

    /**
     * 상품코드로 상품 조회 (DTO 변환)
     */
    public ProductDTO getProductDTOByCode(String productCode) {
        return productDAO.findByProductCode(productCode);
    }

    /**
     * 상품 등록
     */
    public boolean createProduct(ProductDTO productDTO) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

            // 시작일이 비어 있으면 오늘 날짜로 설정
            if (productDTO.getStartDate() == null || productDTO.getStartDate().trim().isEmpty()) {
                String today = LocalDate.now().format(formatter);
                productDTO.setStartDate(today);
            }

            // 종료일이 비어 있으면 시작일로부터 1개월 후로 설정
            if (productDTO.getEndDate() == null || productDTO.getEndDate().trim().isEmpty()) {
                LocalDate startDate = LocalDate.parse(productDTO.getStartDate(), formatter);
                LocalDate endDate = startDate.plusMonths(1);
                productDTO.setEndDate(endDate.format(formatter));
            }

            if(productDTO.getStock() == null) {
                productDTO.setStock(0);
            }

            if(productDTO.getDeliveryFee() == null) {
                productDTO.setDeliveryFee(0);
            }

            productDAO.save(productDTO);
            return true;
        } catch (Exception e) {
            log.error("상품 생성 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 상품 수정
     */
    public boolean updateProduct(ProductDTO productDTO) {
        try {
            productDAO.modify(productDTO);
            return true;
        } catch (Exception e) {
            log.error("상품 수정 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 상품 삭제
     */
    public boolean deleteProduct(String productCode) {
        try {
            return productDAO.delete(productCode);
        } catch (Exception e) {
            log.error("상품 삭제 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }


    /**
     * 상품을 품절 상태로 변경 (재고를 0으로 설정)
     */
    public boolean markAsSoldOut(String productCode) {
        try {
            log.info("품절 처리 시작: productCode={}", productCode);
            
            // 현재 상품 정보 조회
            ProductDTO productDTO = productDAO.findByProductCode(productCode);
            if (productDTO == null) {
                log.error("품절 처리 실패: 상품을 찾을 수 없음 (productCode={})", productCode);
                return false;
            }
            
            log.info("품절 처리 중: 상품명={}, 현재 재고={}", productDTO.getProductName(), productDTO.getStock());
            
            // 재고를 0으로 설정
            boolean result = productDAO.modifyStock(productCode, 0);
            
            if (result) {
                log.info("품절 처리 성공: productCode={}", productCode);
            } else {
                log.error("품절 처리 실패: 재고 업데이트 실패 (productCode={})", productCode);
            }
            
            return result;
        } catch (Exception e) {
            log.error("상품 품절 처리 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 상품을 판매 중지 상태로 변경 (판매 종료일을 현재 날짜로 설정)
     */
    public boolean stopSelling(String productCode) {
        try {
            // 현재 날짜-1를 종료일로 설정
            String currentDate = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            // 현재 상품 정보 조회
            ProductDTO productDTO = productDAO.findByProductCode(productCode);
            if (productDTO == null) {
                return false;
            }
            
            String startDate = productDTO.getStartDate(); // 시작일은 그대로 유지
            
            // 판매 종료일을 현재 날짜로 설정하여 판매 중지 상태로 만듦
            return productDAO.modifySaleStatus(productCode, startDate, currentDate);
        } catch (Exception e) {
            log.error("상품 판매 중지 처리 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 상품을 판매중 상태로 변경 (재고 > 0 및 판매 기간 설정)
     */
    public boolean startSelling(String productCode) {
        try {
            boolean success = true;
            
            // 현재 상품 정보 조회
            ProductDTO productDTO = productDAO.findByProductCode(productCode);
            if (productDTO == null) {
                return false;
            }
            
            // 1. 재고가 0이면 1로 설정
            if (productDTO.getStock() == null || productDTO.getStock() <= 0) {
                success = productDAO.modifyStock(productCode, 1);
            }
            
            if (!success) {
                return false;
            }
            
            // 2. 판매 기간 설정 (현재 날짜부터 1개월)
            LocalDate today = LocalDate.now();
            String startDate = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String endDate = today.plusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            // 판매 기간을 업데이트하여 판매중 상태로 만듦
            return productDAO.modifySaleStatus(productCode, startDate, endDate);
        } catch (Exception e) {
            log.error("상품 판매 시작 처리 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 페이지네이션 처리된 상품 목록 조회
     */
    public List<ProductDTO> getProductDTOsWithPagination(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return productDAO.findAllWithPagination(offset, pageSize);
    }

    /**
     * 가격순으로 정렬된 상품 목록 조회 (페이지네이션)
     */
    public List<ProductDTO> getProductDTOsOrderByPriceWithPagination(boolean ascending, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return productDAO.findAllOrderByPriceWithPagination(ascending, offset, pageSize);
    }

    /**
     * 상품명으로 상품 검색 (페이지네이션)
     */
    public List<ProductDTO> searchProductDTOsWithPagination(String keyword, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return productDAO.findByProductNameWithPagination(keyword, offset, pageSize);
    }

    /**
     * 전체 상품 개수 조회
     */
    public int getTotalProductCount() {
        return productDAO.countAll();
    }

    /**
     * 검색 결과 상품 개수 조회
     */
    public int getSearchResultCount(String keyword) {
        return productDAO.countByProductName(keyword);
    }

    /**
     * 상품명으로 상품 검색 + 가격 정렬 + 페이지네이션
     */
    public List<ProductDTO> searchProductDTOsOrderByPriceWithPagination(String keyword, boolean ascending, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return productDAO.findByProductNameOrderByPriceWithPagination(keyword, ascending, offset, pageSize);
    }
}