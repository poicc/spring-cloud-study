package com.soft1851.content.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.soft1851.content.dto.*;
import com.soft1851.content.entity.MidUserShare;
import com.soft1851.content.entity.Share;
import com.soft1851.content.enums.AuditStatusEnum;
import com.soft1851.content.feignclient.UserCenterFeignClient;
import com.soft1851.content.mapper.MidUserShareMapper;
import com.soft1851.content.mapper.ShareMapper;
import com.soft1851.content.service.ShareService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.StringUtil;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author crq
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class ShareServiceImpl implements ShareService {
    private final ShareMapper shareMapper;
    private final MidUserShareMapper midUserShareMapper;
    private final UserCenterFeignClient userCenterFeignClient;
    private final RocketMQTemplate rocketMQTemplate;
   // private final AsyncRestTemplate asyncRestTemplate;

    @Override
    public ShareDto findById(Integer id) {
        // 获取分享实体
        Share share = this.shareMapper.selectByPrimaryKey(id);
        // 获得发布人id
        Integer userId = share.getUserId();

        UserDto userDTO = this.userCenterFeignClient.findUserById(userId);

        ShareDto shareDTO = new ShareDto();
        // 属性的装配
//        BeanUtils.copyProperties(share, shareDTO);
//        shareDTO.setWxNickname(userDTO.getWxNickname());
        shareDTO.setShare(share);
        shareDTO.setWxNickname(userDTO.getWxNickname());
        return shareDTO;
    }

    @Override
    public PageInfo<Share> query(String title, Integer pageNo, Integer pageSize, Integer userId) {
        //启动分页
        PageHelper.startPage(pageNo,pageSize);
        Example example = new Example(Share.class);
        Example.Criteria criteria = example.createCriteria();
        //如标题关键字不空，则加上模糊查询条件，否则结果即所有数据
        if(StringUtil.isNotEmpty(title)){
            criteria.andLike("title","%"+title+"%");

        }
        //执行按条件查询
        List<Share> shares = this.shareMapper.selectByExample(example);
        //处理后的share数据表
        List<Share> sharesDeal;
        //如果用户未登录，那么下载地址全设为Null
        if(userId == null) {
            sharesDeal = shares.stream()
                    .peek(share -> {
                        share.setDownloadUrl(null);
                    })
                    .collect(Collectors.toList());
        }
        //如果用户登录了，那么查询一下mid_user_share，如果没有数据，那么这条share的downloadURl也设置为Null
        //只有自己分享的资源才能直接看到下载连接，否则显示“兑换”
        else{
            sharesDeal = shares.stream()
                    .peek(share -> {
                        MidUserShare midUserShare = this.midUserShareMapper.selectOne(
                                MidUserShare.builder()
                                .userId(userId)
                                .shareId(share.getId())
                                .build()
                        );
                        if(midUserShare == null) {
                            share.setDownloadUrl(null);
                        }
                    })
                    .collect(Collectors.toList());
        }
        return new PageInfo<>(sharesDeal);
    }

    @Override
    public int putShare(ShareContributeDto shareContributeDto) {
        Share share = Share.builder()
                .isOriginal(shareContributeDto.getIsOriginal())
                .author(shareContributeDto.getAuthor())
                .price(shareContributeDto.getPrice())
                .downloadUrl(shareContributeDto.getDownloadUrl())
                .summary(shareContributeDto.getSummary())
                .buyCount(shareContributeDto.getBuyCount())
                .title(shareContributeDto.getTitle())
                .userId(1)
                .cover("https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcTE6YzGMeIEclvty31gPxuNa71ErBv7N8BS3g&usqp=CAU")
                .createTime(new Date())
                .updateTime(new Date())
                .showFlag(false)
                .auditStatus("NOT_YET")
                .reason("未审核")
                .build();

        return shareMapper.insert(share);
    }

    @Override
    public Share auditById(Integer id, ShareAudioDto shareAudioDto) {
        //查询share是否存在，不存在或者当前的audit_status ！=NOT_YET,那么抛异常
        Share share = this.shareMapper.selectByPrimaryKey(id);
        if(share == null) {
            throw new IllegalArgumentException("参数非法！该分享不存在！");
        }
        if(!Objects.equals("NOT_YET",share.getAuditStatus())) {
            throw new IllegalArgumentException("参数非法！ 该分享已审核通过或审核不通过！");
            //审核资源，将状态改为PASS或REJECT
            //这个API的主要流程是审核，所以不需要更新积分的结果返回，可以将加积分改为异步
        }
        share.setAuditStatus(shareAudioDto.getAuditStatusEnum().toString());
        share.setReason(shareAudioDto.getReason());
        this.shareMapper.updateByPrimaryKey(share);

            //如果是PASS，那么发送消息给rocketmq，让用户中心去消费，并为发布人添加积分
            if (AuditStatusEnum.PASS.equals(shareAudioDto.getAuditStatusEnum())){
             //   String url = "http://localhost:8081/users/bonus/new";
                //设置Header
            //    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            //    headers.add("Content-Type", "application/json;charset=UTF-8");
           //     HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
//                UserAddBonusMsgDto userAddBonusMsgDto = UserAddBonusMsgDto.builder()
//                        .userId(share.getUserId())
//                        .bonus(50)
//                        .build();
//                //异步发送
//                ListenableFuture<ResponseEntity<UserAddBonusMsgDto>> entity = asyncRestTemplate.postForEntity(url, httpEntity, UserAddBonusMsgDto.class, userAddBonusMsgDto);
               // entity.addCallback(result -> log.info(result.getBody()),(e) -> log.error(e.getMessage()));
                //log.info("C");
//            this.rocketMQTemplate.convertAndSend(
//                    "add-bonus",
//                    UserAddBonusMsgDto.builder()
//                    .userId(share.getUserId())
//                    .bonus(50)
//                    .build());
                //使用Feign来调用用户中心更改积分的接口（同步）
                this.userCenterFeignClient.addBonus(UserAddBonusMsgDto.builder()
                        .userId(share.getUserId())
                        .bonus(50)
                        .build());
            }

        return share;
    }


    @Override
    public String getHello() {
        return userCenterFeignClient.getHello();
    }
}
