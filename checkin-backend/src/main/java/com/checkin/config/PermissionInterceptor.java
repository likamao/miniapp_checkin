package com.checkin.config;

import com.checkin.entity.User;
import com.checkin.repository.UserRepository;
import com.checkin.service.PermissionService;
import com.checkin.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionService permissionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取Authorization头
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            sendErrorResponse(response, "Unauthorized: No token provided");
            return false;
        }

        // 解析token获取用户ID
        token = token.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            sendErrorResponse(response, "Unauthorized: Invalid token");
            return false;
        }

        // 获取用户信息
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            sendErrorResponse(response, "Unauthorized: User not found");
            return false;
        }

        // 检查权限
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        // 定义权限映射
        Map<String, String> permissionMap = new HashMap<>();
        permissionMap.put("GET /api/users", "user:read");
        permissionMap.put("POST /api/users", "user:create");
        permissionMap.put("PUT /api/users", "user:update");
        permissionMap.put("DELETE /api/users", "user:delete");

        // 检查是否需要权限验证
        String key = method + " " + requestURI;
        if (permissionMap.containsKey(key)) {
            String permissionCode = permissionMap.get(key);
            if (!permissionService.hasPermission(user, permissionCode)) {
                sendErrorResponse(response, "Forbidden: Insufficient permissions");
                return false;
            }
        }

        // 将用户信息存储到请求中，以便后续使用
        request.setAttribute("user", user);
        return true;
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.write("{\"error\": \"" + message + "\"}");
        out.flush();
        out.close();
    }
}