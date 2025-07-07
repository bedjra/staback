package com.Megatram.Megatram.repository;

import com.Megatram.Megatram.Entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findByProduitIdAndEntrepotIdAndDateBefore(Long produitId, Long entrepotId, Date date);
    @Query("SELECT s FROM Stock s WHERE s.entrepot.id = :entrepotId AND s.date <= :date")
    List<Stock> findByEntrepotAndDateBefore(@Param("entrepotId") Long entrepotId, @Param("date") Date date);

}
