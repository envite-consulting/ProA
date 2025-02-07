package de.envite.proa.security;

import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.SecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RolesAllowedIfWebVersionInterceptorTest {

    private static final String USER_ROLE = "User";
    private static final String WEB_MODE = "web";
    private static final String DESKTOP_MODE = "desktop";
    private static final String TEST_METHOD_WITH_ANNOTATION = "methodWithRolesAllowedIfWebVersion";
    private static final String TEST_METHOD_WITHOUT_ANNOTATION = "methodWithoutRolesAllowedIfWebVersion";
    private static final String TEST_METHOD_IN_CLASS = "insideMethodWithoutAnnotation";

    @Mock
    SecurityContext securityContext;

    @Mock
    InvocationContext invocationContext;

    @Mock
    Object target;

    @InjectMocks
    RolesAllowedIfWebVersionInterceptor interceptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCheckRoles_userHasRole_webMode_shouldProceed() throws Exception {
        interceptor.appMode = WEB_MODE;
        Method method = this.getClass().getDeclaredMethod(TEST_METHOD_WITH_ANNOTATION);
        when(invocationContext.getMethod()).thenReturn(method);
        when(securityContext.isUserInRole(USER_ROLE)).thenReturn(true);

        interceptor.checkRoles(invocationContext);

        verify(invocationContext).proceed();
    }

    @Test
    void testCheckRoles_userDoesNotHaveRole_webMode_shouldThrowForbiddenException() throws Exception {
        interceptor.appMode = WEB_MODE;
        Method method = this.getClass().getDeclaredMethod(TEST_METHOD_WITH_ANNOTATION);
        when(invocationContext.getMethod()).thenReturn(method);
        when(securityContext.isUserInRole(anyString())).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> interceptor.checkRoles(invocationContext));
    }

    @Test
    void testCheckRoles_desktopMode_shouldProceed() throws Exception {
        interceptor.appMode = DESKTOP_MODE;
        Method method = this.getClass().getDeclaredMethod(TEST_METHOD_WITH_ANNOTATION);
        when(invocationContext.getMethod()).thenReturn(method);

        interceptor.checkRoles(invocationContext);

        verify(invocationContext).proceed();
    }

    @Test
    void testCheckRoles_noAnnotationOnMethodOrClass_shouldProceed() throws Exception {
        interceptor.appMode = WEB_MODE;
        Method method = this.getClass().getDeclaredMethod(TEST_METHOD_WITHOUT_ANNOTATION);

        when(invocationContext.getTarget()).thenReturn(target);
        when(invocationContext.getMethod()).thenReturn(method);

        interceptor.checkRoles(invocationContext);

        verify(invocationContext).proceed();
    }

    @Test
    void testCheckRoles_noAnnotationOnMethod_shouldProceedIfClassHasAnnotation() throws Exception {
        interceptor.appMode = WEB_MODE;

        TestClassWithAnnotation testClassWithAnnotation = new TestClassWithAnnotation();
        Method method = testClassWithAnnotation.getClass().getDeclaredMethod(TEST_METHOD_IN_CLASS);
        when(invocationContext.getMethod()).thenReturn(method);
        when(invocationContext.getTarget()).thenReturn(testClassWithAnnotation);

        when(securityContext.isUserInRole(USER_ROLE)).thenReturn(true);

        interceptor.checkRoles(invocationContext);

        verify(invocationContext).proceed();
    }

    @RolesAllowedIfWebVersion({USER_ROLE})
    private void methodWithRolesAllowedIfWebVersion() {}

    private void methodWithoutRolesAllowedIfWebVersion() {}

    @RolesAllowedIfWebVersion({USER_ROLE})
    private static class TestClassWithAnnotation {
        private void insideMethodWithoutAnnotation() {}
    }
}
