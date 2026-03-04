package com.item.util;

import com.item.framework.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * JWT工具类
 * 提供JWT token的生成、验证、解析等功能
 */
@Component
public class JwtUtils {
    
    private static JwtConfig jwtConfig;
    
    @Resource
    public void setJwtConfig(JwtConfig jwtConfig) {
        JwtUtils.jwtConfig = jwtConfig;
    }

    /**
     * 生成JWT token
     * @param claims 额外的声明信息
     * @return JWT token字符串
     */
    public static String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setSubject(jwtConfig.getSubject())
                .addClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpirationTime()))
                .signWith(SignatureAlgorithm.HS256, jwtConfig.getSecretKey().getBytes())
                .compact();
    }

    /**
     * 验证JWT token
     * @param token JWT token字符串
     * @return 验证是否成功
     */
    public static boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(jwtConfig.getSecretKey().getBytes())
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解析JWT token获取Claims
     * @param token JWT token字符串
     * @return Claims对象
     */
    private static Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtConfig.getSecretKey().getBytes())
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从token中获取用户名
     * @param token JWT token字符串
     * @return 用户名
     */
    public static String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("user", String.class);
    }

    /**
     * 从token中获取邮箱
     * @param token JWT token字符串
     * @return 邮箱
     */
    public static String getEmailFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("email", String.class);
    }

    /**
     * 从token中获取role
     * @param token JWT token字符串
     * @return 邮箱
     */
    public static String getRole(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    /**
     * 检查token是否过期
     * @param token JWT token字符串
     * @return 是否过期
     */
    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 获取token的过期时间
     * @param token JWT token字符串
     * @return 过期时间
     */
    public static Date getExpirationDateFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration();
    }

    /**
     * 获取token的签发时间
     * @param token JWT token字符串
     * @return 签发时间
     */
    public static Date getIssuedDateFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getIssuedAt();
    }

    /**
     * 从Authorization header中提取token
     * @param authHeader Authorization header值
     * @return token字符串，如果格式不正确返回null
     */
    public static String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
} 