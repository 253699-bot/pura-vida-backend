package com.puravida.modules.notifications.application.port.in;

public interface OrderNotificationPort {

    void notifyOrderCreated(Integer orderId);

    void notifyOrderAccepted(Integer orderId, Integer clientId);

    void notifyOrderRejected(Integer orderId, Integer clientId, String reason);
}
