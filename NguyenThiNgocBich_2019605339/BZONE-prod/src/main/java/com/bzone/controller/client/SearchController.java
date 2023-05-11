package com.bzone.controller.client;

import com.bzone.model.Category;
import com.bzone.model.Product;
import com.bzone.model.Product_;
import com.bzone.repository.CategoryRepository;
import com.bzone.repository.ProductRepository;
import com.bzone.service.ProductSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class SearchController {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    Map<Integer, Boolean> pricesMap = new HashMap<>();

    SearchController() {
        for (int i = 0; i < 5; i++) {
            pricesMap.put(i, false);
        }
    }

    @GetMapping("list")
    public String list(ModelMap model,
                       @RequestParam(value = "page") Optional<Integer> page,
                       @RequestParam(value = "size") Optional<Integer> size) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(24);

        Pageable pageable = PageRequest.of(currentPage - 1, pageSize, Sort.by("productName"));

        Page<Product> resultPage = null;

        resultPage = productRepository.findAll(pageable);
        int totalPages = resultPage.getTotalPages();
        if (totalPages > 0) {
            int start = Math.max(1, currentPage - 2);
            int end = Math.min(currentPage + 2, totalPages);

            if (totalPages > 5) {
                if (end == totalPages) start = end - 5;
                else if (start == 1) end = start + 5;
            }
            List<Integer> pageNumbers = IntStream.rangeClosed(start, end)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }
        model.addAttribute("products", resultPage);
        return "/pages/client/test";
    }

    @GetMapping("search")
    public String searchByName(ModelMap model,
                               @RequestParam(value = "name", required = false) String name,
                               @RequestParam("type") Optional<Integer[]> languageIDs,
                               @RequestParam("price") Optional<Integer[]> priceIDs,
                               @RequestParam("color") Optional<Integer[]> bookLayoutIDs,
                               @RequestParam("exists") Optional<Integer> quantity,
                               @RequestParam(value = "page") Optional<Integer> page,
                               @RequestParam(value = "size") Optional<Integer> size,
                               @RequestParam(name = "Sortby") Optional<Integer> sort) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(24);
        int qty = quantity.orElse(1);
        int srt = sort.orElse(1);
        boolean[] pricesArr = new boolean[6];
        boolean[] langsArr = new boolean[6];
        boolean[] bookLayoutsArr = new boolean[6];
        Arrays.fill(pricesArr, false);
        Arrays.fill(langsArr, false);
        Arrays.fill(bookLayoutsArr, false);
        String url = "search?name=" + name + "&exists=" + qty + "&size=" + pageSize + "&Sortby=" + srt;
        List<Product> products;
        Specification<Product> spe = (root, query, criteriaBuilder) -> criteriaBuilder.isNotNull(root.get(Product_.PRODUCT_NAME));
        spe = spe.and(ProductSpecification.qtyGreaterThan0(qty));
        if (priceIDs.isPresent()) {
            Specification<Product> priceSpe = Specification.where(ProductSpecification.priceInRange(priceIDs.get()[0]));
            if (priceIDs.get().length > 1) {
                for (int i = 1; i < priceIDs.get().length; i++) {
                    priceSpe = priceSpe.or(ProductSpecification.priceInRange(priceIDs.get()[i]));
                }
            }
            spe = spe.and(priceSpe);
            for (int i = 0; i < priceIDs.get().length; i++) {
                pricesArr[priceIDs.get()[i]] = true;
                url += "&price=" + priceIDs.get()[i].toString();
            }
        }
        if (languageIDs.isPresent()) {
            Specification<Product> langSpe = Specification.where(ProductSpecification.hasLanguage(languageIDs.get()[0]));
            if (languageIDs.get().length > 1) {
                for (int i = 1; i < languageIDs.get().length; i++) {
                    langSpe = langSpe.or(ProductSpecification.hasLanguage(languageIDs.get()[i]));
                }
            }
            spe = spe.and(langSpe);
            for (int i = 0; i < languageIDs.get().length; i++) {
                langsArr[languageIDs.get()[i]] = true;
                url += "&color=" + languageIDs.get()[i].toString();
            }
        }
        if (bookLayoutIDs.isPresent()) {
            Specification<Product> bookLayoutSpe = Specification.where(ProductSpecification.hasBookLayout(bookLayoutIDs.get()[0]));
            if (bookLayoutIDs.get().length > 1) {
                for (int i = 1; i < bookLayoutIDs.get().length; i++) {
                    bookLayoutSpe = bookLayoutSpe.or(ProductSpecification.hasBookLayout(bookLayoutIDs.get()[i]));
                }
            }
            spe = spe.and(bookLayoutSpe);
            for (int i = 0; i < bookLayoutIDs.get().length; i++) {
                bookLayoutsArr[bookLayoutIDs.get()[i]] = true;
                url += "&color=" + bookLayoutIDs.get()[i].toString();
            }
        }
        if (StringUtils.hasText(name)) {
            Specification<Product> speName = Specification.where(ProductSpecification.startWith(name));
            speName = speName.or(ProductSpecification.endWith(name));
            speName = speName.or(ProductSpecification.hasPublisher(name));
            speName = speName.or(ProductSpecification.hasAuthor(name));
            spe = spe.and(speName);
        }
        products = productRepository.findAll(spe);
        if (srt == 1) {
            Collections.sort(products, (o1, o2) -> o1.getProductName().compareTo(o2.getProductName()));
        } else if (srt == 2) {
            Collections.sort(products, (o1, o2) -> (int) (o1.getPrice() - o2.getPrice()));
        } else {
            Collections.sort(products, (o1, o2) -> (int) (o2.getPrice() - o1.getPrice()));
        }
        List<Product> productList = products.stream().skip((currentPage - 1) * pageSize).limit(pageSize).toList();
        model.addAttribute("products", productList);
        model.addAttribute("pricesArr", pricesArr);
        model.addAttribute("bookLayoutsArr", bookLayoutsArr);
        model.addAttribute("langsArr", langsArr);
        model.addAttribute("size", pageSize);
        model.addAttribute("exists", qty);
        model.addAttribute("name", name);
        model.addAttribute("cuPage", currentPage);
        model.addAttribute("url", url);
        model.addAttribute("endPage", products.size() % pageSize == 0 ? products.size() / pageSize : products.size() / pageSize + 1);
        return "/pages/client/searchByWord";
    }

    @GetMapping("category")
    public String searchByCategory(ModelMap model,
                                   @RequestParam(value = "id", required = false) Long categoryId,
                                   @RequestParam("type") Optional<Integer[]> languageIDs,
                                   @RequestParam("price") Optional<Integer[]> priceIDs,
                                   @RequestParam("color") Optional<Integer[]> bookLayoutIDs,
                                   @RequestParam("exists") Optional<Integer> quantity,
                                   @RequestParam("page") Optional<Integer> page,
                                   @RequestParam("size") Optional<Integer> size,
                                   @RequestParam(name = "sortBy") Optional<Integer> sort) {

        int currentPage = page.orElse(1);
        int pageSize = size.orElse(24);
        int qty = quantity.orElse(1);
        int srt = sort.orElse(1);
        boolean[] pricesArr = new boolean[6];
        boolean[] langsArr = new boolean[6];
        boolean[] bookLayoutsArr = new boolean[6];
        Arrays.fill(pricesArr, false);
        Arrays.fill(langsArr, false);
        Arrays.fill(bookLayoutsArr, false);
        String url = "category?id=" + categoryId + "&exists=" + qty + "&size=" + pageSize + "&Sortby=" + srt;
        List<Product> products;
        Category category = productRepository.findByCategoryID(categoryId);
        List<Long> idLst = new ArrayList<>();
        List<Product> lst = productRepository.findAll();
        for (int i = 0; i < lst.size(); i++) {
            if (lst.get(i).getCategory().getId() == categoryId ||
                    lst.get(i).getCategory().getParentCategory().getId() == categoryId ||
                    lst.get(i).getCategory().getParentCategory().getParentCategory().getId() == categoryId) {
                idLst.add(lst.get(i).getId());
            }
        }
        Specification<Product> spe = Specification.where(ProductSpecification.hasProductId(idLst));
        spe = spe.and(ProductSpecification.qtyGreaterThan0(qty));
        if (priceIDs.isPresent()) {
            Specification<Product> priceSpe = Specification.where(ProductSpecification.priceInRange(priceIDs.get()[0]));
            if (priceIDs.get().length > 1) {
                for (int i = 1; i < priceIDs.get().length; i++) {
                    priceSpe = priceSpe.or(ProductSpecification.priceInRange(priceIDs.get()[i]));

                }
            }
            spe = spe.and(priceSpe);
            for (int i = 0; i < priceIDs.get().length; i++) {
                pricesArr[priceIDs.get()[i]] = true;
                url += "&price=" + priceIDs.get()[i].toString();
            }
        }
        if (languageIDs.isPresent()) {
            Specification<Product> langSpe = Specification.where(ProductSpecification.hasLanguage(languageIDs.get()[0]));
            if (languageIDs.get().length > 1) {
                for (int i = 1; i < languageIDs.get().length; i++) {
                    langSpe = langSpe.or(ProductSpecification.hasLanguage(languageIDs.get()[i]));
                }
            }
            spe = spe.and(langSpe);
            for (int i = 0; i < languageIDs.get().length; i++) {
                langsArr[languageIDs.get()[i]] = true;
                url += "&lang=" + languageIDs.get()[i].toString();
            }
        }
        if (bookLayoutIDs.isPresent()) {
            Specification<Product> bookLayoutSpe = Specification.where(ProductSpecification.hasBookLayout(bookLayoutIDs.get()[0]));
            if (bookLayoutIDs.get().length > 1) {
                for (int i = 1; i < bookLayoutIDs.get().length; i++) {
                    bookLayoutSpe = bookLayoutSpe.or(ProductSpecification.hasBookLayout(bookLayoutIDs.get()[i]));
                }
            }
            spe = spe.and(bookLayoutSpe);
            for (int i = 0; i < bookLayoutIDs.get().length; i++) {
                bookLayoutsArr[bookLayoutIDs.get()[i]] = true;
                url += "&color=" + bookLayoutIDs.get()[i].toString();
            }
        }
        products = productRepository.findAll(spe);
        if (srt == 1) {
            Collections.sort(products, (o1, o2) -> o1.getProductName().compareTo(o2.getProductName()));
        } else if (srt == 2) {
            Collections.sort(products, (o1, o2) -> (int) (o1.getPrice() - o2.getPrice()));
        } else {
            Collections.sort(products, (o1, o2) -> (int) (o2.getPrice() - o1.getPrice()));
        }
        List<Product> productList = products.stream().skip((currentPage - 1) * pageSize).limit(pageSize).toList();
        model.addAttribute("category", category);
        model.addAttribute("products", productList);
        model.addAttribute("pricesArr", pricesArr);
        model.addAttribute("priceID", priceIDs.orElse(null));
        model.addAttribute("bookLayoutsArr", bookLayoutsArr);
        model.addAttribute("langsArr", langsArr);
        model.addAttribute("size", pageSize);
        model.addAttribute("exists", qty);
        model.addAttribute("bookLayoutId", bookLayoutIDs);
        model.addAttribute("endPage", products.size() % pageSize == 0 ? products.size() / pageSize : products.size() / pageSize + 1);
        model.addAttribute("url", url);
        model.addAttribute("cuPage", currentPage);
        return "/pages/client/searchByCategory";
    }
}

