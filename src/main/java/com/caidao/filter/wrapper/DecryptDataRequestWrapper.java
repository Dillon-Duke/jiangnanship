package com.caidao.filter.wrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tom
 * 重写 HttpServletRequestWrapper
 * 处理表单、ajax请求
 */
public class DecryptDataRequestWrapper extends HttpServletRequestWrapper {

    /** 用于储存请求参数 */
    private Map<String,String[]> params = new HashMap<>();

    /** 实现父类的构造方法 */
    public DecryptDataRequestWrapper(HttpServletRequest request) {
        super(request);
        // 把请求参数添加到我们自己的map当中
        this.params.putAll(request.getParameterMap());
    }

    /** 添加参数到map中 */
    public void setParameterMap(Map<String, Object> extraParams) {
        for (Map.Entry<String, Object> entry : extraParams.entrySet()) {
            setParameter(entry.getKey(), entry.getValue());
        }
    }

    /** 添加参数到map中 */
    public void setParameter(String name, Object value) {
        if (value != null) {
            if (value instanceof String[]) {
                params.put(name, (String[]) value);
            } else if (value instanceof String) {
                params.put(name, new String[]{(String) value});
            } else {
                params.put(name, new String[]{String.valueOf(value)});
            }
        }
    }

    /** 重写getParameter，代表参数从当前类中的map获取 */
    @Override
    public String getParameter(String name) {
        String[] values = params.get(name);
        if(values == null || values.length == 0) {
            return null;
        }
        return values[0];
    }

    /** 重写getParameterValues方法，从当前类的 map中取值 */
    @Override
    public String[] getParameterValues(String name) {
        return params.get(name);
    }
}
