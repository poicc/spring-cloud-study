package com.soft1851.user.service.impl;

import com.soft1851.user.dto.UserAddBonusMsgDto;
import com.soft1851.user.entity.BonusEventLog;
import com.soft1851.user.entity.User;
import com.soft1851.user.mapper.BonusEventLogMapper;
import com.soft1851.user.mapper.UserMapper;
import com.soft1851.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * @author crq
 */
@Service
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final BonusEventLogMapper bonusEventLogMapper;

    @Override
    public User findById(Integer id) {
        return userMapper.selectByPrimaryKey(id);
    }

    @Override
    public int addBonus(UserAddBonusMsgDto userAddBonusMsgDto) {
        Integer userId = userAddBonusMsgDto.getUserId();
        System.out.println(userId);
        User user = this.userMapper.selectByPrimaryKey(userId);
        user.setBonus(user.getBonus() + userAddBonusMsgDto.getBonus());
        this.userMapper.updateByPrimaryKeySelective(user);
        return this.bonusEventLogMapper.insert(BonusEventLog.builder()
                .userId(userId)
                .value(userAddBonusMsgDto.getBonus())
                .event("CONTRIBUTE")
                .description("投稿加积分")
                .createTime(Timestamp.valueOf(LocalDateTime.now()))
                .build());

    }
}
