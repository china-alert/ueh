package com.mcinfotech.event.transmit.push;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**

 * date 2024/3/13 18:56
 * @version V1.0
 * @Package com.mcinfotech.event.transmit.push
 */
public class MethodToBytes implements TemplateMethodModelEx {
    @Override
    public Object exec(List arguments) throws TemplateModelException {
        String content = arguments.get(0)+"";
        try {
            return content.getBytes("gbk").length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }
}
