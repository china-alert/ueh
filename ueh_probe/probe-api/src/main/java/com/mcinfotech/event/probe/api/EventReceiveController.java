package com.mcinfotech.event.probe.api;

import cn.mcinfotech.data.service.domain.DataLoadParams;
import cn.mcinfotech.data.service.domain.ResultPattern;
import com.mcinfotech.event.filter.IFilter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description: 事件管理
 */
@RestController
@RequestMapping("/event")
@Api("事件管理")
public class EventReceiveController {
    private static Logger logger = LogManager.getLogger(EventReceiveController.class);
    @Resource
    private List<IFilter<String>> messageFilters;

    /**
     * 以RestApi的方式对外提供事件接收入口
     *
     * @param params
     * @return
     */
    @ApiOperation("接收事件")
    @PostMapping(value = "/receive")
    public ResultPattern receive(@RequestBody String params) {
        ResultPattern resultPattern = new ResultPattern();
        DataLoadParams loadParams = new DataLoadParams();
        logger.debug("message is:{}", params);
        for (IFilter<String> messageFilter : messageFilters) {
            boolean doNext = false;
            try {
                doNext = messageFilter.chain(params, null);
                resultPattern.setStrData(params);
                resultPattern.setSuccess(true);
                resultPattern.setEmpty(true);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                resultPattern.setDcName(loadParams.getDcName());
                resultPattern.setFilter(loadParams.getFilter());
                resultPattern.setSuccess(false);
                resultPattern.setEmpty(true);
                resultPattern.setErrorMsg(e.getMessage());
            }
            if (!doNext) {
                break;
            }
        }
        return resultPattern;
    }
}
