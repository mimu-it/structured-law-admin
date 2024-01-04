-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('关联文件', '2000', '1', 'other-file', 'structured-law/other-file/index', 1, 0, 'C', '0', '0', 'structured-law:other-file:list', '#', 'admin', sysdate(), '', null, '关联文件菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('关联文件查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'structured-law:other-file:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('关联文件新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'structured-law:other-file:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('关联文件修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'structured-law:other-file:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('关联文件删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'structured-law:other-file:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('关联文件导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'structured-law:other-file:export',       '#', 'admin', sysdate(), '', null, '');