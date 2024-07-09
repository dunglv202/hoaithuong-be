package dev.dunglv202.hoaithuong.aop;

import dev.dunglv202.hoaithuong.dto.ApiError;
import dev.dunglv202.hoaithuong.helper.MessageProvider;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class ExceptionHandlerAspect {
    private final MessageProvider messageProvider;

    @Pointcut("execution(* dev.dunglv202.hoaithuong.exception.DefaultExceptionHandler.handle*(..))")
    private void exceptionHandlerMethod() {}

    @AfterReturning(pointcut = "exceptionHandlerMethod()", returning = "error")
    public void resolveClientMessage(ApiError<?> error) {
        String rawMessage = error.getError();
        error.setError(messageProvider.getLocalizedMessage(rawMessage));
    }
}
