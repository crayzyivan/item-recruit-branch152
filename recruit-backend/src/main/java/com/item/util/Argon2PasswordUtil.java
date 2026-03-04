package com.item.util;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class Argon2PasswordUtil {
    // 使用Argon2id算法（推荐）
    private static final Argon2 ARGON2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    // 算法参数（可根据系统性能调整）
    private static final int ITERATIONS = 3;         // 时间成本（迭代次数）
    private static final int MEMORY = 65536;         // 内存成本（65536 KB = 64 MB）
    private static final int PARALLELISM = 4;        // 并行度（线程数，建议与CPU核心数匹配）

    /**
     * 加密密码
     * @param password 原始密码（字符串）
     * @return 加密后的哈希字符串（包含算法参数、盐值和哈希结果）
     */
    public static String encryptPassword(String password)  {
        // 将密码转换为char数组，使用后可清除内存
        char[] passwordChars = password.toCharArray();
        try {
            // 生成哈希（自动生成随机盐值）
            return ARGON2.hash(ITERATIONS, MEMORY, PARALLELISM, passwordChars);
        } finally {
            // 清除内存中的密码数据，防止泄露
            ARGON2.wipeArray(passwordChars);
        }
    }

    /**
     * 验证密码是否匹配
     * @param hashedPassword 加密后的哈希字符串
     * @param plainPassword 待验证的原始密码
     * @return 匹配返回true，否则返回false
     */
    public static boolean verifyPassword(String hashedPassword, String plainPassword)  {
        char[] passwordChars = plainPassword.toCharArray();
        try {
            // 验证哈希与密码是否匹配
            return ARGON2.verify(hashedPassword, passwordChars);
        } finally {
            // 清除内存中的密码数据
            ARGON2.wipeArray(passwordChars);
        }
    }
}

