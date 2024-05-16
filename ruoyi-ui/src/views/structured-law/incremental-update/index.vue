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
                            plain
                            icon="el-icon-coin"
                            size="mini"
                            @click="merge"
                            v-hasPermi="['structured-law:law:merge']"
                        >合并增量数据入库
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
                    <el-table-column label="法律名称" align="left" prop="name" fixed width="260">
                        <template slot-scope="scope">
                            <router-link
                                :to="{path:'/law-mgr/provision-incremental', query:{lawId: scope.row.id, lawName: scope.row.name}}"
                                class="link-type">
                                <span>{{ scope.row.name }}</span>
                            </router-link>
                        </template>
                    </el-table-column>
                    <el-table-column label="发布文号" align="left" prop="documentNo" width="200"/>
                    <el-table-column label="效力级别" align="left" prop="lawLevel" width="200"/>
                    <el-table-column label="制定机关" align="left" prop="authority" width="200"/>
                    <el-table-column label="制定机关所在省" align="left" prop="authorityProvince" width="200"/>
                    <el-table-column label="制定机关所在市" align="left" prop="authorityCity" width="200"/>
                    <el-table-column label="制定机关所在区" align="left" prop="authorityDistrict" width="200"/>

                    <el-table-column label="状态" align="center" prop="status">
                        <template slot-scope="scope">
                            <dict-tag :options="dict.type.law_status" :value="scope.row.status"/>
                        </template>
                    </el-table-column>
                    <el-table-column label="排序数字" align="center" prop="lawOrder"/>
                    <el-table-column label="版本" align="center" prop="ver"/>
                    <el-table-column label="标签" align="left" prop="tags" width="200"/>
                    <el-table-column label="前言" align="left" prop="preface" width="500"/>
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
            <el-form ref="form" :model="form" :rules="rules" label-width="120px">
                <el-form-item label="法律名称" prop="name">
                    <el-input v-model="form.name" placeholder="请输入法律名称"/>
                </el-form-item>

                <el-form-item label="目录" prop="categoryId">
                    <TreeSelect
                        :props="props"
                        :options="optionOfCategory"
                        :value="form.categoryId"
                        :clearable="true"
                        :accordion="true"
                        @getValue="getCategoryPickedValue($event)"
                    />
                </el-form-item>

                <el-form-item label="制定机关" prop="authority">
                    <el-input v-model="form.authority" placeholder="请输入制定机关"/>
                </el-form-item>
                <el-form-item label="制定机关所在省" prop="authorityProvince">
                    <el-input v-model="form.authorityProvince" placeholder="请输入制定机关所在省"/>
                </el-form-item>
                <el-form-item label="制定机关所在市" prop="authorityCity">
                    <el-input v-model="form.authorityCity" placeholder="请输入制定机关所在市"/>
                </el-form-item>
                <el-form-item label="制定机关所在区" prop="authorityDistrict">
                    <el-input v-model="form.authorityDistrict" placeholder="请输入制定机关所在区"/>
                </el-form-item>
                <el-form-item label="发布文号" prop="documentNo">
                    <el-input v-model="form.documentNo" placeholder="请输入发布文号"/>
                </el-form-item>
                <el-form-item label="效力级别" prop="lawLevel">
                    <el-input v-model="form.lawLevel" placeholder="请输入效力级别"/>
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
    import {addLaw, delLaw, getLaw, listLaw, updateLaw} from "@/api/structured-law/incremental-update";
    import CategoryTree from "@/views/structured-law/incremental-update/category_tree.vue";
    import Util from "@/utils/util";
    import TreeSelect from "@/components/Xiao/TreeSelect.vue";
    import {makeTree2} from "@/utils/tree-maker";
    import {listAllCategory, merge} from "@/api/structured-law/incremental-update";


    export default {
        name: "Law",
        dicts: ['law_status'],
        components: {
            CategoryTree, TreeSelect
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
                    ]
                },
                props: {
                    // 配置项（必选）
                    value: "id",
                    label: "folder",
                    children: "children"
                    // disabled:true
                },
                optionOfCategory: []
            };
        },
        created() {
            this.getList();
        },
        watch: {

        },
        methods: {
            /** 查询法律信息列表 */
            getList() {
                this.loading = true;
                let reqParam = JSON.parse(JSON.stringify(this.queryParams));
                let publish = reqParam.publish;
                if (!Util.isNone(publish) && publish.length == 2) {
                    reqParam.publish = JSON.stringify(publish);
                }

                let validFrom = reqParam.validFrom;
                if (!Util.isNone(validFrom) && validFrom.length == 2) {
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
                    lawLevel: null,
                    authority: null,
                    authorityProvince: null,
                    authorityCity: null,
                    authorityDistrict: null,
                    publish: null,
                    expired: null,
                    lawOrder: null,
                    subtitle: null,
                    documentNo: null,
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
            /** 修改按钮操作 */
            handleUpdate(row) {
                this.reset();
                const id = row.id || this.ids
                getLaw(id).then(response => {
                    this.form = response.data;
                    this.title = "修改法律信息";

                    this.getCategory().then(() => {
                        this.open = true;
                    });
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
                }).catch((err) => {
                    this.$modal.msgError(err);
                });
            },
            /** 导出按钮操作 */
            handleExport() {
                this.download('structured-law/law/export-existing-law', {
                    ...this.queryParams
                }, `existing_law.json`)
            },
            /**
             * 合并增量数据入库
             */
            merge(row) {
                const ids = row.id || this.ids;
                merge(ids).then(response => {
                    this.$modal.msgSuccess("合并成功");
                }).catch((err) => {
                    this.$modal.msgError(err);
                });
            },
            handleNodeClick(data) {
                console.log(data);
            },
            onTreeClick(id) {
                this.queryParams.categoryId = id;
                this.handleQuery();
            },
            /**
             * [{
             * "id": "1",
             * "name": "关联文件",
             * "folder": "increment/关联文件",
             * "categoryOrder": 0
             * },
             * ...]
             */
            getCategory() {
                return new Promise((resolve, reject) => {
                    listAllCategory(this.queryParams).then(response => {
                        this.optionOfCategory = makeTree2(response.data);
                        console.log("this.optionOfCategory => " + this.optionOfCategory)
                        resolve(response);
                    }).catch((err) => {
                        reject(err);
                    });
                })
            },
            // 取值
            getCategoryPickedValue(value) {
                this.form.categoryId = parseInt(value);
                console.log(this.form.categoryId);
            }
        }
    };
</script>

<style scoped>
    /deep/ .el-textarea__inner {
        height: 150px;
    }

</style>
