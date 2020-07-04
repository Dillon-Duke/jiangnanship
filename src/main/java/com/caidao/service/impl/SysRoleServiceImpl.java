package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.exception.MyException;
import com.caidao.mapper.SysRoleMapper;
import com.caidao.mapper.SysRoleMenuMapper;
import com.caidao.mapper.SysUserRoleMapper;
import com.caidao.pojo.SysRole;
import com.caidao.pojo.SysRoleMenu;
import com.caidao.pojo.SysUser;
import com.caidao.pojo.SysUserRole;
import com.caidao.service.SysRoleService;
import com.caidao.util.EntityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author jinpeng
 * @since 2020-03-25
 */
@Service
@Slf4j
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

	@Autowired
	private SysRoleMapper sysRoleMapper;
	
	@Autowired
	private SysRoleMenuMapper sysRoleMenuMapper;

	@Autowired
	private SysUserRoleMapper sysUserRoleMapper;
	
	@Override
	public IPage<SysRole> findSysRolePage(Page<SysRole> page, SysRole sysRole) {
		Assert.notNull(page,"页面属性不能为空");
		log.info("查询角色页面当前页{}，页大小{}",page.getCurrent(),page.getSize());
		IPage<SysRole> selectPage = sysRoleMapper.selectPage(page, new LambdaQueryWrapper<SysRole>()
					.eq(StringUtils.hasText(sysRole.getRoleName()),SysRole::getRoleName,sysRole.getRoleName()));
		return selectPage;
	}
	
	/**
	 * 新增角色
	 */
	@Override
	@Transactional(rollbackFor = RuntimeException.class)
	public boolean save(SysRole sysRole) {
		Assert.notNull(sysRole,"新增角色不能为空");
		log.info("新增角色名为{}的角色",sysRole.getRoleName());
		SysUser sUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
		sysRole.setCreateId(sUser.getUserId());
		if (sysRole.getRoleName() == null || sysRole.getCreateId() == null ) {
			throw new MyException("新增角色参数错误");
		}
		sysRole.setCreateDate(LocalDateTime.now());
		sysRole.setState(1);
		boolean save = super.save(sysRole);
		//批量新增角色菜单
		List<Integer> menuIdLists = sysRole.getMenuIdList();
		List<SysRoleMenu> roleMenus = new ArrayList<>(menuIdLists.size());
		if (!menuIdLists.isEmpty() && save) {
			for (Integer menuIdList: menuIdLists) {
				roleMenus.add(EntityUtils.getSysRoleMenu(sysRole.getRoleId(),menuIdList));
			}
		}
		return sysRoleMenuMapper.insertBatches(roleMenus);
	}
	
	/**
	 * 修改角色
	 * @Transactional(rollbackFor = Exception.class) 让checked例外也回滚
	 */
	@Override
	@Transactional(rollbackFor = RuntimeException.class)
	public boolean updateById(SysRole sysRole) {
		Assert.notNull(sysRole,"修改角色不能为空");
		log.info("修改角色名为{}的角色",sysRole.getRoleName());
		SysUser principal = (SysUser)SecurityUtils.getSubject().getPrincipal();
		sysRole.setUpdateId(principal.getUserId());
		sysRole.setUpdateDate(LocalDateTime.now());
		if (sysRole.getRoleId() == null || sysRole.getRoleName() == null ) {
			throw new MyException("修改角色参数错误");
		}
		boolean updateById = super.updateById(sysRole);
		List<Integer> menuIdLists = sysRole.getMenuIdList();
		List<SysRoleMenu> roleMenus = new ArrayList<>(menuIdLists.size());
		if (!menuIdLists.isEmpty()&&updateById) {
			//修改角色之前吧所有的角色对应的菜单id删除掉
			sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId,sysRole.getRoleId()));
			for (Integer menuIdList: menuIdLists) {
				roleMenus.add(EntityUtils.getSysRoleMenu(sysRole.getRoleId(),menuIdList));
			}
		}
		return sysRoleMenuMapper.insertBatches(roleMenus);
	}

	/**
	 * 获取所有的角色列表
	 * @return
	 */
	@Override
	public List<SysRole> list() {
		log.info("获取所有的角色列表");
		return super.list();
	}

	/**
	 * 修改角色前查询角色对应的菜单id
	 */
	@Override
	public SysRole getById(Serializable id) {
		Assert.notNull(id,"id不能为空");
		log.info("查询角色ID为{}的角色",id);
		SysRole sysRole = super.getById(id);
		List<Object> menuIds = sysRoleMenuMapper.selectObjs(new LambdaQueryWrapper<SysRoleMenu>()
				.select(SysRoleMenu::getMenuId)
				.eq(SysRoleMenu::getRoleId, id));
		if (menuIds == null || menuIds.isEmpty()) {
			return sysRole;
		}
		List<Integer> menuIdList = new ArrayList<Integer>(menuIds.size());
		for (Object menuId : menuIds) {
			menuIdList.add(Integer.valueOf(menuId.toString()));
		}
		sysRole.setMenuIdList(menuIdList);
		return sysRole;
	}
	/**
	 * 批量删除校验
	 * @Transactional(noRollbackFor = RuntimeException.class)  让unchecked例外不回滚
	 */
	@Override
	@Transactional(rollbackFor = RuntimeException.class)
	public boolean removeByIds(Collection<? extends Serializable> idList) {
		Assert.notNull(idList,"删除角色的ids不能为空 ");
		log.info("删除角色id为{}的角色",idList);
		//删除之前判断是否还有用户绑定角色 要是有，则删除失败，抛出异常
		List<Serializable> arrayList = new ArrayList<>(idList.size());
		for (Serializable serializable : idList) {
			arrayList.add(serializable);
		}
		List<SysUserRole> userRoles = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
				.in(SysUserRole::getRoleId, arrayList));
		if (userRoles.size() != 0){
			throw new MyException("ids为" + idList + "的用户绑定该角色，删除失败");
		}
		List<Serializable> list = new ArrayList<>(idList.size());
		for (Serializable roleId : idList) {
			list.add(roleId);
		}
		return sysRoleMenuMapper.deleteBatchWithRoleIds(list);
	}

}
