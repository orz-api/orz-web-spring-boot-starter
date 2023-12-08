package orz.springboot.web;

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import orz.springboot.web.annotation.OrzWebApi;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static orz.springboot.base.description.OrzDescriptionUtils.desc;

@Slf4j
@Component
public class OrzWebMvcRegistrations implements WebMvcRegistrations {
    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new Mapping();
    }

    private static class Mapping extends RequestMappingHandlerMapping {
        @Override
        protected RequestMappingInfo getMappingForMethod(@Nonnull Method method, @Nonnull Class<?> handlerType) {
            var apiAnnotation = handlerType.getAnnotation(OrzWebApi.class);
            if (apiAnnotation != null) {
                if (!"request".equals(method.getName())) {
                    if (log.isDebugEnabled()) {
                        log.debug(desc("getMappingForMethod skipped", "beanClass", handlerType, "method", method));
                    }
                    return null;
                }

                var scope = OrzWebUtils.getScope(handlerType);
                checkWebApiBean(handlerType, scope, method, apiAnnotation);

                var idempotent = apiAnnotation.idempotent();
                var methodArray = new RequestMethod[]{idempotent ? RequestMethod.PUT : RequestMethod.POST};

                return RequestMappingInfo
                        .paths(buildPath(scope, apiAnnotation))
                        .methods(methodArray)
                        .options(getBuilderConfiguration())
                        .build();
            }
            return super.getMappingForMethod(method, handlerType);
        }

        private static String buildPath(String scope, OrzWebApi annotation) {
            var builder = new StringBuilder();
            builder.append("/").append(scope).append("/").append(annotation.domain()).append("/");
            if (StringUtils.isNotBlank(annotation.resource())) {
                builder.append(annotation.resource());
            }
            builder.append(annotation.action());
            builder.append("V").append(annotation.variant());
            return builder.toString();
        }

        private static void checkWebApiBean(Class<?> beanClass, String scope, Method method, OrzWebApi annotation) {
            if (Arrays.stream(beanClass.getDeclaredMethods()).noneMatch(m -> "request".equals(m.getName()))) {
                throw new FatalBeanException(desc("@OrzWebApi missing request method", "beanClass", beanClass));
            }
            if (StringUtils.isBlank(scope)) {
                throw new FatalBeanException(desc("@OrzWebApi scope is blank", "beanClass", beanClass));
            }
            if (StringUtils.isBlank(annotation.domain())) {
                throw new FatalBeanException(desc("@OrzWebApi domain is blank", "beanClass", beanClass));
            }
            if (StringUtils.isBlank(annotation.action())) {
                throw new FatalBeanException(desc("@OrzWebApi action is blank", "beanClass", beanClass));
            }
            var expectClassName = annotation.domain()
                    + StringUtils.defaultIfBlank(annotation.resource(), "")
                    + annotation.action()
                    + "V" + annotation.variant()
                    + "Api";
            if (!expectClassName.equals(beanClass.getSimpleName())) {
                throw new FatalBeanException(desc("@OrzWebApi class name is invalid", "beanClass", beanClass.getSimpleName(), "expectClassName", expectClassName));
            }
            if (method.getModifiers() != Modifier.PUBLIC) {
                throw new FatalBeanException(desc("@OrzWebApi request method is not public", "beanClass", beanClass));
            }
            if (method.getParameters() == null || method.getParameters().length != 1) {
                throw new FatalBeanException(desc("@OrzWebApi request method parameter is invalid", "beanClass", beanClass));
            }
            var parameter = method.getParameters()[0];
            if (!parameter.isAnnotationPresent(Validated.class)) {
                throw new FatalBeanException(desc("@OrzWebApi request method parameter missing @Validated annotation", "beanClass", beanClass));
            }
            if (!parameter.isAnnotationPresent(RequestBody.class)) {
                throw new FatalBeanException(desc("@OrzWebApi request method parameter missing @RequestBody annotation", "beanClass", beanClass));
            }
            var parameterClass = parameter.getType();
            if (parameterClass.isAnnotation() || parameterClass.isArray() || parameterClass.isEnum() || parameterClass.isInterface() || parameterClass.isPrimitive()) {
                throw new FatalBeanException(desc("@OrzWebApi request method parameter class is invalid", "beanClass", beanClass, "parameterClass", parameterClass));
            }
            if (!(expectClassName + "Req").equals(parameterClass.getSimpleName())) {
                throw new FatalBeanException(desc("@OrzWebApi request method parameter class name is invalid", "beanClass", beanClass, "parameterClass", parameterClass.getSimpleName(), "expectClassName", expectClassName + "Req"));
            }
            var returnClass = method.getReturnType();
            if (returnClass.isAnnotation() || returnClass.isArray() || returnClass.isEnum() || returnClass.isInterface() || returnClass.isPrimitive()) {
                throw new FatalBeanException(desc("@OrzWebApi request method return class is invalid", "beanClass", beanClass, "returnClass", returnClass));
            }
            if (!(expectClassName + "Rsp").equals(returnClass.getSimpleName())) {
                throw new FatalBeanException(desc("@OrzWebApi request method return class name is invalid", "beanClass", beanClass, "returnClass", parameterClass.getSimpleName(), "expectClassName", expectClassName + "Rsp"));
            }
        }
    }
}
