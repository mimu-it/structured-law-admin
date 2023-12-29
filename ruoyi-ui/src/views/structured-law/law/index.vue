<template>
  <div class="app-container">
    <el-row>
      <el-col :span="4">
        <el-card class="box-card">
          <CategoryTree @change="onTreeClick"></CategoryTree>
        </el-card>
      </el-col>
      <el-col :span="20" style="padding-left: 20px">
        <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch"
                 label-width="68px">
          <el-form-item label="法律名称" prop="name">
            <el-input
              v-model="queryParams.name"
              placeholder="请输入法律名称"
              clearable
              @keyup.enter.native="handleQuery"
            />
          </el-form-item>
          <el-form-item label="公布日期" prop="publish">
            <el-date-picker clearable
                            v-model="queryParams.publish"
                            type="daterange"
                            unlink-panels
                            value-format="yyyy-MM-dd"
                            range-separator="至"
                            start-placeholder="开始日期"
                            end-placeholder="结束日期">
            </el-date-picker>
          </el-form-item>
          <el-form-item label="生效日期" prop="validFrom">
            <el-date-picker clearable
                            v-model="queryParams.validFrom"
                            type="daterange"
                            unlink-panels
                            value-format="yyyy-MM-dd"
                            range-separator="至"
                            start-placeholder="开始日期"
                            end-placeholder="结束日期">
            </el-date-picker>
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
              v-hasPermi="['structured-law:law:add']"
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
              v-hasPermi="['structured-law:law:edit']"
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
              v-hasPermi="['structured-law:law:remove']"
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
              v-hasPermi="['structured-law:law:export']"
            >导出
            </el-button>
          </el-col>
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="lawList" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" align="center"/>
          <el-table-column label="id" align="center" prop="id" width="55" fixed/>
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

          <el-table-column label="公布日期" align="center" prop="publish" width="100">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.publish, '{y}-{m}-{d}') }}</span>
            </template>
          </el-table-column>
          <el-table-column label="生效日期" align="center" prop="validFrom" width="100">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.validFrom, '{y}-{m}-{d}') }}</span>
            </template>
          </el-table-column>

          <el-table-column label="目录id" align="center" prop="_escaped__category_id" width="200"/>
          <el-table-column label="法律名称" align="left" prop="name" fixed  width="260">
            <template slot-scope="scope">
              <router-link :to="{path:'/law-mgr/provision', query:{lawId: scope.row.id, lawName: scope.row.name}}" class="link-type">
                <span>{{ scope.row.name }}</span>
              </router-link>
            </template>
          </el-table-column>
          <el-table-column label="子标题" align="left" prop="subtitle" width="200"/>
          <el-table-column label="类型" align="left" prop="lawType" width="200"/>
          <el-table-column label="制定机关" align="left" prop="authority" width="200"/>

          <el-table-column label="状态" align="center" prop="status">
            <template slot-scope="scope">
              <dict-tag :options="dict.type.law_status" :value="scope.row.status"/>
            </template>
          </el-table-column>
          <el-table-column label="排序数字" align="center" prop="lawOrder"/>
          <el-table-column label="版本" align="center" prop="ver"/>
          <el-table-column label="标签" align="left" prop="tags" width="200"/>
          <el-table-column label="前言" align="left" prop="preface"  width="500"/>
        </el-table>

        <pagination
          v-show="total>0"
          :total="total"
          :page.sync="queryParams.pageNum"
          :limit.sync="queryParams.pageSize"
          @pagination="getList"
        />


      </el-col>
    </el-row>


    <!-- 添加或修改法律信息对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="法律名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入法律名称"/>
        </el-form-item>
        <el-form-item label="制定机关" prop="authority">
          <el-input v-model="form.authority" placeholder="请输入制定机关"/>
        </el-form-item>
        <el-form-item label="类型" prop="lawType">
          <el-input v-model="form.lawType" placeholder="请输入类型"/>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请输入状态">
            <el-option
              v-for="dict in dict.type.law_status"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="公布日期" prop="publish">
          <el-date-picker clearable
                          v-model="form.publish"
                          type="date"
                          value-format="yyyy-MM-dd"
                          placeholder="请选择公布日期">
          </el-date-picker>
        </el-form-item>

        <el-form-item label="生效日期" prop="validFrom">
          <el-date-picker clearable
                          v-model="form.validFrom"
                          type="date"
                          value-format="yyyy-MM-dd"
                          placeholder="请选择生效日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="排序数字" prop="lawOrder">
          <el-input v-model="form.lawOrder" placeholder="请输入排序数字"/>
        </el-form-item>
        <el-form-item label="子标题" prop="subtitle">
          <el-input v-model="form.subtitle" placeholder="请输入子标题"/>
        </el-form-item>
        <el-form-item label="版本" prop="ver">
          <el-input v-model="form.ver" placeholder="请输入版本"/>
        </el-form-item>
        <el-form-item label="标签" prop="tags">
          <el-input v-model="form.tags" placeholder="请输入标签"/>
        </el-form-item>
        <el-form-item label="前言">
          <el-input type="textarea" v-model="form.preface"></el-input>
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
  import {addLaw, delLaw, getLaw, listLaw, updateLaw} from "@/api/structured-law/law";
  import CategoryTree from "@/views/structured-law/category/category_tree.vue";
  import Util from "@/utils/util";
  export default {
    name: "Law",
    dicts: ['law_status'],
    components: {
      CategoryTree
    },
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
        // 法律信息表格数据
        lawList: [],
        // 弹出层标题
        title: "",
        // 是否显示弹出层
        open: false,
        // 查询参数
        queryParams: {
          pageNum: 1,
          pageSize: 10,
          name: null,
          lawType: null,
          publish: null,
          status: null,
          validFrom: null,
        },
        // 表单参数
        form: {},
        // 表单校验
        rules: {
          categoryId: [
            {required: true, message: "目录id不能为空", trigger: "blur"}
          ],
          name: [
            {required: true, message: "法律名称不能为空", trigger: "blur"}
          ],
          lawType: [
            {required: true, message: "类型,对标level不能为空", trigger: "change"}
          ],
        },
      };
    },
    created() {
      this.getList();
    },
    methods: {
      /** 查询法律信息列表 */
      getList() {
        this.loading = true;
        let reqParam = JSON.parse(JSON.stringify(this.queryParams));
        let publish = reqParam.publish;
        if(!Util.isNone(publish) && publish.length == 2) {
          reqParam.publish = JSON.stringify(publish);
        }

        let validFrom = reqParam.validFrom;
        if(!Util.isNone(validFrom) && validFrom.length == 2) {
          reqParam.validFrom = JSON.stringify(validFrom);
        }

        listLaw(reqParam).then(response => {
          this.lawList = response.rows;
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
          categoryId: null,
          name: null,
          lawType: null,
          filename: null,
          publish: null,
          expired: null,
          lawOrder: null,
          subtitle: null,
          validFrom: null,
          ver: null,
          tags: null
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
        this.title = "添加法律信息";
      },
      /** 修改按钮操作 */
      handleUpdate(row) {
        this.reset();
        const id = row.id || this.ids
        getLaw(id).then(response => {
          this.form = response.data;
          this.open = true;
          this.title = "修改法律信息";
        });
      },
      /** 提交按钮 */
      submitForm() {
        this.$refs["form"].validate(valid => {
          if (valid) {
            if (this.form.id != null) {
              updateLaw(this.form).then(response => {
                this.$modal.msgSuccess("修改成功");
                this.open = false;
                this.getList();
              });
            } else {
              addLaw(this.form).then(response => {
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
        this.$modal.confirm('是否确认删除法律信息编号为"' + ids + '"的数据项？').then(function () {
          return delLaw(ids);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => {
        });
      },
      /** 导出按钮操作 */
      handleExport() {
        this.download('structured-law/law/export', {
          ...this.queryParams
        }, `law_${new Date().getTime()}.xlsx`)
      },
      handleNodeClick(data) {
        console.log(data);
      },
      onTreeClick(id) {
        this.queryParams.categoryId = id;
        this.handleQuery();
      }
    }
  };
</script>

<style scoped>
  /deep/.el-textarea__inner {
    height: 150px;
  }

</style>
