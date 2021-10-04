package ink.mastermind.AllINOne.security;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		if (request.getContentType().equals(MediaType.APPLICATION_JSON_UTF8_VALUE)
				|| request.getContentType().equals(MediaType.APPLICATION_JSON_VALUE)) {
			ObjectMapper mapper = new ObjectMapper();
			UsernamePasswordAuthenticationToken authRequest = null;
			try (InputStream is = request.getInputStream()) {
				Map<String, String> authenticationBean = mapper.readValue(is, Map.class);
				authRequest = new UsernamePasswordAuthenticationToken(authenticationBean.get("userName"),
						authenticationBean.get("password"));
			} catch (IOException e) {
				e.printStackTrace();
				authRequest = new UsernamePasswordAuthenticationToken("", "");
			} finally {
				setDetails(request, authRequest);
				return this.getAuthenticationManager().authenticate(authRequest);
			}
		} else {
			return super.attemptAuthentication(request, response);
		}
	}
}