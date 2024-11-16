package com.squirrel.shortLink.admin.controller;

import com.squirrel.shortLink.admin.common.convention.result.Result;
import com.squirrel.shortLink.admin.common.convention.result.Results;
import com.squirrel.shortLink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import com.squirrel.shortLink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.squirrel.shortLink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.squirrel.shortLink.admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短链接分组控制层
 */
@RestController
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /**
     * 新增短链接分组
     * @param requestParam 新增短链接信息
     * @return Result<Void>
     */
    @PostMapping("/api/short-link/v1/group")
    public Result<Void> save(@RequestBody ShortLinkGroupSaveReqDTO requestParam) {
        groupService.saveGroup(requestParam.getName());
        return Results.success();
    }

    /**
     * 查询短链接分组信息
     * @return Result<List<ShortLinkGroupRespDTO>>
     */
    @GetMapping("/api/short-link/v1/group")
    public Result<List<ShortLinkGroupRespDTO>> listGroup() {
        return Results.success(groupService.listGroup());
    }

    /**
     * 修改短链接分组名称
     * @param requestParam 更新分组信息
     * @return Result<Void>
     */
    @PutMapping("/api/short-link/v1/group")
    public Result<Void> updateGroup(@RequestBody ShortLinkGroupUpdateReqDTO requestParam) {
        groupService.updateGroup(requestParam);
        return Results.success();
    }

    /**
     * 删除短链接分组
     * @param gid 分组id
     * @return Result<Void>
     */
    @DeleteMapping("/api/short-link/v1/group")
    public Result<Void> updateGroup(@RequestParam String gid) {
        groupService.deleteGroup(gid);
        return Results.success();
    }
}
