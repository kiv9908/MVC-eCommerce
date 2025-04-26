package domain.model;

import java.util.Date;

public class Product {
    private String productCode;     // no_product - VARCHAR2(30), PK
    private String productName;     // nm_product - VARCHAR2(200)
    private String detailExplain;   // nm_detail_explain - CLOB
    private String fileId;          // id_file - VARCHAR2(30)
    private String startDate;       // dt_start_date - VARCHAR2(8)
    private String endDate;         // dt_end_date - VARCHAR2(8)
    private Integer customerPrice;  // qt_customer - NUMBER(9)
    private Integer salePrice;      // qt_sale_price - NUMBER(9)
    private Integer stock;          // qt_stock - NUMBER(9)
    private Integer deliveryFee;    // qt_delivery_fee - NUMBER(9)
    private String registerId;      // no_register - VARCHAR2(30)
    private Date firstDate;         // da_first_date - DATE

    public Product() {
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDetailExplain() {
        return detailExplain;
    }

    public void setDetailExplain(String detailExplain) {
        this.detailExplain = detailExplain;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Integer getCustomerPrice() {
        return customerPrice;
    }

    public void setCustomerPrice(Integer customerPrice) {
        this.customerPrice = customerPrice;
    }

    public Integer getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(Integer salePrice) {
        this.salePrice = salePrice;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(Integer deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public String getRegisterId() {
        return registerId;
    }

    public void setRegisterId(String registerId) {
        this.registerId = registerId;
    }

    public Date getFirstDate() {
        return firstDate;
    }

    public void setFirstDate(Date firstDate) {
        this.firstDate = firstDate;
    }

    /**
     * 상품의 상태를 판단하는 메서드
     * @return 상품 상태 문자열: "판매중", "품절", "판매중지", "판매예정"
     */
    public String getProductStatus() {
        // 현재 날짜 가져오기
        String currentDate = new java.text.SimpleDateFormat("yyyyMMdd").format(new Date());
        
        // 재고 확인
        if (stock != null && stock <= 0) {
            return "품절";
        }
        
        // 판매 기간 확인
        if (startDate != null && endDate != null) {
            // 판매 시작일이 현재 날짜보다 미래인 경우 - 판매 예정
            if (startDate.compareTo(currentDate) > 0) {
                return "판매예정";
            }
            
            // 판매 종료일이 현재 날짜보다 과거인 경우 - 판매 중지
            if (endDate.compareTo(currentDate) < 0) {
                return "판매중지";
            }
        }
        
        // 위 조건 모두 해당하지 않으면 판매중
        return "판매중";
    }

}
