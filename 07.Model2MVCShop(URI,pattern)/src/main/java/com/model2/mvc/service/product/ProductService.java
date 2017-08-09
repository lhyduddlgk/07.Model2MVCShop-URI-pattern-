package com.model2.mvc.service.product;

import java.util.Map;

import com.model2.mvc.common.Search;
import com.model2.mvc.service.domain.Product;

public interface ProductService {

		//상품등록
		public void addProduct(Product product) throws Exception;
		
		// 상품정보 확인
		public Product getProduct(int prodNo) throws Exception;
			
		// 상품정보 리스트
		public Map<String, Object> getProductList(Search search) throws Exception;	
		
		// 상품정보수정
		public void updateProduct(Product product) throws Exception;
		
		// 상품명 중복 확인
		public boolean checkDuplication(int prodNo) throws Exception;

}
