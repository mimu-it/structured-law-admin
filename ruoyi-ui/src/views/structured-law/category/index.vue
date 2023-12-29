<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="目录名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入目录名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="是否有子目录" prop="isSubFolder" label-width="120px">
        <el-select v-model="queryParams.isSubFolder" placeholder="请选择是否有子目录" clearable>
          <el-option
            v-for="dict in dict.type.sys_bool_number"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="分类分组" prop="categoryGroup">
        <el-input
          v-model="queryParams.categoryGroup"
          placeholder="请输入分类分组"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['structured-law:category:add']"
        >新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['structured-law:category:edit']"
        >修改
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['structured-law:category:remove']"
        >删除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['structured-law:category:export']"
        >导出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="categoryList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="id" align="center" prop="id" width="55"/>
      <el-table-column label="创建时间" align="center" prop="createTime" width="100">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="最近修改时间" align="center" prop="updateTime" width="100">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="目录名称" align="center" prop="name"/>
      <el-table-column label="文件夹路径" align="center" prop="folder"/>
      <el-table-column label="是否有子目录" align="center" prop="isSubFolder">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_bool_number" :value="scope.row.isSubFolder"/>
        </template>
      </el-table-column>
      <el-table-column label="分类分组" align="center" prop="categoryGroup"/>
      <el-table-column label="排序序号" align="center" prop="categoryOrder"/>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['structured-law:category:edit']"
          >修改
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['structured-law:category:remove']"
          >删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改法库目录对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="目录名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入目录名称"/>
        </el-form-item>
        <el-form-item label="文件夹路径" prop="folder">
          <el-input v-model="form.folder" placeholder="请输入文件夹路径"/>
        </el-form-item>
        <el-form-item label="是否有子目录" prop="isSubFolder">
          <el-select v-model="form.isSubFolder" placeholder="请选择是否有子目录">
            <el-option
              v-for="dict in dict.type.sys_bool_number"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="分类分组" prop="categoryGroup">
          <el-input v-model="form.categoryGroup" placeholder="请输入分类分组"/>
        </el-form-item>
        <el-form-item label="排序序号" prop="categoryOrder">
          <el-input v-model="form.categoryOrder" placeholder="请输入排序序号"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  import {addCategory, delCategory, getCategory, listCategory, updateCategory} from "@/api/structured-law/category";

  export default {
    name: "Category",
    dicts: ['sys_bool_number'],
    data() {
      return {
        // 遮罩层
        loading: true,
        // 选中数组
        ids: [],
        // 非单个禁用
        single: true,
        // 非多个禁用
        multiple: true,
        // 显示搜索条件
        showSearch: true,
        // 总条数
        total: 0,
        // 法库目录表格数据
        categoryList: [],
        // 弹出层标题
        title: "",
        // 是否显示弹出层
        open: false,
        // 查询参数
        queryParams: {
          pageNum: 1,
          pageSize: 10,
          name: null,
          folder: null,
          isSubFolder: null,
          categoryGroup: null,
          categoryOrder: null
        },
        // 表单参数
        form: {},
        // 表单校验
        rules: {
          name: [
            {required: true, message: "目录名称不能为空", trigger: "blur"}
          ],
          folder: [
            {required: true, message: "文件夹路径不能为空", trigger: "blur"}
          ],
          isSubFolder: [
            {required: true, message: "是否有子目录不能为空", trigger: "change"}
          ],
        }
      };
    },
    created() {
      this.getList();
    },
    methods: {
      /** 查询法库目录列表 */
      getList() {
        this.loading = true;
        listCategory(this.queryParams).then(response => {
          this.categoryList = response.rows;
          this.total = response.total;
          this.loading = false;
        });
      },
      // 取消按钮
      cancel() {
        this.open = false;
        this.reset();
      },
      // 表单重置
      reset() {
        this.form = {
          id: null,
          createdAt: null,
          creator: null,
          updatedAt: null,
          modifier: null,
          name: null,
          folder: null,
          isSubFolder: null,
          categoryGroup: null,
          categoryOrder: null
        };
        this.resetForm("form");
      },
      /** 搜索按钮操作 */
      handleQuery() {
        this.queryParams.pageNum = 1;
        this.getList();
      },
      /** 重置按钮操作 */
      resetQuery() {
        this.resetForm("queryForm");
        this.handleQuery();
      },
      // 多选框选中数据
      handleSelectionChange(selection) {
        this.ids = selection.map(item => item.id)
        this.single = selection.length !== 1
        this.multiple = !selection.length
      },
      /** 新增按钮操作 */
      handleAdd() {
        this.reset();
        this.open = true;
        this.title = "添加法库目录";
      },
      /** 修改按钮操作 */
      handleUpdate(row) {
        this.reset();
        const id = row.id || this.ids
        getCategory(id).then(response => {
          this.form = response.data;
          this.open = true;
          this.title = "修改法库目录";
        });
      },
      /** 提交按钮 */
      submitForm() {
        this.$refs["form"].validate(valid => {
          if (valid) {
            if (this.form.id != null) {
              updateCategory(this.form).then(response => {
                this.$modal.msgSuccess("修改成功");
                this.open = false;
                this.getList();
              });
            } else {
              addCategory(this.form).then(response => {
                this.$modal.msgSuccess("新增成功");
                this.open = false;
                this.getList();
              });
            }
          }
        });
      },
      /** 删除按钮操作 */
      handleDelete(row) {
        const ids = row.id || this.ids;
        this.$modal.confirm('是否确认删除法库目录编号为"' + ids + '"的数据项？').then(function () {
          return delCategory(ids);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => {
        });
      },
      /** 导出按钮操作 */
      handleExport() {
        this.download('structured-law/category/export', {
          ...this.queryParams
        }, `category_${new Date().getTime()}.xlsx`)
      }
    }
  };
</script>
