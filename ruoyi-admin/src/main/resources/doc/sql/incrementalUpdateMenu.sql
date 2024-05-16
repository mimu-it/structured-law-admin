-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('增量更新', '2000', '5', 'incremental-update', 'structured-law/incremental-update/index', 1, 0, 'C', '0', '0', 'structured-law:incremental_update:list', '#', 'admin', sysdate(), '', null, '增量更新菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('增量更新数据查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'structured-law:incremental_update:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('增量更新数据新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'structured-law:incremental_update:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('增量更新数据修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'structured-law:incremental_update:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('增量更新数据删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'structured-law:incremental_update:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('增量更新数据入库', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'structured-law:incremental_update:merge',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('增量更新列举效力级别', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'structured-law:incremental_update:list-category',       '#', 'admin', sysdate(), '', null, '');



-- 新增完后再去后台菜单管理进一步完善