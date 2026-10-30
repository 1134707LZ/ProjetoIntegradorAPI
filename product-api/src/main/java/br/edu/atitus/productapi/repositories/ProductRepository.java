package br.edu.atitus.productapi.repositories;

import br.edu.atitus.productapi.entities.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    @Query("""
           select p from ProductEntity p
           where (:category = '' or p.category = :category)
             and (lower(p.name) like :q or lower(p.description) like :q)
           order by p.id desc
           """)
    List<ProductEntity> search(@Param("q") String q, @Param("category") String category);

    List<ProductEntity> findByOwnerIdOrderByIdDesc(Long ownerId);
}
