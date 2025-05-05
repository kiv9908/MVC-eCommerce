package domain.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import domain.model.Product;
import java.util.Date;

/**
 * 상품 정보를 전송하기 위한 DTO 클래스
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private String productCode;
    private String productName;
    private String detailExplain;
    private String fileId;
    private String startDate;
    private String endDate;
    private Integer customerPrice;
    private Integer salePrice;
    private Integer stock;
    private Integer deliveryFee;
    private String registerId;
    private Date firstDate;
    private String status;
    
    /**
     * Product 엔티티로부터 DTO 객체 생성
     */
    public static ProductDTO toDTO(Product product) {
        if (product == null) {
            return null;
        }
        
        ProductDTO dto = new ProductDTO();
        dto.setProductCode(product.getProductCode());
        dto.setProductName(product.getProductName());
        dto.setDetailExplain(product.getDetailExplain());
        dto.setFileId(product.getFileId());
        dto.setStartDate(product.getStartDate());
        dto.setEndDate(product.getEndDate());
        dto.setCustomerPrice(product.getCustomerPrice());
        dto.setSalePrice(product.getSalePrice());
        dto.setStock(product.getStock());
        dto.setDeliveryFee(product.getDeliveryFee());
        dto.setRegisterId(product.getRegisterId());
        dto.setFirstDate(product.getFirstDate());
        dto.setStatus(product.getStatus()); // 동적으로 계산된 상태 설정
        
        return dto;
    }
    
    /**
     * DTO 객체로부터 Product 엔티티 생성
     */
    public Product toEntity() {
        Product product = new Product();
        
        product.setProductCode(this.productCode);
        product.setProductName(this.productName);
        product.setDetailExplain(this.detailExplain);
        product.setFileId(this.fileId);
        product.setStartDate(this.startDate);
        product.setEndDate(this.endDate);
        product.setCustomerPrice(this.customerPrice);
        product.setSalePrice(this.salePrice);
        product.setStock(this.stock);
        product.setDeliveryFee(this.deliveryFee);
        product.setRegisterId(this.registerId);
        product.setFirstDate(this.firstDate);

        return product;
    }
    
    /**
     * 유효성 검증 메서드
     */
    public boolean isValid() {
        // 필수 입력 항목 검증
        if (productName == null || productName.trim().isEmpty()) {
            return false;
        }
        
        // 판매가격은 필수이며 양수여야 함
        if (salePrice == null || salePrice <= 0) {
            return false;
        }
        
        return true;
    }
}