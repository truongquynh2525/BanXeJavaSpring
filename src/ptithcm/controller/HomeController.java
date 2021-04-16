package ptithcm.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import ptithcm.bean.Mailer;
import ptithcm.entity.Advertisement;
import ptithcm.entity.Brand;
import ptithcm.entity.Order;
import ptithcm.entity.Product;
import ptithcm.entity.User;


@Transactional
@Controller
@RequestMapping(value={"", "/home/"})
public class HomeController {
	@Autowired
	SessionFactory factory;
	
	@Autowired
	Mailer mailer;
	
	@RequestMapping("index")
	public String index(ModelMap model, HttpServletRequest req, HttpServletResponse response) {
		HttpSession user_session = req.getSession();
		user_session.getAttribute("user");
		if(user_session.getAttribute("user") == null) {
			model.addAttribute("isLogin", false);
		}
		if(user_session.getAttribute("user") != null) {
			model.addAttribute("isLogin", true);
		}
		Session session = factory.getCurrentSession();
		String hql = "FROM Product";
		Query query = session.createQuery(hql);
		List<Product> list = query.list();
		model.addAttribute("products", list);
		model.addAttribute("ads", getAds());
		model.addAttribute("brands", getBrands());
		model.addAttribute("types", getTypes());
		return "home/index";
	}
	
	//láº¥y danh sÃ¡ch hÃ£ng
	@SuppressWarnings("unchecked")
	public List<Brand> getBrands() {
		Session session = factory.getCurrentSession();
		String hql = "FROM Brand";
		Query query = session.createQuery(hql);
		List<Brand> list = query.list();
		return list;
	}
	
	//láº¥y loáº¡i xe
	public List<String> getTypes() {
		List<String> list = new ArrayList<>();
		list.add("Xe tay ga");
		list.add("Xe sá»‘");
		list.add("Xe cÃ´n tay");
		return list;
	}
	
	//láº¥y danh sÃ¡ch quáº£ng cÃ¡o
	@SuppressWarnings("unchecked")
	public List<Advertisement> getAds() {
		Session session = factory.getCurrentSession();
		String hql = "FROM Advertisement";
		Query query = session.createQuery(hql);
		List<Advertisement> list = query.list();
		return list;
	}
	
	//Ä‘Äƒng nháº­p
	@RequestMapping(value="login", method=RequestMethod.GET)
	public String adminlogin() {
		return "home/login";
	}
	
	@RequestMapping(value="login", method=RequestMethod.POST) 
	public String adminlogin(HttpServletRequest request, ModelMap model) {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		username.trim();
		password.trim();
		
		Session session = factory.getCurrentSession();
		String hql = "FROM User";
		Query query = session.createQuery(hql);
		List<User> list = query.list();
		for(User u : list) {
			if(username.equals(u.getUsername().trim())==true) {
				if(password.equals(u.getPassword().trim()) == false) {
					model.addAttribute("message2", "Sai máº­t kháº©u !");
					return "home/login";
				}
				else {
					if(u.getRole() == true) {
						HttpSession admin_session = request.getSession();
						admin_session.setAttribute("admin", u);
						return "redirect:/admin/home.htm";
					}
					else {
						HttpSession user_session = request.getSession();
						user_session.setAttribute("user", u);
						return "redirect:/home/index.htm";
					}
				}
			}
		}
		model.addAttribute("message1", "Sai tÃªn Ä‘Äƒng nháº­p !");
		return "home/login";
	}
	
	//Ä‘Äƒng xuáº¥t
	@RequestMapping("logout") 
	public String logout(ModelMap model, HttpServletRequest req) {
		HttpSession user_session = req.getSession();
		user_session.removeAttribute("user");
		return "redirect:/home/index.htm";
	}
	
	//xem sáº£n pháº©m
	@RequestMapping("{id}")
	public String show(ModelMap model, @PathVariable("id") String id, HttpServletRequest req, HttpServletResponse response) {
		HttpSession user_session = req.getSession();
		user_session.getAttribute("user");
		if(user_session.getAttribute("user") == null) {
			model.addAttribute("isLogin", false);
		}
		if(user_session.getAttribute("user") != null) {
			model.addAttribute("isLogin", true);
		}
		Session session = factory.getCurrentSession();
		Product product = (Product) session.get(Product.class, id);
		
		model.addAttribute("product", product);
		model.addAttribute("products", getProducts());
		model.addAttribute("brands", getBrands());
		return "home/show";
	}
	
	//láº¥y danh sÃ¡ch sáº£n pháº©m
	@SuppressWarnings("unchecked")
	public List<Product> getProducts() {
		Session session = factory.getCurrentSession();
		String hql = "FROM Product";
		Query query = session.createQuery(hql);
		List<Product> list = query.list();
		return list;
	}
	
	//Ä‘Äƒng kÃ½ tk má»›i
	@RequestMapping(value="register", method=RequestMethod.GET)
	public String register(ModelMap model) {
		model.addAttribute("user", new User());
		return "home/register";
	}
	
	@RequestMapping(value="register", method=RequestMethod.POST)
	public String register1(ModelMap model, @ModelAttribute("user") User user, 
										   HttpServletRequest request, BindingResult errors) {
		String repassword = request.getParameter("repassword").trim();
		
		if(user.getUsername().trim().length() == 0) {
			errors.rejectValue("username", "user", "Vui lÃ²ng nháº­p tÃªn Ä‘Äƒng nháº­p !");
		}
		if(user.getPassword().trim().length() == 0) {
			errors.rejectValue("password", "user", "Vui lÃ²ng nháº­p máº­t kháº©u !");
		}
		if(user.getLastname().trim().length() == 0) {
			errors.rejectValue("lastname", "user", "Vui lÃ²ng nháº­p há»� !");
		}
		if(user.getFirstname().trim().length() == 0) {
			errors.rejectValue("firstname", "user", "Vui lÃ²ng nháº­p tÃªn !");
		}
		if(user.getEmail().trim().length() == 0) {
			errors.rejectValue("email", "user", "Vui lÃ²ng nháº­p email !");
		}
		if(user.getEmail().trim().length() != 0) {
			if(checkEmail(user.getEmail().trim()) == false) {
				errors.rejectValue("email", "user", "Email khÃ´ng há»£p lá»‡. Vui lÃ²ng nháº­p láº¡i email !");
				return "home/register";
			}
		}
		if(user.getPhone().trim().length() == 0) {
			errors.rejectValue("phone", "user", "Vui lÃ²ng nháº­p sá»‘ Ä‘iá»‡n thoáº¡i !");
		}
		if(user.getPhone().trim().length() != 0) {
			if(checkPhone(user.getPhone().trim()) == false) {
				errors.rejectValue("phone", "user", "Sá»‘ Ä‘iá»‡n thoáº¡i khÃ´ng há»£p lá»‡ !");
				return "home/register";
			}
		}
		if(user.getAddress().trim().length() == 0) {
			errors.rejectValue("address", "user", "Vui lÃ²ng nháº­p Ä‘á»‹a chá»‰ !");
		}
		if(errors.hasErrors()) {
			/*model.addAttribute("message", "Vui lÃ²ng kiá»ƒm tra lá»—i !");*/
		}
		if(repassword.trim().length() == 0) {
			model.addAttribute("message_pass", "Vui lÃ²ng xÃ¡c nháº­n máº­t kháº©u !");
			return "home/register";
		}
		else {
			if(repassword.trim().equals(user.getPassword().trim()) == false) {
				model.addAttribute("message_pass", "XÃ¡c nháº­n máº­t kháº©u khÃ´ng trÃ¹ng khá»›p, vui lÃ²ng nháº­p láº¡i !");
				return "home/register";
			}
			List<User> kiemtra = getUsers();
			for(User u : kiemtra) {
				if(user.getUsername().trim().equals(u.getUsername()) == true ) {
					errors.rejectValue("username", "user", "TÃªn Ä‘Äƒng nháº­p Ä‘Ã£ tá»“n táº¡i. Vui lÃ²ng nháº­p láº¡i !");
					return "home/register";
				}
			}
			Session session = factory.openSession();
			Transaction t = session.beginTransaction();
			try {
				user.setRole(false);
				session.save(user);
				t.commit();
				model.addAttribute("message", "Ä�Äƒng kÃ½ thÃ nh cÃ´ng !");
				mailer.send("showroomxemay.nhatduy@gmail.com", user.getEmail(), 
						"Ä�Ä‚NG KÃ� TÃ€I KHOáº¢N THÃ€NH CÃ”NG", 
						"ChÃºc má»«ng báº¡n Ä‘Ã£ Ä‘Äƒng kÃ½ tÃ i khoáº£n thÃ nh cÃ´ng táº¡i Showroom xe mÃ¡y Nháº­t Duy. "
						+ "Showroom xe mÃ¡y Nháº­t Duy xin cáº£m Æ¡n !");
				return "redirect:/home/login.htm";
			}
			catch(Exception e) {
				t.rollback();
				model.addAttribute("message", "Ä�Äƒng kÃ½ tháº¥t báº¡i !");
			}
			finally {
				session.close();
			}
		}
		return "home/register";
	}
	
	// Ä‘á»•i máº­t kháº©u
	@RequestMapping("changepass")
	public String changepass(ModelMap model, HttpServletRequest req, HttpServletResponse response) {
		HttpSession user_session = req.getSession();
		user_session.getAttribute("user");
		if(user_session.getAttribute("user") == null) {
			return "redirect:/home/index.htm";
		}
		return "home/changepass";
	}
	
	@RequestMapping("change")
	public String change(ModelMap model, HttpServletRequest req, HttpServletResponse response) {
		HttpSession user_session = req.getSession();
		User user = (User) user_session.getAttribute("user");
		if(user_session.getAttribute("user") == null) {
			return "redirect:/home/index.htm";
		}
		String oldpass = req.getParameter("oldpass");
		String newpass = req.getParameter("newpass");
		String renewpass = req.getParameter("renewpass");
		oldpass.trim();
		newpass.trim();
		renewpass.trim();
		
		if(oldpass.length() == 0) {
			model.addAttribute("message", "Vui lÃ²ng Ä‘iá»�n Ä‘á»§ thÃ´ng tin !");
			return "home/changepass";
		}
		if(newpass.length() == 0) {
			model.addAttribute("message", "Vui lÃ²ng Ä‘iá»�n Ä‘á»§ thÃ´ng tin !");
			return "home/changepass";
		}
		if(renewpass.length() == 0) {
			model.addAttribute("message", "Vui lÃ²ng Ä‘iá»�n Ä‘á»§ thÃ´ng tin !");
			return "home/changepass";
		}
		else {
			if(oldpass.equals(user.getPassword()) == false) {
				model.addAttribute("message", "Máº­t kháº©u cÅ© khÃ´ng Ä‘Ãºng !");
				return "home/changepass";
			}
			if(newpass.equals(renewpass) == false) {
				model.addAttribute("message", "XÃ¡c nháº­n máº­t kháº©u má»›i khÃ´ng chÃ­nh xÃ¡c !");
				return "home/changepass";
			}
			else {
				Session session = factory.openSession();
				Transaction t = session.beginTransaction();
				try {
					user.setPassword(newpass);
					session.update(user);
					t.commit();
					model.addAttribute("message", "Ä�á»•i máº­t kháº©u thÃ nh cÃ´ng !");
				}
				catch(Exception ex) {
					t.rollback();
					model.addAttribute("message", "Ä�á»•i máº­t kháº©u tháº¥t báº¡i !");
				}
				finally {
					session.close();
				}
			}
		}
		return "home/changepass";
	}
	
	//quÃªn máº­t kháº©u
	@RequestMapping(value="forgotpass", method=RequestMethod.GET)
	public String forgotpass(ModelMap model, HttpServletRequest req, HttpServletResponse response) {
		HttpSession user_session = req.getSession();
		if(user_session.getAttribute("user") != null) {
			return "redirect:/home/index.htm";
		}
		else {
			return "home/forgotpass";
		}
	}
	
	@RequestMapping(value="forgotpass", method=RequestMethod.POST)
	public String forgotpass1(ModelMap model, HttpServletRequest req) {
		String uname = req.getParameter("uname");
		String mail = req.getParameter("mail");
		uname.trim();
		mail.trim();
		if(uname.length() == 0) {
			model.addAttribute("message", "Vui lÃ²ng Ä‘iá»�n Ä‘á»§ thÃ´ng tin !");
			return "home/forgotpass";
		}
		if(mail.length() == 0) {
			model.addAttribute("message", "Vui lÃ²ng Ä‘iá»�n Ä‘á»§ thÃ´ng tin !");
			return "home/forgotpass";
		}
		else {
			if(checkEmail(mail) == false) {
				model.addAttribute("message", "Email báº¡n nháº­p chÆ°a Ä‘Ãºng Ä‘á»‹nh dáº¡ng !");
				return "home/forgotpass";
			}
			List<User> check = getUsers();
			for(User u : check) {
				if(uname.trim().equals(u.getUsername())== true && mail.trim().equals(u.getEmail()) == true) {
					Session session = factory.openSession();
					Transaction t = session.beginTransaction();
					try {
						String newpass = randomNumber();
						model.addAttribute("message", "Máº­t kháº©u cá»§a báº¡n Ä‘Ã£ Ä‘Æ°á»£c thay Ä‘á»•i. Vui lÃ²ng kiá»ƒm tra Email cá»§a báº¡n !");
						mailer.send("showroomxemay.nhatduy@gmail.com", mail, 
								"THÃ”NG BÃ�O Tá»ª SHOWROOM XE MÃ�Y NHáº¬T DUY", 
								"Máº­t kháº©u cá»§a báº¡n Ä‘Ã£ Ä‘Æ°á»£c Ä‘áº·t vá»� máº·c Ä‘á»‹nh lÃ : " + newpass + " . Vui lÃ²ng Ä‘Äƒng nháº­p láº¡i Ä‘á»ƒ thay Ä‘á»•i máº­t kháº©u má»›i. "
								+ "Showroom xe mÃ¡y Nháº­t Duy xin cáº£m Æ¡n !");
						u.setPassword(newpass);
						session.update(u);
						t.commit();
						return "home/forgotpass";
					}
					catch(Exception ex) {
						t.rollback();
						return "home/forgotpass";
					}
					finally {
						session.close();
					}
				}
			}
			model.addAttribute("message", "Username hoáº·c Email chÆ°a Ä‘Ãºng. Vui lÃ²ng kiá»ƒm tra láº¡i !");
		}
		return "home/forgotpass";
	}
	
	// hÃ m kiá»ƒm tra email há»£p lá»‡
	public Boolean checkEmail(String email) {
	        String emailPattern = "^[\\w!#$%&â€™*+/=?`{|}~^-]+(?:\\.[\\w!#$%&â€™*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
	        Pattern regex = Pattern.compile(emailPattern);
	        Matcher matcher = regex.matcher(email);
	        if (matcher.find()) {
	            return true;
	        } else {
	            return false;
	        }
	}
	
	// hÃ m kiá»ƒm tra sÄ‘t
	public Boolean checkPhone(String phone) {
		String phonePattern = "[0]{1}[35789]{1}[0-9]{8}";
		Pattern regex = Pattern.compile(phonePattern);
		Matcher matcher = regex.matcher(phone);
		if(matcher.find()) {
			return true;
		}
		else {
			return false;
		}
	}
	
	//sinh sá»‘ ngáº«u nhiÃªn
	public String randomNumber() {
		String randNumber = "";
		Random rd = new Random();
		for(int i = 0; i<8; i++) {
			Integer number = rd.nextInt(9);
			randNumber += number.toString();
		}
		return randNumber;
	}
	
	//láº¥y danh sÃ¡ch user
	@SuppressWarnings("unchecked")
	public List<User> getUsers() {
		Session session = factory.getCurrentSession();
		String hql = "FROM User";
		Query query = session.createQuery(hql);
		List<User> list = query.list();
		return list;
	}
	
	//mua hÃ ng
	@RequestMapping(value="order/{id}", method=RequestMethod.GET)
	public String order(ModelMap model, @PathVariable("id") String id, HttpServletRequest req) {
		HttpSession user_session = req.getSession();
		user_session.getAttribute("user");
		if(user_session.getAttribute("user") == null) {
			return "redirect:/home/index.htm";
		}
		Session session = factory.getCurrentSession();
		Product product = (Product) session.get(Product.class, id);
		
		model.addAttribute("product", product);
		model.addAttribute("products", getProducts());
		model.addAttribute("order", new Order());
		return "home/order";
	}
	
	@RequestMapping(value="order/{id}", method=RequestMethod.POST)
	public String order(ModelMap model, @ModelAttribute("order") Order order, BindingResult errors, 
									HttpServletRequest req, @PathVariable("id") String id) {
		HttpSession user_session = req.getSession();
		User user = (User) user_session.getAttribute("user");
		
		Session session1 = factory.getCurrentSession();
		Product product = (Product) session1.get(Product.class, id);
		
		model.addAttribute("product", product);
		model.addAttribute("products", getProducts());
		
		System.out.println(user.getUsername());
		System.out.println(product.getId());
		
		if(order.getDeliveryAddress().trim().length() == 0) {
			errors.rejectValue("deliveryAddress", "order", "Vui lÃ²ng nháº­p Ä‘á»‹a chá»‰ giao hÃ ng !");
		}
		if(order.getNote().trim().length() == 0) {
			order.setNote("");
		}
		if(order.getAmount() == null) {
			errors.rejectValue("amount", "order", "Vui lÃ²ng nháº­p sá»‘ lÆ°á»£ng !");
		}
		if(errors.hasErrors()) {
			/*model.addAttribute("message", "Vui lÃ²ng kiá»ƒm tra lá»—i !");
			System.out.println(errors);*/
		}
		else {
			Session session = factory.openSession();
			Session session2 = factory.openSession();
			Transaction t = session.beginTransaction();
			Transaction t2 = session2.beginTransaction();
			try {
				Integer newAmount = product.getAmount() - order.getAmount();
				product.setAmount(newAmount);
				session2.update(product);
				t2.commit();
			}
			catch(Exception ex) {
				t2.rollback();
			}
			try {
				Integer total = product.getPrice() * order.getAmount();
				order.setDate(new Date());
				order.setUser(user);
				order.setProduct(product);
				session.save(order);
				t.commit();
				model.addAttribute("message", "Ä�áº·t hÃ ng thÃ nh cÃ´ng !");
				String body = "Chi tiáº¿t Ä‘Æ¡n hÃ ng cá»§a báº¡n || " +
							  "NgÃ y: " + order.getDate() + " || " +
							  "TÃªn sáº£n pháº©m: " + product.getName() + " || " +
							  "HÃ£ng sáº£n xuáº¥t: " + product.getBrand().getName() + " || " +
							  "GiÃ¡ tiá»�n: " + product.getPrice() + " VNÄ� " + " || " +
							  "Sá»‘ lÆ°á»£ng : " + order.getAmount() + " || " +
							  "ThÃ nh tiá»�n : " + total + " VNÄ� " + " || " +
							  "TÃªn khÃ¡ch hÃ ng: " + user.getLastname() + " " + user.getFirstname() + " || " +
							  "Sá»‘ Ä‘iá»‡n thoáº¡i: " + user.getPhone() + " || " +
							  "Ä�á»‹a chá»‰ giao hÃ ng: " + order.getDeliveryAddress() + " || " +
							  "Ghi chÃº: " + order.getNote() + " || ";
				mailer.send("showroomxemay.nhatduy@gmail.com", user.getEmail(), 
						"Ä�áº¶T HÃ€NG THÃ€NH CÃ”NG Tá»ª SHOWROOM XE MÃ�Y NHáº¬T DUY", 
						body + "Cáº£m Æ¡n báº¡n Ä‘Ã£ Ä‘áº·t hÃ ng táº¡i website cá»§a chÃºng tÃ´i. Vui lÃ²ng truy cáº­p vÃ o giá»� hÃ ng cá»§a báº¡n Ä‘á»ƒ xem chi tiáº¿t Ä‘Æ¡n hÃ ng cá»§a báº¡n. "
						+ "Showroom xe mÃ¡y Nháº­t Duy xin cáº£m Æ¡n !");
				return "redirect:/home/success.htm";	
			}
			catch(Exception e) {
				t2.rollback();
				t.rollback();
				model.addAttribute("message", "Ä�áº·t hÃ ng tháº¥t báº¡i !");
			}
			finally {
				session2.close();
				session.close();
			}
		}
		return "home/order";
	}
	
	@RequestMapping("success")
	public String success(ModelMap model, HttpServletRequest req, HttpServletResponse response) {
		HttpSession user_session = req.getSession();
		if(user_session.getAttribute("user") == null) {
			return "redirect:/home/index.htm";
		}
		return "home/success";
	}
	
	//xem sáº£n pháº©m theo hÃ£ng
	@RequestMapping("finding/{id}")
	public String productsOfBrand(ModelMap model, @PathVariable("id") String id, 
											HttpServletRequest req, HttpServletResponse response) {
		HttpSession user_session = req.getSession();
		user_session.getAttribute("user");
		if(user_session.getAttribute("user") == null) {
			model.addAttribute("isLogin", false);
		}
		if(user_session.getAttribute("user") != null) {
			model.addAttribute("isLogin", true);
		}
		Session session = factory.getCurrentSession();
		String hql = "FROM Product p WHERE p.brand.id = '" + id + "'";
		Query query = session.createQuery(hql);
		List<Product> list = query.list();
		model.addAttribute("products", list);
		model.addAttribute("ads", getAds());
		model.addAttribute("brands", getBrands());
		model.addAttribute("types", getTypes());
		return "home/finding";
	}
	
	//tÃ¬m kiáº¿m sáº£n pháº©m
	@RequestMapping("search")
	public String search(ModelMap model, HttpServletRequest req, HttpServletResponse response) {
		HttpSession user_session = req.getSession();
		user_session.getAttribute("user");
		if(user_session.getAttribute("user") == null) {
			model.addAttribute("isLogin", false);
		}
		if(user_session.getAttribute("user") != null) {
			model.addAttribute("isLogin", true);
		}
		String keyword = req.getParameter("search");
		Session session = factory.getCurrentSession();
		String hql = "FROM Product p WHERE p.name LIKE '%" + keyword + "%'" ;
		Query query = session.createQuery(hql);
		List<Product> list = query.list();
		model.addAttribute("products", list);
		model.addAttribute("ads", getAds());
		model.addAttribute("brands", getBrands());
		return "home/search";
	}
	
	//Giá»� hÃ ng cá»§a tÃ´i
	@RequestMapping("mycart")
	public String mycart(ModelMap model, HttpServletRequest req, HttpServletResponse response) {
		HttpSession user_session = req.getSession();
		user_session.getAttribute("user");
		if(user_session.getAttribute("user") == null) {
			return "redirect:/home/login.htm";
		}
		User user = (User) user_session.getAttribute("user");
		Session session = factory.getCurrentSession();
		String hql = "FROM Order o WHERE o.user.username = '" + user.getUsername() + "'";
		Query query = session.createQuery(hql);
		List<Order> list = query.list();
		model.addAttribute("orders", list);
		return "home/mycart";
	}
	
}
