-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('增量更新日志', '2000', '1', 'log', 'structured-law/log/index', 1, 0, 'C', '0', '0', 'structured-law:log:list', '#', 'admin', sysdate(), '', null, '增量更新日志菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('增量更新日志查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'structured-law:log:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('增量更新日志新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'structured-law:log:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('增量更新日志修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'structured-law:log:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('增量更新日志删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'structured-law:log:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('增量更新日志导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'structured-law:log:export',       '#', 'admin', sysdate(), '', null, '');