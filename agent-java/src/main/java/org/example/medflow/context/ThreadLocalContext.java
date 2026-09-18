package org.example.medflow.context;

public class ThreadLocalContext {
    // 替换原有的ThreadLocal为InheritableThreadLocal
    private static final ThreadLocal<Integer> PATIENT_ID = new InheritableThreadLocal<>();
    private static final ThreadLocal<String> ORDER_ID = new InheritableThreadLocal<>();

    public static void setPatientId(Integer patientId) {
        PATIENT_ID.set(patientId);
    }

    public static Integer getPatientId() {
        return PATIENT_ID.get();
    }

    public static void removePatientId() {
        PATIENT_ID.remove(); // 关键：使用后清理，避免内存泄漏
    }
    // 存储系统消息
    private static final ThreadLocal<String> SYSTEM_MESSAGE = new ThreadLocal<>();

    // 存储患者ID（你现在要用的）
//    private static final ThreadLocal<Integer> PATIENT_ID = new ThreadLocal<>();

    // 存储用户ID（以后可能用）
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    // 存储订单ID（以后可能用）
//    private static final ThreadLocal<String> ORDER_ID = new ThreadLocal<>();

    // ====================== 系统消息 ======================
    public static void setSystemMessage(String message) {
        SYSTEM_MESSAGE.set(message);
    }

    public static String getSystemMessage() {
        return SYSTEM_MESSAGE.get();
    }

    // ====================== 患者ID ======================
//    public static void setPatientId(Integer patientId) {
//        PATIENT_ID.set(patientId);
//    }
//
//    public static Integer getPatientId() {
//        return PATIENT_ID.get();
//    }

    // ====================== 用户ID ======================
    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    // ====================== 订单ID ======================
    // 存订单ID
    public static void setORDER_ID(String orderId) {
        ORDER_ID.set(orderId);
    }

    // 取订单ID
    public static String getORDER_ID() {
        return ORDER_ID.get();
    }

    // ====================== 清空（必须调用） ======================
    public static void clear() {
        SYSTEM_MESSAGE.remove();
        PATIENT_ID.remove();
        USER_ID.remove();
    }
}
