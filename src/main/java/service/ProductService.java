package service;

import domain.dao.ProductDAO;
import domain.dto.PageDTO;
import domain.dto.ProductDTO;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Slf4j
public class ProductService {
    private final ProductDAO productDAO;
    private final MappingService mappingService;
    private final FileService fileService;

    public ProductService(ProductDAO productDAO, MappingService mappingService, FileService fileService) {
        this.productDAO = productDAO;
        this.mappingService = mappingService;
        this.fileService = fileService;
    }


    /**
     * 상품코드로 상품 조회
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

    /**
     * PageDTO 설정 메서드 (카테고리 지원)
     */
    public PageDTO setupProductPage(PageDTO pageDTO, Long categoryId) {
        // 카테고리가 지정된 경우
        if (categoryId != null) {
            pageDTO.setTotalCount(getProductCountByCategory(categoryId));
        }
        // 검색어가 있는 경우
        else if (pageDTO.getKeyword() != null && !pageDTO.getKeyword().trim().isEmpty()) {
            pageDTO.setTotalCount(getSearchResultCount(pageDTO.getKeyword()));
        } else {
            // 전체 상품 개수 조회
            pageDTO.setTotalCount(getTotalProductCount());
        }

        // 페이지네이션 계산
        pageDTO.calculatePagination();

        return pageDTO;
    }

    /**
     * 기본 setupProductPage 메서드 (오버로드)
     */
    public PageDTO setupProductPage(PageDTO pageDTO) {
        return setupProductPage(pageDTO, null);
    }

    // 페이지네이션 정보와 정렬 조건을 기반으로 상품 목록 조회
    public List<ProductDTO> getProductsByPage(PageDTO pageDTO) {
        // 검색어가 있는 경우
        if (pageDTO.getKeyword() != null && !pageDTO.getKeyword().trim().isEmpty()) {
            // 정렬 조건에 따라 다른 메서드 호출
            if ("priceAsc".equals(pageDTO.getSortBy())) {
                return searchProductDTOsOrderByPriceWithPagination(
                        pageDTO.getKeyword(), true, pageDTO.getCurrentPage(), pageDTO.getPageSize());
            } else if ("priceDesc".equals(pageDTO.getSortBy())) {
                return searchProductDTOsOrderByPriceWithPagination(
                        pageDTO.getKeyword(), false, pageDTO.getCurrentPage(), pageDTO.getPageSize());
            } else {
                // 기본 검색 결과
                return searchProductDTOsWithPagination(
                        pageDTO.getKeyword(), pageDTO.getCurrentPage(), pageDTO.getPageSize());
            }
        } else {
            // 전체 상품 목록
            if ("priceAsc".equals(pageDTO.getSortBy())) {
                return getProductDTOsOrderByPriceWithPagination(
                        true, pageDTO.getCurrentPage(), pageDTO.getPageSize());
            } else if ("priceDesc".equals(pageDTO.getSortBy())) {
                return getProductDTOsOrderByPriceWithPagination(
                        false, pageDTO.getCurrentPage(), pageDTO.getPageSize());
            } else {
                // 기본 정렬
                return getProductDTOsWithPagination(
                        pageDTO.getCurrentPage(), pageDTO.getPageSize());
            }
        }
    }

    /**
     * 카테고리별 상품 목록 조회 및 정렬 (확장)
     */
    public List<ProductDTO> getProductsByPage(PageDTO pageDTO, Long categoryId) {
        // 카테고리가 지정된 경우
        if (categoryId != null) {
            // 정렬 조건에 따라 조회
            if ("priceAsc".equals(pageDTO.getSortBy())) {
                return getProductsByCategoryOrderByPriceWithPagination(
                        categoryId, true, pageDTO.getCurrentPage(), pageDTO.getPageSize());
            } else if ("priceDesc".equals(pageDTO.getSortBy())) {
                return getProductsByCategoryOrderByPriceWithPagination(
                        categoryId, false, pageDTO.getCurrentPage(), pageDTO.getPageSize());
            } else {
                // 기본 정렬 (최신순)
                return getProductsByCategoryWithPagination(
                        categoryId, pageDTO.getCurrentPage(), pageDTO.getPageSize());
            }
        }

        // 카테고리가 없는 경우 기존 메서드 사용
        return getProductsByPage(pageDTO);
    }

    // PageDTO에서 요청 파라미터 설정 메서드
    public PageDTO createPageDTOFromParameters(String pageParam, String sortByParam, String keywordParam) {
        PageDTO pageDTO = new PageDTO();

        // 페이지 번호 설정
        if (pageParam != null && !pageParam.isEmpty()) {
            try {
                int currentPage = Integer.parseInt(pageParam);
                pageDTO.setCurrentPage(Math.max(1, currentPage));
            } catch (NumberFormatException e) {
                // 잘못된 형식이면 기본값 1 사용
                pageDTO.setCurrentPage(1);
            }
        }

        // 정렬 옵션 설정
        pageDTO.setSortBy(sortByParam);

        // 검색어 설정
        pageDTO.setKeyword(keywordParam);

        return pageDTO;
    }

    // 상품 및 관련 데이터 삭제를 위한 통합 메서드
    public boolean deleteProductWithRelations(String productCode) {
        try {
            // 상품 정보 조회 (이미지 ID 확인을 위해)
            ProductDTO productDTO = getProductDTOByCode(productCode);
            if (productDTO == null) {
                return false;
            }

            // 카테고리 매핑 삭제
            try {
                mappingService.deleteAllMappingsByProductCode(productCode);
            } catch (Exception e) {
                log.error("상품 관련 카테고리 매핑 삭제 중 오류 발생: {}", e.getMessage(), e);
                // 매핑 삭제 실패해도 계속 진행
            }

            // 상품 삭제
            boolean success = deleteProduct(productCode);

            // 이미지 파일 삭제
            if (success && productDTO.getFileId() != null && !productDTO.getFileId().isEmpty()) {
                try {
                    fileService.deleteFile(productDTO.getFileId());
                } catch (Exception e) {
                    log.warn("상품은 삭제되었으나 이미지 삭제 실패: {}", e.getMessage());
                    // 이미지 삭제 실패해도 성공으로 간주
                }
            }

            return success;
        } catch (Exception e) {
            log.error("상품 및 관련 데이터 삭제 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 카테고리별 상품 목록 조회
     */
    public List<ProductDTO> getProductsByCategoryWithPagination(Long categoryId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return productDAO.findByCategoryId(categoryId, offset, pageSize);
    }

    /**
     * 카테고리별 상품 목록 가격순 정렬
     */
    public List<ProductDTO> getProductsByCategoryOrderByPriceWithPagination(Long categoryId, boolean ascending, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return productDAO.findByCategoryIdOrderByPriceWithPagination(categoryId, ascending, offset, pageSize);
    }

    /**
     * 카테고리별 상품 개수 조회
     */
    public int getProductCountByCategory(Long categoryId) {
        return productDAO.countByCategoryId(categoryId);
    }

    /**
     * 재고 확인 및 수량 업데이트(감소)
     * @param productCode
     * @param orderQuantity
     * @return
     */
    public boolean checkAndUpdateStock(String productCode, int orderQuantity) {
        int currentStock = productDAO.getProductStock(productCode);
        if (currentStock < orderQuantity) {
            return false; // 재고 부족
        }
        // 재고 감소
        int result = productDAO.updateProductStock(productCode, currentStock - orderQuantity);
        return result > 0; // int를 boolean으로 변환
    }

    public boolean hasEnoughStock(String productCode, int quantity) {
        int currentStock = productDAO.getProductStock(productCode);
        return currentStock >= quantity;
    }

    public boolean reduceStock(String productCode, int quantity) {
        int currentStock = productDAO.getProductStock(productCode);
        if (currentStock < quantity) {
            return false;
        }
        int result = productDAO.updateProductStock(productCode, currentStock - quantity);
        return result > 0; // int를 boolean으로 변환
    }

    /**
     * 상품의 현재 재고 수량을 조회합니다.
     * @param productCode 상품 코드
     * @return 현재 재고 수량
     */
    public int getProductStock(String productCode) {
        try {
            return productDAO.getProductStock(productCode);
        } catch (Exception e) {
            log.error("상품 재고 조회 중 오류 발생: " + e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 상품 재고를 주문 수량만큼 감소시킵니다.
     * @param productCode 상품 코드
     * @param orderQuantity 주문 수량
     * @return 성공 여부
     */
    public boolean updateProductStock(String productCode, int orderQuantity) {
        try {
            // 현재 재고 조회
            int currentStock = getProductStock(productCode);

            // 재고가 부족한 경우
            if (currentStock < orderQuantity) {
                log.warn("상품 재고 부족: 상품코드={}, 현재재고={}, 주문수량={}",
                        productCode, currentStock, orderQuantity);
                return false;
            }

            // 재고 감소
            int newStock = currentStock - orderQuantity;
            int result = productDAO.updateProductStock(productCode, newStock);

            return result > 0;  // 영향받은 행이 있으면 성공으로 간주
        } catch (Exception e) {
            log.error("상품 재고 업데이트 중 오류 발생: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 상품 재고를 증가시킵니다 (주문 취소 시 사용)
     * @param productCode 상품 코드
     * @param quantity 증가시킬 수량
     * @return 성공 여부
     */
    public boolean increaseProductStock(String productCode, int quantity) {
        try {
            // 현재 재고 조회
            int currentStock = getProductStock(productCode);

            // 재고 증가
            int newStock = currentStock + quantity;
            log.info("상품 재고 증가: 상품코드={}, 현재재고={}, 증가수량={}, 새재고={}",
                    productCode, currentStock, quantity, newStock);

            int result = productDAO.updateProductStock(productCode, newStock);
            return result > 0;
        } catch (Exception e) {
            log.error("상품 재고 증가 중 오류 발생: " + e.getMessage(), e);
            return false;
        }
    }
}