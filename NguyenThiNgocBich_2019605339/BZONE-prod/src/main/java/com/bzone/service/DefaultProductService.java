package com.bzone.service;

import com.bzone.model.Product;
import com.bzone.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service("productService")
public class DefaultProductService implements ProductService {

    @Autowired
    private ProductRepository productRepository;


    @Override
    public void addProduct(Product product) {
        productRepository.save(product);
    }


    @Override
    public Set<Product> getProductsByCategory(int id, Set<Product> set) {
        Set<Product> set_result = new HashSet<>();
        for (Product i : set) {
            if(i.getCategory().getId() == id || i.getCategory().getParentCategory().getId() == id ||
             i.getCategory().getParentCategory().getParentCategory().getId() == id){
                set_result.add(i);
            }
        }
        return set_result;
    }
}
