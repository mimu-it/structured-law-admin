-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('法库目录', '2000', '1', 'category', 'structured-law/category/index', 1, 0, 'C', '0', '0', 'structured-law:category:list', '#', 'admin', sysdate(), '', null, '法库目录菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('法库目录查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'structured-law:category:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('法库目录新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'structured-law:category:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('法库目录修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'structured-law:category:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('法库目录删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'structured-law:category:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('法库目录导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'structured-law:category:export',       '#', 'admin', sysdate(), '', null, '');