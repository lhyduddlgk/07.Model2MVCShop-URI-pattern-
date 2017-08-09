package com.model2.mvc.web.product;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.model2.mvc.common.Page;
import com.model2.mvc.common.Search;
import com.model2.mvc.service.domain.Product;
import com.model2.mvc.service.product.ProductService;
import com.model2.mvc.service.product.impl.ProductServiceImpl;

@Controller
@RequestMapping("/product/*")
public class ProductController {

	// Field
	@Autowired
	@Qualifier("productServiceImpl")
	private ProductService productService;
	// setter Method 구현 않음

	// Constructor
	public ProductController() {
		System.out.println(this.getClass());
	}

	// ==> classpath:config/common.properties ,
	// classpath:config/commonservice.xml 참조 할것
	// ==> 아래의 두개를 주석을 풀어 의미를 확인 할것
	@Value("#{commonProperties['pageUnit']}")
	// @Value("#{commonProperties['pageUnit'] ?: 3}")
	int pageUnit;

	@Value("#{commonProperties['pageSize']}")
	// @Value("#{commonProperties['pageSize'] ?: 2}")
	int pageSize;

	@RequestMapping(value = "addProduct"/* , method = RequestMethod.GET */)
	public ModelAndView addProduct(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView modelAndView = new ModelAndView();
		if (FileUpload.isMultipartContent(request)) {

			String temDir = "C:\\workspace\\07.Model2MVCShop(URI,pattern)\\WebContent\\images\\uploadFiles";

			DiskFileUpload fileUpload = new DiskFileUpload();
			fileUpload.setRepositoryPath(temDir);
			fileUpload.setSizeMax(1024 * 1024 * 10);
			fileUpload.setSizeThreshold(1024 * 100);

			if (request.getContentLength() < fileUpload.getSizeMax()) {
				Product product = new Product();

				StringTokenizer token = null;

				List fileItemList = fileUpload.parseRequest(request);
				int Size = fileItemList.size();
				for (int i = 0; i < Size; i++) {
					FileItem fileItem = (FileItem) fileItemList.get(i);

					if (fileItem.isFormField()) {
						if (fileItem.getFieldName().equals("manuDate")) {
							token = new StringTokenizer(fileItem.getString("euc-kr"), "-");
							String manuDate = token.nextToken() + token.nextToken() + token.nextToken();
							product.setManuDate(manuDate);
						} else if (fileItem.getFieldName().equals("prodName"))
							product.setProdName(fileItem.getString("euc-kr"));
						else if (fileItem.getFieldName().equals("prodDetail"))
							product.setFileName(fileItem.getString("euc-kr"));
						else if (fileItem.getFieldName().equals("price"))
							product.setPrice(Integer.parseInt(fileItem.getString("euc-kr")));
					} else {
						if (fileItem.getSize() > 0) {
							int idx = fileItem.getName().lastIndexOf("\\");
							if (idx == -1) {
								idx = fileItem.getName().lastIndexOf("/");
							}
							String fileName = fileItem.getName().substring(idx + 1);
							product.setFileName(fileName);
							try {
								File uploadedFile = new File(temDir, fileName);
								fileItem.write(uploadedFile);
							} catch (IOException e) {
								System.out.println(e);
							}
						} else {
							product.setFileName("../../images/empty.GIF");
						}
					} // else
				} // for
				System.out.println("product는?"+product);
				product.setManuDate(product.getManuDate().replaceAll("-", ""));
				productService.addProduct(product);

				modelAndView.addObject("product", product);
				modelAndView.setViewName("forward:/product/addProduct.jsp");
			} else {
				int overSize = (request.getContentLength() / 1000000);
				System.out.println("<script>alert('파일의 크기는 1MB까지 입니다. 올리신 파일 용량은 " + overSize + "MB입니다');");
				System.out.println("history.back();</script>");
			}
		} else {
			System.out.println("인코딩 타입이 multipart/form-data가 아닙니다.");
		}
		return modelAndView;
	}

	/*
	 * @RequestMapping(value = "addProduct", method = RequestMethod.GET) public
	 * ModelAndView addProduct(@ModelAttribute("product") Product product)
	 * throws Exception { System.out.println("/product/addProduct : GET");
	 * 
	 * product.setManuDate(product.getManuDate().replaceAll("-", ""));
	 * 
	 * // Client에 보여줄 데이터 productService.addProduct(product);
	 * 
	 * ModelAndView modelAndView = new ModelAndView();
	 * 
	 * modelAndView.addObject("product", product);
	 * modelAndView.setViewName("forward:/product/addProduct.jsp");
	 * 
	 * return modelAndView; }
	 */

	@RequestMapping(value = "getProduct")
	public ModelAndView getProduct(@RequestParam("prodNo") int prodNo,
			@RequestParam(value = "menu", required = false) String menu, @ModelAttribute("search") Search search,
			Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		System.out.println("/getProduct");

		Cookie[] cookies = request.getCookies();
		Cookie cookie = null;
		if (cookies != null && cookies.length > 0) {
			for (int i = 0; i < cookies.length; i++) {
				cookie = cookies[i];
				if (cookie.getName().equals("history")) {
					cookie = new Cookie("history", cookies[i].getValue() + "," + prodNo);
				} else {
					cookie = new Cookie("history", "prodNo");
				}
			}
		}

		cookie.setMaxAge(60 * 5);
		response.addCookie(cookie);

		Product product = productService.getProduct(prodNo);

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("product", product);

		System.out.println("\n\ngetProduct의 prodName is " + product.getProdName());

		System.out.println("\n\ngetProduct의 prodNo is " + product.getProdNo());

		System.out.println("updateProduct에서 수정 후 찍히는 menu = " + menu);
		if (menu != null) {
			if (menu.equals("search")) {
				modelAndView.setViewName("forward:/product/getProduct.jsp");
			} else {
				modelAndView.setViewName("forward:/product/updateProductView.jsp");
			}
		} else {
			modelAndView.setViewName("forward:/product/getProduct.jsp");
		}
		return modelAndView;
	}

	@RequestMapping(value = "updateProduct", method = RequestMethod.POST)
	public ModelAndView updateProduct(@ModelAttribute("product") Product product, @RequestParam("prodNo") int prodNo,
			@RequestParam(value = "menu", required = false) String menu) throws Exception {
		System.out.println("/updateProduct로 이동");

		// Business Logic
		product.setManuDate(product.getManuDate().replaceAll("-", ""));
		productService.getProduct(prodNo);
		productService.updateProduct(product);

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("product", product);
		// 상위에서 @RequestParam으로 처리 중이고, 파라미터 값을 받아오지 않응 상태로 전송
		// 파라미터 값을 받아올 시, getProduct의 if문에서 updateProductView로 가는 무한반복상태가 유지됨
		modelAndView.setViewName("forward:/product/getProduct");

		System.out.println(modelAndView.getViewName());

		return modelAndView;
	}

	@RequestMapping(value = "listProduct")
	// @ModelAttribute는 getParameter를 하는 Annotation
	public ModelAndView listProduct(@ModelAttribute("search") Search search,
			@RequestParam(value = "menu", required = false) String menu, Model model, HttpServletRequest request)
			throws Exception {

		System.out.println("/listProduct");

		if (search.getCurrentPage() == 0) {
			search.setCurrentPage(1);
		}
		search.setPageSize(pageSize);

		// Business logic 수행
		Map<String, Object> map = productService.getProductList(search);

		Page resultPage = new Page(search.getCurrentPage(), ((Integer) map.get("totalCount")).intValue(), pageUnit,
				pageSize);

		System.out.println(resultPage);

		// Model 과 View 연결
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("message", "ProductController 출력 확인");
		modelAndView.setViewName("forward:/product/listProduct.jsp");
		
		model.addAttribute("menu", menu);
		model.addAttribute("list", map.get("list"));
		model.addAttribute("resultPage", resultPage);
		model.addAttribute("search", search);

		return modelAndView;
	}

}
