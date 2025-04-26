package service;

import domain.model.Product;
import domain.repository.ProductRepository;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;

public class ProductService {
    private final ProductRepository productRepository;

    // 생성자 주입
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // 상품 등록
    public void registerProduct(Product product) {
        // 시작일이 없으면 현재 날짜로 설정
        if (product.getStartDate() == null || product.getStartDate().trim().isEmpty()) {
            product.setStartDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        }

        // 종료일도 없으면 1년 후로 설정
        if (product.getEndDate() == null || product.getEndDate().trim().isEmpty()) {
            product.setEndDate(LocalDate.now().plusYears(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        }

        // 필요한 비즈니스 검증 로직 추가
        productRepository.save(product);
    }

    // 상품 조회
    public Product getProductByCode(String productCode) {
        return productRepository.findByProductCode(productCode);
    }

    // 가격 순 정렬된 상품 목록 조회
    public List<Product> getProductsSortedByPrice(boolean ascending) {
        return productRepository.findAllOrderByPrice(ascending);
    }

    // 상품 수정
    public void updateProduct(Product product) {
        productRepository.update(product);
    }

    // 상품 삭제
    public boolean deleteProduct(String productCode) {
        return productRepository.delete(productCode);
    }

    // 재고 업데이트
    public boolean updateProductStock(String productCode, int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("재고는 0 이상이어야 합니다.");
        }
        return productRepository.updateStock(productCode, stock);
    }

    // 판매 상태 관리
    public boolean updateProductSaleStatus(String productCode, String startDate, String endDate) {
        // 날짜 형식 검증 로직 (YYYYMMDD 형식)
        if (!isValidDateFormat(startDate) || !isValidDateFormat(endDate)) {
            throw new IllegalArgumentException("날짜는 YYYYMMDD 형식이어야 합니다.");
        }
        return productRepository.updateSaleStatus(productCode, startDate, endDate);
    }

    // 날짜 형식 검증 메서드 (간단한 구현)
    private boolean isValidDateFormat(String date) {
        if (date == null || date.length() != 8) {
            return false;
        }
        
        try {
            int year = Integer.parseInt(date.substring(0, 4));
            int month = Integer.parseInt(date.substring(4, 6));
            int day = Integer.parseInt(date.substring(6, 8));
            
            return (year >= 2000 && year <= 2100) && 
                   (month >= 1 && month <= 12) && 
                   (day >= 1 && day <= 31);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // 품절 처리
    public boolean markProductAsOutOfStock(String productCode) {
        return productRepository.updateStock(productCode, 0);
    }

    // 판매 중지 처리
    public boolean suspendProductSale(String productCode) {
        // 현재 날짜를 이전 날짜로 설정하여 판매 중지 처리
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1); // 하루 전
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String yesterday = dateFormat.format(calendar.getTime());
        
        // 판매 기간을 어제까지로 설정 (판매 종료)
        return productRepository.updateSaleStatus(productCode, "20000101", yesterday);
    }
    
    // 상품명으로 상품 검색
    public List<Product> searchProductsByName(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("검색어를 입력해주세요.");
        }
        
        return productRepository.findByProductName(productName);
    }

}