package com.squirrel.project.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.squirrel.common.convention.result.Result;
import com.squirrel.project.dto.req.ShortLinkCreateReqDTO;
import com.squirrel.project.dto.resp.ShortLinkCreateRespDTO;

/**
 * 自定义流控策略
 */
public class CustomBlockHandler {

    /**
     * 创建短链接的降级策略
     * @param requestParam 创建短链接请求参数
     * @param exception BlockException
     * @return Result<ShortLinkCreateReqDTO>
     */
    public static Result<ShortLinkCreateRespDTO> createShortLinkBlockHandlerMethod(ShortLinkCreateReqDTO requestParam, BlockException exception) {
        return new Result<ShortLinkCreateRespDTO>().setCode("B100000").setMessage("当前访问网站人数过多，请稍后再试...");
    }
}
