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
	
	//l??????y danh s????ch h????ng
	@SuppressWarnings("unchecked")
	public List<Brand> getBrands() {
		Session session = factory.getCurrentSession();
		String hql = "FROM Brand";
		Query query = session.createQuery(hql);
		List<Brand> list = query.list();
		return list;
	}
	
	//l??????y lo??????i xe
	public List<String> getTypes() {
		List<String> list = new ArrayList<>();
		list.add("Xe tay ga");
		list.add("Xe s???????");
		list.add("Xe c????n tay");
		return list;
	}
	
	//l??????y danh s????ch qu??????ng c????o
	@SuppressWarnings("unchecked")
	public List<Advertisement> getAds() {
		Session session = factory.getCurrentSession();
		String hql = "FROM Advertisement";
		Query query = session.createQuery(hql);
		List<Advertisement> list = query.list();
		return list;
	}
	
	//?????????ng nh??????p
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
					model.addAttribute("message2", "Sai m??????t kh??????u !");
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
		model.addAttribute("message1", "Sai t????n ?????????ng nh??????p !");
		return "home/login";
	}
	
	//?????????ng xu??????t
	@RequestMapping("logout") 
	public String logout(ModelMap model, HttpServletRequest req) {
		HttpSession user_session = req.getSession();
		user_session.removeAttribute("user");
		return "redirect:/home/index.htm";
	}
	
	//xem s??????n ph??????m
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
	
	//l??????y danh s????ch s??????n ph??????m
	@SuppressWarnings("unchecked")
	public List<Product> getProducts() {
		Session session = factory.getCurrentSession();
		String hql = "FROM Product";
		Query query = session.createQuery(hql);
		List<Product> list = query.list();
		return list;
	}
	
	//?????????ng k???? tk m???????i
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
			errors.rejectValue("username", "user", "Vui l????ng nh??????p t????n ?????????ng nh??????p !");
		}
		if(user.getPassword().trim().length() == 0) {
			errors.rejectValue("password", "user", "Vui l????ng nh??????p m??????t kh??????u !");
		}
		if(user.getLastname().trim().length() == 0) {
			errors.rejectValue("lastname", "user", "Vui l????ng nh??????p h??????? !");
		}
		if(user.getFirstname().trim().length() == 0) {
			errors.rejectValue("firstname", "user", "Vui l????ng nh??????p t????n !");
		}
		if(user.getEmail().trim().length() == 0) {
			errors.rejectValue("email", "user", "Vui l????ng nh??????p email !");
		}
		if(user.getEmail().trim().length() != 0) {
			if(checkEmail(user.getEmail().trim()) == false) {
				errors.rejectValue("email", "user", "Email kh????ng h??????p l???????. Vui l????ng nh??????p l??????i email !");
				return "home/register";
			}
		}
		if(user.getPhone().trim().length() == 0) {
			errors.rejectValue("phone", "user", "Vui l????ng nh??????p s??????? ?????i???????n tho??????i !");
		}
		if(user.getPhone().trim().length() != 0) {
			if(checkPhone(user.getPhone().trim()) == false) {
				errors.rejectValue("phone", "user", "S??????? ?????i???????n tho??????i kh????ng h??????p l??????? !");
				return "home/register";
			}
		}
		if(user.getAddress().trim().length() == 0) {
			errors.rejectValue("address", "user", "Vui l????ng nh??????p ????????????a ch??????? !");
		}
		if(errors.hasErrors()) {
			/*model.addAttribute("message", "Vui l????ng ki??????m tra l???????i !");*/
		}
		if(repassword.trim().length() == 0) {
			model.addAttribute("message_pass", "Vui l????ng x????c nh??????n m??????t kh??????u !");
			return "home/register";
		}
		else {
			if(repassword.trim().equals(user.getPassword().trim()) == false) {
				model.addAttribute("message_pass", "X????c nh??????n m??????t kh??????u kh????ng tr????ng kh???????p, vui l????ng nh??????p l??????i !");
				return "home/register";
			}
			List<User> kiemtra = getUsers();
			for(User u : kiemtra) {
				if(user.getUsername().trim().equals(u.getUsername()) == true ) {
					errors.rejectValue("username", "user", "T????n ?????????ng nh??????p ????????? t???????n t??????i. Vui l????ng nh??????p l??????i !");
					return "home/register";
				}
			}
			Session session = factory.openSession();
			Transaction t = session.beginTransaction();
			try {
				user.setRole(false);
				session.save(user);
				t.commit();
				model.addAttribute("message", "?????????ng k???? th????nh c????ng !");
				mailer.send("showroomxemay.nhatduy@gmail.com", user.getEmail(), 
						"??????????NG K????? T?????I KHO??????N TH?????NH C?????NG", 
						"Ch????c m??????ng b??????n ????????? ?????????ng k???? t????i kho??????n th????nh c????ng t??????i Showroom xe m????y Nh??????t Duy. "
						+ "Showroom xe m????y Nh??????t Duy xin c??????m ????n !");
				return "redirect:/home/login.htm";
			}
			catch(Exception e) {
				t.rollback();
				model.addAttribute("message", "?????????ng k???? th??????t b??????i !");
			}
			finally {
				session.close();
			}
		}
		return "home/register";
	}
	
	// ????????????i m??????t kh??????u
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
			model.addAttribute("message", "Vui l????ng ?????i???????n ??????????? th????ng tin !");
			return "home/changepass";
		}
		if(newpass.length() == 0) {
			model.addAttribute("message", "Vui l????ng ?????i???????n ??????????? th????ng tin !");
			return "home/changepass";
		}
		if(renewpass.length() == 0) {
			model.addAttribute("message", "Vui l????ng ?????i???????n ??????????? th????ng tin !");
			return "home/changepass";
		}
		else {
			if(oldpass.equals(user.getPassword()) == false) {
				model.addAttribute("message", "M??????t kh??????u c???? kh????ng ?????????ng !");
				return "home/changepass";
			}
			if(newpass.equals(renewpass) == false) {
				model.addAttribute("message", "X????c nh??????n m??????t kh??????u m???????i kh????ng ch????nh x????c !");
				return "home/changepass";
			}
			else {
				Session session = factory.openSession();
				Transaction t = session.beginTransaction();
				try {
					user.setPassword(newpass);
					session.update(user);
					t.commit();
					model.addAttribute("message", "????????????i m??????t kh??????u th????nh c????ng !");
				}
				catch(Exception ex) {
					t.rollback();
					model.addAttribute("message", "????????????i m??????t kh??????u th??????t b??????i !");
				}
				finally {
					session.close();
				}
			}
		}
		return "home/changepass";
	}
	
	//qu????n m??????t kh??????u
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
			model.addAttribute("message", "Vui l????ng ?????i???????n ??????????? th????ng tin !");
			return "home/forgotpass";
		}
		if(mail.length() == 0) {
			model.addAttribute("message", "Vui l????ng ?????i???????n ??????????? th????ng tin !");
			return "home/forgotpass";
		}
		else {
			if(checkEmail(mail) == false) {
				model.addAttribute("message", "Email b??????n nh??????p ch????a ?????????ng ????????????nh d??????ng !");
				return "home/forgotpass";
			}
			List<User> check = getUsers();
			for(User u : check) {
				if(uname.trim().equals(u.getUsername())== true && mail.trim().equals(u.getEmail()) == true) {
					Session session = factory.openSession();
					Transaction t = session.beginTransaction();
					try {
						String newpass = randomNumber();
						model.addAttribute("message", "M??????t kh??????u c??????a b??????n ????????? ???????????????c thay ????????????i. Vui l????ng ki??????m tra Email c??????a b??????n !");
						mailer.send("showroomxemay.nhatduy@gmail.com", mail, 
								"TH?????NG B?????O T?????? SHOWROOM XE M?????Y NH??????T DUY", 
								"M??????t kh??????u c??????a b??????n ????????? ???????????????c ???????????t v??????? m??????c ????????????nh l????: " + newpass + " . Vui l????ng ?????????ng nh??????p l??????i ??????????? thay ????????????i m??????t kh??????u m???????i. "
								+ "Showroom xe m????y Nh??????t Duy xin c??????m ????n !");
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
			model.addAttribute("message", "Username ho??????c Email ch????a ?????????ng. Vui l????ng ki??????m tra l??????i !");
		}
		return "home/forgotpass";
	}
	
	// h????m ki??????m tra email h??????p l???????
	public Boolean checkEmail(String email) {
	        String emailPattern = "^[\\w!#$%&????????*+/=?`{|}~^-]+(?:\\.[\\w!#$%&????????*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
	        Pattern regex = Pattern.compile(emailPattern);
	        Matcher matcher = regex.matcher(email);
	        if (matcher.find()) {
	            return true;
	        } else {
	            return false;
	        }
	}
	
	// h????m ki??????m tra s?????t
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
	
	//sinh s??????? ng??????u nhi????n
	public String randomNumber() {
		String randNumber = "";
		Random rd = new Random();
		for(int i = 0; i<8; i++) {
			Integer number = rd.nextInt(9);
			randNumber += number.toString();
		}
		return randNumber;
	}
	
	//l??????y danh s????ch user
	@SuppressWarnings("unchecked")
	public List<User> getUsers() {
		Session session = factory.getCurrentSession();
		String hql = "FROM User";
		Query query = session.createQuery(hql);
		List<User> list = query.list();
		return list;
	}
	
	//mua h????ng
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
			errors.rejectValue("deliveryAddress", "order", "Vui l????ng nh??????p ????????????a ch??????? giao h????ng !");
		}
		if(order.getNote().trim().length() == 0) {
			order.setNote("");
		}
		if(order.getAmount() == null) {
			errors.rejectValue("amount", "order", "Vui l????ng nh??????p s??????? l??????????ng !");
		}
		if(errors.hasErrors()) {
			/*model.addAttribute("message", "Vui l????ng ki??????m tra l???????i !");
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
				model.addAttribute("message", "???????????t h????ng th????nh c????ng !");
				String body = "Chi ti??????t ?????????n h????ng c??????a b??????n || " +
							  "Ng????y: " + order.getDate() + " || " +
							  "T????n s??????n ph??????m: " + product.getName() + " || " +
							  "H????ng s??????n xu??????t: " + product.getBrand().getName() + " || " +
							  "Gi???? ti???????n: " + product.getPrice() + " VN????? " + " || " +
							  "S??????? l??????????ng : " + order.getAmount() + " || " +
							  "Th????nh ti???????n : " + total + " VN????? " + " || " +
							  "T????n kh????ch h????ng: " + user.getLastname() + " " + user.getFirstname() + " || " +
							  "S??????? ?????i???????n tho??????i: " + user.getPhone() + " || " +
							  "????????????a ch??????? giao h????ng: " + order.getDeliveryAddress() + " || " +
							  "Ghi ch????: " + order.getNote() + " || ";
				mailer.send("showroomxemay.nhatduy@gmail.com", user.getEmail(), 
						"???????????T H?????NG TH?????NH C?????NG T?????? SHOWROOM XE M?????Y NH??????T DUY", 
						body + "C??????m ????n b??????n ????????? ???????????t h????ng t??????i website c??????a ch????ng t????i. Vui l????ng truy c??????p v????o gi??????? h????ng c??????a b??????n ??????????? xem chi ti??????t ?????????n h????ng c??????a b??????n. "
						+ "Showroom xe m????y Nh??????t Duy xin c??????m ????n !");
				return "redirect:/home/success.htm";	
			}
			catch(Exception e) {
				t2.rollback();
				t.rollback();
				model.addAttribute("message", "???????????t h????ng th??????t b??????i !");
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
	
	//xem s??????n ph??????m theo h????ng
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
	
	//t????m ki??????m s??????n ph??????m
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
	
	//Gi??????? h????ng c??????a t????i
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
