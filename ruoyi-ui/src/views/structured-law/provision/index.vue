<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="所属法律" prop="title">
        <TableViewPicker v-model="queryParams.lawId" :searchItems="customerSearchFields"
                         :defaultLabel="lawName"
                         :disabled="false"
                         :width="800"
                         @change="onLawPick"
                         :labelKeys="['name']"
                         ></TableViewPicker>
      </el-form-item>

      <el-form-item label="条款标题" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入条款标题"
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
          v-hasPermi="['structured-law:provision:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['structured-law:provision:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['structured-law:provision:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['structured-law:provision:export']"
        >导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-folder-checked"
          size="mini"
          @click="buildIndex"
          v-hasPermi="['structured-law:provision:build_index']"
        >建立倒排索引</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-folder-delete"
          size="mini"
          @click="deleteIndex"
          v-hasPermi="['structured-law:provision:delete_index']"
        >删除索引</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          plain
          icon="el-icon-coin"
          size="mini"
          @click="backup"
          v-hasPermi="['structured-law:provision:backup']"
        >备份数据库</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          plain
          icon="el-icon-copy-document"
          size="mini"
          @click="sync"
          v-hasPermi="['structured-law:provision:sync']"
        >同步爬虫库</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="provisionList" @selection-change="handleSelectionChange">
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
      <el-table-column label="所属法律" align="left" prop="_escaped__law_id" width="200"/>
      <el-table-column label="条款标题" align="left" prop="title" width="200"/>
      <el-table-column label="条款数字标题" align="left" prop="titleNumber" width="100"/>
      <el-table-column label="标签" align="left" prop="tags" width="100"/>
      <el-table-column label="条款正文" align="left" prop="termText" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width"  width="200">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['structured-law:provision:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['structured-law:provision:remove']"
          >删除</el-button>
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

    <!-- 添加或修改法律条款对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="条款标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入条款标题" />
        </el-form-item>
        <el-form-item label="条款数字标题" prop="title">
          <el-input v-model="form.titleNumber" placeholder="请输入条款数字标题" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input type="textarea" v-model="form.tags"></el-input>
        </el-form-item>
        <el-form-item label="条款正文">
          <el-input type="textarea" v-model="form.termText"></el-input>
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
import { listProvision, getProvision, delProvision, addProvision, updateProvision,
  buildElasticSearchIndex, deleteElasticSearchIndex, backup, sync } from "@/api/structured-law/provision";
import TableViewPicker from "@/components/Xiao/TableViewPicker";
import Util from '@/utils/util';

export default {
  name: "Provision",
  components: {
    TableViewPicker
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
      // 法律条款表格数据
      provisionList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        lawId: null,
        title: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        lawId: [
          { required: true, message: "所属法律不能为空", trigger: "blur" }
        ],
        title: [
          { required: true, message: "条款标题不能为空", trigger: "blur" }
        ],
        titleNumber: [
          { required: true, message: "条款数字标题不能为空", trigger: "blur" }
        ],
        termText: [
          { required: true, message: "条款正文不能为空", trigger: "blur" }
        ]
      },
      lawName: "",
      customerSearchFields: [{
        label: "名称",
        param: "name"
      }],
    };
  },
  created() {
    this.getList();
  },
  activated(){
    this.$nextTick(() => {
      //console.log('代码中获取路由参数：', this.$route.query.lawId)
      let lawId = this.$route.query.lawId;
      let lawName = this.$route.query.lawName;
      if(Util.isBlank(lawId) || Util.isBlank(lawName)) {
        return
      }

      this.queryParams.lawId = parseInt(lawId);
      this.lawName = lawName;
      this.getList();
    });
  },
  methods: {
    onLawPick() {

    },
    /** 查询法律条款列表 */
    getList() {
      this.loading = true;
      listProvision(this.queryParams).then(response => {
        this.provisionList = response.rows;
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
        lawId: null,
        title: null,
        tags: null,
        termText: null
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
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加法律条款";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getProvision(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改法律条款";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateProvision(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addProvision(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除法律条款编号为"' + ids + '"的数据项？').then(function() {
        return delProvision(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('structured-law/provision/export', {
        ...this.queryParams
      }, `provision_${new Date().getTime()}.xlsx`)
    },
    /** 建立 elasticsearch 索引 */
    buildIndex() {
      buildElasticSearchIndex().then(response => {
        this.$alert('索引建立正在异步进行...', '提示', {
          confirmButtonText: '确定',
          callback: action => {}
        });
      });
    },
    deleteIndex() {
      deleteElasticSearchIndex().then(response => {
        this.$alert('索引删除成功', '提示', {
          confirmButtonText: '确定',
          type: 'success',
          callback: action => {}
        });
      }).catch((err) => {
        console.error(err);
      });
    },
    backup() {
      backup().then(response => {
        this.$alert('备份成功', '提示', {
          confirmButtonText: '确定',
          type: 'success',
          callback: action => {}
        });
      });
    },
    sync() {
      this.$router.push('/law-mgr/sync');
    }
  }
};
</script>
