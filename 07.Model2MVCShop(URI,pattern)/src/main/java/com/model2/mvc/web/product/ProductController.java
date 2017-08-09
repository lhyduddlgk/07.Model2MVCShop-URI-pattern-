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
				System.out.println("product��?"+product);
				product.setManuDate(product.getManuDate().replaceAll("-", ""));
				productService.addProduct(product);

				modelAndView.addObject("product", product);
				modelAndView.setViewName("forward:/product/addProduct.jsp");
			} else {
				int overSize = (request.getContentLength() / 1000000);
				System.out.println("<script>alert('������ ũ��� 1MB���� �Դϴ�. �ø��� ���� �뷮�� " + overSize + "MB�Դϴ�');");
				System.out.println("history.back();</script>");
			}
		} else {
			System.out.println("���ڵ� Ÿ���� multipart/form-data�� �ƴմϴ�.");
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
	 * // Client�� ������ ������ productService.addProduct(product);
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
		
		model.addAttribute("menu", menu);
		model.addAttribute("list", map.get("list"));
		model.addAttribute("resultPage", resultPage);
		model.addAttribute("search", search);

		return modelAndView;
	}

}
