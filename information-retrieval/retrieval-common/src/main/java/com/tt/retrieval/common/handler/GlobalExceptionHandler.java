package com.tt.retrieval.common.handler;

import com.tt.retrieval.common.BaseException;
import com.tt.retrieval.common.ExceptionUtil;
import com.tt.retrieval.common.ResultCodeEnum;
import com.tt.retrieval.common.dto.ResultDto;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchStatusException;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * 全局异常处理
 *
 * @author LuHongGang
 * @version 1.0
 */
@Slf4j
//@Component
@ControllerAdvice
//@ConditionalOnWebApplication
//@ConditionalOnMissingBean(GlobalExceptionHandler.class)
public class GlobalExceptionHandler {

    /**
     * 生产环境
     */
    private final static String ENV_PROD = "prod";

    /**
     * 当前环境
     */
    @Value("${spring.profiles.active}")
    private String profile;


    /**
     * -------- 通用异常处理方法 若没有相应的异常处理机制来处理子异常 那么就走当前父类异常处理逻辑 统一返回客户端--------
     **/
    @ResponseBody
    @ExceptionHandler
    public ResultDto error(Exception e) {
        //e.printStackTrace();
        log.error(ExceptionUtil.getMessage(e));
        return ResultDto.error();
    }

    /**
     * ElasticsearchStatusException 异常处理
     **/
    @ResponseBody
    @ExceptionHandler(ElasticsearchStatusException.class)
    public ResultDto error(ElasticsearchStatusException e) {
        //e.printStackTrace();
        log.error(ExceptionUtil.getMessage(e));
        return ResultDto.setResult(ResultCodeEnum.ES_ERROR, e.getMessage());
    }


    /**
     * -------- 指定NullPointerException异常处理方法 --------
     **/
    @ExceptionHandler(NullPointerException.class)
    @ResponseBody
    public ResultDto error(NullPointerException e) {
        log.error(ExceptionUtil.getMessage(e));
        return ResultDto.setResult(ResultCodeEnum.NULL_POINTER);
    }

    /**
     * -------- 自定义BaseException定异常处理方法 --------
     **/
    @ExceptionHandler(BaseException.class)
    @ResponseBody
    public ResultDto handlerBaseException(BaseException e) {
        log.error(ExceptionUtil.getMessage(e));
        return ResultDto.error().message(e.getMessage()).code(e.getCode());
    }

    /**
     * Controller上一层相关异常 一个http请求到达Controller前,会对该请求的请求信息和目标控制器信息进行一系列的校验
     *
     * @param e 异常
     * @return 异常结果
     */
    @ExceptionHandler({
            // 首先根据请求Url查找有没有对应的控制器，若没有则会抛该异常
            NoHandlerFoundException.class,
            // 若匹配到了（匹配结果是一个列表，不同的是http方法不同，如：Get、Post等），则尝试将请求的http方法与列表的控制器做匹配，若没有对应http方法的控制器，则抛该异常
            HttpRequestMethodNotSupportedException.class,
            // 后再对请求头与控制器支持的做比较，比如content-type请求头，若控制器的参数签名包含注解@RequestBody，但是请求的content-type请求头的值没有包含application/json
            HttpMediaTypeNotSupportedException.class,
            // 未检测到路径参数 比如url为：/licence/{licenceId}，参数签名包含@PathVariable("licenceId")，当请求的url为/licence，在没有明确定义url为/licence的情况下，会被判定为：缺少路径参数
            MissingPathVariableException.class,
            // 缺少请求参数。比如定义了参数@RequestParam("licenceId") String licenceId，但发起请求时，未携带该参数，则会抛该异常
            MissingServletRequestParameterException.class,
            // 参数类型匹配失败。比如：接收参数为Long型，但传入的值确是一个字符串，那么将会出现类型转换失败的情况，这时会抛该异常
            TypeMismatchException.class,
            // 与上面的HttpMediaTypeNotSupportedException举的例子完全相反，即请求头携带了"content-type: application/json;charset=UTF-8"，但接收参数却没有添加注解@RequestBody，或者请求体携带的 json 串反序列化成 pojo 的过程中失败了，也会抛该异常
            HttpMessageNotReadableException.class,
            // 返回的 pojo 在序列化成 json 过程失败了，那么抛该异常
            HttpMessageNotWritableException.class,
//            BindException.class,
//            MethodArgumentNotValidException.class,
            // 请求媒体类型不匹配...
            HttpMediaTypeNotAcceptableException.class,
            ServletRequestBindingException.class,
            ConversionNotSupportedException.class,
            MissingServletRequestPartException.class,
            AsyncRequestTimeoutException.class
    })
    @ResponseBody
    public ResultDto handleServletException(Exception e) {
        log.error(e.getMessage(), e);
//        int code = CommonResponseEnum.SERVER_ERROR.getCode();
//        try {
//            ServletResponseEnum servletExceptionEnum = ServletResponseEnum.valueOf(e.getClass().getSimpleName());
//            code = servletExceptionEnum.getCode();
//        } catch (IllegalArgumentException e1) {
//            log.error("class [{}] not defined in enum {}", e.getClass().getName(), ServletResponseEnum.class.getName());
//        }

//        if (ENV_PROD.equals(profile)) {
//            // 当为生产环境, 不适合把具体的异常信息展示给用户, 比如404.
//            code = CommonResponseEnum.SERVER_ERROR.getCode();
//            BaseException baseException = new BaseException(CommonResponseEnum.SERVER_ERROR);
//            String message = getMessage(baseException);
//            return new ErrorResponse(code, message);
//        }

        return ResultDto.setResult(ResultCodeEnum.SERVER_ERROR);
    }

    /**
     * 参数绑定异常
     *
     * @param e 异常
     * @return 异常结果
     */
    @ExceptionHandler(value = BindException.class)
    @ResponseBody
    public ResultDto handleBindException(BindException e) {
        log.error("参数绑定校验异常", e);

        return wrapperBindingResult(e.getBindingResult());
    }

    /**
     * 参数校验异常，将校验失败的所有异常组合成一条错误信息
     *
     * @param e 异常
     * @return 异常结果
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseBody
    public ResultDto handleValidException(MethodArgumentNotValidException e) {
        log.error("参数绑定校验异常", e);

        return wrapperBindingResult(e.getBindingResult());
    }

    /**
     * 包装绑定异常结果
     *
     * @param bindingResult 绑定结果
     * @return 异常结果
     */
    private ResultDto wrapperBindingResult(BindingResult bindingResult) {
        StringBuilder msg = new StringBuilder();

        for (ObjectError error : bindingResult.getAllErrors()) {
            msg.append(", ");
            if (error instanceof FieldError) {
                msg.append(((FieldError) error).getField()).append(": ");
            }
            msg.append(error.getDefaultMessage() == null ? "" : error.getDefaultMessage());

        }
        return ResultDto.error().message(msg.substring(2)).code(ResultCodeEnum.VALID_ERROR.getCode());
    }


}
