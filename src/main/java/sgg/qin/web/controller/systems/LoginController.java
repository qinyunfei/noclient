package sgg.qin.web.controller.systems;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import sgg.qin.framework.redis.RedisClientTemplate;
import sgg.qin.util.GlobalSessions;
import sgg.qin.util.SerializeUtil;

/**
 * 
 * @Description: sso单点登录服务器
 * @author: Qin YunFei
 * @date: 2017年12月15日 下午4:42:58
 * @version V1.0
 */
@Controller
@RequestMapping(value = "/page")
public class LoginController {
	@Autowired
	private RedisClientTemplate redisClientTemplate;

	// 认证中心 4大接口之一
	@RequestMapping(value = "/tologin")
	public String showLoginForm(@RequestParam(value = "service", required = false) String service,
			@CookieValue(value = "JSESSIONID", required = false) String sessionId,HttpSession session,Model model) {
		
		//首先判断全局会话是否存在 
		Object isLogin = session.getAttribute("isLogin");
		System.out.println("全局会话状态"+isLogin);
		if(isLogin!=null) {
				//表示用户已经登录
				//如果用户已经登录 生成零时token 60秒 放入redis
				String token = UUID.randomUUID().toString();
				redisClientTemplate.setex(SerializeUtil.serialize(token), 60, SerializeUtil.serialize(session.getAttribute("user")));
				//判定是否是业务系统
				if(StringUtils.isNotEmpty(service)) {
					//重定向到业务系统
					return "redirect:"+service+"?token="+token;
				}else {
					//如果不是业务系统返回认证中心首页
					return "index";
				}
			}else {
				//表示用户没有登录
				model.addAttribute("service", service);
				System.out.println("全局会话不存在 去到登录页面");
				return "login";
			}
	}

	/**
	 * 登录操作
	 * @param
	 */
	@RequestMapping(value = "/login")
	public String longing(String username, String password, @RequestParam(value = "service", required = false)String service,HttpSession session,Model model) {

		
		if ("admin".equals(username) && "1234".equals(password)) {
			//登录成功向session写入用户信息
			//redisClientTemplate.set(SerializeUtil.serialize(sessionId), SerializeUtil.serialize("admin"));
			
			//修改session的登录状态
			session.setAttribute("isLogin",true);
			//将用户信息放入session
			System.out.println("将用户信息放入session");
			session.setAttribute("user", username);
			
			//判定是否是业务系统
			if(StringUtils.isEmpty(service)) {
				return "index";
			}else {
				//生成零时token 60秒 放入redis
				String token = UUID.randomUUID().toString();
				redisClientTemplate.setex(SerializeUtil.serialize(token), 60, SerializeUtil.serialize(username));
				//重定向到业务系统
				return "redirect:"+service+"?token="+token;
			}
		}else {
			//登录失败返回登录页面
			model.addAttribute("error", "用户名或密码错误");
			model.addAttribute("service", service);
			return "login";
		}
	}

	/**
	 * 
	 * 此接口和用户游览器没有如何关系 纯粹是认证中心和业务系统间的通信
	 * 这里注意 不能使用session对象   除非业务系统发送jsessionid
	 */
	@RequestMapping("verify")
	@ResponseBody
	public Map<String, Object> verify(String token,String localld) {
		Map<String,Object> map=new HashMap<>();
		//判定token是否存在
		if(redisClientTemplate.exists(SerializeUtil.serialize(token))) {
			byte[] bs = redisClientTemplate.get(SerializeUtil.serialize(token));
			//认证成功返回用户信息
			String user = (String)SerializeUtil.deserialize(bs);
			map.put("status", 0);
			map.put("msg", "认证成功");
			map.put("user", user);
			//map.put("sessionkey", session.getId());
			//注册业务系统  单点注销使用 待做
			
			
			
			//删除token
			redisClientTemplate.del(SerializeUtil.serialize(token));
			return map;
		}
		map.put("status", 1);
		map.put("msg", "token认证失败");
		map.put("user", null);
		map.put("sessionkey", null);
		return map;
	}
	
	//单点注销带做
	@RequestMapping("loginOut")
	public String loginOut(String JSESSIONID) {
		/**
		 * gId参数说明直接从认证中心注销，有，说明从应用中来
		 * 接口首先取消当前全局登录会话，其次根据注册的已登录应用，通知它们进行登出操作
		 */
		
		return null;
	}
	
}
