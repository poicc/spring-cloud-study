package com.soft1851.content.controller;

import com.soft1851.content.dto.ShareDto;
import com.soft1851.content.entity.Share;
import com.soft1851.content.service.ShareService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * @author crq
 */
@Slf4j
@RestController
@RequestMapping(value = "/share")
@Api(tags = "分享接口",value = "提供分享相关的Rest API")
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class ShareController {
    private final ShareService shareService;

    @GetMapping(value = "/{id}")
    @ApiOperation(value = "查询指定id的分享详情",notes = "查询指定Id的分享详情")
    public ShareDto findById(@PathVariable Integer id) {
        return this.shareService.findById(id);
    }

    @GetMapping(value = "/query")
    @ApiOperation(value = "分享列表",notes = "分享列表")
    public List<Share> query(
            @RequestParam(required = false)String title,
            @RequestParam(required = false,defaultValue = "1")Integer pageNo,
            @RequestParam(required = false,defaultValue = "10")Integer pageSize,
            @RequestParam(required = false) Integer userId) throws Exception {
        if(pageSize > 100) {
            pageSize = 100;
        }
        return this.shareService.query(title, pageNo, pageSize, userId).getList();
    }


    @GetMapping(value = "/hello")
    @ApiIgnore
    public String getHello(){
        return shareService.getHello();
    }
}