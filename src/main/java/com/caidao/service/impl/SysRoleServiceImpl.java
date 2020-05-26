package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.entity.SysRole;
import com.caidao.entity.SysRoleMenu;
import com.caidao.entity.SysUserRole;
import com.caidao.mapper.SysRoleMapper;
import com.caidao.mapper.SysRoleMenuMapper;
import com.caidao.mapper.SysUserRoleMapper;
import com.caidao.service.SysRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
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
		log.info("角色分页 页大小{},第几页{}",page.getCurrent(),page.getSize());
			IPage<SysRole> selectPage = sysRoleMapper.selectPage(page, new LambdaQueryWrapper<SysRole>()
					.eq(StringUtils.hasText(sysRole.getRoleName()),SysRole::getRoleName,sysRole.getRoleName()));
		return selectPage;
	}
	
	/**
	 * 新增角色
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean save(SysRole sysRole) {
		Assert.notNull(sysRole, "sysRole must not be null");
		log.info("新增角色{}",sysRole);
		if (sysRole.getRoleName() == null || sysRole.getCreateId() == null ) {
			throw new RuntimeException("新增角色参数错误");
		}
		sysRole.setCreateDate(LocalDateTime.now());
		sysRole.setState(1);
		boolean save = super.save(sysRole);
		
		List<Integer> menuIdLists = sysRole.getMenuIdList();
		if (!menuIdLists.isEmpty()&&save) {
			for (Integer menuIdList: menuIdLists) {
				SysRoleMenu sysRoleMenu = new SysRoleMenu();
				sysRoleMenu.setRoleId(sysRole.getRoleId());
				sysRoleMenu.setMenuId(menuIdList);
				sysRoleMenuMapper.insert(sysRoleMenu);				
			}
		}
		return save;
	}
	
	/**
	 * 修改角色
	 * @Transactional(rollbackFor = Exception.class) 让checked例外也回滚
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean updateById(SysRole sysRole) {
		Assert.notNull(sysRole, "sysRole must not be null");
		log.info("修改角色{}",sysRole);

		sysRole.setUpdateDate(LocalDateTime.now());
		if (sysRole.getRoleId() == null || sysRole.getRoleName() == null ) {
			throw new RuntimeException("修改角色参数错误");
		}
		
		boolean updateById = super.updateById(sysRole);
		
		List<Integer> menuIdLists = sysRole.getMenuIdList();
					
		if (!menuIdLists.isEmpty()&&updateById) {
			
			//修改角色之前吧所有的角色对应的菜单id删除掉
			sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId,sysRole.getRoleId()));
			
			for (Integer menuIdList: menuIdLists) {
				SysRoleMenu sysRoleMenu = new SysRoleMenu();
				sysRoleMenu.setRoleId(sysRole.getRoleId());
				sysRoleMenu.setMenuId(menuIdList);
				sysRoleMenuMapper.insert(sysRoleMenu);					
			}
		}
		return updateById;
	}
	
	/**
	 * 修改角色前查询角色对应的菜单id
	 */
	@Override
	public SysRole getById(Serializable id) {
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
	@Transactional(rollbackFor = Exception.class)
	public boolean removeByIds(Collection<? extends Serializable> idList) {

		//删除之前判断是否还有用户绑定角色 要是有，则删除失败，抛出异常
		for (Serializable serializable : idList) {
			SysUserRole sysUserRole = sysUserRoleMapper.selectOne(new LambdaQueryWrapper<SysUserRole>()
					.eq(SysUserRole::getRoleId, serializable));
			if (sysUserRole != null){
				throw new RuntimeException("id为" + sysUserRole.getUserId() + "的用户绑定该角色，删除失败");
			}
		}

		for (Serializable roleId : idList) {
			sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleId));
		}
		return super.removeByIds(idList);
	}

}
