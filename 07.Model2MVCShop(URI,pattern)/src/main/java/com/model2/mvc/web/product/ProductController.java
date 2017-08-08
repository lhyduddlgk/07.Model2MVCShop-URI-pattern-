package com.model2.mvc.web.product;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

@Controller
@RequestMapping("/product/*")
public class ProductController {

	// Field
	@Autowired
	@Qualifier("productServiceImpl")
	private ProductService productService;
	// setter Method ���� ����

	// Constructor
	public ProductController() {
		System.out.println(this.getClass());
	}

	// ==> classpath:config/common.properties ,
	// classpath:config/commonservice.xml ���� �Ұ�
	// ==> �Ʒ��� �ΰ��� �ּ��� Ǯ�� �ǹ̸� Ȯ�� �Ұ�
	@Value("#{commonProperties['pageUnit']}")
	// @Value("#{commonProperties['pageUnit'] ?: 3}")
	int pageUnit;

	@Value("#{commonProperties['pageSize']}")
	// @Value("#{commonProperties['pageSize'] ?: 2}")
	int pageSize;

	@RequestMapping(value = "addProduct", method = RequestMethod.GET)
	public ModelAndView addProduct(@ModelAttribute("product") Product product) throws Exception {
		System.out.println("/product/addProduct : GET");

		product.setManuDate(product.getManuDate().replaceAll("-", ""));

		// Client�� ������ ������
		productService.addProduct(product);

		ModelAndView modelAndView = new ModelAndView();

		modelAndView.addObject("product", product);
		modelAndView.setViewName("forward:/product/addProduct.jsp");

		return modelAndView;
	}

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

		System.out.println("\n\ngetProduct�� prodName is " + product.getProdName());

		System.out.println("\n\ngetProduct�� prodNo is " + product.getProdNo());

		System.out.println("updateProduct���� ���� �� ������ menu = " + menu);
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
		System.out.println("/updateProduct�� �̵�");

		// Business Logic
		product.setManuDate(product.getManuDate().replaceAll("-", ""));
		productService.getProduct(prodNo);
		productService.updateProduct(product);

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("product", product);
		// �������� @RequestParam���� ó�� ���̰�, �Ķ���� ���� �޾ƿ��� ���� ���·� ����
		// �Ķ���� ���� �޾ƿ� ��, getProduct�� if������ updateProductView�� ���� ���ѹݺ����°� ������
		modelAndView.setViewName("forward:/product/getProduct");

		System.out.println(modelAndView.getViewName());

		return modelAndView;
	}

	@RequestMapping(value = "listProduct")
	// @ModelAttribute�� getParameter�� �ϴ� Annotation
	public ModelAndView listProduct(@ModelAttribute("search") Search search,
			@RequestParam(value = "menu", required = false) String menu, Model model, HttpServletRequest request)
			throws Exception {

		System.out.println("/listProduct");

		if (search.getCurrentPage() == 0) {
			search.setCurrentPage(1);
		}
		search.setPageSize(pageSize);

		// Business logic ����
		Map<String, Object> map = productService.getProductList(search);

		Page resultPage = new Page(search.getCurrentPage(), ((Integer) map.get("totalCount")).intValue(), pageUnit,
				pageSize);

		System.out.println(resultPage);

		// Model �� View ����
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("message", "ProductController ��� Ȯ��");
		modelAndView.setViewName("forward:/product/listProduct.jsp");

		model.addAttribute("list", map.get("list"));
		model.addAttribute("resultPage", resultPage);
		model.addAttribute("search", search);

		return modelAndView;
	}

}
