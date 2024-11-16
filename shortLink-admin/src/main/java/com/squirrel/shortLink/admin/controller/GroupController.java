package com.squirrel.shortLink.admin.controller;

import com.squirrel.shortLink.admin.common.convention.result.Result;
import com.squirrel.shortLink.admin.common.convention.result.Results;
import com.squirrel.shortLink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import com.squirrel.shortLink.admin.dto.req.ShortLinkGroupSortReqDTO;
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
@RequestMapping("/api/short-link/admin/v1")
public class GroupController {

    private final GroupService groupService;

    /**
     * 新增短链接分组
     * @param requestParam 新增短链接信息
     * @return Result<Void>
     */
    @PostMapping("/group")
    public Result<Void> save(@RequestBody ShortLinkGroupSaveReqDTO requestParam) {
        groupService.saveGroup(requestParam.getName());
        return Results.success();
    }

    /**
     * 查询短链接分组信息
     * @return Result<List<ShortLinkGroupRespDTO>>
     */
    @GetMapping("/group")
    public Result<List<ShortLinkGroupRespDTO>> listGroup() {
        return Results.success(groupService.listGroup());
    }

    /**
     * 修改短链接分组名称
     * @param requestParam 更新分组信息
     * @return Result<Void>
     */
    @PutMapping("/group")
    public Result<Void> updateGroup(@RequestBody ShortLinkGroupUpdateReqDTO requestParam) {
        groupService.updateGroup(requestParam);
        return Results.success();
    }

    /**
     * 删除短链接分组
     * @param gid 分组id
     * @return Result<Void>
     */
    @DeleteMapping("/group")
    public Result<Void> deleteGroup(@RequestParam String gid) {
        groupService.deleteGroup(gid);
        return Results.success();
    }

    /**
     * 短链接分组排序
     * @param requestParam 排序信息
     */
    @PostMapping("/group/sort")
    public Result<Void> sortGroup(@RequestBody List<ShortLinkGroupSortReqDTO> requestParam) {
        groupService.sortGroup(requestParam);
        return Results.success();
    }
}
