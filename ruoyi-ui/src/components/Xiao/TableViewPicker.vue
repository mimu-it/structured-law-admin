<template>
  <div>
    <el-popover ref="popoverRef"
                placement="right"
                :popper-options="{boundariesElement: 'viewport'}" popper-class="overflow-auto"
                :width="width"
                trigger="manual"
                v-model="visible">
      <div>
        <el-form :model="queryParams" ref="formSearch" size="small" :inline="true">
          <el-form-item :label="item.label" v-for="item in fieldsCopy">
            <el-input v-model="queryParams[item.param]" clearable></el-input>
          </el-form-item>

          <el-form-item style="margin-bottom: 0">
            <el-button type="primary" @click="getList" icon="el-icon-search">查询</el-button>
          </el-form-item>
        </el-form>
      </div>
      <div>
        <el-table v-loading="loading" :data="lawList">
          <el-table-column label="id" align="center" width="80" >
            <template slot-scope="scope">
              <el-radio v-model="idPicked" :label="scope.row.id" @input="handleSelectionChange"></el-radio>
            </template>
          </el-table-column>

          <el-table-column label="法律名称" align="left" prop="name" width="200"/>
          <el-table-column label="类型" align="center" prop="lawType"/>
          <el-table-column label="公布日期" align="center" prop="publish" width="100">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.publish, '{y}-{m}-{d}') }}</span>
            </template>
          </el-table-column>
          <el-table-column label="是否过期" align="center" prop="expired"/>
          <el-table-column label="子标题" align="center" prop="subtitle" width="200"/>
          <el-table-column label="生效日期" align="center" prop="validFrom" width="100">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.validFrom, '{y}-{m}-{d}') }}</span>
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
      </div>
      <div class="footer">
        <div>
          <el-button @click="clear" size="small">清除</el-button>
        </div>
        <div>
          <el-button @click="cancel" size="small">取消</el-button>
        </div>
      </div>
      <el-button slot="reference" @click="show()">{{valueShow}}</el-button>
    </el-popover>
  </div>

</template>
<script>
  import Util from '@/utils/util.js';
  import {listLaw} from "@/api/structured-law/law";

  export default {
    name: 'TableViewPicker',
    components: {

    },
    props: {
      width: {
        type: Number,
        default() {
          return 600;
        }
      },
      /** 这里保存了id值 */
      value: {
        type: Number
      },
      /** id值对应的可读内容，用于编辑时给的默认值 */
      defaultLabel: {
        type: String,
        default() {
          return "";
        }
      },
      labelKeys: {
        type: Array,
        default() {
          return [];
        }
      },
      searchItems: {
        type: Array,
        default() {
          return [];
        }
      },
      columnsDef: {
        type: Array,
        default() {
          return [];
        }
      },
      fetch: {
        type: Function
      },
      disabled: {
        type: Boolean,
        default() {
          return false;
        }
      },
    },
    mounted() {
      this.$nextTick(() => {
        /** 新增时必须加这个，不然显示空白 */
        this.valueShow = this.makeValue(this.idPicked);
      });
    },
    watch: {
      /**
       * 打开页面的时候才从传入的value中读取值并给 this.idPicked
       * 内环境的 idPicked 不应该随时根据 prop 的 value 进行变化
       */
      visible(newVal) {
        if (newVal) {
          console.log("new.idPicked: " + newVal);
          this.idPicked = this.value;
          let allFieldsKv = JSON.parse(JSON.stringify(this.searchItems));
          this.fieldsCopy = allFieldsKv;
          this.getList();
        } else {
          /** 关掉之后应该把 this.rowPickedTemp 清空，不然会一直保存这个状态，从而影响 valueShow的显示*/
          this.rowPickedTemp = null;
        }
      },
      /**
       * 首次创建组件时，有可能还未获取数据，那么就是空白
       * 如果数据到达，这里观测到之后，应及时更新值
       * value的值只能通过emit来更新，不要在内部更新
       */
      value(newVal) {
        this.idPicked = newVal;
        this.valueShow = this.makeValue(newVal);
      },
    },
    computed: {},
    data() {
      return {
        visible: false,
        fieldsCopy: [],
        lawList: [],
        // 总条数
        total: 0,
        // 查询参数
        queryParams: {
          pageNum: 1,
          pageSize: 10,
          name: null
        },
        formSearch: {
          page_size: 5
        },
        /** 数据表是否显示正在加载中的提示 */
        loading: false,
        /** 目前选中的id值 */
        idPicked: null,
        /** 目前选中的行数据 */
        rowPicked: null,
        rowPickedTemp: null,
        valueShow: ""
      }
    },
    methods: {
      // 选中数据
      handleSelectionChange(selection) {
        //console.log(selection)
        //console.log(this.rowPicked)
        this.confirm();
      },
      getList() {
        this.loading = true;
        listLaw(this.queryParams).then(response => {
          this.lawList = response.rows;
          this.total = response.total;
          this.loading = false;
        });
      },
      /** recover 必须等待表数据加载完之后才能设置 highlightRowId */
      recover() {
        this.highlightRowId = this.idPicked;
      },
      /**
       * 按钮的显示内容
       * 编辑时候 依赖 idPicked + defaultLabel
       *         this.rowPicked = null
       * 选择指定项后 依赖  idPicked 及内部的 valueShow( makeValue()生成 )
       */
      makeValue(id) {
        /** 如果没有设置被选中的id，就默认显示未选择 */
        if (Util.isBlank(id)) {
          console.log('message.term.unselected');
          return "未选择";
        }

        this.rowPicked = this.lawList.find((item) => {
          return item.id === id;
        })

        /** 如果没有行数据，则没办法解析idPicked为可读值，所以采用默认值，这个默认值需要外部设置
         *  关掉之后应该把 this.rowPicked 清空，不然会一直保存这个状态，从而影响 valueShow的显示
         * */
        if (Util.isBlank(this.rowPicked)) {
          return this.defaultLabel;
        }

        /**
         * 如果此时更换的待编辑记录，则要 this.rowPicked 是没有用的
         */
        if (this.rowPicked.id != id) {
          this.rowPicked = null;
          return this.defaultLabel;
        }

        let labelShow = "";
        /** 如果有行数据，则可以根据 idPicked，匹配对应行，从而自定义显示哪些可读性内容 */
        for (let key of this.labelKeys) {
          labelShow += "-" + this.rowPicked[key];
        }

        /** 如果有行数据，idPicked匹配了对应行，可读性内容仍为空，则默认使用 name */
        if (Util.isBlank(labelShow)) {
          /** rowPicked 不是外部传入的 */
          labelShow = this.rowPicked.name;
        } else {
          labelShow = labelShow.substring(1);
        }

        return labelShow;
      },
      /** 清除选择 */
      clear() {
        this.$emit("input", null);
        this.rowPicked = null;
        let expectValueShow = this.makeValue(null);
        this.$emit("change", {id: null, label: expectValueShow});
        this.visible = false;
      },
      /** 取消 不改变什么 */
      cancel() {
        /** this.highlightRowId = null 是必须的，不然下次再次打开选择框，不会触发高亮，因为没有变化 */
        this.highlightRowId = null;
        this.visible = false;
      },
      /** 确认 */
      confirm() {
        this.$emit("input", this.idPicked);
        let expectValueShow = this.makeValue(this.idPicked);
        this.$emit("change", {id: this.rowPicked, label: expectValueShow});
        this.visible = false;
      },
      show() {
        if (this.disabled !== true) {
          this.visible = true;
        }
      }
    }
  }
</script>
<style scoped>
  .footer {
    margin-top: 30px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
</style>
<style>
  .overflow-auto {
    height: 420px;
    overflow: auto;
  }
</style>
