package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.exception.MyException;
import com.caidao.mapper.AppCommonMsgMapper;
import com.caidao.mapper.AppUserCommonMsgMapper;
import com.caidao.mapper.DeptUserMapper;
import com.caidao.pojo.AppCommonMsg;
import com.caidao.pojo.AppUserCommonMsg;
import com.caidao.pojo.DeptUser;
import com.caidao.pojo.SysUser;
import com.caidao.service.AppCommonMsgService;
import com.caidao.util.DateUtils;
import com.caidao.util.EntityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Dillon
 * @since 2020-07-07
 */
@Service
@Slf4j
public class AppCommonMsgServiceImpl extends ServiceImpl<AppCommonMsgMapper, AppCommonMsg> implements AppCommonMsgService {

    /**
     * 可修改的最长发布时间段
     */
    private static final Long MAX_PUBLISH_TIME = 30L;

    @Autowired
    private AppCommonMsgMapper appCommonMsgMapper;

    @Autowired
    private AppUserCommonMsgMapper appUserCommonMsgMapper;

    @Autowired
    private DeptUserMapper deptUserMapper;

    /**
     * 获取分页的用户数据
     * @param page
     * @param appCommonMsg
     * @return
     */
    @Override
    public IPage<AppCommonMsg> getAppCommonPage(Page<AppCommonMsg> page, AppCommonMsg appCommonMsg) {
        Assert.notNull(page,"分页信息不能为空");
        log.info("用户获取消息的当前页{}，页大小{}",page.getCurrent(),page.getSize());
        IPage<AppCommonMsg> selectPage = appCommonMsgMapper.selectPage(page, new LambdaQueryWrapper<AppCommonMsg>()
                .like(StringUtils.hasText(appCommonMsg.getMassageName()), AppCommonMsg::getMassageName, appCommonMsg.getMassageName())
                .eq(AppCommonMsg::getState, 1)
                .orderByDesc(AppCommonMsg::getCreateTime));
        return selectPage;
    }

    /**
     * 新增通用消息
     * @param msg
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void saveAppCommonMassage(AppCommonMsg msg) {
        Assert.notNull(msg,"新增通用消息不能为空");
        SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
        Assert.notNull(sysUser,"用户登录超时，请重新登录");
        log.info("新增消息名称为{}的通用消息",msg.getMassageName());
        msg.setCreateId(sysUser.getUserId());
        msg.setCreateTime(LocalDateTime.now());
        msg.setIsPublish(0);
        msg.setState(1);
        appCommonMsgMapper.insert(msg);
    }

    /**
     * 通过id获取消息数据
     * @param id
     * @return
     */
    @Override
    public AppCommonMsg getAppCommonMassageById(Integer id) {
        Assert.notNull(id,"消息Id不能为空");
        log.info("获取Id为{}的消息",id);
        return appCommonMsgMapper.selectById(id);
    }

    /**
     * 批量删除消息 假删除
     * @param ids
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void beachDeleteAppCommonMassage(List<Integer> ids) {
        Assert.notNull(ids,"消息Ids不能为空");
        log.info("获取Ids为{}的消息",ids);
        //获取需要删除的所有消息
        List<AppCommonMsg> appCommonMassages = appCommonMsgMapper.selectBatchIds(ids);
        //判断消息是否发布 发布，则不能删除
        List<AppCommonMsg> publishMsg = appCommonMassages.stream().filter((x) -> x.getIsPublish().equals(1)).collect(Collectors.toList());
        if (publishMsg.size() != 0) {
            throw new MyException("删除的消息中包含已发布的消息，不能删除");
        }
        //判断消息是否发布超过30分钟，如果超过，不能删除
        List<Integer> longTimeUserCommonMsg = ids.stream().filter((x) -> DateUtils.getTimesLengthBetweenEndTimeAndStartTimeSecond(LocalDateTime.now(),getAppUserCommonMsgPublishTime(x)) > MAX_PUBLISH_TIME).collect(Collectors.toList());
        if (longTimeUserCommonMsg.size() != 0) {
            throw new MyException("删除的消息中包含发布时长超30分钟的消息，不能删除");
        }
        //批量删除消息
        boolean result1 = appCommonMsgMapper.removeBatchIds(ids);
        if (!result1) {
            throw new MyException("消息删除失败，请重试");
        }
    }

    /**
     * 修改消息
     * @param msg
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Boolean updateAppCommonMassage(AppCommonMsg msg) {
        Assert.notNull(msg,"修改消息不能为空");
        log.info("更新Id为{}的消息",msg.getId());
        //消息如果已经发布，则不能修改
        if (msg.getIsPublish() == 1) {
            throw new MyException("消息已经发布，不能修改");
        }
        //消息发布时间过长，不能修改
        long longTime = DateUtils.getTimesLengthBetweenEndTimeAndStartTimeSecond(LocalDateTime.now(), getAppUserCommonMsgPublishTime(msg.getId()));
        if (longTime >= MAX_PUBLISH_TIME) {
            throw new MyException("消息发布时间超过30分钟，不能修改");
        }
        int update = appCommonMsgMapper.updateById(msg);
        if (update == 0) {
            return false;
        }
        return true;
    }

    /**
     * 发布或取消发布消息
     * @param msg
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void publishAppCommonMassage(AppCommonMsg msg) {
        Assert.notNull(msg,"修改消息不能为空");
        log.info("发布Id为{}的消息",msg.getId());
        //获得发布或者是取消发布的状态
        Integer publish = msg.getIsPublish();
        if (publish == 1) {
            //推送消息到各个用户
            List<DeptUser> deptUsers = deptUserMapper.selectList(null);
            List<Integer> userIds = deptUsers.stream().map((x) -> x.getUserId()).collect(Collectors.toList());
            List<AppUserCommonMsg> list = userIds.stream().map((x) -> EntityUtils.getAppUserCommonMsg(msg.getId(),msg.getFileName(),msg.getFileResource(),msg.getMassageDetail(),msg.getMassageName(),x)).collect(Collectors.toList());
            boolean result = appUserCommonMsgMapper.insertBatches(list);
            if (!result) {
                throw new MyException("新增消息失败，请重试");
            }
        } else {
            //查询发布时间是不是超过30分钟，超过则不能取消发布
            long longTime = DateUtils.getTimesLengthBetweenEndTimeAndStartTimeSecond(LocalDateTime.now(), getAppUserCommonMsgPublishTime(msg.getId()));
            if (longTime >= MAX_PUBLISH_TIME) {
                throw new MyException("消息发布时间超过30分钟，不能取消发布");
            }
            //取消发布 删除推送到用户里面的信息
            appUserCommonMsgMapper.deleteBatchCommId(msg.getId());
        }
        appCommonMsgMapper.updateById(msg);
    }

    /**
     * 获取消息发布的时间
     * @param commId
     * @return
     */
    private LocalDateTime getAppUserCommonMsgPublishTime(Integer commId) {
        List<AppUserCommonMsg> list = appUserCommonMsgMapper.selectList(new LambdaQueryWrapper<AppUserCommonMsg>()
                .eq(AppUserCommonMsg::getCommId, commId));
        LocalDateTime first = list.stream().map((x) -> x.getCreateTime()).findFirst().orElse(LocalDateTime.now());
        return first;
    }
}
