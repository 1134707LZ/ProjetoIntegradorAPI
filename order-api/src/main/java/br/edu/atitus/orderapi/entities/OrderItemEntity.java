package br.edu.atitus.orderapi.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_order_item")
public class OrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    private String productName;
    /** preço unitário calculado no servidor (produto + acréscimos de personalização) */
    private double unitPrice;
    private int quantity;
    private String size;
    private String color;
    private String customModel;
    private String customName;
    private String customNumber;

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getCustomModel() { return customModel; }
    public void setCustomModel(String customModel) { this.customModel = customModel; }
    public String getCustomName() { return customName; }
    public void setCustomName(String customName) { this.customName = customName; }
    public String getCustomNumber() { return customNumber; }
    public void setCustomNumber(String customNumber) { this.customNumber = customNumber; }
}
