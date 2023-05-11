package com.bzone.repository;

import com.bzone.model.Category;
import com.bzone.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Product findByProductName(String productName);

    Page<Product> findByProductNameLike(String productName,PageRequest pageRequest);


    @Query(value = "Select * from product order by product_id desc limit 10", nativeQuery = true)
    Set<Product> findTopNewProduct();

    @Query(value = "Select * from product order by discount desc limit 10 ", nativeQuery = true)
    Set<Product> findTopDiscountProduct();
    @Query(value = "SELECT p.* FROM product p \n" +
            "left join orderdetail o on p.product_id = o.product_id\n" +
            "where order_id is not null", nativeQuery = true)
    Set<Product> findBestsellerProduct();

    Set<Product> findByOrderByDiscountDesc();

    Product findById(long id);

    @Query(value = "Select c from Category c WHERE c.id = ?1")
    Category findByCategoryID(Long id);

    @Query(value = "select p from Product p")
    public List<Product> searchAll();

    @Query(value = "select p from Product p where p.productName Like ?1%")
    public List<Product> searchByProductName(String name, Sort sort);

    @Query(value = "select p from Product p where p.productName LIKE ?1")
    Page<Product> searchByNameContain(String name, Pageable pageable);

    @Query(value = "select p from Product p where p.category.id = ?1")
    Page<Product> searchByCategory(Long categoryId, Pageable pageable);

    Page<Product> findAll(Specification<Product> var, Pageable pageable);


    Page<Product> findByAvailable(boolean available, PageRequest pageRequest);


    List<Product> findAllByAuthor(String author);

    List<Product> findAllByCategory(Category category);
}
