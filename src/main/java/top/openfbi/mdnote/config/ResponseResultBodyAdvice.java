package top.openfbi.mdnote.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.elasticsearch.ResourceNotFoundException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.util.WebUtils;
import top.openfbi.mdnote.common.Result;
import top.openfbi.mdnote.common.ResultStatus;
import top.openfbi.mdnote.common.exception.ResultException;

import java.lang.annotation.Annotation;
import java.net.SocketException;

@Slf4j
@RestControllerAdvice
@ControllerAdvice
public class ResponseResultBodyAdvice implements ResponseBodyAdvice<Object> {

    private static final Class<? extends Annotation> ANNOTATION_TYPE = ResponseResultBody.class;

    /**
     * 判断类或者方法是否使用了 @ResponseResultBody
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (org.springframework.web.servlet.mvc.method.annotation.SseEmitter.class.isAssignableFrom(returnType.getParameterType())) {
            return false;
        }
        return AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(), ANNOTATION_TYPE) || returnType.hasMethodAnnotation(ANNOTATION_TYPE);
    }

    /**
     * 当类或者方法使用了 @ResponseResultBody 就会调用这个方法
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof Result) {
            return body;
        }
        return Result.success(body);
    }

    // ES未查到数据
    @ExceptionHandler(ResourceNotFoundException.class)
    public final ResponseEntity<Result<?>> DataAccessResourceFailureExceptionHandler(ResourceNotFoundException ex, WebRequest request) {
        log.error("es连接异常信息: {}", ex);
        return this.handleResultException(new ResultException(ResultStatus.INTERNAL_ELASTIC_NO_DATA), request);
    }

    //  SprigData 连接数据框错误。
    @ExceptionHandler(DataAccessResourceFailureException.class)
    public final ResponseEntity<Result<?>> DataAccessResourceFailureExceptionHandler(DataAccessResourceFailureException ex, WebRequest request) {
        log.error("SprigData连接异常信息: {}", ex);
        return this.handleResultException(new ResultException(ResultStatus.INTERNAL_SPRING_DATA_CONNECT_FAIL), request);
    }

    // 内部服务连接错误
    @ExceptionHandler(SocketException.class)
    public final ResponseEntity<Result<?>> SocketExceptionHandler(SocketException ex, WebRequest request) {
        log.error("系统内部服务操作异常信息: {}", ex);
        return this.handleResultException(new ResultException(ResultStatus.INTERNAL_SERVICE_CONNECT_FAIL), request);
    }

    // Redis连接错误
    @ExceptionHandler(RedisConnectionFailureException.class)
    public final ResponseEntity<Result<?>> RedisConnectionFailureExceptionHandler(RedisConnectionFailureException ex, WebRequest request) {
        log.error("Redis连接错误异常信息: {}", ex.getMessage());
        return this.handleResultException(new ResultException(ResultStatus.INTERNAL_REDIS_CONNECT_FAIL), request);
    }

    // 请求体过大异常
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public final ResponseEntity<Result<?>> RedisConnectionFailureExceptionHandler(MaxUploadSizeExceededException ex, WebRequest request) {
        log.error("请求体过大异常信息: {}", ex.getMessage());
        return this.handleResultException(new ResultException(ResultStatus.CLIENT_REQUEST_ENTITY_TOO_LARGE), request);
    }

    // 业务异常错误
    @ExceptionHandler(ResultException.class)
    public final ResponseEntity<Result<?>> ResultExceptionHandler(ResultException ex, WebRequest request) {
        log.error("业务异常信息: {}", ex);
        return this.handleResultException(ex, request);
    }
    // 数据类型转换异常
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public final ResponseEntity<Result<?>> NumberFormatException(MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.error("业务异常信息,接口数据类型转换异常: {}", ex);
        return this.handleResultException(new ResultException(ResultStatus.CLIENT_REQUEST_PARAMETERS_ILLEGAL), request);
    }
    // 兜底的异常处理，出现这种异常说明代码有bug需要修复，或者需要添加对应的excpetionHandler
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Result<?>> exceptionHandler(Exception ex, WebRequest request) {
        log.error("未知异常信息: {}", ex);
        HttpHeaders headers = new HttpHeaders();
        return this.handleException(ex, headers, request);
    }

    // 供上面的各种excpeitonHandler使用
    public final ResponseEntity<Result<?>> handleResultException(ResultException ex, WebRequest request) {
        // 设置异常返回信息
        Result<?> body = Result.failure(ex.getResultStatus());
        HttpHeaders headers = new HttpHeaders();
        // 设置http请求状态
        HttpStatus status = HttpStatus.OK;
        return this.handleExceptionInternal(ex, body, headers, status, request);
    }

    /**
     * 未知异常类的统一处理
     */
    protected ResponseEntity<Result<?>> handleException(Exception ex, HttpHeaders headers, WebRequest request) {
//        log.error("handleException: {}", ex);
        // 设置异常返回信息
        Result<?> body = Result.unknownFailure();
        // 设置http请求状态
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return this.handleExceptionInternal(ex, body, headers, status, request);
    }

    /**
     * org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler#handleExceptionInternal(java.lang.Exception, java.lang.Object, org.springframework.http.HttpHeaders, org.springframework.http.HttpStatus, org.springframework.web.context.request.WebRequest)
     * <p>
     * A single place to customize the response body of all exception types.
     * <p>The default implementation sets the {@link WebUtils#ERROR_EXCEPTION_ATTRIBUTE}
     * request attribute and creates a {@link ResponseEntity} from the given
     * body, headers, and status.
     */
    protected ResponseEntity<Result<?>> handleExceptionInternal(
            Exception ex, Result<?> body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST);
        }
        //返回异常状态
        return new ResponseEntity<>(body, headers, status);
    }
}

