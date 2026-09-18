package org.example.medflow;

import org.mindrot.jbcrypt.BCrypt;

public class TestPwd {
    public static void main(String[] args) {
        // 明文密码
        String rawPassword = "admin123";
        // 生成 BCrypt 密文
        String encodedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        System.out.println(encodedPassword);
    }
}
