package sgg.qin.listener;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * @Title:GlobalSessionListener
 * @Description:全局会话的 会话监听器
 * @author 张颖辉
 * @date 2017年9月4日下午9:43:31
 * @version 1.0
 */
public class GlobalSessionListener implements HttpSessionListener {

	@Override
	public void sessionCreated(HttpSessionEvent event) {
		HttpSession httpSession = event.getSession();
		httpSession.setMaxInactiveInterval(3*60*60);//过期时间暂定3小时
		System.out.println(">>>>>认证中心session 创建 id:"+httpSession.getId());

	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		HttpSession httpSession = event.getSession();
		System.out.println(">>>>>认证中心session 销毁 id:"+httpSession.getId());
	}

}
