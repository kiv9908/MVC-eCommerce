package service;

import domain.dao.ProductDAO;
import domain.dto.ProductDTO;
import domain.model.Product;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Slf4j
public class ProductService {
    private final ProductDAO productDAO;

    public ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    /**
     * 모든 상품 조회 (Entity 반환)
     */
    public List<Product> findAll() {
        return productDAO.findAll();
    }

    /**
     * 모든 상품 조회 (Entity 반환) - findAll의 별명
     */
    public List<Product> getAllProducts() {
        return findAll();
    }

    /**
     * 상품 목록 조회 (DTO 변환)
     */
    public List<ProductDTO> getAllProductDTOs() {
        List<Product> products = productDAO.findAll();
        return products.stream()
                .map(ProductDTO::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 가격순으로 정렬된 상품 목록 조회 (DTO 변환)
     */
    public List<ProductDTO> getAllProductDTOsOrderByPrice(boolean ascending) {
        List<Product> products = productDAO.findAllOrderByPrice(ascending);
        return products.stream()
                .map(ProductDTO::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 상품명으로 상품 검색 (DTO 변환)
     */
    public List<ProductDTO> searchProductDTOs(String keyword) {
        List<Product> products = productDAO.findByProductName(keyword);
        return products.stream()
                .map(ProductDTO::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 상품코드로 상품 조회 (DTO 변환)
     */
    public ProductDTO getProductDTOByCode(String productCode) {
        Product product = productDAO.findByProductCode(productCode);
        return ProductDTO.toDTO(product);
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

            Product product = productDTO.toEntity();
            productDAO.save(product);
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
            Product product = productDTO.toEntity();
            productDAO.modify(product);
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
     * 상품 재고 수정
     */
    public boolean updateProductStock(String productCode, int stock) {
        try {
            return productDAO.modifyStock(productCode, stock);
        } catch (Exception e) {
            log.error("상품 재고 수정 중 오류 발생: {}", e.getMessage(), e);
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
            Product product = productDAO.findByProductCode(productCode);
            if (product == null) {
                log.error("품절 처리 실패: 상품을 찾을 수 없음 (productCode={})", productCode);
                return false;
            }
            
            log.info("품절 처리 중: 상품명={}, 현재 재고={}", product.getProductName(), product.getStock());
            
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
            Product product = productDAO.findByProductCode(productCode);
            if (product == null) {
                return false;
            }
            
            String startDate = product.getStartDate(); // 시작일은 그대로 유지
            
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
            Product product = productDAO.findByProductCode(productCode);
            if (product == null) {
                return false;
            }
            
            // 1. 재고가 0이면 1로 설정
            if (product.getStock() == null || product.getStock() <= 0) {
                success = productDAO.modifyStock(productCode, 1);
            }
            
            if (!success) {
                return false;
            }
            
            // 2. 판매 기간 설정 (현재 날짜부터 3개월)
            LocalDate today = LocalDate.now();
            String startDate = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String endDate = today.plusMonths(3).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            // 판매 기간을 업데이트하여 판매중 상태로 만듦
            return productDAO.modifySaleStatus(productCode, startDate, endDate);
        } catch (Exception e) {
            log.error("상품 판매 시작 처리 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 상품의 현재 판매 상태 조회
     */
    public String getProductStatus(String productCode) {
        try {
            Product product = productDAO.findByProductCode(productCode);
            if (product == null) {
                return null;
            }
            
            // 재고 확인
            if (product.getStock() != null && product.getStock() <= 0) {
                return "품절";
            }
            
            // 판매 기간 확인
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            if (product.getEndDate() != null && product.getEndDate().compareTo(currentDate) < 0) {
                return "판매중지";
            }
            
            return "판매중";
        } catch (Exception e) {
            log.error("상품 상태 조회 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }
}