package de.envite.proa.security;

import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.ForbiddenException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.annotation.Priority;
import jakarta.ws.rs.core.SecurityContext;
import java.util.Arrays;

@RolesAllowedIfWebVersion({})
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class RolesAllowedIfWebVersionInterceptor {

    @Inject
    @ConfigProperty(name = "app.mode", defaultValue = "desktop")
    String appMode;

    @Inject
    SecurityContext securityContext;

    @AroundInvoke
    public Object checkRoles(InvocationContext context) throws Exception {

        RolesAllowedIfWebVersion annotation = context.getMethod().getAnnotation(RolesAllowedIfWebVersion.class);
        if (annotation == null) {
            annotation = context.getTarget().getClass().getAnnotation(RolesAllowedIfWebVersion.class);
        }

        if (appMode.equals("web")) {
            if (annotation != null) {
                String[] rolesAllowed = annotation.value();
                boolean hasRole = Arrays.stream(rolesAllowed)
                        .anyMatch(role -> securityContext.isUserInRole(role));

                if (!hasRole) {
                    throw new ForbiddenException("User does not have permission to access this resource.");
                }
            }
        }

        return context.proceed();
    }
}
