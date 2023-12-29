-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('法律条款', '2000', '1', 'provision', 'structured-law/provision/index', 1, 0, 'C', '0', '0', 'structured-law:provision:list', '#', 'admin', sysdate(), '', null, '法律条款菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('法律条款查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'structured-law:provision:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('法律条款新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'structured-law:provision:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('法律条款修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'structured-law:provision:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('法律条款删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'structured-law:provision:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('法律条款导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'structured-law:provision:export',       '#', 'admin', sysdate(), '', null, '');