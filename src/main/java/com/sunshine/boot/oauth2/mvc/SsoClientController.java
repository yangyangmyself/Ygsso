package com.sunshine.boot.oauth2.mvc;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sunshine.boot.oauth2.dao.UserDao;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartframework.common.GlobalConfig;
import org.smartframework.common.utils.ExDigestUtils;
import org.smartframework.common.utils.ExHttpClientUtils;
import org.smartframework.common.utils.ExRedisUtils;
import org.smartframework.common.utils.ExRequestUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.sunshine.boot.oauth2.service.yhtjsServerimpl;
import com.sunshine.common.utils.ExControllerUtils;

@RestController
public class SsoClientController implements InitializingBean{
	protected final Logger log = LoggerFactory.getLogger(SsoClientController.class);
	 @Autowired
	    private Environment environment;
	@Autowired
private yhtjsServerimpl yh;
	private String ssoServerUrl = "";
	private String clientId = "";
	private String clientSecret ="";
	
	private String sso_authorization_callback_uri = "";
	
	  @Override
	    public void afterPropertiesSet() throws Exception {
		  GlobalConfig.environment=this.environment;
		this.ssoServerUrl = GlobalConfig.getConfigValue("sso.serverUrl", "http://localhost:8066/ssov6");
		this.clientId = GlobalConfig.getConfigValue("sso.clientId", "SheChe");
		this.clientSecret = GlobalConfig.getConfigValue("sso.clientSecret", "12345678");
		
	}
	
	protected String getSsoAuthCallbackUrl(String localServerUrl, String returnUrl){		
		return  localServerUrl + "/sso/check_code?return_url=" + returnUrl;
	}
	
	protected String getLocalServerUrl(HttpServletRequest request){
		String contextPath = request.getContextPath();
		String requestURI = request.getRequestURI();
		String localServerUrl = request.getRequestURL().toString();
		int indexPos = localServerUrl.indexOf(requestURI);
		if (indexPos > 0){
			localServerUrl = localServerUrl.substring(0, (indexPos + contextPath.length()));
		}
		/*String username=ExRedisUtils.get("Username");
		if(username!=null && !username.equals("")){
			ExRedisUtils.put("User:"+change(new Date())+":"+username, username);
		}

		ExRedisUtils.put("Usersjb", username);*/
		return localServerUrl;
	}
	protected String getAuthorizationData(){
		return "Basic " + ExDigestUtils.base64Encode(clientId + ":" + clientSecret);
	}

	protected String getAuthorizationData(String cid, String cs){
		return "Basic " + ExDigestUtils.base64Encode(cid + ":" + cs);
	}

	@RequestMapping(value="/sso/login", produces = "application/json")
	public void ssoLogin(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String localServerUrl =this.getLocalServerUrl(request);
		String returnUrl = localServerUrl + "/home.html";
//		String returnUrl = "http://192.168.110.131/index.html";
		this.sso_authorization_callback_uri = this.getSsoAuthCallbackUrl(localServerUrl, returnUrl);
		String ssoLoginUrl =  this.ssoServerUrl +  "/oauth/authorize?response_type=code&client_id=" + clientId + "&redirect_uri=" + this.sso_authorization_callback_uri;
  /*log.debug("localServerUrl:"+localServerUrl);
  log.debug("returnUrl:"+returnUrl);
  log.debug("ssoLoginUrl:"+ssoLoginUrl);*/
		response.sendRedirect(ssoLoginUrl);
	}

	public static String change(Date d){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(d);
		
	}
	@RequestMapping(value = "/oauth2/token", produces = "application/json")
	public Object processOauthToken(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Map paramMap = ExRequestUtils.buildParametersMap(request);
        log.debug("输入参数:" + paramMap);
        String grant_type = (String) paramMap.get("grant_type");
        String proxyUrl = (String) paramMap.get("http_proxy_url");

        Map resultMap = new HashMap();
        if (proxyUrl != null) {
            String respText = ExHttpClientUtils.doPost(proxyUrl, paramMap);
            if (respText != null) {
                resultMap.put("resultCode", 0);
                resultMap.put("resultMsg", "请求成功");
                resultMap.put("data", respText);
            } else {
                resultMap.put("resultCode", 8000);
                resultMap.put("resultMsg", "请求失败");
            }
        } else {
            //
            if ("password".equalsIgnoreCase(grant_type)) {

            } else if ("authorization_code".equalsIgnoreCase(grant_type)) {

            } else if ("refresh_token".equalsIgnoreCase(grant_type)) {

            }

        }

        return resultMap;
    }

	
    
    @RequestMapping(value="/sso/check_code", produces = "application/json")
	public Object code2token(HttpServletRequest request, HttpServletResponse response) throws IOException{
    	
    	String localServerUrl = getLocalServerUrl(request);
    	String requestUrl = this.ssoServerUrl + "/oauth/token";
    	String returnUrl = request.getParameter("return_url");
//            String returnUrl = "http://192.168.110.132/index.html";
    	String code = request.getParameter("code");
    	Map resultMap = new HashMap();
    	
    	if (!verifySignature(code)){
    		resultMap.put("resultCode", 36001);
    		resultMap.put("resultMsg", "数字签名验证失败");
    		return resultMap;
    	}
    	
		Map paramMap = new HashMap();		
		paramMap.put("grant_type", "authorization_code");
		paramMap.put("client_id", clientId);
		paramMap.put("code", code);
		paramMap.put("redirect_uri", this.getSsoAuthCallbackUrl(localServerUrl, returnUrl));
		
		Map headerMap = new HashMap();			
		headerMap.put("Authorization", getAuthorizationData());
		
		String resultText = ExHttpClientUtils.doPost(requestUrl, paramMap, headerMap);
		System.out.println(resultText);
		log.debug(resultText);
		
		
		if (resultText != null){
			resultMap = JSON.parseObject(resultText);
			if (resultMap.get("access_token") != null){
				String token = (String)resultMap.get("access_token");
				if (returnUrl == null){
					returnUrl = "/";
				}
				response.sendRedirect(returnUrl);
			}

			//expires_in = json.getIntValue("expires_in");
		}	
		String username=ExRedisUtils.get("Username");
		log.debug("UsernameSSO"+username);
		log.info("UsernameSSO"+username);
		if(username!=null && !username.equals("")){
			ExRedisUtils.hset("User:"+change(new Date())+":"+username,"7200",username);
		}

		ExRedisUtils.put("Usersjb", username);
		return resultMap;
	}
    
    protected boolean verifySignature(String payload){

        String[] parts = payload.split("\\.");
        String code, user, signature1;
        if (parts != null && parts.length == 3) {
            code = parts[0];
            user = parts[1];
            signature1 = parts[2];
        } else {
            code = payload;
            signature1 = "";
        }

        String data = code + "," + new String(Base64.encodeBase64(clientId.getBytes()));
        String signature2 = hmacSHA256(data, clientSecret);
        if (signature1 != null && signature1.equals(signature2)) {
            return true;
        }
        return false;
    }

    protected String hmacSHA256(String data, String password) {

        try {

            // 初始化HmacMD5摘要算法的密钥产生器
            KeyGenerator generator = KeyGenerator.getInstance("HmacSHA256");
            // 产生密钥
            SecretKey secretKey = generator.generateKey();
            // 还原密钥
            SecretKey secretKeySpec = new SecretKeySpec(password.getBytes(), "HmacSHA256");
            // 实例化Mac
            Mac mac = Mac.getInstance(secretKey.getAlgorithm());

            //初始化mac
            mac.init(secretKeySpec);

            //执行消息摘要
            byte[] digest = mac.doFinal(data.getBytes());

            //转为BASE64的字符串
            String signature = convertData(Base64.encodeBase64(digest));

            return signature;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return null;
    }

    private String convertData(byte[] input) {
        String temp = new String(input);
        temp = temp.split("=")[0]; // Remove any trailing '='s
        temp = temp.replace('+', '-'); // 62nd char of encoding
        temp = temp.replace('/', '_'); // 63rd char of encoding
        return temp;
    }

    @RequestMapping(value = "/sso/get_token", produces = "application/json")
    public Object getToken() {

        String token = null;
        int expires_in = 0;
        String requestUrl = this.ssoServerUrl + "/oauth/token";
        Map paramMap = new HashMap();
        paramMap.put("grant_type", "client_credentials");

		
		Map headerMap = new HashMap();		
		headerMap.put("Authorization", getAuthorizationData());
		String resultText = ExHttpClientUtils.doPost(requestUrl, paramMap, headerMap);
		
		Map resultMap = new HashMap();
		if (resultText != null){
			resultMap = JSON.parseObject(resultText);
		}
		return resultMap;
	}
	
	
    @RequestMapping(value="/oauth2/tokeninfo", produces = "application/json")
	public Object checkToken(HttpServletRequest request){
		String requestUrl = this.ssoServerUrl + "/oauth/check_token";
		String token = request.getParameter("access_token");
		// =============可以增加Token获取客户端信息=========
		
		// ============================================
		Map paramMap = new HashMap();
		paramMap.put("token", token);
		Map headerMap = new HashMap();		
		headerMap.put("Authorization", getAuthorizationData());
		
		String resultText = ExHttpClientUtils.doPost(requestUrl, paramMap, headerMap);
		//log.info("checkToken：" + resultText);
		Map resultMap = new HashMap();
		if (resultText != null){
			resultMap = JSON.parseObject(resultText);
		}
		if (resultMap.containsKey("user_name")) {
			resultMap.put("username", resultMap.get("user_name"));
			resultMap.put("expires_in", resultMap.get("exp"));
			resultMap.put("appId", resultMap.get("client_id"));
			resultMap.put("lxdh", resultMap.get("lxdh1"));
			
		}
		
		Set keys=ExRedisUtils.getKeys("User:"+change(new Date())+":*");
		int zxs=keys.size();
		Map sqs=yh.yhsqs();
		Map zdls=yh.yhzdls();				
		List isShow1=new ArrayList<>();
				isShow1=yh.isShow(String.valueOf(resultMap.get("username")));
				String isShow=null;
				if(isShow1.isEmpty()){
					isShow=null;
				}else{
					isShow=String.valueOf(((Map)isShow1.get(0)).get("menuoperation"));
				}
		Long zs= 1L;
		String zdl=String.valueOf(zdls.get("zdls"));
		if(ExRedisUtils.get("logintotal") == null)
			ExRedisUtils.put("logintotal", "120000");
		int zfwl=Integer.parseInt(ExRedisUtils.get("logintotal"))+Integer.parseInt(zdl);
		resultMap.put("sqs", sqs.get("loginCount"));
		resultMap.put("zfwl",zfwl);
		resultMap.put("yhzxs",zxs);
		resultMap.put("isShow", isShow);
		
		
		return resultMap;
	}
	
	

    @RequestMapping(value = "/sso/refresh_token", produces = "application/json")
    public Object refreshToken(String token) {

        String requestUrl = this.ssoServerUrl + "/oauth/check_token";
        Map paramMap = new HashMap();
        paramMap.put("refresh_token", token);
        paramMap.put("grant_type", "refresh_token");

        Map headerMap = new HashMap();
        headerMap.put("Authorization", getAuthorizationData());

        String resultText = ExHttpClientUtils.doPost(requestUrl, paramMap, headerMap);

        Map resultMap = new HashMap();
        if (resultText != null) {
            resultMap = JSON.parseObject(resultText);
        }
        return resultMap;
    }

    @RequestMapping(value = "/sso/ssllogin")
    public void SSLLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String localServerUrl = this.getLocalServerUrl(request);
        String returnUrl = localServerUrl + "/home.html";
        this.sso_authorization_callback_uri = this.getSsoAuthCallbackUrl(localServerUrl, returnUrl);
        String ssoLoginUrl = this.ssoServerUrl + "/oauth/authorize?response_type=code&client_id=" + clientId + "&redirect_uri=" + this.sso_authorization_callback_uri;
        response.sendRedirect(ssoLoginUrl);
    }




}
