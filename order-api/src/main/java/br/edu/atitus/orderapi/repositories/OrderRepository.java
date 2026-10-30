package br.edu.atitus.orderapi.repositories;

import br.edu.atitus.orderapi.entities.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByUserIdOrderByIdDesc(Long userId);
    List<OrderEntity> findAllByOrderByIdDesc();
}
