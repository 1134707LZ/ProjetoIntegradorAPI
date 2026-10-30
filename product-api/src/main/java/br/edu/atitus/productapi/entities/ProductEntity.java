package br.edu.atitus.productapi.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_product")
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    @Column(nullable = false)
    private double price;
    @Column(nullable = false)
    private String category;
    /** lista separada por vírgula, ex.: "P,M,G" */
    private String sizes;
    /** lista de cores hex separadas por vírgula */
    private String colors;
    /** URL ou data URI base64 */
    @Column(columnDefinition = "TEXT")
    private String image;
    private boolean customizable;
    /** id do usuário (auth-api) dono do anúncio */
    @Column(name = "owner_id")
    private Long ownerId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSizes() { return sizes; }
    public void setSizes(String sizes) { this.sizes = sizes; }
    public String getColors() { return colors; }
    public void setColors(String colors) { this.colors = colors; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public boolean isCustomizable() { return customizable; }
    public void setCustomizable(boolean customizable) { this.customizable = customizable; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
}
