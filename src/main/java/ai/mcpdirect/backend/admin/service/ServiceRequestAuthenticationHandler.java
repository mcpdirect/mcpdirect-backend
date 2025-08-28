package ai.mcpdirect.backend.admin.service;

import appnet.hstp.ServiceEngine;
import appnet.hstp.ServiceRequest;
import appnet.hstp.SimpleServiceResponseMessage;
import appnet.hstp.annotation.ServiceRequestAuthentication;
import ai.mcpdirect.backend.dao.entity.account.AIPortAccount;
import ai.mcpdirect.backend.util.AIPortAccessKeyValidator;

public class ServiceRequestAuthenticationHandler {
    protected ServiceEngine engine;
    @ServiceRequestAuthentication("auk")
    public AIPortAccount aukAuthenticate(
            ServiceRequest request, Class<?> authObjectType, int[] authRoles, boolean anonymous) {
        String hstpAuth = request.getRequestHeaders().getHeader("hstp-auth");
        if(hstpAuth==null) return null;
        String header = request.getRequestHeaders().getHeader("mcpdirect-device");
        int userDevice = header!=null?header.hashCode():0;
        Long userId = AIPortAccessKeyValidator.extractUserId(AIPortAccessKeyValidator.PREFIX_AUK, hstpAuth);
        if (userId != null) {
            AccessKeyServiceHandler a = engine.getServiceRequestHandler(AccessKeyServiceHandler.class);
            SimpleServiceResponseMessage<Boolean> ar = new SimpleServiceResponseMessage<>();
            a.verify(new AccessKeyServiceHandler.RequestOfVerify(userId, userDevice, hstpAuth), ar);
            if (Boolean.TRUE.equals(ar.data)) {
                return new AIPortAccount(userId);
            }
        }
        return null;
    }
}
